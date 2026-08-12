package com.chirawn.app.game

class SlidingEngine {
    fun initialize(): IntArray {
        var tiles: IntArray
        do {
            tiles = IntArray(9) { if (it == 8) 0 else it + 1 }
            repeat(150) {
                val z = tiles.indexOf(0)
                val choices = listOf(z - 3, z + 3, z - 1, z + 1).filter { 
                    it in 0..8 && (it / 3 == z / 3 || it % 3 == z % 3) 
                }
                val n = choices.random()
                tiles[z] = tiles[n]
                tiles[n] = 0
            }
        } while (isSolved(tiles))
        return tiles
    }

    fun move(tiles: IntArray, index: Int): IntArray? {
        val z = tiles.indexOf(0)
        if (index !in listOf(z - 3, z + 3, z - 1, z + 1) || !(index / 3 == z / 3 || index % 3 == z % 3)) {
            return null
        }
        return tiles.copyOf().also {
            it[z] = it[index]
            it[index] = 0
        }
    }

    fun isSolved(tiles: IntArray): Boolean {
        return tiles.take(8).withIndex().all { it.value == it.index + 1 }
    }
}
