package com.alma.cat_api.api

import com.alma.cat_api.model.Cat
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class CatApi {
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                useAlternativeNames = false
            })
        }
    }

    suspend fun getCats(limit: Int = 10): List<Cat> {
        return httpClient.get("https://api.thecatapi.com/v1/images/search") {
            parameter("limit", limit)
            header("x-api-key", "live_LCB0prW8wV2YOqYlTRn3AvtjJGKA9KfSw094kC8T1ihvjalZtfMksPWC8eXimRbC")
        }.body()
    }
}
