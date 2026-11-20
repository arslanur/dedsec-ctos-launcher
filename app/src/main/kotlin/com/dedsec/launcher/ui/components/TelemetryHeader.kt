package com.dedsec.launcher.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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

@Composable
fun TelemetryHeader(
    battery: BatteryState,
    network: NetworkInfo,
    time: String,
    modifier: Modifier = Modifier
) {
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
            // ── skull · progress bar · skull ──────────────────────────────
            Row(
                modifier            = Modifier.fillMaxWidth(),
                verticalAlignment   = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "☠", color = CrispWhite, fontSize = 20.sp, fontFamily = FontFamily.Monospace)

                Column(
                    modifier            = Modifier.weight(1f).padding(horizontal = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text       = "BOTNET/XP",
                        color      = NeonMagenta,
                        fontSize   = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Spacer(Modifier.height(3.dp))
                    BatteryBar(battery.percentage, battery.isCharging)
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text       = "${battery.percentage}%",
                            color      = CrispWhite,
                            fontSize   = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        if (battery.isCharging) {
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.BatteryChargingFull,
                                contentDescription = null,
                                tint = NeonMagenta,
                                modifier = Modifier.size(10.dp)
                            )
                            Text(
                                text       = " [CHG]",
                                color      = NeonMagenta,
                                fontSize   = 8.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Text(text = "☠", color = CrispWhite, fontSize = 20.sp, fontFamily = FontFamily.Monospace)
            }

            Spacer(Modifier.height(6.dp))

            // ── IP · wifi status · clock ───────────────────────────────────
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

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
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
                        text       = if (network.isWifiConnected || network.isCellularConnected) "ONLINE" else "OFFLINE",
                        color      = if (network.isWifiConnected || network.isCellularConnected) NeonMagenta else Color.Gray,
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
private fun BatteryBar(percentage: Int, isCharging: Boolean) {
    val progress by animateFloatAsState(
        targetValue    = percentage / 100f,
        animationSpec  = tween(1_000),
        label          = "battery"
    )
    val barColor = when {
        isCharging      -> NeonMagenta
        percentage > 50 -> NeonMagenta
        percentage > 20 -> WarnAmber
        else            -> DangerRed
    }
    Canvas(
        modifier = Modifier.fillMaxWidth().height(5.dp)
    ) {
        drawLine(
            color       = Color(0xFF2A2A2A),
            start       = Offset(0f, size.height / 2f),
            end         = Offset(size.width, size.height / 2f),
            strokeWidth = size.height,
            cap         = StrokeCap.Square
        )
        if (progress > 0f) {
            drawLine(
                color       = barColor,
                start       = Offset(0f, size.height / 2f),
                end         = Offset(size.width * progress, size.height / 2f),
                strokeWidth = size.height,
                cap         = StrokeCap.Square
            )
        }
    }
}
