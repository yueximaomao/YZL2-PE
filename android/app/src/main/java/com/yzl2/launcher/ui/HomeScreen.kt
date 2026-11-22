package com.yzl2.launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yzl2.launcher.AppViewModel
import com.yzl2.launcher.data.clockText
import com.yzl2.launcher.data.dateText
import com.yzl2.launcher.data.dayPeriod
import com.yzl2.launcher.data.greeting
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(vm: AppViewModel) {
    var tick by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(20000)
            tick++
        }
    }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val compact = maxHeight < 560.dp
        val leftWidth = if (maxWidth < 780.dp) 310.dp else (maxWidth * 0.36f).coerceIn(370.dp, 430.dp)
        CardBox(Modifier.fillMaxSize(), corner = 18.dp, borderColor = Color(0xFFE3EBF5)) {
            Row(Modifier.fillMaxSize()) {
                HomeLeft(vm, compact, Modifier.width(leftWidth).fillMaxHeight())
                HeroPanel(vm, compact, Modifier.weight(1f).fillMaxHeight())
            }
        }
    }
}

@Composable
private fun HomeLeft(vm: AppViewModel, compact: Boolean, modifier: Modifier) {
    Column(
        modifier
            .verticalScroll(rememberScrollState())
            .padding(
                start = if (compact) 22.dp else 32.dp,
                end = if (compact) 22.dp else 32.dp,
                top = if (compact) 16.dp else 26.dp,
                bottom = if (compact) 14.dp else 24.dp,
            ),
    ) {
        // 账户
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(44.dp).clip(CircleShape).background(Yzl.AccentSoft),
                contentAlignment = Alignment.Center,
            ) {
                Text(vm.accountInitial(), color = Yzl.Accent, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(vm.account.name, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold, color = Yzl.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(5.dp).clip(CircleShape).background(Color(0xFF4CC38A)))
                    Spacer(Modifier.width(6.dp))
                    Text(vm.accountTypeLabel(), fontSize = 11.5.sp, color = Yzl.TextSecondary)
                }
            }
            YzlButton("切换", { vm.openAccountSheet() }, style = YzlButtonStyle.Ghost, height = 30.dp, fontSize = 12.sp)
        }

        Spacer(Modifier.height(18.dp))
        DividerLine()
        Spacer(Modifier.height(18.dp))

        SectionLabel("当 前 版 本")
        Spacer(Modifier.height(7.dp))
        Text(vm.versionTitle(), fontSize = 21.sp, fontWeight = FontWeight.Bold, color = Yzl.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            val loader = vm.selectedVersion?.loader
            val loaderColor = Color(loader?.color ?: 0xFF5FA86B)
            Box(Modifier.clip(RoundedCornerShape(6.dp)).background(loaderColor).padding(horizontal = 8.dp, vertical = 3.dp)) {
                Text(vm.versionLoaderLabel(), color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.width(7.dp))
            Badge(vm.versionTypeLabel(), bg = Color(0xFFF1F5FB), fg = Color(0xFF71839C))
            Spacer(Modifier.width(7.dp))
            Badge(vm.versionClient(), bg = Color(0xFFF1F5FB), fg = Color(0xFF71839C))
        }

        Spacer(Modifier.height(18.dp))
        Row {
            StatCell("${vm.modsCount()} 个 Mod", "MODS", Modifier.weight(1f))
            StatCell("${vm.savesCount()} 个存档", "存 档", Modifier.weight(1f))
            StatCell(vm.lastPlayedText(), "上 次 启 动", Modifier.weight(1f))
        }

        if (vm.versions.isEmpty()) {
            Spacer(Modifier.height(14.dp))
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFFF7FAFE)).padding(horizontal = 10.dp, vertical = 8.dp)) {
                Text("还没有找到版本，去「下载」页安装一个吧。", fontSize = 11.5.sp, color = Yzl.Warning)
            }
        }

        Spacer(Modifier.height(if (compact) 18.dp else 86.dp))
        SectionLabel("游 戏 目 录")
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                vm.gameDirPath(),
                fontSize = 11.5.sp,
                color = Yzl.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            YzlButton("打开", { vm.openGameFolder() }, style = YzlButtonStyle.Ghost, height = 26.dp, fontSize = 10.5.sp)
        }

        Spacer(Modifier.height(16.dp))
        LaunchButton(vm, compact)

        Spacer(Modifier.height(8.dp))
        Text(vm.launchHint(), fontSize = 11.5.sp, color = Yzl.TextMuted, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)

        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            QuickButton(Glyph.Grid, "版本", Modifier.weight(1f)) { vm.selectPage(com.yzl2.launcher.data.Page.VERSIONS) }
            QuickButton(androidx.compose.material.icons.Icons.Filled.Settings, "设置", Modifier.weight(1f)) { vm.selectPage(com.yzl2.launcher.data.Page.SETTINGS) }
            QuickButton(Glyph.Folder, "目录", Modifier.weight(1f)) { vm.openGameFolder() }
        }

        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(vm.javaSummary(), fontSize = 11.5.sp, color = Yzl.TextSecondary, maxLines = 1, modifier = Modifier.weight(1f))
            Box(Modifier.size(5.dp).clip(CircleShape).background(Color(0xFF4CC38A)))
            Spacer(Modifier.width(5.dp))
            Text("就绪", fontSize = 11.5.sp, color = Yzl.TextSecondary)
        }
    }
}

