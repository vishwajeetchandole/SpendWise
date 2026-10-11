package com.spendwise

import com.spendwise.util.Formatters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testIndianNumberFormatting() {
        val formattedLakh = Formatters.formatCurrency(124000.0, "₹", useIndianFormat = true)
        assertEquals("₹1,24,000", formattedLakh)

        val formattedCrore = Formatters.formatCurrency(12345678.0, "₹", useIndianFormat = true)
        assertEquals("₹1,23,45,678", formattedCrore)

        val formattedStandard = Formatters.formatCurrency(124000.0, "$", useIndianFormat = false)
        assertEquals("$124,000", formattedStandard)
    }

    @Test
    fun testMonthCalculations() {
        val label = Formatters.getMonthLabel(2025, 1) // February
        assertTrue(label.contains("February 2025"))

        val start = Formatters.getStartOfMonth(2025, 1)
        val end = Formatters.getEndOfMonth(2025, 1)
        assertTrue(end > start)
    }
}
