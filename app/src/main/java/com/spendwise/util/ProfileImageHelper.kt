package com.spendwise.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.Calendar
import kotlin.math.min

object ProfileImageHelper {

    fun loadOrientedBitmap(context: Context, uri: Uri, maxDimension: Int = 1200): Bitmap? {
        return decodeBitmapFromUri(context, uri, maxDimension)
    }

    fun saveCroppedImage(
        context: Context,
        sourceBitmap: Bitmap,
        scale: Float,
        offsetX: Float,
        offsetY: Float,
        viewportRadiusPx: Float
    ): String? {
        return saveCroppedCircularBitmap(
            context = context,
            sourceBitmap = sourceBitmap,
            scale = scale,
            offsetX = offsetX,
            offsetY = offsetY,
            viewportSizePx = viewportRadiusPx * 2f
        )
    }

    fun decodeBitmapFromUri(context: Context, uri: Uri, maxDimension: Int = 1200): Bitmap? {
        return try {
            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOptions)
            }
            if (boundsOptions.outWidth <= 0 || boundsOptions.outHeight <= 0) return null

            var sampleSize = 1
            while (
                boundsOptions.outWidth / sampleSize > maxDimension ||
                boundsOptions.outHeight / sampleSize > maxDimension
            ) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Crops the user-transformed bitmap (with pan offset and zoom scale inside a viewport of size [viewportSizePx])
     * into a crisp circular avatar bitmap and saves it to internal storage.
     */
    fun saveCroppedCircularBitmap(
        context: Context,
        sourceBitmap: Bitmap,
        scale: Float,
        offsetX: Float,
        offsetY: Float,
        viewportSizePx: Float
    ): String? {
        return try {
            val outputSize = 512
            val outputBitmap = Bitmap.createBitmap(outputSize, outputSize, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(outputBitmap)

            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

            // Draw circular mask
            val radius = outputSize / 2f
            canvas.drawCircle(radius, radius, radius, paint)

            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

            val ratioToOutput = if (viewportSizePx > 0f) outputSize / viewportSizePx else 1f
            val baseFitScale = if (sourceBitmap.width > 0 && sourceBitmap.height > 0) {
                val minSide = min(sourceBitmap.width, sourceBitmap.height).toFloat()
                outputSize / minSide
            } else 1f

            val matrix = Matrix().apply {
                // Center bitmap at origin
                postTranslate(-sourceBitmap.width / 2f, -sourceBitmap.height / 2f)
                // Apply base fit scale + user pinch zoom
                val totalScale = baseFitScale * scale
                postScale(totalScale, totalScale)
                // Translate to center of output + user drag offset scaled to output
                postTranslate(
                    radius + offsetX * ratioToOutput,
                    radius + offsetY * ratioToOutput
                )
            }

            canvas.drawBitmap(sourceBitmap, matrix, paint)

            val profileDir = File(context.filesDir, "profile")
            if (!profileDir.exists()) profileDir.mkdirs()

            val destFile = File(profileDir, "profile_avatar_${System.currentTimeMillis()}.png")
            FileOutputStream(destFile).use { out ->
                outputBitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
            }
            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun copyUriToPrivateStorage(context: Context, sourceUri: Uri): String? {
        return try {
            val profileDir = File(context.filesDir, "profile")
            if (!profileDir.exists()) profileDir.mkdirs()

            val destFile = File(profileDir, "profile_avatar_${System.currentTimeMillis()}.jpg")
            val inputStream: InputStream? = context.contentResolver.openInputStream(sourceUri)
            val outputStream = FileOutputStream(destFile)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getInitials(name: String): String {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return "U"
        val parts = trimmed.split(" ").filter { it.isNotBlank() }
        return when {
            parts.size >= 2 -> "${parts[0].first().uppercase()}${parts[1].first().uppercase()}"
            parts.isNotEmpty() && parts[0].length >= 2 -> parts[0].take(2).uppercase()
            parts.isNotEmpty() -> parts[0].take(1).uppercase()
            else -> "U"
        }
    }

    fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..21 -> "Good evening"
            else -> "Good night"
        }
    }
}
