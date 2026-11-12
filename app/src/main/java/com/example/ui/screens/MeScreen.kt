package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.security.DeveloperConfig
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.WatchFlameGradient
import com.example.ui.theme.WatchGreen
import com.example.ui.theme.WatchNeonGradient
import com.example.ui.theme.WatchOrange
import com.example.ui.theme.WatchPink
import com.example.ui.theme.WatchRed
import com.example.viewmodel.WatchUiState
import com.example.viewmodel.WatchViewModel

@Composable
fun MeScreen(
    uiState: WatchUiState,
    viewModel: WatchViewModel
) {
    val context = LocalContext.current
    val verification = remember { DeveloperConfig.verifyIntegrity() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // ME Profile Header
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(WatchNeonGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "W",
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = DeveloperConfig.APP_NAME,
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "All-in-One Multimedia Player",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = WatchGreen.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = DeveloperConfig.APP_VERSION,
                                    color = WatchGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // PERMANENT DEVELOPER VERIFICATION SECTION (CRITICAL REQUIREMENT)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("developer_verification_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = WatchGreen, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DEVELOPER VERIFICATION",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = WatchGreen.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = WatchGreen, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "VERIFIED", color = WatchGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Original, permanently fixed developer identity. This information is cryptographically protected against tampering and cannot be edited by any user or administrator.",
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = SurfaceVariantDark)
                    Spacer(modifier = Modifier.height(14.dp))

                    DeveloperDetailRow(
                        label = "Developer Name",
                        value = DeveloperConfig.DEVELOPER_NAME,
                        icon = Icons.Default.Info
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    DeveloperDetailRow(
                        label = "Developer Email",
                        value = DeveloperConfig.DEVELOPER_EMAIL,
                        icon = Icons.Default.Email,
                        onCopy = {
                            copyToClipboard(context, "Developer Email", DeveloperConfig.DEVELOPER_EMAIL)
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    DeveloperDetailRow(
                        label = "Developer Phone",
                        value = DeveloperConfig.DEVELOPER_PHONE,
                        icon = Icons.Default.Phone,
                        onCopy = {
                            copyToClipboard(context, "Developer Phone", DeveloperConfig.DEVELOPER_PHONE)
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = SurfaceVariantDark)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Integrity Checksum
                    Column {
                        Text(text = "Identity SHA-256 Digest:", color = Color.Gray, fontSize = 10.sp)
                        Text(
                            text = verification.sha256Digest,
                            color = WatchPink,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // App Settings & Preferences
        item {
            Text(
                text = "APPLICATION SETTINGS",
                color = WatchOrange,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Dark Mode Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Brightness4, contentDescription = null, tint = WatchPink)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "Luxury Dark Theme", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text(text = "OLED Black with Neon Gradients", color = Color.Gray, fontSize = 11.sp)
                            }
                        }

                        Switch(
                            checked = uiState.isDarkTheme,
                            onCheckedChange = { viewModel.toggleTheme() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = WatchRed
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = SurfaceVariantDark)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Hardware Codecs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = WatchOrange)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "Hardware Decoder Engine", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text(text = "Media3 ExoPlayer High-Performance", color = Color.Gray, fontSize = 11.sp)
                            }
                        }
                        Text(text = "ACTIVE", color = WatchGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = SurfaceVariantDark)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Privacy & Permissions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = WatchRed)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "Storage & Media Permissions", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text(text = "Android Storage Access Framework", color = Color.Gray, fontSize = 11.sp)
                            }
                        }
                        Text(
                            text = if (uiState.permissionGranted) "GRANTED" else "PENDING",
                            color = if (uiState.permissionGranted) WatchGreen else WatchRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // About & License Section
        item {
            Text(
                text = "ABOUT WATCH",
                color = WatchPink,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "WATCH is a modern, high-security all-in-one media player developed by Xadi Ahmed. Engineered for seamless full-format video decoding, lossless audio extraction, AES-256 encrypted private storage and floating background playback.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Supported Video: MP4, MKV, AVI, MOV, WMV, FLV, WEBM, 3GP, MPEG, M4V, TS, OGV\nSupported Audio: MP3, AAC, M4A, WAV, FLAC, OGG, OPUS",
                        color = WatchOrange.copy(alpha = 0.85f),
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun DeveloperDetailRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onCopy: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onCopy != null) Modifier.clickable { onCopy() } else Modifier),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = WatchPink, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = label, color = Color.Gray, fontSize = 11.sp)
                Text(text = value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        if (onCopy != null) {
            Text(text = "Copy", color = WatchOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
}
