@file:Suppress(
    "UNUSED_VALUE",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "unused_variable",
    "unused_parameter"
)
package com.cuso.mobile.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.mobile.model.*
import com.cuso.mobile.repository.AuthRepository
import com.cuso.mobile.repository.LoginRepository
import com.cuso.mobile.utils.isValidPhoneNumber
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log
import com.cuso.mobile.database.dao.OrganizationDao
import com.cuso.mobile.database.dao.SettingsDao
import com.cuso.mobile.database.dao.TokensDao
import com.cuso.mobile.database.entities.OrganizationEntity
import com.cuso.mobile.database.entities.SettingsEntity
import com.cuso.mobile.database.entities.TokensEntity
import com.cuso.mobile.database.entities.UserEntity
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.async
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Success(val message: String) : UiState()
    data class Error(val message: String) : UiState()
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
    private val loginRepository: LoginRepository,
    private val organizationDao: OrganizationDao,
    private val settingsDao: SettingsDao,
    private val tokensDao: TokensDao
) : ViewModel() {

    // ── Login Data State ──
    private val _loginData = MutableStateFlow<LoginData?>(null)
//    val loginData: StateFlow<LoginData?> = _loginData.asStateFlow()

    // ── User State ──
//    private val _user = MutableStateFlow<UserEntity?>(null)
//    val user: StateFlow<UserEntity?> = _user.asStateFlow()
    val user: StateFlow<UserEntity?> = loginRepository.getUserFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Keep collecting for 5 seconds after active collectors
            initialValue = null // Initial value before any data is emitted
        )
    // ── Organization State ──
    private val _organization = MutableStateFlow<OrganizationEntity?>(null)
    val organization: StateFlow<OrganizationEntity?> = _organization.asStateFlow()

    // ── Settings State ──
    private val _settings = MutableStateFlow<SettingsEntity?>(null)
    val settings: StateFlow<SettingsEntity?> = _settings.asStateFlow()

    // ── Tokens State ──
    private val _tokens = MutableStateFlow<TokensEntity?>(null)
    val tokens: StateFlow<TokensEntity?> = _tokens.asStateFlow()

    // ── Account State ──
    private val _accountState = MutableStateFlow<UiState>(UiState.Idle)
    val accountState: StateFlow<UiState> = _accountState

    // ── Loading State ──
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // ── OTP States ──
    private val _otpSendResult = MutableLiveData<Result<otpSendResponse>>()
