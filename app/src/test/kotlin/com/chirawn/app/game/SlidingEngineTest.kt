package com.chirawn.app.game

import org.junit.Assert.*
import org.junit.Test

class SlidingEngineTest {
    private val engine = SlidingEngine()

    @Test
    fun `initialize returns a shuffled but solvable board`() {
        val tiles = engine.initialize()
        assertEquals(9, tiles.size)
        assertTrue(tiles.contains(0))
        assertFalse(engine.isSolved(tiles))
    }

    @Test
    fun `move swaps tile with empty space if adjacent`() {
        // [1, 2, 3, 4, 5, 6, 7, 0, 8] - 0 is at index 7
        val tiles = intArrayOf(1, 2, 3, 4, 5, 6, 7, 0, 8)
        // Move tile 8 (index 8) to index 7
        val newTiles = engine.move(tiles, 8)
        assertNotNull(newTiles)
        assertEquals(0, newTiles!![8])
        assertEquals(8, newTiles[7])
    }

    @Test
    fun `move returns null if tile is not adjacent to empty space`() {
        // [1, 2, 3, 4, 5, 6, 7, 0, 8] - 0 is at index 7
        val tiles = intArrayOf(1, 2, 3, 4, 5, 6, 7, 0, 8)
        // Try to move tile 1 (index 0)
        val newTiles = engine.move(tiles, 0)
        assertNull(newTiles)
    }

    @Test
    fun `isSolved returns true for correct order`() {
        val solved = intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 0)
        assertTrue(engine.isSolved(solved))
    }
}
