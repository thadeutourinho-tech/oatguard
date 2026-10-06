package com.capsec.oatguard.data.api.models

import com.capsec.oatguard.BuildConfig
import com.google.gson.annotations.SerializedName

/** Corpo do request para `POST v4/threatMatches:find` do Google Safe Browsing API. */
data class SafeBrowsingRequest(
    @SerializedName("client") val client: Client,
    @SerializedName("threatInfo") val threatInfo: ThreatInfo
) {
    data class Client(
        @SerializedName("clientId") val clientId: String = "com.capsec.oatguard",
        @SerializedName("clientVersion") val clientVersion: String = BuildConfig.VERSION_NAME
    )

    data class ThreatInfo(
        @SerializedName("threatTypes") val threatTypes: List<String> = ThreatTypes.ALL,
        @SerializedName("platformTypes") val platformTypes: List<String> = listOf("ANY_PLATFORM"),
        @SerializedName("threatEntryTypes") val threatEntryTypes: List<String> = listOf("URL"),
        @SerializedName("threatEntries") val threatEntries: List<ThreatEntry>
    )

    data class ThreatEntry(
        @SerializedName("url") val url: String
    )

    companion object {
        /** Constrói o request padrão do OatGuard para validar uma única URL final. */
        fun forUrl(url: String): SafeBrowsingRequest = SafeBrowsingRequest(
            client = Client(),
            threatInfo = ThreatInfo(threatEntries = listOf(ThreatEntry(url)))
        )
    }
}

/** Threat types verificados pelo OatGuard (vide seção "Score Mapping" da especificação). */
object ThreatTypes {
    const val MALWARE = "MALWARE"
    const val SOCIAL_ENGINEERING = "SOCIAL_ENGINEERING"
    const val UNWANTED_SOFTWARE = "UNWANTED_SOFTWARE"
    const val POTENTIALLY_HARMFUL_APPLICATION = "POTENTIALLY_HARMFUL_APPLICATION"

    val ALL = listOf(MALWARE, SOCIAL_ENGINEERING, UNWANTED_SOFTWARE, POTENTIALLY_HARMFUL_APPLICATION)
}
