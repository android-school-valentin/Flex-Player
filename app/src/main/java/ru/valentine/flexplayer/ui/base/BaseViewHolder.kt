package ru.valentine.flexplayer.ui.base

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import ru.valentine.flexplayer.ui.ext.inflate

/**
 * @param parent The ViewGroup into which the item will be added after it is bound to
 * an adapter position.
 * @param layoutResId Identifier of the layout resource to inflate for this ViewHolder.
 */
abstract class BaseViewHolder<in T, in L>(
    parent: ViewGroup,
    @LayoutRes layoutResId: Int
) : RecyclerView.ViewHolder(parent.inflate(layoutResId)) {

    /**
     * Called when the adapter requests this ViewHolder to update its view
     * to reflect the passed media item.
     * @param data The media item this ViewHolder should represent.
     */
    abstract fun bind(data: T, listener: L)
}