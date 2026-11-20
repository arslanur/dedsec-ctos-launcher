package com.dedsec.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dedsec.launcher.data.LauncherViewModel
import com.dedsec.launcher.ui.screens.BootSequenceScreen
import com.dedsec.launcher.ui.screens.HomeScreen
import com.dedsec.launcher.ui.theme.DedSecTheme
import com.dedsec.launcher.ui.theme.PureBlack

class MainActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle     = SystemBarStyle.dark(Color.Transparent.toArgb()),
            navigationBarStyle = SystemBarStyle.dark(Color.Black.toArgb())
        )
        super.onCreate(savedInstanceState)

        setContent {
            DedSecTheme {
                val showBoot by viewModel.showBoot.collectAsStateWithLifecycle()

                // Gallery image picker — registered here so it has an Activity context
                val galleryLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri ->
                    if (uri != null) viewModel.setCustomWallpaper(uri)
                }

                BackHandler(enabled = true) {}

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(PureBlack)
                ) {
                    AnimatedContent(
                        targetState  = showBoot,
                        transitionSpec = {
                            fadeIn(tween(400)) togetherWith fadeOut(tween(400))
                        },
                        label = "boot_home"
                    ) { booting ->
                        if (booting) {
                            BootSequenceScreen(onComplete = { viewModel.dismissBoot() })
                        } else {
                            HomeScreen(
                                viewModel     = viewModel,
                                onPickGallery = { galleryLauncher.launch("image/*") }
                            )
                        }
                    }
                }
            }
        }
    }
}
