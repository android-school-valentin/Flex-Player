package ru.valentine.flexplayer.client

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import ru.valentine.flexplayer.core.media.MediaId
import ru.valentine.flexplayer.service.FlexMusicService
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class BrowserClientImpl constructor(
        applicationContext: Context,
) : BrowserClient {

    private val controllerCallback = ClientControllerCallback()
    private val connectionCallback = ConnectionCallback(applicationContext)

    @Volatile
    private var deferredController = CompletableDeferred<MediaControllerCompat>()

    private val mediaBrowser = MediaBrowserCompat(
            applicationContext,
            ComponentName(applicationContext, FlexMusicService::class.java),
            connectionCallback,
            null
    )

    private val _playbackState = MutableStateFlow<PlaybackStateCompat>(EMPTY_PLAYBACK_STATE)
    private val _nowPlaying = MutableStateFlow<MediaMetadataCompat?>(null)
    private val _shuffleMode = MutableStateFlow(PlaybackStateCompat.SHUFFLE_MODE_NONE)
    private val _repeatMode = MutableStateFlow(PlaybackStateCompat.REPEAT_MODE_NONE)

    override val playbackState: StateFlow<PlaybackStateCompat> = _playbackState
    override val nowPlaying: StateFlow<MediaMetadataCompat?> = _nowPlaying
    override val shuffleMode: StateFlow<Int> = _shuffleMode
    override val repeatMode: StateFlow<Int> = _repeatMode

    override fun connect() {
        if (!mediaBrowser.isConnected) {
            Timber.tag("BrowserClientImpl").i("Connecting to service...")
            mediaBrowser.connect()
        }
    }

    override fun disconnect() {
        if (mediaBrowser.isConnected) {
            Timber.tag("BrowserClientImpl").i("Disconnecting from service...")
            mediaBrowser.disconnect()
            deferredController = CompletableDeferred()
        }
    }

    override fun getChildren(parentId: MediaId): Flow<List<MediaBrowserCompat.MediaItem>> =
            callbackFlow<List<MediaBrowserCompat.MediaItem>> {
                // It seems that the (un)subscription does not work properly when MediaBrowser is disconnected.
                // Wait for the media browser to be connected before registering subscription.
                deferredController.await()
                val subscription = ChannelSubscription(channel)
                mediaBrowser.subscribe(parentId.encoded, subscription)
                awaitClose { mediaBrowser.unsubscribe(parentId.encoded, subscription) }
            }.conflate()

    override suspend fun getItem(itemId: MediaId): MediaBrowserCompat.MediaItem? {
        deferredController.await()
        return suspendCoroutine { continuation ->
            mediaBrowser.getItem(itemId.encoded, object : MediaBrowserCompat.ItemCallback() {
                override fun onItemLoaded(item: MediaBrowserCompat.MediaItem?) {
                    continuation.resume(item)
                }

                override fun onError(itemId: String) {
                    continuation.resume(null)
                }
            })
        }
    }

    override suspend fun search(query: String): List<MediaBrowserCompat.MediaItem> {
        deferredController.await()

        return suspendCoroutine { continuation ->
            mediaBrowser.search(query, null, object : MediaBrowserCompat.SearchCallback() {

                override fun onSearchResult(
                        query: String,
                        extras: Bundle?,
                        items: List<MediaBrowserCompat.MediaItem>
                ) {
                    continuation.resume(items)
                }

                override fun onError(query: String, extras: Bundle?) {
                    error("Unexpected failure when searching \"$query\".")
                }
            })
        }
    }

    override suspend fun play() {
        val controller = deferredController.await()
        controller.transportControls.play()
    }

    override suspend fun pause() {
        val controller = deferredController.await()
        controller.transportControls.pause()
    }

    override suspend fun playFromMediaId(mediaId: MediaId) {
        val controller = deferredController.await()
        controller.transportControls.playFromMediaId(mediaId.encoded, null)
    }

    override suspend fun seekTo(positionMs: Long) {
        val controller = deferredController.await()
        controller.transportControls.seekTo(positionMs)
    }

    override suspend fun skipToPrevious() {
        val controller = deferredController.await()
        controller.transportControls.skipToPrevious()
    }

    override suspend fun skipToNext() {
        val controller = deferredController.await()
        controller.transportControls.skipToNext()
    }

    override suspend fun setShuffleModeEnabled(enabled: Boolean) {
        val controller = deferredController.await()
        controller.transportControls.setShuffleMode(
                when {
                    enabled -> PlaybackStateCompat.SHUFFLE_MODE_ALL
                    else -> PlaybackStateCompat.SHUFFLE_MODE_NONE
                }
        )
    }

    override suspend fun setRepeatMode(@PlaybackStateCompat.RepeatMode repeatMode: Int) {
        if (
                repeatMode == PlaybackStateCompat.REPEAT_MODE_NONE ||
                repeatMode == PlaybackStateCompat.REPEAT_MODE_ONE ||
                repeatMode == PlaybackStateCompat.REPEAT_MODE_ALL
        ) {
            val controller = deferredController.await()
            controller.transportControls.setRepeatMode(repeatMode)
        }
    }

    /**
     * A subscription that sends updates to media children to a [SendChannel].
     */
    private class ChannelSubscription(
            private val channel: SendChannel<List<MediaBrowserCompat.MediaItem>>
    ) : MediaBrowserCompat.SubscriptionCallback() {

        override fun onChildrenLoaded(
                parentId: String,
                children: List<MediaBrowserCompat.MediaItem>
        ) {
            channel.offer(children)
        }

        override fun onChildrenLoaded(
                parentId: String,
                children: List<MediaBrowserCompat.MediaItem>,
                options: Bundle
        ) = onChildrenLoaded(parentId, children)

        override fun onError(parentId: String) {
            channel.close(MediaSubscriptionException(parentId))
        }

        override fun onError(parentId: String, options: Bundle) = onError(parentId)
    }

    private inner class ConnectionCallback(
            private val context: Context
    ) : MediaBrowserCompat.ConnectionCallback() {

        override fun onConnected() {
            Timber.tag("BrowserClientImpl").i("MediaBrowser is connected.")
            val controller = MediaControllerCompat(context, mediaBrowser.sessionToken).also {
                it.registerCallback(controllerCallback)
                _playbackState.value = it.playbackState ?: EMPTY_PLAYBACK_STATE
                _nowPlaying.value = it.metadata
                _repeatMode.value = it.repeatMode
                _shuffleMode.value = it.shuffleMode
            }

            // Trigger all operations waiting for the browser to be connected.
            deferredController.complete(controller)

            // TODO: 19.11.2021 Implement later
            // Prepare last played playlist if nothing to play.
            //if (controller.playbackState?.isPrepared != true && preferences.prepareQueueOnStartup) {
            //    controller.transportControls.prepare()
            //}
        }

        override fun onConnectionSuspended() {
            Timber.tag("BrowserClientImpl").i("Connection to the service has been suspended.")
            if (deferredController.isCompleted) {
                val controller = deferredController.getCompleted()
                controller.unregisterCallback(controllerCallback)
            } else {
                deferredController.cancel()
            }

            deferredController = CompletableDeferred()
        }

        override fun onConnectionFailed() {
            error("Failed to connect to the MediaBrowserService.")
        }
    }

    private inner class ClientControllerCallback : MediaControllerCompat.Callback() {

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            val newState = state ?: EMPTY_PLAYBACK_STATE

            when (newState.state) {
                PlaybackStateCompat.STATE_NONE,
                PlaybackStateCompat.STATE_STOPPED,
                PlaybackStateCompat.STATE_PAUSED,
                PlaybackStateCompat.STATE_PLAYING,
                PlaybackStateCompat.STATE_ERROR -> _playbackState.value = newState
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            _nowPlaying.value = metadata
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            _repeatMode.value = repeatMode
        }

        override fun onShuffleModeChanged(shuffleMode: Int) {
            _shuffleMode.value = shuffleMode
        }

        override fun onSessionDestroyed() {
            Timber.tag("BrowserClientImpl").i("MediaSession has been destroyed.")
            connectionCallback.onConnectionSuspended()
        }
    }
}

/**
 * The playback state used as an alternative to `null`.
 */
private val EMPTY_PLAYBACK_STATE = PlaybackStateCompat.Builder()
        .setState(PlaybackStateCompat.STATE_NONE, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0f, 0L)
        .build()

private val PlaybackStateCompat.isPrepared
    get() = (state == PlaybackStateCompat.STATE_BUFFERING) ||
            (state == PlaybackStateCompat.STATE_PLAYING) ||
            (state == PlaybackStateCompat.STATE_PAUSED)