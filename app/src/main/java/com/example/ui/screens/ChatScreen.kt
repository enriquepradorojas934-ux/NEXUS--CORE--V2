package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.PersonalOsUiState
import com.example.ui.theme.*
import com.example.util.SensoryFeedbackHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla del Chatbot Inteligente Multiturno con Gemini.
 * Ofrece:
 * - Hilo conversacional con historial multiturno persistente en la sesión.
 * - Selector de Rol/Persona con instrucciones del sistema dedicadas:
 *   * Estratega Ejecutivo (Productividad y Enfoque)
 *   * Coach de Rendimiento Físico (Fitness y Sobrecarga)
 *   * Arquitecto Patrimonial (Finanzas y Ahorro)
 *   * Guardián de Atención (Dopamina y Bienestar Digital)
 * - Selector dinámico de modelo de Gemini según la complejidad de la tarea:
 *   * gemini-3.1-flash-lite-preview (Rápido)
 *   * gemini-3.5-flash (General / Estándar)
 *   * gemini-3.1-pro-preview (Complejo / Razonamiento Táctico)
 * - Sugerencias tácticas de inicio rápido y retroalimentación háptica.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    state: PersonalOsUiState,
    onSendMessage: (String) -> Unit,
    onSelectPersona: (ChatbotPersona) -> Unit,
    onSelectModel: (GeminiModelOption) -> Unit,
    onClearChat: () -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var inputText by remember { mutableStateOf("") }
    var showModelPicker by remember { mutableStateOf(false) }

    // Auto-scroll al último mensaje cuando llegan nuevas respuestas
    LaunchedEffect(state.chatMessages.size, state.isChatLoading) {
        if (state.chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(state.chatMessages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Gemini LifeOS AI",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = ExecutiveTextWhite
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = ExecutiveCyan.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, ExecutiveCyan.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = state.selectedGeminiModel.badge,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = ExecutiveCyan,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "${state.selectedChatPersona.title} • ${state.selectedGeminiModel.displayName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = ExecutiveTextMuted
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            SensoryFeedbackHelper.vibrateLightTick(context)
                            onClose()
                        },
                        modifier = Modifier.testTag("btn_close_chat")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = ExecutiveTextWhite
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showModelPicker = true },
                        modifier = Modifier.testTag("btn_select_model")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Cambiar Modelo",
                            tint = ExecutiveCyan
                        )
                    }
                    if (state.chatMessages.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                SensoryFeedbackHelper.vibrateLightTick(context)
                                onClearChat()
                            },
                            modifier = Modifier.testTag("btn_clear_chat")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Limpiar Chat",
                                tint = ExecutiveTextMuted
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ExecutiveDarkSurface
                )
            )
        },
        containerColor = ExecutiveDarkBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Selector horizontal de Personas / Roles del Asistente
            ChatPersonaSelector(
                currentPersona = state.selectedChatPersona,
                onSelectPersona = { persona ->
                    SensoryFeedbackHelper.vibrateLightTick(context)
                    onSelectPersona(persona)
                }
            )

            HorizontalDivider(color = ExecutiveBorderSubtle, thickness = 1.dp)

            // 2. Contenedor de Mensajes
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (state.chatMessages.isEmpty()) {
                    ChatEmptyState(
                        persona = state.selectedChatPersona,
                        modelOption = state.selectedGeminiModel,
                        onSelectPrompt = { prompt ->
                            SensoryFeedbackHelper.vibrateLightTick(context)
                            onSendMessage(prompt)
                        }
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.chatMessages, key = { it.id }) { message ->
                            ChatMessageBubble(message = message)
                        }

                        if (state.isChatLoading) {
                            item {
                                ChatTypingIndicator(
                                    persona = state.selectedChatPersona,
                                    model = state.selectedGeminiModel
                                )
                            }
                        }
                    }
                }
            }

            // 3. Barra inferior de Entrada de Texto
            ChatInputBar(
                inputText = inputText,
                isLoading = state.isChatLoading,
                onTextChanged = { inputText = it },
                onSend = {
                    if (inputText.isNotBlank() && !state.isChatLoading) {
                        val text = inputText.trim()
                        inputText = ""
                        SensoryFeedbackHelper.vibrateLightTick(context)
                        onSendMessage(text)
                    }
                }
            )
        }
    }

    // Modal para seleccionar el modelo de Gemini por nivel de complejidad
    if (showModelPicker) {
        AlertDialog(
            onDismissRequest = { showModelPicker = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = null,
                        tint = ExecutiveCyan
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Selección de Modelo Gemini", style = MaterialTheme.typography.titleMedium)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Elige el modelo según la complejidad de la tarea:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    GeminiModelOption.entries.forEach { option ->
                        val isSelected = option == state.selectedGeminiModel
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) ExecutiveCyan.copy(alpha = 0.15f) else ExecutiveDarkSurfaceElevated,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) ExecutiveCyan else ExecutiveBorderSubtle
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    SensoryFeedbackHelper.vibrateLightTick(context)
                                    onSelectModel(option)
                                    showModelPicker = false
                                }
                                .testTag("model_option_${option.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        SensoryFeedbackHelper.vibrateLightTick(context)
                                        onSelectModel(option)
                                        showModelPicker = false
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = ExecutiveCyan)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = option.displayName,
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (isSelected) ExecutiveCyan else ExecutiveTextWhite
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (isSelected) ExecutiveCyan else ExecutiveDarkSurfaceVariant
                                        ) {
                                            Text(
                                                text = option.badge,
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                                color = if (isSelected) ExecutiveDarkBg else ExecutiveTextMuted,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = option.description,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = ExecutiveTextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showModelPicker = false }) {
                    Text("Cerrar", color = ExecutiveCyan)
                }
            }
        )
    }
}

