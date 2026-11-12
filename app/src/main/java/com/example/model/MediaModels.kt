package com.example.model

import android.net.Uri

enum class MediaType {
    VIDEO, AUDIO
}

data class MediaItem(
    val id: Long,
    val title: String,
    val artistOrResolution: String = "",
    val durationMs: Long = 0,
    val sizeBytes: Long = 0,
    val uri: Uri,
    val path: String = "",
    val folderName: String = "Internal",
    val dateAdded: Long = 0,
    val type: MediaType = MediaType.VIDEO,
    val isFavorite: Boolean = false,
    val lastPositionMs: Long = 0
)

data class MediaFolder(
    val name: String,
    val path: String,
    val videoCount: Int,
    val audioCount: Int,
    val lastModified: Long,
    val thumbnailUri: Uri? = null
)

data class MediaPlaylist(
    val id: String,
    val title: String,
    val itemsCount: Int = 0,
    val createdDate: Long = System.currentTimeMillis()
)

data class VaultItem(
    val id: String,
    val originalFileName: String,
    val encryptedFileName: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val importDate: Long,
    val isVideo: Boolean
)

data class ConvertedAudioItem(
    val id: String,
    val fileName: String,
    val filePath: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val format: String,
    val bitrateKbps: Int,
    val timestamp: Long
)
