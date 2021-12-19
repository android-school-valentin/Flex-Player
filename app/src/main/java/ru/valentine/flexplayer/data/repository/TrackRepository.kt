package ru.valentine.flexplayer.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.valen.flexplayer.data.api.vkapi.response.toTrack
import ru.valentine.flexplayer.data.domain.Track
import ru.valentine.flexplayer.data.vk.VkService

class TrackRepository(private val vkService: VkService) {

    fun getAllTracks() : Flow<List<Track>> {
        return flow {  emit(vkService.getAudio().response.items.map { it.toTrack() }) }
    }

}