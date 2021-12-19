package ru.valentine.flexplayer.service.playback

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.media.session.MediaButtonReceiver
import ru.valentine.flexplayer.R
import ru.valentine.flexplayer.service.ext.*

private const val NOW_PLAYING_CHANNEL = "fr.nihilus.music.media.NOW_PLAYING"
const val NOW_PLAYING_NOTIFICATION = 0x1ee7

class NotificationBuilder(
        private val context: Context,
        session: MediaSessionCompat
) {

    private val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val controller = MediaControllerCompat(context, session.sessionToken)

    /**
     * An action that does nothing.
     * Used to display a blank space in lieu of a disabled action.
     */
    private val noOpAction = NotificationCompat.Action(R.drawable.ic_blank, null, null)

    private val previousAction = NotificationCompat.Action(
            R.drawable.ic_skip_previous,
            context.getString(R.string.action_previous),
            MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
    )

    private val playAction = NotificationCompat.Action(
            R.drawable.ic_play,
            context.getString(R.string.action_play),
            MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_PLAY)
    )

    private val pauseAction = NotificationCompat.Action(
            R.drawable.ic_pause,
            context.getString(R.string.action_pause),
            MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_PAUSE)
    )

    private val nextAction = NotificationCompat.Action(
            R.drawable.ic_skip_next,
            context.getString(R.string.action_next),
            MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_SKIP_TO_NEXT)
    )

    private val stopPendingIntent: PendingIntent =
            MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_STOP)

    fun buildNotification(): Notification {
        if (shouldCreateNowPlayingChannel()) {
            createNowPlayingChannel()
        }

        val metadata = controller.metadata
        val playbackState = controller.playbackState

        val isPlaying = playbackState.isPlaying

        // Display notification actions depending on playback state and actions availability
        val builder = NotificationCompat.Builder(context, NOW_PLAYING_CHANNEL)
        builder.addAction(if (playbackState.isSkipToPreviousEnabled) previousAction else noOpAction)
        if (isPlaying) {
            builder.addAction(pauseAction)
        } else if (playbackState.isPlayEnabled) {
            builder.addAction(playAction)
        }
        builder.addAction(if (playbackState.isSkipToNextEnabled) nextAction else noOpAction)

        // Display current playback position as a chronometer on Android 9 and older.
        // On Android 10 and onwards a progress bar is already displayed in the notification.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (isPlaying && playbackState.position >= 0) {
                builder.setWhen(System.currentTimeMillis() - playbackState.position)
                        .setUsesChronometer(true)
                        .setShowWhen(true)
            } else {
                builder.setWhen(0)
                        .setUsesChronometer(false)
                        .setShowWhen(false)
            }
        }

        // Specific style for media playback notifications
        val mediaStyle = androidx.media.app.NotificationCompat.MediaStyle()
                .setCancelButtonIntent(stopPendingIntent)
                .setMediaSession(controller.sessionToken)
                .setShowActionsInCompactView(0, 1, 2)
                .setShowCancelButton(true)

        return builder.setContentIntent(controller.sessionActivity)
                .setContentTitle(metadata.displayTitle)
                .setContentText(metadata.displaySubtitle)
                .setDeleteIntent(stopPendingIntent)
                .setLargeIcon(metadata.albumArt)
                .setShowWhen(false)
                .setOnlyAlertOnce(true)
                .setSmallIcon(if (isPlaying) R.drawable.ic_play else R.drawable.ic_pause)
                .setStyle(mediaStyle)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build()
    }

    private fun shouldCreateNowPlayingChannel(): Boolean =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !nowPlayingChannelExists()

    @RequiresApi(Build.VERSION_CODES.O)
    private fun nowPlayingChannelExists(): Boolean =
            notificationManager.getNotificationChannel(NOW_PLAYING_CHANNEL) != null

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNowPlayingChannel() {
        val notificationChannel = NotificationChannel(
                NOW_PLAYING_CHANNEL,
                context.getString(R.string.channel_mediasession),
                NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.channel_mediasession_description)
            lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            setShowBadge(false)
        }

        notificationManager.createNotificationChannel(notificationChannel)
    }
}