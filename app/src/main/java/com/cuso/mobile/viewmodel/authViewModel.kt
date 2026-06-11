//package com.cuso.mobile.viewmodel
//
//import android.app.Application
//import android.os.Message
//import androidx.lifecycle.AndroidViewModel
//import androidx.lifecycle.viewModelScope
//import com.cuso.mobile.data.User
//import com.cuso.mobile.data.UserDatabase
//import com.cuso.mobile.repository.UserRepository
////import com.google.firebase.firestore.auth.User
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.launch
//
//sealed class AuthState{
//    object idle: AuthState()
//    object loading: AuthState()
//    object RegisterSuccess: AuthState()
//
//    data class LoginSuccess(val username:String): AuthState()
//    data class Error(val message:String): AuthState()
//
//}
//
//class AuthViewModel(application: Application): AndroidViewModel(application){
//    private val repository: UserRepository
//    private val _authstate=MutableStateFlow<AuthState>(AuthState.idle)
//    val authState: StateFlow<AuthState> =_authstate
//
//    init{
//        val dao= UserDatabase.getDatabase(application).userDao()
//        repository=UserRepository(dao)
//    }
//
//
//    fun login(username: String,password: String){
//        if(username.isBlank()||password.isBlank()){
//            _authstate.value= AuthState.Error("Please fill all the fields")
//            return
//        }
//        viewModelScope.launch {
//            _authstate.value=AuthState.loading
//            val user= repository.login(username.trim(),password.trim())
//            if(user!=null){
//                AuthState.LoginSuccess(user.email)
//            }
//            else{
//                _authstate.value= AuthState.Error("Invalid credentials or user doesnt exist")
//            }
//        }
//    }
//
//    fun register(
//        firstName: String,
//        lastName: String,
//        email: String,
//        phone: String,
//        country: String,
//        state: String,
//        organization: String,
//        password: String
//    ) {
//        if (
//            firstName.isBlank() ||
//            lastName.isBlank() ||
//            email.isBlank() ||
//            phone.isBlank() ||
//            country.isBlank() ||
//            state.isBlank() ||
//            organization.isBlank() ||
//            password.isBlank()
//        ) {
//            _authstate.value = AuthState.Error("Please fill all the fields")
//            return
//        }
//
//        if (password.length < 6) {
//            _authstate.value = AuthState.Error("Password should be at least 6 characters")
//            return
//        }
//
//        viewModelScope.launch {
//            _authstate.value = AuthState.loading
//
//            // ✅ CREATE USER OBJECT
//            val user = User(
//                firstName = firstName,
//                lastName = lastName,
//                email = email,
//                phone = phone,
//                country = country,
//                state = state,
//                organization = organization,
//                password = password
//            )
//
//            // ✅ PASS USER TO REPOSITORY
//            val success = repository.registerUser(user)
//
//            // ❌ IMPORTANT FIX: you forgot this in login too (you must assign state)
//            _authstate.value = if (success) {
//                AuthState.RegisterSuccess
//            } else {
//                AuthState.Error("User already exists")
//            }
//        }
//    }
//
//    fun reState(){
//        _authstate.value= AuthState.idle
//    }
//}
//
//
//
//
//
//
////class AuthViewModel(application: Application) : AndroidViewModel(application) {
////
////    private val repository: UserRepository
////
////    private val _authState = MutableStateFlow<com.example.room_db.viewmodel.AuthState>(com.example.room_db.viewmodel.AuthState.Idle)
////    val authState: StateFlow<com.example.room_db.viewmodel.AuthState> = _authState
////
////    init {
////        val dao = UserDatabase.getDatabase(application).userDao()
////        repository = UserRepository(dao)
////    }
////
////    fun login(username: String, password: String) {
////        if (username.isBlank() || password.isBlank()) {
////            _authState.value = com.example.room_db.viewmodel.AuthState.Error("Please fill in all fields")
////            return
////        }
////        viewModelScope.launch {
////            _authState.value = com.example.room_db.viewmodel.AuthState.Loading
////            val user = repository.login(username.trim(), password.trim())
////            _authState.value = if (user != null) {
////                com.example.room_db.viewmodel.AuthState.LoginSuccess(user.username)
////            } else {
////                com.example.room_db.viewmodel.AuthState.Error("Invalid credentials or user does not exist")
////            }
////        }
////    }