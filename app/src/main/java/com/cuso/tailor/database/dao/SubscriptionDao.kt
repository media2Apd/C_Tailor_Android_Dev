package com.cuso.tailor.database.dao

import androidx.room.*
import com.cuso.tailor.database.entities.FeatureEnabledEntity
import com.cuso.tailor.database.entities.SubscriptionEntity


@Dao
interface SubscriptionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(subscription: SubscriptionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeatures(features: List<FeatureEnabledEntity>)

    @Query("SELECT * FROM subscription LIMIT 1")
    suspend fun getSubscription(): SubscriptionEntity?

    @Query("DELETE FROM subscription")
    suspend fun clearSubscription()

    @Query("DELETE FROM features_enabled")
    suspend fun clearFeatures()
}