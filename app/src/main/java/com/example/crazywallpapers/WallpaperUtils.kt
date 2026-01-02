package com.example.crazywallpapers

import android.app.WallpaperManager
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.provider.MediaStore
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult


suspend fun setWallpaper(
    context: Context,
    imageUrl: String
) {
    val loader = ImageLoader(context)

    val request = ImageRequest.Builder(context)
        .data(imageUrl)
        .allowHardware(false) // REQUIRED for Bitmap access
        .build()

    val result = loader.execute(request)

    if (result is SuccessResult) {
        val bitmap = (result.drawable).toBitmap()
        val manager = WallpaperManager.getInstance(context)
        manager.setBitmap(bitmap)
    }
}

suspend fun downloadToGallery(
    context: Context,
    imageUrl: String
) {
    val loader = ImageLoader(context)

    val request = ImageRequest.Builder(context)
        .data(imageUrl)
        .allowHardware(false)
        .build()

    val result = loader.execute(request)

    if (result is SuccessResult) {
        val bitmap = result.drawable.toBitmap()

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "wallpaper_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CrazyWallpapers")
        }

        val uri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        )

        uri?.let {
            context.contentResolver.openOutputStream(it)?.use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
            }
        }
    }
}