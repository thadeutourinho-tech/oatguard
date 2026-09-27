package com.capsec.oatguard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capsec.oatguard.R
import com.capsec.oatguard.data.repository.SafeBrowsingRepository
import com.capsec.oatguard.data.repository.SafeBrowsingRepositoryProvider
import com.capsec.oatguard.data.repository.ValidationResult
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
    private val repository: SafeBrowsingRepository = SafeBrowsingRepositoryProvider.create()
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState = _uiState.asStateFlow()

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
            } catch (_: IOException) {
                // Sem conexão ou timeout — mesma causa raiz (rede indisponível) na Semana 1.
                UiState.Error(R.string.error_no_internet)
            } catch (_: Exception) {
                UiState.Error(R.string.error_api_unavailable)
            }
        }
    }

    /** Volta pro estado inicial — usado ao navegar de volta pra HomeScreen/Scanner. */
    fun reset() {
        _uiState.value = UiState.Idle
    }
}
