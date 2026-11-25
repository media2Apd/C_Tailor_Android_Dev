package com.cuso.tailor.di

import android.content.Context
import androidx.room.Room
import com.cuso.tailor.database.AppDatabase
import com.cuso.tailor.database.DatabaseKeyManager
import com.cuso.tailor.database.dao.LeadDao
import com.cuso.tailor.database.dao.OrganizationDao
import com.cuso.tailor.database.dao.PermissionDao
import com.cuso.tailor.database.dao.SalesStatusDao
import com.cuso.tailor.database.dao.SalesSummaryDao
import com.cuso.tailor.database.dao.SelectedGarmentDao
import com.cuso.tailor.database.dao.SettingsDao
import com.cuso.tailor.database.dao.SubscriptionDao
import com.cuso.tailor.database.dao.TokensDao
import com.cuso.tailor.database.dao.UserDao
import com.cuso.tailor.repository.LoginRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        System.loadLibrary("sqlcipher")

        val dbName = "cusotailor_db"

        // Existing unencrypted DB irundha crash aagama irukka clean pannum
        val dbFile = context.getDatabasePath(dbName)
        if (dbFile.exists() && isUnencryptedSqlite(dbFile)) {
            dbFile.delete()
            File("${dbFile.path}-wal").delete()
            File("${dbFile.path}-shm").delete()
            File("${dbFile.path}-journal").delete()
        }

        val passphrase = DatabaseKeyManager.getPassphrase(context)
        val factory = SupportOpenHelperFactory(passphrase)

        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            dbName
        )
            .openHelperFactory(factory)
            // TEMPORARY for dev. Replace with real Migration(2, 3)
            // before shipping to anyone with existing data.
            .fallbackToDestructiveMigration(false)
            .build()
    }

    private fun isUnencryptedSqlite(file: File): Boolean {
        return try {
            file.inputStream().use { input ->
                val header = ByteArray(15)
                if (input.read(header) < 15) return false
                String(header, Charsets.US_ASCII) == "SQLite format 3"
            }
        } catch (_: Exception) {
            false
        }
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

    @Provides
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides
    fun provideSubscriptionDao(db: AppDatabase): SubscriptionDao = db.subscriptionDao()

    @Provides
    fun provideSelectedGarmentDao(db: AppDatabase): SelectedGarmentDao = db.selectedGarmentDao()

    @Provides
    fun providePermissionDao(db: AppDatabase): PermissionDao = db.permissionDao()
}