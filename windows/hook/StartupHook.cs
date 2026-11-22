using System;
using System.Collections;
using System.Diagnostics;
using System.IO;
using System.Reflection;
using System.Text;

// Startup hook for the YZL2 launcher (DOTNET_STARTUP_HOOKS).
//
// Why this exists:
//   ContentView.xaml (inside YZL2.dll) uses {StaticResource AccentSoft} inside a
//   ComboBoxItem ControlTemplate trigger, but Themes/Theme.xaml only defines
//   "AccentSoftBrush". BAML defers that lookup, so it resolves to
//   DependencyProperty.UnsetValue; as soon as a dropdown item is highlighted,
//   Border.OnRender throws InvalidOperationException ("...不是属性“Background”的有效值").
//
// What this hook does:
//   Registers an "AccentSoft" brush in Application.Resources before any view is
//   created, so the broken lookup succeeds and the UI works normally.
//
// It only activates in the YZL2 process and uses reflection (no WPF references),
// so inheriting DOTNET_STARTUP_HOOKS by unrelated child .NET processes is harmless.

public static class StartupHook
{
    private static readonly string LogPath = Path.Combine(Path.GetTempPath(), "yzl2_hook.log");
    private static bool _scheduledIdleInjection;
    private static readonly byte R = 0xEA, G = 0xF3, B = 0xFF; // matches AccentSoftBrush

    public static void Initialize()
    {
        try
        {
            string processName = Process.GetCurrentProcess().ProcessName;
            if (processName.IndexOf("YZL2", StringComparison.OrdinalIgnoreCase) < 0)
            {
                return;
            }

            try { File.WriteAllText(LogPath, ""); } catch { }
            Log("hook active for '" + processName + "' (pid " + Environment.ProcessId + ")");

            Schedule(Setup, "Send");
            Schedule(Inject, "ApplicationIdle");
        }
        catch
        {
            // Never break the host process.
        }
    }

    private static void Schedule(Action action, string priorityName)
    {
        var windowsBase = Assembly.Load("WindowsBase");
        var dispatcherType = windowsBase.GetType("System.Windows.Threading.Dispatcher", true);
        var current = dispatcherType.GetProperty("CurrentDispatcher", BindingFlags.Public | BindingFlags.Static).GetValue(null, null);
        var priorityType = windowsBase.GetType("System.Windows.Threading.DispatcherPriority", true);
        var priority = Enum.Parse(priorityType, priorityName);
        var beginInvoke = dispatcherType.GetMethod("BeginInvoke", new[] { priorityType, typeof(Delegate) });
        beginInvoke.Invoke(current, new object[] { priority, action });
    }

    private static void Setup()
    {
        try
        {
            var app = GetApplication();
            if (app == null)
            {
                if (!_scheduledIdleInjection)
                {
                    _scheduledIdleInjection = true;
                    Schedule(Setup, "Normal");
                }
                return;
            }

            Log("Application found; injecting");
            Inject();
            Schedule(Inject, "ApplicationIdle");
        }
        catch (Exception ex)
        {
            Log("Setup failed: " + ex.Message);
        }
    }

    private static object GetApplication()
    {
        var presentationFramework = Assembly.Load("PresentationFramework");
        var appType = presentationFramework.GetType("System.Windows.Application", true);
        return appType.GetProperty("Current", BindingFlags.Public | BindingFlags.Static).GetValue(null, null);
    }

    private static void Inject()
    {
        try
        {
            var app = GetApplication();
            if (app == null) return;

            var resources = app.GetType().GetProperty("Resources").GetValue(app, null) as IDictionary;
            if (resources == null) return;
            if (resources.Contains("AccentSoft")) return;

            resources["AccentSoft"] = CreateBrush();
            Log("resource 'AccentSoft' added to Application.Resources");
        }
        catch (Exception ex)
        {
            Log("Inject failed: " + ex.Message);
        }
    }

    private static object CreateBrush()
    {
        var presentationCore = Assembly.Load("PresentationCore");
        var colorType = presentationCore.GetType("System.Windows.Media.Color", true);
        var fromRgb = colorType.GetMethod("FromRgb", new[] { typeof(byte), typeof(byte), typeof(byte) });
        var color = fromRgb.Invoke(null, new object[] { R, G, B });
        var brushType = presentationCore.GetType("System.Windows.Media.SolidColorBrush", true);
        var brush = Activator.CreateInstance(brushType, new object[] { color });
        var freeze = brushType.GetMethod("Freeze", Type.EmptyTypes);
        if (freeze != null) freeze.Invoke(brush, null);
        return brush;
    }

    private static void Log(string message)
    {
        try
        {
            File.AppendAllText(LogPath, "[" + DateTime.Now.ToString("HH:mm:ss.fff") + "] " + message + Environment.NewLine, Encoding.UTF8);
        }
        catch { }
    }
}
