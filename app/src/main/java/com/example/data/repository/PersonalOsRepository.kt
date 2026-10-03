package com.example.data.repository

import com.example.data.dao.PersonalOsDao
import com.example.data.model.*
import com.example.service.PersonalAiProductivityService
import com.example.service.UserProductivitySnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Interfaz del Repositorio principal del Sistema Operativo Personal.
 * Actúa como puente unificado entre la persistencia local (Room Database)
 * y los servicios inteligentes remotos (Firebase AI / Gemini Productivity Service).
 */
interface PersonalOsRepository {

    // === TAREAS & TIEMPO (ROOM) ===
    val allTasks: Flow<List<TaskEntity>>
    fun getTasksByCycle(cycleDate: String): Flow<List<TaskEntity>>
    suspend fun insertTask(task: TaskEntity): Long
    suspend fun updateTask(task: TaskEntity)
    suspend fun deleteTask(task: TaskEntity)

    // === BLOQUES DE TIEMPO (ROOM) ===
    val allTimeBlocks: Flow<List<TimeBlockEntity>>
    fun getTimeBlocksByCycle(cycleDate: String): Flow<List<TimeBlockEntity>>
    suspend fun insertTimeBlock(block: TimeBlockEntity): Long
    suspend fun updateTimeBlock(block: TimeBlockEntity)
    suspend fun deleteTimeBlock(block: TimeBlockEntity)

    // === TIME ENTRIES (TRACKER - ROOM) ===
    fun getTimeEntriesByCycle(cycleDate: String): Flow<List<TimeEntryEntity>>
    val allTimeEntries: Flow<List<TimeEntryEntity>>
    suspend fun insertTimeEntry(entry: TimeEntryEntity): Long

    // === HÁBITOS (ROOM) ===
    val activeHabits: Flow<List<HabitEntity>>
    fun getHabitsForGoal(goalId: Long): Flow<List<HabitEntity>>
    suspend fun insertHabit(habit: HabitEntity): Long
    suspend fun updateHabit(habit: HabitEntity)
    suspend fun deleteHabit(habit: HabitEntity)
    suspend fun linkHabitToGoal(habitId: Long, goalId: Long?)

    // === LOGS DE HÁBITOS (ROOM) ===
    val allHabitLogs: Flow<List<HabitLogEntity>>
    fun getHabitLogsByCycle(cycleDate: String): Flow<List<HabitLogEntity>>
    fun getLogsForHabit(habitId: Long): Flow<List<HabitLogEntity>>
    suspend fun insertHabitLog(log: HabitLogEntity): Long
    suspend fun deleteHabitLogForCycle(habitId: Long, cycleDate: String)

    // === METAS & SUB-METAS (ROOM) ===
    val allGoals: Flow<List<GoalEntity>>
    val allSubGoals: Flow<List<SubGoalEntity>>
    suspend fun insertGoal(goal: GoalEntity): Long
    suspend fun updateGoal(goal: GoalEntity)
    suspend fun deleteGoal(goal: GoalEntity)
    suspend fun insertSubGoal(subGoal: SubGoalEntity): Long
    suspend fun updateSubGoal(subGoal: SubGoalEntity)
    suspend fun deleteSubGoal(subGoal: SubGoalEntity)

    // === FINANZAS (ROOM) ===
    val allTransactions: Flow<List<TransactionEntity>>
    fun getTransactionsByCycle(cycleDate: String): Flow<List<TransactionEntity>>
    suspend fun insertTransaction(transaction: TransactionEntity): Long
    suspend fun deleteTransaction(transaction: TransactionEntity)

    val financialGoals: Flow<List<FinancialGoalEntity>>
    suspend fun insertFinancialGoal(goal: FinancialGoalEntity): Long
    suspend fun updateFinancialGoal(goal: FinancialGoalEntity)
    suspend fun deleteFinancialGoal(goal: FinancialGoalEntity)

    val assetsAndDebts: Flow<List<AssetDebtEntity>>
    suspend fun insertAssetDebt(item: AssetDebtEntity): Long
    suspend fun updateAssetDebt(item: AssetDebtEntity)
    suspend fun deleteAssetDebt(item: AssetDebtEntity)

    // === BIENESTAR DIGITAL (ROOM) ===
    val allAppLimits: Flow<List<AppLimitEntity>>
    suspend fun insertAppLimit(limit: AppLimitEntity): Long
    suspend fun updateAppLimit(limit: AppLimitEntity)
    suspend fun deleteAppLimit(limit: AppLimitEntity)
    suspend fun resetAppLimitsForNewCycle(newCycleDate: String)

