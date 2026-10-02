package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonalOsDao {

    // === TAREAS & TIEMPO ===
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, priority DESC, id DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE cycleDate = :cycleDate ORDER BY isCompleted ASC, id DESC")
    fun getTasksByCycle(cycleDate: String): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    // === BLOQUES DE TIEMPO ===
    @Query("SELECT * FROM time_blocks WHERE cycleDate = :cycleDate ORDER BY startTime ASC")
    fun getTimeBlocksByCycle(cycleDate: String): Flow<List<TimeBlockEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeBlock(block: TimeBlockEntity): Long

    @Update
    suspend fun updateTimeBlock(block: TimeBlockEntity)

    @Delete
    suspend fun deleteTimeBlock(block: TimeBlockEntity)

    // === REGISTRO DE TIEMPO (TRACKER) ===
    @Query("SELECT * FROM time_entries WHERE cycleDate = :cycleDate ORDER BY timestamp DESC")
    fun getTimeEntriesByCycle(cycleDate: String): Flow<List<TimeEntryEntity>>

    @Query("SELECT * FROM time_entries ORDER BY timestamp DESC")
    fun getAllTimeEntries(): Flow<List<TimeEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeEntry(entry: TimeEntryEntity): Long

    // === HÁBITOS ===
    @Query("SELECT * FROM habits WHERE isActive = 1 ORDER BY id ASC")
    fun getActiveHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE goalId = :goalId AND isActive = 1 ORDER BY id ASC")
    fun getHabitsForGoal(goalId: Long): Flow<List<HabitEntity>>

    @Query("UPDATE habits SET goalId = :goalId WHERE id = :habitId")
    suspend fun linkHabitToGoal(habitId: Long, goalId: Long?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Delete
    suspend fun deleteHabit(habit: HabitEntity)

    // === REGISTRO DE HÁBITOS (LOGS) ===
    @Query("SELECT * FROM habit_logs")
    fun getAllHabitLogs(): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE cycleDate = :cycleDate")
    fun getHabitLogsByCycle(cycleDate: String): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY timestamp DESC")
    fun getLogsForHabit(habitId: Long): Flow<List<HabitLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabitLog(log: HabitLogEntity): Long

    @Delete
    suspend fun deleteHabitLog(log: HabitLogEntity)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND cycleDate = :cycleDate")
    suspend fun deleteHabitLogForCycle(habitId: Long, cycleDate: String)

    // === METAS & SUB-METAS ===
    @Query("SELECT * FROM goals ORDER BY status ASC, id DESC")
    fun getAllGoals(): Flow<List<GoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity): Long

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Delete
    suspend fun deleteGoal(goal: GoalEntity)

    @Query("SELECT * FROM sub_goals WHERE goalId = :goalId ORDER BY id ASC")
    fun getSubGoalsForGoal(goalId: Long): Flow<List<SubGoalEntity>>

    @Query("SELECT * FROM sub_goals")
    fun getAllSubGoals(): Flow<List<SubGoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubGoal(subGoal: SubGoalEntity): Long

    @Update
    suspend fun updateSubGoal(subGoal: SubGoalEntity)

    @Delete
    suspend fun deleteSubGoal(subGoal: SubGoalEntity)

    // === FINANZAS ===
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE cycleDate = :cycleDate ORDER BY timestamp DESC")
    fun getTransactionsByCycle(cycleDate: String): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM financial_goals ORDER BY id ASC")
    fun getFinancialGoals(): Flow<List<FinancialGoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFinancialGoal(goal: FinancialGoalEntity): Long

    @Update
    suspend fun updateFinancialGoal(goal: FinancialGoalEntity)

    @Delete
    suspend fun deleteFinancialGoal(goal: FinancialGoalEntity)

    @Query("SELECT * FROM assets_debts ORDER BY type ASC, id ASC")
    fun getAssetsAndDebts(): Flow<List<AssetDebtEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssetDebt(item: AssetDebtEntity): Long

    @Update
    suspend fun updateAssetDebt(item: AssetDebtEntity)

    @Delete
    suspend fun deleteAssetDebt(item: AssetDebtEntity)

    // === BIENESTAR DIGITAL (APP LIMITS) ===
    @Query("SELECT * FROM app_limits ORDER BY appName ASC")
    fun getAllAppLimits(): Flow<List<AppLimitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppLimit(limit: AppLimitEntity): Long

    @Update
    suspend fun updateAppLimit(limit: AppLimitEntity)

    @Delete
    suspend fun deleteAppLimit(limit: AppLimitEntity)

    @Query("UPDATE app_limits SET currentUsageMinutes = 0, isBlocked = 0 WHERE isStrictLocked = 0 OR cycleDate != :newCycleDate")
    suspend fun resetAppLimitsForNewCycle(newCycleDate: String)

    // === CONFIGURACIÓN GLOBAL ===
    @Query("SELECT configValue FROM system_config WHERE configKey = :key LIMIT 1")
    suspend fun getConfigValue(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setConfigValue(config: SystemConfigEntity)

    @Query("SELECT * FROM system_config")
    fun getAllConfigs(): Flow<List<SystemConfigEntity>>

    // === ENTRENAMIENTO / FITNESS ===
    @Query("SELECT * FROM workout_sessions ORDER BY timestamp DESC")
    fun getAllWorkoutSessions(): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions WHERE cycleDate = :cycleDate ORDER BY timestamp DESC")
    fun getWorkoutSessionsByCycle(cycleDate: String): Flow<List<WorkoutSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutSession(session: WorkoutSessionEntity): Long

    @Update
    suspend fun updateWorkoutSession(session: WorkoutSessionEntity)

    @Delete
    suspend fun deleteWorkoutSession(session: WorkoutSessionEntity)

    @Query("SELECT * FROM workout_exercises ORDER BY id ASC")
    fun getAllWorkoutExercises(): Flow<List<WorkoutExerciseEntity>>

    @Query("SELECT * FROM workout_exercises WHERE sessionId = :sessionId ORDER BY id ASC")
    fun getExercisesForSession(sessionId: Long): Flow<List<WorkoutExerciseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutExercise(exercise: WorkoutExerciseEntity): Long

    @Update
    suspend fun updateWorkoutExercise(exercise: WorkoutExerciseEntity)

    @Delete
    suspend fun deleteWorkoutExercise(exercise: WorkoutExerciseEntity)
}
