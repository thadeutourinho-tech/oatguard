package com.capsec.oatguard.data.network

import com.capsec.oatguard.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Resolve redirecionamentos (encurtadores como bit.ly, tinyurl) ANTES de
 * validar contra o Safe Browsing API — phishing usa encurtadores para
 * esconder o destino real.
 *
 * Segue o Location header via HEAD request, no máximo [Constants.MAX_REDIRECT_HOPS]
 * hops, com timeout de [Constants.REDIRECT_TIMEOUT_SECONDS]s por request. Em caso de
 * erro, loop detectado, ou esquema não http(s), retorna a última URL válida conhecida
 * (nunca lança exceção — resolução best-effort).
 */
object URLResolver {

    private val ALLOWED_SCHEMES = setOf("http", "https")

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .followRedirects(false)
            .followSslRedirects(false)
            .connectTimeout(Constants.REDIRECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(Constants.REDIRECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    suspend fun resolve(originalUrl: String): String = withContext(Dispatchers.IO) {
        if (!isHttpOrHttps(originalUrl)) return@withContext originalUrl

        var current = originalUrl
        val visited = mutableSetOf(current)

        repeat(Constants.MAX_REDIRECT_HOPS) {
            val next = followOneHop(current) ?: return@withContext current

            if (!isHttpOrHttps(next) || next in visited) {
                // Loop detectado ou esquema inválido no destino: fica na última URL válida.
                return@withContext current
            }
            visited += next
            current = next
        }

        current
    }

    /** Retorna a próxima URL do Location header, ou null se não houver redirecionamento. */
    private fun followOneHop(url: String): String? {
        return try {
            val request = Request.Builder().url(url).head().build()
            client.newCall(request).execute().use { response ->
                if (response.code in 300..399) {
                    response.header("Location")?.let { location ->
                        response.request.url.resolve(location)?.toString()
                    }
                } else {
                    null
                }
            }
        } catch (_: IOException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    private fun isHttpOrHttps(url: String): Boolean {
        val scheme = url.substringBefore("://", missingDelimiterValue = "").lowercase()
        return scheme in ALLOWED_SCHEMES
    }
}
