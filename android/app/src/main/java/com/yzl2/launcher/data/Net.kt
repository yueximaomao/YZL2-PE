package com.yzl2.launcher.data

import android.content.Context
import android.content.Intent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

object Http {
    private const val UA = "Mozilla/5.0 (Linux; Android 13) YZL2/1.0.0"

    fun connection(url: String): HttpURLConnection {
        val c = URL(url).openConnection() as HttpURLConnection
        c.connectTimeout = 12000
        c.readTimeout = 25000
        c.instanceFollowRedirects = true
        c.setRequestProperty("User-Agent", UA)
        return c
    }

    suspend fun text(
        url: String,
        method: String = "GET",
        headers: Map<String, String> = emptyMap(),
        body: String? = null,
    ): String = withContext(Dispatchers.IO) {
        val c = connection(url)
        c.requestMethod = method
        headers.forEach { (k, v) -> c.setRequestProperty(k, v) }
        if (body != null) {
            c.doOutput = true
            c.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            c.outputStream.use { it.write(body.toByteArray()) }
        }
        val code = c.responseCode
        val stream: InputStream = if (code in 200..299) c.inputStream else (c.errorStream ?: c.inputStream)
        val text = stream.bufferedReader().use { it.readText() }
        c.disconnect()
        if (code !in 200..299) throw RuntimeException("HTTP $code: ${text.take(180)}")
        text
    }

    suspend fun json(
        url: String,
        method: String = "GET",
        headers: Map<String, String> = emptyMap(),
        body: String? = null,
    ): JSONObject = JSONObject(text(url, method, headers, body))

    suspend fun download(url: String, dest: File, onProgress: ((Long, Long) -> Unit)? = null) {
        withContext(Dispatchers.IO) {
            dest.parentFile?.mkdirs()
            val c = connection(url)
            val len = c.contentLengthLong
            c.inputStream.use { input ->
                dest.outputStream().use { out ->
                    val buf = ByteArray(64 * 1024)
                    var read = 0L
                    while (true) {
                        val n = input.read(buf)
                        if (n <= 0) break
                        out.write(buf, 0, n)
                        read += n
                        onProgress?.invoke(read, len)
                    }
                }
            }
            c.disconnect()
        }
    }

    /** 依次尝试多个下载源（官方 + 镜像），任一成功即可。 */
    suspend fun downloadAny(urls: List<String>, dest: File, onProgress: ((Long, Long) -> Unit)? = null): Boolean {
        var last: Throwable? = null
        for (url in urls.distinct()) {
            try {
                download(url, dest, onProgress)
                return true
            } catch (t: Throwable) {
                last = t
                runCatching { dest.delete() }
            }
        }
        throw last ?: RuntimeException("下载失败：没有可用的下载源")
    }
}

object Mojang {
    private const val MANIFEST = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"
    private const val MIRROR = "https://bmclapi2.bangbang93.com/mc/game/version_manifest_v2.json"

