package ru.valentine.flexplayer.data.vk


import com.google.gson.annotations.SerializedName

data class VkAudioGetPlaylistResponse(
    @SerializedName("response")
    val response: Response = Response()
) {
    data class Response(
        @SerializedName("count")
        val count: Int = 0,
        @SerializedName("groups")
        val groups: List<Any> = listOf(),
        @SerializedName("items")
        val vkAlbums: List<VkAlbum> = listOf(),
        @SerializedName("next_from")
        val nextFrom: String = "",
        @SerializedName("profiles")
        val profiles: List<Any> = listOf()
    )
}
