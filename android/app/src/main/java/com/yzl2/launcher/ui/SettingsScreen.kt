package com.yzl2.launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yzl2.launcher.AppViewModel

@Composable
fun SettingsScreen(vm: AppViewModel) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(
            Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SettingsCard(Glyph.Code, "Java 与内存", "为游戏选择合适的运行时与堆大小") {
                SectionLabel("Java 运行时")
                Spacer(Modifier.height(7.dp))
                val kernelNames = vm.installedKernelNames()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (kernelNames.isBlank()) "ZL2 内核 · 未安装" else "ZL2 内核 · 已就绪",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Yzl.TextPrimary,
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            if (kernelNames.isBlank()) "内核自带 Java 运行时，下载安装后即可启动游戏"
                            else "已安装：$kernelNames · Java 由内核提供",
                            fontSize = 10.5.sp,
                            color = Yzl.TextMuted,
                        )
                    }
                    YzlButton(
                        if (vm.kernelState?.finished == false) "下载中…" else "自动下载内核",
                        { vm.ensureKernel() },
                        height = 32.dp,
                        fontSize = 11.sp,
                        enabled = vm.kernelState?.finished != false,
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text("由 ZL2 / Pojav 内核提供 · 无需手动安装 Java", fontSize = 10.5.sp, color = Yzl.TextMuted)
                Spacer(Modifier.height(8.dp))
                SettingToggleRow(
                    "安装版本后自动准备内核",
                    "下载游戏版本时自动补齐 ZL2 内核（自带 Java）",
                    vm.config.autoKernel,
                ) { value -> vm.updateConfig { it.copy(autoKernel = value) } }
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("最大内存", fontSize = 12.5.sp, fontWeight = FontWeight.Medium, color = Yzl.TextPrimary)
                        Spacer(Modifier.height(3.dp))
                        Text("当前 ${vm.config.memoryMb} MB · 建议 2048 – 8192", fontSize = 10.5.sp, color = Yzl.TextMuted)
                    }
                    Stepper(vm.config.memoryMb, { vm.updateConfig { c -> c.copy(memoryMb = it) } })
                }
                Spacer(Modifier.height(16.dp))
                SectionLabel("额外 JVM 参数")
                Spacer(Modifier.height(7.dp))
                YzlField(
                    vm.config.extraJvmArgs,
                    { v -> vm.updateConfig { it.copy(extraJvmArgs = v) } },
                    "-XX:+UseG1GC …",
                    Modifier.fillMaxWidth(),
                )
            }
            SettingsCard(Glyph.Grid, "游戏窗口", "设置启动时的分辨率与显示模式") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("窗口尺寸", fontSize = 12.5.sp, fontWeight = FontWeight.Medium, color = Yzl.TextPrimary)
                        Spacer(Modifier.height(3.dp))
                        Text("仅对支持自定义分辨率的版本生效", fontSize = 10.5.sp, color = Yzl.TextMuted)
                    }
                    YzlField(
                        "${vm.config.windowWidth}", { v ->
                            v.toIntOrNull()?.let { n -> vm.updateConfig { c -> c.copy(windowWidth = n) } }
                        }, "1280", Modifier.width(88.dp), height = 34.dp, fontSize = 12.sp,
                    )
                    Text(" × ", fontSize = 12.sp, color = Yzl.TextMuted)
                    YzlField(
                        "${vm.config.windowHeight}", { v ->
                            v.toIntOrNull()?.let { n -> vm.updateConfig { c -> c.copy(windowHeight = n) } }
                        }, "720", Modifier.width(88.dp), height = 34.dp, fontSize = 12.sp,
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1280 to 720, 1600 to 900, 1920 to 1080).forEach { (w, h) ->
                        YzlButton("$w × $h", {
                            vm.updateConfig { it.copy(windowWidth = w, windowHeight = h) }
                        }, height = 28.dp, fontSize = 10.5.sp)
                    }
                }
                Spacer(Modifier.height(16.dp))
                SettingToggleRow("全屏启动", "以全屏模式打开游戏", vm.config.fullscreen) {
                    vm.updateConfig { c -> c.copy(fullscreen = it) }
                }
            }
        }
        Column(
            Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SettingsCard(Glyph.Users, "玩家账户", "离线模式 · 名称仅保存在本地") {
                SectionLabel("玩家名称")
                Spacer(Modifier.height(7.dp))
                YzlField(
                    vm.config.playerName,
                    { v -> vm.updateConfig { it.copy(playerName = v) } },
                    "Steve",
                    Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "离线账户可以正常游玩单人游戏与局域网联机；连接正版验证服务器时，需要使用正版账户登录。",
                    fontSize = 10.5.sp,
                    color = Yzl.TextMuted,
                    lineHeight = 15.sp,
                )
            }
            SettingsCard(Glyph.Sliders, "界面与体验", "让 YZL2 更符合你的使用习惯") {
                SettingToggleRow("页面切换动画", "在页面之间使用淡入与位移动画", vm.config.animationsEnabled) {
                    vm.updateConfig { c -> c.copy(animationsEnabled = it) }
                }
                DividerLine()
                SettingToggleRow("启动后最小化", "启动后自动返回桌面，让游戏占用前台", vm.config.minimizeOnLaunch) {
                    vm.updateConfig { c -> c.copy(minimizeOnLaunch = it) }
                }
                DividerLine()
                Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("当前主题", fontSize = 12.5.sp, fontWeight = FontWeight.Medium, color = Yzl.TextPrimary)
                        Spacer(Modifier.height(3.dp))
                        Text("冰蓝 · Ice Blue", fontSize = 10.5.sp, color = Yzl.TextMuted)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        listOf(Yzl.Accent, Color(0xFF5AA9E6), Color(0xFF9AC8F0)).forEachIndexed { i, c ->
                            Box(
                                Modifier
                                    .size(if (i == 0) 16.dp else 12.dp)
                                    .clip(CircleShape)
                                    .background(c),
                            )
                        }
                    }
                }
            }
            SettingsCard(Glyph.Image, "个性化", "自定义启动器背景、视频与背景音乐") {
                BackgroundPickerContent(vm)
            }
        }
    }
}

@Composable
private fun SettingsCard(
    icon: ImageVector,
    title: String,
    desc: String,
    content: @Composable () -> Unit,
) {
    CardBox(Modifier.fillMaxWidth(), corner = 16.dp) {
        Column(Modifier.padding(22.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(Yzl.AccentSoft),
                    contentAlignment = Alignment.Center,
                ) { Icon(icon, null, tint = Yzl.Accent, modifier = Modifier.size(18.dp)) }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(title, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold, color = Yzl.TextPrimary)
                    Spacer(Modifier.height(3.dp))
                    Text(desc, fontSize = 10.5.sp, color = Yzl.TextMuted)
                }
            }
            Spacer(Modifier.height(16.dp))
            DividerLine()
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun SettingToggleRow(title: String, desc: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 12.5.sp, fontWeight = FontWeight.Medium, color = Yzl.TextPrimary)
            Spacer(Modifier.height(3.dp))
            Text(desc, fontSize = 10.5.sp, color = Yzl.TextMuted)
        }
        YzlToggle(checked, onChange)
    }
}
