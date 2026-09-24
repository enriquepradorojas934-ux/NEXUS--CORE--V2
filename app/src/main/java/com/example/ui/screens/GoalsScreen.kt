package com.example.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GoalEntity
import com.example.data.model.SubGoalEntity
import com.example.ui.GoalWithSubGoals
import com.example.ui.PersonalOsUiState
import com.example.ui.components.ExecutiveHeader
import com.example.ui.theme.*

@Composable
fun GoalsScreen(
    state: PersonalOsUiState,
    onAddGoal: (String, String, String, String) -> Unit,
    onAddSubGoal: (Long, String) -> Unit,
    onToggleSubGoal: (SubGoalEntity) -> Unit,
    onDeleteGoal: (GoalEntity) -> Unit,
    onToggleHabit: (Long) -> Unit = {}
) {
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var subGoalDialogGoalId by remember { mutableStateOf<Long?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        ExecutiveHeader(
            title = "Módulo de Metas",
            subtitle = "Objetivos estratégicos y desglose de submetas",
            cycleDate = state.currentCycleDate,
            timeUntilReset = state.timeUntilNextCycle,
            cycleProgress = state.cycleProgress,
            actionIcon = Icons.Default.Add,
            onActionClick = { showAddGoalDialog = true }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Metas en Desarrollo (${state.goalsWithSubGoals.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Button(
                        onClick = { showAddGoalDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = ExecutivePurple),
                        modifier = Modifier.testTag("btn_create_goal")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Nueva Meta")
                    }
                }
            }

            if (state.goalsWithSubGoals.isEmpty()) {
                item {
                    Text(
                        text = "No hay metas registradas. Crea una meta general y divídela en subobjetivos medibles.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            } else {
                items(state.goalsWithSubGoals, key = { it.goal.id }) { item ->
                    GoalCard(
                        item = item,
                        onAddSubGoalClick = { subGoalDialogGoalId = item.goal.id },
                        onToggleSubGoal = onToggleSubGoal,
                        onDeleteGoal = { onDeleteGoal(item.goal) },
                        onToggleHabit = onToggleHabit
                    )
                }
            }
        }
    }

    if (showAddGoalDialog) {
        AddGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onConfirm = { title, desc, cat, date ->
                onAddGoal(title, desc, cat, date)
                showAddGoalDialog = false
            }
        )
    }

    if (subGoalDialogGoalId != null) {
        AddSubGoalDialog(
            onDismiss = { subGoalDialogGoalId = null },
            onConfirm = { title ->
                onAddSubGoal(subGoalDialogGoalId!!, title)
                subGoalDialogGoalId = null
            }
        )
    }
}

@Composable
private fun GoalCard(
    item: GoalWithSubGoals,
    onAddSubGoalClick: () -> Unit,
    onToggleSubGoal: (SubGoalEntity) -> Unit,
    onDeleteGoal: () -> Unit,
    onToggleHabit: (Long) -> Unit = {}
) {
    val animatedProgress by animateFloatAsState(
        targetValue = item.progressPercent,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "goalProgressAnimation"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().testTag("goal_card_${item.goal.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = ExecutivePurple.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = item.goal.category,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = ExecutivePurple,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (item.goal.targetDate.isNotBlank()) {
                        Text(
                            text = "Objetivo: ${item.goal.targetDate}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDeleteGoal, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.goal.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            if (item.goal.description.isNotBlank()) {
                Text(
                    text = item.goal.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Barra de Progreso Dinámica e Interactiva de la Meta
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Progreso total de la meta",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (item.associatedHabits.isNotEmpty() && item.subGoals.isNotEmpty()) {
                        Text(
                            text = "Hitos: ${(item.subGoalsProgress * 100).toInt()}% • Hábitos hoy: ${(item.habitsProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = ExecutivePurple.copy(alpha = 0.8f)
                        )
                    }
                }
                Text(
                    text = "${(animatedProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = ExecutivePurple
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = ExecutivePurple,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            // Sección de Hábitos Asociados si existen
            if (item.associatedHabits.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Hábitos diarios vinculados:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = ExecutiveSuccessGreen
                    )
                    Text(
                        text = "${item.associatedHabits.count { it.isCompletedToday }} de ${item.associatedHabits.size} completados",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                item.associatedHabits.forEach { habitItem ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clickable { onToggleHabit(habitItem.habit.id) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onToggleHabit(habitItem.habit.id) },
                            modifier = Modifier.size(32.dp).testTag("goal_habit_toggle_${habitItem.habit.id}")
                        ) {
                            Icon(
                                if (habitItem.isCompletedToday) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = habitItem.habit.title,
                                tint = if (habitItem.isCompletedToday) ExecutiveSuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = habitItem.habit.title,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (habitItem.isCompletedToday) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        if (habitItem.habit.currentStreak > 0) {
                            Text(
                                text = "🔥 ${habitItem.habit.currentStreak}d",
                                style = MaterialTheme.typography.labelSmall.copy(color = ExecutiveSecondaryAmber)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Lista de Subobjetivos
            Text(
                text = "Hitos y Subobjetivos:",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            if (item.subGoals.isEmpty()) {
                Text(
                    text = "Sin subobjetivos definidos aún.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                item.subGoals.forEach { sub ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = sub.isCompleted,
                            onCheckedChange = { onToggleSubGoal(sub) },
                            colors = CheckboxDefaults.colors(checkedColor = ExecutivePurple),
                            modifier = Modifier.size(24.dp).testTag("subgoal_checkbox_${sub.id}")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = sub.title,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (sub.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = onAddSubGoalClick,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Agregar Subobjetivo", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun AddGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Profesional") }
    var targetDate by remember { mutableStateOf("2026-12-31") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Crear Nueva Meta") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nombre de la meta") },
                    modifier = Modifier.fillMaxWidth().testTag("dialog_goal_title")
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción o propósito") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Categoría (ej: Carrera, Finanzas, Salud)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = targetDate,
                    onValueChange = { targetDate = it },
                    label = { Text("Fecha objetivo (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(title, description, category, targetDate) },
                modifier = Modifier.testTag("dialog_goal_confirm")
            ) {
                Text("Crear Meta")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun AddSubGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var title by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Subobjetivo") },
        text = {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Descripción del hito o submeta") },
                modifier = Modifier.fillMaxWidth().testTag("dialog_subgoal_title")
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(title) },
                modifier = Modifier.testTag("dialog_subgoal_confirm")
            ) {
                Text("Agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
