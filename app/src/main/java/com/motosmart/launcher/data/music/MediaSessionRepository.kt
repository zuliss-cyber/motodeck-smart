package com.motosmart.launcher.data.music

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Build
import android.provider.Settings
import android.service.notification.NotificationListenerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class MusicState(
    val hasAccess: Boolean = false,
    val isConnected: Boolean = false,
    val isPlaying: Boolean = false,
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val artwork: Bitmap? = null,
    val mediaPackage: String? = null,
    val canSkipNext: Boolean = true,
    val canSkipPrevious: Boolean = true,
    val canPlayPause: Boolean = true
)

/**
 * Reads the active media session. Requires the rider to grant notification access;
 * notification content is never stored or transmitted.
 */
class MediaSessionRepository(private val context: Context) {

    private val manager = context.getSystemService(MediaSessionManager::class.java)
    private val component = ComponentName(context, MediaNotificationListener::class.java)
    private val _state = MutableStateFlow(MusicState(hasAccess = hasAccess()))
    val state: StateFlow<MusicState> = _state.asStateFlow()

    private var controller: MediaController? = null
    private var listenerRegistered = false

    private val controllerCallback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) = publish()
        override fun onPlaybackStateChanged(state: PlaybackState?) = publish()
        override fun onSessionDestroyed() {
            detachController()
            refresh()
        }
    }

    private val sessionsChangedListener =
        MediaSessionManager.OnActiveSessionsChangedListener { sessions ->
            attachBestSession(sessions.orEmpty())
        }

    fun hasAccess(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            val notificationManager =
                context.getSystemService(android.app.NotificationManager::class.java)
            notificationManager?.isNotificationListenerAccessGranted(component) == true
        } else {
            val enabled = Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            ).orEmpty()
            enabled.contains(component.flattenToString()) || enabled.contains(context.packageName)
        }
    }

    fun refreshAccessState() {
        _state.update { it.copy(hasAccess = hasAccess()) }
    }

    fun start() {
        val access = hasAccess()
        _state.update { it.copy(hasAccess = access) }
        if (!access) {
            detachController()
            return
        }
        if (listenerRegistered) {
            refresh()
            return
        }
        runCatching {
            manager.addOnActiveSessionsChangedListener(sessionsChangedListener, component)
            listenerRegistered = true
            attachBestSession(manager.getActiveSessions(component))
        }.onFailure {
            listenerRegistered = false
            _state.update { state -> state.copy(isConnected = false) }
        }
    }

    fun stop() {
        if (listenerRegistered) {
            runCatching {
                manager.removeOnActiveSessionsChangedListener(sessionsChangedListener)
            }
            listenerRegistered = false
        }
        detachController()
    }

    fun refresh() {
        if (!hasAccess()) {
            _state.value = MusicState(hasAccess = false)
            detachController()
            return
        }
        runCatching {
            attachBestSession(manager.getActiveSessions(component))
        }
    }

    fun playPause() {
        val active = controller ?: return
        runCatching {
            if (_state.value.isPlaying) active.transportControls.pause()
            else active.transportControls.play()
        }
    }

    fun next() {
        runCatching { controller?.transportControls?.skipToNext() }
    }

    fun previous() {
        runCatching { controller?.transportControls?.skipToPrevious() }
    }

    fun openMusicApp(): Boolean {
        val packageName = _state.value.mediaPackage
        val launch = packageName?.let { context.packageManager.getLaunchIntentForPackage(it) }
        val intent = launch ?: android.content.Intent(android.content.Intent.ACTION_MAIN)
            .addCategory(android.content.Intent.CATEGORY_APP_MUSIC)
        return runCatching {
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    fun openAccessSettings(): Boolean {
        val intents = listOf(
            android.content.Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS),
            android.content.Intent(Settings.ACTION_SETTINGS)
        )
        return intents.any { intent ->
            runCatching {
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            }.getOrDefault(false)
        }
    }

    private fun attachBestSession(sessions: List<MediaController>) {
        val best = sessions.firstOrNull {
            it.playbackState?.state == PlaybackState.STATE_PLAYING
        } ?: sessions.firstOrNull()
        if (best == null) {
            detachController()
            _state.update {
                it.copy(
                    hasAccess = true,
                    isConnected = false,
                    isPlaying = false,
                    title = null,
                    artist = null,
                    album = null,
                    artwork = null,
                    mediaPackage = null
                )
            }
            return
        }
        if (controller?.sessionToken == best.sessionToken) {
            publish()
            return
        }
        detachController()
        controller = best
        runCatching { best.registerCallback(controllerCallback) }
        publish()
    }

    private fun detachController() {
        controller?.let { runCatching { it.unregisterCallback(controllerCallback) } }
        controller = null
    }

    private fun publish() {
        val active = controller ?: return
        val metadata = active.metadata
        val playbackState = active.playbackState
        val artwork = metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
            ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_ART)
        val actions = playbackState?.actions ?: 0L
        _state.value = MusicState(
            hasAccess = true,
            isConnected = true,
            isPlaying = playbackState?.state == PlaybackState.STATE_PLAYING,
            title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)?.takeIf { it.isNotBlank() },
            artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)?.takeIf { it.isNotBlank() }
                ?: metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)?.takeIf { it.isNotBlank() },
            album = metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM)?.takeIf { it.isNotBlank() },
            artwork = artwork,
            mediaPackage = active.packageName,
            canSkipNext = actions and PlaybackState.ACTION_SKIP_TO_NEXT != 0L,
            canSkipPrevious = actions and PlaybackState.ACTION_SKIP_TO_PREVIOUS != 0L,
            canPlayPause = actions and (PlaybackState.ACTION_PLAY or PlaybackState.ACTION_PAUSE) != 0L
        )
    }
}

/**
 * The platform requires a NotificationListenerService component before the rider can
 * grant notification access. It deliberately does not read or persist notifications.
 */
class MediaNotificationListener : NotificationListenerService()
