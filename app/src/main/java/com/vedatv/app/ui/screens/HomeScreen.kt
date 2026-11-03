package com.vedatv.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.vedatv.app.R
import com.vedatv.app.data.db.ChannelEntity
import com.vedatv.app.ui.viewmodels.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenPlaylist: (Long) -> Unit,
    onAddPlaylist: () -> Unit,
    onPlayChannel: (String, String) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val playlists by viewModel.playlists
    val favorites by viewModel.favorites
    val recent by viewModel.recentlyWatched

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("VedaTV") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddPlaylist) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_playlist))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (recent.isNotEmpty()) {
                item {
                    HomeSection(title = "Недавно смотрели") {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(recent, key = { it.id }) { channel ->
                                ChannelTile(channel = channel, onClick = {
                                    onPlayChannel(channel.name, channel.url)
                                })
                            }
                        }
                    }
                }
            }

            if (favorites.isNotEmpty()) {
                item {
                    HomeSection(title = stringResource(R.string.favorites)) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(favorites, key = { it.id }) { channel ->
                                ChannelTile(channel = channel, onClick = {
                                    onPlayChannel(channel.name, channel.url)
                                })
                            }
                        }
                    }
                }
            }

            item {
                HomeSection(title = stringResource(R.string.playlists)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (playlists.isEmpty()) {
                            Text(stringResource(R.string.no_playlists))
                        }

                        playlists.forEach { playlist ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { onOpenPlaylist(playlist.id) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(playlist.name, style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            playlist.url,
                                            style = MaterialTheme.typography.bodySmall,
                                            maxLines = 1
                                        )
                                    }
                                    IconButton(onClick = { viewModel.refresh(playlist) }) {
                                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.refresh))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        content()
    }
}

@Composable
private fun ChannelTile(
    channel: ChannelEntity,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.size(width = 150.dp, height = 110.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = channel.logo.ifBlank { null },
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = channel.name,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
