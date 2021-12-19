package ru.valentine.flexplayer.ui.album

import android.support.v4.media.MediaBrowserCompat
import android.view.ViewGroup
import ru.valentine.flexplayer.R
import ru.valentine.flexplayer.ui.base.BaseViewHolder
import ru.valentine.flexplayer.ui.base.MediaItemClickListener
import ru.valentine.flexplayer.databinding.ItemAlbumBinding

class AlbumViewHolder(parent: ViewGroup) :
    BaseViewHolder<MediaBrowserCompat.MediaItem, MediaItemClickListener>(
        parent,
        R.layout.item_album
    ) {

    private val binding: ItemAlbumBinding = ItemAlbumBinding.bind(itemView)

    override fun bind(data: MediaBrowserCompat.MediaItem, listener: MediaItemClickListener) {
        binding.mediaItem = data
        binding.clickListener = listener
    }

}