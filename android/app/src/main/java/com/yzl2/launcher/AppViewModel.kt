package com.yzl2.launcher

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yzl2.launcher.data.Account
import com.yzl2.launcher.data.AccountKind
import com.yzl2.launcher.data.AiBot
import com.yzl2.launcher.data.AiMessage
import com.yzl2.launcher.data.AuthService
import com.yzl2.launcher.data.Config
import com.yzl2.launcher.data.ErrorInfo
import com.yzl2.launcher.data.InstallState
import com.yzl2.launcher.data.InstalledVersion
import com.yzl2.launcher.data.Kernel
import com.yzl2.launcher.data.KernelState
import com.yzl2.launcher.data.LaunchManager
import com.yzl2.launcher.data.Loader
import com.yzl2.launcher.data.Mojang
import com.yzl2.launcher.data.Modrinth
import com.yzl2.launcher.data.MrHit
import com.yzl2.launcher.data.Page
import com.yzl2.launcher.data.RemoteVersion
import com.yzl2.launcher.data.Repo
import com.yzl2.launcher.data.clockText
import com.yzl2.launcher.data.dateText
import com.yzl2.launcher.data.dayPeriod
import com.yzl2.launcher.data.greeting
import com.yzl2.launcher.data.isVideoFileName
import com.yzl2.launcher.data.relativeTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.File

class AppViewModel(app: Application) : AndroidViewModel(app) {

    val repo = Repo(app)
    private val auth = AuthService()
    private val launcher = LaunchManager(app)

    var config by mutableStateOf(repo.loadConfig()); private set
    var accounts by mutableStateOf(repo.loadAccounts()); private set
    var account by mutableStateOf(
        repo.loadSelectedAccount() ?: repo.loadAccounts().firstOrNull() ?: Account()
    ); private set

    var page by mutableStateOf(Page.HOME); private set
    var splash by mutableStateOf(true); private set

    var versions by mutableStateOf<List<InstalledVersion>>(emptyList()); private set
    var selectedVersionId by mutableStateOf<String?>(null); private set
    val selectedVersion: InstalledVersion?
        get() = versions.firstOrNull { it.id == (selectedVersionId ?: config.lastVersion) } ?: versions.firstOrNull()

    var manifest by mutableStateOf<List<RemoteVersion>>(emptyList()); private set
    var manifestLoading by mutableStateOf(false); private set
    var manifestError by mutableStateOf<String?>(null); private set

    var installState by mutableStateOf<InstallState?>(null); private set
    private var installJob: Job? = null
    private var manifestJob: Job? = null
    private var searchJob: Job? = null

    // 内核（ZL2 / Pojav）下载状态
    var kernelState by mutableStateOf<KernelState?>(null); private set
    var pendingKernelApk by mutableStateOf<File?>(null); private set
    var kernelPackages by mutableStateOf<List<String>>(Kernel.installedPackages(app)); private set
    private var kernelJob: Job? = null

    var toastText by mutableStateOf<String?>(null); private set
    private var toastJob: Job? = null

    var accountSheet by mutableStateOf(false); private set
    var aiSheet by mutableStateOf(false); private set
    var linkSheet by mutableStateOf(false); private set
    var infoDialog by mutableStateOf<InfoDialog?>(null); private set

    var launching by mutableStateOf(false); private set
    var launchStatus by mutableStateOf("正在准备启动…"); private set

    var errorInfo by mutableStateOf<ErrorInfo?>(null); private set

    // 账户登录状态
    var msCode by mutableStateOf<String?>(null); private set
    var msStatus by mutableStateOf(""); private set
    var msBusy by mutableStateOf(false); private set
    var authError by mutableStateOf<String?>(null); private set
    private var msJob: Job? = null

    // 资源页
    var contentQuery by mutableStateOf(""); private set
    var contentType by mutableStateOf("mod"); private set
    var contentGameVersion by mutableStateOf("全部版本"); private set
    var contentLoader by mutableStateOf("全部加载器"); private set
    var contentSort by mutableStateOf("下载量"); private set
    var contentResults by mutableStateOf<List<MrHit>>(emptyList()); private set
    var contentLoading by mutableStateOf(false); private set
    var contentSearched by mutableStateOf(false); private set
    var contentError by mutableStateOf<String?>(null); private set
    var searchRequestCount by mutableStateOf(0); private set

