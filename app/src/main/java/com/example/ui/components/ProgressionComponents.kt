package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AchievementItem
import com.example.data.model.UserProgression
import com.example.ui.theme.*

/**
 * Tarjeta táctica HUD de Nivel y Progresión RPG en el Centro de Mando
 */
@Composable
fun TacticalProgressionWidget(
    progression: UserProgression,
    onOpenDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    TacticalCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onOpenDetails() }
            .testTag("card_tactical_progression"),
        accentColor = SoloNeonCyan
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SoloTrack)
                            .border(1.5.dp, SoloGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "LV${progression.level}",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = SoloGold // Acento dorado #FFC700
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = progression.rankTitle.uppercase(),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SoloNeonCyan,
                            letterSpacing = 1.2.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "XP TOTAL: ",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = SoloTextSecondary
                            )
                            Text(
                                text = "${progression.totalXp} XP",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold,
                                color = SoloGold // Acento dorado #FFC700
                            )
                        }
                    }
                }

                // Botón secundario con borde neón
                OutlinedButton(
                    onClick = onOpenDetails,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = SoloTrack,
                        contentColor = SoloNeonCyan
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SoloNeonCyan.copy(alpha = 0.7f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("btn_view_progression_details")
                ) {
                    Text(
                        text = stringResource(R.string.progression_details).uppercase(),
                        fontSize = 10.sp,
                        color = SoloNeonCyan,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Barra de XP del nivel actual con degradado cyan -> morado sobre track #1A1A24
            DailyProgressBar(
                progress = progression.progressPercent,
                modifier = Modifier.fillMaxWidth(),
                label = "NIVEL ${progression.level} // PROGRESO",
                valueText = "${progression.currentLevelXp} / ${progression.nextLevelXp} XP"
            )

            Spacer(modifier = Modifier.height(14.dp))

            // BARRAS HORIZONTALES DE ATRIBUTOS (DISCIPLINA, ENFOQUE, VITALIDAD, FINANZAS)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ATRIBUTOS DE RANGO S",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.4.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = SoloTextSecondary
                )
                Text(
                    text = "STATUS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = SoloNeonCyan
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DailyProgressBar(
                    progress = progression.disciplineScore / 100f,
                    label = "DISCIPLINA",
                    valueText = "${progression.disciplineScore}/100"
                )
                DailyProgressBar(
                    progress = progression.focusScore / 100f,
                    label = "ENFOQUE",
                    valueText = "${progression.focusScore}/100"
                )
                DailyProgressBar(
                    progress = progression.vitalityScore / 100f,
                    label = "VITALIDAD",
                    valueText = "${progression.vitalityScore}/100"
                )
                DailyProgressBar(
                    progress = progression.financialScore / 100f,
                    label = "FINANZAS",
                    valueText = "${progression.financialScore}/100"
                )
            }
        }
    }
}

/**
 * Diálogo interactivo con los 5 Atributos Tácticos y el Sistema de Logros
 */
@Composable
fun TacticalProgressionDialog(
    progression: UserProgression,
    achievements: List<AchievementItem>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SoloCardBg, // Fondo tarjetas #0F0F14
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp).testTag("dialog_progression_details"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SOLO LEVELING // RANGO S",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = SoloNeonCyan,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "NIVEL ${progression.level} · ${progression.rankTitle.uppercase()}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SoloTextPrimary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_close), tint = SoloTextSecondary)
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // SECCIÓN DE ATRIBUTOS TÁCTICOS
                item {
                    Text(
                        text = "ATRIBUTOS PRINCIPALES DEL OPERADOR",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SoloTextSecondary,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    DailyProgressBar(
                        progress = progression.disciplineScore / 100f,
                        label = "DISCIPLINA",
                        valueText = "${progression.disciplineScore}/100"
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    DailyProgressBar(
                        progress = progression.productivityScore / 100f,
                        label = "PRODUCTIVIDAD",
                        valueText = "${progression.productivityScore}/100"
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    DailyProgressBar(
                        progress = progression.focusScore / 100f,
                        label = "ENFOQUE",
                        valueText = "${progression.focusScore}/100"
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    DailyProgressBar(
                        progress = progression.vitalityScore / 100f,
                        label = "VITALIDAD",
                        valueText = "${progression.vitalityScore}/100"
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    DailyProgressBar(
                        progress = progression.financialScore / 100f,
                        label = "FINANZAS",
                        valueText = "${progression.financialScore}/100"
                    )
                }

                // SECCIÓN DE LOGROS
                item {
                    val unlockedCount = achievements.count { it.isUnlocked }
                    Text(
                        text = "LOGROS DESBLOQUEADOS ($unlockedCount / ${achievements.size})",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SoloNeonCyan,
                        letterSpacing = 1.2.sp
                    )
                }

                items(achievements, key = { it.id }) { ach ->
                    val borderCol = if (ach.isUnlocked) SoloNeonCyan.copy(alpha = 0.6f) else SoloCardBorder
                    val bgCol = if (ach.isUnlocked) SoloTrack else SoloCardBg

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(bgCol)
                            .border(1.dp, borderCol, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                            .testTag("achievement_item_${ach.id}")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (ach.isUnlocked) Icons.Default.EmojiEvents else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (ach.isUnlocked) SoloGold else SoloTextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = ach.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (ach.isUnlocked) SoloTextPrimary else SoloTextSecondary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "+${ach.xpReward} XP",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = SoloGold, // Acento dorado #FFC700
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                                Text(
                                    text = ach.description,
                                    fontSize = 11.sp,
                                    color = SoloTextSecondary,
                                    lineHeight = 14.sp
                                )
                                if (!ach.isUnlocked) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(SoloTrack)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth((ach.currentProgress.toFloat() / ach.maxProgress.toFloat()).coerceIn(0f, 1f))
                                                .fillMaxHeight()
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(SoloNeonGradient)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = SoloNeonCyan),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(stringResource(R.string.action_close), color = SoloBg, fontWeight = FontWeight.Bold)
            }
        }
    )
}
