package com.cuso.mobile.model

data class organizationSetUpRequest(
    val orgType: String,
    val businessType: String,
    val settings: OrgSettingsRequest,
    val segments: List<String>,
    val isTaxId: Boolean,
    val taxId: String
)

data class OrgSettingsRequest(
    val companySize: String,
    val country: String,
    val state: String,
    val timezone: String,
    val currency: String,
    val language: String,
    val marketingEmails: Boolean,
    val address: String,
    val city: String,
    val pincode: String
)