package com.yzl2.launcher.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class Repo(private val context: Context) {

    private val prefs = context.getSharedPreferences("yzl2_launcher", Context.MODE_PRIVATE)

    val defaultGameDir: File
        get() = File(context.getExternalFilesDir(null) ?: context.filesDir, "minecraft")

    fun gameDir(cfg: Config): File {
        val path = cfg.gamePath
        val f = if (path.isBlank()) defaultGameDir else File(path)
        if (!f.exists()) f.mkdirs()
        return f
    }

    fun loadConfig(): Config {
        val raw = prefs.getString("config", null) ?: return Config(gamePath = defaultGameDir.absolutePath)
        return runCatching { Config.fromJson(JSONObject(raw)) }.getOrElse { Config(gamePath = defaultGameDir.absolutePath) }
            .let { if (it.gamePath.isBlank()) it.copy(gamePath = defaultGameDir.absolutePath) else it }
    }

    fun saveConfig(cfg: Config) {
        prefs.edit().putString("config", cfg.toJson().toString()).apply()
    }

    fun loadAccounts(): List<Account> {
        val raw = prefs.getString("accounts", null) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { Account.fromJson(arr.getJSONObject(it)) }
        }.getOrDefault(emptyList())
    }

    fun saveAccounts(accounts: List<Account>) {
        val arr = JSONArray()
        accounts.forEach { arr.put(it.toJson()) }
        prefs.edit().putString("accounts", arr.toString()).apply()
    }

    fun loadSelectedAccount(): Account? {
        val name = prefs.getString("selectedAccount", null) ?: return null
        return loadAccounts().firstOrNull { it.name == name && it.kind == runCatching {
            AccountKind.valueOf(prefs.getString("selectedKind", "OFFLINE") ?: "OFFLINE")
        }.getOrDefault(AccountKind.OFFLINE) }
    }

    fun saveSelectedAccount(account: Account?) {
        prefs.edit().apply {
            putString("selectedAccount", account?.name)
            putString("selectedKind", account?.kind?.name)
        }.apply()
    }

    fun scanVersions(cfg: Config): List<InstalledVersion> {
        val root = File(gameDir(cfg), "versions")
        if (!root.isDirectory) return emptyList()
        val result = mutableListOf<InstalledVersion>()
        root.listFiles()?.forEach { dir ->
            if (!dir.isDirectory) return@forEach
            val jsonFile = File(dir, "${dir.name}.json")
            if (!jsonFile.isFile) return@forEach
            val obj = runCatching { JSONObject(jsonFile.readText()) }.getOrNull() ?: return@forEach
            val id = obj.optString("id", dir.name)
            val type = obj.optString("type", "release")
            val inherits = obj.optString("inheritsFrom", "")
            val client = inherits.ifBlank { id }
            val loader = detectLoader(id)
            val isolation = File(dir, "mods").isDirectory || cfg.versionIsolation
            val modsDir = if (File(dir, "mods").isDirectory) File(dir, "mods") else File(gameDir(cfg), "mods")
            val savesDir = if (File(dir, "saves").isDirectory) File(dir, "saves") else File(gameDir(cfg), "saves")
            val mods = modsDir.listFiles()?.count { it.isFile && it.name.endsWith(".jar", true) } ?: 0
            val saves = savesDir.listFiles()?.count { it.isDirectory } ?: 0
            result += InstalledVersion(
                id = id,
                clientVersion = client,
                type = type,
                loader = loader,
                path = dir,
                mods = mods,
                saves = saves,
                lastPlayed = cfg.lastPlayed[id] ?: 0L,
                hasIsolation = isolation,
            )
        }
        return result.sortedWith(compareByDescending<InstalledVersion> { it.lastPlayed }.thenByDescending { it.id })
    }

    private fun detectLoader(id: String): Loader {
        val lower = id.lowercase()
        return when {
            lower.contains("neoforge") -> Loader.NEOFORGE
            lower.contains("forge") -> Loader.FORGE
            lower.contains("quilt") -> Loader.QUILT
            lower.contains("fabric") -> Loader.FABRIC
            else -> Loader.VANILLA
        }
    }

    fun latestLog(cfg: Config): File = File(gameDir(cfg), "logs/latest.log")

    fun readLatestLog(cfg: Config, maxChars: Int = 6000): String {
        val f = latestLog(cfg)
        if (!f.isFile) return ""
        return runCatching {
            val text = f.readText()
            if (text.length > maxChars) text.takeLast(maxChars) else text
        }.getOrDefault("")
    }

    fun modsDir(cfg: Config, version: InstalledVersion?): File {
        val base = gameDir(cfg)
        val dir = if (version != null && cfg.versionIsolation) File(version.path, "mods") else File(base, "mods")
        dir.mkdirs()
        return dir
    }

    fun savesDir(cfg: Config, version: InstalledVersion?): File {
        val base = gameDir(cfg)
        val dir = if (version != null && cfg.versionIsolation) File(version.path, "saves") else File(base, "saves")
        dir.mkdirs()
        return dir
    }
}
