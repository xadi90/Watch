package com.example.converter

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import com.example.model.ConvertedAudioItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer

class VideoAudioConverter(private val context: Context) {

    private val convertedPrefs = context.getSharedPreferences("watch_converted_audio", Context.MODE_PRIVATE)

    val outputDir: File
        get() = File(context.filesDir, "extracted_audios").apply {
            if (!exists()) mkdirs()
        }

    suspend fun convertVideoToAudio(
        videoUri: Uri,
        outputFileName: String,
        format: String = "m4a", // m4a/aac or mp3 audio container
        bitrateKbps: Int = 192,
        onProgress: (Float) -> Unit
    ): Result<ConvertedAudioItem> = withContext(Dispatchers.IO) {
        val safeName = outputFileName.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
        val outFile = File(outputDir, "${safeName}_${System.currentTimeMillis()}.$format")

        val extractor = MediaExtractor()
        var muxer: MediaMuxer? = null

        try {
            extractor.setDataSource(context, videoUri, null)
            val trackCount = extractor.trackCount
            var audioTrackIndex = -1
            var audioFormat: MediaFormat? = null

            for (i in 0 until trackCount) {
                val mf = extractor.getTrackFormat(i)
                val mime = mf.getString(MediaFormat.KEY_MIME) ?: ""
                if (mime.startsWith("audio/")) {
                    audioTrackIndex = i
                    audioFormat = mf
                    break
                }
            }

            if (audioTrackIndex == -1 || audioFormat == null) {
                return@withContext Result.failure(Exception("No audio track detected in source video"))
            }

            extractor.selectTrack(audioTrackIndex)
            val durationUs = if (audioFormat.containsKey(MediaFormat.KEY_DURATION)) {
                audioFormat.getLong(MediaFormat.KEY_DURATION)
            } else 1L

            val muxerFormat = MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4
            muxer = MediaMuxer(outFile.absolutePath, muxerFormat)
            val muxerAudioTrack = muxer.addTrack(audioFormat)
            muxer.start()

            val maxBufferSize = if (audioFormat.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
                audioFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
            } else 1024 * 1024

            val buffer = ByteBuffer.allocate(maxBufferSize)
            val bufferInfo = MediaCodec.BufferInfo()

            while (true) {
                val sampleSize = extractor.readSampleData(buffer, 0)
                if (sampleSize < 0) {
                    break
                }

                bufferInfo.offset = 0
                bufferInfo.size = sampleSize
                bufferInfo.presentationTimeUs = extractor.sampleTime
                bufferInfo.flags = extractor.sampleFlags

                muxer.writeSampleData(muxerAudioTrack, buffer, bufferInfo)

                if (durationUs > 0) {
                    val progress = (bufferInfo.presentationTimeUs.toFloat() / durationUs.toFloat()).coerceIn(0f, 1f)
                    onProgress(progress)
                }

                extractor.advance()
            }

            onProgress(1.0f)

            val item = ConvertedAudioItem(
                id = outFile.name,
                fileName = outFile.name,
                filePath = outFile.absolutePath,
                durationMs = durationUs / 1000,
                sizeBytes = outFile.length(),
                format = format.uppercase(),
                bitrateKbps = bitrateKbps,
                timestamp = System.currentTimeMillis()
            )

            saveConvertedItem(item)

            Result.success(item)
        } catch (e: Exception) {
            if (outFile.exists()) outFile.delete()
            Result.failure(e)
        } finally {
            try {
                muxer?.stop()
                muxer?.release()
            } catch (ignored: Exception) {}
            try {
                extractor.release()
            } catch (ignored: Exception) {}
        }
    }

    private fun saveConvertedItem(item: ConvertedAudioItem) {
        val set = convertedPrefs.getStringSet("list", emptySet())?.toMutableSet() ?: mutableSetOf()
        val str = "${item.id}::${item.fileName}::${item.filePath}::${item.durationMs}::${item.sizeBytes}::${item.format}::${item.bitrateKbps}::${item.timestamp}"
        set.add(str)
        convertedPrefs.edit().putStringSet("list", set).apply()
    }

    fun getConvertedItems(): List<ConvertedAudioItem> {
        val set = convertedPrefs.getStringSet("list", emptySet()) ?: emptySet()
        val list = mutableListOf<ConvertedAudioItem>()
        for (s in set) {
            val parts = s.split("::")
            if (parts.size >= 8) {
                val f = File(parts[2])
                if (f.exists()) {
                    list.add(
                        ConvertedAudioItem(
                            id = parts[0],
                            fileName = parts[1],
                            filePath = parts[2],
                            durationMs = parts[3].toLongOrNull() ?: 0L,
                            sizeBytes = parts[4].toLongOrNull() ?: 0L,
                            format = parts[5],
                            bitrateKbps = parts[6].toIntOrNull() ?: 192,
                            timestamp = parts[7].toLongOrNull() ?: 0L
                        )
                    )
                }
            }
        }
        return list.sortedByDescending { it.timestamp }
    }
}
