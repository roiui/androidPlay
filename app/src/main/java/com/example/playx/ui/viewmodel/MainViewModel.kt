package com.example.playx.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.playx.data.PreferencesManager
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class ScreenMode { COVER, VIDEO }

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsManager = PreferencesManager(application)

    var screenMode: ScreenMode by mutableStateOf(ScreenMode.COVER)
        private set

    private var exoPlayer: ExoPlayer? = null

    val uiState: StateFlow<MainUiState> = combine(
        prefsManager.imageUri,
        prefsManager.videoUri
    ) { imageUri, videoUri ->
        val imageOk = imageUri != null && prefsManager.isUriAccessible(imageUri)
        val videoOk = videoUri != null && prefsManager.isUriAccessible(videoUri)
        MainUiState(
            imageUri = if (imageOk) imageUri else null,
            videoUri = if (videoOk) videoUri else null,
            isConfigured = imageOk && videoOk
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MainUiState())

    fun onCoverTapped() {
        if (screenMode == ScreenMode.COVER && uiState.value.isConfigured) {
            screenMode = ScreenMode.VIDEO
        }
    }

    fun onBackPressed(): Boolean {
        return if (screenMode == ScreenMode.VIDEO) {
            screenMode = ScreenMode.COVER
            releasePlayer()
            true
        } else {
            false
        }
    }

    fun getOrCreatePlayer(): ExoPlayer? {
        val uri = uiState.value.videoUri ?: return null
        return exoPlayer ?: ExoPlayer.Builder(getApplication()).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.parse(uri)))
            repeatMode = Player.REPEAT_MODE_ALL
            playWhenReady = true
            prepare()
            exoPlayer = this
        }
    }

    val player: ExoPlayer? get() = exoPlayer

    fun togglePlayPause() {
        exoPlayer?.let {
            if (it.isPlaying) it.pause() else it.play()
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
    }

    fun releasePlayer() {
        exoPlayer?.release()
        exoPlayer = null
    }

    override fun onCleared() {
        super.onCleared()
        releasePlayer()
    }
}

data class MainUiState(
    val imageUri: String? = null,
    val videoUri: String? = null,
    val isConfigured: Boolean = false
)