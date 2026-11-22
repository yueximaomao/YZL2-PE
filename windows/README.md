# Windows · YZL2 PE 联机版（修复启动器）

原版 YZL2 启动器有一个 WPF 资源键笔误：`ContentView.xaml` 的 `ComboBoxItem` 控件模板触发器引用
`{StaticResource AccentSoft}`，而 `Themes/Theme.xaml` 只定义了 `AccentSoftBrush`。BAML 里
`StaticResource` 是延迟解析的，键不存在时**加载期不报错**，只在真正渲染时解析成
`DependencyProperty.UnsetValue`，于是下拉框条目一高亮，`Border.OnRender` 就抛：

```
System.InvalidOperationException: “DependencyProperty.UnsetValue”不是属性“Background”的有效值。
   在 System.Windows.Controls.Border.OnRender(DrawingContext dc)
```

本目录用**零侵入**的方式修掉它：不重打包、不改原文件、不反编译注入，只在目标进程启动时补一个资源键。

## 组成

| 文件 | 作用 |
| --- | --- |
| `launcher/` | `YZL2 PE联机版.exe`：读 `target.txt` → 设置 `DOTNET_STARTUP_HOOKS` → 以原工作目录拉起原程序 |
| `hook/` | `YZL2Hook.dll`：Startup Hook，在 `Application` 创建后向 `Application.Resources` 注入 `AccentSoft` 画刷 |
| `target.txt` | 原版 YZL2 启动器的完整路径（一行，UTF-8） |

## 运行流程

```
YZL2 PE联机版.exe
  ├─ 读 target.txt → 校验目标存在
  ├─ 设置环境变量 DOTNET_STARTUP_HOOKS=<同目录>\YZL2Hook.dll
  └─ Process.Start(目标, WorkingDirectory=目标所在目录)
        └─ 原程序启动 → coreclr 加载 StartupHook.Initialize()
              ├─ 进程名不含 "YZL2" → 直接返回（不影响其它 .NET 程序）
              └─ Dispatcher.BeginInvoke(Send / ApplicationIdle)
                    └─ Application.Resources["AccentSoft"] = SolidColorBrush(#EAF3FF)
```

钩子用反射访问 `System.Windows.Application` / `PresentationCore`，自身不引用 WPF，因此
任何子 .NET 进程继承 `DOTNET_STARTUP_HOOKS` 都不会出错；异常全部被吞掉，绝不拖垮宿主。

## 使用

1. 解压发行包到任意目录（`YZL2 PE联机版.exe` 与 `YZL2Hook.dll` 必须同目录）。
2. 编辑同目录 `target.txt`：
   ```
   D:\Games\YZL2\YZL2.exe
   ```
3. 双击 `YZL2 PE联机版.exe`。
4. 验证：下拉框条目悬停高亮不再崩溃；`%TEMP%\yzl2_hook.log` 中出现
   `resource 'AccentSoft' added to Application.Resources`。

## 版本说明

| 发行包 | 大小 | 运行时要求 |
| --- | --- | --- |
| `YZL2-PE-Launcher-1.0.0-win-x64.zip` | ≈ 0.44 MB | 需安装 .NET 10 **桌面**运行时 |
| `YZL2-PE-Launcher-1.0.0-win-x64-full.zip` | ≈ 31 MB | 无（自带运行时） |

## 构建

```powershell
dotnet build hook/hook.csproj -c Release

# 轻量版
dotnet publish launcher/launcher.csproj -c Release -r win-x64 -p:PublishSingleFile=true -o out

# 免运行时版
dotnet publish launcher/launcher.csproj -c Release -r win-x64 --self-contained true `
  -p:PublishSingleFile=true -p:IncludeNativeLibrariesForSelfExtract=true `
  -p:EnableCompressionInSingleFile=true -o out-full
```

> `hook` 必须与目标程序的运行时匹配（当前为 `net10.0-windows`）。原版 YZL2 是 .NET 10 的
> 单文件自包含程序，因此钩子目标框架保持 `net10.0-windows`。

## 注意

- 本目录**不包含**原版 YZL2 启动器，需要自备。
- 修复作用于运行时，删除本工具即完全还原，原程序文件不受影响。
