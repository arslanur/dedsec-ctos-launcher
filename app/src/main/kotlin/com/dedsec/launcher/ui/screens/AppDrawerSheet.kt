package com.dedsec.launcher.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.dedsec.launcher.data.AppInfo
import com.dedsec.launcher.ui.theme.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppDrawerSheet(
    apps: List<AppInfo>,
    onDismiss: () -> Unit,
    onAppClick: (AppInfo) -> Unit,
    onTogglePin: (String) -> Unit,
    onOpenAppInfo: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, apps) {
        if (query.isBlank()) apps
        else apps.filter { it.appName.contains(query, ignoreCase = true) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF0000000))
            .clickable(onClick = onDismiss)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp)
                .clickable(enabled = false) {}  // consume clicks to prevent dismiss
        ) {
            // ── DRAG HANDLE ───────────────────────────────────────────────
            Box(
                modifier          = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                contentAlignment  = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp).height(3.dp)
                        .background(NeonMagentaDim, RoundedCornerShape(2.dp))
                )
            }

            Text(
                text          = "// APP REGISTRY",
                color         = NeonMagenta,
                fontSize      = 11.sp,
                fontFamily    = FontFamily.Monospace,
                modifier      = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // ── SEARCH BAR ────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF0A0A0A))
                    .border(1.dp, NeonMagentaDim, RoundedCornerShape(2.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text       = "root@ctOS:~# grep ",
                        color      = NeonMagenta,
                        fontSize   = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    BasicTextField(
                        value         = query,
                        onValueChange = { query = it },
                        singleLine    = true,
                        textStyle     = TextStyle(
                            color      = CrispWhite,
                            fontSize   = 12.sp,
                            fontFamily = FontFamily.Monospace
                        ),
                        cursorBrush   = SolidColor(NeonMagenta),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        modifier      = Modifier.weight(1f),
                        decorationBox = { inner ->
                            if (query.isEmpty()) {
                                Text(
                                    text       = "_",
                                    color      = NeonMagenta,
                                    fontSize   = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            inner()
                        }
                    )
                }
            }

            // ── APP GRID ──────────────────────────────────────────────────
            LazyVerticalGrid(
                columns               = GridCells.Fixed(4),
                contentPadding        = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement   = Arrangement.spacedBy(12.dp),
                modifier              = Modifier.fillMaxSize()
            ) {
                items(filtered, key = { it.packageName }) { app ->
                    DrawerAppTile(
                        app           = app,
                        onClick       = { onAppClick(app) },
                        onTogglePin   = { onTogglePin(app.packageName) },
                        onOpenAppInfo = { onOpenAppInfo(app.packageName) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DrawerAppTile(
    app: AppInfo,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onOpenAppInfo: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier            = Modifier
            .combinedClickable(
                onClick     = onClick,
                onLongClick = { showMenu = true }
            )
            .padding(4.dp)
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            val bitmap = remember(app.icon) { app.icon.toBitmap(96, 96).asImageBitmap() }
            val filter = remember {
                val am = android.graphics.ColorMatrix()
                am.setSaturation(0f)
                ColorFilter.colorMatrix(ColorMatrix(am.array))
            }

            Image(
                bitmap             = bitmap,
                contentDescription = app.appName,
                colorFilter        = filter,
                modifier           = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF101010))
                    .padding(4.dp)
            )

            if (app.isPinned) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(NeonMagenta, RoundedCornerShape(2.dp))
                )
            }
        }

        Spacer(Modifier.height(3.dp))

        Text(
            text       = app.appName,
            color      = DimWhite,
            fontSize   = 8.sp,
            fontFamily = FontFamily.Monospace,
            textAlign  = TextAlign.Center,
            maxLines   = 1,
            overflow   = TextOverflow.Ellipsis,
            modifier   = Modifier.width(60.dp)
        )

        DropdownMenu(
            expanded        = showMenu,
            onDismissRequest = { showMenu = false },
            modifier        = Modifier.background(Color(0xFF0A0A0A))
        ) {
            DropdownMenuItem(
                text    = { Text(if (app.isPinned) "UNPIN" else "PIN TO HOME", fontFamily = FontFamily.Monospace, color = NeonMagenta, fontSize = 11.sp) },
                onClick = { showMenu = false; onTogglePin() }
            )
            DropdownMenuItem(
                text    = { Text("APP INFO", fontFamily = FontFamily.Monospace, color = DimWhite, fontSize = 11.sp) },
                onClick = { showMenu = false; onOpenAppInfo() }
            )
        }
    }
}