//    val otpSendResult: LiveData<Result<otpSendResponse>> = _otpSendResult

    private val _otpVerifyResult = MutableLiveData<Result<otpVerifyResponse>>()
    val otpVerifyResult: LiveData<Result<otpVerifyResponse>> = _otpVerifyResult

    // ── Register OTP State ──
    private val _registerOtpVerifyResult = MutableLiveData<Result<RegisterVerifyOtpResponse>?>()
    val registerOtpVerifyResult: LiveData<Result<RegisterVerifyOtpResponse>?> = _registerOtpVerifyResult

    // ── Forgot Password State ──
    private val _forgotPasswordState = MutableStateFlow<UiState>(UiState.Idle)
    val forgotPasswordState: StateFlow<UiState> = _forgotPasswordState

    private val _resetPasswordState = MutableStateFlow<UiState>(UiState.Idle)
    val resetPasswordState: StateFlow<UiState> = _resetPasswordState

    // ─────────────────────────────────────────────────────────────
    // INIT - Load all data from Room DB
    // ─────────────────────────────────────────────────────────────

    init {
        viewModelScope.launch {
            // ✅ We don't need loadUser() here anymore because the 'user' Flow
            // will fetch the data automatically as soon as it starts.
            loadOrganization()
            loadSettings()
            loadTokens()

            // Observe the user flow to update Crashlytics when data arrives
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

    // ─────────────────────────────────────────────────────────────
    // Load Data Functions
    // ─────────────────────────────────────────────────────────────

    fun loadAllData() {
        viewModelScope.launch {
//            loadUser()
            loadOrganization()
            loadSettings()
            loadTokens()
        }
    }



//    private suspend fun loadUser() {
//        user.value = loginRepository.getUser()
//    }

    private suspend fun loadOrganization() {
        _organization.value = organizationDao.getOrganization()
    }

    private suspend fun loadSettings() {
        _settings.value = settingsDao.getSettings()
    }

    private suspend fun loadTokens() {
        _tokens.value = loginRepository.getTokens()
    }

//    fun loadTokensFromDb() {
//        viewModelScope.launch {
//            _tokens.value = loginRepository.getTokens()
//        }
//    }

    // ─────────────────────────────────────────────────────────────
    // Login Function
    // ─────────────────────────────────────────────────────────────

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

        viewModelScope.launch {
            val result = repository.login(email, password)
            if (result.isSuccess) {
                val response = result.getOrNull()
                if (response != null) {
                    loginRepository.saveLoginData(response.data)
                    _loginData.value = response.data
//                    loadUser()

                    val token = "Bearer ${response.data.tokens.accessToken}"

                    val meDeferred = async { repository.getMe(token) }
                    val orgDeferred = async { repository.getMyOrganization(token) }
                    val layoutDeferred = async { repository.getMyLayout(token) }

                    val meResult = meDeferred.await()
                    val orgResult = orgDeferred.await()
                    val layoutResult = layoutDeferred.await()

                    meResult.getOrNull()?.let { /* TODO: save or use */ }

                    orgResult.getOrNull()?.let { response ->
                        loginRepository.saveOrganizationData(response.data.organization.toOrganization())
                        loadOrganization()
                        loadSettings()
                    }

                    layoutResult.getOrNull()?.let { /* TODO: save or use */ }

                    loadTokens()

                    _accountState.value = UiState.LoginSuccess(
                        firstName = response.data.user.firstName,
                        lastName = response.data.user.lastName,
                        orgToken = response.data.tokens.orgToken,
                        organization = response.data.user.organizationId
                    )

                    //get user id for crashlytics
                    FirebaseCrashlytics.getInstance().apply {
                        setUserId(response.data.user.userId)
                        setCustomKey("user_email", response.data.user.email)
                        setCustomKey("user_name", "${response.data.user.firstName} ${response.data.user.lastName}")
                    }
                }
            } else {
                _accountState.value = UiState.Error(
                    result.exceptionOrNull()?.message ?: "Invalid password"
                )
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Google Login - FIXED
    // ─────────────────────────────────────────────────────────────

// In the googleLogin function, remove the Branch conversion since Organization expects List<String>

    fun googleLogin(idToken: String) {
        viewModelScope.launch {
            _accountState.value = UiState.Loading
            when (val result = repository.googleLogin(idToken)) {
                is GoogleLoginResult.ExistingUser -> {
                    val googleData = result.response.data

                    // ✅ Google already returns branches as List<String>
                    // No conversion needed - Organization expects List<String>
                    val loginData = LoginData(
                        user = User(
                            id = googleData.user.id,
                            userId = googleData.user.userId,
                            firstName = googleData.user.firstName,
                            lastName = googleData.user.lastName,
                            email = googleData.user.email,
                            profilePicture = googleData.user.profilePicture ?: "",
                            organizationId = Organization(
                                _id = googleData.user.organizationId.id,
                                businessId = googleData.user.organizationId.businessId,
                                name = googleData.user.organizationId.name,
                                industry = googleData.user.organizationId.industry,
                                orgType = googleData.user.organizationId.orgType,
                                organizationPicture = googleData.user.organizationId.organizationPicture,
                                organizationPictureId = googleData.user.organizationId.organizationPictureId,
                                domains = googleData.user.organizationId.domains,
                                email = googleData.user.organizationId.email,
                                mobile = googleData.user.organizationId.mobile,
                                orgSetupComplete = googleData.user.organizationId.orgSetupComplete,
                                totalMembers = googleData.user.organizationId.totalMembers,
                                activeMembers = googleData.user.organizationId.activeMembers,
                                segments = googleData.user.organizationId.segments,
                                branches = googleData.user.organizationId.branches, // ✅ Already List<String>
                                isTaxId = googleData.user.organizationId.isTaxId,
                                status = googleData.user.organizationId.status,
                                isInternalOrganization = googleData.user.organizationId.isInternalOrganization,
                                createdAt = googleData.user.organizationId.createdAt,
                                updatedAt = googleData.user.organizationId.updatedAt,
                                slug = googleData.user.organizationId.slug,
                                __v = googleData.user.organizationId.version,
                                defaultBranch = googleData.user.organizationId.defaultBranch,
                                ownerId = googleData.user.organizationId.ownerId,
                                ownerMemberId = googleData.user.organizationId.ownerMemberId,
                                businessType = googleData.user.organizationId.businessType,
                                taxId = googleData.user.organizationId.taxId,
                                subscription = Subscription(
                                    startDate = googleData.user.organizationId.subscription.startDate,
                                    endDate = googleData.user.organizationId.subscription.endDate,
                                    status = googleData.user.organizationId.subscription.status,
                                    memberLimit = googleData.user.organizationId.subscription.memberLimit,
                                    featuresEnabled = googleData.user.organizationId.subscription.featuresEnabled
                                ),
                                settings = Settings(
                                    country = googleData.user.organizationId.settings.country,
                                    state = googleData.user.organizationId.settings.state,
                                    portalName = googleData.user.organizationId.settings.portalName,
                                    termsAccepted = googleData.user.organizationId.settings.termsAccepted,
                                    marketingEmails = googleData.user.organizationId.settings.marketingEmails,
                                    workingDays = googleData.user.organizationId.settings.workingDays,
                                    timezone = googleData.user.organizationId.settings.timezone,
                                    currency = googleData.user.organizationId.settings.currency,
                                    language = googleData.user.organizationId.settings.language,
                                    address = googleData.user.organizationId.settings.address,
                                    city = googleData.user.organizationId.settings.city,
                                    pincode = googleData.user.organizationId.settings.pincode
                                )
                            ),
                            role = googleData.user.role,
                            memberId = googleData.user.memberId
                        ),
                        tokens = Tokens(
                            accessToken = googleData.tokens.accessToken,
                            refreshToken = googleData.tokens.refreshToken,
                            csrfToken = googleData.tokens.csrfToken,
                            sessionLoginToken = googleData.tokens.sessionLoginToken,
                            orgToken = ""
                        )
                    )

                    loginRepository.saveLoginData(loginData)
                    _loginData.value = loginData
                    loadTokens()

                    _accountState.value = UiState.GoogleLoginExisting(
                        firstName = googleData.user.firstName,
                        lastName = googleData.user.lastName,
                        message = result.response.message
                    )

                    //crashlytics user id get for crash
                    FirebaseCrashlytics.getInstance().apply {
                        setUserId(googleData.user.userId)
                        setCustomKey("user_email", googleData.user.email)
                        setCustomKey("user_name", "${googleData.user.firstName} ${googleData.user.lastName}")
                    }
                }
                is GoogleLoginResult.NewUser -> {
                    val profile = result.response.googleProfile
                    _accountState.value = UiState.GoogleLoginNew(
                        email = profile.email,
                        firstName = profile.firstName,
                        lastName = profile.lastName,
                        googleId = profile.googleId,
                        message = result.response.message
                    )
                }
                is GoogleLoginResult.Failure -> {
                    _accountState.value = UiState.Error(result.message)
                }
            }
        }
    }
    // ─────────────────────────────────────────────────────────────
    // OTP Functions
    // ─────────────────────────────────────────────────────────────

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

            if (result.isSuccess) {
                val loginData = result.getOrNull()?.data
                if (loginData != null) {
                    loginRepository.saveLoginData(loginData)
                    _loginData.value = loginData
//                    loadUser()
                    loadTokens()
                }
            }
        }
    }

    fun registerVerifyOtp(email: String, otp: String) {
        viewModelScope.launch {
            _isLoading.value = true
            Log.d("OTP_API", "Calling API with $email $otp")

            val result = repository.registerVerifyOtp(email, otp)

            _isLoading.value = false
            _registerOtpVerifyResult.value = result

            result.onFailure { error ->
                Log.e("OTP_API", "OTP verification failed", error)
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Forgot Password Functions
    // ─────────────────────────────────────────────────────────────

    fun forgotPasswordOtp(email: String) {
        viewModelScope.launch {
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
        viewModelScope.launch {
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

        viewModelScope.launch {
            _resetPasswordState.value = UiState.Loading
            val result = repository.resetNewPassword(token, newPassword, confirmPassword)
            _resetPasswordState.value = if (result.isSuccess) {
                UiState.Success(result.getOrNull()?.message ?: "Password reset successful")
            } else {
                UiState.Error(result.exceptionOrNull()?.message ?: "Something went wrong")
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Organization Setup
    // ─────────────────────────────────────────────────────────────

    fun organizationSetup(request: organizationSetUpRequest) {
        viewModelScope.launch {
            _accountState.value = UiState.Loading

            val tokens = tokensDao.getTokens()
            val accessToken = tokens?.accessToken
            val csrfToken = tokens?.csrfToken

            if (accessToken.isNullOrBlank() || csrfToken.isNullOrBlank()) {
                _accountState.value = UiState.Error("Session expired. Please log in again.")
                return@launch
            }

            val result = repository.organizationSetup("Bearer $accessToken", csrfToken, request)

            _accountState.value = if (result.isSuccess) {
                UiState.Success(result.getOrNull()?.message ?: "Organization setup completed")
            } else {
                UiState.Error(result.exceptionOrNull()?.message ?: "Something went wrong")
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Sign Up
    // ─────────────────────────────────────────────────────────────

    fun signUp(
        firstName: String,
        lastName: String,
        email: String,
        mobile: String,
        countryIso: String,
        country: String,
        state: String,
        organizationName: String,
        password: String,
        termsAccepted: Boolean
    ) {
        when {
            firstName.isBlank() -> {
                _accountState.value = UiState.Error("First name is required")
                return
            }
            lastName.isBlank() -> {
                _accountState.value = UiState.Error("Last name is required")
                return
            }
            !email.contains("@") -> {
                _accountState.value = UiState.Error("Enter a valid email")
                return
            }
            !isValidPhoneNumber(mobile, countryIso) -> {
                _accountState.value = UiState.Error("Enter a valid phone number for selected country")
                return
            }
            country.isBlank() -> {
                _accountState.value = UiState.Error("Country is required")
                return
            }
            state.isBlank() -> {
                _accountState.value = UiState.Error("State is required")
                return
            }
            organizationName.isBlank() -> {
                _accountState.value = UiState.Error("Organization name is required")
                return
            }
            password.length < 6 -> {
                _accountState.value = UiState.Error("Password must be 6+ characters")
                return
            }
            !termsAccepted -> {
                _accountState.value = UiState.Error("Please accept terms and conditions")
                return
            }
        }

        _accountState.value = UiState.Loading

        viewModelScope.launch {
            val result = repository.createAccount(
                SignupRequest(
                    country = country,
                    email = email,
                    firstName = firstName,
                    lastName = lastName,
                    mobile = mobile,
                    organizationName = organizationName,
                    password = password,
                    state = state,
                    termsAccepted = termsAccepted
                )
            )

            _accountState.value = if (result.isSuccess) {
                UiState.RegisterSuccess
            } else {
                UiState.Error(
                    result.exceptionOrNull()?.message ?: "Something went wrong"
                )
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Email Verification
    // ─────────────────────────────────────────────────────────────

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
                    UiState.EmailNotFound
                } else {
                    UiState.Error(status)
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
// Update Profile Picture (after upload/delete in HR module)
// ─────────────────────────────────────────────────────────────



    fun updateUserProfilePictureIfCurrentUser(targetUserId: String?, newUrl: String?) {
        val currentUser = user.value
        val currentMemberId = currentUser?.id

        // 👈 LOG போடுங்கள் - இரண்டும் ஒன்றாக இருக்கிறதா என்று பாருங்கள்
        Log.d("PROFILE_PIC_DEBUG", "Target: $targetUserId | Current: $currentMemberId")

        // சில சமயம் targetUserId-இல் "_id" இருக்கலாம், அதுவும் memberId-உம் ஒன்றாக இருக்க வேண்டும்
        if (targetUserId.isNullOrBlank() || currentMemberId.isNullOrBlank() || targetUserId != currentMemberId) {
            Log.d("PROFILE_PIC_DEBUG", "Mismatch! Update cancelled.")
            return
        }

        viewModelScope.launch {
            loginRepository.updateProfilePicture(currentUser.id, newUrl)
            // loadUser() தேவையில்லை ஏனேனில் நாம் Flow பயன்படுத்துகிறோம்
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Logout
    // ─────────────────────────────────────────────────────────────

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            // ✅ 1. Clear the Database
            loginRepository.clearAll()

            // ✅ 2. The 'user' Flow will automatically emit 'null' because the DB is now empty.
            // You don't need (and can't do) user.value = null.

            _organization.value = null
            _settings.value = null
            _tokens.value = null
            _loginData.value = null
            _accountState.value = UiState.Idle
            onLoggedOut()
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Reset State
    // ─────────────────────────────────────────────────────────────

    fun resetState() {
        _accountState.value = UiState.Idle
    }

    // ─────────────────────────────────────────────────────────────
    // Set Login Data (for external use)
    // ─────────────────────────────────────────────────────────────

//    fun setLoginData(data: LoginData) {
//        _loginData.value = data
//        viewModelScope.launch {
//            loadTokens()
//        }
//    }

    // Organization.kt — add this extension function at the bottom

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
        branches = this.branches.map { it._id },  // ← List<Branch> → List<String>
        isTaxId = this.isTaxId,
        status = this.status,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        slug = this.slug,
        __v = this.__v,
        defaultBranch = this.defaultBranch?._id?:"",
        ownerId = this.ownerId,
        ownerMemberId = this.ownerMemberId,
        businessType = this.businessType,
        taxId = this.taxId,
        plan = this.plan?._id  // ← Plan object → String ID
    )
}