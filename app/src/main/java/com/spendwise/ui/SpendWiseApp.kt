package com.spendwise.ui

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.spendwise.ui.components.AppStartSplashScreen
import com.spendwise.ui.components.CategoryManagerDialog
import com.spendwise.ui.components.ExportDialog
import com.spendwise.ui.components.MonthsOverviewDialog
import com.spendwise.ui.components.PinLockScreen
import com.spendwise.ui.components.SetAppLockScreen
import com.spendwise.ui.components.SpendWiseTopHeader
import com.spendwise.ui.navigation.NavRoutes
import com.spendwise.ui.screens.DashboardScreen
import com.spendwise.ui.screens.ExpenseEntryScreen
import com.spendwise.ui.screens.HistoryScreen
import com.spendwise.ui.screens.ProfileScreen
import com.spendwise.ui.screens.ReportsScreen
import com.spendwise.ui.screens.SettingsScreen
import com.spendwise.ui.screens.WelcomeCreateAccountScreen
import com.spendwise.ui.theme.SpendWiseTheme
import com.spendwise.util.Formatters

enum class SpendWiseTab(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    DASHBOARD(NavRoutes.DASHBOARD, "Dashboard", Icons.Default.Dashboard),
    HISTORY(NavRoutes.HISTORY, "History", Icons.Default.History),
    REPORTS(NavRoutes.REPORTS, "Reports", Icons.Default.Insights),
    SETTINGS(NavRoutes.SETTINGS, "Settings", Icons.Default.Settings)
}

private enum class StartupStage {
    SPLASH,
    WELCOME_CREATE_ACCOUNT,
    SET_APP_LOCK,
    APP_LOCK,
    MAIN_APP
}

