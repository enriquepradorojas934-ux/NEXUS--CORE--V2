package com.example.data.repository

import android.util.Log
import com.example.data.dao.PersonalOsDao
import com.example.data.model.*
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Estado reactivo de la sincronización en la nube con Firestore.
 */
data class CloudSyncState(
    val isSyncing: Boolean = false,
    val lastSyncTimestamp: Long? = null,
    val lastSyncSuccess: Boolean = true,
    val statusMessage: String = "Sincronización en espera",
    val lastSyncedCount: Int = 0,
    val error: String? = null
)

/**
 * Resultado detallado de una operación de sincronización.
 */
data class SyncResult(
    val isSuccess: Boolean,
    val syncedItemsCount: Int = 0,
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Interfaz del Repositorio de Sincronización en la Nube (Cloud Sync) con Cloud Firestore.
 * Permite respaldar, restaurar y sincronizar bidireccionalmente los módulos de
 * tareas, tiempo, hábitos, metas, finanzas y bienestar digital.
 */
interface PersonalOsCloudSyncRepository {

    val syncState: StateFlow<CloudSyncState>

    /** Sube todos los datos locales de Room hacia Firestore */
    suspend fun uploadAllToCloud(userId: String = DEFAULT_USER_ID): Result<SyncResult>

    /** Descarga todos los datos remotos de Firestore hacia la base local Room */
    suspend fun downloadAllFromCloud(userId: String = DEFAULT_USER_ID): Result<SyncResult>

    /** Sincronización bidireccional completa (descarga remota y respaldo local) */
    suspend fun synchronize(userId: String = DEFAULT_USER_ID): Result<SyncResult>

    // Sincronizaciones parciales por módulo
    suspend fun syncTasksToCloud(userId: String = DEFAULT_USER_ID): Result<Int>
    suspend fun syncHabitsToCloud(userId: String = DEFAULT_USER_ID): Result<Int>
    suspend fun syncGoalsToCloud(userId: String = DEFAULT_USER_ID): Result<Int>
    suspend fun syncFinancesToCloud(userId: String = DEFAULT_USER_ID): Result<Int>
    suspend fun syncTimeTrackingToCloud(userId: String = DEFAULT_USER_ID): Result<Int>
    suspend fun syncAppLimitsToCloud(userId: String = DEFAULT_USER_ID): Result<Int>

    // Observadores en tiempo real (Remote Live Streams)
    fun observeRemoteTasks(userId: String = DEFAULT_USER_ID): Flow<List<TaskEntity>>
    fun observeRemoteHabits(userId: String = DEFAULT_USER_ID): Flow<List<HabitEntity>>
    fun observeRemoteGoals(userId: String = DEFAULT_USER_ID): Flow<List<GoalEntity>>

    companion object {
        const val DEFAULT_USER_ID = "executive_user_default"
    }
}

/**
 * Extensión segura para esperar la resolución de Task<T> en corrutinas
 * sin colisionar con overloads externos y con soporte de cancelación.
 */
suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { result ->
        if (cont.isActive) cont.resume(result)
    }
    addOnFailureListener { exception ->
        if (cont.isActive) cont.resumeWithException(exception)
    }
    addOnCanceledListener {
        cont.cancel()
    }
}

/**
 * Implementación de PersonalOsCloudSyncRepository que orquesta operaciones
 * batch y streams reactivos con Google Cloud Firestore.
 */
