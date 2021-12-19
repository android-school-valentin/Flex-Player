package ru.valentine.flexplayer.data.domain

data class Album(
        val id: Long,
        val title: String,
        val description: String,
        val trackCount: Int,
        val albumArtUri: String?
)