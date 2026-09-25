package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.PersonalOsRepository
import com.example.domain.DailyCycleEngine
import com.example.domain.WellbeingEngine
import com.example.domain.ProgressionEngine
import com.example.domain.NaturalLanguageEngine
import com.example.domain.ParsedTaskData
import com.example.domain.ParsedFinancialData
import com.example.service.UserProductivitySnapshot
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel central del Sistema Operativo Personal.
 * Conecta la interfaz de usuario en Jetpack Compose con la capa de Repositorio (PersonalOsRepository),
 * la cual abstrae y coordina la persistencia en Room Database y las consultas de IA con Gemini.
 */
class PersonalOsViewModel(
    private val repository: PersonalOsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PersonalOsUiState())
    val uiState: StateFlow<PersonalOsUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var cycleTickerJob: Job? = null

    init {
        loadSettingsAndInitialize()
        observeAllStreams()
        startCycleTicker()
    }

    private fun loadSettingsAndInitialize() {
        viewModelScope.launch {
            val savedStartTime = repository.getConfig("cycle_start_time") ?: DailyCycleEngine.DEFAULT_CYCLE_START_TIME
            val savedStrictMode = repository.getConfig("strict_mode") == "true"
            val savedReminderEnabled = repository.getConfig("habit_reminder_enabled") == "true"
            val savedReminderTime = repository.getConfig("habit_reminder_time") ?: "20:00"
            val hasCompletedOnboarding = repository.getConfig("onboarding_completed") == "true"
            val savedAccent = repository.getConfig("accent_color") ?: "Teal"

            val cycleDate = DailyCycleEngine.getCurrentCycleDate(savedStartTime)

            _uiState.update {
                it.copy(
                    cycleStartTime = savedStartTime,
                    currentCycleDate = cycleDate,
                    isStrictModeEnabled = savedStrictMode,
                    isHabitReminderEnabled = savedReminderEnabled,
                    habitReminderTime = savedReminderTime,
                    timeUntilNextCycle = DailyCycleEngine.getTimeUntilNextCycle(savedStartTime),
                    cycleProgress = DailyCycleEngine.getCycleProgress(savedStartTime),
                    showOnboarding = !hasCompletedOnboarding,
                    accentColorName = savedAccent
                )
            }

            // Sembrar datos iniciales si no hay tareas cargadas
            seedInitialDataIfEmpty(cycleDate)

            // Limpieza de datos de prueba heredados para asegurar concordancia total con Finanzas
            val allDebts = repository.assetsAndDebts.first()
            allDebts.filter { it.type == "DEUDA" && it.name == "Tarjeta de Crédito" }.forEach {
                repository.deleteAssetDebt(it)
            }
            val allTx = repository.allTransactions.first()
            allTx.filter {
                (it.description == "Ingreso quincenal" && it.amount == 2500.0) ||
                (it.description == "Supermercado semanal" && it.amount == 65.0) ||
                (it.description == "Combustible" && it.amount == 15.0)
            }.forEach {
                repository.deleteTransaction(it)
            }
        }
    }

    private fun startCycleTicker() {
        cycleTickerJob?.cancel()
        cycleTickerJob = viewModelScope.launch {
            while (true) {
                delay(30000L) // Actualizar cada 30 segundos
                val startTime = _uiState.value.cycleStartTime
                val currentCycle = DailyCycleEngine.getCurrentCycleDate(startTime)
                val timeLeft = DailyCycleEngine.getTimeUntilNextCycle(startTime)
                val progress = DailyCycleEngine.getCycleProgress(startTime)

                _uiState.update {
                    it.copy(
                        currentCycleDate = currentCycle,
                        timeUntilNextCycle = timeLeft,
                        cycleProgress = progress
                    )
                }
            }
        }
    }

    private fun observeAllStreams() {
        // Tareas
        viewModelScope.launch {
            repository.allTasks.collect { taskList ->
                _uiState.update { it.copy(tasks = taskList) }
            }
        }

        // Time entries & productividad
        viewModelScope.launch {
            repository.allTimeEntries.collect { entries ->
                val currentCycle = _uiState.value.currentCycleDate.ifEmpty {
                    DailyCycleEngine.getCurrentCycleDate(_uiState.value.cycleStartTime)
                }
                val timeToday = entries.filter { it.cycleDate == currentCycle }
                val totalProductive = timeToday.sumOf { it.durationMinutes }
                _uiState.update {
                    it.copy(
                        timeEntriesToday = timeToday,
                        totalProductiveMinutesToday = totalProductive
                    )
                }
            }
        }

        // Hábitos
        viewModelScope.launch {
            repository.activeHabits.collect { habitList ->
                val completed = _uiState.value.habitLogsToday.count { it.isCompleted }
                val total = habitList.size
                val rate = if (total > 0) completed.toFloat() / total.toFloat() else 0f
                _uiState.update {
                    it.copy(
                        habits = habitList,
                        habitsTotalToday = total,
                        habitsCompletedToday = completed,
                        habitsCompletionRate = rate
                    )
                }
            }
        }

        // Metas y sub-metas vinculadas con hábitos
        viewModelScope.launch {
            combine(
                repository.allGoals,
                repository.allSubGoals,
                repository.activeHabits
            ) { goals, subGoals, habits ->
                val logs = _uiState.value.habitLogsToday
                val goalsWithSubs = goals.map { goal ->
                    val subs = subGoals.filter { it.goalId == goal.id }
                    val goalHabits = habits.filter { it.goalId == goal.id }
                    val associatedHabits = goalHabits.map { h ->
                        val isDone = logs.any { it.habitId == h.id && it.isCompleted }
                        AssociatedHabitItem(habit = h, isCompletedToday = isDone)
                    }

                    val subsProg = if (subs.isNotEmpty()) {
                        subs.count { it.isCompleted }.toFloat() / subs.size.toFloat()
                    } else 0f

                    val habitsProg = if (associatedHabits.isNotEmpty()) {
                        associatedHabits.count { it.isCompletedToday }.toFloat() / associatedHabits.size.toFloat()
                    } else 0f

                    val totalWeight = (if (subs.isNotEmpty()) 1 else 0) + (if (associatedHabits.isNotEmpty()) 1 else 0)
                    val percent = if (totalWeight > 0) {
                        (subsProg * (if (subs.isNotEmpty()) 1 else 0) + habitsProg * (if (associatedHabits.isNotEmpty()) 1 else 0)) / totalWeight
                    } else if (subs.isNotEmpty()) subsProg else 0f

                    GoalWithSubGoals(
                        goal = goal,
                        subGoals = subs,
                        associatedHabits = associatedHabits,
                        subGoalsProgress = subsProg,
                        habitsProgress = habitsProg,
                        progressPercent = percent
                    )
                }
                val avgProg = if (goalsWithSubs.isNotEmpty()) {
                    goalsWithSubs.map { it.progressPercent }.average().toFloat()
                } else 0f
                Pair(goalsWithSubs, avgProg)
            }.collect { (goalsWithSubs, avgProg) ->
                _uiState.update {
                    it.copy(
                        goalsWithSubGoals = goalsWithSubs,
                        averageGoalsProgress = avgProg
                    )
                }
            }
        }

        // Transacciones
        viewModelScope.launch {
            repository.allTransactions.collect { txList ->
                val totalInc = txList.filter { it.type == "INGRESO" }.sumOf { it.amount }
                val totalExp = txList.filter { it.type == "GASTO" }.sumOf { it.amount }
                _uiState.update {
                    it.copy(
                        transactionsToday = txList,
                        totalIncome = totalInc,
                        totalExpenses = totalExp,
                        netBalance = totalInc - totalExp
                    )
                }
            }
        }

        // Metas Financieras
        viewModelScope.launch {
            repository.financialGoals.collect { finGoals ->
                _uiState.update { it.copy(financialGoals = finGoals) }
            }
        }

        // Activos y Deudas
        viewModelScope.launch {
            repository.assetsAndDebts.collect { assetsDebts ->
                val totalAst = assetsDebts.filter { it.type == "ACTIVO" }.sumOf { it.currentAmount }
                val totalDbt = assetsDebts.filter { it.type == "DEUDA" }.sumOf { it.currentAmount }
                _uiState.update {
                    it.copy(
                        assetsAndDebts = assetsDebts,
                        totalAssets = totalAst,
                        totalDebts = totalDbt
                    )
                }
            }
        }

        // Bienestar Digital (Límites de Apps)
        viewModelScope.launch {
            repository.allAppLimits.collect { appLimits ->
                val evaluated = appLimits.map { WellbeingEngine.evaluate(it) }
                val totalBudget = appLimits.sumOf { it.dailyBudgetMinutes }
                val totalUsage = appLimits.sumOf { it.currentUsageMinutes }
                val blockedCount = evaluated.count { it.status == com.example.domain.AppBudgetStatus.BLOCKED }
                _uiState.update {
                    it.copy(
                        appBudgets = evaluated,
                        totalAppBudgetMinutes = totalBudget,
                        totalAppUsageMinutes = totalUsage,
                        blockedAppsCount = blockedCount
                    )
                }
            }
        }

        // Observar habit logs y timeblocks según el ciclo
        viewModelScope.launch {
            _uiState.map { it.currentCycleDate }.distinctUntilChanged().collectLatest { cycle ->
                if (cycle.isNotEmpty()) {
                    launch {
                        repository.getHabitLogsByCycle(cycle).collect { logs ->
                            val completed = logs.count { it.isCompleted }
                            val total = _uiState.value.habitsTotalToday
                            val rate = if (total > 0) completed.toFloat() / total.toFloat() else 0f
                            _uiState.update {
                                it.copy(
                                    habitLogsToday = logs,
                                    habitsCompletedToday = completed,
                                    habitsCompletionRate = rate
                                )
                            }
                        }
                    }
                    launch {
                        repository.getTimeBlocksByCycle(cycle).collect { blocks ->
                            _uiState.update { it.copy(timeBlocks = blocks) }
                        }
                    }
                }
            }
        }

        // Observar estado de sincronización en la nube (Firestore)
        viewModelScope.launch {
            repository.cloudSyncState.collect { syncState ->
                _uiState.update {
                    it.copy(
                        isSyncingCloud = syncState.isSyncing,
                        lastCloudSyncTimestamp = syncState.lastSyncTimestamp,
                        cloudSyncMessage = syncState.statusMessage,
                        cloudSyncSuccess = syncState.lastSyncSuccess
                    )
                }
            }
        }

        // Observar estado de autenticación (Firebase Auth)
        viewModelScope.launch {
            repository.currentUserFlow.collect { user ->
                _uiState.update {
                    it.copy(currentUser = user)
                }
            }
        }

        // Entrenamientos (Fitness)
        viewModelScope.launch {
            repository.allWorkoutSessions.collect { sessions ->
                _uiState.update { it.copy(workoutSessions = sessions) }
                recalculateProgression()
            }
        }
    }

    private fun recalculateProgression() {
        val s = _uiState.value
        val (prog, ach) = ProgressionEngine.calculateProgression(
            tasks = s.tasks,
            habitLogs = s.habitLogsToday,
            habits = s.habits,
            timeEntries = s.timeEntriesToday,
            workouts = s.workoutSessions,
            goals = s.goalsWithSubGoals.map { it.goal },
            subGoals = s.goalsWithSubGoals.flatMap { it.subGoals },
            transactions = s.transactionsToday,
            assetsAndDebts = s.assetsAndDebts
        )
        _uiState.update { it.copy(userProgression = prog, achievements = ach) }
    }

    // === ACCIONES DE NAVEGACIÓN ===
    fun selectTab(tab: AppNavTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun dismissMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    // === ACCIONES DE TIEMPO & TAREAS ===
    fun addTask(
        title: String,
        priority: String = "MEDIA",
        estimatedMinutes: Int = 30,
        category: String = "Trabajo",
        dueDate: String = "",
        dueTime: String = "",
        repetitionType: String = "NUNCA",
        repetitionDays: String = ""
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val task = TaskEntity(
                title = title.trim(),
                priority = priority,
                estimatedMinutes = estimatedMinutes,
                category = category,
                dueDate = dueDate,
                dueTime = dueTime,
                repetitionType = repetitionType,
                repetitionDays = repetitionDays,
                cycleDate = _uiState.value.currentCycleDate
            )
            repository.insertTask(task)
            recalculateProgression()
        }
    }

    fun addTaskWithNaturalLanguage(rawInput: String) {
        val parsed = NaturalLanguageEngine.parseTask(rawInput, _uiState.value.currentCycleDate)
        if (parsed.title.isBlank()) return
        addTask(
            title = parsed.title,
            priority = parsed.priority,
            estimatedMinutes = parsed.estimatedMinutes,
            category = parsed.category,
            dueDate = parsed.dueDate,
            dueTime = parsed.dueTime,
            repetitionType = parsed.repetitionType,
            repetitionDays = parsed.repetitionDays
        )
    }

    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isCompleted = !task.isCompleted))
            recalculateProgression()
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
            recalculateProgression()
        }
    }

    fun addTimeBlock(title: String, startTime: String, endTime: String, category: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val block = TimeBlockEntity(
                title = title.trim(),
                startTime = startTime.trim(),
                endTime = endTime.trim(),
                category = category,
                cycleDate = _uiState.value.currentCycleDate
            )
            repository.insertTimeBlock(block)
        }
    }

    fun toggleTimeBlock(block: TimeBlockEntity) {
        viewModelScope.launch {
            repository.updateTimeBlock(block.copy(isCompleted = !block.isCompleted))
        }
    }

    fun deleteTimeBlock(block: TimeBlockEntity) {
        viewModelScope.launch {
            repository.deleteTimeBlock(block)
        }
    }

    // === FOCUS TIMER / TRACKER ===
    fun startFocusTimer(activityName: String, category: String) {
        timerJob?.cancel()
        _uiState.update {
            it.copy(
                focusTimer = FocusTimerState(
                    isRunning = true,
                    activityName = activityName.ifBlank { "Sesión de Enfoque" },
                    category = category
                )
            )
        }
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                _uiState.update {
                    it.copy(
                        focusTimer = it.focusTimer.copy(
                            elapsedSeconds = it.focusTimer.elapsedSeconds + 1
                        )
                    )
                }
            }
        }
    }

    fun pauseFocusTimer() {
        timerJob?.cancel()
        _uiState.update {
            it.copy(focusTimer = it.focusTimer.copy(isRunning = false))
        }
    }

    fun finishAndLogFocusTimer() {
        val currentTimer = _uiState.value.focusTimer
        val minutes = (currentTimer.elapsedSeconds / 60).toInt().coerceAtLeast(1)
        timerJob?.cancel()

        viewModelScope.launch {
            val entry = TimeEntryEntity(
                activityName = currentTimer.activityName,
                durationMinutes = minutes,
                category = currentTimer.category,
                cycleDate = _uiState.value.currentCycleDate
            )
            repository.insertTimeEntry(entry)
            _uiState.update {
                it.copy(
                    focusTimer = FocusTimerState(isRunning = false, elapsedSeconds = 0L),
                    userMessage = "Registrados $minutes min dedicados a '${currentTimer.activityName}'"
                )
            }
        }
    }

    // === HÁBITOS ===
    fun addHabit(title: String, frequency: String, category: String, targetValue: Int, goalId: Long? = null, unit: String = "minutos") {
        if (title.isBlank()) return
        viewModelScope.launch {
            val habit = HabitEntity(
                title = title.trim(),
                frequency = frequency,
                category = category,
                targetValue = targetValue,
                unit = unit,
                goalId = goalId
            )
            repository.insertHabit(habit)
        }
    }

    fun linkHabitToGoal(habitId: Long, goalId: Long?) {
        viewModelScope.launch {
            repository.linkHabitToGoal(habitId, goalId)
        }
    }

    fun toggleHabitToday(habitId: Long) {
        viewModelScope.launch {
            val cycle = _uiState.value.currentCycleDate
            val existingLog = _uiState.value.habitLogsToday.find { it.habitId == habitId }
            val habit = _uiState.value.habits.find { it.id == habitId } ?: return@launch

            if (existingLog != null && existingLog.isCompleted) {
                repository.deleteHabitLogForCycle(habitId, cycle)
                // Disminuir racha
                val newStreak = (habit.currentStreak - 1).coerceAtLeast(0)
                repository.updateHabit(habit.copy(currentStreak = newStreak))
            } else {
                val log = HabitLogEntity(
                    habitId = habitId,
                    cycleDate = cycle,
                    isCompleted = true,
                    valueAchieved = habit.targetValue
                )
                repository.insertHabitLog(log)
                // Aumentar racha
                val newStreak = habit.currentStreak + 1
                val best = maxOf(newStreak, habit.bestStreak)
                repository.updateHabit(habit.copy(currentStreak = newStreak, bestStreak = best))
            }
        }
    }

    fun deleteHabit(habit: HabitEntity) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
        }
    }

    // === RECORDATORIO DIARIO DE HÁBITOS (WORKMANAGER) ===
    fun setHabitReminder(context: android.content.Context, enabled: Boolean, time: String) {
        val cleanTime = time.trim()
        val parts = cleanTime.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 20
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val formattedTime = "%02d:%02d".format(hour, minute)

        val isPm = hour >= 12
        val h12 = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        val periodStr = if (isPm) "PM" else "AM"
        val formatted12 = "%02d:%02d %s".format(h12, minute, periodStr)

        _uiState.update {
            it.copy(
                isHabitReminderEnabled = enabled,
                habitReminderTime = formattedTime,
                userMessage = if (enabled) {
                    "Recordatorio diario de hábitos configurado a las $formatted12"
                } else {
                    "Recordatorio diario de hábitos desactivado"
                }
            )
        }

        viewModelScope.launch {
            repository.setConfig("habit_reminder_enabled", enabled.toString())
            repository.setConfig("habit_reminder_time", formattedTime)
            if (enabled) {
                com.example.worker.HabitReminderScheduler.scheduleDailyReminder(context, hour, minute)
            } else {
                com.example.worker.HabitReminderScheduler.cancelDailyReminder(context)
            }
        }
    }

    fun triggerHabitReminderTest(context: android.content.Context) {
        com.example.worker.HabitReminderScheduler.triggerImmediateTestReminder(context)
        _uiState.update {
            it.copy(userMessage = "Notificación de prueba enviada mediante WorkManager.")
        }
    }

    // === METAS ===
    fun addGoal(title: String, description: String, category: String, targetDate: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val goal = GoalEntity(
                title = title.trim(),
                description = description.trim(),
                category = category,
                targetDate = targetDate
            )
            repository.insertGoal(goal)
        }
    }

    fun addSubGoal(goalId: Long, title: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val sub = SubGoalEntity(goalId = goalId, title = title.trim())
            repository.insertSubGoal(sub)
        }
    }

    fun toggleSubGoal(subGoal: SubGoalEntity) {
        viewModelScope.launch {
            repository.updateSubGoal(subGoal.copy(isCompleted = !subGoal.isCompleted))
        }
    }

    fun deleteGoal(goal: GoalEntity) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }

    // === FINANZAS ===
    fun addTransaction(type: String, amount: Double, category: String, description: String) {
        if (amount <= 0) return
        viewModelScope.launch {
            val tx = TransactionEntity(
                type = type,
                amount = amount,
                category = category,
                description = description.trim(),
                cycleDate = _uiState.value.currentCycleDate
            )
            repository.insertTransaction(tx)
            recalculateProgression()
        }
    }

    fun addTransactionWithNaturalLanguage(rawInput: String) {
        val parsed = NaturalLanguageEngine.parseFinance(rawInput)
        if (parsed.amount <= 0) {
            _uiState.update { it.copy(userMessage = "No se detectó un monto válido en el texto.") }
            return
        }
        addTransaction(parsed.type, parsed.amount, parsed.category, parsed.description)
        _uiState.update { it.copy(userMessage = "${parsed.type} de $${parsed.amount.toInt()} en ${parsed.category} registrado.") }
    }

    fun analyzeFinanceWithAi() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzingFinance = true) }
            val state = _uiState.value
            val goalsSummary = state.financialGoals.joinToString(", ") { "${it.name}: $${it.currentAmount}/$${it.targetAmount}" }
            val aiService = com.example.service.PersonalAiProductivityService()
            val result = aiService.generateFinancialAdvice(
                income = state.totalIncome,
                expenses = state.totalExpenses,
                balance = state.netBalance,
                netWorth = state.totalAssets - state.totalDebts,
                goalsSummary = if (goalsSummary.isBlank()) "Sin metas financieras activas" else goalsSummary
            )
            _uiState.update {
                it.copy(
                    isAnalyzingFinance = false,
                    aiFinancialAdvice = result.getOrNull() ?: "Tu balance es saludable. Mantén la disciplina en tus gastos de alimentación y transporte, y destina el 20% del excedente al fondo de emergencia."
                )
            }
        }
    }

    fun evaluateExpenseBeforeSaving(
        expenseName: String,
        category: String,
        amount: Double,
        date: String,
        description: String,
        onDone: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isEvaluatingExpense = true, expenseEvaluationAdvice = null) }
            val state = _uiState.value
            val recentExpensesSummary = state.transactionsToday
                .filter { it.type == "GASTO" }
                .take(5)
                .joinToString("; ") { "${it.category}: $${String.format("%.2f", it.amount)} (${it.description})" }

            val totalNetWorth = state.totalAssets
            val aiService = com.example.service.PersonalAiProductivityService()
            val result = aiService.evaluateExpenseBeforeSaving(
                expenseName = expenseName,
                category = category,
                amount = amount,
                date = date,
                description = description,
                totalNetWorth = totalNetWorth,
                recentExpensesSummary = recentExpensesSummary
            )

            val netWorthPercent = if (totalNetWorth > 0) (amount / totalNetWorth * 100) else 0.0
            val fallbackDiagnosis = """
                📊 IMPACTO PATRIMONIAL:
                Este gasto de $${String.format("%,.2f", amount)} representa el ${String.format("%.1f", netWorthPercent)}% de tu patrimonio total ($${String.format("%,.2f", totalNetWorth)}).
                ${if (netWorthPercent > 10.0) "⚠️ Alerta de liquidez: Supera el 10% de tu patrimonio total. Ten cautela con el flujo de caja." else "✅ Impacto moderado: Se encuentra en un rango manejable para tus reservas."}

                👍 PUNTOS A FAVOR:
                • Justificación presentada: "${description.ifBlank { "Gasto de necesidad directa" }}".
                • Categoría $category: Si incrementa tu eficiencia, bienestar o seguridad patrimonial, el desembolso tiene retorno funcional.

                ⚠️ PUNTOS EN CONTRA / RIESGOS:
                • Costo de oportunidad: Este capital deja de generar rendimiento o amortizar pasivos existentes.
                • Revisa si el gasto es estrictamente impostergable o si existen alternativas comparables más económicas.

                💡 VEREDICTO EJECUTIVO:
                ${if (netWorthPercent > 15.0) "Considera aplazar 48 horas la compra o buscar una versión de menor costo para proteger tu liquidez." else "El impacto es razonable con tu nivel patrimonial. Si está alineado con tus metas, puedes proceder."}
            """.trimIndent()

            val finalAdvice = result.getOrNull() ?: fallbackDiagnosis
            _uiState.update {
                it.copy(
                    isEvaluatingExpense = false,
                    expenseEvaluationAdvice = finalAdvice
                )
            }
            onDone(finalAdvice)
        }
    }

    fun clearExpenseEvaluationAdvice() {
        _uiState.update { it.copy(expenseEvaluationAdvice = null, isEvaluatingExpense = false) }
    }

    fun askFinanceAdvisorQuestion(question: String) {
        if (question.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isAskingFinanceAdvisor = true, customFinanceAdvice = null) }
            val state = _uiState.value
            val recentExpensesSummary = state.transactionsToday
                .filter { it.type == "GASTO" }
                .take(5)
                .joinToString("; ") { "${it.category}: $${String.format("%.2f", it.amount)}" }

            val aiService = com.example.service.PersonalAiProductivityService()
            val result = aiService.askFinancialAdvisorQuestion(
                question = question,
                totalNetWorth = state.totalAssets,
                income = state.totalIncome,
                expenses = state.totalExpenses,
                recentExpensesSummary = recentExpensesSummary
            )

            val fallback = "Como asesor patrimonial, ante tu consulta sobre '$question': Te sugiero mantener un fondo de emergencia mínimo de 3 a 6 meses de gastos corrientes antes de comprometer capital significativo, y evaluar cualquier inversión considerando su liquidez y horizonte temporal."

            _uiState.update {
                it.copy(
                    isAskingFinanceAdvisor = false,
                    customFinanceAdvice = result.getOrNull() ?: fallback
                )
            }
        }
    }

    fun clearFinanceAdvisorAdvice() {
        _uiState.update { it.copy(customFinanceAdvice = null, isAskingFinanceAdvisor = false) }
    }

    // === ACCIONES DE ENTRENAMIENTO / FITNESS ===
    fun addWorkoutSession(
        title: String,
        targetGoal: String,
        durationMinutes: Int,
        notes: String,
        exercises: List<WorkoutExerciseEntity>
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val session = WorkoutSessionEntity(
                title = title.trim(),
                targetGoal = targetGoal,
                durationMinutes = durationMinutes,
                notes = notes,
                cycleDate = _uiState.value.currentCycleDate
            )
            val sessionId = repository.insertWorkoutSession(session)
            for (ex in exercises) {
                repository.insertWorkoutExercise(ex.copy(sessionId = sessionId))
            }
            recalculateProgression()
            _uiState.update { it.copy(userMessage = "Rutina registrada exitosamente (+100 XP)") }
        }
    }

    fun deleteWorkoutSession(session: WorkoutSessionEntity) {
        viewModelScope.launch {
            repository.deleteWorkoutSession(session)
            recalculateProgression()
        }
    }

    fun analyzeWorkoutWithAi() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzingWorkout = true) }
            val workouts = _uiState.value.workoutSessions
            val summary = if (workouts.isEmpty()) "Sin sesiones previas registradas" else {
                workouts.take(5).joinToString("\n") {
                    "- ${it.title} (${it.durationMinutes} min, meta: ${it.targetGoal}): ${it.notes}"
                }
            }
            val aiService = com.example.service.PersonalAiProductivityService()
            val result = aiService.generateWorkoutAdvice(summary, "Progresión de Fuerza y Rendimiento")
            _uiState.update {
                it.copy(
                    isAnalyzingWorkout = false,
                    aiWorkoutAdvice = result.getOrNull() ?: "Se recomienda una cadencia de 3-4 sesiones semanales alternando grupos musculares y un descanso de 48h entre sesiones intensas para asegurar la recuperación."
                )
            }
        }
    }

    // === ENTRADA GLOBAL DE TEXTO (MULTI-MÓDULO) ===
    fun detectGlobalCommand(text: String) {
        val detected = NaturalLanguageEngine.parseGlobalCommand(text)
        _uiState.update {
            it.copy(
                detectedGlobalEntries = detected,
                isGlobalInputActive = detected.isNotEmpty()
            )
        }
    }

    fun clearGlobalCommand() {
        _uiState.update {
            it.copy(
                detectedGlobalEntries = emptyList(),
                isGlobalInputActive = false
            )
        }
    }

    fun executeGlobalCommand(entries: List<DetectedGlobalEntry>) {
        viewModelScope.launch {
            for (entry in entries) {
                when (entry.type) {
                    GlobalEntryType.WORKOUT -> {
                        val session = WorkoutSessionEntity(
                            title = entry.title,
                            durationMinutes = entry.numericValue.toInt().coerceAtLeast(20),
                            cycleDate = _uiState.value.currentCycleDate,
                            notes = entry.detail
                        )
                        repository.insertWorkoutSession(session)
                    }
                    GlobalEntryType.TIME_TRACKING -> {
                        val timeEntry = TimeEntryEntity(
                            activityName = entry.title,
                            durationMinutes = entry.numericValue.toInt().coerceAtLeast(15),
                            category = entry.category,
                            cycleDate = _uiState.value.currentCycleDate
                        )
                        repository.insertTimeEntry(timeEntry)
                    }
                    GlobalEntryType.TRANSACTION_EXPENSE -> {
                        addTransaction("GASTO", entry.numericValue, entry.category, entry.detail)
                    }
                    GlobalEntryType.TRANSACTION_INCOME -> {
                        addTransaction("INGRESO", entry.numericValue, entry.category, entry.detail)
                    }
                    GlobalEntryType.TASK -> {
                        addTask(title = entry.title, category = entry.category, estimatedMinutes = entry.numericValue.toInt())
                    }
                }
            }
            clearGlobalCommand()
            recalculateProgression()
            _uiState.update { it.copy(userMessage = "${entries.size} registros aplicados a LifeOS.") }
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun addFinancialGoal(name: String, targetAmount: Double, deadline: String) {
        if (name.isBlank() || targetAmount <= 0) return
        viewModelScope.launch {
            val fg = FinancialGoalEntity(
                name = name.trim(),
                targetAmount = targetAmount,
                deadline = deadline
            )
            repository.insertFinancialGoal(fg)
        }
    }

    fun contributeToFinancialGoal(goal: FinancialGoalEntity, amount: Double) {
        viewModelScope.launch {
            val updated = goal.copy(currentAmount = goal.currentAmount + amount)
            repository.updateFinancialGoal(updated)
            // También registrar como gasto de ahorro
            val tx = TransactionEntity(
                type = "GASTO",
                amount = amount,
                category = "Ahorro: ${goal.name}",
                description = "Aporte a meta financiera",
                cycleDate = _uiState.value.currentCycleDate
            )
            repository.insertTransaction(tx)
        }
    }

    fun addAssetDebt(type: String, name: String, amount: Double, notes: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val item = AssetDebtEntity(
                type = type,
                name = name.trim(),
                currentAmount = amount,
                notes = notes.trim()
            )
            repository.insertAssetDebt(item)
        }
    }

    fun updateAssetDebt(item: AssetDebtEntity) {
        viewModelScope.launch {
            repository.updateAssetDebt(item)
            _uiState.update { it.copy(userMessage = "Activo '${item.name}' actualizado.") }
        }
    }

    fun deleteAssetDebt(item: AssetDebtEntity) {
        viewModelScope.launch {
            repository.deleteAssetDebt(item)
            _uiState.update { it.copy(userMessage = "Activo '${item.name}' eliminado.") }
        }
    }

    // === BIENESTAR DIGITAL ===
    fun addAppLimit(appName: String, packageName: String, dailyBudgetMinutes: Int) {
        if (appName.isBlank() || dailyBudgetMinutes <= 0) return
        viewModelScope.launch {
            val limit = AppLimitEntity(
                appName = appName.trim(),
                packageName = packageName.trim().ifBlank { "com.example.${appName.lowercase()}" },
                dailyBudgetMinutes = dailyBudgetMinutes,
                currentUsageMinutes = 0,
                cycleDate = _uiState.value.currentCycleDate
            )
            repository.insertAppLimit(limit)
        }
    }

    fun simulateAppUsage(limitId: Long, additionalMinutes: Int) {
        viewModelScope.launch {
            val limit = _uiState.value.appBudgets.find { it.limit.id == limitId }?.limit ?: return@launch
            val newUsage = limit.currentUsageMinutes + additionalMinutes
            val isNowBlocked = newUsage >= limit.dailyBudgetMinutes
            val updated = limit.copy(
                currentUsageMinutes = newUsage,
                isBlocked = isNowBlocked
            )
            repository.updateAppLimit(updated)
            if (isNowBlocked) {
                _uiState.update {
                    it.copy(userMessage = "⚠️ ${limit.appName}: Presupuesto agotado. Bloqueado hasta las ${_uiState.value.cycleStartTime}")
                }
            }
        }
    }

    fun setStrictMode(enabled: Boolean) {
        // En Modo Estricto, si se intenta desactivar durante el ciclo activo, se valida la regla de evasión
        if (!enabled && _uiState.value.isStrictModeEnabled) {
            _uiState.update {
                it.copy(userMessage = "🛡️ Modo Estricto activo: Por regla personal, no puedes desactivar restricciones durante el ciclo actual. Espera a las ${_uiState.value.cycleStartTime}.")
            }
            return
        }

        viewModelScope.launch {
            repository.setConfig("strict_mode", enabled.toString())
            _uiState.update {
                it.copy(
                    isStrictModeEnabled = enabled,
                    userMessage = if (enabled) "🛡️ Modo Estricto activado. Las restricciones quedan bloqueadas hasta las ${it.cycleStartTime}." else "Modo Normal activado."
                )
            }
        }
    }

    fun resetDailyCycleNow() {
        viewModelScope.launch {
            val newCycle = DailyCycleEngine.getCurrentCycleDate(_uiState.value.cycleStartTime)
            repository.resetAppLimitsForNewCycle(newCycle)
            _uiState.update {
                it.copy(
                    currentCycleDate = newCycle,
                    userMessage = "Ciclo diario reiniciado con éxito. Presupuestos restaurados a 0 min."
                )
            }
        }
    }

    fun setCycleStartTime(newTime: String) {
        viewModelScope.launch {
            repository.setConfig("cycle_start_time", newTime)
            val newCycle = DailyCycleEngine.getCurrentCycleDate(newTime)
            _uiState.update {
                it.copy(
                    cycleStartTime = newTime,
                    currentCycleDate = newCycle,
                    timeUntilNextCycle = DailyCycleEngine.getTimeUntilNextCycle(newTime),
                    cycleProgress = DailyCycleEngine.getCycleProgress(newTime),
                    userMessage = "Inicio de ciclo configurado a las $newTime"
                )
            }
        }
    }

    // === MÓDULO ASESOR DE PRODUCTIVIDAD IA (FIREBASE AI / GEMINI) ===
    fun requestAiProductivityAdvice() {
        if (_uiState.value.isGeneratingAiAdvice) return

        val state = _uiState.value
        val snapshot = UserProductivitySnapshot(
            cycleDate = state.currentCycleDate,
            productiveMinutesToday = state.totalProductiveMinutesToday,
            pendingTasks = state.tasks.filter { !it.isCompleted }.map { it.title },
            completedTasks = state.tasks.filter { it.isCompleted }.map { it.title },
            habitsCompleted = state.habitsCompletedToday,
            habitsTotal = state.habitsTotalToday,
            habitNames = state.habits.map { it.title },
            appUsageMinutes = state.totalAppUsageMinutes,
            appBudgetMinutes = state.totalAppBudgetMinutes,
            blockedApps = state.appBudgets.filter { it.status == com.example.domain.AppBudgetStatus.BLOCKED }.map { it.limit.appName },
            activeGoals = state.goalsWithSubGoals.map { "${it.goal.title} (${(it.progressPercent * 100).toInt()}%)" },
            netFinancialBalance = state.netBalance
        )

        _uiState.update {
            it.copy(
                isGeneratingAiAdvice = true,
                aiAdviceError = null
            )
        }

        viewModelScope.launch {
            val result = repository.generateProductivityAdvice(snapshot)
            result.fold(
                onSuccess = { advice ->
                    _uiState.update {
                        it.copy(
                            isGeneratingAiAdvice = false,
                            aiProductivityAdvice = advice,
                            aiAdviceError = null,
                            userMessage = "Consejos de productividad generados con éxito."
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isGeneratingAiAdvice = false,
                            aiAdviceError = error.message ?: "No se pudo conectar con Gemini."
                        )
                    }
                }
            )
        }
    }

    fun askCustomAiQuestion(question: String) {
        if (question.isBlank() || _uiState.value.isGeneratingAiAdvice) return
        val state = _uiState.value
        val snapshot = UserProductivitySnapshot(
            cycleDate = state.currentCycleDate,
            productiveMinutesToday = state.totalProductiveMinutesToday,
            pendingTasks = state.tasks.filter { !it.isCompleted }.map { it.title },
            completedTasks = state.tasks.filter { it.isCompleted }.map { it.title },
            habitsCompleted = state.habitsCompletedToday,
            habitsTotal = state.habitsTotalToday,
            habitNames = state.habits.map { it.title },
            appUsageMinutes = state.totalAppUsageMinutes,
            appBudgetMinutes = state.totalAppBudgetMinutes,
            blockedApps = state.appBudgets.filter { it.status == com.example.domain.AppBudgetStatus.BLOCKED }.map { it.limit.appName },
            activeGoals = state.goalsWithSubGoals.map { "${it.goal.title} (${(it.progressPercent * 100).toInt()}%)" },
            netFinancialBalance = state.netBalance
        )

        _uiState.update {
            it.copy(
                isGeneratingAiAdvice = true,
                aiAdviceError = null
            )
        }

        viewModelScope.launch {
            val result = repository.askCustomProductivityQuestion(question.trim(), snapshot)
            result.fold(
                onSuccess = { answer ->
                    _uiState.update {
                        it.copy(
                            isGeneratingAiAdvice = false,
                            aiProductivityAdvice = answer,
                            aiAdviceError = null,
                            userMessage = "Asesor Gemini respondió a tu consulta."
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isGeneratingAiAdvice = false,
                            aiAdviceError = error.message ?: "No se pudo consultar a Gemini."
                        )
                    }
                }
            )
        }
    }

    fun clearAiAdvice() {
        _uiState.update {
            it.copy(
                aiProductivityAdvice = null,
                aiAdviceError = null
            )
        }
    }

    // === CHATBOT MULTITURNO GEMINI ===
    fun selectChatPersona(persona: ChatbotPersona) {
        _uiState.update { it.copy(selectedChatPersona = persona) }
    }

    fun selectGeminiModel(model: GeminiModelOption) {
        _uiState.update { it.copy(selectedGeminiModel = model) }
    }

    fun clearChatHistory() {
        _uiState.update {
            it.copy(
                chatMessages = emptyList(),
                chatError = null
            )
        }
    }

    fun sendChatMessage(messageText: String) {
        val trimmed = messageText.trim()
        if (trimmed.isBlank() || _uiState.value.isChatLoading) return

        val state = _uiState.value
        val userMsg = ChatMessage(
            sender = ChatSender.USER,
            text = trimmed,
            persona = state.selectedChatPersona,
            modelUsed = state.selectedGeminiModel
        )

        val updatedMessages = state.chatMessages + userMsg

        _uiState.update {
            it.copy(
                chatMessages = updatedMessages,
                isChatLoading = true,
                chatError = null
            )
        }

        val snapshot = UserProductivitySnapshot(
            cycleDate = state.currentCycleDate,
            productiveMinutesToday = state.totalProductiveMinutesToday,
            pendingTasks = state.tasks.filter { !it.isCompleted }.map { it.title },
            completedTasks = state.tasks.filter { it.isCompleted }.map { it.title },
            habitsCompleted = state.habitsCompletedToday,
            habitsTotal = state.habitsTotalToday,
            habitNames = state.habits.map { it.title },
            appUsageMinutes = state.totalAppUsageMinutes,
            appBudgetMinutes = state.totalAppBudgetMinutes,
            blockedApps = state.appBudgets.filter { it.status == com.example.domain.AppBudgetStatus.BLOCKED }.map { it.limit.appName },
            activeGoals = state.goalsWithSubGoals.map { "${it.goal.title} (${(it.progressPercent * 100).toInt()}%)" },
            netFinancialBalance = state.netBalance
        )

        viewModelScope.launch {
            val result = repository.sendChatMessage(
                history = state.chatMessages,
                userMessage = trimmed,
                persona = state.selectedChatPersona,
                modelOption = state.selectedGeminiModel,
                snapshot = snapshot
            )

            result.fold(
                onSuccess = { reply ->
                    val assistantMsg = ChatMessage(
                        sender = ChatSender.ASSISTANT,
                        text = reply,
                        persona = state.selectedChatPersona,
                        modelUsed = state.selectedGeminiModel
                    )
                    _uiState.update {
                        it.copy(
                            chatMessages = it.chatMessages + assistantMsg,
                            isChatLoading = false,
                            chatError = null
                        )
                    }
                },
                onFailure = { error ->
                    val errorMsg = ChatMessage(
                        sender = ChatSender.ASSISTANT,
                        text = "Error al comunicar con ${state.selectedGeminiModel.displayName}: ${error.message ?: "Sin respuesta."}",
                        persona = state.selectedChatPersona,
                        modelUsed = state.selectedGeminiModel,
                        isError = true
                    )
                    _uiState.update {
                        it.copy(
                            chatMessages = it.chatMessages + errorMsg,
                            isChatLoading = false,
                            chatError = error.message
                        )
                    }
                }
            )
        }
    }

    // === ACCIONES DE AUTENTICACIÓN FIREBASE AUTH ===
    fun signInWithGoogle(context: android.content.Context, serverClientId: String? = null) {
        _uiState.update { it.copy(isAuthLoading = true, authError = null) }
        viewModelScope.launch {
            val result = repository.signInWithGoogle(context, serverClientId)
            result.fold(
                onSuccess = { user ->
                    _uiState.update {
                        it.copy(
                            currentUser = user,
                            isAuthLoading = false,
                            authError = null,
                            userMessage = "Sesión iniciada con Google: ${user.email ?: user.displayName ?: "Usuario"}"
                        )
                    }
                    // Sincronizar automáticamente datos en Cloud Firestore para el nuevo usuario
                    syncWithCloud(user.uid)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            authError = error.message,
                            userMessage = "Error en Google Sign-In: ${error.message}"
                        )
                    }
                }
            )
        }
    }

    fun signInAnonymously() {
        _uiState.update { it.copy(isAuthLoading = true, authError = null) }
        viewModelScope.launch {
            val result = repository.signInAnonymously()
            result.fold(
                onSuccess = { user ->
                    _uiState.update {
                        it.copy(
                            currentUser = user,
                            isAuthLoading = false,
                            authError = null,
                            userMessage = "Sesión segura anónima iniciada (${user.uid.take(6)}...)"
                        )
                    }
                    syncWithCloud(user.uid)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            authError = error.message,
                            userMessage = "Error en sesión anónima: ${error.message}"
                        )
                    }
                }
            )
        }
    }

    fun signOut(context: android.content.Context? = null) {
        viewModelScope.launch {
            val result = repository.signOut(context)
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            currentUser = null,
                            userMessage = "Sesión cerrada correctamente"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(userMessage = "Error al cerrar sesión: ${error.message}")
                    }
                }
            )
        }
    }

    // === ACCIONES DE SINCRONIZACIÓN EN LA NUBE (FIRESTORE) ===
    fun syncWithCloud(userId: String? = null) {
        val targetUid = userId ?: _uiState.value.currentUser?.uid ?: repository.getCurrentUserId()
        viewModelScope.launch {
            val result = repository.syncAllWithCloud(targetUid)
            result.fold(
                onSuccess = { res ->
                    _uiState.update {
                        it.copy(userMessage = "Sincronización con la nube exitosa (${res.syncedItemsCount} elementos)")
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(userMessage = "Error al sincronizar con Firestore: ${error.message}")
                    }
                }
            )
        }
    }

    fun uploadAllToCloud(userId: String? = null) {
        val targetUid = userId ?: _uiState.value.currentUser?.uid ?: repository.getCurrentUserId()
        viewModelScope.launch {
            val result = repository.uploadAllToCloud(targetUid)
            result.fold(
                onSuccess = { res ->
                    _uiState.update {
                        it.copy(userMessage = "Copia de respaldo en la nube completada (${res.syncedItemsCount} elementos)")
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(userMessage = "Error de subida a la nube: ${error.message}")
                    }
                }
            )
        }
    }

    fun downloadAllFromCloud(userId: String? = null) {
        val targetUid = userId ?: _uiState.value.currentUser?.uid ?: repository.getCurrentUserId()
        viewModelScope.launch {
            val result = repository.downloadAllFromCloud(targetUid)
            result.fold(
                onSuccess = { res ->
                    _uiState.update {
                        it.copy(userMessage = "Restauración desde la nube completada (${res.syncedItemsCount} elementos)")
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(userMessage = "Error al descargar desde la nube: ${error.message}")
                    }
                }
            )
        }
    }

    // === SEMBRADOR DE DATOS INICIALES (FIRST LAUNCH) ===
    private suspend fun seedInitialDataIfEmpty(cycleDate: String) {
        val existingTasks = repository.allTasks.first()
        if (existingTasks.isNotEmpty()) return

        // Tareas iniciales
        repository.insertTask(TaskEntity(title = "Revisar arquitectura del sistema personal", priority = "ALTA", estimatedMinutes = 45, category = "Ingeniería", cycleDate = cycleDate))
        repository.insertTask(TaskEntity(title = "Sesión de lectura técnica (30 min)", priority = "MEDIA", estimatedMinutes = 30, category = "Estudio", cycleDate = cycleDate))
        repository.insertTask(TaskEntity(title = "Conciliar presupuesto mensual", priority = "BAJA", estimatedMinutes = 20, category = "Finanzas", cycleDate = cycleDate))

        // Bloques de tiempo
        repository.insertTimeBlock(TimeBlockEntity(title = "Bloque de Trabajo Profundo", startTime = "09:00", endTime = "11:30", category = "Enfoque", cycleDate = cycleDate))
        repository.insertTimeBlock(TimeBlockEntity(title = "Entrenamiento Físico", startTime = "18:00", endTime = "19:00", category = "Salud", cycleDate = cycleDate))

        // Hábitos solicitados por el usuario
        repository.insertHabit(HabitEntity(title = "Ejercicio físico", description = "Entrenamiento de fuerza o cardio", frequency = "DIARIO", targetValue = 45, unit = "min", currentStreak = 5, bestStreak = 14, category = "Salud"))
        repository.insertHabit(HabitEntity(title = "Estudio y aprendizaje", description = "Lectura o cursos técnicos", frequency = "DIARIO", targetValue = 30, unit = "min", currentStreak = 8, bestStreak = 21, category = "Estudio"))
        repository.insertHabit(HabitEntity(title = "Lectura", description = "Libro de no ficción", frequency = "DIARIO", targetValue = 20, unit = "págs", currentStreak = 3, bestStreak = 12, category = "Crecimiento"))
        repository.insertHabit(HabitEntity(title = "Rutina matutina / Cero pantalla", description = "No tocar teléfono en la primera hora", frequency = "DIARIO", targetValue = 1, unit = "vez", currentStreak = 12, bestStreak = 30, category = "Atención"))

        // Metas y Submetas
        val g1Id = repository.insertGoal(GoalEntity(title = "Desarrollar mi Sistema Operativo Personal", description = "Extensión digital de control de vida y métricas", category = "Tecnología", targetDate = "2026-12-31"))
        repository.insertSubGoal(SubGoalEntity(goalId = g1Id, title = "Definir arquitectura y modelo de datos", isCompleted = true))
        repository.insertSubGoal(SubGoalEntity(goalId = g1Id, title = "Construir V1 con Room y Jetpack Compose", isCompleted = true))
        repository.insertSubGoal(SubGoalEntity(goalId = g1Id, title = "Integrar servicios de bienestar y Accessibility", isCompleted = false))

        val g2Id = repository.insertGoal(GoalEntity(title = "Construir Fondo de Emergencia de 6 Meses", description = "Colchón de seguridad financiera", category = "Finanzas", targetDate = "2026-10-30"))
        repository.insertSubGoal(SubGoalEntity(goalId = g2Id, title = "Ahorrar primeros $1,000", isCompleted = true))
        repository.insertSubGoal(SubGoalEntity(goalId = g2Id, title = "Alcanzar $3,000 en cuenta de alto rendimiento", isCompleted = false))

        repository.insertFinancialGoal(FinancialGoalEntity(name = "Fondo de Emergencia", targetAmount = 5000.0, currentAmount = 0.0, deadline = "Dic 2026", category = "Seguridad"))

        // Bienestar Digital (Apps del ejemplo explícito del usuario: Facebook 30m, Instagram 20m, YouTube 45m)
        repository.insertAppLimit(AppLimitEntity(appName = "YouTube", packageName = "com.google.android.youtube", dailyBudgetMinutes = 45, currentUsageMinutes = 15, isBlocked = false, cycleDate = cycleDate))
        repository.insertAppLimit(AppLimitEntity(appName = "Facebook", packageName = "com.facebook.katana", dailyBudgetMinutes = 30, currentUsageMinutes = 10, isBlocked = false, cycleDate = cycleDate))
        repository.insertAppLimit(AppLimitEntity(appName = "Instagram", packageName = "com.instagram.android", dailyBudgetMinutes = 20, currentUsageMinutes = 20, isBlocked = true, cycleDate = cycleDate))

        // Entrenamiento Inicial
        val wsId = repository.insertWorkoutSession(
            WorkoutSessionEntity(
                title = "Rutina Torso: Fuerza & Hipertrofia",
                targetGoal = "Hipertrofia",
                durationMinutes = 50,
                notes = "Sobrecarga progresiva en press militar y dominadas",
                cycleDate = cycleDate
            )
        )
        repository.insertWorkoutExercise(WorkoutExerciseEntity(sessionId = wsId, exerciseName = "Press de Banca", sets = 4, reps = "10, 8, 8, 6", weightKg = 75.0, restSeconds = 90))
        repository.insertWorkoutExercise(WorkoutExerciseEntity(sessionId = wsId, exerciseName = "Dominadas", sets = 3, reps = "8, 8, 7", weightKg = 0.0, restSeconds = 90))
        repository.insertWorkoutExercise(WorkoutExerciseEntity(sessionId = wsId, exerciseName = "Press Militar", sets = 3, reps = "12, 10, 10", weightKg = 20.0, restSeconds = 60))
    }

    fun completeOnboarding(selectedHabitTitles: List<String>, cycleStartTime: String) {
        viewModelScope.launch {
            repository.setConfig("onboarding_completed", "true")
            setCycleStartTime(cycleStartTime)
            selectedHabitTitles.forEach { title ->
                val cat = when {
                    title.contains("Ejercicio", ignoreCase = true) -> "Salud"
                    title.contains("Lectura", ignoreCase = true) -> "Estudio"
                    else -> "Enfoque"
                }
                repository.insertHabit(HabitEntity(title = title, frequency = "DIARIO", category = cat, targetValue = 1))
            }
            _uiState.update { it.copy(showOnboarding = false) }
        }
    }

    fun openOnboarding() {
        _uiState.update { it.copy(showOnboarding = true) }
    }

    fun dismissOnboarding() {
        viewModelScope.launch {
            repository.setConfig("onboarding_completed", "true")
            _uiState.update { it.copy(showOnboarding = false) }
        }
    }

    fun setAccentColor(colorName: String) {
        viewModelScope.launch {
            repository.setConfig("accent_color", colorName)
            _uiState.update { it.copy(accentColorName = colorName, userMessage = "Color de acento actualizado a $colorName") }
        }
    }

    fun exportBackup(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val json = com.example.util.PersonalOsBackupManager.exportDatabaseToJson(repository)
            onResult(json)
        }
    }

    fun importBackup(jsonString: String) {
        viewModelScope.launch {
            val result = com.example.util.PersonalOsBackupManager.importDatabaseFromJson(repository, jsonString)
            result.onSuccess { count ->
                _uiState.update { it.copy(userMessage = "¡Copia de seguridad restaurada! ($count elementos importados)") }
            }.onFailure { err ->
                _uiState.update { it.copy(userMessage = "Error al restaurar copia: ${err.message}") }
            }
        }
    }
}

/**
 * Factory para la instanciación limpia de PersonalOsViewModel con inyección de dependencias
 * de la capa de Repositorio (Room Database + Gemini Productivity Service).
 */
class PersonalOsViewModelFactory(
    private val repository: PersonalOsRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PersonalOsViewModel::class.java)) {
            return PersonalOsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
