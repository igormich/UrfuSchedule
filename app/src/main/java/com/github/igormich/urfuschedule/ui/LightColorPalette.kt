package com.github.igormich.urfuschedule.ui
import android.annotation.SuppressLint
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.shapes
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val primaryOrange = Color(0xFFff9900)
val primaryCharcoal = Color(0xFF2b2b2b)
val textColorDark = Color(0xFFf3f3f3)
val gridLineColorLight = Color.Black
val lightGrey = Color(0xFFf3f3f3)
val lightGreyAlpha = Color(0xDCf3f3f3)

@SuppressLint("ConflictingOnColor")
private val LightColorPalette = lightColors(
    primary = primaryOrange,
    secondary = primaryOrange,
    surface = lightGrey,
    primaryVariant = gridLineColorLight,
    onPrimary = Color.White,
    onSurface = Color.DarkGray,
)

private val DarkColorPalette = darkColors(
    primary = primaryCharcoal,
    secondary = textColorDark,
    surface = lightGreyAlpha,
    primaryVariant = gridLineColorLight,
    onPrimary = Color.DarkGray,
    onSurface = Color.DarkGray,
)

@Composable
fun UrfuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = if (darkTheme) DarkColorPalette else LightColorPalette,
        typography = typography,
        shapes = shapes,
        content = content
    )
}