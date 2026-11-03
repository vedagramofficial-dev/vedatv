package com.vedatv.app.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vedatv.app.ui.screens.ChannelsScreen
import com.vedatv.app.ui.screens.GroupsScreen
import com.vedatv.app.ui.screens.HomeScreen
import com.vedatv.app.ui.screens.PlayerScreen
import java.net.URLEncoder

@Composable
fun VedaTvApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onOpenPlaylist = { playlistId ->
                    navController.navigate("channels/$playlistId")
                },
                onAddPlaylist = {
                    navController.navigate("playlists")
                },
                onPlayChannel = { channel ->
                    val encodedName = URLEncoder.encode(channel.name, "UTF-8")
                    val encodedUrl = URLEncoder.encode(channel.url, "UTF-8")
                    navController.navigate("player/${channel.id}/$encodedName/$encodedUrl")
                }
            )
        }

        composable("playlists") {
            com.vedatv.app.ui.screens.PlaylistsScreen(
                onOpenPlaylist = { playlistId ->
                    navController.navigate("channels/$playlistId")
                }
            )
        }

        composable(
            route = "channels/{playlistId}",
            arguments = listOf(navArgument("playlistId") { type = NavType.LongType })
        ) { entry ->
            val playlistId = entry.arguments?.getLong("playlistId") ?: return@composable
            ChannelsScreen(
                playlistId = playlistId,
                onBack = { navController.popBackStack() },
                onOpenGroups = {
                    navController.navigate("groups/$playlistId")
                },
                onPlayChannel = { channel ->
                    val encodedName = URLEncoder.encode(channel.name, "UTF-8")
                    val encodedUrl = URLEncoder.encode(channel.url, "UTF-8")
                    navController.navigate("player/${channel.id}/$encodedName/$encodedUrl")
                }
            )
        }

        composable(
            route = "groups/{playlistId}",
            arguments = listOf(navArgument("playlistId") { type = NavType.LongType })
        ) { entry ->
            val playlistId = entry.arguments?.getLong("playlistId") ?: return@composable
            GroupsScreen(
                playlistId = playlistId,
                onBack = { navController.popBackStack() },
                onOpenGroup = { group ->
                    val encodedGroup = URLEncoder.encode(group, "UTF-8")
                    navController.navigate("channels/$playlistId?group=$encodedGroup")
                }
            )
        }

        composable(
            route = "player/{channelId}/{name}/{url}",
            arguments = listOf(
                navArgument("channelId") { type = NavType.LongType },
                navArgument("name") { type = NavType.StringType },
                navArgument("url") { type = NavType.StringType }
            )
        ) { entry ->
            val name = entry.arguments?.getString("name").orEmpty()
            val url = entry.arguments?.getString("url").orEmpty()

            PlayerScreen(
                channelName = name,
                channelUrl = url,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
