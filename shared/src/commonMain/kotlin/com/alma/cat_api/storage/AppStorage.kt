package com.alma.cat_api.storage

import com.alma.cat_api.model.Cat
import com.alma.cat_api.model.ThemeMode

/**
 * Small local cache: cats already shown to the user (so they can be revisited without
 * asking the API again) plus the chosen theme preference.
 */
interface AppStorage {
    fun getSeenCats(): List<Cat>
    fun saveSeenCats(cats: List<Cat>)
    fun getThemeMode(): ThemeMode
    fun saveThemeMode(mode: ThemeMode)
}

expect fun getAppStorage(): AppStorage
