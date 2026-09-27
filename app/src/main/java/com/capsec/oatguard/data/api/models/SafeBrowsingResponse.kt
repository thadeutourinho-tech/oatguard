package com.capsec.oatguard.data.api.models

import com.google.gson.annotations.SerializedName

/**
 * Resposta de `POST v4/threatMatches:find`. `matches` nulo/vazio significa
 * "nenhuma ameaça conhecida" — NÃO significa "site 100% seguro" (linguagem
 * qualificada, conformidade Google — vide especificação seção 6.0.7).
 */
data class SafeBrowsingResponse(
    @SerializedName("matches") val matches: List<ThreatMatch>? = null
) {
    data class ThreatMatch(
        @SerializedName("threatType") val threatType: String,
        @SerializedName("platformType") val platformType: String? = null,
        @SerializedName("threat") val threat: Threat? = null,
        @SerializedName("cacheDuration") val cacheDuration: String? = null
    )

    data class Threat(
        @SerializedName("url") val url: String? = null
    )

    val isSafe: Boolean get() = matches.isNullOrEmpty()
}
