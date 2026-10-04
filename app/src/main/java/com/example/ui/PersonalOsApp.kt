package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.database.AppDatabase
import com.example.data.repository.PersonalOsRepository
import com.example.data.repository.PersonalOsRepositoryImpl
import com.example.service.PersonalAiProductivityService
import com.example.ui.components.ExecutiveBottomNav
import com.example.ui.components.OnboardingDialog
import com.example.ui.screens.*

@Composable
fun PersonalOsApp() {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val aiService = remember { PersonalAiProductivityService(context) }
    val repository: PersonalOsRepository = remember {
        PersonalOsRepositoryImpl(database.personalOsDao(), aiService)
    }
    val viewModel: PersonalOsViewModel = viewModel(
        factory = remember { PersonalOsViewModelFactory(repository) }
    )

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showChatOverlay by remember { mutableStateOf(false) }

    // Solicitud de permisos en tiempo de ejecución para notificaciones locales (Android 13+)
    val currentRegistryOwner = androidx.activity.compose.LocalActivityResultRegistryOwner.current
    val notificationPermissionLauncher = if (currentRegistryOwner != null) {
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { _ -> }
    } else null

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher?.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    LaunchedEffect(state.userMessage) {
        state.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.dismissMessage()
        }
    }

    val currentLocale = remember(state.selectedLanguage) {
        com.example.util.LocaleHelper.getLocaleForLanguage(state.selectedLanguage)
    }

    val localizedContext = remember(currentLocale, context) {
        com.example.util.LocaleHelper.applyLocale(context, state.selectedLanguage)
    }
    val localizedConfiguration = remember(currentLocale, context) {
        val cfg = android.content.res.Configuration(context.resources.configuration)
        cfg.setLocale(currentLocale)
        cfg
    }

    DisposableEffect(currentLocale) {
        java.util.Locale.setDefault(currentLocale)
        val config = context.resources.configuration
        config.setLocale(currentLocale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
        onDispose { }
    }

    val activityResultRegistryOwner = androidx.activity.compose.LocalActivityResultRegistryOwner.current
        ?: (context as? androidx.activity.result.ActivityResultRegistryOwner)

    val providedValues = remember(localizedContext, localizedConfiguration, activityResultRegistryOwner) {
        buildList {
            add(androidx.compose.ui.platform.LocalContext provides localizedContext)
            add(androidx.compose.ui.platform.LocalConfiguration provides localizedConfiguration)
            activityResultRegistryOwner?.let {
                add(androidx.activity.compose.LocalActivityResultRegistryOwner provides it)
            }
        }.toTypedArray()
    }

    CompositionLocalProvider(*providedValues) {
        key(currentLocale) {
            com.example.ui.theme.MyApplicationTheme(accentColorName = state.accentColorName) {
            Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            ExecutiveBottomNav(
                activeTab = state.activeTab,
                onTabSelected = { viewModel.selectTab(it) },
                language = state.selectedLanguage
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (state.activeTab != AppNavTab.DASHBOARD) {
                androidx.activity.compose.BackHandler {
                    viewModel.selectTab(AppNavTab.DASHBOARD)
                }
            }

            Crossfade(targetState = state.activeTab, label = "tab_crossfade") { tab ->
                when (tab) {
                    AppNavTab.DASHBOARD -> DashboardScreen(
                        state = state,
                        onNavigate = { viewModel.selectTab(it) },
                        onToggleTask = { viewModel.toggleTask(it) },
                        onToggleHabit = { viewModel.toggleHabitToday(it) },
                        onQuickStartTimer = { activity ->
                            viewModel.startFocusTimer(activity, "Trabajo")
                            viewModel.selectTab(AppNavTab.TIME)
                        },
                        onRequestAiAdvice = { viewModel.requestAiProductivityAdvice() },
                        onClearAiAdvice = { viewModel.clearAiAdvice() },
                        onAskCustomAiQuestion = { viewModel.askCustomAiQuestion(it) },
                        onAddTask = { title, priority, est -> viewModel.addTask(title, priority, est, "Trabajo") },
                        onAddHabit = { title, cat -> viewModel.addHabit(title, "DIARIO", cat, 1) },
                        onAddTransaction = { type, amount, cat, desc -> viewModel.addTransaction(type, amount, cat, desc) },
                        onDetectGlobalCommand = { viewModel.detectGlobalCommand(it) },
                        onExecuteGlobalCommand = { viewModel.executeGlobalCommand(it) },
                        onClearGlobalCommand = { viewModel.clearGlobalCommand() },
                        onOpenFitness = { viewModel.selectTab(AppNavTab.FITNESS) },
                        onOpenChat = { showChatOverlay = true }
                    )
                    AppNavTab.TIME -> TimeScreen(
                        state = state,
                        onAddTask = { title, priority, est, cat -> viewModel.addTask(title, priority, est, cat) },
                        onToggleTask = { viewModel.toggleTask(it) },
                        onDeleteTask = { viewModel.deleteTask(it) },
                        onAddTimeBlock = { title, start, end, cat -> viewModel.addTimeBlock(title, start, end, cat) },
                        onToggleTimeBlock = { viewModel.toggleTimeBlock(it) },
                        onDeleteTimeBlock = { viewModel.deleteTimeBlock(it) },
                        onStartFocusTimer = { act, cat -> viewModel.startFocusTimer(act, cat) },
                        onPauseFocusTimer = { viewModel.pauseFocusTimer() },
                        onFinishFocusTimer = { viewModel.finishAndLogFocusTimer() }
                    )
                    AppNavTab.HABITS -> HabitsScreen(
                        state = state,
                        onToggleHabit = { viewModel.toggleHabitToday(it) },
                        onAddHabit = { title, freq, cat, target, goalId, unit -> viewModel.addHabit(title, freq, cat, target, goalId, unit) },
                        onDeleteHabit = { viewModel.deleteHabit(it) },
                        onSetHabitReminder = { enabled, time -> viewModel.setHabitReminder(context, enabled, time) },
                        onTestHabitReminder = { viewModel.triggerHabitReminderTest(context) }
                    )
                    AppNavTab.FITNESS -> FitnessScreen(
                        state = state,
                        onAddWorkout = { title, goal, dur, notes, exercises ->
                            viewModel.addWorkoutSession(title, goal, dur, notes, exercises)
                        },
                        onDeleteWorkout = { viewModel.deleteWorkoutSession(it) },
                        onAnalyzeAi = { viewModel.analyzeWorkoutWithAi() },
                        onSuggestTacticalNote = { name, goal, mins, summary, callback ->
                            viewModel.suggestTacticalWorkoutNote(name, goal, mins, summary, callback)
                        },
                        onToggleExercise = { viewModel.toggleWorkoutExercise(it) },
                        onToggleWorkoutSession = { viewModel.toggleWorkoutSessionCompleted(it) },
                        onAddExerciseToSession = { sessionId, name, sets, reps, weight, rest, notes ->
                            viewModel.addWorkoutExercise(sessionId, name, sets, reps, weight, rest, notes)
                        },
                        onUpdateExercise = { viewModel.updateWorkoutExercise(it) },
                        onDeleteExercise = { viewModel.deleteWorkoutExercise(it) },
                        onClose = { viewModel.selectTab(AppNavTab.DASHBOARD) }
                    )
                    AppNavTab.GOALS -> GoalsScreen(
                        state = state,
                        onAddGoal = { title, desc, cat, date -> viewModel.addGoal(title, desc, cat, date) },
                        onAddSubGoal = { goalId, title -> viewModel.addSubGoal(goalId, title) },
                        onToggleSubGoal = { viewModel.toggleSubGoal(it) },
                        onDeleteGoal = { viewModel.deleteGoal(it) },
                        onToggleHabit = { viewModel.toggleHabitToday(it) }
                    )
                    AppNavTab.FINANCE -> FinanceScreen(
                        state = state,
                        onAddTransaction = { type, amt, cat, desc -> viewModel.addTransaction(type, amt, cat, desc) },
                        onDeleteTransaction = { viewModel.deleteTransaction(it) },
                        onAddFinancialGoal = { name, target, deadline -> viewModel.addFinancialGoal(name, target, deadline) },
                        onContributeToGoal = { goal, amt -> viewModel.contributeToFinancialGoal(goal, amt) },
                        onAddAssetDebt = { type, name, amt, notes -> viewModel.addAssetDebt(type, name, amt, notes) },
                        onUpdateAssetDebt = { viewModel.updateAssetDebt(it) },
                        onDeleteAssetDebt = { viewModel.deleteAssetDebt(it) },
                        onAddTransactionNatural = { viewModel.addTransactionWithNaturalLanguage(it) },
                        onAnalyzeFinanceAi = { viewModel.analyzeFinanceWithAi() },
                        onEvaluateExpenseBeforeSaving = { name, cat, amt, date, desc, onDone ->
                            viewModel.evaluateExpenseBeforeSaving(name, cat, amt, date, desc, onDone)
                        },
                        onClearExpenseEvaluation = { viewModel.clearExpenseEvaluationAdvice() },
                        onAskFinanceAdvisor = { viewModel.askFinanceAdvisorQuestion(it) },
                        onClearFinanceAdvisor = { viewModel.clearFinanceAdvisorAdvice() }
                    )
                    AppNavTab.SETTINGS -> SettingsScreen(
                        state = state,
                        onSetCycleStartTime = { viewModel.setCycleStartTime(it) },
                        onSetStrictMode = { viewModel.setStrictMode(it) },
                        onForceResetCycle = { viewModel.resetDailyCycleNow() },
                        onSyncAllWithCloud = { viewModel.syncWithCloud() },
                        onUploadToCloud = { viewModel.uploadAllToCloud() },
                        onDownloadFromCloud = { viewModel.downloadAllFromCloud() },
                        onSignInWithGoogle = { viewModel.signInWithGoogle(context) },
                        onSignInAnonymously = { viewModel.signInAnonymously() },
                        onSignOut = { viewModel.signOut(context) },
                        onOpenOnboarding = { viewModel.openOnboarding() },
                        onSetAccentColor = { viewModel.setAccentColor(it) },
                        onExportBackup = { callback -> viewModel.exportBackup(callback) },
                        onImportBackup = { json -> viewModel.importBackup(json) },
                        onSetCurrency = { symbol, code -> viewModel.setAppCurrency(symbol, code) },
                        onSetLanguage = { viewModel.setAppLanguage(it) }
                    )
                }
            }

            if (state.showOnboarding) {
                OnboardingDialog(
                    onDismiss = { viewModel.dismissOnboarding() },
                    onComplete = { habits, cycleTime ->
                        viewModel.completeOnboarding(habits, cycleTime)
                    },
                    onEnableReminder = { time ->
                        viewModel.setHabitReminder(context, true, time)
                    }
                )
            }

            if (showChatOverlay) {
                ChatScreen(
                    state = state,
                    onSendMessage = { viewModel.sendChatMessage(it) },
                    onSelectPersona = { viewModel.selectChatPersona(it) },
                    onSelectModel = { viewModel.selectGeminiModel(it) },
                    onClearChat = { viewModel.clearChatHistory() },
                    onClose = { showChatOverlay = false }
                )
            }
        }
    }
}
}
}
}
