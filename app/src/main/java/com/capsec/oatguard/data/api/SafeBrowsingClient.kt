package com.capsec.oatguard.data.api

import com.capsec.oatguard.BuildConfig
import com.capsec.oatguard.utils.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Constrói o cliente Retrofit para o Google Safe Browsing API.
 * NetworkSecurityConfig já força HTTPS; aqui só configuramos timeouts + log
 * (log só em build de debug, para nunca vazar URLs/telemetria em release).
 */
object SafeBrowsingClient {

    private const val TIMEOUT_SECONDS = 10L

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                }
            }
            .build()
    }

    val service: SafeBrowsingService by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.SAFE_BROWSING_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SafeBrowsingService::class.java)
    }
}
