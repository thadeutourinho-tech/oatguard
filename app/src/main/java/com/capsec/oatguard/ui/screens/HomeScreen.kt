package com.capsec.oatguard.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.capsec.oatguard.R
import com.capsec.oatguard.ui.components.CapSecBanner
import com.capsec.oatguard.ui.components.OatCallBanner
import com.capsec.oatguard.ui.components.OatHeader

@Composable
fun HomeScreen(onScanClick: () -> Unit) {
    Scaffold(topBar = { OatHeader() }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxSize().weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = onScanClick,
                    modifier = Modifier.fillMaxWidth().height(64.dp)
                ) {
                    Text(
                        text = stringResource(R.string.btn_scan_qr),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CapSecBanner()
                OatCallBanner()
            }
        }
    }
}
