package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.ui.PersonalOsUiState
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.util.SensoryFeedbackHelper

/**
 * Módulo de Bienestar Digital & Control de Atención LifeOS Precision Executive.
 */
@Composable
fun WellbeingScreen(
    state: PersonalOsUiState,
    onAddAppLimit: (String, String, Int) -> Unit,
    onSimulateUsage: (Long, Int) -> Unit,
    onSetStrictMode: (Boolean) -> Unit,
    onForceResetCycle: () -> Unit
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var lockScreenAppTarget by remember { mutableStateOf<EvaluatedAppBudget?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(ExecutiveDarkBg)) {
        Column(modifier = Modifier.fillMaxSize()) {
            ExecutiveHeader(
                title = "Control de Atención",
                subtitle = "Presupuestos de apps, bloqueo y modo estricto",
                cycleDate = state.currentCycleDate,
                timeUntilReset = state.timeUntilNextCycle,
                cycleProgress = state.cycleProgress,
                actionIcon = Icons.Default.Add,
                onActionClick = { showAddDialog = true }
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ========================================
                // MODO ESTRICTO [LOCK]
                // ========================================
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (state.isStrictModeEnabled) ExecutiveDangerRed.copy(alpha = 0.08f) else ExecutiveDarkSurface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (state.isStrictModeEnabled) ExecutiveDangerRed.copy(alpha = 0.4f) else ExecutiveBorder
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("card_strict_mode")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(if (state.isStrictModeEnabled) ExecutiveDangerRed else ExecutivePrimaryTeal)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "MODO ESTRICTO",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.sp
                                            ),
                                            color = if (state.isStrictModeEnabled) ExecutiveDangerRed else ExecutiveTextMuted
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = if (state.isStrictModeEnabled) "ACTIVO" else "INACTIVO",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 0.5.sp
                                                ),
                                                color = if (state.isStrictModeEnabled) ExecutiveDangerRed else ExecutiveTextWhite
                                            )
                                            if (state.isStrictModeEnabled) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "[LOCK]",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontFamily = FontFamily.Monospace,
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    color = ExecutiveDangerRed
                                                )
                                            }
                                        }
                                    }
                                }

                                Switch(
                                    checked = state.isStrictModeEnabled,
                                    onCheckedChange = {
                                        SensoryFeedbackHelper.vibrateAlert(context)
                                        onSetStrictMode(it)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = ExecutiveDangerRed,
                                        checkedTrackColor = ExecutiveDangerRed.copy(alpha = 0.3f),
                                        uncheckedThumbColor = ExecutiveTextMuted,
                                        uncheckedTrackColor = ExecutiveDarkSurfaceElevated
                                    ),
                                    modifier = Modifier.testTag("switch_strict_mode")
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = if (state.isStrictModeEnabled) {
                                    "Anti-evasión en rigor: No es posible aumentar cuotas ni liberar aplicaciones durante el ciclo activo hasta las ${state.cycleStartTime}. Tu atención queda asegurada por el sistema."
                                } else {
                                    "Modo Flexible: Las restricciones notifican y pausan pero admiten modificaciones inmediatas. Activa el Modo Estricto para imponer cumplimiento forzoso."
                                },
                                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                                color = ExecutiveTextMuted
                            )
                        }
                    }
                }

                // ========================================
                // TELEMETRÍA DEL CICLO OPERATIVO
                // ========================================
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = ExecutiveDarkSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ExecutiveBorderSubtle),
                        modifier = Modifier.fillMaxWidth().testTag("card_cycle_info")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "CICLO DIARIO: 05:00 → 04:59",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = ExecutiveSecondaryAmber
                                )
                                Text(
                                    text = "Reinicio en ${state.timeUntilNextCycle}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = ExecutiveSecondaryAmber
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "El ciclo operativo reinicia diariamente a las ${state.cycleStartTime}:00 AM. El consumo de minutos no se resetea a medianoche para coincidir con tu ritmo cognitivo.",
                                style = MaterialTheme.typography.bodySmall,
                                color = ExecutiveTextMuted
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedButton(
                                onClick = {
                                    SensoryFeedbackHelper.vibrateSuccess(context)
                                    onForceResetCycle()
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ExecutiveSecondaryAmber),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ExecutiveSecondaryAmber.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth().testTag("btn_force_reset_cycle")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Simular Reinicio de Ciclo (05:00)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }

                // ========================================
                // PRESUPUESTOS DE APLICACIONES
                // ========================================
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LifeOsSectionHeader(
                            title = "Aplicaciones",
                            subtag = "${state.appBudgets.size} Rastreadas",
                            indicatorColor = ExecutivePrimaryTeal
                        )

                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ExecutiveDangerRed,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("btn_add_app_limit")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Nuevo Límite", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }

                if (state.appBudgets.isEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ExecutiveDarkSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, ExecutiveBorderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "No hay límites configurados. Registra cuotas para YouTube, TikTok o Instagram para proteger tu tiempo.",
                                style = MaterialTheme.typography.bodySmall,
                                color = ExecutiveTextMuted,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                } else {
                    items(state.appBudgets, key = { it.limit.id }) { budget ->
                        AppLimitCard(
                            budget = budget,
                            onSimulate = { onSimulateUsage(budget.limit.id, 5) },
                            onInspectLock = { lockScreenAppTarget = budget }
                        )
                    }
                }
            }
        }

        // Pantalla de Bloqueo de Sistema
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

        if (showAddDialog) {
            AddAppLimitDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { name, pkg, budget ->
                    onAddAppLimit(name, pkg, budget)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
private fun AppLimitCard(
    budget: EvaluatedAppBudget,
    onSimulate: () -> Unit,
    onInspectLock: () -> Unit
) {
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
        shape = RoundedCornerShape(12.dp),
        color = if (isBlocked) ExecutiveDangerRed.copy(alpha = 0.08f) else ExecutiveDarkSurface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isBlocked) ExecutiveDangerRed.copy(alpha = 0.4f) else ExecutiveBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (isBlocked) onInspectLock()
            }
            .testTag("app_budget_card_${budget.limit.appName.lowercase()}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Fila de encabezado: Icono, Nombre y Badge de Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (isBlocked) ExecutiveDangerRed.copy(alpha = 0.15f) else ExecutiveDarkSurfaceElevated
                            )
                            .border(
                                1.dp,
                                if (isBlocked) ExecutiveDangerRed.copy(alpha = 0.3f) else ExecutiveBorderSubtle,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isBlocked) Icons.Default.Lock else Icons.Default.PhoneAndroid,
                            contentDescription = budget.limit.appName,
                            tint = if (isBlocked) ExecutiveDangerRed else ExecutivePrimaryTeal,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = budget.limit.appName,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = ExecutiveTextWhite
                        )
                        Text(
                            text = budget.limit.packageName,
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            color = ExecutiveTextMuted
                        )
                    }
                }

                LifeOsBadge(
                    text = badgeLabel,
                    type = badgeType
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Fila de telemetría: Uso actual / Presupuesto y Tiempo restante
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

            // Barra de consumo
            LinearProgressIndicator(
                progress = { budget.percentUsed },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = if (isBlocked) ExecutiveDangerRed else if (isNearLimit) ExecutiveSecondaryAmber else ExecutivePrimaryTeal,
                trackColor = ExecutiveDarkSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isBlocked) {
                    TextButton(
                        onClick = onInspectLock,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(12.dp), tint = ExecutiveDangerRed)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ver Pantalla de Bloqueo", style = MaterialTheme.typography.labelSmall, color = ExecutiveDangerRed)
                    }
                } else {
                    Text(
                        text = "Consumo dentro de cuota",
                        style = MaterialTheme.typography.labelSmall,
                        color = ExecutiveTextMuted
                    )
                }

                TextButton(
                    onClick = onSimulate,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.testTag("btn_simulate_${budget.limit.appName.lowercase()}")
                ) {
                    Icon(Icons.Default.HourglassBottom, contentDescription = null, modifier = Modifier.size(14.dp), tint = ExecutiveCyan)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+5 min (simular)", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = ExecutiveCyan)
                }
            }
        }
    }
}

