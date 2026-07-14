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
    version = 8,
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

    // ⚠️ NOTE: This companion getDatabase() creates a database instance
    // OUTSIDE of Hilt's singleton graph. If any code calls
    // AppDatabase.getDatabase(context) directly while Hilt ALSO provides
    // an AppDatabase via DatabaseModule, you get two separate Room
    // instances on the same underlying file "cusotailor_db" — a classic
    // source of "table already exists" / migration errors.
    //
    // Search your codebase for `AppDatabase.getDatabase(` — if anything
    // outside of this file calls it, replace that usage with @Inject
    // of AppDatabase instead, then delete this companion object.
//    companion object {
//        @Volatile
//        private var INSTANCE: AppDatabase? = null
//
////        fun getDatabase(context: Context): AppDatabase {
////            return INSTANCE ?: synchronized(this) {
////                val instance = Room.databaseBuilder(
////                    context.applicationContext,
////                    AppDatabase::class.java,
////                    "cusotailor_db"
////                )
////                    .fallbackToDestructiveMigration()
////                    .build()
////                INSTANCE = instance
////                instance
////            }
////        }
//    }
}