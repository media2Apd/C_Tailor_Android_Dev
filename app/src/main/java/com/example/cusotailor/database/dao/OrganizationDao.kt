package com.example.cusotailor.database.dao

import androidx.room.*
import com.example.cusotailor.database.entities.*
import com.example.cusotailor.database.entities.OrganizationEntity

@Dao
interface OrganizationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrganization(org: OrganizationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDomains(domains: List<OrgDomainEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSegments(segments: List<OrgSegmentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBranches(branches: List<OrgBranchEntity>)

    @Query("SELECT * FROM organization LIMIT 1")
    suspend fun getOrganization(): OrganizationEntity?

    @Query("SELECT domain FROM org_domains WHERE orgId = :orgId")
    suspend fun getDomains(orgId: String): List<String>

    @Query("DELETE FROM organization")
    suspend fun clearOrganization()

    @Query("DELETE FROM org_domains")
    suspend fun clearDomains()

    @Query("DELETE FROM org_segments")
    suspend fun clearSegments()

    @Query("DELETE FROM org_branches")
    suspend fun clearBranches()
}