package ru.valentine.flexplayer.service.browser.provider

import androidx.core.net.toUri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.valentine.flexplayer.core.media.MediaId
import ru.valentine.flexplayer.core.media.MediaId.Builder.TYPE_ALBUMS
import ru.valentine.flexplayer.data.domain.Album
import ru.valentine.flexplayer.data.domain.Track
import ru.valentine.flexplayer.data.repository.AlbumRepository
import ru.valentine.flexplayer.service.browser.tree.AudioTrack
import ru.valentine.flexplayer.service.browser.tree.MediaCategory
import ru.valentine.flexplayer.service.browser.tree.MediaContent

class AlbumChildrenProvider(private val albumRepository: AlbumRepository) : ChildrenProvider() {

    override fun findChildren(parentId: MediaId): Flow<List<MediaContent>> {
        check(parentId.type == TYPE_ALBUMS)

        val albumId = parentId.category?.toLongOrNull()
        return when {
            albumId != null -> getAlbumTracks(albumId)
            else -> getAlbums()
        }
    }

    private fun getAlbumTracks(albumId: Long): Flow<List<AudioTrack>> {
        return albumRepository.getAlbumTracks(albumId)
            .map { tracks -> tracks.map { it.toPlayableMedia(albumId) } }
    }

    private fun getAlbums(): Flow<List<MediaCategory>> {
        return albumRepository.getAllAlbums().map { albums -> albums.map { it.toCategory() } }
    }

    private fun Album.toCategory(): MediaCategory {
        return MediaCategory(
            id = MediaId(TYPE_ALBUMS, id.toString()),
            title = title,
            subtitle = description,
            iconUri = albumArtUri?.toUri(),
            playable = true,
            count = trackCount
        )
    }

    private fun Track.toPlayableMedia(albumId: Long): AudioTrack {
        return AudioTrack(
            id = MediaId(TYPE_ALBUMS, albumId.toString(), id),
            title = title,
            artist = artist,
            album = album,
            mediaUri = mediaUri.toUri(),
            iconUri = albumArtUri?.toUri(),
            duration = duration,
        )
    }

}