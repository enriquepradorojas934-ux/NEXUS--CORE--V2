package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppNavTab
import com.example.ui.theme.*
import com.example.util.SensoryFeedbackHelper

@Composable
fun ExecutiveHeader(
    title: String,
    subtitle: String? = null,
    cycleDate: String,
    timeUntilReset: String,
    cycleProgress: Float,
    cycleStartTime: String = "05:00",
    showCycleBar: Boolean = false,
    actionIcon: ImageVector? = null,
    onActionClick: (() -> Unit)? = null
) {
    Surface(
        color = SoloBg,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // HUD Tag & Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(SoloNeonCyan)
                            .border(1.5.dp, SoloNeonCyan.copy(alpha = 0.8f), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = "SOLO LEVELING // RANGO S",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.4.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        ),
                        color = SoloNeonCyan
                    )
                }

                if (actionIcon != null && onActionClick != null) {
                    IconButton(
                        onClick = onActionClick,
                        modifier = Modifier
                            .size(32.dp)
                            .border(1.dp, SoloCardBorder, RoundedCornerShape(8.dp))
                            .background(SoloCardBg, RoundedCornerShape(8.dp))
                            .testTag("header_action_button")
                    ) {
                        Icon(
                            actionIcon,
                            contentDescription = "Acción",
                            tint = SoloNeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Screen Title & Subtitle
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    letterSpacing = (-0.5).sp
                ),
                color = SoloTextPrimary
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp
                    ),
                    color = SoloTextSecondary
                )
            }

            // Bloque CICLO ACTIVO: Solo visible en el HUD (Centro de Mando)
            if (showCycleBar) {
                Spacer(modifier = Modifier.height(10.dp))
                val cycleRangeText = com.example.domain.DailyCycleEngine.getCycleRangeDisplay(cycleStartTime)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = SoloCardBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SoloBorderGlow),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                Text(
                                    text = "CICLO ACTIVO // $cycleRangeText",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.1.sp,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    color = SoloTextSecondary,
                                    maxLines = 1
                                )
                                Text(
                                    text = cycleDate,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp
                                    ),
                                    color = SoloTextPrimary,
                                    maxLines = 1
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Chip compacto y alineado para el reinicio
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SoloTrack,
                                border = androidx.compose.foundation.BorderStroke(1.dp, SoloGold.copy(alpha = 0.55f))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = SoloGold,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = timeUntilReset,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp,
                                            letterSpacing = 0.4.sp
                                        ),
                                        color = SoloGold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(7.dp))

                        // Barra de progreso del ciclo con degradado cyan -> morado sobre track #1A1A24
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(SoloTrack)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(cycleProgress.coerceIn(0f, 1f))
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(SoloNeonGradient)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LifeOsBadge(
    text: String,
    type: LifeOsBadgeType,
    modifier: Modifier = Modifier
) {
    val (textColor, borderColor) = when (type) {
        LifeOsBadgeType.AVAILABLE, LifeOsBadgeType.HEALTHY -> Pair(
            ExecutiveSuccessGreen,
            ExecutiveSuccessGreen.copy(alpha = 0.6f)
        )
        LifeOsBadgeType.ACTIVE -> Pair(
            SoloNeonCyan,
            SoloNeonCyan.copy(alpha = 0.7f)
        )
        LifeOsBadgeType.WARNING, LifeOsBadgeType.NEAR_LIMIT -> Pair(
            SoloGold,
            SoloGold.copy(alpha = 0.6f)
        )
        LifeOsBadgeType.EXHAUSTED, LifeOsBadgeType.BLOCKED, LifeOsBadgeType.CRITICAL -> Pair(
            ExecutiveDangerRed,
            ExecutiveDangerRed.copy(alpha = 0.6f)
        )
        LifeOsBadgeType.NEUTRAL -> Pair(
            SoloTextSecondary,
            SoloCardBorder
        )
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = SoloTrack, // Fondo #1A1A24
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor), // Borde neón
        modifier = modifier
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                fontFamily = FontFamily.Monospace
            ),
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

enum class LifeOsBadgeType {
    AVAILABLE, HEALTHY, WARNING, NEAR_LIMIT, EXHAUSTED, BLOCKED, CRITICAL, NEUTRAL, ACTIVE
}

