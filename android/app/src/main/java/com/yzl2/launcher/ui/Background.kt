package com.yzl2.launcher.ui

import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.view.Surface
import android.view.TextureView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.yzl2.launcher.AppViewModel
import com.yzl2.launcher.data.isVideoFileName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

fun isVideoPath(path: String): Boolean = isVideoFileName(File(path).name)

/** 启动器最底层的自定义背景：图片铺满，视频循环播放。 */
@Composable
fun LauncherBackground(
    path: String,
    videoLoop: Boolean,
    videoMuted: Boolean,
    modifier: Modifier = Modifier,
) {
    if (path.isBlank() || !File(path).isFile) return
    Box(modifier.fillMaxSize()) {
        if (isVideoPath(path)) {
            VideoBackground(path, videoLoop, videoMuted, Modifier.fillMaxSize())
        } else {
            ImageBackground(path, Modifier.fillMaxSize())
        }
        // 淡蓝色半透明遮罩：既能看到背景，又不影响卡片文字可读性。
        Box(Modifier.fillMaxSize().background(Color(0x59F2F7FE)))
    }
}

@Composable
private fun ImageBackground(path: String, modifier: Modifier) {
    val bitmap = rememberBackgroundBitmap(path)
    if (bitmap != null) {
        Image(
            bitmap,
            contentDescription = null,
            modifier = modifier.blur(16.dp),
            contentScale = ContentScale.Crop,
        )
    }
}

@Composable
private fun rememberBackgroundBitmap(path: String): ImageBitmap? {
    var bitmap by remember(path) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(path) {
        bitmap = withContext(Dispatchers.IO) {
            runCatching {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(path, bounds)
                var sample = 1
                while (bounds.outWidth / (sample * 2) >= 1600 && bounds.outHeight / (sample * 2) >= 900) {
                    sample *= 2
                }
                BitmapFactory.decodeFile(
                    path,
                    BitmapFactory.Options().apply { inSampleSize = sample },
                )?.asImageBitmap()
            }.getOrNull()
        }
    }
    return bitmap
}

@Composable
private fun VideoBackground(path: String, loop: Boolean, muted: Boolean, modifier: Modifier) {
    val context = LocalContext.current
    val texture = remember(path) {
        TextureView(context).apply { isOpaque = true }
    }
    DisposableEffect(path, loop, muted) {
        var player: MediaPlayer? = null
        var videoWidth = 1
        var videoHeight = 1

        fun fitToView() {
            val viewWidth = texture.width.toFloat()
            val viewHeight = texture.height.toFloat()
            if (viewWidth <= 0f || viewHeight <= 0f) return
            val viewAspect = viewWidth / viewHeight
            val videoAspect = videoWidth.toFloat() / videoHeight.toFloat()
            val matrix = Matrix()
            if (videoAspect > viewAspect) {
                matrix.setScale(videoAspect / viewAspect, 1f, viewWidth / 2f, viewHeight / 2f)
            } else {
                matrix.setScale(1f, viewAspect / videoAspect, viewWidth / 2f, viewHeight / 2f)
            }
            texture.setTransform(matrix)
        }

        fun startPlayer(surfaceTexture: SurfaceTexture) {
            val mp = MediaPlayer()
            player = mp
            runCatching {
                mp.setDataSource(path)
                mp.isLooping = loop
                val volume = if (muted) 0f else 1f
                mp.setVolume(volume, volume)
                mp.setSurface(Surface(surfaceTexture))
                mp.setOnVideoSizeChangedListener { _, width, height ->
                    videoWidth = width.coerceAtLeast(1)
                    videoHeight = height.coerceAtLeast(1)
                    fitToView()
                }
                mp.setOnPreparedListener { prepared ->
                    fitToView()
                    prepared.start()
                }
                mp.prepareAsync()
            }.onFailure {
                runCatching { mp.release() }
                player = null
            }
        }

        val listener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(st: SurfaceTexture, width: Int, height: Int) {
                startPlayer(st)
            }

            override fun onSurfaceTextureSizeChanged(st: SurfaceTexture, width: Int, height: Int) {
                fitToView()
            }

            override fun onSurfaceTextureDestroyed(st: SurfaceTexture): Boolean {
                runCatching { player?.release() }
                player = null
                return true
            }

            override fun onSurfaceTextureUpdated(st: SurfaceTexture) = Unit
        }
        texture.surfaceTextureListener = listener
        val available = texture.surfaceTexture
        if (texture.isAvailable && available != null) {
            listener.onSurfaceTextureAvailable(available, texture.width, texture.height)
        }
        onDispose {
            runCatching { player?.release() }
            player = null
            texture.surfaceTextureListener = null
        }
    }
    AndroidView(factory = { texture }, modifier = modifier)
}