/**
 * Selector horizontal de Roles / Personas para guiar el sistema y tono del chatbot.
 */
@Composable
private fun ChatPersonaSelector(
    currentPersona: ChatbotPersona,
    onSelectPersona: (ChatbotPersona) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(ExecutiveDarkSurface)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(ChatbotPersona.entries) { persona ->
            val isSelected = persona == currentPersona
            val icon = getPersonaIcon(persona)
            val accentColor = getPersonaColor(persona)

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) accentColor.copy(alpha = 0.2f) else ExecutiveDarkSurfaceElevated,
                border = BorderStroke(
                    1.dp,
                    if (isSelected) accentColor else ExecutiveBorderSubtle
                ),
                modifier = Modifier
                    .clickable { onSelectPersona(persona) }
                    .testTag("persona_tab_${persona.name.lowercase()}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) accentColor else ExecutiveTextMuted,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = persona.title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (isSelected) ExecutiveTextWhite else ExecutiveTextMuted
                    )
                }
            }
        }
    }
}

/**
 * Burbuja individual de mensaje (Usuario o Asistente).
 */
@Composable
private fun ChatMessageBubble(message: ChatMessage) {
    val isUser = message.sender == ChatSender.USER
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val timeString = remember(message.timestamp) { timeFormatter.format(Date(message.timestamp)) }
    val personaColor = getPersonaColor(message.persona)
    val personaIcon = getPersonaIcon(message.persona)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(personaColor.copy(alpha = 0.2f))
                    .border(1.dp, personaColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = personaIcon,
                    contentDescription = null,
                    tint = personaColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 14.dp,
                topEnd = 14.dp,
                bottomStart = if (isUser) 14.dp else 2.dp,
                bottomEnd = if (isUser) 2.dp else 14.dp
            ),
            color = when {
                message.isError -> ExecutiveDangerRed.copy(alpha = 0.18f)
                isUser -> ExecutivePrimaryTeal.copy(alpha = 0.22f)
                else -> ExecutiveDarkSurfaceElevated
            },
            border = BorderStroke(
                1.dp,
                when {
                    message.isError -> ExecutiveDangerRed.copy(alpha = 0.5f)
                    isUser -> ExecutivePrimaryTeal.copy(alpha = 0.5f)
                    else -> ExecutiveBorderSubtle
                }
            ),
            modifier = Modifier
                .widthIn(max = 310.dp)
                .testTag("chat_bubble_${message.id}")
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (!isUser) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = message.persona.title.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = personaColor
                        )
                        Text(
                            text = message.modelUsed.badge,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                            color = ExecutiveTextMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 20.sp,
                        fontSize = 13.5.sp
                    ),
                    color = if (message.isError) ExecutiveDangerRed else ExecutiveTextWhite
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = timeString,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = ExecutiveTextMuted,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(ExecutivePrimaryTeal.copy(alpha = 0.25f))
                    .border(1.dp, ExecutivePrimaryTeal, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = ExecutivePrimaryTeal,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Indicador de respuesta en curso.
 */
@Composable
private fun ChatTypingIndicator(
    persona: ChatbotPersona,
    model: GeminiModelOption
) {
    val color = getPersonaColor(persona)
    val icon = getPersonaIcon(persona)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f))
                .border(1.dp, color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = ExecutiveDarkSurfaceElevated,
            border = BorderStroke(1.dp, ExecutiveBorderSubtle)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = color,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "${model.displayName} formulando respuesta...",
                    style = MaterialTheme.typography.labelSmall,
                    color = ExecutiveTextMuted
                )
            }
        }
    }
}

