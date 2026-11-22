package com.dedsec.launcher.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dedsec.launcher.ui.theme.*

data class ControlToggle(
    val label: String,
    val icon: ImageVector,
    val iconOff: ImageVector = Icons.Default.Close,
    val isOn: Boolean,
    val onClick: () -> Unit
)

@Composable
fun ControlCentrePanel(
    torchOn: Boolean,
    bluetoothOn: Boolean,
    dndOn: Boolean,
    autoRotateOn: Boolean,
    brightness: Float,
    volume: Float,
    onToggleTorch: () -> Unit,
    onToggleBluetooth: () -> Unit,
    onToggleDnd: () -> Unit,
    onToggleAutoRotate: () -> Unit,
    onOpenWifi: () -> Unit,
    onOpenData: () -> Unit,
    onOpenAirplane: () -> Unit,
    onOpenPowerSave: () -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(onClick = onDismiss)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.55f)
                .align(Alignment.TopCenter)
                .background(Color(0xF2000000))
                .border(1.dp, NeonMagenta, RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                .clickable(enabled = false) {}
        ) {
            Spacer(Modifier.height(48.dp))

            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Canvas8x8Accent()
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "// ctOS CONTROL CENTRE",
                    color = NeonMagenta,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            HorizontalDivider(color = NeonMagentaDim, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(Modifier.height(12.dp))

            // 4x2 Toggle Grid
            val row1 = listOf(
                ControlToggle("WIFI", Icons.Default.Wifi, Icons.Default.WifiOff, isOn = false, onClick = onOpenWifi),
                ControlToggle("DATA", Icons.Default.SignalCellularAlt, Icons.Default.SignalCellularOff, isOn = false, onClick = onOpenData),
                ControlToggle("BT", Icons.Default.Bluetooth, Icons.Default.BluetoothDisabled, isOn = bluetoothOn, onClick = onToggleBluetooth),
                ControlToggle("TORCH", Icons.Default.FlashOn, Icons.Default.FlashOff, isOn = torchOn, onClick = onToggleTorch)
            )
            val row2 = listOf(
                ControlToggle("ROTATE", Icons.Default.ScreenRotation, Icons.Default.ScreenLockRotation, isOn = autoRotateOn, onClick = onToggleAutoRotate),
                ControlToggle("DND", Icons.Default.DoNotDisturb, Icons.Default.NotificationsActive, isOn = dndOn, onClick = onToggleDnd),
                ControlToggle("FLIGHT", Icons.Default.AirplanemodeActive, Icons.Default.AirplanemodeInactive, isOn = false, onClick = onOpenAirplane),
                ControlToggle("PSAVE", Icons.Default.BatterySaver, Icons.Default.BatteryFull, isOn = false, onClick = onOpenPowerSave)
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row1.forEach { toggle ->
                    ToggleTile(toggle, Modifier.weight(1f))
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row2.forEach { toggle ->
                    ToggleTile(toggle, Modifier.weight(1f))
                }
            }

            Spacer(Modifier.height(16.dp))

            // Sliders
            SliderRow(
                label = "BRITE",
                value = brightness,
                onValueChange = onBrightnessChange,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(10.dp))
            SliderRow(
                label = "VOL  ",
                value = volume,
                onValueChange = onVolumeChange,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ToggleTile(toggle: ControlToggle, modifier: Modifier = Modifier) {
    val bg = if (toggle.isOn) NeonMagenta.copy(alpha = 0.18f) else Color(0xFF0D0D0D)
    val border = if (toggle.isOn) NeonMagenta else NeonMagentaDim
    val iconColor = if (toggle.isOn) NeonMagenta else DimWhite

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(3.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(3.dp))
            .clickable(onClick = toggle.onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp)
    ) {
        Icon(
            imageVector = if (toggle.isOn) toggle.icon else toggle.iconOff,
            contentDescription = toggle.label,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = toggle.label,
            color = iconColor,
            fontSize = 7.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun SliderRow(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = NeonMagentaDim,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.width(8.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f).height(24.dp),
            colors = SliderDefaults.colors(
                thumbColor = NeonMagenta,
                activeTrackColor = NeonMagenta,
                inactiveTrackColor = NeonMagentaDim.copy(alpha = 0.4f)
            )
        )
    }
}

@Composable
private fun Canvas8x8Accent() {
    Canvas(modifier = Modifier.size(8.dp)) {
        drawRect(NeonMagenta, size = this.size)
    }
}
