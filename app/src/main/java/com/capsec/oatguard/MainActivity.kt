package com.capsec.oatguard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.capsec.oatguard.ui.navigation.Navigation
import com.capsec.oatguard.ui.theme.OatGuardTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Precisa vir antes de super.onCreate() para trocar SplashScreenTheme -> Theme.OatGuard.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OatGuardTheme {
                Navigation()
            }
        }
    }
}
