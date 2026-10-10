package com.example.util

import com.example.data.model.*
import com.example.data.repository.PersonalOsRepository
import com.example.domain.DailyCycleEngine
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

/**
 * Gestor de copia de seguridad local (Offline-first Export/Import en JSON).
 * Permite a los usuarios tener soberanía total sobre sus datos:
 * exportar toda la base de datos a un archivo JSON o texto portátil,
 * e importar/restaurar en cualquier dispositivo.
 * Incluye todas las tablas de Room y conserva las fechas reales de los registros.
 */
object PersonalOsBackupManager {

    suspend fun exportDatabaseToJson(repository: PersonalOsRepository): String {
        val root = JSONObject()
        root.put("app", "Sistema Personal")
        root.put("version", 2)
        root.put("exportDate", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))

        // 1. Tareas
        val tasks = repository.allTasks.first()
        val tasksArray = JSONArray()
        tasks.forEach { t ->
            val obj = JSONObject().apply {
                put("title", t.title)
                put("description", t.description)
                put("priority", t.priority)
                put("cycleDate", t.cycleDate)
                put("isCompleted", t.isCompleted)
                put("estimatedMinutes", t.estimatedMinutes)
                put("actualMinutes", t.actualMinutes)
                put("category", t.category)
                put("dueDate", t.dueDate)
                put("dueTime", t.dueTime)
                put("repetitionType", t.repetitionType)
                put("repetitionDays", t.repetitionDays)
            }
            tasksArray.put(obj)
        }
        root.put("tasks", tasksArray)

        // 2. Hábitos
        val habits = repository.activeHabits.first()
        val habitsArray = JSONArray()
        habits.forEach { h ->
            val obj = JSONObject().apply {
                put("id", h.id)
                put("title", h.title)
                put("frequency", h.frequency)
                put("category", h.category)
                put("targetValue", h.targetValue)
                put("currentStreak", h.currentStreak)
                put("bestStreak", h.bestStreak)
                put("unit", h.unit)
                put("goalId", h.goalId ?: 0L)
            }
            habitsArray.put(obj)
        }
        root.put("habits", habitsArray)

        // 3. Registros de Hábitos (Habit Logs)
        val habitLogs = repository.allHabitLogs.first()
        val habitLogsArray = JSONArray()
        habitLogs.forEach { hl ->
            val obj = JSONObject().apply {
                put("habitId", hl.habitId)
                put("cycleDate", hl.cycleDate)
                put("isCompleted", hl.isCompleted)
                put("valueAchieved", hl.valueAchieved)
                put("timestamp", hl.timestamp)
            }
            habitLogsArray.put(obj)
        }
        root.put("habitLogs", habitLogsArray)

        // 4. Bloques de Tiempo
        val timeBlocks = repository.allTimeBlocks.first()
        val timeBlocksArray = JSONArray()
        timeBlocks.forEach { tb ->
            val obj = JSONObject().apply {
                put("title", tb.title)
                put("startTime", tb.startTime)
                put("endTime", tb.endTime)
                put("category", tb.category)
                put("cycleDate", tb.cycleDate)
                put("isCompleted", tb.isCompleted)
            }
            timeBlocksArray.put(obj)
        }
        root.put("timeBlocks", timeBlocksArray)

        // 5. Registros de Tiempo (Time Entries)
        val timeEntries = repository.allTimeEntries.first()
        val timeEntriesArray = JSONArray()
        timeEntries.forEach { te ->
            val obj = JSONObject().apply {
                put("activityName", te.activityName)
                put("durationMinutes", te.durationMinutes)
                put("category", te.category)
                put("cycleDate", te.cycleDate)
                put("timestamp", te.timestamp)
            }
            timeEntriesArray.put(obj)
        }
        root.put("timeEntries", timeEntriesArray)

        // 6. Metas
        val goals = repository.allGoals.first()
        val goalsArray = JSONArray()
        goals.forEach { g ->
            val obj = JSONObject().apply {
                put("id", g.id)
                put("title", g.title)
                put("description", g.description)
                put("category", g.category)
                put("targetDate", g.targetDate)
                put("status", g.status)
            }
            goalsArray.put(obj)
        }
        root.put("goals", goalsArray)

