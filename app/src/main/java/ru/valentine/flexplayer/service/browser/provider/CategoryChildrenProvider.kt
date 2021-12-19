package ru.valentine.flexplayer.service.browser.provider

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import ru.valentine.flexplayer.core.media.MediaId
import ru.valentine.flexplayer.service.browser.tree.MediaCategory
import ru.valentine.flexplayer.service.browser.tree.MediaContent
import ru.valentine.flexplayer.service.browser.tree.MediaTree

/**
 * Provides children from a pre-configured set of categories.
 * Depending on the requested parent media id, this returns either all configured categories
 * or the children of a specific category.
 */
class CategoryChildrenProvider(
    private val categories: Map<String, MediaTree.Category>
) : ChildrenProvider() {

    override fun findChildren(parentId: MediaId): Flow<List<MediaContent>> {
        return when (val categoryId = parentId.category) {
            null -> getCategories()
            else -> getCategoryChildren(categoryId)
        }
    }

    private fun getCategoryChildren(categoryId: String?): Flow<List<MediaContent>> =
        categories[categoryId]
            ?.children()
            ?: flow { throw NoSuchElementException("No such category: $categoryId") }

    private fun getCategories(): Flow<List<MediaCategory>> = flow {
        val categoryItems = categories.map { (_, category) -> category.item }

        emit(categoryItems)
        suspendCancellableCoroutine<Nothing> {}
    }
}