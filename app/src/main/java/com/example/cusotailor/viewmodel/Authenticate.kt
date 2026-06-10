package com.example.cusotailor.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cusotailor.model.SignupRequest
import com.example.cusotailor.model.otpSendResponse
import com.example.cusotailor.model.otpVerifyResponse
import com.example.cusotailor.repository.AuthRepository
import com.example.cusotailor.repository.LoginRepository
import com.example.cusotailor.utils.isValidPhoneNumber
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log

sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Success(val message: String) : UiState()
    data class Error(val message: String) : UiState()
    data class LoginSuccess(val firstName: String, val lastName: String) : UiState()
    object RegisterSuccess : UiState()
    data class EmailVerified(val message: String) : UiState()
    object EmailNotFound: UiState()
}

@HiltViewModel
class Authenticate @Inject constructor(
    private val repository: AuthRepository,
    private val loginRepository: LoginRepository
) : ViewModel() {

    // private val repository = AuthRepository() ← இது delete பண்ணுங்க

    private val _accountState = MutableStateFlow<UiState>(UiState.Idle)
    val accountState: StateFlow<UiState> = _accountState

    private val _otpSendResult = MutableLiveData<Result<otpSendResponse>>()
    private val _otpVerifyResult= MutableLiveData<Result<otpVerifyResponse>>()
    val otpSendResult: LiveData<Result<otpSendResponse>> = _otpSendResult
    val otpVerifyResult: LiveData<Result<otpVerifyResponse>> = _otpVerifyResult


    fun login(email: String, password: String) {
        when {
            !email.contains("@") -> { _accountState.value = UiState.Error("");  }
            password.isBlank()   -> { _accountState.value = UiState.Error("");  }
        }

        _accountState.value = UiState.Loading

        viewModelScope.launch {
            val result = repository.login(email, password)
            if (result.isSuccess) {
                val response = result.getOrNull()
                if (response != null) {
                    loginRepository.saveLoginData(response.data)
                    _accountState.value = UiState.LoginSuccess(
                        firstName = response.data.user.firstName,
                        lastName  = response.data.user.lastName
                    )
                }
            } else {
                _accountState.value = UiState.Error(
                    result.exceptionOrNull()?.message ?: "Invalid password"
                )
            }
        }
    }

    fun sendOtp(email: String) {
        viewModelScope.launch {
            _otpSendResult.value = repository.sendOtp(email)
        }
    }

    fun verifyOtp(email: String, otp: String) {
        viewModelScope.launch {
            Log.d("OTP_API", "Calling API with $email $otp")

            val result = repository.verifyOtp(email, otp)

            Log.d("OTP_API", "Response = $result")

            _otpVerifyResult.value = result
        }
    }
    fun signUp(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        countryIso: String,
        country: String,
        state: String,
        organization: String,
        password: String
    ) {
        when {
            firstName.isBlank()                    -> { _accountState.value = UiState.Error("First name is required");         return }
            lastName.isBlank()                     -> { _accountState.value = UiState.Error("Last name is required");          return }
            !email.contains("@")                   -> { _accountState.value = UiState.Error("Enter a valid email");            return }
            !isValidPhoneNumber(phone, countryIso) -> { _accountState.value = UiState.Error("Enter a valid phone number for selected country"); return }
            country.isBlank()                      -> { _accountState.value = UiState.Error("Country is required");            return }
            state.isBlank()                        -> { _accountState.value = UiState.Error("State is required");              return }
            organization.isBlank()                 -> { _accountState.value = UiState.Error("Organization name is required");  return }
            password.length < 6                    -> { _accountState.value = UiState.Error("Password must be 6+ characters"); return }
        }

        _accountState.value = UiState.Loading

        viewModelScope.launch {
            val result = repository.createAccount(
                SignupRequest(
                    firstName    = firstName,
                    lastName     = lastName,
                    email        = email,
                    phone        = phone,
                    country      = country,
                    state        = state,
                    organization = organization,
                    password     = password
                )
            )
            _accountState.value = if (result.isSuccess) {
                UiState.RegisterSuccess
            } else {
                UiState.Error(result.exceptionOrNull()?.message ?: "Something went wrong")
            }
        }
    }

    fun verifyEmail(email: String) {
        if (!email.contains("@")) {
            _accountState.value = UiState.Error("Enter a valid email")
            return
        }

        _accountState.value = UiState.Loading

        viewModelScope.launch {
            val result = repository.verifyEmail(email)
            _accountState.value = if (result.isSuccess) {
                UiState.EmailVerified(result.getOrNull()?.message ?: "Email verified")
            } else {
                val status = result.exceptionOrNull()?.message ?: ""
                if (status == "not_found") {
                    UiState.EmailNotFound  // ← your custom state
                } else {
                    UiState.Error(status)
                }
            }
        }
    }

    fun resetState() {
        _accountState.value = UiState.Idle
    }
}