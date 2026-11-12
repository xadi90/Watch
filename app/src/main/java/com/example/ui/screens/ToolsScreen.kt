package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MediaItem
import com.example.model.VaultItem
import com.example.ui.components.formatTime
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.WatchFlameGradient
import com.example.ui.theme.WatchNeonGradient
import com.example.ui.theme.WatchOrange
import com.example.ui.theme.WatchPink
import com.example.ui.theme.WatchPurple
import com.example.ui.theme.WatchRed
import com.example.viewmodel.WatchUiState
import com.example.viewmodel.WatchViewModel

@Composable
fun ToolsScreen(
    uiState: WatchUiState,
    viewModel: WatchViewModel
) {
    val context = LocalContext.current
    var selectedToolSection by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tools Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "MULTIMEDIA TOOLS",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = Color.White
            )
            Text(
                text = "Video to MP3 • Privacy Vault • Sharing • Floating Player",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
        }

        // Section Selector Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ToolTabButton(
                label = "Video to MP3",
                selected = selectedToolSection == null || selectedToolSection == "CONVERTER",
                onClick = { selectedToolSection = "CONVERTER" }
            )
            ToolTabButton(
                label = "Private Vault",
                selected = selectedToolSection == "VAULT",
                onClick = { selectedToolSection = "VAULT" }
            )
        }

        if (selectedToolSection == "VAULT") {
            PrivateVaultSection(uiState = uiState, viewModel = viewModel)
        } else {
            VideoToMp3Section(uiState = uiState, viewModel = viewModel)
        }
    }
}

@Composable
fun ToolTabButton(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = if (selected) WatchRed else SurfaceVariantDark
    ) {
        Text(
            text = label,
            color = Color.White,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun VideoToMp3Section(
    uiState: WatchUiState,
    viewModel: WatchViewModel
) {
    val context = LocalContext.current
    var selectedVideo by remember { mutableStateOf<MediaItem?>(null) }
    var selectedFormat by remember { mutableStateOf("M4A") }
    var selectedBitrate by remember { mutableIntStateOf(192) }
    var showFormatMenu by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(WatchFlameGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Transform, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(text = "Extract Audio from Video", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(text = "High-speed local audio demuxing & remuxing", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(text = "1. Select Source Video:", color = WatchOrange, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (selectedVideo == null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = SurfaceVariantDark
                        ) {
                            Text(
                                text = if (uiState.allVideos.isEmpty()) "No videos found in storage." else "Pick a video from the list below",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 13.sp,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    } else {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = WatchPink.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Audiotrack, contentDescription = null, tint = WatchPink)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = selectedVideo?.title ?: "",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Output Format:", color = Color.White, fontSize = 13.sp)
                            Text(text = "$selectedFormat ($selectedBitrate kbps)", color = WatchPink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                selectedFormat = if (selectedFormat == "M4A") "AAC" else "M4A"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Switch: $selectedFormat")
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Progress indicator if converting
                    if (uiState.isConverting) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            LinearProgressIndicator(
                                progress = { uiState.conversionProgress ?: 0.5f },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = WatchRed
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Extracting audio... ${((uiState.conversionProgress ?: 0f) * 100).toInt()}%",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                selectedVideo?.let { v ->
                                    viewModel.convertVideo(v, selectedFormat, selectedBitrate) { success ->
                                        Toast.makeText(
                                            context,
                                            if (success) "Extraction successful!" else "Extraction failed. Unsupported codec.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            },
                            enabled = selectedVideo != null,
                            colors = ButtonDefaults.buttonColors(containerColor = WatchRed),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("convert_audio_button")
                        ) {
                            Text("Extract Audio Now", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "DEVICE VIDEOS (Tap to select for extraction):",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        items(uiState.allVideos.take(10), key = { "conv_sel_${it.id}" }) { v ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { selectedVideo = v },
                shape = RoundedCornerShape(12.dp),
                color = if (selectedVideo?.id == v.id) WatchPink.copy(alpha = 0.25f) else SurfaceCard
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = v.title,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = formatTime(v.durationMs),
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Extracted list
        if (uiState.convertedAudios.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "COMPLETED AUDIO EXTRACTIONS (${uiState.convertedAudios.size}):",
                    color = WatchOrange,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            items(uiState.convertedAudios, key = { "ext_${it.id}" }) { item ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceCard
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Audiotrack, contentDescription = null, tint = WatchPink)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = item.fileName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(text = "${item.format} • ${formatTime(item.durationMs)}", color = Color.Gray, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PrivateVaultSection(
    uiState: WatchUiState,
    viewModel: WatchViewModel
) {
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        if (!uiState.isVaultUnlocked) {
            // Locked / Setup View
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(WatchFlameGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (uiState.vaultRequiresSetup) "Setup Vault Master PIN" else "Unlock Private Media Vault",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Protected with Android KeyStore & Hardware AES-256-GCM. Videos are encrypted on flash memory.",
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = {
                            if (it.length <= 6) {
                                enteredPin = it
                                pinError = false
                            }
                        },
                        placeholder = { Text("Enter 4-6 digit PIN", color = Color.Gray) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        isError = pinError,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WatchRed,
                            unfocusedBorderColor = SurfaceVariantDark,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("vault_pin_input")
                    )

                    if (pinError) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Incorrect PIN. Please try again.", color = WatchRed, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            if (uiState.vaultRequiresSetup) {
                                if (enteredPin.length >= 4) {
                                    viewModel.setupVaultPin(enteredPin)
                                } else {
                                    pinError = true
                                }
                            } else {
                                val ok = viewModel.unlockVault(enteredPin)
                                if (!ok) pinError = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WatchRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("unlock_vault_button")
                    ) {
                        Text(if (uiState.vaultRequiresSetup) "Set PIN & Create Vault" else "Unlock Vault", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Unlocked Vault View
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "VAULT SECURED", color = WatchPink, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(text = "${uiState.vaultItems.size} encrypted media files", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                }

                Button(
                    onClick = { viewModel.lockVault() },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Lock")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.vaultItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No media in private vault", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Items imported here are encrypted with AES-256.", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(uiState.vaultItems, key = { "vault_${it.id}" }) { item ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceCard
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = WatchOrange)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = item.originalFileName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text(text = "AES-256-GCM Encrypted", color = Color.Gray, fontSize = 11.sp)
                            }
                            Button(
                                onClick = { viewModel.deleteVaultItem(item) },
                                colors = ButtonDefaults.buttonColors(containerColor = WatchRed.copy(alpha = 0.2f))
                            ) {
                                Text("Delete", color = WatchRed, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
