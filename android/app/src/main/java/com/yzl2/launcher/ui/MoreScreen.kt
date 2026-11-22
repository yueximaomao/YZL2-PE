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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yzl2.launcher.AppViewModel
import com.yzl2.launcher.data.Page

@Composable
fun MoreScreen(vm: AppViewModel) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(
            Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CardBox(Modifier.fillMaxWidth(), corner = 16.dp) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(Yzl.AccentSoft),
                            contentAlignment = Alignment.Center,
                        ) { Icon(Glyph.Grid, null, tint = Yzl.Accent, modifier = Modifier.size(18.dp)) }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("快捷入口", fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold, color = Yzl.TextPrimary)
                            Spacer(Modifier.height(3.dp))
                            Text("直接打开常用的游戏文件夹", fontSize = 10.5.sp, color = Yzl.TextMuted)
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    DividerLine()
                    Spacer(Modifier.height(14.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            ShortcutTile(Glyph.Folder, "游戏目录", Yzl.Accent, Modifier.weight(1f)) { vm.openGameFolder() }
                            ShortcutTile(Glyph.Box, "Mods", Color(0xFFE39A3B), Modifier.weight(1f)) { vm.openGameFolder() }
                            ShortcutTile(Glyph.Clock, "存档", Yzl.Success, Modifier.weight(1f)) { vm.openGameFolder() }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            ShortcutTile(Glyph.List, "日志", Color(0xFF8B7BD8), Modifier.weight(1f)) { vm.openLog() }
                            ShortcutTile(Glyph.Image, "截图", Color(0xFFD9789C), Modifier.weight(1f)) { vm.openGameFolder() }
                            ShortcutTile(Glyph.Package, "资源包", Color(0xFFC49A3A), Modifier.weight(1f)) { vm.openGameFolder() }
                        }
                    }
                }
            }

            CardBox(Modifier.fillMaxWidth(), corner = 16.dp) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFFFF6EA)),
                            contentAlignment = Alignment.Center,
                        ) { Icon(Glyph.Sliders, null, tint = Yzl.Warning, modifier = Modifier.size(18.dp)) }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("工具", fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold, color = Yzl.TextPrimary)
                            Spacer(Modifier.height(3.dp))
                            Text("维护版本与查看运行状态", fontSize = 10.5.sp, color = Yzl.TextMuted)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    ToolRow(Glyph.Clock, "重新扫描版本", "重新读取游戏目录中的版本列表") { vm.rescan() }
                    ToolRow(Glyph.List, "查看最近启动日志", "启动失败时，日志里会有更详细的信息") { vm.openLog() }
                    ToolRow(Glyph.Play, "结束正在运行的游戏", "游戏未运行") { vm.showToast("当前没有运行中的游戏") }
                }
            }

            CardBox(Modifier.fillMaxWidth(), corner = 16.dp) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Glyph.Sparkle, null, tint = Yzl.Accent, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "YZL2 启动诊断 · Microsoft 正版与 Authlib / LittleSkin 账户；遇到报错可以先查看自动诊断，或让离线助手帮你分析。",
                        fontSize = 10.5.sp,
                        color = Yzl.TextSecondary,
                        lineHeight = 15.sp,
                    )
                }
            }
        }

        Column(
            Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CardBox(
                Modifier.fillMaxWidth(),
                corner = 16.dp,
                color = Color(0xFFF3FBF7),
                borderColor = Color(0xFFD8F0E2),
            ) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFE2F6EC)),
                            contentAlignment = Alignment.Center,
                        ) { Icon(Glyph.Users, null, tint = Yzl.Success, modifier = Modifier.size(18.dp)) }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("联机", fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold, color = Yzl.TextPrimary)
                            Spacer(Modifier.height(3.dp))
                            Text("创建或加入房间，和朋友一起玩", fontSize = 10.5.sp, color = Yzl.TextMuted)
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    DividerLine()
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "使用 EasyTier 把不同地方的玩家组成虚拟局域网。房主在游戏里开启局域网，朋友输入邀请码即可加入。",
                        fontSize = 11.sp,
                        color = Yzl.TextSecondary,
                        lineHeight = 16.sp,
                    )
                    Spacer(Modifier.height(12.dp))
                    YzlButton("打开联机", { vm.openLink() }, style = YzlButtonStyle.Primary, height = 36.dp, fontSize = 12.5.sp)
                }
            }

            CardBox(Modifier.fillMaxWidth(), corner = 16.dp) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFEFEAFF)),
                            contentAlignment = Alignment.Center,
                        ) { Icon(Glyph.Sparkle, null, tint = Color(0xFF7C5BD0), modifier = Modifier.size(18.dp)) }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("AI 助手 · 柚子姐姐", fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold, color = Yzl.TextPrimary)
                            Spacer(Modifier.height(3.dp))
                            Text("离线诊断 · 不上传任何日志", fontSize = 10.5.sp, color = Yzl.TextMuted)
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    DividerLine()
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "根据启动日志中的关键字，帮你分析 Java、Mod、内存与崩溃问题。所有内容只在设备本地处理。",
                        fontSize = 11.sp,
                        color = Yzl.TextSecondary,
                        lineHeight = 16.sp,
                    )
                    Spacer(Modifier.height(12.dp))
                    YzlButton("打开 AI 助手", { vm.openAi() }, style = YzlButtonStyle.Primary, height = 36.dp, fontSize = 12.5.sp)
                }
            }

            CardBox(Modifier.fillMaxWidth(), corner = 16.dp) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(64.dp).clip(RoundedCornerShape(18.dp))) { AssetImage("art.png", Modifier.fillMaxSize()) }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text("YZL2", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Yzl.TextPrimary)
                            Spacer(Modifier.height(3.dp))
                            Text("M I N E C R A F T   L A U N C H E R", fontSize = 8.sp, color = Yzl.TextMuted)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "一个用 Jetpack Compose 重写的 Minecraft 启动器。界面结构借鉴 Plain Craft Launcher 2，沿用 YZL2 图标与冰蓝视觉。",
                        fontSize = 11.sp,
                        color = Yzl.TextSecondary,
                        lineHeight = 16.sp,
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Badge("Android ${com.yzl2.launcher.BuildConfig.VERSION_NAME}", bg = Color(0xFFF3F7FD), fg = Color(0xFF71839C))
                        Spacer(Modifier.weight(1f))
                        Text("横屏 · 沉浸式", fontSize = 10.sp, color = Yzl.TextMuted)
                    }
                }
            }
        }
    }
}

@Composable
private fun ShortcutTile(icon: ImageVector, label: String, tint: Color, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier
            .height(68.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, Yzl.Border, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
        Spacer(Modifier.height(6.dp))
        Text(label, fontSize = 11.sp, color = Yzl.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun ToolRow(icon: ImageVector, title: String, desc: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = Yzl.TextSecondary, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 12.5.sp, color = Yzl.TextPrimary)
            Spacer(Modifier.height(2.dp))
            Text(desc, fontSize = 10.5.sp, color = Yzl.TextMuted)
        }
        Text("›", fontSize = 16.sp, color = Yzl.TextMuted)
    }
}
