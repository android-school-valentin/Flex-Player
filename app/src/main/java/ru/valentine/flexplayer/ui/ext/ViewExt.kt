package ru.valentine.flexplayer.ui.ext

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

fun ViewGroup.inflate(@LayoutRes resource: Int, attach: Boolean = false): View =
    LayoutInflater.from(context).inflate(resource, this, attach)