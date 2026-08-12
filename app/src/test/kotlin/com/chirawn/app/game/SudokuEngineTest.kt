package com.chirawn.app.game

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SudokuEngineTest {
    private val engine = SudokuEngine()

    @Test
    fun `isValid returns false when value exists in the same row`() {
        val board = IntArray(81)
        board[0] = 5
        // Attempt to place 5 at index 1 (same row as index 0)
        assertFalse(engine.isValid(board, 1, 5))
    }

    @Test
    fun `isValid returns false when value exists in the same column`() {
        val board = IntArray(81)
        board[0] = 5
        // Attempt to place 5 at index 9 (same column as index 0)
        assertFalse(engine.isValid(board, 9, 5))
    }

    @Test
    fun `isValid returns false when value exists in the same 3x3 box`() {
        val board = IntArray(81)
        board[0] = 5
        // Attempt to place 5 at index 10 (same 3x3 box as index 0)
        assertFalse(engine.isValid(board, 10, 5))
    }

    @Test
    fun `isValid returns true when value is unique in row, column, and box`() {
        val board = IntArray(81)
        board[0] = 5
        // Attempt to place 6 at index 1
        assertTrue(engine.isValid(board, 1, 6))
    }

    @Test
    fun `isComplete returns false when board has zeros`() {
        val board = IntArray(81) { 1 }
        board[80] = 0
        assertFalse(engine.isComplete(board))
    }

    @Test
    fun `isComplete returns true when board is full and valid`() {
        val solved = intArrayOf(
            5,3,4,6,7,8,9,1,2,
            6,7,2,1,9,5,3,4,8,
            1,9,8,3,4,2,5,6,7,
            8,5,9,7,6,1,4,2,3,
            4,2,6,8,5,3,7,9,1,
            7,1,3,9,2,4,8,5,6,
            9,6,1,5,3,7,2,8,4,
            2,8,7,4,1,9,6,3,5,
            3,4,5,2,8,6,1,7,9
        )
        assertTrue(engine.isComplete(solved))
    }

    @Test
    fun `createBoard generates correct number of blanks for Easy difficulty`() {
        val (board, fixed) = engine.createBoard("Easy")
        val blanks = board.count { it == 0 }
        // Easy: 34 blanks
        org.junit.Assert.assertEquals(34, blanks)
    }

    @Test
    fun `createBoard generates correct number of blanks for Hard difficulty`() {
        val (board, fixed) = engine.createBoard("Hard")
        val blanks = board.count { it == 0 }
        // Hard: 52 blanks
        org.junit.Assert.assertEquals(52, blanks)
    }
}
