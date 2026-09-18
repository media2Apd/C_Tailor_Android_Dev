package com.cuso.tailor.model.sales

import com.cuso.tailor.model.login_forgotPassword_resetPassword.OrganizationDetails

data class myOrganizationResponse(
    val success: Boolean,
    val data: OrganizationDataWrapper
)

data class OrganizationDataWrapper(
    val organization: OrganizationDetails,
    val stats: OrgStats?
)

data class OrgStats(
    val totalBranches: Int,
    val totalDepartments: Int,
    val totalEmployees: Int
)