package ru.valentine.flexplayer.ui.base

import android.support.v4.media.MediaBrowserCompat
import android.text.TextUtils
import androidx.recyclerview.widget.DiffUtil

class MediaItemDiff : DiffUtil.ItemCallback<MediaBrowserCompat.MediaItem>() {

    override fun areItemsTheSame(
        oldItem: MediaBrowserCompat.MediaItem,
        newItem: MediaBrowserCompat.MediaItem
    ) =
        oldItem.mediaId == newItem.mediaId

    override fun areContentsTheSame(
        oldItem: MediaBrowserCompat.MediaItem,
        newItem: MediaBrowserCompat.MediaItem
    ): Boolean {
        val oldDesc = oldItem.description
        val newDesc = newItem.description

        return TextUtils.equals(oldDesc.title, newDesc.title)
                && TextUtils.equals(oldDesc.subtitle, newDesc.subtitle)
                && oldDesc.iconUri == newDesc.iconUri
                && oldDesc.mediaUri == newDesc.mediaUri
                && TextUtils.equals(oldDesc.description, newDesc.description)
    }

}