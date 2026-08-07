package com.cuso.mobile.model


// In your model package - BranchModels.kt

data class CreateBranchRequest(
    val name: String,
    val address: CreateBranchAddress,
    val branchHead: String,
    val contactEmail: String,
    val contactMobile: String,
    val isMainBranch: Boolean = false
)

data class CreateBranchAddress(
    val street: String,
    val city: String,
    val postalCode: String
)

data class CreateBranchResponse(
    val success: Boolean,
    val message: String? = null,
    val data: BranchItem? = null
)