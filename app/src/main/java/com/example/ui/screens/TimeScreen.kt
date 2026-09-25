package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import com.example.data.model.TaskEntity
import com.example.data.model.TimeBlockEntity
import com.example.ui.PersonalOsUiState
import com.example.ui.components.ExecutiveHeader
import com.example.ui.components.LifeOsBadge
import com.example.ui.components.LifeOsBadgeType
import com.example.ui.components.LifeOsSectionHeader
import com.example.ui.theme.*
import com.example.util.SensoryFeedbackHelper

/**
 * Módulo de Tiempo & Agenda LifeOS Precision Executive.
 * Línea temporal visual de precisión con diferenciación por categorías:
 * FOCO, TRABAJO, ESTUDIO, ENTRENAMIENTO, DESCANSO, OTROS.
 */
@Composable
fun TimeScreen(
    state: PersonalOsUiState,
    onAddTask: (String, String, Int, String) -> Unit,
    onToggleTask: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    onAddTimeBlock: (String, String, String, String) -> Unit,
    onToggleTimeBlock: (TimeBlockEntity) -> Unit,
    onDeleteTimeBlock: (TimeBlockEntity) -> Unit,
    onStartFocusTimer: (String, String) -> Unit,
    onPauseFocusTimer: () -> Unit,
    onFinishFocusTimer: () -> Unit
) {
    var selectedSection by remember { mutableIntStateOf(1) } // 0: Tareas, 1: Bloques (Línea Temporal), 2: Cronómetro, 3: Estadísticas
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showAddBlockDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(ExecutiveDarkBg)) {
        ExecutiveHeader(
            title = "Gestión del Tiempo",
            subtitle = "Línea temporal, bloques y cronómetro operativo",
            cycleDate = state.currentCycleDate,
            timeUntilReset = state.timeUntilNextCycle,
            cycleProgress = state.cycleProgress,
            actionIcon = Icons.Default.Add,
            onActionClick = {
                if (selectedSection == 0) showAddTaskDialog = true else showAddBlockDialog = true
            }
        )

        // Selector de Pestañas HUD con tipografía técnica
        TabRow(
            selectedTabIndex = selectedSection,
            containerColor = ExecutiveDarkSurface,
            contentColor = ExecutivePrimaryTeal,
            divider = { HorizontalDivider(color = ExecutiveBorderSubtle) }
        ) {
            val tabs = listOf(
                "Línea Temporal (${state.timeBlocks.size})",
                "Tareas (${state.tasks.count { !it.isCompleted }})",
                "Tracker",
                "Métricas"
            )
            val mapping = listOf(1, 0, 2, 3)

            mapping.forEachIndexed { index, targetSection ->
                Tab(
                    selected = selectedSection == targetSection,
                    onClick = { selectedSection = targetSection },
                    text = {
                        Text(
                            text = tabs[index],
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (selectedSection == targetSection) FontWeight.Bold else FontWeight.Medium,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = if (selectedSection == targetSection) ExecutivePrimaryTeal else ExecutiveTextMuted
                        )
                    },
                    modifier = Modifier.testTag("tab_time_$targetSection")
                )
            }
        }

        when (selectedSection) {
            0 -> TasksListSection(
                tasks = state.tasks,
                onToggle = onToggleTask,
                onDelete = onDeleteTask,
                onAddNew = { showAddTaskDialog = true }
            )
            1 -> TimeTimelineSection(
                blocks = state.timeBlocks,
                onToggle = onToggleTimeBlock,
                onDelete = onDeleteTimeBlock,
                onAddNew = { showAddBlockDialog = true }
            )
            2 -> FocusTrackerSection(
                timerState = state.focusTimer,
                onStart = onStartFocusTimer,
                onPause = onPauseFocusTimer,
                onFinish = onFinishFocusTimer
            )
            3 -> TimeStatsSection(
                totalMinutes = state.totalProductiveMinutesToday,
                entries = state.timeEntriesToday,
                tasks = state.tasks
            )
        }
    }

    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { title, priority, est, cat ->
                onAddTask(title, priority, est, cat)
                showAddTaskDialog = false
            }
        )
    }

    if (showAddBlockDialog) {
        AddTimeBlockDialog(
            onDismiss = { showAddBlockDialog = false },
            onConfirm = { title, start, end, cat ->
                onAddTimeBlock(title, start, end, cat)
                showAddBlockDialog = false
            }
        )
    }
}

/**
 * LÍNEA TEMPORAL VISUAL EXTREMADAMENTE CLARA
 * Muestra: Hora, Actividad, Duración, Estado.
 * Diferencia visualmente: FOCO, TRABAJO, ESTUDIO, ENTRENAMIENTO, DESCANSO, OTROS.
 * El bloque activo es visualmente evidente.
 */
