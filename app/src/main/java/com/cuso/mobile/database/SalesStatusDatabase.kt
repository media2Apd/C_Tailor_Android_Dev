//package com.cuso.mobile.database
//
//import android.content.Context
//import androidx.room.Database
//import androidx.room.Room
//import androidx.room.RoomDatabase
//import androidx.room.migration.Migration
//import androidx.sqlite.db.SupportSQLiteDatabase
//import com.cuso.mobile.database.dao.LeadDao
//import com.cuso.mobile.database.dao.SalesStatusDao
//import com.cuso.mobile.database.dao.SalesSummaryDao
//import com.cuso.mobile.database.entities.LeadEntity
//import com.cuso.mobile.database.entities.SalesStatusEntity
//import com.cuso.mobile.database.entities.SalesSummaryEntity
//import dagger.Module
//import dagger.Provides
//import dagger.hilt.InstallIn
//import dagger.hilt.android.qualifiers.ApplicationContext
//import dagger.hilt.components.SingletonComponent
//import javax.inject.Named
//import javax.inject.Singleton
//
//@Database(
//    entities = [
//        SalesStatusEntity::class,
//        SalesSummaryEntity::class,
//        LeadEntity::class
//    ],
//    version = 2,
//    exportSchema = false
//)
//abstract class SalesDatabase : RoomDatabase() {
//    abstract fun salesStatusDao(): SalesStatusDao
//    abstract fun salesSummaryDao(): SalesSummaryDao
//    abstract fun leadDao(): LeadDao
//}
//
//val MIGRATION_1_2 = object : Migration(1, 2) {
//    override fun migrate(database: SupportSQLiteDatabase) {
//        database.execSQL(
//            "CREATE TABLE IF NOT EXISTS sales_summary (" +
//                    "id INTEGER PRIMARY KEY NOT NULL, " +
//                    "total_assigned INTEGER NOT NULL, " +
//                    "active INTEGER NOT NULL, " +
//                    "inactive INTEGER NOT NULL, " +
//                    "available_slots INTEGER)"
//        )
//    }
//}
//
//@Module
//@InstallIn(SingletonComponent::class)
//object SalesDatabaseModule {
//
//    @Provides
//    @Singleton
//    @Named("sales_db")
//    fun provideSalesDatabase(
//        @ApplicationContext context: Context
//    ): SalesDatabase {
//        return Room.databaseBuilder(
//            context,
//            SalesDatabase::class.java,
//            "sales_database"
//        )
//            .addMigrations(MIGRATION_1_2)
//            .fallbackToDestructiveMigrationOnDowngrade() // safety net for downgrades
//            .build()
//    }
//
//    @Provides
//    @Singleton
//    fun provideSalesStatusDao(
//        @Named("sales_db") db: SalesDatabase
//    ): SalesStatusDao = db.salesStatusDao()
//
//    @Provides
//    @Singleton
//    fun provideSalesSummaryDao(
//        @Named("sales_db") db: SalesDatabase
//    ): SalesSummaryDao = db.salesSummaryDao()
//
//    // ✅ LeadDao now correctly comes from SalesDatabase, not AppDatabase
//    @Provides
//    @Singleton
//    fun provideLeadDao(
//        @Named("sales_db") db: SalesDatabase
//    ): LeadDao = db.leadDao()
//}