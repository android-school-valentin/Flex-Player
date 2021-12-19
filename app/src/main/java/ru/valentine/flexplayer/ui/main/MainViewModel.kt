package ru.valentine.flexplayer.ui.main

import android.support.v4.media.MediaBrowserCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import ru.valentine.flexplayer.ui.base.LoadRequest
import ru.valentine.flexplayer.core.media.MediaId
import ru.valentine.flexplayer.client.BrowserClient
import ru.valentine.flexplayer.client.MediaSubscriptionException
import ru.valentine.flexplayer.core.media.parse
import timber.log.Timber

class MainViewModel(private val browserClient: BrowserClient) : ViewModel() {

    init {
        Timber.d("MAIN VIEW MODEL CREATED")
        browserClient.connect()
    }

    override fun onCleared() {
        browserClient.disconnect()
        super.onCleared()
    }

    val tracks: LiveData<LoadRequest<List<MediaBrowserCompat.MediaItem>>> =
        childrenOf(MediaId.ALL_TRACKS)
    val albums: LiveData<LoadRequest<List<MediaBrowserCompat.MediaItem>>> =
        childrenOf(MediaId.ALL_ALBUMS)
    val saved: LiveData<LoadRequest<List<MediaBrowserCompat.MediaItem>>> =
        childrenOf(MediaId.ALL_SAVED)
    val recommendations: LiveData<LoadRequest<List<MediaBrowserCompat.MediaItem>>> =
        childrenOf(MediaId.ALL_RECOMMENDATIONS)

    private fun childrenOf(parentId: MediaId) = browserClient.getChildren(parentId)
        .loadState()
        .asLiveData()

    fun playMedia(mediaItem: MediaBrowserCompat.MediaItem) {
        viewModelScope.launch {
            browserClient.playFromMediaId(mediaItem.mediaId.parse())
        }
    }


}

private fun <T> Flow<T>.loadState(): Flow<LoadRequest<T>> =
    map { LoadRequest.Success(it) as LoadRequest<T> }
        .onStart { emit(LoadRequest.Pending) }
        .catch { if (it is MediaSubscriptionException) emit(LoadRequest.Error(it)) }