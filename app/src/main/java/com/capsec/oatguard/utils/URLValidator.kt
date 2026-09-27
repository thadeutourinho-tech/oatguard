package com.capsec.oatguard.utils

import android.util.Patterns

/**
 * Valida se o conteúdo lido de um QR code é uma URL http(s) utilizável.
 * QR codes podem conter texto livre, vCard, Wi-Fi config, etc — só nos
 * interessa o caso de URL, e apenas HTTP/HTTPS (nunca outros esquemas
 * como file://, intent://, javascript:).
 */
object URLValidator {

    private val ALLOWED_SCHEMES = setOf("http", "https")

    fun isValidUrl(raw: String?): Boolean {
        val text = raw?.trim().orEmpty()
        if (text.isEmpty()) return false

        val scheme = text.substringBefore("://", missingDelimiterValue = "").lowercase()
        if (scheme !in ALLOWED_SCHEMES) return false

        return Patterns.WEB_URL.matcher(text).matches()
    }

    /** Retorna a URL normalizada (trim) se válida, ou null caso contrário. */
    fun sanitize(raw: String?): String? {
        val text = raw?.trim().orEmpty()
        return if (isValidUrl(text)) text else null
    }
}
