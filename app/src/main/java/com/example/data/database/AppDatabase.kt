package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.PersonalOsDao
import com.example.data.model.*

@Database(
    entities = [
        TaskEntity::class,
        TimeBlockEntity::class,
        TimeEntryEntity::class,
        HabitEntity::class,
        HabitLogEntity::class,
        GoalEntity::class,
        SubGoalEntity::class,
        TransactionEntity::class,
        FinancialGoalEntity::class,
        AssetDebtEntity::class,
        AppLimitEntity::class,
        SystemConfigEntity::class,
        WorkoutSessionEntity::class,
        WorkoutExerciseEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun personalOsDao(): PersonalOsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `workout_sessions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `targetGoal` TEXT NOT NULL, `durationMinutes` INTEGER NOT NULL, `notes` TEXT NOT NULL, `cycleDate` TEXT NOT NULL, `isCompleted` INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `workout_exercises` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `sessionId` INTEGER NOT NULL, `exerciseName` TEXT NOT NULL, `sets` INTEGER NOT NULL, `reps` TEXT NOT NULL, `weightKg` REAL NOT NULL, `restSeconds` INTEGER NOT NULL, `isCompleted` INTEGER NOT NULL)")
            }
        }

        val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `system_config` (`key` TEXT PRIMARY KEY NOT NULL, `value` TEXT NOT NULL)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "personal_os_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
