package com.example.playx.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.playx.data.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ConfigViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsManager = PreferencesManager(application)

    private val _imageUri = MutableStateFlow<String?>(null)
    val imageUri: StateFlow<String?> = _imageUri

    private val _videoUri = MutableStateFlow<String?>(null)
    val videoUri: StateFlow<String?> = _videoUri

    init {
        viewModelScope.launch {
            _imageUri.value = prefsManager.imageUri.first()
            _videoUri.value = prefsManager.videoUri.first()
        }
    }

    fun updateImageUri(uri: Uri, context: Context) {
        context.contentResolver.takePersistableUriPermission(
            uri,
            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        val uriString = uri.toString()
        _imageUri.value = uriString
        viewModelScope.launch {
            prefsManager.setImageUri(uriString)
        }
    }

    fun updateVideoUri(uri: Uri, context: Context) {
        context.contentResolver.takePersistableUriPermission(
            uri,
            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        val uriString = uri.toString()
        _videoUri.value = uriString
        viewModelScope.launch {
            prefsManager.setVideoUri(uriString)
        }
    }

    fun clearImageUri() {
        _imageUri.value = null
        viewModelScope.launch {
            prefsManager.setImageUri(null)
        }
    }

    fun clearVideoUri() {
        _videoUri.value = null
        viewModelScope.launch {
            prefsManager.setVideoUri(null)
        }
    }
}