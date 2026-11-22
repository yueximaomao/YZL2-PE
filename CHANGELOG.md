# 更新日志

## v1.1.0 — 2026-11-22

同时发布 Android 端与 Windows 端的首个公开版本。

### Android 1.1.0

- 新增：安装版本后**自动准备 ZL2 内核**（按 CPU ABI 选择 arm64-v8a / armeabi-v7a / x86_64，官方源 + 镜像），并调起系统安装器。
- 新增：设置页「自动下载内核」按钮与内核状态显示。
- 新增：通过系统文件选择器指定**任意目录**的背景图片 / 视频 / 背景音乐。
- 新增：视频背景 `TextureView + MediaPlayer` 全屏循环播放，可开关循环与静音；背景音乐循环与音量。
- 修复（沿用 1.0.1）：BMCLAPI 回退路径拼错导致安装 404；搜索按钮点击区域；按钮涟漪反馈丢失；网络超时与重试。

### Windows 1.0.0

- 新增：`YZL2Hook.dll` Startup Hook，向 `Application.Resources` 注入缺失的 `AccentSoft` 画刷（`#EAF3FF`），消除下拉框高亮时 `Border.OnRender` 的 `InvalidOperationException`。
- 新增：`YZL2 PE联机版.exe` 启动器，从 `target.txt` 读取原程序路径并注入钩子后拉起，原程序零改动。
- 发布轻量版（0.6 MB，需 .NET 10 桌面运行时）与免运行时版（约 38 MB，自带运行时）。
