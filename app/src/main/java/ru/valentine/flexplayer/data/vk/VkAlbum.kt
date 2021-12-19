package ru.valentine.flexplayer.data.vk

import com.google.gson.annotations.SerializedName
import ru.valen.flexplayer.data.api.vkapi.response.VkAudioItem
import ru.valentine.flexplayer.data.domain.Album

data class VkAlbum(
    @SerializedName("access_key")
    val accessKey: String = "",
    @SerializedName("album_type")
    val albumType: String = "",
    @SerializedName("count")
    val count: Int = 0,
    @SerializedName("create_time")
    val createTime: Int = 0,
    @SerializedName("description")
    val description: String = "",
    @SerializedName("followed")
    val followed: Followed = Followed(),
    @SerializedName("followers")
    val followers: Int = 0,
    @SerializedName("genres")
    val genres: List<Genre> = listOf(),
    @SerializedName("id")
    val id: Long = 0,
    @SerializedName("is_explicit")
    val isExplicit: Boolean = false,
    @SerializedName("is_following")
    val isFollowing: Boolean = false,
    @SerializedName("main_artists")
    val mainArtists: List<VkAudioItem.MainArtist> = listOf(),
    @SerializedName("original")
    val original: Original = Original(),
    @SerializedName("owner_id")
    val ownerId: Long = 0,
    @SerializedName("photo")
    val photo: Photo = Photo(),
    @SerializedName("plays")
    val plays: Int = 0,
    @SerializedName("title")
    val title: String = "",
    @SerializedName("type")
    val type: Int = 0,
    @SerializedName("update_time")
    val updateTime: Int = 0,
    @SerializedName("year")
    val year: Int = 0
) {
    data class Followed(
        @SerializedName("owner_id")
        val ownerId: Int = 0,
        @SerializedName("playlist_id")
        val playlistId: Int = 0
    )

    data class Genre(
        @SerializedName("id")
        val id: Int = 0,
        @SerializedName("name")
        val name: String = ""
    )

    data class MainArtist(
        @SerializedName("domain")
        val domain: String = "",
        @SerializedName("id")
        val id: String = "",
        @SerializedName("name")
        val name: String = ""
    )

    data class Original(
        @SerializedName("access_key")
        val accessKey: String = "",
        @SerializedName("owner_id")
        val ownerId: Int = 0,
        @SerializedName("playlist_id")
        val playlistId: Int = 0
    )

    data class Photo(
        @SerializedName("height")
        val height: Int = 0,
        @SerializedName("photo_1200")
        val photo1200: String = "",
        @SerializedName("photo_135")
        val photo135: String = "",
        @SerializedName("photo_270")
        val photo270: String = "",
        @SerializedName("photo_300")
        val photo300: String = "",
        @SerializedName("photo_34")
        val photo34: String = "",
        @SerializedName("photo_600")
        val photo600: String = "",
        @SerializedName("photo_68")
        val photo68: String = "",
        @SerializedName("width")
        val width: Int = 0
    )
}

fun VkAlbum.toAlbum(): Album {
    return Album(id, title, original.ownerId.toString(), count, photo.photo600)
}