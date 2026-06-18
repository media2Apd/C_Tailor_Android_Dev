package com.cuso.mobile.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.cuso.mobile.database.dao.OrganizationDao
import com.cuso.mobile.database.dao.SettingsDao
import com.cuso.mobile.database.dao.SubscriptionDao
import com.cuso.mobile.database.dao.TokensDao
import com.cuso.mobile.database.dao.UserDao
import com.cuso.mobile.database.entities.FeatureEnabledEntity
import com.cuso.mobile.database.entities.OrgBranchEntity
import com.cuso.mobile.database.entities.OrgDomainEntity
import com.cuso.mobile.database.entities.OrgSegmentEntity
import com.cuso.mobile.database.entities.OrganizationEntity
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
        FeatureEnabledEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun organizationDao(): OrganizationDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun settingsDao(): SettingsDao
    abstract fun tokensDao(): TokensDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cusotailor_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}