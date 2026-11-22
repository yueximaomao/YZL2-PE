package com.yzl2.launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.yzl2.launcher.data.MrHit
import com.yzl2.launcher.data.formatCount

@Composable
fun ContentScreen(vm: AppViewModel) {
    var query by remember { mutableStateOf(vm.contentQuery) }
    val gameVersions = remember(vm.manifest) {
        listOf("全部版本") + vm.manifest.filter { it.isRelease }.take(40).map { it.id }
    }

    Column(Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("资源下载", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Yzl.TextPrimary)
            Spacer(Modifier.width(10.dp))
            Badge("Modrinth", bg = Color(0xFFEFF6FF), fg = Color(0xFF4C86D6))
        }
        Spacer(Modifier.height(4.dp))
        Text("Mods · 整合包 · 数据包 · 资源包 · 光影包，搜索并安装到当前游戏目录", fontSize = 11.5.sp, color = Yzl.TextSecondary)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Mods" to "mod", "整合包" to "modpack", "数据包" to "datapack", "资源包" to "resourcepack", "光影包" to "shader").forEach { (label, type) ->
                YzlTab(label, vm.contentType == type) {
                    vm.changeContentType(type)
                    vm.searchContent()
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            YzlDropdown(gameVersions, vm.contentGameVersion, { vm.changeContentGameVersion(it); vm.searchContent() }, Modifier.width(150.dp), height = 34.dp, fontSize = 11.5.sp)
            YzlDropdown(listOf("全部加载器", "fabric", "forge", "neoforge", "quilt"), vm.contentLoader, { vm.changeContentLoader(it); vm.searchContent() }, Modifier.width(140.dp), height = 34.dp, fontSize = 11.5.sp)
            YzlDropdown(listOf("下载量", "最近更新", "关注度", "相关度"), vm.contentSort, { vm.changeContentSort(it); vm.searchContent() }, Modifier.width(120.dp), height = 34.dp, fontSize = 11.5.sp)
            YzlField(
                query, { query = it }, "搜索项目名称…",
                Modifier.weight(1f), height = 34.dp, fontSize = 12.sp,
            )
            YzlButton(
                if (vm.contentLoading) "搜索中…" else "搜索",
                {
                    vm.changeContentQuery(query)
                    vm.searchContent()
                },
                style = YzlButtonStyle.Primary,
                height = 34.dp,
                fontSize = 12.sp,
                enabled = !vm.contentLoading,
            )
        }
        Spacer(Modifier.height(12.dp))
        Box(Modifier.weight(1f).fillMaxWidth()) {
            when {
                vm.contentLoading -> Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    SpinningRing(28.dp)
                    Spacer(Modifier.height(12.dp))
                    Text("正在从 Modrinth 搜索…", fontSize = 12.sp, color = Yzl.TextSecondary)
                }
                vm.contentError != null -> Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text("搜索失败", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Yzl.TextPrimary)
                    Spacer(Modifier.height(6.dp))
                    Text(vm.contentError ?: "", fontSize = 11.5.sp, color = Yzl.Danger)
                    Spacer(Modifier.height(12.dp))
                    YzlButton("重试", { vm.searchContent() })
                }
                vm.contentResults.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = { Icon(Glyph.Box, null, tint = Yzl.TextMuted, modifier = Modifier.size(26.dp)) },
                        title = "没有找到匹配的内容",
                        hint = "换一个关键词，或把游戏版本切换到「全部版本」试试",
                    )
                }
                else -> LazyVerticalGrid(
                    columns = GridCells.Adaptive(320.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(vm.contentResults, key = { it.id }) { hit ->
                        ContentCard(hit) { vm.installContent(hit) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContentCard(hit: MrHit, onInstall: () -> Unit) {
    CardBox(Modifier.fillMaxWidth(), corner = 14.dp, shadow = 6.dp) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(Yzl.AccentSoft),
                    contentAlignment = Alignment.Center,
                ) {
                    AsyncImage(hit.iconUrl, Modifier.fillMaxSize()) {
                        Text(hit.title.take(1), color = Yzl.Accent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(hit.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Yzl.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(2.dp))
                    Text("by ${hit.author}", fontSize = 10.5.sp, color = Yzl.TextMuted, maxLines = 1)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                hit.description,
                fontSize = 11.5.sp,
                color = Yzl.TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp,
            )
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("下载 ${formatCount(hit.downloads)}", fontSize = 10.5.sp, color = Yzl.TextMuted)
                Spacer(Modifier.width(8.dp))
                hit.categories.take(2).forEach { c ->
                    Badge(c, bg = Color(0xFFF3F7FD), fg = Color(0xFF71839C), fontSize = 9.5.sp)
                    Spacer(Modifier.width(4.dp))
                }
                Spacer(Modifier.weight(1f))
                YzlButton("安装", onInstall, height = 30.dp, fontSize = 11.5.sp)
            }
        }
    }
}