/**
 * Estado vacío con sugerencias de inicio rápido según el rol seleccionado.
 */
@Composable
private fun ChatEmptyState(
    persona: ChatbotPersona,
    modelOption: GeminiModelOption,
    onSelectPrompt: (String) -> Unit
) {
    val icon = getPersonaIcon(persona)
    val color = getPersonaColor(persona)

    val starterPrompts = when (persona) {
        ChatbotPersona.EXECUTIVE_STRATEGIST -> listOf(
            "¿Cuál debería ser mi bloque de máxima prioridad hoy?",
            "¿Cómo optimizar mi consistencia de hábitos este ciclo?",
            "Dame una regla táctica para eliminar distracciones hoy"
        )
        ChatbotPersona.TACTICAL_COACH -> listOf(
            "¿Cómo estructurar mi sobrecarga progresiva en banca?",
            "Calcula el RIR óptimo para hipertrofia en sentadillas",
            "Estrategia de recuperación muscular post-sesión"
        )
        ChatbotPersona.WEALTH_ARCHITECT -> listOf(
            "¿Cómo elevar mi ratio de ahorro este mes?",
            "Estrategia para liquidar pasivos y acelerar patrimonio",
            "Regla de presupuesto para gastos imprevistos"
        )
        ChatbotPersona.WELLBEING_GUARDIAN -> listOf(
            "Protocolo de desconexión de pantallas antes de dormir",
            "¿Cómo reiniciar mis circuitos de dopamina y enfoque?",
            "Ejercicio de respiración táctica para reducir cortisol"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f))
                .border(1.dp, color.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = persona.title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = ExecutiveTextWhite
        )
        Text(
            text = persona.roleLabel,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Asesor interactivo multiturno con memoria de conversación en vivo y lectura de métricas del ciclo.",
            style = MaterialTheme.typography.bodySmall,
            color = ExecutiveTextMuted,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "PROMPTS TÁCTICOS RECOMENDADOS:",
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = ExecutiveTextMuted
        )

        Spacer(modifier = Modifier.height(10.dp))

        starterPrompts.forEach { prompt ->
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = ExecutiveDarkSurfaceElevated,
                border = BorderStroke(1.dp, ExecutiveBorderSubtle),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onSelectPrompt(prompt) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = prompt,
                        style = MaterialTheme.typography.bodySmall,
                        color = ExecutiveTextWhite
                    )
                }
            }
        }
    }
}

/**
 * Campo de entrada de texto inferior con botón de envío táctil y soporte de tecla Enter.
 */
@Composable
private fun ChatInputBar(
    inputText: String,
    isLoading: Boolean,
    onTextChanged: (String) -> Unit,
    onSend: () -> Unit
) {
    Surface(
        color = ExecutiveDarkSurface,
        border = BorderStroke(1.dp, ExecutiveBorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = onTextChanged,
                placeholder = {
                    Text(
                        "Pregunta o conversa con Gemini...",
                        style = MaterialTheme.typography.bodySmall,
                        color = ExecutiveTextMuted
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_chat_message"),
                maxLines = 4,
                shape = RoundedCornerShape(20.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ExecutiveCyan,
                    unfocusedBorderColor = ExecutiveBorderSubtle,
                    focusedTextColor = ExecutiveTextWhite,
                    unfocusedTextColor = ExecutiveTextWhite,
                    cursorColor = ExecutiveCyan
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onSend,
                enabled = inputText.isNotBlank() && !isLoading,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (inputText.isNotBlank() && !isLoading) ExecutiveCyan else ExecutiveDarkSurfaceElevated
                    )
                    .testTag("btn_send_chat_message")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Enviar",
                    tint = if (inputText.isNotBlank() && !isLoading) ExecutiveDarkBg else ExecutiveTextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private fun getPersonaIcon(persona: ChatbotPersona): ImageVector = when (persona) {
    ChatbotPersona.EXECUTIVE_STRATEGIST -> Icons.Default.Insights
    ChatbotPersona.TACTICAL_COACH -> Icons.Default.FitnessCenter
    ChatbotPersona.WEALTH_ARCHITECT -> Icons.Default.AccountBalanceWallet
    ChatbotPersona.WELLBEING_GUARDIAN -> Icons.Default.Shield
}

private fun getPersonaColor(persona: ChatbotPersona): Color = when (persona) {
    ChatbotPersona.EXECUTIVE_STRATEGIST -> ExecutiveCyan
    ChatbotPersona.TACTICAL_COACH -> ExecutiveElectricViolet
    ChatbotPersona.WEALTH_ARCHITECT -> ExecutiveSuccessGreen
    ChatbotPersona.WELLBEING_GUARDIAN -> ExecutiveSecondaryAmber
}
