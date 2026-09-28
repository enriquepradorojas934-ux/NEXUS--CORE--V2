package com.example.util

import java.util.Locale

/**
 * Formateador centralizado y reutilizable de moneda para LifeOS.
 * Lee la divisa seleccionada por el usuario (símbolo y código) y formatea los montos numéricos.
 * NO aplica conversiones cambiarias (el valor numérico se preserva íntegro).
 * Garantiza que en todo lugar se muestre el código de moneda (COP, USD, EUR, MXN, etc.)
 * y el formato numérico correspondiente.
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
        val cleanCode = code.trim().uppercase().ifBlank { "USD" }
        val cleanSymbol = symbol.trim().ifBlank { "$" }

        val locale = when (cleanCode) {
            "EUR", "BRL" -> Locale.GERMANY
            else -> Locale.US
        }

        val formattedNumber = if (includeDecimals) {
            String.format(locale, "%,.2f", absAmount)
        } else {
            String.format(locale, "%,.0f", absAmount)
        }

        return if (cleanCode == "EUR") {
            "$sign$formattedNumber € EUR"
        } else {
            "$sign$cleanSymbol$formattedNumber $cleanCode"
        }
    }

    fun formatWithCode(
        amount: Double,
        symbol: String = "$",
        code: String = "USD",
        includePlus: Boolean = false,
        includeDecimals: Boolean = true
    ): String {
        return format(amount, symbol, code, includePlus, includeDecimals)
    }
}

