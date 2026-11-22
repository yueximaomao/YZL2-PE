package com.yzl2.launcher.data

import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class Page(val label: String) {
    HOME("启动"), VERSIONS("版本"), DOWNLOAD("下载"), CONTENT("资源"), SETTINGS("设置"), MORE("更多")
}

enum class AccountKind(val label: String) {
    OFFLINE("离线账户"), MICROSOFT("Microsoft 正版"), AUTHLIB("LittleSkin")
}

data class Account(
    val name: String = "Steve",
    val kind: AccountKind = AccountKind.OFFLINE,
    val uuid: String = offlineUuid("Steve"),
    val accessToken: String = "",
    val skinModel: String = "classic",
) {
    val initial: String get() = name.take(1).uppercase(Locale.getDefault())
    val typeLabel: String get() = kind.label
    val isOnline: Boolean get() = kind != AccountKind.OFFLINE && accessToken.isNotBlank()

    fun toJson(): JSONObject = JSONObject()
        .put("name", name).put("kind", kind.name).put("uuid", uuid)
        .put("accessToken", accessToken).put("skinModel", skinModel)

    companion object {
        fun offlineUuid(name: String): String = UUID.nameUUIDFromBytes(("OfflinePlayer:$name").toByteArray()).toString()
        fun fromJson(o: JSONObject) = Account(
            name = o.optString("name", "Steve"),
            kind = runCatching { AccountKind.valueOf(o.optString("kind", "OFFLINE")) }.getOrDefault(AccountKind.OFFLINE),
            uuid = o.optString("uuid", offlineUuid(o.optString("name", "Steve"))),
            accessToken = o.optString("accessToken", ""),
            skinModel = o.optString("skinModel", "classic"),
        )
    }
}

enum class Loader(val label: String, val color: Long) {
    VANILLA("原版", 0xFF5FA86B), FABRIC("Fabric", 0xFFC49A3A), QUILT("Quilt", 0xFF9B6BD6),
    FORGE("Forge", 0xFF6C8FC9), NEOFORGE("NeoForge", 0xFFD07A4A), UNKNOWN("原版", 0xFF5FA86B)
}

data class InstalledVersion(
    val id: String,
    val clientVersion: String,
    val type: String,
    val loader: Loader,
    val path: File,
    val mods: Int = 0,
    val saves: Int = 0,
    val lastPlayed: Long = 0L,
    val hasIsolation: Boolean = false,
) {
    val typeLabel: String
        get() = when (type) {
            "release" -> "正式版"; "snapshot" -> "快照"; "old_beta" -> "旧版 Beta"; "old_alpha" -> "旧版 Alpha"
            else -> "正式版"
        }
}

data class Config(
    val gamePath: String = "",
    val memoryMb: Int = 4096,
    val windowWidth: Int = 1280,
    val windowHeight: Int = 720,
    val fullscreen: Boolean = false,
    val extraJvmArgs: String = "",
    val versionIsolation: Boolean = false,
    val playerName: String = "Steve",
    val lastVersion: String = "",
    val animationsEnabled: Boolean = true,
    val themeAccent: String = "Blue",
    val minimizeOnLaunch: Boolean = false,
    val backgroundImage: String = "",
    val backgroundMusic: String = "",
    val backgroundVideoLoop: Boolean = true,
    val backgroundVideoMuted: Boolean = true,
    val musicVolume: Int = 60,
    val musicAutoPlay: Boolean = true,
    val aiEndpoint: String = "",
    val aiKey: String = "",
    val autoKernel: Boolean = true,
    val lastPlayed: Map<String, Long> = emptyMap(),
) {
    fun toJson(): JSONObject {
        val o = JSONObject()
        o.put("gamePath", gamePath); o.put("memoryMb", memoryMb)
        o.put("windowWidth", windowWidth); o.put("windowHeight", windowHeight)
        o.put("fullscreen", fullscreen); o.put("extraJvmArgs", extraJvmArgs)
        o.put("versionIsolation", versionIsolation); o.put("playerName", playerName)
        o.put("lastVersion", lastVersion); o.put("animationsEnabled", animationsEnabled)
        o.put("themeAccent", themeAccent); o.put("minimizeOnLaunch", minimizeOnLaunch)
        o.put("backgroundImage", backgroundImage); o.put("backgroundMusic", backgroundMusic)
        o.put("backgroundVideoLoop", backgroundVideoLoop); o.put("backgroundVideoMuted", backgroundVideoMuted)
        o.put("musicVolume", musicVolume); o.put("musicAutoPlay", musicAutoPlay)
        o.put("aiEndpoint", aiEndpoint); o.put("aiKey", aiKey)
        o.put("autoKernel", autoKernel)
        val lp = JSONObject(); lastPlayed.forEach { (k, v) -> lp.put(k, v) }
        o.put("lastPlayed", lp)
        return o
    }

    companion object {
        fun fromJson(o: JSONObject): Config {
            val lp = mutableMapOf<String, Long>()
            o.optJSONObject("lastPlayed")?.let { j ->
                j.keys().forEach { k -> lp[k] = j.optLong(k) }
            }
            return Config(
                gamePath = o.optString("gamePath", ""),
                memoryMb = o.optInt("memoryMb", 4096),
                windowWidth = o.optInt("windowWidth", 1280),
                windowHeight = o.optInt("windowHeight", 720),
                fullscreen = o.optBoolean("fullscreen", false),
                extraJvmArgs = o.optString("extraJvmArgs", ""),
                versionIsolation = o.optBoolean("versionIsolation", false),
                playerName = o.optString("playerName", "Steve"),
                lastVersion = o.optString("lastVersion", ""),
                animationsEnabled = o.optBoolean("animationsEnabled", true),
                themeAccent = o.optString("themeAccent", "Blue"),
                minimizeOnLaunch = o.optBoolean("minimizeOnLaunch", false),
                backgroundImage = o.optString("backgroundImage", ""),
                backgroundMusic = o.optString("backgroundMusic", ""),
                backgroundVideoLoop = o.optBoolean("backgroundVideoLoop", true),
                backgroundVideoMuted = o.optBoolean("backgroundVideoMuted", true),
                musicVolume = o.optInt("musicVolume", 60),
                musicAutoPlay = o.optBoolean("musicAutoPlay", true),
                aiEndpoint = o.optString("aiEndpoint", ""),
                aiKey = o.optString("aiKey", ""),
                autoKernel = o.optBoolean("autoKernel", true),
                lastPlayed = lp,
            )
        }
    }
}

