package com.yzl2.launcher.ui

import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun CardBox(
    modifier: Modifier = Modifier,
    corner: Dp = 16.dp,
    color: Color = Yzl.Card,
    borderColor: Color = Yzl.Border,
    borderWidth: Dp = 1.dp,
    shadow: Dp = 10.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = RoundedCornerShape(corner)
    Box(
        modifier
            .shadow(
                elevation = shadow,
                shape = shape,
                clip = false,
                ambientColor = Color(0x335E86C4),
                spotColor = Color(0x335E86C4),
            )
            .clip(shape)
            .background(color)
            .border(borderWidth, borderColor, shape),
        content = content,
    )
}

enum class YzlButtonStyle { Primary, Secondary, Ghost }

@Composable
fun YzlButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: YzlButtonStyle = YzlButtonStyle.Secondary,
    enabled: Boolean = true,
    height: Dp = 38.dp,
    fontSize: TextUnit = 13.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    leading: (@Composable () -> Unit)? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val shape = RoundedCornerShape(if (style == YzlButtonStyle.Primary) 12.dp else 10.dp)
    val bg: Color = when (style) {
        YzlButtonStyle.Primary -> Color.Transparent
        YzlButtonStyle.Secondary -> Yzl.Card
        YzlButtonStyle.Ghost -> if (pressed) Yzl.AccentSoft else Color.Transparent
    }
    val fg: Color = when (style) {
        YzlButtonStyle.Primary -> Color.White
        YzlButtonStyle.Secondary -> Yzl.TextPrimary
        YzlButtonStyle.Ghost -> if (pressed) Yzl.Accent else Yzl.TextSecondary
    }
    val base = modifier
        .height(height)
        .then(
            if (style == YzlButtonStyle.Primary && enabled) {
                Modifier.shadow(
                    elevation = if (pressed) 4.dp else 12.dp,
                    shape = shape,
                    clip = false,
                    ambientColor = Yzl.Accent.copy(alpha = 0.35f),
                    spotColor = Yzl.Accent.copy(alpha = 0.55f),
                )
            } else Modifier
        )
        .clip(shape)
        .background(if (style == YzlButtonStyle.Primary) Yzl.primaryBrush() else SolidColor(bg))
        .then(
            if (style == YzlButtonStyle.Secondary) Modifier.border(1.dp, Yzl.BorderStrong, shape) else Modifier
        )
        .clickable(enabled = enabled, interactionSource = interaction, indication = LocalIndication.current, onClick = onClick)
        .then(if (enabled) Modifier else Modifier.alpha(0.45f))
        .padding(horizontal = 14.dp)

    Row(
        modifier = base,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = fontWeight,
            color = fg,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun IconAction(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 34.dp,
    tint: Color = Yzl.TextSecondary,
) {
    Box(
        modifier
            .size(size)
            .clip(RoundedCornerShape(size / 2))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription, tint = tint, modifier = Modifier.size(16.dp))
    }
}

