@file:Suppress(
    "UNUSED_VALUE",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "unused_variable",
    "unused_parameter",
    "unused",
    "RedundantSuppression"
)

package com.cuso.mobile.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.mobile.model.inventory.AdjustStockQuantityRequest
import com.cuso.mobile.model.inventory.CreateInventoryItemResponse
import com.cuso.mobile.model.inventory.CreateItemGroupRequest
import com.cuso.mobile.model.inventory.CreatePoItemRequest
import com.cuso.mobile.model.inventory.CreatePurchaseOrderRequest
import com.cuso.mobile.model.inventory.InventoryItem
import com.cuso.mobile.model.inventory.InventoryItemviewone
import com.cuso.mobile.model.inventory.InventoryPagination
import com.cuso.mobile.model.inventory.ItemGroupDto
import com.cuso.mobile.model.inventory.ItemGroupViewOneData
import com.cuso.mobile.model.inventory.LowStockItemDto
import com.cuso.mobile.model.inventory.PhysicalAttributes
import com.cuso.mobile.model.inventory.PurchaseOrderData
import com.cuso.mobile.model.inventory.StockAdjustmentData
import com.cuso.mobile.model.inventory.TransferStockRequest
import com.cuso.mobile.model.inventory.VariantSelection
import com.cuso.mobile.repository.InventoryRepository
import com.cuso.mobile.utils.launchBusy
import com.google.gson.Gson
import com.google.gson.JsonParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

// =============================================================================
// UI STATE & ENUM DEFINITIONS
// =============================================================================

sealed class CreateItemUiState {
    object Idle : CreateItemUiState()
    object Loading : CreateItemUiState()
    data class Success(val response: CreateInventoryItemResponse) : CreateItemUiState()
    data class Error(val message: String) : CreateItemUiState()
}

enum class ItemSection {
    ITEM_IDENTITY,
    PRODUCT_IMAGES,
    PHYSICAL_ATTRIBUTES,
    TAX_INFO,
    SALES_INFO,
    PURCHASE_INFO
}

data class CreateItemFormState(
    val itemId: String? = null,
    val name: String = "",
    val sku: String = "",
    val barcode: String = "890123456999",
    val parentGroupId: String? = null,
    val category: String = "",
    val unit: String = "pcs",
    val itemType: String = "goods",
    val autoGenerateSku: Boolean = true,
    val returnable: Boolean = false,
    val status: String = "active",
    val costPrice: String = "",
    val sellingPrice: String = "",
    val salesAccount: String = "",
    val purchaseAccount: String = "",
    val salesDescription: String = "",
    val purchaseDescription: String = "",
    val preferredVendor: String = "",
    val length: String = "",
    val width: String = "",
    val height: String = "",
    val weight: String = "0.25",
    val manufacturer: String = "",
    val brand: String = "",
    val hsnCode: String = "",
    val taxPercentage: String = "",
    val taxInclusive: Boolean = false,
    val taxCategory: String = "GST 5%",
    val trackInventory: Boolean = true,
    val isSerialTracked: Boolean = false,
    val reorderLevel: String = "15",
    val safetyStock: String = "8",
    val inventoryAccount: String = "",
    val openingStock: String = "",
    val imageUri: Uri? = null,
    val existingImageUrl: String? = null,
    val variantSelections: List<VariantSelection> = listOf(
        VariantSelection(name = "Size", value = "S"),
        VariantSelection(name = "Color", value = "Blue")
    )
)

data class ItemGroupUiState(
    val isLoading: Boolean = false,
    val itemGroups: List<ItemGroupDto> = emptyList(),
    val filteredList: List<ItemGroupDto> = emptyList(),
    val errorMessage: String? = null,
    val searchQuery: String = ""
)

