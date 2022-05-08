package ru.valentine.flexplayer.data.vk

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import ru.valen.flexplayer.data.api.vkapi.response.VkAudioGetByIdResponse
import ru.valen.flexplayer.data.api.vkapi.response.VkAudioSearchResponse
import ru.valentine.flexplayer.data.vk.login.VkLoginRequest
import ru.valentine.flexplayer.data.vk.login.VkLoginResponse

interface VkService {

    @GET("audio.get")
    suspend fun getAudio(
        @Query("owner_id") ownerId: Int? = null,
        @Query("album_id") albumId: Long? = null,
        @Query("count") count: Int? = 10000,
        @Query("offset") offset: Int? = null,
        @Query("access_token") accessToken: String = "5f3c04cd30792ef465641550c0905eae940b3c282db3b66f5ecca3755c92b2ead48a526e50bd6186feedf",
        @Query("v") version: Double = 5.95
    ): VkAudioGetResponse

    @GET("audio.getById")
    suspend fun getAudioById(
        @Query("audios") id: String? = "236437680_456240266",
        @Query("access_token") accessToken: String = "5f3c04cd30792ef465641550c0905eae940b3c282db3b66f5ecca3755c92b2ead48a526e50bd6186feedf",
        @Query("v") version: Double = 5.95
    ): VkAudioGetByIdResponse

    @GET("audio.getPlaylists")
    suspend fun getPlaylists(
        @Query("owner_id") id: Long = 236437680,
        @Query("count") count: Int? = null,
        @Query("offset") offset: Int? = null,
        @Query("extended") extended: Int? = null,
        @Query("access_token") accessToken: String = "5f3c04cd30792ef465641550c0905eae940b3c282db3b66f5ecca3755c92b2ead48a526e50bd6186feedf",
        @Query("v") version: Double = 5.95
    ): VkAudioGetPlaylistResponse

    @GET("audio.search")
    suspend fun search(
        @Query("owner_id") id: Long = 236437680,
        @Query("q") query: String = "",
        @Query("count") count: Int? = 100,
        @Query("offset") offset: Int? = null,
        @Query("search_own") searchOwn: Int = 1,
        @Query("sort") sort: Int? = null, // 2 — по популярности, 1 — по длительности аудиозаписи, 0 — по дате добавления
        @Query("access_token") accessToken: String = "5f3c04cd30792ef465641550c0905eae940b3c282db3b66f5ecca3755c92b2ead48a526e50bd6186feedf",
        @Query("v") version: Double = 5.95
    ): VkAudioSearchResponse

    @POST("token")
    suspend fun login(@Body loginRequest: VkLoginRequest): Result<VkLoginResponse>

}