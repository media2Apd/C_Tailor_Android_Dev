package com.cuso.mobile.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.mobile.model.LoginData
import com.cuso.mobile.model.Organization
import com.cuso.mobile.model.Settings
import com.cuso.mobile.model.SignupRequest
import com.cuso.mobile.model.Subscription
import com.cuso.mobile.model.Tokens
import com.cuso.mobile.model.User
import com.cuso.mobile.model.otpSendResponse
import com.cuso.mobile.model.otpVerifyResponse
import com.cuso.mobile.repository.AuthRepository
import com.cuso.mobile.repository.LoginRepository
import com.cuso.mobile.utils.isValidPhoneNumber
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log
import com.cuso.mobile.model.GoogleLoginResult

sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Success(val message: String) : UiState()
    data class Error(val message: String) : UiState()
    data class LoginSuccess(val firstName: String, val lastName: String) : UiState()
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
    private val loginRepository: LoginRepository
) : ViewModel() {

    private val _accountState = MutableStateFlow<UiState>(UiState.Idle)
    val accountState: StateFlow<UiState> = _accountState

    private val _otpSendResult = MutableLiveData<Result<otpSendResponse>>()
    private val _otpVerifyResult = MutableLiveData<Result<otpVerifyResponse>>()
    val otpSendResult: LiveData<Result<otpSendResponse>> = _otpSendResult
    val otpVerifyResult: LiveData<Result<otpVerifyResponse>> = _otpVerifyResult

    private val _forgotPasswordState = MutableStateFlow<UiState>(UiState.Idle)
    val forgotPasswordState: StateFlow<UiState> = _forgotPasswordState
    private val _resetPasswordState = MutableStateFlow<UiState>(UiState.Idle)
    val resetPasswordState: StateFlow<UiState> = _resetPasswordState
    fun login(email: String, password: String) {
        when {
            !email.contains("@") -> { _accountState.value = UiState.Error("") }
            password.isBlank()   -> { _accountState.value = UiState.Error("") }
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
    fun googleLogin(idToken: String) {
        viewModelScope.launch {
            _accountState.value = UiState.Loading
            when (val result = repository.googleLogin(idToken)) {
                is GoogleLoginResult.ExistingUser -> {
                    val googleData = result.response.data

                    val loginData = LoginData(
                        user = User(
                            id = googleData.user.id,
                            userId = googleData.user.userId,
                            firstName = googleData.user.firstName,
                            lastName = googleData.user.lastName,
                            email = googleData.user.email,
                            profilePicture = googleData.user.profilePicture,
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
                                branches = googleData.user.organizationId.branches,
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
                                    companySize = googleData.user.organizationId.settings.companySize,
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
                            orgToken =""
                        )
                    )

                    loginRepository.saveLoginData(loginData)
                    _accountState.value = UiState.GoogleLoginExisting(
                        firstName = googleData.user.firstName,
                        lastName = googleData.user.lastName,
                        message = result.response.message
                    )
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

            // 👇 Save to Room DB if success
            if (result.isSuccess) {
                val loginData = result.getOrNull()?.data
                if (loginData != null) {
                    loginRepository.saveLoginData(loginData)
                }
            }
        }
    }

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
            firstName.isBlank() ->
            { _accountState.value = UiState.Error("First name is required"); return }

            lastName.isBlank() ->
            { _accountState.value = UiState.Error("Last name is required"); return }

            !email.contains("@") ->
            { _accountState.value = UiState.Error("Enter a valid email"); return }

            !isValidPhoneNumber(mobile, countryIso) ->
            { _accountState.value = UiState.Error("Enter a valid phone number for selected country"); return }

            country.isBlank() ->
            { _accountState.value = UiState.Error("Country is required"); return }

            state.isBlank() ->
            { _accountState.value = UiState.Error("State is required"); return }

            organizationName.isBlank() ->
            { _accountState.value = UiState.Error("Organization name is required"); return }

            password.length < 6 ->
            { _accountState.value = UiState.Error("Password must be 6+ characters"); return }

            !termsAccepted ->
            { _accountState.value = UiState.Error("Please accept terms and conditions"); return }
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

    fun resetState() {
        _accountState.value = UiState.Idle
    }
}