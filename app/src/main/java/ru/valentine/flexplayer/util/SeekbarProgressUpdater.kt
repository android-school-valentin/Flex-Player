package ru.valentine.flexplayer.util

import android.os.Handler
import android.os.SystemClock
import android.widget.SeekBar
import android.widget.TextView
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit


private const val PROGRESS_UPDATE_INITIAL_DELAY = 100L

private const val PROGRESS_UPDATE_PERIOD = 1000L

//спизжно
class SeekbarProgressUpdater(
        private val seekBar: SeekBar,
        private val seekPosition: TextView,
        private val seekDuration: TextView,
        private val updateListener: (Long) -> Unit
) {
    private val executorService = Executors.newSingleThreadScheduledExecutor()
    private val handler = Handler()

    private val uiThreadUpdate = Runnable(this::updateProgress)
    private val scheduledUpdate = Runnable {
        handler.post(uiThreadUpdate)
    }

    private var lastPositionUpdate = -1L
    private var lastPositionUpdateTime = -1L
    private var shouldAutoAdvance = false
    private var updateFuture: ScheduledFuture<*>? = null

    init {
        val listener = UserSeekListener()
        seekBar.setOnSeekBarChangeListener(listener)
    }

    fun update(position: Long, duration: Long, updateTime: Long, autoAdvance: Boolean) {
        //assert(position <= duration)

        // Remember the last time position was updated for auto-update based on time.
        lastPositionUpdate = position
        lastPositionUpdateTime = updateTime

        // Update the max progression.
        seekBar.max = duration.toInt()
        seekDuration.text = TextUtils.formatMillis(duration)

        // Update the visual playback position.
        if (position != -1L) {
            seekBar.progress = position.toInt()
            seekPosition.text = TextUtils.formatMillis(position)
        } else {
            seekBar.progress = 0
            seekPosition.text = null
        }

        // Schedule to automatically update playback position if requested.
        shouldAutoAdvance = autoAdvance
        if (autoAdvance) {
            scheduleProgressUpdate()
        } else {
            stopProgressUpdate()
        }
    }

    private fun scheduleProgressUpdate() {
        updateFuture = executorService.scheduleAtFixedRate(
                scheduledUpdate,
                PROGRESS_UPDATE_INITIAL_DELAY,
                PROGRESS_UPDATE_PERIOD, TimeUnit.MILLISECONDS
        )
    }

    private fun stopProgressUpdate() {
        updateFuture?.cancel(false)
    }

    private fun updateProgress() {
        if (shouldAutoAdvance) {
            val elapsedTime = SystemClock.elapsedRealtime() - lastPositionUpdateTime
            val currentPosition = lastPositionUpdate + elapsedTime
            seekBar.progress = currentPosition.toInt()
        }
    }

    private inner class UserSeekListener : SeekBar.OnSeekBarChangeListener {

        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            seekPosition.text = TextUtils.formatMillis(progress.toLong())
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {
            stopProgressUpdate()
        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {
            updateListener(seekBar.progress.toLong())
            scheduleProgressUpdate()
        }
    }
}