    suspend fun manifest(): List<RemoteVersion> {
        val text = runCatching { Http.text(MANIFEST) }.getOrElse { Http.text(MIRROR) }
        val arr = JSONObject(text).getJSONArray("versions")
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            RemoteVersion(
                id = o.getString("id"),
                type = o.getString("type"),
                url = o.getString("url"),
                time = o.optString("time", ""),
                releaseTime = o.optString("releaseTime", ""),
            )
        }
    }

    suspend fun install(
        version: RemoteVersion,
        gameDir: File,
        onState: (InstallState) -> Unit,
    ) {
        fun emit(stage: String, done: Int, total: Int, detail: String) =
            onState(InstallState(version.id, stage, done, total, detail))

        emit("读取版本信息", 0, 1, version.id)
        val meta = fetchVersionJson(version)
        val id = meta.optString("id", version.id)
        emit("读取版本信息", 1, 1, id)

        val versionDir = File(gameDir, "versions/$id").apply { mkdirs() }
        val mirrorVersion = "https://bmclapi2.bangbang93.com/version/$id"

        // 客户端 jar：官方源失败时改用 BMCLAPI 镜像
        val client = meta.optJSONObject("downloads")?.optJSONObject("client")
        if (client != null) {
            val target = File(versionDir, "$id.jar")
            if (!target.isFile || target.length() == 0L) {
                emit("下载客户端", 0, 1, "$id.jar")
                Http.downloadAny(
                    listOfNotNull(client.optString("url").takeIf { it.isNotBlank() }, "$mirrorVersion/client"),
                    target,
                )
            }
        }

        // 依赖库：只补缺失文件
        val libraries = meta.optJSONArray("libraries") ?: JSONArray()
        val libTasks = mutableListOf<Pair<List<String>, File>>()
        for (i in 0 until libraries.length()) {
            val lib = libraries.getJSONObject(i)
            if (!allowed(lib.optJSONArray("rules"))) continue
            val downloads = lib.optJSONObject("downloads") ?: continue
            val artifact = downloads.optJSONObject("artifact") ?: continue
            val path = artifact.optString("path")
            if (path.isBlank()) continue
            val local = File(gameDir, "libraries/$path")
            if (local.isFile && local.length() == artifact.optLong("size", 0L)) continue
            val urls = buildList {
                artifact.optString("url").takeIf { it.isNotBlank() }?.let { add(it) }
                add("https://bmclapi2.bangbang93.com/maven/$path")
            }
            libTasks += urls to local
        }
        libTasks.forEachIndexed { index, (urls, local) ->
            emit("补全依赖库", index + 1, libTasks.size, local.name)
            runCatching { Http.downloadAny(urls, local) }
        }

        // 资源索引与资源文件
        val assetIndex = meta.optJSONObject("assetIndex")
        if (assetIndex != null) {
            val indexId = assetIndex.optString("id", id)
            val indexPath = File(gameDir, "assets/indexes/$indexId.json")
            if (!indexPath.isFile) {
                emit("下载资源索引", 0, 1, indexId)
                runCatching {
                    Http.downloadAny(
                        listOfNotNull(
                            assetIndex.optString("url").takeIf { it.isNotBlank() },
                            "https://bmclapi2.bangbang93.com/assets/indexes/$indexId.json",
                        ),
                        indexPath,
                    )
                }
            }
            if (indexPath.isFile) {
                val objects = runCatching { JSONObject(indexPath.readText()).optJSONObject("objects") }.getOrNull()
                val entries = mutableListOf<Pair<List<String>, File>>()
                objects?.keys()?.forEach { key ->
                    val obj = objects.optJSONObject(key) ?: return@forEach
                    val hash = obj.optString("hash")
                    if (hash.length < 2) return@forEach
                    val prefix = hash.substring(0, 2)
                    val local = File(gameDir, "assets/objects/$prefix/$hash")
                    if (!local.isFile || local.length() != obj.optLong("size", 0L)) {
                        entries += listOf(
                            "https://resources.download.minecraft.net/$prefix/$hash",
                            "https://bmclapi2.bangbang93.com/assets/$prefix/$hash",
                        ) to local
                    }
                }
                entries.forEachIndexed { index, (urls, local) ->
                    if (index % 20 == 0 || index == entries.lastIndex) {
                        emit("补全资源文件", index + 1, entries.size, "${index + 1}/${entries.size}")
                    }
                    runCatching { Http.downloadAny(urls, local) }
                }
            }
        }

        val jsonTarget = File(versionDir, "$id.json")
        jsonTarget.writeText(meta.toString())
        emit("安装完成", 1, 1, id)
        onState(InstallState(version.id, "安装完成", 1, 1, id, finished = true))
    }

    private suspend fun fetchVersionJson(version: RemoteVersion): JSONObject {
        return try {
            Http.json(version.url)
        } catch (t: Throwable) {
            Http.json("https://bmclapi2.bangbang93.com/version/${version.id}/json")
        }
    }

    private fun allowed(rules: JSONArray?): Boolean {
        if (rules == null) return true
        var result = false
        for (i in 0 until rules.length()) {
            val rule = rules.getJSONObject(i)
            val action = rule.optString("action", "allow")
            val os = rule.optJSONObject("os")?.optString("name", "")
            val matches = os.isNullOrBlank() || os == "android" || os == "linux"
            if (matches) result = action == "allow"
        }
        return result
    }

    suspend fun installFabric(
        minecraft: String,
        loader: String,
        gameDir: File,
        onState: (InstallState) -> Unit,
    ) {
        onState(InstallState(minecraft, "生成 Fabric 配置", 0, 1, loader))
        val profile = Http.json("https://meta.fabricmc.net/v2/versions/loader/$minecraft/$loader/profile/json")
        val id = profile.optString("id", "fabric-loader-$loader-$minecraft")
        val dir = File(gameDir, "versions/$id").apply { mkdirs() }
        File(dir, "$id.json").writeText(profile.toString())
        onState(InstallState(id, "安装完成", 1, 1, id, finished = true))
    }

    suspend fun fabricLoaders(minecraft: String): List<String> {
        val arr = JSONArray(Http.text("https://meta.fabricmc.net/v2/versions/loader/$minecraft"))
        return (0 until arr.length()).mapNotNull { arr.optJSONObject(it)?.optJSONObject("loader")?.optString("version") }
    }
}

