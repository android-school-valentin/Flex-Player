package ru.valentine.flexplayer.service.playback

import android.os.Bundle
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import ru.valentine.flexplayer.service.browser.tree.AudioTrack

class FlexQueueNavigator(session: MediaSessionCompat) : TimelineQueueNavigator(session) {
    override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
        val currentItem = player.getMediaItemAt(windowIndex)
        val track = currentItem.playbackProperties?.tag as AudioTrack
        val extras = Bundle()
        extras.apply {
            putString(MediaMetadataCompat.METADATA_KEY_ARTIST, track.artist)
            putString(MediaMetadataCompat.METADATA_KEY_ALBUM, track.album)
        }


        return MediaDescriptionCompat.Builder()
                .setMediaId(track.id.encoded)
                .setTitle(track.title)
                .setSubtitle(track.artist)
                .setIconUri(track.iconUri)
                .setExtras(extras)
                .build()
    }
}