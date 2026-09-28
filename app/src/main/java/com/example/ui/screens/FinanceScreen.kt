package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssetDebtEntity
import com.example.data.model.FinancialGoalEntity
import com.example.data.model.TransactionEntity
import com.example.ui.PersonalOsUiState
import com.example.ui.components.ExecutiveHeader
import com.example.ui.theme.*
import java.util.Calendar
import kotlin.math.atan2

// Paleta ejecutiva vibrante para la distribución de activos en la dona
private val AssetPalette = listOf(
    Color(0xFF8B5CF6), // Violeta / Púrpura ejecutivo
    Color(0xFF06B6D4), // Cian luminoso
    Color(0xFFF59E0B), // Ámbar dorado
    Color(0xFF10B981), // Esmeralda táctico
    Color(0xFFEC4899), // Magenta vivo
    Color(0xFF3B82F6), // Azul eléctrico
    Color(0xFFF97316), // Naranja coral
    Color(0xFF14B8A6), // Verde azulado
    Color(0xFFA855F7), // Púrpura brillante
    Color(0xFFEAB308)  // Oro
)

enum class FinanceSectionTab {
    PATRIMONIO,
    GASTOS
}

@Composable
fun FinanceScreen(
    state: PersonalOsUiState,
    onAddTransaction: (String, Double, String, String) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    onAddFinancialGoal: (String, Double, String) -> Unit = { _, _, _ -> },
    onContributeToGoal: (FinancialGoalEntity, Double) -> Unit = { _, _ -> },
    onAddAssetDebt: (String, String, Double, String) -> Unit,
    onUpdateAssetDebt: (AssetDebtEntity) -> Unit = {},
    onDeleteAssetDebt: (AssetDebtEntity) -> Unit = {},
    onAddTransactionNatural: (String) -> Unit = {},
    onAnalyzeFinanceAi: () -> Unit = {},
    onEvaluateExpenseBeforeSaving: (String, String, Double, String, String, (String) -> Unit) -> Unit = { _, _, _, _, _, _ -> },
    onClearExpenseEvaluation: () -> Unit = {},
    onAskFinanceAdvisor: (String) -> Unit = {},
    onClearFinanceAdvisor: () -> Unit = {}
) {
    var activeTab by remember { mutableStateOf(FinanceSectionTab.PATRIMONIO) }

    // Diálogos de Patrimonio
    var showAddAssetDialog by remember { mutableStateOf(false) }
    var assetToEdit by remember { mutableStateOf<AssetDebtEntity?>(null) }
    var assetToDelete by remember { mutableStateOf<AssetDebtEntity?>(null) }

    // Diálogo de Registro de Gasto con IA
    var showRegisterExpenseDialog by remember { mutableStateOf(false) }

    // Diálogo de Pregunta Libre al Asesor
    var showAskAdvisorDialog by remember { mutableStateOf(false) }

    // Filtros de movimientos
    var naturalTxText by remember { mutableStateOf("") }
    var txFilterType by remember { mutableStateOf("TODOS") }
    var txSearchQuery by remember { mutableStateOf("") }

    val activeAssets = remember(state.assetsAndDebts) {
        state.assetsAndDebts.filter { it.type == "ACTIVO" }
    }
    val totalPatrimonio = remember(activeAssets) {
        activeAssets.sumOf { it.currentAmount }
    }

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
            title = "Módulo de Finanzas",
            subtitle = "Patrimonio, distribución de activos y control de gastos",
            cycleDate = state.currentCycleDate,
            timeUntilReset = state.timeUntilNextCycle,
            cycleProgress = state.cycleProgress,
            actionIcon = if (activeTab == FinanceSectionTab.PATRIMONIO) Icons.Default.AddBusiness else Icons.Default.AddShoppingCart,
            onActionClick = {
                if (activeTab == FinanceSectionTab.PATRIMONIO) {
                    showAddAssetDialog = true
                } else {
                    showRegisterExpenseDialog = true
                }
            }
        )

        // Cabecera de pestañas con Chip pequeño de Moneda activa
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Selector Segmentado de Secciones (Patrimonio vs Gastos)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FinanceTabButton(
                    selected = activeTab == FinanceSectionTab.PATRIMONIO,
                    title = "Patrimonio",
                    icon = Icons.Default.PieChart,
                    modifier = Modifier.weight(1f).testTag("tab_patrimonio"),
                    onClick = { activeTab = FinanceSectionTab.PATRIMONIO }
                )
                FinanceTabButton(
                    selected = activeTab == FinanceSectionTab.GASTOS,
                    title = "Gastos",
                    icon = Icons.Default.ReceiptLong,
                    modifier = Modifier.weight(1f).testTag("tab_gastos"),
                    onClick = { activeTab = FinanceSectionTab.GASTOS }
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Chip pequeño en la cabecera que muestra la moneda activa (ej: "COP")
            Surface(
                color = ExecutivePrimaryTeal.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, ExecutivePrimaryTeal.copy(alpha = 0.35f)),
                modifier = Modifier.testTag("chip_active_currency")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(
                        Icons.Default.MonetizationOn,
                        contentDescription = null,
                        tint = ExecutivePrimaryTeal,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = state.selectedCurrencyCode,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = ExecutivePrimaryTeal
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            if (activeTab == FinanceSectionTab.PATRIMONIO) {
                // ==================== SECCIÓN 1: PATRIMONIO ====================
                item {
                    PatrimonioDonutCard(
                        assets = activeAssets,
                        totalPatrimonio = totalPatrimonio,
                        onAddAssetClick = { showAddAssetDialog = true },
                        currencySymbol = state.selectedCurrencySymbol,
                        currencyCode = state.selectedCurrencyCode
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Composición de Activos (${activeAssets.size})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Button(
                            onClick = { showAddAssetDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = ExecutivePrimaryTeal),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("btn_add_asset")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Agregar Activo", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (activeAssets.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Aún no tienes activos registrados",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Agrega tus cuentas, bienes raíces, inversiones u oro para visualizar la distribución de tu patrimonio.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(activeAssets, key = { it.id }) { asset ->
                        val index = activeAssets.indexOf(asset)
                        val color = AssetPalette[index % AssetPalette.size]
                        val percentage = if (totalPatrimonio > 0) (asset.currentAmount / totalPatrimonio * 100) else 0.0

                        AssetItemCard(
                            asset = asset,
                            color = color,
                            percentage = percentage,
                            onEdit = { assetToEdit = asset },
                            onDelete = { assetToDelete = asset },
                            currencySymbol = state.selectedCurrencySymbol,
                            currencyCode = state.selectedCurrencyCode
                        )
                    }
                }
            } else {
                // ==================== SECCIÓN 2: GASTOS ====================
                // Resumen rápido de gastos y flujo
                item {
                    GastosSummaryCard(
                        totalExpenses = state.totalExpenses,
                        totalIncome = state.totalIncome,
                        netBalance = state.netBalance,
                        onRegisterExpenseClick = { showRegisterExpenseDialog = true },
                        currencySymbol = state.selectedCurrencySymbol,
                        currencyCode = state.selectedCurrencyCode
                    )
                }

                // FUNCIÓN CONSERVADA: Registro Rápido Inteligente
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

                // FUNCIÓN CONSERVADA & MEJORADA: Asesor Financiero IA (Analizar Flujo + Consultas Libres)
                item {
                    AsesorFinancieroIaCard(
                        state = state,
                        onAnalyzeFlow = onAnalyzeFinanceAi,
                        onOpenAskAdvisor = { showAskAdvisorDialog = true },
                        onClearCustomAdvice = onClearFinanceAdvisor
                    )
                }

                // Historial de Movimientos con Filtros y Búsqueda
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Historial de Movimientos",
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

                        OutlinedTextField(
                            value = txSearchQuery,
                            onValueChange = { txSearchQuery = it },
                            placeholder = { Text("Buscar por nombre, categoría o nota...", fontSize = 11.sp) },
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
                            text = if (state.transactionsToday.isEmpty()) "Sin gastos ni ingresos registrados en el ciclo." else "No se encontraron movimientos con los filtros aplicados.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(filteredTransactions, key = { it.id }) { tx ->
                        TransactionRowItem(
                            tx = tx,
                            onDelete = { onDeleteTransaction(tx) },
                            currencySymbol = state.selectedCurrencySymbol,
                            currencyCode = state.selectedCurrencyCode
                        )
                    }
                }
            }
        }
    }

    // ==================== DIÁLOGOS DE PATRIMONIO ====================
    if (showAddAssetDialog) {
        AddEditAssetDialog(
            title = "Nuevo Activo",
            existingAsset = null,
            currencySymbol = state.selectedCurrencySymbol,
            currencyCode = state.selectedCurrencyCode,
            onDismiss = { showAddAssetDialog = false },
            onConfirm = { name, category, amount ->
                onAddAssetDebt("ACTIVO", name, amount, category)
                showAddAssetDialog = false
            }
        )
    }

    if (assetToEdit != null) {
        AddEditAssetDialog(
            title = "Editar Activo",
            existingAsset = assetToEdit,
            currencySymbol = state.selectedCurrencySymbol,
            currencyCode = state.selectedCurrencyCode,
            onDismiss = { assetToEdit = null },
            onConfirm = { name, category, amount ->
                onUpdateAssetDebt(assetToEdit!!.copy(name = name, notes = category, currentAmount = amount))
                assetToEdit = null
            }
        )
    }

    if (assetToDelete != null) {
        AlertDialog(
            onDismissRequest = { assetToDelete = null },
            title = { Text("Eliminar Activo") },
            text = { Text("¿Deseas eliminar el activo '${assetToDelete!!.name}' de tu patrimonio? Esta acción recalculará los porcentajes de la dona.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteAssetDebt(assetToDelete!!)
                        assetToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExecutiveDangerRed)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { assetToDelete = null }) { Text("Cancelar") }
            }
        )
    }

    // ==================== DIÁLOGO DE REGISTRO DE GASTO CON ASESOR IA ====================
    if (showRegisterExpenseDialog) {
        RegisterExpenseWithAiDialog(
            totalPatrimonio = totalPatrimonio,
            currentCycleDate = state.currentCycleDate,
            isEvaluatingExpense = state.isEvaluatingExpense,
            expenseEvaluationAdvice = state.expenseEvaluationAdvice,
            currencySymbol = state.selectedCurrencySymbol,
            currencyCode = state.selectedCurrencyCode,
            onDismiss = {
                onClearExpenseEvaluation()
                showRegisterExpenseDialog = false
            },
            onEvaluateWithAi = { name, cat, amt, date, desc, onDone ->
                onEvaluateExpenseBeforeSaving(name, cat, amt, date, desc, onDone)
            },
            onConfirmExpense = { name, cat, amt, date, desc ->
                val fullDescription = if (desc.isNotBlank()) "$name: $desc" else name
                onAddTransaction("GASTO", amt, cat, fullDescription)
                onClearExpenseEvaluation()
                showRegisterExpenseDialog = false
            }
        )
    }

    if (showAskAdvisorDialog) {
        AskFinancialAdvisorDialog(
            isAsking = state.isAskingFinanceAdvisor,
            advice = state.customFinanceAdvice,
            onDismiss = { showAskAdvisorDialog = false },
            onAsk = { question -> onAskFinanceAdvisor(question) }
        )
    }
}