    // AI 对话
    var aiMessages by mutableStateOf(
        mutableListOf(
            AiMessage(
                "你好呀，小家伙～我是柚子姐姐，住在 YZL2 里的本地 AI。可以帮你分析启动日志、Java / Mod 报错和内存设置。所有内容只在设备本地处理，放心说吧。",
                fromUser = false
            )
        )
    ); private set
    var aiTyping by mutableStateOf(false); private set

    init {
        refreshVersions(selectFirst = true)
        viewModelScope.launch {
            delay(1500)
            splash = false
        }
    }

    // ---------------- 基础 ----------------

    fun updateConfig(block: (Config) -> Config) {
        config = block(config)
        repo.saveConfig(config)
    }

    fun skipSplash() { splash = false }

    fun selectPage(p: Page) {
        if (page != p) page = p
        if (p == Page.DOWNLOAD && manifest.isEmpty() && !manifestLoading) loadManifest()
    }

    fun showToast(text: String) {
        toastText = text
        toastJob?.cancel()
        toastJob = viewModelScope.launch {
            delay(2400)
            toastText = null
        }
    }

    fun openAccountSheet() { accountSheet = true }
    fun closeAccountSheet() { accountSheet = false; authError = null }

    fun openAi() { aiSheet = true }
    fun closeAi() { aiSheet = false }

    fun openLink() { linkSheet = true }
    fun closeLink() { linkSheet = false }

    fun dismissError() { errorInfo = null }

    fun dismissInfo() { infoDialog = null }

    fun showInfo(title: String, body: String) { infoDialog = InfoDialog(title, body) }

    fun clearAuthError() { authError = null }

    fun copyToClipboard(text: String) {
        val manager = getApplication<Application>().getSystemService(android.content.ClipboardManager::class.java)
        manager?.setPrimaryClip(android.content.ClipData.newPlainText("YZL2", text))
    }

    // ---------------- 版本 ----------------

    fun refreshVersions(selectFirst: Boolean = false) {
        val list = repo.scanVersions(config)
        versions = list
        if (list.isEmpty()) {
            selectedVersionId = null
            return
        }
        val keep = selectedVersionId ?: config.lastVersion
        if (list.none { it.id == keep }) {
            selectedVersionId = list.first().id
        } else if (selectFirst && selectedVersionId == null) {
            selectedVersionId = list.first().id
        }
    }

    fun selectVersion(id: String) {
        selectedVersionId = id
        updateConfig { it.copy(lastVersion = id) }
    }

    fun gameDirPath(): String = repo.gameDir(config).absolutePath

    fun openGameFolder() {
        val path = gameDirPath()
        showInfo(
            "游戏目录",
            "$path\n\n可以在文件管理器中打开 Android/data/com.yzl2.launcher/files/minecraft 查看版本、Mods 与存档。",
        )
    }

    fun openLog() {
        val log = repo.readLatestLog(config, 20000)
        showInfo("最近启动日志", if (log.isBlank()) "还没有日志文件，启动一次游戏后就会生成。" else log.takeLast(6000))
    }

    fun rescan() {
        refreshVersions()
        showToast("已重新扫描 ${versions.size} 个版本")
    }

    fun loadManifest(force: Boolean = false) {
        if (manifestLoading && !force) return
        if (manifest.isNotEmpty() && !force) return
        manifestJob?.cancel()
        manifestLoading = true
        manifestError = null
        manifestJob = viewModelScope.launch {
            runCatching { withTimeout(30_000) { Mojang.manifest() } }
                .onSuccess { manifest = it; manifestLoading = false }
                .onFailure {
                    manifestLoading = false
                    manifestError = it.message ?: "版本清单加载失败"
                }
        }
    }

