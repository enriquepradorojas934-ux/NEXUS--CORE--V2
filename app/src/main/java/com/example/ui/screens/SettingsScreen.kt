package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import com.example.R
import com.example.util.SensoryFeedbackHelper
import com.example.ui.PersonalOsUiState
import com.example.ui.components.ExecutiveHeader
import com.example.ui.theme.*
import com.example.BuildConfig
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: PersonalOsUiState,
    onSetCycleStartTime: (String) -> Unit,
    onSetStrictMode: (Boolean) -> Unit,
    onForceResetCycle: () -> Unit,
    onSyncAllWithCloud: () -> Unit = {},
    onUploadToCloud: () -> Unit = {},
    onDownloadFromCloud: () -> Unit = {},
    onSignInWithGoogle: () -> Unit = {},
    onSignInAnonymously: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onOpenOnboarding: () -> Unit = {},
    onSetAccentColor: (String) -> Unit = {},
    onExportBackup: ((String) -> Unit) -> Unit = {},
    onImportBackup: (String) -> Unit = {},
    onSetCurrency: (symbol: String, code: String) -> Unit = { _, _ -> },
    onSetLanguage: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var showCycleTimePicker by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportedJsonText by remember { mutableStateOf("") }
    var showImportDialog by remember { mutableStateOf(false) }
    var importJsonInput by remember { mutableStateOf("") }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    val (versionName, versionCode) = remember {
        try {
            val pInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, android.content.pm.PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            val vName = pInfo.versionName ?: BuildConfig.VERSION_NAME
            val vCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                pInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                pInfo.versionCode
            }
            vName to vCode
        } catch (_: Exception) {
            BuildConfig.VERSION_NAME to BuildConfig.VERSION_CODE
        }
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = ExecutivePrimaryTeal)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.backup_json_title))
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.settings_backup_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = exportedJsonText,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(exportedJsonText))
                        SensoryFeedbackHelper.vibrateSuccess(context)
                        showExportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExecutivePrimaryTeal)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.copy_to_clipboard))
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text(stringResource(R.string.action_close))
                }
            }
        )
    }

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Upload, contentDescription = null, tint = ExecutiveSecondaryAmber)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.restore_backup_title))
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.settings_backup_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = importJsonInput,
                        onValueChange = { importJsonInput = it },
                        placeholder = { Text(stringResource(R.string.paste_json_hint)) },
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importJsonInput.isNotBlank()) {
                            onImportBackup(importJsonInput)
                            SensoryFeedbackHelper.vibrateSuccess(context)
                            showImportDialog = false
                            importJsonInput = ""
                        }
                    },
                    enabled = importJsonInput.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = ExecutiveSecondaryAmber)
                ) {
                    Text(stringResource(R.string.restore_data_btn))
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    if (showPrivacyPolicyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicyDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = ExecutiveSuccessGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.privacy_policy_title))
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.privacy_policy_title),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "1. Offline-first Room SQLite",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "2. Zero data selling / telemetry",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "3. Strict local notifications permission (POST_NOTIFICATIONS)",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "4. Secure Gemini AI queries",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showPrivacyPolicyDialog = false }) {
                    Text(stringResource(R.string.understood))
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ExecutiveHeader(
            title = androidx.compose.ui.res.stringResource(com.example.R.string.settings_title),
            subtitle = androidx.compose.ui.res.stringResource(com.example.R.string.settings_subtitle),
            cycleDate = state.currentCycleDate,
            timeUntilReset = state.timeUntilNextCycle,
            cycleProgress = state.cycleProgress,
            cycleStartTime = state.cycleStartTime
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // MODO LOCAL SOBERANO (ROOM DATABASE) - PEQUEÑO Y DISCRETO
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SoloCardBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SoloCardBorder),
                    modifier = Modifier.fillMaxWidth().testTag("card_local_storage_sovereign")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.Storage,
                                contentDescription = null,
                                tint = SoloTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "ALMACENAMIENTO LOCAL // ROOM",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    color = SoloTextSecondary
                                )
                                Text(
                                    text = "Persistencia SQLite local en dispositivo · Sin telemetría ni nube",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                    color = SoloTextSecondary.copy(alpha = 0.8f)
                                )
                            }
                        }
                        Surface(
                            color = SoloTrack,
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SoloCardBorder)
                        ) {
                            Text(
                                text = "OFFLINE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = SoloTextSecondary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // PARÁMETRO: INICIO DEL CICLO DIARIO (SELECTOR ÚNICO TIPO RELOJ M3)
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SoloCardBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SoloCardBorder),
                    modifier = Modifier.fillMaxWidth().testTag("card_cycle_setting")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PARÁMETRO: INICIO DEL CICLO DIARIO",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.1.sp,
                                    fontSize = 11.sp
                                ),
                                color = SoloNeonCyan
                            )
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                tint = SoloNeonCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        val currentRange = com.example.domain.DailyCycleEngine.getCycleRangeDisplay(state.cycleStartTime)
                        Text(
                            text = "Corte operativo del sistema: $currentRange. Hábitos, tareas y entrenamientos se sincronizan con este horario.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = SoloTextSecondary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Fila de hora actual y botón selector de reloj
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = SoloTrack,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, SoloGold.copy(alpha = 0.7f))
                                ) {
                                    Text(
                                        text = state.cycleStartTime,
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 24.sp
                                        ),
                                        color = SoloGold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "HORA ACTIVA",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SoloTextSecondary
                                    )
                                    Text(
                                        text = "Reinicio diario",
                                        fontSize = 10.sp,
                                        color = SoloTextSecondary
                                    )
                                }
                            }

                            Button(
                                onClick = { showCycleTimePicker = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SoloNeonCyan,
                                    contentColor = SoloBg
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                modifier = Modifier.testTag("btn_select_cycle_time")
                            ) {
                                Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp), tint = SoloBg)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ELEGIR HORA",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = SoloBg
                                )
                            }
                        }
                    }
                }
            }

            // PERSONALIZACIÓN VISUAL EJECUTIVA (TEMA Y ACENTOS)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().testTag("card_theme_accent")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "IDENTIDAD VISUAL Y COLOR EJECUTIVO",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Selecciona tu tono de acento operativo para personalizar la estética del sistema:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        val paletteOptions = listOf(
                            "Teal" to ExecutivePrimaryTeal,
                            "Púrpura" to ExecutivePurple,
                            "Zafiro" to ExecutiveAccentBlue,
                            "Ámbar" to ExecutiveSecondaryAmber,
                            "Esmeralda" to ExecutiveSuccessGreen
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            paletteOptions.forEach { (name, color) ->
                                val isSelected = state.accentColorName == name
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        SensoryFeedbackHelper.vibrateLightTick(context)
                                        onSetAccentColor(name)
                                    },
                                    label = { Text(name, style = MaterialTheme.typography.labelSmall) },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clip(androidx.compose.foundation.shape.CircleShape)
                                                .background(color)
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // REGIONALIZACIÓN: MONEDA E IDIOMA
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().testTag("card_regional_settings")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Language,
                                contentDescription = null,
                                tint = ExecutivePrimaryTeal,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = androidx.compose.ui.res.stringResource(com.example.R.string.settings_regional_title),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = ExecutivePrimaryTeal
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = androidx.compose.ui.res.stringResource(com.example.R.string.settings_regional_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Selector Moneda
                        Surface(
                            onClick = { showCurrencyDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier.fillMaxWidth().testTag("btn_select_currency")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.AttachMoney,
                                    contentDescription = null,
                                    tint = ExecutiveSecondaryAmber,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = androidx.compose.ui.res.stringResource(com.example.R.string.settings_currency_label),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    val currName = availableCurrencies.find { it.code == state.selectedCurrencyCode }?.name
                                        ?: "${state.selectedCurrencyCode} (${state.selectedCurrencySymbol})"
                                    Text(
                                        text = "$currName • ${state.selectedCurrencySymbol} ${state.selectedCurrencyCode}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = "Cambiar moneda",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Selector Idioma
                        Surface(
                            onClick = { showLanguageDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier.fillMaxWidth().testTag("btn_select_language")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Translate,
                                    contentDescription = null,
                                    tint = ExecutiveAccentBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = androidx.compose.ui.res.stringResource(com.example.R.string.settings_language_label),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    val displayLang = if (state.selectedLanguage.contains("sistema", ignoreCase = true) || state.selectedLanguage.contains("system", ignoreCase = true)) {
                                        androidx.compose.ui.res.stringResource(com.example.R.string.settings_language_system_default)
                                    } else {
                                        state.selectedLanguage
                                    }
                                    Text(
                                        text = displayLang,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = "Cambiar idioma",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // COPIA DE SEGURIDAD SOBERANA (OFFLINE-FIRST JSON)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().testTag("card_json_backup")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Archive, contentDescription = null, tint = ExecutiveSuccessGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "COPIA DE SEGURIDAD LOCAL (JSON)",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = ExecutiveSuccessGreen
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Exporta todos tus datos a un archivo de texto JSON independiente para tener control absoluto o transferir a otro dispositivo.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    onExportBackup { json ->
                                        exportedJsonText = json
                                        showExportDialog = true
                                    }
                                },
                                modifier = Modifier.weight(1f).testTag("btn_export_json")
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(R.string.settings_export_json), style = MaterialTheme.typography.labelSmall)
                            }
                            OutlinedButton(
                                onClick = { showImportDialog = true },
                                modifier = Modifier.weight(1f).testTag("btn_import_json")
                            ) {
                                Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(R.string.settings_restore_json), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            // ARQUITECTURA DEL SISTEMA
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SoloCardBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SoloCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "ARQUITECTURA DEL SISTEMA PERSONAL",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.1.sp,
                                fontSize = 11.sp
                            ),
                            color = SoloNeonCyan
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val currentRange = com.example.domain.DailyCycleEngine.getCycleRangeDisplay(state.cycleStartTime)
                        Text(
                            text = "• Persistencia: Room SQLite Offline-first (almacenamiento soberano)\n• Control de Ciclo: DailyCycleEngine ($currentRange)\n• Recordatorios en Segundo Plano: WorkManager Android\n• Interfaz: Jetpack Compose Material Design 3 (Solo Leveling Rango S)",
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                            color = SoloTextSecondary
                        )
                    }
                }
            }

            // PRIVACIDAD Y SEGURIDAD (PLAY STORE COMPLIANCE)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().testTag("card_privacy_policy")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PRIVACIDAD Y TRANSPARENCIA",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = ExecutiveSuccessGreen
                            )
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = ExecutiveSuccessGreen, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tus datos residen únicamente en tu dispositivo con cifrado y almacenamiento local seguro. Cumplimiento de políticas de Google Play.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = { showPrivacyPolicyDialog = true },
                            modifier = Modifier.fillMaxWidth().testTag("btn_view_privacy_policy")
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.view_privacy_statement), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // INFORMACIÓN DE LANZAMIENTO
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.settings_app_name),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.settings_version_info, versionName, versionCode),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.settings_tagline),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = onOpenOnboarding,
                            modifier = Modifier.testTag("btn_reopen_onboarding")
                        ) {
                            Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.settings_view_onboarding), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }

    // DIÁLOGO DE SELECCIÓN DE HORA DE CICLO CON TIME PICKER MATERIAL 3 OSCURO NEÓN
    if (showCycleTimePicker) {
        val (curH, curM) = remember(state.cycleStartTime) {
            val parts = state.cycleStartTime.split(":")
            val h = parts.getOrNull(0)?.toIntOrNull() ?: 5
            val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
            h to m
        }
        val is24Hr = android.text.format.DateFormat.is24HourFormat(context)
        val timePickerState = rememberTimePickerState(
            initialHour = curH,
            initialMinute = curM,
            is24Hour = is24Hr
        )

        AlertDialog(
            onDismissRequest = { showCycleTimePicker = false },
            containerColor = SoloCardBg,
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = SoloNeonCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "INICIO DEL CICLO OPERATIVO",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp
                        ),
                        color = SoloTextPrimary
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Selecciona la hora de corte diario (${if (is24Hr) "formato 24h" else "formato 12h"}).",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoloTextSecondary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    TimePicker(
                        state = timePickerState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor = SoloTrack,
                            selectorColor = SoloNeonCyan,
                            containerColor = SoloCardBg,
                            periodSelectorBorderColor = SoloCardBorder,
                            periodSelectorSelectedContainerColor = SoloNeonCyan.copy(alpha = 0.25f),
                            periodSelectorSelectedContentColor = SoloNeonCyan,
                            periodSelectorUnselectedContainerColor = SoloTrack,
                            periodSelectorUnselectedContentColor = SoloTextSecondary,
                            timeSelectorSelectedContainerColor = SoloNeonCyan.copy(alpha = 0.25f),
                            timeSelectorSelectedContentColor = SoloNeonCyan,
                            timeSelectorUnselectedContainerColor = SoloTrack,
                            timeSelectorUnselectedContentColor = SoloTextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val formatted = "%02d:%02d".format(timePickerState.hour, timePickerState.minute)
                        onSetCycleStartTime(formatted)
                        showCycleTimePicker = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoloNeonCyan)
                ) {
                    Text("APLICAR HORA", color = SoloBg, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCycleTimePicker = false }) {
                    Text("CANCELAR", color = SoloTextSecondary)
                }
            }
        )
    }

    if (showCurrencyDialog) {
        var searchQuery by remember { mutableStateOf("") }
        val filtered = remember(searchQuery) {
            val q = searchQuery.trim().lowercase()
            if (q.isEmpty()) {
                availableCurrencies
            } else {
                availableCurrencies.filter { c ->
                    c.name.lowercase().contains(q) ||
                    c.code.lowercase().contains(q) ||
                    c.country.lowercase().contains(q) ||
                    c.searchTerms.any { it.contains(q) || q.contains(it) }
                }
            }
        }

        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Paid, contentDescription = null, tint = ExecutiveSecondaryAmber)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.settings_select_currency), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(stringResource(R.string.settings_search_currency), fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_clear), modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_search_currency")
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (filtered.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                    Text(stringResource(R.string.settings_no_currencies_found), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        } else {
                            items(filtered.size) { index ->
                                val item = filtered[index]
                                val isSelected = state.selectedCurrencyCode == item.code
                                Surface(
                                    onClick = {
                                        SensoryFeedbackHelper.vibrateLightTick(context)
                                        onSetCurrency(item.symbol, item.code)
                                        showCurrencyDialog = false
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) ExecutiveSecondaryAmber.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                    modifier = Modifier.fillMaxWidth().testTag("currency_option_${item.code}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.name,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                ),
                                                color = if (isSelected) ExecutiveSecondaryAmber else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "${item.country} • ${item.code} (${item.symbol})",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        if (isSelected) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = ExecutiveSecondaryAmber, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCurrencyDialog = false }) {
                    Text(stringResource(R.string.action_close))
                }
            }
        )
    }

    if (showLanguageDialog) {
        var searchQuery by remember { mutableStateOf("") }
        val filtered = remember(searchQuery) {
            val q = searchQuery.trim().lowercase()
            if (q.isEmpty()) {
                availableLanguages
            } else {
                availableLanguages.filter { l ->
                    l.name.lowercase().contains(q) ||
                    l.subtitle.lowercase().contains(q) ||
                    l.searchTerms.any { it.contains(q) || q.contains(it) }
                }
            }
        }

        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Translate, contentDescription = null, tint = ExecutiveAccentBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.settings_select_language), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(stringResource(R.string.settings_search_language), fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_clear), modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_search_language")
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (filtered.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                    Text(stringResource(R.string.settings_no_languages_found), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        } else {
                            items(filtered.size) { index ->
                                val item = filtered[index]
                                val isSelected = state.selectedLanguage == item.name
                                Surface(
                                    onClick = {
                                        SensoryFeedbackHelper.vibrateLightTick(context)
                                        onSetLanguage(item.name)
                                        showLanguageDialog = false
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) ExecutiveAccentBlue.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                    modifier = Modifier.fillMaxWidth().testTag("language_option_${item.name}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.name,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                ),
                                                color = if (isSelected) ExecutiveAccentBlue else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = item.subtitle,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        if (isSelected) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = ExecutiveAccentBlue, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.action_close))
                }
            }
        )
    }
}

