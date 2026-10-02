package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.ui.theme.*
import java.util.Calendar
import java.util.TimeZone

/**
 * Selector de fecha unificado Material 3 para LifeOS.
 * Funciona de manera 100% nativa en Compose sobre el tema oscuro / graphite de LifeOS,
 * sin depender de Activity window tokens ni de android.app.DatePickerDialog para evitar crashes globales.
 *
 * Muestra la fecha limpia formateada: "DD/MM/YYYY" (ej: 15/10/2026).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LifeOsDatePickerDialog(
    initialDateText: String = "",
    onDismissRequest: () -> Unit,
    onDateSelected: (cleanFormattedDate: String) -> Unit
) {
    val initialMillis = parseDateStringToMillis(initialDateText)
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis
    )

    DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    val selectedMillis = datePickerState.selectedDateMillis ?: initialMillis
                    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                        timeInMillis = selectedMillis
                    }
                    val day = calendar.get(Calendar.DAY_OF_MONTH)
                    val month = calendar.get(Calendar.MONTH) + 1
                    val year = calendar.get(Calendar.YEAR)
                    val cleanDate = "%02d/%02d/%04d".format(day, month, year)
                    onDateSelected(cleanDate)
                    onDismissRequest()
                }
            ) {
                Text(
                    text = stringResource(R.string.action_save),
                    color = ExecutivePrimaryTeal,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(
                    text = stringResource(R.string.action_cancel),
                    color = ExecutiveTextSecondary
                )
            }
        },
        shape = RoundedCornerShape(16.dp),
        colors = DatePickerDefaults.colors(
            containerColor = ExecutiveDarkSurfaceElevated
        ),
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .wrapContentHeight()
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = ExecutiveDarkSurfaceElevated,
                    titleContentColor = ExecutiveTextWhite,
                    headlineContentColor = ExecutivePrimaryTeal,
                    weekdayContentColor = ExecutiveTextSecondary,
                    subheadContentColor = ExecutiveTextSecondary,
                    yearContentColor = ExecutiveTextWhite,
                    currentYearContentColor = ExecutivePrimaryTeal,
                    selectedYearContentColor = ExecutiveDarkBg,
                    selectedYearContainerColor = ExecutivePrimaryTeal,
                    dayContentColor = ExecutiveTextWhite,
                    disabledDayContentColor = ExecutiveTextSecondary.copy(alpha = 0.38f),
                    selectedDayContentColor = ExecutiveDarkBg,
                    selectedDayContainerColor = ExecutivePrimaryTeal,
                    todayContentColor = ExecutivePrimaryTeal,
                    todayDateBorderColor = ExecutivePrimaryTeal
                )
            )
        }
    }
}

/**
 * Selector de hora nativo Compose Material 3 para LifeOS, evitando caídas de TimePickerDialog tradicional.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LifeOsTimePickerDialog(
    initialTimeText: String = "08:00",
    initialPeriod: String = "AM",
    onDismissRequest: () -> Unit,
    onTimeSelected: (hour24: Int, minute: Int, formatted12: String, period: String) -> Unit
) {
    val initial24Hour = run {
        val parts = initialTimeText.split(":")
        var h = parts.getOrNull(0)?.toIntOrNull() ?: 8
        val isPm = initialPeriod.equals("PM", ignoreCase = true)
        if (isPm && h < 12) h += 12
        if (!isPm && h == 12) h = 0
        h
    }
    val initialMin = initialTimeText.split(":").getOrNull(1)?.toIntOrNull() ?: 0

    val timePickerState = rememberTimePickerState(
        initialHour = initial24Hour,
        initialMinute = initialMin,
        is24Hour = false
    )

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    val h24 = timePickerState.hour
                    val min = timePickerState.minute
                    val isPm = h24 >= 12
                    val h12 = when {
                        h24 == 0 -> 12
                        h24 > 12 -> h24 - 12
                        else -> h24
                    }
                    val formatted12 = "%02d:%02d".format(h12, min)
                    val period = if (isPm) "PM" else "AM"
                    onTimeSelected(h24, min, formatted12, period)
                    onDismissRequest()
                }
            ) {
                Text(
                    text = stringResource(R.string.action_save),
                    color = ExecutivePrimaryTeal
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(
                    text = stringResource(R.string.action_cancel),
                    color = ExecutiveTextSecondary
                )
            }
        },
        text = {
            TimePicker(
                state = timePickerState,
                colors = TimePickerDefaults.colors(
                    clockDialColor = ExecutiveDarkSurface,
                    selectorColor = ExecutivePrimaryTeal,
                    containerColor = ExecutiveDarkSurfaceElevated,
                    periodSelectorBorderColor = ExecutivePrimaryTeal,
                    periodSelectorSelectedContainerColor = ExecutivePrimaryTeal.copy(alpha = 0.25f),
                    periodSelectorUnselectedContainerColor = ExecutiveDarkSurface,
                    periodSelectorSelectedContentColor = ExecutivePrimaryTeal,
                    periodSelectorUnselectedContentColor = ExecutiveTextSecondary,
                    timeSelectorSelectedContainerColor = ExecutivePrimaryTeal.copy(alpha = 0.25f),
                    timeSelectorUnselectedContainerColor = ExecutiveDarkSurface,
                    timeSelectorSelectedContentColor = ExecutivePrimaryTeal,
                    timeSelectorUnselectedContentColor = ExecutiveTextWhite
                )
            )
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = ExecutiveDarkSurfaceElevated
    )
}

fun parseDateStringToMillis(dateStr: String): Long {
    val clean = dateStr.trim()
    return try {
        val (d, m, y) = when {
            clean.contains("/") -> {
                val parts = clean.split("/")
                Triple(
                    parts.getOrNull(0)?.toIntOrNull() ?: 1,
                    (parts.getOrNull(1)?.toIntOrNull() ?: 1) - 1,
                    parts.getOrNull(2)?.toIntOrNull() ?: 2026
                )
            }
            clean.contains("-") -> {
                val parts = clean.split("-")
                Triple(
                    parts.getOrNull(2)?.toIntOrNull() ?: 1,
                    (parts.getOrNull(1)?.toIntOrNull() ?: 1) - 1,
                    parts.getOrNull(0)?.toIntOrNull() ?: 2026
                )
            }
            else -> {
                val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                Triple(cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH), cal.get(Calendar.YEAR))
            }
        }
        Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(Calendar.YEAR, y)
            set(Calendar.MONTH, m)
            set(Calendar.DAY_OF_MONTH, d)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    } catch (_: Exception) {
        Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