// =========================================================================
// COMPONENTES DE PATRIMONIO (GRÁFICO TIPO DONA INTERACTIVO)
// =========================================================================

@Composable
private fun PatrimonioDonutCard(
    assets: List<AssetDebtEntity>,
    totalPatrimonio: Double,
    onAddAssetClick: () -> Unit,
    currencySymbol: String = "$",
    currencyCode: String = "USD"
) {
    var selectedAssetId by remember { mutableStateOf<Long?>(null) }
    val selectedAsset = remember(selectedAssetId, assets) {
        assets.find { it.id == selectedAssetId }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().testTag("card_patrimonio_donut")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.DonutLarge,
                        contentDescription = null,
                        tint = ExecutivePrimaryTeal,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DISTRIBUCIÓN PATRIMONIAL",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = ExecutivePrimaryTeal
                    )
                }

                if (selectedAssetId != null) {
                    TextButton(
                        onClick = { selectedAssetId = null },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Ver total", fontSize = 11.sp, color = ExecutivePrimaryTeal)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lienzo interactivo del gráfico tipo Dona
            Box(
                modifier = Modifier
                    .size(230.dp)
                    .testTag("donut_chart_canvas"),
                contentAlignment = Alignment.Center
            ) {
                if (assets.isEmpty() || totalPatrimonio <= 0.0) {
                    // Estado vacío de la dona
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Color.DarkGray.copy(alpha = 0.35f),
                            style = Stroke(width = 30.dp.toPx())
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("PATRIMONIO", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(com.example.util.CurrencyFormatter.format(0.0, currencySymbol, currencyCode), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                        Text("Sin activos", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    // Segmentos de la dona calculados
                    val slices = remember(assets, totalPatrimonio) {
                        var cumulativeAngle = -90f
                        assets.mapIndexed { index, asset ->
                            val sweep = ((asset.currentAmount / totalPatrimonio) * 360f).toFloat()
                            val slice = DonutSlice(
                                asset = asset,
                                color = AssetPalette[index % AssetPalette.size],
                                startAngle = cumulativeAngle,
                                sweepAngle = sweep
                            )
                            cumulativeAngle += sweep
                            slice
                        }
                    }

                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(slices) {
                                detectTapGestures { offset ->
                                    val centerX = size.width / 2f
                                    val centerY = size.height / 2f
                                    val dx = offset.x - centerX
                                    val dy = offset.y - centerY
                                    // Ángulo normalizado de 0 a 360 partiendo de las 12 horas (-90°)
                                    val touchAngle = (Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat() + 360f + 90f) % 360f

                                    val clickedSlice = slices.find { s ->
                                        val startNorm = (s.startAngle + 360f + 90f) % 360f
                                        val endNorm = startNorm + s.sweepAngle
                                        if (endNorm <= 360f) {
                                            touchAngle in startNorm..endNorm
                                        } else {
                                            touchAngle >= startNorm || touchAngle <= (endNorm % 360f)
                                        }
                                    }

                                    selectedAssetId = if (clickedSlice != null && selectedAssetId != clickedSlice.asset.id) {
                                        clickedSlice.asset.id
                                    } else {
                                        null
                                    }
                                }
                            }
                    ) {
                        val strokeWidth = 32.dp.toPx()
                        val padding = strokeWidth / 2f + 4.dp.toPx()
                        val arcSize = Size(size.width - padding * 2, size.height - padding * 2)
                        val arcTopLeft = Offset(padding, padding)

                        slices.forEach { slice ->
                            val isSelected = slice.asset.id == selectedAssetId
                            val currentStroke = if (isSelected) strokeWidth + 10.dp.toPx() else strokeWidth

                            drawArc(
                                color = if (selectedAssetId == null || isSelected) slice.color else slice.color.copy(alpha = 0.35f),
                                startAngle = slice.startAngle,
                                sweepAngle = (slice.sweepAngle - 1.5f).coerceAtLeast(0.5f),
                                useCenter = false,
                                topLeft = arcTopLeft,
                                size = arcSize,
                                style = Stroke(width = currentStroke, cap = StrokeCap.Round)
                            )
                        }
                    }

                    // Información central de la dona (Total o detalle interactivo)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        if (selectedAsset != null) {
                            val selPercentage = (selectedAsset.currentAmount / totalPatrimonio * 100)
                            Text(
                                text = selectedAsset.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = com.example.util.CurrencyFormatter.format(selectedAsset.currentAmount, currencySymbol, currencyCode),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = AssetPalette[assets.indexOf(selectedAsset) % AssetPalette.size]
                            )
                            Text(
                                text = "${String.format("%.1f", selPercentage)}% del patrimonio",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = ExecutiveSecondaryAmber
                            )
                        } else {
                            Text(
                                text = "PATRIMONIO TOTAL",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = com.example.util.CurrencyFormatter.format(totalPatrimonio, currencySymbol, currencyCode),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = ExecutivePrimaryTeal
                            )
                            Text(
                                text = "${assets.size} activos registrados",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Toca cualquier segmento de la rueda para inspeccionar su peso relativo.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private data class DonutSlice(
    val asset: AssetDebtEntity,
    val color: Color,
    val startAngle: Float,
    val sweepAngle: Float
)

@Composable
private fun AssetItemCard(
    asset: AssetDebtEntity,
    color: Color,
    percentage: Double,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    currencySymbol: String = "$",
    currencyCode: String = "USD"
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().testTag("asset_item_${asset.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = asset.name,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${String.format("%.1f", percentage)}% de tu cartera",
                        style = MaterialTheme.typography.labelSmall,
                        color = color
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = com.example.util.CurrencyFormatter.format(asset.currentAmount, currencySymbol, currencyCode),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar", modifier = Modifier.size(16.dp), tint = ExecutiveDangerRed)
                }
            }
        }
    }
}

// =========================================================================
// COMPONENTES DE GASTOS & ASESOR FINANCIERO
// =========================================================================

@Composable
private fun GastosSummaryCard(
    totalExpenses: Double,
    totalIncome: Double,
    netBalance: Double,
    onRegisterExpenseClick: () -> Unit,
    currencySymbol: String = "$",
    currencyCode: String = "USD"
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().testTag("card_gastos_summary")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "GASTOS DEL CICLO",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = ExecutiveDangerRed
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = com.example.util.CurrencyFormatter.format(totalExpenses, currencySymbol, currencyCode),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = ExecutiveDangerRed
                    )
                }

                Button(
                    onClick = onRegisterExpenseClick,
                    colors = ButtonDefaults.buttonColors(containerColor = ExecutiveDangerRed),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("btn_open_register_expense")
                ) {
                    Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Registrar Gasto", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = ExecutiveBorderSubtle)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Ingresos Registrados", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = com.example.util.CurrencyFormatter.format(totalIncome, currencySymbol, currencyCode, includePlus = true),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = ExecutiveSuccessGreen
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Flujo Neto Actual", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = com.example.util.CurrencyFormatter.format(netBalance, currencySymbol, currencyCode, includePlus = true),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (netBalance >= 0) ExecutiveSuccessGreen else ExecutiveDangerRed
                    )
                }
            }
        }
    }
}

