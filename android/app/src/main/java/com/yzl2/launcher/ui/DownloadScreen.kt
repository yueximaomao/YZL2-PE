package com.yzl2.launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.yzl2.launcher.data.RemoteVersion

@Composable
fun DownloadScreen(vm: AppViewModel) {
    var filter by remember { mutableStateOf("全部") }
    var query by remember { mutableStateOf("") }
    val filtered = remember(vm.manifest, filter, query) {
        vm.manifest.filter { v ->
            val typeOk = when (filter) {
                "正式版" -> v.isRelease
                "快照" -> v.isSnapshot
                "旧版" -> v.isOld
                else -> true
            }
            typeOk && (query.isBlank() || v.id.contains(query, true))
        }
    }
    Column(Modifier.fillMaxSize()) {
        ScreenTitle("下载游戏", "来自 Mojang 官方源 · 安装时自动补全依赖并准备 ZL2 内核") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${vm.manifest.size} 个官方版本", fontSize = 11.5.sp, color = Yzl.TextMuted)
                Spacer(Modifier.width(6.dp))
                IconAction(Icons.Filled.Refresh, "刷新", { vm.loadManifest(true) })
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("全部", "正式版", "快照", "旧版").forEach { tab ->
                    YzlTab(tab, filter == tab) { filter = tab }
                }
            }
            Spacer(Modifier.weight(1f))
            YzlField(
                query, { query = it }, "搜索版本号…",
                Modifier.width(250.dp), height = 36.dp, fontSize = 12.sp,
            )
        }
        Spacer(Modifier.height(12.dp))
        CardBox(Modifier.weight(1f).fillMaxWidth(), corner = 16.dp) {
            Column(Modifier.fillMaxSize()) {
                Row(
                    Modifier.fillMaxWidth().height(42.dp).padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("版本", fontSize = 11.5.sp, color = Yzl.TextMuted, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                    Text("类型", fontSize = 11.5.sp, color = Yzl.TextMuted, fontWeight = FontWeight.Medium, modifier = Modifier.width(80.dp))
                    Text("发布时间", fontSize = 11.5.sp, color = Yzl.TextMuted, fontWeight = FontWeight.Medium, modifier = Modifier.width(120.dp))
                    Text("操作", fontSize = 11.5.sp, color = Yzl.TextMuted, fontWeight = FontWeight.Medium, modifier = Modifier.width(80.dp))
                }
                DividerLine()
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    when {
                        vm.manifestLoading && vm.manifest.isEmpty() -> {
                            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                SpinningRing(30.dp)
                                Spacer(Modifier.height(12.dp))
                                Text("正在获取 Mojang 版本清单…", fontSize = 12.sp, color = Yzl.TextSecondary)
                            }
                        }
                        vm.manifestError != null && vm.manifest.isEmpty() -> {
                            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Text("版本清单加载失败", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Yzl.TextPrimary)
                                Spacer(Modifier.height(6.dp))
                                Text(vm.manifestError ?: "", fontSize = 11.5.sp, color = Yzl.Danger)
                                Spacer(Modifier.height(12.dp))
                                YzlButton("重试", { vm.loadManifest(true) })
                            }
                        }
                        filtered.isEmpty() -> {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                EmptyState(
                                    icon = { Icon(Glyph.Download, null, tint = Yzl.TextMuted, modifier = Modifier.size(24.dp)) },
                                    title = "没有找到版本",
                                    hint = "换个关键词，或切换上方分类试试",
                                )
                            }
                        }
                        else -> {
                            LazyColumn(Modifier.fillMaxSize()) {
                                items(filtered, key = { it.id }) { version ->
                                    DownloadRow(vm, version)
                                }
                            }
                        }
                    }
                }
                if (vm.installState != null) {
                    val st = vm.installState!!
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("${st.stage} · ${st.versionId}", fontSize = 11.sp, color = Yzl.TextSecondary)
                            Spacer(Modifier.height(6.dp))
                            LinearBar(st.fraction)
                        }
                        Spacer(Modifier.width(12.dp))
                        Text("${(st.fraction * 100).toInt()}%", fontSize = 11.5.sp, color = Yzl.Accent)
                    }
                }
                DividerLine()
                Text(
                    "版本清单来自 Mojang · 安装过程会自动补全依赖库与资源文件，并在未安装内核时自动下载 ZL2 内核（自带 Java）",
                    fontSize = 11.sp,
                    color = Yzl.TextMuted,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                )
            }
        }
    }
}

@Composable
private fun DownloadRow(vm: AppViewModel, version: RemoteVersion) {
    Column {
    Row(
        Modifier
            .fillMaxWidth()
            .height(62.dp)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(30.dp).clip(CircleShape).background(Yzl.AccentSoft),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Glyph.Download, null, tint = Yzl.Accent, modifier = Modifier.size(15.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(version.id, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Yzl.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(3.dp))
            Text("Minecraft 官方版本", fontSize = 10.5.sp, color = Yzl.TextMuted)
        }
        Box(Modifier.width(80.dp)) {
            if (version.isRelease) Badge("正式版", bg = Color(0xFFE8F6EE), fg = Color(0xFF2F8F5B))
            else if (version.isSnapshot) Badge("快照", bg = Color(0xFFF1EAFE), fg = Color(0xFF7C5BD0))
            else Badge("旧版", bg = Color(0xFFF3F5F9), fg = Color(0xFF7A879B))
        }
        Text(version.releaseTime.take(10), fontSize = 11.5.sp, color = Yzl.TextSecondary, modifier = Modifier.width(120.dp))
        Box(Modifier.width(80.dp)) {
            YzlButton(
                if (vm.installState?.versionId == version.id && vm.installState?.finished == false) "安装中" else "安装",
                { vm.install(version) },
                height = 32.dp, fontSize = 12.sp,
                enabled = vm.installState?.finished != false,
            )
        }
    }
    DividerLine()
    }
}
