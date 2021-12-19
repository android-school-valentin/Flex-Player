package ru.valentine.flexplayer.data.vk

import retrofit2.http.GET
import retrofit2.http.Query
import ru.valen.flexplayer.data.api.vkapi.response.VkAudioGetByIdResponse
import ru.valen.flexplayer.data.api.vkapi.response.VkAudioSearchResponse

interface VkService {

    @GET("audio.get")
    suspend fun getAudio(
        @Query("owner_id") ownerId: Int? = null,
        @Query("album_id") albumId: Long? = null,
        @Query("count") count: Int? = 10000,
        @Query("offset") offset: Int? = null,
        @Query("access_token") accessToken: String = "29ab83f098d738baa6f1dd6a6a50bdfa1e01292ee5385614d212921c0bd5ac9e6d539a26bd249508d9872",
        @Query("v") version: Double = 5.95
    ): VkAudioGetResponse

    @GET("audio.getById")
    suspend fun getAudioById(
        @Query("audios") id: String? = "236437680_456240266",
        @Query("access_token") accessToken: String = "29ab83f098d738baa6f1dd6a6a50bdfa1e01292ee5385614d212921c0bd5ac9e6d539a26bd249508d9872",
        @Query("v") version: Double = 5.95
    ): VkAudioGetByIdResponse

    @GET("audio.getPlaylists")
    suspend fun getPlaylists(
        @Query("owner_id") id: Long = 236437680,
        @Query("count") count: Int? = null,
        @Query("offset") offset: Int? = null,
        @Query("extended") extended: Int? = null,
        @Query("access_token") accessToken: String = "29ab83f098d738baa6f1dd6a6a50bdfa1e01292ee5385614d212921c0bd5ac9e6d539a26bd249508d9872",
        @Query("v") version: Double = 5.95
    ): VkAudioGetPlaylistResponse

    @GET("audio.search")
    suspend fun search(
        @Query("owner_id") id: Long = 236437680,
        @Query("q") query: String = "",
        @Query("count") count: Int? = 100,
        @Query("offset") offset: Int? = null,
        @Query("search_own") searchOwn:Int = 1,
        @Query("sort") sort:Int? = null, // 2 — по популярности, 1 — по длительности аудиозаписи, 0 — по дате добавления
        @Query("access_token") accessToken: String = "29ab83f098d738baa6f1dd6a6a50bdfa1e01292ee5385614d212921c0bd5ac9e6d539a26bd249508d9872",
        @Query("v") version: Double = 5.95
    ) : VkAudioSearchResponse


}