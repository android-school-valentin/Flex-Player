package ru.valen.flexplayer.data.api.vkapi.response


import com.google.gson.annotations.SerializedName

data class VkAudioGetByIdResponse(
    @SerializedName("response")
    val response: List<VkAudioItem> = listOf()
)