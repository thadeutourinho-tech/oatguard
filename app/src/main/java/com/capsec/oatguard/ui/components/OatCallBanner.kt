package com.capsec.oatguard.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.capsec.oatguard.R
import com.capsec.oatguard.utils.Constants

@Composable
fun OatCallBanner(modifier: Modifier = Modifier) {
    AdBanner(
        logoResId = R.drawable.oat_icon,
        logoContentDescription = "OatCall",
        title = stringResource(R.string.btn_know_oatcall),
        subtitle = stringResource(R.string.banner_oatcall_subtitle),
        url = Constants.OATCALL_URL,
        modifier = modifier
    )
}
