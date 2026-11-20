package com.dedsec.launcher.data

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable,
    val isPinned: Boolean = false
)

data class BatteryState(
    val percentage: Int = 0,
    val isCharging: Boolean = false
)

data class MediaInfo(
    val title: String = "UNKNOWN TRACK // DEDSEC RADIO",
    val artist: String = "UNKNOWN ARTIST",
    val isPlaying: Boolean = false
)

data class NetworkInfo(
    val ipAddress: String = "0.0.0.0",
    val isWifiConnected: Boolean = false,
    val isCellularConnected: Boolean = false
)

enum class MediaAction { PREVIOUS, PLAY_PAUSE, NEXT, SHUFFLE }
