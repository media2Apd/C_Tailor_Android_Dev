// com/cuso/mobile/model/UpdateBranchModels.kt
package com.cuso.tailor.model.settings

data class UpdateBranchAddress(
    val flatNo: String? = null,
    val street: String? = null,
    val areaZone: String? = null,
    val city: String? = null,
    val subdivisionName: String? = null,
    val pincode: String? = null,
    val countryName: String? = null,
    val state: String? = null,
    val postalCode: String? = null,
    val country: String? = null,
)

data class UpdateBranchRequest(
    val name: String,
    val address: UpdateBranchAddress,
    val contactEmail: String,
    val contactMobile: String,
    val status: String,
    val branchHead: String? = null,
)

data class UpdateBranchResponse(
    val success: Boolean,
    val data: BranchItem,
)