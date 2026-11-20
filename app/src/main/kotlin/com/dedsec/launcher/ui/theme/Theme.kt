package com.dedsec.launcher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DedSecColorScheme = darkColorScheme(
    primary                = NeonMagenta,
    onPrimary              = PureBlack,
    primaryContainer       = Color(0xFF3D0020),
    onPrimaryContainer     = NeonMagenta,
    secondary              = CrispWhite,
    onSecondary            = PureBlack,
    secondaryContainer     = Color(0xFF1A1A1A),
    onSecondaryContainer   = CrispWhite,
    background             = PureBlack,
    onBackground           = CrispWhite,
    surface                = SurfaceDark,
    onSurface              = CrispWhite,
    surfaceVariant         = Color(0xFF0D0D0D),
    onSurfaceVariant       = DimWhite,
    outline                = NeonMagenta,
    outlineVariant         = NeonMagentaDim,
    error                  = DangerRed,
    onError                = PureBlack,
)

@Composable
fun DedSecTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DedSecColorScheme,
        typography  = DedSecTypography,
        content     = content
    )
}
