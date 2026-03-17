package com.logan.project90.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val AppColors = lightColorScheme(
    primary = Ink,
    secondary = Steel,
    tertiary = Teal,
    background = Cloud,
    surface = Cloud,
    surfaceVariant = Mist
)

@Composable
fun Project90Theme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColors,
        typography = AppTypography,
        content = content
    )
}
