package com.dedsec.launcher.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

@Composable
fun HalftoneBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val spacing = 26f
        val radius  = 1.4f
        val color   = Color(0xFF1A1A1A)

        var col = spacing / 2f
        while (col < size.width) {
            var row = spacing / 2f
            while (row < size.height) {
                drawCircle(color = color, radius = radius, center = Offset(col, row))
                row += spacing
            }
            col += spacing
        }
    }
}
