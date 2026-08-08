package com.alma.cat_api.storage

import android.content.Context
import com.alma.cat_api.model.Cat
import com.alma.cat_api.model.ThemeMode
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val PREFS_NAME = "showcat_prefs"
private const val KEY_SEEN_CATS = "seen_cats"
private const val KEY_THEME_MODE = "theme_mode"

class AndroidAppStorage(context: Context) : AppStorage {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    override fun getSeenCats(): List<Cat> {
        val raw = prefs.getString(KEY_SEEN_CATS, null) ?: return emptyList()
        return try {
            json.decodeFromString(raw)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun saveSeenCats(cats: List<Cat>) {
        prefs.edit().putString(KEY_SEEN_CATS, json.encodeToString(cats)).apply()
    }

    override fun getThemeMode(): ThemeMode {
        val raw = prefs.getString(KEY_THEME_MODE, null) ?: return ThemeMode.SYSTEM
        return try {
            ThemeMode.valueOf(raw)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }

    override fun saveThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
    }
}

private lateinit var appStorage: AppStorage

fun initAppStorage(context: Context) {
    appStorage = AndroidAppStorage(context)
}

actual fun getAppStorage(): AppStorage = appStorage
