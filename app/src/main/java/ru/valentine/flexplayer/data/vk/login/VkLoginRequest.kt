package ru.valentine.flexplayer.data.vk.login

import com.google.gson.annotations.SerializedName

data class VkLoginRequest(
    @SerializedName("username")
    val username: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("grant_type")
    val grantType: String = "token",
    @SerializedName("client_id")
    val clientId: String = "2274003",
    @SerializedName("client_secret")
    val clientSecret: String = "hHbZxrka2uZ6jB1inYsH",
    @SerializedName("validate_token")
    val validateToken: String = "0",

)