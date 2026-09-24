package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FinancialGoalEntity
import com.example.data.model.TransactionEntity
import com.example.ui.PersonalOsUiState
import com.example.ui.components.ExecutiveHeader
import com.example.ui.theme.*

@Composable
fun FinanceScreen(
    state: PersonalOsUiState,
    onAddTransaction: (String, Double, String, String) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    onAddFinancialGoal: (String, Double, String) -> Unit,
    onContributeToGoal: (FinancialGoalEntity, Double) -> Unit,
    onAddAssetDebt: (String, String, Double, String) -> Unit,
    onAddTransactionNatural: (String) -> Unit = {},
    onAnalyzeFinanceAi: () -> Unit = {}
) {
    var showTxDialog by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var showAssetDebtDialog by remember { mutableStateOf(false) }
    var contributeGoalTarget by remember { mutableStateOf<FinancialGoalEntity?>(null) }
    var naturalTxText by remember { mutableStateOf("") }
    var txFilterType by remember { mutableStateOf("TODOS") } // "TODOS", "GASTO", "INGRESO"
    var txSearchQuery by remember { mutableStateOf("") }

    val filteredTransactions = remember(state.transactionsToday, txFilterType, txSearchQuery) {
        state.transactionsToday.filter { tx ->
            val matchesType = when (txFilterType) {
                "GASTO" -> tx.type == "GASTO"
                "INGRESO" -> tx.type == "INGRESO"
                else -> true
            }
            val matchesSearch = txSearchQuery.isBlank() ||
                tx.description.contains(txSearchQuery, ignoreCase = true) ||
                tx.category.contains(txSearchQuery, ignoreCase = true)
            matchesType && matchesSearch
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ExecutiveHeader(
            title = "Finanzas Personales",
            subtitle = "Ingresos, gastos, patrimonio y metas de ahorro",
            cycleDate = state.currentCycleDate,
            timeUntilReset = state.timeUntilNextCycle,
            cycleProgress = state.cycleProgress,
            actionIcon = Icons.Default.Add,
            onActionClick = { showTxDialog = true }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ENTRADA EN LENGUAJE NATURAL PARA FINANZAS
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("card_natural_finance_input")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = TacticalWarningAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "REGISTRO RÁPIDO INTELIGENTE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = TacticalWarningAmber
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = naturalTxText,
                                onValueChange = { naturalTxText = it },
                                placeholder = { Text("Ej: Gasté 15000 en comida o Cobré 50000", fontSize = 12.sp) },
                                modifier = Modifier.weight(1f).testTag("input_natural_tx"),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (naturalTxText.isNotBlank()) {
                                        onAddTransactionNatural(naturalTxText.trim())
                                        naturalTxText = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TacticalWarningAmber),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("btn_submit_natural_tx")
                            ) {
                                Text("Añadir", color = ExecutiveDarkBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // ASESOR FINANCIERO GEMINI
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("card_ai_finance_advisor")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = TacticalLuminousBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ASESOR FINANCIERO IA",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = TacticalLuminousBlue
                                )
                            }

                            if (state.isAnalyzingFinance) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = TacticalLuminousBlue
                                )
                            } else {
                                FilledTonalButton(
                                    onClick = onAnalyzeFinanceAi,
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = TacticalLuminousBlue.copy(alpha = 0.15f)
                                    ),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("btn_analyze_finance_ai")
                                ) {
                                    Text("Analizar Flujo", fontSize = 11.sp, color = TacticalLuminousBlue, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (!state.aiFinancialAdvice.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = ExecutiveBorderSubtle)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.aiFinancialAdvice,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 18.sp
                            )
                        } else {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Pulsa 'Analizar Flujo' para evaluar tu tasa de ahorro, patrimonio y salud frente a imprevistos.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            // Tarjeta de Balance y Flujo
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().testTag("finance_balance_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "FLUJO NETO REGISTRADO",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = ExecutiveSecondaryAmber
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$${String.format("%.2f", state.netBalance)}",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (state.netBalance >= 0) ExecutiveSuccessGreen else ExecutiveDangerRed
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Ingresos", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "+$${String.format("%.2f", state.totalIncome)}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = ExecutiveSuccessGreen
                                )
                            }
                            Column {
                                Text("Gastos", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "-$${String.format("%.2f", state.totalExpenses)}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = ExecutiveDangerRed
                                )
                            }
                            Column {
                                Text("Patrimonio Neto", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "$${String.format("%.2f", state.totalAssets - state.totalDebts)}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = ExecutiveAccentBlue
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { showTxDialog = true },
                                modifier = Modifier.weight(1f).testTag("btn_record_tx"),
                                colors = ButtonDefaults.buttonColors(containerColor = ExecutiveSecondaryAmber)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Transacción")
                            }

                            OutlinedButton(
                                onClick = { showGoalDialog = true },
                                modifier = Modifier.weight(1f).testTag("btn_new_fin_goal")
                            ) {
                                Text("Meta Ahorro")
                            }
                        }
                    }
                }
            }

            // Metas Financieras
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Metas de Ahorro y Capital",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (state.financialGoals.isEmpty()) {
                item {
                    Text(
                        text = "No tienes metas de ahorro definidas (ej: Fondo de Emergencia).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(state.financialGoals, key = { it.id }) { goal ->
                    val progress = if (goal.targetAmount > 0) {
                        (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f)
                    } else 0f

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = goal.name,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${(progress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = ExecutiveSecondaryAmber
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$${String.format("%.2f", goal.currentAmount)} de $${String.format("%.2f", goal.targetAmount)} • Meta: ${goal.deadline}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = ExecutiveSecondaryAmber,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick = { contributeGoalTarget = goal },
                                modifier = Modifier.align(Alignment.End),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Aportar a meta", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            // Historial de Transacciones con Filtros y Búsqueda
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Historial Reciente",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${filteredTransactions.size} movimientos",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Selector de tipo (Todos / Gastos / Ingresos)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("TODOS" to "Todos", "GASTO" to "Solo Gastos", "INGRESO" to "Solo Ingresos").forEach { (typeKey, label) ->
                            FilterChip(
                                selected = txFilterType == typeKey,
                                onClick = { txFilterType = typeKey },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = when (typeKey) {
                                        "GASTO" -> ExecutiveDangerRed.copy(alpha = 0.2f)
                                        "INGRESO" -> ExecutiveSuccessGreen.copy(alpha = 0.2f)
                                        else -> ExecutiveSecondaryAmber.copy(alpha = 0.2f)
                                    },
                                    selectedLabelColor = when (typeKey) {
                                        "GASTO" -> ExecutiveDangerRed
                                        "INGRESO" -> ExecutiveSuccessGreen
                                        else -> ExecutiveSecondaryAmber
                                    }
                                ),
                                modifier = Modifier.testTag("filter_tx_$typeKey")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Barra de búsqueda rápida
                    OutlinedTextField(
                        value = txSearchQuery,
                        onValueChange = { txSearchQuery = it },
                        placeholder = { Text("Filtrar por categoría o detalle...", fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        trailingIcon = {
                            if (txSearchQuery.isNotBlank()) {
                                IconButton(onClick = { txSearchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Limpiar", modifier = Modifier.size(14.dp))
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("input_search_tx"),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            if (filteredTransactions.isEmpty()) {
                item {
                    Text(
                        text = if (state.transactionsToday.isEmpty()) "Sin movimientos registrados en el ciclo." else "No se encontraron movimientos con los filtros actuales.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(filteredTransactions, key = { it.id }) { tx ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (tx.type == "INGRESO") ExecutiveSuccessGreen.copy(alpha = 0.15f) else ExecutiveDangerRed.copy(alpha = 0.15f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (tx.type == "INGRESO") Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                    contentDescription = tx.type,
                                    tint = if (tx.type == "INGRESO") ExecutiveSuccessGreen else ExecutiveDangerRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = tx.description.ifBlank { tx.category },
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = tx.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${if (tx.type == "INGRESO") "+" else "-"}$${String.format("%.2f", tx.amount)}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (tx.type == "INGRESO") ExecutiveSuccessGreen else ExecutiveDangerRed
                                )
                            )
                            IconButton(onClick = { onDeleteTransaction(tx) }) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }

    // Diálogos
    if (showTxDialog) {
        AddTransactionDialog(
            onDismiss = { showTxDialog = false },
            onConfirm = { type, amount, cat, desc ->
                onAddTransaction(type, amount, cat, desc)
                showTxDialog = false
            }
        )
    }

    if (showGoalDialog) {
        AddFinancialGoalDialog(
            onDismiss = { showGoalDialog = false },
            onConfirm = { name, target, deadline ->
                onAddFinancialGoal(name, target, deadline)
                showGoalDialog = false
            }
        )
    }

    if (contributeGoalTarget != null) {
        ContributeGoalDialog(
            goal = contributeGoalTarget!!,
            onDismiss = { contributeGoalTarget = null },
            onConfirm = { amount ->
                onContributeToGoal(contributeGoalTarget!!, amount)
                contributeGoalTarget = null
            }
        )
    }
}

@Composable
private fun AddTransactionDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double, String, String) -> Unit
) {
    var type by remember { mutableStateOf("GASTO") }
    var amountText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Alimentación") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar Movimiento") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == "GASTO",
                        onClick = { type = "GASTO" },
                        label = { Text("Gasto") }
                    )
                    FilterChip(
                        selected = type == "INGRESO",
                        onClick = { type = "INGRESO" },
                        label = { Text("Ingreso") }
                    )
                }
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Monto ($)") },
                    modifier = Modifier.fillMaxWidth().testTag("dialog_tx_amount")
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Categoría (ej: Salario, Comida, Transporte)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción o notas") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (amt > 0) onConfirm(type, amt, category, description)
                },
                modifier = Modifier.testTag("dialog_tx_confirm")
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
private fun AddFinancialGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var targetText by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("2026-12-31") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Meta Financiera") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Concepto (ej: Fondo de Emergencia)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it },
                    label = { Text("Monto Objetivo ($)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = deadline,
                    onValueChange = { deadline = it },
                    label = { Text("Fecha límite estimada") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val target = targetText.toDoubleOrNull() ?: 0.0
                if (name.isNotBlank() && target > 0) onConfirm(name, target, deadline)
            }) {
                Text("Guardar Meta")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun ContributeGoalDialog(
    goal: FinancialGoalEntity,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amountText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Aportar a: ${goal.name}") },
        text = {
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Monto a aportar ($)") },
                modifier = Modifier.fillMaxWidth().testTag("dialog_contribute_amount")
            )
        },
        confirmButton = {
            Button(onClick = {
                val amt = amountText.toDoubleOrNull() ?: 0.0
                if (amt > 0) onConfirm(amt)
            }) {
                Text("Aportar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
