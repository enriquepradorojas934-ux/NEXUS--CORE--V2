package com.example.util

/**
 * Diccionario y motor de localización tipo-seguro para LifeOS.
 * Soporta de forma completa Español e Inglés, y provee fallback en Inglés
 * para cualquier otro idioma seleccionado que aún no cuente con localización completa.
 */
object AppStrings {

    fun isSpanish(language: String): Boolean {
        return LocaleHelper.isSpanish(language)
    }

    fun get(key: String, language: String): String {
        val spanish = isSpanish(language)
        return when (key) {
            // Tabs
            "nav_hub" -> "Hub"
            "nav_time" -> if (spanish) "Tiempo" else "Time"
            "nav_habits" -> if (spanish) "Hábitos" else "Habits"
            "nav_goals" -> if (spanish) "Metas" else "Goals"
            "nav_finance" -> if (spanish) "Finanzas" else "Finance"

            // Common Actions
            "save" -> if (spanish) "Guardar" else "Save"
            "cancel" -> if (spanish) "Cancelar" else "Cancel"
            "confirm" -> if (spanish) "Confirmar" else "Confirm"
            "delete" -> if (spanish) "Eliminar" else "Delete"
            "edit" -> if (spanish) "Editar" else "Edit"
            "close" -> if (spanish) "Cerrar" else "Close"
            "add" -> if (spanish) "Añadir" else "Add"
            "select" -> if (spanish) "Seleccionar" else "Select"

            // Hub
            "hub_title" -> if (spanish) "Centro de Mando Ejecutivo" else "Executive Command Center"
            "hub_subtitle" -> if (spanish) "Extensión digital y control operativo" else "Digital extension and operational control"
            "hub_next_task" -> if (spanish) "SIGUIENTE TAREA PRIORITARIA" else "NEXT PRIORITY TASK"
            "hub_active_block" -> if (spanish) "BLOQUE ACTIVO" else "ACTIVE BLOCK"
            "hub_no_block" -> if (spanish) "Sin bloque activo programado" else "No active block scheduled"
            "hub_habits" -> if (spanish) "Hábitos de Hoy" else "Today's Habits"
            "hub_goals" -> if (spanish) "Panorama de Metas" else "Goals Overview"
            "hub_finance" -> if (spanish) "Resumen Financiero" else "Financial Summary"
            "hub_fitness" -> if (spanish) "Entrenamiento" else "Training"
            "hub_quick_actions" -> if (spanish) "Acciones Tácticas Rápidas" else "Tactical Quick Actions"

            // Metas
            "goals_title" -> if (spanish) "Metas de Alto Impacto" else "High Impact Goals"
            "goals_subtitle" -> if (spanish) "Sistema OKR y seguimiento de resultados clave" else "OKR system and key results tracking"
            "goals_milestones_section" -> if (spanish) "Hitos y Subobjetivos" else "Milestones & Sub-goals"
            "goals_add_milestone" -> if (spanish) "+ Agregar hito" else "+ Add milestone"
            "goals_create_title" -> if (spanish) "Crear Nueva Meta" else "Create New Goal"
            "goals_name_label" -> if (spanish) "Nombre de la meta" else "Goal Name"
            "goals_desc_label" -> if (spanish) "Descripción o propósito" else "Description or purpose"
            "goals_cat_label" -> if (spanish) "Categoría de meta" else "Goal Category"
            "goals_date_label" -> if (spanish) "Fecha objetivo" else "Target Date"

            // Tiempo
            "time_title" -> if (spanish) "Gestión del Tiempo" else "Time Management"
            "time_subtitle" -> if (spanish) "Bloques de foco profundo, tareas y temporizador ejecutivo" else "Deep work blocks, tasks, and executive timer"
            "time_create_block_title" -> if (spanish) "Crear Bloque de Tiempo" else "Create Time Block"
            "time_block_activity" -> if (spanish) "Actividad o Enfoque" else "Activity or Focus"
            "time_block_start" -> if (spanish) "Inicio" else "Start"
            "time_block_end" -> if (spanish) "Fin" else "End"
            "time_block_category" -> if (spanish) "Categoría" else "Category"
            "time_save_block" -> if (spanish) "Guardar Bloque" else "Save Block"

            // Fitness
            "fitness_title" -> if (spanish) "Módulo de Entrenamiento" else "Training Module"
            "fitness_subtitle" -> if (spanish) "Sobrecarga progresiva y rendimiento físico" else "Progressive overload and physical performance"
            "fitness_dialog_title" -> if (spanish) "Registrar Sesión de Entrenamiento" else "Log Training Session"
            "fitness_routine_name" -> if (spanish) "Nombre de la rutina" else "Routine Name"
            "fitness_goal_label" -> if (spanish) "Objetivo" else "Goal"
            "fitness_minutes_label" -> if (spanish) "Minutos" else "Minutes"
            "fitness_tactical_notes" -> if (spanish) "Notas tácticas" else "Tactical Notes"
            "fitness_ai_suggest" -> if (spanish) "Sugerencia IA" else "AI Suggestion"
            "fitness_add_day" -> if (spanish) "+ Agregar día" else "+ Add Day"
            "fitness_add_exercise" -> if (spanish) "+ Agregar ejercicio" else "+ Add Exercise"
            "fitness_series" -> "SERIES"
            "fitness_reps" -> "REPS"
            "fitness_kg" -> "KG"
            "fitness_save_session" -> if (spanish) "Guardar Sesión" else "Save Session"

            // Finanzas
            "finance_title" -> if (spanish) "Finanzas Personales & Patrimonio" else "Personal Finance & Net Worth"
            "finance_subtitle" -> if (spanish) "Balance patrimonial en dona, gastos y flujo de caja" else "Donut asset balance, expenses, and cash flow"
            "finance_register_expense" -> if (spanish) "Registrar Gasto" else "Register Expense"
            "finance_net_flow" -> if (spanish) "Flujo Neto Actual" else "Net Cash Flow"
            "finance_income" -> if (spanish) "Ingresos Registrados" else "Recorded Income"
            "finance_expenses" -> if (spanish) "Total Gastos" else "Total Expenses"
            "finance_patrimonio" -> if (spanish) "PATRIMONIO" else "NET WORTH"

            // Settings
            "settings_title" -> if (spanish) "Configuración del Sistema" else "System Settings"
            "settings_subtitle" -> if (spanish) "Configuración global, regionalización y respaldo soberano" else "Global configuration, localization, and sovereign backup"
            "settings_regional_header" -> if (spanish) "MONEDA E IDIOMA" else "CURRENCY & LANGUAGE"
            "settings_regional_desc" -> if (spanish) "Personaliza la divisa para tus finanzas y el idioma preferido para tu sistema ejecutivo." else "Customize currency for your finances and preferred system language."
            "settings_currency_field" -> if (spanish) "Moneda de Finanzas" else "Finance Currency"
            "settings_language_field" -> if (spanish) "Idioma del Sistema" else "System Language"

            else -> key
        }
    }
}