@Composable
private fun TimeTimelineSection(
    blocks: List<TimeBlockEntity>,
    onToggle: (TimeBlockEntity) -> Unit,
    onDelete: (TimeBlockEntity) -> Unit,
    onAddNew: () -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LifeOsSectionHeader(
                    title = "Línea Temporal",
                    subtag = "Bloques de Enfoque",
                    indicatorColor = ExecutiveAccentBlue
                )

                Button(
                    onClick = onAddNew,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ExecutiveAccentBlue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_create_block")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Nuevo Bloque", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
            }
        }

        // Barra explicativa de categorías
        item {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = ExecutiveDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, ExecutiveBorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CategoryLegendItem("FOCO", ExecutiveCyan)
                    CategoryLegendItem("TRABAJO", ExecutiveAccentBlue)
                    CategoryLegendItem("ESTUDIO", ExecutivePurple)
                    CategoryLegendItem("ENTRENAMIENTO", ExecutiveSuccessGreen)
                    CategoryLegendItem("DESCANSO", ExecutiveSecondaryAmber)
                }
            }
        }

        if (blocks.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ExecutiveDarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ExecutiveBorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No hay bloques de tiempo programados en la línea temporal. Crea un bloque para estructurar tu ciclo operativo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ExecutiveTextMuted,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            // Asignamos el primer bloque no completado como "ACTIVO" si coincide o por defecto el primero
            val activeBlockId = blocks.firstOrNull { !it.isCompleted }?.id

            items(blocks, key = { it.id }) { block ->
                val isActive = block.id == activeBlockId && !block.isCompleted
                val category = normalizeCategory(block.category)
                val catColor = getCategoryColor(category)
                val durationMinutes = calculateDuration(block.startTime, block.endTime)

                val blockStatusText = when {
                    block.isCompleted -> "COMPLETADO"
                    isActive -> "ACTIVO"
                    else -> "PROGRAMADO"
                }

                val badgeType = when {
                    block.isCompleted -> LifeOsBadgeType.AVAILABLE
                    isActive -> LifeOsBadgeType.ACTIVE
                    else -> LifeOsBadgeType.WARNING
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    // Rail de la Línea Temporal (Nodo vertical + Conector)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(28.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(if (isActive) catColor else if (block.isCompleted) ExecutiveSuccessGreen else ExecutiveDarkSurfaceVariant)
                                .border(
                                    2.dp,
                                    if (isActive) catColor else ExecutiveBorder,
                                    CircleShape
                                )
                        )
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(70.dp)
                                .background(ExecutiveBorderSubtle)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Tarjeta Principal del Bloque
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isActive) catColor.copy(alpha = 0.08f) else ExecutiveDarkSurface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isActive) catColor else ExecutiveBorderSubtle
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("time_block_card_${block.id}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Fila Superior: Hora, Duración y Estado
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${block.startTime} — ${block.endTime}",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = if (isActive) catColor else ExecutiveTextWhite
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "•  $durationMinutes min",
                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                        color = ExecutiveTextMuted
                                    )
                                }

                                LifeOsBadge(
                                    text = blockStatusText,
                                    type = badgeType
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Actividad (Título) y Categoría
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = block.title,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = ExecutiveTextWhite
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = catColor.copy(alpha = 0.15f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, catColor.copy(alpha = 0.3f))
                                    ) {
                                        Text(
                                            text = category,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.5.sp
                                            ),
                                            color = catColor,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            SensoryFeedbackHelper.vibrateSuccess(context)
                                            onToggle(block)
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            if (block.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                            contentDescription = "Completar",
                                            tint = if (block.isCompleted) ExecutiveSuccessGreen else ExecutiveTextMuted,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { onDelete(block) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.DeleteOutline,
                                            contentDescription = "Eliminar",
                                            tint = ExecutiveTextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryLegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold
            ),
            color = ExecutiveTextMuted
        )
    }
}

private fun normalizeCategory(category: String): String {
    val upper = category.trim().uppercase()
    return when {
        upper.contains("FOCO") || upper.contains("ENFOQUE") -> "FOCO"
        upper.contains("TRABAJO") || upper.contains("WORK") -> "TRABAJO"
        upper.contains("ESTUDIO") || upper.contains("STUDY") -> "ESTUDIO"
        upper.contains("ENTRENAMIENTO") || upper.contains("GYM") || upper.contains("FIT") -> "ENTRENAMIENTO"
        upper.contains("DESCANSO") || upper.contains("BREAK") || upper.contains("PAUSA") -> "DESCANSO"
        else -> "OTROS"
    }
}

private fun getCategoryColor(category: String): Color {
    return when (category) {
        "FOCO" -> ExecutiveCyan
        "TRABAJO" -> ExecutiveAccentBlue
        "ESTUDIO" -> ExecutivePurple
        "ENTRENAMIENTO" -> ExecutiveSuccessGreen
        "DESCANSO" -> ExecutiveSecondaryAmber
        else -> ExecutiveTextMuted
    }
}

