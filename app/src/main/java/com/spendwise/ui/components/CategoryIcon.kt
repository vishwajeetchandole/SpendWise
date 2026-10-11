package com.spendwise.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object CategoryIcons {
    val iconMap: Map<String, ImageVector> = mapOf(
        "restaurant" to Icons.Default.Restaurant,
        "directions_car" to Icons.Default.DirectionsCar,
        "receipt_long" to Icons.Default.ReceiptLong,
        "shopping_bag" to Icons.Default.ShoppingBag,
        "medical_services" to Icons.Default.MedicalServices,
        "movie" to Icons.Default.Movie,
        "school" to Icons.Default.School,
        "home" to Icons.Default.Home,
        "local_grocery_store" to Icons.Default.LocalGroceryStore,
        "flight" to Icons.Default.Flight,
        "cafe" to Icons.Default.LocalCafe,
        "work" to Icons.Default.Work,
        "gym" to Icons.Default.FitnessCenter,
        "pets" to Icons.Default.Pets,
        "tech" to Icons.Default.Smartphone,
        "gift" to Icons.Default.CardGiftcard,
        "category" to Icons.Default.Category
    )

    fun getIcon(iconName: String): ImageVector {
        return iconMap[iconName] ?: Icons.Default.Category
    }

    val availableIcons = listOf(
        "restaurant",
        "directions_car",
        "receipt_long",
        "shopping_bag",
        "medical_services",
        "movie",
        "school",
        "home",
        "local_grocery_store",
        "flight",
        "cafe",
        "gym",
        "tech",
        "gift",
        "work",
        "pets",
        "category"
    )

    val availableColors = listOf(
        "#F59E0B", // Amber
        "#EF4444", // Red
        "#3B82F6", // Blue
        "#10B981", // Emerald
        "#8B5CF6", // Purple
        "#EC4899", // Pink
        "#06B6D4", // Cyan
        "#6366F1", // Indigo
        "#14B8A6", // Teal
        "#F97316", // Orange
        "#84CC16", // Lime
        "#64748B"  // Slate
    )

    fun parseColor(hex: String, defaultColor: Color = Color(0xFF64748B)): Color {
        return try {
            val cleanHex = if (hex.startsWith("#")) hex else "#$hex"
            Color(android.graphics.Color.parseColor(cleanHex))
        } catch (e: Exception) {
            defaultColor
        }
    }
}

@Composable
fun CategoryBadge(
    iconName: String,
    colorHex: String,
    size: Dp = 44.dp,
    iconSize: Dp = 22.dp,
    modifier: Modifier = Modifier
) {
    val categoryColor = CategoryIcons.parseColor(colorHex)
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(categoryColor.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = CategoryIcons.getIcon(iconName),
            contentDescription = null,
            tint = categoryColor,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
fun CategoryCircleBadge(
    iconName: String,
    colorHex: String,
    size: Dp = 36.dp,
    iconSize: Dp = 18.dp,
    modifier: Modifier = Modifier
) {
    val categoryColor = CategoryIcons.parseColor(colorHex)
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(categoryColor.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = CategoryIcons.getIcon(iconName),
            contentDescription = null,
            tint = categoryColor,
            modifier = Modifier.size(iconSize)
        )
    }
}
