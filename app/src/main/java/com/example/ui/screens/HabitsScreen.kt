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
    onAddHabit: (String, String, String, Int, Long?, String) -> Unit,
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
            // Tarjeta de Recordatorio Diario
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
                                    text = "RECORDATORIO DIARIO",
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
            onDismiss = { showAddDialog = false },
            onConfirm = { title, cat, timeAmount, timeUnit ->
                onAddHabit(title, "DIARIO", cat, timeAmount, null, timeUnit)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AddHabitDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Salud") }
    var timeAmountInput by remember { mutableStateOf("30") }
    var timeUnit by remember { mutableStateOf("Minutos") } // "Minutos" o "Horas"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Crear Hábito") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nombre del hábito") },
                    modifier = Modifier.fillMaxWidth().testTag("dialog_habit_title")
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Categorías del hábito") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Campo "Tiempo del hábito" con selector de cantidad y unidad
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Tiempo del hábito",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = timeAmountInput,
                            onValueChange = { timeAmountInput = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Cantidad") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("dialog_habit_time_amount")
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = timeUnit == "Minutos",
                                onClick = { timeUnit = "Minutos" },
                                label = { Text("Minutos") }
                            )
                            FilterChip(
                                selected = timeUnit == "Horas",
                                onClick = { timeUnit = "Horas" },
                                label = { Text("Horas") }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = timeAmountInput.toIntOrNull()?.coerceAtLeast(1) ?: 30
                    val unitStr = if (timeUnit == "Horas") "horas" else "minutos"
                    onConfirm(title, category, amount, unitStr)
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
