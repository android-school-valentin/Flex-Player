package ru.valentine.flexplayer.ui.base

/**
 * Encapsulate state of a data load operation.
 * @param T The type of data to be loaded.
 */
sealed class LoadRequest<out T> {

    /**
     * State of a data load that is pending.
     */
    object Pending : LoadRequest<Nothing>()

    /**
     * State of a data load where the data is available.
     * @param T The type of data that has been loaded.
     * @param data The loaded data.
     */
    class Success<T>(val data: T) : LoadRequest<T>()

    /**
     * State of a data load that has failed for some reason.
     * @param error An exception describing the error that occurred.
     */
    class Error(val error: Exception?) : LoadRequest<Nothing>()
}