object Modrinth {
    private const val API = "https://api.modrinth.com/v2/"

    suspend fun search(
        query: String,
        projectType: String,
        gameVersion: String?,
        loader: String?,
        index: String = "downloads",
        limit: Int = 20,
        offset: Int = 0,
    ): List<MrHit> {
        val facets = mutableListOf<JSONArray>()
        facets += JSONArray().put("project_type:$projectType")
        if (!gameVersion.isNullOrBlank() && gameVersion != "全部版本") facets += JSONArray().put("versions:$gameVersion")
        if (!loader.isNullOrBlank() && loader != "全部加载器") facets += JSONArray().put("categories:${loader.lowercase()}")
        val url = buildString {
            append(API).append("search?query=").append(URLEncoder.encode(query, "UTF-8"))
            append("&limit=").append(limit).append("&offset=").append(offset)
            append("&index=").append(index)
            if (facets.isNotEmpty()) append("&facets=").append(URLEncoder.encode(JSONArray(facets).toString(), "UTF-8"))
        }
        val root = try {
            Http.json(url)
        } catch (t: Throwable) {
            throw RuntimeException("无法连接 Modrinth，请检查网络后再试（部分地区可能需要加速）", t)
        }
        val hits = root.optJSONArray("hits") ?: return emptyList()
        return (0 until hits.length()).map { i ->
            val o = hits.getJSONObject(i)
            MrHit(
                id = o.optString("project_id"),
                slug = o.optString("slug"),
                title = o.optString("title"),
                description = o.optString("description"),
                iconUrl = o.optString("icon_url").takeIf { it.isNotBlank() && it != "null" },
                downloads = o.optLong("downloads"),
                author = o.optString("author"),
                projectType = o.optString("project_type"),
                categories = o.optJSONArray("categories")?.let { a -> (0 until a.length()).map { a.getString(it) } } ?: emptyList(),
                versions = o.optJSONArray("versions")?.let { a -> (0 until a.length()).map { a.getString(it) } } ?: emptyList(),
            )
        }
    }

    suspend fun versions(projectId: String, gameVersion: String?, loader: String?): List<MrVersion> {
        val url = buildString {
            append(API).append("project/").append(projectId).append("/version")
            val params = mutableListOf<String>()
            if (!gameVersion.isNullOrBlank() && gameVersion != "全部版本") {
                params += "game_versions=" + URLEncoder.encode("[\"$gameVersion\"]", "UTF-8")
            }
            if (!loader.isNullOrBlank() && loader != "全部加载器") {
                params += "loaders=" + URLEncoder.encode("[\"${loader.lowercase()}\"]", "UTF-8")
            }
            if (params.isNotEmpty()) append("?").append(params.joinToString("&"))
        }
        val arr = try {
            JSONArray(Http.text(url))
        } catch (t: Throwable) {
            throw RuntimeException("无法获取 Modrinth 版本列表，请检查网络后再试", t)
        }
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            val files = o.optJSONArray("files") ?: JSONArray()
            var file = if (files.length() > 0) files.getJSONObject(0) else null
            for (j in 0 until files.length()) {
                val f = files.getJSONObject(j)
                if (f.optBoolean("primary")) { file = f; break }
            }
            MrVersion(
                id = o.optString("id"),
                name = o.optString("name"),
                versionNumber = o.optString("version_number"),
                gameVersions = o.optJSONArray("game_versions")?.let { a -> (0 until a.length()).map { a.getString(it) } } ?: emptyList(),
                loaders = o.optJSONArray("loaders")?.let { a -> (0 until a.length()).map { a.getString(it) } } ?: emptyList(),
                fileUrl = file?.optString("url") ?: "",
                fileName = file?.optString("filename") ?: "",
                fileSize = file?.optLong("size") ?: 0L,
                datePublished = o.optString("date_published"),
            )
        }
    }

    suspend fun install(hit: MrHit, gameVersion: String?, loader: String?, destDir: File, onProgress: (Long, Long) -> Unit): String {
        val list = versions(hit.id, gameVersion, loader)
        val version = list.firstOrNull { it.fileUrl.isNotBlank() }
            ?: throw RuntimeException("该资源没有适用于当前版本的下载文件。")
        val target = File(destDir, version.fileName.ifBlank { "${hit.slug}.jar" })
        Http.download(version.fileUrl, target) { read, total -> onProgress(read, total) }
        return target.name
    }
}

