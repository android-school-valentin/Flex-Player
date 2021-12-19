package ru.valentine.flexplayer.service.browser.provider

import androidx.core.net.toUri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import ru.valentine.flexplayer.core.media.MediaId
import ru.valentine.flexplayer.core.media.MediaId.Builder.CATEGORY_ALL
import ru.valentine.flexplayer.core.media.MediaId.Builder.TYPE_TRACKS
import ru.valentine.flexplayer.data.domain.Track
import ru.valentine.flexplayer.data.repository.TrackRepository
import ru.valentine.flexplayer.service.browser.tree.AudioTrack
import ru.valentine.flexplayer.service.browser.tree.MediaContent

class TrackChildrenProvider(private val trackRepository: TrackRepository) : ChildrenProvider() {


    override fun findChildren(parentId: MediaId): Flow<List<MediaContent>> {
        check(parentId.type == TYPE_TRACKS && parentId.category != null)

        return when (parentId.category) {
            CATEGORY_ALL -> getAllTracks()
            else -> flow { throw NoSuchElementException("No such parent: $parentId") }
        }

    }

    private fun getAllTracks(): Flow<List<AudioTrack>> {
        return trackRepository.getAllTracks().map { tracks ->
            tracks.map {
                it.toPlayableMedia(
                    CATEGORY_ALL
                )
            }
        }
    }

    private fun Track.toPlayableMedia(
        category: String
    ) = AudioTrack(
        id = MediaId(TYPE_TRACKS, category, id),
        title = title,
        artist = artist,
        album = album,
        mediaUri = mediaUri.toUri(),
        iconUri = albumArtUri?.toUri(),
        duration = duration
    )

}