package com.example.util

import java.util.Calendar

data class ExecutiveQuote(
    val quote: String,
    val author: String
)

object ExecutiveAphorisms {
    private val quotesEs = listOf(
        ExecutiveQuote("No te elevas al nivel de tus metas, caes al nivel de tus sistemas.", "James Clear"),
        ExecutiveQuote("No es que tengamos poco tiempo, sino que perdemos mucho.", "Séneca"),
        ExecutiveQuote("Primero dite a ti mismo qué serías; y luego haz lo que tengas que hacer.", "Epicteto"),
        ExecutiveQuote("Lo que se mide, se gestiona y se perfecciona. La eficacia es una práctica deliberada.", "Peter Drucker"),
        ExecutiveQuote("No te disperses: mantén tu atención plena en la tarea presente.", "Marco Aurelio"),
        ExecutiveQuote("La disciplina es el puente entre las metas y los logros tangibles.", "Jim Rohn"),
        ExecutiveQuote("La simplicidad es el requisito previo para la máxima fiabilidad.", "Edsger W. Dijkstra"),
        ExecutiveQuote("La excelencia no es un acto fortuito, sino un hábito constante.", "Aristóteles"),
        ExecutiveQuote("El hombre que mueve una montaña comienza cargando pequeñas piedras.", "Confucio"),
        ExecutiveQuote("El coraje es la gracia bajo presión continua.", "Ernest Hemingway"),
        ExecutiveQuote("La energía fluye hacia donde diriges tu atención sin distracciones.", "Tony Robbins"),
        ExecutiveQuote("Cuanto más difícil es la victoria, mayor es la satisfacción de vencer.", "Pelé"),
        ExecutiveQuote("Dominar a otros es fuerza; dominarte a ti mismo es verdadero poder.", "Lao-Tsé"),
        ExecutiveQuote("No esperes el momento perfecto: toma el momento y hazlo perfecto.", "George Bernard Shaw"),
        ExecutiveQuote("La consistencia supera al talento cuando el talento carece de constancia.", "Tim Notke"),
        ExecutiveQuote("El secreto para avanzar es simplemente comenzar.", "Mark Twain"),
        ExecutiveQuote("La mente es todo; en lo que piensas sostenidamente, te conviertes.", "Buda"),
        ExecutiveQuote("El éxito es la suma de pequeños esfuerzos repetidos día tras día.", "Robert Collier"),
        ExecutiveQuote("Trabaja duro en silencio; deja que tu progreso haga el ruido.", "Frank Ocean"),
        ExecutiveQuote("No cuentes los días, haz que cada uno de los días cuente.", "Muhammad Ali"),
        ExecutiveQuote("Quien tiene un porqué para vivir, puede soportar casi cualquier cómo.", "Friedrich Nietzsche"),
        ExecutiveQuote("La acción es la medida fundamental de toda inteligencia aplicada.", "Napoleon Hill"),
        ExecutiveQuote("La concentración absoluta es la clave maestra de cualquier maestría.", "Robert Greene"),
        ExecutiveQuote("Haz cada cosa en tu vida como si fuera la última.", "Marco Aurelio"),
        ExecutiveQuote("El mejor momento para plantar un árbol fue hace veinte años. El segundo mejor momento es hoy.", "Proverbio"),
        ExecutiveQuote("La suerte favorece a la mente preparada y disciplinada.", "Louis Pasteur"),
        ExecutiveQuote("Nunca confundas el movimiento con el verdadero progreso estratégico.", "Alfred Sloan"),
        ExecutiveQuote("Controla tus mañanas para gobernar con precisión tus días.", "Robin Sharma"),
        ExecutiveQuote("La victoria pertenece al jugador más perseverante del tablero.", "Napoleón Bonaparte"),
        ExecutiveQuote("Si no diseñas tu propio plan de vida, caerás en el plan de alguien más.", "Jim Rohn"),
        ExecutiveQuote("El verdadero poder radica en la calma durante la tormenta.", "Séneca")
    )

    private val quotesEn = listOf(
        ExecutiveQuote("You do not rise to the level of your goals. You fall to the level of your systems.", "James Clear"),
        ExecutiveQuote("It is not that we have a short time to live, but that we waste a lot of it.", "Seneca"),
        ExecutiveQuote("First say to yourself what you would be; and then do what you have to do.", "Epictetus"),
        ExecutiveQuote("What gets measured gets managed. Efficiency is a deliberate practice.", "Peter Drucker"),
        ExecutiveQuote("Concentrate every minute like a Roman on doing what's in front of you.", "Marcus Aurelius"),
        ExecutiveQuote("Discipline is the bridge between goals and tangible accomplishment.", "Jim Rohn"),
        ExecutiveQuote("Simplicity is prerequisite for reliability.", "Edsger W. Dijkstra"),
        ExecutiveQuote("We are what we repeatedly do. Excellence, then, is not an act, but a habit.", "Aristotle"),
        ExecutiveQuote("The man who moves a mountain begins by carrying away small stones.", "Confucius"),
        ExecutiveQuote("Courage is grace under pressure.", "Ernest Hemingway"),
        ExecutiveQuote("Energy flows where attention goes.", "Tony Robbins"),
        ExecutiveQuote("The more difficult the victory, the greater the happiness in winning.", "Pele"),
        ExecutiveQuote("Mastering others is strength. Mastering yourself is true power.", "Lao Tzu"),
        ExecutiveQuote("Do not wait for the perfect moment: take the moment and make it perfect.", "George Bernard Shaw"),
        ExecutiveQuote("Hard work beats talent when talent fails to work hard.", "Tim Notke"),
        ExecutiveQuote("The secret of getting ahead is getting started.", "Mark Twain"),
        ExecutiveQuote("The mind is everything. What you think you become.", "Buddha"),
        ExecutiveQuote("Success is the sum of small efforts, repeated day in and day out.", "Robert Collier"),
        ExecutiveQuote("Work hard in silence, let your success be your noise.", "Frank Ocean"),
        ExecutiveQuote("Don't count the days, make the days count.", "Muhammad Ali"),
        ExecutiveQuote("He who has a why to live can bear almost any how.", "Friedrich Nietzsche"),
        ExecutiveQuote("Action is the real measure of intelligence.", "Napoleon Hill"),
        ExecutiveQuote("Deep focus is the master key to greatness.", "Robert Greene"),
        ExecutiveQuote("Perform every act of your life as though it were the last.", "Marcus Aurelius"),
        ExecutiveQuote("The best time to plant a tree was 20 years ago. The second best time is now.", "Proverb"),
        ExecutiveQuote("Chance favors the prepared mind.", "Louis Pasteur"),
        ExecutiveQuote("Never confuse movement with action and progress.", "Alfred Sloan"),
        ExecutiveQuote("Own your morning, elevate your life.", "Robin Sharma"),
        ExecutiveQuote("Victory belongs to the most persevering.", "Napoleon Bonaparte"),
        ExecutiveQuote("If you don't design your own life plan, you'll fall into someone else's.", "Jim Rohn"),
        ExecutiveQuote("True power lies in staying calm amid chaos.", "Seneca")
    )

    fun getTodayAphorism(language: String = "Español"): ExecutiveQuote {
        val quotes = if (AppStrings.isSpanish(language)) quotesEs else quotesEn
        val cal = Calendar.getInstance()
        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
        val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
        // Combina día del año y día del mes para garantizar rotación diaria única
        val index = (dayOfYear + dayOfMonth) % quotes.size
        return quotes[index]
    }
}
