package com.example.util

import java.util.Calendar

data class ExecutiveQuote(
    val quote: String,
    val author: String,
    val focusArea: String
)

object ExecutiveAphorisms {
    private val quotesEs = listOf(
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

    private val quotesEn = listOf(
        ExecutiveQuote(
            quote = "You do not rise to the level of your goals. You fall to the level of your systems.",
            author = "James Clear",
            focusArea = "Consistency & Habits"
        ),
        ExecutiveQuote(
            quote = "It is not that we have a short time to live, but that we waste a lot of it.",
            author = "Seneca",
            focusArea = "Time Management"
        ),
        ExecutiveQuote(
            quote = "First say to yourself what you would be; and then do what you have to do.",
            author = "Epictetus",
            focusArea = "Identity & Discipline"
        ),
        ExecutiveQuote(
            quote = "What gets measured gets managed. Efficiency is a deliberate practice.",
            author = "Peter Drucker",
            focusArea = "Operational Clarity"
        ),
        ExecutiveQuote(
            quote = "Concentrate every minute like a Roman on doing what's in front of you.",
            author = "Marcus Aurelius",
            focusArea = "Deep Focus"
        ),
        ExecutiveQuote(
            quote = "Discipline is the bridge between goals and tangible accomplishment.",
            author = "Jim Rohn",
            focusArea = "Execution"
        ),
        ExecutiveQuote(
            quote = "Simplicity is prerequisite for reliability.",
            author = "Edsger W. Dijkstra",
            focusArea = "Efficient Systems"
        )
    )

    fun getTodayAphorism(language: String = "Español"): ExecutiveQuote {
        val quotes = if (AppStrings.isSpanish(language)) quotesEs else quotesEn
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val index = dayOfYear % quotes.size
        return quotes[index]
    }
}