    // === CONFIGURACIONES (ROOM) ===
    val allConfigs: Flow<List<SystemConfigEntity>>
    suspend fun getConfig(key: String): String?
    suspend fun setConfig(key: String, value: String)

    // === ENTRENAMIENTO / FITNESS (ROOM) ===
    val allWorkoutSessions: Flow<List<WorkoutSessionEntity>>
    fun getWorkoutSessionsByCycle(cycleDate: String): Flow<List<WorkoutSessionEntity>>
    suspend fun insertWorkoutSession(session: WorkoutSessionEntity): Long
    suspend fun updateWorkoutSession(session: WorkoutSessionEntity)
    suspend fun deleteWorkoutSession(session: WorkoutSessionEntity)
    val allWorkoutExercises: Flow<List<WorkoutExerciseEntity>>
    fun getExercisesForSession(sessionId: Long): Flow<List<WorkoutExerciseEntity>>
    suspend fun insertWorkoutExercise(exercise: WorkoutExerciseEntity): Long
    suspend fun updateWorkoutExercise(exercise: WorkoutExerciseEntity)
    suspend fun deleteWorkoutExercise(exercise: WorkoutExerciseEntity)

    // === PUENTE CON SERVICIO DE PRODUCTIVIDAD GEMINI (FIREBASE AI) ===
    suspend fun generateProductivityAdvice(snapshot: UserProductivitySnapshot): Result<String>
    suspend fun askCustomProductivityQuestion(question: String, snapshot: UserProductivitySnapshot): Result<String>
    suspend fun sendChatMessage(
        history: List<ChatMessage>,
        userMessage: String,
        persona: ChatbotPersona,
        modelOption: GeminiModelOption,
        snapshot: UserProductivitySnapshot? = null
    ): Result<String>
    suspend fun buildCurrentCycleSnapshot(cycleDate: String): UserProductivitySnapshot
    suspend fun generateProductivityAdviceForCycle(cycleDate: String): Result<String>

    // === AUTENTICACIÓN FIREBASE (GOOGLE SIGN-IN & ANÓNIMO) ===
    val currentUserFlow: Flow<AuthUser?>
    fun getCurrentUser(): AuthUser?
    fun getCurrentUserId(): String
    suspend fun signInWithGoogle(context: android.content.Context, serverClientId: String? = null): Result<AuthUser>
    suspend fun signInAnonymously(): Result<AuthUser>
    suspend fun signOut(context: android.content.Context? = null): Result<Unit>

    // === SINCRONIZACIÓN EN LA NUBE (CLOUD FIRESTORE) ===
    val cloudSyncState: kotlinx.coroutines.flow.StateFlow<CloudSyncState>
    val cloudSyncRepo: PersonalOsCloudSyncRepository
    suspend fun syncAllWithCloud(userId: String = PersonalOsCloudSyncRepository.DEFAULT_USER_ID): Result<SyncResult>
    suspend fun uploadAllToCloud(userId: String = PersonalOsCloudSyncRepository.DEFAULT_USER_ID): Result<SyncResult>
    suspend fun downloadAllFromCloud(userId: String = PersonalOsCloudSyncRepository.DEFAULT_USER_ID): Result<SyncResult>
}

/**
 * Implementación concreta del repositorio que combina Room Database (vía DAO)
 * con el servicio de IA de productividad de Gemini (Firebase AI)
 * y el repositorio de sincronización con Cloud Firestore.
 */
