package com.yzl2.launcher

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import com.yzl2.launcher.data.Page
import com.yzl2.launcher.ui.AppRoot
import com.yzl2.launcher.ui.Yzl
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.io.FileOutputStream

/** 校验自定义背景（图片）真的铺在界面底层，并且设置页能识别当前文件。 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w1180dp-h760dp-land-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class BackgroundTest {

    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun isolatedBackground() {
        Yzl.animate = false
        val app = ApplicationProvider.getApplicationContext<Application>()
        val file = writeSample(app)
        assertTrue("图片解码失败", android.graphics.BitmapFactory.decodeFile(file.absolutePath) != null)
        compose.setContent {
            androidx.compose.foundation.layout.Box(
                androidx.compose.ui.Modifier
                    .fillMaxSize()
                    .background(androidx.compose.ui.graphics.Color.White),
            ) {
                com.yzl2.launcher.ui.LauncherBackground(file.absolutePath, true, true)
            }
        }
        compose.waitForIdle()
        Thread.sleep(1500)
        compose.waitForIdle()
        shot("background_isolated")
    }

    private fun writeSample(app: Application): File {
        val file = File(app.filesDir, "background/sample-${System.nanoTime()}.png")
        file.parentFile?.mkdirs()
        val bitmap = Bitmap.createBitmap(1280, 800, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(0xFF3366AA.toInt())
        val paint = android.graphics.Paint()
        paint.shader = android.graphics.LinearGradient(
            0f, 0f, 1280f, 800f,
            intArrayOf(0xFF1B4F9C.toInt(), 0xFFE7A63C.toInt(), 0xFFD4534E.toInt()),
            null,
            android.graphics.Shader.TileMode.CLAMP,
        )
        canvas.drawRect(0f, 0f, 1280f, 800f, paint)
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        return file
    }

    @Test
    fun customImageBackgroundRenders() {
        Yzl.animate = false
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = AppViewModel(app)
        vm.skipSplash()

        val file = writeSample(app)

        vm.updateConfig { it.copy(backgroundImage = file.absolutePath) }
        assertTrue("背景文件不存在", File(vm.config.backgroundImage).isFile)

        compose.setContent { AppRoot(vm) { } }
        compose.waitForIdle()
        // 背景图片在 IO 线程解码，等它回落到主线程后再截图。
        Thread.sleep(1500)
        compose.waitForIdle()
        shot("background_home")

        vm.selectPage(Page.SETTINGS)
        compose.waitForIdle()
        shot("background_settings")
    }

    private fun shot(name: String) {
        compose.waitForIdle()
        val activity = compose.activity
        val density = activity.resources.displayMetrics.density
        val config = activity.resources.configuration
        val w = (config.screenWidthDp * density).toInt().coerceAtLeast(1)
        val h = (config.screenHeightDp * density).toInt().coerceAtLeast(1)
        val decor: View = activity.window.decorView
        decor.measure(
            View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(h, View.MeasureSpec.EXACTLY),
        )
        decor.layout(0, 0, w, h)
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(0xFFEEF4FC.toInt())
        decor.draw(canvas)
        val out = File("build/screenshots/$name.png")
        out.parentFile?.mkdirs()
        FileOutputStream(out).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
