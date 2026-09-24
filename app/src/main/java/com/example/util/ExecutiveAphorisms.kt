package com.example.util

import java.util.Calendar

data class ExecutiveQuote(
    val quote: String,
    val author: String,
    val focusArea: String
)

object ExecutiveAphorisms {
    private val quotes = listOf(
        ExecutiveQuote(
            quote = "No te elevas al nivel de tus metas, caes al nivel de tus sistemas.",
            author = "James Clear",
            focusArea = "Consistencia y Hábitos"
        ),
        ExecutiveQuote(
            quote = "No es que tengamos poco tiempo, sino que perdemos mucho.",
            author = "Séneca",
            focusArea = "Gestión del Tiempo"
        ),
        ExecutiveQuote(
            quote = "Primero dite a ti mismo qué serías; y luego haz lo que tengas que hacer.",
            author = "Epicteto",
            focusArea = "Identidad y Disciplina"
        ),
        ExecutiveQuote(
            quote = "Lo que se mide, se gestiona y se perfecciona. La eficacia es una práctica deliberada.",
            author = "Peter Drucker",
            focusArea = "Claridad Operativa"
        ),
        ExecutiveQuote(
            quote = "No te disperses: mantén tu atención plena en la tarea presente.",
            author = "Marco Aurelio",
            focusArea = "Enfoque Profundo"
        ),
        ExecutiveQuote(
            quote = "La disciplina es el puente entre las metas y los logros tangibles.",
            author = "Jim Rohn",
            focusArea = "Ejecución"
        ),
        ExecutiveQuote(
            quote = "La simplicidad es el requisito previo para la máxima fiabilidad.",
            author = "Edsger W. Dijkstra",
            focusArea = "Sistemas Eficientes"
        )
    )

    fun getTodayAphorism(): ExecutiveQuote {
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val index = (dayOfYear) % quotes.size
        return quotes[index]
    }
}
