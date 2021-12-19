package ru.valentine.flexplayer.util

import kotlin.math.abs

object TextUtils {

    @JvmStatic
    fun formatMillis(millis: Long, maintainZeros: Boolean = false) : String {
        val isNegative = millis < 0L
        val second = abs(millis / 1000 % 60)
        val minute = abs(millis / (1000 * 60) % 60)
        val hour = abs(millis / (1000 * 60 * 60) % 24)

        if (hour == 0L && minute == 0L && second == 0L){
            if (maintainZeros){
                return "00:00"
            }
            return "0:00"
        }

        val formattedSeconds = if (second < 10) "0%d" else "%d"
        val formattedMinutes = if (minute < 10) "0%d" else "%d"

        val result: String = if (hour < 1){
            if (maintainZeros && minute < 10){
                String.format("0%d:$formattedSeconds", minute, second)
            } else {
                String.format("%d:$formattedSeconds", minute, second)
            }
        } else {
            String.format("%d:$formattedMinutes:$formattedMinutes", hour, minute, second)
        }


        if (isNegative){
            return "-$result"
        }
        return result
    }

}