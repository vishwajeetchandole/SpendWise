package com.spendwise.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    indices = [
        Index(value = ["dateMillis"]),
        Index(value = ["categoryId"])
    ]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val amount: Double,
    val dateMillis: Long,
    val categoryId: Long,
    val note: String = "",
    val recurring: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class ExpenseWithCategory(
    val expense: ExpenseEntity,
    val category: CategoryEntity?
)
