package com.example.domain

import com.example.data.model.AppLimitEntity

enum class AppBudgetStatus {
    AVAILABLE,
    NEAR_LIMIT,
    WARNING,
    EXHAUSTED,
    BLOCKED
}

data class EvaluatedAppBudget(
    val limit: AppLimitEntity,
    val remainingMinutes: Int,
    val percentUsed: Float,
    val status: AppBudgetStatus,
    val isStrictLocked: Boolean
)

object WellbeingEngine {

    fun evaluate(limit: AppLimitEntity): EvaluatedAppBudget {
        val remaining = (limit.dailyBudgetMinutes - limit.currentUsageMinutes).coerceAtLeast(0)
        val percent = if (limit.dailyBudgetMinutes > 0) {
            (limit.currentUsageMinutes.toFloat() / limit.dailyBudgetMinutes.toFloat()).coerceIn(0f, 1f)
        } else {
            1f
        }

        val status = when {
            limit.isBlocked -> AppBudgetStatus.BLOCKED
            remaining == 0 -> AppBudgetStatus.EXHAUSTED
            remaining <= 10 -> AppBudgetStatus.NEAR_LIMIT
            else -> AppBudgetStatus.AVAILABLE
        }

        return EvaluatedAppBudget(
            limit = limit,
            remainingMinutes = remaining,
            percentUsed = percent,
            status = status,
            isStrictLocked = limit.isStrictLocked
        )
    }

    /**
     * Valida si se puede modificar una restricción en el ciclo actual.
     * En MODO ESTRICTO, no se permite aumentar el presupuesto ni eliminar la restricción
     * durante el ciclo activo.
     */
    fun canModifyRestriction(isStrictMode: Boolean, isStrictLocked: Boolean): Boolean {
        if (!isStrictMode) return true
        return !isStrictLocked
    }
}
