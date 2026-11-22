using System;
using System.Diagnostics;
using System.IO;
using System.Runtime.InteropServices;
using System.Text;

namespace Yzl2Fix
{
    // Starts the YZL2 launcher with DOTNET_STARTUP_HOOKS pointing at YZL2Hook.dll,
    // which supplies the "AccentSoft" resource the app forgets to define.
    internal static class Program
    {
        [DllImport("user32.dll", CharSet = CharSet.Unicode)]
        private static extern int MessageBoxW(IntPtr hWnd, string text, string caption, uint type);

        private const uint MB_ICONWARNING = 0x00000030;
        private const uint MB_ICONERROR = 0x00000010;

        [STAThread]
        private static void Main()
        {
            try
            {
                string baseDir = AppContext.BaseDirectory.TrimEnd(Path.DirectorySeparatorChar);
                string hook = Path.Combine(baseDir, "YZL2Hook.dll");
                string targetFile = Path.Combine(baseDir, "target.txt");

                if (!File.Exists(hook))
                {
                    MessageBoxW(IntPtr.Zero, "缺少组件 YZL2Hook.dll，请将它与本程序放在同一文件夹。", "YZL2 修复启动器", MB_ICONERROR);
                    return;
                }

                string target = "";
                if (File.Exists(targetFile))
                {
                    target = File.ReadAllText(targetFile, Encoding.UTF8).Trim().Trim('"', '\uFEFF');
                }

                if (string.IsNullOrWhiteSpace(target) || !File.Exists(target))
                {
                    MessageBoxW(IntPtr.Zero,
                        "找不到 YZL2 启动器：\r\n" + (string.IsNullOrWhiteSpace(target) ? "（target.txt 为空）" : target) +
                        "\r\n\r\n请用记事本打开同目录下的 target.txt，写入 YZL2 启动器的完整路径后重试。",
                        "YZL2 修复启动器", MB_ICONWARNING);
                    return;
                }

                Environment.SetEnvironmentVariable("DOTNET_STARTUP_HOOKS", hook);

                var psi = new ProcessStartInfo
                {
                    FileName = target,
                    WorkingDirectory = Path.GetDirectoryName(target),
                    UseShellExecute = false,
                };
                Process.Start(psi);
            }
            catch (Exception ex)
            {
                MessageBoxW(IntPtr.Zero, "启动失败：\r\n" + ex.Message, "YZL2 修复启动器", MB_ICONERROR);
            }
        }
    }
}
