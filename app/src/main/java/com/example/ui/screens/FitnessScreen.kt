package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WorkoutExerciseEntity
import com.example.data.model.WorkoutSessionEntity
import com.example.ui.PersonalOsUiState
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FitnessScreen(
    state: PersonalOsUiState,
    onAddWorkout: (title: String, goal: String, duration: Int, notes: String, exercises: List<WorkoutExerciseEntity>) -> Unit,
    onDeleteWorkout: (WorkoutSessionEntity) -> Unit,
    onAnalyzeAi: () -> Unit,
    onClose: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    // Campos para nueva sesión
    var workoutTitle by remember { mutableStateOf("") }
    var targetGoal by remember { mutableStateOf("Hipertrofia") }
    var durationMinutes by remember { mutableStateOf("45") }
    var notesText by remember { mutableStateOf("") }

    // Lista temporal de ejercicios
    val exercises = remember { mutableStateListOf<WorkoutExerciseEntity>() }
    var exName by remember { mutableStateOf("") }
    var exSets by remember { mutableStateOf("3") }
    var exReps by remember { mutableStateOf("10, 10, 8") }
    var exWeight by remember { mutableStateOf("20") }
    var exRest by remember { mutableStateOf("60") }

    Box(modifier = Modifier.fillMaxSize().background(ExecutiveDarkBg)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header táctico
            ExecutiveHeader(
                title = "Módulo de Entrenamiento",
                subtitle = "Sobrecarga progresiva y rendimiento físico",
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
                                Text("Registrar", color = ExecutiveDarkBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
                                        Text("Analizar Volumen", fontSize = 11.sp, color = TacticalVioletIdentity, fontWeight = FontWeight.Bold)
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

    // DIÁLOGO PARA REGISTRAR NUEVO ENTRENAMIENTO
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = ExecutiveDarkSurface,
            title = {
                Text(
                    text = "Registrar Sesión de Entrenamiento",
                    color = ExecutiveTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = workoutTitle,
                        onValueChange = { workoutTitle = it },
                        label = { Text("Nombre de la rutina") },
                        modifier = Modifier.fillMaxWidth().testTag("input_workout_title"),
                        singleLine = true
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = targetGoal,
                            onValueChange = { targetGoal = it },
                            label = { Text("Objetivo") },
                            modifier = Modifier.weight(1f).testTag("input_workout_goal"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = durationMinutes,
                            onValueChange = { durationMinutes = it },
                            label = { Text("Minutos") },
                            modifier = Modifier.weight(1f).testTag("input_workout_duration"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        label = { Text("Notas tácticas") },
                        modifier = Modifier.fillMaxWidth().testTag("input_workout_notes"),
                        maxLines = 2
                    )

                    HorizontalDivider(color = ExecutiveBorderSubtle)

                    Text(
                        text = "Agregar Ejercicio a la Rutina:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TacticalNeonCyan
                    )

                    OutlinedTextField(
                        value = exName,
                        onValueChange = { exName = it },
                        label = { Text("Ejercicio") },
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
                            if (exName.isNotBlank()) {
                                exercises.add(
                                    WorkoutExerciseEntity(
                                        sessionId = 0,
                                        exerciseName = exName.trim(),
                                        sets = exSets.toIntOrNull() ?: 3,
                                        reps = exReps.trim(),
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
                        Text("Añadir Ejercicio (${exercises.size} listos)", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (workoutTitle.isNotBlank()) {
                            onAddWorkout(
                                workoutTitle.trim(),
                                targetGoal.trim(),
                                durationMinutes.toIntOrNull() ?: 45,
                                notesText.trim(),
                                exercises.toList()
                            )
                            showAddDialog = false
                            workoutTitle = ""
                            exercises.clear()
                        }
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
