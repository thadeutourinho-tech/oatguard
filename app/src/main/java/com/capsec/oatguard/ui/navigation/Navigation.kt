package com.capsec.oatguard.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.capsec.oatguard.ui.screens.HomeScreen
import com.capsec.oatguard.ui.screens.PixResultScreen
import com.capsec.oatguard.ui.screens.QRScannerScreen
import com.capsec.oatguard.ui.screens.ResultScreen
import com.capsec.oatguard.ui.screens.SplashScreen
import com.capsec.oatguard.viewmodel.QRValidatorViewModel

object Routes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val SCANNER = "scanner"
    const val RESULT = "result"
    const val PIX_RESULT = "pix_result"
}

/**
 * Grafo de navegação do OatGuard: Splash -> Home -> Scanner -> Result
 * (ou Scanner -> PixResult quando o QR é uma chave PIX).
 *
 * O [QRValidatorViewModel] é criado uma única vez aqui (escopo do NavHost,
 * que por padrão é a Activity) e compartilhado entre Scanner e Result — a
 * URL escaneada trafega pelo estado do ViewModel em vez de argumento de
 * navegação, evitando problemas de encoding com URLs arbitrárias.
 */
@Composable
fun Navigation(navController: NavHostController = rememberNavController()) {
    val qrValidatorViewModel: QRValidatorViewModel = viewModel(factory = QRValidatorViewModel.Factory)

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
                onQrDetected = { rawValue ->
                    val isPix = qrValidatorViewModel.onQrScanned(rawValue)
                    navController.navigate(if (isPix) Routes.PIX_RESULT else Routes.RESULT)
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

        composable(Routes.PIX_RESULT) {
            val pixResult by qrValidatorViewModel.pixResult.collectAsStateWithLifecycle()
            // Não limpa o estado aqui: a tela ainda aparece durante a animação de saída,
            // e o próximo scan sobrescreve pixResult de qualquer forma.
            val backToCamera: () -> Unit = { navController.popBackStack(Routes.SCANNER, inclusive = false) }
            val pix = pixResult
            if (pix != null) {
                PixResultScreen(result = pix, onBackToCamera = backToCamera)
            } else {
                // Sem chave (ex.: estado perdido após morte do processo) — não há o que exibir.
                LaunchedEffect(Unit) { navController.popBackStack(Routes.SCANNER, inclusive = false) }
            }
        }
    }
}
