package ru.valentine.flexplayer.ui.album

import android.support.v4.media.MediaBrowserCompat
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import ru.valentine.flexplayer.ui.base.MediaItemClickListener
import ru.valentine.flexplayer.ui.base.MediaItemDiff

class AlbumAdapter(private val clickListener: MediaItemClickListener) :
    ListAdapter<MediaBrowserCompat.MediaItem, AlbumViewHolder>(MediaItemDiff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        return AlbumViewHolder(parent)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }
}