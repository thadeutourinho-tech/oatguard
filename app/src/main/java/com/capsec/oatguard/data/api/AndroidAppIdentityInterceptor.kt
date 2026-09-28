package com.capsec.oatguard.data.api

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import okhttp3.Interceptor
import okhttp3.Response
import java.security.MessageDigest

/**
 * Adiciona `X-Android-Package` e `X-Android-Cert` em cada request ao Google
 * Safe Browsing API. A API key é restrita no Google Cloud a
 * `com.capsec.oatguard` + SHA-1 do certificado de assinatura; chamadas REST
 * via OkHttp NÃO enviam esses headers automaticamente (só as libs do Play
 * Services fazem isso), então sem este interceptor o Google responde 403.
 *
 * O SHA-1 é calculado em runtime a partir do certificado que assinou o APK
 * instalado — debug.keystore em desenvolvimento, Play App Signing em release.
 */
class AndroidAppIdentityInterceptor(context: Context) : Interceptor {

    private val packageName: String = context.packageName
    private val certSha1: String? = signingCertSha1(context)

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header(HEADER_PACKAGE, packageName)
            .apply { certSha1?.let { header(HEADER_CERT, it) } }
            .build()
        return chain.proceed(request)
    }

    private companion object {
        const val HEADER_PACKAGE = "X-Android-Package"
        const val HEADER_CERT = "X-Android-Cert"

        /** SHA-1 do certificado de assinatura em hex maiúsculo sem ":" (formato aceito pelo Google). */
        fun signingCertSha1(context: Context): String? = try {
            signatures(context).firstOrNull()?.let { signature ->
                MessageDigest.getInstance("SHA-1")
                    .digest(signature.toByteArray())
                    .joinToString("") { "%02X".format(it) }
            }
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }

        @SuppressLint("PackageManagerGetSignatures")
        @Suppress("DEPRECATION")
        fun signatures(context: Context): List<Signature> {
            val pm = context.packageManager
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val signingInfo = pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                    .signingInfo ?: return emptyList()
                if (signingInfo.hasMultipleSigners()) {
                    signingInfo.apkContentsSigners.toList()
                } else {
                    // Com key rotation, o Google Cloud registra o certificado atual (último do histórico).
                    signingInfo.signingCertificateHistory.toList().reversed()
                }
            } else {
                pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES).signatures?.toList().orEmpty()
            }
        }
    }
}
