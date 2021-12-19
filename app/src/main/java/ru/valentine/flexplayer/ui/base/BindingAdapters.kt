package ru.valentine.flexplayer.core.ui

import android.net.Uri
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import ru.valentine.flexplayer.R

@BindingAdapter("image")
fun loadImage(imageView: ImageView, uri: Uri?) {
    Glide.with(imageView).load(uri).placeholder(R.drawable.ic_music_note).into(imageView)
}