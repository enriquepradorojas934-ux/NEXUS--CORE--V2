package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.DetectedGlobalEntry
import com.example.data.model.GlobalEntryType
import com.example.ui.theme.*

@Composable
fun GlobalCommandInput(
    detectedEntries: List<DetectedGlobalEntry>,
    onDetect: (String) -> Unit,
    onExecute: (List<DetectedGlobalEntry>) -> Unit,
    onClear: () -> Unit,
    dailyAiSummary: String? = null,
    modifier: Modifier = Modifier
) {
    var rawText by remember { mutableStateOf("") }

    TacticalCard(
        modifier = modifier.fillMaxWidth().testTag("card_global_command_center"),
        accentColor = TacticalNeonCyan
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = null,
                        tint = TacticalNeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.global_command_title),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TacticalNeonCyan,
                        letterSpacing = 1.sp
                    )
                }

                if (rawText.isNotBlank()) {
                    IconButton(
                        onClick = {
                            rawText = ""
                            onClear()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_clear), tint = ExecutiveTextSecondary, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Resumen diario generado automáticamente por IA (aparece sin escribir nada)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = ExecutiveDarkSurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, TacticalNeonCyan.copy(alpha = 0.22f)),
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp).testTag("card_daily_ai_summary")
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = TacticalNeonCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                stringResource(R.string.global_command_daily_summary),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TacticalNeonCyan,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = TacticalNeonCyan.copy(alpha = 0.15f)
                        ) {
                            Text(
                                stringResource(R.string.global_command_ai_badge),
                                fontSize = 9.sp,
                                color = TacticalNeonCyan,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = dailyAiSummary ?: stringResource(R.string.global_command_synthesizing),
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                        color = ExecutiveTextPrimary
                    )
                }
            }

            OutlinedTextField(
                value = rawText,
                onValueChange = {
                    rawText = it
                    if (it.length > 5) {
                        onDetect(it)
                    } else {
                        onClear()
                    }
                },
                placeholder = {
                    Text(
                        stringResource(R.string.global_command_placeholder),
                        fontSize = 12.sp,
                        color = ExecutiveTextSecondary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_global_command"),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TacticalNeonCyan,
                    unfocusedBorderColor = ExecutiveBorderSubtle,
                    focusedContainerColor = ExecutiveDarkSurfaceElevated,
                    unfocusedContainerColor = ExecutiveDarkSurfaceElevated,
                    focusedTextColor = ExecutiveTextPrimary,
                    unfocusedTextColor = ExecutiveTextPrimary
                ),
                maxLines = 3
            )

            AnimatedVisibility(visible = detectedEntries.isNotEmpty()) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                    Text(
                        text = stringResource(R.string.global_command_preview, detectedEntries.size),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TacticalLuminousBlue,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (entry in detectedEntries) {
                            val badgeColor = when (entry.type) {
                                GlobalEntryType.WORKOUT -> TacticalNeonCyan
                                GlobalEntryType.TIME_TRACKING -> TacticalLuminousBlue
                                GlobalEntryType.TRANSACTION_EXPENSE -> TacticalCriticalCrimson
                                GlobalEntryType.TRANSACTION_INCOME -> TacticalSuccessGreen
                                GlobalEntryType.TASK -> TacticalVioletIdentity
                            }
                            val typeLabel = when (entry.type) {
                                GlobalEntryType.WORKOUT -> stringResource(R.string.type_workout)
                                GlobalEntryType.TIME_TRACKING -> stringResource(R.string.type_focus)
                                GlobalEntryType.TRANSACTION_EXPENSE -> stringResource(R.string.type_expense)
                                GlobalEntryType.TRANSACTION_INCOME -> stringResource(R.string.type_income)
                                GlobalEntryType.TASK -> stringResource(R.string.type_task)
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(ExecutiveDarkSurfaceElevated)
                                    .border(1.dp, badgeColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(badgeColor.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = typeLabel,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = badgeColor
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = entry.title,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ExecutiveTextPrimary
                                        )
                                        Text(
                                            text = entry.detail,
                                            fontSize = 11.sp,
                                            color = ExecutiveTextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                rawText = ""
                                onClear()
                            },
                            modifier = Modifier.testTag("btn_cancel_global_command")
                        ) {
                            Text(stringResource(R.string.action_discard), color = ExecutiveTextSecondary, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onExecute(detectedEntries)
                                rawText = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TacticalNeonCyan),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.testTag("btn_confirm_global_command")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = ExecutiveDarkBg, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.global_command_confirm_count, detectedEntries.size),
                                color = ExecutiveDarkBg,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
