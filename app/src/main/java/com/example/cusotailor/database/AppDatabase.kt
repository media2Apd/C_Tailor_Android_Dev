package com.example.cusotailor.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.cusotailor.database.dao.OrganizationDao
import com.example.cusotailor.database.dao.SettingsDao
import com.example.cusotailor.database.dao.SubscriptionDao
import com.example.cusotailor.database.dao.TokensDao
import com.example.cusotailor.database.dao.UserDao
import com.example.cusotailor.database.entities.FeatureEnabledEntity
import com.example.cusotailor.database.entities.OrgBranchEntity
import com.example.cusotailor.database.entities.OrgDomainEntity
import com.example.cusotailor.database.entities.OrgSegmentEntity
import com.example.cusotailor.database.entities.OrganizationEntity
import com.example.cusotailor.database.entities.SettingsEntity
import com.example.cusotailor.database.entities.SubscriptionEntity
import com.example.cusotailor.database.entities.TokensEntity
import com.example.cusotailor.database.entities.UserEntity
import com.example.cusotailor.database.entities.WorkingDayEntity

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
    version = 1,
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
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}