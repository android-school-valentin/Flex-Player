package ru.valentine.flexplayer.service.ext

import android.support.v4.media.MediaBrowserCompat
import ru.valentine.flexplayer.util.TextUtils

const val EXTRA_DURATION = "ru.valentine.music.extra.DURATION"

const val EXTRA_NUMBER_OF_TRACKS = "ru.valentine.music.extra.NUMBER_OF_TRACKS"

//todo: add setters!

val MediaBrowserCompat.MediaItem.duration: String
    get() {
        val extras = checkNotNull(description.extras)
        return TextUtils.formatMillis(extras.getLong(EXTRA_DURATION))
    }


val MediaBrowserCompat.MediaItem.trackCount: Int
    get() {
        val extras = checkNotNull(description.extras)
        return extras.getInt(EXTRA_NUMBER_OF_TRACKS)
    }
