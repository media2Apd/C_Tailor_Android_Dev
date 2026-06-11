//package com.cuso.mobile.viewmodel
//
//import android.app.Application
//import androidx.lifecycle.AndroidViewModel
//import androidx.lifecycle.viewModelScope
//import com.cuso.mobile.data.User
//import com.cuso.mobile.data.UserDatabase
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.launch
//
//sealed class SignUpState {
//    object Idle : SignUpState()
//    object Loading : SignUpState()
//    object Success : SignUpState()
//    data class Error(val message: String) : SignUpState()
//}
//
//class SignUpViewModel(application: Application) : AndroidViewModel(application) {
//    private val dao = UserDatabase.getDatabase(application).userDao()
//
//    private val _state = MutableStateFlow<SignUpState>(SignUpState.Idle)
//    val state: StateFlow<SignUpState> = _state
//
//    fun signUp(
//        firstName: String,
//        lastName: String,
//        email: String,
//        phone: String,
//        country: String,
//        state: String,
//        organization: String,
//        password: String
//    ) {
//        viewModelScope.launch {
//            if (firstName.isBlank() || lastName.isBlank() || email.isBlank() ||
//                phone.isBlank() || country.isBlank() || state.isBlank() ||
//                organization.isBlank() || password.isBlank()
//            ) {
//                _state.value = SignUpState.Error("All fields are required")
//                return@launch
//            }
//
//            _state.value = SignUpState.Loading
//
//            try {
//                val existing = dao.getUserByEmail(email)
//                if (existing != null) {
//                    _state.value = SignUpState.Error("Email already registered")
//                    return@launch
//                }
//                dao.insertUser(
//                    User(
//                        firstName = firstName,
//                        lastName = lastName,
//                        email = email,
//                        phone = phone,
//                        country = country,
//                        state = state,
//                        organization = organization,
//                        password = password
//                    )
//                )
//                _state.value = SignUpState.Success
//            } catch (e: Exception) {
//                _state.value = SignUpState.Error("Error: ${e.message}")
//            }
//        }
//    }
//
//    fun resetState() {
//        _state.value = SignUpState.Idle
//    }
//}