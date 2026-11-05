package com.cuso.tailor.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.cuso.tailor.database.entities.PlanModuleEntity
import com.cuso.tailor.database.entities.UserPermissionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PermissionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserPermissions(entity: UserPermissionEntity)

    @Query("SELECT * FROM user_permissions WHERE id = 1 LIMIT 1")
    fun getUserPermissionsFlow(): Flow<UserPermissionEntity?>

    @Query("SELECT * FROM user_permissions WHERE id = 1 LIMIT 1")
    suspend fun getUserPermissions(): UserPermissionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlanModules(entity: PlanModuleEntity)

    @Query("SELECT * FROM plan_modules WHERE id = 1 LIMIT 1")
    fun getPlanModulesFlow(): Flow<PlanModuleEntity?>

    @Query("SELECT * FROM plan_modules WHERE id = 1 LIMIT 1")
    suspend fun getPlanModules(): PlanModuleEntity?

    @Query("DELETE FROM user_permissions")
    suspend fun clearUserPermissions()

    @Query("DELETE FROM plan_modules")
    suspend fun clearPlanModules()

    @Transaction
    suspend fun clearAllPermissions() {
        clearUserPermissions()
        clearPlanModules()
    }
}