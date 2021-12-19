package ru.valentine.flexplayer.injection

import org.koin.dsl.module
import ru.valentine.flexplayer.data.repository.AlbumRepository
import ru.valentine.flexplayer.data.repository.TrackRepository

val repositoryModule = module {

    factory { TrackRepository(get()) }
    factory { AlbumRepository(get()) }

}