// =============================================================================
// VIEW MODEL
// =============================================================================

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

    private val gson = Gson()

    // -------------------------------------------------------------------------
    // 1. Item Groups State
    // -------------------------------------------------------------------------
    private val _uiState = MutableStateFlow(ItemGroupUiState())
    val uiState: StateFlow<ItemGroupUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    // ── Item Group Creation State ──
    private val _isCreatingItemGroup = MutableStateFlow(false)
    val isCreatingItemGroup: StateFlow<Boolean> = _isCreatingItemGroup.asStateFlow()

    private val _createItemGroupError = MutableStateFlow<String?>(null)
    val createItemGroupError: StateFlow<String?> = _createItemGroupError.asStateFlow()

    private val _createItemGroupSuccess = MutableStateFlow<String?>(null)
    val createItemGroupSuccess: StateFlow<String?> = _createItemGroupSuccess.asStateFlow()

    // ── Item Group View One State ──
    private val _selectedItemGroupDetail = MutableStateFlow<ItemGroupViewOneData?>(null)
    val selectedItemGroupDetail: StateFlow<ItemGroupViewOneData?> = _selectedItemGroupDetail.asStateFlow()

    private val _isLoadingItemGroupDetail = MutableStateFlow(false)
    val isLoadingItemGroupDetail: StateFlow<Boolean> = _isLoadingItemGroupDetail.asStateFlow()

    // ── Delete State ──
    private val _deleteItemGroupSuccess = MutableStateFlow<String?>(null)
    val deleteItemGroupSuccess: StateFlow<String?> = _deleteItemGroupSuccess.asStateFlow()

    // -------------------------------------------------------------------------
    // 2. Inventory Items: List & Pagination State
    // -------------------------------------------------------------------------
    private val _inventoryItems = MutableStateFlow<List<InventoryItem>>(emptyList())
    val inventoryItems: StateFlow<List<InventoryItem>> = _inventoryItems.asStateFlow()

    private val _inventoryPagination = MutableStateFlow<InventoryPagination?>(null)
    val inventoryPagination: StateFlow<InventoryPagination?> = _inventoryPagination.asStateFlow()

    private val _isLoadingInventoryItems = MutableStateFlow(false)
    val isLoadingInventoryItems: StateFlow<Boolean> = _isLoadingInventoryItems.asStateFlow()

    private val _isLoadingMoreInventoryItems = MutableStateFlow(false)
    val isLoadingMoreInventoryItems: StateFlow<Boolean> = _isLoadingMoreInventoryItems.asStateFlow()

    private val _canLoadMoreInventoryItems = MutableStateFlow(true)
    val canLoadMoreInventoryItems: StateFlow<Boolean> = _canLoadMoreInventoryItems.asStateFlow()

    private val _currentInventoryPage = MutableStateFlow(1)
    val currentInventoryPage: StateFlow<Int> = _currentInventoryPage.asStateFlow()

    private val _inventoryError = MutableStateFlow<String?>(null)
    val inventoryError: StateFlow<String?> = _inventoryError.asStateFlow()

    private var activeInventorySearch: String? = null
    private var activeInventoryStatus: String? = null
    private var fetchInventoryJob: Job? = null

    // -------------------------------------------------------------------------
    // 3. Inventory Item: View One State
    // -------------------------------------------------------------------------
    private val _viewOneItem = MutableStateFlow<InventoryItemviewone?>(null)
    val viewOneItem: StateFlow<InventoryItemviewone?> = _viewOneItem.asStateFlow()

    private val _isLoadingViewOne = MutableStateFlow(false)
    val isLoadingViewOne: StateFlow<Boolean> = _isLoadingViewOne.asStateFlow()

    private val _viewOneError = MutableStateFlow<String?>(null)
    val viewOneError: StateFlow<String?> = _viewOneError.asStateFlow()

    private val _showViewOneSheet = MutableStateFlow(false)
    val showViewOneSheet: StateFlow<Boolean> = _showViewOneSheet.asStateFlow()

    // -------------------------------------------------------------------------
    // 4. Inventory Item: Detail State
    // -------------------------------------------------------------------------
    private val _selectedItem = MutableStateFlow<InventoryItem?>(null)
    val selectedItem: StateFlow<InventoryItem?> = _selectedItem.asStateFlow()

    private val _isLoadingItemDetail = MutableStateFlow(false)
    val isLoadingItemDetail: StateFlow<Boolean> = _isLoadingItemDetail.asStateFlow()

    private val _itemDetailError = MutableStateFlow<String?>(null)
    val itemDetailError: StateFlow<String?> = _itemDetailError.asStateFlow()

    private val _showItemDetailSheet = MutableStateFlow(false)
    val showItemDetailSheet: StateFlow<Boolean> = _showItemDetailSheet.asStateFlow()

    // -------------------------------------------------------------------------
    // 5. Recent Items State
    // -------------------------------------------------------------------------
    private val _recentItems = MutableStateFlow<List<InventoryItem>>(emptyList())
    val recentItems: StateFlow<List<InventoryItem>> = _recentItems.asStateFlow()

    private val _isLoadingRecentItems = MutableStateFlow(false)
    val isLoadingRecentItems: StateFlow<Boolean> = _isLoadingRecentItems.asStateFlow()

    private val _recentItemsError = MutableStateFlow<String?>(null)
    val recentItemsError: StateFlow<String?> = _recentItemsError.asStateFlow()

    // -------------------------------------------------------------------------
    // 6. Stock Adjustment State
    // -------------------------------------------------------------------------
    private val _isAdjustingStock = MutableStateFlow(false)
    val isAdjustingStock: StateFlow<Boolean> = _isAdjustingStock.asStateFlow()

    private val _adjustStockError = MutableStateFlow<String?>(null)
    val adjustStockError: StateFlow<String?> = _adjustStockError.asStateFlow()

    private val _adjustStockSuccess = MutableStateFlow(false)
    val adjustStockSuccess: StateFlow<Boolean> = _adjustStockSuccess.asStateFlow()

    // -------------------------------------------------------------------------
    // 7. Low Stock Alerts & Purchase Order State
    // -------------------------------------------------------------------------
    private val _lowStockItems = MutableStateFlow<List<LowStockItemDto>>(emptyList())
    val lowStockItems: StateFlow<List<LowStockItemDto>> = _lowStockItems.asStateFlow()

    private val _isLoadingLowStock = MutableStateFlow(false)
    val isLoadingLowStock: StateFlow<Boolean> = _isLoadingLowStock.asStateFlow()

    private val _lowStockError = MutableStateFlow<String?>(null)
    val lowStockError: StateFlow<String?> = _lowStockError.asStateFlow()

    private val _isCreatingPO = MutableStateFlow(false)
    val isCreatingPO: StateFlow<Boolean> = _isCreatingPO.asStateFlow()

    private val _createPOError = MutableStateFlow<String?>(null)
    val createPOError: StateFlow<String?> = _createPOError.asStateFlow()

    private val _reorderItemDetail = MutableStateFlow<LowStockItemDto?>(null)
    val reorderItemDetail: StateFlow<LowStockItemDto?> = _reorderItemDetail.asStateFlow()

    private val _isLoadingReorderDetail = MutableStateFlow(false)
    val isLoadingReorderDetail: StateFlow<Boolean> = _isLoadingReorderDetail.asStateFlow()

    private val _reorderDetailError = MutableStateFlow<String?>(null)
    val reorderDetailError: StateFlow<String?> = _reorderDetailError.asStateFlow()

    // -------------------------------------------------------------------------
    // 8. Create Item Form State
    // -------------------------------------------------------------------------
    private val _expandedSection = MutableStateFlow(ItemSection.ITEM_IDENTITY)
    val expandedSection: StateFlow<ItemSection> = _expandedSection.asStateFlow()

    private val _createItemForm = MutableStateFlow(CreateItemFormState())
    val createItemForm: StateFlow<CreateItemFormState> = _createItemForm.asStateFlow()

    private val _createItemUiState = MutableStateFlow<CreateItemUiState>(CreateItemUiState.Idle)
    val createItemUiState: StateFlow<CreateItemUiState> = _createItemUiState.asStateFlow()

    // -------------------------------------------------------------------------
    // 9. Stock Adjustments & Transfer History State
    // -------------------------------------------------------------------------
    private val _validAdjustmentReasons = MutableStateFlow<List<String>>(emptyList())
    val validAdjustmentReasons: StateFlow<List<String>> = _validAdjustmentReasons.asStateFlow()

    private val _stockAdjustmentsList = MutableStateFlow<List<StockAdjustmentData>>(emptyList())
    val stockAdjustmentsList: StateFlow<List<StockAdjustmentData>> = _stockAdjustmentsList.asStateFlow()

    private val _selectedAdjustmentDetail = MutableStateFlow<StockAdjustmentData?>(null)
    val selectedAdjustmentDetail: StateFlow<StockAdjustmentData?> = _selectedAdjustmentDetail.asStateFlow()

    private val _isLoadingAdjustments = MutableStateFlow(false)
    val isLoadingAdjustments: StateFlow<Boolean> = _isLoadingAdjustments.asStateFlow()

    private val _isSubmittingAdjustment = MutableStateFlow(false)
    val isSubmittingAdjustment: StateFlow<Boolean> = _isSubmittingAdjustment.asStateFlow()

    private val _adjustmentSuccessMessage = MutableStateFlow<String?>(null)
    val adjustmentSuccessMessage: StateFlow<String?> = _adjustmentSuccessMessage.asStateFlow()

    private val _adjustmentErrorMessage = MutableStateFlow<String?>(null)
    val adjustmentErrorMessage: StateFlow<String?> = _adjustmentErrorMessage.asStateFlow()

    // =========================================================================
    // INIT
    // =========================================================================
    init {
        loadItemGroups()
    }

    // =========================================================================
    // ITEM GROUP ACTIONS
    // =========================================================================

    /**
     * Fetch all item groups from repository
     */
    fun loadItemGroups(query: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = inventoryRepository.getInventoryItemGroup(search = query)
            result.onSuccess { response ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        itemGroups = response.groups,
                        filteredList = response.groups,
                        errorMessage = null
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = extractErrorMessage(error.message)
                    )
                }
            }
        }
    }

    /**
     * Handle local and server-side debounced search for item groups
     */
    fun onSearchQueryChanged(newQuery: String) {
        _uiState.update { state ->
            val filtered = if (newQuery.isBlank()) {
                state.itemGroups
            } else {
                state.itemGroups.filter { group ->
                    group.name.contains(newQuery, ignoreCase = true) ||
                            (group.groupCode?.contains(newQuery, ignoreCase = true) == true) ||
                            group.variantAttributes.any { attr -> attr.name.contains(newQuery, ignoreCase = true) }
                }
            }
            state.copy(searchQuery = newQuery, filteredList = filtered)
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)
            if (newQuery.isNotBlank()) {
                loadItemGroups(query = newQuery)
            }
        }
    }

    /**
     * Create a new item group and refresh list on success.
     */
    fun createItemGroup(
        request: CreateItemGroupRequest,
        onSuccessCallback: () -> Unit
    ) {
        viewModelScope.launch {
            _isCreatingItemGroup.value = true
            _createItemGroupError.value = null
            _createItemGroupSuccess.value = null

            val result = inventoryRepository.createItemGroup(request)
            _isCreatingItemGroup.value = false

            result.onSuccess {
                _createItemGroupSuccess.value = "Item Group created successfully!"
                loadItemGroups() // Refresh groups list
                onSuccessCallback()
            }.onFailure { error ->
                _createItemGroupError.value = extractErrorMessage(error.message)
            }
        }
    }

    /**
     * Delete item group and refresh list on success.
     */
    fun deleteItemGroup(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = inventoryRepository.deleteItemGroup(id = id)

            result.onSuccess { message ->
                _deleteItemGroupSuccess.value = message
                loadItemGroups(query = _uiState.value.searchQuery.takeIf { it.isNotBlank() })
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = extractErrorMessage(error.message)
                    )
                }
            }
        }
    }

    /**
     * Fetch single item group by ID for prefilling/editing.
     */
    fun fetchItemGroupViewOne(id: String, onLoaded: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoadingItemGroupDetail.value = true
            val result = inventoryRepository.getInventoryItemGroupViewOne(id)
            _isLoadingItemGroupDetail.value = false

            result.onSuccess { data ->
                _selectedItemGroupDetail.value = data
                onLoaded()
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = extractErrorMessage(error.message)) }
            }
        }
    }

    /**
     * Update an existing item group.
     */
