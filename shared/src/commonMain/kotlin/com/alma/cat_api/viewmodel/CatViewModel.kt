package com.alma.cat_api.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alma.cat_api.api.CatApi
import com.alma.cat_api.model.Cat
import com.alma.cat_api.model.ThemeMode
import com.alma.cat_api.storage.AppStorage
import com.alma.cat_api.storage.getAppStorage
import com.alma.cat_api.util.getPlatformActions
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val MAX_CACHED_CATS = 60

class CatViewModel(
    private val storage: AppStorage = getAppStorage()
) : ViewModel() {
    private val catApi = CatApi()

    private val _currentCat = MutableStateFlow<Cat?>(null)
    val currentCat: StateFlow<Cat?> = _currentCat.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _seenCats = MutableStateFlow(storage.getSeenCats())
    val seenCats: StateFlow<List<Cat>> = _seenCats.asStateFlow()

    private val _themeMode = MutableStateFlow(storage.getThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    init {
        loadRandomCat()
    }

    fun loadRandomCat() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val cat = catApi.getCats(1).firstOrNull()
                if (cat != null) {
                    _currentCat.value = cat
                    addToCache(cat)
                } else {
                    _messages.tryEmit("No se pudo cargar el gato")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _messages.tryEmit("No se pudo cargar el gato")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Shows a cat that was already cached, without hitting the API again. */
    fun selectCachedCat(cat: Cat) {
        _currentCat.value = cat
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        storage.saveThemeMode(mode)
    }

    private fun addToCache(cat: Cat) {
        val updated = (listOf(cat) + _seenCats.value.filterNot { it.id == cat.id })
            .take(MAX_CACHED_CATS)
        _seenCats.value = updated
        storage.saveSeenCats(updated)
    }

    fun saveCurrentCat() {
        val url = _currentCat.value?.url ?: return
        viewModelScope.launch {
            val saved = getPlatformActions().saveImage(url)
            _messages.tryEmit(
                if (saved) "Foto guardada en la galería" else "No se pudo guardar la foto"
            )
        }
    }

    fun shareCurrentCat() {
        val url = _currentCat.value?.url ?: return
        getPlatformActions().shareImage(url)
    }
}
