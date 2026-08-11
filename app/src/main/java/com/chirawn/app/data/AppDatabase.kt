package com.chirawn.app.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

enum class GameType { SUDOKU, GAME_2048, SLIDING }

@Entity(tableName = "user_profile") data class UserProfile(@PrimaryKey val id: Int = 1, val nickname: String = "Bạn")
@Entity(tableName = "game_sessions") data class GameSession(@PrimaryKey(autoGenerate = true) val id: Long = 0, val gameType: GameType, val score: Int = 0, val durationSeconds: Long = 0, val completed: Boolean, val timestamp: Long = System.currentTimeMillis())
@Entity(tableName = "game_best_stats", primaryKeys = ["gameType"]) data class GameBestStats(val gameType: GameType, val bestScore: Int = 0, val bestTimeSeconds: Long? = null)

class Converters { @TypeConverter fun gameToString(v: GameType) = v.name; @TypeConverter fun stringToGame(v: String) = GameType.valueOf(v) }
@Dao interface HubDao {
 @Query("SELECT * FROM user_profile WHERE id = 1") fun profile(): Flow<UserProfile?>
 @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun saveProfile(p: UserProfile)
 @Query("SELECT * FROM game_sessions ORDER BY timestamp DESC LIMIT 5") fun recent(): Flow<List<GameSession>>
 @Query("SELECT COUNT(*) FROM game_sessions") fun total(): Flow<Int>
 @Query("SELECT * FROM game_best_stats") fun bests(): Flow<List<GameBestStats>>
 @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun saveBest(b: GameBestStats)
 @Insert suspend fun session(s: GameSession)
}
@Database(entities = [UserProfile::class, GameSession::class, GameBestStats::class], version = 1)
@TypeConverters(Converters::class) abstract class AppDatabase: RoomDatabase() { abstract fun dao(): HubDao
 companion object { fun create(context: Context) = Room.databaseBuilder(context, AppDatabase::class.java, "chirawn.db").fallbackToDestructiveMigration().build() }
}

class HubRepository(private val dao: HubDao) {
 val profile = dao.profile(); val recent = dao.recent(); val total = dao.total(); val bests = dao.bests()
 suspend fun nickname(value: String) = dao.saveProfile(UserProfile(nickname = value.ifBlank { "Bạn" }))
 suspend fun finish(type: GameType, score: Int, seconds: Long, completed: Boolean) {
  dao.session(GameSession(gameType = type, score = score, durationSeconds = seconds, completed = completed))
  if (completed) {
   // A score is better when larger; time is better when smaller.
   val current = dao.bests().first().firstOrNull { it.gameType == type }
   val scoreBest = maxOf(current?.bestScore ?: 0, score)
   val timeBest = if (current?.bestTimeSeconds == null) seconds else minOf(current.bestTimeSeconds, seconds)
   dao.saveBest(GameBestStats(type, scoreBest, timeBest))
  }
 }
}
