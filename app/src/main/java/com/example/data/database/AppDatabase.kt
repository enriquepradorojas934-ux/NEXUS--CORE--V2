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

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "personal_os_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
