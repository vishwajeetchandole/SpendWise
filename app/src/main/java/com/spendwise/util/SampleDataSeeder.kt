package com.spendwise.util

import com.spendwise.data.model.ExpenseEntity
import java.util.Calendar

object SampleDataSeeder {

    fun generateSampleExpenses(): List<ExpenseEntity> {
        val expenses = mutableListOf<ExpenseEntity>()
        val cal = Calendar.getInstance()
        val currentYear = cal.get(Calendar.YEAR)
        val currentMonth = cal.get(Calendar.MONTH)

        fun createCal(year: Int, month: Int, day: Int, hour: Int = 14): Long {
            return Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, day)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, 30)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }

        // Current Month sample expenses
        expenses.add(
            ExpenseEntity(
                name = "Apartment Rent",
                amount = 14500.0,
                dateMillis = createCal(currentYear, currentMonth, 1),
                categoryId = 8, // Rent & Housing
                note = "Monthly apartment rent paid online",
                recurring = true
            )
        )
        expenses.add(
            ExpenseEntity(
                name = "Supermarket Groceries",
                amount = 3850.0,
                dateMillis = createCal(currentYear, currentMonth, 2),
                categoryId = 9, // Groceries
                note = "Weekly fresh fruits, veggies, milk and supplies",
                recurring = false
            )
        )
        expenses.add(
            ExpenseEntity(
                name = "Electricity & Water Bill",
                amount = 2150.0,
                dateMillis = createCal(currentYear, currentMonth, 3),
                categoryId = 3, // Bills & Utilities
                note = "State power utility bill",
                recurring = true
            )
        )
        expenses.add(
            ExpenseEntity(
                name = "Fuel Refill",
                amount = 1800.0,
                dateMillis = createCal(currentYear, currentMonth, 4),
                categoryId = 2, // Transport
                note = "Shell petrol station",
                recurring = false
            )
        )
        expenses.add(
            ExpenseEntity(
                name = "Weekend Bistro Dinner",
                amount = 2450.0,
                dateMillis = createCal(currentYear, currentMonth, 5),
                categoryId = 1, // Food & Dining
                note = "Dinner with friends at Olive Bistro",
                recurring = false
            )
        )
        expenses.add(
            ExpenseEntity(
                name = "New Running Shoes",
                amount = 4200.0,
                dateMillis = createCal(currentYear, currentMonth, 6),
                categoryId = 4, // Shopping
                note = "Nike trail sneakers sale",
                recurring = false
            )
        )
        expenses.add(
            ExpenseEntity(
                name = "Fiber High-Speed Internet",
                amount = 1199.0,
                dateMillis = createCal(currentYear, currentMonth, 7),
                categoryId = 3, // Bills
                note = "300 Mbps unlimited plan",
                recurring = true
            )
        )
        expenses.add(
            ExpenseEntity(
                name = "Cinema & Snacks",
                amount = 950.0,
                dateMillis = createCal(currentYear, currentMonth, 8),
                categoryId = 6, // Entertainment
                note = "IMAX 3D tickets and popcorn",
                recurring = false
            )
        )
        expenses.add(
            ExpenseEntity(
                name = "Pharmacy & Vitamins",
                amount = 1250.0,
                dateMillis = createCal(currentYear, currentMonth, 9),
                categoryId = 5, // Healthcare
                note = "Daily multivitamins and first aid",
                recurring = false
            )
        )
        expenses.add(
            ExpenseEntity(
                name = "Online Tech Course",
                amount = 1999.0,
                dateMillis = createCal(currentYear, currentMonth, 10),
                categoryId = 7, // Education
                note = "Kotlin Multiplatform certification",
                recurring = false
            )
        )

        // Previous Month sample expenses (for realistic MoM comparison)
        val prevMonthCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth)
            add(Calendar.MONTH, -1)
        }
        val prevYear = prevMonthCal.get(Calendar.YEAR)
        val prevMonth = prevMonthCal.get(Calendar.MONTH)

        expenses.add(
            ExpenseEntity(
                name = "Apartment Rent",
                amount = 14500.0,
                dateMillis = createCal(prevYear, prevMonth, 1),
                categoryId = 8,
                note = "Rent for previous month",
                recurring = true
            )
        )
        expenses.add(
            ExpenseEntity(
                name = "Organic Supermarket",
                amount = 4120.0,
                dateMillis = createCal(prevYear, prevMonth, 8),
                categoryId = 9,
                note = "Bulk pantry stocking",
                recurring = false
            )
        )
        expenses.add(
            ExpenseEntity(
                name = "Metro Pass & Cabs",
                amount = 2300.0,
                dateMillis = createCal(prevYear, prevMonth, 14),
                categoryId = 2,
                note = "Monthly commuter transit pass",
                recurring = false
            )
        )
        expenses.add(
            ExpenseEntity(
                name = "Electricity Bill",
                amount = 1950.0,
                dateMillis = createCal(prevYear, prevMonth, 18),
                categoryId = 3,
                note = "Summer AC usage",
                recurring = true
            )
        )
        expenses.add(
            ExpenseEntity(
                name = "Gourmet Italian Dinner",
                amount = 3100.0,
                dateMillis = createCal(prevYear, prevMonth, 22),
                categoryId = 1,
                note = "Birthday celebration",
                recurring = false
            )
        )

        return expenses
    }
}
