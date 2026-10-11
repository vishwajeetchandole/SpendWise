package com.spendwise.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.spendwise.data.model.CategoryEntity
import com.spendwise.data.model.ExpenseWithCategory
import com.spendwise.ui.HistorySortOption
import com.spendwise.ui.components.ExpenseItemCard
import com.spendwise.ui.components.getCategoryIcon
import com.spendwise.ui.theme.parseHexColor
import com.spendwise.util.Formatters
import java.util.Calendar

private data class DayExpenseGroup(
    val dayKey: String,
    val dayHeaderLabel: String,
    val daySubtotal: Double,
    val items: List<ExpenseWithCategory>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    expenses: List<ExpenseWithCategory>,
    categories: List<CategoryEntity>,
    searchQuery: String,
    selectedCategoryFilter: Long?,
    dateRangeFilter: Pair<Long?, Long?>,
    sortOption: HistorySortOption,
    currencySymbol: String,
    useIndianFormat: Boolean,
    hideAmounts: Boolean = false,
    onSearchQueryChange: (String) -> Unit,
    onCategoryFilterChange: (Long?) -> Unit,
    onDateRangeFilterChange: (Long?, Long?) -> Unit,
    onSortOptionChange: (HistorySortOption) -> Unit,
    onAddExpense: () -> Unit,
    onEditExpense: (ExpenseWithCategory) -> Unit,
    onDeleteExpense: (ExpenseWithCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSortMenu by remember { mutableStateOf(false) }
    var showDateRangeDialog by remember { mutableStateOf(false) }

    val hasDateFilter = dateRangeFilter.first != null && dateRangeFilter.second != null
    val hasAnyFilter = searchQuery.isNotBlank() || selectedCategoryFilter != null || hasDateFilter

    val groupedByDay: List<DayExpenseGroup> = remember(expenses) {
        val map = LinkedHashMap<String, MutableList<ExpenseWithCategory>>()
        for (item in expenses) {
            val cal = Calendar.getInstance().apply { timeInMillis = item.expense.dateMillis }
            val key = "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}-${cal.get(Calendar.DAY_OF_MONTH)}"
            map.getOrPut(key) { mutableListOf() }.add(item)
        }
        map.entries.map { (key, list) ->
            val label = Formatters.formatDateHeader(list.first().expense.dateMillis)
            val subtotal = list.sumOf { it.expense.amount }
            DayExpenseGroup(
                dayKey = key,
                dayHeaderLabel = label,
                daySubtotal = subtotal,
                items = list
            )
        }
    }

    val filteredTotal = remember(expenses) { expenses.sumOf { it.expense.amount } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("history_screen")
    ) {
        // Top Search + Date Range + Sort Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = {
                            Text(
                                text = "Search by name, note, category...",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search"
                            )
                        },
                        trailingIcon = if (searchQuery.isNotEmpty()) {
                            {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear search"
                                    )
                                }
                            }
                        } else null,
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("history_search_input")
                    )

                    // Date Range Filter Button
                    Surface(
                        onClick = { showDateRangeDialog = true },
                        shape = RoundedCornerShape(14.dp),
                        color = if (hasDateFilter) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        },
                        modifier = Modifier
                            .size(52.dp)
                            .testTag("history_date_range_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Filter by Date Range",
                                tint = if (hasDateFilter) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }

                    // Sort Menu Button
                    Box {
                        Surface(
                            onClick = { showSortMenu = true },
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier
                                .size(52.dp)
                                .testTag("history_sort_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Sort,
                                    contentDescription = "Sort Expenses",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Date: Newest First",
                                        fontWeight = if (sortOption == HistorySortOption.DATE_DESC) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    onSortOptionChange(HistorySortOption.DATE_DESC)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Date: Oldest First",
                                        fontWeight = if (sortOption == HistorySortOption.DATE_ASC) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    onSortOptionChange(HistorySortOption.DATE_ASC)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Amount: Highest First",
                                        fontWeight = if (sortOption == HistorySortOption.AMOUNT_DESC) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    onSortOptionChange(HistorySortOption.AMOUNT_DESC)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Amount: Lowest First",
                                        fontWeight = if (sortOption == HistorySortOption.AMOUNT_ASC) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    onSortOptionChange(HistorySortOption.AMOUNT_ASC)
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }

                // Active Date Range Chip if selected
                if (hasDateFilter) {
                    val startStr = Formatters.formatShortDate(dateRangeFilter.first!!)
                    val endStr = Formatters.formatShortDate(dateRangeFilter.second!!)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = true,
                            onClick = { showDateRangeDialog = true },
                            label = {
                                Text(
                                    text = "$startStr – $endStr",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = { onDateRangeFilterChange(null, null) },
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear Date Range",
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        )
                    }
                }

                // Category Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(end = 8.dp)
                ) {
                    item(key = "cat_filter_all") {
                        FilterChip(
                            selected = selectedCategoryFilter == null,
                            onClick = { onCategoryFilterChange(null) },
                            label = { Text("All") },
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                    items(
                        items = categories,
                        key = { "cat_filter_${it.id}" }
                    ) { cat ->
                        val isSelected = selectedCategoryFilter == cat.id
                        val catColor = parseHexColor(cat.colorHex)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                onCategoryFilterChange(if (isSelected) null else cat.id)
                            },
                            label = {
                                Text(
                                    text = cat.name,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = getCategoryIcon(cat.iconName),
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else catColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    }
                }
            }
        }

        // Filtered Count & Total Strip
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${expenses.size} ${if (expenses.size == 1) "expense" else "expenses"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Total: ${
                        Formatters.formatCurrency(
                            filteredTotal,
                            currencySymbol,
                            useIndianFormat,
                            hideAmounts = hideAmounts
                        )
                    }",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1
                )
            }
        }

        // Expenses List Grouped by Day
        if (groupedByDay.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(28.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = if (hasAnyFilter) "No Matching Expenses" else "No Expenses This Month",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (hasAnyFilter) {
                            "Try clearing your search query, category filter, or date range."
                        } else {
                            "Expenses added for the selected month will appear here grouped by day."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (hasAnyFilter) {
                        OutlinedButton(
                            onClick = {
                                onSearchQueryChange("")
                                onCategoryFilterChange(null)
                                onDateRangeFilterChange(null, null)
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.FilterListOff, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Clear Filters")
                        }
                    } else {
                        Button(
                            onClick = onAddExpense,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Expense")
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                groupedByDay.forEachIndexed { groupIndex, group ->
                    item(key = "day_header_${group.dayKey}_$groupIndex") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = if (groupIndex == 0) 2.dp else 8.dp, bottom = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = group.dayHeaderLabel,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                            ) {
                                Text(
                                    text = Formatters.formatCurrency(
                                        group.daySubtotal,
                                        currencySymbol,
                                        useIndianFormat,
                                        hideAmounts = hideAmounts
                                    ),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    items(
                        items = group.items,
                        key = { "hist_item_${it.expense.id}" }
                    ) { item ->
                        ExpenseItemCard(
                            item = item,
                            currencySymbol = currencySymbol,
                            useIndianFormat = useIndianFormat,
                            hideAmounts = hideAmounts,
                            showDate = false,
                            onEdit = { onEditExpense(item) },
                            onDelete = { onDeleteExpense(item) }
                        )
                    }
                }
            }
        }
    }

    if (showDateRangeDialog) {
        val pickerState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = dateRangeFilter.first,
            initialSelectedEndDateMillis = dateRangeFilter.second
        )
        DatePickerDialog(
            onDismissRequest = { showDateRangeDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val s = pickerState.selectedStartDateMillis
                        val e = pickerState.selectedEndDateMillis ?: s
                        if (s != null && e != null) {
                            val startDay = Calendar.getInstance().apply {
                                timeInMillis = s
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }.timeInMillis
                            val endDay = Calendar.getInstance().apply {
                                timeInMillis = e
                                set(Calendar.HOUR_OF_DAY, 23)
                                set(Calendar.MINUTE, 59)
                                set(Calendar.SECOND, 59)
                                set(Calendar.MILLISECOND, 999)
                            }.timeInMillis
                            onDateRangeFilterChange(startDay, endDay)
                        }
                        showDateRangeDialog = false
                    }
                ) {
                    Text("Apply")
                }
            },
            dismissButton = {
                Row {
                    if (hasDateFilter) {
                        TextButton(
                            onClick = {
                                onDateRangeFilterChange(null, null)
                                showDateRangeDialog = false
                            }
                        ) {
                            Text("Clear")
                        }
                    }
                    TextButton(onClick = { showDateRangeDialog = false }) {
                        Text("Cancel")
                    }
                }
            }
        ) {
            DateRangePicker(
                state = pickerState,
                title = {
                    Text(
                        text = "Filter by Date Range",
                        modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp),
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                headline = {
                    val startLabel = pickerState.selectedStartDateMillis?.let { Formatters.formatShortDate(it) } ?: "Start Date"
                    val endLabel = pickerState.selectedEndDateMillis?.let { Formatters.formatShortDate(it) } ?: "End Date"
                    Text(
                        text = "$startLabel – $endLabel",
                        modifier = Modifier.padding(start = 24.dp, end = 12.dp, bottom = 12.dp),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                showModeToggle = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
            )
        }
    }
}
