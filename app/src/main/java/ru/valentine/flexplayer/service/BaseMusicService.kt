package ru.valentine.flexplayer.service

import android.content.Intent
import androidx.media.MediaBrowserServiceCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

abstract class BaseMusicService : MediaBrowserServiceCompat(), CoroutineScope {

    private var isStarted = false

    private lateinit var scopeJob: Job
    override val coroutineContext: CoroutineContext
        get() = scopeJob + Dispatchers.Main.immediate

    override fun onCreate() {
        super.onCreate()
        scopeJob = SupervisorJob()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isStarted = true
        return super.onStartCommand(intent, flags, startId)
    }

    protected fun startSelf() {
        if (!isStarted) {
            startService(Intent(this, this::class.java))
        }
    }

    protected fun stop() {
        if (isStarted) {
            isStarted = false
            stopSelf()
        }
    }

    override fun onDestroy() {
        scopeJob.cancel()
        super.onDestroy()
    }

}