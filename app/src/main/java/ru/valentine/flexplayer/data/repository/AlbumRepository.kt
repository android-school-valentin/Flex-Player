package ru.valentine.flexplayer.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.valentine.flexplayer.data.vk.toTrack
import ru.valentine.flexplayer.data.domain.Album
import ru.valentine.flexplayer.data.domain.Track
import ru.valentine.flexplayer.data.vk.VkService
import ru.valentine.flexplayer.data.vk.toAlbum

class AlbumRepository(private val vkService: VkService) {

    fun getAllAlbums(): Flow<List<Album>> {
        return flow { emit(vkService.getPlaylists().response.vkAlbums.map { it.toAlbum() }) }
    }

    fun getAlbumTracks(albumId: Long): Flow<List<Track>> {
        return flow { emit(vkService.getAudio(albumId = albumId).response.items.map { it.toTrack() }) }
    }

}