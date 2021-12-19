package ru.valentine.flexplayer

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import ru.valentine.flexplayer.injection.*
import timber.log.Timber

class FlexPlayerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        startKoin {
            androidLogger()
            androidContext(this@FlexPlayerApp)
            modules(
                listOf(
                    repositoryModule,
                    uiModule,
                    serviceModule,
                    repositoryModule,
                    networkModule,
                )
            )
        }
    }
}