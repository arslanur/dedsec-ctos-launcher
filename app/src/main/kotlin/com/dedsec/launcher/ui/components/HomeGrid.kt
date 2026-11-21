package com.dedsec.launcher.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.dedsec.launcher.data.AppInfo
import com.dedsec.launcher.ui.theme.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeGrid(
    apps: List<AppInfo>,
    onAppClick: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    if (apps.isEmpty()) {
        Box(
            modifier         = modifier.fillMaxWidth().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text       = "[ NO APPS PINNED ]",
                    color      = NeonMagentaDim,
                    fontSize   = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text       = "SWIPE UP — ACCESS APP DRAWER",
                    color      = FaintWhite,
                    fontSize   = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
        return
    }

    LazyVerticalGrid(
        columns               = GridCells.Fixed(4),
        modifier              = modifier.fillMaxWidth(),
        contentPadding        = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement   = Arrangement.spacedBy(8.dp),
        userScrollEnabled     = false
    ) {
        itemsIndexed(apps) { index, app ->
            AppIconTile(
                app         = app,
                isDiamond   = (index % 5 == 2),
                onClick     = { onAppClick(app) },
                onLongClick = { onAppLongClick(app) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppIconTile(
    app: AppInfo,
    isDiamond: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier            = Modifier
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier         = Modifier
                .size(60.dp)
                .let { if (isDiamond) it.rotate(45f) else it }
                .clip(RoundedCornerShape(if (isDiamond) 6.dp else 12.dp))
                .background(androidx.compose.ui.graphics.Color(0xFF0E0E0E))
        ) {
            IconImage(
                drawable = app.icon,
                modifier = Modifier
                    .size(40.dp)
                    .let { if (isDiamond) it.rotate(-45f) else it }
            )
            Canvas(modifier = Modifier.align(Alignment.TopEnd).padding(3.dp).size(5.dp)) {
                drawCircle(NeonMagenta, radius = size.minDimension / 2f)
            }
        }

        Spacer(Modifier.height(4.dp))

        Text(
            text       = app.appName,
            color      = CrispWhite,
            fontSize   = 8.sp,
            fontFamily = FontFamily.Monospace,
            textAlign  = TextAlign.Center,
            maxLines   = 1,
            overflow   = TextOverflow.Ellipsis,
            modifier   = Modifier.width(64.dp)
        )
    }
}

@Composable
private fun IconImage(drawable: Drawable, modifier: Modifier = Modifier) {
    val bitmap = remember(drawable) {
        drawable.toBitmap(96, 96).asImageBitmap()
    }
    val filter = remember {
        // Use android.graphics.ColorMatrix for setSaturation/postConcat, then
        // convert the resulting float array to Compose's ColorMatrix.
        val am = android.graphics.ColorMatrix()
        am.setSaturation(0f)
        val bright = android.graphics.ColorMatrix(floatArrayOf(
            1.4f, 0f,   0f,   0f, 20f,
            0f,   1.4f, 0f,   0f, 20f,
            0f,   0f,   1.4f, 0f, 20f,
            0f,   0f,   0f,   1f,  0f
        ))
        am.postConcat(bright)
        ColorFilter.colorMatrix(ColorMatrix(am.array))
    }
    Image(
        bitmap             = bitmap,
        contentDescription = null,
        colorFilter        = filter,
        modifier           = modifier
    )
}
