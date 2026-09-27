package com.capsec.oatguard.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.capsec.oatguard.R
import com.capsec.oatguard.utils.Constants
import com.capsec.oatguard.utils.openUrl

/**
 * Banner clicável genérico usado por CapSecBanner e OatCallBanner: logo +
 * título + subtítulo, abre [url] no navegador ao ser tocado.
 */
@Composable
internal fun AdBanner(
    logoResId: Int,
    logoContentDescription: String,
    title: String,
    subtitle: String,
    url: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { openUrl(context, url) }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = logoResId),
            contentDescription = logoContentDescription,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(40.dp)
        )
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelLarge)
            Text(text = subtitle, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun CapSecBanner(modifier: Modifier = Modifier) {
    AdBanner(
        logoResId = R.drawable.logo_capsec,
        logoContentDescription = "CapSEC",
        title = stringResource(R.string.btn_know_capsec),
        subtitle = stringResource(R.string.banner_capsec_subtitle),
        url = Constants.CAPSEC_URL,
        modifier = modifier
    )
}
