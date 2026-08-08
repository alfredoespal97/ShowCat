@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package com.alma.cat_api.util

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.core.*
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImageWriteToSavedPhotosAlbum

class IosPlatformActions : PlatformActions {
    override fun shareImage(url: String) {
        val activityViewController = UIActivityViewController(
            activityItems = listOf("Mira este gato! $url"),
            applicationActivities = null
        )
        UIApplication.sharedApplication.keyWindow
            ?.rootViewController
            ?.presentViewController(activityViewController, animated = true, completion = null)
    }

    override suspend fun saveImage(url: String): Boolean {
        return try {
            val client = HttpClient()
            val response: HttpResponse = client.get(url)
            val bytes = response.readRawBytes()
            val image = UIImage(data = bytes.toNSData())
            UIImageWriteToSavedPhotosAlbum(image, null, null, null)
            true
        } catch (e: Exception) {
            false
        }
    }
}

private fun ByteArray.toNSData(): NSData = usePinned {
    NSData.create(bytes = it.addressOf(0), length = size.convert())
}

actual fun getPlatformActions(): PlatformActions = IosPlatformActions()
