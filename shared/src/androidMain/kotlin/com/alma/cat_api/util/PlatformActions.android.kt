package com.alma.cat_api.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.core.*
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class AndroidPlatformActions(private val context: Context) : PlatformActions {
    override fun shareImage(url: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Mira este gato! $url")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    override suspend fun saveImage(url: String): Boolean {
        return try {
            val client = HttpClient()
            val response: HttpResponse = client.get(url)
            val bytes = response.readRawBytes()
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            saveBitmapToGallery(bitmap)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun saveBitmapToGallery(bitmap: Bitmap) {
        val filename = "cat_${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            context.contentResolver?.also { resolver ->
                val contentValues = android.content.ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val imageUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }
        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }
    }
}

private lateinit var platformActions: PlatformActions

fun initPlatformActions(context: Context) {
    platformActions = AndroidPlatformActions(context)
}

actual fun getPlatformActions(): PlatformActions = platformActions
