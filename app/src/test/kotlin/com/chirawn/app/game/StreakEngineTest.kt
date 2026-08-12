package com.chirawn.app.game

import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class StreakEngineTest {
    private val engine = StreakEngine()

    private fun dateAt(daysOffset: Int): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, daysOffset)
        return cal.timeInMillis
    }

    @Test
    fun `same day session does not increase streak`() {
        val lastActive = dateAt(0)
        val result = engine.calculate(currentStreak = 5, lastActiveDate = lastActive)
        assertEquals(5, result.newStreak)
        assertFalse(result.shouldReset)
    }

    @Test
    fun `next day session increases streak`() {
        val lastActive = dateAt(-1)
        val result = engine.calculate(currentStreak = 5, lastActiveDate = lastActive)
        assertEquals(6, result.newStreak)
        assertFalse(result.shouldReset)
    }

    @Test
    fun `missing one day resets streak and marks as broken`() {
        val lastActive = dateAt(-2)
        val result = engine.calculate(currentStreak = 5, lastActiveDate = lastActive)
        assertEquals(0, result.newStreak)
        assertTrue(result.shouldReset)
        assertNotNull(result.brokenDate)
    }

    @Test
    fun `recovery window is open within 48 hours`() {
        val brokenDate = dateAt(-1) // 24 hours ago
        assertTrue(engine.canRecover(brokenDate))
    }

    @Test
    fun `recovery window is closed after 48 hours`() {
        val brokenDate = dateAt(-3) // 72 hours ago
        assertFalse(engine.canRecover(brokenDate))
    }
}
