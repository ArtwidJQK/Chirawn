package com.chirawn.app.game

import kotlin.random.Random

class Game2048Engine {
    fun initialize(): IntArray {
        val cells = IntArray(16)
        addTile(cells)
        addTile(cells)
        return cells
    }

    fun addTile(cells: IntArray) {
        val empty = cells.indices.filter { cells[it] == 0 }
        if (empty.isNotEmpty()) {
            cells[empty.random()] = if (Random.nextInt(10) == 0) 4 else 2
        }
    }

    fun move(cells: IntArray, direction: Int): Pair<IntArray, Int> {
        val newCells = cells.copyOf()
        var addedScore = 0
        repeat(4) { index ->
            addedScore += moveLine(newCells, direction, index)
        }
        return newCells to addedScore
    }

    private fun moveLine(cells: IntArray, d: Int, index: Int): Int {
        val p = when (d) {
            0 -> IntArray(4) { it * 4 + index } // Up
            1 -> IntArray(4) { (3 - it) * 4 + index } // Down
            2 -> IntArray(4) { index * 4 + it } // Left
            else -> IntArray(4) { index * 4 + 3 - it } // Right
        }
        val v = p.map { cells[it] }.filter { it > 0 }.toMutableList()
        var score = 0
        var i = 0
        while (i < v.size - 1) {
            if (v[i] == v[i + 1]) {
                v[i] *= 2
                score += v[i]
                v.removeAt(i + 1)
            }
            i++
        }
        while (v.size < 4) v.add(0)
        p.forEachIndexed { x, pos -> cells[pos] = v[x] }
        return score
    }

    fun canMove(cells: IntArray): Boolean {
        if (cells.any { it == 0 }) return true
        return (0 until 4).any { r ->
            (0 until 4).any { c ->
                (r < 3 && cells[r * 4 + c] == cells[(r + 1) * 4 + c]) ||
                (c < 3 && cells[r * 4 + c] == cells[r * 4 + c + 1])
            }
        }
    }
}
