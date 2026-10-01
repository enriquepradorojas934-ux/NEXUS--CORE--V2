package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.R
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
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

    // Control reactivo del estado de completado de cada meta
    var manuallyCompletedGoalIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var manuallyUncompletedGoalIds by remember { mutableStateOf<Set<Long>>(emptySet()) }

    val isGoalCompleted: (GoalWithSubGoals) -> Boolean = remember(manuallyCompletedGoalIds, manuallyUncompletedGoalIds) {
        { item ->
            val id = item.goal.id
            if (id in manuallyCompletedGoalIds) {
                true
            } else if (id in manuallyUncompletedGoalIds) {
                false
            } else {
                item.goal.status == "COMPLETADA" || (item.subGoals.isNotEmpty() && item.progressPercent >= 1.0f)
            }
        }
    }

    // Ordenamiento: Metas activas arriba, metas completadas automáticamente al fondo de la lista
    val (activeGoals, completedGoals) = remember(state.goalsWithSubGoals, manuallyCompletedGoalIds, manuallyUncompletedGoalIds) {
        val active = state.goalsWithSubGoals.filter { !isGoalCompleted(it) }
        val completed = state.goalsWithSubGoals.filter { isGoalCompleted(it) }
        Pair(active, completed)
    }

    val sortedGoals = remember(activeGoals, completedGoals) {
        activeGoals + completedGoals
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ExecutiveHeader(
            title = androidx.compose.ui.res.stringResource(com.example.R.string.goals_title),
            subtitle = androidx.compose.ui.res.stringResource(com.example.R.string.goals_subtitle),
            cycleDate = state.currentCycleDate,
            timeUntilReset = state.timeUntilNextCycle,
            cycleProgress = state.cycleProgress,
            actionIcon = Icons.Default.Add,
            onActionClick = { showAddGoalDialog = true }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Metas Estratégicas (${state.goalsWithSubGoals.size})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (state.goalsWithSubGoals.isNotEmpty()) {
                            Text(
                                text = "${activeGoals.size} activas • ${completedGoals.size} completadas",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Button(
                        onClick = { showAddGoalDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = ExecutivePurple),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("btn_create_goal")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.goals_new_goal), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (state.goalsWithSubGoals.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Flag, contentDescription = null, tint = ExecutivePurple, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No hay metas registradas",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Crea una meta y desglósala en subobjetivos medibles para visualizar tu avance.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(sortedGoals, key = { it.goal.id }) { item ->
                    val isCompleted = isGoalCompleted(item)
                    GoalCard(
                        item = item,
                        isCompleted = isCompleted,
                        modifier = Modifier.animateItem(),
                        onToggleGoalCompletion = {
                            val id = item.goal.id
                            if (isCompleted) {
                                manuallyCompletedGoalIds = manuallyCompletedGoalIds - id
                                manuallyUncompletedGoalIds = manuallyUncompletedGoalIds + id
                                // Desmarcar submetas
                                item.subGoals.filter { it.isCompleted }.forEach { onToggleSubGoal(it) }
                            } else {
                                manuallyUncompletedGoalIds = manuallyUncompletedGoalIds - id
                                manuallyCompletedGoalIds = manuallyCompletedGoalIds + id
                                // Marcar submetas pendientes como completadas
                                item.subGoals.filter { !it.isCompleted }.forEach { onToggleSubGoal(it) }
                            }
                        },
                        onAddSubGoalClick = { subGoalDialogGoalId = item.goal.id },
                        onToggleSubGoal = { sub ->
                            manuallyCompletedGoalIds = manuallyCompletedGoalIds - item.goal.id
                            manuallyUncompletedGoalIds = manuallyUncompletedGoalIds - item.goal.id
                            onToggleSubGoal(sub)
                        },
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

// =========================================================================
// TARJETA DE META: FORMATO INTERMEDIO (TIPO LISTA DE TAREAS / NOTAS)
// =========================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GoalCard(
    item: GoalWithSubGoals,
    isCompleted: Boolean,
    modifier: Modifier = Modifier,
    onToggleGoalCompletion: () -> Unit,
    onAddSubGoalClick: () -> Unit,
    onToggleSubGoal: (SubGoalEntity) -> Unit,
    onDeleteGoal: () -> Unit,
    onToggleHabit: (Long) -> Unit = {}
) {
    val totalSubs = item.subGoals.size
    val doneSubs = if (isCompleted && item.subGoals.isNotEmpty() && item.subGoals.all { it.isCompleted }) {
        totalSubs
    } else {
        item.subGoals.count { it.isCompleted }
    }

    val effectiveProgress = if (isCompleted) 1.0f else item.progressPercent
    val percentInt = (effectiveProgress * 100).toInt().coerceIn(0, 100)

    val animatedProgress by animateFloatAsState(
        targetValue = effectiveProgress,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "goalProgressAnimation"
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            1.dp,
            if (isCompleted) ExecutiveSuccessGreen.copy(alpha = 0.35f) else ExecutivePurple.copy(alpha = 0.25f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("goal_card_${item.goal.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Cabecera: Círculo de completado + Título y Descripción + Papelera arriba a la derecha
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.Top
                ) {
                    // Círculo interactivo junto al título:
                    // Verde con check si completada; círculo vacío (solo borde) si activa/pendiente
                    IconButton(
                        onClick = onToggleGoalCompletion,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("goal_complete_toggle_${item.goal.id}")
                    ) {
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Desmarcar meta",
                                tint = ExecutiveSuccessGreen,
                                modifier = Modifier.size(28.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.RadioButtonUnchecked,
                                contentDescription = "Completar meta",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.goal.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (item.goal.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.goal.description,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Ícono de eliminar (papelera) arriba a la derecha
                IconButton(
                    onClick = onDeleteGoal,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("goal_delete_${item.goal.id}")
                ) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "Eliminar meta",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Fila de chips con FlowRow: categoría, conteo de hitos, fecha objetivo y porcentaje
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Chip de categoría
                Surface(
                    color = ExecutivePurple.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = item.goal.category,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        ),
                        color = ExecutivePurple,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Chip de conteo de hitos
                if (totalSubs > 0) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.Flag,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = androidx.compose.ui.res.pluralStringResource(com.example.R.plurals.milestones_count, totalSubs, "$doneSubs/$totalSubs"),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Chip de fecha objetivo
                if (item.goal.targetDate.isNotBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = item.goal.targetDate,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Chip de porcentaje: 100% en verde, avance real en violeta
                val isFullyComplete = isCompleted || percentInt >= 100
                Surface(
                    color = if (isFullyComplete) ExecutiveSuccessGreen.copy(alpha = 0.2f) else ExecutivePurple.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "$percentInt%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = if (isFullyComplete) ExecutiveSuccessGreen else ExecutivePurple,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Barra de progreso: verde si 100%, violeta si < 100%
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isCompleted || effectiveProgress >= 1f) ExecutiveSuccessGreen else ExecutivePurple,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Sección "Hitos y Subobjetivos" SIEMPRE VISIBLE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.example.R.string.goals_milestones_title),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(
                    onClick = onAddSubGoalClick,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = ExecutivePurple,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        androidx.compose.ui.res.stringResource(com.example.R.string.goals_add_milestone),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        ),
                        color = ExecutivePurple
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (item.subGoals.isEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Sin subobjetivos definidos. Pulsa '+ Agregar hito' para desglosar la meta.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    item.subGoals.forEach { sub ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleSubGoal(sub) }
                                .testTag("subgoal_row_${sub.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Círculo violeta con check si completado, círculo vacío si pendiente
                                if (sub.isCompleted) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Hito completado",
                                        tint = ExecutivePurple,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.RadioButtonUnchecked,
                                        contentDescription = "Hito pendiente",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                // Texto limpio sin tachado
                                Text(
                                    text = sub.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                    color = if (sub.isCompleted) {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // Hábitos vinculados si existen
            if (item.associatedHabits.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Hábitos vinculados:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = ExecutiveSuccessGreen
                )
                Spacer(modifier = Modifier.height(4.dp))
                item.associatedHabits.forEach { habitItem ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clickable { onToggleHabit(habitItem.habit.id) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (habitItem.isCompletedToday) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (habitItem.isCompletedToday) ExecutiveSuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = habitItem.habit.title,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = if (habitItem.isCompletedToday) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Profesional") }
    var targetDate by remember { mutableStateOf("2026-12-31") }

    val datePickerDialog = remember {
        val parts = targetDate.split("-")
        val y = parts.getOrNull(0)?.toIntOrNull() ?: 2026
        val m = (parts.getOrNull(1)?.toIntOrNull() ?: 12) - 1
        val d = parts.getOrNull(2)?.toIntOrNull() ?: 31
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                targetDate = "%04d-%02d-%02d".format(year, month + 1, dayOfMonth)
            },
            y, m, d
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.goals_create_title), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.goals_name_label)) },
                    modifier = Modifier.fillMaxWidth().testTag("dialog_goal_title")
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.goals_desc_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text(stringResource(R.string.goals_category_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = targetDate,
                    onValueChange = { targetDate = it },
                    label = { Text(stringResource(R.string.goals_date_label)) },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { datePickerDialog.show() }) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = stringResource(R.string.goals_date_label),
                                tint = ExecutivePurple
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePickerDialog.show() }
                        .testTag("dialog_goal_date_picker")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(title, description, category, targetDate) },
                colors = ButtonDefaults.buttonColors(containerColor = ExecutivePurple),
                modifier = Modifier.testTag("dialog_goal_confirm")
            ) {
                Text(stringResource(R.string.goals_create_goal_btn), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
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
        title = { Text(stringResource(R.string.goals_new_subgoal_title), fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.goals_subgoal_desc_label)) },
                modifier = Modifier.fillMaxWidth().testTag("dialog_subgoal_title")
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(title) },
                colors = ButtonDefaults.buttonColors(containerColor = ExecutivePurple),
                modifier = Modifier.testTag("dialog_subgoal_confirm")
            ) {
                Text(stringResource(R.string.goals_add_btn), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )
}
