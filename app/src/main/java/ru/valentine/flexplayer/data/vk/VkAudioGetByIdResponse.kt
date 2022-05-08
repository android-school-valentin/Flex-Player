package ru.valen.flexplayer.data.api.vkapi.response


import com.google.gson.annotations.SerializedName
import ru.valentine.flexplayer.data.vk.VkAudioItem

data class VkAudioGetByIdResponse(
    @SerializedName("response")
    val response: List<VkAudioItem> = listOf()
)