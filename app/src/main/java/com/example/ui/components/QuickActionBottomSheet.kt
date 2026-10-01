package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.*

enum class QuickActionType {
    NONE,
    ADD_TASK,
    ADD_HABIT,
    ADD_TRANSACTION
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickActionBottomSheet(
    onDismiss: () -> Unit,
    onAddTask: (title: String, priority: String, estimatedMinutes: Int) -> Unit,
    onAddHabit: (title: String, category: String) -> Unit,
    onAddTransaction: (type: String, amount: Double, category: String, desc: String) -> Unit,
    onStartTimer: (activity: String) -> Unit,
    onOpenFitness: () -> Unit = {},
    onOpenChat: () -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var activeAction by remember { mutableStateOf(QuickActionType.NONE) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.testTag("bottom_sheet_quick_action")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            when (activeAction) {
                QuickActionType.NONE -> {
                    Text(
                        text = stringResource(R.string.qa_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.qa_subtitle),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    QuickActionTile(
                        icon = Icons.Default.Checklist,
                        iconTint = ExecutivePrimaryTeal,
                        title = stringResource(R.string.qa_add_task_title),
                        subtitle = stringResource(R.string.qa_add_task_desc),
                        onClick = { activeAction = QuickActionType.ADD_TASK }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    QuickActionTile(
                        icon = Icons.Default.FitnessCenter,
                        iconTint = ExecutiveSecondaryAmber,
                        title = stringResource(R.string.qa_add_habit_title),
                        subtitle = stringResource(R.string.qa_add_habit_desc),
                        onClick = { activeAction = QuickActionType.ADD_HABIT }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    QuickActionTile(
                        icon = Icons.Default.SportsScore,
                        iconTint = ExecutiveElectricViolet,
                        title = stringResource(R.string.qa_open_fitness_title),
                        subtitle = stringResource(R.string.qa_open_fitness_desc),
                        onClick = {
                            onDismiss()
                            onOpenFitness()
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    QuickActionTile(
                        icon = Icons.Default.AccountBalanceWallet,
                        iconTint = ExecutiveSuccessGreen,
                        title = stringResource(R.string.qa_add_tx_title),
                        subtitle = stringResource(R.string.qa_add_tx_desc),
                        onClick = { activeAction = QuickActionType.ADD_TRANSACTION }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    QuickActionTile(
                        icon = Icons.Default.Timer,
                        iconTint = ExecutivePurple,
                        title = stringResource(R.string.qa_start_timer_title),
                        subtitle = stringResource(R.string.qa_start_timer_desc),
                        onClick = {
                            onStartTimer("Focus")
                            onDismiss()
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    QuickActionTile(
                        icon = Icons.Default.AutoAwesome,
                        iconTint = ExecutiveCyan,
                        title = stringResource(R.string.qa_open_chat_title),
                        subtitle = stringResource(R.string.qa_open_chat_desc),
                        onClick = {
                            onDismiss()
                            onOpenChat()
                        }
                    )
                }

                QuickActionType.ADD_TASK -> {
                    FormAddTask(
                        onBack = { activeAction = QuickActionType.NONE },
                        onConfirm = { title, priority, mins ->
                            onAddTask(title, priority, mins)
                            onDismiss()
                        }
                    )
                }

                QuickActionType.ADD_HABIT -> {
                    FormAddHabit(
                        onBack = { activeAction = QuickActionType.NONE },
                        onConfirm = { title, category ->
                            onAddHabit(title, category)
                            onDismiss()
                        }
                    )
                }

                QuickActionType.ADD_TRANSACTION -> {
                    FormAddTransaction(
                        onBack = { activeAction = QuickActionType.NONE },
                        onConfirm = { type, amount, cat, desc ->
                            onAddTransaction(type, amount, cat, desc)
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionTile(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun FormAddTask(
    onBack: () -> Unit,
    onConfirm: (title: String, priority: String, estimatedMinutes: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("ALTA") }
    var minutes by remember { mutableIntStateOf(30) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.onboarding_back))
        }
        Text(
            text = stringResource(R.string.qa_new_priority_task),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
        value = title,
        onValueChange = { title = it },
        label = { Text(stringResource(R.string.qa_task_focus_question)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().testTag("input_quick_task_title")
    )

    Spacer(modifier = Modifier.height(12.dp))

    Text(stringResource(R.string.qa_priority_level), style = MaterialTheme.typography.labelMedium)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
        listOf("ALTA", "MEDIA", "BAJA").forEach { p ->
            FilterChip(
                selected = priority == p,
                onClick = { priority = p },
                label = { Text(p) }
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    Text(stringResource(R.string.qa_estimated_duration), style = MaterialTheme.typography.labelMedium)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
        listOf(15 to "15m", 30 to "30m", 45 to "45m", 60 to "1h").forEach { (m, label) ->
            FilterChip(
                selected = minutes == m,
                onClick = { minutes = m },
                label = { Text(label) }
            )
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    Button(
        onClick = { if (title.isNotBlank()) onConfirm(title.trim(), priority, minutes) },
        enabled = title.isNotBlank(),
        colors = ButtonDefaults.buttonColors(containerColor = ExecutivePrimaryTeal),
        modifier = Modifier.fillMaxWidth().testTag("btn_save_quick_task")
    ) {
        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(stringResource(R.string.qa_btn_add_task))
    }
}

@Composable
private fun FormAddHabit(
    onBack: () -> Unit,
    onConfirm: (title: String, category: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Salud") }

    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.onboarding_back))
        }
        Text(
            text = stringResource(R.string.qa_register_habit_title),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
        value = title,
        onValueChange = { title = it },
        label = { Text(stringResource(R.string.habits_name_label)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().testTag("input_quick_habit_title")
    )

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
        value = category,
        onValueChange = { category = it },
        label = { Text(stringResource(R.string.habits_category_label)) },
        placeholder = { Text(stringResource(R.string.qa_habit_category_placeholder)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().testTag("input_quick_habit_category")
    )

    Spacer(modifier = Modifier.height(20.dp))

    Button(
        onClick = { if (title.isNotBlank()) onConfirm(title.trim(), category) },
        enabled = title.isNotBlank(),
        colors = ButtonDefaults.buttonColors(containerColor = ExecutiveSecondaryAmber),
        modifier = Modifier.fillMaxWidth().testTag("btn_save_quick_habit")
    ) {
        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(stringResource(R.string.qa_btn_save_habit))
    }
}

@Composable
private fun FormAddTransaction(
    onBack: () -> Unit,
    onConfirm: (type: String, amount: Double, category: String, desc: String) -> Unit
) {
    var type by remember { mutableStateOf("GASTO") }
    var amountText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Alimentación") }
    var desc by remember { mutableStateOf("") }

    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.onboarding_back))
        }
        Text(
            text = stringResource(R.string.qa_register_tx_title),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(
            onClick = { type = "GASTO" },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (type == "GASTO") ExecutiveDangerRed else MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.weight(1f)
        ) {
            Text(stringResource(R.string.hub_expenses_label))
        }
        Button(
            onClick = { type = "INGRESO" },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (type == "INGRESO") ExecutiveSuccessGreen else MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.weight(1f)
        ) {
            Text(stringResource(R.string.hub_income_label))
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
        value = amountText,
        onValueChange = { amountText = it },
        label = { Text(stringResource(R.string.qa_tx_amount_hint, "$")) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        modifier = Modifier.fillMaxWidth().testTag("input_quick_amount")
    )

    Spacer(modifier = Modifier.height(10.dp))

    OutlinedTextField(
        value = desc,
        onValueChange = { desc = it },
        label = { Text(stringResource(R.string.qa_tx_concept)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
        value = category,
        onValueChange = { category = it },
        label = { Text(stringResource(R.string.habits_category_label)) },
        placeholder = { Text(stringResource(R.string.qa_habit_category_placeholder)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().testTag("input_quick_category")
    )

    Spacer(modifier = Modifier.height(20.dp))

    val amountDouble = amountText.toDoubleOrNull() ?: 0.0

    Button(
        onClick = {
            if (amountDouble > 0) {
                onConfirm(type, amountDouble, category, desc.ifBlank { category })
            }
        },
        enabled = amountDouble > 0,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (type == "GASTO") ExecutiveDangerRed else ExecutiveSuccessGreen
        ),
        modifier = Modifier.fillMaxWidth().testTag("btn_save_quick_trans")
    ) {
        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(stringResource(R.string.qa_tx_btn_record))
    }
}
