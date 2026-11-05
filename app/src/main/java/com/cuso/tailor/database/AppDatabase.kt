package com.cuso.tailor.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cuso.tailor.database.entities.SelectedGarment
import com.cuso.tailor.database.dao.LeadDao
import com.cuso.tailor.database.dao.OrganizationDao
import com.cuso.tailor.database.dao.PermissionDao
import com.cuso.tailor.database.dao.SalesStatusDao
import com.cuso.tailor.database.dao.SalesSummaryDao
import com.cuso.tailor.database.dao.SelectedGarmentDao
import com.cuso.tailor.database.dao.SettingsDao
import com.cuso.tailor.database.dao.SubscriptionDao
import com.cuso.tailor.database.dao.TokensDao
import com.cuso.tailor.database.dao.UserDao
import com.cuso.tailor.database.entities.*

@Database(
    entities = [
        UserEntity::class,
        OrganizationEntity::class,
        SubscriptionEntity::class,
        SettingsEntity::class,
        TokensEntity::class,
        OrgDomainEntity::class,
        OrgSegmentEntity::class,
        OrgBranchEntity::class,
        WorkingDayEntity::class,
        FeatureEnabledEntity::class,
        SalesStatusEntity::class,
        SalesSummaryEntity::class,
        LeadEntity::class,
        SelectedGarment::class,
        UserPermissionEntity::class,
        PlanModuleEntity::class
    ],
    version = 19,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun organizationDao(): OrganizationDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun settingsDao(): SettingsDao
    abstract fun tokensDao(): TokensDao
    abstract fun salesStatusDao(): SalesStatusDao
    abstract fun salesSummaryDao(): SalesSummaryDao
    abstract fun leadDao(): LeadDao
    abstract fun selectedGarmentDao(): SelectedGarmentDao
    abstract fun permissionDao(): PermissionDao

    // Removed the duplicate nested "AppDatabase" class. It was unused and confusing.
}