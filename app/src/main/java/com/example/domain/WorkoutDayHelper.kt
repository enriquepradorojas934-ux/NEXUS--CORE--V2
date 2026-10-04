package com.example.domain

import com.example.data.model.WorkoutExerciseEntity
import com.example.data.model.WorkoutSessionEntity

/**
 * Ayudante para gestión de días estructurados en rutinas de entrenamiento (ej: Lunes, Miércoles, Viernes).
 * Permite aislar los ejercicios por día, identificar si hoy es día de entrenamiento o descanso
 * según el ciclo operativo 05:00–04:59, y detectar el próximo día de entrenamiento.
 */
object WorkoutDayHelper {

    const val DAY_PREFIX = "DAY:"
    const val SUB_PREFIX = "|SUB:"
    const val DAYS_CONFIG_PREFIX = "DAYS_CONFIG:"

    /**
     * Codifica el título del día y subtítulo dentro del campo notes de WorkoutExerciseEntity.
     */
    fun formatNotesWithDay(dayTitle: String, daySubtitle: String, userNotes: String = ""): String {
        val base = "$DAY_PREFIX${dayTitle.trim()}$SUB_PREFIX${daySubtitle.trim()}"
        val cleanUser = userNotes.trim()
        return if (cleanUser.isNotBlank() && !cleanUser.startsWith(DAY_PREFIX)) {
            "$base ;; $cleanUser"
        } else {
            base
        }
    }

    /**
     * Extrae el título del día asignado a un ejercicio (ej: "LUNES · DIA 1").
     */
    fun getDayTitle(exercise: WorkoutExerciseEntity): String {
        val notes = exercise.notes
        if (notes.startsWith(DAY_PREFIX)) {
            val subIdx = notes.indexOf(SUB_PREFIX)
            val noteEnd = notes.indexOf(";;")
            val endIdx = when {
                subIdx != -1 -> subIdx
                noteEnd != -1 -> noteEnd
                else -> notes.length
            }
            return notes.substring(DAY_PREFIX.length, endIdx).trim()
        }
        return "LUNES · DIA 1"
    }

    /**
     * Extrae el subtítulo o grupo muscular del día (ej: "Push (pecho, hombros, tríceps)").
     */
    fun getDaySubtitle(exercise: WorkoutExerciseEntity): String {
        val notes = exercise.notes
        val subIdx = notes.indexOf(SUB_PREFIX)
        if (subIdx != -1) {
            val afterSub = notes.substring(subIdx + SUB_PREFIX.length)
            val noteEnd = afterSub.indexOf(";;")
            return if (noteEnd != -1) afterSub.substring(0, noteEnd).trim() else afterSub.trim()
        }
        return ""
    }

    /**
     * Normaliza un texto eliminando acentos y convirtiendo a mayúsculas para comparaciones robustas de días.
     */
    fun normalizeDayText(text: String): String {
        return text.uppercase()
            .replace("Á", "A")
            .replace("É", "E")
            .replace("Í", "I")
            .replace("Ó", "O")
            .replace("Ú", "U")
    }

    /**
     * Obtiene la lista ordenada de días configurados para una rutina: List<Pair<Título, Subtítulo>>.
     */
    fun getConfiguredDays(session: WorkoutSessionEntity, exercises: List<WorkoutExerciseEntity>): List<Pair<String, String>> {
        // 1. Intentar leer desde DAYS_CONFIG en session.notes
        val sNotes = session.notes
        if (sNotes.contains(DAYS_CONFIG_PREFIX)) {
            val startIdx = sNotes.indexOf(DAYS_CONFIG_PREFIX) + DAYS_CONFIG_PREFIX.length
            val endIdx = sNotes.indexOf("\n", startIdx).let { if (it != -1) it else sNotes.length }
            val rawConfig = sNotes.substring(startIdx, endIdx).trim()
            if (rawConfig.isNotBlank()) {
                val list = rawConfig.split("##").mapNotNull { item ->
                    val parts = item.split("||")
                    if (parts.isNotEmpty() && parts[0].isNotBlank()) {
                        Pair(parts[0].trim(), parts.getOrElse(1) { "" }.trim())
                    } else null
                }
                if (list.isNotEmpty()) return list
            }
        }

        // 2. Extraer días distintos presentes en los ejercicios de la sesión
        val fromExercises = exercises
            .filter { it.sessionId == session.id }
            .map { Pair(getDayTitle(it), getDaySubtitle(it)) }
            .distinctBy { it.first }

        if (fromExercises.isNotEmpty()) {
            return fromExercises
        }

        // 3. Fallback predeterminado si no hay días definidos
        return listOf(Pair("LUNES · DIA 1", "Push (pecho, hombros, tríceps)"))
    }

    /**
     * Busca el índice del día que corresponde a HOY según el ciclo operativo (05:00–04:59).
     * Retorna null si hoy no es un día de entrenamiento configurado (día de descanso).
     */
    fun findTodayDayIndex(days: List<Pair<String, String>>, cycleStartTime: String): Int? {
        val todayName = normalizeDayText(DailyCycleEngine.getCurrentCycleDayName(cycleStartTime))

        days.forEachIndexed { index, (title, _) ->
            val normTitle = normalizeDayText(title)
            if (normTitle.contains(todayName)) {
                return index
            }
        }
        return null
    }

    /**
     * Encuentra el próximo día de entrenamiento programado si hoy es día de descanso.
     */
    fun findNextWorkoutDay(days: List<Pair<String, String>>, cycleStartTime: String): Pair<String, String>? {
        if (days.isEmpty()) return null
        val dayOrder = listOf("LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES", "SABADO", "DOMINGO")
        val todayName = normalizeDayText(DailyCycleEngine.getCurrentCycleDayName(cycleStartTime))
        val todayIdx = dayOrder.indexOf(todayName).let { if (it != -1) it else 0 }

        for (offset in 1..7) {
            val checkDay = dayOrder[(todayIdx + offset) % 7]
            val match = days.firstOrNull { normalizeDayText(it.first).contains(checkDay) }
            if (match != null) {
                return match
            }
        }
        return days.firstOrNull()
    }
}
