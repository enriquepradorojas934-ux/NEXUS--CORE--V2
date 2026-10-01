package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import com.example.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WorkoutExerciseEntity
import com.example.data.model.WorkoutSessionEntity
import com.example.ui.PersonalOsUiState
import com.example.ui.components.*
import com.example.ui.theme.*

data class RoutineDayPlan(
    val id: String = java.util.UUID.randomUUID().toString(),
    var title: String,
    var subtitle: String,
    val exercises: SnapshotStateList<WorkoutExerciseEntity> = mutableStateListOf()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FitnessScreen(
    state: PersonalOsUiState,
    onAddWorkout: (title: String, goal: String, duration: Int, notes: String, exercises: List<WorkoutExerciseEntity>) -> Unit,
    onDeleteWorkout: (WorkoutSessionEntity) -> Unit,
    onAnalyzeAi: () -> Unit,
    onSuggestTacticalNote: (routineName: String, goal: String, minutes: Int, summary: String, onResult: (String) -> Unit) -> Unit = { _, _, _, _, _ -> },
    onClose: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    // Campos para nueva sesión
    var workoutTitle by remember { mutableStateOf("") }
    var targetGoal by remember { mutableStateOf("Hipertrofia") }
    var durationMinutes by remember { mutableStateOf("45") }
    var notesText by remember { mutableStateOf("") }

    // Rutina estructurada por días / grupos musculares
    val routineDays = remember {
        mutableStateListOf(
            RoutineDayPlan(
                title = "LUNES · DIA 1",
                subtitle = "Push (pecho, hombros, tríceps)",
                exercises = mutableStateListOf(
                    WorkoutExerciseEntity(0, 0, "Press de Banca", 4, "10, 8, 8, 6", 75.0, 90),
                    WorkoutExerciseEntity(0, 0, "Press Militar", 3, "10", 20.0, 60)
                )
            )
        )
    }
    var selectedDayIndex by remember { mutableStateOf(0) }

    // Campos para añadir ejercicio
    var exName by remember { mutableStateOf("") }
    var exSets by remember { mutableStateOf("3") }
    var exReps by remember { mutableStateOf("10, 10, 8") }
    var exWeight by remember { mutableStateOf("20") }
    var exRest by remember { mutableStateOf("60") }

    // Estado para edición pequeña al tocar (campo pequeño al tocarlo)
    var editingFieldLabel by remember { mutableStateOf<String?>(null) }
    var editingFieldValue by remember { mutableStateOf("") }
    var editingFieldKeyboardType by remember { mutableStateOf(KeyboardType.Text) }
    var onSaveEditedField by remember { mutableStateOf<((String) -> Unit)?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(ExecutiveDarkBg)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header táctico
            ExecutiveHeader(
                title = androidx.compose.ui.res.stringResource(com.example.R.string.fitness_title),
                subtitle = androidx.compose.ui.res.stringResource(com.example.R.string.fitness_subtitle),
                cycleDate = state.currentCycleDate,
                timeUntilReset = state.timeUntilNextCycle,
                cycleProgress = state.cycleProgress,
                actionIcon = Icons.Default.Close,
                onActionClick = onClose
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // TARJETA DE RESUMEN TÁCTICO
                item {
                    TacticalCard(
                        modifier = Modifier.fillMaxWidth().testTag("card_fitness_summary"),
                        accentColor = TacticalNeonCyan
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "VITALIDAD Y RENDIMIENTO",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TacticalNeonCyan,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${state.workoutSessions.size} Sesiones Registradas",
                                    color = ExecutiveTextPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Atributo de Vitalidad: ${state.userProgression.vitalityScore}/100",
                                    color = ExecutiveTextSecondary,
                                    fontSize = 12.sp
                                )
                            }

                            Button(
                                onClick = { showAddDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = TacticalNeonCyan),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("btn_add_workout")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = ExecutiveDarkBg, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.fitness_register_btn), color = ExecutiveDarkBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // ASESOR TÁCTICO IA (GEMINI)
                item {
                    TacticalCard(
                        modifier = Modifier.fillMaxWidth().testTag("card_ai_trainer"),
                        accentColor = TacticalVioletIdentity
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.SmartToy,
                                        contentDescription = null,
                                        tint = TacticalVioletIdentity,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "ENTRENADOR TÁCTICO IA",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TacticalVioletIdentity
                                    )
                                }

                                if (state.isAnalyzingWorkout) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = TacticalNeonCyan
                                    )
                                } else {
                                    FilledTonalButton(
                                        onClick = onAnalyzeAi,
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = TacticalVioletIdentity.copy(alpha = 0.18f)
                                        ),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.testTag("btn_analyze_workout_ai")
                                    ) {
                                        Text(stringResource(R.string.fitness_analyze_vol_btn), fontSize = 11.sp, color = TacticalVioletIdentity, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            if (!state.aiWorkoutAdvice.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = ExecutiveBorderSubtle)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = state.aiWorkoutAdvice,
                                    fontSize = 13.sp,
                                    color = ExecutiveTextPrimary,
                                    lineHeight = 18.sp
                                )
                            } else {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Presiona 'Analizar Volumen' para que la IA evalúe la frecuencia de tus rutinas, sobrecarga y tiempos de descanso.",
                                    fontSize = 12.sp,
                                    color = ExecutiveTextSecondary
                                )
                            }
                        }
                    }
                }

                // LISTA DE SESIONES REGISTRADAS
                item {
                    LifeOsSectionHeader(
                        title = "HISTORIAL DE SESIONES",
                        badge = "${state.workoutSessions.size}",
                        subtag = "REGISTROS",
                        indicatorColor = TacticalNeonCyan
                    )
                }

                if (state.workoutSessions.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay rutinas registradas en este ciclo.",
                                color = ExecutiveTextSecondary,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                } else {
                    items(state.workoutSessions, key = { it.id }) { session ->
                        TacticalCard(
                            modifier = Modifier.fillMaxWidth().testTag("workout_item_${session.id}"),
                            accentColor = TacticalNeonCyan.copy(alpha = 0.6f)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = session.title,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ExecutiveTextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "${session.durationMinutes} min",
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 11.sp,
                                                color = TacticalNeonCyan,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(text = " • ", color = ExecutiveTextSecondary, fontSize = 11.sp)
                                            Text(
                                                text = "Meta: ${session.targetGoal}",
                                                fontSize = 11.sp,
                                                color = ExecutiveTextSecondary
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { onDeleteWorkout(session) },
                                        modifier = Modifier.size(28.dp).testTag("btn_delete_workout_${session.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Eliminar",
                                            tint = ExecutiveTextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                if (session.notes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = session.notes,
                                        fontSize = 12.sp,
                                        color = ExecutiveTextSecondary,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // DIÁLOGO PEQUEÑO PARA EDICIÓN DE VALOR AL TOCAR
    if (editingFieldLabel != null) {
        AlertDialog(
            onDismissRequest = { editingFieldLabel = null },
            containerColor = ExecutiveDarkSurface,
            title = {
                Text(
                    text = editingFieldLabel ?: "",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ExecutiveTextPrimary
                )
            },
            text = {
                OutlinedTextField(
                    value = editingFieldValue,
                    onValueChange = { editingFieldValue = it },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = editingFieldKeyboardType),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TacticalNeonCyan,
                        unfocusedBorderColor = ExecutiveBorderSubtle,
                        focusedTextColor = ExecutiveTextPrimary,
                        unfocusedTextColor = ExecutiveTextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("small_edit_field_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveEditedField?.invoke(editingFieldValue)
                        editingFieldLabel = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TacticalNeonCyan),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(stringResource(R.string.action_save), color = ExecutiveDarkBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingFieldLabel = null }) {
                    Text(stringResource(R.string.action_cancel), color = ExecutiveTextSecondary, fontSize = 12.sp)
                }
            }
        )
    }

    // DIÁLOGO PARA REGISTRAR NUEVO ENTRENAMIENTO
    if (showAddDialog) {
        val scrollState = rememberScrollState()
        var isGeneratingNote by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = ExecutiveDarkSurface,
            title = {
                Text(
                    text = stringResource(R.string.fitness_dialog_title),
                    color = ExecutiveTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = workoutTitle,
                        onValueChange = { workoutTitle = it },
                        label = { Text(stringResource(R.string.fitness_routine_name_label)) },
                        placeholder = { Text(stringResource(R.string.fitness_routine_name_hint)) },
                        modifier = Modifier.fillMaxWidth().testTag("input_workout_title"),
                        singleLine = true
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = targetGoal,
                            onValueChange = { targetGoal = it },
                            label = { Text(stringResource(R.string.fitness_goal_label)) },
                            placeholder = { Text(stringResource(R.string.fitness_goal_hint)) },
                            modifier = Modifier.weight(1f).testTag("input_workout_goal"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = durationMinutes,
                            onValueChange = { durationMinutes = it.filter { ch -> ch.isDigit() } },
                            label = { Text(stringResource(R.string.fitness_minutes_label)) },
                            placeholder = { Text("20") },
                            modifier = Modifier.weight(1f).testTag("input_workout_duration"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }

                    // DÍAS Y GRUPOS MUSCULARES DE LA RUTINA
                    Text(
                        text = "Días y Grupos Musculares:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TacticalNeonCyan
                    )

                    routineDays.forEachIndexed { dayIdx, day ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                1.dp,
                                if (selectedDayIndex == dayIdx) TacticalNeonCyan.copy(alpha = 0.6f) else ExecutiveBorderSubtle
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedDayIndex = dayIdx }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                // Título y subtítulo del día (editables con campo pequeño al tocarlos)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.clickable {
                                                editingFieldLabel = "Título del día (ej: LUNES · DIA 1)"
                                                editingFieldValue = day.title
                                                editingFieldKeyboardType = KeyboardType.Text
                                                onSaveEditedField = { day.title = it }
                                            }
                                        ) {
                                            Text(
                                                text = day.title,
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TacticalNeonCyan
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Editar título",
                                                tint = ExecutiveTextSecondary.copy(alpha = 0.6f),
                                                modifier = Modifier.size(11.dp)
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.clickable {
                                                editingFieldLabel = "Subtítulo de rutina (ej: Push (pecho, hombros, tríceps))"
                                                editingFieldValue = day.subtitle
                                                editingFieldKeyboardType = KeyboardType.Text
                                                onSaveEditedField = { day.subtitle = it }
                                            }
                                        ) {
                                            Text(
                                                text = if (day.subtitle.startsWith("(")) day.subtitle else "(${day.subtitle})",
                                                fontSize = 11.sp,
                                                color = ExecutiveTextSecondary
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Editar subtítulo",
                                                tint = ExecutiveTextSecondary.copy(alpha = 0.6f),
                                                modifier = Modifier.size(10.dp)
                                            )
                                        }
                                    }

                                    if (routineDays.size > 1) {
                                        IconButton(
                                            onClick = {
                                                routineDays.removeAt(dayIdx)
                                                if (selectedDayIndex >= routineDays.size) {
                                                    selectedDayIndex = (routineDays.size - 1).coerceAtLeast(0)
                                                }
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Eliminar día",
                                                tint = ExecutiveTextSecondary.copy(alpha = 0.6f),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }

                                // Lista de ejercicios en este día
                                if (day.exercises.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    HorizontalDivider(color = ExecutiveBorderSubtle.copy(alpha = 0.5f))
                                    Spacer(modifier = Modifier.height(6.dp))

                                    day.exercises.forEachIndexed { exIdx, ex ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = ex.exerciseName,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = ExecutiveTextPrimary,
                                                modifier = Modifier.weight(1f)
                                            )

                                            // SERIES chip (editable con campo pequeño al tocar)
                                            Surface(
                                                color = ExecutiveDarkBg,
                                                shape = RoundedCornerShape(4.dp),
                                                border = BorderStroke(1.dp, ExecutiveBorderSubtle),
                                                modifier = Modifier.clickable {
                                                    editingFieldLabel = "SERIES: ${ex.exerciseName}"
                                                    editingFieldValue = ex.sets.toString()
                                                    editingFieldKeyboardType = KeyboardType.Number
                                                    onSaveEditedField = { newVal ->
                                                        val s = newVal.toIntOrNull() ?: ex.sets
                                                        day.exercises[exIdx] = ex.copy(sets = s)
                                                    }
                                                }
                                            ) {
                                                Text(
                                                    text = "${ex.sets} S",
                                                    fontSize = 11.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TacticalVioletIdentity,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(4.dp))

                                            // REPS chip (editable con campo pequeño al tocar)
                                            Surface(
                                                color = ExecutiveDarkBg,
                                                shape = RoundedCornerShape(4.dp),
                                                border = BorderStroke(1.dp, ExecutiveBorderSubtle),
                                                modifier = Modifier.clickable {
                                                    editingFieldLabel = "REPS: ${ex.exerciseName}"
                                                    editingFieldValue = ex.reps
                                                    editingFieldKeyboardType = KeyboardType.Text
                                                    onSaveEditedField = { newVal ->
                                                        day.exercises[exIdx] = ex.copy(reps = newVal.trim())
                                                    }
                                                }
                                            ) {
                                                Text(
                                                    text = "${ex.reps} R",
                                                    fontSize = 11.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = ExecutiveTextPrimary,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(4.dp))

                                            // KG chip (editable con campo pequeño al tocar)
                                            Surface(
                                                color = ExecutiveDarkBg,
                                                shape = RoundedCornerShape(4.dp),
                                                border = BorderStroke(1.dp, ExecutiveBorderSubtle),
                                                modifier = Modifier.clickable {
                                                    editingFieldLabel = "KG: ${ex.exerciseName}"
                                                    editingFieldValue = if (ex.weightKg % 1.0 == 0.0) ex.weightKg.toInt().toString() else ex.weightKg.toString()
                                                    editingFieldKeyboardType = KeyboardType.Decimal
                                                    onSaveEditedField = { newVal ->
                                                        val w = newVal.toDoubleOrNull() ?: ex.weightKg
                                                        day.exercises[exIdx] = ex.copy(weightKg = w)
                                                    }
                                                }
                                            ) {
                                                Text(
                                                    text = "${if (ex.weightKg % 1.0 == 0.0) ex.weightKg.toInt() else ex.weightKg} kg",
                                                    fontSize = 11.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TacticalNeonCyan,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(4.dp))

                                            IconButton(
                                                onClick = { day.exercises.removeAt(exIdx) },
                                                modifier = Modifier.size(20.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Close,
                                                    contentDescription = "Quitar ejercicio",
                                                    tint = ExecutiveTextSecondary.copy(alpha = 0.6f),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Botón para agregar un nuevo día/grupo muscular a la rutina
                    OutlinedButton(
                        onClick = {
                            val nextNum = routineDays.size + 1
                            val (dTitle, dSub) = when (nextNum) {
                                2 -> "MARTES · DIA 2" to "Pull (espalda, bíceps)"
                                3 -> "MIÉRCOLES · DIA 3" to "Legs (pierna, pantorrilla)"
                                4 -> "JUEVES · DIA 4" to "Push / Hombros"
                                5 -> "VIERNES · DIA 5" to "Pull / Brazos"
                                6 -> "SÁBADO · DIA 6" to "Core / Acondicionamiento"
                                else -> "DÍA $nextNum" to "Grupo muscular"
                            }
                            routineDays.add(RoutineDayPlan(title = dTitle, subtitle = dSub))
                            selectedDayIndex = routineDays.size - 1
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TacticalNeonCyan),
                        border = BorderStroke(1.dp, TacticalNeonCyan.copy(alpha = 0.45f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("btn_add_routine_day")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("+ Agregar día", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    HorizontalDivider(color = ExecutiveBorderSubtle)

                    // AGREGAR EJERCICIO A LA RUTINA
                    val activeDay = routineDays.getOrNull(selectedDayIndex) ?: routineDays.firstOrNull()
                    Text(
                        text = "Agregar Ejercicio a: ${activeDay?.title ?: "Rutina"}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TacticalNeonCyan
                    )

                    OutlinedTextField(
                        value = exName,
                        onValueChange = { exName = it },
                        label = { Text("Ejercicio") },
                        placeholder = { Text("ej: Press de Banca") },
                        modifier = Modifier.fillMaxWidth().testTag("input_exercise_name"),
                        singleLine = true
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = exSets,
                            onValueChange = { exSets = it },
                            label = { Text("Series") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = exReps,
                            onValueChange = { exReps = it },
                            label = { Text("Reps") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = exWeight,
                            onValueChange = { exWeight = it },
                            label = { Text("Kg") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }

                    Button(
                        onClick = {
                            if (exName.isNotBlank() && activeDay != null) {
                                activeDay.exercises.add(
                                    WorkoutExerciseEntity(
                                        sessionId = 0,
                                        exerciseName = exName.trim(),
                                        sets = exSets.toIntOrNull() ?: 3,
                                        reps = exReps.trim().ifBlank { "10" },
                                        weightKg = exWeight.toDoubleOrNull() ?: 0.0,
                                        restSeconds = exRest.toIntOrNull() ?: 60
                                    )
                                )
                                exName = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TacticalVioletIdentity),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth().testTag("btn_add_exercise_item")
                    ) {
                        val totalExercises = routineDays.sumOf { it.exercises.size }
                        Text("Añadir Ejercicio ($totalExercises listos)", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    HorizontalDivider(color = ExecutiveBorderSubtle)

                    // NOTAS TÁCTICAS CON SUGERENCIA IA GEMINI
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Notas tácticas",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ExecutiveTextPrimary
                        )

                        FilledTonalButton(
                            onClick = {
                                isGeneratingNote = true
                                val summary = buildString {
                                    append("Rutina: $workoutTitle, Objetivo: $targetGoal, Duración: $durationMinutes min.\n")
                                    routineDays.forEach { d ->
                                        append("${d.title} (${d.subtitle}): ")
                                        if (d.exercises.isEmpty()) append("Sin ejercicios específicos")
                                        else append(d.exercises.joinToString(", ") { "${it.exerciseName} (${it.sets}x${it.reps} @ ${it.weightKg}kg)" })
                                        append("\n")
                                    }
                                }
                                onSuggestTacticalNote(
                                    workoutTitle.ifBlank { "Rutina Personal" },
                                    targetGoal,
                                    durationMinutes.toIntOrNull() ?: 45,
                                    summary
                                ) { suggested ->
                                    notesText = suggested
                                    isGeneratingNote = false
                                }
                            },
                            enabled = !isGeneratingNote,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = TacticalVioletIdentity.copy(alpha = 0.2f),
                                contentColor = TacticalVioletIdentity
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.testTag("btn_suggest_ai_note")
                        ) {
                            if (isGeneratingNote) {
                                CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp, color = TacticalVioletIdentity)
                                Spacer(modifier = Modifier.width(4.dp))
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = if (isGeneratingNote) "Analizando..." else "Sugerir con IA",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        placeholder = { Text("Escribe notas tácticas o usa 'Sugerir con IA'...") },
                        modifier = Modifier.fillMaxWidth().testTag("input_workout_notes"),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val titleToSave = workoutTitle.trim().ifBlank {
                            routineDays.firstOrNull()?.let { "${it.title} - ${it.subtitle}" } ?: "Sesión de Entrenamiento"
                        }
                        val allExercises = mutableListOf<WorkoutExerciseEntity>()
                        routineDays.forEach { d ->
                            allExercises.addAll(d.exercises)
                        }
                        onAddWorkout(
                            titleToSave,
                            targetGoal.trim(),
                            durationMinutes.toIntOrNull() ?: 45,
                            notesText.trim(),
                            allExercises
                        )
                        showAddDialog = false
                        workoutTitle = ""
                        notesText = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TacticalNeonCyan),
                    modifier = Modifier.testTag("btn_save_workout_confirm")
                ) {
                    Text("Guardar Sesión", color = ExecutiveDarkBg, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancelar", color = ExecutiveTextSecondary)
                }
            }
        )
    }
}
