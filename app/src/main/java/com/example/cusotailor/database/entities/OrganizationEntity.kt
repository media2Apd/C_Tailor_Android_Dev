package com.example.cusotailor.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "organization")
data class OrganizationEntity(
    @PrimaryKey
    val orgId: String,        // Organization._id
    val businessId: String,
    val name: String,
    val industry: String,
    val orgType: String,
    val organizationPicture: String?,
    val organizationPictureId: String?,
    val email: String,
    val mobile: String,
    val orgSetupComplete: Boolean,
    val totalMembers: Int,
    val activeMembers: Int,
    val isTaxId: Boolean,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
    val slug: String,
    val v: Int,
    val defaultBranch: String,
    val ownerId: String,
    val ownerMemberId: String,
    val businessType: String,
    val taxId: String,
    val isInternalOrganization: Boolean
    // Domains, segments, branches - separate table
)