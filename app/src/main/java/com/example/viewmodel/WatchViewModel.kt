package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.converter.VideoAudioConverter
import com.example.data.MediaRepository
import com.example.model.ConvertedAudioItem
import com.example.model.MediaFolder
import com.example.model.MediaItem
import com.example.model.MediaType
import com.example.model.VaultItem
import com.example.security.SecureVaultManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

enum class NavigationTab {
    VIDEO, MUSIC, FOLDERS, TOOLS, ME
}

data class WatchUiState(
    val currentTab: NavigationTab = NavigationTab.VIDEO,
    val isLoading: Boolean = false,
    val permissionGranted: Boolean = false,
    val allVideos: List<MediaItem> = emptyList(),
    val allAudios: List<MediaItem> = emptyList(),
    val folders: List<MediaFolder> = emptyList(),
    val searchQuery: String = "",
    val activeFolder: MediaFolder? = null,
    val selectedVideoForPlayback: MediaItem? = null,
    val selectedAudioForPlayback: MediaItem? = null,
    val isAudioPlaying: Boolean = false,
    val audioPlaybackPosition: Long = 0L,
    val audioPlaybackDuration: Long = 0L,
    val isShuffle: Boolean = false,
    val isRepeat: Boolean = false,
    val equalizerPreset: String = "Normal",
    val sleepTimerMinutesRemaining: Int? = null,
    val vaultItems: List<VaultItem> = emptyList(),
    val isVaultUnlocked: Boolean = false,
    val vaultRequiresSetup: Boolean = false,
    val convertedAudios: List<ConvertedAudioItem> = emptyList(),
    val conversionProgress: Float? = null,
    val isConverting: Boolean = false,
    val isDarkTheme: Boolean = true
)

class WatchViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MediaRepository(application)
    private val vaultManager = SecureVaultManager(application)
    private val converter = VideoAudioConverter(application)

    private val _uiState = MutableStateFlow(WatchUiState())
    val uiState: StateFlow<WatchUiState> = _uiState.asStateFlow()

    init {
        checkVaultStatus()
        loadConvertedAudios()
    }

    fun setPermissionGranted(granted: Boolean) {
        _uiState.update { it.copy(permissionGranted = granted) }
        if (granted) {
            refreshMediaLibrary()
        }
    }

    fun selectTab(tab: NavigationTab) {
        _uiState.update { it.copy(currentTab = tab, activeFolder = null) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun openFolder(folder: MediaFolder) {
        _uiState.update { it.copy(activeFolder = folder) }
    }

    fun closeFolder() {
        _uiState.update { it.copy(activeFolder = null) }
    }

    fun refreshMediaLibrary() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val videos = repository.getVideos()
            val audios = repository.getAudios()
            val folders = repository.getFolders(videos, audios)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    allVideos = videos,
                    allAudios = audios,
                    folders = folders
                )
            }
        }
    }

    fun playVideo(video: MediaItem?) {
        _uiState.update { it.copy(selectedVideoForPlayback = video) }
    }

    fun playAudio(audio: MediaItem?) {
        _uiState.update {
            it.copy(
                selectedAudioForPlayback = audio,
                isAudioPlaying = audio != null,
                audioPlaybackPosition = 0L,
                audioPlaybackDuration = audio?.durationMs ?: 0L
            )
        }
    }

    fun toggleAudioPlayPause() {
        _uiState.update { it.copy(isAudioPlaying = !it.isAudioPlaying) }
    }

    fun nextAudio() {
        val list = _uiState.value.allAudios
        val current = _uiState.value.selectedAudioForPlayback ?: return
        val index = list.indexOfFirst { it.id == current.id }
        if (index != -1 && list.isNotEmpty()) {
            val nextIndex = if (_uiState.value.isShuffle) {
                list.indices.random()
            } else {
                (index + 1) % list.size
            }
            playAudio(list[nextIndex])
        }
    }

    fun previousAudio() {
        val list = _uiState.value.allAudios
        val current = _uiState.value.selectedAudioForPlayback ?: return
        val index = list.indexOfFirst { it.id == current.id }
        if (index != -1 && list.isNotEmpty()) {
            val prevIndex = if (index > 0) index - 1 else list.size - 1
            playAudio(list[prevIndex])
        }
    }

    fun toggleShuffle() {
        _uiState.update { it.copy(isShuffle = !it.isShuffle) }
    }

    fun toggleRepeat() {
        _uiState.update { it.copy(isRepeat = !it.isRepeat) }
    }

    fun setEqualizerPreset(preset: String) {
        _uiState.update { it.copy(equalizerPreset = preset) }
    }

    fun toggleFavorite(item: MediaItem) {
        val isFav = repository.toggleFavorite(item)
        val updatedVideos = _uiState.value.allVideos.map {
            if (it.id == item.id && it.type == item.type) it.copy(isFavorite = isFav) else it
        }
        val updatedAudios = _uiState.value.allAudios.map {
            if (it.id == item.id && it.type == item.type) it.copy(isFavorite = isFav) else it
        }
        _uiState.update {
            it.copy(
                allVideos = updatedVideos,
                allAudios = updatedAudios
            )
        }
    }

    fun saveVideoPosition(video: MediaItem, posMs: Long) {
        repository.saveLastPosition(video, posMs)
    }

    // Vault Operations
    private fun checkVaultStatus() {
        val isSetup = vaultManager.isVaultSetup()
        _uiState.update { it.copy(vaultRequiresSetup = !isSetup) }
    }

    fun setupVaultPin(pin: String): Boolean {
        val success = vaultManager.setVaultPin(pin)
        if (success) {
            _uiState.update { it.copy(vaultRequiresSetup = false, isVaultUnlocked = true) }
            loadVaultItems()
        }
        return success
    }

    fun unlockVault(pin: String): Boolean {
        val ok = vaultManager.verifyPin(pin)
        if (ok) {
            _uiState.update { it.copy(isVaultUnlocked = true) }
            loadVaultItems()
        }
        return ok
    }

    fun lockVault() {
        _uiState.update { it.copy(isVaultUnlocked = false) }
    }

    fun loadVaultItems() {
        val items = vaultManager.listVaultItems()
        _uiState.update { it.copy(vaultItems = items) }
    }

    fun encryptFileToVault(file: File, displayName: String, isVideo: Boolean) {
        viewModelScope.launch {
            vaultManager.encryptFileToVault(file, displayName, isVideo)
            loadVaultItems()
        }
    }

    fun deleteVaultItem(item: VaultItem) {
        viewModelScope.launch {
            vaultManager.deleteVaultItem(item)
            loadVaultItems()
        }
    }

    fun getDecryptedFileForViewing(item: VaultItem): File? {
        val encFile = File(getApplication<Application>().filesDir, "secure_vault_encrypted/${item.encryptedFileName}")
        return vaultManager.decryptVaultFileForViewing(encFile)
    }

    // Converter Operations
    fun loadConvertedAudios() {
        val list = converter.getConvertedItems()
        _uiState.update { it.copy(convertedAudios = list) }
    }

    fun convertVideo(video: MediaItem, format: String, bitrate: Int, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isConverting = true, conversionProgress = 0.0f) }
            val result = converter.convertVideoToAudio(
                videoUri = video.uri,
                outputFileName = video.title,
                format = format.lowercase(),
                bitrateKbps = bitrate,
                onProgress = { p ->
                    _uiState.update { it.copy(conversionProgress = p) }
                }
            )
            _uiState.update { it.copy(isConverting = false, conversionProgress = null) }
            loadConvertedAudios()
            onComplete(result.isSuccess)
        }
    }

    fun toggleTheme() {
        _uiState.update { it.copy(isDarkTheme = !it.isDarkTheme) }
    }
}
