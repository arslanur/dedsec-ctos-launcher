package com.dedsec.launcher.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dedsec.launcher.data.BUNDLED_WALLPAPERS
import com.dedsec.launcher.data.WallpaperState
import com.dedsec.launcher.ui.theme.*

@Composable
fun WallpaperPickerSheet(
    current:       WallpaperState,
    onSelectBundled: (Int) -> Unit,     // passes resId
    onPickGallery: () -> Unit,
    onDismiss:     () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC000000))
            .clickable(onClick = onDismiss)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color(0xFF050505))
                .clickable(enabled = false) {}
                .padding(bottom = 32.dp)
        ) {
            // Handle
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier
                        .width(40.dp).height(3.dp)
                        .background(NeonMagentaDim, RoundedCornerShape(2.dp))
                )
            }

            // Title
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Corner-bracket accent lines
                Box(Modifier.width(12.dp).height(1.dp).background(NeonMagenta))
                Spacer(Modifier.width(8.dp))
                Text(
                    text          = "WALLPAPER SELECT",
                    color         = NeonMagenta,
                    fontSize      = 11.sp,
                    fontFamily    = FontFamily.Monospace,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.width(8.dp))
                Box(Modifier.weight(1f).height(1.dp).background(NeonMagentaDim))
            }

            Spacer(Modifier.height(12.dp))

            // Bundled wallpaper thumbnails
            LazyRow(
                contentPadding        = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(BUNDLED_WALLPAPERS) { _, resId ->
                    val isSelected = current is WallpaperState.Bundled && current.resId == resId
                    BundledWallpaperThumb(
                        resId      = resId,
                        isSelected = isSelected,
                        label      = "DEDSEC ${BUNDLED_WALLPAPERS.indexOf(resId) + 1}",
                        onClick    = { onSelectBundled(resId) }
                    )
                }

                // Placeholder tiles for future wallpapers shown as "locked"
                items(2) {
                    LockedWallpaperThumb()
                }
            }

            Spacer(Modifier.height(16.dp))

            // Gallery picker button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .border(1.dp, NeonMagentaDim, RoundedCornerShape(3.dp))
                    .clickable(onClick = onPickGallery)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = null,
                    tint     = NeonMagenta,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text       = "IMPORT FROM GALLERY",
                        color      = CrispWhite,
                        fontSize   = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text       = "choose any image from your device",
                        color      = DimWhite,
                        fontSize   = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                if (current is WallpaperState.Custom) {
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.Check, null, tint = NeonMagenta, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun BundledWallpaperThumb(
    resId:     Int,
    isSelected: Boolean,
    label:     String,
    onClick:   () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier            = Modifier.clickable(onClick = onClick)
    ) {
        Box {
            Image(
                painter            = painterResource(resId),
                contentDescription = label,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier
                    .width(90.dp).height(160.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .border(
                        width  = if (isSelected) 2.dp else 1.dp,
                        color  = if (isSelected) NeonMagenta else Color(0xFF333333),
                        shape  = RoundedCornerShape(6.dp)
                    )
            )
            if (isSelected) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(5.dp)
                        .size(18.dp)
                        .background(NeonMagenta, RoundedCornerShape(3.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, null, tint = Color.Black, modifier = Modifier.size(12.dp))
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text       = label,
            color      = if (isSelected) NeonMagenta else DimWhite,
            fontSize   = 8.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun LockedWallpaperThumb() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier         = Modifier
                .width(90.dp).height(160.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF0D0D0D))
                .border(1.dp, Color(0xFF222222), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🔒", fontSize = 22.sp)
                Spacer(Modifier.height(4.dp))
                Text("LOCKED", color = Color(0xFF333333), fontSize = 7.sp, fontFamily = FontFamily.Monospace)
            }
        }
        Spacer(Modifier.height(4.dp))
        Text("COMING SOON", color = Color(0xFF333333), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
    }
}
