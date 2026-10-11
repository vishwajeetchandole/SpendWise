package com.spendwise.data.repository

import com.spendwise.data.local.CategoryDao
import com.spendwise.data.local.ExpenseDao
import com.spendwise.data.model.CategoryEntity
import com.spendwise.data.model.DefaultCategories
import com.spendwise.data.model.ExpenseEntity
import com.spendwise.data.model.ExpenseWithCategory
import com.spendwise.util.SampleDataSeeder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar

class ExpenseRepository(
    private val categoryDao: CategoryDao,
    private val expenseDao: ExpenseDao
) {
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()

    fun getExpensesBetween(startDate: Long, endDate: Long): Flow<List<ExpenseEntity>> {
        return expenseDao.getExpensesBetween(startDate, endDate)
    }

    fun getExpensesWithCategoryBetween(startDate: Long, endDate: Long): Flow<List<ExpenseWithCategory>> {
        return combine(
            expenseDao.getExpensesBetween(startDate, endDate),
            categoryDao.getAllCategories()
        ) { expenses, categories ->
            val catMap = categories.associateBy { it.id }
            expenses.map { expense ->
                ExpenseWithCategory(
                    expense = expense,
                    category = catMap[expense.categoryId]
                )
            }
        }
    }

    fun getAllExpensesWithCategory(): Flow<List<ExpenseWithCategory>> {
        return combine(
            expenseDao.getAllExpenses(),
            categoryDao.getAllCategories()
        ) { expenses, categories ->
            val catMap = categories.associateBy { it.id }
            expenses.map { expense ->
                ExpenseWithCategory(
                    expense = expense,
                    category = catMap[expense.categoryId]
                )
            }
        }
    }

    suspend fun getExpensesBetweenSync(startDate: Long, endDate: Long): List<ExpenseEntity> {
        return expenseDao.getExpensesBetweenList(startDate, endDate)
    }

    suspend fun getAllExpensesSync(): List<ExpenseEntity> {
        return expenseDao.getAllExpensesList()
    }

    suspend fun getAllCategoriesSync(): List<CategoryEntity> {
        val list = categoryDao.getAllCategoriesList()
        return list.ifEmpty { DefaultCategories.list }
    }

    suspend fun insertExpense(expense: ExpenseEntity): Long {
        return expenseDao.insertExpense(expense)
    }

    suspend fun updateExpense(expense: ExpenseEntity) {
        expenseDao.updateExpense(expense)
    }

    suspend fun deleteExpense(expense: ExpenseEntity) {
        expenseDao.deleteExpense(expense)
    }

    suspend fun deleteExpenseById(id: Long) {
        expenseDao.deleteExpenseById(id)
    }

    suspend fun insertCategory(category: CategoryEntity): Long {
        return categoryDao.insertCategory(category)
    }

    suspend fun updateCategory(category: CategoryEntity) {
        categoryDao.updateCategory(category)
    }

    suspend fun deleteCategory(category: CategoryEntity) {
        categoryDao.deleteCategory(category)
    }

    suspend fun deleteCategoryById(id: Long) {
        categoryDao.deleteCategoryById(id)
    }

    suspend fun clearAllExpenses() {
        expenseDao.clearAllExpenses()
    }

    suspend fun seedDefaultCategoriesIfEmpty() {
        val count = categoryDao.getCategoryCount()
        if (count == 0) {
            categoryDao.insertCategories(DefaultCategories.list)
        }
    }

    suspend fun seedSampleData() {
        seedDefaultCategoriesIfEmpty()
        val sample = SampleDataSeeder.generateSampleExpenses()
        expenseDao.insertExpenses(sample)
    }

    suspend fun checkAndGenerateRecurringExpenses(targetYear: Int, targetMonth: Int) {
        val recurring = expenseDao.getRecurringExpenses()
        if (recurring.isEmpty()) return

        // Check if recurring items already exist in target month
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, targetYear)
            set(Calendar.MONTH, targetMonth)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val monthStart = cal.timeInMillis
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val monthEnd = cal.timeInMillis

        val currentMonthExpenses = expenseDao.getExpensesBetweenList(monthStart, monthEnd)
        val currentNames = currentMonthExpenses.map { it.name.trim().lowercase() }.toSet()

        for (rec in recurring) {
            if (!currentNames.contains(rec.name.trim().lowercase())) {
                // Create recurring instance on the 1st of this month
                val newDate = Calendar.getInstance().apply {
                    set(Calendar.YEAR, targetYear)
                    set(Calendar.MONTH, targetMonth)
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 9)
                    set(Calendar.MINUTE, 0)
                }.timeInMillis

                val newExpense = rec.copy(
                    id = 0,
                    dateMillis = newDate,
                    createdAt = System.currentTimeMillis()
                )
                expenseDao.insertExpense(newExpense)
            }
        }
    }

    suspend fun restoreDatabase(categories: List<CategoryEntity>, expenses: List<ExpenseEntity>) {
        if (categories.isNotEmpty()) {
            categoryDao.insertCategories(categories)
        }
        if (expenses.isNotEmpty()) {
            expenseDao.insertExpenses(expenses)
        }
    }
}
