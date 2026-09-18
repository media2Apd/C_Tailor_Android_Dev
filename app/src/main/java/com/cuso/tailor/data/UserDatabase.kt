//import android.content.Context
//import androidx.room.Database
//import androidx.room.Room
//import androidx.room.RoomDatabase
//import com.cuso.mobile.database.dao.UserDao
//import com.cuso.mobile.database.entities.TokensEntity
//import com.cuso.mobile.database.entities.OrganizationEntity
//import com.cuso.mobile.database.entities.UserEntity
//import com.cuso.mobile.database.local.MIGRATION_1_2
//
//@Database(
//    entities = [UserEntity::class, OrganizationEntity::class, TokensEntity::class],
//    version = 2,
//    exportSchema = false
//)
//abstract class AppDatabase : RoomDatabase() {
//
//    abstract fun userDao(): UserDao
//
//    companion object {
//
//        @Volatile
//        private var INSTANCE: AppDatabase? = null
//
//        fun getDatabase(context: Context): AppDatabase {
//            return INSTANCE ?: synchronized(this) {
//                val instance = Room.databaseBuilder(
//                    context.applicationContext,
//                    AppDatabase::class.java,
//                    "cusotailor_db"
//                )
//                    .addMigrations(MIGRATION_1_2)
//                    .build()
//
//                INSTANCE = instance
//                instance
//            }
//        }
//    }
//}