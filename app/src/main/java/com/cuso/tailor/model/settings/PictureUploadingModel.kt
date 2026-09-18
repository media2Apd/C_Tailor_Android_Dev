package com.cuso.tailor.model.settings

import com.cuso.tailor.model.login_forgotPassword_resetPassword.Organization

// Response for organization picture upload
data class UploadOrganizationPictureResponse(
    val success: Boolean,
    val message: String?,
    val data: Organization?   // same wrapper used in getMyOrganization
)
//
//data class OrganizationPictureData(
//    val _id: String,
//    val organizationPicture: String?,
//    val organizationPictureId: String?
//)
