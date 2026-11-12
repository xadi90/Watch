package com.example.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.example.model.MediaFolder
import com.example.model.MediaItem
import com.example.model.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class MediaRepository(private val context: Context) {

    private val favoritesPrefs = context.getSharedPreferences("watch_favorites_prefs", Context.MODE_PRIVATE)
    private val historyPrefs = context.getSharedPreferences("watch_history_prefs", Context.MODE_PRIVATE)

    suspend fun getVideos(): List<MediaItem> = withContext(Dispatchers.IO) {
        val videos = mutableListOf<MediaItem>()
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT
        )
        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        try {
            val cursor = context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )

            cursor?.use {
                val idCol = it.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val titleCol = it.getColumnIndex(MediaStore.Video.Media.TITLE)
                val durCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val sizeCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val dataCol = it.getColumnIndex(MediaStore.Video.Media.DATA)
                val dateCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                val widthCol = it.getColumnIndex(MediaStore.Video.Media.WIDTH)
                val heightCol = it.getColumnIndex(MediaStore.Video.Media.HEIGHT)

                while (it.moveToNext()) {
                    val id = it.getLong(idCol)
                    val rawName = it.getString(nameCol) ?: "Video $id"
                    val title = if (titleCol != -1 && !it.getString(titleCol).isNullOrEmpty()) {
                        it.getString(titleCol)
                    } else rawName

                    val duration = it.getLong(durCol)
                    val size = it.getLong(sizeCol)
                    val path = if (dataCol != -1) it.getString(dataCol) ?: "" else ""
                    val dateAdded = it.getLong(dateCol)
                    val width = if (widthCol != -1) it.getInt(widthCol) else 0
                    val height = if (heightCol != -1) it.getInt(heightCol) else 0
                    val res = if (width > 0 && height > 0) "${width}x${height}" else "HD"

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    val folderName = if (path.isNotEmpty()) {
                        try {
                            File(path).parentFile?.name ?: "Internal"
                        } catch (e: Exception) {
                            "Internal"
                        }
                    } else "Internal"

                    val isFav = favoritesPrefs.getBoolean("fav_v_$id", false)
                    val lastPos = historyPrefs.getLong("pos_v_$id", 0L)

                    videos.add(
                        MediaItem(
                            id = id,
                            title = title,
                            artistOrResolution = res,
                            durationMs = duration,
                            sizeBytes = size,
                            uri = contentUri,
                            path = path,
                            folderName = folderName,
                            dateAdded = dateAdded,
                            type = MediaType.VIDEO,
                            isFavorite = isFav,
                            lastPositionMs = lastPos
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Permission or query exception handled gracefully
        }

        videos
    }

    suspend fun getAudios(): List<MediaItem> = withContext(Dispatchers.IO) {
        val audios = mutableListOf<MediaItem>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DATE_ADDED
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

        try {
            val cursor = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )

            cursor?.use {
                val idCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val nameCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                val titleCol = it.getColumnIndex(MediaStore.Audio.Media.TITLE)
                val artistCol = it.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                val durCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val sizeCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val dataCol = it.getColumnIndex(MediaStore.Audio.Media.DATA)
                val dateCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

                while (it.moveToNext()) {
                    val id = it.getLong(idCol)
                    val rawName = it.getString(nameCol) ?: "Audio $id"
                    val title = if (titleCol != -1 && !it.getString(titleCol).isNullOrEmpty()) {
                        it.getString(titleCol)
                    } else rawName

                    val artist = if (artistCol != -1 && !it.getString(artistCol).isNullOrEmpty()) {
                        it.getString(artistCol)
                    } else "Unknown Artist"

                    val duration = it.getLong(durCol)
                    val size = it.getLong(sizeCol)
                    val path = if (dataCol != -1) it.getString(dataCol) ?: "" else ""
                    val dateAdded = it.getLong(dateCol)

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    val folderName = if (path.isNotEmpty()) {
                        try {
                            File(path).parentFile?.name ?: "Music"
                        } catch (e: Exception) {
                            "Music"
                        }
                    } else "Music"

                    val isFav = favoritesPrefs.getBoolean("fav_a_$id", false)
                    val lastPos = historyPrefs.getLong("pos_a_$id", 0L)

                    audios.add(
                        MediaItem(
                            id = id,
                            title = title,
                            artistOrResolution = artist,
                            durationMs = duration,
                            sizeBytes = size,
                            uri = contentUri,
                            path = path,
                            folderName = folderName,
                            dateAdded = dateAdded,
                            type = MediaType.AUDIO,
                            isFavorite = isFav,
                            lastPositionMs = lastPos
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Permission or query exception handled
        }

        audios
    }

    suspend fun getFolders(videos: List<MediaItem>, audios: List<MediaItem>): List<MediaFolder> =
        withContext(Dispatchers.Default) {
            val folderMap = mutableMapOf<String, Pair<MutableList<MediaItem>, MutableList<MediaItem>>>()

            for (v in videos) {
                val fName = v.folderName
                val pair = folderMap.getOrPut(fName) { Pair(mutableListOf(), mutableListOf()) }
                pair.first.add(v)
            }
            for (a in audios) {
                val fName = a.folderName
                val pair = folderMap.getOrPut(fName) { Pair(mutableListOf(), mutableListOf()) }
                pair.second.add(a)
            }

            folderMap.map { (name, pair) ->
                val thumb = pair.first.firstOrNull()?.uri ?: pair.second.firstOrNull()?.uri
                val maxDate = maxOf(
                    pair.first.maxOfOrNull { it.dateAdded } ?: 0L,
                    pair.second.maxOfOrNull { it.dateAdded } ?: 0L
                )
                MediaFolder(
                    name = name,
                    path = pair.first.firstOrNull()?.path?.let { File(it).parent ?: "" } ?: "",
                    videoCount = pair.first.size,
                    audioCount = pair.second.size,
                    lastModified = maxDate,
                    thumbnailUri = thumb
                )
            }.sortedByDescending { it.videoCount + it.audioCount }
        }

    fun toggleFavorite(item: MediaItem): Boolean {
        val key = if (item.type == MediaType.VIDEO) "fav_v_${item.id}" else "fav_a_${item.id}"
        val current = favoritesPrefs.getBoolean(key, false)
        val newState = !current
        favoritesPrefs.edit().putBoolean(key, newState).apply()
        return newState
    }

    fun saveLastPosition(item: MediaItem, positionMs: Long) {
        val key = if (item.type == MediaType.VIDEO) "pos_v_${item.id}" else "pos_a_${item.id}"
        historyPrefs.edit().putLong(key, positionMs).apply()
    }
}
