package com.spendwise.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spendwise.data.local.AppDatabase
import com.spendwise.data.model.CategoryEntity
import com.spendwise.data.model.ExpenseEntity
import com.spendwise.data.model.ExpenseWithCategory
import com.spendwise.data.preferences.AppSettings
import com.spendwise.data.preferences.SettingsManager
import com.spendwise.data.repository.ExpenseRepository
import com.spendwise.ui.components.CategorySpendItem
import com.spendwise.ui.components.DaySpendItem
import com.spendwise.ui.components.ExportFormat
import com.spendwise.ui.components.ExportScope
import com.spendwise.ui.components.MonthSummary
import com.spendwise.ui.components.MonthTrendItem
import com.spendwise.util.ExportHelper
import com.spendwise.util.Formatters
import com.spendwise.util.SoundAndNotificationHelper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class HistorySortOption {
    DATE_DESC,
    DATE_ASC,
    AMOUNT_DESC,
    AMOUNT_ASC
}

data class DashboardState(
    val totalSpent: Double = 0.0,
    val expenseCount: Int = 0,
    val dailyAverage: Double = 0.0,
    val highestExpense: Double = 0.0,
    val highestExpenseName: String = "",
    val categoryBreakdown: List<CategorySpendItem> = emptyList(),
    val dailySpending: List<DaySpendItem> = emptyList(),
    val sixMonthTrend: List<MonthTrendItem> = emptyList(),
    val comparisonMessage: String = "",
    val isMoreThanPreviousMonth: Boolean = false,
    val recentExpenses: List<ExpenseWithCategory> = emptyList()
)

data class InsightsState(
    val topCategories: List<CategorySpendItem> = emptyList(),
    val biggestExpense: ExpenseWithCategory? = null,
    val mostExpensiveWeekday: String = "N/A",
    val weekdaySpendPercent: Int = 0,
    val activeDaysCount: Int = 0
)

class SpendWiseViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = ExpenseRepository(db.categoryDao(), db.expenseDao())
    val settingsManager = SettingsManager(application)

    val settings: StateFlow<AppSettings> = settingsManager.settings

    // App Lock State: locked by default on cold start for returning users
    private val _isLocked = MutableStateFlow(true)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private var lastBackgroundTimestamp: Long? = null

    // Selected Month & Year
    private val cal = Calendar.getInstance()
    private val _selectedYear = MutableStateFlow(cal.get(Calendar.YEAR))
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _selectedMonth = MutableStateFlow(cal.get(Calendar.MONTH))
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    // Categories
    val categories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All expenses
    val allExpensesWithCategory: StateFlow<List<ExpenseWithCategory>> = repository.getAllExpensesWithCategory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // History Filters & Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<Long?>(null)
    val selectedCategoryFilter: StateFlow<Long?> = _selectedCategoryFilter.asStateFlow()

    private val _dateRangeFilter = MutableStateFlow<Pair<Long?, Long?>>(null to null)
    val dateRangeFilter: StateFlow<Pair<Long?, Long?>> = _dateRangeFilter.asStateFlow()

    private val _sortOption = MutableStateFlow(HistorySortOption.DATE_DESC)
    val sortOption: StateFlow<HistorySortOption> = _sortOption.asStateFlow()

    // Undo buffer
    private var lastDeletedExpense: ExpenseEntity? = null
    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.seedDefaultCategoriesIfEmpty()

            // Wait for DataStore settings to load to configure initial lock & reminder state
            val loadedSettings = settingsManager.settingsFlow.first()
            if (loadedSettings.isFirstTimeUser) {
                _isLocked.value = false
            } else {
                _isLocked.value = true
            }

            if (loadedSettings.reminderEnabled) {
                val (hour, minute) = SoundAndNotificationHelper.parseTime(loadedSettings.reminderTime)
                SoundAndNotificationHelper.scheduleDailyReminder(
                    getApplication(),
                    hour,
                    minute,
                    true
                )
            }

            val curCal = Calendar.getInstance()
            repository.checkAndGenerateRecurringExpenses(
                curCal.get(Calendar.YEAR),
                curCal.get(Calendar.MONTH)
            )
        }
    }

    fun unlockApp() {
        _isLocked.value = false
        lastBackgroundTimestamp = null
    }

    fun lockAppImmediately() {
        if (!settings.value.isFirstTimeUser) {
            _isLocked.value = true
        }
    }

    fun onAppBackgrounded() {
        if (!settings.value.isFirstTimeUser) {
            lastBackgroundTimestamp = System.currentTimeMillis()
        }
    }

    fun onAppForegrounded() {
        val currentSettings = settings.value
        if (!currentSettings.isLoaded || currentSettings.isFirstTimeUser) return
        val bgTime = lastBackgroundTimestamp ?: return
        val elapsedMillis = System.currentTimeMillis() - bgTime
        val timeoutMillis = currentSettings.autoLockTimeoutMinutes * 60_000L
        if (elapsedMillis >= timeoutMillis) {
            _isLocked.value = true
        }
        lastBackgroundTimestamp = null
    }

    fun setYearAndMonth(year: Int, month: Int) {
        _selectedYear.value = year
        _selectedMonth.value = month
        _dateRangeFilter.value = null to null
    }

    fun previousMonth() {
        val c = Calendar.getInstance().apply {
            set(Calendar.YEAR, _selectedYear.value)
            set(Calendar.MONTH, _selectedMonth.value)
            add(Calendar.MONTH, -1)
        }
        _selectedYear.value = c.get(Calendar.YEAR)
        _selectedMonth.value = c.get(Calendar.MONTH)
        _dateRangeFilter.value = null to null
    }

    fun nextMonth() {
        val c = Calendar.getInstance().apply {
            set(Calendar.YEAR, _selectedYear.value)
            set(Calendar.MONTH, _selectedMonth.value)
            add(Calendar.MONTH, 1)
        }
        _selectedYear.value = c.get(Calendar.YEAR)
        _selectedMonth.value = c.get(Calendar.MONTH)
        _dateRangeFilter.value = null to null
    }

    fun jumpToCurrentMonth() {
        val c = Calendar.getInstance()
        _selectedYear.value = c.get(Calendar.YEAR)
        _selectedMonth.value = c.get(Calendar.MONTH)
        _dateRangeFilter.value = null to null
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(categoryId: Long?) {
        _selectedCategoryFilter.value = categoryId
    }

    fun setDateRangeFilter(startMillis: Long?, endMillis: Long?) {
        _dateRangeFilter.value = startMillis to endMillis
    }

    fun setSortOption(sortOption: HistorySortOption) {
        _sortOption.value = sortOption
    }

    // Current Month Expenses
    val currentMonthExpensesWithCategory: StateFlow<List<ExpenseWithCategory>> = combine(
        allExpensesWithCategory,
        selectedYear,
        selectedMonth
    ) { all, year, month ->
        val start = Formatters.getStartOfMonth(year, month)
        val end = Formatters.getEndOfMonth(year, month)
        all.filter { it.expense.dateMillis in start..end }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Expenses for History Screen
    val filteredHistoryExpenses: StateFlow<List<ExpenseWithCategory>> = combine(
        currentMonthExpensesWithCategory,
        searchQuery,
        selectedCategoryFilter,
        dateRangeFilter,
        sortOption
    ) { expenses, query, catFilter, dateRange, sort ->
        var list = expenses

        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.expense.name.lowercase().contains(q) ||
                    it.expense.note.lowercase().contains(q) ||
                    (it.category?.name?.lowercase()?.contains(q) == true)
            }
        }

        if (catFilter != null) {
            list = list.filter { it.expense.categoryId == catFilter }
        }

        val (startMillis, endMillis) = dateRange
        if (startMillis != null && endMillis != null) {
            list = list.filter { it.expense.dateMillis in startMillis..endMillis }
        }

        when (sort) {
            HistorySortOption.DATE_DESC -> list.sortedByDescending { it.expense.dateMillis }
            HistorySortOption.DATE_ASC -> list.sortedBy { it.expense.dateMillis }
            HistorySortOption.AMOUNT_DESC -> list.sortedByDescending { it.expense.amount }
            HistorySortOption.AMOUNT_ASC -> list.sortedBy { it.expense.amount }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dashboard State (expenses only - no budget, savings, or income)
    val dashboardState: StateFlow<DashboardState> = combine(
        currentMonthExpensesWithCategory,
        allExpensesWithCategory,
        selectedYear,
        selectedMonth
    ) { monthExpenses, allExpenses, year, month ->
        val totalSpent = monthExpenses.sumOf { it.expense.amount }
        val expenseCount = monthExpenses.size

        // Daily average
        val daysInMonth = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
        }.getActualMaximum(Calendar.DAY_OF_MONTH)

        val calNow = Calendar.getInstance()
        val isCurrentMonth = calNow.get(Calendar.YEAR) == year && calNow.get(Calendar.MONTH) == month
        val effectiveDays = if (isCurrentMonth) {
            calNow.get(Calendar.DAY_OF_MONTH).coerceAtLeast(1)
        } else {
            daysInMonth.coerceAtLeast(1)
        }
        val dailyAverage = if (effectiveDays > 0) totalSpent / effectiveDays else 0.0

        // Highest Expense
        val highest = monthExpenses.maxByOrNull { it.expense.amount }
        val highestAmount = highest?.expense?.amount ?: 0.0
        val highestName = highest?.expense?.name ?: ""

        // Category breakdown
        val catMap = monthExpenses.groupBy {
            it.category ?: CategoryEntity(0, "Other", "category", "#64748B")
        }
        val categoryBreakdown = catMap.map { (cat, items) ->
            val amt = items.sumOf { it.expense.amount }
            val pct = if (totalSpent > 0.0) (amt / totalSpent * 100.0).toFloat() else 0f
            CategorySpendItem(
                categoryName = cat.name,
                iconName = cat.iconName,
                colorHex = cat.colorHex,
                amount = amt,
                percentage = pct,
                count = items.size
            )
        }.sortedByDescending { it.amount }

        // Daily spending items (1 to daysInMonth)
        val dailySpendItems = mutableListOf<DaySpendItem>()
        val calDay = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
        }
        for (day in 1..daysInMonth) {
            calDay.set(Calendar.DAY_OF_MONTH, day)
            val dStart = Calendar.getInstance().apply {
                timeInMillis = calDay.timeInMillis
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val dEnd = Calendar.getInstance().apply {
                timeInMillis = calDay.timeInMillis
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis

            val dayAmt = monthExpenses
                .filter { it.expense.dateMillis in dStart..dEnd }
                .sumOf { it.expense.amount }
            val dayName = SimpleDateFormat("EEE", Locale.getDefault()).format(calDay.time)
            dailySpendItems.add(DaySpendItem(day, dayName, dayAmt))
        }

        // Six Month Trend (5 months back + current selected month)
        val sixMonthTrend = mutableListOf<MonthTrendItem>()
        for (i in 5 downTo 0) {
            val tCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                add(Calendar.MONTH, -i)
            }
            val tYear = tCal.get(Calendar.YEAR)
            val tMonth = tCal.get(Calendar.MONTH)
            val tStart = Formatters.getStartOfMonth(tYear, tMonth)
            val tEnd = Formatters.getEndOfMonth(tYear, tMonth)
            val tTotal = allExpenses
                .filter { it.expense.dateMillis in tStart..tEnd }
                .sumOf { it.expense.amount }
            val tLabel = SimpleDateFormat("MMM", Locale.getDefault()).format(tCal.time)
            sixMonthTrend.add(MonthTrendItem(tLabel, tTotal))
        }

        // Previous Month Comparison
        val prevCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            add(Calendar.MONTH, -1)
        }
        val prevStart = Formatters.getStartOfMonth(prevCal.get(Calendar.YEAR), prevCal.get(Calendar.MONTH))
        val prevEnd = Formatters.getEndOfMonth(prevCal.get(Calendar.YEAR), prevCal.get(Calendar.MONTH))
        val prevTotal = allExpenses
            .filter { it.expense.dateMillis in prevStart..prevEnd }
            .sumOf { it.expense.amount }
        val prevMonthName = SimpleDateFormat("MMMM", Locale.getDefault()).format(prevCal.time)

        val (comparisonMsg, isMore) = if (prevTotal > 0.0) {
            val diff = totalSpent - prevTotal
            val percentDiff = Math.abs((diff / prevTotal) * 100.0).toInt()
            if (diff >= 0) {
                Pair("$percentDiff% more than $prevMonthName", true)
            } else {
                Pair("$percentDiff% less than $prevMonthName", false)
            }
        } else {
            Pair("No expenses recorded in $prevMonthName", false)
        }

        val recent = monthExpenses.sortedByDescending { it.expense.dateMillis }.take(5)

        DashboardState(
            totalSpent = totalSpent,
            expenseCount = expenseCount,
            dailyAverage = dailyAverage,
            highestExpense = highestAmount,
            highestExpenseName = highestName,
            categoryBreakdown = categoryBreakdown,
            dailySpending = dailySpendItems,
            sixMonthTrend = sixMonthTrend,
            comparisonMessage = comparisonMsg,
            isMoreThanPreviousMonth = isMore,
            recentExpenses = recent
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardState())

    // Reports / Insights State
    val insightsState: StateFlow<InsightsState> = combine(
        currentMonthExpensesWithCategory,
        dashboardState
    ) { monthExpenses, dashState ->
        val top3 = dashState.categoryBreakdown.take(3)
        val biggest = monthExpenses.maxByOrNull { it.expense.amount }

        val dayOfWeekSpend = mutableMapOf<String, Double>()
        for (item in monthExpenses) {
            val c = Calendar.getInstance().apply { timeInMillis = item.expense.dateMillis }
            val dayName = SimpleDateFormat("EEEE", Locale.getDefault()).format(c.time)
            dayOfWeekSpend[dayName] = (dayOfWeekSpend[dayName] ?: 0.0) + item.expense.amount
        }
        val topDayEntry = dayOfWeekSpend.maxByOrNull { it.value }
        val topDayName = topDayEntry?.key ?: "N/A"
        val topDayPercent = if (dashState.totalSpent > 0.0 && topDayEntry != null) {
            ((topDayEntry.value / dashState.totalSpent) * 100.0).toInt()
        } else 0

        val activeDays = dashState.dailySpending.count { it.amount > 0.0 }

        InsightsState(
            topCategories = top3,
            biggestExpense = biggest,
            mostExpensiveWeekday = topDayName,
            weekdaySpendPercent = topDayPercent,
            activeDaysCount = activeDays
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InsightsState())

    // All Months Overview list
    val allMonthsSummary: StateFlow<List<MonthSummary>> = allExpensesWithCategory.combine(selectedYear) { all, _ ->
        val monthGroup = all.groupBy {
            val c = Calendar.getInstance().apply { timeInMillis = it.expense.dateMillis }
            Pair(c.get(Calendar.YEAR), c.get(Calendar.MONTH))
        }

        monthGroup.map { (ym, items) ->
            val label = Formatters.getMonthLabel(ym.first, ym.second)
            val total = items.sumOf { it.expense.amount }
            MonthSummary(
                year = ym.first,
                month = ym.second,
                label = label,
                totalSpent = total,
                count = items.size
            )
        }.sortedWith(compareByDescending<MonthSummary> { it.year }.thenByDescending { it.month })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Expense CRUD
    fun addExpense(
        name: String,
        amount: Double,
        dateMillis: Long,
        categoryId: Long,
        note: String,
        recurring: Boolean
    ) {
        viewModelScope.launch {
            val expense = ExpenseEntity(
                name = name,
                amount = amount,
                dateMillis = dateMillis,
                categoryId = categoryId,
                note = note,
                recurring = recurring
            )
            repository.insertExpense(expense)

            val currentSettings = settings.value
            if (currentSettings.soundOnSaveEnabled) {
                SoundAndNotificationHelper.playExpenseSavedSound()
            }

            val formattedAmt = Formatters.formatCurrency(
                amount,
                currentSettings.currencySymbol,
                currentSettings.indianNumberFormat,
                hideAmounts = currentSettings.hideAmounts
            )
            if (currentSettings.saveConfirmationNotification) {
                SoundAndNotificationHelper.showExpenseSavedNotification(
                    getApplication(),
                    name,
                    formattedAmt
                )
            }
            _snackbarMessage.emit("Saved \"$name\" ($formattedAmt)")
        }
    }

    fun updateExpense(
        id: Long,
        name: String,
        amount: Double,
        dateMillis: Long,
        categoryId: Long,
        note: String,
        recurring: Boolean
    ) {
        viewModelScope.launch {
            val expense = ExpenseEntity(
                id = id,
                name = name,
                amount = amount,
                dateMillis = dateMillis,
                categoryId = categoryId,
                note = note,
                recurring = recurring
            )
            repository.updateExpense(expense)
            if (settings.value.soundOnSaveEnabled) {
                SoundAndNotificationHelper.playExpenseSavedSound()
            }
            _snackbarMessage.emit("Expense updated")
        }
    }

    fun deleteExpense(item: ExpenseWithCategory) {
        viewModelScope.launch {
            lastDeletedExpense = item.expense
            repository.deleteExpense(item.expense)
            _snackbarMessage.emit("Deleted \"${item.expense.name}\"")
        }
    }

    fun undoDelete() {
        val deleted = lastDeletedExpense ?: return
        viewModelScope.launch {
            repository.insertExpense(deleted)
            lastDeletedExpense = null
            _snackbarMessage.emit("Restored \"${deleted.name}\"")
        }
    }

    // Category CRUD
    fun saveCategory(category: CategoryEntity) {
        viewModelScope.launch {
            if (category.id == 0L) {
                repository.insertCategory(category)
                _snackbarMessage.emit("Category \"${category.name}\" added")
            } else {
                repository.updateCategory(category)
                _snackbarMessage.emit("Category updated")
            }
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(category)
            _snackbarMessage.emit("Category \"${category.name}\" removed")
        }
    }

    // Account & App Lock Setup
    fun createAccount(
        fullName: String,
        email: String,
        phone: String,
        photoPath: String?,
        securityQuestion: String,
        securityAnswer: String
    ) {
        settingsManager.createAccount(
            fullName = fullName,
            email = email,
            phone = phone,
            photoPath = photoPath,
            securityQuestion = securityQuestion,
            securityAnswer = securityAnswer
        )
    }

    fun setupAppLock(pin: String, enableBiometric: Boolean) {
        settingsManager.setupAppLock(pin, enableBiometric)
        _isLocked.value = false
    }

    fun updateProfile(
        fullName: String,
        email: String,
        phone: String,
        photoPath: String?,
        securityQuestion: String?,
        securityAnswer: String?
    ) {
        settingsManager.updateProfile(
            fullName = fullName,
            email = email,
            phone = phone,
            photoPath = photoPath,
            securityQuestion = securityQuestion,
            securityAnswer = securityAnswer
        )
        viewModelScope.launch {
            _snackbarMessage.emit("Profile updated")
        }
    }

    fun removeProfilePhoto() {
        settingsManager.removeProfilePhoto()
    }

    fun updateReminderSettings(enabled: Boolean, time: String) {
        settingsManager.updateReminder(enabled, time)
        val (hour, minute) = SoundAndNotificationHelper.parseTime(time)
        SoundAndNotificationHelper.scheduleDailyReminder(
            context = getApplication(),
            hourOfDay = hour,
            minute = minute,
            enabled = enabled
        )
        viewModelScope.launch {
            if (enabled) {
                _snackbarMessage.emit("Daily reminder set for ${SoundAndNotificationHelper.formatTime12Hour(time)}")
            } else {
                _snackbarMessage.emit("Daily reminder turned off")
            }
        }
    }

    // Export & Print
    fun handleExport(
        context: Context,
        format: ExportFormat,
        scope: ExportScope,
        startDate: Long,
        endDate: Long,
        scopeTitle: String
    ) {
        viewModelScope.launch {
            val allList = allExpensesWithCategory.value
            val exportItems = if (scope == ExportScope.ALL_TIME) {
                allList
            } else {
                allList.filter { it.expense.dateMillis in startDate..endDate }
            }

            val currentSettings = settings.value

            when (format) {
                ExportFormat.PDF, ExportFormat.DIRECT_PRINT -> {
                    val file = ExportHelper.exportToPdf(
                        context = context,
                        title = scopeTitle,
                        subTitle = scopeTitle,
                        expensesWithCategory = exportItems,
                        categories = categories.value,
                        currencySymbol = currentSettings.currencySymbol,
                        useIndianFormat = currentSettings.indianNumberFormat,
                        userName = currentSettings.userName.ifBlank { "User" },
                        userPhotoPath = currentSettings.profilePhotoPath
                    )
                    if (file != null) {
                        ExportHelper.shareFile(
                            context = context,
                            file = file,
                            mimeType = "application/pdf",
                            chooserTitle = if (format == ExportFormat.DIRECT_PRINT) "Print / View PDF" else "Share PDF Report"
                        )
                    }
                }
                ExportFormat.CSV -> {
                    val file = ExportHelper.exportToCsv(
                        context = context,
                        title = scopeTitle,
                        expensesWithCategory = exportItems,
                        currencySymbol = currentSettings.currencySymbol,
                        userName = currentSettings.userName.ifBlank { "User" }
                    )
                    if (file != null) {
                        ExportHelper.shareFile(
                            context = context,
                            file = file,
                            mimeType = "text/csv",
                            chooserTitle = "Share CSV Spreadsheet"
                        )
                    }
                }
                ExportFormat.JSON_BACKUP -> {
                    val file = ExportHelper.exportToJson(
                        context = context,
                        expenses = exportItems.map { it.expense },
                        categories = categories.value
                    )
                    if (file != null) {
                        ExportHelper.shareFile(
                            context = context,
                            file = file,
                            mimeType = "application/json",
                            chooserTitle = "Save / Share JSON Backup"
                        )
                    }
                }
            }
        }
    }

    fun restoreBackupJson(jsonString: String) {
        viewModelScope.launch {
            val parsed = ExportHelper.parseBackupJson(jsonString)
            if (parsed != null) {
                repository.restoreDatabase(parsed.first, parsed.second)
                _snackbarMessage.emit("Backup restored (${parsed.second.size} expenses)")
            } else {
                _snackbarMessage.emit("Failed to parse backup JSON file")
            }
        }
    }

    fun seedSampleData() {
        viewModelScope.launch {
            repository.seedSampleData()
            settingsManager.setSampleDataLoaded(true)
            _snackbarMessage.emit("Sample expenses loaded")
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllExpenses()
            _snackbarMessage.emit("All expenses cleared")
        }
    }
}
