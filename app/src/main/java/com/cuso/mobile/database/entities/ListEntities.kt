package com.cuso.mobile.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "org_domains")
data class OrgDomainEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val orgId: String,
    val domain: String
)

@Entity(tableName = "org_segments")
data class OrgSegmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val orgId: String,
    val segment: String
)

@Entity(tableName = "org_branches")
data class OrgBranchEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val orgId: String,
    val branch: String
)

@Entity(tableName = "working_days")
data class WorkingDayEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val orgId: String,
    val day: String
)

@Entity(tableName = "features_enabled")
data class FeatureEnabledEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val orgId: String,
    val feature: String
)