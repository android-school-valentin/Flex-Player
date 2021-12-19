package ru.valentine.flexplayer.ui.base

import android.support.v4.media.MediaBrowserCompat

class MediaItemClickListener(val clickListener: (mediaItem: MediaBrowserCompat.MediaItem) -> Unit) {
    fun onClick(mediaItem: MediaBrowserCompat.MediaItem) = clickListener(mediaItem)
}