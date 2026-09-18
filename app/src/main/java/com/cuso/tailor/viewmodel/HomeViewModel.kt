package com.cuso.tailor.viewmodel


import androidx.lifecycle.ViewModel
import com.cuso.tailor.database.entities.UserEntity
import com.cuso.tailor.database.entities.OrganizationEntity
import com.cuso.tailor.database.entities.TokensEntity
import com.cuso.tailor.repository.LoginRepository
import com.cuso.tailor.utils.launchBusy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
@Suppress("UNUSED_PARAMETER")
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
        launchBusy {
            _user.value = loginRepository.getUser()
            _org.value = loginRepository.getOrganization()
            _tokens.value = loginRepository.getTokens()
        }
    }
    fun logout() {
        launchBusy {
            loginRepository.clearAll()
            _isLoggedOut.value = true
        }
    }
}