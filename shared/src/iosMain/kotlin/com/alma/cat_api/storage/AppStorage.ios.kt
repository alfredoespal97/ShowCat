package com.alma.cat_api.storage

import com.alma.cat_api.model.Cat
import com.alma.cat_api.model.ThemeMode
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.Foundation.NSUserDefaults

private const val KEY_SEEN_CATS = "seen_cats"
private const val KEY_THEME_MODE = "theme_mode"

class IosAppStorage : AppStorage {
    private val defaults = NSUserDefaults.standardUserDefaults
    private val json = Json { ignoreUnknownKeys = true }

    override fun getSeenCats(): List<Cat> {
        val raw = defaults.stringForKey(KEY_SEEN_CATS) ?: return emptyList()
        return try {
            json.decodeFromString(raw)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun saveSeenCats(cats: List<Cat>) {
        defaults.setObject(json.encodeToString(cats), KEY_SEEN_CATS)
    }

    override fun getThemeMode(): ThemeMode {
        val raw = defaults.stringForKey(KEY_THEME_MODE) ?: return ThemeMode.SYSTEM
        return try {
            ThemeMode.valueOf(raw)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }

    override fun saveThemeMode(mode: ThemeMode) {
        defaults.setObject(mode.name, KEY_THEME_MODE)
    }
}

actual fun getAppStorage(): AppStorage = IosAppStorage()
