package com.spendwise.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendwise.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private data class CategoryDoodleSpec(
    val icon: ImageVector,
    val label: String,
    val xFraction: Float,
    val yFraction: Float,
    val sizeDp: Dp,
    val baseRotation: Float,
    val phaseOffset: Float,
    val accentColor: Color
)

private val splashCategoryDoodles = listOf(
    CategoryDoodleSpec(
        icon = Icons.Default.Restaurant,
        label = "Food",
        xFraction = 0.12f,
        yFraction = 0.11f,
        sizeDp = 46.dp,
        baseRotation = -14f,
        phaseOffset = 0.0f,
        accentColor = Color(0xFF2563EB)
    ),
    CategoryDoodleSpec(
        icon = Icons.Default.ShoppingBag,
        label = "Shopping",
        xFraction = 0.85f,
        yFraction = 0.10f,
        sizeDp = 48.dp,
        baseRotation = 12f,
        phaseOffset = 1.1f,
        accentColor = Color(0xFF1D4ED8)
    ),
    CategoryDoodleSpec(
        icon = Icons.Default.DirectionsCar,
        label = "Transport",
        xFraction = 0.10f,
        yFraction = 0.28f,
        sizeDp = 44.dp,
        baseRotation = 10f,
        phaseOffset = 2.0f,
        accentColor = Color(0xFF3B82F6)
    ),
    CategoryDoodleSpec(
        icon = Icons.Default.Flight,
        label = "Travel",
        xFraction = 0.88f,
        yFraction = 0.27f,
        sizeDp = 46.dp,
        baseRotation = -16f,
        phaseOffset = 2.8f,
        accentColor = Color(0xFF0284C7)
    ),
    CategoryDoodleSpec(
        icon = Icons.Default.Receipt,
        label = "Bills",
        xFraction = 0.15f,
        yFraction = 0.47f,
        sizeDp = 42.dp,
        baseRotation = -10f,
        phaseOffset = 3.5f,
        accentColor = Color(0xFF1E40AF)
    ),
    CategoryDoodleSpec(
        icon = Icons.Default.Movie,
        label = "Entertainment",
        xFraction = 0.86f,
        yFraction = 0.46f,
        sizeDp = 44.dp,
        baseRotation = 14f,
        phaseOffset = 4.2f,
        accentColor = Color(0xFF2563EB)
    ),
    CategoryDoodleSpec(
        icon = Icons.Default.LocalGroceryStore,
        label = "Groceries",
        xFraction = 0.11f,
        yFraction = 0.66f,
        sizeDp = 46.dp,
        baseRotation = 11f,
        phaseOffset = 4.9f,
        accentColor = Color(0xFF0369A1)
    ),
    CategoryDoodleSpec(
        icon = Icons.Default.MedicalServices,
        label = "Health",
        xFraction = 0.87f,
        yFraction = 0.65f,
        sizeDp = 44.dp,
        baseRotation = -12f,
        phaseOffset = 5.5f,
        accentColor = Color(0xFF1D4ED8)
    ),
    CategoryDoodleSpec(
        icon = Icons.Default.School,
        label = "Education",
        xFraction = 0.20f,
        yFraction = 0.82f,
        sizeDp = 44.dp,
        baseRotation = -8f,
        phaseOffset = 1.6f,
        accentColor = Color(0xFF3B82F6)
    ),
    CategoryDoodleSpec(
        icon = Icons.Default.Home,
        label = "Home",
        xFraction = 0.80f,
        yFraction = 0.81f,
        sizeDp = 44.dp,
        baseRotation = 9f,
        phaseOffset = 3.1f,
        accentColor = Color(0xFF1E3A8A)
    ),
    CategoryDoodleSpec(
        icon = Icons.Default.CurrencyRupee,
        label = "Expenses",
        xFraction = 0.48f,
        yFraction = 0.14f,
        sizeDp = 40.dp,
        baseRotation = -6f,
        phaseOffset = 2.4f,
        accentColor = Color(0xFF2563EB)
    ),
    CategoryDoodleSpec(
        icon = Icons.Default.Category,
        label = "Categories",
        xFraction = 0.50f,
        yFraction = 0.78f,
        sizeDp = 40.dp,
        baseRotation = 8f,
        phaseOffset = 0.7f,
        accentColor = Color(0xFF3B82F6)
    )
)

/**
 * Reusable SpendWise Logo Mark composable using R.drawable.logo_mark.
 */
@Composable
fun SpendWiseLogoMark(
    size: Dp = 40.dp,
    cornerRadius: Dp = 12.dp,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = R.drawable.logo_mark),
        contentDescription = stringResource(R.string.app_name),
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.28f),
                shape = RoundedCornerShape(cornerRadius)
            )
    )
}

