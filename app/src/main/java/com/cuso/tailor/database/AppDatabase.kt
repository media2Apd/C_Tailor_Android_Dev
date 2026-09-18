package com.cuso.tailor.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cuso.tailor.database.entities.SelectedGarment
import com.cuso.tailor.database.dao.LeadDao
import com.cuso.tailor.database.dao.OrganizationDao
import com.cuso.tailor.database.dao.SalesStatusDao
import com.cuso.tailor.database.dao.SalesSummaryDao
import com.cuso.tailor.database.dao.SelectedGarmentDao
import com.cuso.tailor.database.dao.SettingsDao
import com.cuso.tailor.database.dao.SubscriptionDao
import com.cuso.tailor.database.dao.TokensDao
import com.cuso.tailor.database.dao.UserDao
import com.cuso.tailor.database.entities.FeatureEnabledEntity
import com.cuso.tailor.database.entities.LeadEntity
import com.cuso.tailor.database.entities.OrgBranchEntity
import com.cuso.tailor.database.entities.OrgDomainEntity
import com.cuso.tailor.database.entities.OrgSegmentEntity
import com.cuso.tailor.database.entities.OrganizationEntity
import com.cuso.tailor.database.entities.SalesStatusEntity
import com.cuso.tailor.database.entities.SalesSummaryEntity
import com.cuso.tailor.database.entities.SettingsEntity
import com.cuso.tailor.database.entities.SubscriptionEntity
import com.cuso.tailor.database.entities.TokensEntity
import com.cuso.tailor.database.entities.UserEntity
import com.cuso.tailor.database.entities.WorkingDayEntity

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
    version = 15,
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