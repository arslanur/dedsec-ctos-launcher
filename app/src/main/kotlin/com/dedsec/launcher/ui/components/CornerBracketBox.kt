package com.dedsec.launcher.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap

@Composable
fun CornerBracketBox(
    modifier: Modifier = Modifier,
    color: Color,
    bracketLength: Float = 24f,
    strokeWidth: Float = 3f
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val bl = bracketLength

        fun line(x1: Float, y1: Float, x2: Float, y2: Float) =
            drawLine(color, Offset(x1, y1), Offset(x2, y2), strokeWidth, StrokeCap.Square)

        // Top-left
        line(0f, bl, 0f, 0f); line(0f, 0f, bl, 0f)
        // Top-right
        line(w - bl, 0f, w, 0f); line(w, 0f, w, bl)
        // Bottom-left
        line(0f, h - bl, 0f, h); line(0f, h, bl, h)
        // Bottom-right
        line(w - bl, h, w, h); line(w, h - bl, w, h)
    }
}
