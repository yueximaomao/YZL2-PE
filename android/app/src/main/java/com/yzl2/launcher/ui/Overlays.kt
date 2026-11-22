package com.yzl2.launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yzl2.launcher.AppViewModel
import com.yzl2.launcher.data.Account
import com.yzl2.launcher.data.AiMessage
import com.yzl2.launcher.data.AuthService
import com.yzl2.launcher.data.ErrorInfo

@Composable
fun AccountSheet(vm: AppViewModel) {
    var tab by remember { mutableStateOf("微软登录") }
    var offlineName by remember { mutableStateOf(vm.config.playerName) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var server by remember { mutableStateOf(AuthService.AUTHLIB_SERVER) }
    BoxWithConstraints(Modifier.fillMaxSize().background(Color(0x660A1626)), contentAlignment = Alignment.Center) {
        val cardWidth = minOf(780.dp, maxWidth * 0.94f)
        val cardHeight = minOf(520.dp, maxHeight * 0.92f)
        CardBox(
            Modifier.width(cardWidth).height(cardHeight),
            corner = 16.dp,
            shadow = 24.dp,
        ) {
            Row(Modifier.fillMaxSize()) {
                // 左：账户列表
                Column(
                    Modifier
                        .width(258.dp)
                        .fillMaxSize()
                        .background(Color(0xFFF7FAFE))
                        .padding(18.dp),
                ) {
                    Text("账户", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Yzl.TextPrimary)
                    Spacer(Modifier.height(2.dp))
                    Text("选择要用于启动游戏的账户", fontSize = 10.5.sp, color = Yzl.TextMuted)
                    Spacer(Modifier.height(12.dp))
                    Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                        val list = vm.accounts.ifEmpty { listOf(vm.account) }
                        list.forEach { acc ->
                            AccountRow(acc, vm.account.name == acc.name) { vm.selectAccount(acc) }
                            Spacer(Modifier.height(4.dp))
                        }
                    }
                    YzlButton("刷新登录", { vm.showToast("已刷新账户状态") }, Modifier.fillMaxWidth(), height = 34.dp, fontSize = 11.5.sp)
                    Spacer(Modifier.height(6.dp))
                    YzlButton("删除当前账户", {
                        vm.removeAccount(vm.account)
                        vm.showToast("已删除当前账户")
                    }, Modifier.fillMaxWidth(), style = YzlButtonStyle.Ghost, height = 30.dp, fontSize = 11.sp)
                }
                // 右：登录面板
                Column(Modifier.weight(1f).fillMaxSize().padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("账户管理", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Yzl.TextPrimary)
                        Spacer(Modifier.weight(1f))
                        CloseButton { vm.closeAccountSheet() }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("登录后即可使用正版皮肤、正版服务器与云端角色。", fontSize = 11.sp, color = Yzl.TextSecondary)
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("微软登录", "离线登录", "第三方登录").forEach { t ->
                            YzlTab(t, tab == t, soft = true) { tab = t; vm.clearAuthError() }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    when (tab) {
                        "微软登录" -> MicrosoftPane(vm)
                        "离线登录" -> OfflinePane(vm, offlineName, { offlineName = it })
                        else -> AuthlibPane(vm, server, { server = it }, email, { email = it }, password, { password = it })
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountRow(account: Account, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) Yzl.AccentSoft else Color.Transparent)
            .border(1.dp, if (selected) Color(0xFFC8DDFA) else Color.Transparent, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(32.dp).clip(CircleShape).background(if (selected) Color.White else Color(0xFFEFF4FB)),
            contentAlignment = Alignment.Center,
        ) {
            Text(account.name.take(1).uppercase(), color = Yzl.Accent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(9.dp))
        Column(Modifier.weight(1f)) {
            Text(account.name, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = Yzl.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(2.dp))
            Text(account.kind.label, fontSize = 10.sp, color = Yzl.TextMuted)
        }
    }
}

@Composable
private fun MicrosoftPane(vm: AppViewModel) {
    Text("使用 Microsoft 账户登录", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Yzl.TextPrimary)
    Spacer(Modifier.height(6.dp))
    Text(
        "点击下方按钮获取微软官方登录代码，在浏览器中完成登录后会自动返回。无需在启动器里输入密码。",
        fontSize = 11.sp,
        color = Yzl.TextSecondary,
        lineHeight = 16.sp,
    )
    Spacer(Modifier.height(12.dp))
    if (vm.msCode != null) {
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Yzl.AccentSoft)
                .border(1.dp, Color(0xFFD5E6FB), RoundedCornerShape(12.dp))
                .padding(16.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(vm.msCode!!, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Yzl.AccentDeep, letterSpacing = 4.sp)
                Spacer(Modifier.height(6.dp))
                Text(vm.msStatus.ifBlank { "请在浏览器中打开 microsoft.com/link 并输入代码" }, fontSize = 11.sp, color = Yzl.TextSecondary)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    YzlButton("复制代码", { vm.copyToClipboard(vm.msCode ?: ""); vm.showToast("登录代码已复制") }, height = 30.dp, fontSize = 11.sp)
                    YzlButton("取消登录", { vm.cancelMicrosoftLogin() }, style = YzlButtonStyle.Ghost, height = 30.dp, fontSize = 11.sp)
                }
            }
        }
    } else {
        YzlButton("开始微软登录", { vm.startMicrosoftLogin() }, style = YzlButtonStyle.Primary, height = 38.dp, fontSize = 13.sp, enabled = !vm.msBusy)
        if (vm.msBusy) {
            Spacer(Modifier.height(10.dp))
            Text(vm.msStatus, fontSize = 11.sp, color = Yzl.TextSecondary)
        }
    }
    vm.authError?.let {
        Spacer(Modifier.height(10.dp))
        Text(it, fontSize = 11.sp, color = Yzl.Danger)
    }
    Spacer(Modifier.height(18.dp))
    Text("登录令牌会加密保存在设备本地，不会上传到 YZL2。", fontSize = 10.5.sp, color = Yzl.TextMuted)
}

@Composable
private fun OfflinePane(vm: AppViewModel, name: String, onName: (String) -> Unit) {
    Text("离线登录", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Yzl.TextPrimary)
    Spacer(Modifier.height(6.dp))
    Text("离线账户仅在本设备生效，适合单机与局域网联机。", fontSize = 11.sp, color = Yzl.TextSecondary)
    Spacer(Modifier.height(12.dp))
    SectionLabel("玩家名称")
    Spacer(Modifier.height(6.dp))
    YzlField(name, onName, "Steve", Modifier.width(280.dp))
    Spacer(Modifier.height(12.dp))
    YzlButton("添加离线账户", {
        if (vm.addOfflineAccount(name)) {
            vm.updateConfig { c -> c.copy(playerName = name) }
        }
    }, style = YzlButtonStyle.Primary, height = 36.dp, fontSize = 12.5.sp)
    vm.authError?.let {
        Spacer(Modifier.height(10.dp))
        Text(it, fontSize = 11.sp, color = Yzl.Danger)
    }
}

@Composable
private fun AuthlibPane(
    vm: AppViewModel, server: String, onServer: (String) -> Unit,
    email: String, onEmail: (String) -> Unit, password: String, onPassword: (String) -> Unit,
) {
    Text("第三方登录 · Authlib Injector", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Yzl.TextPrimary)
    Spacer(Modifier.height(6.dp))
    Text("支持 LittleSkin 等兼容 Yggdrasil 协议的皮肤站账户。", fontSize = 11.sp, color = Yzl.TextSecondary)
    Spacer(Modifier.height(12.dp))
    SectionLabel("验证服务器")
    Spacer(Modifier.height(6.dp))
    YzlField(server, onServer, AuthService.AUTHLIB_SERVER, Modifier.fillMaxWidth(), fontSize = 11.5.sp)
    Spacer(Modifier.height(10.dp))
    SectionLabel("邮箱 / 用户名")
    Spacer(Modifier.height(6.dp))
    YzlField(email, onEmail, "you@example.com", Modifier.fillMaxWidth())
    Spacer(Modifier.height(10.dp))
    SectionLabel("密码")
    Spacer(Modifier.height(6.dp))
    YzlField(password, onPassword, "••••••••", Modifier.fillMaxWidth(), password = true)
    Spacer(Modifier.height(12.dp))
    YzlButton("登录", { vm.loginAuthlib(server, email, password) }, style = YzlButtonStyle.Primary, height = 36.dp, fontSize = 12.5.sp, enabled = !vm.msBusy)
    vm.authError?.let {
        Spacer(Modifier.height(10.dp))
        Text(it, fontSize = 11.sp, color = Yzl.Danger)
    }
}

@Composable
fun AiSheet(vm: AppViewModel) {
    var input by remember { mutableStateOf("") }
    val state = rememberLazyListState()
    LaunchedEffect(vm.aiMessages.size) {
        if (vm.aiMessages.isNotEmpty()) state.animateScrollToItem(vm.aiMessages.lastIndex)
    }
    BoxWithConstraints(Modifier.fillMaxSize().background(Color(0x730A1626)), contentAlignment = Alignment.Center) {
        val cardWidth = minOf(840.dp, maxWidth * 0.94f)
        val cardHeight = minOf(540.dp, maxHeight * 0.92f)
        CardBox(
            Modifier.width(cardWidth).height(cardHeight),
            corner = 16.dp,
            shadow = 24.dp,
        ) {
            Column(Modifier.fillMaxSize().padding(22.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(Yzl.AccentSoft), contentAlignment = Alignment.Center) {
                        Icon(Glyph.Sparkle, null, tint = Yzl.Accent, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("YZL2 AI 助手 · 柚子姐姐", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Yzl.TextPrimary)
                        Spacer(Modifier.height(2.dp))
                        Text("离线诊断 · 不联网 · 日志只在本地处理", fontSize = 10.5.sp, color = Yzl.TextMuted)
                    }
                    Spacer(Modifier.weight(1f))
                    Text("清空对话", fontSize = 11.5.sp, color = Yzl.TextSecondary, modifier = Modifier.clickable { vm.clearAi() })
                    Spacer(Modifier.width(14.dp))
                    CloseButton { vm.closeAi() }
                }
                Spacer(Modifier.height(14.dp))
                LazyColumn(Modifier.weight(1f).fillMaxWidth(), state = state, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(vm.aiMessages) { msg -> AiBubble(msg) }
                    if (vm.aiTyping) {
                        item {
                            Text("柚子姐姐正在思考…", fontSize = 11.sp, color = Yzl.TextMuted, modifier = Modifier.padding(start = 6.dp))
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("分析最近的游戏报错", "Java 版本不兼容怎么办", "内存不足怎么设置").forEach { s ->
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF3F7FD))
                                .clickable { vm.sendAi(s) }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                        ) { Text(s, fontSize = 10.5.sp, color = Yzl.TextSecondary) }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    YzlField(input, { input = it }, "问 AI 一个关于启动、Java、Mod 或崩溃日志的问题…", Modifier.weight(1f))
                    YzlButton("发送", {
                        if (input.isNotBlank()) {
                            vm.sendAi(input)
                            input = ""
                        }
                    }, style = YzlButtonStyle.Primary, height = 38.dp, fontSize = 13.sp)
                }
                Spacer(Modifier.height(8.dp))
                Text("离线规则引擎 · 不会上传任何内容", fontSize = 10.sp, color = Yzl.TextMuted)
            }
        }
    }
}

@Composable
private fun AiBubble(msg: AiMessage) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (msg.fromUser) Arrangement.End else Arrangement.Start,
    ) {
        Box(
            Modifier
                .widthIn(max = 560.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (msg.fromUser) Yzl.Accent else Color(0xFFF3F8FF))
                .padding(horizontal = 12.dp, vertical = 9.dp),
        ) {
            Column {
                Text(
                    if (msg.fromUser) "我" else "柚子姐姐",
                    fontSize = 9.5.sp,
                    color = if (msg.fromUser) Color(0xCCFFFFFF) else Yzl.AccentDeep,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    msg.text,
                    fontSize = 12.sp,
                    color = if (msg.fromUser) Color.White else Yzl.TextPrimary,
                    lineHeight = 17.sp,
                )
            }
        }
    }
}

