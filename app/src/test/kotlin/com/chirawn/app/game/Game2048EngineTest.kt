package com.chirawn.app.game

import org.junit.Assert.*
import org.junit.Test

class Game2048EngineTest {
    private val engine = Game2048Engine()

    @Test
    fun `initialize returns board with two non-zero tiles`() {
        val cells = engine.initialize()
        assertEquals(16, cells.size)
        assertEquals(2, cells.count { it != 0 })
    }

    @Test
    fun `move merges identical tiles and increases score`() {
        val cells = IntArray(16)
        cells[0] = 2
        cells[1] = 2
        // Move Left (2)
        val (newCells, addedScore) = engine.move(cells, 2)
        assertEquals(4, newCells[0])
        assertEquals(0, newCells[1])
        assertEquals(4, addedScore)
    }

    @Test
    fun `move does not merge non-identical tiles`() {
        val cells = IntArray(16)
        cells[0] = 2
        cells[1] = 4
        // Move Left (2)
        val (newCells, addedScore) = engine.move(cells, 2)
        assertEquals(2, newCells[0])
        assertEquals(4, newCells[1])
        assertEquals(0, addedScore)
    }

    @Test
    fun `canMove returns false when board is full and no merges possible`() {
        val cells = intArrayOf(
            2, 4, 2, 4,
            4, 2, 4, 2,
            2, 4, 2, 4,
            4, 2, 4, 2
        )
        assertFalse(engine.canMove(cells))
    }

    @Test
    fun `canMove returns true when merges are possible even if full`() {
        val cells = intArrayOf(
            2, 2, 4, 8,
            16, 32, 64, 128,
            2, 4, 8, 16,
            32, 64, 128, 256
        )
        assertTrue(engine.canMove(cells))
    }
}
