package ru.valentine.flexplayer.service.browser

import android.content.Context
import kotlinx.coroutines.flow.Flow
import ru.valentine.flexplayer.R
import ru.valentine.flexplayer.core.media.MediaId
import ru.valentine.flexplayer.core.media.MediaId.Builder.CATEGORY_ALL
import ru.valentine.flexplayer.core.media.MediaId.Builder.TYPE_ALBUMS
import ru.valentine.flexplayer.core.media.MediaId.Builder.TYPE_TRACKS
import ru.valentine.flexplayer.data.repository.AlbumRepository
import ru.valentine.flexplayer.data.repository.TrackRepository
import ru.valentine.flexplayer.service.browser.provider.AlbumChildrenProvider
import ru.valentine.flexplayer.service.browser.provider.TrackChildrenProvider
import ru.valentine.flexplayer.service.browser.tree.MediaContent
import ru.valentine.flexplayer.service.browser.tree.MediaTree

class BrowserTreeImpl(
    private val context: Context,
    private val trackRepository: TrackRepository,
    private val albumRepository: AlbumRepository
) :
    BrowserTree {

    /**
     * The tree structure of the media browser.
     */
    private val tree = MediaTree(
        rootId = MediaId.ROOT,
        rootName = context.getString(R.string.browser_root_title)
    ) {
        type(
            TYPE_TRACKS,
            title = context.getString(R.string.tracks_type_title)
        ) {
            val res = context.resources
            val trackProvider = TrackChildrenProvider(trackRepository)

            category(
                CATEGORY_ALL,
                title = res.getString(R.string.all_music),
                playable = true,
                provider = trackProvider
            )
        }

        type(
            TYPE_ALBUMS,
            title = context.getString(R.string.albums_type_title),
            provider = AlbumChildrenProvider(albumRepository)
        )

    }


    override fun getChildren(parentId: MediaId): Flow<List<MediaContent>> {
        return tree.getChildren(parentId)
    }

    override suspend fun getItem(itemId: MediaId): MediaContent? {
        return tree.getItem(itemId)
    }

    override suspend fun search(query: String): List<MediaContent> {
        TODO("Not yet implemented")
    }
}