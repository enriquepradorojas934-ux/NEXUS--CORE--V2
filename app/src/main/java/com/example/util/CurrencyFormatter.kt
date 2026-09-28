package com.example.util

import java.util.Locale

/**
 * Formateador centralizado y reutilizable de moneda para LifeOS.
 * Lee la divisa seleccionada por el usuario (símbolo y código) y formatea los montos numéricos.
 * NO aplica conversiones cambiarias (el valor numérico se preserva íntegro).
 */
object CurrencyFormatter {

    fun format(
        amount: Double,
        symbol: String = "$",
        code: String = "USD",
        includePlus: Boolean = false,
        includeDecimals: Boolean = true
    ): String {
        val sign = if (includePlus && amount > 0) "+" else if (amount < 0) "-" else ""
        val absAmount = kotlin.math.abs(amount)
        val formattedNumber = if (includeDecimals) {
            String.format(Locale.US, "%,.2f", absAmount)
        } else {
            String.format(Locale.US, "%,.0f", absAmount)
        }
        return "$sign$symbol$formattedNumber"
    }

    fun formatWithCode(
        amount: Double,
        symbol: String = "$",
        code: String = "USD",
        includePlus: Boolean = false,
        includeDecimals: Boolean = true
    ): String {
        val base = format(amount, symbol, code, includePlus, includeDecimals)
        return "$base $code"
    }
}
