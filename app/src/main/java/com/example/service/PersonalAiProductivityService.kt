package com.example.service

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.generationConfig
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
 * Servicio de IA de Productividad que utiliza la librería firebase-ai para consultar Gemini.
 */
class PersonalAiProductivityService(private val context: Context? = null) {

    // Modelo de lenguaje para tareas de texto básicas / razonamiento de productividad
    // De acuerdo con la guía oficial de modelos: gemini-3.5-flash
    private val modelName = "gemini-3.5-flash"

    private val generativeModel by lazy {
        val ai = Firebase.ai(backend = GenerativeBackend.googleAI())
        ai.generativeModel(
            modelName = modelName,
            generationConfig = generationConfig {
                temperature = 0.7f
                topK = 40
                topP = 0.95f
            }
        )
    }

    /**
     * Construye un prompt contextual con los datos operativos del usuario
     * y solicita a Gemini un análisis de productividad conciso y accionable.
     */
    suspend fun generateProductivityAdvice(snapshot: UserProductivitySnapshot): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val prompt = buildProductivityPrompt(snapshot)
                val response = generativeModel.generateContent(prompt)
                val responseText = response.text

                if (!responseText.isNullOrBlank()) {
                    Result.success(responseText.trim())
                } else {
                    Result.failure(Exception("Gemini no retornó contenido en la respuesta."))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Permite enviar una consulta personalizada del usuario con el contexto de sus métricas.
     */
    suspend fun askCustomProductivityQuestion(
        question: String,
        snapshot: UserProductivitySnapshot
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                Eres el asesor de productividad de un Sistema Operativo Personal.
                
                CONTEXTO ACTUAL DEL USUARIO:
                - Fecha de ciclo: ${snapshot.cycleDate}
                - Tiempo productivo enfocado: ${snapshot.productiveMinutesToday} minutos
                - Tareas pendientes (${snapshot.pendingTasks.size}): ${snapshot.pendingTasks.joinToString(", ").ifEmpty { "Ninguna" }}
                - Tareas completadas: ${snapshot.completedTasks.size}
                - Hábitos de hoy: ${snapshot.habitsCompleted}/${snapshot.habitsTotal} completados
                - Tiempo de apps en pantalla: ${snapshot.appUsageMinutes}m de ${snapshot.appBudgetMinutes}m de presupuesto
                - Metas activas: ${snapshot.activeGoals.joinToString("; ").ifEmpty { "Ninguna" }}
                
                PREGUNTA DEL USUARIO:
                "$question"
                
                Instrucciones:
                Responde en español de forma directa, motivadora, ejecutiva y estructurada (máximo 3 párrafos o puntos clave).
            """.trimIndent()

            val response = generativeModel.generateContent(prompt)
            val text = response.text
            if (!text.isNullOrBlank()) {
                Result.success(text.trim())
            } else {
                Result.failure(Exception("Respuesta vacía de Gemini."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildProductivityPrompt(snapshot: UserProductivitySnapshot): String {
        val screenTimeRatio = if (snapshot.appBudgetMinutes > 0) {
            (snapshot.appUsageMinutes.toFloat() / snapshot.appBudgetMinutes.toFloat() * 100).toInt()
        } else 0

        return """
            Actúa como un estratega y asesor de productividad ejecutivo de alto rendimiento para el "Sistema Operativo Personal".
            
            Analiza los datos reales del ciclo diario del usuario y genera un informe de productividad estructurado y altamente accionable:
            
            DATOS OPERATIVOS DEL CICLO (${snapshot.cycleDate}):
            1. TIEMPO Y ENFOQUE:
               - Minutos productivos registrados hoy: ${snapshot.productiveMinutesToday} min.
               - Tareas pendientes: ${if (snapshot.pendingTasks.isEmpty()) "¡Todas completadas!" else snapshot.pendingTasks.joinToString(", ")}
               - Tareas finalizadas hoy: ${snapshot.completedTasks.size}
               
            2. HÁBITOS:
               - Cumplimiento: ${snapshot.habitsCompleted} de ${snapshot.habitsTotal} completados.
               - Hábitos monitoreados: ${snapshot.habitNames.joinToString(", ")}
               
            3. ATENCIÓN Y BIENESTAR DIGITAL:
               - Uso de pantalla de ocio/apps: ${snapshot.appUsageMinutes} min de ${snapshot.appBudgetMinutes} min presupuestados ($screenTimeRatio%).
               - Apps actualmente bloqueadas por exceso de tiempo: ${if (snapshot.blockedApps.isEmpty()) "Ninguna" else snapshot.blockedApps.joinToString(", ")}
               
            4. METAS ESTRATÉGICAS:
               - Metas en curso: ${if (snapshot.activeGoals.isEmpty()) "Sin metas registradas" else snapshot.activeGoals.joinToString("; ")}
               
            5. BALANCE FINANCIERO DEL CICLO:
               - Balance neto: $${String.format("%.2f", snapshot.netFinancialBalance)}
               
            OBJETIVO DE TU RESPUESTA:
            Proporciona un análisis ejecutivo dividido en las siguientes 3 secciones breves con emojis sobrios:
            🎯 DIAGNÓSTICO RÁPIDO: Evalúa cómo va el ciclo (enfoque vs. distracción digital y consistencia de hábitos).
            ⚡ PRÓXIMA MEJOR ACCIÓN: 1 o 2 recomendaciones inmediatas y concretas para las próximas horas del ciclo.
            🛡️ CONTROL DE ATENCIÓN: Un consejo para proteger el foco y evitar fugas de energía o tiempo.
            
            Formato: Español, tono profesional pero cercano, sin relleno ni introducciones genéricas.
        """.trimIndent()
    }

    /**
     * IA para Entrenamiento: Analiza volumen, frecuencia, progresión y descansos.
     */
    suspend fun generateWorkoutAdvice(
        workoutsSummary: String,
        goal: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                Actúa como un entrenador personal táctico y científico para LifeOS.
                Objetivo físico: $goal
                Historial de sesiones y rutinas recientes:
                $workoutsSummary
                
                Analiza:
                1. Volumen y frecuencia de entrenamiento.
                2. Progresión de cargas y repeticiones.
                3. Recomendaciones específicas para la próxima sesión y optimización de descansos.
                
                Responde en español con formato conciso, táctico y motivador.
            """.trimIndent()
            val response = generativeModel.generateContent(prompt)
            val text = response.text
            if (!text.isNullOrBlank()) Result.success(text.trim())
            else Result.failure(Exception("Sin respuesta de IA"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * IA Financiera: Analiza gastos, ahorros, net worth y salud financiera.
     */
    suspend fun generateFinancialAdvice(
        income: Double,
        expenses: Double,
        balance: Double,
        netWorth: Double,
        goalsSummary: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                Actúa como un asesor patrimonial y estratega financiero personal de LifeOS.
                Datos financieros actuales:
                - Ingresos del período: $$income
                - Gastos: $$expenses
                - Balance neto: $$balance
                - Patrimonio neto estimado (Activos - Pasivos): $$netWorth
                - Metas de ahorro: $goalsSummary
                
                Proporciona:
                1. DIAGNÓSTICO FINANCIERO: Ratio de ahorro y salud del flujo de caja.
                2. OPTIMIZACIÓN DE PATRIMONIO: Cómo acelerar el cumplimiento de metas y reducir pasivos.
                3. ACCIÓN CONCRETA: Un ajuste práctico para esta semana.
                
                Responde en español, profesional, directo y al grano.
            """.trimIndent()
            val response = generativeModel.generateContent(prompt)
            val text = response.text
            if (!text.isNullOrBlank()) Result.success(text.trim())
            else Result.failure(Exception("Sin respuesta de IA"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Evalúa un gasto específico antes de su registro definitivo.
     * Considera el patrimonio total, los patrones de gasto registrados y el impacto del nuevo desembolso.
     */
    suspend fun evaluateExpenseBeforeSaving(
        expenseName: String,
        category: String,
        amount: Double,
        date: String,
        description: String,
        totalNetWorth: Double,
        recentExpensesSummary: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val netWorthPercent = if (totalNetWorth > 0) (amount / totalNetWorth * 100) else 0.0
            val prompt = """
                Actúa como el Asesor Financiero Ejecutivo y Estratega Patrimonial de LifeOS.
                El usuario está evaluando registrar el siguiente gasto y solicita tu diagnóstico antes de confirmar la compra:

                DETALLES DEL GASTO PROPUESTO:
                - Concepto / Nombre: $expenseName
                - Categoría: $category
                - Monto: $$amount (${String.format("%.1f", netWorthPercent)}% de su patrimonio total)
                - Fecha: $date
                - Motivo / Justificación dada por el usuario: "$description"

                CONTEXTO PATRIMONIAL DEL USUARIO:
                - Patrimonio Total Actual (Activos): $$totalNetWorth
                - Historial reciente de gastos: ${recentExpensesSummary.ifBlank { "Sin gastos previos registrados en el ciclo" }}

                INSTRUCCIONES DE RESPUESTA:
                Genera un análisis conciso, estratégico y objetivo estructurado exactamente con los siguientes puntos:
                1. IMPACTO PATRIMONIAL: Analiza el peso de este gasto frente al patrimonio total y la liquidez.
                2. PUNTOS A FAVOR (PROS): Beneficios reales o retorno (calidad de vida, utilidad, valor duradero) del desembolso según la justificación.
                3. PUNTOS EN CONTRA (CONTRAS / RIESGOS): Costo de oportunidad, alternativas más eficientes o señales de alerta.
                4. VEREDICTO EJECUTIVO: Recomendación clara sobre si proceder, postergar, negociar o buscar alternativas.

                Responde en español, tono ejecutivo, honesto y estructurado (máximo 4 párrafos breves o bullets claros).
            """.trimIndent()

            val response = generativeModel.generateContent(prompt)
            val text = response.text
            if (!text.isNullOrBlank()) Result.success(text.trim())
            else Result.failure(Exception("Sin respuesta de Gemini."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Responde preguntas financieras libres del usuario con el contexto de su patrimonio y gastos.
     */
    suspend fun askFinancialAdvisorQuestion(
        question: String,
        totalNetWorth: Double,
        income: Double,
        expenses: Double,
        recentExpensesSummary: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                Actúa como el Asesor Financiero y Consultor de Patrimonio de LifeOS.
                
                CONTEXTO FINANCIERO DEL USUARIO:
                - Patrimonio Total (Activos): $$totalNetWorth
                - Ingresos del ciclo: $$income
                - Gastos acumulados: $$expenses
                - Flujo neto: $${income - expenses}
                - Movimientos recientes: ${recentExpensesSummary.ifBlank { "Sin movimientos registrados" }}

                PREGUNTA O CONSULTA DEL USUARIO:
                "$question"

                INSTRUCCIONES:
                Responde en español de forma profesional, práctica, pedagógica y estructurada. Si el usuario pregunta por decisiones grandes (comprar una casa, vehículos, inversiones, protección patrimonial o ahorro), evalúa escenarios realistas basados en su situación patrimonial.
            """.trimIndent()

            val response = generativeModel.generateContent(prompt)
            val text = response.text
            if (!text.isNullOrBlank()) Result.success(text.trim())
            else Result.failure(Exception("Sin respuesta de Gemini."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Chatbot Multiturno Inteligente con Gemini:
     * - Mantiene el historial de conversación en una sola sesión conversacional.
     * - Aplica instrucciones del sistema personalizadas según el rol / persona seleccionada.
     * - Permite seleccionar el modelo de Gemini idóneo según la complejidad:
     *   * gemini-3.1-flash-lite-preview (tareas rápidas)
     *   * gemini-3.5-flash (tareas generales y estándar)
     *   * gemini-3.1-pro-preview (tareas complejas y razonamiento profundo)
     */
    suspend fun sendChatMessage(
        history: List<com.example.data.model.ChatMessage>,
        userMessage: String,
        persona: com.example.data.model.ChatbotPersona,
        modelOption: com.example.data.model.GeminiModelOption,
        snapshot: UserProductivitySnapshot? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val ai = Firebase.ai(backend = GenerativeBackend.googleAI())
            val model = ai.generativeModel(
                modelName = modelOption.modelId,
                generationConfig = generationConfig {
                    temperature = when (modelOption) {
                        com.example.data.model.GeminiModelOption.FLASH_LITE -> 0.5f
                        com.example.data.model.GeminiModelOption.FLASH -> 0.7f
                        com.example.data.model.GeminiModelOption.PRO -> 0.6f
                    }
                    topK = 40
                    topP = 0.95f
                },
                systemInstruction = com.google.firebase.ai.type.content {
                    text(persona.systemInstruction)
                }
            )

            // Construir el prompt multiturno completo con contexto operativo si está disponible
            val contextPrefix = snapshot?.let { snap ->
                """
                [CONTEXTO OPERATIVO DEL CICLO ACTUAL (${snap.cycleDate})]:
                - Minutos productivos hoy: ${snap.productiveMinutesToday}
                - Tareas pendientes: ${if (snap.pendingTasks.isEmpty()) "Ninguna" else snap.pendingTasks.joinToString(", ")}
                - Hábitos cumplidos: ${snap.habitsCompleted}/${snap.habitsTotal}
                - Tiempo ocio/pantalla: ${snap.appUsageMinutes}/${snap.appBudgetMinutes} min
                - Metas activas: ${if (snap.activeGoals.isEmpty()) "Sin metas" else snap.activeGoals.joinToString("; ")}
                - Balance financiero neto: $${String.format("%.2f", snap.netFinancialBalance)}
                ---
                """.trimIndent()
            } ?: ""

            val promptBuilder = StringBuilder()
            if (contextPrefix.isNotBlank()) {
                promptBuilder.append(contextPrefix).append("\n\n")
            }

            // Añadir los últimos turnos del historial
            val recentHistory = history.takeLast(10)
            if (recentHistory.isNotEmpty()) {
                promptBuilder.append("HISTORIAL PREVIO DE LA CONVERSACIÓN:\n")
                recentHistory.forEach { msg ->
                    val senderName = if (msg.sender == com.example.data.model.ChatSender.USER) "USUARIO" else "ASISTENTE (${persona.title})"
                    promptBuilder.append("$senderName: ${msg.text}\n")
                }
                promptBuilder.append("\n")
            }

            promptBuilder.append("NUEVO MENSAJE DEL USUARIO:\n$userMessage\n\n")
            promptBuilder.append("RESPUESTA DEL ASISTENTE (${persona.title}):")

            val response = model.generateContent(promptBuilder.toString())
            val text = response.text
            if (!text.isNullOrBlank()) {
                Result.success(text.trim())
            } else {
                Result.failure(Exception("Gemini no generó ninguna respuesta."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

