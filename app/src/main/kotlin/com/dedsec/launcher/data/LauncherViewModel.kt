package com.dedsec.launcher.data

import android.app.Application
import android.app.NotificationManager
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dedsec.launcher.service.MediaNotificationService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.Inet4Address
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val ctx = application.applicationContext
    private val prefs = ctx.getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)

    private val _apps         = MutableStateFlow<List<AppInfo>>(emptyList())
    val apps: StateFlow<List<AppInfo>> = _apps.asStateFlow()

    private val _pinnedApps   = MutableStateFlow<List<AppInfo>>(emptyList())
    val pinnedApps: StateFlow<List<AppInfo>> = _pinnedApps.asStateFlow()

    private val _battery      = MutableStateFlow(BatteryState())
    val battery: StateFlow<BatteryState> = _battery.asStateFlow()

    private val _media        = MutableStateFlow(MediaInfo())
    val media: StateFlow<MediaInfo> = _media.asStateFlow()

    private val _network      = MutableStateFlow(NetworkInfo())
    val network: StateFlow<NetworkInfo> = _network.asStateFlow()

    private val _time         = MutableStateFlow("")
    val time: StateFlow<String> = _time.asStateFlow()

    private val _dayProgress  = MutableStateFlow(0f)
    val dayProgress: StateFlow<Float> = _dayProgress.asStateFlow()

    private val _showBoot     = MutableStateFlow(true)
    val showBoot: StateFlow<Boolean> = _showBoot.asStateFlow()

    private val _wallpaper    = MutableStateFlow<WallpaperState>(loadWallpaper(ctx))
    val wallpaper: StateFlow<WallpaperState> = _wallpaper.asStateFlow()

    // --- Control Centre ---
    private val _torch       = MutableStateFlow(false)
    val torch: StateFlow<Boolean> = _torch.asStateFlow()

    private val _bluetooth   = MutableStateFlow(false)
    val bluetooth: StateFlow<Boolean> = _bluetooth.asStateFlow()

    private val _dnd         = MutableStateFlow(false)
    val dnd: StateFlow<Boolean> = _dnd.asStateFlow()

    private val _autoRotate  = MutableStateFlow(true)
    val autoRotate: StateFlow<Boolean> = _autoRotate.asStateFlow()

    private val _brightness  = MutableStateFlow(0.5f)
    val brightness: StateFlow<Float> = _brightness.asStateFlow()

    private val _volume      = MutableStateFlow(0.5f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return
            val level  = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale  = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val pct    = if (level >= 0 && scale > 0) level * 100 / scale else 0
            val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                           status == BatteryManager.BATTERY_STATUS_FULL
            _battery.value = BatteryState(pct, charging)
        }
    }

    init {
        loadApps()
        tickClock()
        registerBattery()
        pollNetwork()
        collectMedia()
        readControlState()
    }

    private fun loadApps() {
        viewModelScope.launch {
            val pm      = ctx.packageManager
            val pinned  = prefs.getStringSet("pinned_apps", emptySet()) ?: emptySet()
            val list    = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                .filter { pm.getLaunchIntentForPackage(it.packageName) != null }
                .map { ai ->
                    AppInfo(
                        packageName = ai.packageName,
                        appName     = pm.getApplicationLabel(ai).toString(),
                        icon        = pm.getApplicationIcon(ai.packageName),
                        isPinned    = ai.packageName in pinned
                    )
                }
                .sortedBy { it.appName }
            _apps.value      = list
            _pinnedApps.value = list.filter { it.isPinned }
        }
    }

    fun togglePin(packageName: String) {
        val current = _pinnedApps.value.map { it.packageName }.toMutableSet()
        if (packageName in current) current.remove(packageName) else current.add(packageName)
        prefs.edit().putStringSet("pinned_apps", current).apply()
        val updated = _apps.value.map { it.copy(isPinned = it.packageName in current) }
        _apps.value       = updated
        _pinnedApps.value = updated.filter { it.isPinned }
    }

    private fun tickClock() {
        viewModelScope.launch {
            val fmt = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            while (true) {
                val now = Calendar.getInstance()
                _time.value = fmt.format(now.time)
                val minuteOfDay = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
                _dayProgress.value = minuteOfDay / 1440f   // 1440 = 24 × 60
                delay(1_000)
            }
        }
    }

    private fun registerBattery() {
        ctx.registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    private fun pollNetwork() {
        viewModelScope.launch {
            while (true) {
                _network.value = readNetworkInfo()
                delay(5_000)
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun readNetworkInfo(): NetworkInfo {
        return try {
            val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val caps = cm.getNetworkCapabilities(cm.activeNetwork)
            val wifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
            val cell = caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true

            val ip = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                cm.getLinkProperties(cm.activeNetwork)
                    ?.linkAddresses
                    ?.firstOrNull { it.address is Inet4Address }
                    ?.address?.hostAddress ?: "0.0.0.0"
            } else {
                val wm = ctx.getSystemService(Context.WIFI_SERVICE) as android.net.wifi.WifiManager
                val raw = wm.connectionInfo.ipAddress
                if (raw == 0) "0.0.0.0"
                else {
                    val b = ByteArray(4) { i -> (raw shr (i * 8) and 0xFF).toByte() }
                    java.net.InetAddress.getByAddress(b).hostAddress ?: "0.0.0.0"
                }
            }
            NetworkInfo(ip, wifi, cell)
        } catch (_: Exception) { NetworkInfo() }
    }

    private fun collectMedia() {
        viewModelScope.launch {
            MediaNotificationService.currentTrack.collect { info ->
                _media.value = info ?: MediaInfo()
            }
        }
    }

    fun mediaAction(action: MediaAction) {
        try {
            val manager = ctx.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
            val comp = ComponentName(ctx, MediaNotificationService::class.java)
            val ctrl = manager.getActiveSessions(comp).firstOrNull() ?: return
            val t    = ctrl.transportControls
            when (action) {
                MediaAction.PREVIOUS  -> t.skipToPrevious()
                MediaAction.PLAY_PAUSE -> if (ctrl.playbackState?.state == PlaybackState.STATE_PLAYING)
                    t.pause() else t.play()
                MediaAction.NEXT      -> t.skipToNext()
                MediaAction.SHUFFLE   -> t.skipToNext()
            }
        } catch (_: Exception) {}
    }

    fun launchApp(packageName: String) {
        ctx.packageManager.getLaunchIntentForPackage(packageName)
            ?.apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            ?.let { ctx.startActivity(it) }
    }

    fun openAppInfo(packageName: String) {
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.fromParts("package", packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        ctx.startActivity(intent)
    }

    // ── Control Centre ────────────────────────────────────────────────────────

    private fun readControlState() {
        val btAdapter = (ctx.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter
        _bluetooth.value = btAdapter?.isEnabled == true

        _autoRotate.value = Settings.System.getInt(
            ctx.contentResolver, Settings.System.ACCELEROMETER_ROTATION, 1
        ) == 1

        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        _dnd.value = nm.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL

        if (Settings.System.canWrite(ctx)) {
            val b = Settings.System.getInt(ctx.contentResolver, Settings.System.SCREEN_BRIGHTNESS, 128)
            _brightness.value = b / 255f
        }

        val am  = ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val cur = am.getStreamVolume(AudioManager.STREAM_MUSIC)
        _volume.value = if (max > 0) cur.toFloat() / max else 0f
    }

    fun toggleTorch() {
        val cm = ctx.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = cm.cameraIdList.firstOrNull {
            cm.getCameraCharacteristics(it).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        } ?: return
        val next = !_torch.value
        try { cm.setTorchMode(cameraId, next); _torch.value = next } catch (_: Exception) {}
    }

    fun toggleBluetooth() {
        ctx.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    fun toggleAutoRotate() {
        if (!Settings.System.canWrite(ctx)) { openWriteSettingsPermission(); return }
        val next = if (_autoRotate.value) 0 else 1
        Settings.System.putInt(ctx.contentResolver, Settings.System.ACCELEROMETER_ROTATION, next)
        _autoRotate.value = next == 1
    }

    fun toggleDnd() {
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!nm.isNotificationPolicyAccessGranted) {
            ctx.startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            return
        }
        val next = !_dnd.value
        nm.setInterruptionFilter(
            if (next) NotificationManager.INTERRUPTION_FILTER_PRIORITY
            else      NotificationManager.INTERRUPTION_FILTER_ALL
        )
        _dnd.value = next
    }

    fun setBrightness(value: Float) {
        if (!Settings.System.canWrite(ctx)) { openWriteSettingsPermission(); return }
        Settings.System.putInt(
            ctx.contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE,
            Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
        )
        Settings.System.putInt(
            ctx.contentResolver, Settings.System.SCREEN_BRIGHTNESS,
            (value * 255).toInt().coerceIn(1, 255)
        )
        _brightness.value = value
    }

    fun setVolume(value: Float) {
        val am  = ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        am.setStreamVolume(AudioManager.STREAM_MUSIC, (value * max).toInt(), 0)
        _volume.value = value
    }

    fun openWifiSettings() {
        ctx.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
    }

    fun openDataSettings() {
        ctx.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
    }

    fun openAirplaneSettings() {
        ctx.startActivity(Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
    }

    fun openPowerSaveSettings() {
        ctx.startActivity(Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
    }

    fun openWriteSettingsPermission() {
        ctx.startActivity(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
            data = Uri.parse("package:${ctx.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    fun expandNotificationShade() {
        try {
            val sb  = ctx.getSystemService("statusbar")
            val cls = Class.forName("android.app.StatusBarManager")
            cls.getMethod("expandNotificationsPanel").invoke(sb)
        } catch (_: Exception) {}
    }

    fun dismissBoot() { _showBoot.value = false }

    fun selectBundledWallpaper(resId: Int) {
        val state = WallpaperState.Bundled(resId)
        _wallpaper.value = state
        saveWallpaper(ctx, state)
    }

    fun setCustomWallpaper(uri: Uri) {
        // Persist read permission across reboots
        try { ctx.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
        catch (_: Exception) {}
        val state = WallpaperState.Custom(uri)
        _wallpaper.value = state
        saveWallpaper(ctx, state)
    }

    override fun onCleared() {
        super.onCleared()
        try { ctx.unregisterReceiver(batteryReceiver) } catch (_: Exception) {}
    }
}
