package com.yzl2.launcher.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yzl2.launcher.AppViewModel
import com.yzl2.launcher.data.Page
import com.yzl2.launcher.data.clockText
import kotlinx.coroutines.delay

@Composable
fun AppRoot(vm: AppViewModel, onExit: () -> Unit) {
    YzlTheme {
        val context = androidx.compose.ui.platform.LocalContext.current
        val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) vm.refreshKernelStatus()
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }
        LaunchedEffect(vm.pendingKernelApk) {
            val apk = vm.pendingKernelApk ?: return@LaunchedEffect
            runCatching { context.startActivity(com.yzl2.launcher.data.Kernel.install(context, apk)) }
                .onSuccess { vm.onKernelInstallerOpened() }
                .onFailure { vm.onKernelInstallFailed(it.message ?: "无法打开系统安装器") }
        }
        Box(Modifier.fillMaxSize().background(Yzl.WindowBg)) {
            LauncherBackground(
                path = vm.config.backgroundImage,
                videoLoop = vm.config.backgroundVideoLoop,
                videoMuted = vm.config.backgroundVideoMuted,
            )
            BackgroundMusicPlayer(
                path = vm.config.backgroundMusic,
                enabled = vm.config.musicAutoPlay,
                volume = vm.config.musicVolume,
            )
            Column(Modifier.fillMaxSize()) {
                HeaderBar(vm, onExit)
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 16.dp),
                ) {
                    when (vm.page) {
                        Page.HOME -> HomeScreen(vm)
                        Page.VERSIONS -> VersionsScreen(vm)
                        Page.DOWNLOAD -> DownloadScreen(vm)
                        Page.CONTENT -> ContentScreen(vm)
                        Page.SETTINGS -> SettingsScreen(vm)
                        Page.MORE -> MoreScreen(vm)
                    }
                }
            }

            AnimatedVisibility(vm.splash, enter = fadeIn(), exit = fadeOut()) {
                SplashOverlay()
            }
            vm.launching.let { if (it) LaunchOverlay(vm.launchStatus) }
            vm.installState?.let { InstallOverlay(vm, it) }
            vm.kernelState?.let { KernelOverlay(vm, it) }
            if (vm.accountSheet) AccountSheet(vm)
            if (vm.aiSheet) AiSheet(vm)
            if (vm.linkSheet) LinkSheet(vm)
            vm.errorInfo?.let { ErrorDialog(vm, it) }
            vm.infoDialog?.let { InfoDialogCard(it.title, it.body) { vm.dismissInfo() } }
            vm.toastText?.let { text ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                    Box(Modifier.padding(bottom = 30.dp)) { YzlToast(text) }
                }
            }
        }
    }
}

@Composable
fun HeaderBar(vm: AppViewModel, onExit: () -> Unit) {
    var clock by remember { mutableStateOf(clockText()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(15000)
            clock = clockText()
        }
    }
    Box(
        Modifier
            .fillMaxWidth()
            .height(66.dp)
            .background(Yzl.HeaderBrush),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(Color(0x10FFFFFF), radius = 180.dp.toPx(), center = Offset(size.width * 0.32f, size.height * 0.5f - 150.dp.toPx()))
            drawCircle(Color(0x0CFFFFFF), radius = 105.dp.toPx(), center = Offset(size.width * 0.72f, size.height * 0.5f - 110.dp.toPx()))
            drawCircle(Color(0x0AFFFFFF), radius = 65.dp.toPx(), center = Offset(size.width * 0.02f, size.height * 0.5f + 30.dp.toPx()))
        }
        Row(
            Modifier.fillMaxSize().padding(start = 20.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(37.dp)
                    .clip(RoundedCornerShape(11.dp)),
            ) { AssetImage("logo.png", Modifier.fillMaxSize()) }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("YZL2", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                Text(
                    "M I N E C R A F T   L A U N C H E R",
                    color = Color(0xA6FFFFFF),
                    fontSize = 7.5.sp,
                    letterSpacing = 0.4.sp,
                )
            }
            Spacer(Modifier.weight(1f))
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NavPill("启动", Icons.Filled.PlayArrow, vm.page == Page.HOME) { vm.selectPage(Page.HOME) }
                NavPill("版本", Icons.Filled.List, vm.page == Page.VERSIONS) { vm.selectPage(Page.VERSIONS) }
                NavPill("下载", Glyph.Download, vm.page == Page.DOWNLOAD) { vm.selectPage(Page.DOWNLOAD) }
                NavPill("资源", Glyph.Box, vm.page == Page.CONTENT) { vm.selectPage(Page.CONTENT) }
                NavPill("设置", Icons.Filled.Settings, vm.page == Page.SETTINGS) { vm.selectPage(Page.SETTINGS) }
                NavPill("更多", Icons.Filled.MoreVert, vm.page == Page.MORE) { vm.selectPage(Page.MORE) }
            }
            Spacer(Modifier.width(10.dp))
            Text(clock, color = Color(0xCCFFFFFF), fontSize = 12.sp)
            Spacer(Modifier.width(2.dp))
            IconAction(Icons.Filled.Close, "退出", onExit, tint = Color(0xE6FFFFFF))
        }
    }
}

