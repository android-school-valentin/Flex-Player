package ru.valentine.flexplayer.data.vk.login


import com.google.gson.annotations.SerializedName

data class VkLoginResponse(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("expires_in")
    val expiresIn: Int,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("error_description")
    val errorDescription: String
)