class AuthService {
    companion object {
        const val CLIENT_ID = "fe72edc2-3a6f-4280-90e8-e2beb64ce7e1"
        const val AUTHLIB_SERVER = "https://littleskin.cn/api/yggdrasil"
    }

    data class DeviceCode(
        val userCode: String, val verificationUri: String, val deviceCode: String,
        val interval: Int, val expiresIn: Int,
    )

    suspend fun requestDeviceCode(): DeviceCode {
        val body = "client_id=$CLIENT_ID&scope=" + URLEncoder.encode("XboxLive.signin offline_access", "UTF-8")
        val o = Http.json(
            "https://login.microsoftonline.com/consumers/oauth2/v2.0/devicecode",
            method = "POST",
            headers = mapOf("Content-Type" to "application/x-www-form-urlencoded"),
            body = body,
        )
        return DeviceCode(
            userCode = o.optString("user_code"),
            verificationUri = o.optString("verification_uri", "https://www.microsoft.com/link"),
            deviceCode = o.optString("device_code"),
            interval = o.optInt("interval", 5),
            expiresIn = o.optInt("expires_in", 900),
        )
    }

    /** 轮询设备码，返回微软账户。onStatus 用于向界面反馈等待状态。 */
    suspend fun pollDeviceCode(device: DeviceCode, onStatus: (String) -> Unit): Account {
        val deadline = System.currentTimeMillis() + device.expiresIn * 1000L
        while (System.currentTimeMillis() < deadline) {
            delay(device.interval * 1000L)
            onStatus("等待浏览器中完成授权…")
            val body = "grant_type=" + URLEncoder.encode("urn:ietf:params:oauth:grant-type:device_code", "UTF-8") +
                "&client_id=$CLIENT_ID&device_code=" + URLEncoder.encode(device.deviceCode, "UTF-8")
            val result = try {
                Http.json(
                    "https://login.microsoftonline.com/consumers/oauth2/v2.0/token",
                    method = "POST",
                    headers = mapOf("Content-Type" to "application/x-www-form-urlencoded"),
                    body = body,
                )
            } catch (e: Exception) {
                null
            } ?: continue
            val access = result.optString("access_token").takeIf { it.isNotBlank() } ?: continue
            onStatus("正在验证 Xbox 账户…")
            return loginWithXbox(access)
        }
        throw RuntimeException("设备码已过期，请重新登录。")
    }

    private suspend fun loginWithXbox(msAccessToken: String): Account {
        val xbl = Http.json(
            "https://user.auth.xboxlive.com/user/authenticate",
            method = "POST",
            headers = mapOf("Content-Type" to "application/json", "Accept" to "application/json"),
            body = JSONObject()
                .put("Properties", JSONObject().put("AuthMethod", "RPS").put("SiteName", "user.auth.xboxlive.com").put("RpsTicket", "d=$msAccessToken"))
                .put("RelyingParty", "http://auth.xboxlive.com")
                .put("TokenType", "JWT")
                .toString(),
        )
        val xblToken = xbl.optString("Token")
        val uhs = xbl.optJSONObject("DisplayClaims")?.optJSONArray("xui")?.optJSONObject(0)?.optString("uhs") ?: ""

        val xsts = Http.json(
            "https://xsts.auth.xboxlive.com/xsts/authorize",
            method = "POST",
            headers = mapOf("Content-Type" to "application/json", "Accept" to "application/json"),
            body = JSONObject()
                .put("Properties", JSONObject().put("SandboxId", "RETAIL").put("UserTokens", JSONArray().put(xblToken)))
                .put("RelyingParty", "rp://api.minecraftservices.com/")
                .put("TokenType", "JWT")
                .toString(),
        )
        val xstsToken = xsts.optString("Token")

        val mc = Http.json(
            "https://api.minecraftservices.com/authentication/login_with_xbox",
            method = "POST",
            headers = mapOf("Content-Type" to "application/json", "Accept" to "application/json"),
            body = JSONObject().put("identityToken", "XBL3.0 x=$uhs;$xstsToken").toString(),
        )
        val mcToken = mc.optString("access_token")
        val profile = Http.json(
            "https://api.minecraftservices.com/minecraft/profile",
            headers = mapOf("Authorization" to "Bearer $mcToken"),
        )
        return Account(
            name = profile.optString("name", "Player"),
            kind = AccountKind.MICROSOFT,
            uuid = profile.optString("id", Account.offlineUuid(profile.optString("name", "Player"))),
            accessToken = mcToken,
        )
    }

