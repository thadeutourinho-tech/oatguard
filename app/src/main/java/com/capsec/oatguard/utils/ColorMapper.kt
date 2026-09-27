package com.capsec.oatguard.utils

import androidx.compose.ui.graphics.Color
import com.capsec.oatguard.ui.theme.ScoreDangerRed
import com.capsec.oatguard.ui.theme.ScoreSafeGreen
import com.capsec.oatguard.ui.theme.ScoreSuspiciousYellow

/** Mapeia o score (0-100) retornado pela validação para a cor exibida no círculo de resultado. */
object ColorMapper {

    fun colorForScore(score: Int): Color = when {
        score >= Constants.SCORE_SAFE_MIN -> ScoreSafeGreen
        score >= Constants.SCORE_SUSPICIOUS_MIN -> ScoreSuspiciousYellow
        else -> ScoreDangerRed
    }
}
