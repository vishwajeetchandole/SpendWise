package com.spendwise.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val iconName: String,
    val colorHex: String,
    val budget: Double? = null,
    val isDefault: Boolean = false
)

object DefaultCategories {
    val list = listOf(
        CategoryEntity(id = 1, name = "Food & Dining", iconName = "restaurant", colorHex = "#F59E0B", isDefault = true),
        CategoryEntity(id = 2, name = "Transport", iconName = "directions_car", colorHex = "#3B82F6", isDefault = true),
        CategoryEntity(id = 3, name = "Bills & Utilities", iconName = "receipt_long", colorHex = "#EF4444", isDefault = true),
        CategoryEntity(id = 4, name = "Shopping", iconName = "shopping_bag", colorHex = "#EC4899", isDefault = true),
        CategoryEntity(id = 5, name = "Healthcare", iconName = "medical_services", colorHex = "#10B981", isDefault = true),
        CategoryEntity(id = 6, name = "Entertainment", iconName = "movie", colorHex = "#8B5CF6", isDefault = true),
        CategoryEntity(id = 7, name = "Education", iconName = "school", colorHex = "#06B6D4", isDefault = true),
        CategoryEntity(id = 8, name = "Rent & Housing", iconName = "home", colorHex = "#6366F1", isDefault = true),
        CategoryEntity(id = 9, name = "Groceries", iconName = "local_grocery_store", colorHex = "#14B8A6", isDefault = true),
        CategoryEntity(id = 10, name = "Other", iconName = "category", colorHex = "#64748B", isDefault = true)
    )
}
