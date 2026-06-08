package com.example.cusotailor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cusotailor.model.SignupRequest
import com.example.cusotailor.repository.AuthRepository
import com.example.cusotailor.utils.isValidPhoneNumber
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Success(val message: String) : UiState()
    data class Error(val message: String) : UiState()
    data class LoginSuccess(val username: String) : UiState()
    object RegisterSuccess : UiState()
}

class Authenticate : ViewModel() {

    private val repository = AuthRepository()

    private val _accountState = MutableStateFlow<UiState>(UiState.Idle)
    val accountState: StateFlow<UiState> = _accountState

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

    fun login(email: String, password: String) {
        when {
            !email.contains("@") -> { _accountState.value = UiState.Error("Enter a valid email"); return }
            password.isBlank()   -> { _accountState.value = UiState.Error("Password is required"); return }
        }

        _accountState.value = UiState.Loading

        viewModelScope.launch {
            val result = repository.login(email, password)
            _accountState.value = if (result.isSuccess) {
                val response = result.getOrNull()
                val username = "${response?.firstName ?: ""} ${response?.lastName ?: ""}".trim()
                UiState.LoginSuccess(username.ifBlank { email })
            } else {
                UiState.Error(result.exceptionOrNull()?.message ?: "Invalid email or password")
            }
        }
    }

    fun resetState() {
        _accountState.value = UiState.Idle
    }
}