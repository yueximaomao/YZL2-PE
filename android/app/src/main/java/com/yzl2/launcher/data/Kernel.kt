package com.yzl2.launcher.data

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.File

/**
 * ZL2 / Pojav 内核：真正提供 JVM 与图形层的那一层。
 *
 * 内核自带 Java 运行时，所以「自动下载内核」就等于把 Java 环境一起准备好。
 * 如果设备上还没有内核，YZL2 会从 ZalithLauncher 官方仓库拉取与当前 ABI
 * 匹配的安装包，再交给系统安装器。
 */
object Kernel {

    private const val REPO = "ZalithLauncher/ZalithLauncher2"
    private const val API = "https://api.github.com/repos/$REPO/releases/latest"
    private const val MIRROR = "https://ghfast.top/"
    private const val MIRROR_2 = "https://gh-proxy.com/"

    /** 已知会提供 Minecraft Java 运行环境的应用包名。 */
    val knownPackages = listOf(
        "com.movtery.zalithlauncher",
        "com.movtery.zalithlauncher.v2",
        "net.kdt.pojavlaunch",
    )

    data class Release(
        val name: String,
        val url: String,
        val size: Long,
    )

    fun installed(context: Context): Boolean = installedPackages(context).isNotEmpty()

    /** 通过桌面启动器查询，避免 Android 11+ 的包可见性限制。 */
    fun installedPackages(context: Context): List<String> {
        val pm = context.packageManager
        val visible = runCatching {
            pm.queryIntentActivities(
                Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),
                0,
            ).map { it.activityInfo.packageName }
        }.getOrDefault(emptyList())
        return (visible + knownPackages).distinct().filter { pkg ->
            (knownPackages.any { it.equals(pkg, ignoreCase = true) } ||
                pkg.contains("zalithlauncher", ignoreCase = true) ||
                pkg.contains("pojavlaunch", ignoreCase = true)) &&
                runCatching { pm.getPackageInfo(pkg, 0); true }.getOrDefault(false)
        }
    }

    fun label(context: Context, pkg: String): String = runCatching {
        val info = context.packageManager.getApplicationInfo(pkg, 0)
        context.packageManager.getApplicationLabel(info).toString()
    }.getOrDefault(pkg)

    suspend fun latestRelease(): Release {
        val body = runCatching {
            Http.text(API, headers = mapOf("Accept" to "application/vnd.github+json"))
        }.getOrElse {
            Http.text(MIRROR + API)
        }
        val assets = JSONObject(body).getJSONArray("assets")
        val apks = (0 until assets.length())
            .map { assets.getJSONObject(it) }
            .filter { it.optString("name").endsWith(".apk", ignoreCase = true) }
        if (apks.isEmpty()) throw RuntimeException("这个版本没有提供 APK 安装包")

        val abi = runCatching { Build.SUPPORTED_ABIS.firstOrNull() }.getOrNull() ?: "arm64-v8a"
        val abiKey = when {
            abi.startsWith("arm64") -> "arm64-v8a"
            abi.startsWith("arm") -> "armeabi-v7a"
            abi.startsWith("x86_64") -> "x86_64"
            abi.startsWith("x86") -> "x86"
            else -> "arm64-v8a"
        }
        val pick = apks.firstOrNull { it.optString("name").contains(abiKey, ignoreCase = true) }
            ?: apks.firstOrNull { it.optString("name").contains(abi, ignoreCase = true) }
            ?: apks.firstOrNull { it.optString("name").contains("arm64", ignoreCase = true) }
            ?: apks.first()
        return Release(
            name = pick.optString("name", "ZalithLauncher.apk"),
            url = pick.optString("browser_download_url"),
            size = pick.optLong("size", 0L),
        )
    }

    suspend fun download(context: Context, onProgress: (Long, Long) -> Unit): File {
        val release = latestRelease()
        val dir = File(context.cacheDir, "kernel").apply { mkdirs() }
        val dest = File(dir, release.name)
        val complete = dest.isFile && dest.length() > 10_000_000L &&
            (release.size <= 0L || dest.length() == release.size)
        if (complete) return dest
        val urls = listOf(
            release.url,
            MIRROR + release.url,
            MIRROR_2 + release.url,
        )
        Http.downloadAny(urls, dest, onProgress)
        return dest
    }

    fun install(context: Context, apk: File): Intent {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", apk)
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
