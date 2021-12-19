package ru.valentine.flexplayer.service.browser

import kotlinx.coroutines.flow.Flow
import ru.valentine.flexplayer.core.media.MediaId
import ru.valentine.flexplayer.service.browser.tree.MediaContent

interface BrowserTree {

    /**
     * Retrieve children media of a media with the given [parentId] in the browser tree.
     * The nature of those children depends on the media id of its parent and the internal structure of the media tree.
     * See [MediaId] for more information.
     *
     * @param parentId The media id of a media whose children should be loaded.
     *
     * @return An asynchronous stream whose latest emitted value is the current list of children
     * of the specified parent node (whose media id is [parentId]) in the media tree.
     * A new list of children is emitted whenever it has changed.
     * The returned flow will throw [NoSuchElementException] if the requested parent node
     * is not browsable or not part of the media tree.
     */
    fun getChildren(parentId: MediaId): Flow<List<MediaContent>>

    /**
     * Retrieve an item identified by the specified [itemId] from the media tree.
     * If no item matches that media id, `null` is returned.
     *
     * @param itemId The media id of the item to retrieve.
     * @return An item with the same media id as the requested one, or `null` if no item matches.
     */
    suspend fun getItem(itemId: MediaId): MediaContent?

    /**
     * Search the browser tree for media whose title matches the supplied [query].
     * What exactly should be searched is determined by the type of the [SearchQuery].
     *
     * @param query The client-provided search query.
     * @return A list of media matching the search criteria.
     */
    suspend fun search(query: String): List<MediaContent>
}

/**
 * Define the parameters for paginating media items returned by [BrowserTree.getChildren].
 *
 * @param page The index of the page of results to return, `0` being the first page.
 * @param size The number of items returned per page.
 */
class PaginationOptions(page: Int, size: Int) {
    val page = page.coerceAtLeast(MINIMUM_PAGE_NUMBER)
    val size = size.coerceAtLeast(MINIMUM_PAGE_SIZE)

    companion object {

        /**
         * The default index of the returned page of media children when none is specified.
         * This is the index of the first page.
         */
        const val DEFAULT_PAGE_NUMBER = 0

        /**
         * The default number of media items to return in a page when none is specified.
         * All children will be returned in the same page.
         */
        const val DEFAULT_PAGE_SIZE = Int.MAX_VALUE

        /**
         * The minimum accepted value for [PaginationOptions.page].
         * This is the index of the first page.
         */
        private const val MINIMUM_PAGE_NUMBER = 0

        /**
         * The minimum accepted value for [PaginationOptions.size].
         * This is the minimum of items that can be displayed in a page.
         */
        private const val MINIMUM_PAGE_SIZE = 1
    }
}