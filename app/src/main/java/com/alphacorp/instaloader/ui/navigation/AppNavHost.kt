package com.alphacorp.instaloader.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alphacorp.instaloader.data.repository.DownloadRepository
import com.alphacorp.instaloader.data.repository.SessionRepository
import com.alphacorp.instaloader.domain.usecase.ParseInstagramInputUseCase
import com.alphacorp.instaloader.ui.home.HomeScreen
import com.alphacorp.instaloader.ui.home.HomeViewModel
import com.alphacorp.instaloader.ui.login.LoginScreen
import com.alphacorp.instaloader.ui.login.LoginViewModel
import com.alphacorp.instaloader.ui.settings.SettingsScreen
import com.alphacorp.instaloader.ui.settings.SettingsViewModel

object Routes {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val LOGIN = "login"
}

@Composable
fun AppNavHost(
    downloadRepository: DownloadRepository,
    sessionRepository: SessionRepository,
    parseInput: ParseInstagramInputUseCase,
    onStartDownload: (String) -> Unit,
    onCancelDownload: () -> Unit,
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            val viewModel: HomeViewModel = viewModel(
                factory = HomeViewModel.Factory(
                    downloadRepository = downloadRepository,
                    parseInput = parseInput,
                    onStartDownload = onStartDownload,
                    onCancelDownload = onCancelDownload,
                ),
            )
            HomeScreen(
                viewModel = viewModel,
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onOpenLogin = { navController.navigate(Routes.LOGIN) },
            )
        }
        composable(Routes.SETTINGS) {
            val viewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModel.Factory(downloadRepository),
            )
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.LOGIN) {
            val viewModel: LoginViewModel = viewModel(
                factory = LoginViewModel.Factory(sessionRepository, downloadRepository),
            )
            LoginScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
