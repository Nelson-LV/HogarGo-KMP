package com.hogargo.kmp.advice.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hogargo.kmp.advice.data.DailyAdvice
import com.hogargo.kmp.advice.data.DailyAdviceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AdviceUiState {
    data object Loading : AdviceUiState
    data class Success(val advice: DailyAdvice) : AdviceUiState
    data class Error(val message: String, val fallbackAdvice: DailyAdvice) : AdviceUiState
}

class DailyAdviceViewModel(
    private val repository: DailyAdviceRepository = DailyAdviceRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow<AdviceUiState>(AdviceUiState.Loading)
    val uiState: StateFlow<AdviceUiState> = _uiState.asStateFlow()

    init {
        loadDailyAdvice()
    }

    fun loadDailyAdvice() {
        viewModelScope.launch {
            _uiState.value = AdviceUiState.Loading
            repository.fetchRandomAdvice()
                .onSuccess { advice ->
                    _uiState.value = AdviceUiState.Success(advice)
                }
                .onFailure { error ->
                    val fallback = repository.getRandomFallbackAdvice()
                    _uiState.value = AdviceUiState.Error(
                        message = error.message ?: "Sin conexión",
                        fallbackAdvice = fallback,
                    )
                }
        }
    }
}
