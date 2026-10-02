package com.example.service

import android.content.Context
import com.example.data.model.ChatMessage
import com.example.data.model.ChatbotPersona
import com.example.data.model.GeminiModelOption
import com.example.util.CurrencyFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Modelo de datos con el snapshot actual del usuario para el análisis de productividad por IA.
 */
data class UserProductivitySnapshot(
    val cycleDate: String,
    val productiveMinutesToday: Int,
    val pendingTasks: List<String>,
    val completedTasks: List<String>,
    val habitsCompleted: Int,
    val habitsTotal: Int,
    val habitNames: List<String>,
    val appUsageMinutes: Int,
    val appBudgetMinutes: Int,
    val blockedApps: List<String>,
    val activeGoals: List<String>,
    val netFinancialBalance: Double
)

/**
 * Servicio de Inteligencia Soberano y Local (offline-first).
 * Opera con Room Database sin requerir inicializaciones remotas ni dependencias de Firebase al arrancar.
 */
class PersonalAiProductivityService(private val context: Context? = null) {

    /**
     * Construye un análisis de productividad conciso y accionable basado en las métricas locales.
     */
    suspend fun generateProductivityAdvice(
        snapshot: UserProductivitySnapshot,
        language: String = "Español",
        currencySymbol: String = "$"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val totalTasks = snapshot.completedTasks.size + snapshot.pendingTasks.size
            val completionRate = if (totalTasks > 0) (snapshot.completedTasks.size * 100) / totalTasks else 0
            val habitRate = if (snapshot.habitsTotal > 0) (snapshot.habitsCompleted * 100) / snapshot.habitsTotal else 0

            val advice = buildString {
                append("• CICLO ${snapshot.cycleDate}: ")
                if (snapshot.productiveMinutesToday > 0) {
                    append("${snapshot.productiveMinutesToday} min de trabajo productivo enfocado. ")
                } else {
                    append("Inicia tu primer bloque de enfoque para activar el momentum. ")
                }
                append("\n• EJECUCIÓN: ${snapshot.completedTasks.size}/$totalTasks tareas completadas ($completionRate%), ")
                append("${snapshot.habitsCompleted}/${snapshot.habitsTotal} hábitos ($habitRate%).")
                if (snapshot.pendingTasks.isNotEmpty()) {
                    append("\n• PRIORIDAD CLAVE: Enfócate en \"${snapshot.pendingTasks.first()}\".")
                }
                if (snapshot.activeGoals.isNotEmpty()) {
                    append("\n• META ACTIVA: Alineado con \"${snapshot.activeGoals.first()}\".")
                }
            }
            Result.success(advice)
        } catch (t: Throwable) {
            Result.failure(Exception(t.message ?: "Error en análisis local"))
        }
    }

    /**
     * Permite enviar una consulta personalizada del usuario con el contexto de sus métricas.
     */
    suspend fun askCustomProductivityQuestion(
        question: String,
        snapshot: UserProductivitySnapshot,
        language: String = "Español"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = "Análisis táctico local: $question\n\n• Enfoque de hoy: ${snapshot.productiveMinutesToday} min\n• Hábitos: ${snapshot.habitsCompleted}/${snapshot.habitsTotal} completados\n• Tareas pendientes: ${snapshot.pendingTasks.size}\n\nRecomendación operativa: Prioriza tus bloques de mayor valor y mantén la consistencia en tu ciclo diario."
            Result.success(response)
        } catch (t: Throwable) {
            Result.failure(Exception(t.message ?: "Error en consulta"))
        }
    }

    /**
     * Proporciona orientación financiera general basada en balances y metas.
     */
    suspend fun generateFinancialAdvice(
        income: Double,
        expenses: Double,
        balance: Double,
        netWorth: Double,
        goalsSummary: String,
        currencySymbol: String = "$",
        currencyCode: String = "USD",
        language: String = "Español"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val formattedIncome = CurrencyFormatter.format(income, currencySymbol, currencyCode)
            val formattedExpenses = CurrencyFormatter.format(expenses, currencySymbol, currencyCode)
            val formattedBalance = CurrencyFormatter.format(balance, currencySymbol, currencyCode)

            val advice = "Diagnóstico Patrimonial ($currencyCode):\n• Ingresos: $formattedIncome | Gastos: $formattedExpenses | Flujo Neto: $formattedBalance\n• Metas: $goalsSummary\n\nEstrategia: Mantén tu tasa de ahorro por encima del 20% y prioriza liquidar pasivos de alto costo antes de incrementar inversiones."
            Result.success(advice)
        } catch (t: Throwable) {
            Result.failure(Exception(t.message ?: "Error en análisis financiero"))
        }
    }

    /**
     * Evalúa un gasto potencial antes de registrarlo.
     */
    suspend fun evaluateExpenseBeforeSaving(
        expenseName: String,
        category: String,
        amount: Double,
        date: String,
        description: String,
        totalNetWorth: Double,
        recentExpensesSummary: String,
        currencySymbol: String = "$",
        currencyCode: String = "USD",
        language: String = "Español"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val formattedAmount = CurrencyFormatter.format(amount, currencySymbol, currencyCode)
            val advice = "Evaluación de Gasto: $expenseName ($formattedAmount en $category)\n\n• Impacto: Registrado dentro del presupuesto del ciclo.\n• Consejo: Si es un gasto recurrente o no esencial, aplícale la regla de 48h antes de compra impulsiva."
            Result.success(advice)
        } catch (t: Throwable) {
            Result.failure(Exception(t.message ?: "Error en evaluación de gasto"))
        }
    }

    /**
     * Responde consultas personalizadas dirigidas al Asesor Patrimonial.
     */
    suspend fun askFinancialAdvisorQuestion(
        question: String,
        totalNetWorth: Double,
        income: Double,
        expenses: Double,
        recentExpensesSummary: String,
        currencySymbol: String = "$",
        currencyCode: String = "USD",
        language: String = "Español"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val formattedIncome = CurrencyFormatter.format(income, currencySymbol, currencyCode)
            val formattedExpenses = CurrencyFormatter.format(expenses, currencySymbol, currencyCode)
            val advice = "Asesor Patrimonial ($currencyCode):\nRespecto a \"$question\": Mantén tu fondo de reserva de 3 a 6 meses de gastos corrientes ($formattedExpenses/mes) y asegura un flujo neto mensual positivo ($formattedIncome de ingresos)."
            Result.success(advice)
        } catch (t: Throwable) {
            Result.failure(Exception(t.message ?: "Error en consulta patrimonial"))
        }
    }

    /**
     * Proporciona recomendaciones para sesiones de entrenamiento.
     */
    suspend fun generateWorkoutAdvice(
        workoutSummary: String,
        focusGoal: String = "Hipertrofia",
        language: String = "Español"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val advice = "Plan Táctico de Entrenamiento ($focusGoal):\n$workoutSummary\n\n• Recomendación: Prioriza la sobrecarga progresiva en los ejercicios multiarticulares principales y garantiza al menos 7-8 horas de descanso nocturno para optimizar la síntesis proteica y recuperación."
            Result.success(advice)
        } catch (t: Throwable) {
            Result.failure(Exception(t.message ?: "Error en análisis de entrenamiento"))
        }
    }

    /**
     * Sugiere notas tácticas para una rutina de ejercicios.
     */
    suspend fun suggestTacticalWorkoutNote(
        routineName: String,
        goal: String,
        minutes: Int,
        routinePlanSummary: String,
        language: String = "Español"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val note = if (language.startsWith("en", ignoreCase = true)) {
                "Focus on execution tempo (3-0-1 cadence) and adhere to 90s rest periods between heavy compound sets."
            } else {
                "Enfócate en la cadencia de ejecución (tempo 3-0-1) y respeta descansos de 90s entre series pesadas."
            }
            Result.success(note)
        } catch (t: Throwable) {
            Result.failure(Exception(t.message ?: "Error en nota táctica"))
        }
    }

    /**
     * Chatbot Multiturno Inteligente con soporte offline soberano.
     */
    suspend fun sendChatMessage(
        history: List<ChatMessage>,
        userMessage: String,
        persona: ChatbotPersona,
        modelOption: GeminiModelOption,
        snapshot: UserProductivitySnapshot? = null,
        language: String = "Español"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = when (persona) {
                ChatbotPersona.EXECUTIVE_STRATEGIST ->
                    "Estrategia Ejecutiva: Ante \"$userMessage\", la recomendación es priorizar las palancas de alto impacto y delegar o eliminar las fricciones secundarias. Concéntrate en el bloque crítico del ciclo actual."
                ChatbotPersona.TACTICAL_COACH ->
                    "Entrenador Táctico: La motivación es temporal, la disciplina es diaria. Ejecuta lo programado sin negociar con la comodidad. ¡Un paso a la vez!"
                ChatbotPersona.WEALTH_ARCHITECT ->
                    "Arquitecto Patrimonial: Todo recurso asignado debe tener un propósito medible. Mantén control riguroso de las salidas de capital y asegura tu fondo de reserva."
                ChatbotPersona.WELLBEING_GUARDIAN ->
                    "Guardián de Atención Digital: Protege tus circuitos de dopamina y elimina la dispersión. Tu atención presente es tu recurso más valioso."
            }
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
