package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HabitEntity
import com.example.data.model.HabitLogEntity
import com.example.ui.theme.*

enum class HabitFilter {
    ALL,
    PENDING,
    COMPLETED
}

/**
 * Componente interactivo de Jetpack Compose que lista los hábitos del día,
 * calcula métricas en tiempo real y permite marcar/desmarcar cada uno
 * actualizando reactivamente la base de datos Room local.
 */
@Composable
fun DailyHabitsListComponent(
    habits: List<HabitEntity>,
    habitLogsToday: List<HabitLogEntity>,
    onToggleHabit: (Long) -> Unit,
    modifier: Modifier = Modifier,
    onDeleteHabit: ((HabitEntity) -> Unit)? = null,
    onAddHabitClick: (() -> Unit)? = null,
    showSummaryCard: Boolean = true,
    showFilterChips: Boolean = true,
    maxItems: Int? = null
) {
    var currentFilter by remember { mutableStateOf(HabitFilter.ALL) }

    // Métricas en vivo basadas en la lista y los logs de Room
    val totalCount = habits.size
    val completedCount = remember(habits, habitLogsToday) {
        habits.count { habit ->
            habitLogsToday.any { it.habitId == habit.id && it.isCompleted }
        }
    }
    val completionRate = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = completionRate,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "habitProgressAnimation"
    )

    val filteredHabits = remember(habits, habitLogsToday, currentFilter) {
        val list = when (currentFilter) {
            HabitFilter.ALL -> habits
            HabitFilter.PENDING -> habits.filter { habit ->
                !habitLogsToday.any { it.habitId == habit.id && it.isCompleted }
            }
            HabitFilter.COMPLETED -> habits.filter { habit ->
                habitLogsToday.any { it.habitId == habit.id && it.isCompleted }
            }
        }
        if (maxItems != null) list.take(maxItems) else list
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_habits_component"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Tarjeta resumen de progreso interactiva
        if (showSummaryCard && habits.isNotEmpty()) {
            HabitProgressSummaryCard(
                completedCount = completedCount,
                totalCount = totalCount,
                animatedProgress = animatedProgress,
                habits = habits
            )
        }

        // Barra de filtros rápidos (Todos / Pendientes / Completados)
        if (showFilterChips && habits.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("habit_filter_chips_row"),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = currentFilter == HabitFilter.ALL,
                    onClick = { currentFilter = HabitFilter.ALL },
                    label = { Text("Todos ($totalCount)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ExecutivePrimaryTeal.copy(alpha = 0.2f),
                        selectedLabelColor = ExecutivePrimaryTeal
                    ),
                    modifier = Modifier.testTag("filter_chip_all")
                )
                val pendingCount = totalCount - completedCount
                FilterChip(
                    selected = currentFilter == HabitFilter.PENDING,
                    onClick = { currentFilter = HabitFilter.PENDING },
                    label = { Text("Pendientes ($pendingCount)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ExecutiveSecondaryAmber.copy(alpha = 0.2f),
                        selectedLabelColor = ExecutiveSecondaryAmber
                    ),
                    modifier = Modifier.testTag("filter_chip_pending")
                )
                FilterChip(
                    selected = currentFilter == HabitFilter.COMPLETED,
                    onClick = { currentFilter = HabitFilter.COMPLETED },
                    label = { Text("Completados ($completedCount)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ExecutiveSuccessGreen.copy(alpha = 0.2f),
                        selectedLabelColor = ExecutiveSuccessGreen
                    ),
                    modifier = Modifier.testTag("filter_chip_completed")
                )
            }
        }

        // Estado cuando no hay hábitos registrados o la lista filtrada está vacía
        if (habits.isEmpty()) {
            EmptyHabitsView(onAddHabitClick = onAddHabitClick)
        } else if (filteredHabits.isEmpty()) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (currentFilter == HabitFilter.PENDING)
                            "🎉 ¡Excelente trabajo! No tienes hábitos pendientes en este ciclo."
                        else
                            "Aún no has completado hábitos en este filtro.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // Lista de hábitos del día
            filteredHabits.forEach { habit ->
                val isDoneToday = habitLogsToday.any { it.habitId == habit.id && it.isCompleted }
                DailyHabitItemCard(
                    habit = habit,
                    isCompletedToday = isDoneToday,
                    onToggle = { onToggleHabit(habit.id) },
                    onDelete = onDeleteHabit?.let { { it(habit) } }
                )
            }
        }
    }
}

/**
 * Tarjeta individual interactiva para cada hábito con animación y botón táctil accesible.
 */
