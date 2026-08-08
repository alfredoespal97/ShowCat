package com.alma.cat_api.util

interface PlatformActions {
    fun shareImage(url: String)
    suspend fun saveImage(url: String): Boolean
}

expect fun getPlatformActions(): PlatformActions
