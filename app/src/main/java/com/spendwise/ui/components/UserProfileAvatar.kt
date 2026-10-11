package com.spendwise.ui.components

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.spendwise.ui.theme.SpendWiseBrandGradient
import com.spendwise.util.ProfileImageHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.min

@Composable
fun UserProfileAvatar(
    name: String,
    photoPath: String?,
    size: Dp = 44.dp,
    fontSize: TextUnit = 18.sp,
    borderColor: Color = Color.White.copy(alpha = 0.35f),
    borderWidth: Dp = 1.5.dp,
    onClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val hasValidPhoto = remember(photoPath) {
        !photoPath.isNullOrBlank() && File(photoPath).exists()
    }

    val modifier = Modifier
        .size(size)
        .clip(CircleShape)
        .border(borderWidth, borderColor, CircleShape)
        .then(
            if (onClick != null) Modifier.clickable { onClick() } else Modifier
        )

    if (hasValidPhoto && photoPath != null) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(File(photoPath))
                .crossfade(true)
                .build(),
            contentDescription = "Profile Picture of $name",
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        val initials = remember(name) {
            name.trim()
                .split("\\s+".toRegex())
                .filter { it.isNotEmpty() }
                .take(2)
                .joinToString("") { it.first().uppercase() }
                .ifEmpty { "" }
        }

        Box(
            modifier = modifier.background(SpendWiseBrandGradient),
            contentAlignment = Alignment.Center
        ) {
            if (initials.isNotEmpty()) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = fontSize
                    ),
                    color = Color.White
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User Avatar",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.55f)
                )
            }
        }
    }
}

/**
 * Full-screen dialog that lets the user drag and pinch-to-zoom an image inside a circular
 * crop area and saves the cropped circular square bitmap to internal storage.
 */
@Composable
fun CircularImageCropDialog(
    sourceUri: Uri?,
    initialBitmap: android.graphics.Bitmap? = null,
    onDismiss: () -> Unit,
    onCropped: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var loadedBitmap by remember(sourceUri, initialBitmap) { mutableStateOf(initialBitmap) }
    var isSaving by remember { mutableStateOf(false) }
    var loadError by remember { mutableStateOf(false) }

    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var viewportRadiusPx by remember { mutableFloatStateOf(300f) }

    LaunchedEffect(sourceUri, initialBitmap) {
        if (initialBitmap != null) {
            loadedBitmap = initialBitmap
            loadError = false
        } else if (sourceUri != null) {
            withContext(Dispatchers.IO) {
                val bmp = ProfileImageHelper.loadOrientedBitmap(context, sourceUri)
                if (bmp != null) {
                    loadedBitmap = bmp
                    loadError = false
                } else {
                    loadError = true
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF0B1320)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel Crop",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "Adjust Profile Photo",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    IconButton(
                        onClick = {
                            scale = 1f
                            offsetX = 0f
                            offsetY = 0f
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Zoom",
                            tint = Color.White
                        )
                    }
                }

                Text(
                    text = "Drag and pinch to position your photo inside the circle",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Interactive Crop Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black)
                        .clipToBounds()
                        .onSizeChanged { size ->
                            val minDim = min(size.width, size.height).toFloat()
                            viewportRadiusPx = (minDim * 0.38f).coerceAtLeast(120f)
                        }
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                val newScale = (scale * zoom).coerceIn(1f, 5f)
                                scale = newScale
                                val maxOffset = viewportRadiusPx * newScale
                                offsetX = (offsetX + pan.x).coerceIn(-maxOffset, maxOffset)
                                offsetY = (offsetY + pan.y).coerceIn(-maxOffset, maxOffset)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val bmp = loadedBitmap
                    if (bmp != null) {
                        val imageBitmap = remember(bmp) { bmp.asImageBitmap() }
                        val aspect = bmp.width.toFloat() / bmp.height.toFloat().coerceAtLeast(1f)

                        Image(
                            bitmap = imageBitmap,
                            contentDescription = "Crop preview",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    // Fit base scale so the shorter side covers the circle diameter
                                    val circleDiameter = viewportRadiusPx * 2f
                                    val fitScale = if (aspect >= 1f) {
                                        (circleDiameter / (size.width / aspect).coerceAtLeast(1f))
                                    } else {
                                        (circleDiameter / (size.height * aspect).coerceAtLeast(1f))
                                    }.coerceAtLeast(0.8f)

                                    scaleX = scale * fitScale
                                    scaleY = scale * fitScale
                                    translationX = offsetX
                                    translationY = offsetY
                                }
                        )

                        // Circular cutout mask overlay
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val radius = (min(size.width, size.height) * 0.38f).coerceAtLeast(120f)
                            val circlePath = Path().apply {
                                addOval(
                                    Rect(
                                        left = center.x - radius,
                                        top = center.y - radius,
                                        right = center.x + radius,
                                        bottom = center.y + radius
                                    )
                                )
                            }
                            clipPath(circlePath, clipOp = ClipOp.Difference) {
                                drawRect(Color.Black.copy(alpha = 0.62f))
                            }
                            drawCircle(
                                color = Color.White,
                                radius = radius,
                                center = center,
                                style = Stroke(width = 3.dp.toPx())
                            )
                        }
                    } else if (loadError) {
                        Text(
                            text = "Unable to load selected image.",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text(
                            text = "Loading photo...",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Zoom buttons for accessibility and easy one-handed adjustment
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    IconButton(
                        onClick = { scale = (scale - 0.25f).coerceAtLeast(1f) },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.14f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Zoom Out",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "${(scale * 100).toInt()}%",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        modifier = Modifier.width(56.dp),
                        textAlign = TextAlign.Center
                    )
                    IconButton(
                        onClick = { scale = (scale + 0.25f).coerceAtMost(5f) },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.14f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Zoom In",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isSaving,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", color = Color.White)
                    }
                    Button(
                        onClick = {
                            val bmp = loadedBitmap ?: return@Button
                            isSaving = true
                            scope.launch {
                                val savedPath = withContext(Dispatchers.IO) {
                                    ProfileImageHelper.saveCroppedImage(
                                        context = context,
                                        sourceBitmap = bmp,
                                        scale = scale,
                                        offsetX = offsetX,
                                        offsetY = offsetY,
                                        viewportRadiusPx = viewportRadiusPx
                                    )
                                }
                                isSaving = false
                                if (savedPath != null) {
                                    onCropped(savedPath)
                                }
                            }
                        },
                        enabled = loadedBitmap != null && !isSaving,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isSaving) "Saving..." else "Save Photo",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
