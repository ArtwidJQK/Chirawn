package com.chirawn.app.game

class SudokuEngine {
    fun isValid(board: IntArray, index: Int, value: Int): Boolean {
        if (value == 0) return true
        val r = index / 9
        val c = index % 9
        
        return (0..8).none { i ->
            // Check row
            (board[r * 9 + i] == value && (r * 9 + i) != index) ||
            // Check column
            (board[i * 9 + c] == value && (i * 9 + c) != index) ||
            // Check box
            run {
                val br = r / 3 * 3
                val bc = c / 3 * 3
                val p = (br + i / 3) * 9 + (bc + i % 3)
                board[p] == value && p != index
            }
        }
    }

    fun isComplete(board: IntArray): Boolean {
        return board.none { it == 0 } && board.indices.all { i ->
            isValid(board, i, board[i])
        }
    }

    private val solved = intArrayOf(
        5, 3, 4, 6, 7, 8, 9, 1, 2,
        6, 7, 2, 1, 9, 5, 3, 4, 8,
        1, 9, 8, 3, 4, 2, 5, 6, 7,
        8, 5, 9, 7, 6, 1, 4, 2, 3,
        4, 2, 6, 8, 5, 3, 7, 9, 1,
        7, 1, 3, 9, 2, 4, 8, 5, 6,
        9, 6, 1, 5, 3, 7, 2, 8, 4,
        2, 8, 7, 4, 1, 9, 6, 3, 5,
        3, 4, 5, 2, 8, 6, 1, 7, 9
    )

    fun createBoard(difficulty: String): Pair<IntArray, BooleanArray> {
        val blanks = when (difficulty) {
            "Hard" -> 52
            "Medium" -> 43
            else -> 34
        }
        val fixed = BooleanArray(81) { true }
        val indices = (0 until 81).shuffled()
        indices.take(blanks).forEach { fixed[it] = false }
        
        val board = IntArray(81) { if (fixed[it]) solved[it] else 0 }
        return board to fixed
    }
}
