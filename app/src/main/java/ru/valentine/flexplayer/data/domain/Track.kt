package ru.valentine.flexplayer.data.domain

data class Track(
        val id: Long, // note that is vk id
        val title: String,
        val album: String,
        val artist: String,
        val duration: Long,
        val mediaUri: String,
        val albumArtUri: String?,
)