package com.example.domain

import java.text.SimpleDateFormat
import java.util.*

object DailyCycleEngine {

    const val DEFAULT_CYCLE_START_TIME = "05:00"

    /**
     * Calcula la fecha del ciclo activo según la hora de inicio configurada (ej. 05:00).
     * Si la hora actual es antes de las 05:00, pertenece al ciclo que inició el día anterior.
     * Retorna una cadena con formato "YYYY-MM-DD".
     */
    fun getCurrentCycleDate(cycleStartTime: String = DEFAULT_CYCLE_START_TIME, currentTimeMillis: Long = System.currentTimeMillis()): String {
        val (startHour, startMinute) = parseHourMinute(cycleStartTime)
        val calendar = Calendar.getInstance().apply { timeInMillis = currentTimeMillis }

        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        val currentMinutesOfDay = currentHour * 60 + currentMinute
        val cycleStartMinutesOfDay = startHour * 60 + startMinute

        if (currentMinutesOfDay < cycleStartMinutesOfDay) {
            // El ciclo actual comenzó el día calendario de ayer a las 05:00
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(calendar.time)
    }

    /**
     * Retorna los milisegundos del próximo reinicio de ciclo (próximas 05:00).
     */
    fun getNextCycleResetMillis(cycleStartTime: String = DEFAULT_CYCLE_START_TIME, currentTimeMillis: Long = System.currentTimeMillis()): Long {
        val (startHour, startMinute) = parseHourMinute(cycleStartTime)
        val calendar = Calendar.getInstance().apply {
            timeInMillis = currentTimeMillis
            set(Calendar.HOUR_OF_DAY, startHour)
            set(Calendar.MINUTE, startMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (currentTimeMillis >= calendar.timeInMillis) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return calendar.timeInMillis
    }

    /**
     * Retorna el tiempo restante en formato amigable "Xh Ym" hasta el próximo reinicio de ciclo.
     */
    fun getTimeUntilNextCycle(cycleStartTime: String = DEFAULT_CYCLE_START_TIME): String {
        val diff = getNextCycleResetMillis(cycleStartTime) - System.currentTimeMillis()
        if (diff <= 0) return "0h 0m"
        val totalMinutes = diff / (1000 * 60)
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return "${hours}h ${minutes}m"
    }

    /**
     * Retorna el porcentaje del ciclo diario transcurrido (0.0 a 1.0) para visualización en dashboards.
     */
    fun getCycleProgress(cycleStartTime: String = DEFAULT_CYCLE_START_TIME): Float {
        val nextReset = getNextCycleResetMillis(cycleStartTime)
        val cycleDuration = 24 * 60 * 60 * 1000L
        val cycleStart = nextReset - cycleDuration
        val now = System.currentTimeMillis()
        val elapsed = (now - cycleStart).coerceIn(0L, cycleDuration)
        return elapsed.toFloat() / cycleDuration.toFloat()
    }

    private fun parseHourMinute(time: String): Pair<Int, Int> {
        return try {
            val parts = time.split(":")
            val h = parts[0].trim().toInt().coerceIn(0, 23)
            val m = if (parts.size > 1) parts[1].trim().toInt().coerceIn(0, 59) else 0
            Pair(h, m)
        } catch (_: Exception) {
            Pair(5, 0)
        }
    }
}
