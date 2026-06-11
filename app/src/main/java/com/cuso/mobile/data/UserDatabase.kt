import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.cuso.mobile.database.dao.UserDao
import com.cuso.mobile.database.entities.TokensEntity
import com.cuso.mobile.database.entities.OrganizationEntity
import com.cuso.mobile.database.entities.UserEntity

@Database(
    entities = [UserEntity::class, OrganizationEntity::class, TokensEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cusotailor_db"
                )
                    .fallbackToDestructiveMigration() // dev stage-ku OK
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}