package com.cuso.tailor.di

import com.cuso.tailor.database.dao.TokensDao
import com.cuso.tailor.network.user.UserApiService
import com.cuso.tailor.repository.DashboardRepository
import com.cuso.tailor.repository.DashboardRepositoryImpl
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