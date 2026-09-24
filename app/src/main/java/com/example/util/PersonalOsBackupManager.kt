package com.example.util

import com.example.data.model.*
import com.example.data.repository.PersonalOsRepository
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
 */
object PersonalOsBackupManager {

    suspend fun exportDatabaseToJson(repository: PersonalOsRepository): String {
        val root = JSONObject()
        root.put("app", "Sistema Personal")
        root.put("version", 1)
        root.put("exportDate", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))

        // Tareas
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
            }
            tasksArray.put(obj)
        }
        root.put("tasks", tasksArray)

        // Hábitos
        val habits = repository.activeHabits.first()
        val habitsArray = JSONArray()
        habits.forEach { h ->
            val obj = JSONObject().apply {
                put("title", h.title)
                put("frequency", h.frequency)
                put("category", h.category)
                put("targetValue", h.targetValue)
                put("currentStreak", h.currentStreak)
                put("bestStreak", h.bestStreak)
            }
            habitsArray.put(obj)
        }
        root.put("habits", habitsArray)

        // Metas
        val goals = repository.allGoals.first()
        val goalsArray = JSONArray()
        goals.forEach { g ->
            val obj = JSONObject().apply {
                put("title", g.title)
                put("description", g.description)
                put("category", g.category)
                put("targetDate", g.targetDate)
                put("status", g.status)
            }
            goalsArray.put(obj)
        }
        root.put("goals", goalsArray)

        // Finanzas: Transacciones
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

        // Metas Financieras
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

        // Activos y Deudas
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

        return root.toString(2)
    }

    suspend fun importDatabaseFromJson(repository: PersonalOsRepository, jsonString: String): Result<Int> {
        return try {
            val root = JSONObject(jsonString)
            var totalRestored = 0

            // Restaurar tareas
            if (root.has("tasks")) {
                val array = root.getJSONArray("tasks")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    repository.insertTask(
                        TaskEntity(
                            title = obj.optString("title", "Tarea sin título"),
                            description = obj.optString("description", ""),
                            priority = obj.optString("priority", "MEDIA"),
                            cycleDate = obj.optString("cycleDate", "2026-09-12"),
                            isCompleted = obj.optBoolean("isCompleted", false),
                            estimatedMinutes = obj.optInt("estimatedMinutes", 30),
                            actualMinutes = obj.optInt("actualMinutes", 0),
                            category = obj.optString("category", "Trabajo")
                        )
                    )
                    totalRestored++
                }
            }

            // Restaurar hábitos
            if (root.has("habits")) {
                val array = root.getJSONArray("habits")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    repository.insertHabit(
                        HabitEntity(
                            title = obj.optString("title", "Hábito"),
                            frequency = obj.optString("frequency", "DIARIO"),
                            category = obj.optString("category", "General"),
                            targetValue = obj.optInt("targetValue", 1),
                            currentStreak = obj.optInt("currentStreak", 0),
                            bestStreak = obj.optInt("bestStreak", 0)
                        )
                    )
                    totalRestored++
                }
            }

            // Restaurar metas
            if (root.has("goals")) {
                val array = root.getJSONArray("goals")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    repository.insertGoal(
                        GoalEntity(
                            title = obj.optString("title", "Meta"),
                            description = obj.optString("description", ""),
                            category = obj.optString("category", "Personal"),
                            targetDate = obj.optString("targetDate", ""),
                            status = obj.optString("status", "EN_PROGRESO")
                        )
                    )
                    totalRestored++
                }
            }

            // Restaurar transacciones
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
                            cycleDate = obj.optString("cycleDate", "2026-09-12"),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                    totalRestored++
                }
            }

            // Restaurar metas financieras
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

            // Restaurar activos y deudas
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

            Result.success(totalRestored)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
