package com.vedatv.app.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.vedatv.app.ui.screens.ChannelsScreen
import com.vedatv.app.ui.screens.PlaylistsScreen
import com.vedatv.app.ui.screens.PlayerScreen
import java.net.URLEncoder

@Composable
fun VedaTvApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "playlists") {
        composable("playlists") {
            PlaylistsScreen(
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
                onPlayChannel = { name, url ->
                    val encodedName = URLEncoder.encode(name, "UTF-8")
                    val encodedUrl = URLEncoder.encode(url, "UTF-8")
                    navController.navigate("player/$encodedName/$encodedUrl")
                }
            )
        }

        composable(
            route = "player/{name}/{url}",
            arguments = listOf(
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
