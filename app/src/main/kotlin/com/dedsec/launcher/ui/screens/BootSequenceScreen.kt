package com.dedsec.launcher.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dedsec.launcher.ui.theme.*
import kotlinx.coroutines.delay

private val BOOT_LOGS = listOf(
    "ctOS 2.0 // KERNEL_INIT...",
    "LOADING SYSTEM MODULES............OK",
    "MOUNTING ENCRYPTED VOLUMES.......OK",
    "CONNECTING TO DEDSEC NODE [TOR]...",
    "NODE HANDSHAKE: 192.168.0.∅......OK",
    "AUTHENTICATING CREDENTIALS........",
    "IDENTITY MASKED // TOR ACTIVE....OK",
    "BYPASSING CTOS SECURITY LAYER.....",
    "FIREWALL BREACH IN PROGRESS.......",
    "FIREWALL BYPASSED.................OK",
    "ROOTKIT INJECTION COMPLETE........OK",
    "BOTNET FRAMEWORK LOADED...........",
    "DEDSEC NETWORK ACTIVE [8 NODES]..OK",
    "LOADING ctOS INTERFACE............",
    "▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓ 100%",
    "SYSTEM READY // WELCOME TO DEDSEC"
)

@Composable
fun BootSequenceScreen(onComplete: () -> Unit) {
    var visibleLogs by remember { mutableStateOf(listOf<String>()) }
    val listState   = rememberLazyListState()

    // Infinite pulse for 8×8 dot matrix
    val infinite    = rememberInfiniteTransition(label = "boot_pulse")
    val scanIndex   by infinite.animateFloat(
        initialValue  = 0f,
        targetValue   = 64f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1_600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scan"
    )

    // Corner bracket reveal
    val bracketAlpha by infinite.animateFloat(
        initialValue  = 0.3f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bracket"
    )

    // Auto-scroll as logs append
    LaunchedEffect(visibleLogs.size) {
        if (visibleLogs.isNotEmpty()) listState.animateScrollToItem(visibleLogs.size - 1)
    }

    // Log ticker
    LaunchedEffect(Unit) {
        delay(600)
        for (line in BOOT_LOGS) {
            visibleLogs = visibleLogs + line
            delay(180)
        }
        delay(600)
        onComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(onClick = onComplete)
    ) {
        // ── 8×8 dot matrix grid ───────────────────────────────────────────
        Canvas(modifier = Modifier.align(Alignment.TopCenter).padding(top = 48.dp).size(200.dp)) {
            val cellSize = size.width / 8f
            for (row in 0 until 8) {
                for (col in 0 until 8) {
                    val idx = row * 8 + col
                    val lit = idx.toFloat() <= scanIndex
                    drawCircle(
                        color  = if (lit) CrispWhite else Color(0xFF2A2A2A),
                        radius = cellSize * 0.22f,
                        center = Offset(col * cellSize + cellSize / 2f, row * cellSize + cellSize / 2f)
                    )
                }
            }
        }

        // ── corner brackets ───────────────────────────────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            val bl = 40f
            val sw = 2.5f
            val c  = NeonMagenta.copy(alpha = bracketAlpha)
            fun line(x1: Float, y1: Float, x2: Float, y2: Float) =
                drawLine(c, Offset(x1, y1), Offset(x2, y2), sw, StrokeCap.Square)
            line(0f, bl, 0f, 0f); line(0f, 0f, bl, 0f)
            line(size.width - bl, 0f, size.width, 0f); line(size.width, 0f, size.width, bl)
            line(0f, size.height - bl, 0f, size.height); line(0f, size.height, bl, size.height)
            line(size.width - bl, size.height, size.width, size.height)
            line(size.width, size.height - bl, size.width, size.height)
        }

        // ── terminal log scroll ───────────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .height(260.dp)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            LazyColumn(state = listState) {
                itemsIndexed(visibleLogs) { _, line ->
                    Text(
                        text          = "> $line",
                        color         = if (line.endsWith("OK")) NeonMagenta else CrispWhite,
                        fontSize      = 11.sp,
                        fontFamily    = FontFamily.Monospace,
                        fontWeight    = if (line.contains("DEDSEC") || line.contains("READY"))
                            FontWeight.Bold else FontWeight.Normal,
                        lineHeight    = 16.sp
                    )
                }
            }
        }

        // ── skip hint ─────────────────────────────────────────────────────
        Text(
            text       = "[ TAP TO SKIP ]",
            color      = FaintWhite,
            fontSize   = 9.sp,
            fontFamily = FontFamily.Monospace,
            modifier   = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        )
    }
}
