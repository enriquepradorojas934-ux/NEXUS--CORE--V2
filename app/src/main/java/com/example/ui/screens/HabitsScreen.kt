package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.model.HabitEntity
import com.example.ui.PersonalOsUiState
import com.example.ui.components.DailyHabitsListComponent
import com.example.ui.components.ExecutiveHeader
import com.example.ui.theme.*

@Composable
fun HabitsScreen(
    state: PersonalOsUiState,
    onToggleHabit: (Long) -> Unit,
    onAddHabit: (String, String, String, Int, Long?) -> Unit,
    onDeleteHabit: (HabitEntity) -> Unit,
    onSetHabitReminder: (Boolean, String) -> Unit = { _, _ -> },
    onTestHabitReminder: () -> Unit = {}
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var reminderTimeInput by remember(state.habitReminderTime) { mutableStateOf(state.habitReminderTime) }
    val context = LocalContext.current

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onSetHabitReminder(true, reminderTimeInput)
        }
    }

    val testNotificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onTestHabitReminder()
        }
    }

    fun requestAndToggleReminder(enable: Boolean) {
        if (enable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }
        onSetHabitReminder(enable, reminderTimeInput)
    }

    fun requestAndTriggerTest() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                testNotificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }
        onTestHabitReminder()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ExecutiveHeader(
            title = "Módulo de Hábitos",
            subtitle = "Consistencia, objetivos y registro de rachas",
            cycleDate = state.currentCycleDate,
            timeUntilReset = state.timeUntilNextCycle,
            cycleProgress = state.cycleProgress,
            actionIcon = Icons.Default.Add,
            onActionClick = { showAddDialog = true }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Tarjeta de Recordatorio Diario (WorkManager)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().testTag("habits_reminder_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = ExecutiveSecondaryAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "RECORDATORIO DIARIO (WORKMANAGER)",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = ExecutiveSecondaryAmber
                                )
                            }
                            Switch(
                                checked = state.isHabitReminderEnabled,
                                onCheckedChange = { enabled ->
                                    requestAndToggleReminder(enabled)
                                },
                                modifier = Modifier.testTag("switch_habit_reminder")
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Recibe una alerta periódica programada para revisar y marcar tus hábitos pendientes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (state.isHabitReminderEnabled) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = reminderTimeInput,
                                    onValueChange = { reminderTimeInput = it },
                                    label = { Text("Hora (HH:mm)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f).testTag("input_reminder_time")
                                )
                                Button(
                                    onClick = { requestAndToggleReminder(true) },
                                    colors = ButtonDefaults.buttonColors(containerColor = ExecutiveSecondaryAmber),
                                    modifier = Modifier.testTag("btn_save_reminder_time")
                                ) {
                                    Text("Fijar", color = ExecutiveDarkBg, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = { requestAndTriggerTest() },
                            modifier = Modifier.fillMaxWidth().testTag("btn_test_reminder")
                        ) {
                            Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Enviar Alerta de Prueba Ahora", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mis Hábitos del Día",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = ExecutiveSuccessGreen),
                        modifier = Modifier.testTag("btn_add_habit")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Nuevo Hábito")
                    }
                }
            }

            item {
                DailyHabitsListComponent(
                    habits = state.habits,
                    habitLogsToday = state.habitLogsToday,
                    onToggleHabit = onToggleHabit,
                    onDeleteHabit = onDeleteHabit,
                    onAddHabitClick = { showAddDialog = true },
                    showSummaryCard = true,
                    showFilterChips = true
                )
            }
        }
    }

    if (showAddDialog) {
        AddHabitDialog(
            goals = state.goalsWithSubGoals.map { it.goal },
            onDismiss = { showAddDialog = false },
            onConfirm = { title, freq, cat, target, goalId ->
                onAddHabit(title, freq, cat, target, goalId)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AddHabitDialog(
    goals: List<com.example.data.model.GoalEntity>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Int, Long?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("DIARIO") }
    var category by remember { mutableStateOf("Salud") }
    var targetValue by remember { mutableStateOf("1") }
    var selectedGoalId by remember { mutableStateOf<Long?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Crear Hábito") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nombre del hábito (ej: Ejercicio, Lectura)") },
                    modifier = Modifier.fillMaxWidth().testTag("dialog_habit_title")
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Categoría (ej: Salud, Estudio, Rutina)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = targetValue,
                    onValueChange = { targetValue = it },
                    label = { Text("Objetivo numérico (ej: 30 min, 1 vez)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("DIARIO", "DIAS_LABORABLES", "SEMANAL").forEach { f ->
                        FilterChip(
                            selected = frequency == f,
                            onClick = { frequency = f },
                            label = { Text(f.replace("_", " ")) }
                        )
                    }
                }

                if (goals.isNotEmpty()) {
                    Text(
                        text = "Vincular a una meta (opcional):",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedGoalId == null,
                            onClick = { selectedGoalId = null },
                            label = { Text("Ninguna") }
                        )
                        goals.take(2).forEach { goal ->
                            FilterChip(
                                selected = selectedGoalId == goal.id,
                                onClick = { selectedGoalId = goal.id },
                                label = { Text(goal.title.take(15)) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val target = targetValue.toIntOrNull() ?: 1
                    onConfirm(title, frequency, category, target, selectedGoalId)
                },
                modifier = Modifier.testTag("dialog_habit_confirm")
            ) {
                Text("Guardar Hábito")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
