package com.example

import android.Manifest
import android.app.PictureInPictureParams
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.FullMusicPlayerSheet
import com.example.ui.components.MiniMusicPlayer
import com.example.ui.components.VideoPlayerScreen
import com.example.ui.screens.FoldersScreen
import com.example.ui.screens.MeScreen
import com.example.ui.screens.MusicScreen
import com.example.ui.screens.ToolsScreen
import com.example.ui.screens.VideoHomeScreen
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.WatchPink
import com.example.ui.theme.WatchRed
import com.example.ui.theme.WatchTheme
import com.example.viewmodel.NavigationTab
import com.example.viewmodel.WatchViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: WatchViewModel by viewModels()

    private val requestMediaPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions.values.any { it }
        viewModel.setPermissionGranted(isGranted)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        checkAndRequestPermissions()

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            var showFullAudioPlayer by remember { mutableStateOf(false) }

            WatchTheme(darkTheme = uiState.isDarkTheme) {
                // If a video is playing, show the full-screen video player
                if (uiState.selectedVideoForPlayback != null) {
                    VideoPlayerScreen(
                        videoItem = uiState.selectedVideoForPlayback!!,
                        onClose = { viewModel.playVideo(null) },
                        onSavePosition = { pos ->
                            uiState.selectedVideoForPlayback?.let {
                                viewModel.saveVideoPosition(it, pos)
                            }
                        },
                        onEnterPiP = { enterPictureInPicture() }
                    )
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = MaterialTheme.colorScheme.background,
                        bottomBar = {
                            Column {
                                // Floating Mini Music Player if audio is active
                                if (uiState.selectedAudioForPlayback != null) {
                                    MiniMusicPlayer(
                                        currentAudio = uiState.selectedAudioForPlayback!!,
                                        isPlaying = uiState.isAudioPlaying,
                                        onPlayPause = { viewModel.toggleAudioPlayPause() },
                                        onNext = { viewModel.nextAudio() },
                                        onExpand = { showFullAudioPlayer = true }
                                    )
                                }

                                // 5 Bottom Navigation Items: VIDEO, MUSIC, FOLDERS, TOOLS, ME
                                NavigationBar(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .navigationBarsPadding()
                                        .testTag("bottom_nav_bar"),
                                    containerColor = SurfaceDark,
                                    tonalElevation = 8.dp
                                ) {
                                    val tabs = listOf(
                                        Triple(NavigationTab.VIDEO, "VIDEO", Pair(Icons.Default.Movie, Icons.Outlined.Movie)),
                                        Triple(NavigationTab.MUSIC, "MUSIC", Pair(Icons.Default.MusicNote, Icons.Outlined.MusicNote)),
                                        Triple(NavigationTab.FOLDERS, "FOLDERS", Pair(Icons.Default.Folder, Icons.Outlined.Folder)),
                                        Triple(NavigationTab.TOOLS, "TOOLS", Pair(Icons.Default.Build, Icons.Outlined.Build)),
                                        Triple(NavigationTab.ME, "ME", Pair(Icons.Default.Person, Icons.Outlined.Person))
                                    )

                                    tabs.forEach { (tab, label, icons) ->
                                        val isSelected = uiState.currentTab == tab
                                        NavigationBarItem(
                                            selected = isSelected,
                                            onClick = { viewModel.selectTab(tab) },
                                            icon = {
                                                Icon(
                                                    if (isSelected) icons.first else icons.second,
                                                    contentDescription = label
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = label,
                                                    fontSize = 11.sp
                                                )
                                            },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = Color.White,
                                                selectedTextColor = WatchRed,
                                                indicatorColor = WatchRed,
                                                unselectedIconColor = Color.White.copy(alpha = 0.5f),
                                                unselectedTextColor = Color.White.copy(alpha = 0.5f)
                                            ),
                                            modifier = Modifier.testTag("nav_tab_${label.lowercase()}")
                                        )
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (uiState.currentTab) {
                                NavigationTab.VIDEO -> VideoHomeScreen(
                                    uiState = uiState,
                                    viewModel = viewModel,
                                    onRequestPermission = { checkAndRequestPermissions() }
                                )
                                NavigationTab.MUSIC -> MusicScreen(
                                    uiState = uiState,
                                    viewModel = viewModel
                                )
                                NavigationTab.FOLDERS -> FoldersScreen(
                                    uiState = uiState,
                                    viewModel = viewModel
                                )
                                NavigationTab.TOOLS -> ToolsScreen(
                                    uiState = uiState,
                                    viewModel = viewModel
                                )
                                NavigationTab.ME -> MeScreen(
                                    uiState = uiState,
                                    viewModel = viewModel
                                )
                            }

                            // Full-screen Music Player Modal Sheet
                            AnimatedVisibility(
                                visible = showFullAudioPlayer && uiState.selectedAudioForPlayback != null,
                                enter = slideInVertically(initialOffsetY = { it }),
                                exit = slideOutVertically(targetOffsetY = { it })
                            ) {
                                uiState.selectedAudioForPlayback?.let { audio ->
                                    FullMusicPlayerSheet(
                                        audio = audio,
                                        isPlaying = uiState.isAudioPlaying,
                                        isShuffle = uiState.isShuffle,
                                        isRepeat = uiState.isRepeat,
                                        equalizerPreset = uiState.equalizerPreset,
                                        onPlayPause = { viewModel.toggleAudioPlayPause() },
                                        onNext = { viewModel.nextAudio() },
                                        onPrevious = { viewModel.previousAudio() },
                                        onToggleShuffle = { viewModel.toggleShuffle() },
                                        onToggleRepeat = { viewModel.toggleRepeat() },
                                        onToggleFavorite = { viewModel.toggleFavorite(audio) },
                                        onSelectPreset = { viewModel.setEqualizerPreset(it) },
                                        onDismiss = { showFullAudioPlayer = false }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_VIDEO)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_AUDIO)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestMediaPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            viewModel.setPermissionGranted(true)
        }
    }

    private fun enterPictureInPicture() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(16, 9))
                .build()
            enterPictureInPictureMode(params)
        }
    }
}