@Composable
fun LifeOsSectionHeader(
    title: String,
    subtag: String = "",
    tag: String = "",
    badge: String? = null,
    icon: ImageVector? = null,
    indicatorColor: Color = SoloNeonCyan,
    trailingText: String? = null,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val effectiveTag = when {
        !badge.isNullOrBlank() -> badge
        subtag.isNotBlank() -> subtag
        else -> tag
    }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(indicatorColor)
                    .border(1.dp, indicatorColor.copy(alpha = 0.8f), CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = indicatorColor,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.6.sp,
                    fontFamily = FontFamily.Monospace
                ),
                color = SoloTextSecondary // Color secundario #8B8B9B
            )
            if (effectiveTag.isNotBlank()) {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SoloTrack,
                    border = androidx.compose.foundation.BorderStroke(1.dp, indicatorColor.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = effectiveTag.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.8.sp
                        ),
                        color = indicatorColor,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            }
        }
        if (actionLabel != null && onActionClick != null) {
            OutlinedButton(
                onClick = onActionClick,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = indicatorColor
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, indicatorColor.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    )
                )
            }
        } else if (trailingText != null) {
            Text(
                text = trailingText,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                ),
                color = SoloTextSecondary
            )
        }
    }
}

@Composable
fun TacticalCard(
    modifier: Modifier = Modifier,
    accentColor: Color = SoloNeonCyan,
    glowEffect: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp), // border-radius 16dp
        color = SoloCardBg, // Fondo tarjetas #0F0F14
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            androidx.compose.ui.graphics.Brush.linearGradient(
                listOf(
                    accentColor.copy(alpha = 0.50f),
                    SoloCardBorder,
                    SoloNeonPurple.copy(alpha = 0.35f)
                )
            )
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(20.dp), // padding interno ~20dp
            content = content
        )
    }
}

/**
 * Control de completado circular moderno táctico.
 * Pendiente: ○
 * Completado: ●
 * Animación sutil al completarse, feedback táctil y buen contraste.
 */
@Composable
fun CircularCompletionControl(
    isCompleted: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = ExecutiveSuccessGreen,
    inactiveColor: Color = SoloCardBorder,
    size: Dp = 24.dp
) {
    val context = LocalContext.current
    val scale by animateFloatAsState(targetValue = if (isCompleted) 1.15f else 1.0f, label = "scale")
    val animatedBorderColor by animateColorAsState(
        targetValue = if (isCompleted) activeColor else inactiveColor,
        label = "border_color"
    )
    val animatedFillColor by animateColorAsState(
        targetValue = if (isCompleted) activeColor else Color.Transparent,
        label = "fill_color"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .clip(CircleShape)
            .clickable {
                if (!isCompleted) {
                    SensoryFeedbackHelper.vibrateSuccess(context)
                } else {
                    SensoryFeedbackHelper.vibrateLightTick(context)
                }
                onToggle()
            }
            .border(2.dp, animatedBorderColor, CircleShape)
            .background(animatedFillColor),
        contentAlignment = Alignment.Center
    ) {
        if (isCompleted) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = androidx.compose.ui.res.stringResource(com.example.R.string.action_completed),
                tint = Color.Black,
                modifier = Modifier.size(size * 0.65f)
            )
        } else {
            // Anillo interior sutil
            Box(
                modifier = Modifier
                    .size(size * 0.35f)
                    .clip(CircleShape)
                    .background(inactiveColor.copy(alpha = 0.4f))
            )
        }
    }
}

/**
 * Barra de Progreso Diario LifeOS
 * Muestra: "X / Y completadas", barra porcentual reactiva con degradado Solo Leveling,
 * y estado "DÍA COMPLETADO" con estilo táctico cuando se alcanza el 100%.
 */
