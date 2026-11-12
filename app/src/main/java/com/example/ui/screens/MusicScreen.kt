package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MediaItem
import com.example.ui.components.formatTime
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.WatchFlameGradient
import com.example.ui.theme.WatchNeonGradient
import com.example.ui.theme.WatchOrange
import com.example.ui.theme.WatchPink
import com.example.ui.theme.WatchRed
import com.example.viewmodel.WatchUiState
import com.example.viewmodel.WatchViewModel

@Composable
fun MusicScreen(
    uiState: WatchUiState,
    viewModel: WatchViewModel
) {
    val audios = uiState.allAudios

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Music Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "MUSIC",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = Color.White
                )
                Text(
                    text = "${audios.size} audio tracks available",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }

            // Shuffle play all
            if (audios.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .clickable {
                            val random = audios.random()
                            viewModel.playAudio(random)
                        }
                        .testTag("shuffle_all_music_button"),
                    shape = RoundedCornerShape(20.dp),
                    color = WatchRed
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Shuffle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Shuffle Play", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Music Categories / Quick filters
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickFilterChip(label = "All Songs (${audios.size})", selected = true)
            QuickFilterChip(label = "Favorites (${audios.count { it.isFavorite }})", selected = false)
            QuickFilterChip(label = "Extracted (${uiState.convertedAudios.size})", selected = false)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 10.dp, bottom = 120.dp)
        ) {
            if (audios.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(60.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No audio tracks found", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Supported: MP3, AAC, M4A, FLAC, WAV, OGG, OPUS", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            }

            items(audios, key = { "audio_${it.id}" }) { audio ->
                val isCurrentPlaying = uiState.selectedAudioForPlayback?.id == audio.id

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.playAudio(audio) }
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                        .testTag("audio_track_${audio.id}"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .then(
                                if (isCurrentPlaying) Modifier.background(WatchFlameGradient)
                                else Modifier.background(SurfaceVariantDark)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = if (isCurrentPlaying) Color.White else WatchPink,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = audio.title,
                            color = if (isCurrentPlaying) WatchPink else Color.White,
                            fontSize = 14.sp,
                            fontWeight = if (isCurrentPlaying) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "${audio.artistOrResolution} • ${formatTime(audio.durationMs)}",
                            color = Color.White.copy(alpha = 0.55f),
                            fontSize = 12.sp
                        )
                    }

                    IconButton(onClick = { viewModel.toggleFavorite(audio) }) {
                        Icon(
                            if (audio.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (audio.isFavorite) WatchRed else Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickFilterChip(label: String, selected: Boolean) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (selected) WatchPink.copy(alpha = 0.2f) else SurfaceVariantDark
    ) {
        Text(
            text = label,
            color = if (selected) WatchPink else Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}
