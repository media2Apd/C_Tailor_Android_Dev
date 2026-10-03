package com.cuso.tailor.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

// One state type for every screen / API
sealed interface UiState<out T> {
    object Idle : UiState<Nothing>
    object Loading : UiState<Nothing>
    data class Success<out T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

// Sets Loading -> runs the API -> sets Success or Error
// onSuccess / onError are optional callbacks (close screen, refresh list, show toast)
fun <T> ViewModel.launchState(
    state: MutableStateFlow<UiState<T>>,
    onSuccess: (T) -> Unit = {},
    onError: (String) -> Unit = {},
    block: suspend () -> Result<T>
) {
    viewModelScope.launch {
        state.value = UiState.Loading
        block().fold(
            onSuccess = {
                state.value = UiState.Success(it)
                onSuccess(it)
            },
            onFailure = {
                val message = it.message ?: "Something went wrong"
                state.value = UiState.Error(message)
                onError(message)
            }
        )
    }
}