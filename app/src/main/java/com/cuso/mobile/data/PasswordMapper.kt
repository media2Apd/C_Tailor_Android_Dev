package com.cuso.mobile.data.mapper

import com.cuso.mobile.database.entities.OrganizationEntity
import com.cuso.mobile.database.entities.TokensEntity
import com.cuso.mobile.database.entities.UserEntity
import com.cuso.mobile.model.PasswordResponse

fun PasswordResponse.toEntities(): Triple<UserEntity, OrganizationEntity, TokensEntity> {
    val org = data.user.organizationId

    val userEntity = UserEntity(
        id = data.user.id,
        userId = data.user.userId,
        firstName = data.user.firstName,
        lastName = data.user.lastName,
        email = data.user.email,
        profilePicture = data.user.profilePicture,
        role = data.user.role,
        memberId = data.user.memberId,
        organizationId = org._id,
        companySize = ""
    )

    val orgEntity = OrganizationEntity(
        orgId = org._id,                                          // _id → orgId
        businessId = org.businessId?:"",
        name = org.name?:"",
        industry = org.industry,
        orgType = org.orgType,
        organizationPicture = org.organizationPicture,
        organizationPictureId = org.organizationPictureId,
        email = org.email,
        mobile = org.mobile,
        orgSetupComplete = org.orgSetupComplete,
        totalMembers = org.totalMembers,
        activeMembers = org.activeMembers,
        isTaxId = org.isTaxId,
        status = org.status,
        createdAt = org.createdAt,
        updatedAt = org.updatedAt,
        slug = org.slug,
        v = org.__v?:0,
        defaultBranch = org.defaultBranch,
        ownerId = org.ownerId,
        ownerMemberId = org.ownerMemberId,
        businessType = org.businessType,
        taxId = org.taxId,
        isInternalOrganization = org.isInternalOrganization
    )

    val tokenEntity = TokensEntity(
        userId = data.user.id,                                    // userId add பண்ணுங்க
        accessToken = data.tokens.accessToken,
        refreshToken = data.tokens.refreshToken,
        csrfToken = data.tokens.csrfToken,
        sessionLoginToken = data.tokens.sessionLoginToken,
        orgToken = data.tokens.orgToken
    )

    return Triple(userEntity, orgEntity, tokenEntity)
}