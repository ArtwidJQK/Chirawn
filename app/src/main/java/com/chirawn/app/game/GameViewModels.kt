package com.chirawn.app.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.*
import com.chirawn.app.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

abstract class TimedGameViewModel(protected val repo: HubRepository, private val type: GameType): ViewModel() {
 var seconds by mutableLongStateOf(0L); protected set
 private var ticker: Job? = null
 fun startTimer() { if (ticker == null) ticker = viewModelScope.launch { while (true) { delay(1000); seconds++ } } }
 fun stop(completed: Boolean, score: Int = 0) { ticker?.cancel(); ticker = null; if (completed) viewModelScope.launch { repo.finish(type, score, seconds, true) } }
 override fun onCleared() { ticker?.cancel() }
}

class SudokuViewModel(repo: HubRepository): TimedGameViewModel(repo, GameType.SUDOKU) {
    private val engine = SudokuEngine()
    var board by mutableStateOf(IntArray(81)); private set
    var fixed by mutableStateOf(BooleanArray(81)); private set
    var selected by mutableIntStateOf(-1); private set
    var mistakes by mutableIntStateOf(0); private set
    var difficulty by mutableStateOf("Easy"); private set
    var finished by mutableStateOf(false); private set

    init {
        newGame("Easy")
    }

    fun newGame(level: String = difficulty) {
        stop(false)
        seconds = 0
        difficulty = level
        finished = false
        mistakes = 0
        selected = -1
        val (newBoard, newFixed) = engine.createBoard(level)
        board = newBoard
        fixed = newFixed
        startTimer()
    }

    fun select(i: Int) {
        if (!fixed[i] && !finished) selected = i
    }

    fun input(n: Int) {
        val i = selected
        if (i !in 0..80 || finished) return
        if (n != 0 && !engine.isValid(board, i, n)) {
            mistakes++
            return
        }
        board = board.copyOf().also { it[i] = n }
        if (engine.isComplete(board)) {
            finished = true
            stop(true)
        }
    }

    fun reset() {
        newGame(difficulty)
    }
}

class Game2048ViewModel(repo: HubRepository): TimedGameViewModel(repo, GameType.GAME_2048) {
    private val engine = Game2048Engine()
    var cells by mutableStateOf(IntArray(16)); private set
    var score by mutableIntStateOf(0); private set
    var won by mutableStateOf(false); private set
    var over by mutableStateOf(false); private set

    init {
        restart()
    }

    fun restart() {
        stop(false)
        seconds = 0
        score = 0
        won = false
        over = false
        cells = engine.initialize()
        startTimer()
    }

    fun move(direction: Int) {
        if (over) return
        val before = cells.copyOf()
        val (newCells, addedScore) = engine.move(cells, direction)
        if (!newCells.contentEquals(before)) {
            cells = newCells
            score += addedScore
            engine.addTile(cells)
            won = won || cells.any { it >= 2048 }
            over = !engine.canMove(cells)
            if (over) stop(true, score)
        }
    }
}

class SlidingViewModel(repo: HubRepository): TimedGameViewModel(repo, GameType.SLIDING) {
    private val engine = SlidingEngine()
    var tiles by mutableStateOf(IntArray(9) { it + 1 }.also { it[8] = 0 }); private set
    var moves by mutableIntStateOf(0); private set
    var finished by mutableStateOf(false); private set

    init {
        restart()
    }

    fun restart() {
        stop(false)
        seconds = 0
        moves = 0
        finished = false
        tiles = engine.initialize()
        startTimer()
    }

    fun tap(index: Int) {
        if (finished) return
        val newTiles = engine.move(tiles, index)
        if (newTiles != null) {
            tiles = newTiles
            moves++
            if (engine.isSolved(tiles)) {
                finished = true
                stop(true, 0)
            }
        }
    }
}
