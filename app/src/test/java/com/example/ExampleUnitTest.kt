package com.example

import com.example.util.CurrencyFormatter
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testCurrencyFormattingCop() {
        // Pruébalo con estos montos: 100.000, 40.000.000 y 1.250.000.000
        val cop100k = CurrencyFormatter.format(100_000.0, "$", "COP")
        val cop40m = CurrencyFormatter.format(40_000_000.0, "$", "COP")
        val cop1_25b = CurrencyFormatter.format(1_250_000_000.0, "$", "COP")

        assertEquals("$100.000 COP", cop100k)
        assertEquals("$40.000.000 COP", cop40m)
        assertEquals("$1.250.000.000 COP", cop1_25b)

        // Centro de la dona para COP (sin código de moneda en la línea)
        // Montos menores a 1 millón se muestran completos: "$100.000"
        assertEquals("$100.000", CurrencyFormatter.formatCenterAmount(100_000.0, "$", "COP", isSpanish = true))
        // Montos >= 1M en español: "$40 M"
        assertEquals("$40 M", CurrencyFormatter.formatCenterAmount(40_000_000.0, "$", "COP", isSpanish = true))
        // Montos >= 1M en inglés: "$40M"
        assertEquals("$40M", CurrencyFormatter.formatCenterAmount(40_000_000.0, "$", "COP", isSpanish = false))
        // Montos >= 1B: "$1,25 B" (español) / "$1.25B" (inglés)
        assertEquals("$1,25 B", CurrencyFormatter.formatCenterAmount(1_250_000_000.0, "$", "COP", isSpanish = true))
        assertEquals("$1.25B", CurrencyFormatter.formatCenterAmount(1_250_000_000.0, "$", "COP", isSpanish = false))
    }

    @Test
    fun testCurrencyFormattingUsd() {
        val usd100k = CurrencyFormatter.format(100_000.0, "$", "USD")
        assertEquals("$100,000.00 USD", usd100k)
    }

    @Test
    fun testCurrencyFormattingEur() {
        val eur100k = CurrencyFormatter.format(100_000.0, "€", "EUR")
        assertEquals("100.000,00 € EUR", eur100k)
    }
}