        // 7. Sub-Metas
        val subGoals = repository.allSubGoals.first()
        val subGoalsArray = JSONArray()
        subGoals.forEach { sg ->
            val obj = JSONObject().apply {
                put("goalId", sg.goalId)
                put("title", sg.title)
                put("isCompleted", sg.isCompleted)
            }
            subGoalsArray.put(obj)
        }
        root.put("subGoals", subGoalsArray)

        // 8. Finanzas: Transacciones
        val transactions = repository.allTransactions.first()
        val transArray = JSONArray()
        transactions.forEach { tr ->
            val obj = JSONObject().apply {
                put("type", tr.type)
                put("amount", tr.amount)
                put("category", tr.category)
                put("description", tr.description)
                put("cycleDate", tr.cycleDate)
                put("timestamp", tr.timestamp)
            }
            transArray.put(obj)
        }
        root.put("transactions", transArray)

        // 9. Metas Financieras
        val finGoals = repository.financialGoals.first()
        val finGoalsArray = JSONArray()
        finGoals.forEach { fg ->
            val obj = JSONObject().apply {
                put("name", fg.name)
                put("targetAmount", fg.targetAmount)
                put("currentAmount", fg.currentAmount)
                put("deadline", fg.deadline)
                put("category", fg.category)
            }
            finGoalsArray.put(obj)
        }
        root.put("financialGoals", finGoalsArray)

        // 10. Activos y Deudas
        val assetsDebts = repository.assetsAndDebts.first()
        val adArray = JSONArray()
        assetsDebts.forEach { ad ->
            val obj = JSONObject().apply {
                put("type", ad.type)
                put("name", ad.name)
                put("currentAmount", ad.currentAmount)
                put("notes", ad.notes)
            }
            adArray.put(obj)
        }
        root.put("assetsAndDebts", adArray)

        // 11. Sesiones de Entrenamiento
        val workoutSessions = repository.allWorkoutSessions.first()
        val wsArray = JSONArray()
        workoutSessions.forEach { ws ->
            val obj = JSONObject().apply {
                put("id", ws.id)
                put("title", ws.title)
                put("targetGoal", ws.targetGoal)
                put("durationMinutes", ws.durationMinutes)
                put("notes", ws.notes)
                put("cycleDate", ws.cycleDate)
                put("isCompleted", ws.isCompleted)
            }
            wsArray.put(obj)
        }
        root.put("workoutSessions", wsArray)

        // 12. Ejercicios de Entrenamiento
        val workoutExercises = repository.allWorkoutExercises.first()
        val weArray = JSONArray()
        workoutExercises.forEach { we ->
            val obj = JSONObject().apply {
                put("sessionId", we.sessionId)
                put("exerciseName", we.exerciseName)
                put("sets", we.sets)
                put("reps", we.reps)
                put("weightKg", we.weightKg)
                put("restSeconds", we.restSeconds)
                put("isCompleted", we.isCompleted)
            }
            weArray.put(obj)
        }
        root.put("workoutExercises", weArray)

        // 13. Límites de Apps (Bienestar Digital)
        val appLimits = repository.allAppLimits.first()
        val alArray = JSONArray()
        appLimits.forEach { al ->
            val obj = JSONObject().apply {
                put("packageName", al.packageName)
                put("appName", al.appName)
                put("dailyBudgetMinutes", al.dailyBudgetMinutes)
                put("isBlocked", al.isBlocked)
            }
            alArray.put(obj)
        }
        root.put("appLimits", alArray)

        // 14. Ajustes del Sistema y Progresión RPG
        val configs = repository.allConfigs.first()
        val cfgObj = JSONObject()
        configs.forEach { c ->
            cfgObj.put(c.configKey, c.configValue)
        }
        root.put("systemConfig", cfgObj)

