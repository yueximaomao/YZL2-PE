package com.yzl2.launcher.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object Yzl {
    val WindowBg = Color(0xFFEEF4FC)
    val Card = Color(0xFFFFFFFF)
    val TextPrimary = Color(0xFF17233A)
    val TextSecondary = Color(0xFF6F8099)
    val TextMuted = Color(0xFF9AA9BE)
    val Border = Color(0xFFE3EBF5)
    val BorderStrong = Color(0xFFD3E0F0)
    val Accent = Color(0xFF2F7BE5)
    val AccentDeep = Color(0xFF1E5FC4)
    val AccentSoft = Color(0xFFEAF3FF)
    val AccentSoft2 = Color(0xFFDCEBFF)
    val Danger = Color(0xFFE25555)
    val Success = Color(0xFF34A06A)
    val Warning = Color(0xFFE39A3B)
    val FlatCard = Color(0xFFF7FAFE)
    val HeaderStart = Color(0xFF1C5CC0)
    val HeaderMid = Color(0xFF2B72DB)
    val HeaderEnd = Color(0xFF3C88ED)

    val HeaderBrush = Brush.horizontalGradient(listOf(HeaderStart, HeaderMid, HeaderEnd))

    fun primaryBrush(accent: Color = Accent): Brush {
        val second = if (accent == Accent) Color(0xFF4B95F2) else accent.copy(alpha = 0.82f)
        return Brush.linearGradient(listOf(accent, second))
    }

    val ArtScrim = Brush.verticalGradient(
        0f to Color(0x0012213A),
        0.45f to Color(0x3D12213A),
        0.78f to Color(0x8C0B1A30),
        1f to Color(0xB80B1A30),
    )

    val CardShape = RoundedCornerShape(16.dp)
    val CardShapeLarge = RoundedCornerShape(18.dp)

    val appFont = FontFamily.Default
    val Bold = FontWeight.Bold
    val SemiBold = FontWeight.SemiBold
    val Medium = FontWeight.Medium

    /** 关闭持续动画，便于截图测试与低性能设备。 */
    @Volatile
    var animate: Boolean = true
}

@Composable
fun YzlTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Yzl.Accent,
            onPrimary = Color.White,
            background = Yzl.WindowBg,
            surface = Yzl.Card,
            onSurface = Yzl.TextPrimary,
            outline = Yzl.Border,
            error = Yzl.Danger,
        ),
        typography = Typography(
            bodyLarge = TextStyle(fontFamily = Yzl.appFont, fontSize = 14.sp),
            bodyMedium = TextStyle(fontFamily = Yzl.appFont, fontSize = 13.sp),
            labelSmall = TextStyle(fontFamily = Yzl.appFont, fontSize = 11.sp),
        ),
        content = content,
    )
}