@Composable
fun DailyHabitItemCard(
    habit: HabitEntity,
    isCompletedToday: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    onDelete: (() -> Unit)? = null
) {
    val cardBgColor by animateColorAsState(
        targetValue = if (isCompletedToday)
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        else
            MaterialTheme.colorScheme.surface,
        label = "cardBgColor"
    )

    val checkBgColor by animateColorAsState(
        targetValue = if (isCompletedToday) ExecutiveSuccessGreen else Color.Transparent,
        label = "checkBgColor"
    )

    val checkBorderColor by animateColorAsState(
        targetValue = if (isCompletedToday) ExecutiveSuccessGreen else MaterialTheme.colorScheme.outline,
        label = "checkBorderColor"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (isCompletedToday) ExecutiveSuccessGreen.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("habit_card_${habit.id}")
            .clickable { onToggle() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botón interactivo de marcado tipo Checkbox circular con target táctil de 48dp
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable(onClick = onToggle)
                    .testTag("toggle_habit_${habit.id}"),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(checkBgColor)
                        .then(
                            if (!isCompletedToday) Modifier.background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ) else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        if (!isCompletedToday) {
                            drawCircle(
                                color = checkBorderColor,
                                radius = size.minDimension / 2 - 1.dp.toPx(),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                            )
                        }
                    }
                    if (isCompletedToday) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completado",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Información descriptiva del hábito
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = habit.title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = if (isCompletedToday) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        color = if (isCompletedToday)
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        else
                            MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    // Chip de Categoría
                    CategoryChip(category = habit.category)
                }

                if (habit.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = habit.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = if (isCompletedToday) 0.6f else 0.9f
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Métricas y racha activa
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Meta: ${habit.targetValue} ${habit.unit} • ${habit.frequency}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Racha",
                            tint = if (habit.currentStreak > 0) ExecutiveSecondaryAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${habit.currentStreak} d",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (habit.currentStreak > 0) ExecutiveSecondaryAmber else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    if (isCompletedToday) {
                        Text(
                            text = "✓ Listo",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = ExecutiveSuccessGreen
                        )
                    }
                }
            }

            // Botón opcional de eliminación
            if (onDelete != null) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp).testTag("delete_habit_${habit.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Eliminar hábito",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Chip visual que identifica la categoría temática del hábito.
 */
@Composable
private fun CategoryChip(category: String) {
    val (bgColor, textColor) = when (category.lowercase()) {
        "salud" -> Pair(ExecutiveSuccessGreen.copy(alpha = 0.15f), ExecutiveSuccessGreen)
        "estudio", "aprendizaje" -> Pair(ExecutiveAccentBlue.copy(alpha = 0.15f), ExecutiveAccentBlue)
        "atención", "enfoque" -> Pair(ExecutivePurple.copy(alpha = 0.15f), ExecutivePurple)
        "finanzas" -> Pair(ExecutiveSecondaryAmber.copy(alpha = 0.15f), ExecutiveSecondaryAmber)
        else -> Pair(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = category.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            ),
            color = textColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

/**
 * Tarjeta superior que muestra el estado de cumplimiento diario con barra interactiva.
 */
@Composable
private fun HabitProgressSummaryCard(
    completedCount: Int,
    totalCount: Int,
    animatedProgress: Float,
    habits: List<HabitEntity>
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().testTag("habits_progress_summary_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CUMPLIMIENTO DE HÁBITOS DE HOY",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = ExecutiveSuccessGreen
                    )
                    Text(
                        text = when {
                            completedCount == totalCount && totalCount > 0 -> "🎯 ¡Todos los hábitos completados!"
                            completedCount > 0 -> "Buen ritmo, continúa con los pendientes"
                            else -> "Inicia tu ciclo marcando tu primer hábito"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "$completedCount de $totalCount",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = ExecutiveSuccessGreen,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${(animatedProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = ExecutiveSuccessGreen
                    )
                    Text(
                        "Tasa de éxito",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                val maxStreak = habits.maxOfOrNull { it.bestStreak } ?: 0
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "🔥 $maxStreak d",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = ExecutiveSecondaryAmber
                    )
                    Text(
                        "Racha máxima",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Estado vacío cuando no existen hábitos cargados en la base de datos local Room.
 */
@Composable
private fun EmptyHabitsView(
    onAddHabitClick: (() -> Unit)?
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().testTag("empty_habits_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                tint = ExecutivePrimaryTeal,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "No tienes hábitos registrados hoy",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Agrega tus primeros hábitos clave (ej: Ejercicio, Lectura, Meditación) para comenzar a medir tu consistencia.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            if (onAddHabitClick != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onAddHabitClick,
                    colors = ButtonDefaults.buttonColors(containerColor = ExecutiveSuccessGreen),
                    modifier = Modifier.testTag("btn_add_first_habit")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Agregar Primer Hábito")
                }
            }
        }
    }
}
