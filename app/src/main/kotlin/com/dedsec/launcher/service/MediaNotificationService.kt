package com.dedsec.launcher.service

import android.content.ComponentName
import android.media.MediaMetadata
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.dedsec.launcher.data.MediaInfo
import kotlinx.coroutines.flow.MutableStateFlow

class MediaNotificationService : NotificationListenerService() {

    companion object {
        val currentTrack = MutableStateFlow<MediaInfo?>(null)
        var instance: MediaNotificationService? = null

        const val ACTION_PREVIOUS   = 0
        const val ACTION_PLAY_PAUSE = 1
        const val ACTION_NEXT       = 2
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this
        refreshMediaInfo()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        instance = null
        currentTrack.value = null
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        refreshMediaInfo()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        refreshMediaInfo()
    }

    private fun refreshMediaInfo() {
        try {
            val manager = getSystemService(MEDIA_SESSION_SERVICE) as MediaSessionManager
            val component = ComponentName(this, MediaNotificationService::class.java)
            val controllers = manager.getActiveSessions(component)
            val controller = controllers.firstOrNull()

            if (controller != null) {
                val meta = controller.metadata
                val title = meta?.getString(MediaMetadata.METADATA_KEY_TITLE)
                    ?: "UNKNOWN TRACK // DEDSEC RADIO"
                val artist = meta?.getString(MediaMetadata.METADATA_KEY_ARTIST)
                    ?: meta?.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
                    ?: "UNKNOWN ARTIST"
                val isPlaying = controller.playbackState?.state == PlaybackState.STATE_PLAYING
                currentTrack.value = MediaInfo(title, artist, isPlaying)
            } else {
                currentTrack.value = null
            }
        } catch (_: Exception) {
            currentTrack.value = null
        }
    }

    fun dispatchMediaAction(action: Int) {
        try {
            val manager = getSystemService(MEDIA_SESSION_SERVICE) as MediaSessionManager
            val component = ComponentName(this, MediaNotificationService::class.java)
            val controller = manager.getActiveSessions(component).firstOrNull() ?: return
            val transport = controller.transportControls
            when (action) {
                ACTION_PREVIOUS   -> transport.skipToPrevious()
                ACTION_PLAY_PAUSE -> if (controller.playbackState?.state == PlaybackState.STATE_PLAYING)
                    transport.pause() else transport.play()
                ACTION_NEXT       -> transport.skipToNext()
            }
        } catch (_: Exception) {}
    }
}
