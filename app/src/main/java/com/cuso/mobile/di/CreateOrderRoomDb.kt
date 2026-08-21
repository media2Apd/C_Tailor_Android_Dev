//// ─────────────────────────────────────────────────────────────
//// These pieces are NOT inside CreateOrderScreen.kt — add them
//// to your existing Room/DI/ViewModel files. Method/field names
//// here must match what CreateOrderScreen.kt calls.
//// ─────────────────────────────────────────────────────────────
//
//package com.cuso.mobile.data.local
//
//import androidx.room.*
//import com.cuso.mobile.view.sales.SelectedGarment
//import kotlinx.coroutines.flow.Flow
//
//// ── 1. DAO ──
//@Dao
//interface SelectedGarmentDao {
//
//    @Query("SELECT * FROM selected_garments WHERE orderSessionId = :sessionId")
//    fun getGarmentsForSession(sessionId: String): Flow<List<SelectedGarment>>
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertGarment(garment: SelectedGarment)
//
//    @Query("DELETE FROM selected_garments WHERE id = :garmentId")
//    suspend fun deleteGarmentById(garmentId: String)
//
//    @Query("DELETE FROM selected_garments WHERE orderSessionId = :sessionId")
//    suspend fun clearSession(sessionId: String)
//}
//
//// ── 2. Add SelectedGarment::class to your existing @Database entities list ──
//// @Database(entities = [SelectedGarment::class, /* ...your other entities */], version = X)
//// abstract class AppDatabase : RoomDatabase() {
////     abstract fun selectedGarmentDao(): SelectedGarmentDao
////     ...
//// }
//
//// ── 3. Provide the DAO in your Hilt DB module ──
//// @Provides
//// fun provideSelectedGarmentDao(db: AppDatabase): SelectedGarmentDao = db.selectedGarmentDao()
//
//
//// ─────────────────────────────────────────────────────────────
//// 4. Add this block inside SalesViewModel (constructor + new members)
//// ─────────────────────────────────────────────────────────────
///*
//
//@HiltViewModel
//class SalesViewModel @Inject constructor(
//    private val selectedGarmentDao: SelectedGarmentDao,
//    // ...your existing dependencies
//) : ViewModel() {
//
//    // current draft order session id — kept stable per draft so re-saving
//    // the same garment updates it instead of duplicating rows.
//    private val currentSessionId = "draft_order"
//
//    private val _selectedGarments = MutableStateFlow<List<SelectedGarment>>(emptyList())
//    val selectedGarments: StateFlow<List<SelectedGarment>> = _selectedGarments.asStateFlow()
//
//    fun loadSelectedGarments() {
//        launchBusy {
//            selectedGarmentDao.getGarmentsForSession(currentSessionId)
//                .collect { list -> _selectedGarments.value = list }
//        }
//    }
//
//    fun addOrUpdateGarment(garment: SelectedGarment) {
//        launchBusy {
//            selectedGarmentDao.insertGarment(garment.copy(orderSessionId = currentSessionId))
//        }
//    }
//
//    fun deleteSelectedGarment(garmentId: String) {
//        launchBusy {
//            selectedGarmentDao.deleteGarmentById(garmentId)
//        }
//    }
//
//    fun clearAllSelectedGarments() {
//        launchBusy {
//            selectedGarmentDao.clearSession(currentSessionId)
//        }
//    }
//}
//
//*/