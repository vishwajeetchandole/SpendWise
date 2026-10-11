package com.spendwise.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.spendwise.data.model.CategoryEntity
import com.spendwise.data.model.ExpenseEntity
import com.spendwise.data.model.ExpenseWithCategory
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportHelper {

    fun exportToPdf(
        context: Context,
        title: String,
        subTitle: String,
        expensesWithCategory: List<ExpenseWithCategory>,
        categories: List<CategoryEntity>,
        currencySymbol: String,
        useIndianFormat: Boolean,
        userName: String = "User",
        userPhotoPath: String? = null
    ): File? {
        try {
            val pdfDoc = PdfDocument()
            val pageWidth = 595 // Standard A4 width at 72 dpi
            val pageHeight = 842 // Standard A4 height at 72 dpi

            var pageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = pdfDoc.startPage(pageInfo)
            var canvas = page.canvas

            val titlePaint = Paint().apply {
                color = Color.rgb(11, 79, 122)
                textSize = 19f
                isFakeBoldText = true
                isAntiAlias = true
            }

            val subTitlePaint = Paint().apply {
                color = Color.rgb(71, 85, 105)
                textSize = 11f
                isAntiAlias = true
            }

            val headerBgPaint = Paint().apply {
                color = Color.rgb(241, 245, 249)
            }

            val tableHeaderPaint = Paint().apply {
                color = Color.rgb(30, 41, 59)
                textSize = 10f
                isFakeBoldText = true
                isAntiAlias = true
            }

            val textPaint = Paint().apply {
                color = Color.rgb(15, 23, 42)
                textSize = 9.5f
                isAntiAlias = true
            }

            val textBoldPaint = Paint().apply {
                color = Color.rgb(15, 23, 42)
                textSize = 9.5f
                isFakeBoldText = true
                isAntiAlias = true
            }

            val textMutedPaint = Paint().apply {
                color = Color.rgb(100, 116, 139)
                textSize = 8.5f
                isAntiAlias = true
            }

            val linePaint = Paint().apply {
                color = Color.rgb(226, 232, 240)
                strokeWidth = 1f
            }

            val cardBgPaint = Paint().apply {
                color = Color.rgb(248, 250, 252)
            }

            var y = 45f

            // App Header with user's full name
            val headerTitle = if (userName.isNotBlank()) "Expense Report for $userName" else "SpendWise Expense Report"
            canvas.drawText(headerTitle, 40f, y, titlePaint)
            val genDate = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
            canvas.drawText("Generated on: $genDate", pageWidth - 200f, y - 4f, textMutedPaint)
            y += 18f
            canvas.drawText("Period: $subTitle • Prepared for ${userName.ifBlank { "User" }}", 40f, y, subTitlePaint)
            y += 15f
            canvas.drawLine(40f, y, pageWidth - 40f, y, linePaint)
            y += 20f

            // KPI Summary Box (No monthly budget/savings/income)
            val totalSpent = expensesWithCategory.sumOf { it.expense.amount }
            val count = expensesWithCategory.size
            val topCategory = expensesWithCategory
                .groupBy { it.category?.name ?: "Other" }
                .maxByOrNull { entry -> entry.value.sumOf { it.expense.amount } }?.key ?: "N/A"
            val highestExpense = expensesWithCategory.maxOfOrNull { it.expense.amount } ?: 0.0

            // Draw Summary Card
            canvas.drawRoundRect(40f, y, pageWidth - 40f, y + 60f, 8f, 8f, cardBgPaint)

            val colW = (pageWidth - 80f) / 4f
            // Col 1: Total Spent
            canvas.drawText("TOTAL SPENT", 55f, y + 22f, textMutedPaint)
            canvas.drawText(
                Formatters.formatCurrency(totalSpent, currencySymbol, useIndianFormat),
                55f,
                y + 44f,
                titlePaint
            )

            // Col 2: Number of Expenses
            canvas.drawText("EXPENSES", 55f + colW, y + 22f, textMutedPaint)
            canvas.drawText("$count items", 55f + colW, y + 44f, Paint(textBoldPaint).apply { textSize = 13f })

            // Col 3: Top Category
            canvas.drawText("TOP CATEGORY", 55f + colW * 2, y + 22f, textMutedPaint)
            canvas.drawText(topCategory.take(15), 55f + colW * 2, y + 44f, Paint(textBoldPaint).apply { textSize = 11f })

            // Col 4: Highest Single Expense
            canvas.drawText("HIGHEST EXPENSE", 55f + colW * 3, y + 22f, textMutedPaint)
            canvas.drawText(
                Formatters.formatCurrency(highestExpense, currencySymbol, useIndianFormat),
                55f + colW * 3,
                y + 44f,
                Paint(textBoldPaint).apply { textSize = 12f }
            )

            y += 85f

            // Section: Category Breakdown
            canvas.drawText("Category Breakdown", 40f, y, Paint(textBoldPaint).apply { textSize = 12f })
            y += 12f

            val categoryGroups = expensesWithCategory
                .groupBy { it.category ?: CategoryEntity(0, "Uncategorized", "category", "#64748B") }
                .toList()
                .sortedByDescending { it.second.sumOf { e -> e.expense.amount } }

            // Table headers for Category
            canvas.drawRect(40f, y, pageWidth - 40f, y + 20f, headerBgPaint)
            canvas.drawText("Category", 50f, y + 14f, tableHeaderPaint)
            canvas.drawText("Count", 220f, y + 14f, tableHeaderPaint)
            canvas.drawText("% Share", 320f, y + 14f, tableHeaderPaint)
            canvas.drawText("Total Amount", pageWidth - 120f, y + 14f, tableHeaderPaint)
            y += 24f

            for ((cat, items) in categoryGroups.take(8)) {
                val catTotal = items.sumOf { it.expense.amount }
                val share = if (totalSpent > 0) (catTotal / totalSpent * 100) else 0.0

                canvas.drawText(cat.name, 50f, y + 12f, textPaint)
                canvas.drawText("${items.size}", 220f, y + 12f, textMutedPaint)
                canvas.drawText(String.format(Locale.US, "%.1f%%", share), 320f, y + 12f, textMutedPaint)
                canvas.drawText(
                    Formatters.formatCurrency(catTotal, currencySymbol, useIndianFormat),
                    pageWidth - 120f,
                    y + 12f,
                    textBoldPaint
                )
                y += 18f
            }

            y += 15f

            // Section: Itemized Expenses Table
            canvas.drawText("Detailed Expense List", 40f, y, Paint(textBoldPaint).apply { textSize = 12f })
            y += 12f

            fun drawExpenseTableHeader(c: Canvas, currY: Float) {
                c.drawRect(40f, currY, pageWidth - 40f, currY + 20f, headerBgPaint)
                c.drawText("Date", 50f, currY + 14f, tableHeaderPaint)
                c.drawText("Expense Name & Note", 130f, currY + 14f, tableHeaderPaint)
                c.drawText("Category", 320f, currY + 14f, tableHeaderPaint)
                c.drawText("Amount", pageWidth - 110f, currY + 14f, tableHeaderPaint)
            }

            drawExpenseTableHeader(canvas, y)
            y += 24f

            val altRowPaint = Paint().apply { color = Color.rgb(250, 250, 250) }

            for ((index, item) in expensesWithCategory.withIndex()) {
                if (y > pageHeight - 60f) {
                    canvas.drawText("Page $pageNumber", pageWidth / 2f - 15f, pageHeight - 25f, textMutedPaint)
                    pdfDoc.finishPage(page)

                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = pdfDoc.startPage(pageInfo)
                    canvas = page.canvas
                    y = 40f
                    drawExpenseTableHeader(canvas, y)
                    y += 24f
                }

                if (index % 2 == 1) {
                    canvas.drawRect(40f, y - 2f, pageWidth - 40f, y + 18f, altRowPaint)
                }

                val dateStr = Formatters.formatDate(item.expense.dateMillis, "dd MMM yyyy")
                canvas.drawText(dateStr, 50f, y + 12f, textMutedPaint)

                val name = item.expense.name.take(28)
                canvas.drawText(name, 130f, y + 12f, textPaint)

                val catName = (item.category?.name ?: "Other").take(18)
                canvas.drawText(catName, 320f, y + 12f, textMutedPaint)

                val amtStr = Formatters.formatCurrency(item.expense.amount, currencySymbol, useIndianFormat)
                canvas.drawText(amtStr, pageWidth - 110f, y + 12f, textBoldPaint)

                y += 20f
            }

            if (y > pageHeight - 50f) {
                canvas.drawText("Page $pageNumber", pageWidth / 2f - 15f, pageHeight - 25f, textMutedPaint)
                pdfDoc.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDoc.startPage(pageInfo)
                canvas = page.canvas
                y = 40f
            }

            y += 10f
            canvas.drawLine(40f, y, pageWidth - 40f, y, linePaint)
            y += 18f
            canvas.drawText(
                "Grand Total: ${Formatters.formatCurrency(totalSpent, currencySymbol, useIndianFormat)} ($count expenses) • Account: ${userName.ifBlank { "User" }}",
                40f,
                y,
                Paint(textBoldPaint).apply { textSize = 11f }
            )
            canvas.drawText("Page $pageNumber", pageWidth / 2f - 15f, pageHeight - 25f, textMutedPaint)
            pdfDoc.finishPage(page)

            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) exportDir.mkdirs()

            val sanitizedTitle = title.replace(Regex("[^a-zA-Z0-9_]"), "_")
            val pdfFile = File(exportDir, "SpendWise_${sanitizedTitle}.pdf")
            FileOutputStream(pdfFile).use { outputStream ->
                pdfDoc.writeTo(outputStream)
            }
            pdfDoc.close()

            return pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun exportToCsv(
        context: Context,
        title: String,
        expensesWithCategory: List<ExpenseWithCategory>,
        currencySymbol: String,
        userName: String = "User"
    ): File? {
        try {
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) exportDir.mkdirs()

            val sanitizedTitle = title.replace(Regex("[^a-zA-Z0-9_]"), "_")
            val csvFile = File(exportDir, "SpendWise_${sanitizedTitle}.csv")

            val sb = StringBuilder()
            sb.append("Account Holder,ID,Date,Expense Name,Category,Amount ($currencySymbol),Note,Recurring,Created At\n")

            val escapedUser = escapeCsv(userName.ifBlank { "User" })
            for (item in expensesWithCategory) {
                val dateStr = Formatters.formatDate(item.expense.dateMillis, "yyyy-MM-dd")
                val name = escapeCsv(item.expense.name)
                val cat = escapeCsv(item.category?.name ?: "Other")
                val note = escapeCsv(item.expense.note)
                val recurring = if (item.expense.recurring) "Yes" else "No"
                val createdStr = Formatters.formatDate(item.expense.createdAt, "yyyy-MM-dd HH:mm:ss")

                sb.append("$escapedUser,${item.expense.id},$dateStr,$name,$cat,${item.expense.amount},$note,$recurring,$createdStr\n")
            }

            csvFile.writeText(sb.toString())
            return csvFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun exportToJson(
        context: Context,
        expenses: List<ExpenseEntity>,
        categories: List<CategoryEntity>
    ): File? {
        try {
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) exportDir.mkdirs()

            val rootJson = JSONObject()
            rootJson.put("version", 2)
            rootJson.put("appName", "SpendWise")
            rootJson.put("exportedAt", System.currentTimeMillis())

            val catArray = JSONArray()
            for (c in categories) {
                val cObj = JSONObject()
                cObj.put("id", c.id)
                cObj.put("name", c.name)
                cObj.put("iconName", c.iconName)
                cObj.put("colorHex", c.colorHex)
                cObj.put("isDefault", c.isDefault)
                catArray.put(cObj)
            }
            rootJson.put("categories", catArray)

            val expArray = JSONArray()
            for (e in expenses) {
                val eObj = JSONObject()
                eObj.put("id", e.id)
                eObj.put("name", e.name)
                eObj.put("amount", e.amount)
                eObj.put("dateMillis", e.dateMillis)
                eObj.put("categoryId", e.categoryId)
                eObj.put("note", e.note)
                eObj.put("recurring", e.recurring)
                eObj.put("createdAt", e.createdAt)
                expArray.put(eObj)
            }
            rootJson.put("expenses", expArray)

            val jsonFile = File(exportDir, "SpendWise_Backup_${System.currentTimeMillis()}.json")
            jsonFile.writeText(rootJson.toString(2))
            return jsonFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun parseBackupJson(jsonString: String): Pair<List<CategoryEntity>, List<ExpenseEntity>>? {
        return try {
            val root = JSONObject(jsonString)
            val categories = mutableListOf<CategoryEntity>()
            val expenses = mutableListOf<ExpenseEntity>()

            if (root.has("categories")) {
                val catArr = root.getJSONArray("categories")
                for (i in 0 until catArr.length()) {
                    val obj = catArr.getJSONObject(i)
                    categories.add(
                        CategoryEntity(
                            id = obj.optLong("id", 0L),
                            name = obj.getString("name"),
                            iconName = obj.optString("iconName", "category"),
                            colorHex = obj.optString("colorHex", "#64748B"),
                            budget = null,
                            isDefault = obj.optBoolean("isDefault", false)
                        )
                    )
                }
            }

            if (root.has("expenses")) {
                val expArr = root.getJSONArray("expenses")
                for (i in 0 until expArr.length()) {
                    val obj = expArr.getJSONObject(i)
                    expenses.add(
                        ExpenseEntity(
                            id = obj.optLong("id", 0L),
                            name = obj.getString("name"),
                            amount = obj.getDouble("amount"),
                            dateMillis = obj.getLong("dateMillis"),
                            categoryId = obj.getLong("categoryId"),
                            note = obj.optString("note", ""),
                            recurring = obj.optBoolean("recurring", false),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            }

            Pair(categories, expenses)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun shareFile(context: Context, file: File, mimeType: String, chooserTitle: String = "Share SpendWise Report") {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, file.name)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, chooserTitle))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Could not open share sheet: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }
}
