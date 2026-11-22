package com.dedsec.launcher.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dedsec.launcher.data.LauncherViewModel
import com.dedsec.launcher.ui.components.ControlCentrePanel
import com.dedsec.launcher.ui.components.DedSecMediaWidget
import com.dedsec.launcher.ui.components.HomeGrid
import com.dedsec.launcher.ui.components.TelemetryHeader
import com.dedsec.launcher.ui.components.WallpaperBackground
import com.dedsec.launcher.ui.components.WallpaperPickerSheet
import com.dedsec.launcher.ui.theme.NeonMagentaDim

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel:    LauncherViewModel,
    onPickGallery: () -> Unit           // hoisted to MainActivity for ActivityResult
) {
    val battery     by viewModel.battery.collectAsStateWithLifecycle()
    val media       by viewModel.media.collectAsStateWithLifecycle()
    val network     by viewModel.network.collectAsStateWithLifecycle()
    val time        by viewModel.time.collectAsStateWithLifecycle()
    val dayProgress by viewModel.dayProgress.collectAsStateWithLifecycle()
    val pinnedApps  by viewModel.pinnedApps.collectAsStateWithLifecycle()
    val allApps     by viewModel.apps.collectAsStateWithLifecycle()
    val wallpaper   by viewModel.wallpaper.collectAsStateWithLifecycle()

    // Control Centre state
    val torch       by viewModel.torch.collectAsStateWithLifecycle()
    val bluetooth   by viewModel.bluetooth.collectAsStateWithLifecycle()
    val dnd         by viewModel.dnd.collectAsStateWithLifecycle()
    val autoRotate  by viewModel.autoRotate.collectAsStateWithLifecycle()
    val brightness  by viewModel.brightness.collectAsStateWithLifecycle()
    val volume      by viewModel.volume.collectAsStateWithLifecycle()

    var showDrawer        by remember { mutableStateOf(false) }
    var showWpPicker      by remember { mutableStateOf(false) }
    var showControlCentre by remember { mutableStateOf(false) }

    // Back gestures
    BackHandler(enabled = showWpPicker)      { showWpPicker = false }
    BackHandler(enabled = showControlCentre) { showControlCentre = false }
    // Drawer BackHandler lives inside AppDrawerSheet itself

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(showDrawer, showControlCentre) {
                var startX = 0f
                var startY = 0f
                detectDragGestures(
                    onDragStart = { offset ->
                        startX = offset.x
                        startY = offset.y
                    },
                    onDrag = { _, dragAmount ->
                        // Swipe up anywhere → drawer
                        if (dragAmount.y < -40f && !showDrawer && !showControlCentre) {
                            showDrawer = true
                        }
                        // Swipe down from top 120px zone
                        if (startY < 120f && dragAmount.y > 40f && !showDrawer && !showControlCentre) {
                            if (startX > size.width / 2f) {
                                showControlCentre = true
                            } else {
                                viewModel.expandNotificationShade()
                            }
                        }
                    }
                )
            }
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = {},
                onLongClick       = { showWpPicker = true }
            )
    ) {
        // ── Wallpaper + halftone overlay ──────────────────────────────────
        WallpaperBackground(state = wallpaper)

        // ── Main content ──────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            Spacer(Modifier.height(8.dp))

            TelemetryHeader(
                battery     = battery,
                network     = network,
                time        = time,
                dayProgress = dayProgress,
                modifier    = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(Modifier.height(10.dp))

            DedSecMediaWidget(
                media    = media,
                onAction = { viewModel.mediaAction(it) }
            )

            Spacer(Modifier.weight(1f))

            Box(
                modifier         = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = "↑ SWIPE UP  ·  HOLD TO CHANGE WALLPAPER  ·  ↓ TOP-RIGHT = ctOS",
                    color      = NeonMagentaDim,
                    fontSize   = 8.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            HomeGrid(
                apps           = pinnedApps,
                onAppClick     = { viewModel.launchApp(it.packageName) },
                onAppLongClick = { app -> viewModel.togglePin(app.packageName) },
                modifier       = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            )

            Spacer(Modifier.height(8.dp))
        }

        // ── App Drawer ────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = showDrawer,
            enter   = slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)) +
                      fadeIn(tween(200)),
            exit    = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(260)) +
                      fadeOut(tween(200))
        ) {
            AppDrawerSheet(
                apps          = allApps,
                onDismiss     = { showDrawer = false },
                onAppClick    = { app -> showDrawer = false; viewModel.launchApp(app.packageName) },
                onTogglePin   = { viewModel.togglePin(it) },
                onOpenAppInfo = { viewModel.openAppInfo(it) }
            )
        }

        // ── Wallpaper Picker ──────────────────────────────────────────────
        AnimatedVisibility(
            visible = showWpPicker,
            enter   = slideInVertically(initialOffsetY = { it }, animationSpec = tween(280)) +
                      fadeIn(tween(180)),
            exit    = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(240)) +
                      fadeOut(tween(180))
        ) {
            WallpaperPickerSheet(
                current         = wallpaper,
                onSelectBundled = { resId ->
                    viewModel.selectBundledWallpaper(resId)
                    showWpPicker = false
                },
                onPickGallery   = {
                    showWpPicker = false
                    onPickGallery()
                },
                onDismiss       = { showWpPicker = false }
            )
        }

        // ── ctOS Control Centre ───────────────────────────────────────────
        AnimatedVisibility(
            visible = showControlCentre,
            enter   = slideInVertically(initialOffsetY = { -it }, animationSpec = tween(280)) +
                      fadeIn(tween(180)),
            exit    = slideOutVertically(targetOffsetY = { -it }, animationSpec = tween(240)) +
                      fadeOut(tween(180))
        ) {
            ControlCentrePanel(
                torchOn          = torch,
                bluetoothOn      = bluetooth,
                dndOn            = dnd,
                autoRotateOn     = autoRotate,
                brightness       = brightness,
                volume           = volume,
                onToggleTorch    = { viewModel.toggleTorch() },
                onToggleBluetooth = { viewModel.toggleBluetooth() },
                onToggleDnd      = { viewModel.toggleDnd() },
                onToggleAutoRotate = { viewModel.toggleAutoRotate() },
                onOpenWifi       = { viewModel.openWifiSettings() },
                onOpenData       = { viewModel.openDataSettings() },
                onOpenAirplane   = { viewModel.openAirplaneSettings() },
                onOpenPowerSave  = { viewModel.openPowerSaveSettings() },
                onBrightnessChange = { viewModel.setBrightness(it) },
                onVolumeChange   = { viewModel.setVolume(it) },
                onDismiss        = { showControlCentre = false }
            )
        }
    }
}
