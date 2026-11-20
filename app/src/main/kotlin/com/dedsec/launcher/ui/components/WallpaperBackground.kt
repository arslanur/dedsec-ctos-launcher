package com.dedsec.launcher.ui.components

import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.dedsec.launcher.data.WallpaperState

@Composable
fun WallpaperBackground(
    state:         WallpaperState,
    overlayAlpha:  Float = 0.55f,    // dark veil so UI stays readable
    modifier:      Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (state) {
            is WallpaperState.Default -> {
                // pure black — halftone is the only texture
            }

            is WallpaperState.Bundled -> {
                Image(
                    painter            = painterResource(state.resId),
                    contentDescription = null,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize()
                )
            }

            is WallpaperState.Custom  -> {
                val ctx    = LocalContext.current
                val bitmap = remember(state.uri) {
                    runCatching {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            val src = ImageDecoder.createSource(ctx.contentResolver, state.uri)
                            ImageDecoder.decodeBitmap(src) { decoder, _, _ ->
                                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                            }
                        } else {
                            @Suppress("DEPRECATION")
                            android.provider.MediaStore.Images.Media.getBitmap(
                                ctx.contentResolver, state.uri
                            )
                        }
                    }.getOrNull()
                }
                if (bitmap != null) {
                    Image(
                        bitmap             = bitmap.asImageBitmap(),
                        contentDescription = null,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Dark overlay so halftone dots + UI text always pop
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = overlayAlpha))
        )

        // Halftone dot-matrix on top of overlay
        HalftoneBackground()
    }
}
