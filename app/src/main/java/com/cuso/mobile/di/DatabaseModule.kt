package com.cuso.mobile.di

import com.cuso.mobile.repository.LoginRepository


import android.content.Context
import androidx.room.Room
import com.cuso.mobile.database.AppDatabase
import com.cuso.mobile.database.dao.OrganizationDao
import com.cuso.mobile.database.dao.SettingsDao
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
        ).build()
    }
    @Provides
    fun provideOrganizationDao(db: AppDatabase): OrganizationDao = db.organizationDao()
    // ADD THIS:
    @Provides
    @Singleton
    fun provideSettingsDao(database: AppDatabase): SettingsDao {
        return database.settingsDao()
    }
    @Provides
    fun provideLoginRepository(db: AppDatabase): LoginRepository {
        return LoginRepository(db)
    }
}