@Composable
private fun StatCell(value: String, label: String, modifier: Modifier) {
    Column(modifier) {
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Yzl.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(4.dp))
        SectionLabel(label)
    }
}

@Composable
private fun LaunchButton(vm: AppViewModel, compact: Boolean) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        Modifier
            .fillMaxWidth()
            .height(if (compact) 52.dp else 60.dp)
            .shadow(12.dp, shape, clip = false, ambientColor = Yzl.Accent.copy(alpha = 0.4f), spotColor = Yzl.Accent.copy(alpha = 0.5f))
            .clip(shape)
            .background(Yzl.primaryBrush())
            .clickable { vm.launch() },
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(30.dp).clip(CircleShape).background(Color(0x2EFFFFFF)), contentAlignment = Alignment.Center) {
                Icon(Glyph.Play, null, tint = Color.White, modifier = Modifier.size(13.dp))
            }
            Spacer(Modifier.width(11.dp))
            Text("启动游戏", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun QuickButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .height(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .border(1.dp, Yzl.BorderStrong, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Yzl.TextSecondary, modifier = Modifier.size(13.dp))
            Spacer(Modifier.width(6.dp))
            Text(label, fontSize = 12.sp, color = Yzl.TextPrimary)
        }
    }
}


@Composable
private fun HeroPanel(vm: AppViewModel, compact: Boolean, modifier: Modifier) {
    Box(modifier.clip(RoundedCornerShape(topEnd = 18.dp, bottomEnd = 18.dp))) {
        AssetImage("hero.png", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Yzl.ArtScrim))
        Box(Modifier.fillMaxSize().background(Color(0x0FFFFFFF)))

        // 星点
        Text("✦", color = Color(0xD9FFFFFF), fontSize = 20.sp, modifier = Modifier.offset(x = 520.dp, y = 110.dp))
        Text("✦", color = Color(0x99FFFFFF), fontSize = 12.sp, modifier = Modifier.offset(x = 570.dp, y = 20.dp))
        Text("✦", color = Color(0xB3FFFFFF), fontSize = 14.sp, modifier = Modifier.offset(x = 90.dp, y = 270.dp))
        Text("✦", color = Color(0x80FFFFFF), fontSize = 10.sp, modifier = Modifier.offset(x = 470.dp, y = 200.dp))

        // 左上角 chip
        Row(
            Modifier
                .padding(start = 26.dp, top = 24.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0x591E5FC4))
                .padding(horizontal = 13.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("✦", color = Color.White, fontSize = 11.sp)
            Spacer(Modifier.width(7.dp))
            Text(vm.chipText(), color = Color.White, fontSize = 11.5.sp)
        }

        // 右上角头像
        Column(
            Modifier.align(Alignment.TopEnd).padding(top = 22.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier
                    .size(88.dp)
                    .shadow(8.dp, RoundedCornerShape(24.dp), clip = false)
                    .clip(RoundedCornerShape(24.dp))
                    .border(3.dp, Color(0xE6FFFFFF), RoundedCornerShape(24.dp)),
            ) {
                AssetImage("art.png", Modifier.fillMaxSize())
            }
            Spacer(Modifier.height(7.dp))
            Text("YZL2", color = Color(0xD9FFFFFF), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        }

        // 左下角问候语
        Column(Modifier.align(Alignment.BottomStart).padding(start = 34.dp, end = 34.dp, bottom = 30.dp)) {
            Text(
                "${greeting()}，${vm.account.name}",
                color = Color.White,
                fontSize = if (compact) 26.sp else 31.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(9.dp))
            Text("打开游戏，去遇见今天的第一缕阳光。", color = Color(0xF2FFFFFF), fontSize = 13.sp)
            Spacer(Modifier.height(7.dp))
            Text(
                "${dateText()} · ${dayPeriod()} ${clockText()}",
                color = Color(0xD9FFFFFF),
                fontSize = 10.5.sp,
            )
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HeroBadge("Minecraft ${vm.versionClient()}")
                HeroBadge(vm.versionLoaderLabel())
                HeroBadge("YZL2 1.0")
            }
        }

        Text(
            "PLAIN CRAFT INSPIRED · ANDROID EDITION",
            color = Color(0x8CFFFFFF),
            fontSize = 8.sp,
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 26.dp, bottom = 14.dp),
        )
    }
}

@Composable
private fun HeroBadge(text: String) {
    Box(Modifier.clip(RoundedCornerShape(14.dp)).background(Color(0x38FFFFFF)).padding(horizontal = 12.dp, vertical = 7.dp)) {
        Text(text, color = Color.White, fontSize = 11.sp)
    }
}
