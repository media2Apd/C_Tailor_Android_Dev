@file:Suppress(
    "UNUSED_VALUE",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "unused_variable",
    "unused_parameter"
)
package com.cuso.mobile.viewmodel

import androidx.lifecycle.ViewModel
import com.cuso.mobile.model.inventory.CreatePoItemRequest
import com.cuso.mobile.model.inventory.CreatePurchaseOrderRequest
import com.cuso.mobile.model.inventory.InventoryItem
import com.cuso.mobile.model.inventory.InventoryItemviewone
import com.cuso.mobile.model.inventory.InventoryPagination
import com.cuso.mobile.model.inventory.LowStockItemDto
import com.cuso.mobile.model.inventory.PurchaseOrderData
import com.cuso.mobile.repository.InventoryRepository
import com.cuso.mobile.utils.launchBusy
import com.google.gson.JsonParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

sealed class CreateItemUiState {
    object Idle : CreateItemUiState()
    object Loading : CreateItemUiState()
    data class Success(val item: InventoryItem) : CreateItemUiState()
    data class Error(val message: String) : CreateItemUiState()
}

enum class ItemSection {
    ITEM_IDENTITY, PRODUCT_IMAGES, PHYSICAL_ATTRIBUTES, TAX_INFO, SALES_INFO, PURCHASE_INFO
}

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

    // ── Helper to extract clean message string from JSON error response ──
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

    // ── Inventory Items: List & Pagination State ──
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

    // ── Inventory Item: View One ──
    private val _viewOneItem = MutableStateFlow<InventoryItemviewone?>(null)
    val viewOneItem: StateFlow<InventoryItemviewone?> = _viewOneItem.asStateFlow()

    private val _isLoadingViewOne = MutableStateFlow(false)
    val isLoadingViewOne: StateFlow<Boolean> = _isLoadingViewOne.asStateFlow()

    private val _viewOneError = MutableStateFlow<String?>(null)
    val viewOneError: StateFlow<String?> = _viewOneError.asStateFlow()

    private val _showViewOneSheet = MutableStateFlow(false)
    val showViewOneSheet: StateFlow<Boolean> = _showViewOneSheet.asStateFlow()

    fun onImageSelected(uri: android.net.Uri?) {
        updateCreateItemForm { it.copy(imageUri = uri) }
    }

    // ── Inventory Item Detail ──
    private val _selectedItem = MutableStateFlow<InventoryItem?>(null)
    val selectedItem: StateFlow<InventoryItem?> = _selectedItem.asStateFlow()

    private val _isLoadingItemDetail = MutableStateFlow(false)
    val isLoadingItemDetail: StateFlow<Boolean> = _isLoadingItemDetail.asStateFlow()

    private val _itemDetailError = MutableStateFlow<String?>(null)
    val itemDetailError: StateFlow<String?> = _itemDetailError.asStateFlow()

    private val _showItemDetailSheet = MutableStateFlow(false)
    val showItemDetailSheet: StateFlow<Boolean> = _showItemDetailSheet.asStateFlow()

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

    // ── Recent Items ──
    private val _recentItems = MutableStateFlow<List<InventoryItem>>(emptyList())
    val recentItems: StateFlow<List<InventoryItem>> = _recentItems.asStateFlow()

    private val _isLoadingRecentItems = MutableStateFlow(false)
    val isLoadingRecentItems: StateFlow<Boolean> = _isLoadingRecentItems.asStateFlow()

    private val _recentItemsError = MutableStateFlow<String?>(null)
    val recentItemsError: StateFlow<String?> = _recentItemsError.asStateFlow()

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

    // ── Adjust Stock ──
    private val _isAdjustingStock = MutableStateFlow(false)
    val isAdjustingStock: StateFlow<Boolean> = _isAdjustingStock.asStateFlow()

    private val _adjustStockError = MutableStateFlow<String?>(null)
    val adjustStockError: StateFlow<String?> = _adjustStockError.asStateFlow()

    private val _adjustStockSuccess = MutableStateFlow(false)
    val adjustStockSuccess: StateFlow<Boolean> = _adjustStockSuccess.asStateFlow()

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

    private val _createItemForm = MutableStateFlow(com.cuso.mobile.model.inventory.CreateItemFormState())
    val createItemForm: StateFlow<com.cuso.mobile.model.inventory.CreateItemFormState> = _createItemForm.asStateFlow()

    private val _expandedSection = MutableStateFlow(ItemSection.ITEM_IDENTITY)
    val expandedSection: StateFlow<ItemSection> = _expandedSection.asStateFlow()

    private val _createItemUiState = MutableStateFlow<CreateItemUiState>(CreateItemUiState.Idle)
    val createItemUiState: StateFlow<CreateItemUiState> = _createItemUiState.asStateFlow()

    fun toggleSection(section: ItemSection) {
        _expandedSection.value = section
    }

    fun updateCreateItemForm(update: (com.cuso.mobile.model.inventory.CreateItemFormState) -> com.cuso.mobile.model.inventory.CreateItemFormState) {
        _createItemForm.value = update(_createItemForm.value)
    }

    fun resetCreateItemForm() {
        _createItemForm.value = com.cuso.mobile.model.inventory.CreateItemFormState()
        _createItemUiState.value = CreateItemUiState.Idle
        _expandedSection.value = ItemSection.ITEM_IDENTITY
    }

    fun populateFormForEdit(item: InventoryItemviewone) {
        _createItemForm.value = com.cuso.mobile.model.inventory.CreateItemFormState(
            itemId = item._id,
            existingImageUrl = item.images.firstOrNull()?.fileUrl,
            itemType = item.type,
            name = item.name,
            sku = item.sku,
            category = "",
            status = item.status,
            unit = item.unit,
            autoGenerateSku = false,
            returnable = false,
            hsnCode = "",
            taxPercentage = "",
            taxInclusive = false,
            length = "",
            width = "",
            height = "",
            weight = "",
            manufacturer = "",
            brand = "",
            barcode = "",
            sellingPrice = item.sellingPrice.toString(),
            salesAccount = "",
            salesDescription = "",
            costPrice = item.costPrice.toString(),
            purchaseAccount = "",
            preferredVendor = "",
            purchaseDescription = "",
            trackInventory = item.trackInventory,
            isSerialTracked = item.isSerialTracked,
            inventoryAccount = "",
            openingStock = item.openingStock?.toString() ?: "",
            imageUri = null
        )
        _expandedSection.value = ItemSection.ITEM_IDENTITY
        _createItemUiState.value = CreateItemUiState.Idle
    }

    fun createInventoryItem(context: android.content.Context) {
        val form = _createItemForm.value
        val validationError = form.validate()
        if (validationError != null) {
            _createItemUiState.value = CreateItemUiState.Error(validationError)
            return
        }

        launchBusy {
            _createItemUiState.value = CreateItemUiState.Loading
            val result = inventoryRepository.createInventoryItem(context, form)
            _createItemUiState.value = result.fold(
                onSuccess = { item ->
                    _inventoryItems.value = listOf(item) + _inventoryItems.value
                    CreateItemUiState.Success(item)
                },
                onFailure = { e -> CreateItemUiState.Error(e.message ?: "Failed to create item") }
            )
        }
    }

    // ── Low Stock Alerts State ──
    private val _lowStockItems = MutableStateFlow<List<LowStockItemDto>>(emptyList())
    val lowStockItems: StateFlow<List<LowStockItemDto>> = _lowStockItems.asStateFlow()

    private val _isLoadingLowStock = MutableStateFlow(false)
    val isLoadingLowStock: StateFlow<Boolean> = _isLoadingLowStock.asStateFlow()

    private val _lowStockError = MutableStateFlow<String?>(null)
    val lowStockError: StateFlow<String?> = _lowStockError.asStateFlow()

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

    private val _isCreatingPO = MutableStateFlow(false)
    val isCreatingPO: StateFlow<Boolean> = _isCreatingPO.asStateFlow()

    private val _createPOError = MutableStateFlow<String?>(null)
    val createPOError: StateFlow<String?> = _createPOError.asStateFlow()

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

    // ── Single Low Stock Item Detail for PO Creation ──
    private val _reorderItemDetail = MutableStateFlow<LowStockItemDto?>(null)
    val reorderItemDetail: StateFlow<LowStockItemDto?> = _reorderItemDetail.asStateFlow()

    private val _isLoadingReorderDetail = MutableStateFlow(false)
    val isLoadingReorderDetail: StateFlow<Boolean> = _isLoadingReorderDetail.asStateFlow()

    private val _reorderDetailError = MutableStateFlow<String?>(null)
    val reorderDetailError: StateFlow<String?> = _reorderDetailError.asStateFlow()

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

    private fun generateSku(itemName: String): String {
        val prefix = itemName
            .filter { it.isLetter() }
            .take(3)
            .uppercase()
            .ifBlank { "ITM" }
        val randomDigits = (100000..999999).random()
        return "$prefix-$randomDigits"
    }

    fun onAutoGenerateSkuToggle(enabled: Boolean) {
        updateCreateItemForm { current ->
            current.copy(
                autoGenerateSku = enabled,
                sku = if (enabled) generateSku(current.name) else current.sku
            )
        }
    }

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

    fun clearAdjustStockSuccess() {
        _adjustStockSuccess.value = false
    }
}