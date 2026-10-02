package com.example.ui

import com.example.data.model.*

enum class AppNavTab(val label: String) {
    DASHBOARD("Hub"),
    TIME("Tiempo"),
    HABITS("Hábitos"),
    GOALS("Metas"),
    FINANCE("Finanzas"),
    SETTINGS("Ajustes")
}

data class FocusTimerState(
    val isRunning: Boolean = false,
    val activityName: String = "Sesión de Enfoque",
    val category: String = "Trabajo",
    val elapsedSeconds: Long = 0L
)

data class AssociatedHabitItem(
    val habit: HabitEntity,
    val isCompletedToday: Boolean
)

data class GoalWithSubGoals(
    val goal: GoalEntity,
    val subGoals: List<SubGoalEntity>,
    val associatedHabits: List<AssociatedHabitItem> = emptyList(),
    val subGoalsProgress: Float = 0f,
    val habitsProgress: Float = 0f,
    val progressPercent: Float = 0f // 0.0 to 1.0
)

data class PersonalOsUiState(
    val activeTab: AppNavTab = AppNavTab.DASHBOARD,
    val cycleStartTime: String = "05:00",
    val currentCycleDate: String = "",
    val timeUntilNextCycle: String = "",
    val cycleProgress: Float = 0f,

    // Módulo Tiempo
    val tasks: List<TaskEntity> = emptyList(),
    val timeBlocks: List<TimeBlockEntity> = emptyList(),
    val timeEntriesToday: List<TimeEntryEntity> = emptyList(),
    val totalProductiveMinutesToday: Int = 0,
    val focusTimer: FocusTimerState = FocusTimerState(),

    // Módulo Hábitos
    val habits: List<HabitEntity> = emptyList(),
    val habitLogsToday: List<HabitLogEntity> = emptyList(),
    val habitsCompletedToday: Int = 0,
    val habitsTotalToday: Int = 0,
    val habitsCompletionRate: Float = 0f,
    val isHabitReminderEnabled: Boolean = false,
    val habitReminderTime: String = "20:00",

    // Módulo Metas
    val goalsWithSubGoals: List<GoalWithSubGoals> = emptyList(),
    val averageGoalsProgress: Float = 0f,

    // Módulo Finanzas
    val transactionsToday: List<TransactionEntity> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val netBalance: Double = 0.0,
    val financialGoals: List<FinancialGoalEntity> = emptyList(),
    val assetsAndDebts: List<AssetDebtEntity> = emptyList(),
    val totalAssets: Double = 0.0,
    val totalDebts: Double = 0.0,
    val netWorthHistory: List<NetWorthSnapshot> = emptyList(),

    // Módulo Entrenamiento / Fitness
    val workoutSessions: List<WorkoutSessionEntity> = emptyList(),
    val workoutExercises: List<WorkoutExerciseEntity> = emptyList(),
    val recentExercises: List<WorkoutExerciseEntity> = emptyList(),
    val isAnalyzingWorkout: Boolean = false,
    val aiWorkoutAdvice: String? = null,

    // Historial para Progresión RPG Táctica Persistente
    val allHabitLogs: List<HabitLogEntity> = emptyList(),

    // Módulo Sistema de Logros y Progresión RPG Táctico
    val userProgression: UserProgression = UserProgression(),
    val achievements: List<AchievementItem> = emptyList(),

    // Entrada Global de Texto (Multi-Módulo)
    val detectedGlobalEntries: List<DetectedGlobalEntry> = emptyList(),
    val isGlobalInputActive: Boolean = false,

    // IA Financiera
    val isAnalyzingFinance: Boolean = false,
    val aiFinancialAdvice: String? = null,
    val isEvaluatingExpense: Boolean = false,
    val expenseEvaluationAdvice: String? = null,
    val customFinanceAdvice: String? = null,
    val isAskingFinanceAdvisor: Boolean = false,

    val isStrictModeEnabled: Boolean = false,

    // Módulo Asesor IA (Firebase AI / Gemini)
    val isGeneratingAiAdvice: Boolean = false,
    val aiProductivityAdvice: String? = null,
    val aiAdviceError: String? = null,

    // Chatbot Multiturno Inteligente con Gemini
    val chatMessages: List<ChatMessage> = emptyList(),
    val isChatLoading: Boolean = false,
    val selectedChatPersona: ChatbotPersona = ChatbotPersona.EXECUTIVE_STRATEGIST,
    val selectedGeminiModel: GeminiModelOption = GeminiModelOption.FLASH,
    val chatError: String? = null,

    // Módulo Sincronización en la Nube (Cloud Firestore)
    val isSyncingCloud: Boolean = false,
    val lastCloudSyncTimestamp: Long? = null,
    val cloudSyncMessage: String? = null,
    val cloudSyncSuccess: Boolean = true,

    // Módulo Firebase Auth (Google Sign-In & Email/Anonymous)
    val currentUser: AuthUser? = null,
    val isAuthLoading: Boolean = false,
    val authError: String? = null,

    // Notificaciones / Mensajes
    val userMessage: String? = null,

    // Onboarding de Bienvenida (Primer inicio)
    val showOnboarding: Boolean = false,

    // Personalización del Tema
    val accentColorName: String = "Teal",

    // Resumen Diario Automático por IA (Hub)
    val dailyAiSummary: String? = null,
    val isGeneratingDailySummary: Boolean = false,

    // Configuración Global (Idioma y Moneda)
    val selectedLanguage: String = com.example.util.LocaleHelper.SYSTEM_DEFAULT,
    val selectedCurrencySymbol: String = "$",
    val selectedCurrencyCode: String = "USD"
)
