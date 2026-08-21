package com.cuso.mobile.di

import com.cuso.mobile.database.dao.TokensDao
import com.cuso.mobile.network.user.UserApiService
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
        userApiService: UserApiService,
        tokensDao: TokensDao
    ): DashboardRepository {
        return DashboardRepositoryImpl(userApiService, tokensDao)
    }
}