package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.AppBudgetStatus
import com.example.domain.EvaluatedAppBudget
import com.example.ui.AppNavTab
import com.example.ui.PersonalOsUiState
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.util.ExecutiveAphorisms
import com.example.util.SensoryFeedbackHelper

/**
 * Dashboard de LifeOS Precision Executive.
 * Jerarquía estricta:
 * ESTADO DEL SISTEMA → ATENCIÓN → TIEMPO → HÁBITOS → METAS → FINANZAS
 */
@Composable
fun DashboardScreen(
    state: PersonalOsUiState,
    onNavigate: (AppNavTab) -> Unit,
    onToggleTask: (com.example.data.model.TaskEntity) -> Unit,
    onToggleHabit: (Long) -> Unit,
    onQuickStartTimer: (String) -> Unit,
    onRequestAiAdvice: () -> Unit = {},
    onClearAiAdvice: () -> Unit = {},
    onAskCustomAiQuestion: (String) -> Unit = {},
    onAddTask: (title: String, priority: String, estimatedMinutes: Int) -> Unit = { _, _, _ -> },
    onAddHabit: (title: String, category: String) -> Unit = { _, _ -> },
    onAddTransaction: (type: String, amount: Double, category: String, desc: String) -> Unit = { _, _, _, _ -> },
    onDetectGlobalCommand: (String) -> Unit = {},
    onExecuteGlobalCommand: (List<com.example.data.model.DetectedGlobalEntry>) -> Unit = {},
    onClearGlobalCommand: () -> Unit = {},
    onOpenFitness: () -> Unit = {},
    onOpenChat: () -> Unit = {}
) {
    val context = LocalContext.current
    var showCustomQuestionDialog by remember { mutableStateOf(false) }
    var customQuestionText by remember { mutableStateOf("") }
    var showQuickActions by remember { mutableStateOf(false) }
    var showProgressionDialog by remember { mutableStateOf(false) }
    var lockScreenAppTarget by remember { mutableStateOf<EvaluatedAppBudget?>(null) }
    val todayAphorism = remember { ExecutiveAphorisms.getTodayAphorism() }

    Box(modifier = Modifier.fillMaxSize().background(ExecutiveDarkBg)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ========================================
            // 1. ESTADO DEL SISTEMA (HUD SUPERIOR)
            // ========================================
            ExecutiveHeader(
                title = "Centro de Mando",
                subtitle = "Extensión digital y control operativo",
                cycleDate = state.currentCycleDate,
                timeUntilReset = state.timeUntilNextCycle,
                cycleProgress = state.cycleProgress,
                actionIcon = Icons.Default.Settings,
                onActionClick = { onNavigate(AppNavTab.SETTINGS) }
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // BARRA DE ESTADO DE CUENTA Y SINCRONIZACIÓN CLOUD FIRESTORE
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ExecutiveDarkSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ExecutiveBorderSubtle),
                        modifier = Modifier.fillMaxWidth().testTag("card_cloud_user_status")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (state.currentUser != null) Icons.Default.CloudDone else Icons.Default.CloudQueue,
                                    contentDescription = null,
                                    tint = if (state.currentUser != null) ExecutiveSuccessGreen else ExecutiveSecondaryAmber,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = state.currentUser?.let {
                                            it.displayName ?: it.email ?: if (it.isAnonymous) "Sesión Anónima Nube" else "Perfil Cloud"
                                        } ?: "Almacenamiento Local Offline",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = ExecutiveTextWhite
                                    )
                                    Text(
                                        text = if (state.currentUser != null) "Sincronización Cloud Firestore activa" else "Vincular cuenta en Configuración",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = ExecutiveTextMuted
                                    )
                                }
                            }

                            TextButton(
                                onClick = { onNavigate(AppNavTab.SETTINGS) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text(
                                    text = if (state.currentUser != null) "GESTIONAR" else "CONECTAR",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp
                                    ),
                                    color = ExecutivePrimaryTeal
                                )
                            }
                        }
                    }
                }

                // HUD DE PROGRESIÓN RPG TÁCTICA
                item {
                    TacticalProgressionWidget(
                        progression = state.userProgression,
                        onOpenDetails = { showProgressionDialog = true }
                    )
                }

                // COMANDO GLOBAL MULTI-MÓDULO
                item {
                    GlobalCommandInput(
                        detectedEntries = state.detectedGlobalEntries,
                        onDetect = onDetectGlobalCommand,
                        onExecute = onExecuteGlobalCommand,
                        onClear = onClearGlobalCommand
                    )
                }

                // PRINCIPIO OPERATIVO
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ExecutiveDarkSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ExecutiveBorderSubtle),
                        modifier = Modifier.fillMaxWidth().testTag("card_daily_aphorism")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.FormatQuote,
                                        contentDescription = null,
                                        tint = ExecutiveSecondaryAmber,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "PRINCIPIO OPERATIVO",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        ),
                                        color = ExecutiveSecondaryAmber
                                    )
                                }
                                LifeOsBadge(
                                    text = todayAphorism.focusArea.uppercase(),
                                    type = LifeOsBadgeType.ACTIVE
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "\"${todayAphorism.quote}\"",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    lineHeight = 18.sp
                                ),
                                color = ExecutiveTextWhite
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "— ${todayAphorism.author}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = ExecutiveTextMuted,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }
                }

                // ========================================
                // 2. ATENCIÓN (BIENESTAR DIGITAL)
                // ========================================
                item {
                    LifeOsSectionHeader(
                        title = "Atención",
                        subtag = "Presupuestos de Pantalla",
                        indicatorColor = if (state.blockedAppsCount > 0) ExecutiveDangerRed else ExecutivePrimaryTeal
                    )
                }

                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = ExecutiveDarkSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ExecutiveBorder),
                        modifier = Modifier.fillMaxWidth().testTag("card_attention_hub")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Cabecera de telemetría de atención
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "CONSUMO GLOBAL DEL CICLO",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = ExecutiveTextMuted
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${state.totalAppUsageMinutes}m / ${state.totalAppBudgetMinutes}m permitidos",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        ),
                                        color = if (state.totalAppUsageMinutes >= state.totalAppBudgetMinutes) ExecutiveDangerRed else ExecutiveTextWhite
                                    )
                                }

                                if (state.isStrictModeEnabled) {
                                    LifeOsBadge(
                                        text = "MODO ESTRICTO [LOCK]",
                                        type = LifeOsBadgeType.BLOCKED
                                    )
                                } else {
                                    LifeOsBadge(
                                        text = "MODO NORMAL",
                                        type = LifeOsBadgeType.AVAILABLE
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Lista precisa de Aplicaciones con el formato especificado:
                            // Nombre
                            // Uso actual / Presupuesto (ej: 18:42 / 30:00)
                            // Tiempo restante (ej: 11:18 restantes)
                            // Estado (DISPONIBLE / CERCA DEL LÍMITE / AGOTADO / BLOQUEADO)
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                state.appBudgets.forEach { budget ->
                                    val isBlocked = budget.status == AppBudgetStatus.BLOCKED || budget.status == AppBudgetStatus.EXHAUSTED
                                    val isNearLimit = budget.status == AppBudgetStatus.NEAR_LIMIT || budget.status == AppBudgetStatus.WARNING

                                    val badgeType = when (budget.status) {
                                        AppBudgetStatus.BLOCKED -> LifeOsBadgeType.BLOCKED
                                        AppBudgetStatus.EXHAUSTED -> LifeOsBadgeType.EXHAUSTED
                                        AppBudgetStatus.NEAR_LIMIT, AppBudgetStatus.WARNING -> LifeOsBadgeType.NEAR_LIMIT
                                        AppBudgetStatus.AVAILABLE -> LifeOsBadgeType.AVAILABLE
                                    }

                                    val badgeLabel = when (budget.status) {
                                        AppBudgetStatus.BLOCKED -> "BLOQUEADO"
                                        AppBudgetStatus.EXHAUSTED -> "AGOTADO"
                                        AppBudgetStatus.NEAR_LIMIT, AppBudgetStatus.WARNING -> "CERCA DEL LÍMITE"
                                        AppBudgetStatus.AVAILABLE -> "DISPONIBLE"
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isBlocked) ExecutiveDangerRed.copy(alpha = 0.08f) else ExecutiveDarkSurfaceElevated,
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isBlocked) ExecutiveDangerRed.copy(alpha = 0.35f) else ExecutiveBorderSubtle
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                if (isBlocked) {
                                                    SensoryFeedbackHelper.vibrateAlert(context)
                                                    lockScreenAppTarget = budget
                                                }
                                            }
                                            .testTag("app_budget_item_${budget.limit.appName.lowercase()}")
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = if (isBlocked) Icons.Default.Lock else Icons.Default.PhoneAndroid,
                                                        contentDescription = budget.limit.appName,
                                                        tint = if (isBlocked) ExecutiveDangerRed else ExecutivePrimaryTeal,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = budget.limit.appName,
                                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                        color = ExecutiveTextWhite
                                                    )
                                                }

                                                LifeOsBadge(
                                                    text = badgeLabel,
                                                    type = badgeType
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = String.format("%02d:00 / %02d:00", budget.limit.currentUsageMinutes, budget.limit.dailyBudgetMinutes),
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        fontFamily = FontFamily.Monospace,
                                                        fontWeight = FontWeight.SemiBold
                                                    ),
                                                    color = ExecutiveTextMuted
                                                )

                                                Text(
                                                    text = if (isBlocked) "00:00 restantes" else String.format("%02d:00 restantes", budget.remainingMinutes),
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        fontFamily = FontFamily.Monospace,
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    color = if (isBlocked) ExecutiveDangerRed else if (isNearLimit) ExecutiveSecondaryAmber else ExecutiveSuccessGreen
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            LinearProgressIndicator(
                                                progress = { budget.percentUsed },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(4.dp)
                                                    .clip(RoundedCornerShape(2.dp)),
                                                color = if (isBlocked) ExecutiveDangerRed else if (isNearLimit) ExecutiveSecondaryAmber else ExecutivePrimaryTeal,
                                                trackColor = ExecutiveDarkSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = {
                                        val sample = state.appBudgets.firstOrNull { it.status == AppBudgetStatus.BLOCKED || it.status == AppBudgetStatus.EXHAUSTED }
                                            ?: state.appBudgets.firstOrNull()
                                        lockScreenAppTarget = sample
                                    },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Security,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = ExecutiveSecondaryAmber
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Inspeccionar Pantalla de Bloqueo",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = ExecutiveSecondaryAmber
                                    )
                                }

                                TextButton(
                                    onClick = { onNavigate(AppNavTab.WELLBEING) },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(
                                        "Administrar Cuotas →",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = ExecutivePrimaryTeal
                                    )
                                }
                            }
                        }
                    }
                }

                // ========================================
                // 3. TIEMPO (AGENDA Y TAREAS DEL CICLO)
                // ========================================
                item {
                    LifeOsSectionHeader(
                        title = "Tiempo",
                        subtag = "Bloque Activo & Agenda",
                        indicatorColor = ExecutiveAccentBlue
                    )
                }

                item {
                    val nextTask = state.tasks.firstOrNull { !it.isCompleted }
                    val nextTimeBlock = state.timeBlocks.firstOrNull { !it.isCompleted }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = ExecutiveDarkSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ExecutiveBorder),
                        modifier = Modifier.fillMaxWidth().testTag("card_what_to_do")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "ENFOQUE INMEDIATO",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = ExecutiveAccentBlue
                                )

                                TextButton(
                                    onClick = { onNavigate(AppNavTab.TIME) },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(
                                        "Línea Temporal →",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = ExecutiveAccentBlue
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            if (nextTask != null) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = ExecutiveDarkSurfaceElevated,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, ExecutiveBorderSubtle),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = nextTask.isCompleted,
                                            onCheckedChange = {
                                                SensoryFeedbackHelper.vibrateSuccess(context)
                                                onToggleTask(nextTask)
                                            },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = ExecutiveSuccessGreen,
                                                uncheckedColor = ExecutiveBorder
                                            ),
                                            modifier = Modifier.testTag("checkbox_next_task")
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = nextTask.title,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                color = ExecutiveTextWhite
                                            )
                                            Text(
                                                text = "${nextTask.priority.uppercase()} • Est: ${nextTask.estimatedMinutes}m • ${nextTask.category}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = ExecutiveTextMuted
                                            )
                                        }
                                    }
                                }
                            } else {
                                Text(
                                    text = "Todas las tareas del ciclo han sido completadas.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ExecutiveTextMuted
                                )
                            }

                            if (nextTimeBlock != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = ExecutiveDarkSurfaceVariant,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, ExecutiveSecondaryAmber.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(ExecutiveSecondaryAmber)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "BLOQUE ACTIVO: ${nextTimeBlock.startTime} - ${nextTimeBlock.endTime}",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = ExecutiveSecondaryAmber
                                            )
                                            Text(
                                                text = "${nextTimeBlock.title} (${nextTimeBlock.category})",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = ExecutiveTextWhite
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tiempo productivo registrado hoy: ${state.totalProductiveMinutesToday / 60}h ${state.totalProductiveMinutesToday % 60}m",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                color = ExecutiveTextMuted
                            )
                        }
                    }
                }

                // ========================================
                // 4. HÁBITOS (CONSISTENCIA OPERATIVA)
                // ========================================
                item {
                    LifeOsSectionHeader(
                        title = "Hábitos",
                        subtag = "Matriz de Consistencia",
                        indicatorColor = ExecutiveSuccessGreen
                    )
                }

                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = ExecutiveDarkSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ExecutiveBorder),
                        modifier = Modifier.fillMaxWidth().testTag("card_habits_status")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "CUMPLIMIENTO DIARIO",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = ExecutiveSuccessGreen
                                )
                                Text(
                                    text = "${state.habitsCompletedToday}/${state.habitsTotalToday} (${(state.habitsCompletionRate * 100).toInt()}%)",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    color = ExecutiveSuccessGreen
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { state.habitsCompletionRate },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = ExecutiveSuccessGreen,
                                trackColor = ExecutiveDarkSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Hábitos rápidos con toggle inmediato
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                state.habits.take(3).forEach { habit ->
                                    val isDone = state.habitLogsToday.any { it.habitId == habit.id && it.isCompleted }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = ExecutiveDarkSurfaceElevated,
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isDone) ExecutiveSuccessGreen.copy(alpha = 0.3f) else ExecutiveBorderSubtle
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    SensoryFeedbackHelper.vibrateSuccess(context)
                                                    onToggleHabit(habit.id)
                                                },
                                                modifier = Modifier.size(28.dp).testTag("quick_habit_${habit.id}")
                                            ) {
                                                Icon(
                                                    if (isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                                    contentDescription = habit.title,
                                                    tint = if (isDone) ExecutiveSuccessGreen else ExecutiveTextMuted,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = habit.title,
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                                color = ExecutiveTextWhite,
                                                modifier = Modifier.weight(1f)
                                            )
                                            LifeOsBadge(
                                                text = "🔥 ${habit.currentStreak}D",
                                                type = LifeOsBadgeType.ACTIVE
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            TextButton(
                                onClick = { onNavigate(AppNavTab.HABITS) },
                                modifier = Modifier.align(Alignment.End),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    "Matriz Completa →",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = ExecutiveSuccessGreen
                                )
                            }
                        }
                    }
                }

                // ========================================
                // 5. METAS (OBJETIVOS & OKRs)
                // ========================================
                item {
                    LifeOsSectionHeader(
                        title = "Metas",
                        subtag = "OKRs & Key Results",
                        indicatorColor = ExecutivePurple
                    )
                }

                item {
                    ExecutiveMetricCard(
                        title = "Objetivos Estratégicos",
                        question = "¿CÓMO VOY CON MIS METAS?",
                        value = "${(state.averageGoalsProgress * 100).toInt()}% de avance global",
                        detail = "${state.goalsWithSubGoals.size} metas activas • ${state.goalsWithSubGoals.sumOf { it.subGoals.count { sg -> sg.isCompleted } }} key results alcanzados",
                        icon = Icons.Default.Flag,
                        accentColor = ExecutivePurple,
                        onClick = { onNavigate(AppNavTab.GOALS) }
                    )
                }

                // ========================================
                // 6. FINANZAS (FLUJO & PATRIMONIO)
                // ========================================
                item {
                    LifeOsSectionHeader(
                        title = "Finanzas",
                        subtag = "Flujo Operativo & Balance",
                        indicatorColor = ExecutiveSecondaryAmber
                    )
                }

                item {
                    val patrimonioDona = state.assetsAndDebts
                        .filter { it.type == "ACTIVO" }
                        .sumOf { it.currentAmount }
                    val gastosTotal = state.totalExpenses
                    val ingresosTotal = state.totalIncome
                    val flujoNeto = state.netBalance

                    ExecutiveMetricCard(
                        title = "Control Financiero",
                        question = "¿CÓMO ESTÁ MI PATRIMONIO?",
                        value = "${if (flujoNeto >= 0) "+" else ""}$${String.format("%,.2f", flujoNeto)} flujo neto",
                        detail = "Ingresos: +$${String.format("%,.2f", ingresosTotal)} • Gastos: -$${String.format("%,.2f", gastosTotal)} • Patrimonio: $${String.format("%,.2f", patrimonioDona)}",
                        icon = Icons.Default.AccountBalanceWallet,
                        accentColor = ExecutiveSecondaryAmber,
                        onClick = { onNavigate(AppNavTab.FINANCE) }
                    )
                }

                // ========================================
                // ENTRENAMIENTO / RENDIMIENTO FÍSICO
                // ========================================
                item {
                    LifeOsSectionHeader(
                        title = "Entrenamiento",
                        badge = "${state.workoutSessions.size}",
                        subtag = "Sobrecarga & Vitalidad",
                        indicatorColor = TacticalNeonCyan,
                        actionLabel = "Ver Módulo",
                        onActionClick = onOpenFitness
                    )
                }

                item {
                    TacticalCard(
                        modifier = Modifier.fillMaxWidth().clickable { onOpenFitness() }.testTag("card_fitness_hub_preview"),
                        accentColor = TacticalNeonCyan
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                val latest = state.workoutSessions.firstOrNull()
                                Text(
                                    text = latest?.title ?: "Sin rutina registrada hoy",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ExecutiveTextPrimary
                                )
                                Text(
                                    text = if (latest != null) "${latest.durationMinutes} min • Meta: ${latest.targetGoal}" else "Pulsa para registrar una rutina o planificar series",
                                    fontSize = 12.sp,
                                    color = ExecutiveTextSecondary
                                )
                            }
                            FilledTonalButton(
                                onClick = onOpenFitness,
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = TacticalNeonCyan.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Abrir", fontSize = 11.sp, color = TacticalNeonCyan, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // ========================================
                // AUXILIAR: ASESOR ANALÍTICO GEMINI
                // ========================================
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = ExecutiveDarkSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ExecutiveBorderSubtle),
                        modifier = Modifier.fillMaxWidth().testTag("card_ai_advisor")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "IA Gemini",
                                        tint = ExecutiveCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "ASESOR EJECUTIVO GEMINI",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        ),
                                        color = ExecutiveCyan
                                    )
                                }
                                if (state.aiProductivityAdvice != null) {
                                    IconButton(
                                        onClick = onClearAiAdvice,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Cerrar",
                                            tint = ExecutiveTextMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            if (state.isGeneratingAiAdvice) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(
                                        color = ExecutiveCyan,
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Analizando métricas operativas del ciclo...",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ExecutiveTextMuted
                                    )
                                }
                            } else if (state.aiProductivityAdvice != null) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = ExecutiveDarkSurfaceElevated,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, ExecutiveCyan.copy(alpha = 0.25f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = state.aiProductivityAdvice,
                                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                                        color = ExecutiveTextWhite,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            } else {
                                Text(
                                    text = "Diagnóstico inteligente de tu ciclo con Gemini.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ExecutiveTextMuted
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onRequestAiAdvice,
                                    enabled = !state.isGeneratingAiAdvice,
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ExecutiveCyan),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, ExecutiveCyan.copy(alpha = 0.5f)),
                                    modifier = Modifier.weight(1f).testTag("btn_request_ai_advice")
                                ) {
                                    Icon(Icons.Default.Insights, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Diagnóstico", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                }

                                OutlinedButton(
                                    onClick = { showCustomQuestionDialog = true },
                                    enabled = !state.isGeneratingAiAdvice,
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ExecutiveTextWhite),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, ExecutiveBorder),
                                    modifier = Modifier.weight(1f).testTag("btn_ask_custom_ai")
                                ) {
                                    Icon(Icons.Default.QuestionAnswer, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Consultar", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    SensoryFeedbackHelper.vibrateLightTick(context)
                                    onOpenChat()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ExecutiveCyan.copy(alpha = 0.15f),
                                    contentColor = ExecutiveCyan
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ExecutiveCyan.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth().testTag("btn_open_full_chat")
                            ) {
                                Icon(Icons.Default.Forum, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Abrir Chatbot Multiturno con Gemini",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }
        }

        // FAB de Acción Rápida
        FloatingActionButton(
            onClick = {
                SensoryFeedbackHelper.vibrateLightTick(context)
                showQuickActions = true
            },
            containerColor = ExecutivePrimaryTeal,
            contentColor = Color.Black,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_quick_action")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Acción Rápida",
                modifier = Modifier.size(26.dp)
            )
        }

        // DIÁLOGO DE PROGRESIÓN Y LOGROS TÁCTICOS
        if (showProgressionDialog) {
            TacticalProgressionDialog(
                progression = state.userProgression,
                achievements = state.achievements,
                onDismiss = { showProgressionDialog = false }
            )
        }

        // PANTALLA DE BLOQUEO DE SISTEMA (Overlay Full-Screen auténtico)
        if (lockScreenAppTarget != null) {
            SystemLockScreenOverlay(
                visible = true,
                appName = lockScreenAppTarget?.limit?.appName ?: "Aplicación",
                usedMinutes = lockScreenAppTarget?.limit?.currentUsageMinutes ?: 30,
                budgetMinutes = lockScreenAppTarget?.limit?.dailyBudgetMinutes ?: 30,
                resetTime = state.cycleStartTime + ":00 AM",
                timeUntilReset = state.timeUntilNextCycle,
                isStrictMode = state.isStrictModeEnabled,
                onDismissToHub = { lockScreenAppTarget = null }
            )
        }

        // Dialog de Consulta Personalizada a la IA
        if (showCustomQuestionDialog) {
            AlertDialog(
                onDismissRequest = { showCustomQuestionDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ExecutiveCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Consulta a LifeOS AI")
                    }
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Realiza una consulta sobre tu desempeño, hábitos o enfoque en este ciclo:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = customQuestionText,
                            onValueChange = { customQuestionText = it },
                            placeholder = { Text("Ej: ¿En qué debería enfocarme esta tarde?") },
                            modifier = Modifier.fillMaxWidth().testTag("input_custom_ai_question"),
                            maxLines = 3
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (customQuestionText.isNotBlank()) {
                                onAskCustomAiQuestion(customQuestionText)
                                customQuestionText = ""
                                showCustomQuestionDialog = false
                            }
                        },
                        enabled = customQuestionText.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = ExecutiveCyan)
                    ) {
                        Text("Preguntar", color = ExecutiveDarkBg, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCustomQuestionDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // BottomSheet de Acciones Rápidas
        if (showQuickActions) {
            QuickActionBottomSheet(
                onDismiss = { showQuickActions = false },
                onAddTask = { title, priority, mins ->
                    SensoryFeedbackHelper.vibrateSuccess(context)
                    onAddTask(title, priority, mins)
                },
                onAddHabit = { title, category ->
                    SensoryFeedbackHelper.vibrateSuccess(context)
                    onAddHabit(title, category)
                },
                onAddTransaction = { type, amount, category, desc ->
                    SensoryFeedbackHelper.vibrateSuccess(context)
                    onAddTransaction(type, amount, category, desc)
                },
                onStartTimer = { activity ->
                    SensoryFeedbackHelper.vibrateSuccess(context)
                    onQuickStartTimer(activity)
                },
                onOpenFitness = {
                    SensoryFeedbackHelper.vibrateLightTick(context)
                    onOpenFitness()
                },
                onOpenChat = {
                    SensoryFeedbackHelper.vibrateLightTick(context)
                    onOpenChat()
                }
            )
        }
    }
}
