package ru.valen.flexplayer.data.api.vkapi.response


import com.google.gson.annotations.SerializedName

data class VkAudioSearchResponse(
    @SerializedName("response")
    val response: Response = Response()
) {
    data class Response(
        @SerializedName("count")
        val count: Int = 0,
        @SerializedName("items")
        val items: List<VkAudioItem> = listOf()
    )
}