package com.example.domain

import com.example.data.model.DetectedGlobalEntry
import com.example.data.model.GlobalEntryType
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

data class ParsedTaskData(
    val title: String,
    val dueDate: String = "",
    val dueTime: String = "",
    val estimatedMinutes: Int = 30,
    val priority: String = "MEDIA",
    val category: String = "Trabajo",
    val repetitionType: String = "NUNCA",
    val repetitionDays: String = ""
)

data class ParsedFinancialData(
    val type: String, // INGRESO, GASTO
    val amount: Double,
    val category: String,
    val description: String
)

object NaturalLanguageEngine {

    /**
     * Parsea texto natural para crear tareas de forma predictiva.
     * Ejemplo: "Estudiar matemáticas mañana a las 5pm #estudio 45m !alta"
     */
    fun parseTask(rawText: String, currentCycleDate: String): ParsedTaskData {
        var text = rawText.trim()
        if (text.isBlank()) return ParsedTaskData(title = "")

        var priority = "MEDIA"
        var category = "Trabajo"
        var estimatedMinutes = 30
        var dueDate = ""
        var dueTime = ""
        var repetitionType = "NUNCA"
        var repetitionDays = ""

        // Prioridad
        if (Pattern.compile("(?i)(!alta|#alta|urgente|prioridad alta|muy importante)").matcher(text).find()) {
            priority = "ALTA"
            text = text.replace(Regex("(?i)(!alta|#alta|urgente|prioridad alta|muy importante)"), "")
        } else if (Pattern.compile("(?i)(!baja|#baja|prioridad baja|secundario)").matcher(text).find()) {
            priority = "BAJA"
            text = text.replace(Regex("(?i)(!baja|#baja|prioridad baja|secundario)"), "")
        } else if (Pattern.compile("(?i)(!media|#media|normal)").matcher(text).find()) {
            priority = "MEDIA"
            text = text.replace(Regex("(?i)(!media|#media|normal)"), "")
        }

        // Categoría con hashtag o palabras clave
        val hashCatMatch = Regex("#([a-zA-ZáéíóúÁÉÍÓÚñÑ]+)").find(text)
        if (hashCatMatch != null) {
            val catCandidate = hashCatMatch.groupValues[1].lowercase().replaceFirstChar { it.uppercase() }
            category = catCandidate
            text = text.replace(hashCatMatch.value, "")
        } else {
            when {
                Pattern.compile("(?i)(estudio|estudiar|universidad|examen|libro|leer)").matcher(text).find() -> category = "Estudio"
                Pattern.compile("(?i)(entrenar|gym|ejercicio|pesas|cardio|correr|workout)").matcher(text).find() -> category = "Fitness"
                Pattern.compile("(?i)(salud|médico|dentista|medicina|terapia)").matcher(text).find() -> category = "Salud"
                Pattern.compile("(?i)(dinero|banco|pago|comprar|factura|finanza)").matcher(text).find() -> category = "Finanzas"
                Pattern.compile("(?i)(casa|hogar|limpiar|compras|super)").matcher(text).find() -> category = "Personal"
            }
        }

        // Estimación de tiempo: "30m", "45 min", "1h", "2 horas", "90m"
        val hourMatch = Regex("(?i)(\\d+)\\s*(?:h|hora|horas)").find(text)
        val minMatch = Regex("(?i)(\\d+)\\s*(?:m|min|minutos)").find(text)
        if (hourMatch != null) {
            val hours = hourMatch.groupValues[1].toIntOrNull() ?: 1
            estimatedMinutes = hours * 60
            text = text.replace(hourMatch.value, "")
        } else if (minMatch != null) {
            estimatedMinutes = minMatch.groupValues[1].toIntOrNull() ?: 30
            text = text.replace(minMatch.value, "")
        }

        // Horas: "a las 5pm", "a las 17:00", "18:30", "9am"
        val timeRegex = Regex("(?i)(?:a\\s+las\\s+)?(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?")
        val timeMatch = timeRegex.find(text)
        if (timeMatch != null && (text.contains("las") || timeMatch.value.contains(":") || timeMatch.groupValues[3].isNotBlank())) {
            var hour = timeMatch.groupValues[1].toIntOrNull() ?: 9
            val minute = timeMatch.groupValues[2].toIntOrNull() ?: 0
            val amPm = timeMatch.groupValues[3].lowercase()
            if (amPm == "pm" && hour < 12) hour += 12
            if (amPm == "am" && hour == 12) hour = 0
            dueTime = String.format("%02d:%02d", hour, minute)
            text = text.replace(timeMatch.value, "")
        }

        // Fechas relativas
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()

        if (Pattern.compile("(?i)\\b(hoy)\\b").matcher(text).find()) {
            dueDate = if (currentCycleDate.isNotBlank()) currentCycleDate else sdf.format(cal.time)
            text = text.replace(Regex("(?i)\\b(hoy)\\b"), "")
        } else if (Pattern.compile("(?i)\\b(mañana)\\b").matcher(text).find()) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
            dueDate = sdf.format(cal.time)
            text = text.replace(Regex("(?i)\\b(mañana)\\b"), "")
        } else if (Pattern.compile("(?i)\\b(pasado\\s+mañana)\\b").matcher(text).find()) {
            cal.add(Calendar.DAY_OF_YEAR, 2)
            dueDate = sdf.format(cal.time)
            text = text.replace(Regex("(?i)\\b(pasado\\s+mañana)\\b"), "")
        } else {
            // Días de la semana
            val daysMap = mapOf(
                "lunes" to Calendar.MONDAY,
                "martes" to Calendar.TUESDAY,
                "miércoles" to Calendar.WEDNESDAY,
                "miercoles" to Calendar.WEDNESDAY,
                "jueves" to Calendar.THURSDAY,
                "viernes" to Calendar.FRIDAY,
                "sábado" to Calendar.SATURDAY,
                "sabado" to Calendar.SATURDAY,
                "domingo" to Calendar.SUNDAY
            )
            for ((dayName, dayConst) in daysMap) {
                if (Pattern.compile("(?i)\\b(el\\s+)?$dayName\\b").matcher(text).find()) {
                    while (cal.get(Calendar.DAY_OF_WEEK) != dayConst) {
                        cal.add(Calendar.DAY_OF_YEAR, 1)
                    }
                    dueDate = sdf.format(cal.time)
                    text = text.replace(Regex("(?i)\\b(el\\s+)?$dayName\\b"), "")
                    break
                }
            }
        }

