package com.dedsec.launcher.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShuffleOn
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dedsec.launcher.data.MediaAction
import com.dedsec.launcher.data.MediaInfo
import com.dedsec.launcher.ui.theme.*

@Composable
fun DedSecMediaWidget(
    media: MediaInfo,
    onAction: (MediaAction) -> Unit,
    modifier: Modifier = Modifier
) {
    var shuffleActive by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF050505))
            .border(1.5.dp, NeonMagenta, RoundedCornerShape(4.dp))
    ) {
        // Notched corner decorations
        NotchCorners(
            modifier = Modifier.matchParentSize(),
            color    = NeonMagenta
        )

        Row(
            modifier          = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: skull / album art placeholder
            SkullArtwork(
                modifier = Modifier.size(72.dp)
            )

            Spacer(Modifier.width(12.dp))

            // Right: title, artist, controls
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text          = "DEDSEC PIRATE RADIO",
                    color         = NeonMagenta,
                    fontSize      = 8.sp,
                    fontFamily    = FontFamily.Monospace,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text     = media.title,
                    color    = CrispWhite,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    maxLines  = 1,
                    overflow  = TextOverflow.Ellipsis
                )
                Text(
                    text     = media.artist,
                    color    = DimWhite,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines  = 1,
                    overflow  = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(8.dp))

                // Playback controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    IconButton(
                        onClick  = { shuffleActive = !shuffleActive },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShuffleOn,
                            contentDescription = "Shuffle",
                            tint     = if (shuffleActive) NeonMagenta else DimWhite,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick  = { onAction(MediaAction.PREVIOUS) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous",
                            tint     = CrispWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(NeonMagenta, RoundedCornerShape(2.dp))
                            .clickable { onAction(MediaAction.PLAY_PAUSE) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (media.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint     = Color.Black,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    IconButton(
                        onClick  = { onAction(MediaAction.NEXT) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next",
                            tint     = CrispWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SkullArtwork(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Halftone rows in background
        var row = 4f
        while (row < h) {
            var col = if ((row / 8f).toInt() % 2 == 0) 4f else 8f
            while (col < w) {
                drawCircle(Color(0xFF1A1A1A), radius = 1.5f, center = Offset(col, row))
                col += 8f
            }
            row += 8f
        }

        // Simple skull shape
        val cx = w / 2f
        val cy = h * 0.42f
        val r  = w * 0.35f
        drawCircle(Color(0xFF2A2A2A), radius = r, center = Offset(cx, cy))
        drawCircle(Color(0xFF1A1A1A), radius = r * 0.3f, center = Offset(cx - r * 0.32f, cy + r * 0.1f))
        drawCircle(Color(0xFF1A1A1A), radius = r * 0.3f, center = Offset(cx + r * 0.32f, cy + r * 0.1f))

        // Jaw
        val jawTop = cy + r * 0.6f
        val path   = Path().apply {
            moveTo(cx - r * 0.5f, jawTop)
            lineTo(cx - r * 0.4f, h * 0.9f)
            lineTo(cx + r * 0.4f, h * 0.9f)
            lineTo(cx + r * 0.5f, jawTop)
            close()
        }
        drawPath(path, Color(0xFF2A2A2A))

        // Teeth lines
        for (i in -1..1) {
            drawLine(
                color       = Color(0xFF0A0A0A),
                start       = Offset(cx + i * r * 0.26f, jawTop + 2f),
                end         = Offset(cx + i * r * 0.26f, h * 0.88f),
                strokeWidth = 2f,
                cap         = StrokeCap.Square
            )
        }

        // Magenta border
        drawRect(
            color       = NeonMagenta,
            topLeft     = Offset(0f, 0f),
            size        = size,
            style       = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth = 1.5f)
        )
    }
}

@Composable
private fun NotchCorners(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val notch = 12f
        val sw    = 2f

        fun corner(ox: Float, oy: Float, dx: Float, dy: Float) {
            drawLine(color, Offset(ox, oy), Offset(ox + dx * notch, oy), sw, StrokeCap.Square)
            drawLine(color, Offset(ox, oy), Offset(ox, oy + dy * notch), sw, StrokeCap.Square)
        }
        corner(0f, 0f, 1f, 1f)
        corner(size.width, 0f, -1f, 1f)
        corner(0f, size.height, 1f, -1f)
        corner(size.width, size.height, -1f, -1f)
    }
}