private fun calculateDuration(startTime: String, endTime: String): Int {
    return try {
        val sParts = startTime.split(":")
        val eParts = endTime.split(":")
        val sMin = sParts[0].trim().toInt() * 60 + sParts[1].trim().toInt()
        val eMin = eParts[0].trim().toInt() * 60 + eParts[1].trim().toInt()
        val diff = eMin - sMin
        if (diff > 0) diff else 60
    } catch (_: Exception) {
        60
    }
}

@Composable
private fun TasksListSection(
    tasks: List<TaskEntity>,
    onToggle: (TaskEntity) -> Unit,
    onDelete: (TaskEntity) -> Unit,
    onAddNew: () -> Unit
) {
    val context = LocalContext.current
    var taskFilter by remember { mutableStateOf("TODOS") } // "TODOS", "PENDIENTES", "COMPLETADAS", "ALTA"
    var taskSearchQuery by remember { mutableStateOf("") }

    val filteredTasks = remember(tasks, taskFilter, taskSearchQuery) {
        tasks.filter { task ->
            val matchesFilter = when (taskFilter) {
                "PENDIENTES" -> !task.isCompleted
                "COMPLETADAS" -> task.isCompleted
                "ALTA" -> task.priority.uppercase() == "ALTA"
                else -> true
            }
            val matchesSearch = taskSearchQuery.isBlank() ||
                task.title.contains(taskSearchQuery, ignoreCase = true) ||
                task.category.contains(taskSearchQuery, ignoreCase = true)
            matchesFilter && matchesSearch
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LifeOsSectionHeader(
                    title = "Tareas del Ciclo",
                    subtag = "${tasks.count { !it.isCompleted }} Pendientes",
                    indicatorColor = ExecutivePrimaryTeal
                )

                Button(
                    onClick = onAddNew,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ExecutivePrimaryTeal,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_create_task")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Nueva Tarea", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
            }
        }

        // Barra de filtros y búsqueda de tareas
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "TODOS" to "Todas",
                        "PENDIENTES" to "Pendientes",
                        "COMPLETADAS" to "Hechas",
                        "ALTA" to "!Alta"
                    ).forEach { (key, label) ->
                        FilterChip(
                            selected = taskFilter == key,
                            onClick = { taskFilter = key },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (key == "ALTA") ExecutiveDangerRed.copy(alpha = 0.2f) else ExecutivePrimaryTeal.copy(alpha = 0.2f),
                                selectedLabelColor = if (key == "ALTA") ExecutiveDangerRed else ExecutivePrimaryTeal
                            ),
                            modifier = Modifier.testTag("filter_task_$key")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = taskSearchQuery,
                    onValueChange = { taskSearchQuery = it },
                    placeholder = { Text("Buscar tarea o categoría (#trabajo, etc.)...", fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    trailingIcon = {
                        if (taskSearchQuery.isNotBlank()) {
                            IconButton(onClick = { taskSearchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Limpiar", modifier = Modifier.size(14.dp))
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("input_search_tasks"),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        if (filteredTasks.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ExecutiveDarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ExecutiveBorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (tasks.isEmpty()) "No hay tareas registradas para este ciclo." else "No se encontraron tareas con el filtro aplicado.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ExecutiveTextMuted,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(filteredTasks, key = { it.id }) { task ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (task.isCompleted) ExecutiveDarkSurfaceVariant.copy(alpha = 0.5f) else ExecutiveDarkSurface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (task.isCompleted) ExecutiveBorderSubtle else ExecutiveBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("task_item_${task.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        com.example.ui.components.CircularCompletionControl(
                            isCompleted = task.isCompleted,
                            onToggle = {
                                SensoryFeedbackHelper.vibrateSuccess(context)
                                onToggle(task)
                            },
                            modifier = Modifier
                                .padding(end = 10.dp)
                                .testTag("task_checkbox_${task.id}")
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (task.isCompleted) ExecutiveTextMuted else ExecutiveTextWhite
                                )
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                LifeOsBadge(
                                    text = task.priority,
                                    type = when (task.priority) {
                                        "ALTA" -> LifeOsBadgeType.BLOCKED
                                        "MEDIA" -> LifeOsBadgeType.WARNING
                                        else -> LifeOsBadgeType.AVAILABLE
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${task.estimatedMinutes} min • ${task.category}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                    color = ExecutiveTextMuted
                                )
                            }
                        }
                        IconButton(onClick = { onDelete(task) }) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar", tint = ExecutiveTextMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FocusTrackerSection(
    timerState: com.example.ui.FocusTimerState,
    onStart: (String, String) -> Unit,
    onPause: () -> Unit,
    onFinish: () -> Unit
) {
    var activityInput by remember { mutableStateOf(timerState.activityName) }
    var categoryInput by remember { mutableStateOf(timerState.category) }

    val hours = timerState.elapsedSeconds / 3600
    val minutes = (timerState.elapsedSeconds % 3600) / 60
    val seconds = timerState.elapsedSeconds % 60
    val timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds)

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "CRONÓMETRO DE PRECISIÓN OPERATIVA",
            style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = ExecutivePrimaryTeal
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (!timerState.isRunning && timerState.elapsedSeconds == 0L) {
            OutlinedTextField(
                value = activityInput,
                onValueChange = { activityInput = it },
                label = { Text("Actividad en curso") },
                modifier = Modifier.fillMaxWidth().testTag("input_timer_activity")
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = categoryInput,
                onValueChange = { categoryInput = it },
                label = { Text("Categoría (FOCO, TRABAJO, ESTUDIO)") },
                modifier = Modifier.fillMaxWidth().testTag("input_timer_category")
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(ExecutiveDarkSurface)
                .border(2.dp, if (timerState.isRunning) ExecutivePrimaryTeal else ExecutiveBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = timeFormatted,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    ),
                    color = ExecutivePrimaryTeal
                )
                Text(
                    text = if (timerState.isRunning) "SESIÓN ACTIVA" else if (timerState.elapsedSeconds > 0) "EN PAUSA" else "LISTO",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = ExecutiveTextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            if (!timerState.isRunning) {
                Button(
                    onClick = { onStart(activityInput, categoryInput) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ExecutivePrimaryTeal,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.testTag("btn_timer_start")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (timerState.elapsedSeconds > 0) "Reanudar" else "Iniciar Enfoque", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onPause,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ExecutiveSecondaryAmber,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.testTag("btn_timer_pause")
                ) {
                    Icon(Icons.Default.Pause, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Pausar", fontWeight = FontWeight.Bold)
                }
            }

            if (timerState.elapsedSeconds > 0) {
                OutlinedButton(
                    onClick = onFinish,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ExecutiveSuccessGreen),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ExecutiveSuccessGreen),
                    modifier = Modifier.testTag("btn_timer_finish")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Consolidar Registro", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun TimeStatsSection(
    totalMinutes: Int,
    entries: List<com.example.data.model.TimeEntryEntity>,
    tasks: List<TaskEntity>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = ExecutiveDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, ExecutiveBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "RESUMEN DEL CICLO ACTIVO",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = ExecutiveAccentBlue
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${totalMinutes / 60}h ${totalMinutes % 60}m productivos",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = ExecutiveTextWhite
                    )
                    Text(
                        text = "${entries.size} sesiones de cronómetro registradas en este ciclo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ExecutiveTextMuted
                    )
                }
            }
        }

        item {
            LifeOsSectionHeader(
                title = "Historial",
                subtag = "Sesiones Registradas",
                indicatorColor = ExecutivePrimaryTeal
            )
        }

        if (entries.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ExecutiveDarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ExecutiveBorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Aún no hay sesiones cronometradas hoy. Inicia una sesión en el Tracker.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ExecutiveTextMuted,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(entries, key = { it.id }) { entry ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ExecutiveDarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ExecutiveBorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = entry.activityName,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = ExecutiveTextWhite
                            )
                            Text(
                                text = "${entry.category} • ${entry.timestamp}",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                color = ExecutiveTextMuted
                            )
                        }
                        Text(
                            text = "${entry.durationMinutes} min",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = ExecutivePrimaryTeal
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("ALTA") }
    var estimatedMinutes by remember { mutableStateOf("30") }
    var category by remember { mutableStateOf("Trabajo") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Tarea") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título de la tarea") },
                    modifier = Modifier.fillMaxWidth().testTag("input_task_title")
                )
                OutlinedTextField(
                    value = estimatedMinutes,
                    onValueChange = { estimatedMinutes = it },
                    label = { Text("Minutos estimados") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Categoría (ej: Trabajo, Estudio, Personal)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_task_category")
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("ALTA", "MEDIA", "BAJA").forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val est = estimatedMinutes.toIntOrNull() ?: 30
                    onConfirm(title, priority, est, category)
                },
                modifier = Modifier.testTag("dialog_task_confirm")
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun AddTimeBlockDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("09:00") }
    var endTime by remember { mutableStateOf("10:30") }
    var category by remember { mutableStateOf("FOCO") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Crear Bloque de Tiempo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Actividad o Enfoque") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Inicio (HH:mm)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("Fin (HH:mm)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Categoría (ej: Foco, Trabajo, Estudio)") },
                    placeholder = { Text("Escribe la categoría que desees...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_block_category")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(title, startTime, endTime, category) },
                colors = ButtonDefaults.buttonColors(containerColor = ExecutiveAccentBlue)
            ) {
                Text("Guardar Bloque")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
