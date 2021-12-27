package ru.valentine.flexplayer.ui.player.mini

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.valentine.flexplayer.client.BrowserClient
import ru.valentine.flexplayer.core.media.MediaId
import ru.valentine.flexplayer.core.media.parse
import ru.valentine.flexplayer.service.ext.*

class MiniPlayerViewModel(private val client: BrowserClient) : ViewModel() {

    val state: LiveData<MiniPlayerState> = liveData {
        val currentTrackState = client.nowPlaying
                .filter { it == null || it.id != null }
                .map { nowPlaying ->
                    nowPlaying?.let {
                        MiniPlayerState.Track(
                            id = it.id.parse(),
                            title = it.displayTitle!!,
                            artist = it.displaySubtitle!!,
                            albumArt = it.displayIconUri!!
                        )
                    }
                }
        combine(client.playbackState, currentTrackState) { state, nowPlaying ->
            MiniPlayerState(isPlaying = state.isPlaying, currentTrack = nowPlaying)
        }.collect { emit(it) }
    }

    fun togglePlayPause() {
        viewModelScope.launch {
            when (state.value?.isPlaying) {
                true -> client.pause()
                false -> client.play()
            }
        }
    }

}

data class MiniPlayerState(
    val isPlaying: Boolean,
    val currentTrack: Track?,
) {
    data class Track(
            val id: MediaId,
            val title: String,
            val artist: String,
            val albumArt: Uri
    )
}