@Composable
private fun AddAppLimitDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int) -> Unit
) {
    var appName by remember { mutableStateOf("") }
    var packageName by remember { mutableStateOf("") }
    var budgetMinutes by remember { mutableStateOf("30") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Security, contentDescription = null, tint = ExecutiveDangerRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nuevo Presupuesto de Atención")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = appName,
                    onValueChange = {
                        appName = it
                        if (packageName.isBlank()) {
                            packageName = "com.${it.lowercase().replace(" ", "")}.android"
                        }
                    },
                    label = { Text("Nombre de la App (ej: Instagram, YouTube)") },
                    modifier = Modifier.fillMaxWidth().testTag("dialog_app_name")
                )
                OutlinedTextField(
                    value = packageName,
                    onValueChange = { packageName = it },
                    label = { Text("Identificador de Paquete") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = budgetMinutes,
                    onValueChange = { budgetMinutes = it },
                    label = { Text("Presupuesto diario (minutos)") },
                    modifier = Modifier.fillMaxWidth().testTag("dialog_app_budget")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val b = budgetMinutes.toIntOrNull() ?: 30
                    if (appName.isNotBlank() && b > 0) onConfirm(appName, packageName, b)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ExecutiveDangerRed),
                modifier = Modifier.testTag("dialog_limit_confirm")
            ) {
                Text("Fijar Presupuesto", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
