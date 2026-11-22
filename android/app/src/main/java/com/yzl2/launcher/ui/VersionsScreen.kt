package com.yzl2.launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yzl2.launcher.AppViewModel
import com.yzl2.launcher.data.InstalledVersion
import com.yzl2.launcher.data.relativeTime

@Composable
fun VersionsScreen(vm: AppViewModel) {
    var query by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize()) {
        ScreenTitle("版本管理", "${vm.versions.size} 个版本 · ${vm.gameDirPath()}") {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                YzlField(
                    query, { query = it }, "搜索版本…",
                    Modifier.width(210.dp), height = 36.dp, fontSize = 12.sp,
                )
                IconAction(Icons.Filled.Refresh, "刷新", { vm.rescan() }, size = 34.dp)
                YzlButton("导入整合包", { vm.showToast("整合包导入将在后续版本提供") }, height = 36.dp, fontSize = 12.sp)
                YzlButton("导出整合包", { vm.showToast("已准备导出配置") }, height = 36.dp, fontSize = 12.sp)
                YzlButton("安装版本", { vm.selectPage(com.yzl2.launcher.data.Page.DOWNLOAD) }, height = 36.dp, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(14.dp))
        BoxWithConstraints(Modifier.weight(1f)) {
            val listWidth = if (maxWidth < 820.dp) 300.dp else 380.dp
            Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                CardBox(Modifier.width(listWidth).fillMaxHeight(), corner = 16.dp) {
                    val filtered = remember(vm.versions, query) {
                        if (query.isBlank()) vm.versions else vm.versions.filter { it.id.contains(query, true) || it.clientVersion.contains(query, true) }
                    }
                    if (filtered.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            EmptyState(
                                icon = { Icon(Glyph.Box, null, tint = Yzl.TextMuted, modifier = Modifier.size(24.dp)) },
                                title = if (vm.versions.isEmpty()) "还没有安装版本" else "没有匹配的版本",
                                hint = if (vm.versions.isEmpty()) "去「下载」页挑一个版本安装吧" else "换个关键词试试",
                            )
                        }
                    } else {
                        LazyColumn(Modifier.fillMaxSize().padding(10.dp)) {
                            items(filtered, key = { it.id }) { version ->
                                VersionRow(vm, version) { vm.selectVersion(version.id) }
                            }
                        }
                    }
                }
                CardBox(Modifier.weight(1f).fillMaxHeight(), corner = 16.dp) {
                    VersionDetail(vm)
                }
            }
        }
    }
}

@Composable
private fun VersionRow(vm: AppViewModel, version: InstalledVersion, onClick: () -> Unit) {
    val selected = vm.selectedVersion?.id == version.id
    Row(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 7.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Yzl.AccentSoft else Color.Transparent)
            .border(1.dp, if (selected) Color(0xFFC8DDFA) else Color.Transparent, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        McCube(34.dp, 11.sp)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(version.id, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = Yzl.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(3.dp))
            Text("${version.clientVersion} · ${version.loader.label}", fontSize = 11.5.sp, color = Yzl.TextSecondary, maxLines = 1)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(version.lastPlayed.relativeTime(), fontSize = 10.5.sp, color = Yzl.TextMuted)
            if (version.hasIsolation) {
                Spacer(Modifier.height(3.dp))
                Text("隔离", fontSize = 10.5.sp, color = Yzl.Accent)
            }
        }
    }
}

@Composable
fun McCube(size: androidx.compose.ui.unit.Dp, fontSize: androidx.compose.ui.unit.TextUnit) {
    Box(
        Modifier.size(size).clip(RoundedCornerShape(size * 0.28f)).background(Color(0xFF5FA86B)),
        contentAlignment = Alignment.Center,
    ) {
        Text("MC", color = Color.White, fontSize = fontSize, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun VersionDetail(vm: AppViewModel) {
    val version = vm.selectedVersion
    if (version == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            EmptyState(
                icon = { Icon(Glyph.Box, null, tint = Yzl.TextMuted, modifier = Modifier.size(24.dp)) },
                title = "选择一个版本",
                hint = "左侧列表里还没有版本，先去「下载」页安装一个吧",
            )
        }
        return
    }
    Column(Modifier.fillMaxSize()) {
        // 顶部渐变头图
        Box(Modifier.fillMaxWidth().height(104.dp)) {
            AssetImage("hero.png", Modifier.fillMaxSize())
            Box(Modifier.fillMaxSize().background(Color(0x66FFFFFF)))
            Row(Modifier.fillMaxSize().padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
                McCube(60.dp, 20.sp)
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(version.id, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Yzl.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(3.dp))
                    Text("${version.clientVersion} · ${version.typeLabel}", fontSize = 11.5.sp, color = Yzl.TextSecondary)
                }
                Badge(version.typeLabel, bg = Color(0xFFE8F6EE), fg = Color(0xFF2F8F5B))
            }
        }
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FlatTile("游戏版本", version.clientVersion, Modifier.weight(1f))
                FlatTile("加载器", version.loader.label, Modifier.weight(1f))
                FlatTile("运行环境", "Android 内置", Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FlatTile("内容", "${version.mods} 个 Mod", Modifier.weight(1f), icon = Glyph.Box)
                FlatTile("上次启动", version.lastPlayed.relativeTime(), Modifier.weight(1f), icon = Glyph.Clock)
            }
            Spacer(Modifier.height(18.dp))
            SectionLabel("版本位置")
            Spacer(Modifier.height(7.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(9.dp))
                    .background(Color(0xFFF7FAFE))
                    .border(1.dp, Color(0xFFE9F0F9), RoundedCornerShape(9.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Glyph.Folder, null, tint = Yzl.TextMuted, modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(8.dp))
                Text(version.path.absolutePath, fontSize = 11.5.sp, color = Yzl.TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Text("复制", fontSize = 10.5.sp, color = Yzl.Accent, modifier = Modifier.clickable { vm.showToast("路径已复制") })
            }
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF1F7FF))
                    .border(1.dp, Color(0xFFD5E6FB), RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 11.dp),
            ) {
                Icon(Icons.Filled.Info, null, tint = Yzl.Accent, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(8.dp))
                Text("启动前会自动检查依赖库与资源索引；如果版本文件不完整，YZL2 会给出提示。", fontSize = 11.5.sp, color = Yzl.TextSecondary)
            }
            Spacer(Modifier.height(22.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                YzlButton("启动游戏", { vm.launch() }, style = YzlButtonStyle.Primary, height = 44.dp, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, leading = {
                    Icon(Glyph.Play, null, tint = Color.White, modifier = Modifier.size(12.dp))
                })
                YzlButton("打开目录", { vm.openGameFolder() }, height = 44.dp, fontSize = 12.5.sp)
                YzlButton("刷新版本", { vm.rescan() }, style = YzlButtonStyle.Ghost, height = 44.dp, fontSize = 12.5.sp)
            }
        }
    }
}

@Composable
private fun FlatTile(label: String, value: String, modifier: Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector? = null) {
    Column(
        modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF7FAFE))
            .border(1.dp, Color(0xFFE9F0F9), RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(icon, null, tint = Yzl.TextMuted, modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(5.dp))
            }
            Text(label, fontSize = 10.5.sp, color = Yzl.TextMuted)
        }
        Spacer(Modifier.height(4.dp))
        Text(value, fontSize = 12.5.sp, fontWeight = FontWeight.Medium, color = Yzl.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
