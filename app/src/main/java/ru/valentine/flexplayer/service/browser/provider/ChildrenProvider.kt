package ru.valentine.flexplayer.service.browser.provider

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.valentine.flexplayer.core.media.MediaId
import ru.valentine.flexplayer.service.browser.tree.MediaContent

/**
 * Provides children of media categories.
 */
abstract class ChildrenProvider {

    /**
     * Returns children of a given media as an asynchronous stream of events.
     *
     * @param parentId The media id of the parent in the media tree.
     * This parent should be browsable.
     * @return An asynchronous stream whose latest emitted value is the current list of children
     * of the given parent. A new list of children is emitted whenever it changes.
     * The returned flow throws [NoSuchElementException] if the requested parent
     * is not browsable or is not part of the media tree.
     */
    fun getChildren(parentId: MediaId): Flow<List<MediaContent>> = when (parentId.track) {
        null -> findChildren(parentId)
        else -> flow<Nothing> {
            throw NoSuchElementException("$parentId is not browsable")
        }
    }

    /**
     * Override this function to provide children of a given browsable media.
     *
     * @param parentId The media id of the browsable parent in the media tree.
     * @return asynchronous stream whose last emitted value is the current list of children
     * of the given media restricted by the provided pagination parameters.
     * The returned flow should throw [NoSuchElementException] if the requested parent
     * is not browsable or is not part of the media tree.
     */
    protected abstract fun findChildren(parentId: MediaId): Flow<List<MediaContent>>
}
