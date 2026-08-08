package com.alma.cat_api.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alma.cat_api.api.CatApi
import com.alma.cat_api.model.Cat
import com.alma.cat_api.util.getPlatformActions
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CatViewModel : ViewModel() {
    private val catApi = CatApi()

    private val _currentCat = MutableStateFlow<Cat?>(null)
    val currentCat: StateFlow<Cat?> = _currentCat.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    init {
        loadRandomCat()
    }

    fun loadRandomCat() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _currentCat.value = catApi.getCats(1).firstOrNull()
            } catch (e: Exception) {
                e.printStackTrace()
                _messages.tryEmit("No se pudo cargar el gato")
            } finally {
                _isLoading.value = false
            }
        }
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
