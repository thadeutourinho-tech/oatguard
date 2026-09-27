package com.capsec.oatguard.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.capsec.oatguard.ui.screens.HomeScreen
import com.capsec.oatguard.ui.screens.QRScannerScreen
import com.capsec.oatguard.ui.screens.ResultScreen
import com.capsec.oatguard.ui.screens.SplashScreen
import com.capsec.oatguard.viewmodel.QRValidatorViewModel

object Routes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val SCANNER = "scanner"
    const val RESULT = "result"
}

/**
 * Grafo de navegação do OatGuard: Splash -> Home -> Scanner -> Result.
 *
 * O [QRValidatorViewModel] é criado uma única vez aqui (escopo do NavHost,
 * que por padrão é a Activity) e compartilhado entre Scanner e Result — a
 * URL escaneada trafega pelo estado do ViewModel em vez de argumento de
 * navegação, evitando problemas de encoding com URLs arbitrárias.
 */
@Composable
fun Navigation(navController: NavHostController = rememberNavController()) {
    val qrValidatorViewModel: QRValidatorViewModel = viewModel()

    NavHost(navController = navController, startDestination = Routes.SPLASH) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onFinished = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onScanClick = { navController.navigate(Routes.SCANNER) }
            )
        }

        composable(Routes.SCANNER) {
            QRScannerScreen(
                onQrDetected = { url ->
                    qrValidatorViewModel.validateQRCode(url)
                    navController.navigate(Routes.RESULT)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.RESULT) {
            ResultScreen(
                viewModel = qrValidatorViewModel,
                onScanAnother = {
                    qrValidatorViewModel.reset()
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = false }
                    }
                }
            )
        }
    }
}