    suspend fun authlibLogin(server: String, username: String, password: String): Account {
        val base = server.trim().trimEnd('/').ifBlank { AUTHLIB_SERVER }
        val result = Http.json(
            "$base/authserver/authenticate",
            method = "POST",
            headers = mapOf("Content-Type" to "application/json", "Accept" to "application/json"),
            body = JSONObject()
                .put("username", username)
                .put("password", password)
                .put("clientToken", "")
                .put("requestUser", true)
                .put("agent", JSONObject().put("name", "Minecraft").put("version", 1))
                .toString(),
        )
        val profile = result.optJSONObject("selectedProfile") ?: throw RuntimeException("该账户没有可用的角色。")
        return Account(
            name = profile.optString("name", username),
            kind = AccountKind.AUTHLIB,
            uuid = profile.optString("id", Account.offlineUuid(username)),
            accessToken = result.optString("accessToken", ""),
        )
    }
}

class LaunchManager(private val context: Context) {
    data class Target(val pkg: String, val label: String)

    fun installed(): List<Target> = Kernel.installedPackages(context).map { pkg ->
        Target(pkg, Kernel.label(context, pkg))
    }

    fun open(pkg: String): Boolean {
        val intent = context.packageManager.getLaunchIntentForPackage(pkg) ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return runCatching { context.startActivity(intent); true }.getOrDefault(false)
    }
}

object AiBot {
    fun reply(question: String, log: String): String {
        val q = question.lowercase(Locale.getDefault())
        val logHint = when {
            log.contains("OutOfMemoryError") -> "日志里出现了 OutOfMemoryError，说明内存真的不够用了。"
            log.contains("NoClassDefFoundError") || log.contains("ClassNotFoundException") -> "日志里出现了类缺失，通常是 Mod 与游戏版本不匹配。"
            log.contains("UnsupportedClassVersionError") -> "日志里出现了 Java 版本不兼容的报错。"
            log.contains("GLFW") || log.contains("LWJGL") -> "日志里出现了图形库报错，可能与渲染器或驱动有关。"
            log.contains("Mixin") -> "日志里出现了 Mixin 注入失败，通常是多个 Mod 互相冲突。"
            else -> ""
        }
        return when {
            q.contains("内存") || q.contains("outofmemory") || q.contains("卡") ->
                "把「最大内存」调到 4096 MB 左右再试一次，同时关掉后台其他应用。\nAndroid 设备内存本来就比电脑紧张，2 GB 以下很难带动大型整合包。$logHint"
            q.contains("mod") || q.contains("模组") || q.contains("mixin") ->
                "先确认 Mod 与游戏版本、加载器完全对应；只保留必要 Mod 逐个排查。\n如果日志里出现 Mixin 注入失败，大概率是冲突，可以先把最近添加的 Mod 移出 mods 文件夹。$logHint"
            q.contains("java") || q.contains("jvm") || q.contains("版本") ->
                "Android 端由内置运行时负责 Java 环境，不需要手动安装。\n如果是导入的桌面整合包，请检查它要求的 Java 版本（1.16 及以下用 Java 8，1.17+ 用 Java 17，1.20.5+ 用 Java 21）。$logHint"
            q.contains("崩溃") || q.contains("报错") || q.contains("打不开") || q.contains("闪退") ->
                "先看「更多 → 日志」里最后 50 行，重点找 Caused by。\n常见原因：Mod 冲突、内存不足、资源文件不完整。可以先用原版启动一次，确认基础环境正常。$logHint"
            q.contains("联机") || q.contains("服务器") ->
                "同一局域网内可以直接用「对局域网开放」；跨网络联机需要 EasyTier 之类的虚拟局域网工具，把房间码发给朋友再加入即可。"
            q.contains("皮肤") || q.contains("账号") ->
                "Microsoft 账户支持正版皮肤；LittleSkin 账户需要在皮肤站上传皮肤并把模型设为 classic/slim。离线账户只影响本地显示，不影响单机游玩。"
            logHint.isNotBlank() -> logHint + "\n需要我继续分析具体某一行日志吗？把那一行发给我就行。"
            else ->
                "我在呢。可以问我启动失败、Mod 冲突、内存设置、Java 版本这类问题；也可以把日志里最关键的一行发给我，我帮你看看。"
        }
    }
}
