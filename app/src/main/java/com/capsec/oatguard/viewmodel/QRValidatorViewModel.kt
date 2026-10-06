package com.capsec.oatguard.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.capsec.oatguard.BuildConfig
import com.capsec.oatguard.R
import com.capsec.oatguard.data.repository.SafeBrowsingRepository
import com.capsec.oatguard.data.repository.SafeBrowsingRepositoryProvider
import com.capsec.oatguard.data.repository.ValidationResult
import com.capsec.oatguard.pix.PixDetectionResult
import com.capsec.oatguard.pix.PixKeyDetector
import com.capsec.oatguard.utils.URLValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

/** Estado da tela de resultado, dirigido pela validação de um QR code. */
sealed interface UiState {
    data object Idle : UiState
    data object Loading : UiState
    data class Success(val result: ValidationResult) : UiState
    data class Error(val messageResId: Int) : UiState
}

class QRValidatorViewModel(
    private val repository: SafeBrowsingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState = _uiState.asStateFlow()

    /** Chave PIX do último QR escaneado (null se não era PIX). Nunca logar a chave. */
    private val _pixResult = MutableStateFlow<PixDetectionResult?>(null)
    val pixResult = _pixResult.asStateFlow()

    /**
     * Ponto de entrada do QR escaneado. Chave PIX tem prioridade e não passa
     * pelo Safe Browsing; o resto segue pra validação de URL.
     *
     * @return true se era chave PIX (navegar pra tela PIX), false se seguiu pra validação de URL.
     */
    fun onQrScanned(rawValue: String): Boolean {
        val pix = PixKeyDetector.detect(rawValue)
        _pixResult.value = pix
        if (pix != null) return true
        validateQRCode(rawValue)
        return false
    }

    fun validateQRCode(rawUrl: String) {
        val sanitizedUrl = URLValidator.sanitize(rawUrl)
        if (sanitizedUrl == null) {
            _uiState.value = UiState.Error(R.string.error_no_url)
            return
        }

        _uiState.value = UiState.Loading
        viewModelScope.launch {
            _uiState.value = try {
                UiState.Success(repository.checkUrl(sanitizedUrl))
            } catch (e: IOException) {
                // Sem conexão ou timeout — mesma causa raiz (rede indisponível).
                logDebug(e)
                UiState.Error(R.string.error_no_internet)
            } catch (e: Exception) {
                // Inclui HttpException (ex.: 403 se package/SHA-1 não batem com a restrição da API key).
                logDebug(e)
                UiState.Error(R.string.error_api_unavailable)
            }
        }
    }

    /** Volta pro estado inicial — usado ao navegar de volta pra HomeScreen/Scanner. */
    fun reset() {
        _uiState.value = UiState.Idle
        _pixResult.value = null
    }

    private fun logDebug(e: Exception) {
        if (BuildConfig.DEBUG) Log.w(TAG, "Falha ao validar URL", e)
    }

    companion object {
        private const val TAG = "QRValidatorViewModel"

        val Factory = viewModelFactory {
            initializer {
                QRValidatorViewModel(SafeBrowsingRepositoryProvider.create(this[APPLICATION_KEY]!!))
            }
        }
    }
}
