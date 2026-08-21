// com/cuso/mobile/viewmodel/SettingsViewModel.kt

package com.cuso.mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.mobile.model.sales.myOrganizationResponse
import com.cuso.mobile.repository.AuthRepository
import com.cuso.mobile.repository.SessionManager
import com.cuso.mobile.utils.launchBusy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
@Suppress("UNUSED_PARAMETER")
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    // ── Organization State ──
    private val _organization = MutableStateFlow<myOrganizationResponse?>(null)
    val organization: StateFlow<myOrganizationResponse?> = _organization.asStateFlow()

    private val _isLoadingOrganization = MutableStateFlow(false)
    val isLoadingOrganization: StateFlow<Boolean> = _isLoadingOrganization.asStateFlow()

    private val _organizationError = MutableStateFlow<String?>(null)
    val organizationError: StateFlow<String?> = _organizationError.asStateFlow()

    fun fetchMyOrganization(token: String) {
        launchBusy {
            _isLoadingOrganization.value = true
            _organizationError.value = null
            try {
                val result = authRepository.getMyOrganization(token)
                if (result.isSuccess) {
                    _organization.value = result.getOrNull()
                } else {
                    _organizationError.value = result.exceptionOrNull()?.message ?: "Failed to fetch organization"
                }
            } catch (e: Exception) {
                _organizationError.value = e.message ?: "An error occurred"
            } finally {
                _isLoadingOrganization.value = false
            }
        }
    }

    fun clearOrganization() {
        _organization.value = null
        _organizationError.value = null
    }

    // ── Logout ──
    fun logout(onComplete: () -> Unit) {
        launchBusy {
            sessionManager.logout()
            clearOrganization()   // clear in-memory state too, avoids stale data flash on next login
            onComplete()
        }
    }
}