@Composable
fun SpendWiseApp(
    viewModel: SpendWiseViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val navController = rememberNavController()

    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val isLocked by viewModel.isLocked.collectAsStateWithLifecycle()
    val selectedYear by viewModel.selectedYear.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()

    val dashboardState by viewModel.dashboardState.collectAsStateWithLifecycle()
    val insightsState by viewModel.insightsState.collectAsStateWithLifecycle()
    val allExpensesWithCategory by viewModel.allExpensesWithCategory.collectAsStateWithLifecycle()
    val filteredHistoryExpenses by viewModel.filteredHistoryExpenses.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val allMonthsSummary by viewModel.allMonthsSummary.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategoryFilter by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
    val dateRangeFilter by viewModel.dateRangeFilter.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()

    var splashFinished by rememberSaveable { mutableStateOf(false) }
    var showCategoryManagerDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showMonthsOverviewDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Apply or clear FLAG_SECURE based on settings.blockScreenshots
    LaunchedEffect(settings.blockScreenshots) {
        val window = (context as? Activity)?.window ?: return@LaunchedEffect
        if (settings.blockScreenshots) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    // Observe app background/foreground transitions to enforce auto-lock timeout
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> viewModel.onAppBackgrounded()
                Lifecycle.Event.ON_START -> viewModel.onAppForegrounded()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Observe snackbar messages with Undo action
    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { msg ->
            if (msg.startsWith("Deleted")) {
                val result = snackbarHostState.showSnackbar(
                    message = msg,
                    actionLabel = "Undo",
                    duration = SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.undoDelete()
                }
            } else {
                snackbarHostState.showSnackbar(
                    message = msg,
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    // Determine current stage from saved DataStore state without flashing the wrong screen
    val currentStage = when {
        !splashFinished || !settings.isLoaded -> StartupStage.SPLASH
        !settings.accountCreated -> StartupStage.WELCOME_CREATE_ACCOUNT
        !settings.appLockConfigured || settings.pinHash.isBlank() -> StartupStage.SET_APP_LOCK
        isLocked -> StartupStage.APP_LOCK
        else -> StartupStage.MAIN_APP
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isTopLevelRoute = currentRoute in listOf(
        NavRoutes.DASHBOARD,
        NavRoutes.HISTORY,
        NavRoutes.REPORTS,
        NavRoutes.SETTINGS
    )

    val currentTab = when (currentRoute) {
        NavRoutes.HISTORY -> SpendWiseTab.HISTORY
        NavRoutes.REPORTS -> SpendWiseTab.REPORTS
        NavRoutes.SETTINGS -> SpendWiseTab.SETTINGS
        else -> SpendWiseTab.DASHBOARD
    }

    fun navigateToTopLevelTab(route: String) {
        if (currentRoute != route) {
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    SpendWiseTheme(themeMode = settings.themeMode) {
        AnimatedContent(
            targetState = currentStage,
            transitionSpec = {
                fadeIn(animationSpec = tween(320)) togetherWith fadeOut(animationSpec = tween(240))
            },
            label = "startup_stage_transition"
        ) { stage ->
            when (stage) {
                StartupStage.SPLASH -> {
                    AppStartSplashScreen(
                        themeMode = settings.themeMode,
                        onFinished = { splashFinished = true }
                    )
                }

                StartupStage.WELCOME_CREATE_ACCOUNT -> {
                    WelcomeCreateAccountScreen(
                        onCreateAccount = { fullName, email, phone, photoPath, question, answer ->
                            viewModel.createAccount(
                                fullName = fullName,
                                email = email,
                                phone = phone,
                                photoPath = photoPath,
                                securityQuestion = question,
                                securityAnswer = answer
                            )
                        }
                    )
                }

                StartupStage.SET_APP_LOCK -> {
                    SetAppLockScreen(
                        onSetupComplete = { pin, enableBiometric ->
                            viewModel.setupAppLock(pin, enableBiometric)
                        }
                    )
                }

                StartupStage.APP_LOCK -> {
                    PinLockScreen(
                        userName = settings.userName,
                        userPhotoPath = settings.profilePhotoPath,
                        biometricEnabled = settings.biometricEnabled,
                        securityQuestion = settings.securityQuestion,
                        onVerifyPin = { enteredPin ->
                            viewModel.settingsManager.verifyPin(enteredPin)
                        },
                        onVerifySecurityAnswer = { enteredAnswer ->
                            viewModel.settingsManager.verifySecurityAnswer(enteredAnswer)
                        },
                        onResetPin = { newPin ->
                            viewModel.settingsManager.updatePin(newPin)
                        },
                        onUnlocked = { viewModel.unlockApp() }
                    )
                }

                StartupStage.MAIN_APP -> {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        contentWindowInsets = WindowInsets(0, 0, 0, 0),
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        topBar = {
                            if (isTopLevelRoute) {
                                SpendWiseTopHeader(
                                    currentTab = currentTab,
                                    userName = settings.userName,
                                    userPhotoPath = settings.profilePhotoPath,
                                    selectedYear = selectedYear,
                                    selectedMonth = selectedMonth,
                                    hideAmounts = settings.hideAmounts,
                                    onToggleHideAmounts = {
                                        viewModel.settingsManager.updateHideAmounts(!settings.hideAmounts)
                                    },
                                    onPreviousMonth = { viewModel.previousMonth() },
                                    onNextMonth = { viewModel.nextMonth() },
                                    onOpenMonthPicker = { showMonthsOverviewDialog = true },
                                    onJumpToCurrentMonth = { viewModel.jumpToCurrentMonth() },
                                    onProfileClick = {
                                        navController.navigate(NavRoutes.PROFILE) {
                                            launchSingleTop = true
                                        }
                                    }
                                )
                            }
                        },
                        bottomBar = {
                            if (isTopLevelRoute) {
                                NavigationBar(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    tonalElevation = 4.dp
                                ) {
                                    SpendWiseTab.entries.forEach { tab ->
                                        val isSelected = currentRoute == tab.route
                                        NavigationBarItem(
                                            selected = isSelected,
                                            onClick = { navigateToTopLevelTab(tab.route) },
                                            icon = {
                                                Icon(
                                                    imageVector = tab.icon,
                                                    contentDescription = tab.title
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = tab.title,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                    ),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                            ),
                                            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                                        )
                                    }
                                }
                            }
                        },
                        floatingActionButton = {
                            if (isTopLevelRoute && currentRoute != NavRoutes.SETTINGS) {
                                FloatingActionButton(
                                    onClick = {
                                        navController.navigate(NavRoutes.expenseEntryRoute())
                                    },
                                    shape = CircleShape,
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                    elevation = FloatingActionButtonDefaults.elevation(
                                        defaultElevation = 6.dp,
                                        pressedElevation = 8.dp
                                    ),
                                    modifier = Modifier
                                        .size(56.dp)
                                        .testTag("add_expense_fab")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add Expense",
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            NavHost(
                                navController = navController,
                                startDestination = NavRoutes.DASHBOARD
                            ) {
                                // 1. Dashboard View
                                composable(NavRoutes.DASHBOARD) {
                                    DashboardScreen(
                                        state = dashboardState,
                                        currencySymbol = settings.currencySymbol,
                                        useIndianFormat = settings.indianNumberFormat,
                                        hideAmounts = settings.hideAmounts,
                                        onNavigateToHistory = {
                                            navigateToTopLevelTab(NavRoutes.HISTORY)
                                        },
                                        onAddExpense = {
                                            navController.navigate(NavRoutes.expenseEntryRoute())
                                        },
                                        onEditExpense = { item ->
                                            navController.navigate(NavRoutes.expenseEntryRoute(item.expense.id))
                                        },
                                        onDeleteExpense = { item ->
                                            viewModel.deleteExpense(item)
                                        }
                                    )
                                }

                                // 2. Expense Entry View (new or editing)
                                composable(route = NavRoutes.EXPENSE_ENTRY) {
                                    ExpenseEntryScreen(
                                        expenseId = null,
                                        initialExpense = null,
                                        categories = categories,
                                        allExpenses = allExpensesWithCategory,
                                        currencySymbol = settings.currencySymbol,
                                        useIndianFormat = settings.indianNumberFormat,
                                        onNavigateBack = { navController.popBackStack() },
                                        onSaveExpense = { name, amount, dateMillis, categoryId, note, recurring ->
                                            viewModel.addExpense(
                                                name = name,
                                                amount = amount,
                                                dateMillis = dateMillis,
                                                categoryId = categoryId,
                                                note = note,
                                                recurring = recurring
                                            )
                                        },
                                        onAddNewCategory = { showCategoryManagerDialog = true }
                                    )
                                }

                                composable(
                                    route = NavRoutes.EXPENSE_ENTRY_WITH_ARG,
                                    arguments = listOf(
                                        navArgument("expenseId") {
                                            type = NavType.LongType
                                            defaultValue = -1L
                                        }
                                    )
                                ) { backStackEntry ->
                                    val expenseId = backStackEntry.arguments?.getLong("expenseId") ?: -1L
                                    val expenseItem = allExpensesWithCategory.firstOrNull {
                                        it.expense.id == expenseId
                                    }

                                    ExpenseEntryScreen(
                                        expenseId = expenseId,
                                        initialExpense = expenseItem?.expense,
                                        categories = categories,
                                        allExpenses = allExpensesWithCategory,
                                        currencySymbol = settings.currencySymbol,
                                        useIndianFormat = settings.indianNumberFormat,
                                        onNavigateBack = { navController.popBackStack() },
                                        onSaveExpense = { name, amount, dateMillis, categoryId, note, recurring ->
                                            if (expenseItem != null) {
                                                viewModel.updateExpense(
                                                    id = expenseItem.expense.id,
                                                    name = name,
                                                    amount = amount,
                                                    dateMillis = dateMillis,
                                                    categoryId = categoryId,
                                                    note = note,
                                                    recurring = recurring
                                                )
                                            } else {
                                                viewModel.addExpense(
                                                    name = name,
                                                    amount = amount,
                                                    dateMillis = dateMillis,
                                                    categoryId = categoryId,
                                                    note = note,
                                                    recurring = recurring
                                                )
                                            }
                                        },
                                        onDeleteExpense = if (expenseItem != null) {
                                            { viewModel.deleteExpense(expenseItem) }
                                        } else null,
                                        onAddNewCategory = { showCategoryManagerDialog = true }
                                    )
                                }

                                // 3. History View
                                composable(NavRoutes.HISTORY) {
                                    HistoryScreen(
                                        expenses = filteredHistoryExpenses,
                                        categories = categories,
                                        searchQuery = searchQuery,
                                        selectedCategoryFilter = selectedCategoryFilter,
                                        dateRangeFilter = dateRangeFilter,
                                        sortOption = sortOption,
                                        currencySymbol = settings.currencySymbol,
                                        useIndianFormat = settings.indianNumberFormat,
                                        hideAmounts = settings.hideAmounts,
                                        onSearchQueryChange = { viewModel.setSearchQuery(it) },
                                        onCategoryFilterChange = { viewModel.setCategoryFilter(it) },
                                        onDateRangeFilterChange = { start, end ->
                                            viewModel.setDateRangeFilter(start, end)
                                        },
                                        onSortOptionChange = { viewModel.setSortOption(it) },
                                        onAddExpense = {
                                            navController.navigate(NavRoutes.expenseEntryRoute())
                                        },
                                        onEditExpense = { item ->
                                            navController.navigate(NavRoutes.expenseEntryRoute(item.expense.id))
                                        },
                                        onDeleteExpense = { item ->
                                            viewModel.deleteExpense(item)
                                        }
                                    )
                                }

                                // 4. Reports View
                                composable(NavRoutes.REPORTS) {
                                    ReportsScreen(
                                        dashboardState = dashboardState,
                                        insightsState = insightsState,
                                        currencySymbol = settings.currencySymbol,
                                        useIndianFormat = settings.indianNumberFormat,
                                        hideAmounts = settings.hideAmounts,
                                        onOpenExportDialog = { showExportDialog = true }
                                    )
                                }

                                // 5. Settings View
                                composable(NavRoutes.SETTINGS) {
                                    SettingsScreen(
                                        settings = settings,
                                        onOpenEditProfile = {
                                            navController.navigate(NavRoutes.PROFILE) {
                                                launchSingleTop = true
                                            }
                                        },
                                        onVerifyPin = { enteredPin ->
                                            viewModel.settingsManager.verifyPin(enteredPin)
                                        },
                                        onChangePin = { newPin ->
                                            viewModel.settingsManager.updatePin(newPin)
                                        },
                                        onUpdateBiometric = { enabled ->
                                            viewModel.settingsManager.updateBiometricEnabled(enabled)
                                        },
                                        onUpdateAutoLockTimeout = { minutes ->
                                            viewModel.settingsManager.updateAutoLockTimeout(minutes)
                                        },
                                        onUpdateHideAmounts = { hide ->
                                            viewModel.settingsManager.updateHideAmounts(hide)
                                        },
                                        onUpdateBlockScreenshots = { block ->
                                            viewModel.settingsManager.updateBlockScreenshots(block)
                                        },
                                        onUpdateReminder = { enabled, time ->
                                            viewModel.updateReminderSettings(enabled, time)
                                        },
                                        onUpdateSaveConfirmationNotification = { enabled ->
                                            viewModel.settingsManager.updateSaveConfirmationNotification(enabled)
                                        },
                                        onUpdateSoundOnSave = { enabled ->
                                            viewModel.settingsManager.updateSoundOnSave(enabled)
                                        },
                                        onUpdateCurrency = { symbol ->
                                            viewModel.settingsManager.updateCurrency(symbol)
                                        },
                                        onUpdateIndianFormat = { enabled ->
                                            viewModel.settingsManager.updateIndianNumberFormat(enabled)
                                        },
                                        onUpdateTheme = { themeMode ->
                                            viewModel.settingsManager.updateThemeMode(themeMode)
                                        },
                                        onOpenCategoryManager = { showCategoryManagerDialog = true },
                                        onOpenExportDialog = { showExportDialog = true },
                                        onRestoreJson = { jsonString ->
                                            viewModel.restoreBackupJson(jsonString)
                                        },
                                        onClearAllData = {
                                            viewModel.clearAllData()
                                        }
                                    )
                                }

                                // 6. Profile Screen
                                composable(NavRoutes.PROFILE) {
                                    ProfileScreen(
                                        initialName = settings.userName,
                                        initialEmail = settings.userEmail,
                                        initialPhone = settings.userPhone,
                                        initialPhotoPath = settings.profilePhotoPath,
                                        initialSecurityQuestion = settings.securityQuestion,
                                        onRemovePhoto = {
                                            viewModel.removeProfilePhoto()
                                        },
                                        onNavigateBack = { navController.popBackStack() },
                                        onSaveProfile = { fullName, email, phone, photoPath, question, newAnswer ->
                                            viewModel.updateProfile(
                                                fullName = fullName,
                                                email = email,
                                                phone = phone,
                                                photoPath = photoPath,
                                                securityQuestion = question,
                                                securityAnswer = newAnswer
                                            )
                                            navController.popBackStack()
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (showCategoryManagerDialog) {
                        CategoryManagerDialog(
                            categories = categories,
                            onDismiss = { showCategoryManagerDialog = false },
                            onSaveCategory = { viewModel.saveCategory(it) },
                            onDeleteCategory = { viewModel.deleteCategory(it) }
                        )
                    }

                    if (showExportDialog) {
                        val monthLabel = Formatters.getMonthLabel(selectedYear, selectedMonth)
                        val monthStart = Formatters.getStartOfMonth(selectedYear, selectedMonth)
                        val monthEnd = Formatters.getEndOfMonth(selectedYear, selectedMonth)

                        ExportDialog(
                            currentMonthLabel = monthLabel,
                            currentMonthStart = monthStart,
                            currentMonthEnd = monthEnd,
                            onDismiss = { showExportDialog = false },
                            onExport = { format, scope, startDate, endDate, title ->
                                viewModel.handleExport(context, format, scope, startDate, endDate, title)
                                showExportDialog = false
                            }
                        )
                    }

                    if (showMonthsOverviewDialog) {
                        MonthsOverviewDialog(
                            months = allMonthsSummary,
                            selectedYear = selectedYear,
                            selectedMonth = selectedMonth,
                            currencySymbol = settings.currencySymbol,
                            useIndianFormat = settings.indianNumberFormat,
                            hideAmounts = settings.hideAmounts,
                            onSelectMonth = { year, month ->
                                viewModel.setYearAndMonth(year, month)
                            },
                            onDismiss = { showMonthsOverviewDialog = false }
                        )
                    }
                }
            }
        }
    }
}
