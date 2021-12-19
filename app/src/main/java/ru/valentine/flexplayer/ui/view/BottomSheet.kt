package ru.valentine.flexplayer.ui.view

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout

class BottomSheet : LinearLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        // Prevent from dispatching touch events to views behind the Bottom Sheet.
        isFocusable = true
        isClickable = true
    }

    override fun dispatchSetPressed(pressed: Boolean) {
        // Do not dispatch pressed state to children.
    }

    override fun dispatchSetActivated(activated: Boolean) {
        // Do not dispatch activated state to children.
    }
}