class FirestoreCloudSyncRepository(
    private val dao: PersonalOsDao,
    private val firestoreProvider: () -> FirebaseFirestore? = { com.example.util.FirebaseProvider.getFirestore() }
) : PersonalOsCloudSyncRepository {

    private val _syncState = MutableStateFlow(CloudSyncState())
    override val syncState: StateFlow<CloudSyncState> = _syncState.asStateFlow()

    private fun getFirestoreSafe(): FirebaseFirestore? {
        return try {
            firestoreProvider()
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo obtener instancia de FirebaseFirestore: ${e.message}")
            null
        }
    }

    override suspend fun uploadAllToCloud(userId: String): Result<SyncResult> {
        val firestore = getFirestoreSafe()
            ?: return Result.failure(IllegalStateException("Firebase Firestore no está disponible en este entorno."))

        _syncState.update {
            it.copy(isSyncing = true, statusMessage = "Subiendo datos locales a Firestore...")
        }

        return try {
            var totalSynced = 0

            // 1. Tareas
            val tasks = dao.getAllTasks().first()
            totalSynced += pushItemsToFirestore(firestore, userId, "tasks", tasks.map { it.id.toString() to taskToMap(it) })

            // 2. Bloques de Tiempo
            // Leemos de la tabla
            val configs = dao.getAllConfigs().first()
            val cycleStartTime = configs.find { it.configKey == "cycle_start_time" }?.configValue ?: "05:00"
            val cycleDate = com.example.domain.DailyCycleEngine.getCurrentCycleDate(cycleStartTime)
            val timeBlocks = dao.getTimeBlocksByCycle(cycleDate).first()
            totalSynced += pushItemsToFirestore(firestore, userId, "time_blocks", timeBlocks.map { it.id.toString() to timeBlockToMap(it) })

            // 3. Registros de Tiempo
            val timeEntries = dao.getAllTimeEntries().first()
            totalSynced += pushItemsToFirestore(firestore, userId, "time_entries", timeEntries.map { it.id.toString() to timeEntryToMap(it) })

            // 4. Hábitos
            val habits = dao.getActiveHabits().first()
            totalSynced += pushItemsToFirestore(firestore, userId, "habits", habits.map { it.id.toString() to habitToMap(it) })

            // 5. Logs de Hábitos
            val habitLogs = dao.getHabitLogsByCycle(cycleDate).first()
            totalSynced += pushItemsToFirestore(firestore, userId, "habit_logs", habitLogs.map { it.id.toString() to habitLogToMap(it) })

            // 6. Metas y SubMetas
            val goals = dao.getAllGoals().first()
            totalSynced += pushItemsToFirestore(firestore, userId, "goals", goals.map { it.id.toString() to goalToMap(it) })
            val subGoals = dao.getAllSubGoals().first()
            totalSynced += pushItemsToFirestore(firestore, userId, "sub_goals", subGoals.map { it.id.toString() to subGoalToMap(it) })

            // 7. Finanzas
            val transactions = dao.getAllTransactions().first()
            totalSynced += pushItemsToFirestore(firestore, userId, "transactions", transactions.map { it.id.toString() to transactionToMap(it) })
            val financialGoals = dao.getFinancialGoals().first()
            totalSynced += pushItemsToFirestore(firestore, userId, "financial_goals", financialGoals.map { it.id.toString() to financialGoalToMap(it) })
            val assetsDebts = dao.getAssetsAndDebts().first()
            totalSynced += pushItemsToFirestore(firestore, userId, "assets_debts", assetsDebts.map { it.id.toString() to assetDebtToMap(it) })

            // 8. Bienestar Digital (Límites)
            val appLimits = dao.getAllAppLimits().first()
            totalSynced += pushItemsToFirestore(firestore, userId, "app_limits", appLimits.map { it.id.toString() to appLimitToMap(it) })

            // 9. Configuraciones
            totalSynced += pushItemsToFirestore(firestore, userId, "system_configs", configs.map { it.configKey to systemConfigToMap(it) })

            // 10. Actualizar metadatos de sincronización
            val metadata = mapOf(
                "lastSyncTimestamp" to System.currentTimeMillis(),
                "totalDocuments" to totalSynced,
                "status" to "SUCCESS",
                "clientApp" to "PersonalOS"
            )
            firestore.collection("users").document(userId)
                .collection("metadata").document("sync_info")
                .set(metadata, SetOptions.merge())
                .awaitTask()

            val now = System.currentTimeMillis()
            _syncState.update {
                it.copy(
                    isSyncing = false,
                    lastSyncTimestamp = now,
                    lastSyncSuccess = true,
                    lastSyncedCount = totalSynced,
                    statusMessage = "Respaldo completado ($totalSynced elementos)",
                    error = null
                )
            }

            Result.success(
                SyncResult(
                    isSuccess = true,
                    syncedItemsCount = totalSynced,
                    message = "Subida exitosa de $totalSynced registros a Firestore.",
                    timestamp = now
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error durante uploadAllToCloud: ${e.message}", e)
            _syncState.update {
                it.copy(
                    isSyncing = false,
                    lastSyncSuccess = false,
                    statusMessage = "Error en subida a Firestore",
                    error = e.message
                )
            }
            Result.failure(e)
        }
    }

    override suspend fun downloadAllFromCloud(userId: String): Result<SyncResult> {
        val firestore = getFirestoreSafe()
            ?: return Result.failure(IllegalStateException("Firebase Firestore no está disponible en este entorno."))

        _syncState.update {
            it.copy(isSyncing = true, statusMessage = "Descargando datos desde Firestore...")
        }

        return try {
            var restoredCount = 0

            // 1. Tareas
            val tasksSnapshot = firestore.collection("users").document(userId).collection("tasks").get().awaitTask()
            for (doc in tasksSnapshot.documents) {
                mapToTask(doc)?.let {
                    dao.insertTask(it)
                    restoredCount++
                }
            }

            // 2. Bloques de Tiempo
            val timeBlocksSnapshot = firestore.collection("users").document(userId).collection("time_blocks").get().awaitTask()
            for (doc in timeBlocksSnapshot.documents) {
                mapToTimeBlock(doc)?.let {
                    dao.insertTimeBlock(it)
                    restoredCount++
                }
            }

            // 3. Registros de Tiempo
            val timeEntriesSnapshot = firestore.collection("users").document(userId).collection("time_entries").get().awaitTask()
            for (doc in timeEntriesSnapshot.documents) {
                mapToTimeEntry(doc)?.let {
                    dao.insertTimeEntry(it)
                    restoredCount++
                }
            }

            // 4. Hábitos
            val habitsSnapshot = firestore.collection("users").document(userId).collection("habits").get().awaitTask()
            for (doc in habitsSnapshot.documents) {
                mapToHabit(doc)?.let {
                    dao.insertHabit(it)
                    restoredCount++
                }
            }

            // 5. Logs de Hábitos
            val habitLogsSnapshot = firestore.collection("users").document(userId).collection("habit_logs").get().awaitTask()
            for (doc in habitLogsSnapshot.documents) {
                mapToHabitLog(doc)?.let {
                    dao.insertHabitLog(it)
                    restoredCount++
                }
            }

            // 6. Metas y SubMetas
            val goalsSnapshot = firestore.collection("users").document(userId).collection("goals").get().awaitTask()
            for (doc in goalsSnapshot.documents) {
                mapToGoal(doc)?.let {
                    dao.insertGoal(it)
                    restoredCount++
                }
            }
            val subGoalsSnapshot = firestore.collection("users").document(userId).collection("sub_goals").get().awaitTask()
            for (doc in subGoalsSnapshot.documents) {
                mapToSubGoal(doc)?.let {
                    dao.insertSubGoal(it)
                    restoredCount++
                }
            }

            // 7. Finanzas
            val transactionsSnapshot = firestore.collection("users").document(userId).collection("transactions").get().awaitTask()
            for (doc in transactionsSnapshot.documents) {
                mapToTransaction(doc)?.let {
                    dao.insertTransaction(it)
                    restoredCount++
                }
            }
            val financialGoalsSnapshot = firestore.collection("users").document(userId).collection("financial_goals").get().awaitTask()
            for (doc in financialGoalsSnapshot.documents) {
                mapToFinancialGoal(doc)?.let {
                    dao.insertFinancialGoal(it)
                    restoredCount++
                }
            }
            val assetsDebtsSnapshot = firestore.collection("users").document(userId).collection("assets_debts").get().awaitTask()
            for (doc in assetsDebtsSnapshot.documents) {
                mapToAssetDebt(doc)?.let {
                    dao.insertAssetDebt(it)
                    restoredCount++
                }
            }

            // 8. Límites de Apps
            val appLimitsSnapshot = firestore.collection("users").document(userId).collection("app_limits").get().awaitTask()
            for (doc in appLimitsSnapshot.documents) {
                mapToAppLimit(doc)?.let {
                    dao.insertAppLimit(it)
                    restoredCount++
                }
            }

            // 9. Configuraciones
            val configsSnapshot = firestore.collection("users").document(userId).collection("system_configs").get().awaitTask()
            for (doc in configsSnapshot.documents) {
                mapToSystemConfig(doc)?.let {
                    dao.setConfigValue(it)
                    restoredCount++
                }
            }

            val now = System.currentTimeMillis()
            _syncState.update {
                it.copy(
                    isSyncing = false,
                    lastSyncTimestamp = now,
                    lastSyncSuccess = true,
                    lastSyncedCount = restoredCount,
                    statusMessage = "Descarga completada ($restoredCount elementos)",
                    error = null
                )
            }

            Result.success(
                SyncResult(
                    isSuccess = true,
                    syncedItemsCount = restoredCount,
                    message = "Descarga exitosa de $restoredCount registros desde Firestore.",
                    timestamp = now
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error durante downloadAllFromCloud: ${e.message}", e)
            _syncState.update {
                it.copy(
                    isSyncing = false,
                    lastSyncSuccess = false,
                    statusMessage = "Error al descargar desde Firestore",
                    error = e.message
                )
            }
            Result.failure(e)
        }
    }

    override suspend fun synchronize(userId: String): Result<SyncResult> {
        // Enfoque robusto: primero asegurar la descarga de novedades en la nube y luego respaldar lo local
        val downloadRes = downloadAllFromCloud(userId)
        val uploadRes = uploadAllToCloud(userId)

        return if (uploadRes.isSuccess) {
            val total = (downloadRes.getOrNull()?.syncedItemsCount ?: 0) + (uploadRes.getOrNull()?.syncedItemsCount ?: 0)
            Result.success(
                SyncResult(
                    isSuccess = true,
                    syncedItemsCount = total,
                    message = "Sincronización completa finalizada exitosamente.",
                    timestamp = System.currentTimeMillis()
                )
            )
        } else {
            uploadRes
        }
    }

    // === SINCRONIZACIONES PARCIALES POR MÓDULO ===
    override suspend fun syncTasksToCloud(userId: String): Result<Int> = runCatching {
        val firestore = getFirestoreSafe() ?: throw IllegalStateException("Firestore no disponible")
        val tasks = dao.getAllTasks().first()
        pushItemsToFirestore(firestore, userId, "tasks", tasks.map { it.id.toString() to taskToMap(it) })
    }

    override suspend fun syncHabitsToCloud(userId: String): Result<Int> = runCatching {
        val firestore = getFirestoreSafe() ?: throw IllegalStateException("Firestore no disponible")
        val habits = dao.getActiveHabits().first()
        pushItemsToFirestore(firestore, userId, "habits", habits.map { it.id.toString() to habitToMap(it) })
    }

    override suspend fun syncGoalsToCloud(userId: String): Result<Int> = runCatching {
        val firestore = getFirestoreSafe() ?: throw IllegalStateException("Firestore no disponible")
        val goals = dao.getAllGoals().first()
        pushItemsToFirestore(firestore, userId, "goals", goals.map { it.id.toString() to goalToMap(it) })
    }

    override suspend fun syncFinancesToCloud(userId: String): Result<Int> = runCatching {
        val firestore = getFirestoreSafe() ?: throw IllegalStateException("Firestore no disponible")
        val txs = dao.getAllTransactions().first()
        pushItemsToFirestore(firestore, userId, "transactions", txs.map { it.id.toString() to transactionToMap(it) })
    }

    override suspend fun syncTimeTrackingToCloud(userId: String): Result<Int> = runCatching {
        val firestore = getFirestoreSafe() ?: throw IllegalStateException("Firestore no disponible")
        val entries = dao.getAllTimeEntries().first()
        pushItemsToFirestore(firestore, userId, "time_entries", entries.map { it.id.toString() to timeEntryToMap(it) })
    }

    override suspend fun syncAppLimitsToCloud(userId: String): Result<Int> = runCatching {
        val firestore = getFirestoreSafe() ?: throw IllegalStateException("Firestore no disponible")
        val limits = dao.getAllAppLimits().first()
        pushItemsToFirestore(firestore, userId, "app_limits", limits.map { it.id.toString() to appLimitToMap(it) })
    }

    // === OBSERVADORES EN TIEMPO REAL (REMOTE LIVE FLOWS) ===
    override fun observeRemoteTasks(userId: String): Flow<List<TaskEntity>> = callbackFlow {
        val firestore = getFirestoreSafe()
        if (firestore == null) {
            close(IllegalStateException("Firestore no disponible"))
            return@callbackFlow
        }

        val registration = firestore.collection("users").document(userId).collection("tasks")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val tasks = snapshot.documents.mapNotNull { mapToTask(it) }
                    trySend(tasks)
                }
            }

        awaitClose { registration.remove() }
    }

    override fun observeRemoteHabits(userId: String): Flow<List<HabitEntity>> = callbackFlow {
        val firestore = getFirestoreSafe()
        if (firestore == null) {
            close(IllegalStateException("Firestore no disponible"))
            return@callbackFlow
        }

        val registration = firestore.collection("users").document(userId).collection("habits")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val habits = snapshot.documents.mapNotNull { mapToHabit(it) }
                    trySend(habits)
                }
            }

        awaitClose { registration.remove() }
    }

    override fun observeRemoteGoals(userId: String): Flow<List<GoalEntity>> = callbackFlow {
        val firestore = getFirestoreSafe()
        if (firestore == null) {
            close(IllegalStateException("Firestore no disponible"))
            return@callbackFlow
        }

        val registration = firestore.collection("users").document(userId).collection("goals")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val goals = snapshot.documents.mapNotNull { mapToGoal(it) }
                    trySend(goals)
                }
            }

        awaitClose { registration.remove() }
    }

    // === HELPER DE ESCRITURA EN LOTES (BATCH WRITING) ===
    private suspend fun pushItemsToFirestore(
        firestore: FirebaseFirestore,
        userId: String,
        subcollectionName: String,
        items: List<Pair<String, Map<String, Any?>>>
    ): Int {
        if (items.isEmpty()) return 0

        // El límite de Firestore es de 500 operaciones por batch. Chunking a 400.
        val chunks = items.chunked(400)
        for (chunk in chunks) {
            val batch = firestore.batch()
            val collectionRef = firestore.collection("users").document(userId).collection(subcollectionName)
            for ((docId, data) in chunk) {
                val docRef = collectionRef.document(docId)
                batch.set(docRef, data, SetOptions.merge())
            }
            batch.commit().awaitTask()
        }
        return items.size
    }

    // === MAPPERS DE ENTIDADES A MAPAS (FIRESTORE) ===
    private fun taskToMap(task: TaskEntity): Map<String, Any?> = mapOf(
        "id" to task.id,
        "title" to task.title,
        "description" to task.description,
        "priority" to task.priority,
        "dueDate" to task.dueDate,
        "estimatedMinutes" to task.estimatedMinutes,
        "actualMinutes" to task.actualMinutes,
        "isCompleted" to task.isCompleted,
        "category" to task.category,
        "timeBlockId" to task.timeBlockId,
        "cycleDate" to task.cycleDate,
        "syncedAt" to System.currentTimeMillis()
    )

    private fun timeBlockToMap(block: TimeBlockEntity): Map<String, Any?> = mapOf(
        "id" to block.id,
        "title" to block.title,
        "startTime" to block.startTime,
        "endTime" to block.endTime,
        "category" to block.category,
        "cycleDate" to block.cycleDate,
        "isCompleted" to block.isCompleted,
        "syncedAt" to System.currentTimeMillis()
    )

    private fun timeEntryToMap(entry: TimeEntryEntity): Map<String, Any?> = mapOf(
        "id" to entry.id,
        "activityName" to entry.activityName,
        "durationMinutes" to entry.durationMinutes,
        "category" to entry.category,
        "cycleDate" to entry.cycleDate,
        "timestamp" to entry.timestamp,
        "syncedAt" to System.currentTimeMillis()
    )

    private fun habitToMap(habit: HabitEntity): Map<String, Any?> = mapOf(
        "id" to habit.id,
        "title" to habit.title,
        "description" to habit.description,
        "frequency" to habit.frequency,
        "targetValue" to habit.targetValue,
        "unit" to habit.unit,
        "reminderTime" to habit.reminderTime,
        "currentStreak" to habit.currentStreak,
        "bestStreak" to habit.bestStreak,
        "category" to habit.category,
        "isActive" to habit.isActive,
        "goalId" to habit.goalId,
        "syncedAt" to System.currentTimeMillis()
    )

    private fun habitLogToMap(log: HabitLogEntity): Map<String, Any?> = mapOf(
        "id" to log.id,
        "habitId" to log.habitId,
        "cycleDate" to log.cycleDate,
        "isCompleted" to log.isCompleted,
        "valueAchieved" to log.valueAchieved,
        "timestamp" to log.timestamp,
        "syncedAt" to System.currentTimeMillis()
    )

    private fun goalToMap(goal: GoalEntity): Map<String, Any?> = mapOf(
        "id" to goal.id,
        "title" to goal.title,
        "description" to goal.description,
        "category" to goal.category,
        "targetDate" to goal.targetDate,
        "status" to goal.status,
        "createdAt" to goal.createdAt,
        "syncedAt" to System.currentTimeMillis()
    )

    private fun subGoalToMap(subGoal: SubGoalEntity): Map<String, Any?> = mapOf(
        "id" to subGoal.id,
        "goalId" to subGoal.goalId,
        "title" to subGoal.title,
        "isCompleted" to subGoal.isCompleted,
        "weight" to subGoal.weight,
        "syncedAt" to System.currentTimeMillis()
    )

    private fun transactionToMap(tx: TransactionEntity): Map<String, Any?> = mapOf(
        "id" to tx.id,
        "type" to tx.type,
        "amount" to tx.amount,
        "category" to tx.category,
        "description" to tx.description,
        "cycleDate" to tx.cycleDate,
        "timestamp" to tx.timestamp,
        "syncedAt" to System.currentTimeMillis()
    )

    private fun financialGoalToMap(fg: FinancialGoalEntity): Map<String, Any?> = mapOf(
        "id" to fg.id,
        "name" to fg.name,
        "targetAmount" to fg.targetAmount,
        "currentAmount" to fg.currentAmount,
        "deadline" to fg.deadline,
        "category" to fg.category,
        "syncedAt" to System.currentTimeMillis()
    )

    private fun assetDebtToMap(ad: AssetDebtEntity): Map<String, Any?> = mapOf(
        "id" to ad.id,
        "type" to ad.type,
        "name" to ad.name,
        "currentAmount" to ad.currentAmount,
        "notes" to ad.notes,
        "syncedAt" to System.currentTimeMillis()
    )

    private fun appLimitToMap(al: AppLimitEntity): Map<String, Any?> = mapOf(
        "id" to al.id,
        "packageName" to al.packageName,
        "appName" to al.appName,
        "dailyBudgetMinutes" to al.dailyBudgetMinutes,
        "currentUsageMinutes" to al.currentUsageMinutes,
        "isBlocked" to al.isBlocked,
        "isStrictLocked" to al.isStrictLocked,
        "cycleDate" to al.cycleDate,
        "syncedAt" to System.currentTimeMillis()
    )

    private fun systemConfigToMap(sc: SystemConfigEntity): Map<String, Any?> = mapOf(
        "configKey" to sc.configKey,
        "configValue" to sc.configValue,
        "syncedAt" to System.currentTimeMillis()
    )

    // === MAPPERS DE FIRESTORE (DOCUMENT SNAPSHOT) A ENTIDADES ROOM ===
    private fun mapToTask(doc: DocumentSnapshot): TaskEntity? {
        val title = doc.getString("title") ?: return null
        return TaskEntity(
            id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: 0L,
            title = title,
            description = doc.getString("description") ?: "",
            priority = doc.getString("priority") ?: "MEDIA",
            dueDate = doc.getString("dueDate") ?: "",
            estimatedMinutes = doc.getLong("estimatedMinutes")?.toInt() ?: 30,
            actualMinutes = doc.getLong("actualMinutes")?.toInt() ?: 0,
            isCompleted = doc.getBoolean("isCompleted") ?: false,
            category = doc.getString("category") ?: "Trabajo",
            timeBlockId = doc.getLong("timeBlockId"),
            cycleDate = doc.getString("cycleDate") ?: ""
        )
    }

    private fun mapToTimeBlock(doc: DocumentSnapshot): TimeBlockEntity? {
        val title = doc.getString("title") ?: return null
        return TimeBlockEntity(
            id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: 0L,
            title = title,
            startTime = doc.getString("startTime") ?: "09:00",
            endTime = doc.getString("endTime") ?: "10:00",
            category = doc.getString("category") ?: "Enfoque",
            cycleDate = doc.getString("cycleDate") ?: "",
            isCompleted = doc.getBoolean("isCompleted") ?: false
        )
    }

    private fun mapToTimeEntry(doc: DocumentSnapshot): TimeEntryEntity? {
        val activityName = doc.getString("activityName") ?: return null
        return TimeEntryEntity(
            id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: 0L,
            activityName = activityName,
            durationMinutes = doc.getLong("durationMinutes")?.toInt() ?: 0,
            category = doc.getString("category") ?: "Enfoque",
            cycleDate = doc.getString("cycleDate") ?: "",
            timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
        )
    }

    private fun mapToHabit(doc: DocumentSnapshot): HabitEntity? {
        val title = doc.getString("title") ?: return null
        return HabitEntity(
            id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: 0L,
            title = title,
            description = doc.getString("description") ?: "",
            frequency = doc.getString("frequency") ?: "DIARIO",
            targetValue = doc.getLong("targetValue")?.toInt() ?: 1,
            unit = doc.getString("unit") ?: "veces",
            reminderTime = doc.getString("reminderTime") ?: "",
            currentStreak = doc.getLong("currentStreak")?.toInt() ?: 0,
            bestStreak = doc.getLong("bestStreak")?.toInt() ?: 0,
            category = doc.getString("category") ?: "Salud",
            isActive = doc.getBoolean("isActive") ?: true,
            goalId = doc.getLong("goalId")
        )
    }

    private fun mapToHabitLog(doc: DocumentSnapshot): HabitLogEntity? {
        val habitId = doc.getLong("habitId") ?: return null
        val cycleDate = doc.getString("cycleDate") ?: return null
        return HabitLogEntity(
            id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: 0L,
            habitId = habitId,
            cycleDate = cycleDate,
            isCompleted = doc.getBoolean("isCompleted") ?: false,
            valueAchieved = doc.getLong("valueAchieved")?.toInt() ?: 1,
            timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
        )
    }

    private fun mapToGoal(doc: DocumentSnapshot): GoalEntity? {
        val title = doc.getString("title") ?: return null
        return GoalEntity(
            id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: 0L,
            title = title,
            description = doc.getString("description") ?: "",
            category = doc.getString("category") ?: "Personal",
            targetDate = doc.getString("targetDate") ?: "",
            status = doc.getString("status") ?: "EN_PROGRESO",
            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
        )
    }

    private fun mapToSubGoal(doc: DocumentSnapshot): SubGoalEntity? {
        val goalId = doc.getLong("goalId") ?: return null
        val title = doc.getString("title") ?: return null
        return SubGoalEntity(
            id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: 0L,
            goalId = goalId,
            title = title,
            isCompleted = doc.getBoolean("isCompleted") ?: false,
            weight = doc.getLong("weight")?.toInt() ?: 1
        )
    }

    private fun mapToTransaction(doc: DocumentSnapshot): TransactionEntity? {
        val type = doc.getString("type") ?: return null
        val amount = doc.getDouble("amount") ?: return null
        return TransactionEntity(
            id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: 0L,
            type = type,
            amount = amount,
            category = doc.getString("category") ?: "General",
            description = doc.getString("description") ?: "",
            cycleDate = doc.getString("cycleDate") ?: "",
            timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
        )
    }

    private fun mapToFinancialGoal(doc: DocumentSnapshot): FinancialGoalEntity? {
        val name = doc.getString("name") ?: return null
        val targetAmount = doc.getDouble("targetAmount") ?: return null
        return FinancialGoalEntity(
            id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: 0L,
            name = name,
            targetAmount = targetAmount,
            currentAmount = doc.getDouble("currentAmount") ?: 0.0,
            deadline = doc.getString("deadline") ?: "",
            category = doc.getString("category") ?: "Ahorro"
        )
    }

    private fun mapToAssetDebt(doc: DocumentSnapshot): AssetDebtEntity? {
        val type = doc.getString("type") ?: return null
        val name = doc.getString("name") ?: return null
        val currentAmount = doc.getDouble("currentAmount") ?: return null
        return AssetDebtEntity(
            id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: 0L,
            type = type,
            name = name,
            currentAmount = currentAmount,
            notes = doc.getString("notes") ?: ""
        )
    }

    private fun mapToAppLimit(doc: DocumentSnapshot): AppLimitEntity? {
        val packageName = doc.getString("packageName") ?: return null
        val appName = doc.getString("appName") ?: return null
        return AppLimitEntity(
            id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: 0L,
            packageName = packageName,
            appName = appName,
            dailyBudgetMinutes = doc.getLong("dailyBudgetMinutes")?.toInt() ?: 30,
            currentUsageMinutes = doc.getLong("currentUsageMinutes")?.toInt() ?: 0,
            isBlocked = doc.getBoolean("isBlocked") ?: false,
            isStrictLocked = doc.getBoolean("isStrictLocked") ?: false,
            cycleDate = doc.getString("cycleDate") ?: ""
        )
    }

    private fun mapToSystemConfig(doc: DocumentSnapshot): SystemConfigEntity? {
        val key = doc.getString("configKey") ?: doc.id
        val value = doc.getString("configValue") ?: return null
        return SystemConfigEntity(configKey = key, configValue = value)
    }

    companion object {
        private const val TAG = "FirestoreCloudSync"
    }
}
