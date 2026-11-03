package com.vedatv.app.ui.screens

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.vedatv.app.ui.viewmodels.EpgViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    channelName: String,
    channelUrl: String,
    channelTvgId: String = "",
    onBack: () -> Unit,
    epgViewModel: EpgViewModel = viewModel()
) {
    val context = LocalContext.current
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var retryToken by remember { mutableStateOf(0) }
    var catchUpOffset by remember { mutableStateOf(0L) }

    LaunchedEffect(channelTvgId) {
        epgViewModel.load(channelTvgId)
    }

    val currentProgram by epgViewModel.currentProgram.collectAsState()
    val programs by epgViewModel.programs.collectAsState()

    val player = remember(channelUrl, retryToken, catchUpOffset) {
        ExoPlayer.Builder(context)
            .build()
            .apply {
                setMediaItem(MediaItem.fromUri(channelUrl))
                prepare()
                playWhenReady = true

                if (catchUpOffset > 0) {
                    seekTo(catchUpOffset)
                }

                addListener(object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        errorMessage = error.errorCodeName
                    }
                })
            }
    }

    DisposableEffect(Unit) {
        onDispose {
            player.release()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(channelName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxWidth(),
                    factory = { context ->
                        PlayerView(context).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            useController = true
                            this.player = player
                        }
                    },
                    update = { view ->
                        view.player = player
                    }
                )
            }

            errorMessage?.let { error ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Ошибка воспроизведения",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = {
                            errorMessage = null
                            retryToken++
                        },
                        modifier = Modifier.padding(top = 12.dp)
                    ) {
                        Text("Повторить")
                    }
                }
            }

            currentProgram?.let { program ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Сейчас в эфире",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = program.title,
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (program.description.isNotBlank()) {
                            Text(
                                text = program.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (catchUpOffset == 0L) {
                            Button(
                                onClick = {
                                    catchUpOffset = epgViewModel.getCatchUpOffset(program)
                                },
                                modifier = Modifier.padding(top = 12.dp)
                            ) {
                                Text("Смотреть с начала")
                            }
                        } else {
                            Button(
                                onClick = {
                                    catchUpOffset = 0L
                                },
                                modifier = Modifier.padding(top = 12.dp)
                            ) {
                                Text("Вернуться к прямому эфиру")
                            }
                        }
                    }
                }
            }

            if (programs.isNotEmpty()) {
                Text(
                    text = "Далее",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(programs, key = { it.id }) { program ->
                        ProgramCard(
                            program = program,
                            onWatchFromStart = {
                                catchUpOffset = epgViewModel.getCatchUpOffset(program)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgramCard(
    program: com.vedatv.app.data.db.EpgProgramEntity,
    onWatchFromStart: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = formatTime(program.startTime) + " — " + formatTime(program.endTime),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = program.title,
                style = MaterialTheme.typography.bodyLarge
            )

            Button(
                onClick = onWatchFromStart,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Смотреть с начала")
            }
        }
    }
}

private fun formatTime(timestamp: Long): String {
    if (timestamp <= 0) return ""
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
}
