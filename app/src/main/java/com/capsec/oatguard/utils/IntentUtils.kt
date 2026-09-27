package com.capsec.oatguard.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Abre uma URL no navegador padrão via ACTION_VIEW. Usado por InfoButton,
 * banners (CapSEC/OatCall) e o botão "Abrir Link" da ResultScreen.
 * Envolve em try-catch pois, embora raro, pode não haver navegador instalado.
 */
fun openUrl(context: Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (_: ActivityNotFoundException) {
        // Sem navegador instalado — falha silenciosa (caso raríssimo em Android).
    }
}
