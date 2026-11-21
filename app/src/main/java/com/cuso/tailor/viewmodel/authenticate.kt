@file:Suppress(
    "UNUSED_VALUE",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "unused_variable",
    "unused_parameter"
)
package com.cuso.tailor.viewmodel


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.tailor.database.dao.OrganizationDao
import com.cuso.tailor.database.dao.PermissionDao
import com.cuso.tailor.database.dao.SettingsDao
import com.cuso.tailor.database.dao.TokensDao
import com.cuso.tailor.database.entities.OrganizationEntity
import com.cuso.tailor.database.entities.PlanModuleEntity
import com.cuso.tailor.database.entities.SettingsEntity
import com.cuso.tailor.database.entities.TokensEntity
import com.cuso.tailor.database.entities.UserEntity
import com.cuso.tailor.database.entities.UserPermissionEntity
import com.cuso.tailor.model.login_forgotPassword_resetPassword.GoogleLoginResult
import com.cuso.tailor.model.login_forgotPassword_resetPassword.LoginData
import com.cuso.tailor.model.login_forgotPassword_resetPassword.Organization
import com.cuso.tailor.model.login_forgotPassword_resetPassword.OrganizationDetails
import com.cuso.tailor.model.login_forgotPassword_resetPassword.RegisterVerifyOtpResponse
import com.cuso.tailor.model.login_forgotPassword_resetPassword.ChangeTemporaryPasswordRequest
import com.cuso.tailor.model.login_forgotPassword_resetPassword.ChangeTemporaryPasswordResponse
import com.cuso.tailor.model.login_forgotPassword_resetPassword.VerifyTwoFARequest
import com.cuso.tailor.model.login_forgotPassword_resetPassword.VerifyTwoFAResponse
import com.cuso.tailor.model.login_forgotPassword_resetPassword.organizationSetUpRequest
import com.cuso.tailor.model.login_forgotPassword_resetPassword.otpSendResponse
import com.cuso.tailor.model.login_forgotPassword_resetPassword.otpVerifyResponse
import com.cuso.tailor.model.permission.ModulePermissionsData
import com.cuso.tailor.model.permission.ModulePermissionsResponse
import com.cuso.tailor.model.permission.MyPermissionsData
import com.cuso.tailor.model.permission.MyPermissionsResponse
import com.cuso.tailor.model.sales.toOrganization
import com.cuso.tailor.repository.ApiRepository
import com.cuso.tailor.repository.AuthRepository
import com.cuso.tailor.repository.LoginRepository
import com.cuso.tailor.utils.launchBusy
import com.cuso.tailor.utils.toformatCurrency
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// Authentication Endpoints
private const val VERIFY_2FA_URL = "/api/auth/login/verify-2fa"
private const val CHANGE_TEMP_PASSWORD_URL = "/api/auth/change-password"
private const val PERMISSIONS_ME_URL = "/api/permissions/me"
private const val PLAN_MODULES_URL = "/api/common/plan-setups/customer/my-plan-modules"

sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Success(val message: String) : UiState()
    data class Error(val message: String) : UiState()
    data class TwoFactorRequired(
        val challengeToken: String,
        val method: String,
        val message: String
    ) : UiState()
    data class MustChangePassword(
        val challengeToken: String?,
        val message: String
    ) : UiState()
    data class WorkspaceLocked(val message: String) : UiState()
    data class LoginSuccess(
        val firstName: String,
        val lastName: String,
        val orgToken: String?,
        val organization: Organization?
    ) : UiState()
    object RegisterSuccess : UiState()
    data class EmailVerified(val message: String) : UiState()
    object EmailNotFound : UiState()
    data class GoogleLoginExisting(
        val firstName: String,
        val lastName: String,
        val message: String
    ) : UiState()
    data class GoogleLoginNew(
        val email: String,
        val firstName: String,
        val lastName: String,
        val googleId: String,
        val message: String
    ) : UiState()

    data class ForgotPasswordVerified(
        val resetToken: String,
        val message: String
    ) : UiState()
}