/** 背景音乐：循环播放，音量跟随设置。 */
@Composable
fun BackgroundMusicPlayer(path: String, enabled: Boolean, volume: Int) {
    val context = LocalContext.current
    var player by remember { mutableStateOf<MediaPlayer?>(null) }
    DisposableEffect(path, enabled) {
        if (enabled && path.isNotBlank() && File(path).isFile) {
            val mp = MediaPlayer()
            runCatching {
                mp.setDataSource(path)
                mp.isLooping = true
                mp.setVolume(volume / 100f, volume / 100f)
                mp.prepare()
                mp.start()
                player = mp
            }.onFailure {
                runCatching { mp.release() }
            }
        }
        onDispose {
            player?.let { mp ->
                runCatching { mp.stop() }
                runCatching { mp.release() }
            }
            player = null
        }
    }
    LaunchedEffect(volume) {
        player?.setVolume(volume / 100f, volume / 100f)
    }
}

/** 设置页「个性化」卡片内容：选择文件、循环/静音开关。 */
@Composable
fun BackgroundPickerContent(vm: AppViewModel) {
    val backgroundPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) vm.importBackground(uri) else vm.showToast("没有选择文件")
    }
    val musicPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) vm.importMusic(uri) else vm.showToast("没有选择文件")
    }
    val backgroundPath = vm.config.backgroundImage
    val hasBackground = backgroundPath.isNotBlank() && File(backgroundPath).isFile
    val isVideo = hasBackground && isVideoPath(backgroundPath)
    val musicName = if (vm.config.backgroundMusic.isBlank()) "未选择" else File(vm.config.backgroundMusic).name

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        YzlButton(
            "选择背景图 / 视频",
            { backgroundPicker.launch(arrayOf("image/*", "video/*")) },
            height = 34.dp,
            fontSize = 11.5.sp,
        )
        YzlButton(
            "选择背景音乐",
            { musicPicker.launch(arrayOf("audio/*")) },
            height = 34.dp,
            fontSize = 11.5.sp,
        )
        if (hasBackground) {
            YzlButton("清除背景", { vm.clearBackground() }, height = 34.dp, fontSize = 11.5.sp)
        }
    }
    Spacer(Modifier.height(10.dp))
    Text(
        if (!hasBackground) {
            "支持 png / jpg 图片与 mp4 / webm 视频；点击按钮会打开系统文件选择器，可进入任意文件夹挑选文件。"
        } else if (isVideo) {
            "当前背景视频：${File(backgroundPath).name}"
        } else {
            "当前背景图片：${File(backgroundPath).name}"
        },
        fontSize = 10.5.sp,
        color = Yzl.TextMuted,
        lineHeight = 15.sp,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
    Spacer(Modifier.height(4.dp))
    Text("当前背景音乐：$musicName", fontSize = 10.5.sp, color = Yzl.TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)

    if (hasBackground && isVideo) {
        Spacer(Modifier.height(8.dp))
        BgToggleRow(
            "循环播放视频",
            "关闭后视频只播放一次",
            vm.config.backgroundVideoLoop,
        ) { value -> vm.updateConfig { it.copy(backgroundVideoLoop = value) } }
        BgToggleRow(
            "视频静音",
            "背景视频不播放声音",
            vm.config.backgroundVideoMuted,
        ) { value -> vm.updateConfig { it.copy(backgroundVideoMuted = value) } }
    }
    if (vm.config.backgroundMusic.isNotBlank()) {
        BgToggleRow(
            "自动播放背景音乐",
            "启动器打开后自动循环播放",
            vm.config.musicAutoPlay,
        ) { value -> vm.updateConfig { it.copy(musicAutoPlay = value) } }
    }
    Spacer(Modifier.height(6.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("背景音量", fontSize = 12.5.sp, color = Yzl.TextPrimary, modifier = Modifier.weight(1f))
        Stepper(vm.config.musicVolume, { vm.updateConfig { c -> c.copy(musicVolume = it.coerceIn(0, 100)) } }, 0, 100, 10)
        Text(" %", fontSize = 10.5.sp, color = Yzl.TextMuted)
    }
}

@Composable
private fun BgToggleRow(title: String, desc: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 12.5.sp, fontWeight = FontWeight.Medium, color = Yzl.TextPrimary)
            Spacer(Modifier.height(3.dp))
            Text(desc, fontSize = 10.5.sp, color = Yzl.TextMuted)
        }
        YzlToggle(checked, onChange)
    }
}
