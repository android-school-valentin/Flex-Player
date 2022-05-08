package ru.valentine.flexplayer.injection

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.valentine.flexplayer.data.vk.VkService
import ru.valentine.flexplayer.util.HeaderInterceptor
import ru.valentine.flexplayer.util.retrofit.ResultCallAdapterFactory
import java.util.concurrent.TimeUnit

val networkModule = module {
    single { provideVkService(get()) }
    single { provideLoggingInterceptor() }
    single { provideRetrofit(get()) }
    single { provideHttpClient(get(), get()) }
    single { HeaderInterceptor() }
}


private fun provideVkService(retrofit: Retrofit): VkService {
    return retrofit.create(VkService::class.java)
}

private fun provideRetrofit(client: OkHttpClient): Retrofit {
    return Retrofit.Builder()
        .baseUrl("https://api.vk.com/method/")
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(ResultCallAdapterFactory())
        .client(client)
        .build()
}

private fun provideHttpClient(
    headerInterceptor: HeaderInterceptor,
    loggingInterceptor: HttpLoggingInterceptor
): OkHttpClient {
    return OkHttpClient.Builder()
        .addInterceptor(headerInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(100, TimeUnit.SECONDS)
        .readTimeout(100, TimeUnit.SECONDS)
        .build()
}

private fun provideLoggingInterceptor(): HttpLoggingInterceptor {
    val logger = HttpLoggingInterceptor()
    logger.level = HttpLoggingInterceptor.Level.BASIC
    return logger
}