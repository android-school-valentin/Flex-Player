package ru.valentine.flexplayer.injection

import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.support.v4.media.RatingCompat
import android.support.v4.media.session.MediaSessionCompat
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.util.EventLogger
import org.koin.dsl.module
import ru.valentine.flexplayer.client.BrowserClient
import ru.valentine.flexplayer.client.BrowserClientImpl
import ru.valentine.flexplayer.service.browser.BrowserTree
import ru.valentine.flexplayer.service.browser.BrowserTreeImpl
import ru.valentine.flexplayer.service.playback.AudioExtractorsFactory
import ru.valentine.flexplayer.service.playback.AudioRenderersFactory

val serviceModule = module {
    single { BrowserClientImpl(get()) as BrowserClient }

    single { BrowserTreeImpl(get(), get(), get()) as BrowserTree }

    single { provideMediaSession(get()) }

    single { provideExoPlayer(get()) as ExoPlayer }

}

fun provideMediaSession(service: Application): MediaSessionCompat {
    val sessionActivityPendingIntent =
            service.packageManager.getLaunchIntentForPackage(service.packageName)
                    ?.let { sessionIntent ->
                        sessionIntent.action = "flexplayer.NO_ACTION"
                        PendingIntent.getActivity(service, 0, sessionIntent, 0)
                    }

    return MediaSessionCompat(service, "MusicService").also {
        it.setSessionActivity(sessionActivityPendingIntent)
        it.setRatingType(RatingCompat.RATING_NONE)
    }
}

fun provideExoPlayer(context: Context): ExoPlayer {
    val musicAttributes = AudioAttributes.Builder()
            .setContentType(C.CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

    val player = SimpleExoPlayer.Builder(
            context,
            AudioRenderersFactory(context),
            AudioExtractorsFactory()
    )
            .setAudioAttributes(musicAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build()

    player.addAnalyticsListener(EventLogger(null))
    player.setThrowsWhenUsingWrongThread(true)

    return player
}