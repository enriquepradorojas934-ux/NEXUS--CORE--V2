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
        accentColor = TacticalVioletIdentity
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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(TacticalVioletIdentity.copy(alpha = 0.2f))
                            .border(1.dp, TacticalVioletIdentity, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "LV${progression.level}",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = TacticalLuminousBlue
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = progression.rankTitle,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TacticalNeonCyan,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = stringResource(R.string.progression_total_xp, progression.totalXp),
                            fontSize = 11.sp,
                            color = ExecutiveTextSecondary
                        )
                    }
                }

                FilledTonalButton(
                    onClick = onOpenDetails,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = TacticalVioletIdentity.copy(alpha = 0.25f)
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("btn_view_progression_details")
                ) {
                    Text(stringResource(R.string.progression_details), fontSize = 11.sp, color = TacticalLuminousBlue, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Barra de XP del nivel actual
            DailyProgressBar(
                progress = progression.progressPercent,
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(R.string.progression_level_progress, progression.level),
                valueText = "${progression.currentLevelXp} / ${progression.nextLevelXp} XP",
                fillColor = TacticalNeonCyan
            )
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
        containerColor = ExecutiveDarkSurface,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp).testTag("dialog_progression_details"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.progression_operator_status),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TacticalNeonCyan,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = stringResource(R.string.progression_level_title, progression.level, progression.rankTitle),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ExecutiveTextPrimary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_close), tint = ExecutiveTextSecondary)
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
                        text = stringResource(R.string.progression_primary_attributes),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TacticalLuminousBlue,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    DailyProgressBar(
                        progress = progression.disciplineScore / 100f,
                        label = stringResource(R.string.attr_discipline),
                        valueText = "${progression.disciplineScore}/100",
                        fillColor = TacticalSuccessGreen
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    DailyProgressBar(
                        progress = progression.productivityScore / 100f,
                        label = stringResource(R.string.attr_productivity),
                        valueText = "${progression.productivityScore}/100",
                        fillColor = TacticalLuminousBlue
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    DailyProgressBar(
                        progress = progression.focusScore / 100f,
                        label = stringResource(R.string.attr_focus),
                        valueText = "${progression.focusScore}/100",
                        fillColor = TacticalNeonCyan
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    DailyProgressBar(
                        progress = progression.vitalityScore / 100f,
                        label = stringResource(R.string.attr_vitality),
                        valueText = "${progression.vitalityScore}/100",
                        fillColor = TacticalVioletIdentity
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    DailyProgressBar(
                        progress = progression.financialScore / 100f,
                        label = stringResource(R.string.attr_finance),
                        valueText = "${progression.financialScore}/100",
                        fillColor = TacticalWarningAmber
                    )
                }

                // SECCIÓN DE LOGROS
                item {
                    val unlockedCount = achievements.count { it.isUnlocked }
                    Text(
                        text = stringResource(R.string.progression_tactical_achievements, unlockedCount, achievements.size),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TacticalNeonCyan,
                        letterSpacing = 1.sp
                    )
                }

                items(achievements, key = { it.id }) { ach ->
                    val borderCol = if (ach.isUnlocked) TacticalSuccessGreen.copy(alpha = 0.6f) else ExecutiveBorderSubtle
                    val bgCol = if (ach.isUnlocked) TacticalSuccessGreen.copy(alpha = 0.08f) else ExecutiveDarkSurfaceElevated

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(bgCol)
                            .border(1.dp, borderCol, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                            .testTag("achievement_item_${ach.id}")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (ach.isUnlocked) Icons.Default.EmojiEvents else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (ach.isUnlocked) TacticalSuccessGreen else ExecutiveTextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = ach.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (ach.isUnlocked) ExecutiveTextPrimary else ExecutiveTextSecondary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "+${ach.xpReward} XP",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = TacticalNeonCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = ach.description,
                                    fontSize = 11.sp,
                                    color = ExecutiveTextSecondary,
                                    lineHeight = 14.sp
                                )
                                if (!ach.isUnlocked) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { ach.currentProgress.toFloat() / ach.maxProgress.toFloat() },
                                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                        color = TacticalLuminousBlue,
                                        trackColor = ExecutiveBorderSubtle
                                    )
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
                colors = ButtonDefaults.buttonColors(containerColor = TacticalNeonCyan)
            ) {
                Text(stringResource(R.string.action_close), color = ExecutiveDarkBg, fontWeight = FontWeight.Bold)
            }
        }
    )
}
