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
    actionIcon: ImageVector? = null,
    onActionClick: (() -> Unit)? = null
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
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
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(ExecutivePrimaryTeal)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIFEOS // PRECISION EXECUTIVE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = ExecutivePrimaryTeal
                    )
                }

                if (actionIcon != null && onActionClick != null) {
                    IconButton(
                        onClick = onActionClick,
                        modifier = Modifier.size(32.dp).testTag("header_action_button")
                    ) {
                        Icon(
                            actionIcon,
                            contentDescription = "Acción",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Screen Title & Subtitle
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Prominent Cycle Bar (05:00 → 04:59) & Next Reset Countdown
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = ExecutiveDarkSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, ExecutiveBorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = androidx.compose.ui.res.stringResource(com.example.R.string.active_cycle),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "05:00 → 04:59  •  $cycleDate",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = androidx.compose.ui.res.stringResource(com.example.R.string.cycle_reset_in, timeUntilReset),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = ExecutiveSecondaryAmber
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { cycleProgress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = ExecutivePrimaryTeal,
                        trackColor = ExecutiveDarkSurface
                    )
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
    val (bg, textColor, border) = when (type) {
        LifeOsBadgeType.AVAILABLE, LifeOsBadgeType.HEALTHY -> Triple(
            ExecutiveSuccessGreen.copy(alpha = 0.12f),
            ExecutiveSuccessGreen,
            ExecutiveSuccessGreen.copy(alpha = 0.3f)
        )
        LifeOsBadgeType.ACTIVE -> Triple(
            ExecutiveElectricViolet.copy(alpha = 0.2f),
            ExecutiveVioletGlow,
            ExecutiveElectricViolet.copy(alpha = 0.5f)
        )
        LifeOsBadgeType.WARNING, LifeOsBadgeType.NEAR_LIMIT -> Triple(
            ExecutiveSecondaryAmber.copy(alpha = 0.15f),
            ExecutiveSecondaryAmber,
            ExecutiveSecondaryAmber.copy(alpha = 0.4f)
        )
        LifeOsBadgeType.EXHAUSTED, LifeOsBadgeType.BLOCKED, LifeOsBadgeType.CRITICAL -> Triple(
            ExecutiveDangerRed.copy(alpha = 0.15f),
            ExecutiveDangerRed,
            ExecutiveDangerRed.copy(alpha = 0.4f)
        )
        LifeOsBadgeType.NEUTRAL -> Triple(
            ExecutiveDarkSurfaceElevated,
            ExecutiveTextMuted,
            ExecutiveBorderSubtle
        )
    }

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = bg,
        border = androidx.compose.foundation.BorderStroke(1.dp, border),
        modifier = modifier
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp
            ),
            color = textColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
    indicatorColor: Color = MaterialTheme.colorScheme.primary,
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
                    .border(1.dp, indicatorColor.copy(alpha = 0.5f), CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.4.sp
                ),
                color = ExecutiveTextWhite
            )
            if (effectiveTag.isNotBlank()) {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = indicatorColor.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, indicatorColor.copy(alpha = 0.3f))
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
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
        if (actionLabel != null && onActionClick != null) {
            TextButton(
                onClick = onActionClick,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = indicatorColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                )
            }
        } else if (trailingText != null) {
            Text(
                text = trailingText,
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                color = ExecutiveTextMuted
            )
        }
    }
}

@Composable
fun TacticalCard(
    modifier: Modifier = Modifier,
    accentColor: Color = ExecutiveElectricViolet,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = ExecutiveDarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, ExecutiveBorderSubtle),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp), content = content)
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
    inactiveColor: Color = ExecutiveBorder,
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
 * Muestra: "X / Y completadas", barra porcentual reactiva,
 * y estado "DÍA COMPLETADO" con estilo táctico cuando se alcanza el 100%.
 */
@Composable
fun DailyProgressBar(
    completed: Int,
    total: Int,
    modifier: Modifier = Modifier,
    accentColor: Color = ExecutiveElectricViolet
) {
    val percent = if (total > 0) (completed.toFloat() / total).coerceIn(0f, 1f) else 0f
    val percentText = (percent * 100).toInt()
    val isAllDone = total > 0 && completed >= total

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isAllDone) ExecutiveSuccessGreen.copy(alpha = 0.08f) else ExecutiveDarkSurface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isAllDone) ExecutiveSuccessGreen.copy(alpha = 0.4f) else ExecutiveBorderSubtle
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                            .background(if (isAllDone) ExecutiveSuccessGreen else accentColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isAllDone) androidx.compose.ui.res.stringResource(com.example.R.string.day_completed) else androidx.compose.ui.res.stringResource(com.example.R.string.daily_progress),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        ),
                        color = if (isAllDone) ExecutiveSuccessGreen else ExecutiveTextWhite
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.example.R.string.completed_tasks_count, completed, total),
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = ExecutiveTextMuted
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$percentText%",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = if (isAllDone) ExecutiveSuccessGreen else accentColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { percent },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isAllDone) ExecutiveSuccessGreen else accentColor,
                trackColor = ExecutiveDarkSurfaceVariant
            )

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
    fillColor: Color = ExecutiveElectricViolet
) {
    val percentText = (progress.coerceIn(0f, 1f) * 100).toInt()
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                ),
                color = ExecutiveTextWhite
            )
            Text(
                text = if (valueText.isNotBlank()) valueText else "$percentText%",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                ),
                color = fillColor
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = fillColor,
            trackColor = ExecutiveDarkSurfaceVariant
        )
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
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("metric_card_${title.lowercase().replace(" ", "_")}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
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
                    text = question,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 16.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Ir",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
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
        modifier = modifier.testTag("bottom_navigation_bar"),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        val items = listOf(
            Triple(AppNavTab.DASHBOARD, Icons.Default.Dashboard, androidx.compose.ui.res.stringResource(com.example.R.string.nav_hub)),
            Triple(AppNavTab.TIME, Icons.Default.Schedule, androidx.compose.ui.res.stringResource(com.example.R.string.nav_time)),
            Triple(AppNavTab.HABITS, Icons.Default.CheckCircle, androidx.compose.ui.res.stringResource(com.example.R.string.nav_habits)),
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
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ExecutiveDarkBg,
                    selectedTextColor = ExecutivePrimaryTeal,
                    indicatorColor = ExecutivePrimaryTeal,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag("nav_item_${tab.name.lowercase()}")
            )
        }
    }
}
