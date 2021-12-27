package ru.valentine.flexplayer.service

import android.app.Notification
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.DefaultControlDispatcher
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.koin.android.ext.android.inject
import ru.valentine.flexplayer.R
import ru.valentine.flexplayer.core.media.MalformedMediaIdException
import ru.valentine.flexplayer.core.media.parse
import ru.valentine.flexplayer.service.browser.BrowserTree
import ru.valentine.flexplayer.service.browser.tree.AudioTrack
import ru.valentine.flexplayer.service.browser.tree.MediaCategory
import ru.valentine.flexplayer.service.browser.tree.MediaContent
import ru.valentine.flexplayer.service.ext.EXTRA_DURATION
import ru.valentine.flexplayer.service.ext.EXTRA_NUMBER_OF_TRACKS
import ru.valentine.flexplayer.service.playback.*
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

const val NOW_PLAYING_CHANNEL_ID = "ru.valentine.flexplayer.media.NOW_PLAYING"
const val NOW_PLAYING_NOTIFICATION_ID = 0xffcdf // Arbitrary number used to identify notification

class FlexMusicService : MediaBrowserServiceCompat(), CoroutineScope {

    private lateinit var scopeJob: Job
    override val coroutineContext: CoroutineContext
        get() = scopeJob + Dispatchers.Main.immediate

    private val browserTree: BrowserTree by inject()
    private val session: MediaSessionCompat by inject()
    private val player: ExoPlayer by inject()
    private val playerListener = PlayerEventListener()

    private lateinit var mediaController: MediaControllerCompat
    private lateinit var notificationManager: PlayerNotificationManager

    private var isForegroundService = false

    override fun onCreate() {
        super.onCreate()

        scopeJob = SupervisorJob()

        session.isActive = true

        player.addListener(playerListener)

        mediaController = MediaControllerCompat(this, session)

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

        notificationManager = PlayerNotificationManager.createWithNotificationChannel(
                this,
                NOW_PLAYING_CHANNEL_ID,
                R.string.channel_media_session,
                R.string.channel_media_session_description,
                NOW_PLAYING_NOTIFICATION_ID,
                DescriptionAdapter(this, this, mediaController),
                FlexNotificationListener()
        ).apply {

            setMediaSessionToken(session.sessionToken)
            setSmallIcon(R.drawable.ic_music_note)
            setControlDispatcher(DefaultControlDispatcher(0, 0))

        }
        notificationManager.setPlayer(player)

        sessionToken = session.sessionToken

    }

    override fun onDestroy() {
        session.run {
            isActive = false
            release()
        }

        scopeJob.cancel()

        notificationManager.setPlayer(null)
        player.removeListener(playerListener)
        player.release()
        super.onDestroy()
    }

    override fun onGetRoot(
            clientPackageName: String,
            clientUid: Int,
            rootHints: Bundle?
    ): BrowserRoot {
        return BrowserRoot("root", null)
    }

    override fun onLoadChildren(
            parentId: String,
            result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
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

    private inner class FlexNotificationListener :
            PlayerNotificationManager.NotificationListener {
        override fun onNotificationPosted(
                notificationId: Int,
                notification: Notification,
                ongoing: Boolean
        ) {
            if (ongoing && !isForegroundService) {
                ContextCompat.startForegroundService(
                        applicationContext,
                        Intent(applicationContext, this@FlexMusicService.javaClass)
                )

                startForeground(notificationId, notification)
                isForegroundService = true
            }
        }

        override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
            stopForeground(true)
            isForegroundService = false
            stopSelf()
        }
    }

    private inner class PlayerEventListener : Player.EventListener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            when (playbackState) {
                Player.STATE_BUFFERING,
                Player.STATE_READY -> {
                    notificationManager.setPlayer(player)
                    if (playbackState == Player.STATE_READY) {
                        if (!playWhenReady) {
                            stopForeground(false)
                            isForegroundService = false
                        }
                    }
                }
                else -> {
                    notificationManager.setPlayer(null)
                }
            }
        }
    }

}

