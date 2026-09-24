package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val priority: String = "MEDIA", // ALTA, MEDIA, BAJA
    val dueDate: String = "",
    val dueTime: String = "", // HH:mm
    val estimatedMinutes: Int = 30,
    val actualMinutes: Int = 0,
    val isCompleted: Boolean = false,
    val category: String = "Trabajo",
    val timeBlockId: Long? = null,
    val cycleDate: String = "",
    val repetitionType: String = "NUNCA", // NUNCA, DIARIO, DIAS_ESPECIFICOS, SEMANAL, MENSUAL, PERSONALIZADO
    val repetitionDays: String = "" // ej: "L,X,V"
)

@Entity(tableName = "time_blocks")
data class TimeBlockEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val startTime: String, // HH:mm
    val endTime: String,   // HH:mm
    val category: String = "Enfoque",
    val cycleDate: String = "",
    val isCompleted: Boolean = false
)

@Entity(tableName = "time_entries")
data class TimeEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val activityName: String,
    val durationMinutes: Int,
    val category: String,
    val cycleDate: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val frequency: String = "DIARIO", // DIARIO, DIAS_LABORABLES, SEMANAL
    val targetValue: Int = 1,
    val unit: String = "veces",
    val reminderTime: String = "",
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val category: String = "Salud",
    val isActive: Boolean = true,
    val goalId: Long? = null
)

@Entity(tableName = "habit_logs")
data class HabitLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val cycleDate: String,
    val isCompleted: Boolean,
    val valueAchieved: Int = 1,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = "Personal",
    val targetDate: String = "",
    val status: String = "EN_PROGRESO", // EN_PROGRESO, COMPLETADA, PAUSADA
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "sub_goals")
data class SubGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val goalId: Long,
    val title: String,
    val isCompleted: Boolean = false,
    val weight: Int = 1
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // INGRESO, GASTO
    val amount: Double,
    val category: String,
    val description: String = "",
    val cycleDate: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "financial_goals")
data class FinancialGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val deadline: String = "",
    val category: String = "Ahorro"
)

@Entity(tableName = "assets_debts")
data class AssetDebtEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // ACTIVO, DEUDA
    val name: String,
    val currentAmount: Double,
    val notes: String = ""
)

@Entity(tableName = "app_limits")
data class AppLimitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val appName: String,
    val dailyBudgetMinutes: Int, // Ej: 30
    val currentUsageMinutes: Int = 0,
    val isBlocked: Boolean = false,
    val isStrictLocked: Boolean = false,
    val cycleDate: String = ""
)

@Entity(tableName = "system_config")
data class SystemConfigEntity(
    @PrimaryKey val configKey: String,
    val configValue: String
)

@Entity(tableName = "workout_sessions")
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String, // ej: "Rutina Torso-Pierna", "Fuerza / Hipertrofia", "Cardio táctico"
    val targetGoal: String = "Hipertrofia", // Hipertrofia, Fuerza, Resistencia, Pérdida de grasa, Salud
    val cycleDate: String = "",
    val durationMinutes: Int = 45,
    val restDefaultSeconds: Int = 90,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "workout_exercises")
data class WorkoutExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val exerciseName: String,
    val sets: Int = 3,
    val reps: String = "10, 10, 8",
    val weightKg: Double = 0.0,
    val restSeconds: Int = 90,
    val notes: String = ""
)

data class WorkoutSessionWithExercises(
    val session: WorkoutSessionEntity,
    val exercises: List<WorkoutExerciseEntity> = emptyList()
)

data class UserProgression(
    val level: Int = 1,
    val rankTitle: String = "OPERADOR INICIADO",
    val totalXp: Long = 0L,
    val currentLevelXp: Long = 0L,
    val nextLevelXp: Long = 1000L,
    val progressPercent: Float = 0f,
    val currentStreak: Int = 0,
    val disciplineScore: Int = 10,
    val productivityScore: Int = 10,
    val focusScore: Int = 10,
    val vitalityScore: Int = 10,
    val financialScore: Int = 10
)

data class AchievementItem(
    val id: String,
    val title: String,
    val description: String,
    val category: String, // DISCIPLINA, PRODUCTIVIDAD, FINANZAS, BIENESTAR, ENTRENAMIENTO
    val isUnlocked: Boolean,
    val unlockedDate: String? = null,
    val currentProgress: Int = 0,
    val maxProgress: Int = 1,
    val xpReward: Int = 200
)