private data class CurrencyOption(
    val code: String,
    val symbol: String,
    val name: String,
    val country: String,
    val searchTerms: List<String>
)

private data class LanguageOption(
    val name: String,
    val subtitle: String,
    val searchTerms: List<String>
)

private val availableCurrencies = listOf(
    CurrencyOption("COP", "$", "Peso Colombiano", "Colombia", listOf("colombia", "peso", "pesos", "cop")),
    CurrencyOption("MXN", "$", "Peso Mexicano", "México", listOf("mexico", "méxico", "peso", "pesos", "mxn")),
    CurrencyOption("ARS", "$", "Peso Argentino", "Argentina", listOf("argentina", "peso", "pesos", "ars")),
    CurrencyOption("CLP", "$", "Peso Chileno", "Chile", listOf("chile", "peso", "pesos", "clp")),
    CurrencyOption("UYU", "$", "Peso Uruguayo", "Uruguay", listOf("uruguay", "peso", "pesos", "uyu")),
    CurrencyOption("DOP", "RD$", "Peso Dominicano", "República Dominicana", listOf("dominicana", "republica dominicana", "república dominicana", "peso", "pesos", "dop")),
    CurrencyOption("USD", "$", "Dólar Estadounidense", "Estados Unidos", listOf("estados unidos", "usa", "us", "eeuu", "dolar", "dólar", "dollar", "usd")),
    CurrencyOption("EUR", "€", "Euro", "Unión Europea / España", listOf("europa", "españa", "espana", "francia", "alemania", "italia", "portugal", "euro", "euros", "eur")),
    CurrencyOption("PEN", "S/", "Sol Peruano", "Perú", listOf("peru", "perú", "sol", "soles", "pen")),
    CurrencyOption("BRL", "R$", "Real Brasileño", "Brasil", listOf("brasil", "brazil", "real", "reais", "brl")),
    CurrencyOption("GBP", "£", "Libra Esterlina", "Reino Unido", listOf("reino unido", "uk", "inglaterra", "britanico", "libra", "pound", "gbp")),
    CurrencyOption("CAD", "CA$", "Dólar Canadiense", "Canadá", listOf("canada", "canadá", "dolar", "dólar", "cad")),
    CurrencyOption("AUD", "AU$", "Dólar Australiano", "Australia", listOf("australia", "dolar", "dólar", "aud")),
    CurrencyOption("CHF", "CHF", "Franco Suizo", "Suiza", listOf("suiza", "switzerland", "franco", "chf")),
    CurrencyOption("JPY", "¥", "Yen Japonés", "Japón", listOf("japon", "japón", "japan", "yen", "jpy")),
    CurrencyOption("BOB", "Bs", "Boliviano", "Bolivia", listOf("bolivia", "boliviano", "bob")),
    CurrencyOption("CRC", "₡", "Colón Costarricense", "Costa Rica", listOf("costa rica", "colon", "colón", "crc")),
    CurrencyOption("GTQ", "Q", "Quetzal", "Guatemala", listOf("guatemala", "quetzal", "gtq")),
    CurrencyOption("HNL", "L", "Lempira", "Honduras", listOf("honduras", "lempira", "hnl")),
    CurrencyOption("NIO", "C$", "Córdoba", "Nicaragua", listOf("nicaragua", "cordoba", "córdoba", "nio")),
    CurrencyOption("PYG", "₲", "Guaraní Paraguayo", "Paraguay", listOf("paraguay", "guarani", "guaraní", "pyg")),
    CurrencyOption("VES", "Bs.", "Bolívar Soberano", "Venezuela", listOf("venezuela", "bolivar", "bolívar", "ves"))
)

private val availableLanguages = listOf(
    LanguageOption(
        name = "Idioma del sistema",
        subtitle = "Sigue el idioma del teléfono / Follows phone language",
        searchTerms = listOf("sistema", "system", "default", "predeterminado", "telefono", "phone", "dispositivo")
    ),
    LanguageOption(
        name = "Español (Latinoamérica)",
        subtitle = "Colombia, México, Argentina, Chile, Perú, etc.",
        searchTerms = listOf("español", "espanol", "latinoamerica", "latinoamérica", "latam", "colombia", "mexico", "méxico", "argentina", "chile", "peru", "perú", "venezuela", "ecuador", "bolivia", "uruguay", "paraguay", "costa rica", "panama", "panamá", "guatemala", "republica dominicana", "honduras", "nicaragua", "el salvador")
    ),
    LanguageOption(
        name = "English (United States)",
        subtitle = "United States, North America",
        searchTerms = listOf("english", "ingles", "inglés", "united states", "usa", "us", "estados unidos", "america")
    )
)
