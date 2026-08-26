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
import com.cuso.mobile.model.inventory.InventoryItem
import com.cuso.mobile.model.inventory.InventoryItemviewone
import com.cuso.mobile.model.inventory.InventoryPagination
import com.cuso.mobile.repository.InventoryRepository
import com.cuso.mobile.utils.launchBusy
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

/**
 * InventoryViewModel - Handles inventory item listing with infinite scroll pagination
 * and item detail operations.
 */
@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

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

    /**
     * Initial fetch or filter search for inventory items (resets pagination back to page 1)
     */
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

    /**
     * Loads the next page of inventory items and appends them to the current list
     */
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
                        _inventoryItems.value = _inventoryItems.value + newItems
                        _currentInventoryPage.value = nextPage
                        _inventoryPagination.value = pagination

                        val totalPages = pagination?.totalPages ?: nextPage
                        _canLoadMoreInventoryItems.value = nextPage < totalPages
                    } else {
                        _canLoadMoreInventoryItems.value = false
                    }
                },
                onFailure = {
                    // Retain load status so the user can re-trigger on scroll
                }
            )
            _isLoadingMoreInventoryItems.value = false
        }
    }

    /**
     * Helper to refresh list from page 1 while preserving current search & status filters
     */
    fun refreshInventoryItems() {
        fetchInventoryItems(page = 1, search = activeInventorySearch, status = activeInventoryStatus)
    }

    fun clearInventoryError() {
        _inventoryError.value = null
    }

    // ── Inventory Item: View One (details bottom sheet) ──
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

    // ── Inventory Item: single item detail ──
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

    // ── Inventory Items: recent ──
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

    // ── Inventory Item: adjust stock ──
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