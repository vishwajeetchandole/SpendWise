package com.spendwise.ui.navigation

object NavRoutes {
    const val DASHBOARD = "dashboard"
    const val HISTORY = "history"
    const val REPORTS = "reports"
    const val SETTINGS = "settings"
    const val PROFILE = "profile"
    const val EXPENSE_ENTRY = "expense_entry"
    const val EXPENSE_ENTRY_WITH_ARG = "expense_entry/{expenseId}"

    fun expenseEntryRoute(expenseId: Long? = null): String {
        return if (expenseId != null && expenseId > 0) {
            "expense_entry/$expenseId"
        } else {
            EXPENSE_ENTRY
        }
    }
}
