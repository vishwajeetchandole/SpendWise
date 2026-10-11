package com.spendwise.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object Formatters {

    fun formatCurrency(
        amount: Double,
        currencySymbol: String = "₹",
        useIndianFormat: Boolean = true,
        includeDecimals: Boolean = false,
        hideAmounts: Boolean = false
    ): String {
        if (hideAmounts) {
            return "$currencySymbol••••"
        }
        val isNegative = amount < 0
        val absAmount = Math.abs(amount)

        val formattedNumber = if (useIndianFormat) {
            formatIndianNumber(absAmount, includeDecimals)
        } else {
            val pattern = if (includeDecimals) "#,##0.00" else "#,##0"
            val symbols = DecimalFormatSymbols(Locale.US)
            DecimalFormat(pattern, symbols).format(absAmount)
        }

        val prefix = if (isNegative) "-$currencySymbol" else currencySymbol
        return "$prefix$formattedNumber"
    }

    /**
     * Formats numbers in Indian grouping (Lakhs & Crores):
     * e.g., 1234567 -> 12,34,567
     */
    private fun formatIndianNumber(value: Double, includeDecimals: Boolean): String {
        val longVal = value.toLong()
        val decimalPart = if (includeDecimals) {
            val dec = Math.round((value - longVal) * 100)
            if (dec > 0) ".${String.format(Locale.US, "%02d", dec)}" else ".00"
        } else ""

        val s = longVal.toString()
        if (s.length <= 3) {
            return s + decimalPart
        }

        val lastThree = s.substring(s.length - 3)
        val remaining = s.substring(0, s.length - 3)

        val sb = StringBuilder()
        var count = 0
        for (i in remaining.length - 1 downTo 0) {
            sb.append(remaining[i])
            count++
            if (count == 2 && i != 0) {
                sb.append(',')
                count = 0
            }
        }
        val firstPart = sb.reverse().toString()
        return "$firstPart,$lastThree$decimalPart"
    }

    fun formatCompactCurrency(
        amount: Double,
        currencySymbol: String = "₹",
        useIndianFormat: Boolean = true
    ): String {
        val absAmount = Math.abs(amount)
        return when {
            useIndianFormat && absAmount >= 1_00_00_000 -> {
                "$currencySymbol${String.format(Locale.US, "%.1fCr", absAmount / 1_00_00_000.0)}"
            }
            useIndianFormat && absAmount >= 1_00_000 -> {
                "$currencySymbol${String.format(Locale.US, "%.1fL", absAmount / 1_00_000.0)}"
            }
            absAmount >= 1_000_000 -> {
                "$currencySymbol${String.format(Locale.US, "%.1fM", absAmount / 1_000_000.0)}"
            }
            absAmount >= 1_000 -> {
                "$currencySymbol${String.format(Locale.US, "%.1fk", absAmount / 1_000.0)}"
            }
            else -> formatCurrency(amount, currencySymbol, useIndianFormat, includeDecimals = false)
        }
    }

    fun formatDate(millis: Long, pattern: String = "dd MMM yyyy"): String {
        return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(millis))
    }

    fun formatShortDate(millis: Long): String {
        return SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(millis))
    }

    fun formatFullDate(millis: Long): String {
        return SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(Date(millis))
    }

    fun formatDateHeader(millis: Long): String {
        return formatDayHeader(millis)
    }


    fun formatDayHeader(millis: Long): String {
        val calNow = Calendar.getInstance()
        val calItem = Calendar.getInstance().apply { timeInMillis = millis }

        val isToday = calNow.get(Calendar.YEAR) == calItem.get(Calendar.YEAR) &&
                calNow.get(Calendar.DAY_OF_YEAR) == calItem.get(Calendar.DAY_OF_YEAR)

        calNow.add(Calendar.DAY_OF_YEAR, -1)
        val isYesterday = calNow.get(Calendar.YEAR) == calItem.get(Calendar.YEAR) &&
                calNow.get(Calendar.DAY_OF_YEAR) == calItem.get(Calendar.DAY_OF_YEAR)

        return when {
            isToday -> "Today • " + SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(millis))
            isYesterday -> "Yesterday • " + SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(millis))
            else -> SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(Date(millis))
        }
    }

    fun getMonthLabel(year: Int, month: Int): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        return SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
    }

    fun getStartOfMonth(year: Int, month: Int): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun getEndOfMonth(year: Int, month: Int): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            val maxDay = getActualMaximum(Calendar.DAY_OF_MONTH)
            set(Calendar.DAY_OF_MONTH, maxDay)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return cal.timeInMillis
    }
}
