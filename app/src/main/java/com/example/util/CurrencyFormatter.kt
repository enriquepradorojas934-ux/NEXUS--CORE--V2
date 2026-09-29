package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Formateador centralizado y reutilizable de moneda para LifeOS.
 * Lee la divisa seleccionada por el usuario (símbolo y código) y formatea los montos numéricos.
 * NO aplica conversiones cambiarias (el valor numérico se preserva íntegro).
 * Garantiza que en todo lugar se muestre el código de moneda (COP, USD, EUR, MXN, etc.)
 * y el formato numérico correspondiente con los separadores de miles y decimales exactos.
 */
object CurrencyFormatter {

    /**
     * Determina si la moneda normalmente utiliza decimales (centavos).
     * Monedas como COP, CLP, JPY, KRW no usan decimales en la vida cotidiana.
     */
    fun usesDecimals(currencyCode: String): Boolean {
        val code = currencyCode.trim().uppercase()
        return code !in setOf("COP", "CLP", "JPY", "KRW", "PYG", "VND", "HUF", "IDR")
    }

    /**
     * Obtiene los símbolos de agrupación de miles y decimales según la divisa seleccionada:
     * - COP, EUR, CLP, ARS, BRL usan punto '.' para miles y coma ',' para decimales.
     * - USD, MXN, GBP, CAD usan coma ',' para miles y punto '.' para decimales.
     */
    fun getSymbols(currencyCode: String): DecimalFormatSymbols {
        val code = currencyCode.trim().uppercase()
        val symbols = DecimalFormatSymbols(Locale.US)
        when (code) {
            "COP", "EUR", "CLP", "ARS", "BRL" -> {
                symbols.groupingSeparator = '.'
                symbols.decimalSeparator = ','
            }
            else -> {
                symbols.groupingSeparator = ','
                symbols.decimalSeparator = '.'
            }
        }
        return symbols
    }

    /**
     * Formatea un monto completo con código de divisa (ej: "$100.000 COP", "$40.000.000 COP", "$1.250.000.000 COP").
     */
    fun format(
        amount: Double,
        symbol: String = "$",
        code: String = "USD",
        includePlus: Boolean = false,
        includeDecimals: Boolean? = null
    ): String {
        val sign = if (includePlus && amount > 0) "+" else if (amount < 0) "-" else ""
        val absAmount = kotlin.math.abs(amount)
        val cleanCode = code.trim().uppercase().ifBlank { "USD" }
        val cleanSymbol = symbol.trim().ifBlank { "$" }

        val showDecimals = includeDecimals ?: usesDecimals(cleanCode)
        val symbols = getSymbols(cleanCode)

        val pattern = if (showDecimals) "#,##0.00" else "#,##0"
        val df = DecimalFormat(pattern, symbols)
        val formattedNumber = df.format(absAmount)

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
        includeDecimals: Boolean? = null
    ): String {
        return format(amount, symbol, code, includePlus, includeDecimals)
    }

    /**
     * Formatea únicamente el monto y símbolo SIN el código al final (ej: "$100.000", "$40.000.000").
     */
    fun formatNumberOnly(
        amount: Double,
        symbol: String = "$",
        code: String = "USD",
        includeDecimals: Boolean? = null
    ): String {
        val sign = if (amount < 0) "-" else ""
        val absAmount = kotlin.math.abs(amount)
        val cleanCode = code.trim().uppercase().ifBlank { "USD" }
        val cleanSymbol = symbol.trim().ifBlank { "$" }

        val showDecimals = includeDecimals ?: usesDecimals(cleanCode)
        val symbols = getSymbols(cleanCode)

        val pattern = if (showDecimals) "#,##0.00" else "#,##0"
        val df = DecimalFormat(pattern, symbols)
        val formattedNumber = df.format(absAmount)

        return if (cleanCode == "EUR") {
            "$sign$formattedNumber €"
        } else {
            "$sign$cleanSymbol$formattedNumber"
        }
    }

    fun formatWithoutCode(
        amount: Double,
        symbol: String = "$",
        code: String = "USD",
        includeDecimals: Boolean? = null
    ): String {
        return formatNumberOnly(amount, symbol, code, includeDecimals)
    }

    /**
     * Formateo optimizado para el centro de la dona de Patrimonio.
     * Si el monto es >= 1 millón, muestra formato compacto según el idioma (ej: "$40 M" en español, "$40M" en inglés).
     * Si es >= 1 billón (mil millones), muestra formato compacto (ej: "$1,25 B" en español, "$1.25B" en inglés).
     * Si es menor a 1 millón, se muestra completo (ej: "$100.000").
     * El código de la moneda NO va en esta cadena (va en su propia línea).
     */
    fun formatCenterAmount(
        amount: Double,
        symbol: String = "$",
        code: String = "USD",
        isSpanish: Boolean = true
    ): String {
        val absAmount = kotlin.math.abs(amount)
        val cleanCode = code.trim().uppercase().ifBlank { "USD" }
        val cleanSymbol = symbol.trim().ifBlank { "$" }
        val decimalSeparator = if (isSpanish) "," else "."

        return when {
            absAmount >= 1_000_000_000.0 -> {
                val billions = absAmount / 1_000_000_000.0
                val formatted = if (billions % 1.0 == 0.0) {
                    String.format(Locale.US, "%.0f", billions)
                } else {
                    val dec = String.format(Locale.US, "%.2f", billions).trimEnd('0').trimEnd('.')
                    dec.replace(".", decimalSeparator)
                }
                if (cleanCode == "EUR") {
                    if (isSpanish) "$formatted B €" else "${formatted}B €"
                } else {
                    if (isSpanish) "$cleanSymbol$formatted B" else "$cleanSymbol${formatted}B"
                }
            }
            absAmount >= 1_000_000.0 -> {
                val millions = absAmount / 1_000_000.0
                val formatted = if (millions % 1.0 == 0.0) {
                    String.format(Locale.US, "%.0f", millions)
                } else {
                    val dec = String.format(Locale.US, "%.1f", millions).trimEnd('0').trimEnd('.')
                    dec.replace(".", decimalSeparator)
                }
                if (cleanCode == "EUR") {
                    if (isSpanish) "$formatted M €" else "${formatted}M €"
                } else {
                    if (isSpanish) "$cleanSymbol$formatted M" else "$cleanSymbol${formatted}M"
                }
            }
            else -> {
                formatNumberOnly(amount, symbol, code)
            }
        }
    }

    fun formatCompactWithoutCode(
        amount: Double,
        symbol: String = "$",
        code: String = "USD",
        isSpanish: Boolean = true
    ): String {
        return formatCenterAmount(amount, symbol, code, isSpanish)
    }
}