        return root.toString(2)
    }

    suspend fun importDatabaseFromJson(repository: PersonalOsRepository, jsonString: String): Result<Int> {
        return try {
            val root = JSONObject(jsonString)
            var totalRestored = 0
            val currentCycleFallback = DailyCycleEngine.getCurrentCycleDate()

            // 1. Restaurar tareas
            if (root.has("tasks")) {
                val array = root.getJSONArray("tasks")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    repository.insertTask(
                        TaskEntity(
                            title = obj.optString("title", "Tarea sin título"),
                            description = obj.optString("description", ""),
                            priority = obj.optString("priority", "MEDIA"),
                            cycleDate = obj.optString("cycleDate", currentCycleFallback),
                            isCompleted = obj.optBoolean("isCompleted", false),
                            estimatedMinutes = obj.optInt("estimatedMinutes", 30),
                            actualMinutes = obj.optInt("actualMinutes", 0),
                            category = obj.optString("category", "Trabajo"),
                            dueDate = obj.optString("dueDate", ""),
                            dueTime = obj.optString("dueTime", ""),
                            repetitionType = obj.optString("repetitionType", "NUNCA"),
                            repetitionDays = obj.optString("repetitionDays", "")
                        )
                    )
                    totalRestored++
                }
            }

            // 2. Restaurar hábitos
            val habitOldToNewId = mutableMapOf<Long, Long>()
            if (root.has("habits")) {
                val array = root.getJSONArray("habits")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val oldId = obj.optLong("id", 0L)
                    val newId = repository.insertHabit(
                        HabitEntity(
                            title = obj.optString("title", "Hábito"),
                            frequency = obj.optString("frequency", "DIARIO"),
                            category = obj.optString("category", "General"),
                            targetValue = obj.optInt("targetValue", 1),
                            currentStreak = obj.optInt("currentStreak", 0),
                            bestStreak = obj.optInt("bestStreak", 0),
                            unit = obj.optString("unit", "minutos"),
                            goalId = if (obj.has("goalId") && obj.getLong("goalId") != 0L) obj.getLong("goalId") else null
                        )
                    )
                    if (oldId != 0L) {
                        habitOldToNewId[oldId] = newId
                    }
                    totalRestored++
                }
            }

            // 3. Restaurar registros de hábitos
            if (root.has("habitLogs")) {
                val array = root.getJSONArray("habitLogs")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val oldHabitId = obj.optLong("habitId", 0L)
                    val mappedHabitId = habitOldToNewId[oldHabitId] ?: oldHabitId
                    if (mappedHabitId != 0L) {
                        repository.insertHabitLog(
                            HabitLogEntity(
                                habitId = mappedHabitId,
                                cycleDate = obj.optString("cycleDate", currentCycleFallback),
                                isCompleted = obj.optBoolean("isCompleted", true),
                                valueAchieved = obj.optInt("valueAchieved", 1),
                                timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                            )
                        )
                        totalRestored++
                    }
                }
            }

            // 4. Restaurar bloques de tiempo
            if (root.has("timeBlocks")) {
                val array = root.getJSONArray("timeBlocks")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    repository.insertTimeBlock(
                        TimeBlockEntity(
                            title = obj.optString("title", "Bloque"),
                            startTime = obj.optString("startTime", "09:00"),
                            endTime = obj.optString("endTime", "10:00"),
                            category = obj.optString("category", "Trabajo"),
                            cycleDate = obj.optString("cycleDate", currentCycleFallback),
                            isCompleted = obj.optBoolean("isCompleted", false)
                        )
                    )
                    totalRestored++
                }
            }

            // 5. Restaurar registros de tiempo (time entries)
            if (root.has("timeEntries")) {
                val array = root.getJSONArray("timeEntries")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    repository.insertTimeEntry(
                        TimeEntryEntity(
                            activityName = obj.optString("activityName", "Sesión"),
                            durationMinutes = obj.optInt("durationMinutes", 25),
                            category = obj.optString("category", "Trabajo"),
                            cycleDate = obj.optString("cycleDate", currentCycleFallback),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                    totalRestored++
                }
            }

            // 6. Restaurar metas
            val goalOldToNewId = mutableMapOf<Long, Long>()
            if (root.has("goals")) {
                val array = root.getJSONArray("goals")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val oldId = obj.optLong("id", 0L)
                    val newId = repository.insertGoal(
                        GoalEntity(
                            title = obj.optString("title", "Meta"),
                            description = obj.optString("description", ""),
                            category = obj.optString("category", "Personal"),
                            targetDate = obj.optString("targetDate", ""),
                            status = obj.optString("status", "EN_PROGRESO")
                        )
                    )
                    if (oldId != 0L) {
                        goalOldToNewId[oldId] = newId
                    }
                    totalRestored++
                }
            }

            // 7. Restaurar sub-metas
            if (root.has("subGoals")) {
                val array = root.getJSONArray("subGoals")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val oldGoalId = obj.optLong("goalId", 0L)
                    val mappedGoalId = goalOldToNewId[oldGoalId] ?: oldGoalId
                    repository.insertSubGoal(
                        SubGoalEntity(
                            goalId = mappedGoalId,
                            title = obj.optString("title", "Sub-meta"),
                            isCompleted = obj.optBoolean("isCompleted", false)
                        )
                    )
                    totalRestored++
                }
            }

            // 8. Restaurar transacciones
            if (root.has("transactions")) {
                val array = root.getJSONArray("transactions")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    repository.insertTransaction(
                        TransactionEntity(
                            type = obj.optString("type", "GASTO"),
                            amount = obj.optDouble("amount", 0.0),
                            category = obj.optString("category", "General"),
                            description = obj.optString("description", ""),
                            cycleDate = obj.optString("cycleDate", currentCycleFallback),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                    totalRestored++
                }
            }

            // 9. Restaurar metas financieras
            if (root.has("financialGoals")) {
                val array = root.getJSONArray("financialGoals")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    repository.insertFinancialGoal(
                        FinancialGoalEntity(
                            name = obj.optString("name", "Meta de ahorro"),
                            targetAmount = obj.optDouble("targetAmount", 1000.0),
                            currentAmount = obj.optDouble("currentAmount", 0.0),
                            deadline = obj.optString("deadline", ""),
                            category = obj.optString("category", "Ahorro")
                        )
                    )
                    totalRestored++
                }
            }

            // 10. Restaurar activos y deudas
            if (root.has("assetsAndDebts")) {
                val array = root.getJSONArray("assetsAndDebts")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    repository.insertAssetDebt(
                        AssetDebtEntity(
                            type = obj.optString("type", "ACTIVO"),
                            name = obj.optString("name", "Activo"),
                            currentAmount = obj.optDouble("currentAmount", 0.0),
                            notes = obj.optString("notes", "")
                        )
                    )
                    totalRestored++
                }
            }

            // 11. Restaurar sesiones de entrenamiento
            val sessionOldToNewId = mutableMapOf<Long, Long>()
            if (root.has("workoutSessions")) {
                val array = root.getJSONArray("workoutSessions")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val oldId = obj.optLong("id", 0L)
                    val newId = repository.insertWorkoutSession(
                        WorkoutSessionEntity(
                            title = obj.optString("title", "Rutina"),
                            targetGoal = obj.optString("targetGoal", "Hipertrofia"),
                            durationMinutes = obj.optInt("durationMinutes", 45),
                            notes = obj.optString("notes", ""),
                            cycleDate = obj.optString("cycleDate", currentCycleFallback),
                            isCompleted = obj.optBoolean("isCompleted", false)
                        )
                    )
                    if (oldId != 0L) {
                        sessionOldToNewId[oldId] = newId
                    }
                    totalRestored++
                }
            }

            // 12. Restaurar ejercicios de entrenamiento
            if (root.has("workoutExercises")) {
                val array = root.getJSONArray("workoutExercises")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val oldSessionId = obj.optLong("sessionId", 0L)
                    val mappedSessionId = sessionOldToNewId[oldSessionId] ?: oldSessionId
                    repository.insertWorkoutExercise(
                        WorkoutExerciseEntity(
                            sessionId = mappedSessionId,
                            exerciseName = obj.optString("exerciseName", "Ejercicio"),
                            sets = obj.optInt("sets", 3),
                            reps = obj.optString("reps", "10"),
                            weightKg = obj.optDouble("weightKg", 0.0),
                            restSeconds = obj.optInt("restSeconds", 60),
                            isCompleted = obj.optBoolean("isCompleted", false)
                        )
                    )
                    totalRestored++
                }
            }

            // 13. Restaurar límites de apps
            if (root.has("appLimits")) {
                val array = root.getJSONArray("appLimits")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    repository.insertAppLimit(
                        AppLimitEntity(
                            packageName = obj.optString("packageName", ""),
                            appName = obj.optString("appName", ""),
                            dailyBudgetMinutes = obj.optInt("dailyBudgetMinutes", 60),
                            isBlocked = obj.optBoolean("isBlocked", false)
                        )
                    )
                    totalRestored++
                }
            }

            // 14. Restaurar ajustes y persistencia RPG
            if (root.has("systemConfig")) {
                val cfgObj = root.getJSONObject("systemConfig")
                val keys = cfgObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val value = cfgObj.optString(key, "")
                    repository.setConfig(key, value)
                }
            }

            Result.success(totalRestored)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
