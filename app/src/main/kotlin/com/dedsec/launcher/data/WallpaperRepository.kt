package com.dedsec.launcher.data

import android.content.Context
import android.net.Uri
import com.dedsec.launcher.R

/** All bundled wallpapers in order. Index 0 is the default. */
val BUNDLED_WALLPAPERS: List<Int> = listOf(
    R.drawable.wallpaper_dedsec_01
)

sealed class WallpaperState {
    data object Default                      : WallpaperState()
    data class  Bundled(val resId: Int)      : WallpaperState()
    data class  Custom(val uri: Uri)         : WallpaperState()
}

private const val PREF_TYPE = "wp_type"
private const val PREF_IDX  = "wp_idx"
private const val PREF_URI  = "wp_uri"
private const val TYPE_DEFAULT = "default"
private const val TYPE_BUNDLED = "bundled"
private const val TYPE_CUSTOM  = "custom"

fun saveWallpaper(ctx: Context, state: WallpaperState) {
    val p = ctx.getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE).edit()
    when (state) {
        is WallpaperState.Default       -> p.putString(PREF_TYPE, TYPE_DEFAULT)
        is WallpaperState.Bundled       -> p.putString(PREF_TYPE, TYPE_BUNDLED)
                                            .putInt(PREF_IDX, BUNDLED_WALLPAPERS.indexOf(state.resId))
        is WallpaperState.Custom        -> p.putString(PREF_TYPE, TYPE_CUSTOM)
                                            .putString(PREF_URI, state.uri.toString())
    }
    p.apply()
}

fun loadWallpaper(ctx: Context): WallpaperState {
    val p    = ctx.getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)
    return when (p.getString(PREF_TYPE, TYPE_DEFAULT)) {
        TYPE_BUNDLED -> {
            val idx = p.getInt(PREF_IDX, 0).coerceIn(BUNDLED_WALLPAPERS.indices)
            WallpaperState.Bundled(BUNDLED_WALLPAPERS[idx])
        }
        TYPE_CUSTOM  -> {
            val uriStr = p.getString(PREF_URI, null)
            if (uriStr != null) WallpaperState.Custom(Uri.parse(uriStr))
            else WallpaperState.Bundled(BUNDLED_WALLPAPERS[0])
        }
        else         -> WallpaperState.Bundled(BUNDLED_WALLPAPERS[0])  // first-run default
    }
}