//    fun updateItemGroup(
//        id: String,
//        request: CreateItemGroupRequest,
//        onSuccessCallback: () -> Unit
//    ) {
//        viewModelScope.launch {
//            _isCreatingItemGroup.value = true
//            _createItemGroupError.value = null
//            _createItemGroupSuccess.value = null
//
//            val result = inventoryRepository.updateItemGroup(id, request)
//            _isCreatingItemGroup.value = false
//
//            result.onSuccess {
//                _createItemGroupSuccess.value = "Item Group updated successfully!"
//                loadItemGroups()
//                onSuccessCallback()
//            }.onFailure { error ->
//                _createItemGroupError.value = extractErrorMessage(error.message)
//            }
//        }
//    }

    fun clearSelectedItemGroupDetail() {
        _selectedItemGroupDetail.value = null
    }

    fun clearDeleteSuccessMessage() {
        _deleteItemGroupSuccess.value = null
    }

    fun clearItemGroupAlerts() {
        _createItemGroupError.value = null
        _createItemGroupSuccess.value = null
    }


    fun refreshItemGroups() {
        loadItemGroups(query = _uiState.value.searchQuery.takeIf { it.isNotBlank() })
    }

    // =========================================================================
    // INVENTORY ITEMS ACTIONS
    // =========================================================================

    fun fetchInventoryItems(
        page: Int = 1,
        limit: Int = 10,
        search: String? = null,
        status: String? = null
    ) {
        fetchInventoryJob?.cancel()
        fetchInventoryJob = launchBusy {
            _isLoadingInventoryItems.value = true
            _inventoryError.value = null
            _currentInventoryPage.value = page
            activeInventorySearch = search
            activeInventoryStatus = status

            val result = inventoryRepository.getInventoryItems(page, limit, search, status)
            result.fold(
                onSuccess = { response ->
                    val newItems = response.data
                    val pagination = response.pagination

                    _inventoryItems.value = newItems
                    _inventoryPagination.value = pagination

                    val totalPages = pagination?.totalPages ?: 1
                    _canLoadMoreInventoryItems.value = page < totalPages && newItems.isNotEmpty()
                },
                onFailure = { e ->
                    if (e !is CancellationException) {
                        _inventoryError.value = e.message ?: "Failed to fetch inventory items"
                    }
                }
            )
            _isLoadingInventoryItems.value = false
        }
    }

    fun loadMoreInventoryItems(limit: Int = 10) {
        if (_isLoadingMoreInventoryItems.value || _isLoadingInventoryItems.value || !_canLoadMoreInventoryItems.value) {
            return
        }

        launchBusy {
            _isLoadingMoreInventoryItems.value = true
            val nextPage = _currentInventoryPage.value + 1

            val result = inventoryRepository.getInventoryItems(
                page = nextPage,
                limit = limit,
                search = activeInventorySearch,
                status = activeInventoryStatus
            )

            result.fold(
                onSuccess = { response ->
                    val newItems = response.data
                    val pagination = response.pagination

                    if (newItems.isNotEmpty()) {
                        _inventoryItems.value += newItems
                        _currentInventoryPage.value = nextPage
                        _inventoryPagination.value = pagination

                        val totalPages = pagination?.totalPages ?: nextPage
                        _canLoadMoreInventoryItems.value = nextPage < totalPages
                    } else {
                        _canLoadMoreInventoryItems.value = false
                    }
                },
                onFailure = { }
            )
            _isLoadingMoreInventoryItems.value = false
        }
    }

    fun refreshInventoryItems() {
        fetchInventoryItems(page = 1, search = activeInventorySearch, status = activeInventoryStatus)
    }

    fun clearInventoryError() {
        _inventoryError.value = null
    }

    // =========================================================================
    // INVENTORY VIEW ONE ACTIONS
    // =========================================================================

    fun onViewOneClicked(itemId: String) {
        _showViewOneSheet.value = true
        fetchInventoryViewOne(itemId)
    }

    fun fetchInventoryViewOne(id: String) {
        launchBusy {
            _isLoadingViewOne.value = true
            _viewOneError.value = null

            val result = inventoryRepository.getInventoryViewOne(id)
            result.fold(
                onSuccess = { item -> _viewOneItem.value = item },
                onFailure = { e -> _viewOneError.value = e.message ?: "Failed to fetch item details" }
            )
            _isLoadingViewOne.value = false
        }
    }

    fun dismissViewOneSheet() {
        _showViewOneSheet.value = false
        _viewOneItem.value = null
        _viewOneError.value = null
    }

    fun clearViewOneItem() {
        _viewOneItem.value = null
        _viewOneError.value = null
    }

    // =========================================================================
    // INVENTORY ITEM DETAIL ACTIONS
    // =========================================================================

    fun onViewItemClicked(itemId: String) {
        _showItemDetailSheet.value = true
        fetchInventoryItemDetail(itemId)
    }

    fun fetchInventoryItemDetail(id: String) {
        launchBusy {
            _isLoadingItemDetail.value = true
            _itemDetailError.value = null

            val result = inventoryRepository.getInventoryItemById(id)
            result.fold(
                onSuccess = { item -> _selectedItem.value = item },
                onFailure = { e -> _itemDetailError.value = e.message ?: "Failed to fetch item details" }
            )
            _isLoadingItemDetail.value = false
        }
    }

    fun dismissItemDetailSheet() {
        _showItemDetailSheet.value = false
        _selectedItem.value = null
        _itemDetailError.value = null
    }

    // =========================================================================
    // RECENT ITEMS ACTIONS
    // =========================================================================

    fun fetchRecentInventoryItems(limit: Int = 10) {
        launchBusy {
            _isLoadingRecentItems.value = true
            _recentItemsError.value = null

            val result = inventoryRepository.getRecentInventoryItems(limit)
            result.fold(
                onSuccess = { response -> _recentItems.value = response.data },
                onFailure = { e -> _recentItemsError.value = e.message ?: "Failed to fetch recent items" }
            )
            _isLoadingRecentItems.value = false
        }
    }

    // =========================================================================
    // STOCK ADJUSTMENT ACTIONS
    // =========================================================================

    fun adjustStock(
        itemId: String,
        adjustmentType: String,
        quantity: Double,
        reason: String,
        notes: String
    ) {
        launchBusy {
            _isAdjustingStock.value = true
            _adjustStockError.value = null
            _adjustStockSuccess.value = false

            val result = inventoryRepository.adjustStock(itemId, adjustmentType, quantity, reason, notes)
            result.fold(
                onSuccess = { updatedItem ->
                    _selectedItem.value = updatedItem
                    _adjustStockSuccess.value = true
                },
                onFailure = { e -> _adjustStockError.value = e.message ?: "Failed to adjust stock" }
            )
            _isAdjustingStock.value = false
        }
    }

    fun clearAdjustStockSuccess() {
        _adjustStockSuccess.value = false
    }

    // =========================================================================
    // LOW STOCK ALERTS & PURCHASE ORDER ACTIONS
    // =========================================================================

    fun fetchLowStockAlerts(warehouseId: String? = null) {
        launchBusy {
            _isLoadingLowStock.value = true
            _lowStockError.value = null
            val result = inventoryRepository.getLowStockAlerts(warehouseId)
            _isLoadingLowStock.value = false
            result.onSuccess { list ->
                _lowStockItems.value = list
            }.onFailure { error ->
                _lowStockError.value = error.message ?: "Failed to load low stock alerts"
            }
        }
    }

    fun createPurchaseOrder(
        supplierId: String,
        warehouseId: String,
        itemId: String,
        qty: Double,
        rate: Double,
        eta: String?,
        notes: String?,
        onSuccess: (PurchaseOrderData) -> Unit,
        onError: (String) -> Unit
    ) {
        launchBusy {
            _isCreatingPO.value = true
            _createPOError.value = null

            val poItem = CreatePoItemRequest(
                itemId = itemId,
                qty = qty,
                rate = rate,
                taxPercent = 18.0
            )

            val request = CreatePurchaseOrderRequest(
                supplierId = supplierId,
                warehouseId = warehouseId,
                eta = eta,
                items = listOf(poItem),
                internalNotes = notes?.takeIf { it.isNotBlank() }
            )

            val result = inventoryRepository.createPurchaseOrder(request)
            _isCreatingPO.value = false

            result.onSuccess { data ->
                onSuccess(data)
            }.onFailure { error ->
                val clean = extractErrorMessage(error.message)
                _createPOError.value = clean
                onError(clean)
            }
        }
    }

    fun fetchLowStockItemDetail(itemId: String, warehouseId: String) {
        launchBusy {
            _isLoadingReorderDetail.value = true
            _reorderDetailError.value = null
            val result = inventoryRepository.getLowStockItemDetail(itemId, warehouseId)
            _isLoadingReorderDetail.value = false

            result.onSuccess { item ->
                _reorderItemDetail.value = item
            }.onFailure { error ->
                _reorderDetailError.value = error.message ?: "Failed to fetch reorder item"
            }
        }
    }

    fun setReorderItemDirectly(item: LowStockItemDto?) {
        _reorderItemDetail.value = item
    }

    fun clearReorderItemDetail() {
        _reorderItemDetail.value = null
        _reorderDetailError.value = null
    }

    // =========================================================================
    // CREATE / EDIT ITEM FORM ACTIONS
    // =========================================================================

    fun toggleSection(section: ItemSection) {
        _expandedSection.value = section
    }

    fun updateCreateItemForm(transform: (CreateItemFormState) -> CreateItemFormState) {
        _createItemForm.update(transform)
    }

    fun onImageSelected(uri: Uri) {
        _createItemForm.update { it.copy(imageUri = uri) }
    }

    fun resetCreateItemForm() {
        _createItemForm.value = CreateItemFormState()
        _createItemUiState.value = CreateItemUiState.Idle
        _expandedSection.value = ItemSection.ITEM_IDENTITY
    }

    fun onAutoGenerateSkuToggle(enabled: Boolean) {
        _createItemForm.update { current ->
            current.copy(
                autoGenerateSku = enabled,
                sku = if (enabled) generateSku(current.name) else current.sku
            )
        }
    }

    fun populateFormForEdit(item: InventoryItemviewone) {
        _createItemForm.value = CreateItemFormState(
            itemId = item._id,
            existingImageUrl = item.images.firstOrNull()?.fileUrl,
            itemType = item.type,
            name = item.name,
            sku = item.sku,
            category = item.categoryId ?: "",
            status = item.status,
            unit = item.unit,
            autoGenerateSku = false,
            returnable = item.returnable,
            hsnCode = item.hsnCode ?: "",
            taxCategory = item.taxCategory ?: "GST 5%",
            taxPercentage = "",
            taxInclusive = false,
            length = item.physicalAttributes?.length?.takeIf { it > 0 }?.toString() ?: "",
            width = item.physicalAttributes?.width?.takeIf { it > 0 }?.toString() ?: "",
            height = item.physicalAttributes?.height?.takeIf { it > 0 }?.toString() ?: "",
            weight = item.physicalAttributes?.weight?.takeIf { it > 0 }?.toString() ?: "0.25",
            manufacturer = item.manufacturer ?: "",
            brand = item.brand ?: "",
            barcode = item.barcode ?: "",
            sellingPrice = if (item.sellingPrice > 0) item.sellingPrice.toString() else "",
            costPrice = if (item.costPrice > 0) item.costPrice.toString() else "",
            trackInventory = item.trackInventory,
            isSerialTracked = item.isSerialTracked,
            reorderLevel = item.reorderLevel.toString(),
            safetyStock = item.safetyStock.toString(),
            imageUri = null
        )
        _expandedSection.value = ItemSection.ITEM_IDENTITY
        _createItemUiState.value = CreateItemUiState.Idle
    }

    fun createInventoryItem(context: Context) {
        val form = _createItemForm.value

        viewModelScope.launch {
            _createItemUiState.value = CreateItemUiState.Loading

            val textMedia = "text/plain".toMediaTypeOrNull()

            val physicalAttributesJson = gson.toJson(
                PhysicalAttributes(
                    weight = form.weight.toDoubleOrNull() ?: 0.25,
                    weightUnit = "kg",
                    dimensionUnit = "cm"
                )
            )

            val variantSelectionsJson = gson.toJson(form.variantSelections)

            val params = mutableMapOf<String, RequestBody>(
                "name" to form.name.toRequestBody(textMedia),
                "sku" to form.sku.toRequestBody(textMedia),
                "barcode" to form.barcode.toRequestBody(textMedia),
                "costPrice" to form.costPrice.toRequestBody(textMedia),
                "sellingPrice" to form.sellingPrice.toRequestBody(textMedia),
                "taxCategory" to form.taxCategory.toRequestBody(textMedia),
                "trackInventory" to form.trackInventory.toString().toRequestBody(textMedia),
                "reorderLevel" to form.reorderLevel.toRequestBody(textMedia),
                "safetyStock" to form.safetyStock.toRequestBody(textMedia),
                "physicalAttributes" to physicalAttributesJson.toRequestBody(textMedia),
                "variantSelections" to variantSelectionsJson.toRequestBody(textMedia)
            )

            form.parentGroupId?.takeIf { it.isNotBlank() }?.let {
                params["parentGroupId"] = it.toRequestBody(textMedia)
            }

            form.category.takeIf { it.isNotBlank() }?.let {
                params["categoryId"] = it.toRequestBody(textMedia)
            }

            val imagePart = inventoryRepository.prepareImagePart(context, form.imageUri)
            val result = inventoryRepository.createItem(params, imagePart)

            result.fold(
                onSuccess = { response ->
                    _createItemUiState.value = CreateItemUiState.Success(response)
                },
                onFailure = { error ->
                    _createItemUiState.value = CreateItemUiState.Error(error.localizedMessage ?: "Failed to create item")
                }
            )
        }
    }

    /**
     * Update an existing inventory item on the server.
     */
    fun updateInventoryItem(context: Context) {
        val form = _createItemForm.value
        val itemId = form.itemId ?: return

        viewModelScope.launch {
            _createItemUiState.value = CreateItemUiState.Loading
            val textMedia = "text/plain".toMediaTypeOrNull()

            val physicalAttributesJson = gson.toJson(
                PhysicalAttributes(
                    length = form.length.toDoubleOrNull() ?: 0.0,
                    width = form.width.toDoubleOrNull() ?: 0.0,
                    height = form.height.toDoubleOrNull() ?: 0.0,
                    weight = form.weight.toDoubleOrNull() ?: 0.25,
                    weightUnit = "kg",
                    dimensionUnit = "cm"
                )
            )

            val params = mutableMapOf<String, RequestBody>(
                "name" to form.name.toRequestBody(textMedia),
                "sku" to form.sku.toRequestBody(textMedia),
                "barcode" to form.barcode.toRequestBody(textMedia),
                "costPrice" to form.costPrice.toRequestBody(textMedia),
                "sellingPrice" to form.sellingPrice.toRequestBody(textMedia),
                "unit" to form.unit.toRequestBody(textMedia),
                "status" to form.status.toRequestBody(textMedia),
                "brand" to form.brand.toRequestBody(textMedia),
                "manufacturer" to form.manufacturer.toRequestBody(textMedia),
                "hsnCode" to form.hsnCode.toRequestBody(textMedia),
                "taxCategory" to form.taxCategory.toRequestBody(textMedia),
                "returnable" to form.returnable.toString().toRequestBody(textMedia),
                "trackInventory" to form.trackInventory.toString().toRequestBody(textMedia),
                "reorderLevel" to form.reorderLevel.toRequestBody(textMedia),
                "safetyStock" to form.safetyStock.toRequestBody(textMedia),
                "physicalAttributes" to physicalAttributesJson.toRequestBody(textMedia)
            )

            form.category.takeIf { it.isNotBlank() }?.let {
                params["categoryId"] = it.toRequestBody(textMedia)
            }

            val imagePart = inventoryRepository.prepareImagePart(context, form.imageUri)
            val result = inventoryRepository.updateItem(itemId, params, imagePart)

            result.fold(
                onSuccess = { response ->
                    _createItemUiState.value = CreateItemUiState.Success(
                        CreateInventoryItemResponse(success = response.success, data = response.data)
                    )
                    refreshInventoryItems()
                },
                onFailure = { error ->
                    _createItemUiState.value = CreateItemUiState.Error(
                        error.localizedMessage ?: "Failed to update item"
                    )
                }
            )
        }
    }

    // =========================================================================
    // STOCK ADJUSTMENT ACTIONS
    // =========================================================================

    /**
     * Fetch valid adjustment reasons.
     */
    fun fetchValidAdjustmentReasons() {
        viewModelScope.launch {
            inventoryRepository.getValidAdjustmentReasons().onSuccess { reasons ->
                _validAdjustmentReasons.value = reasons
            }
        }
    }

    /**
     * Submit Increase or Decrease Stock Adjustment.
     */
    fun submitStockAdjustment(
        request: AdjustStockQuantityRequest,
        onSuccess: (StockAdjustmentData) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isSubmittingAdjustment.value = true
            _adjustmentErrorMessage.value = null
            _adjustmentSuccessMessage.value = null

            val result = inventoryRepository.adjustStockQuantity(request)
            _isSubmittingAdjustment.value = false

            result.onSuccess { data ->
                _adjustmentSuccessMessage.value = "Stock adjusted successfully (${data.adjustmentCode})"
                refreshInventoryItems()
                onSuccess(data)
            }.onFailure { error ->
                _adjustmentErrorMessage.value = extractErrorMessage(error.message)
            }
        }
    }

    /**
     * Submit Stock Transfer between Warehouses/Bins.
     */
    fun submitStockTransfer(
        request: TransferStockRequest,
        onSuccess: (StockAdjustmentData) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isSubmittingAdjustment.value = true
            _adjustmentErrorMessage.value = null
            _adjustmentSuccessMessage.value = null

            val result = inventoryRepository.transferStock(request)
            _isSubmittingAdjustment.value = false

            result.onSuccess { data ->
                _adjustmentSuccessMessage.value = "Stock transferred successfully (${data.adjustmentCode})"
                refreshInventoryItems()
                onSuccess(data)
            }.onFailure { error ->
                _adjustmentErrorMessage.value = extractErrorMessage(error.message)
            }
        }
    }

    /**
     * Reverse a previous adjustment record.
     */
    fun reverseAdjustment(
        adjustmentId: String,
        reason: String? = "Other",
        notes: String? = null,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isSubmittingAdjustment.value = true
            _adjustmentErrorMessage.value = null
            _adjustmentSuccessMessage.value = null

            val result = inventoryRepository.reverseStockAdjustment(adjustmentId, reason, notes)
            _isSubmittingAdjustment.value = false

            result.onSuccess {
                _adjustmentSuccessMessage.value = "Adjustment reversed successfully"
                fetchStockAdjustmentsList()
                refreshInventoryItems()
                onSuccess()
            }.onFailure { error ->
                _adjustmentErrorMessage.value = extractErrorMessage(error.message)
            }
        }
    }

    /**
     * Fetch the list of past adjustments.
     */
    fun fetchStockAdjustmentsList(
        page: Int = 1,
        limit: Int = 20,
        itemId: String? = null,
        warehouseId: String? = null
    ) {
        viewModelScope.launch {
            _isLoadingAdjustments.value = true
            _adjustmentErrorMessage.value = null

            val result = inventoryRepository.getStockAdjustmentsList(page, limit, itemId, warehouseId)
            _isLoadingAdjustments.value = false

            result.onSuccess { response ->
                _stockAdjustmentsList.value = response.data
            }.onFailure { error ->
                _adjustmentErrorMessage.value = extractErrorMessage(error.message)
            }
        }
    }

    /**
     * Fetch a single adjustment record by ID.
     */
    fun fetchStockAdjustmentById(id: String) {
        viewModelScope.launch {
            _isLoadingAdjustments.value = true
            val result = inventoryRepository.getStockAdjustmentById(id)
            _isLoadingAdjustments.value = false

            result.onSuccess { data ->
                _selectedAdjustmentDetail.value = data
            }.onFailure { error ->
                _adjustmentErrorMessage.value = extractErrorMessage(error.message)
            }
        }
    }

    fun clearAdjustmentAlerts() {
        _adjustmentSuccessMessage.value = null
        _adjustmentErrorMessage.value = null
    }

    // =========================================================================
    // PRIVATE HELPER FUNCTIONS
    // =========================================================================

    private fun generateSku(itemName: String): String {
        val prefix = itemName
            .filter { it.isLetter() }
            .take(3)
            .uppercase()
            .ifBlank { "ITM" }
        val randomDigits = (100000..999999).random()
        return "$prefix-$randomDigits"
    }

    private fun extractErrorMessage(raw: String?): String {
        if (raw.isNullOrBlank()) return "An unexpected error occurred"
        val trimmed = raw.trim()
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            try {
                val json = JsonParser.parseString(trimmed)
                if (json.isJsonObject) {
                    val obj = json.asJsonObject
                    if (obj.has("message") && !obj.get("message").isJsonNull) {
                        return obj.get("message").asString
                    }
                    if (obj.has("error") && !obj.get("error").isJsonNull) {
                        return obj.get("error").asString
                    }
                }
            } catch (_: Exception) { }
        }
        return trimmed
    }
}