@Composable
private fun AsesorFinancieroIaCard(
    state: PersonalOsUiState,
    onAnalyzeFlow: () -> Unit,
    onOpenAskAdvisor: () -> Unit,
    onClearCustomAdvice: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().testTag("card_ai_finance_advisor")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = ExecutivePrimaryTeal,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ASESOR FINANCIERO IA",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = ExecutivePrimaryTeal
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = ExecutivePrimaryTeal.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Gemini 3.5 Flash",
                        fontSize = 10.sp,
                        color = ExecutivePrimaryTeal,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Área de Diagnóstico o Consulta
            if (state.isAnalyzingFinance) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth().padding(14.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = ExecutivePrimaryTeal, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Asesor Gemini analizando flujo de caja y patrimonio...", style = MaterialTheme.typography.bodySmall)
                }
            } else if (!state.aiFinancialAdvice.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ExecutiveDarkSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ExecutivePrimaryTeal.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = state.aiFinancialAdvice,
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 19.sp),
                        color = ExecutiveTextWhite,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            } else {
                Text(
                    text = "Obtén un diagnóstico de tu tasa de ahorro, o consulta dudas de inversión y protección de patrimonio con Gemini.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Consulta libre personalizada (si fue realizada)
            if (!state.customFinanceAdvice.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ExecutiveDarkSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ExecutiveSecondaryAmber.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Respuesta del Asesor:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = ExecutiveSecondaryAmber)
                            IconButton(onClick = onClearCustomAdvice, modifier = Modifier.size(20.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Cerrar", modifier = Modifier.size(14.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = state.customFinanceAdvice,
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                            color = ExecutiveTextWhite
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botones de acción del asesor
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onAnalyzeFlow,
                    enabled = !state.isAnalyzingFinance,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ExecutivePrimaryTeal),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ExecutivePrimaryTeal.copy(alpha = 0.4f)),
                    modifier = Modifier.weight(1f).testTag("btn_analyze_finance_ai")
                ) {
                    Icon(Icons.Default.Insights, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Analizar Flujo", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }

                Button(
                    onClick = onOpenAskAdvisor,
                    colors = ButtonDefaults.buttonColors(containerColor = ExecutivePrimaryTeal.copy(alpha = 0.2f), contentColor = ExecutivePrimaryTeal),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ExecutivePrimaryTeal.copy(alpha = 0.5f)),
                    modifier = Modifier.weight(1f).testTag("btn_ask_finance_advisor")
                ) {
                    Icon(Icons.Default.QuestionAnswer, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Preguntar a IA", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

@Composable
private fun TransactionRowItem(
    tx: TransactionEntity,
    onDelete: () -> Unit,
    currencySymbol: String = "$",
    currencyCode: String = "USD"
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.description.ifBlank { tx.category },
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${tx.category} • ${tx.cycleDate}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = com.example.util.CurrencyFormatter.format(tx.amount, currencySymbol, currencyCode, includePlus = (tx.type == "INGRESO")),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (tx.type == "INGRESO") ExecutiveSuccessGreen else ExecutiveDangerRed
                )
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// =========================================================================
// DIÁLOGO CON FLUJO DE CONSULTA DE IA ANTES DE GUARDAR EL GASTO
// =========================================================================

@Composable
private fun RegisterExpenseWithAiDialog(
    totalPatrimonio: Double,
    currentCycleDate: String,
    isEvaluatingExpense: Boolean,
    expenseEvaluationAdvice: String?,
    onDismiss: () -> Unit,
    onEvaluateWithAi: (String, String, Double, String, String, (String) -> Unit) -> Unit,
    onConfirmExpense: (String, String, Double, String, String) -> Unit,
    currencySymbol: String = "$",
    currencyCode: String = "USD"
) {
    val context = LocalContext.current

    // Los 5 campos exactos solicitados
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Transporte") }
    var selectedDate by remember { mutableStateOf(currentCycleDate.ifBlank { "2026-09-25" }) }
    var amountText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var hasRequestedAiEvaluation by remember { mutableStateOf(false) }

    // DatePicker selector de día / mes / año
    val datePickerDialog = remember {
        val parts = selectedDate.split("-")
        val y = parts.getOrNull(0)?.toIntOrNull() ?: 2026
        val m = (parts.getOrNull(1)?.toIntOrNull() ?: 9) - 1
        val d = parts.getOrNull(2)?.toIntOrNull() ?: 25
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                selectedDate = "%04d-%02d-%02d".format(year, month + 1, dayOfMonth)
            },
            y, m, d
        )
    }

    val parsedAmount = amountText.toDoubleOrNull() ?: 0.0
    val isFormValid = name.isNotBlank() && parsedAmount > 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = ExecutiveDangerRed, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Registrar Gasto", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Campo 1: Nombre
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre del gasto") },
                        placeholder = { Text("Nombre del gasto") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_expense_name")
                    )
                }

                // Campo 2: Categoría (Texto libre)
                item {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Categoría") },
                        placeholder = { Text("Categoría") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_expense_category")
                    )
                }

                // Campo 3: Fecha (selector día/mes/año)
                item {
                    OutlinedTextField(
                        value = selectedDate,
                        onValueChange = { selectedDate = it },
                        label = { Text("Fecha") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { datePickerDialog.show() }) {
                                Icon(Icons.Default.CalendarToday, contentDescription = "Seleccionar fecha", tint = ExecutivePrimaryTeal)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() }.testTag("input_expense_date")
                    )
                }

                // Campo 4: Monto
                item {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Monto ($currencyCode)") },
                        placeholder = { Text("0.00") },
                        suffix = { Text(currencyCode, color = ExecutivePrimaryTeal, fontWeight = FontWeight.Bold) },
                        supportingText = { Text("Monto expresado en $currencyCode ($currencySymbol)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_expense_amount")
                    )
                }

                // Campo 5: Descripción libre (motivo real)
                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descripción") },
                        placeholder = { Text("Detalle o motivo del gasto") },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth().testTag("input_expense_description")
                    )
                }

                // PASO CLAVE: Evaluación del Asesor Financiero Gemini
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ExecutiveDarkSurfaceElevated),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ExecutivePrimaryTeal.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ExecutivePrimaryTeal, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "CONSULTA CON ASESOR IA",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = ExecutivePrimaryTeal
                                    )
                                }

                                if (isEvaluatingExpense) {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = ExecutivePrimaryTeal)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            if (expenseEvaluationAdvice != null) {
                                Text(
                                    text = expenseEvaluationAdvice,
                                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 17.sp),
                                    color = ExecutiveTextWhite
                                )
                            } else {
                                Text(
                                    text = "Antes de confirmar, el Asesor Financiero evaluará cómo impacta este desembolso en tus ${com.example.util.CurrencyFormatter.format(totalPatrimonio, currencySymbol, currencyCode)} de patrimonio y tus hábitos de gasto.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = {
                                        hasRequestedAiEvaluation = true
                                        onEvaluateWithAi(name, category, parsedAmount, selectedDate, description) {}
                                    },
                                    enabled = isFormValid && !isEvaluatingExpense,
                                    colors = ButtonDefaults.buttonColors(containerColor = ExecutivePrimaryTeal.copy(alpha = 0.25f), contentColor = ExecutivePrimaryTeal),
                                    modifier = Modifier.fillMaxWidth().testTag("btn_consult_expense_ai")
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Consultar con Asesor Financiero", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                    onConfirmExpense(name, category, parsedAmount, selectedDate, description)
                },
                enabled = isFormValid,
                colors = ButtonDefaults.buttonColors(containerColor = ExecutiveSuccessGreen),
                modifier = Modifier.testTag("btn_confirm_register_expense")
            ) {
                Text("Confirmar Gasto", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (expenseEvaluationAdvice != null) "Modificar / Cancelar" else "Cancelar")
            }
        }
    )
}

