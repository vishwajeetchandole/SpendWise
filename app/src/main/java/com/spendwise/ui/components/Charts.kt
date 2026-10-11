package com.spendwise.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.spendwise.ui.theme.parseHexColor
import com.spendwise.util.Formatters

data class CategorySpendItem(
    val categoryName: String,
    val iconName: String,
    val colorHex: String,
    val amount: Double,
    val percentage: Float,
    val count: Int = 0
)

data class DaySpendItem(
    val dayOfMonth: Int,
    val dayName: String,
    val amount: Double
)

data class MonthTrendItem(
    val monthLabel: String,
    val amount: Double
)

@Composable
fun DonutChart(
    items: List<CategorySpendItem>,
    totalSpent: Double,
    currencySymbol: String,
    useIndianFormat: Boolean,
    hideAmounts: Boolean = false,
    modifier: Modifier = Modifier
) {
    var animPlayed by remember { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = if (animPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "donut_progress"
    )
    LaunchedEffect(Unit) { animPlayed = true }

    var selectedIndex by remember(items) { mutableStateOf<Int?>(null) }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(210.dp)
                .padding(12.dp)
        ) {
            val emptyRingColor = MaterialTheme.colorScheme.surfaceVariant
            Canvas(
                modifier = Modifier.size(180.dp)
            ) {
                if (size.width <= 0f || size.height <= 0f) return@Canvas
                val strokeWidth = 30.dp.toPx().coerceAtMost(size.minDimension * 0.25f)
                val diameter = (size.minDimension - strokeWidth).coerceAtLeast(10f)
                val topLeft = Offset(
                    (size.width - diameter) / 2f,
                    (size.height - diameter) / 2f
                )

                if (items.isEmpty() || totalSpent <= 0.0) {
                    drawArc(
                        color = emptyRingColor,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(diameter, diameter),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                } else {
                    var startAngle = -90f
                    val gapAngle = if (items.size > 1) 2.5f else 0f
                    items.forEachIndexed { index, item ->
                        val safePct = if (item.percentage.isNaN() || item.percentage < 0f) 0f else item.percentage
                        val sweepAngle = (safePct / 100f) * 360f * progress
                        val isSelected = selectedIndex == index
                        val currentStroke = if (isSelected) strokeWidth * 1.15f else strokeWidth
                        val color = parseHexColor(item.colorHex)

                        if (sweepAngle > 0.5f) {
                            drawArc(
                                color = color,
                                startAngle = startAngle + gapAngle / 2f,
                                sweepAngle = (sweepAngle - gapAngle).coerceAtLeast(1f),
                                useCenter = false,
                                topLeft = topLeft,
                                size = Size(diameter, diameter),
                                style = Stroke(width = currentStroke, cap = StrokeCap.Butt)
                            )
                        }
                        startAngle += sweepAngle
                    }
                }
            }

            // Center label
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(116.dp)
                    .padding(horizontal = 4.dp)
            ) {
                val selectedItem = selectedIndex?.let { items.getOrNull(it) }
                if (selectedItem != null) {
                    Text(
                        text = selectedItem.categoryName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = Formatters.formatCurrency(
                            selectedItem.amount,
                            currencySymbol,
                            useIndianFormat,
                            hideAmounts = hideAmounts
                        ),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "${String.format("%.1f", selectedItem.percentage)}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = parseHexColor(selectedItem.colorHex),
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Text(
                        text = "Total Spent",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = Formatters.formatCurrency(
                            totalSpent,
                            currencySymbol,
                            useIndianFormat,
                            hideAmounts = hideAmounts
                        ),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Category Legend
        if (items.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items.forEachIndexed { index, item ->
                    val isSelected = selectedIndex == index
                    Surface(
                        onClick = {
                            selectedIndex = if (selectedIndex == index) null else index
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        } else {
                            Color.Transparent
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(parseHexColor(item.colorHex))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = item.categoryName,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${String.format("%.1f", item.percentage)}%",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = Formatters.formatCurrency(
                                    item.amount,
                                    currencySymbol,
                                    useIndianFormat,
                                    hideAmounts = hideAmounts
                                ),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DailyBarChart(
    items: List<DaySpendItem>,
    currencySymbol: String,
    useIndianFormat: Boolean,
    hideAmounts: Boolean = false,
    modifier: Modifier = Modifier
) {
    var animPlayed by remember { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = if (animPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "bar_progress"
    )
    LaunchedEffect(Unit) { animPlayed = true }

    var selectedDay by remember(items) { mutableStateOf<DaySpendItem?>(null) }
    val maxAmount = remember(items) { (items.maxOfOrNull { it.amount } ?: 0.0).coerceAtLeast(1.0) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val emptyBarColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

    Column(modifier = modifier.fillMaxWidth()) {
        // Tooltip Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val active = selectedDay ?: items.lastOrNull { it.amount > 0 } ?: items.lastOrNull()
            if (active != null) {
                Text(
                    text = "Day ${active.dayOfMonth} (${active.dayName})",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = Formatters.formatCurrency(
                        active.amount,
                        currencySymbol,
                        useIndianFormat,
                        hideAmounts = hideAmounts
                    ),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1
                )
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .pointerInput(items) {
                    if (items.isNotEmpty()) {
                        detectTapGestures { offset ->
                            if (size.width > 0) {
                                val slotWidth = size.width / items.size.toFloat()
                                val index = (offset.x / slotWidth).toInt().coerceIn(0, items.lastIndex)
                                selectedDay = items[index]
                            }
                        }
                    }
                }
        ) {
            if (items.isEmpty() || size.width <= 0f || size.height <= 0f) return@Canvas

            val chartHeight = (size.height - 20.dp.toPx()).coerceAtLeast(10f)
            val slotWidth = size.width / items.size.toFloat()
            val barWidth = (slotWidth * 0.62f).coerceAtLeast(3.dp.toPx())

            // Horizontal dashed guide lines
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
            listOf(0.25f, 0.5f, 0.75f, 1f).forEach { fraction ->
                val y = chartHeight * (1f - fraction)
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = dashEffect
                )
            }

            items.forEachIndexed { index, dayItem ->
                val xCenter = index * slotWidth + slotWidth / 2f
                val ratio = (dayItem.amount / maxAmount).toFloat().coerceIn(0f, 1f) * progress
                val barHeight = if (dayItem.amount > 0) {
                    (ratio * (chartHeight - 8.dp.toPx())).coerceAtLeast(4.dp.toPx())
                } else {
                    3.dp.toPx()
                }
                val isSelected = selectedDay?.dayOfMonth == dayItem.dayOfMonth
                val barColor = when {
                    isSelected -> secondaryColor
                    dayItem.amount > 0 -> primaryColor
                    else -> emptyBarColor
                }

                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(xCenter - barWidth / 2f, (chartHeight - barHeight).coerceAtLeast(0f)),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
            }
        }

        // X-axis labels
        if (items.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val milestones = listOf(1, 8, 15, 22, items.size).distinct()
                milestones.forEach { dayNum ->
                    Text(
                        text = "$dayNum",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun MonthlyTrendLineChart(
    items: List<MonthTrendItem>,
    currencySymbol: String,
    useIndianFormat: Boolean,
    hideAmounts: Boolean = false,
    modifier: Modifier = Modifier
) {
    var animPlayed by remember { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = if (animPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing),
        label = "trend_line_progress"
    )
    LaunchedEffect(Unit) { animPlayed = true }

    val maxAmount = remember(items) { (items.maxOfOrNull { it.amount } ?: 0.0).coerceAtLeast(1.0) }
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            if (items.isEmpty() || size.width <= 0f || size.height <= 0f) return@Canvas

            val topPadding = 16.dp.toPx()
            val bottomPadding = 16.dp.toPx()
            val sidePadding = 18.dp.toPx()
            val chartHeight = (size.height - topPadding - bottomPadding).coerceAtLeast(10f)
            val chartWidth = (size.width - sidePadding * 2).coerceAtLeast(10f)

            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
            listOf(0f, 0.5f, 1f).forEach { fraction ->
                val y = topPadding + chartHeight * fraction
                drawLine(
                    color = gridColor,
                    start = Offset(sidePadding, y),
                    end = Offset(size.width - sidePadding, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = dashEffect
                )
            }

            val points = items.mapIndexed { index, item ->
                val x = if (items.size > 1) {
                    sidePadding + (index.toFloat() / (items.size - 1).toFloat()) * chartWidth
                } else {
                    size.width / 2f
                }
                val ratio = (item.amount / maxAmount).toFloat().coerceIn(0f, 1f) * progress
                val y = topPadding + chartHeight * (1f - ratio)
                Offset(x, y)
            }

            if (points.size > 1) {
                val linePath = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        val prev = points[i - 1]
                        val curr = points[i]
                        val ctrlX = (prev.x + curr.x) / 2f
                        cubicTo(ctrlX, prev.y, ctrlX, curr.y, curr.x, curr.y)
                    }
                }

                val fillPath = Path().apply {
                    addPath(linePath)
                    lineTo(points.last().x, topPadding + chartHeight)
                    lineTo(points.first().x, topPadding + chartHeight)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.28f),
                            primaryColor.copy(alpha = 0.0f)
                        ),
                        startY = topPadding,
                        endY = topPadding + chartHeight
                    )
                )

                drawPath(
                    path = linePath,
                    color = primaryColor,
                    style = Stroke(
                        width = 3.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

            points.forEach { pt ->
                drawCircle(
                    color = primaryColor,
                    radius = 5.dp.toPx(),
                    center = pt
                )
                drawCircle(
                    color = surfaceColor,
                    radius = 2.5.dp.toPx(),
                    center = pt
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            items.forEach { item ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.monthLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (hideAmounts) {
                            "$currencySymbol•••"
                        } else {
                            Formatters.formatCompactCurrency(item.amount, currencySymbol, useIndianFormat)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
