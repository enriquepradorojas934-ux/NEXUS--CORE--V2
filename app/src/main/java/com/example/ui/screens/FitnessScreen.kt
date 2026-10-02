package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import com.example.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WorkoutExerciseEntity
import com.example.data.model.WorkoutSessionEntity
import com.example.ui.PersonalOsUiState
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.util.SensoryFeedbackHelper
import kotlinx.coroutines.delay

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
    onToggleExercise: (Long) -> Unit = {},
    onToggleWorkoutSession: (Long) -> Unit = {},
    onAddExerciseToSession: (sessionId: Long, name: String, sets: Int, reps: String, weightKg: Double, restSeconds: Int) -> Unit = { _, _, _, _, _, _ -> },
    onUpdateExercise: (WorkoutExerciseEntity) -> Unit = {},
    onDeleteExercise: (WorkoutExerciseEntity) -> Unit = {},
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

    val context = LocalContext.current
    val expandedSessionIds = remember { mutableStateMapOf<Long, Boolean>() }
    var activeTrainingSession by remember { mutableStateOf<WorkoutSessionEntity?>(null) }
    var exerciseToAddToSessionId by remember { mutableStateOf<Long?>(null) }
    var addExName by remember { mutableStateOf("") }
    var addExSets by remember { mutableStateOf("3") }
    var addExReps by remember { mutableStateOf("10, 10, 8") }
    var addExWeight by remember { mutableStateOf("20") }
    var addExRest by remember { mutableStateOf("60") }

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
                        val exercises = state.workoutExercises.filter { it.sessionId == session.id }
                        val isExpanded = expandedSessionIds[session.id] ?: true
                        InteractiveWorkoutSessionCard(
                            session = session,
                            exercises = exercises,
                            isExpanded = isExpanded,
                            onToggleExpand = {
                                expandedSessionIds[session.id] = !isExpanded
                            },
                            onToggleExercise = onToggleExercise,
                            onToggleWorkoutSession = onToggleWorkoutSession,
                            onDeleteWorkout = onDeleteWorkout,
                            onStartWorkoutRunner = {
                                activeTrainingSession = session
                            },
                            onAddExerciseClick = {
                                exerciseToAddToSessionId = session.id
                                addExName = ""
                                addExSets = "3"
                                addExReps = "10, 10, 8"
                                addExWeight = "20"
                                addExRest = "60"
                            },
                            onEditField = { label, curVal, kbd, onSave ->
                                editingFieldLabel = label
                                editingFieldValue = curVal
                                editingFieldKeyboardType = kbd
                                onSaveEditedField = onSave
                            },
                            onDeleteExercise = onDeleteExercise
                        )
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

    // MODAL DE ENTRENAMIENTO ACTIVO / RUNNER INTERACTIVO
    if (activeTrainingSession != null) {
        val session = activeTrainingSession!!
        val exercises = state.workoutExercises.filter { it.sessionId == session.id }
        ActiveWorkoutRunnerModal(
            session = session,
            exercises = exercises,
            onToggleExercise = onToggleExercise,
            onCompleteSession = {
                onToggleWorkoutSession(session.id)
            },
            onClose = { activeTrainingSession = null }
        )
    }

    // DIÁLOGO PARA AÑADIR EJERCICIO A RUTINA EXISTENTE
    if (exerciseToAddToSessionId != null) {
        val sId = exerciseToAddToSessionId!!
        AddExerciseToSessionDialog(
            sessionId = sId,
            name = addExName,
            onNameChange = { addExName = it },
            sets = addExSets,
            onSetsChange = { addExSets = it },
            reps = addExReps,
            onRepsChange = { addExReps = it },
            weight = addExWeight,
            onWeightChange = { addExWeight = it },
            rest = addExRest,
            onRestChange = { addExRest = it },
            onDismiss = { exerciseToAddToSessionId = null },
            onConfirm = { name, sets, reps, weight, rest ->
                onAddExerciseToSession(sId, name, sets, reps, weight, rest)
                exerciseToAddToSessionId = null
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

// =========================================================================
// TARJETA DE SESIÓN DE ENTRENAMIENTO TOTALMENTE INTERACTIVA
// =========================================================================
@Composable
fun InteractiveWorkoutSessionCard(
    session: WorkoutSessionEntity,
    exercises: List<WorkoutExerciseEntity>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onToggleExercise: (Long) -> Unit,
    onToggleWorkoutSession: (Long) -> Unit,
    onDeleteWorkout: (WorkoutSessionEntity) -> Unit,
    onStartWorkoutRunner: () -> Unit,
    onAddExerciseClick: () -> Unit,
    onEditField: (label: String, currentValue: String, kbdType: KeyboardType, onSave: (String) -> Unit) -> Unit,
    onDeleteExercise: (WorkoutExerciseEntity) -> Unit
) {
    val context = LocalContext.current
    val completedExercisesCount = exercises.count { it.isCompleted }
    val totalExercisesCount = exercises.size
    val progress = if (totalExercisesCount > 0) completedExercisesCount.toFloat() / totalExercisesCount.toFloat() else if (session.isCompleted) 1f else 0f

    // Temporizador de descanso interactivo integrado en la tarjeta
    var restTimerSeconds by remember { mutableIntStateOf(exercises.firstOrNull()?.restSeconds ?: 60) }
    var isRestTimerRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isRestTimerRunning) {
        if (isRestTimerRunning) {
            while (isRestTimerRunning && restTimerSeconds > 0) {
                delay(1000L)
                restTimerSeconds--
            }
            if (restTimerSeconds == 0) {
                isRestTimerRunning = false
                SensoryFeedbackHelper.vibrateSuccess(context)
                SensoryFeedbackHelper.playRewardSound()
            }
        }
    }

    TacticalCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("workout_item_${session.id}"),
        accentColor = if (session.isCompleted) ExecutiveSuccessGreen else TacticalNeonCyan.copy(alpha = 0.8f)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ENCABEZADO INTERACTIVO: Al tocar abre/cierra la rutina
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = session.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ExecutiveTextPrimary
                        )

                        // BADGE DE ESTADO
                        if (session.isCompleted) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = ExecutiveSuccessGreen.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, ExecutiveSuccessGreen.copy(alpha = 0.6f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ExecutiveSuccessGreen, modifier = Modifier.size(11.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("COMPLETADA", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ExecutiveSuccessGreen)
                                }
                            }
                        } else if (totalExercisesCount > 0 && completedExercisesCount > 0) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = TacticalNeonCyan.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, TacticalNeonCyan.copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = "$completedExercisesCount/$totalExercisesCount LISTOS",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TacticalNeonCyan,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = ExecutiveSecondaryAmber.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, ExecutiveSecondaryAmber.copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = "POR INICIAR",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ExecutiveSecondaryAmber,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

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
                        if (totalExercisesCount > 0) {
                            Text(text = " • ", color = ExecutiveTextSecondary, fontSize = 11.sp)
                            Text(
                                text = "$totalExercisesCount ejercicios",
                                fontSize = 11.sp,
                                color = TacticalVioletIdentity
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Botón para marcar/desmarcar sesión completa
                    IconButton(
                        onClick = {
                            SensoryFeedbackHelper.vibrateLightTick(context)
                            onToggleWorkoutSession(session.id)
                        },
                        modifier = Modifier.size(32.dp).testTag("btn_toggle_session_${session.id}")
                    ) {
                        Icon(
                            imageVector = if (session.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = if (session.isCompleted) "Completada" else "Marcar como completada",
                            tint = if (session.isCompleted) ExecutiveSuccessGreen else ExecutiveTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Botón de eliminar
                    IconButton(
                        onClick = { onDeleteWorkout(session) },
                        modifier = Modifier.size(32.dp).testTag("btn_delete_workout_${session.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Eliminar",
                            tint = ExecutiveTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Flecha expandir / colapsar
                    IconButton(
                        onClick = onToggleExpand,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Contraer" else "Expandir",
                            tint = TacticalNeonCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // CONTENIDO EXPANDIBLE DE LA RUTINA
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                    // NOTA O CONSEJO DE LA IA
                    if (session.notes.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, ExecutiveBorderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.SmartToy, contentDescription = null, tint = TacticalVioletIdentity, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = session.notes,
                                    fontSize = 12.sp,
                                    color = ExecutiveTextPrimary,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // BARRA DE PROGRESO DE LA RUTINA
                    if (totalExercisesCount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Progreso del entrenamiento",
                                fontSize = 11.sp,
                                color = ExecutiveTextSecondary
                            )
                            Text(
                                text = "$completedExercisesCount de $totalExercisesCount listos (${(progress * 100).toInt()}%)",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = if (progress == 1f) ExecutiveSuccessGreen else TacticalNeonCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = if (progress == 1f) ExecutiveSuccessGreen else TacticalNeonCyan,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // SECCIÓN DE EJERCICIOS INTERACTIVOS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "EJERCICIOS DE LA RUTINA",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = TacticalNeonCyan,
                            letterSpacing = 1.sp
                        )

                        TextButton(
                            onClick = onAddExerciseClick,
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = TacticalNeonCyan, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Añadir Ejercicio", fontSize = 11.sp, color = TacticalNeonCyan, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (exercises.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Esta rutina aún no tiene ejercicios registrados.",
                                    fontSize = 12.sp,
                                    color = ExecutiveTextSecondary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = onAddExerciseClick,
                                    colors = ButtonDefaults.buttonColors(containerColor = TacticalNeonCyan),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = ExecutiveDarkBg, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Agregar Ejercicios Ahora", color = ExecutiveDarkBg, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    } else {
                        exercises.forEach { ex ->
                            InteractiveExerciseRow(
                                exercise = ex,
                                onToggle = {
                                    SensoryFeedbackHelper.vibrateLightTick(context)
                                    onToggleExercise(ex.id)
                                },
                                onEditField = onEditField,
                                onDelete = { onDeleteExercise(ex) },
                                onStartRest = {
                                    restTimerSeconds = ex.restSeconds
                                    isRestTimerRunning = true
                                    SensoryFeedbackHelper.vibrateLightTick(context)
                                }
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // TEMPORIZADOR DE DESCANSO TÁCTICO INTEGRADO EN LA TARJETA
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, if (isRestTimerRunning) TacticalNeonCyan.copy(alpha = 0.7f) else ExecutiveBorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = if (isRestTimerRunning) TacticalNeonCyan else ExecutiveSecondaryAmber,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "TEMPORIZADOR DE DESCANSO",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isRestTimerRunning) TacticalNeonCyan else ExecutiveTextSecondary
                                    )
                                    val m = restTimerSeconds / 60
                                    val s = restTimerSeconds % 60
                                    Text(
                                        text = "%02d:%02d".format(m, s),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isRestTimerRunning) TacticalNeonCyan else ExecutiveTextPrimary
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                FilledTonalButton(
                                    onClick = {
                                        isRestTimerRunning = !isRestTimerRunning
                                        SensoryFeedbackHelper.vibrateLightTick(context)
                                    },
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = if (isRestTimerRunning) ExecutiveDangerRed.copy(alpha = 0.2f) else TacticalNeonCyan.copy(alpha = 0.2f),
                                        contentColor = if (isRestTimerRunning) ExecutiveDangerRed else TacticalNeonCyan
                                    ),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(if (isRestTimerRunning) "Pausar" else "Iniciar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        restTimerSeconds += 30
                                        SensoryFeedbackHelper.vibrateLightTick(context)
                                    },
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    border = BorderStroke(1.dp, ExecutiveBorderSubtle)
                                ) {
                                    Text("+30s", fontSize = 11.sp, color = ExecutiveTextPrimary)
                                }

                                TextButton(
                                    onClick = {
                                        isRestTimerRunning = false
                                        restTimerSeconds = exercises.firstOrNull()?.restSeconds ?: 60
                                    },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Reiniciar", tint = ExecutiveTextSecondary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // BOTONES DE ACCIÓN PRINCIPALES
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onStartWorkoutRunner,
                            colors = ButtonDefaults.buttonColors(containerColor = TacticalNeonCyan),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("btn_start_training_mode_${session.id}")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = ExecutiveDarkBg, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ENTRENAR AHORA", color = ExecutiveDarkBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        if (!session.isCompleted) {
                            Button(
                                onClick = {
                                    SensoryFeedbackHelper.vibrateSuccess(context)
                                    SensoryFeedbackHelper.playRewardSound()
                                    onToggleWorkoutSession(session.id)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ExecutiveSuccessGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).testTag("btn_complete_routine_${session.id}")
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = ExecutiveDarkBg, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("COMPLETAR (+100 XP)", color = ExecutiveDarkBg, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// FILA DE EJERCICIO INTERACTIVA CON CHECKBOX Y EDICIÓN RÁPIDA
// =========================================================================
@Composable
fun InteractiveExerciseRow(
    exercise: WorkoutExerciseEntity,
    onToggle: () -> Unit,
    onEditField: (label: String, currentValue: String, kbdType: KeyboardType, onSave: (String) -> Unit) -> Unit,
    onDelete: () -> Unit,
    onStartRest: () -> Unit
) {
    val checkColor by animateColorAsState(
        targetValue = if (exercise.isCompleted) ExecutiveSuccessGreen else Color.Transparent,
        label = "exCheck"
    )

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (exercise.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            if (exercise.isCompleted) ExecutiveSuccessGreen.copy(alpha = 0.4f) else ExecutiveBorderSubtle
        ),
        modifier = Modifier.fillMaxWidth().testTag("exercise_row_${exercise.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox táctil circular con target de 48dp
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clickable { onToggle() }
                    .testTag("toggle_exercise_${exercise.id}"),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(checkColor)
                        .border(
                            2.dp,
                            if (exercise.isCompleted) ExecutiveSuccessGreen else ExecutiveTextSecondary.copy(alpha = 0.5f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (exercise.isCompleted) {
                        Icon(Icons.Default.Check, contentDescription = "Completado", tint = ExecutiveDarkBg, modifier = Modifier.size(15.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.exerciseName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (exercise.isCompleted) ExecutiveTextSecondary else ExecutiveTextPrimary,
                    textDecoration = if (exercise.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Chips de Series, Reps, Peso y Descanso (editables al tocar)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "${exercise.sets} series",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TacticalNeonCyan,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "${exercise.reps} reps",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = ExecutiveTextWhite,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "${exercise.weightKg} kg",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = ExecutiveSecondaryAmber,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.clickable { onStartRest() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = TacticalVioletIdentity, modifier = Modifier.size(10.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${exercise.restSeconds}s",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TacticalVioletIdentity
                            )
                        }
                    }
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp).testTag("delete_exercise_${exercise.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Eliminar ejercicio",
                    tint = ExecutiveTextSecondary.copy(alpha = 0.6f),
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

// =========================================================================
// RUNNER / HUD DE ENTRENAMIENTO ACTIVO INTERACTIVO
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutRunnerModal(
    session: WorkoutSessionEntity,
    exercises: List<WorkoutExerciseEntity>,
    onToggleExercise: (Long) -> Unit,
    onCompleteSession: () -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var currentExerciseIndex by remember { mutableIntStateOf(0) }
    var elapsedSeconds by remember { mutableLongStateOf(0L) }
    val currentExercise = exercises.getOrNull(currentExerciseIndex)

    // Series completadas para cada ejercicio: Map<exerciseId, Set<setNumber>>
    val completedSetsMap = remember { mutableStateMapOf<Long, MutableSet<Int>>() }

    // Temporizador de descanso
    var restSecondsLeft by remember { mutableIntStateOf(currentExercise?.restSeconds ?: 60) }
    var isResting by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            elapsedSeconds++
        }
    }

    LaunchedEffect(isResting) {
        if (isResting) {
            while (isResting && restSecondsLeft > 0) {
                delay(1000L)
                restSecondsLeft--
            }
            if (restSecondsLeft == 0) {
                isResting = false
                SensoryFeedbackHelper.vibrateAlert(context)
                SensoryFeedbackHelper.playRewardSound()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onClose,
        modifier = Modifier.fillMaxWidth().fillMaxHeight(0.92f),
        containerColor = ExecutiveDarkBg,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MODO ENTRENAMIENTO",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TacticalNeonCyan,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = session.title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = ExecutiveTextWhite
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val m = elapsedSeconds / 60
                    val s = elapsedSeconds % 60
                    Text(
                        text = "%02d:%02d".format(m, s),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TacticalNeonCyan
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = ExecutiveTextSecondary)
                    }
                }
            }
        },
        text = {
            if (exercises.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay ejercicios registrados en esta rutina.", color = ExecutiveTextSecondary)
                }
            } else if (currentExercise != null) {
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Paginador de ejercicios
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "EJERCICIO ${currentExerciseIndex + 1} DE ${exercises.size}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TacticalVioletIdentity,
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            FilledTonalIconButton(
                                onClick = {
                                    if (currentExerciseIndex > 0) {
                                        currentExerciseIndex--
                                        restSecondsLeft = exercises[currentExerciseIndex].restSeconds
                                        isResting = false
                                    }
                                },
                                enabled = currentExerciseIndex > 0,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "Anterior", tint = TacticalNeonCyan)
                            }
                            FilledTonalIconButton(
                                onClick = {
                                    if (currentExerciseIndex < exercises.size - 1) {
                                        currentExerciseIndex++
                                        restSecondsLeft = exercises[currentExerciseIndex].restSeconds
                                        isResting = false
                                    }
                                },
                                enabled = currentExerciseIndex < exercises.size - 1,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.ChevronRight, contentDescription = "Siguiente", tint = TacticalNeonCyan)
                            }
                        }
                    }

                    // Tarjeta táctica del ejercicio activo
                    TacticalCard(
                        modifier = Modifier.fillMaxWidth(),
                        accentColor = TacticalNeonCyan
                    ) {
                        Column {
                            Text(
                                text = currentExercise.exerciseName,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = ExecutiveTextWhite
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("OBJETIVO", fontSize = 10.sp, color = ExecutiveTextSecondary)
                                    Text("${currentExercise.sets} series x ${currentExercise.reps}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TacticalNeonCyan)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("PESO", fontSize = 10.sp, color = ExecutiveTextSecondary)
                                    Text("${currentExercise.weightKg} kg", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ExecutiveSecondaryAmber)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("DESCANSO", fontSize = 10.sp, color = ExecutiveTextSecondary)
                                    Text("${currentExercise.restSeconds}s", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TacticalVioletIdentity)
                                }
                            }
                        }
                    }

                    // REGISTRO DE SERIES INTERACTIVO
                    Text(
                        text = "REGISTRO DE SERIES",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = ExecutiveTextSecondary,
                        fontWeight = FontWeight.Bold
                    )

                    val completedSets = completedSetsMap.getOrPut(currentExercise.id) { mutableSetOf() }

                    (1..currentExercise.sets).forEach { setNum ->
                        val isSetDone = completedSets.contains(setNum)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSetDone) ExecutiveSuccessGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, if (isSetDone) ExecutiveSuccessGreen.copy(alpha = 0.6f) else ExecutiveBorderSubtle),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isSetDone) {
                                        completedSets.remove(setNum)
                                    } else {
                                        completedSets.add(setNum)
                                        SensoryFeedbackHelper.vibrateLightTick(context)
                                        // Iniciar descanso automáticamente
                                        restSecondsLeft = currentExercise.restSeconds
                                        isResting = true
                                        // Si completó todas las series, marcar ejercicio
                                        if (completedSets.size == currentExercise.sets && !currentExercise.isCompleted) {
                                            onToggleExercise(currentExercise.id)
                                        }
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(if (isSetDone) ExecutiveSuccessGreen else Color.Transparent)
                                            .border(2.dp, if (isSetDone) ExecutiveSuccessGreen else ExecutiveTextSecondary, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSetDone) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = ExecutiveDarkBg, modifier = Modifier.size(15.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Serie $setNum",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isSetDone) ExecutiveTextSecondary else ExecutiveTextWhite
                                    )
                                }

                                Text(
                                    text = "${currentExercise.reps} reps @ ${currentExercise.weightKg} kg",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    color = if (isSetDone) ExecutiveSuccessGreen else ExecutiveTextSecondary
                                )
                            }
                        }
                    }

                    // TEMPORIZADOR DE DESCANSO EN EL RUNNER
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, if (isResting) TacticalNeonCyan else ExecutiveBorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isResting) "DESCANSO EN CURSO" else "TEMPORIZADOR DE DESCANSO",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (isResting) TacticalNeonCyan else ExecutiveTextSecondary
                            )
                            val rm = restSecondsLeft / 60
                            val rs = restSecondsLeft % 60
                            Text(
                                text = "%02d:%02d".format(rm, rs),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isResting) TacticalNeonCyan else ExecutiveTextWhite
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        isResting = !isResting
                                        SensoryFeedbackHelper.vibrateLightTick(context)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isResting) ExecutiveDangerRed else TacticalNeonCyan
                                    ),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (isResting) "Pausar" else "Iniciar Descanso",
                                        color = ExecutiveDarkBg,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        restSecondsLeft += 30
                                        SensoryFeedbackHelper.vibrateLightTick(context)
                                    },
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("+30s", color = ExecutiveTextWhite)
                                }

                                if (isResting) {
                                    TextButton(onClick = { isResting = false }) {
                                        Text("Omitir", color = ExecutiveTextSecondary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    SensoryFeedbackHelper.vibrateSuccess(context)
                    SensoryFeedbackHelper.playRewardSound()
                    onCompleteSession()
                    onClose()
                },
                colors = ButtonDefaults.buttonColors(containerColor = TacticalNeonCyan),
                modifier = Modifier.fillMaxWidth().testTag("btn_finish_active_workout")
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ExecutiveDarkBg, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("FINALIZAR SESIÓN (+100 XP)", color = ExecutiveDarkBg, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onClose) {
                Text("Continuar luego", color = ExecutiveTextSecondary)
            }
        }
    )
}

// =========================================================================
// DIÁLOGO PARA AGREGAR UN EJERCICIO A UNA RUTINA EXISTENTE
// =========================================================================
@Composable
fun AddExerciseToSessionDialog(
    sessionId: Long,
    name: String,
    onNameChange: (String) -> Unit,
    sets: String,
    onSetsChange: (String) -> Unit,
    reps: String,
    onRepsChange: (String) -> Unit,
    weight: String,
    onWeightChange: (String) -> Unit,
    rest: String,
    onRestChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (name: String, sets: Int, reps: String, weightKg: Double, restSeconds: Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ExecutiveDarkSurface,
        title = {
            Text("Añadir Ejercicio a la Rutina", color = ExecutiveTextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Nombre del ejercicio") },
                    placeholder = { Text("Ej: Press militar con mancuernas") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_new_exercise_name")
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = sets,
                        onValueChange = { onSetsChange(it.filter { ch -> ch.isDigit() }) },
                        label = { Text("Series") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = reps,
                        onValueChange = onRepsChange,
                        label = { Text("Reps") },
                        placeholder = { Text("10, 8, 8") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = weight,
                        onValueChange = onWeightChange,
                        label = { Text("Peso (kg)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = rest,
                        onValueChange = { onRestChange(it.filter { ch -> ch.isDigit() }) },
                        label = { Text("Descanso (s)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(
                            name.trim(),
                            sets.toIntOrNull() ?: 3,
                            reps.trim().ifBlank { "10" },
                            weight.toDoubleOrNull() ?: 0.0,
                            rest.toIntOrNull() ?: 60
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = TacticalNeonCyan),
                enabled = name.isNotBlank(),
                modifier = Modifier.testTag("btn_confirm_add_exercise")
            ) {
                Text("Agregar", color = ExecutiveDarkBg, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = ExecutiveTextSecondary)
            }
        }
    )
}
