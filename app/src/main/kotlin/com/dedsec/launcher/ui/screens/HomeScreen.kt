package com.dedsec.launcher.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import com.dedsec.launcher.ui.components.DedSecMediaWidget
import com.dedsec.launcher.ui.components.HalftoneBackground
import com.dedsec.launcher.ui.components.HomeGrid
import com.dedsec.launcher.ui.components.TelemetryHeader
import com.dedsec.launcher.ui.theme.NeonMagentaDim
import com.dedsec.launcher.ui.theme.PureBlack

@Composable
fun HomeScreen(viewModel: LauncherViewModel) {
    val battery    by viewModel.battery.collectAsStateWithLifecycle()
    val media      by viewModel.media.collectAsStateWithLifecycle()
    val network    by viewModel.network.collectAsStateWithLifecycle()
    val time       by viewModel.time.collectAsStateWithLifecycle()
    val pinnedApps by viewModel.pinnedApps.collectAsStateWithLifecycle()
    val allApps    by viewModel.apps.collectAsStateWithLifecycle()

    var showDrawer by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -40f) showDrawer = true
                }
            }
    ) {
        HalftoneBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            Spacer(Modifier.height(8.dp))

            TelemetryHeader(
                battery  = battery,
                network  = network,
                time     = time,
                modifier = Modifier.padding(horizontal = 12.dp)
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
                    text       = "↑ SWIPE UP",
                    color      = NeonMagentaDim,
                    fontSize   = 9.sp,
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
    }
}
