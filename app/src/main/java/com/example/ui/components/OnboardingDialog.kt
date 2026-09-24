package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.R
import com.example.ui.theme.*

@Composable
fun OnboardingDialog(
    onDismiss: () -> Unit,
    onComplete: (selectedHabits: List<String>, cycleStartTime: String) -> Unit,
    onEnableReminder: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var currentStep by remember { mutableIntStateOf(1) }
    val totalSteps = 3

    var selectedCycleTime by remember { mutableStateOf("05:00") }
    val availableHabits = listOf(
        "Ejercicio 30 min" to "Salud y energía corporal",
        "Lectura diaria" to "Crecimiento personal y estudio",
        "Planificación matutina" to "Prioridades del ciclo diario"
    )
    val selectedHabits = remember { mutableStateListOf("Ejercicio 30 min", "Lectura diaria") }
    var reminderEnabled by remember { mutableStateOf(false) }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        reminderEnabled = granted
        if (granted) {
            onEnableReminder("20:00")
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .testTag("dialog_onboarding")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header con indicador de pasos
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Paso $currentStep de $totalSteps",
                        style = MaterialTheme.typography.labelMedium,
                        color = ExecutivePurple
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp).testTag("btn_close_onboarding")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Barra de progreso de pasos
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (step in 1..totalSteps) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(
                                    if (step <= currentStep) ExecutivePurple else MaterialTheme.colorScheme.surfaceVariant
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Contenido dinámico del paso
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    AnimatedContent(
                        targetState = currentStep,
                        label = "onboarding_step_transition"
                    ) { step ->
                        when (step) {
                            1 -> OnboardingStepWelcome()
                            2 -> OnboardingStepCycle(
                                selectedTime = selectedCycleTime,
                                onSelectTime = { selectedCycleTime = it }
                            )
                            3 -> OnboardingStepHabits(
                                availableHabits = availableHabits,
                                selectedHabits = selectedHabits,
                                reminderEnabled = reminderEnabled,
                                onToggleHabit = { habit ->
                                    if (selectedHabits.contains(habit)) {
                                        selectedHabits.remove(habit)
                                    } else {
                                        selectedHabits.add(habit)
                                    }
                                },
                                onRequestNotifications = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        val granted = ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.POST_NOTIFICATIONS
                                        ) == PackageManager.PERMISSION_GRANTED
                                        if (granted) {
                                            reminderEnabled = true
                                            onEnableReminder("20:00")
                                        } else {
                                            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        }
                                    } else {
                                        reminderEnabled = true
                                        onEnableReminder("20:00")
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Barra inferior de navegación
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep > 1) {
                        OutlinedButton(
                            onClick = { currentStep-- },
                            modifier = Modifier.testTag("btn_onboarding_prev")
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Atrás")
                        }
                    } else {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("btn_onboarding_skip")
                        ) {
                            Text("Omitir", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    if (currentStep < totalSteps) {
                        Button(
                            onClick = { currentStep++ },
                            colors = ButtonDefaults.buttonColors(containerColor = ExecutivePurple),
                            modifier = Modifier.testTag("btn_onboarding_next")
                        ) {
                            Text("Siguiente")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    } else {
                        Button(
                            onClick = {
                                onComplete(selectedHabits.toList(), selectedCycleTime)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ExecutivePurple),
                            modifier = Modifier.testTag("btn_onboarding_finish")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Comenzar mi Sistema")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingStepWelcome() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hero Art Illustration
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_onboarding_hero),
                contentDescription = "Ilustración de Bienvenida",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Bienvenido a tu Sistema Personal",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Diseñado para profesionales que buscan claridad, consistencia y enfoque sin distracciones. Todos tus datos se almacenan 100% en tu dispositivo (Offline-first).",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Pilares
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PillarBadge(title = "Ciclo 24h", subtitle = "Sin reset a medianoche", modifier = Modifier.weight(1f))
            PillarBadge(title = "Privacidad", subtitle = "Room SQLite Local", modifier = Modifier.weight(1f))
            PillarBadge(title = "Consistencia", subtitle = "Rachas y enfoque", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun PillarBadge(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Surface(
        color = ExecutivePurple.copy(alpha = 0.08f),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = ExecutivePurple)
            Text(subtitle, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun OnboardingStepCycle(
    selectedTime: String,
    onSelectTime: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                color = ExecutivePurple.copy(alpha = 0.15f),
                shape = CircleShape,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = ExecutivePurple, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Configura tu Ciclo Diario",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Tu día termina cuando tú descansas, no a medianoche",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "¿A qué hora suele comenzar tu jornada diaria productiva?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        val options = listOf(
            "04:00" to "Madrugador ejecutivo",
            "05:00" to "Recomendado (Estándar 05:00 a 04:59)",
            "06:00" to "Jornada matutina tradicional",
            "07:00" to "Horario flexible"
        )

        options.forEach { (time, desc) ->
            val isSelected = selectedTime == time
            Surface(
                color = if (isSelected) ExecutivePurple.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp),
                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, ExecutivePurple) else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onSelectTime(time) }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onSelectTime(time) },
                        colors = RadioButtonDefaults.colors(selectedColor = ExecutivePurple)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "$time AM",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) ExecutivePurple else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingStepHabits(
    availableHabits: List<Pair<String, String>>,
    selectedHabits: List<String>,
    reminderEnabled: Boolean,
    onToggleHabit: (String) -> Unit,
    onRequestNotifications: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                color = ExecutiveSuccessGreen.copy(alpha = 0.15f),
                shape = CircleShape,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ExecutiveSuccessGreen, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Tus Primeros Hábitos Clave",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Selecciona con cuáles deseas arrancar hoy",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        availableHabits.forEach { (title, desc) ->
            val isChecked = selectedHabits.contains(title)
            Surface(
                color = if (isChecked) ExecutiveSuccessGreen.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp),
                border = if (isChecked) androidx.compose.foundation.BorderStroke(1.dp, ExecutiveSuccessGreen.copy(alpha = 0.5f)) else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onToggleHabit(title) }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { onToggleHabit(title) },
                        colors = CheckboxDefaults.colors(checkedColor = ExecutiveSuccessGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tarjeta de recordatorio y permiso de notificación
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = if (reminderEnabled) ExecutiveSecondaryAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Recordatorio diario a las 20:00",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (reminderEnabled) "Activado con éxito" else "Te avisa para no perder tus rachas",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (reminderEnabled) ExecutiveSuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!reminderEnabled) {
                    TextButton(
                        onClick = onRequestNotifications,
                        modifier = Modifier.testTag("btn_onboarding_enable_notif")
                    ) {
                        Text("Activar", color = ExecutivePurple)
                    }
                } else {
                    Icon(Icons.Default.Check, contentDescription = null, tint = ExecutiveSuccessGreen, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