@Composable
fun LinkSheet(vm: AppViewModel) {
    var tab by remember { mutableStateOf("加入房间") }
    var code by remember { mutableStateOf("") }
    BoxWithConstraints(Modifier.fillMaxSize().background(Color(0x730A1626)), contentAlignment = Alignment.Center) {
        val cardWidth = minOf(560.dp, maxWidth * 0.92f)
        val cardMaxHeight = minOf(600.dp, maxHeight * 0.92f)
        CardBox(Modifier.width(cardWidth).heightIn(max = cardMaxHeight), corner = 16.dp, shadow = 24.dp) {
            Column(Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("联机房间", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Yzl.TextPrimary)
                    Spacer(Modifier.weight(1f))
                    CloseButton { vm.closeLink() }
                }
                Spacer(Modifier.height(6.dp))
                Text("EasyTier 虚拟局域网 · 跨网络也能像在同一间教室一样联机", fontSize = 11.sp, color = Yzl.TextSecondary)
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    YzlTab("创建房间", tab == "创建房间") { tab = "创建房间" }
                    YzlTab("加入房间", tab == "加入房间") { tab = "加入房间" }
                }
                Spacer(Modifier.height(16.dp))
                if (tab == "加入房间") {
                    SectionLabel("房间邀请码")
                    Spacer(Modifier.height(6.dp))
                    YzlField(code, { code = it }, "输入朋友发来的邀请码", Modifier.fillMaxWidth())
                    Spacer(Modifier.height(14.dp))
                    YzlButton("加入房间", {
                        if (code.isBlank()) vm.showToast("请先输入邀请码") else vm.showToast("已加入房间 $code（演示）")
                    }, style = YzlButtonStyle.Primary, height = 38.dp, fontSize = 12.5.sp)
                } else {
                    SectionLabel("房间名称")
                    Spacer(Modifier.height(6.dp))
                    YzlField("YZL2 的小屋", { }, "房间名称", Modifier.fillMaxWidth())
                    Spacer(Modifier.height(14.dp))
                    YzlButton("创建房间", { vm.showToast("房间已创建，邀请码 8F2K-Q9（演示）") }, style = YzlButtonStyle.Primary, height = 38.dp, fontSize = 12.5.sp)
                }
                Spacer(Modifier.height(16.dp))
                Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color(0xFFF7FAFE)).padding(12.dp),
                ) {
                    Text(
                        "提示：Android 端可直接使用 EasyTier 官方客户端加入同一网络；房主在游戏里「对局域网开放」，朋友在多人游戏里输入房主地址即可。",
                        fontSize = 10.5.sp,
                        color = Yzl.TextSecondary,
                        lineHeight = 15.sp,
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorDialog(vm: AppViewModel, info: ErrorInfo) {
    BoxWithConstraints(Modifier.fillMaxSize().background(Color(0x730A1626)), contentAlignment = Alignment.Center) {
        val cardWidth = minOf(680.dp, maxWidth * 0.94f)
        val cardMaxHeight = minOf(620.dp, maxHeight * 0.92f)
        CardBox(Modifier.width(cardWidth).heightIn(max = cardMaxHeight), corner = 18.dp, shadow = 26.dp) {
            Column(Modifier.padding(26.dp).verticalScroll(rememberScrollState())) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(Color(0xFFFFF1F1)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Warning, null, tint = Yzl.Danger, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(info.title, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Yzl.TextPrimary)
                        Spacer(Modifier.height(3.dp))
                        Text(info.subtitle, fontSize = 11.5.sp, color = Yzl.TextSecondary)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(info.detail, fontSize = 12.5.sp, color = Yzl.TextSecondary, lineHeight = 19.sp)
                if (info.suggestion.isNotBlank()) {
                    Spacer(Modifier.height(14.dp))
                    Box(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color(0xFFF1F7FF))
                            .border(1.dp, Color(0xFFD5E6FB), RoundedCornerShape(10.dp)).padding(14.dp),
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Glyph.Sparkle, null, tint = Yzl.Accent, modifier = Modifier.size(12.dp))
                                Spacer(Modifier.width(7.dp))
                                Text("怎么处理", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Yzl.Accent)
                            }
                            Spacer(Modifier.height(7.dp))
                            Text(info.suggestion, fontSize = 11.5.sp, color = Color(0xFF38506E), lineHeight = 17.sp)
                        }
                    }
                }
                if (info.log.isNotBlank()) {
                    Spacer(Modifier.height(14.dp))
                    Box(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color(0xFF101C2E)).padding(14.dp),
                    ) {
                        Text(info.log, fontSize = 10.5.sp, color = Color(0xFFB9CBE4), lineHeight = 16.sp)
                    }
                }
                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.align(Alignment.End)) {
                    YzlButton("AI 分析", {
                        vm.dismissError()
                        vm.openAi()
                    }, height = 36.dp, fontSize = 12.sp)
                    YzlButton("复制日志", { vm.copyToClipboard(info.log.ifBlank { info.detail }); vm.showToast("已复制") }, style = YzlButtonStyle.Ghost, height = 36.dp, fontSize = 12.sp)
                    YzlButton("知道了", { vm.dismissError() }, style = YzlButtonStyle.Primary, height = 36.dp, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun InfoDialogCard(title: String, body: String, onDismiss: () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize().background(Color(0x730A1626)), contentAlignment = Alignment.Center) {
        val cardWidth = minOf(560.dp, maxWidth * 0.9f)
        val cardMaxHeight = minOf(520.dp, maxHeight * 0.9f)
        CardBox(Modifier.width(cardWidth).heightIn(max = cardMaxHeight), corner = 16.dp, shadow = 22.dp) {
            Column(Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Yzl.TextPrimary)
                    Spacer(Modifier.weight(1f))
                    CloseButton(onClick = onDismiss)
                }
                Spacer(Modifier.height(12.dp))
                Text(body, fontSize = 12.sp, color = Yzl.TextSecondary, lineHeight = 18.sp, modifier = Modifier.verticalScroll(rememberScrollState()))
                Spacer(Modifier.height(18.dp))
                Row(Modifier.align(Alignment.End)) {
                    YzlButton("关闭", onDismiss, style = YzlButtonStyle.Primary, height = 34.dp, fontSize = 12.5.sp)
                }
            }
        }
    }
}
