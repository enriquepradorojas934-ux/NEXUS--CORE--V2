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
import androidx.compose.ui.res.stringResource
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
    val habit1 = stringResource(R.string.onboarding_habit_1_title)
    val habit2 = stringResource(R.string.onboarding_habit_2_title)
    val habit3 = stringResource(R.string.onboarding_habit_3_title)
    val availableHabits = listOf(
        habit1 to stringResource(R.string.onboarding_habit_1_desc),
        habit2 to stringResource(R.string.onboarding_habit_2_desc),
        habit3 to stringResource(R.string.onboarding_habit_3_desc)
    )
    val selectedHabits = remember { mutableStateListOf(habit1, habit2) }
    var reminderEnabled by remember { mutableStateOf(false) }

    val registryOwner = androidx.activity.compose.LocalActivityResultRegistryOwner.current
        ?: (context as? androidx.activity.result.ActivityResultRegistryOwner)

    val notificationLauncher = if (registryOwner != null) {
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->
            reminderEnabled = granted
            if (granted) {
                onEnableReminder("20:00")
            }
        }
    } else null

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
                        text = stringResource(R.string.onboarding_step_count, currentStep, totalSteps),
                        style = MaterialTheme.typography.labelMedium,
                        color = ExecutivePurple
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp).testTag("btn_close_onboarding")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_close), tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                        } else if (notificationLauncher != null) {
                                            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        } else {
                                            reminderEnabled = true
                                            onEnableReminder("20:00")
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
                            Text(stringResource(R.string.onboarding_back))
                        }
                    } else {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("btn_onboarding_skip")
                        ) {
                            Text(stringResource(R.string.onboarding_skip), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    if (currentStep < totalSteps) {
                        Button(
                            onClick = { currentStep++ },
                            colors = ButtonDefaults.buttonColors(containerColor = ExecutivePurple),
                            modifier = Modifier.testTag("btn_onboarding_next")
                        ) {
                            Text(stringResource(R.string.onboarding_next))
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
                            Text(stringResource(R.string.onboarding_start_system))
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
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.onboarding_welcome_title),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.onboarding_welcome_desc),
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
            PillarBadge(title = stringResource(R.string.onboarding_pillar_cycle_title), subtitle = stringResource(R.string.onboarding_pillar_cycle_sub), modifier = Modifier.weight(1f))
            PillarBadge(title = stringResource(R.string.onboarding_pillar_privacy_title), subtitle = stringResource(R.string.onboarding_pillar_privacy_sub), modifier = Modifier.weight(1f))
            PillarBadge(title = stringResource(R.string.onboarding_pillar_consistency_title), subtitle = stringResource(R.string.onboarding_pillar_consistency_sub), modifier = Modifier.weight(1f))
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
                    text = stringResource(R.string.onboarding_cycle_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.onboarding_cycle_subtitle),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = stringResource(R.string.onboarding_cycle_question),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        val options = listOf(
            "04:00" to stringResource(R.string.onboarding_opt_0400),
            "05:00" to stringResource(R.string.onboarding_opt_0500),
            "06:00" to stringResource(R.string.onboarding_opt_0600),
            "07:00" to stringResource(R.string.onboarding_opt_0700)
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
                    text = stringResource(R.string.onboarding_habits_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.onboarding_habits_subtitle),
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
                        text = stringResource(R.string.onboarding_reminder_title),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (reminderEnabled) stringResource(R.string.onboarding_reminder_active) else stringResource(R.string.onboarding_reminder_inactive),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (reminderEnabled) ExecutiveSuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!reminderEnabled) {
                    TextButton(
                        onClick = onRequestNotifications,
                        modifier = Modifier.testTag("btn_onboarding_enable_notif")
                    ) {
                        Text(stringResource(R.string.onboarding_reminder_activate), color = ExecutivePurple)
                    }
                } else {
                    Icon(Icons.Default.Check, contentDescription = null, tint = ExecutiveSuccessGreen, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
