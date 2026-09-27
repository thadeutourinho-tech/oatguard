package com.capsec.oatguard.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capsec.oatguard.R
import com.capsec.oatguard.ui.components.CapSecBanner
import com.capsec.oatguard.ui.components.OatCallBanner
import com.capsec.oatguard.ui.components.OatHeader
import com.capsec.oatguard.ui.components.SafetyScoreCard
import com.capsec.oatguard.utils.Constants
import com.capsec.oatguard.utils.openUrl
import com.capsec.oatguard.viewmodel.QRValidatorViewModel
import com.capsec.oatguard.viewmodel.UiState

@Composable
fun ResultScreen(viewModel: QRValidatorViewModel, onScanAnother: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(topBar = { OatHeader() }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (val state = uiState) {
                is UiState.Idle, is UiState.Loading -> LoadingContent()
                is UiState.Error -> ErrorContent(
                    messageResId = state.messageResId,
                    onScanAnother = onScanAnother
                )
                is UiState.Success -> SuccessContent(
                    result = state.result,
                    onOpenLink = { openUrl(context, state.result.resolvedUrl) },
                    onOpenLearnMore = { openUrl(context, Constants.SAFE_BROWSING_LEARN_MORE_URL) },
                    onScanAnother = onScanAnother
                )
            }

            CapSecBanner()
            OatCallBanner()
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Text(
                text = stringResource(R.string.result_validating),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Composable
private fun ErrorContent(messageResId: Int, onScanAnother: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(messageResId),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Button(onClick = onScanAnother, modifier = Modifier.padding(top = 24.dp)) {
            Text(stringResource(R.string.btn_scan_another))
        }
    }
}

@Composable
private fun SuccessContent(
    result: com.capsec.oatguard.data.repository.ValidationResult,
    onOpenLink: () -> Unit,
    onOpenLearnMore: () -> Unit,
    onScanAnother: () -> Unit
) {
    SafetyScoreCard(
        score = result.score,
        title = stringResource(result.titleResId),
        description = stringResource(result.descriptionResId)
    )

    Text(
        text = "${stringResource(R.string.result_destination_label)}: ${result.resolvedUrl}",
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )

    Button(onClick = onOpenLink, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.btn_open_link))
    }

    OutlinedButton(onClick = onScanAnother, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.btn_scan_another))
    }

    Text(
        text = stringResource(R.string.disclaimer),
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )

    Text(
        text = stringResource(R.string.learn_more),
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.primary,
        textDecoration = TextDecoration.Underline,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clickable(onClick = onOpenLearnMore)
    )
}
