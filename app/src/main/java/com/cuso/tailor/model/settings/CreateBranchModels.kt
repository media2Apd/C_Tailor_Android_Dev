package com.cuso.tailor.model.settings


// In your model package - BranchModels.kt

data class CreateBranchRequest(
    val name: String,
    val address: CreateBranchAddress,
    val branchHead: String? = null,
    val contactEmail: String,
    val contactMobile: String,
    val isMainBranch: Boolean = false,
)

data class CreateBranchAddress(
    val flatNo: String? = null,
    val street: String? = null,
    val areaZone: String? = null,
    val city: String? = null,
    val subdivisionName: String? = null,
    val pincode: String? = null,
    val countryName: String? = null,
    val state: String? = null,
    val postalCode: String? = null,
//    val country: String? = null,
)

data class CreateBranchResponse(
    val success: Boolean,
    val message: String? = null,
    val data: BranchItem? = null,
)