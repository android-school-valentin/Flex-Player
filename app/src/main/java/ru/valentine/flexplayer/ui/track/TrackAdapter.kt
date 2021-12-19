package ru.valentine.flexplayer.ui.track

import android.support.v4.media.MediaBrowserCompat
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import ru.valentine.flexplayer.ui.base.MediaItemClickListener
import ru.valentine.flexplayer.ui.base.MediaItemDiff

class TrackAdapter(private val clickListener: MediaItemClickListener) :
    ListAdapter<MediaBrowserCompat.MediaItem, TrackViewHolder>(MediaItemDiff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        return TrackViewHolder(parent)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }
}