// =========================================================================
// DIÁLOGO PARA AGREGAR / EDITAR ACTIVOS MANUALMENTE
// =========================================================================

@Composable
private fun AddEditAssetDialog(
    title: String,
    existingAsset: AssetDebtEntity?,
    currencySymbol: String = "$",
    currencyCode: String = "USD",
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double) -> Unit
) {
    var name by remember { mutableStateOf(existingAsset?.name ?: "") }
    var category by remember { mutableStateOf(existingAsset?.notes ?: "") }
    var amountText by remember { mutableStateOf(existingAsset?.currentAmount?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del activo") },
                    placeholder = { Text("Nombre del activo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_asset_name")
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Categoría del activo") },
                    placeholder = { Text("Ej: Bienes raíces, Cripto, Acciones, etc.") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_asset_category")
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Monto ($currencyCode)") },
                    placeholder = { Text("0.00") },
                    suffix = { Text(currencyCode, color = ExecutivePrimaryTeal, fontWeight = FontWeight.Bold) },
                    supportingText = { Text("Valor expresado en $currencyCode ($currencySymbol)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_asset_value")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && amount >= 0.0) {
                        onConfirm(name.trim(), category.trim(), amount)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ExecutivePrimaryTeal),
                modifier = Modifier.testTag("btn_save_asset")
            ) {
                Text("Guardar Activo", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

// =========================================================================
// DIÁLOGO PARA PREGUNTAS LIBRES AL ASESOR FINANCIERO
// =========================================================================

@Composable
private fun AskFinancialAdvisorDialog(
    isAsking: Boolean,
    advice: String?,
    onDismiss: () -> Unit,
    onAsk: (String) -> Unit
) {
    var questionText by remember { mutableStateOf("") }
    val promptExamples = listOf(
        "¿Cómo comprar una casa con $30,000 eficientemente?",
        "¿Qué estructuras de protección patrimonial recomiendas?",
        "¿Cuál debería ser mi fondo de emergencia?"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.QuestionAnswer, contentDescription = null, tint = ExecutivePrimaryTeal)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Consulta con Asesor Financiero")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Escribe cualquier pregunta sobre estrategia patrimonial, decisiones de compra o inversión:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = questionText,
                    onValueChange = { questionText = it },
                    placeholder = { Text("Escribe tu consulta aquí...") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth().testTag("input_ask_advisor_question")
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    promptExamples.forEach { example ->
                        SuggestionChip(
                            onClick = { questionText = example },
                            label = { Text(example, fontSize = 10.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (questionText.isNotBlank()) {
                        onAsk(questionText)
                        onDismiss()
                    }
                },
                enabled = questionText.isNotBlank() && !isAsking,
                colors = ButtonDefaults.buttonColors(containerColor = ExecutivePrimaryTeal)
            ) {
                Text("Enviar Consulta", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}

// =========================================================================
// DIÁLOGOS DE METAS FINANCIERAS
// =========================================================================

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
        title = { Text("Nueva Meta de Ahorro") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Concepto") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it },
                    label = { Text("Monto objetivo") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = deadline,
                    onValueChange = { deadline = it },
                    label = { Text("Fecha") },
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
                label = { Text("Monto a aportar") },
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

@Composable
private fun FinanceTabButton(
    selected: Boolean,
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) ExecutiveDarkSurfaceElevated else Color.Transparent,
        animationSpec = tween(200)
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) ExecutivePrimaryTeal else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(200)
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium),
                color = contentColor
            )
        }
    }
}
