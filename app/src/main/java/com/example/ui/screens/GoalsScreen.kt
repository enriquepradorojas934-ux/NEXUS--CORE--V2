package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.text.font.FontWeight
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
            title = "Módulo de Metas",
            subtitle = "Objetivos estratégicos y desglose de submetas",
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
                        Text("Nueva Meta", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
    if (isCompleted) {
        CompletedGoalCard(
            item = item,
            modifier = modifier,
            onToggleGoalCompletion = onToggleGoalCompletion,
            onDeleteGoal = onDeleteGoal,
            onToggleSubGoal = onToggleSubGoal
        )
        return
    }

    var isExpanded by remember { mutableStateOf(false) }

    val effectiveProgress = item.progressPercent
    val animatedProgress by animateFloatAsState(
        targetValue = effectiveProgress,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "goalProgressAnimation"
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            ExecutivePurple.copy(alpha = 0.22f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("goal_card_${item.goal.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            // Fila principal compacta estilo checklist / nota ejecutiva
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Checkbox / Toggle de completado directo
                IconButton(
                    onClick = onToggleGoalCompletion,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("goal_complete_toggle_${item.goal.id}")
                ) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = if (isCompleted) "Desmarcar meta" else "Completar meta",
                        tint = if (isCompleted) ExecutiveSuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Contenido central: Título en negrita (+ tachado si completada), subtítulo y metadatos
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { isExpanded = !isExpanded }
                ) {
                    Text(
                        text = item.goal.title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.Normal,
                            textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        color = if (isCompleted) {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (item.goal.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.goal.description,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.Normal,
                                textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Badges de metadatos en formato intermedio
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = if (isCompleted) MaterialTheme.colorScheme.surfaceVariant else ExecutivePurple.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = item.goal.category,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else ExecutivePurple,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }

                        if (item.subGoals.isNotEmpty()) {
                            val doneSubs = if (isCompleted) item.subGoals.size else item.subGoals.count { it.isCompleted }
                            Text(
                                text = "$doneSubs/${item.subGoals.size} hitos",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                maxLines = 1,
                                softWrap = false,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text("•", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        if (item.goal.targetDate.isNotBlank()) {
                            Text(
                                text = item.goal.targetDate,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text("•", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Spacer(modifier = Modifier.weight(1f, fill = false))

                        Surface(
                            color = ExecutivePurple.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.defaultMinSize(minWidth = 38.dp)
                        ) {
                            Text(
                                text = "${(effectiveProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = ExecutivePurple
                                ),
                                maxLines = 1,
                                softWrap = false,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Barra de progreso compacta (4dp de espesor)
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(CircleShape),
                        color = if (isCompleted) ExecutiveSuccessGreen else ExecutivePurple,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Acciones a la derecha: Desplegar detalles y eliminar
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(32.dp).testTag("goal_expand_${item.goal.id}")
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Colapsar" else "Ver detalles",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onDeleteGoal,
                        modifier = Modifier.size(32.dp).testTag("goal_delete_${item.goal.id}")
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Vista expandida con detalles de submetas y hábitos (Acordeón limpio y compacto)
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, start = 6.dp, end = 4.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))

                    // Hábitos vinculados si existen
                    if (item.associatedHabits.isNotEmpty()) {
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
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Lista de Subobjetivos / Hitos
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Hitos y Subobjetivos:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(
                            onClick = onAddSubGoalClick,
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("Agregar hito", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp))
                        }
                    }

                    if (item.subGoals.isEmpty()) {
                        Text(
                            text = "Sin subobjetivos definidos. Pulsa 'Agregar hito' para desglosar la meta.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
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
                                    modifier = Modifier.size(20.dp).testTag("subgoal_checkbox_${sub.id}")
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = sub.title,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 12.sp,
                                        textDecoration = if (sub.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                        color = if (sub.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// TARJETA DE META COMPLETADA: REDISEÑO SEGÚN ESPECIFICACIÓN Y REFERENCIA VISUAL
// =========================================================================

@Composable
private fun CompletedGoalCard(
    item: GoalWithSubGoals,
    modifier: Modifier = Modifier,
    onToggleGoalCompletion: () -> Unit,
    onDeleteGoal: () -> Unit,
    onToggleSubGoal: (SubGoalEntity) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = ExecutiveDarkSurface
        ),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ExecutiveSuccessGreen.copy(alpha = 0.35f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("completed_goal_card_${item.goal.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Estado 2: META COMPLETADA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = ExecutiveSuccessGreen.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "ESTADO 2: META COMPLETADA",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            letterSpacing = 0.6.sp
                        ),
                        color = ExecutiveSuccessGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                IconButton(
                    onClick = onDeleteGoal,
                    modifier = Modifier.size(28.dp).testTag("goal_delete_${item.goal.id}")
                ) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "Eliminar meta",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1. Ícono circular de check en verde + 2. Título de la meta en texto grande y en negrita (ej: "GRADUARME")
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(ExecutiveSuccessGreen, CircleShape)
                        .clickable { onToggleGoalCompletion() }
                        .testTag("goal_complete_toggle_${item.goal.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Meta completada",
                        tint = ExecutiveDarkBg,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = item.goal.title.uppercase(),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }

            // 3. Debajo, la descripción de la meta en texto normal
            if (item.goal.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = item.goal.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Una fila de chips/badges compactos con: categoría, conteo de hitos completados, fecha objetivo y porcentaje ("100%")
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Categoría
                Surface(
                    color = ExecutivePurple.copy(alpha = 0.22f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Work,
                            contentDescription = null,
                            tint = ExecutivePurple,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = item.goal.category,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = ExecutivePurple
                        )
                    }
                }

                // Hitos completados (ej: "3/3 hitos")
                val totalHitos = item.subGoals.size
                val completedHitos = if (totalHitos == 0) 1 else item.subGoals.count { it.isCompleted }.coerceAtLeast(totalHitos)
                val displayTotal = if (totalHitos == 0) 1 else totalHitos
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Checklist,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$completedHitos/$displayTotal hitos",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Fecha objetivo
                if (item.goal.targetDate.isNotBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = item.goal.targetDate,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Porcentaje 100% (completamente legible, sin recortes)
                Surface(
                    color = ExecutiveSuccessGreen.copy(alpha = 0.20f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = ExecutiveSuccessGreen,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "100%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp
                            ),
                            color = ExecutiveSuccessGreen,
                            softWrap = false
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 5. Una barra de progreso debajo de los chips (llena y en verde cuando está al 100%)
            LinearProgressIndicator(
                progress = { 1.0f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = ExecutiveSuccessGreen,
                trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 6. Sección "Hitos y Subobjetivos" que liste cada hito de esa meta con su propio check si está completado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hitos y Subobjetivos",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    Icons.Default.FormatListBulleted,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (item.subGoals.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TacticalNeonCyan.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(ExecutivePurple, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Meta principal completada",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item.subGoals.forEach { sub ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (sub.isCompleted) TacticalNeonCyan.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth().clickable { onToggleSubGoal(sub) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(
                                            if (sub.isCompleted) ExecutivePurple else Color.Transparent,
                                            CircleShape
                                        )
                                        .border(
                                            1.dp,
                                            if (sub.isCompleted) ExecutivePurple else MaterialTheme.colorScheme.onSurfaceVariant,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (sub.isCompleted) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = sub.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
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
        title = { Text("Crear Nueva Meta", fontWeight = FontWeight.Bold) },
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
                    label = { Text("Categoría de meta") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = targetDate,
                    onValueChange = { targetDate = it },
                    label = { Text("Fecha") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { datePickerDialog.show() }) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = "Seleccionar fecha",
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
                Text("Crear Meta", fontWeight = FontWeight.Bold)
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
        title = { Text("Nuevo Subobjetivo", fontWeight = FontWeight.Bold) },
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
                colors = ButtonDefaults.buttonColors(containerColor = ExecutivePurple),
                modifier = Modifier.testTag("dialog_subgoal_confirm")
            ) {
                Text("Agregar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
