package ru.valen.flexplayer.data.api.vkapi.response

import com.google.gson.annotations.SerializedName
import ru.valentine.flexplayer.data.domain.Track

data class VkAudioItem(
    @SerializedName("access_key")
    val accessKey: String = "",
    @SerializedName("ads")
    val ads: Ads = Ads(),
    @SerializedName("album")
    val album: Album = Album(),
    @SerializedName("artist")
    val artist: String = "",
    @SerializedName("date")
    val date: Int = 0,
    @SerializedName("duration")
    val duration: Int = 0,
    @SerializedName("featured_artists")
    val featuredArtists: List<FeaturedArtist> = listOf(),
    @SerializedName("genre_id")
    val genreId: Int = 0,
    @SerializedName("id")
    val id: Long = 0,
    @SerializedName("is_explicit")
    val isExplicit: Boolean = false,
    @SerializedName("is_hq")
    val isHq: Boolean = false,
    @SerializedName("is_licensed")
    val isLicensed: Boolean = false,
    @SerializedName("lyrics_id")
    val lyricsId: Int = 0,
    @SerializedName("main_artists")
    val mainArtists: List<MainArtist> = listOf(),
    @SerializedName("no_search")
    val noSearch: Int = 0,
    @SerializedName("owner_id")
    val ownerId: Int = 0,
    @SerializedName("short_videos_allowed")
    val shortVideosAllowed: Boolean = false,
    @SerializedName("stories_allowed")
    val storiesAllowed: Boolean = false,
    @SerializedName("stories_cover_allowed")
    val storiesCoverAllowed: Boolean = false,
    @SerializedName("subtitle")
    val subtitle: String = "",
    @SerializedName("title")
    val title: String = "",
    @SerializedName("track_code")
    val trackCode: String = "",
    @SerializedName("url")
    val url: String = ""
) {
    data class Ads(
        @SerializedName("account_age_type")
        val accountAgeType: String = "",
        @SerializedName("content_id")
        val contentId: String = "",
        @SerializedName("duration")
        val duration: String = "",
        @SerializedName("puid1")
        val puid1: String = "",
        @SerializedName("puid22")
        val puid22: String = ""
    )

    data class Album(
        @SerializedName("access_key")
        val accessKey: String = "",
        @SerializedName("id")
        val id: Long = 0,
        @SerializedName("owner_id")
        val ownerId: Int = 0,
        @SerializedName("thumb")
        val thumb: Thumb = Thumb(),
        @SerializedName("title")
        val title: String = ""
    ) {
        data class Thumb(
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

    data class FeaturedArtist(
        @SerializedName("domain")
        val domain: String = "",
        @SerializedName("id")
        val id: String = "",
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
}

fun VkAudioItem.toTrack(): Track {
    return Track(
        id,
        title,
        album.title,
        artist,
        duration * 1000L,
        url,
        album.thumb.photo600
    )
}