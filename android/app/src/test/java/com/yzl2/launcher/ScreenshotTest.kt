package com.yzl2.launcher

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import com.yzl2.launcher.data.Page
import com.yzl2.launcher.ui.AppRoot
import com.yzl2.launcher.ui.Yzl
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w1180dp-h760dp-land-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ScreenshotTest {

    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun capturePages() {
        val vm = prepare()
        compose.setContent { AppRoot(vm) { } }
        compose.waitForIdle()
        shot("home")
        vm.selectPage(Page.VERSIONS)
        compose.waitForIdle()
        shot("versions")
        vm.debugSetManifest(
            listOf(
                com.yzl2.launcher.data.RemoteVersion("26.4-snapshot-10", "snapshot", "https://invalid.local/a.json", "", "2026-11-18T00:00:00"),
                com.yzl2.launcher.data.RemoteVersion("26.3", "release", "https://invalid.local/b.json", "", "2026-11-12T00:00:00"),
                com.yzl2.launcher.data.RemoteVersion("26.2", "release", "https://invalid.local/c.json", "", "2026-11-05T00:00:00"),
                com.yzl2.launcher.data.RemoteVersion("b1.7.3", "old_beta", "https://invalid.local/d.json", "", "2011-07-08T00:00:00"),
            )
        )
        vm.selectPage(Page.DOWNLOAD)
        compose.waitForIdle()
        shot("download")
        vm.selectPage(Page.CONTENT)
        compose.waitForIdle()
        shot("content")
        vm.selectPage(Page.SETTINGS)
        compose.waitForIdle()
        shot("settings")
        vm.selectPage(Page.MORE)
        compose.waitForIdle()
        shot("more")
        vm.openAccountSheet()
        compose.waitForIdle()
        shot("account")
        vm.closeAccountSheet()
        vm.openAi()
        compose.waitForIdle()
        shot("ai")
        vm.closeAi()
        vm.openLink()
        compose.waitForIdle()
        shot("link")
        vm.closeLink()
    }

    @Test
    @Config(qualifiers = "w891dp-h412dp-land-mdpi")
    fun capturePhone() {
        val vm = prepare()
        compose.setContent { AppRoot(vm) { } }
        compose.waitForIdle()
        shot("phone_home")
        vm.selectPage(Page.VERSIONS)
        compose.waitForIdle()
        shot("phone_versions")
        vm.openAccountSheet()
        compose.waitForIdle()
        shot("phone_account")
    }

    private fun prepare(): AppViewModel {
        Yzl.animate = false
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = AppViewModel(app)
        vm.skipSplash()
        seed(vm)
        return vm
    }

    private fun seed(vm: AppViewModel) {
        val root = File(vm.gameDirPath())
        fun version(id: String, json: String) {
            val dir = File(root, "versions/$id")
            dir.mkdirs()
            File(dir, "$id.json").writeText(json)
        }
        version("26.2", """{"id":"26.2","type":"release"}""")
        version(
            "fabric-loader-0.16.9-1.21.4",
            """{"id":"fabric-loader-0.16.9-1.21.4","type":"release","inheritsFrom":"1.21.4"}""",
        )
        File(root, "mods").mkdirs()
        (1..5).forEach { File(root, "mods/sample-mod-$it.jar").writeText("") }
        File(root, "saves/新的世界").mkdirs()
        vm.refreshVersions()
        vm.selectVersion("26.2")
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