class PersonalOsRepositoryImpl(
    private val dao: PersonalOsDao,
    private val aiService: PersonalAiProductivityService = PersonalAiProductivityService(),
    override val cloudSyncRepo: PersonalOsCloudSyncRepository = FirestoreCloudSyncRepository(dao),
    private val authService: com.example.service.FirebaseAuthService = com.example.service.FirebaseAuthService()
) : PersonalOsRepository {

    // === SINCRONIZACIÓN EN LA NUBE (FIRESTORE) ===
    override val cloudSyncState: kotlinx.coroutines.flow.StateFlow<CloudSyncState> = cloudSyncRepo.syncState
    override suspend fun syncAllWithCloud(userId: String): Result<SyncResult> = cloudSyncRepo.synchronize(userId)
    override suspend fun uploadAllToCloud(userId: String): Result<SyncResult> = cloudSyncRepo.uploadAllToCloud(userId)
    override suspend fun downloadAllFromCloud(userId: String): Result<SyncResult> = cloudSyncRepo.downloadAllFromCloud(userId)

    // === TAREAS & TIEMPO ===
    override val allTasks: Flow<List<TaskEntity>> = dao.getAllTasks()
    override fun getTasksByCycle(cycleDate: String): Flow<List<TaskEntity>> = dao.getTasksByCycle(cycleDate)
    override suspend fun insertTask(task: TaskEntity): Long = dao.insertTask(task)
    override suspend fun updateTask(task: TaskEntity) = dao.updateTask(task)
    override suspend fun deleteTask(task: TaskEntity) = dao.deleteTask(task)

    // === BLOQUES DE TIEMPO ===
    override val allTimeBlocks: Flow<List<TimeBlockEntity>> = dao.getAllTimeBlocks()
    override fun getTimeBlocksByCycle(cycleDate: String): Flow<List<TimeBlockEntity>> = dao.getTimeBlocksByCycle(cycleDate)
    override suspend fun insertTimeBlock(block: TimeBlockEntity): Long = dao.insertTimeBlock(block)
    override suspend fun updateTimeBlock(block: TimeBlockEntity) = dao.updateTimeBlock(block)
    override suspend fun deleteTimeBlock(block: TimeBlockEntity) = dao.deleteTimeBlock(block)

    // === TIME ENTRIES (TRACKER) ===
    override fun getTimeEntriesByCycle(cycleDate: String): Flow<List<TimeEntryEntity>> = dao.getTimeEntriesByCycle(cycleDate)
    override val allTimeEntries: Flow<List<TimeEntryEntity>> = dao.getAllTimeEntries()
    override suspend fun insertTimeEntry(entry: TimeEntryEntity): Long = dao.insertTimeEntry(entry)

    // === HÁBITOS ===
    override val activeHabits: Flow<List<HabitEntity>> = dao.getActiveHabits()
    override fun getHabitsForGoal(goalId: Long): Flow<List<HabitEntity>> = dao.getHabitsForGoal(goalId)
    override suspend fun insertHabit(habit: HabitEntity): Long = dao.insertHabit(habit)
    override suspend fun updateHabit(habit: HabitEntity) = dao.updateHabit(habit)
    override suspend fun deleteHabit(habit: HabitEntity) = dao.deleteHabit(habit)
    override suspend fun linkHabitToGoal(habitId: Long, goalId: Long?) = dao.linkHabitToGoal(habitId, goalId)

    // === LOGS DE HÁBITOS ===
    override val allHabitLogs: Flow<List<HabitLogEntity>> = dao.getAllHabitLogs()
    override fun getHabitLogsByCycle(cycleDate: String): Flow<List<HabitLogEntity>> = dao.getHabitLogsByCycle(cycleDate)
    override fun getLogsForHabit(habitId: Long): Flow<List<HabitLogEntity>> = dao.getLogsForHabit(habitId)
    override suspend fun insertHabitLog(log: HabitLogEntity): Long = dao.insertHabitLog(log)
    override suspend fun deleteHabitLogForCycle(habitId: Long, cycleDate: String) = dao.deleteHabitLogForCycle(habitId, cycleDate)

    // === METAS & SUB-METAS ===
    override val allGoals: Flow<List<GoalEntity>> = dao.getAllGoals()
    override val allSubGoals: Flow<List<SubGoalEntity>> = dao.getAllSubGoals()
    override suspend fun insertGoal(goal: GoalEntity): Long = dao.insertGoal(goal)
    override suspend fun updateGoal(goal: GoalEntity) = dao.updateGoal(goal)
    override suspend fun deleteGoal(goal: GoalEntity) = dao.deleteGoal(goal)
    override suspend fun insertSubGoal(subGoal: SubGoalEntity): Long = dao.insertSubGoal(subGoal)
    override suspend fun updateSubGoal(subGoal: SubGoalEntity) = dao.updateSubGoal(subGoal)
    override suspend fun deleteSubGoal(subGoal: SubGoalEntity) = dao.deleteSubGoal(subGoal)

    // === FINANZAS ===
    override val allTransactions: Flow<List<TransactionEntity>> = dao.getAllTransactions()
    override fun getTransactionsByCycle(cycleDate: String): Flow<List<TransactionEntity>> = dao.getTransactionsByCycle(cycleDate)
    override suspend fun insertTransaction(transaction: TransactionEntity): Long = dao.insertTransaction(transaction)
    override suspend fun deleteTransaction(transaction: TransactionEntity) = dao.deleteTransaction(transaction)

    override val financialGoals: Flow<List<FinancialGoalEntity>> = dao.getFinancialGoals()
    override suspend fun insertFinancialGoal(goal: FinancialGoalEntity): Long = dao.insertFinancialGoal(goal)
    override suspend fun updateFinancialGoal(goal: FinancialGoalEntity) = dao.updateFinancialGoal(goal)
    override suspend fun deleteFinancialGoal(goal: FinancialGoalEntity) = dao.deleteFinancialGoal(goal)

    override val assetsAndDebts: Flow<List<AssetDebtEntity>> = dao.getAssetsAndDebts()
    override suspend fun insertAssetDebt(item: AssetDebtEntity): Long = dao.insertAssetDebt(item)
    override suspend fun updateAssetDebt(item: AssetDebtEntity) = dao.updateAssetDebt(item)
    override suspend fun deleteAssetDebt(item: AssetDebtEntity) = dao.deleteAssetDebt(item)

    // === BIENESTAR DIGITAL ===
    override val allAppLimits: Flow<List<AppLimitEntity>> = dao.getAllAppLimits()
    override suspend fun insertAppLimit(limit: AppLimitEntity): Long = dao.insertAppLimit(limit)
    override suspend fun updateAppLimit(limit: AppLimitEntity) = dao.updateAppLimit(limit)
    override suspend fun deleteAppLimit(limit: AppLimitEntity) = dao.deleteAppLimit(limit)
    override suspend fun resetAppLimitsForNewCycle(newCycleDate: String) = dao.resetAppLimitsForNewCycle(newCycleDate)

    // === CONFIGURACIONES ===
    override val allConfigs: Flow<List<SystemConfigEntity>> = dao.getAllConfigs()
    override suspend fun getConfig(key: String): String? = dao.getConfigValue(key)
    override suspend fun setConfig(key: String, value: String) = dao.setConfigValue(SystemConfigEntity(key, value))

    // === ENTRENAMIENTO / FITNESS ===
    override val allWorkoutSessions: Flow<List<WorkoutSessionEntity>> = dao.getAllWorkoutSessions()
    override fun getWorkoutSessionsByCycle(cycleDate: String): Flow<List<WorkoutSessionEntity>> = dao.getWorkoutSessionsByCycle(cycleDate)
    override suspend fun insertWorkoutSession(session: WorkoutSessionEntity): Long = dao.insertWorkoutSession(session)
    override suspend fun updateWorkoutSession(session: WorkoutSessionEntity) = dao.updateWorkoutSession(session)
    override suspend fun deleteWorkoutSession(session: WorkoutSessionEntity) = dao.deleteWorkoutSession(session)
    override val allWorkoutExercises: Flow<List<WorkoutExerciseEntity>> = dao.getAllWorkoutExercises()
    override fun getExercisesForSession(sessionId: Long): Flow<List<WorkoutExerciseEntity>> = dao.getExercisesForSession(sessionId)
    override suspend fun insertWorkoutExercise(exercise: WorkoutExerciseEntity): Long = dao.insertWorkoutExercise(exercise)
    override suspend fun updateWorkoutExercise(exercise: WorkoutExerciseEntity) = dao.updateWorkoutExercise(exercise)
    override suspend fun deleteWorkoutExercise(exercise: WorkoutExerciseEntity) = dao.deleteWorkoutExercise(exercise)

    // === PUENTE CON SERVICIO DE PRODUCTIVIDAD GEMINI (FIREBASE AI) ===
    override suspend fun generateProductivityAdvice(snapshot: UserProductivitySnapshot): Result<String> {
        val lang = getConfig("app_language") ?: "Español"
        val curr = getConfig("app_currency_symbol") ?: "$"
        return aiService.generateProductivityAdvice(snapshot, lang, curr)
    }

    override suspend fun askCustomProductivityQuestion(
        question: String,
        snapshot: UserProductivitySnapshot
    ): Result<String> {
        val lang = getConfig("app_language") ?: "Español"
        return aiService.askCustomProductivityQuestion(question, snapshot, lang)
    }

    override suspend fun sendChatMessage(
        history: List<ChatMessage>,
        userMessage: String,
        persona: ChatbotPersona,
        modelOption: GeminiModelOption,
        snapshot: UserProductivitySnapshot?
    ): Result<String> {
        val lang = getConfig("app_language") ?: "Español"
        return aiService.sendChatMessage(history, userMessage, persona, modelOption, snapshot, lang)
    }

    /**
     * Construye un snapshot completo con los datos almacenados en Room para un ciclo específico.
     * Esto permite generar diagnósticos inteligentes de IA directamente a partir del estado de la base de datos.
     */
    override suspend fun buildCurrentCycleSnapshot(cycleDate: String): UserProductivitySnapshot {
        val tasks = dao.getAllTasks().first()
        val pendingTasks = tasks.filter { !it.isCompleted }.map { it.title }
        val completedTasks = tasks.filter { it.isCompleted }.map { it.title }

        val timeEntries = dao.getTimeEntriesByCycle(cycleDate).first()
        val productiveMinutes = timeEntries.sumOf { it.durationMinutes }

        val habits = dao.getActiveHabits().first()
        val habitLogs = dao.getHabitLogsByCycle(cycleDate).first()
        val habitsCompleted = habitLogs.count { it.isCompleted }

        val appLimits = dao.getAllAppLimits().first()
        val appUsageMinutes = appLimits.sumOf { it.currentUsageMinutes }
        val appBudgetMinutes = appLimits.sumOf { it.dailyBudgetMinutes }
        val blockedApps = appLimits.filter { it.isBlocked }.map { it.appName }

        val goals = dao.getAllGoals().first()
        val subGoals = dao.getAllSubGoals().first()
        val activeGoals = goals.map { g ->
            val subs = subGoals.filter { it.goalId == g.id }
            val progress = if (subs.isNotEmpty()) {
                (subs.count { it.isCompleted }.toFloat() / subs.size.toFloat() * 100).toInt()
            } else 0
            "${g.title} ($progress%)"
        }

        val transactions = dao.getAllTransactions().first()
        val totalInc = transactions.filter { it.type == "INGRESO" }.sumOf { it.amount }
        val totalExp = transactions.filter { it.type == "GASTO" }.sumOf { it.amount }

        return UserProductivitySnapshot(
            cycleDate = cycleDate,
            productiveMinutesToday = productiveMinutes,
            pendingTasks = pendingTasks,
            completedTasks = completedTasks,
            habitsCompleted = habitsCompleted,
            habitsTotal = habits.size,
            habitNames = habits.map { it.title },
            appUsageMinutes = appUsageMinutes,
            appBudgetMinutes = appBudgetMinutes,
            blockedApps = blockedApps,
            activeGoals = activeGoals,
            netFinancialBalance = totalInc - totalExp
        )
    }

    /**
     * Extrae el snapshot directamente desde Room y consulta el consejo de productividad de Gemini.
     */
    override suspend fun generateProductivityAdviceForCycle(cycleDate: String): Result<String> {
        val snapshot = buildCurrentCycleSnapshot(cycleDate)
        return generateProductivityAdvice(snapshot)
    }

    // === AUTENTICACIÓN FIREBASE ===
    override val currentUserFlow: Flow<AuthUser?> = authService.currentUserFlow
    override fun getCurrentUser(): AuthUser? = authService.getCurrentUser()
    override fun getCurrentUserId(): String = authService.getCurrentUserId()
    override suspend fun signInWithGoogle(context: android.content.Context, serverClientId: String?): Result<AuthUser> =
        authService.signInWithGoogle(context, serverClientId)
    override suspend fun signInAnonymously(): Result<AuthUser> =
        authService.signInAnonymously()
    override suspend fun signOut(context: android.content.Context?): Result<Unit> =
        authService.signOut(context)
}

/**
 * Función fábrica para instanciar el repositorio con compatibilidad hacia atrás y fácil inyección.
 */
fun PersonalOsRepository(
    dao: PersonalOsDao,
    aiService: PersonalAiProductivityService = PersonalAiProductivityService(),
    cloudSyncRepo: PersonalOsCloudSyncRepository = FirestoreCloudSyncRepository(dao),
    authService: com.example.service.FirebaseAuthService = com.example.service.FirebaseAuthService()
): PersonalOsRepository = PersonalOsRepositoryImpl(dao, aiService, cloudSyncRepo, authService)
