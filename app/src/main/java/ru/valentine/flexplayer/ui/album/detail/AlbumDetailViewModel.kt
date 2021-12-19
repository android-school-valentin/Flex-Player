package ru.valentine.flexplayer.ui.album.detail

import android.support.v4.media.MediaBrowserCompat
import androidx.lifecycle.*
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import ru.valentine.flexplayer.client.BrowserClient
import ru.valentine.flexplayer.core.media.MediaId
import ru.valentine.flexplayer.core.media.parse

class AlbumDetailViewModel(private val browserClient: BrowserClient) : ViewModel() {

    private val albumId = MutableLiveData<MediaId>()

    val album = albumId.asFlow().distinctUntilChanged().flatMapLatest { albumId ->
        flow {
            val temp = browserClient.getItem(albumId)
            checkNotNull(temp)
            emit(temp)
        }
    }.asLiveData()

    val albumTracks = albumId.asFlow().distinctUntilChanged().flatMapLatest { albumId ->
        browserClient.getChildren(albumId)
    }.asLiveData()

    fun playAlbum() {
        val albumId = this.albumId.value
        if (albumId != null) {
            playMedia(albumId)
        }
    }

    fun playTrack(track: MediaBrowserCompat.MediaItem) {
        playMedia(track.mediaId.parse())
    }

    private fun playMedia(mediaId: MediaId) = viewModelScope.launch {
        browserClient.playFromMediaId(mediaId)
    }

    fun setAlbumId(albumId: String) {
        this.albumId.value = albumId.parse()
    }

}