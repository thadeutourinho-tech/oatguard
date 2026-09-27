package com.capsec.oatguard.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.capsec.oatguard.R
import com.capsec.oatguard.utils.Constants
import com.capsec.oatguard.utils.openUrl
import java.util.Locale

/**
 * Botão de informações exibido no header. Abre a documentação/privacidade
 * do OatGuard no idioma do device (com fallback para PT), via navegador padrão.
 */
@Composable
fun InfoButton() {
    val context = LocalContext.current

    IconButton(onClick = {
        openUrl(context, Constants.getDocsUrl(Locale.getDefault().language))
    }) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = context.getString(R.string.info_button_description)
        )
    }
}
