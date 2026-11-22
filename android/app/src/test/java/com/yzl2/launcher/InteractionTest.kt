package com.yzl2.launcher

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.yzl2.launcher.data.Page
import com.yzl2.launcher.data.RemoteVersion
import com.yzl2.launcher.ui.AppRoot
import com.yzl2.launcher.ui.Yzl
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w1180dp-h760dp-land-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class InteractionTest {

    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun searchButtonResponds() {
        val vm = prepare()
        compose.setContent { AppRoot(vm) { } }
        compose.waitForIdle()
        vm.selectPage(Page.CONTENT)
        compose.waitForIdle()
        val before = vm.searchRequestCount
        compose.onNodeWithText("搜索").performClick()
        compose.waitForIdle()
        assertTrue("搜索按钮没有触发搜索", vm.searchRequestCount > before)
    }

    @Test
    fun installButtonResponds() {
        val vm = prepare()
        vm.debugSetManifest(
            listOf(
                RemoteVersion("1.20.1", "release", "https://invalid.local/1.20.1.json", "", "2024-01-01T00:00:00"),
                RemoteVersion("1.20.2", "release", "https://invalid.local/1.20.2.json", "", "2024-02-01T00:00:00"),
            )
        )
        compose.setContent { AppRoot(vm) { } }
        compose.waitForIdle()
        vm.selectPage(Page.DOWNLOAD)
        compose.waitForIdle()
        compose.onAllNodesWithText("安装")[0].performClick()
        compose.waitForIdle()
        assertTrue("安装按钮没有触发安装任务", vm.installState != null)
    }

    private fun prepare(): AppViewModel {
        Yzl.animate = false
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = AppViewModel(app)
        vm.skipSplash()
        return vm
    }
}