@Composable
fun NavPill(
    label: String,
    icon: ImageVector?,
    selected: Boolean,
    accent: Color = Yzl.AccentDeep,
    onClick: () -> Unit,
) {
    val bg by animateColorAsState(if (selected) Color.White else Color.Transparent, label = "navBg")
    val fg = if (selected) accent else Color(0xE6FFFFFF)
    Row(
        Modifier
            .height(38.dp)
            .clip(RoundedCornerShape(19.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, null, tint = fg, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(label, color = fg, fontSize = 13.5.sp, maxLines = 1)
    }
}

@Composable
fun YzlTab(text: String, selected: Boolean, soft: Boolean = false, accent: Color = Yzl.Accent, onClick: () -> Unit) {
    Box(
        Modifier
            .height(31.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(
                when {
                    selected && soft -> Yzl.AccentSoft
                    selected -> accent
                    else -> Color(0xFFF3F7FD)
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            color = if (selected) (if (soft) accent else Color.White) else Yzl.TextSecondary,
            fontSize = 12.sp,
            maxLines = 1,
        )
    }
}

@Composable
fun Badge(text: String, bg: Color = Yzl.AccentSoft, fg: Color = Yzl.AccentDeep, fontSize: TextUnit = 10.5.sp) {
    Box(
        Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 7.dp, vertical = 3.dp),
    ) {
        Text(text, color = fg, fontSize = fontSize, maxLines = 1)
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier, color: Color = Yzl.TextMuted) {
    Text(text, modifier = modifier, color = color, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
}

@Composable
fun DividerLine(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(1.dp).background(Yzl.Border))
}

@Composable
fun YzlField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    height: Dp = 38.dp,
    fontSize: TextUnit = 13.sp,
    password: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(color = Yzl.TextPrimary, fontSize = fontSize),
        cursorBrush = SolidColor(Yzl.Accent),
        keyboardOptions = KeyboardOptions(keyboardType = if (password) KeyboardType.Password else keyboardType),
        visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = modifier,
        decorationBox = { inner ->
            Row(
                Modifier
                    .height(height)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(9.dp))
                    .background(Color.White)
                    .border(1.dp, Yzl.BorderStrong, RoundedCornerShape(9.dp))
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (leading != null) {
                    leading()
                    Spacer(Modifier.width(7.dp))
                }
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text(placeholder, color = Yzl.TextMuted, fontSize = 12.5.sp, maxLines = 1)
                    }
                    inner()
                }
                if (trailing != null) trailing()
            }
        },
    )
}

@Composable
fun YzlToggle(checked: Boolean, onChange: (Boolean) -> Unit) {
    val offset by animateDpAsState(if (checked) 20.dp else 3.dp, label = "knob")
    Box(
        Modifier
            .width(40.dp)
            .height(23.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (checked) Yzl.Accent else Color(0xFFD5E1F0))
            .clickable { onChange(!checked) },
    ) {
        Box(
            Modifier
                .offset(x = offset, y = 3.dp)
                .size(17.dp)
                .shadow(1.dp, CircleShape)
                .background(Color.White, CircleShape),
        )
    }
}

@Composable
fun YzlDropdown(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 38.dp,
    fontSize: TextUnit = 12.5.sp,
) {
    var open by remember { mutableStateOf(false) }
    Box(modifier) {
        Row(
            Modifier
                .height(height)
                .fillMaxWidth()
                .clip(RoundedCornerShape(9.dp))
                .background(Color.White)
                .border(1.dp, Yzl.BorderStrong, RoundedCornerShape(9.dp))
                .clickable { open = true }
                .padding(start = 12.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                selected,
                color = Yzl.TextPrimary,
                fontSize = fontSize,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Icon(Icons.Filled.KeyboardArrowDown, null, tint = Yzl.TextSecondary, modifier = Modifier.size(16.dp))
        }
        DropdownMenu(
            expanded = open,
            onDismissRequest = { open = false },
            offset = DpOffset(0.dp, 4.dp),
            modifier = Modifier
                .background(Color.White)
                .border(1.dp, Yzl.Border, RoundedCornerShape(10.dp)),
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            option,
                            fontSize = 12.5.sp,
                            color = if (option == selected) Yzl.AccentDeep else Yzl.TextPrimary,
                        )
                    },
                    onClick = { open = false; onSelect(option) },
                )
            }
        }
    }
}

