package com.example.domain

import com.example.data.model.*
import kotlin.math.sqrt

object ProgressionEngine {

    /**
     * Calcula la progresión real del usuario en base a los datos verificados del sistema.
     * XP otorgada:
     * - Tarea completada: +25 XP
     * - Registro de hábito cumplido: +20 XP
     * - Minuto de enfoque cronometrado: +1 XP
     * - Sesión de entrenamiento completada: +100 XP
     * - Sub-meta cumplida: +75 XP
     * - Meta principal completada: +300 XP
     * - Transacción/registro financiero: +10 XP
     */
    fun calculateProgression(
        tasks: List<TaskEntity>,
        habitLogs: List<HabitLogEntity>,
        habits: List<HabitEntity>,
        timeEntries: List<TimeEntryEntity>,
        workouts: List<WorkoutSessionEntity>,
        goals: List<GoalEntity>,
        subGoals: List<SubGoalEntity>,
        transactions: List<TransactionEntity>,
        assetsAndDebts: List<AssetDebtEntity>
    ): Pair<UserProgression, List<AchievementItem>> {

        val completedTasksCount = tasks.count { it.isCompleted }
        val completedHabitLogsCount = habitLogs.count { it.isCompleted }
        val totalFocusMinutes = timeEntries.sumOf { it.durationMinutes }
        val workoutSessionsCount = workouts.size
        val completedGoalsCount = goals.count { it.status == "COMPLETADA" }
        val completedSubGoalsCount = subGoals.count { it.isCompleted }
        val financialRecordsCount = transactions.size

        // Cálculo total de XP
        val totalXp = (completedTasksCount * 25L) +
                (completedHabitLogsCount * 20L) +
                (totalFocusMinutes * 1L) +
                (workoutSessionsCount * 100L) +
                (completedSubGoalsCount * 75L) +
                (completedGoalsCount * 300L) +
                (financialRecordsCount * 10L)

        // Nivel calculado mediante curva RPG cuadrática equilibrada
        // Nivel 1 = 0 XP, Nivel 2 = 200 XP, Nivel 3 = 600 XP, etc.
        val level = (sqrt(totalXp / 150.0).toInt() + 1).coerceAtLeast(1)
        val xpForCurrentLevelFloor = ((level - 1) * (level - 1) * 150L)
        val xpForNextLevelCeiling = (level * level * 150L)
        val xpRange = (xpForNextLevelCeiling - xpForCurrentLevelFloor).coerceAtLeast(1L)
        val xpInLevel = (totalXp - xpForCurrentLevelFloor).coerceAtLeast(0L)
        val progressPercent = (xpInLevel.toFloat() / xpRange.toFloat()).coerceIn(0f, 1f)

        // Títulos de Rango Táctico
        val rankTitle = when {
            level >= 50 -> "SOBERANO DEL TIEMPO (CLASE S)"
            level >= 35 -> "COMANDANTE DE ENFOQUE"
            level >= 25 -> "MAESTRO DE EJECUCIÓN"
            level >= 18 -> "ARQUITECTO DEL HÁBITO"
            level >= 12 -> "VANGUARDIA TÁCTICA"
            level >= 6  -> "EXPLORADOR DE DISCIPLINA"
            else        -> "OPERADOR INICIADO"
        }

        // Racha actual (máximo entre hábitos)
        val maxStreak = habits.maxOfOrNull { it.currentStreak } ?: 0

        // Atributos RPG Tácticos (0 a 100)
        val disciplineScore = ((maxStreak * 10) + (completedHabitLogsCount * 2)).coerceIn(10, 100)
        val productivityScore = ((completedTasksCount * 4) + (completedSubGoalsCount * 8)).coerceIn(10, 100)
        val focusScore = (totalFocusMinutes / 10).coerceIn(10, 100)
        val vitalityScore = ((workoutSessionsCount * 12) + (habits.filter { it.category == "Salud" && it.currentStreak > 0 }.size * 15)).coerceIn(10, 100)
        val netWorth = assetsAndDebts.filter { it.type == "ACTIVO" }.sumOf { it.currentAmount } -
                assetsAndDebts.filter { it.type == "DEUDA" }.sumOf { it.currentAmount }
        val financialScore = ((financialRecordsCount * 3) + if (netWorth > 0) 30 else 0).coerceIn(10, 100)

        val progression = UserProgression(
            level = level,
            rankTitle = rankTitle,
            totalXp = totalXp,
            currentLevelXp = xpInLevel,
            nextLevelXp = xpRange,
            progressPercent = progressPercent,
            currentStreak = maxStreak,
            disciplineScore = disciplineScore,
            productivityScore = productivityScore,
            focusScore = focusScore,
            vitalityScore = vitalityScore,
            financialScore = financialScore
        )

        // Lista de logros verificados
        val achievements = listOf(
            AchievementItem(
                id = "ach_first_task",
                title = "Iniciación Táctica",
                description = "Completa tu primera tarea en el sistema",
                category = "PRODUCTIVIDAD",
                isUnlocked = completedTasksCount >= 1,
                currentProgress = completedTasksCount.coerceAtMost(1),
                maxProgress = 1,
                xpReward = 100
            ),
            AchievementItem(
                id = "ach_streak_7",
                title = "Consistencia de 7 Días",
                description = "Mantén una racha de hábitos durante 7 días consecutivos",
                category = "DISCIPLINA",
                isUnlocked = maxStreak >= 7,
                currentProgress = maxStreak.coerceAtMost(7),
                maxProgress = 7,
                xpReward = 350
            ),
            AchievementItem(
                id = "ach_streak_30",
                title = "Voluntad Inquebrantable (30D)",
                description = "Alcanza 30 días consecutivos de disciplina",
                category = "DISCIPLINA",
                isUnlocked = maxStreak >= 30,
                currentProgress = maxStreak.coerceAtMost(30),
                maxProgress = 30,
                xpReward = 1000
            ),
            AchievementItem(
                id = "ach_tasks_100",
                title = "Centurión de Ejecución",
                description = "Completa 100 tareas registradas en LifeOS",
                category = "PRODUCTIVIDAD",
                isUnlocked = completedTasksCount >= 100,
                currentProgress = completedTasksCount.coerceAtMost(100),
                maxProgress = 100,
                xpReward = 800
            ),
            AchievementItem(
                id = "ach_deep_work",
                title = "Maestría en Enfoque",
                description = "Acumula más de 300 minutos de trabajo concentrado",
                category = "PRODUCTIVIDAD",
                isUnlocked = totalFocusMinutes >= 300,
                currentProgress = totalFocusMinutes.coerceAtMost(300),
                maxProgress = 300,
                xpReward = 400
            ),
            AchievementItem(
                id = "ach_first_workout",
                title = "Vanguardia Física",
                description = "Registra y completa tu primera rutina de entrenamiento",
                category = "ENTRENAMIENTO",
                isUnlocked = workoutSessionsCount >= 1,
                currentProgress = workoutSessionsCount.coerceAtMost(1),
                maxProgress = 1,
                xpReward = 200
            ),
            AchievementItem(
                id = "ach_workout_10",
                title = "Condición de Acero",
                description = "Completa 10 sesiones de entrenamiento",
                category = "ENTRENAMIENTO",
                isUnlocked = workoutSessionsCount >= 10,
                currentProgress = workoutSessionsCount.coerceAtMost(10),
                maxProgress = 10,
                xpReward = 750
            ),
            AchievementItem(
                id = "ach_goal_completed",
                title = "Conquista de Objetivo",
                description = "Alcanza un objetivo principal al 100%",
                category = "DISCIPLINA",
                isUnlocked = completedGoalsCount >= 1,
                currentProgress = completedGoalsCount.coerceAtMost(1),
                maxProgress = 1,
                xpReward = 600
            ),
            AchievementItem(
                id = "ach_positive_networth",
                title = "Control Patrimonial",
                description = "Registra patrimonio neto positivo y control de deudas",
                category = "FINANZAS",
                isUnlocked = netWorth > 0 && financialRecordsCount >= 3,
                currentProgress = if (netWorth > 0 && financialRecordsCount >= 3) 1 else 0,
                maxProgress = 1,
                xpReward = 500
            )
        )

        return Pair(progression, achievements)
    }
}
