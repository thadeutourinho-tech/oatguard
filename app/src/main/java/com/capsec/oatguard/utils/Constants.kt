package com.capsec.oatguard.utils

/**
 * Constantes globais do OatGuard: URLs de terceiros, limites de rede e
 * thresholds de score. Nenhum dado sensível aqui — a API key vive em
 * BuildConfig (lida de local.properties), nunca hardcoded.
 */
object Constants {

    // Google Safe Browsing API v4
    const val SAFE_BROWSING_BASE_URL = "https://safebrowsing.googleapis.com/"

    // Banners clicáveis (HomeScreen + ResultScreen)
    const val CAPSEC_URL = "https://www.capsec.com.br"
    const val OATCALL_URL = "https://play.google.com/store/apps/details?id=com.capsec.oatcall"

    // Atribuição legal obrigatória (Google Safe Browsing)
    const val SAFE_BROWSING_LEARN_MORE_URL = "https://www.google.com/safebrowsing/"

    // Splash
    const val SPLASH_DELAY_MS = 2000L

    // URLResolver (redirecionamentos de encurtadores)
    const val MAX_REDIRECT_HOPS = 5
    const val REDIRECT_TIMEOUT_SECONDS = 5L

    // Score thresholds
    const val SCORE_SAFE_MIN = 75
    const val SCORE_SUSPICIOUS_MIN = 40

    // Idiomas suportados para a documentação/privacidade
    private val SUPPORTED_DOC_LANGUAGES = setOf("pt", "en", "es", "fr", "ru", "hi", "ar")
    private const val DEFAULT_DOC_LANGUAGE = "pt"

    /**
     * Monta a URL de documentação/privacidade com fallback para PT quando o
     * idioma do device não é suportado.
     */
    fun getDocsUrl(language: String): String {
        val lang = language.lowercase().takeIf { it in SUPPORTED_DOC_LANGUAGES } ?: DEFAULT_DOC_LANGUAGE
        return "https://www.capsec.com.br/oatguard/$lang/docs.html"
    }
}
