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
     * Si no tiene día asignado, retorna cadena vacía para evitar falsos positivos con Lunes.
     */
    fun getDayTitle(exercise: WorkoutExerciseEntity): String {
        val notes = exercise.notes
        val dayIdx = notes.indexOf(DAY_PREFIX)
        if (dayIdx != -1) {
            val after = notes.substring(dayIdx + DAY_PREFIX.length)
            val subIdx = after.indexOf(SUB_PREFIX)
            val noteEnd = after.indexOf(";;")
            val endIdx = when {
                subIdx != -1 && noteEnd != -1 -> minOf(subIdx, noteEnd)
                subIdx != -1 -> subIdx
                noteEnd != -1 -> noteEnd
                else -> after.length
            }
            return after.substring(0, endIdx).trim()
        }
        return ""
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
     * Comprueba si un ejercicio corresponde al día objetivo especificado (ej. Lunes vs Miércoles vs Viernes).
     * Evita mezclar ejercicios de distintos días.
     */
    fun matchesDay(exercise: WorkoutExerciseEntity, targetDayTitle: String): Boolean {
        if (targetDayTitle.isBlank()) return false
        val exDay = getDayTitle(exercise)
        if (exDay.isBlank()) return false

        val normEx = normalizeDayText(exDay)
        val normTarget = normalizeDayText(targetDayTitle)
        if (normEx == normTarget) return true

        // Comparar días de la semana: LUNES, MARTES, MIERCOLES, JUEVES, VIERNES, SABADO, DOMINGO
        val daysOfWeek = listOf("LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES", "SABADO", "DOMINGO")
        val exDayOfWeek = daysOfWeek.firstOrNull { normEx.contains(it) }
        val targetDayOfWeek = daysOfWeek.firstOrNull { normTarget.contains(it) }

        if (exDayOfWeek != null && targetDayOfWeek != null) {
            return exDayOfWeek == targetDayOfWeek
        }

        // Comparar por número de día (ej: DIA 1, DIA 2, etc.)
        val diaRegex = Regex("""DIA\s*(\d+)""")
        val exDiaNum = diaRegex.find(normEx)?.groupValues?.getOrNull(1)
        val targetDiaNum = diaRegex.find(normTarget)?.groupValues?.getOrNull(1)
        if (exDiaNum != null && targetDiaNum != null) {
            return exDiaNum == targetDiaNum
        }

        // Si uno tiene día de semana y el otro no, y ambos tienen número o palabras clave distintas
        val cleanWordsEx = normEx.split(" ", "·", "-", "—", "|", "/", ":").filter { it.isNotBlank() }
        val cleanWordsTarget = normTarget.split(" ", "·", "-", "—", "|", "/", ":").filter { it.isNotBlank() }
        val commonKeywords = cleanWordsEx.intersect(cleanWordsTarget.toSet())
            .filterNot { it in listOf("DE", "EL", "LA", "EN", "Y", "O", "DIA", "DIAS") }
        if (commonKeywords.isNotEmpty()) return true

        return normEx == normTarget
    }

    /**
     * Obtiene de forma estricta los ejercicios que pertenecen exclusivamente al día objetivo.
     * Si la rutina tiene múltiples días (ej. Lunes, Miércoles, Viernes), filtra solo los de ese día.
     */
    fun getDayExercises(
        allSessionExercises: List<WorkoutExerciseEntity>,
        configuredDays: List<Pair<String, String>>,
        targetDayTitle: String
    ): List<WorkoutExerciseEntity> {
        if (configuredDays.size <= 1) {
            return allSessionExercises
        }
        val targetDayIdx = configuredDays.indexOfFirst { matchesDayName(it.first, targetDayTitle) }
        val targetConfiguredPair = if (targetDayIdx != -1) configuredDays[targetDayIdx] else null
        val hasAnyDayTags = allSessionExercises.any { getDayTitle(it).isNotBlank() }

        return if (hasAnyDayTags) {
            allSessionExercises.filter { ex ->
                matchesDay(ex, targetDayTitle) || (targetConfiguredPair != null && matchesDay(ex, targetConfiguredPair.first))
            }
        } else {
            // Si los ejercicios no tienen notas con día (ej. rutinas antiguas), repartirlos equitativamente por día
            if (targetDayIdx != -1) {
                val chunkSize = (allSessionExercises.size + configuredDays.size - 1) / configuredDays.size
                val start = targetDayIdx * chunkSize
                val end = minOf(start + chunkSize, allSessionExercises.size)
                if (start < allSessionExercises.size) allSessionExercises.subList(start, end) else emptyList()
            } else {
                allSessionExercises.filter { matchesDay(it, targetDayTitle) }
            }
        }
    }

    /**
     * Compara dos nombres de días (ej: "LUNES · DÍA 1 — Push" vs "LUNES · DIA 1").
     */
    fun matchesDayName(day1: String, day2: String): Boolean {
        val n1 = normalizeDayText(day1)
        val n2 = normalizeDayText(day2)
        if (n1 == n2) return true

        val daysOfWeek = listOf("LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES", "SABADO", "DOMINGO")
        val d1OfWeek = daysOfWeek.firstOrNull { n1.contains(it) }
        val d2OfWeek = daysOfWeek.firstOrNull { n2.contains(it) }
        if (d1OfWeek != null && d2OfWeek != null) {
            return d1OfWeek == d2OfWeek
        }
        return n1.contains(n2) || n2.contains(n1)
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
            .filter { it.sessionId == session.id && getDayTitle(it).isNotBlank() }
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
