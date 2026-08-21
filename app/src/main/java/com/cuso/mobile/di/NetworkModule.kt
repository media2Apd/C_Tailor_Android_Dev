@file:Suppress("unused")

package com.cuso.mobile.di

import android.util.Log
import com.cuso.mobile.network.auth.AuthApiService
import com.cuso.mobile.network.finance.FinanceApiService
import com.cuso.mobile.network.hr.HrApiService
import com.cuso.mobile.network.inventory.InventoryApiService
import com.cuso.mobile.network.organization.OrganizationApiService
import com.cuso.mobile.network.sales.SalesCustomerApiService
import com.cuso.mobile.network.sales.SalesLeadApiService
import com.cuso.mobile.network.sales.SalesMeasurementsApiService
import com.cuso.mobile.network.sales.SalesOrderApiService
import com.cuso.mobile.network.sales.SalesPricingApiService
import com.cuso.mobile.network.user.UserApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "http://192.168.0.10:5000"
    // private const val BASE_URL = "https://cuso-tailor-production.onrender.com"

    // ---------------------------------------------------------
    // Base Network Infrastructure (From Old File)
    // ---------------------------------------------------------

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor { message ->
            Log.d("OkHttp", message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // ---------------------------------------------------------
    // Modular API Service Providers (From New File)
    // ---------------------------------------------------------

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService =
        retrofit.create(AuthApiService::class.java)

    @Provides
    @Singleton
    fun provideUserApiService(retrofit: Retrofit): UserApiService =
        retrofit.create(UserApiService::class.java)

    @Provides
    @Singleton
    fun provideOrganizationApiService(retrofit: Retrofit): OrganizationApiService =
        retrofit.create(OrganizationApiService::class.java)

    @Provides
    @Singleton
    fun provideSalesLeadApiService(retrofit: Retrofit): SalesLeadApiService =
        retrofit.create(SalesLeadApiService::class.java)

    @Provides
    @Singleton
    fun provideSalesOrderApiService(retrofit: Retrofit): SalesOrderApiService =
        retrofit.create(SalesOrderApiService::class.java)

    @Provides
    @Singleton
    fun provideSalesCustomerApiService(retrofit: Retrofit): SalesCustomerApiService =
        retrofit.create(SalesCustomerApiService::class.java)

    @Provides
    @Singleton
    fun provideSalesPricingApiService(retrofit: Retrofit): SalesPricingApiService =
        retrofit.create(SalesPricingApiService::class.java)

    @Provides
    @Singleton
    fun provideSalesMeasurementsApiService(retrofit: Retrofit): SalesMeasurementsApiService =
        retrofit.create(SalesMeasurementsApiService::class.java)

    @Provides
    @Singleton
    fun provideFinanceApiService(retrofit: Retrofit): FinanceApiService =
        retrofit.create(FinanceApiService::class.java)

    @Provides
    @Singleton
    fun provideInventoryApiService(retrofit: Retrofit): InventoryApiService =
        retrofit.create(InventoryApiService::class.java)

    @Provides
    @Singleton
    fun provideHrApiService(retrofit: Retrofit): HrApiService =
        retrofit.create(HrApiService::class.java)
}