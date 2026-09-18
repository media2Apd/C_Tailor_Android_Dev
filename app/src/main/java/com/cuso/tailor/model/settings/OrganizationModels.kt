// OrganizationModels.kt - Add these models
package com.cuso.tailor.model.settings

import com.cuso.tailor.model.login_forgotPassword_resetPassword.Organization

data class UpdateOrganizationRequest(
    val name: String? = null,
    val orgType: String? = null,
    val businessType: String? = null,
    val email: String? = null,
    val mobile: String? = null,
    val settings: UpdateOrganizationSettings? = null,

)

data class UpdateOrganizationSettings(
    val country: String? = null,
    val state: String? = null,
    val city: String? = null,
    val pincode: String? = null,
    val address: String? = null,
    val timezone: String? = null,
    val currency: String? = null,
    val language: String? = null,
    val portalName: String? = null,
    val companySize: String? = null
)

data class UpdateOrganizationResponse(
    val success: Boolean,
    val message: String? = null,
    val data: Organization? = null
)