/**
 * Reusable SpendWise Full Logo With Name composable using R.drawable.logo_with_name.
 */
@Composable
fun SpendWiseLogoWithName(
    height: Dp = 56.dp,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = R.drawable.logo_with_name),
        contentDescription = stringResource(R.string.app_name),
        contentScale = ContentScale.Fit,
        modifier = modifier.height(height)
    )
}

/**
 * Interactive, theme-aware first splash screen with white background in Light Theme,
 * deep navy background in Dark Theme, floating expense category doodles, SpendWise logo,
 * bold SpendWise heading, and "Built by Vishwajeet Chandole" tag.
 */
@Composable
fun AppStartSplashScreen(
    themeMode: String = "system",
    onFinished: () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> MaterialTheme.colorScheme.background.luminance() < 0.5f && systemInDark
    }
    val scope = rememberCoroutineScope()

    var startAnim by remember { mutableStateOf(false) }
    var tapCount by remember { mutableIntStateOf(0) }
    val interactiveBounce = remember { Animatable(1f) }
    val rippleBurst = remember { Animatable(0f) }

    val logoScale by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0.45f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "splash_logo_scale"
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "splash_content_alpha"
    )

    val progressFraction by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = tween(durationMillis = 2100, easing = FastOutSlowInEasing),
        label = "splash_progress"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "splash_infinite")

    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    val orbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_angle"
    )

    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.16f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    fun triggerInteractivePulse() {
        tapCount++
        scope.launch {
            interactiveBounce.snapTo(0.86f)
            interactiveBounce.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioHighBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
        scope.launch {
            rippleBurst.snapTo(0f)
            rippleBurst.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing)
            )
        }
    }

    LaunchedEffect(Unit) {
        startAnim = true
        delay(2350)
        onFinished()
    }

    val backgroundBrush = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF06101E),
                Color(0xFF0B192E),
                Color(0xFF0F2342),
                Color(0xFF06101E)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFFFFFF),
                Color(0xFFF8FAFF),
                Color(0xFFEFF6FF),
                Color(0xFFFFFFFF)
            )
        )
    }

    val primaryTextColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0B2545)
    val secondaryTextColor = if (isDark) Color(0xFF93C5FD) else Color(0xFF1D4ED8)
    val mutedTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                triggerInteractivePulse()
            }
    ) {
        val screenWidthDp = maxWidth
        val screenHeightDp = maxHeight

        // 1. Decorative Doodle Lines, Orbital Rings & Radial Glow Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width * 0.5f, size.height * 0.45f)

            // Soft radial blue glow behind center logo
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        if (isDark) Color(0xFF2563EB).copy(alpha = 0.25f)
                        else Color(0xFF3B82F6).copy(alpha = 0.14f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = size.minDimension * 0.58f * glowPulse
                ),
                center = center,
                radius = size.minDimension * 0.58f * glowPulse
            )

            // Subtle dashed doodle orbit rings in background
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 18f), orbitAngle * 2f)
            drawCircle(
                color = if (isDark) Color(0xFF3B82F6).copy(alpha = 0.14f)
                else Color(0xFF2563EB).copy(alpha = 0.10f),
                radius = size.minDimension * 0.36f,
                center = center,
                style = Stroke(width = 1.5.dp.toPx(), pathEffect = dashEffect)
            )

            drawCircle(
                color = if (isDark) Color(0xFF60A5FA).copy(alpha = 0.09f)
                else Color(0xFF1D4ED8).copy(alpha = 0.07f),
                radius = size.minDimension * 0.48f,
                center = center,
                style = Stroke(width = 1.dp.toPx(), pathEffect = dashEffect)
            )

            // Interactive tap shockwave ring
            if (rippleBurst.value > 0f && rippleBurst.value < 1f) {
                drawCircle(
                    color = Color(0xFF2563EB).copy(alpha = (1f - rippleBurst.value) * 0.35f),
                    radius = size.minDimension * (0.18f + rippleBurst.value * 0.45f),
                    center = center,
                    style = Stroke(width = 3.dp.toPx())
                )
            }
        }

        // 2. Floating Category Doodles Layer in Background
        splashCategoryDoodles.forEachIndexed { index, doodle ->
            val floatX = sin(wavePhase + doodle.phaseOffset) * 8f
            val floatY = cos(wavePhase + doodle.phaseOffset * 1.2f) * 10f
            val tilt = doodle.baseRotation + sin(wavePhase + doodle.phaseOffset) * 6f
            val entranceScale = (logoScale * (0.85f + (index % 3) * 0.06f)).coerceIn(0f, 1.1f)
            val pulseScale = if (tapCount > 0) interactiveBounce.value else 1f

            val xOffset = screenWidthDp * doodle.xFraction - doodle.sizeDp / 2
            val yOffset = screenHeightDp * doodle.yFraction - doodle.sizeDp / 2

            Box(
                modifier = Modifier
                    .offset(x = xOffset, y = yOffset)
                    .offset {
                        IntOffset(
                            x = floatX.dp.toPx().roundToInt(),
                            y = floatY.dp.toPx().roundToInt()
                        )
                    }
                    .scale(entranceScale * pulseScale)
                    .rotate(tilt)
                    .alpha(contentAlpha * (if (isDark) 0.72f else 0.82f))
                    .size(doodle.sizeDp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isDark) doodle.accentColor.copy(alpha = 0.16f)
                        else doodle.accentColor.copy(alpha = 0.09f)
                    )
                    .border(
                        width = 1.dp,
                        color = if (isDark) doodle.accentColor.copy(alpha = 0.32f)
                        else doodle.accentColor.copy(alpha = 0.20f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        triggerInteractivePulse()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = doodle.icon,
                    contentDescription = doodle.label,
                    tint = if (isDark) Color(0xFF93C5FD) else doodle.accentColor,
                    modifier = Modifier.size(doodle.sizeDp * 0.52f)
                )
            }
        }

        // 3. Center Interactive SpendWise Logo + Bold Heading + Animated Progress
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
                .scale(logoScale * interactiveBounce.value)
                .alpha(contentAlpha),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated Logo Medallion with rotating orbital accent
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(144.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        triggerInteractivePulse()
                    }
            ) {
                // Rotating outer orbital ring
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(orbitAngle)
                ) {
                    val strokeW = 2.5.dp.toPx()
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xFF2563EB).copy(alpha = 0.65f),
                                Color(0xFF60A5FA),
                                Color.Transparent
                            )
                        ),
                        startAngle = 0f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )
                }

                // Soft glowing halo circle
                Box(
                    modifier = Modifier
                        .size(124.dp)
                        .scale(glowPulse)
                        .clip(CircleShape)
                        .background(
                            if (isDark) Color(0xFF1E3A8A).copy(alpha = 0.45f)
                            else Color(0xFFDBEAFE).copy(alpha = 0.75f)
                        )
                )

                // Crisp SpendWise Logo Card
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = Color.Transparent,
                    shadowElevation = 16.dp,
                    modifier = Modifier
                        .size(112.dp)
                        .border(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.85f),
                                    Color(0xFF93C5FD),
                                    Color(0xFF1D4ED8)
                                )
                            ),
                            shape = RoundedCornerShape(28.dp)
                        )
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_mark),
                        contentDescription = "SpendWise Logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(28.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Bold SpendWise Heading
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.6.sp,
                    fontSize = 36.sp
                ),
                color = primaryTextColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle Pill Badge
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isDark) Color(0xFF1E3A8A).copy(alpha = 0.55f)
                else Color(0xFFDBEAFE).copy(alpha = 0.85f)
            ) {
                Text(
                    text = "Smart Daily Expense Tracker",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.3.sp
                    ),
                    color = secondaryTextColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sleek Animated Blue Progress Indicator
            Box(
                modifier = Modifier
                    .width(148.dp)
                    .height(5.dp)
                    .clip(CircleShape)
                    .background(
                        if (isDark) Color(0xFF1E293B) else Color(0xFFDBEAFE)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progressFraction.coerceIn(0.05f, 1f))
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF1D4ED8),
                                    Color(0xFF3B82F6),
                                    Color(0xFF60A5FA)
                                )
                            )
                        )
                )
            }
        }

        // 4. Bottom "Built by Vishwajeet Chandole" Creator Tag
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 32.dp, start = 24.dp, end = 24.dp)
                .alpha(contentAlpha),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = if (isDark) Color(0xFF0F1D33).copy(alpha = 0.9f)
                else Color.White.copy(alpha = 0.94f),
                shadowElevation = 4.dp,
                modifier = Modifier.border(
                    width = 1.dp,
                    color = if (isDark) Color(0xFF1E3A8A) else Color(0xFFDBEAFE),
                    shape = RoundedCornerShape(24.dp)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2563EB))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.tagline_built_by),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.4.sp
                        ),
                        color = primaryTextColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Tap anywhere to interact",
                style = MaterialTheme.typography.labelSmall,
                color = mutedTextColor.copy(alpha = 0.75f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CreatorFooterText(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.tagline_built_by),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
