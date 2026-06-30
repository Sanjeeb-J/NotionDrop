package com.sanjeeb.notiondrop.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sanjeeb.notiondrop.ui.screens.HelpScreen
import com.sanjeeb.notiondrop.ui.screens.HistoryScreen
import com.sanjeeb.notiondrop.ui.screens.MainScreen
import com.sanjeeb.notiondrop.ui.screens.SettingsScreen

sealed class Route(val route: String) {
    object Main : Route("main")
    object History : Route("history")
    object Settings : Route("settings")
    object Help : Route("help")
}

@Composable
fun NotionDropNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Route.Main.route
    ) {
        composable(Route.Main.route) {
            MainScreen(
                onNavigateToHistory = { navController.navigate(Route.History.route) },
                onNavigateToSettings = { navController.navigate(Route.Settings.route) }
            )
        }
        composable(Route.History.route) {
            HistoryScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Route.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToHelp = { navController.navigate(Route.Help.route) }
            )
        }
        composable(Route.Help.route) {
            HelpScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
