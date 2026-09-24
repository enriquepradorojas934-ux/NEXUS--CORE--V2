package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * Pantalla de Bloqueo de Sistema LifeOS Precision Executive.
 * Diseñada deliberadamente para sentirse como una política de bloqueo
 * a nivel de Sistema Operativo y no como un simple modal.
 */
@Composable
fun SystemLockScreenOverlay(
    visible: Boolean,
    appName: String = "Instagram",
    usedMinutes: Int = 30,
    budgetMinutes: Int = 30,
    resetTime: String = "05:00 AM",
    timeUntilReset: String = "11h 24m",
    isStrictMode: Boolean = true,
    onDismissToHub: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("system_lock_screen_overlay"),
            color = ExecutiveDarkBg
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Barra superior de estado técnico
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(ExecutiveDangerRed)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "LIFEOS // SECURITY SUBSYSTEM",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 1.2.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = ExecutiveDangerRed
                        )
                    }

                    // Botón técnico para regresar al Hub (sin evadir el bloqueo de la app)
                    IconButton(
                        onClick = onDismissToHub,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("btn_dismiss_lock_to_hub")
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Volver al Hub",
                            tint = ExecutiveTextMuted
                        )
                    }
                }

                // Contenido Central de Bloqueo Crítico
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Icono de candado escarlata institucional
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(ExecutiveDangerRed.copy(alpha = 0.12f))
                            .border(1.dp, ExecutiveDangerRed.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Bloqueado",
                            tint = ExecutiveDangerRed,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "PRESUPUESTO DE ATENCIÓN AGOTADO",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = ExecutiveDangerRed,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = appName,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = ExecutiveTextWhite
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Tarjeta de métrica de tiempo consumido vs permitido
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ExecutiveDarkSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ExecutiveBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = String.format("%02d:00 / %02d:00 utilizados", usedMinutes, budgetMinutes),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = ExecutiveTextWhite
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "00:00 restantes",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = ExecutiveDangerRed
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            LinearProgressIndicator(
                                progress = { 1f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = ExecutiveDangerRed,
                                trackColor = ExecutiveDarkSurfaceElevated
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tarjeta de Política: Próximo Desbloqueo y Modo Estricto
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ExecutiveDarkSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ExecutiveBorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "PRÓXIMO DESBLOQUEO",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        letterSpacing = 0.8.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = ExecutiveSecondaryAmber
                                )
                                Text(
                                    text = "$resetTime (en $timeUntilReset)",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = ExecutiveTextWhite
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isStrictMode) ExecutiveDangerRed.copy(alpha = 0.15f) else ExecutiveSecondaryAmber.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isStrictMode) ExecutiveDangerRed.copy(alpha = 0.4f) else ExecutiveSecondaryAmber.copy(alpha = 0.4f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = if (isStrictMode) ExecutiveDangerRed else ExecutiveSecondaryAmber,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isStrictMode) "MODO ESTRICTO ACTIVO" else "MODO NORMAL",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = if (isStrictMode) ExecutiveDangerRed else ExecutiveSecondaryAmber
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Esta aplicación ha sido bloqueada por la política operativa de LifeOS para preservar tu foco cognitivo hasta el inicio del nuevo ciclo operativo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ExecutiveTextMuted,
                        textAlign = TextAlign.Center
                    )
                }

                // Botón inferior único: Regresar al Centro de Mando
                Button(
                    onClick = onDismissToHub,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ExecutiveDarkSurfaceElevated,
                        contentColor = ExecutiveTextWhite
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .testTag("btn_return_to_hub")
                ) {
                    Text(
                        text = "REGRESAR AL CENTRO DE MANDO",
                        style = MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 0.8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}
