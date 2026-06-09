package com.example.cusotailor.di

import com.example.cusotailor.repository.LoginRepository


import android.content.Context
import androidx.room.Room
import com.example.cusotailor.database.AppDatabase
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
    fun provideLoginRepository(db: AppDatabase): LoginRepository {
        return LoginRepository(db)
    }
}