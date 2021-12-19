package ru.valentine.flexplayer.service

import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import ru.valentine.flexplayer.core.media.MalformedMediaIdException
import ru.valentine.flexplayer.core.media.parse
import ru.valentine.flexplayer.service.browser.BrowserTree
import ru.valentine.flexplayer.service.browser.tree.AudioTrack
import ru.valentine.flexplayer.service.browser.tree.MediaCategory
import ru.valentine.flexplayer.service.browser.tree.MediaContent
import ru.valentine.flexplayer.service.ext.EXTRA_DURATION
import ru.valentine.flexplayer.service.ext.EXTRA_NUMBER_OF_TRACKS
import ru.valentine.flexplayer.service.playback.FlexPlaybackPreparer
import ru.valentine.flexplayer.service.playback.FlexQueueNavigator
import ru.valentine.flexplayer.service.playback.NOW_PLAYING_NOTIFICATION
import ru.valentine.flexplayer.service.playback.NotificationBuilder
import timber.log.Timber

class FlexMusicService : BaseMusicService() {

    private val browserTree: BrowserTree by inject()
    private val session: MediaSessionCompat by inject()
    private val player: ExoPlayer by inject()
    private val notificationBuilder: NotificationBuilder by inject()

    private lateinit var mediaController: MediaControllerCompat
    private lateinit var notificationManager: NotificationManagerCompat

    private val controllerCallback = MediaControllerCallback()

    override fun onCreate() {
        super.onCreate()


        mediaController = MediaControllerCompat(this, session)

        mediaController.registerCallback(controllerCallback)


        val mediaSessionConnector = MediaSessionConnector(session)

        mediaSessionConnector.setPlayer(player)

        mediaSessionConnector.setPlaybackPreparer(
                FlexPlaybackPreparer(
                        scope = this,
                        player = player,
                        browserTree = browserTree
                )
        )
        mediaSessionConnector.setQueueNavigator(FlexQueueNavigator(session = session))

        notificationManager = NotificationManagerCompat.from(this)

        sessionToken = session.sessionToken

    }

    override fun onDestroy() {
        session.release()
        super.onDestroy()
    }

    override fun onGetRoot(
            clientPackageName: String,
            clientUid: Int,
            rootHints: Bundle?
    ): BrowserRoot {
        return BrowserRoot("root", null)
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        result.detach()

        launch {
            val children = browserTree.getChildren(parentId.parse()).first()
            result.sendResult(children.map { it.toItem() }.toMutableList())
        }

    }

    override fun onLoadItem(itemId: String?, result: Result<MediaBrowserCompat.MediaItem>) {
        if (itemId == null) {
            result.sendResult(null)
        } else {
            result.detach()
            launch {
                try {
                    val itemMediaId = itemId.parse()
                    val requestedContent = browserTree.getItem(itemMediaId)
                    result.sendResult(requestedContent?.toItem())

                } catch (malformedId: MalformedMediaIdException) {
                    Timber.i(
                            malformedId,
                            "Attempt to load item from a malformed media id: %s",
                            itemId
                    )
                    result.sendResult(null)
                }
            }
        }
    }


    private fun MediaContent.toItem(
            builder: MediaDescriptionCompat.Builder = MediaDescriptionCompat.Builder()
    ): MediaBrowserCompat.MediaItem {
        builder
                .setMediaId(id.encoded)
                .setTitle(title)
                .setIconUri(iconUri)

        when (this) {
            is MediaCategory -> {
                builder
                        .setSubtitle(subtitle)
                        .setExtras(Bundle().apply {
                            putInt(EXTRA_NUMBER_OF_TRACKS, count)
                        })
            }

            is AudioTrack -> {
                builder
                        .setSubtitle(artist)
                        .setExtras(Bundle(1).apply {
                            putLong(EXTRA_DURATION, duration)
                        })
            }
        }

        var flags = 0
        if (browsable) {
            flags = flags or FLAG_BROWSABLE
        }
        if (playable) {
            flags = flags or FLAG_PLAYABLE
        }

        return MediaBrowserCompat.MediaItem(builder.build(), flags)
    }

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            mediaController.playbackState?.let(this::updateServiceState)
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            state?.let(this::updateServiceState)
        }

        override fun onShuffleModeChanged(shuffleMode: Int) {
            //settings.shuffleModeEnabled = shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            //settings.repeatMode = when (repeatMode) {
            //    PlaybackStateCompat.REPEAT_MODE_ALL,
            //    PlaybackStateCompat.REPEAT_MODE_GROUP -> RepeatMode.ALL
            //    PlaybackStateCompat.REPEAT_MODE_ONE -> RepeatMode.ONE
            //    else -> RepeatMode.DISABLED
            //}
        }

        private fun updateServiceState(state: PlaybackStateCompat) {
            val updatedState = state.state
            if (mediaController.metadata == null) {
                // Do not update service when no metadata.
                return
            }

            when (updatedState) {
                // Playback started or has been resumed.
                PlaybackStateCompat.STATE_PLAYING -> onPlaybackStarted()

                // Playback has been paused.
                PlaybackStateCompat.STATE_PAUSED -> onPlaybackPaused()

                // Playback ended or an error occurred.
                PlaybackStateCompat.STATE_NONE,
                PlaybackStateCompat.STATE_STOPPED,
                PlaybackStateCompat.STATE_ERROR -> onPlaybackStopped()

                else -> {
                    // Intentionally empty.
                }
            }
        }

        private fun onPlaybackStarted() {
            // Activate the media session if not active
            if (!session.isActive) {
                session.isActive = true
            }

            // Display a notification, putting the service to the foreground.
            val notification = notificationBuilder.buildNotification()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                        NOW_PLAYING_NOTIFICATION,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                startForeground(NOW_PLAYING_NOTIFICATION, notification)
            }

            // Start the service to keep it playing even when all clients unbound.
            this@FlexMusicService.startSelf()
        }

        private fun onPlaybackPaused() {
            // Put the service back to the background, keeping the notification
            stopForeground(false)
            //settings.lastPlayedPosition = player.currentPosition

            // Update the notification content if the session is active
            if (session.isActive) {
                notificationManager.notify(NOW_PLAYING_NOTIFICATION, notificationBuilder.buildNotification())
            }
        }

        private fun onPlaybackStopped() {
            // Clear notification and service foreground status
            stopForeground(true)

            // De-activate the media session.
            if (session.isActive) {
                session.isActive = false
            }

            // Stop the service, killing it if it is not bound.
            this@FlexMusicService.stop()
        }

        override fun onSessionDestroyed() {
            player.run {
                stop()
                release()
            }
        }
    }

}

