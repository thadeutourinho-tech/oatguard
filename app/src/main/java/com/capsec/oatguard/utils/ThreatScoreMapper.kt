package com.capsec.oatguard.utils

import com.capsec.oatguard.R
import com.capsec.oatguard.data.api.models.ThreatTypes

/** Score (0-100) + textos (título curto + descrição qualificada) para um resultado de validação. */
data class ScoreMapping(
    val score: Int,
    val titleResId: Int,
    val descriptionResId: Int
)

/**
 * Traduz um `threatType` do Google Safe Browsing (ou `null` = nenhuma ameaça
 * conhecida) para o score/textos exibidos na ResultScreen. Vide seção
 * "Score Mapping" da especificação técnica.
 */
object ThreatScoreMapper {

    fun forThreatType(threatType: String?): ScoreMapping = when (threatType) {
        ThreatTypes.MALWARE -> ScoreMapping(20, R.string.result_title_malware, R.string.score_malware)
        ThreatTypes.SOCIAL_ENGINEERING -> ScoreMapping(30, R.string.result_title_phishing, R.string.score_phishing)
        ThreatTypes.UNWANTED_SOFTWARE -> ScoreMapping(45, R.string.result_title_unwanted, R.string.score_suspicious)
        ThreatTypes.POTENTIALLY_HARMFUL_APPLICATION -> ScoreMapping(50, R.string.result_title_harmful_app, R.string.score_harmful_app)
        else -> ScoreMapping(100, R.string.result_title_safe, R.string.score_safe)
    }

    /** Dado uma lista de threatTypes retornados pela API, escolhe o mais severo (menor score). */
    fun worstOf(threatTypes: List<String>): ScoreMapping =
        threatTypes.map { forThreatType(it) }.minByOrNull { it.score } ?: forThreatType(null)
}
