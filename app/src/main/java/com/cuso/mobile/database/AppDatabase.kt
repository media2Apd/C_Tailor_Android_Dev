package com.cuso.mobile.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cuso.mobile.database.entities.SelectedGarment
import com.cuso.mobile.database.dao.LeadDao
import com.cuso.mobile.database.dao.OrganizationDao
import com.cuso.mobile.database.dao.SalesStatusDao
import com.cuso.mobile.database.dao.SalesSummaryDao
import com.cuso.mobile.database.dao.SelectedGarmentDao
import com.cuso.mobile.database.dao.SettingsDao
import com.cuso.mobile.database.dao.SubscriptionDao
import com.cuso.mobile.database.dao.TokensDao
import com.cuso.mobile.database.dao.UserDao
import com.cuso.mobile.database.entities.FeatureEnabledEntity
import com.cuso.mobile.database.entities.LeadEntity
import com.cuso.mobile.database.entities.OrgBranchEntity
import com.cuso.mobile.database.entities.OrgDomainEntity
import com.cuso.mobile.database.entities.OrgSegmentEntity
import com.cuso.mobile.database.entities.OrganizationEntity
import com.cuso.mobile.database.entities.SalesStatusEntity
import com.cuso.mobile.database.entities.SalesSummaryEntity
import com.cuso.mobile.database.entities.SettingsEntity
import com.cuso.mobile.database.entities.SubscriptionEntity
import com.cuso.mobile.database.entities.TokensEntity
import com.cuso.mobile.database.entities.UserEntity
import com.cuso.mobile.database.entities.WorkingDayEntity

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
        SelectedGarment::class
    ],
    version = 14,
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
    abstract fun selectedGarmentDao():  SelectedGarmentDao

    abstract class AppDatabase : RoomDatabase() {
//        abstract fun profileDao(): ProfileDao
    }
}