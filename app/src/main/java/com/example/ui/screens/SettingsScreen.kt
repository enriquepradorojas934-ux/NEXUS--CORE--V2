package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import com.example.util.SensoryFeedbackHelper
import com.example.ui.PersonalOsUiState
import com.example.ui.components.ExecutiveHeader
import com.example.ui.theme.*

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
    onImportBackup: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var cycleTimeInput by remember { mutableStateOf(state.cycleStartTime) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportedJsonText by remember { mutableStateOf("") }
    var showImportDialog by remember { mutableStateOf(false) }
    var importJsonInput by remember { mutableStateOf("") }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = ExecutivePrimaryTeal)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Copia de Seguridad (JSON)")
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Tus tareas, hábitos, finanzas y metas exportadas en formato abierto y portable:",
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
                    Text("Copiar al Portapapeles")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Cerrar")
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
                    Text("Restaurar Copia de Seguridad")
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Pega aquí el texto JSON de una copia de seguridad previa para restaurar tus registros:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = importJsonInput,
                        onValueChange = { importJsonInput = it },
                        placeholder = { Text("Pega el JSON aquí...") },
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
                    Text("Restaurar Datos")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancelar")
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
                    Text("Política de Privacidad y Seguridad")
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "En Sistema Personal valoramos tu privacidad absoluta:",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "1. Almacenamiento Local Offline-first: Todas tus tareas, hábitos, finanzas personales y notas se guardan de forma privada en la base de datos local SQLite (Room) de tu dispositivo.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "2. Cero Venta de Datos: No recopilamos, vendemos ni rastreamos tus datos personales con fines publicitarios o de telemetría invasiva.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "3. Permisos Estrictos: El permiso de notificaciones (POST_NOTIFICATIONS) se utiliza únicamente para las alertas locales programadas por ti (WorkManager).",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "4. Inteligencia Artificial Segura: Las solicitudes a Gemini API se procesan únicamente cuando solicitas asesoramiento ejecutivo mediante tu clave autorizada.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showPrivacyPolicyDialog = false }) {
                    Text("Entendido")
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ExecutiveHeader(
            title = "Configuración del Sistema",
            subtitle = "Reglas personales, sincronización en la nube y arquitectura",
            cycleDate = state.currentCycleDate,
            timeUntilReset = state.timeUntilNextCycle,
            cycleProgress = state.cycleProgress
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // AUTENTICACIÓN FIREBASE (GOOGLE SIGN-IN & CUENTA SEGURA)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().testTag("card_firebase_auth")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = ExecutivePrimaryTeal
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "CUENTA Y AUTENTICACIÓN FIREBASE",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = ExecutivePrimaryTeal
                                )
                            }
                            if (state.isAuthLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = ExecutivePrimaryTeal
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val user = state.currentUser
                        if (user != null) {
                            Surface(
                                color = ExecutiveDarkSurfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        if (user.isAnonymous) Icons.Default.Shield else Icons.Default.VerifiedUser,
                                        contentDescription = null,
                                        tint = if (user.isAnonymous) ExecutiveSecondaryAmber else ExecutiveSuccessGreen,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = user.displayName ?: if (user.isAnonymous) "Sesión Anónima Segura" else (user.email ?: "Usuario Autenticado"),
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (user.email != null) user.email else "UID: ${user.uid.take(12)}...",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedButton(
                                onClick = onSignOut,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ExecutiveDangerRed),
                                modifier = Modifier.fillMaxWidth().testTag("btn_auth_sign_out")
                            ) {
                                Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Cerrar Sesión")
                            }
                        } else {
                            Text(
                                text = "Identifícate de forma segura con Google Sign-In para respaldar automáticamente tus tareas, hábitos, finanzas y metas en Firestore asociadas a tu perfil.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            state.authError?.let { err ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = ExecutiveDangerRed.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = err,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ExecutiveDangerRed,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = onSignInWithGoogle,
                                enabled = !state.isAuthLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = ExecutivePrimaryTeal),
                                modifier = Modifier.fillMaxWidth().testTag("btn_auth_google_signin")
                            ) {
                                Icon(
                                    Icons.Default.Login,
                                    contentDescription = null,
                                    tint = ExecutiveDarkBg,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Continuar con Google",
                                    color = ExecutiveDarkBg,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = onSignInAnonymously,
                                enabled = !state.isAuthLoading,
                                modifier = Modifier.fillMaxWidth().testTag("btn_auth_anonymous_signin")
                            ) {
                                Icon(
                                    Icons.Default.Key,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Iniciar Sesión Anónima (Nube)",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }

            // SINCRONIZACIÓN EN LA NUBE (CLOUD FIRESTORE)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().testTag("card_cloud_sync")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CloudSync, contentDescription = null, tint = ExecutivePrimaryTeal)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "SINCRONIZACIÓN CLOUD FIRESTORE",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = ExecutivePrimaryTeal
                                )
                            }
                            if (state.isSyncingCloud) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = ExecutivePrimaryTeal
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Respalda y sincroniza tus tareas, bloques de tiempo, hábitos, finanzas y límites de atención en Google Cloud Firestore.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        state.cloudSyncMessage?.let { msg ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = if (state.cloudSyncSuccess) ExecutivePrimaryTeal.copy(alpha = 0.12f) else ExecutiveDangerRed.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (state.cloudSyncSuccess) ExecutivePrimaryTeal else ExecutiveDangerRed,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = onSyncAllWithCloud,
                            enabled = !state.isSyncingCloud,
                            colors = ButtonDefaults.buttonColors(containerColor = ExecutivePrimaryTeal),
                            modifier = Modifier.fillMaxWidth().testTag("btn_sync_cloud_all")
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, tint = ExecutiveDarkBg, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sincronizar con la Nube", color = ExecutiveDarkBg, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onUploadToCloud,
                                enabled = !state.isSyncingCloud,
                                modifier = Modifier.weight(1f).testTag("btn_upload_cloud")
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Subir Respaldo", style = MaterialTheme.typography.labelSmall)
                            }

                            OutlinedButton(
                                onClick = onDownloadFromCloud,
                                enabled = !state.isSyncingCloud,
                                modifier = Modifier.weight(1f).testTag("btn_download_cloud")
                            ) {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Restaurar Nube", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
            // PARAMETRO INICIO DEL CICLO DIARIO
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().testTag("card_cycle_setting")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "PARÁMETRO: INICIO DEL CICLO DIARIO",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = ExecutivePrimaryTeal
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Configuración del corte diario del sistema. Por defecto: 05:00 AM.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = cycleTimeInput,
                                onValueChange = { cycleTimeInput = it },
                                label = { Text("Hora (HH:mm)") },
                                modifier = Modifier.weight(1f).testTag("input_cycle_time")
                            )

                            Button(
                                onClick = { onSetCycleStartTime(cycleTimeInput) },
                                colors = ButtonDefaults.buttonColors(containerColor = ExecutivePrimaryTeal),
                                modifier = Modifier.testTag("btn_save_cycle_time")
                            ) {
                                Text("Guardar")
                            }
                        }
                    }
                }
            }

            // MODO ESTRICTO Y REGLAS PERSONALES
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().testTag("card_strict_mode_settings")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "MODO ESTRICTO (RIGOR DE ATENCIÓN)",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (state.isStrictModeEnabled) ExecutiveDangerRed else ExecutivePrimaryTeal
                                )
                                Text(
                                    text = "Impide modificar o eliminar límites de apps durante el ciclo activo.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = state.isStrictModeEnabled,
                                onCheckedChange = { onSetStrictMode(it) }
                            )
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
                                Text("Exportar JSON", style = MaterialTheme.typography.labelSmall)
                            }
                            OutlinedButton(
                                onClick = { showImportDialog = true },
                                modifier = Modifier.weight(1f).testTag("btn_import_json")
                            ) {
                                Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Restaurar JSON", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            // RESPUESTA SENSORIAL Y HÁPTICA
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Vibration, contentDescription = null, tint = ExecutiveSecondaryAmber, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "EXPERIENCIA SENSORIAL Y HÁPTICA",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = ExecutiveSecondaryAmber
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Micro-vibraciones táctiles al completar tareas o hábitos, y aviso sonoro armónico al concluir tus bloques de enfoque.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { SensoryFeedbackHelper.playFocusCompleteCelebration(context) },
                            modifier = Modifier.fillMaxWidth().testTag("btn_test_haptics")
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Probar Alerta Táctil y Sonora", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // SIMULACIÓN Y PRUEBAS
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "MANTENIMIENTO DEL SISTEMA",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = ExecutiveSecondaryAmber
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Puedes forzar manualmente el reinicio del ciclo para probar la restauración de contadores a 0 minutos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = onForceResetCycle,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Forzar Reinicio de Ciclo Diario")
                        }
                    }
                }
            }

            // ARQUITECTURA DEL SISTEMA
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "ARQUITECTURA DEL SISTEMA PERSONAL",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = ExecutiveAccentBlue
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• Persistencia: Room SQLite Offline-first\n• Control de Ciclo: DailyCycleEngine (05:00 a 04:59)\n• Recordatorios en Segundo Plano: WorkManager\n• Interfaz: Jetpack Compose Material Design 3",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            Text("Consultar Declaración de Privacidad", style = MaterialTheme.typography.labelSmall)
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
                            text = "Sistema Personal",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Versión 1.0.0 (Build 1) • Producción",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Diseñado para consistencia y enfoque ejecutivo",
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
                            Text("Ver Bienvenida y Tutorial", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}