    fun install(remote: RemoteVersion, loader: Loader = Loader.VANILLA) {
        if (installState?.finished == false) {
            showToast("已有安装任务正在进行")
            return
        }
        showToast("开始安装 ${remote.id}…")
        installJob = viewModelScope.launch {
            installState = InstallState(remote.id, "准备中", 0, 1, remote.id)
            runCatching {
                val dir = repo.gameDir(config)
                Mojang.install(remote, dir) { state -> installState = state }
                if (loader != Loader.VANILLA) {
                    installState = InstallState(remote.id, "生成加载器配置", 0, 1, loader.label)
                    val loaderVersion = runCatching { Mojang.fabricLoaders(remote.id).firstOrNull() }.getOrNull()
                    if (loader == Loader.FABRIC && loaderVersion != null) {
                        Mojang.installFabric(remote.id, loaderVersion, dir) { state -> installState = state }
                    }
                }
            }.onSuccess {
                installState = installState?.copy(stage = "安装完成", finished = true)
                refreshVersions()
                showToast("${remote.id} 安装完成")
                viewModelScope.launch {
                    delay(1200)
                    installState = null
                    // 版本装好后自动准备运行环境：Java 由 ZL2 / Pojav 内核自带，
                    // 所以这里自动下载内核安装包并交给系统安装器。
                    if (config.autoKernel && !Kernel.installed(getApplication())) ensureKernel(auto = true)
                }
            }.onFailure {
                installState = null
                errorInfo = ErrorInfo(
                    title = "安装失败", subtitle = remote.id,
                    detail = it.message ?: "未知错误",
                    suggestion = "检查网络连接后重试；国内网络可尝试切换镜像源。",
                )
            }
        }
    }

    fun cancelInstall() {
        installJob?.cancel()
        installJob = null
        installState = null
        showToast("已取消安装")
    }

    // ---------------- 内核 / 运行环境 ----------------

    fun installedKernelNames(): String {
        val app = getApplication<Application>()
        if (kernelPackages.isEmpty()) return ""
        return kernelPackages.joinToString("、") { Kernel.label(app, it) }
    }

    /** 安装/卸载内核后回到应用时刷新状态。 */
    fun refreshKernelStatus() {
        kernelPackages = Kernel.installedPackages(getApplication())
    }

    fun kernelInstalled(): Boolean = kernelPackages.isNotEmpty()

    fun ensureKernel(auto: Boolean = false) {
        val app = getApplication<Application>()
        if (Kernel.installed(app)) {
            if (!auto) showToast("ZL2 内核已安装")
            return
        }
        if (kernelJob?.isActive == true) return
        kernelJob = viewModelScope.launch {
            kernelState = KernelState("正在获取内核版本")
            runCatching {
                val apk = Kernel.download(app) { done, total ->
                    kernelState = KernelState("正在下载 ZL2 内核", done, total)
                }
                kernelState = KernelState("内核下载完成，准备安装", 1, 1, finished = true)
                pendingKernelApk = apk
            }.onFailure { error ->
                kernelState = null
                if (auto) {
                    showToast("内核下载失败：${error.message ?: "未知错误"}")
                } else {
                    errorInfo = ErrorInfo(
                        title = "内核下载失败",
                        subtitle = "ZL2 / Pojav 内核",
                        detail = error.message ?: "未知错误",
                        suggestion = "检查网络后重试；也可以手动安装 ZL2 启动器（ZalithLauncher）。",
                    )
                }
            }
        }
    }

    fun cancelKernel() {
        kernelJob?.cancel()
        kernelJob = null
        kernelState = null
        showToast("已取消内核下载")
    }

    fun onKernelInstallerOpened() {
        pendingKernelApk = null
        kernelState = null
        viewModelScope.launch {
            delay(1200)
            refreshKernelStatus()
            if (kernelPackages.isNotEmpty()) showToast("内核已就绪，现在可以启动游戏了")
        }
    }

    fun onKernelInstallFailed(message: String) {
        pendingKernelApk = null
        kernelState = null
        errorInfo = ErrorInfo(
            title = "无法打开安装器",
            subtitle = "ZL2 / Pojav 内核",
            detail = message,
            suggestion = "在系统设置里允许 YZL2 安装未知来源应用，然后到「设置 → Java 与内存」重新下载内核。",
        )
    }