@Composable
fun Stepper(value: Int, onChange: (Int) -> Unit, min: Int = 512, max: Int = 16384, step: Int = 512) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable { onChange((value - step).coerceAtLeast(min)) },
            contentAlignment = Alignment.Center,
        ) { Text("−", color = Yzl.TextSecondary, fontSize = 16.sp) }
        Box(
            Modifier
                .width(96.dp)
                .height(38.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(Color.White)
                .border(1.dp, Yzl.BorderStrong, RoundedCornerShape(9.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("$value", color = Yzl.TextPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.Medium)
                Text(" MB", color = Yzl.TextMuted, fontSize = 10.sp)
            }
        }
        Box(
            Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable { onChange((value + step).coerceAtMost(max)) },
            contentAlignment = Alignment.Center,
        ) { Text("+", color = Yzl.TextSecondary, fontSize = 16.sp) }
    }
}

@Composable
fun LinearBar(fraction: Float, modifier: Modifier = Modifier, height: Dp = 6.dp) {
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(Color(0xFFE7EFF9)),
    ) {
        Box(
            Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .height(height)
                .clip(RoundedCornerShape(height / 2))
                .background(Yzl.primaryBrush()),
        )
    }
}

@Composable
fun YzlToast(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier
            .clip(RoundedCornerShape(21.dp))
            .background(Color(0xF2182438))
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(Color(0xFF7FE0B0)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Check, null, tint = Color(0xFF123A2A), modifier = Modifier.size(9.dp))
        }
        Spacer(Modifier.width(8.dp))
        Text(text, color = Color.White, fontSize = 12.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun OverlayScrim(onDismiss: (() -> Unit)? = null, content: @Composable BoxScope.() -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0x660A1626))
            .then(if (onDismiss != null) Modifier.clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onDismiss() } else Modifier),
        content = content,
    )
}

@Composable
fun CloseButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .size(30.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Filled.Close, "关闭", tint = Yzl.TextMuted, modifier = Modifier.size(15.dp))
    }
}

@Composable
fun EmptyState(
    icon: @Composable () -> Unit,
    title: String,
    hint: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF3F7FD)),
            contentAlignment = Alignment.Center,
        ) { icon() }
        Spacer(Modifier.height(14.dp))
        Text(title, color = Yzl.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(hint, color = Yzl.TextMuted, fontSize = 11.5.sp, textAlign = TextAlign.Center)
    }
}

private val imageCache = LruCache<String, ImageBitmap>(80)
private val assetCache = HashMap<String, ImageBitmap>()

@Composable
fun AsyncImage(
    url: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    fallback: @Composable BoxScope.() -> Unit = {},
) {
    var bitmap by remember(url) { mutableStateOf(if (url == null) null else imageCache.get(url)) }
    LaunchedEffect(url) {
        if (url == null || bitmap != null) return@LaunchedEffect
        val loaded = withContext(Dispatchers.IO) {
            runCatching {
                val conn = URL(url).openConnection() as HttpURLConnection
                conn.connectTimeout = 10000
                conn.readTimeout = 15000
                conn.setRequestProperty("User-Agent", "YZL2-Android/1.0")
                conn.inputStream.use { BitmapFactory.decodeStream(it)?.asImageBitmap() }
            }.getOrNull()
        }
        if (loaded != null) {
            imageCache.put(url, loaded)
            bitmap = loaded
        }
    }
    if (bitmap != null) {
        Image(bitmap!!, null, modifier = modifier, contentScale = contentScale)
    } else {
        Box(modifier, content = fallback)
    }
}

@Composable
fun AssetImage(name: String, modifier: Modifier = Modifier, contentScale: ContentScale = ContentScale.Crop) {
    val context = LocalContext.current
    val bitmap = remember(name) {
        assetCache.getOrPut(name) {
            context.assets.open(name).use { BitmapFactory.decodeStream(it).asImageBitmap() }
        }
    }
    Image(bitmap, null, modifier = modifier, contentScale = contentScale)
}

fun Brush.Companion.header() = Yzl.HeaderBrush

@Composable
fun ScreenTitle(title: String, subtitle: String, modifier: Modifier = Modifier, trailing: @Composable () -> Unit = {}) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Yzl.TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, color = Yzl.TextSecondary, fontSize = 11.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        trailing()
    }
}