@HiltViewModel
class Authenticate @Inject constructor(
    private val repository: AuthRepository,
    private val apiRepo: ApiRepository,
    private val loginRepository: LoginRepository,
    private val organizationDao: OrganizationDao,
    private val settingsDao: SettingsDao,
    private val tokensDao: TokensDao,
    private val permissionDao: PermissionDao,
    private val gson: com.google.gson.Gson
) : ViewModel() {

    // Login Data State
    private val _loginData = MutableStateFlow<LoginData?>(null)

    // User State
    val user: StateFlow<UserEntity?> = loginRepository.getUserFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Organization State
    private val _organization = MutableStateFlow<OrganizationEntity?>(null)
    val organization: StateFlow<OrganizationEntity?> = _organization.asStateFlow()

    // Settings State
    private val _settings = MutableStateFlow<SettingsEntity?>(null)
    val settings: StateFlow<SettingsEntity?> = _settings.asStateFlow()

    // Currency Symbol State (Observed directly from Room DB)
    val currencySymbol: StateFlow<String> = settingsDao.getSettingsFlow()
        .map { entity ->
            entity?.currency.toformatCurrency()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    // Tokens State
    private val _tokens = MutableStateFlow<TokensEntity?>(null)
    val tokens: StateFlow<TokensEntity?> = _tokens.asStateFlow()

    // Account State
    private val _accountState = MutableStateFlow<UiState>(UiState.Idle)
    val accountState: StateFlow<UiState> = _accountState

    // Loading State
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // OTP States
    private val _otpSendResult = MutableLiveData<Result<otpSendResponse>>()

    private val _otpVerifyResult = MutableLiveData<Result<otpVerifyResponse>>()
    val otpVerifyResult: LiveData<Result<otpVerifyResponse>> = _otpVerifyResult

    // Register OTP State
    private val _registerOtpVerifyResult = MutableLiveData<Result<RegisterVerifyOtpResponse>?>()
    val registerOtpVerifyResult: LiveData<Result<RegisterVerifyOtpResponse>?> = _registerOtpVerifyResult

    // Forgot Password State
    private val _forgotPasswordState = MutableStateFlow<UiState>(UiState.Idle)
    val forgotPasswordState: StateFlow<UiState> = _forgotPasswordState

    private val _resetPasswordState = MutableStateFlow<UiState>(UiState.Idle)
    val resetPasswordState: StateFlow<UiState> = _resetPasswordState

    // Permissions State
    private val _myPermissions = MutableStateFlow<MyPermissionsData?>(null)
    val myPermissions: StateFlow<MyPermissionsData?> = _myPermissions.asStateFlow()

    private val _modulePermissions = MutableStateFlow<ModulePermissionsData?>(null)
    val modulePermissions: StateFlow<ModulePermissionsData?> = _modulePermissions.asStateFlow()

    init {
        viewModelScope.launch {
            loadOrganization()
            loadSettings()
            loadTokens()

            // Observe user flow to update Crashlytics metadata when available
            user.collect { currentUser ->
                currentUser?.let {
                    FirebaseCrashlytics.getInstance().apply {
                        setUserId(it.userId)
                        setCustomKey("user_email", it.email)
                    }
                }
            }
        }
    }

    fun loadAllData() {
        viewModelScope.launch {
            loadOrganization()
            loadSettings()
            loadTokens()
        }
    }

    private suspend fun loadOrganization() {
        _organization.value = organizationDao.getOrganization()
    }

    private suspend fun loadSettings() {
        _settings.value = settingsDao.getSettings()
    }

    private suspend fun loadTokens() {
        _tokens.value = loginRepository.getTokens()
    }

    // -------------------------------------------------------------------------
    // Login & Two-Factor Authentication Functions
    // -------------------------------------------------------------------------

    fun login(email: String, password: String) {
        when {
            !email.contains("@") -> {
                _accountState.value = UiState.Error("")
                return
            }
            password.isBlank() -> {
                _accountState.value = UiState.Error("")
                return
            }
        }

        _accountState.value = UiState.Loading

        launchBusy {
            val result = repository.login(email, password)
            if (result.isSuccess) {
                val response = result.getOrNull()
                if (response != null) {
                    when {
                        response.workspaceLocked -> {
                            _accountState.value = UiState.WorkspaceLocked(
                                response.message.ifBlank {
                                    "Your workspace is currently locked. Please contact your administrator."
                                }
                            )
                        }
                        response.requiresTwoFA && !response.challengeToken.isNullOrBlank() -> {
                            // Always complete 2FA verification before any password-change or home navigation
                            _accountState.value = UiState.TwoFactorRequired(
                                challengeToken = response.challengeToken,
                                method = response.method ?: "authenticator",
                                message = response.message.ifBlank {
                                    "Please enter the code from your authenticator app to complete login."
                                }
                            )
                        }
                        response.mustChangePassword -> {
                            // Persist temporary session tokens only if needed for the change-password API call
                            response.data?.let { loginData ->
                                loginRepository.saveLoginData(loginData)
                                loadTokens()
                            }
                            _accountState.value = UiState.MustChangePassword(
                                challengeToken = response.challengeToken ?: response.data?.tokens?.sessionLoginToken,
                                message = response.message.ifBlank {
                                    "Your administrator gave you a temporary password. Choose a new one to continue."
                                }
                            )
                        }
                        response.data != null -> {
                            completeLoginSession(response.data)
                        }
                        else -> {
                            _accountState.value = UiState.Error(
                                response.message.ifBlank { "Invalid email or password" }
                            )
                        }
                    }
                }
            } else {
                _accountState.value = UiState.Error(
                    result.exceptionOrNull()?.message ?: "Invalid password"
                )
            }
        }
    }

    /**
     * Updates the temporary password and completes the login session when login data is returned.
     */
    fun changeTemporaryPassword(
        temporaryPassword: String,
        newPassword: String,
        confirmPassword: String,
        challengeToken: String? = null
    ) {
        _accountState.value = UiState.Loading

        launchBusy {
            val request = ChangeTemporaryPasswordRequest(
                temporaryPassword = temporaryPassword,
                newPassword = newPassword,
                confirmPassword = confirmPassword,
                challengeToken = challengeToken
            )

            val result = apiRepo.request<ChangeTemporaryPasswordResponse> {
                post(CHANGE_TEMP_PASSWORD_URL, request)
            }

            result.fold(
                onSuccess = { res ->
                    val loginData = res.data
                    when {
                        res.workspaceLocked -> {
                            _accountState.value = UiState.WorkspaceLocked(
                                res.message.ifBlank {
                                    "Your workspace is currently locked. Please contact your administrator."
                                }
                            )
                        }
                        res.requiresTwoFA && !res.challengeToken.isNullOrBlank() -> {
                            _accountState.value = UiState.TwoFactorRequired(
                                challengeToken = res.challengeToken,
                                method = res.method ?: "authenticator",
                                message = res.message.ifBlank {
                                    "Please enter the code from your authenticator app to complete login."
                                }
                            )
                        }
                        res.success && loginData != null -> {
                            completeLoginSession(loginData)
                        }
                        res.success -> {
                            _accountState.value = UiState.Success(
                                res.message.ifBlank { "Password updated successfully. Please sign in." }
                            )
                        }
                        else -> {
                            _accountState.value = UiState.Error(
                                res.message.ifBlank { "Failed to update password" }
                            )
                        }
                    }
                },
                onFailure = { error ->
                    _accountState.value = UiState.Error(
                        error.message ?: "Failed to update password"
                    )
                }
            )
        }
    }

    /**
     * Verifies the 6-digit authenticator or backup code via ApiRepository.
     */
    fun verifyTwoFa(challengeToken: String, code: String) {
        val trimmedCode = code.trim()
        if (challengeToken.isBlank()) {
            _accountState.value = UiState.Error("Two-factor session expired. Please sign in again.")
            return
        }
        if (trimmedCode.isBlank()) {
            _accountState.value = UiState.Error("Please enter the verification code")
            return
        }

        _accountState.value = UiState.Loading

        launchBusy {
            val request = VerifyTwoFARequest(
                challengeToken = challengeToken,
                code = trimmedCode
            )

            val result = apiRepo.request<VerifyTwoFAResponse> {
                post(VERIFY_2FA_URL, request)
            }

            result.fold(
                onSuccess = { res ->
                    val loginData = res.data
                    when {
                        res.workspaceLocked -> {
                            _accountState.value = UiState.WorkspaceLocked(
                                res.message.ifBlank {
                                    "Your workspace is currently locked. Please contact your administrator."
                                }
                            )
                        }
                        res.mustChangePassword -> {
                            // Transition to Set New Password screen right after 2FA verification
                            res.data?.let { data ->
                                loginRepository.saveLoginData(data)
                                loadTokens()
                            }
                            _accountState.value = UiState.MustChangePassword(
                                challengeToken = res.challengeToken ?: res.data?.tokens?.sessionLoginToken,
                                message = res.message.ifBlank {
                                    "Your administrator gave you a temporary password. Choose a new one to continue."
                                }
                            )
                        }
                        res.success && loginData != null -> {
                            completeLoginSession(loginData)
                        }
                        else -> {
                            _accountState.value = UiState.Error(
                                res.message.ifBlank { "Invalid verification code" }
                            )
                        }
                    }
                },
                onFailure = { error ->
                    _accountState.value = UiState.Error(
                        error.message ?: "Invalid verification code"
                    )
                }
            )
        }
    }

    /**
     * Persists session tokens, organization details, and user permissions after authentication.
     */
    private suspend fun completeLoginSession(loginData: LoginData) {
        loginRepository.saveLoginData(loginData)
        _loginData.value = loginData

        val token = "Bearer ${loginData.tokens.accessToken}"

        coroutineScope {
            val meDeferred = async { repository.getMe(token) }
            val orgDeferred = async { repository.getMyOrganization(token) }

            val mePermissionDeferred = async {
                apiRepo.request<MyPermissionsResponse> {
                    get(PERMISSIONS_ME_URL)
                }
            }

            val modulePermissionDeferred = async {
                apiRepo.request<ModulePermissionsResponse> {
                    get(PLAN_MODULES_URL)
                }
            }

            val meResult = meDeferred.await()
            val orgResult = orgDeferred.await()
            val mePermissionResult = mePermissionDeferred.await()
            val modulePermissionResult = modulePermissionDeferred.await()

            meResult.getOrNull()?.let { }

            orgResult.getOrNull()?.let { orgResponse ->
                try {
                    val org = orgResponse.data?.organization?.toOrganization()
                    if (org != null) {
                        loginRepository.saveOrganizationData(org)
                        loadOrganization()
                        loadSettings()
                    }
                } catch (e: Exception) {
                }
            }

            mePermissionResult.fold(
                onSuccess = { res ->
                    _myPermissions.value = res.data
                    res.data?.let { data ->
                        permissionDao.insertUserPermissions(
                            UserPermissionEntity(
                                id = 1,
                                isOwner = data.isOwner,
                                visiblePermissionsJson = gson.toJson(data.visible),
                                rawPermissionsJson = gson.toJson(data.permissions)
                            )
                        )
                    }
                },
                onFailure = { error ->
                }
            )

            modulePermissionResult.fold(
                onSuccess = { res ->
                    _modulePermissions.value = res.data
                    res.data?.let { data ->
                        permissionDao.insertPlanModules(
                            PlanModuleEntity(
                                id = 1,
                                modulesJson = gson.toJson(data)
                            )
                        )
                    }
                },
                onFailure = { error ->
                }
            )
        }

        loadTokens()

        _accountState.value = UiState.LoginSuccess(
            firstName = loginData.user.firstName,
            lastName = loginData.user.lastName,
            orgToken = loginData.tokens.orgToken,
            organization = loginData.user.organizationId
        )

        FirebaseCrashlytics.getInstance().apply {
            setUserId(loginData.user.userId)
            setCustomKey("user_email", loginData.user.email)
            setCustomKey("user_name", "${loginData.user.firstName} ${loginData.user.lastName}")
        }
    }

    // -------------------------------------------------------------------------
    // Google Login
    // -------------------------------------------------------------------------

    fun googleLogin(idToken: String) {
        launchBusy {
            _accountState.value = UiState.Loading
            when (val result = repository.googleLogin(idToken)) {
                is GoogleLoginResult.ExistingUser -> {
                    val googleData = result.response.data
                    if (googleData == null) {
                        _accountState.value = UiState.Error(
                            result.response.message.ifBlank { "Google login data is missing" }
                        )
                        return@launchBusy
                    }

                    loginRepository.saveGoogleLoginData(googleData)
                    loadOrganization()
                    loadSettings()
                    loadTokens()

                    _accountState.value = UiState.GoogleLoginExisting(
                        firstName = googleData.user.firstName,
                        lastName = googleData.user.lastName,
                        message = result.response.message
                    )

                    FirebaseCrashlytics.getInstance().apply {
                        setUserId(googleData.user.userId)
                        setCustomKey("user_email", googleData.user.email)
                        setCustomKey("user_name", "${googleData.user.firstName} ${googleData.user.lastName}")
                    }
                }
                is GoogleLoginResult.NewUser -> {
                    val profile = result.response.googleProfile
                    _accountState.value = UiState.GoogleLoginNew(
                        email = profile?.email.orEmpty(),
                        firstName = profile?.firstName.orEmpty(),
                        lastName = profile?.lastName.orEmpty(),
                        googleId = profile?.googleId.orEmpty(),
                        message = result.response.message
                    )
                }
                is GoogleLoginResult.Failure -> {
                    _accountState.value = UiState.Error(result.message)
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // OTP Functions
    // -------------------------------------------------------------------------

    fun sendOtp(
        email: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = repository.sendOtp(email.trim())
            result.fold(
                onSuccess = { onSuccess() },
                onFailure = { error -> onError(error.message ?: "Failed to send OTP") }
            )
        }
    }

    fun verifyOtp(email: String, otp: String) {
        launchBusy {
            val result = repository.verifyOtp(email, otp)
            _otpVerifyResult.value = result

            if (result.isSuccess) {
                val loginData = result.getOrNull()?.data
                if (loginData != null) {
                    loginRepository.saveLoginData(loginData)
                    _loginData.value = loginData
                    loadTokens()
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Forgot Password Functions
    // -------------------------------------------------------------------------

    fun forgotPasswordOtp(email: String) {
        launchBusy {
            _forgotPasswordState.value = UiState.Loading
            val result = repository.forgotPassword(email)
            _forgotPasswordState.value = if (result.isSuccess) {
                UiState.Success("Password reset mail sent")
            } else {
                UiState.Error(result.exceptionOrNull()?.message ?: "Something went wrong")
            }
        }
    }

    fun verifyForgotPasswordOtp(email: String, otp: String) {
        launchBusy {
            _accountState.value = UiState.Loading
            val result = repository.verifyForgotPasswordOtp(email, otp)
            _accountState.value = if (result.isSuccess) {
                val response = result.getOrNull()!!
                UiState.ForgotPasswordVerified(
                    resetToken = response.resetToken,
                    message = response.message
                )
            } else {
                UiState.Error(result.exceptionOrNull()?.message ?: "Something went wrong")
            }
        }
    }

    fun resetNewPassword(token: String, newPassword: String, confirmPassword: String) {
        if (newPassword.isBlank()) {
            _resetPasswordState.value = UiState.Error("Password cannot be empty")
            return
        }
        if (newPassword != confirmPassword) {
            _resetPasswordState.value = UiState.Error("Passwords do not match")
            return
        }

        launchBusy {
            _resetPasswordState.value = UiState.Loading
            val result = repository.resetNewPassword(token, newPassword, confirmPassword)
            _resetPasswordState.value = if (result.isSuccess) {
                UiState.Success(result.getOrNull()?.message ?: "Password reset successful")
            } else {
                UiState.Error(result.exceptionOrNull()?.message ?: "Something went wrong")
            }
        }
    }

    // -------------------------------------------------------------------------
    // Organization Setup
    // -------------------------------------------------------------------------

    fun organizationSetup(request: organizationSetUpRequest) {
        launchBusy {
            _accountState.value = UiState.Loading

            val tokens = tokensDao.getTokens()
            val accessToken = tokens?.accessToken
            val csrfToken = tokens?.csrfToken

            if (accessToken.isNullOrBlank() || csrfToken.isNullOrBlank()) {
                _accountState.value = UiState.Error("Session expired. Please log in again.")
                return@launchBusy
            }

            val result = repository.organizationSetup("Bearer $accessToken", csrfToken, request)

            _accountState.value = if (result.isSuccess) {
                UiState.Success(result.getOrNull()?.message ?: "Organization setup completed")
            } else {
                UiState.Error(result.exceptionOrNull()?.message ?: "Something went wrong")
            }
        }
    }

    // -------------------------------------------------------------------------
    // Email Verification
    // -------------------------------------------------------------------------

    fun verifyEmail(email: String) {
        if (!email.contains("@")) {
            _accountState.value = UiState.Error("Enter a valid email")
            return
        }

        _accountState.value = UiState.Loading

        launchBusy {
            val result = repository.verifyEmail(email)
            _accountState.value = if (result.isSuccess) {
                UiState.EmailVerified(result.getOrNull()?.message ?: "Email verified")
            } else {
                val status = result.exceptionOrNull()?.message ?: ""
                if (status == "not_found") {
                    UiState.EmailNotFound
                } else {
                    UiState.Error(status)
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Update Profile Picture
    // -------------------------------------------------------------------------

    fun updateUserProfilePictureIfCurrentUser(targetUserId: String?, newUrl: String?) {
        val currentUser = user.value
        val currentMemberId = currentUser?.id


        if (targetUserId.isNullOrBlank() || currentMemberId.isNullOrBlank() || targetUserId != currentMemberId) {
            return
        }

        launchBusy {
            loginRepository.updateProfilePicture(currentUser.id, newUrl)
        }
    }

    // -------------------------------------------------------------------------
    // Logout & Reset State
    // -------------------------------------------------------------------------

    fun logout(onLoggedOut: () -> Unit) {
        launchBusy {
            loginRepository.clearAll()
            permissionDao.clearAllPermissions()
            _organization.value = null
            _settings.value = null
            _tokens.value = null
            _loginData.value = null
            _myPermissions.value = null
            _modulePermissions.value = null
            _accountState.value = UiState.Idle
            onLoggedOut()
        }
    }

    fun resetState() {
        _accountState.value = UiState.Idle
    }

    fun OrganizationDetails.toOrganization(): Organization = Organization(
        subscription = this.subscription,
        settings = this.settings,
        isInternalOrganization = this.isInternalOrganization,
        _id = this._id,
        businessId = this.businessId,
        name = this.name,
        industry = this.industry,
        orgType = this.orgType,
        organizationPicture = this.organizationPicture,
        organizationPictureId = this.organizationPictureId,
        domains = this.domains,
        email = this.email,
        mobile = this.mobile,
        orgSetupComplete = this.orgSetupComplete,
        totalMembers = this.totalMembers,
        activeMembers = this.activeMembers,
        segments = this.segments,
        branches = this.branches.map { it._id },
        isTaxId = this.isTaxId,
        status = this.status,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        slug = this.slug,
        __v = this.__v,
        defaultBranch = this.defaultBranch?._id ?: "",
        ownerId = this.ownerId,
        ownerMemberId = this.ownerMemberId,
        businessType = this.businessType,
        taxId = this.taxId,
        plan = this.plan?._id
    )
}