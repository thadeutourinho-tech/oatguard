package com.capsec.oatguard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.capsec.oatguard.R
import com.capsec.oatguard.utils.ColorMapper

/**
 * Círculo grande com o score (0-100) + título curto + descrição em linguagem
 * qualificada. Cor calculada via [ColorMapper].
 */
@Composable
fun SafetyScoreCard(
    score: Int,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    val color = ColorMapper.colorForScore(score)
    val scoreDescription = stringResource(R.string.content_description_score)

    Column(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .background(color = color, shape = CircleShape)
                .semantics { contentDescription = "$scoreDescription: $score" },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = score.toString(),
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp)
        )

        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
