package ru.valentine.flexplayer.ui.player

import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
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
import ru.valentine.flexplayer.service.playback.RepeatMode
import java.util.*

class PlayerViewModel(private val client: BrowserClient) : ViewModel() {

    val state: LiveData<PlayerState> = liveData {
        // The Flow combine operator use a cached value of this Flow when the playback state changes.
        // Mapping here ensures that the transformation is only applied when metadata has changed.
        val currentTrackState = client.nowPlaying
            .filter { it == null || it.id != null }
            .map { nowPlaying ->
                nowPlaying?.let {
                    PlayerState.Track(
                        id = it.id.parse(),
                        title = it.displayTitle!!,
                        artist = it.displaySubtitle!!,
                        duration = when (it.containsKey(MediaMetadataCompat.METADATA_KEY_DURATION)) {
                            true -> it.duration
                            else -> PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN
                        },
                        artworkUri = it.displayIconUri
                    )
                }
            }

        combine(
            client.playbackState,
            currentTrackState,
            client.shuffleMode,
            client.repeatMode
        ) { state, nowPlaying, shuffleModeCode, repeatModeCode ->
            PlayerState(
                isPlaying = state.isPlaying,
                currentTrack = nowPlaying,
                position = state.position,
                lastPositionUpdateTime = state.lastPositionUpdateTime,
                availableActions = parseAvailableActions(state.actions),
                shuffleModeEnabled = when (shuffleModeCode) {
                    PlaybackStateCompat.SHUFFLE_MODE_ALL, PlaybackStateCompat.SHUFFLE_MODE_GROUP -> true
                    else -> false
                },
                repeatMode = when (repeatModeCode) {
                    PlaybackStateCompat.REPEAT_MODE_ALL, PlaybackStateCompat.REPEAT_MODE_GROUP -> RepeatMode.ALL
                    PlaybackStateCompat.REPEAT_MODE_ONE -> RepeatMode.ONE
                    else -> RepeatMode.DISABLED
                }
            )
        }.collect {
            emit(it)
        }
    }

    private fun parseAvailableActions(actionCodes: Long): Set<PlayerState.Action> {
        val actions = EnumSet.noneOf(PlayerState.Action::class.java)

        if (actionCodes and PlaybackStateCompat.ACTION_PLAY_PAUSE != 0L) {
            actions += PlayerState.Action.TOGGLE_PLAY_PAUSE
        }

        if (actionCodes and PlaybackStateCompat.ACTION_PLAY != 0L) {
            actions += PlayerState.Action.PLAY
        }

        if (actionCodes and PlaybackStateCompat.ACTION_PAUSE != 0L) {
            actions += PlayerState.Action.PAUSE
        }

        if (actionCodes and PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS != 0L) {
            actions += PlayerState.Action.SKIP_BACKWARD
        }

        if (actionCodes and PlaybackStateCompat.ACTION_SKIP_TO_NEXT != 0L) {
            actions += PlayerState.Action.SKIP_FORWARD
        }

        if (actionCodes and PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE != 0L) {
            actions += PlayerState.Action.SET_SHUFFLE_MODE
        }

        if (actionCodes and PlaybackStateCompat.ACTION_SET_REPEAT_MODE != 0L) {
            actions += PlayerState.Action.SET_REPEAT_MODE
        }

        return actions
    }

    fun togglePlayPause() {
        viewModelScope.launch {
            when (state.value?.isPlaying) {
                true -> client.pause()
                false -> client.play()
            }
        }
    }

    fun skipToPrevious() {
        viewModelScope.launch { client.skipToPrevious() }
    }

    fun skipToNext() {
        viewModelScope.launch { client.skipToNext() }
    }

    fun seekTo(position: Long) {
        viewModelScope.launch { client.seekTo(position) }
    }

    fun toggleShuffleMode() {
        viewModelScope.launch {
            state.value?.let {
                client.setShuffleModeEnabled(!it.shuffleModeEnabled)
            }
        }
    }

    fun toggleRepeatMode() {
        viewModelScope.launch {
            state.value?.let {
                client.setRepeatMode(
                    when (it.repeatMode) {
                        RepeatMode.ALL -> PlaybackStateCompat.REPEAT_MODE_ONE
                        RepeatMode.ONE -> PlaybackStateCompat.REPEAT_MODE_NONE
                        else -> PlaybackStateCompat.REPEAT_MODE_ALL
                    }
                )
            }
        }
    }
}

data class PlayerState(
    val isPlaying: Boolean,
    val currentTrack: Track?,
    val shuffleModeEnabled: Boolean,
    val repeatMode: RepeatMode,
    val position: Long,
    val lastPositionUpdateTime: Long,
    val availableActions: Set<Action>
) {
    data class Track(
        val id: MediaId,
        val title: String,
        val artist: String,
        val duration: Long,
        val artworkUri: Uri?
    )

    enum class Action {
        TOGGLE_PLAY_PAUSE,
        PLAY,
        PAUSE,
        SKIP_FORWARD,
        SKIP_BACKWARD,
        SET_REPEAT_MODE,
        SET_SHUFFLE_MODE
    }
}