@Composable
fun SplashOverlay() {
    Box(Modifier.fillMaxSize().background(Yzl.WindowBg), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier
                    .size(98.dp)
                    .clip(RoundedCornerShape(28.dp)),
            ) { AssetImage("logo.png") }
            Spacer(Modifier.height(22.dp))
            Text("YZL2", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Yzl.TextPrimary)
            Spacer(Modifier.height(5.dp))
            Text(
                "M I N E C R A F T   L A U N C H E R",
                fontSize = 8.5.sp,
                color = Yzl.TextMuted,
            )
            Spacer(Modifier.height(34.dp))
            SpinningRing(size = 32.dp)
            Spacer(Modifier.height(16.dp))
            Text("正在整理你的方块世界…", fontSize = 12.sp, color = Yzl.TextSecondary)
        }
    }
}

@Composable
fun SpinningRing(size: androidx.compose.ui.unit.Dp = 32.dp, stroke: Float = 2.8f, color: Color = Yzl.Accent) {
    var angle by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        if (!Yzl.animate) return@LaunchedEffect
        while (true) {
            angle = (angle + 4f) % 360f
            delay(16)
        }
    }
    Canvas(Modifier.size(size)) {
        val r = this.size.minDimension / 2f - 2.dp.toPx()
        drawCircle(Color(0xFFE1EBF8), radius = r, style = Stroke(width = 2.5.dp.toPx()))
        drawArc(
            color = color,
            startAngle = -90f + angle,
            sweepAngle = 110f,
            useCenter = false,
            topLeft = Offset(2.dp.toPx(), 2.dp.toPx()),
            size = Size(this.size.width - 4.dp.toPx(), this.size.height - 4.dp.toPx()),
            style = Stroke(width = stroke.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round),
        )
    }
}

@Composable
fun LaunchOverlay(status: String) {
    Box(Modifier.fillMaxSize().background(Color(0xF2F6FC)), contentAlignment = Alignment.Center) {
        CardBox(modifier = Modifier.width(420.dp), corner = 18.dp, shadow = 22.dp) {
            Column(
                Modifier.padding(34.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(Modifier.size(78.dp), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.fillMaxSize()) { drawCircle(Yzl.AccentSoft, radius = size.minDimension / 2) }
                    Box(Modifier.size(58.dp), contentAlignment = Alignment.Center) {
                        Canvas(Modifier.fillMaxSize()) { drawCircle(Color.White, radius = size.minDimension / 2) }
                    }
                    SpinningRing(size = 62.dp, stroke = 3.2f)
                    Icon(Glyph.Play, null, tint = Yzl.Accent, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.height(24.dp))
                Text(status, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Yzl.TextPrimary)
                Spacer(Modifier.height(8.dp))
                Text("正在与方块世界建立连接，请稍候", fontSize = 12.sp, color = Yzl.TextMuted)
            }
        }
    }
}

@Composable
fun KernelOverlay(vm: AppViewModel, state: com.yzl2.launcher.data.KernelState) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        CardBox(
            Modifier.padding(bottom = 34.dp).width(520.dp),
            corner = 14.dp,
            shadow = 18.dp,
        ) {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "运行环境 · ZL2 内核",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Yzl.TextPrimary,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        if (state.total > 0L) "${(state.fraction * 100).toInt()}%" else "…",
                        fontSize = 12.sp,
                        color = Yzl.Accent,
                    )
                }
                Spacer(Modifier.height(10.dp))
                LinearBar(state.fraction)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        state.stage + if (state.downloadedText.isNotBlank()) " · ${state.downloadedText}" else "",
                        fontSize = 11.sp,
                        color = Yzl.TextSecondary,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    )
                    Text(
                        "取消",
                        fontSize = 11.5.sp,
                        color = Yzl.Danger,
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { vm.cancelKernel() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
                if (state.finished) {
                    Spacer(Modifier.height(6.dp))
                    Text("下载完成，即将打开系统安装器…", fontSize = 11.sp, color = Yzl.Accent)
                }
                if (state.error != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(state.error, fontSize = 11.sp, color = Yzl.Danger)
                }
            }
        }
    }
}

@Composable
fun InstallOverlay(vm: AppViewModel, state: com.yzl2.launcher.data.InstallState) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        CardBox(
            Modifier.padding(bottom = 34.dp).width(520.dp),
            corner = 14.dp,
            shadow = 18.dp,
        ) {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("正在安装 ${state.versionId}", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = Yzl.TextPrimary)
                    Spacer(Modifier.weight(1f))
                    Text("${(state.fraction * 100).toInt()}%", fontSize = 12.sp, color = Yzl.Accent)
                }
                Spacer(Modifier.height(10.dp))
                LinearBar(state.fraction)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        state.stage + if (state.detail.isNotBlank()) " · ${state.detail}" else "",
                        fontSize = 11.sp,
                        color = Yzl.TextSecondary,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    )
                    Text(
                        "取消",
                        fontSize = 11.5.sp,
                        color = Yzl.Danger,
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { vm.cancelInstall() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}
