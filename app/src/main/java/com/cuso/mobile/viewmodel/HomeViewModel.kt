package com.cuso.mobile.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.mobile.database.entities.UserEntity
import com.cuso.mobile.database.entities.OrganizationEntity
import com.cuso.mobile.database.entities.TokensEntity
import com.cuso.mobile.repository.LoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val loginRepository: LoginRepository
) : ViewModel() {

    private val _user = MutableStateFlow<UserEntity?>(null)
    val user: StateFlow<UserEntity?> = _user

    private val _org = MutableStateFlow<OrganizationEntity?>(null)
    val org: StateFlow<OrganizationEntity?> = _org

    private val _tokens = MutableStateFlow<TokensEntity?>(null)
    val tokens: StateFlow<TokensEntity?> = _tokens

    private val _isLoggedOut = MutableStateFlow(false)
    val isLoggedOut: StateFlow<Boolean> = _isLoggedOut

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _user.value = loginRepository.getUser()
            _org.value = loginRepository.getOrganization()
            _tokens.value = loginRepository.getTokens()
        }
    }
    fun logout() {
        viewModelScope.launch {
            loginRepository.clearAll()
            _isLoggedOut.value = true
        }
    }
}