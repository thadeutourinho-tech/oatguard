package com.capsec.oatguard.data.repository

import android.content.Context
import com.capsec.oatguard.BuildConfig
import com.capsec.oatguard.data.api.SafeBrowsingClient
import com.capsec.oatguard.data.api.SafeBrowsingService
import com.capsec.oatguard.data.api.models.SafeBrowsingRequest
import com.capsec.oatguard.data.api.models.ThreatTypes
import com.capsec.oatguard.data.network.URLResolver
import com.capsec.oatguard.utils.ThreatScoreMapper
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

/** Resultado final exibido na ResultScreen: URL resolvida + score + textos já mapeados. */
data class ValidationResult(
    val originalUrl: String,
    val resolvedUrl: String,
    val score: Int,
    val titleResId: Int,
    val descriptionResId: Int
)

interface SafeBrowsingRepository {
    /** Resolve redirecionamentos e valida [rawUrl] contra o Safe Browsing API. */
    suspend fun checkUrl(rawUrl: String): ValidationResult
}

/**
 * Implementação real: resolve a URL final ([URLResolver]) e consulta o
 * Google Safe Browsing API v4. Usada quando a `SAFE_BROWSING_API_KEY` (Semana 2+)
 * está configurada em `local.properties`.
 */
class RealSafeBrowsingRepository(
    private val service: SafeBrowsingService,
    private val apiKey: String = BuildConfig.SAFE_BROWSING_API_KEY
) : SafeBrowsingRepository {

    override suspend fun checkUrl(rawUrl: String): ValidationResult {
        val resolvedUrl = URLResolver.resolve(rawUrl)
        val response = service.checkThreat(apiKey, SafeBrowsingRequest.forUrl(resolvedUrl))

        val mapping = response.matches
            ?.map { it.threatType }
            ?.takeIf { it.isNotEmpty() }
            ?.let { ThreatScoreMapper.worstOf(it) }
            ?: ThreatScoreMapper.forThreatType(null)

        return ValidationResult(rawUrl, resolvedUrl, mapping.score, mapping.titleResId, mapping.descriptionResId)
    }
}

/**
 * Implementação mockada (Semana 1 — chave de API vazia): sem chamada de
 * rede real. Usa uma heurística por keyword na URL para permitir testar
 * todos os estados visuais da ResultScreen com QR codes de teste:
 * URLs contendo "malware", "phishing", "unwanted" ou "harmful" retornam o
 * respectivo score; qualquer outra URL real retorna "seguro".
 */
class MockSafeBrowsingRepository : SafeBrowsingRepository {

    override suspend fun checkUrl(rawUrl: String): ValidationResult {
        delay(MOCK_LATENCY) // simula latência de rede para o loading ficar visível na UI

        val lower = rawUrl.lowercase()
        val threatType = when {
            "malware" in lower -> ThreatTypes.MALWARE
            "phishing" in lower -> ThreatTypes.SOCIAL_ENGINEERING
            "unwanted" in lower -> ThreatTypes.UNWANTED_SOFTWARE
            "harmful" in lower -> ThreatTypes.POTENTIALLY_HARMFUL_APPLICATION
            else -> null
        }
        val mapping = ThreatScoreMapper.forThreatType(threatType)

        return ValidationResult(rawUrl, rawUrl, mapping.score, mapping.titleResId, mapping.descriptionResId)
    }

    private companion object {
        val MOCK_LATENCY = 800.milliseconds
    }
}

/** Seleciona a implementação real ou mock com base na presença da API key (vide timeline de 3 semanas). */
object SafeBrowsingRepositoryProvider {
    fun create(context: Context): SafeBrowsingRepository =
        if (BuildConfig.SAFE_BROWSING_API_KEY.isBlank()) {
            MockSafeBrowsingRepository()
        } else {
            RealSafeBrowsingRepository(SafeBrowsingClient.create(context))
        }
}
