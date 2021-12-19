package ru.valentine.flexplayer.ui.track

import android.support.v4.media.MediaBrowserCompat
import android.view.ViewGroup
import ru.valentine.flexplayer.R
import ru.valentine.flexplayer.ui.base.BaseViewHolder
import ru.valentine.flexplayer.ui.base.MediaItemClickListener
import ru.valentine.flexplayer.databinding.ItemTrackBinding

class TrackViewHolder(parent: ViewGroup) :
    BaseViewHolder<MediaBrowserCompat.MediaItem, MediaItemClickListener>(
        parent,
        R.layout.item_track
    ) {

    private val binding: ItemTrackBinding = ItemTrackBinding.bind(itemView)

    override fun bind(data: MediaBrowserCompat.MediaItem, listener: MediaItemClickListener) {
        binding.mediaItem = data
        binding.clickListener = listener
    }

}