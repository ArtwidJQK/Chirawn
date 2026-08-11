package com.chirawn.app.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.*
import com.chirawn.app.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

abstract class TimedGameViewModel(protected val repo: HubRepository, private val type: GameType): ViewModel() {
 var seconds by mutableLongStateOf(0L); protected set
 private var ticker: Job? = null
 fun startTimer() { if (ticker == null) ticker = viewModelScope.launch { while (true) { delay(1000); seconds++ } } }
 fun stop(completed: Boolean, score: Int = 0) { ticker?.cancel(); ticker = null; if (completed) viewModelScope.launch { repo.finish(type, score, seconds, true) } }
 override fun onCleared() { ticker?.cancel() }
}

class SudokuViewModel(repo: HubRepository): TimedGameViewModel(repo, GameType.SUDOKU) {
 private val solved = intArrayOf(5,3,4,6,7,8,9,1,2,6,7,2,1,9,5,3,4,8,1,9,8,3,4,2,5,6,7,8,5,9,7,6,1,4,2,3,4,2,6,8,5,3,7,9,1,7,1,3,9,2,4,8,5,6,9,6,1,5,3,7,2,8,4,2,8,7,4,1,9,6,3,5,3,4,5,2,8,6,1,7,9)
 var board by mutableStateOf(IntArray(81)); private set; var fixed by mutableStateOf(BooleanArray(81)); private set; var selected by mutableIntStateOf(-1); private set; var mistakes by mutableIntStateOf(0); private set; var difficulty by mutableStateOf("Easy"); private set; var finished by mutableStateOf(false); private set
 init { newGame("Easy") }
 fun newGame(level: String = difficulty) { stop(false); seconds = 0; difficulty = level; finished = false; mistakes = 0; selected = -1; val blanks = when(level) { "Hard" -> 52; "Medium" -> 43; else -> 34 }; fixed = BooleanArray(81) { true }; Random(System.currentTimeMillis()).shuffledIndices(81).take(blanks).forEach { fixed[it] = false }; board = IntArray(81) { if (fixed[it]) solved[it] else 0 }; startTimer() }
 fun select(i: Int) { if (!fixed[i] && !finished) selected = i }
 fun input(n: Int) { val i = selected; if (i !in 0..80 || finished) return; if (n != 0 && !valid(i,n)) { mistakes++; return }; board = board.copyOf().also { it[i] = n }; if (board.none { it == 0 }) { finished = true; stop(true); } }
 fun reset() { board = IntArray(81) { if (fixed[it]) solved[it] else 0 }; mistakes = 0; seconds = 0; finished = false; startTimer() }
 private fun valid(pos: Int, n: Int): Boolean { val r=pos/9; val c=pos%9; return (0..8).none { i -> (board[r*9+i] == n && i != c) || (board[i*9+c] == n && i != r) || run { val br=r/3*3; val bc=c/3*3; val p=(br+i/3)*9+bc+i%3; board[p] == n && p != pos } } }
}
private fun Random.shuffledIndices(n: Int) = (0 until n).shuffled(this)

class Game2048ViewModel(repo: HubRepository): TimedGameViewModel(repo, GameType.GAME_2048) {
 var cells by mutableStateOf(IntArray(16)); private set; var score by mutableIntStateOf(0); private set; var won by mutableStateOf(false); private set; var over by mutableStateOf(false); private set
 init { restart() }
 fun restart() { stop(false); seconds=0; score=0; won=false; over=false; cells=IntArray(16); add(); add(); startTimer() }
 fun move(direction: Int) { if(over) return; val before=cells.copyOf(); repeat(4) { line(direction,it) }; if (!cells.contentEquals(before)) { add(); won = won || cells.any { it >= 2048 }; over = !canMove(); if(over) stop(true,score) } }
 private fun line(d:Int, index:Int) { val p = when(d) { 0 -> IntArray(4){ it*4+index }; 1 -> IntArray(4){ (3-it)*4+index }; 2 -> IntArray(4){ index*4+it }; else -> IntArray(4){ index*4+3-it } }; val v=p.map{cells[it]}.filter{it>0}.toMutableList(); var i=0; while(i<v.size-1) { if(v[i]==v[i+1]) { v[i]*=2; score+=v[i]; v.removeAt(i+1) }; i++ }; while(v.size<4)v.add(0); p.forEachIndexed { x, pos -> cells[pos]=v[x] } }
 private fun add() { val empty=cells.indices.filter { cells[it]==0 }; if(empty.isNotEmpty()) cells[empty.random()] = if(Random.nextInt(10)==0)4 else 2 }
 private fun canMove() = cells.any{it==0} || (0 until 4).any { r -> (0 until 4).any { c -> (r<3 && cells[r*4+c]==cells[(r+1)*4+c]) || (c<3 && cells[r*4+c]==cells[r*4+c+1]) } }
}

class SlidingViewModel(repo: HubRepository): TimedGameViewModel(repo, GameType.SLIDING) {
 var tiles by mutableStateOf(IntArray(9) { it+1 }.also { it[8]=0 }); private set; var moves by mutableIntStateOf(0); private set; var finished by mutableStateOf(false); private set
 init { restart() }
 fun restart() { stop(false); seconds=0; moves=0; finished=false; do { tiles=IntArray(9){it+1}.also{it[8]=0}; repeat(150) { val z=tiles.indexOf(0); val choices=listOf(z-3,z+3,z-1,z+1).filter { it in 0..8 && (it/3==z/3 || it%3==z%3) }; val n=choices.random(); tiles[z]=tiles[n];tiles[n]=0 } } while(isSolved()); startTimer() }
 fun tap(index:Int) { if(finished) return; val z=tiles.indexOf(0); if(index !in listOf(z-3,z+3,z-1,z+1) || !(index/3==z/3 || index%3==z%3))return; tiles=tiles.copyOf().also{it[z]=it[index];it[index]=0};moves++; if(isSolved()){finished=true;stop(true,0)} }
 private fun isSolved() = tiles.take(8).withIndex().all { it.value == it.index+1 }
}
