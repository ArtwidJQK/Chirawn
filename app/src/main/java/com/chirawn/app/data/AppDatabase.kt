package com.chirawn.app.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

enum class GameType { SUDOKU, GAME_2048, SLIDING }

@Entity(tableName = "user_profile") data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val nickname: String = "Bạn",
    val avatarRes: Int = 0, // 0 for default, or R.drawable index
    val currentStreak: Int = 0,
    val lastActiveDate: Long? = null,
    val streakBrokenDate: Long? = null,
    val previousStreak: Int = 0
)
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
@Database(entities = [UserProfile::class, GameSession::class, GameBestStats::class], version = 3)
@TypeConverters(Converters::class) abstract class AppDatabase: RoomDatabase() { abstract fun dao(): HubDao
 companion object { fun create(context: Context) = Room.databaseBuilder(context, AppDatabase::class.java, "chirawn.db").fallbackToDestructiveMigration().build() }
}

class HubRepository(private val dao: HubDao) {
    val profile = dao.profile()
    val recent = dao.recent()
    val total = dao.total()
    val bests = dao.bests()
    private val streakEngine = com.chirawn.app.game.StreakEngine()

    suspend fun nickname(value: String) = dao.saveProfile(
        dao.profile().first()?.copy(nickname = value.ifBlank { "Bạn" }) ?: UserProfile(nickname = value.ifBlank { "Bạn" })
    )

    suspend fun updateAvatar(resId: Int) {
        val p = dao.profile().first() ?: UserProfile()
        dao.saveProfile(p.copy(avatarRes = resId))
    }

    suspend fun finish(type: GameType, score: Int, seconds: Long, completed: Boolean) {
        dao.session(GameSession(gameType = type, score = score, durationSeconds = seconds, completed = completed))
        if (completed) {
            val p = dao.profile().first() ?: UserProfile()
            val streakResult = streakEngine.calculate(p.currentStreak, p.lastActiveDate)
            
            val updatedProfile = if (streakResult.shouldReset) {
                p.copy(
                    currentStreak = 0,
                    previousStreak = p.currentStreak,
                    streakBrokenDate = streakResult.brokenDate,
                    lastActiveDate = System.currentTimeMillis()
                )
            } else {
                p.copy(
                    currentStreak = streakResult.newStreak,
                    lastActiveDate = System.currentTimeMillis(),
                    streakBrokenDate = null // Clear broken date if they play
                )
            }
            dao.saveProfile(updatedProfile)

            // A score is better when larger; time is better when smaller.
            val current = dao.bests().first().firstOrNull { it.gameType == type }
            val scoreBest = maxOf(current?.bestScore ?: 0, score)
            val timeBest = if (current?.bestTimeSeconds == null) seconds else minOf(current.bestTimeSeconds, seconds)
            dao.saveBest(GameBestStats(type, scoreBest, timeBest))
        }
    }

    suspend fun restoreStreak() {
        val p = dao.profile().first() ?: return
        if (streakEngine.canRecover(p.streakBrokenDate)) {
            dao.saveProfile(p.copy(
                currentStreak = p.previousStreak,
                streakBrokenDate = null,
                previousStreak = 0
            ))
        }
    }
}