        // Repetición
        if (Pattern.compile("(?i)(todos los días|diariamente|cada día)").matcher(text).find()) {
            repetitionType = "DIARIO"
            text = text.replace(Regex("(?i)(todos los días|diariamente|cada día)"), "")
        } else if (Pattern.compile("(?i)(cada semana|semanalmente)").matcher(text).find()) {
            repetitionType = "SEMANAL"
            text = text.replace(Regex("(?i)(cada semana|semanalmente)"), "")
        }

        // Limpieza final del título
        var cleanedTitle = text
            .replace(Regex("(?i)\\b(por la mañana|por la tarde|por la noche)\\b"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
            .trim(',', '.', '-', ';')
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

        if (cleanedTitle.isBlank()) cleanedTitle = rawText.trim()

        return ParsedTaskData(
            title = cleanedTitle,
            dueDate = dueDate,
            dueTime = dueTime,
            estimatedMinutes = estimatedMinutes,
            priority = priority,
            category = category,
            repetitionType = repetitionType,
            repetitionDays = repetitionDays
        )
    }

    /**
     * Parsea transacciones financieras en lenguaje natural.
     * Ejemplo: "Hoy gasté 8.000 pesos en comida" -> Gasto, 8000.0, Comida
     * "Hoy recibí 500.000 pesos por mi trabajo" -> Ingreso, 500000.0, Trabajo
     */
    fun parseFinance(rawText: String): ParsedFinancialData {
        val text = rawText.trim()
        val isIncome = Pattern.compile("(?i)(recibí|cobré|ingreso|gané|sueldo|salario|pago recibido|abono)").matcher(text).find()
        val type = if (isIncome) "INGRESO" else "GASTO"

        // Extraer números: e.g. 8.000, 15000, 500.000, $50, 12.50
        val amountRegex = Regex("(?:\\$|pesos|usd|eur)?\\s*(\\d{1,3}(?:[.,]\\d{3})*(?:[.,]\\d+)?|\\d+)")
        val match = amountRegex.find(text)
        var amount = 0.0
        if (match != null) {
            val numStr = match.groupValues[1]
                .replace(".", "")
                .replace(",", ".")
            amount = numStr.toDoubleOrNull() ?: 0.0
        }

        // Categoría
        var category = if (isIncome) "Trabajo" else "Varios"
        when {
            Pattern.compile("(?i)(comida|almuerzo|cena|desayuno|supermercado|restaurante|cafe|café|uber eats)").matcher(text).find() -> category = "Alimentación"
            Pattern.compile("(?i)(transporte|uber|taxi|metro|gasolina|peaje|bus|nafta)").matcher(text).find() -> category = "Transporte"
            Pattern.compile("(?i)(alquiler|renta|luz|agua|gas|internet|servicio|casa)").matcher(text).find() -> category = "Vivienda"
            Pattern.compile("(?i)(salud|farmacia|medico|médico|dentista|remedio)").matcher(text).find() -> category = "Salud"
            Pattern.compile("(?i)(entretenimiento|cine|salida|juego|netflix|spotify|fiesta)").matcher(text).find() -> category = "Ocio"
            Pattern.compile("(?i)(estudio|curso|libro|capacitacion|capacitación)").matcher(text).find() -> category = "Educación"
            Pattern.compile("(?i)(sueldo|salario|nomina|nómina|freelance|honorario|cliente)").matcher(text).find() -> category = "Trabajo"
            Pattern.compile("(?i)(inversion|inversión|dividendo|interes|interés)").matcher(text).find() -> category = "Inversión"
        }

        return ParsedFinancialData(
            type = type,
            amount = amount,
            category = category,
            description = text
        )
    }

    /**
     * Entrada Global Multi-Módulo (Command Center):
     * Detecta múltiples intenciones en una sola entrada.
     * Ejemplo: "Hoy entrené una hora, estudié programación dos horas y gasté 15.000 pesos en comida."
     */
    fun parseGlobalCommand(rawText: String): List<DetectedGlobalEntry> {
        val results = mutableListOf<DetectedGlobalEntry>()
        // Dividir por conectores y signos de puntuación
        val segments = rawText.split(Regex("(?i)[,;\\n]+|\\s+y\\s+|\\s+además\\s+|\\s+tambien\\s+|\\s+también\\s+"))
            .map { it.trim() }
            .filter { it.isNotBlank() }

        for (seg in segments) {
            when {
                // 1. Detección de Entrenamiento
                Pattern.compile("(?i)(entrené|entrenamiento|gym|gimnasio|pesas|rutina|ejercicio|cardio|correr|workout)").matcher(seg).find() -> {
                    var duration = 45
                    val hourMatch = Regex("(?i)(\\d+)\\s*(?:h|hora|horas)|una\\s+hora").find(seg)
                    val minMatch = Regex("(?i)(\\d+)\\s*(?:m|min|minutos)").find(seg)
                    if (hourMatch != null) {
                        duration = if (hourMatch.value.contains("una")) 60 else (hourMatch.groupValues[1].toIntOrNull() ?: 1) * 60
                    } else if (minMatch != null) {
                        duration = minMatch.groupValues[1].toIntOrNull() ?: 45
                    }
                    results.add(
                        DetectedGlobalEntry(
                            type = GlobalEntryType.WORKOUT,
                            title = "Sesión de Entrenamiento",
                            detail = "$duration min registrados",
                            numericValue = duration.toDouble(),
                            category = "Fitness"
                        )
                    )
                }

                // 2. Detección de Finanzas (Gasto o Ingreso)
                Pattern.compile("(?i)(gasté|gaste|pagué|pague|compré|compre|recibí|recibi|cobré|cobre|pesos|usd|dólares|dolares|\\$\\d+)").matcher(seg).find() -> {
                    val fin = parseFinance(seg)
                    if (fin.amount > 0) {
                        results.add(
                            DetectedGlobalEntry(
                                type = if (fin.type == "INGRESO") GlobalEntryType.TRANSACTION_INCOME else GlobalEntryType.TRANSACTION_EXPENSE,
                                title = "${fin.type}: ${fin.category}",
                                detail = "$${fin.amount.toInt()} (${fin.description.take(28)})",
                                numericValue = fin.amount,
                                category = fin.category
                            )
                        )
                    }
                }

                // 3. Detección de Tiempo / Enfoque / Estudio
                Pattern.compile("(?i)(estudié|estudie|estudio|trabajé|trabaje|trabajo|programé|programe|leí|lei|enfoque|concentrado|bloque)").matcher(seg).find() -> {
                    var duration = 60
                    val hourMatch = Regex("(?i)(\\d+)\\s*(?:h|hora|horas)|dos\\s+horas|una\\s+hora").find(seg)
                    val minMatch = Regex("(?i)(\\d+)\\s*(?:m|min|minutos)").find(seg)
                    if (hourMatch != null) {
                        duration = when {
                            hourMatch.value.contains("dos") -> 120
                            hourMatch.value.contains("una") -> 60
                            else -> (hourMatch.groupValues[1].toIntOrNull() ?: 1) * 60
                        }
                    } else if (minMatch != null) {
                        duration = minMatch.groupValues[1].toIntOrNull() ?: 60
                    }

                    var actName = "Estudio y Enfoque"
                    if (Pattern.compile("(?i)(programación|programar|código)").matcher(seg).find()) actName = "Programación"
                    else if (Pattern.compile("(?i)(leer|libro)").matcher(seg).find()) actName = "Lectura"
                    else if (Pattern.compile("(?i)(trabaj)").matcher(seg).find()) actName = "Trabajo Productivo"

                    results.add(
                        DetectedGlobalEntry(
                            type = GlobalEntryType.TIME_TRACKING,
                            title = actName,
                            detail = "$duration min de enfoque",
                            numericValue = duration.toDouble(),
                            category = "Trabajo"
                        )
                    )
                }

                // 4. Tarea por defecto si es acción futura
                Pattern.compile("(?i)(hacer|comprar|llamar|terminar|enviar|revisar|preparar|agendar|recordar)").matcher(seg).find() -> {
                    val task = parseTask(seg, "")
                    results.add(
                        DetectedGlobalEntry(
                            type = GlobalEntryType.TASK,
                            title = task.title,
                            detail = "Prioridad ${task.priority}, ${task.category}",
                            numericValue = task.estimatedMinutes.toDouble(),
                            category = task.category
                        )
                    )
                }
            }
        }

        return results
    }
}