data class RemoteVersion(
    val id: String, val type: String, val url: String, val time: String, val releaseTime: String
) {
    val typeLabel: String get() = if (type == "release") "正式版" else if (type == "snapshot") "快照" else "旧版"
    val isRelease: Boolean get() = type == "release"
    val isSnapshot: Boolean get() = type == "snapshot"
    val isOld: Boolean get() = type.startsWith("old")
}

data class InstallState(
    val versionId: String,
    val stage: String,
    val done: Int,
    val total: Int,
    val detail: String = "",
    val finished: Boolean = false,
    val failed: String? = null,
) {
    val fraction: Float get() = if (total <= 0) 0f else (done.toFloat() / total).coerceIn(0f, 1f)
}

/** ZL2 / Pojav 内核的下载进度。 */
data class KernelState(
    val stage: String,
    val done: Long = 0L,
    val total: Long = 0L,
    val finished: Boolean = false,
    val error: String? = null,
) {
    val fraction: Float
        get() = if (total <= 0L) 0f else (done.toFloat() / total).coerceIn(0f, 1f)
    val downloadedText: String
        get() = if (total <= 0L) "" else "${done / 1024 / 1024} / ${total / 1024 / 1024} MB"
}

private val VIDEO_EXTENSIONS = setOf("mp4", "webm", "mkv", "mov", "avi", "m4v", "ts", "3gp")

fun isVideoFileName(name: String): Boolean =
    name.substringAfterLast('.', "").lowercase() in VIDEO_EXTENSIONS

data class MrHit(
    val id: String, val slug: String, val title: String, val description: String,
    val iconUrl: String?, val downloads: Long, val author: String, val projectType: String,
    val categories: List<String>, val versions: List<String>,
)

data class MrVersion(
    val id: String, val name: String, val versionNumber: String, val gameVersions: List<String>,
    val loaders: List<String>, val fileUrl: String, val fileName: String, val fileSize: Long, val datePublished: String,
)

data class AiMessage(val text: String, val fromUser: Boolean, val time: Long = System.currentTimeMillis())

data class ErrorInfo(
    val title: String, val subtitle: String, val detail: String,
    val suggestion: String = "", val log: String = "",
)

fun Long.relativeTime(): String {
    if (this <= 0L) return "尚未启动"
    val diff = System.currentTimeMillis() - this
    val min = diff / 60000
    return when {
        min < 1 -> "刚刚"
        min < 60 -> "$min 分钟前"
        min < 60 * 24 -> "${min / 60} 小时前"
        min < 60 * 24 * 2 -> "昨天"
        min < 60 * 24 * 30 -> "${min / (60 * 24)} 天前"
        else -> SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(Date(this))
    }
}

fun formatCount(n: Long): String = when {
    n >= 100_000_000 -> String.format(Locale.US, "%.1f 亿", n / 100_000_000.0)
    n >= 10_000 -> String.format(Locale.US, "%.1f 万", n / 10_000.0)
    else -> n.toString()
}

fun greeting(): String {
    val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        h < 5 -> "夜深了"; h < 9 -> "早上好"; h < 12 -> "上午好"
        h < 14 -> "中午好"; h < 18 -> "下午好"; h < 22 -> "晚上好"; else -> "夜深了"
    }
}

fun dayPeriod(): String = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
    in 5..8 -> "清晨"; in 9..11 -> "上午"; in 12..13 -> "中午"; in 14..17 -> "下午"
    in 18..21 -> "傍晚"; else -> "深夜"
}

fun clockText(): String = SimpleDateFormat("HH:mm", Locale.CHINA).format(Date())

fun dateText(): String = SimpleDateFormat("M月d日 EEEE", Locale.CHINA).format(Date())
