package com.yzl2.launcher.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

private fun icon(name: String, block: ImageVector.Builder.() -> Unit): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply(block).build()

object Glyph {
    val Download: ImageVector by lazy {
        icon("download") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(10.6f, 3f); lineTo(13.4f, 3f); lineTo(13.4f, 12.2f); lineTo(10.6f, 12.2f); close()
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(5.6f, 10.2f); lineTo(12f, 17.4f); lineTo(18.4f, 10.2f); close()
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(3.8f, 18.4f); lineTo(20.2f, 18.4f); lineTo(20.2f, 20.8f); lineTo(3.8f, 20.8f); close()
            }
        }
    }

    val Folder: ImageVector by lazy {
        icon("folder") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(3f, 5.6f); lineTo(9.4f, 5.6f); lineTo(11f, 8f); lineTo(21f, 8f)
                lineTo(21f, 18.6f); lineTo(3f, 18.6f); close()
            }
        }
    }

    val Box: ImageVector by lazy {
        icon("box") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(4f, 7.2f); lineTo(12f, 3.4f); lineTo(20f, 7.2f); lineTo(20f, 16.8f)
                lineTo(12f, 20.6f); lineTo(4f, 16.8f); close()
            }
        }
    }

    val Image: ImageVector by lazy {
        icon("image") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(4.6f, 4.2f); lineTo(19.4f, 4.2f); lineTo(21f, 5.8f); lineTo(21f, 18.2f)
                lineTo(19.4f, 19.8f); lineTo(4.6f, 19.8f); lineTo(3f, 18.2f); lineTo(3f, 5.8f); close()
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(5f, 6.2f); lineTo(19f, 6.2f); lineTo(19f, 17.8f); lineTo(5f, 17.8f); close()
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(8.3f, 8.6f); lineTo(10.1f, 8.6f); lineTo(10.1f, 10.4f); lineTo(8.3f, 10.4f); close()
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(5.2f, 17.2f); lineTo(9.6f, 11.4f); lineTo(12.4f, 14.6f); lineTo(14.8f, 12.2f)
                lineTo(18.8f, 17.2f); close()
            }
        }
    }

    val Clock: ImageVector by lazy {
        icon("clock") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 2.6f)
                arcTo(9.4f, 9.4f, 0f, true, true, 12f, 21.4f)
                arcTo(9.4f, 9.4f, 0f, true, true, 12f, 2.6f)
                close()
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(12f, 5.2f)
                arcTo(6.8f, 6.8f, 0f, true, true, 12f, 18.8f)
                arcTo(6.8f, 6.8f, 0f, true, true, 12f, 5.2f)
                close()
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(11.1f, 7.4f); lineTo(12.9f, 7.4f); lineTo(12.9f, 12.6f); lineTo(11.1f, 12.6f); close()
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(11.8f, 11.4f); lineTo(16.4f, 11.4f); lineTo(16.4f, 13.2f); lineTo(11.8f, 13.2f); close()
            }
        }
    }

    val Copy: ImageVector by lazy {
        icon("copy") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(8f, 8f); lineTo(20f, 8f); lineTo(20f, 20f); lineTo(8f, 20f); close()
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(9.8f, 9.8f); lineTo(18.2f, 9.8f); lineTo(18.2f, 18.2f); lineTo(9.8f, 18.2f); close()
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(4f, 4f); lineTo(16f, 4f); lineTo(16f, 6f); lineTo(6f, 6f); lineTo(6f, 16f); lineTo(4f, 16f); close()
            }
        }
    }

    val Grid: ImageVector by lazy {
        icon("grid") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(3.6f, 3.6f); lineTo(10.6f, 3.6f); lineTo(10.6f, 10.6f); lineTo(3.6f, 10.6f); close()
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(13.4f, 3.6f); lineTo(20.4f, 3.6f); lineTo(20.4f, 10.6f); lineTo(13.4f, 10.6f); close()
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(3.6f, 13.4f); lineTo(10.6f, 13.4f); lineTo(10.6f, 20.4f); lineTo(3.6f, 20.4f); close()
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(13.4f, 13.4f); lineTo(20.4f, 13.4f); lineTo(20.4f, 20.4f); lineTo(13.4f, 20.4f); close()
            }
        }
    }

    val Sparkle: ImageVector by lazy {
        icon("sparkle") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 2.4f); lineTo(14f, 9.8f); lineTo(21.6f, 12f); lineTo(14f, 14.2f)
                lineTo(12f, 21.6f); lineTo(10f, 14.2f); lineTo(2.4f, 12f); lineTo(10f, 9.8f); close()
            }
        }
    }

    val Users: ImageVector by lazy {
        icon("users") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(9f, 3.4f)
                arcTo(3.6f, 3.6f, 0f, true, true, 9f, 10.6f)
                arcTo(3.6f, 3.6f, 0f, true, true, 9f, 3.4f)
                close()
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(2.4f, 20.4f)
                quadTo(2.4f, 12.6f, 9f, 12.6f)
                quadTo(15.6f, 12.6f, 15.6f, 20.4f)
                close()
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(17.2f, 5.2f)
                arcTo(2.7f, 2.7f, 0f, true, true, 17.2f, 10.6f)
                arcTo(2.7f, 2.7f, 0f, true, true, 17.2f, 5.2f)
                close()
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(16.2f, 13.2f)
                quadTo(21.6f, 13.8f, 21.6f, 20.4f)
                lineTo(16.8f, 20.4f)
                quadTo(16.8f, 15.8f, 15.2f, 14.2f)
                close()
            }
        }
    }

    val Code: ImageVector by lazy {
        icon("code") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(8.2f, 6f); lineTo(9.8f, 7.6f); lineTo(5.4f, 12f); lineTo(9.8f, 16.4f)
                lineTo(8.2f, 18f); lineTo(2.2f, 12f); close()
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(15.8f, 6f); lineTo(21.8f, 12f); lineTo(15.8f, 18f); lineTo(14.2f, 16.4f)
                lineTo(18.6f, 12f); lineTo(14.2f, 7.6f); close()
            }
        }
    }

    val Sliders: ImageVector by lazy {
        icon("sliders") {
            path(fill = SolidColor(Color.Black)) { moveTo(3f, 6f); lineTo(21f, 6f); lineTo(21f, 8f); lineTo(3f, 8f); close() }
            path(fill = SolidColor(Color.Black)) { moveTo(3f, 11f); lineTo(21f, 11f); lineTo(21f, 13f); lineTo(3f, 13f); close() }
            path(fill = SolidColor(Color.Black)) { moveTo(3f, 16f); lineTo(21f, 16f); lineTo(21f, 18f); lineTo(3f, 18f); close() }
            path(fill = SolidColor(Color.Black)) { moveTo(7f, 3.5f); lineTo(10f, 3.5f); lineTo(10f, 10.5f); lineTo(7f, 10.5f); close() }
            path(fill = SolidColor(Color.Black)) { moveTo(14f, 8.5f); lineTo(17f, 8.5f); lineTo(17f, 15.5f); lineTo(14f, 15.5f); close() }
        }
    }

    val Music: ImageVector by lazy {
        icon("music") {
            path(fill = SolidColor(Color.Black)) { moveTo(9f, 5f); lineTo(11.4f, 5f); lineTo(11.4f, 17f); lineTo(9f, 17f); close() }
            path(fill = SolidColor(Color.Black)) { moveTo(11f, 4f); lineTo(20f, 2.6f); lineTo(20f, 6f); lineTo(11f, 7.4f); close() }
            path(fill = SolidColor(Color.Black)) {
                moveTo(4.4f, 17.2f); lineTo(9f, 17.2f); lineTo(9f, 21f); lineTo(4.4f, 21f); close()
            }
        }
    }

    val List: ImageVector by lazy {
        icon("list") {
            path(fill = SolidColor(Color.Black)) { moveTo(3f, 5f); lineTo(21f, 5f); lineTo(21f, 7f); lineTo(3f, 7f); close() }
            path(fill = SolidColor(Color.Black)) { moveTo(3f, 11f); lineTo(21f, 11f); lineTo(21f, 13f); lineTo(3f, 13f); close() }
            path(fill = SolidColor(Color.Black)) { moveTo(3f, 17f); lineTo(21f, 17f); lineTo(21f, 19f); lineTo(3f, 19f); close() }
        }
    }

    val Play: ImageVector by lazy {
        icon("play") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(7f, 4.6f); lineTo(19.4f, 12f); lineTo(7f, 19.4f); close()
            }
        }
    }

    val External: ImageVector by lazy {
        icon("external") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(13f, 3f); lineTo(21f, 3f); lineTo(21f, 11f); lineTo(18.6f, 11f); lineTo(18.6f, 5.4f); close()
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(19.6f, 3.4f); lineTo(21f, 4.8f); lineTo(12f, 13.8f); lineTo(10.6f, 12.4f); close()
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(4f, 5.6f); lineTo(11f, 5.6f); lineTo(11f, 8f); lineTo(6.4f, 8f); lineTo(6.4f, 18f)
                lineTo(17.4f, 18f); lineTo(17.4f, 13.2f); lineTo(19.8f, 13.2f); lineTo(19.8f, 20.4f); lineTo(4f, 20.4f); close()
            }
        }
    }

    val Package: ImageVector = Box
    val Cube: ImageVector by lazy {
        icon("cube") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 2.6f); lineTo(21f, 7.4f); lineTo(21f, 16.6f); lineTo(12f, 21.4f);
                lineTo(3f, 16.6f); lineTo(3f, 7.4f); close()
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(12f, 5f); lineTo(18.4f, 8.4f); lineTo(18.4f, 15.6f); lineTo(12f, 19f);
                lineTo(5.6f, 15.6f); lineTo(5.6f, 8.4f); close()
            }
        }
    }
}