    // ---------------- 自定义背景 ----------------

    fun importBackground(uri: Uri) {
        val app = getApplication<Application>()
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    val name = displayName(app, uri)
                    val dir = File(app.filesDir, "background").apply { mkdirs() }
                    val dest = File(dir, "${System.currentTimeMillis()}_${sanitize(name)}")
                    app.contentResolver.openInputStream(uri)?.use { input ->
                        dest.outputStream().use { output -> input.copyTo(output) }
                    } ?: throw RuntimeException("无法读取所选文件")
                    dest to name
                }
            }.onSuccess { (file, name) ->
                updateConfig { it.copy(backgroundImage = file.absolutePath) }
                showToast(if (isVideoFileName(name)) "背景视频已设置，会循环播放" else "背景图片已设置")
            }.onFailure {
                errorInfo = ErrorInfo(
                    title = "背景设置失败",
                    subtitle = "读取所选文件时出错",
                    detail = it.message ?: "未知错误",
                    suggestion = "换一个文件再试；也可以先把文件保存到手机本地再选择。",
                )
            }
        }
    }

    fun importMusic(uri: Uri) {
        val app = getApplication<Application>()
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    val name = displayName(app, uri)
                    val dir = File(app.filesDir, "music").apply { mkdirs() }
                    val dest = File(dir, "${System.currentTimeMillis()}_${sanitize(name)}")
                    app.contentResolver.openInputStream(uri)?.use { input ->
                        dest.outputStream().use { output -> input.copyTo(output) }
                    } ?: throw RuntimeException("无法读取所选文件")
                    dest
                }
            }.onSuccess { file ->
                updateConfig { it.copy(backgroundMusic = file.absolutePath, musicAutoPlay = true) }
                showToast("背景音乐已设置")
            }.onFailure {
                errorInfo = ErrorInfo(
                    title = "背景音乐设置失败",
                    subtitle = "读取所选文件时出错",
                    detail = it.message ?: "未知错误",
                    suggestion = "支持 mp3 / ogg / flac 等常见音频格式，换一首再试。",
                )
            }
        }
    }

    fun clearBackground() {
        updateConfig { it.copy(backgroundImage = "") }
        showToast("已恢复默认背景")
    }

    fun clearMusic() {
        updateConfig { it.copy(backgroundMusic = "") }
        showToast("已关闭背景音乐")
    }

    private fun displayName(app: Application, uri: Uri): String {
        val fallback = "file_${System.currentTimeMillis()}"
        return runCatching {
            app.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) cursor.getString(0) else null
                }
        }.getOrNull()?.takeIf { it.isNotBlank() } ?: fallback
    }

    private fun sanitize(name: String): String =
        name.replace(Regex("[\\\\/:*?\"<>|]"), "_").takeLast(120).ifBlank { "file" }

    // ---------------- 账户 ----------------

    private fun persistAccounts(list: List<Account>) {
        accounts = list
        repo.saveAccounts(list)
    }

    fun selectAccount(a: Account) {
        account = a
        repo.saveSelectedAccount(a)
        showToast("已切换为 ${a.name}")
    }

    fun addOfflineAccount(name: String): Boolean {
        val n = name.trim()
        if (n.isEmpty()) { authError = "请输入玩家名称"; return false }
        val a = Account(name = n, kind = AccountKind.OFFLINE, uuid = Account.offlineUuid(n))
        addAccount(a)
        return true
    }

    private fun addAccount(a: Account) {
        val list = accounts.filterNot { it.name == a.name && it.kind == a.kind } + a
        persistAccounts(list)
        account = a
        repo.saveSelectedAccount(a)
        showToast("已登录 ${a.name}")
        authError = null
    }

    fun removeAccount(a: Account) {
        persistAccounts(accounts.filterNot { it.name == a.name && it.kind == a.kind })
        if (account.name == a.name && account.kind == a.kind) {
            val next = accounts.firstOrNull() ?: Account()
            account = next
            repo.saveSelectedAccount(next)
        }
    }

    fun startMicrosoftLogin() {
        if (msBusy) return
        msBusy = true
        authError = null
        msCode = null
        msStatus = "正在获取登录代码…"
        msJob = viewModelScope.launch {
            runCatching {
                val code = auth.requestDeviceCode()
                msCode = code.userCode
                msStatus = "请在浏览器输入下方代码"
                auth.pollDeviceCode(code) { status -> msStatus = status }
            }.onSuccess {
                addAccount(it)
                msBusy = false
                msCode = null
                delay(600)
            }.onFailure {
                msBusy = false
                authError = it.message ?: "微软登录失败"
            }
        }
    }

    fun cancelMicrosoftLogin() {
        msJob?.cancel()
        msJob = null
        msBusy = false
        msCode = null
        msStatus = ""
    }

    fun loginAuthlib(server: String, username: String, password: String) {
        if (msBusy) return
        if (username.isBlank() || password.isBlank()) { authError = "请输入邮箱与密码"; return }
        msBusy = true
        authError = null
        msStatus = "正在连接 LittleSkin…"
        viewModelScope.launch {
            runCatching { auth.authlibLogin(server, username, password) }
                .onSuccess { addAccount(it); msBusy = false; msStatus = "" }
                .onFailure { msBusy = false; authError = it.message ?: "第三方登录失败" }
        }
    }

    // ---------------- 启动 ----------------

    fun installedLaunchers() = launcher.installed()

    fun launch() {
        if (launching) return
        val version = selectedVersion ?: run {
            errorInfo = ErrorInfo(
                title = "还没有可启动的版本", subtitle = "版本列表为空",
                detail = "当前游戏目录里没有找到任何版本。先去「下载」页安装一个吧。",
                suggestion = "打开「下载」页 → 选择版本 → 安装，完成后回到这里启动。",
            )
            return
        }
        launching = true
        launchStatus = "正在整理你的方块世界…"
        viewModelScope.launch {
            val steps = listOf(
                "正在整理你的方块世界…",
                "检查版本文件完整性…",
                "读取账户 ${account.name}…",
                "分配 ${config.memoryMb} MB 内存…",
                "正在与方块世界建立连接…",
            )
            steps.forEach {
                launchStatus = it
                delay(420)
            }
            val targets = launcher.installed()
            val target = targets.firstOrNull()
            if (target != null && launcher.open(target.pkg)) {
                updateConfig { c -> c.copy(lastPlayed = c.lastPlayed + (version.id to System.currentTimeMillis())) }
                refreshVersions()
                launching = false
                showToast("已交给 ${target.label} 启动 ${version.id}")
            } else {
                launching = false
                errorInfo = ErrorInfo(
                    title = "这一版还不能直接进游戏",
                    subtitle = "缺少 ZL2 / Pojav 内核",
                    detail = "YZL2 的安卓移植版包含完整的启动器界面、账户、版本下载与 Modrinth 资源安装。\n" +
                        "真正运行 Minecraft Java 版需要 Pojav / ZL2 内核提供的 JVM 与图形层，Java 由内核自带。",
                    suggestion = "在「设置 → Java 与内存」点「自动下载内核」，或在下载版本时让 YZL2 自动准备内核；装好后点启动会自动交给它运行。",
                    log = "检测到已安装的内核：${if (targets.isEmpty()) "无" else targets.joinToString { it.label }}",
                )
            }
        }
    }

    // ---------------- 资源 ----------------

    fun changeContentQuery(q: String) { contentQuery = q }
    fun changeContentType(t: String) { contentType = t }
    fun changeContentGameVersion(v: String) { contentGameVersion = v }
    fun changeContentLoader(l: String) { contentLoader = l }
    fun changeContentSort(s: String) { contentSort = s }

    fun searchContent() {
        searchJob?.cancel()
        searchRequestCount += 1
        contentLoading = true
        contentError = null
        contentSearched = true
        val query = contentQuery.trim()
        val type = contentType
        val gameVersion = contentGameVersion
        val loader = contentLoader
        val sort = contentSort
        searchJob = viewModelScope.launch {
            runCatching {
                withTimeout(30_000) {
                    Modrinth.search(
                        query = query,
                        projectType = type,
                        gameVersion = gameVersion,
                        loader = loader,
                        index = when (sort) {
                            "下载量" -> "downloads"
                            "最近更新" -> "updated"
                            "关注度" -> "follows"
                            else -> "relevance"
                        },
                    )
                }
            }.onSuccess {
                contentResults = it
                contentLoading = false
            }.onFailure {
                contentLoading = false
                contentError = it.message ?: "搜索失败"
            }
        }
    }

    fun debugSetManifest(list: List<RemoteVersion>) { manifest = list }

    fun installContent(hit: MrHit) {
        val version = selectedVersion
        if (version == null) {
            showToast("请先在「版本」页安装并选择一个版本")
            return
        }
        val gameVersion = version.clientVersion
        val loader = version?.loader?.label?.lowercase()
        viewModelScope.launch {
            showToast("正在获取 ${hit.title} 的下载地址…")
            runCatching {
                val dest = if (contentType == "mod") repo.modsDir(config, version) else repo.gameDir(config)
                Modrinth.install(hit, gameVersion, if (contentType == "mod") loader else null, dest) { _, _ -> }
            }.onSuccess { showToast("已安装 ${hit.title}") }
                .onFailure { errorInfo = ErrorInfo("安装失败", hit.title, it.message ?: "下载失败", "检查网络后重试。") }
        }
    }

    // ---------------- AI ----------------

    fun sendAi(text: String) {
        val q = text.trim()
        if (q.isEmpty() || aiTyping) return
        aiMessages.add(AiMessage(q, fromUser = true))
        aiTyping = true
        viewModelScope.launch {
            delay(700)
            val reply = AiBot.reply(q, repo.readLatestLog(config))
            aiMessages.add(AiMessage(reply, fromUser = false))
            aiTyping = false
        }
    }

    fun clearAi() {
        aiMessages.clear()
        aiMessages.add(AiMessage("对话清空啦。还有哪里不明白的，随时问我～", fromUser = false))
    }

    // ---------------- 派生文本 ----------------

    fun playerName(): String = account.name
    fun accountTypeLabel(): String = account.kind.label
    fun accountInitial(): String = account.name.take(1).uppercase()

    fun versionTitle(): String = selectedVersion?.id ?: "未安装版本"
    fun versionTypeLabel(): String = selectedVersion?.typeLabel ?: "正式版"
    fun versionClient(): String = selectedVersion?.clientVersion ?: "-"
    fun versionLoaderLabel(): String = selectedVersion?.loader?.label ?: "原版"
    fun modsCount(): Int = selectedVersion?.mods ?: 0
    fun savesCount(): Int = selectedVersion?.saves ?: 0

    fun lastPlayedText(): String {
        val v = selectedVersion ?: return "尚未启动"
        val t = config.lastPlayed[v.id] ?: v.lastPlayed
        return t.relativeTime()
    }

    fun javaSummary(): String = "Android 运行时 · 内存 ${config.memoryMb} MB"

    fun launchHint(): String = "Java · ${config.memoryMb} MB · ${config.windowWidth} × ${config.windowHeight}"

    fun greetingText(): String = greeting()
    fun heroTimeLine(): String = "${dateText()} · ${dayPeriod()} ${clockText()}"
    fun chipText(): String = "今日份的冒险 · ${clockText()}"

    fun openLauncherDownload() {
        infoDialog = InfoDialog(
            "获取 ZL2 内核",
            "ZL2 内核（ZalithLauncher）自带 Java 运行时与图形层。\n\n" +
                "点「设置 → Java 与内存 → 自动下载内核」即可由 YZL2 自动下载安装包并调起系统安装器；" +
                "也可以在下载版本时自动准备。装好后点启动会自动交给它运行。",
        )
    }

    data class InfoDialog(val title: String, val body: String)
}
