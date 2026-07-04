package com.cuso.mobile.di

import com.cuso.mobile.database.dao.TokensDao
import com.cuso.mobile.network.ApiService
import com.cuso.mobile.repository.DashboardRepository
import com.cuso.mobile.repository.DashboardRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideDashboardRepository(
        apiService: ApiService,
        tokensDao: TokensDao
    ): DashboardRepository {
        return DashboardRepositoryImpl(apiService, tokensDao)
    }
}