data class NetWorthSnapshot(
    val dateLabel: String,
    val netWorth: Double,
    val assets: Double,
    val debts: Double
)

enum class GlobalEntryType {
    WORKOUT,
    TIME_TRACKING,
    TRANSACTION_EXPENSE,
    TRANSACTION_INCOME,
    TASK
}

data class DetectedGlobalEntry(
    val type: GlobalEntryType,
    val title: String,
    val detail: String,
    val numericValue: Double = 0.0,
    val category: String = "General"
)

enum class ChatSender {
    USER,
    ASSISTANT
}

enum class GeminiModelOption(val modelId: String, val displayName: String, val badge: String, val description: String) {
    FLASH_LITE(
        modelId = "gemini-3.1-flash-lite-preview",
        displayName = "Gemini 3.1 Flash-Lite",
        badge = "RÁPIDO",
        description = "Respuestas instantáneas y tareas ágiles"
    ),
    FLASH(
        modelId = "gemini-3.5-flash",
        displayName = "Gemini 3.5 Flash",
        badge = "ESTÁNDAR",
        description = "Propósito general y diagnóstico equilibrado"
    ),
    PRO(
        modelId = "gemini-3.1-pro-preview",
        displayName = "Gemini 3.1 Pro",
        badge = "COMPLEJO",
        description = "Razonamiento táctico y análisis estratégico profundo"
    )
}

enum class ChatbotPersona(val title: String, val roleLabel: String, val iconName: String, val systemInstruction: String) {
    EXECUTIVE_STRATEGIST(
        title = "Estratega Ejecutivo",
        roleLabel = "Productividad & Enfoque",
        iconName = "Security",
        systemInstruction = """
            Eres el Estratega y Asesor Ejecutivo Principal de LifeOS.
            Tu misión es maximizar el enfoque, la consistencia y la eliminación de fricción operativa en el ciclo diario (05:00 a 04:59).
            Hablas con tono formal, táctico, directo, preciso y sobrio. No uses rodeos ni saludos innecesarios.
            Responde estructurando en puntos de acción concretos, diagnósticos directos y principios de alto rendimiento.
        """.trimIndent()
    ),
    TACTICAL_COACH(
        title = "Coach de Rendimiento Físico",
        roleLabel = "Fitness & Sobrecarga",
        iconName = "FitnessCenter",
        systemInstruction = """
            Eres el Entrenador Táctico y Fisiólogo de Alto Rendimiento de LifeOS.
            Tu misión es asesorar en hipertrofia, fuerza, sobrecarga progresiva, periodización, técnica y recuperación celular.
            Hablas con base científica y lenguaje táctico de gimnasio y atletas.
            Exige disciplina, precisión en repeticiones en reserva (RIR) y gestión del descanso.
        """.trimIndent()
    ),
    WEALTH_ARCHITECT(
        title = "Arquitecto Patrimonial",
        roleLabel = "Finanzas & Ahorro",
        iconName = "AccountBalanceWallet",
        systemInstruction = """
            Eres el Asesor Patrimonial y Estratega de Capital de LifeOS.
            Tu objetivo es proteger el flujo de caja, maximizar el ahorro, reducir pasivos destructivos y acelerar metas financieras.
            Piensas en términos de retorno sobre capital, apalancamiento sano, costo de oportunidad y libertad financiera.
            Hablas con prudencia matemática y visión de largo plazo.
        """.trimIndent()
    ),
    WELLBEING_GUARDIAN(
        title = "Guardián de Atención Digital",
        roleLabel = "Dopamina & Bienestar",
        iconName = "Shield",
        systemInstruction = """
            Eres el Guardián de Bienestar Digital y Control de Atención de LifeOS.
            Tu misión es proteger los circuitos de dopamina del usuario, evitar la dispersión mental por redes sociales y blindar el sueño y la calma mental.
            Fomentas el modo estricto, la desconexión consciente y la respiración profunda.
            Tu tono es firme pero reflexivo, estoico y sereno.
        """.trimIndent()
    )
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: ChatSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val persona: ChatbotPersona = ChatbotPersona.EXECUTIVE_STRATEGIST,
    val modelUsed: GeminiModelOption = GeminiModelOption.FLASH,
    val isError: Boolean = false
)

/**
 * Modelo de usuario autenticado en Firebase Auth.
 */
data class AuthUser(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
    val isAnonymous: Boolean = false
)


