package com.dedsec.launcher.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dedsec.launcher.data.BatteryState
import com.dedsec.launcher.data.NetworkInfo
import com.dedsec.launcher.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun TelemetryHeader(
    battery:     BatteryState,
    network:     NetworkInfo,
    time:        String,
    dayProgress: Float,          // 0f..1f  (minutes elapsed / 1440)
    modifier:    Modifier = Modifier
) {
    // Derive human-readable day segment label
    val hourOfDay = (dayProgress * 24).toInt()
    val segment = when (hourOfDay) {
        in 0..5   -> "NIGHT"
        in 6..11  -> "MORNING"
        in 12..17 -> "AFTERNOON"
        in 18..20 -> "EVENING"
        else      -> "NIGHT"
    }
    val pct = (dayProgress * 100).roundToInt()

    Box(modifier = modifier.fillMaxWidth()) {
        CornerBracketBox(
            modifier      = Modifier.matchParentSize(),
            color         = NeonMagenta,
            bracketLength = 20f,
            strokeWidth   = 2.5f
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            // ── skull · XP day-progress bar · skull ───────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "☠", color = CrispWhite, fontSize = 20.sp, fontFamily = FontFamily.Monospace)

                Column(
                    modifier            = Modifier.weight(1f).padding(horizontal = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Label row: "BOTNET/XP" on left, segment on right
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            text          = "BOTNET/XP",
                            color         = NeonMagenta,
                            fontSize      = 7.sp,
                            fontFamily    = FontFamily.Monospace,
                            fontWeight    = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text          = segment,
                            color         = NeonMagentaDim,
                            fontSize      = 7.sp,
                            fontFamily    = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(Modifier.height(3.dp))

                    // Day progress bar — fills as the day passes
                    DayProgressBar(progress = dayProgress)

                    Spacer(Modifier.height(2.dp))

                    // Sub-label: "XX% DAY DONE  |  battery%"
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            text       = "$pct% DAY DONE",
                            color      = CrispWhite,
                            fontSize   = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text       = "BAT ${battery.percentage}%" +
                                         if (battery.isCharging) " ⚡" else "",
                            color      = if (battery.isCharging) NeonMagenta
                                         else DimWhite,
                            fontSize   = 7.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Text(text = "☠", color = CrispWhite, fontSize = 20.sp, fontFamily = FontFamily.Monospace)
            }

            Spacer(Modifier.height(6.dp))

            // ── IP · wifi/cell · clock ─────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text       = network.ipAddress,
                    color      = DimWhite,
                    fontSize   = 10.sp,
                    fontFamily = FontFamily.Monospace
                )

                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (network.isWifiConnected) Icons.Default.Wifi else Icons.Default.WifiOff,
                        contentDescription = null,
                        tint     = if (network.isWifiConnected) NeonMagenta else Color.Gray,
                        modifier = Modifier.size(12.dp)
                    )
                    if (network.isCellularConnected) {
                        Icon(
                            imageVector = Icons.Default.SignalCellularAlt,
                            contentDescription = null,
                            tint     = NeonMagenta,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    Text(
                        text       = if (network.isWifiConnected || network.isCellularConnected)
                            "ONLINE" else "OFFLINE",
                        color      = if (network.isWifiConnected || network.isCellularConnected)
                            NeonMagenta else Color.Gray,
                        fontSize   = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    text       = time,
                    color      = CrispWhite,
                    fontSize   = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DayProgressBar(progress: Float) {
    val animated by animateFloatAsState(
        targetValue   = progress,
        animationSpec = tween(1_200),
        label         = "day_xp"
    )
    // Colour shifts through the day: midnight=dim, morning=amber, noon=magenta, evening=dim
    val barColor = when {
        progress < 0.25f -> Color(0xFF4466AA)   // pre-dawn blue
        progress < 0.50f -> WarnAmber            // morning amber
        progress < 0.75f -> NeonMagenta          // afternoon magenta
        else             -> Color(0xFF884499)    // evening purple
    }

    Canvas(modifier = Modifier.fillMaxWidth().height(6.dp)) {
        // Track
        drawLine(
            color       = Color(0xFF2A2A2A),
            start       = Offset(0f, size.height / 2f),
            end         = Offset(size.width, size.height / 2f),
            strokeWidth = size.height,
            cap         = StrokeCap.Square
        )
        // Hour tick marks (every 6 h = 25%)
        for (tick in 1..3) {
            val x = size.width * tick * 0.25f
            drawLine(
                color       = Color(0xFF444444),
                start       = Offset(x, 0f),
                end         = Offset(x, size.height),
                strokeWidth = 1f
            )
        }
        // Fill
        if (animated > 0f) {
            drawLine(
                color       = barColor,
                start       = Offset(0f, size.height / 2f),
                end         = Offset(size.width * animated, size.height / 2f),
                strokeWidth = size.height,
                cap         = StrokeCap.Square
            )
            // Bright leading edge
            drawCircle(
                color  = Color.White,
                radius = size.height * 0.9f,
                center = Offset(size.width * animated, size.height / 2f)
            )
        }
    }
}
