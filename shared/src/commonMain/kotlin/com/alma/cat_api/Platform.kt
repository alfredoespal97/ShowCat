package com.alma.cat_api

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform