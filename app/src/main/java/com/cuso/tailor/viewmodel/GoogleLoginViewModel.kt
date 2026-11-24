@file:Suppress("unused", "AssignedValueIsNeverRead")

package com.cuso.tailor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.tailor.model.login_forgotPassword_resetPassword.GoogleLoginData
import com.cuso.tailor.model.login_forgotPassword_resetPassword.GoogleLoginRequest
import com.cuso.tailor.model.login_forgotPassword_resetPassword.GoogleLoginResponse
import com.cuso.tailor.repository.ApiRepository
import com.cuso.tailor.repository.LoginRepository
import com.cuso.tailor.utils.DynamicIslandManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val GOOGLE_LOGIN_URL = "/api/auth/google/login"

sealed interface GoogleLoginUiState {
    data object Idle : GoogleLoginUiState
    data object Loading : GoogleLoginUiState
    data class ExistingOrg(val data: GoogleLoginData) : GoogleLoginUiState
    data class NewOrg(val data: GoogleLoginData) : GoogleLoginUiState
    data class Error(val message: String) : GoogleLoginUiState
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repo: ApiRepository,
    private val loginRepository: LoginRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _googleLoginState = MutableStateFlow<GoogleLoginUiState>(GoogleLoginUiState.Idle)
    val googleLoginState: StateFlow<GoogleLoginUiState> = _googleLoginState.asStateFlow()

    private val _loginData = MutableStateFlow<GoogleLoginData?>(null)
    val loginData: StateFlow<GoogleLoginData?> = _loginData.asStateFlow()

    fun googleLogin(idToken: String) {
        if (idToken.isBlank()) {
            val msg = "Google idToken is empty"
            _errorMessage.value = msg
            _googleLoginState.value = GoogleLoginUiState.Error(msg)
            DynamicIslandManager.showError(msg)
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _googleLoginState.value = GoogleLoginUiState.Loading

            try {
                val payload = GoogleLoginRequest(idToken = idToken)
                val result = repo.request<GoogleLoginResponse> {
                    post(GOOGLE_LOGIN_URL, payload)
                }

                result.fold(
                    onSuccess = { res ->
                        if (res.success && res.data != null) {
                            loginRepository.saveGoogleLoginData(res.data)
                            _loginData.value = res.data

                            DynamicIslandManager.showSuccess(res.message.ifBlank { "Login successful" })

                            val isOrgComplete = res.data.user.organizationId != null &&
                                    res.data.user.organizationId.orgSetupComplete

                            _googleLoginState.value = if (isOrgComplete) {
                                GoogleLoginUiState.ExistingOrg(res.data)
                            } else {
                                GoogleLoginUiState.NewOrg(res.data)
                            }
                        } else {
                            val msg = res.message.ifBlank { "Google login failed" }
                            _errorMessage.value = msg
                            _googleLoginState.value = GoogleLoginUiState.Error(msg)
                            DynamicIslandManager.showError(msg)
                        }
                    },
                    onFailure = { err ->
                        val msg = err.message ?: "Network error during Google login"
                        _errorMessage.value = msg
                        _googleLoginState.value = GoogleLoginUiState.Error(msg)
                        DynamicIslandManager.showError(msg)
                    }
                )
            } catch (e: Exception) {
                val msg = e.message ?: "Unexpected error during Google login"
                _errorMessage.value = msg
                _googleLoginState.value = GoogleLoginUiState.Error(msg)
                DynamicIslandManager.showError(msg)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetGoogleLoginState() {
        _googleLoginState.value = GoogleLoginUiState.Idle
        _errorMessage.value = null
    }
}