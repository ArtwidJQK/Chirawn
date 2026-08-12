package com.chirawn.app.game

import java.util.Calendar
import java.util.concurrent.TimeUnit

class StreakEngine {
    data class StreakResult(
        val newStreak: Int,
        val shouldReset: Boolean,
        val brokenDate: Long? = null
    )

    fun calculate(currentStreak: Int, lastActiveDate: Long?, now: Long = System.currentTimeMillis()): StreakResult {
        if (lastActiveDate == null) {
            return StreakResult(newStreak = 1, shouldReset = false)
        }

        val lastCal = Calendar.getInstance().apply { timeInMillis = lastActiveDate }
        val nowCal = Calendar.getInstance().apply { timeInMillis = now }

        val isSameDay = lastCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
                lastCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR)

        if (isSameDay) {
            return StreakResult(newStreak = currentStreak, shouldReset = false)
        }

        // Check if it's exactly the next day
        lastCal.add(Calendar.DAY_OF_YEAR, 1)
        val isNextDay = lastCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
                lastCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR)

        return if (isNextDay) {
            StreakResult(newStreak = currentStreak + 1, shouldReset = false)
        } else {
            StreakResult(newStreak = 0, shouldReset = true, brokenDate = now)
        }
    }

    fun canRecover(brokenDate: Long?, now: Long = System.currentTimeMillis()): Boolean {
        if (brokenDate == null) return false
        val diff = now - brokenDate
        return diff <= TimeUnit.HOURS.toMillis(48)
    }
}
