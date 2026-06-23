package com.cuso.mobile.di

import android.content.Context
import androidx.room.Room
import com.cuso.mobile.database.AppDatabase
import com.cuso.mobile.database.dao.LeadDao
import com.cuso.mobile.database.dao.OrganizationDao
import com.cuso.mobile.database.dao.SalesStatusDao
import com.cuso.mobile.database.dao.SalesSummaryDao
import com.cuso.mobile.database.dao.SettingsDao
import com.cuso.mobile.database.dao.TokensDao
import com.cuso.mobile.repository.LoginRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "cusotailor_db"
        )
            // ⚠️ TEMPORARY for dev. Replace with real Migration(2, 3)
            // before shipping to anyone with existing data.
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideOrganizationDao(db: AppDatabase): OrganizationDao = db.organizationDao()

    @Provides
    @Singleton
    fun provideSettingsDao(database: AppDatabase): SettingsDao {
        return database.settingsDao()
    }

    @Provides
    fun provideLoginRepository(db: AppDatabase): LoginRepository {
        return LoginRepository(db)
    }

    @Provides
    fun provideTokensDao(db: AppDatabase): TokensDao = db.tokensDao()

    @Provides
    fun provideLeadDao(db: AppDatabase): LeadDao = db.leadDao()

    @Provides
    fun provideSalesStatusDao(db: AppDatabase): SalesStatusDao = db.salesStatusDao()

    @Provides
    fun provideSalesSummaryDao(db: AppDatabase): SalesSummaryDao = db.salesSummaryDao()
}