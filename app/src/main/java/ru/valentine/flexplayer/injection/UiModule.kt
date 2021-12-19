package ru.valentine.flexplayer.injection

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.valentine.flexplayer.ui.album.detail.AlbumDetailViewModel
import ru.valentine.flexplayer.ui.main.MainViewModel

val uiModule = module {

    viewModel { MainViewModel(get()) }
    viewModel { AlbumDetailViewModel(get()) }

}