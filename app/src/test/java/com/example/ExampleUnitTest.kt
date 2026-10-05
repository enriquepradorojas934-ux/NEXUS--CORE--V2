package com.example

import com.example.util.CurrencyFormatter
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testCurrencyFormattingCop() {
        // Pruébalo con estos montos: 100.000, 40.000.000 y 1.250.000.000
        val cop100k = CurrencyFormatter.format(100_000.0, "$", "COP")
        val cop40m = CurrencyFormatter.format(40_000_000.0, "$", "COP")
        val cop1_25b = CurrencyFormatter.format(1_250_000_000.0, "$", "COP")

        assertEquals("$100.000 COP", cop100k)
        assertEquals("$40.000.000 COP", cop40m)
        assertEquals("$1.250.000.000 COP", cop1_25b)

        // Centro de la dona para COP (sin código de moneda en la línea)
        // Montos menores a 1 millón se muestran completos: "$100.000"
        assertEquals("$100.000", CurrencyFormatter.formatCenterAmount(100_000.0, "$", "COP", isSpanish = true))
        // Montos >= 1M en español: "$40 M"
        assertEquals("$40 M", CurrencyFormatter.formatCenterAmount(40_000_000.0, "$", "COP", isSpanish = true))
        // Montos >= 1M en inglés: "$40M"
        assertEquals("$40M", CurrencyFormatter.formatCenterAmount(40_000_000.0, "$", "COP", isSpanish = false))
        // Montos >= 1B: "$1,25 B" (español) / "$1.25B" (inglés)
        assertEquals("$1,25 B", CurrencyFormatter.formatCenterAmount(1_250_000_000.0, "$", "COP", isSpanish = true))
        assertEquals("$1.25B", CurrencyFormatter.formatCenterAmount(1_250_000_000.0, "$", "COP", isSpanish = false))
    }

    @Test
    fun testCurrencyFormattingUsd() {
        val usd100k = CurrencyFormatter.format(100_000.0, "$", "USD")
        assertEquals("$100,000.00 USD", usd100k)
    }

    @Test
    fun testCurrencyFormattingEur() {
        val eur100k = CurrencyFormatter.format(100_000.0, "€", "EUR")
        assertEquals("100.000,00 € EUR", eur100k)
    }

    @Test
    fun testWorkoutDayIsolation() {
        val session = com.example.data.model.WorkoutSessionEntity(
            id = 1L,
            title = "Rutina 3 Días",
            targetGoal = "Fuerza",
            durationMinutes = 60,
            notes = "DAYS_CONFIG:LUNES · DÍA 1||Push##MIÉRCOLES · DÍA 2||Pull##VIERNES · DÍA 3||Legs\nNotas de rutina",
            cycleDate = "2026-10-05"
        )

        val exLunes1 = com.example.data.model.WorkoutExerciseEntity(
            id = 101L, sessionId = 1L, exerciseName = "Press de Banca", sets = 4, reps = "10", weightKg = 80.0, restSeconds = 90,
            notes = com.example.domain.WorkoutDayHelper.formatNotesWithDay("LUNES · DÍA 1", "Push")
        )
        val exLunes2 = com.example.data.model.WorkoutExerciseEntity(
            id = 102L, sessionId = 1L, exerciseName = "Press Militar", sets = 3, reps = "10", weightKg = 40.0, restSeconds = 60,
            notes = com.example.domain.WorkoutDayHelper.formatNotesWithDay("LUNES · DÍA 1", "Push")
        )
        val exMiercoles1 = com.example.data.model.WorkoutExerciseEntity(
            id = 103L, sessionId = 1L, exerciseName = "Dominadas", sets = 4, reps = "8", weightKg = 0.0, restSeconds = 90,
            notes = com.example.domain.WorkoutDayHelper.formatNotesWithDay("MIÉRCOLES · DÍA 2", "Pull")
        )
        val exMiercoles2 = com.example.data.model.WorkoutExerciseEntity(
            id = 104L, sessionId = 1L, exerciseName = "Remo con Barra", sets = 3, reps = "10", weightKg = 60.0, restSeconds = 60,
            notes = com.example.domain.WorkoutDayHelper.formatNotesWithDay("MIÉRCOLES · DÍA 2", "Pull")
        )
        val exViernes1 = com.example.data.model.WorkoutExerciseEntity(
            id = 105L, sessionId = 1L, exerciseName = "Sentadilla Libre", sets = 4, reps = "8", weightKg = 100.0, restSeconds = 120,
            notes = com.example.domain.WorkoutDayHelper.formatNotesWithDay("VIERNES · DÍA 3", "Legs")
        )

        val allExercises = listOf(exLunes1, exLunes2, exMiercoles1, exMiercoles2, exViernes1)
        val configuredDays = com.example.domain.WorkoutDayHelper.getConfiguredDays(session, allExercises)
        assertEquals(3, configuredDays.size)

        // Al abrir Lunes: SOLO ejercicios del Lunes
        val lunesExercises = com.example.domain.WorkoutDayHelper.getDayExercises(allExercises, configuredDays, "LUNES · DÍA 1 — Push")
        assertEquals(2, lunesExercises.size)
        assertEquals(listOf("Press de Banca", "Press Militar"), lunesExercises.map { it.exerciseName })

        // Al abrir Miércoles: SOLO ejercicios del Miércoles
        val miercolesExercises = com.example.domain.WorkoutDayHelper.getDayExercises(allExercises, configuredDays, "MIÉRCOLES · DÍA 2")
        assertEquals(2, miercolesExercises.size)
        assertEquals(listOf("Dominadas", "Remo con Barra"), miercolesExercises.map { it.exerciseName })

        // Al abrir Viernes: SOLO ejercicios del Viernes
        val viernesExercises = com.example.domain.WorkoutDayHelper.getDayExercises(allExercises, configuredDays, "VIERNES · DÍA 3")
        assertEquals(1, viernesExercises.size)
        assertEquals(listOf("Sentadilla Libre"), viernesExercises.map { it.exerciseName })
    }
}
