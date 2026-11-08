package com.motosmart.launcher.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min

/**
 * Stores a rider-chosen image in app-private storage and loads it as the
 * MotoDeck//smart cockpit background.
 *
 * This is deliberately independent of [android.app.WallpaperManager]: choosing a
 * cockpit image never changes the phone's home or lock screen wallpaper.
 */
object AppWallpaperStore {

    private const val FILE_NAME = "cockpit_wallpaper.jpg"
    private const val MAX_DIMENSION = 2048
    private const val JPEG_QUALITY = 92

    fun file(context: Context): File = File(context.filesDir, FILE_NAME)

    fun exists(context: Context): Boolean = file(context).isFile

    suspend fun save(context: Context, uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val bitmap = decodeSource(context, uri)
            try {
                val target = file(context)
                target.parentFile?.mkdirs()
                target.outputStream().use { stream ->
                    check(bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, stream)) {
                        "Could not save the selected image"
                    }
                }
            } finally {
                bitmap.recycle()
            }
            Unit
        }
    }

    suspend fun load(context: Context): Bitmap? = withContext(Dispatchers.IO) {
        val target = file(context)
        if (!target.isFile) return@withContext null
        runCatching {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(target.absolutePath, bounds)
            var sample = 1
            while (bounds.outWidth / sample > MAX_DIMENSION ||
                bounds.outHeight / sample > MAX_DIMENSION
            ) {
                sample *= 2
            }
            val options = BitmapFactory.Options().apply {
                inSampleSize = sample
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            BitmapFactory.decodeFile(target.absolutePath, options)
        }.getOrNull()
    }

    fun clear(context: Context): Boolean = file(context).delete()

    private fun decodeSource(context: Context, uri: Uri): Bitmap {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            return ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                val scale = min(
                    1f,
                    MAX_DIMENSION.toFloat() / max(info.size.width, info.size.height).toFloat()
                )
                if (scale < 1f) {
                    decoder.setTargetSize(
                        (info.size.width * scale).toInt().coerceAtLeast(1),
                        (info.size.height * scale).toInt().coerceAtLeast(1)
                    )
                }
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        }

        // Android 8.0/8.1 fallback.
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, bounds)
        }
        var sample = 1
        while (bounds.outWidth / sample > MAX_DIMENSION ||
            bounds.outHeight / sample > MAX_DIMENSION
        ) {
            sample *= 2
        }
        val options = BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        return context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: error("Could not read the selected image")
    }
}
