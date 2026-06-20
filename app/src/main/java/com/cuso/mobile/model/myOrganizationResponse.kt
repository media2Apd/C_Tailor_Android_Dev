package com.cuso.mobile.model

data class myOrganizationResponse(
    val success: Boolean,
    val data: OrganizationDataWrapper
)

data class OrganizationDataWrapper(
    val organization: Organization,
    val stats: OrgStats?
)

data class OrgStats(
    val totalBranches: Int,
    val totalDepartments: Int,
    val totalEmployees: Int
)