@Composable
fun DailyProgressBar(
    completed: Int,
    total: Int,
    modifier: Modifier = Modifier,
    accentColor: Color = SoloNeonCyan
) {
    val percent = if (total > 0) (completed.toFloat() / total).coerceIn(0f, 1f) else 0f
    val percentText = (percent * 100).toInt()
    val isAllDone = total > 0 && completed >= total

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = SoloCardBg,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isAllDone) androidx.compose.ui.graphics.SolidColor(ExecutiveSuccessGreen.copy(alpha = 0.5f))
            else androidx.compose.ui.graphics.Brush.linearGradient(listOf(SoloNeonCyan.copy(alpha = 0.4f), SoloCardBorder, SoloNeonPurple.copy(alpha = 0.3f)))
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isAllDone) ExecutiveSuccessGreen else SoloNeonCyan)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isAllDone) androidx.compose.ui.res.stringResource(com.example.R.string.day_completed).uppercase() else androidx.compose.ui.res.stringResource(com.example.R.string.daily_progress).uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.2.sp,
                            fontSize = 11.sp
                        ),
                        color = if (isAllDone) ExecutiveSuccessGreen else SoloTextSecondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.example.R.string.completed_tasks_count, completed, total),
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = SoloTextSecondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$percentText%",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        ),
                        color = if (isAllDone) ExecutiveSuccessGreen else SoloGold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Barra con fondo #1A1A24 y degradado neón cyan -> morado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(SoloTrack)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(percent)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (isAllDone) androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(ExecutiveSuccessGreen, SoloNeonCyan))
                            else SoloNeonGradient
                        )
                )
            }

            if (isAllDone) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = ExecutiveSuccessGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.example.R.string.cycle_objectives_achieved),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = ExecutiveSuccessGreen
                    )
                }
            }
        }
    }
}

@Composable
fun DailyProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    label: String = "PROGRESO",
    valueText: String = "",
    fillColor: Color = SoloNeonCyan
) {
    val coercedProgress = progress.coerceIn(0f, 1f)
    val percentText = (coercedProgress * 100).toInt()
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    letterSpacing = 1.2.sp
                ),
                color = SoloTextSecondary
            )
            Text(
                text = if (valueText.isNotBlank()) valueText else "$percentText%",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                ),
                color = SoloGold
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        // Track #1A1A24 con relleno degradado cyan -> morado
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(SoloTrack)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(coercedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(SoloNeonGradient)
            )
        }
    }
}

@Composable
fun ExecutiveMetricCard(
    modifier: Modifier = Modifier,
    title: String,
    question: String,
    value: String,
    detail: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = SoloCardBg
        ),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SoloCardBorder),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("metric_card_${title.lowercase().replace(" ", "_")}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SoloTrack)
                    .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = question.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp,
                        fontSize = 10.sp
                    ),
                    color = SoloTextSecondary
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    ),
                    color = SoloTextPrimary
                )
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 16.sp, fontSize = 11.sp),
                    color = SoloTextSecondary
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Ir",
                tint = SoloNeonCyan.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun ExecutiveBottomNav(
    activeTab: AppNavTab,
    onTabSelected: (AppNavTab) -> Unit,
    modifier: Modifier = Modifier,
    language: String = "Español"
) {
    NavigationBar(
        modifier = modifier
            .border(androidx.compose.foundation.BorderStroke(1.dp, SoloCardBorder))
            .testTag("bottom_navigation_bar"),
        containerColor = SoloCardBg, // #0F0F14
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            Triple(AppNavTab.DASHBOARD, Icons.Default.Dashboard, androidx.compose.ui.res.stringResource(com.example.R.string.nav_hub)),
            Triple(AppNavTab.TIME, Icons.Default.Schedule, androidx.compose.ui.res.stringResource(com.example.R.string.nav_time)),
            Triple(AppNavTab.HABITS, Icons.Default.CheckCircle, androidx.compose.ui.res.stringResource(com.example.R.string.nav_habits)),
            Triple(AppNavTab.FITNESS, Icons.Default.FitnessCenter, androidx.compose.ui.res.stringResource(com.example.R.string.nav_fitness)),
            Triple(AppNavTab.GOALS, Icons.Default.Flag, androidx.compose.ui.res.stringResource(com.example.R.string.nav_goals)),
            Triple(AppNavTab.FINANCE, Icons.Default.AccountBalanceWallet, androidx.compose.ui.res.stringResource(com.example.R.string.nav_finance))
        )

        items.forEach { (tab, icon, label) ->
            val isSelected = activeTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        icon,
                        contentDescription = label,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                        fontSize = 10.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = SoloBg,
                    selectedTextColor = SoloNeonCyan,
                    indicatorColor = SoloNeonCyan,
                    unselectedIconColor = SoloTextSecondary,
                    unselectedTextColor = SoloTextSecondary
                ),
                modifier = Modifier.testTag("nav_item_${tab.name.lowercase()}")
            )
        }
    }
}
