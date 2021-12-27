package ru.valentine.flexplayer.injection

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.valentine.flexplayer.ui.album.detail.AlbumDetailViewModel
import ru.valentine.flexplayer.ui.main.MainViewModel
import ru.valentine.flexplayer.ui.player.mini.MiniPlayerViewModel
import ru.valentine.flexplayer.ui.player.PlayerViewModel

val uiModule = module {

    viewModel { MainViewModel(get()) }
    viewModel { AlbumDetailViewModel(get()) }
    viewModel { PlayerViewModel(get()) }
    viewModel { MiniPlayerViewModel(get()) }

}