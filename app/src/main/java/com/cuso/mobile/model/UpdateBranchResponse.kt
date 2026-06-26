// com/cuso/mobile/model/UpdateBranchModels.kt
package com.cuso.mobile.model

data class UpdateBranchAddress(
    val street: String,
    val city: String,
    val postalCode: String
)

data class UpdateBranchRequest(
    val name: String,
    val address: UpdateBranchAddress,
    val contactEmail: String,
    val contactMobile: String,
    val status: String,
    val branchHead: String?=null
)

data class UpdateBranchResponse(
    val success: Boolean,
    val data: BranchItem
)