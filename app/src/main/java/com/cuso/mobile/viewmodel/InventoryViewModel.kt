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
import com.cuso.mobile.model.inventory.BarcodeItemDoc
import com.cuso.mobile.model.inventory.BillCreatedData
import com.cuso.mobile.model.inventory.BulkItemDoc
import com.cuso.mobile.model.inventory.CapacitySummary
import com.cuso.mobile.model.inventory.CreateInventoryItemResponse
import com.cuso.mobile.model.inventory.CreateItemGroupRequest
import com.cuso.mobile.model.inventory.CreatePoItemRequest
import com.cuso.mobile.model.inventory.CreatePurchaseOrderRequest
import com.cuso.mobile.model.inventory.CreateRequisitionRequest
import com.cuso.mobile.model.inventory.CreateSupplierRequest
import com.cuso.mobile.model.inventory.CreateWarehouseRequest
import com.cuso.mobile.model.inventory.DecreaseStockRequest
import com.cuso.mobile.model.inventory.GenerateBarcodeRequest
import com.cuso.mobile.model.inventory.IncreaseStockRequest
import com.cuso.mobile.model.inventory.InventoryItem
import com.cuso.mobile.model.inventory.InventoryItemviewone
import com.cuso.mobile.model.inventory.InventoryPagination
import com.cuso.mobile.model.inventory.ItemGroupDto
import com.cuso.mobile.model.inventory.ItemGroupViewOneData
import com.cuso.mobile.model.inventory.LowStockItemDto
import com.cuso.mobile.model.inventory.POBillConvertData
import com.cuso.mobile.model.inventory.PhysicalAttributes
import com.cuso.mobile.model.inventory.PurchaseOrder
import com.cuso.mobile.model.inventory.PurchaseOrderData
import com.cuso.mobile.model.inventory.PurchaseReceiveItem
import com.cuso.mobile.model.inventory.PurchaseRequisition
import com.cuso.mobile.model.inventory.ReceiveHistoryByPoResponse
import com.cuso.mobile.model.inventory.ReceivePurchaseOrderRequest
import com.cuso.mobile.model.inventory.RequisitionApprovalActionRequest
import com.cuso.mobile.model.inventory.StockAdjustmentData
import com.cuso.mobile.model.inventory.StockSummaryItemDto
import com.cuso.mobile.model.inventory.SupplierDropdownItem
import com.cuso.mobile.model.inventory.SupplierDto
import com.cuso.mobile.model.inventory.SupplierLedgerContainer
import com.cuso.mobile.model.inventory.TransferStockRequest
import com.cuso.mobile.model.inventory.UpdateWarehouseRequest
import com.cuso.mobile.model.inventory.VariantSelection
import com.cuso.mobile.model.inventory.WarehouseAddress
import com.cuso.mobile.model.inventory.WarehouseDropdownItem
import com.cuso.mobile.model.inventory.WarehouseItem
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
// UI STATE & ENUMS
// =============================================================================

sealed class CreateItemUiState {
    data object Idle : CreateItemUiState()
    data object Loading : CreateItemUiState()
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

data class WarehouseUiState(
    val isLoading: Boolean = false,
    val warehouses: List<WarehouseItem> = emptyList(),
    val filteredWarehouses: List<WarehouseItem> = emptyList(),
    val searchQuery: String = "",
    val errorMessage: String? = null
)

data class WarehouseFormState(
    val id: String? = null,
    val name: String = "",
    val code: String = "",
    val type: String = "main",
    val description: String = "",
    val contactPerson: String = "",
    val contactPhone: String = "",
    val isDefault: Boolean = false,
    val status: String = "active",
    val branchId: String? = null,
    val address: String = "",
    val city: String = "",
    val state: String = "",
    val country: String = "India",
    val pincode: String = "",
    val totalFloorAreaSqft: String = "0",
    val defaultTemperatureZone: String = "normal"
)

// =============================================================================
// VIEW MODEL
// =============================================================================

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
) : ViewModel() {

    private val gson = Gson()

    // ── 1. Item Groups State ──
    private val _uiState = MutableStateFlow(ItemGroupUiState())
    val uiState: StateFlow<ItemGroupUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    private val _isCreatingItemGroup = MutableStateFlow(false)
    val isCreatingItemGroup: StateFlow<Boolean> = _isCreatingItemGroup.asStateFlow()

    private val _createItemGroupError = MutableStateFlow<String?>(null)
    val createItemGroupError: StateFlow<String?> = _createItemGroupError.asStateFlow()

    private val _createItemGroupSuccess = MutableStateFlow<String?>(null)
    val createItemGroupSuccess: StateFlow<String?> = _createItemGroupSuccess.asStateFlow()

    private val _selectedItemGroupDetail = MutableStateFlow<ItemGroupViewOneData?>(null)
    val selectedItemGroupDetail: StateFlow<ItemGroupViewOneData?> = _selectedItemGroupDetail.asStateFlow()

    private val _isLoadingItemGroupDetail = MutableStateFlow(false)
    val isLoadingItemGroupDetail: StateFlow<Boolean> = _isLoadingItemGroupDetail.asStateFlow()

    private val _deleteItemGroupSuccess = MutableStateFlow<String?>(null)
    val deleteItemGroupSuccess: StateFlow<String?> = _deleteItemGroupSuccess.asStateFlow()

    // ── 2. Inventory Items: List & Pagination State ──
    private val _inventoryItems = MutableStateFlow<List<InventoryItem>>(emptyList())
    val inventoryItems: StateFlow<List<InventoryItem>> = _inventoryItems.asStateFlow()

    private val _isDeletingItem = MutableStateFlow(false)
    val isDeletingItem: StateFlow<Boolean> = _isDeletingItem.asStateFlow()

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

    // ── 3. Inventory Item: View One State ──
    private val _viewOneItem = MutableStateFlow<InventoryItemviewone?>(null)
    val viewOneItem: StateFlow<InventoryItemviewone?> = _viewOneItem.asStateFlow()

    private val _isLoadingViewOne = MutableStateFlow(false)
    val isLoadingViewOne: StateFlow<Boolean> = _isLoadingViewOne.asStateFlow()

    private val _viewOneError = MutableStateFlow<String?>(null)
    val viewOneError: StateFlow<String?> = _viewOneError.asStateFlow()

    private val _showViewOneSheet = MutableStateFlow(false)
    val showViewOneSheet: StateFlow<Boolean> = _showViewOneSheet.asStateFlow()

    // ── 4. Inventory Item: Detail State ──
    private val _selectedItem = MutableStateFlow<InventoryItem?>(null)
    val selectedItem: StateFlow<InventoryItem?> = _selectedItem.asStateFlow()

    private val _isLoadingItemDetail = MutableStateFlow(false)
    val isLoadingItemDetail: StateFlow<Boolean> = _isLoadingItemDetail.asStateFlow()

    private val _itemDetailError = MutableStateFlow<String?>(null)
    val itemDetailError: StateFlow<String?> = _itemDetailError.asStateFlow()

    private val _showItemDetailSheet = MutableStateFlow(false)
    val showItemDetailSheet: StateFlow<Boolean> = _showItemDetailSheet.asStateFlow()

    // ── 5. Recent Items State ──
    private val _recentItems = MutableStateFlow<List<InventoryItem>>(emptyList())
    val recentItems: StateFlow<List<InventoryItem>> = _recentItems.asStateFlow()

    private val _isLoadingRecentItems = MutableStateFlow(false)
    val isLoadingRecentItems: StateFlow<Boolean> = _isLoadingRecentItems.asStateFlow()

    private val _recentItemsError = MutableStateFlow<String?>(null)
    val recentItemsError: StateFlow<String?> = _recentItemsError.asStateFlow()

    // ── 6. Stock Adjustment State ──
    private val _isAdjustingStock = MutableStateFlow(false)
    val isAdjustingStock: StateFlow<Boolean> = _isAdjustingStock.asStateFlow()

    private val _adjustStockError = MutableStateFlow<String?>(null)
    val adjustStockError: StateFlow<String?> = _adjustStockError.asStateFlow()

    private val _adjustStockSuccess = MutableStateFlow(false)
    val adjustStockSuccess: StateFlow<Boolean> = _adjustStockSuccess.asStateFlow()

    private val _stockSummaryList = MutableStateFlow<List<StockSummaryItemDto>>(emptyList())
    val stockSummaryList: StateFlow<List<StockSummaryItemDto>> = _stockSummaryList.asStateFlow()

    private val _isLoadingStockSummary = MutableStateFlow(false)
    val isLoadingStockSummary: StateFlow<Boolean> = _isLoadingStockSummary.asStateFlow()

    private val _stockSummaryError = MutableStateFlow<String?>(null)
    val stockSummaryError: StateFlow<String?> = _stockSummaryError.asStateFlow()

    // ── 7. Low Stock Alerts & Purchase Orders ──
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

    // ── 8. Create Item Form State ──
    private val _expandedSection = MutableStateFlow(ItemSection.ITEM_IDENTITY)
    val expandedSection: StateFlow<ItemSection> = _expandedSection.asStateFlow()

    private val _createItemForm = MutableStateFlow(CreateItemFormState())
    val createItemForm: StateFlow<CreateItemFormState> = _createItemForm.asStateFlow()

    private val _createItemUiState = MutableStateFlow<CreateItemUiState>(CreateItemUiState.Idle)
    val createItemUiState: StateFlow<CreateItemUiState> = _createItemUiState.asStateFlow()

    // ── 9. Stock Adjustments & History State ──
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

    private val _currentAdjustmentPage = MutableStateFlow(1)
    val currentAdjustmentPage: StateFlow<Int> = _currentAdjustmentPage.asStateFlow()

    private val _totalAdjustments = MutableStateFlow(0)
    val totalAdjustments: StateFlow<Int> = _totalAdjustments.asStateFlow()

    private val _canLoadMoreAdjustments = MutableStateFlow(true)
    val canLoadMoreAdjustments: StateFlow<Boolean> = _canLoadMoreAdjustments.asStateFlow()

    // ── 10. Warehouse Management State ──
    private val _warehouseUiState = MutableStateFlow(WarehouseUiState())
    val warehouseUiState: StateFlow<WarehouseUiState> = _warehouseUiState.asStateFlow()

    private var warehouseSearchJob: Job? = null

    private val _warehouseDropdown = MutableStateFlow<List<WarehouseDropdownItem>>(emptyList())
    val warehouseDropdown: StateFlow<List<WarehouseDropdownItem>> = _warehouseDropdown.asStateFlow()

    private val _selectedWarehouse = MutableStateFlow<WarehouseItem?>(null)
    val selectedWarehouse: StateFlow<WarehouseItem?> = _selectedWarehouse.asStateFlow()

    private val _isLoadingWarehouseDetail = MutableStateFlow(false)
    val isLoadingWarehouseDetail: StateFlow<Boolean> = _isLoadingWarehouseDetail.asStateFlow()

    private val _warehouseDetailError = MutableStateFlow<String?>(null)
    val warehouseDetailError: StateFlow<String?> = _warehouseDetailError.asStateFlow()

    private val _warehouseForm = MutableStateFlow(WarehouseFormState())
    val warehouseForm: StateFlow<WarehouseFormState> = _warehouseForm.asStateFlow()

    private val _isSubmittingWarehouse = MutableStateFlow(false)
    val isSubmittingWarehouse: StateFlow<Boolean> = _isSubmittingWarehouse.asStateFlow()

    private val _warehouseActionSuccessMessage = MutableStateFlow<String?>(null)
    val warehouseActionSuccessMessage: StateFlow<String?> = _warehouseActionSuccessMessage.asStateFlow()

    private val _warehouseActionErrorMessage = MutableStateFlow<String?>(null)
    val warehouseActionErrorMessage: StateFlow<String?> = _warehouseActionErrorMessage.asStateFlow()

    // ── 11. Supplier State ──
    private val _suppliers = MutableStateFlow<List<SupplierDto>>(emptyList())
    val suppliers: StateFlow<List<SupplierDto>> = _suppliers.asStateFlow()

    private val _isLoadingSuppliers = MutableStateFlow(false)
    val isLoadingSuppliers: StateFlow<Boolean> = _isLoadingSuppliers.asStateFlow()

    private val _suppliersError = MutableStateFlow<String?>(null)
    val suppliersError: StateFlow<String?> = _suppliersError.asStateFlow()

    private val _supplierDropdown = MutableStateFlow<List<SupplierDropdownItem>>(emptyList())
    val supplierDropdown: StateFlow<List<SupplierDropdownItem>> = _supplierDropdown.asStateFlow()

    private val _selectedSupplier = MutableStateFlow<SupplierDto?>(null)
    val selectedSupplier: StateFlow<SupplierDto?> = _selectedSupplier.asStateFlow()

    private val _isLoadingDetail = MutableStateFlow(false)
    val isLoadingDetail: StateFlow<Boolean> = _isLoadingDetail.asStateFlow()

    private val _supplierLedger = MutableStateFlow<SupplierLedgerContainer?>(null)
    val supplierLedger: StateFlow<SupplierLedgerContainer?> = _supplierLedger.asStateFlow()

    private val _isLoadingLedger = MutableStateFlow(false)
    val isLoadingLedger: StateFlow<Boolean> = _isLoadingLedger.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _selectedAdjustmentType = MutableStateFlow<String?>("transfer") // default to "transfer"
    val selectedAdjustmentType: StateFlow<String?> = _selectedAdjustmentType.asStateFlow()

    //BULK ITEM
    private val _bulkItems = MutableStateFlow<List<BulkItemDoc>>(emptyList())
    val bulkItems: StateFlow<List<BulkItemDoc>> = _bulkItems.asStateFlow()

    private val _selectedBulkItem = MutableStateFlow<BulkItemDoc?>(null)
    val selectedBulkItem: StateFlow<BulkItemDoc?> = _selectedBulkItem.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    // =========================================================================
    // PURCHASE ORDERS STATE & ACTIONS
    // =========================================================================

    // 1. PO List State
    private val _purchaseOrdersList = MutableStateFlow<List<PurchaseOrder>>(emptyList())
    val purchaseOrdersList: StateFlow<List<PurchaseOrder>> = _purchaseOrdersList.asStateFlow()

    private val _isLoadingPurchaseOrders = MutableStateFlow(false)
    val isLoadingPurchaseOrders: StateFlow<Boolean> = _isLoadingPurchaseOrders.asStateFlow()

    private val _purchaseOrdersError = MutableStateFlow<String?>(null)
    val purchaseOrdersError: StateFlow<String?> = _purchaseOrdersError.asStateFlow()

    // 2. Direct PO Operation State (Create/Update/Receive)
    private val _isSubmittingPO = MutableStateFlow(false)
    val isSubmittingPO: StateFlow<Boolean> = _isSubmittingPO.asStateFlow()

    private val _poSuccessMessage = MutableStateFlow<String?>(null)
    val poSuccessMessage: StateFlow<String?> = _poSuccessMessage.asStateFlow()

    // 3. Bill Convert State
    private val _billConvertDetail = MutableStateFlow<POBillConvertData?>(null)
    val billConvertDetail: StateFlow<POBillConvertData?> = _billConvertDetail.asStateFlow()

    private val _isLoadingBillConvert = MutableStateFlow(false)
    val isLoadingBillConvert: StateFlow<Boolean> = _isLoadingBillConvert.asStateFlow()


    private val _requisitionsList = MutableStateFlow<List<PurchaseRequisition>>(emptyList())
    val requisitionsList: StateFlow<List<PurchaseRequisition>> = _requisitionsList.asStateFlow()

    private val _selectedRequisition = MutableStateFlow<PurchaseRequisition?>(null)
    val selectedRequisition: StateFlow<PurchaseRequisition?> = _selectedRequisition.asStateFlow()

    private val _isLoadingRequisitions = MutableStateFlow(false)
    val isLoadingRequisitions: StateFlow<Boolean> = _isLoadingRequisitions.asStateFlow()

    private val _isSubmittingRequisition = MutableStateFlow(false)
    val isSubmittingRequisition: StateFlow<Boolean> = _isSubmittingRequisition.asStateFlow()

    private val _requisitionSuccessMessage = MutableStateFlow<String?>(null)
    val requisitionSuccessMessage: StateFlow<String?> = _requisitionSuccessMessage.asStateFlow()

    private val _requisitionErrorMessage = MutableStateFlow<String?>(null)
    val requisitionErrorMessage: StateFlow<String?> = _requisitionErrorMessage.asStateFlow()

    // =============================================================================
    // BARCODE STATE FLOWS (Add inside InventoryViewModel)
    // =============================================================================

    private val _barcodesList = MutableStateFlow<List<BarcodeItemDoc>>(emptyList())
    val barcodesList: StateFlow<List<BarcodeItemDoc>> = _barcodesList.asStateFlow()

    private val _selectedBarcode = MutableStateFlow<BarcodeItemDoc?>(null)
    val selectedBarcode: StateFlow<BarcodeItemDoc?> = _selectedBarcode.asStateFlow()

    private val _isLoadingBarcodes = MutableStateFlow(false)
    val isLoadingBarcodes: StateFlow<Boolean> = _isLoadingBarcodes.asStateFlow()

    private val _isSubmittingBarcode = MutableStateFlow(false)
    val isSubmittingBarcode: StateFlow<Boolean> = _isSubmittingBarcode.asStateFlow()

    private val _barcodeSuccessMessage = MutableStateFlow<String?>(null)
    val barcodeSuccessMessage: StateFlow<String?> = _barcodeSuccessMessage.asStateFlow()

    private val _barcodeErrorMessage = MutableStateFlow<String?>(null)
    val barcodeErrorMessage: StateFlow<String?> = _barcodeErrorMessage.asStateFlow()
    // =========================================================================
    // purchase receive
    // =========================================================================

    // ── All Receives List ──
    private val _allReceives = MutableStateFlow<List<PurchaseReceiveItem>>(emptyList())
    val allReceives: StateFlow<List<PurchaseReceiveItem>> = _allReceives.asStateFlow()

    // ── PO Receive History & Items Overview ──
    private val _poHistory = MutableStateFlow<ReceiveHistoryByPoResponse?>(null)
    val poHistory: StateFlow<ReceiveHistoryByPoResponse?> = _poHistory.asStateFlow()

    // ── Single Receive Detail ──
    private val _singleReceive = MutableStateFlow<PurchaseReceiveItem?>(null)
    val singleReceive: StateFlow<PurchaseReceiveItem?> = _singleReceive.asStateFlow()

    // ── Active Bill Generated from Conversion ──
    private val _generatedBill = MutableStateFlow<BillCreatedData?>(null)
    val generatedBill: StateFlow<BillCreatedData?> = _generatedBill.asStateFlow()

    // =========================================================================
    // INITIALIZATION
    // =========================================================================
    init {
        loadItemGroups()
        loadWarehouses()
        loadWarehouseDropdown()
        fetchSuppliers()
        fetchSupplierDropdown()
        fetchValidAdjustmentReasons()
    }

    // =========================================================================
    // ITEM GROUP ACTIONS
    // =========================================================================

    fun loadItemGroups(query: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = inventoryRepository.getInventoryItemGroup(search = query)
            result.onSuccess { response ->
                val safeList = response.groups
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        itemGroups = safeList,
                        filteredList = safeList,
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

    fun createItemGroup(
        context: Context,
        request: CreateItemGroupRequest,
        imagesUris: List<Uri>,
        onSuccessCallback: () -> Unit
    ) {
        viewModelScope.launch {
            _isCreatingItemGroup.value = true
            _createItemGroupError.value = null
            _createItemGroupSuccess.value = null

            val textMedia = "text/plain".toMediaTypeOrNull()
            val params = mutableMapOf<String, RequestBody>()

            params["name"] = request.name.toRequestBody(textMedia)
            request.unit?.let { params["unit"] = it.toRequestBody(textMedia) }
            request.status?.let { params["status"] = it.toRequestBody(textMedia) }

            // ── Pricing Fields (Compatible with both JSON body & Multer flat/bracket forms) ──
            request.pricing?.let { pricing ->
                params["costPrice"] = pricing.costPrice.toString().toRequestBody(textMedia)
                params["sellingPrice"] = pricing.sellingPrice.toString().toRequestBody(textMedia)
                params["pricing[costPrice]"] = pricing.costPrice.toString().toRequestBody(textMedia)
                params["pricing[sellingPrice]"] = pricing.sellingPrice.toString().toRequestBody(textMedia)
                pricing.currency?.let {
                    params["currency"] = it.toRequestBody(textMedia)
                    params["pricing[currency]"] = it.toRequestBody(textMedia)
                }
            }

            // ── Variant Attributes (Sends both bracket notation and JSON fallback) ──
            request.variantAttributes.forEachIndexed { index, attr ->
                params["variantAttributes[$index][name]"] = attr.name.toRequestBody(textMedia)
                attr.values.forEachIndexed { valIndex, value ->
                    params["variantAttributes[$index][values][$valIndex]"] = value.toRequestBody(textMedia)
                }
            }

            request.categoryId?.takeIf { it.isNotBlank() }?.let {
                params["categoryId"] = it.toRequestBody(textMedia)
            }
            request.brand?.takeIf { it.isNotBlank() }?.let {
                params["brand"] = it.toRequestBody(textMedia)
            }
            request.shortDescription?.takeIf { it.isNotBlank() }?.let {
                params["shortDescription"] = it.toRequestBody(textMedia)
            }
            request.longDescription?.takeIf { it.isNotBlank() }?.let {
                params["longDescription"] = it.toRequestBody(textMedia)
            }

            // Image part prepared with fieldName = "image"
            val imageParts = inventoryRepository.prepareMultipleImagesPart(context, imagesUris, fieldName = "image")
            val result = inventoryRepository.createItemGroup(params, imageParts.takeIf { it.isNotEmpty() })
            _isCreatingItemGroup.value = false

            result.onSuccess {
                _createItemGroupSuccess.value = "Item Group created successfully!"
                loadItemGroups()
                onSuccessCallback()
            }.onFailure { error ->
                _createItemGroupError.value = extractErrorMessage(error.message)
            }
        }
    }

    fun updateItemGroup(
        context: Context,
        id: String,
        request: CreateItemGroupRequest,
        imagesUris: List<Uri>,
        onSuccessCallback: () -> Unit
    ) {
        viewModelScope.launch {
            _isCreatingItemGroup.value = true
            _createItemGroupError.value = null
            _createItemGroupSuccess.value = null

            val textMedia = "text/plain".toMediaTypeOrNull()
            val params = mutableMapOf<String, RequestBody>()

            params["name"] = request.name.toRequestBody(textMedia)
            request.unit?.let { params["unit"] = it.toRequestBody(textMedia) }
            request.status?.let { params["status"] = it.toRequestBody(textMedia) }

            // ── Pricing Fields (Compatible with both JSON body & Multer flat/bracket forms) ──
            request.pricing?.let { pricing ->
                params["costPrice"] = pricing.costPrice.toString().toRequestBody(textMedia)
                params["sellingPrice"] = pricing.sellingPrice.toString().toRequestBody(textMedia)
                params["pricing[costPrice]"] = pricing.costPrice.toString().toRequestBody(textMedia)
                params["pricing[sellingPrice]"] = pricing.sellingPrice.toString().toRequestBody(textMedia)
                pricing.currency?.let {
                    params["currency"] = it.toRequestBody(textMedia)
                    params["pricing[currency]"] = it.toRequestBody(textMedia)
                }
            }

            // ── Variant Attributes (Sends both bracket notation and JSON fallback) ──
            request.variantAttributes.forEachIndexed { index, attr ->
                params["variantAttributes[$index][name]"] = attr.name.toRequestBody(textMedia)
                attr.values.forEachIndexed { valIndex, value ->
                    params["variantAttributes[$index][values][$valIndex]"] = value.toRequestBody(textMedia)
                }
            }

            request.categoryId?.takeIf { it.isNotBlank() }?.let {
                params["categoryId"] = it.toRequestBody(textMedia)
            }
            request.brand?.takeIf { it.isNotBlank() }?.let {
                params["brand"] = it.toRequestBody(textMedia)
            }
            request.shortDescription?.takeIf { it.isNotBlank() }?.let {
                params["shortDescription"] = it.toRequestBody(textMedia)
            }
            request.longDescription?.takeIf { it.isNotBlank() }?.let {
                params["longDescription"] = it.toRequestBody(textMedia)
            }

            // Image part prepared with fieldName = "image"
            val imageParts = inventoryRepository.prepareMultipleImagesPart(context, imagesUris, fieldName = "image")
            val result = inventoryRepository.updateItemGroup(id, params, imageParts.takeIf { it.isNotEmpty() })
            _isCreatingItemGroup.value = false

            result.onSuccess {
                _createItemGroupSuccess.value = "Item Group updated successfully!"
                loadItemGroups()
                onSuccessCallback()
            }.onFailure { error ->
                _createItemGroupError.value = extractErrorMessage(error.message)
            }
        }
    }

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
    // INVENTORY VIEW ONE & ITEM DETAILS
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
    // STOCK ADJUSTMENT & TRANSFER ACTIONS
    // =========================================================================

    fun fetchValidAdjustmentReasons() {
        viewModelScope.launch {
            inventoryRepository.getValidAdjustmentReasons().onSuccess { reasons ->
                _validAdjustmentReasons.value = reasons
            }
        }
    }

    fun fetchStockAdjustments(
        reset: Boolean = true,
        adjustmentType: String? = "transfer",
        search: String? = null
    ) {
        if (_isLoadingAdjustments.value) return

        viewModelScope.launch {
            _isLoadingAdjustments.value = true
            _adjustmentErrorMessage.value = null
            _selectedAdjustmentType.value = adjustmentType

            if (reset) {
                _currentAdjustmentPage.value = 1
                _canLoadMoreAdjustments.value = true
            }

            inventoryRepository.getStockAdjustments(
                page = _currentAdjustmentPage.value,
                limit = 10,
                adjustmentType = adjustmentType,
                search = search
            ).fold(
                onSuccess = { response ->
                    _stockAdjustmentsList.value = if (reset) response.data else _stockAdjustmentsList.value + response.data
                    _totalAdjustments.value = response.totalCount
                    _canLoadMoreAdjustments.value = _currentAdjustmentPage.value < response.totalPagesCount

                    if (_canLoadMoreAdjustments.value) {
                        _currentAdjustmentPage.value += 1
                    }
                },
                onFailure = { error ->
                    _adjustmentErrorMessage.value = extractErrorMessage(error.message)
                }
            )

            _isLoadingAdjustments.value = false
        }
    }

    fun loadMoreStockAdjustments(search: String? = null) {
        if (_canLoadMoreAdjustments.value && !_isLoadingAdjustments.value) {
            fetchStockAdjustments(
                reset = false,
                adjustmentType = _selectedAdjustmentType.value,
                search = search
            )
        }
    }

    fun submitIncreaseStock(
        request: IncreaseStockRequest,
        onSuccess: (StockAdjustmentData) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isSubmittingAdjustment.value = true
            _adjustmentErrorMessage.value = null
            _adjustmentSuccessMessage.value = null

            val result = inventoryRepository.increaseStock(request)
            _isSubmittingAdjustment.value = false

            result.onSuccess { data ->
                _adjustmentSuccessMessage.value = "Stock increased successfully (+${data.quantity} ${data.unit ?: ""})"
                fetchInventoryItems()
                fetchStockAdjustments(reset = true)
                onSuccess(data)
            }.onFailure { error ->
                _adjustmentErrorMessage.value = extractErrorMessage(error.message)
            }
        }
    }

    fun submitDecreaseStock(
        request: DecreaseStockRequest,
        onSuccess: (StockAdjustmentData) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isSubmittingAdjustment.value = true
            _adjustmentErrorMessage.value = null
            _adjustmentSuccessMessage.value = null

            val result = inventoryRepository.decreaseStock(request)
            _isSubmittingAdjustment.value = false

            result.onSuccess { data ->
                _adjustmentSuccessMessage.value = "Stock decreased successfully (-${data.quantity} ${data.unit ?: ""})"
                fetchInventoryItems()
                fetchStockAdjustments(reset = true)
                onSuccess(data)
            }.onFailure { error ->
                _adjustmentErrorMessage.value = extractErrorMessage(error.message)
            }
        }
    }

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
                fetchInventoryItems()
                fetchStockAdjustments(reset = true)
                onSuccess(data)
            }.onFailure { error ->
                _adjustmentErrorMessage.value = extractErrorMessage(error.message)
            }
        }
    }

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
                fetchStockSummaryList()
                refreshInventoryItems()
                fetchStockAdjustments(reset = true)
                onSuccess()
            }.onFailure { error ->
                _adjustmentErrorMessage.value = extractErrorMessage(error.message)
            }
        }
    }

    fun fetchStockSummaryList(page: Int = 1, limit: Int = 20, search: String? = null) {
        viewModelScope.launch {
            _isLoadingStockSummary.value = true
            _stockSummaryError.value = null

            val result = inventoryRepository.getStockSummaryList(page, limit, search)
            _isLoadingStockSummary.value = false

            result.onSuccess { response ->
                _stockSummaryList.value = response.data
            }.onFailure { error ->
                _stockSummaryError.value = extractErrorMessage(error.message)
            }
        }
    }

    fun clearStockSummaryAlerts() {
        _stockSummaryError.value = null
    }

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

    fun onItemNameChanged(newName: String) {
        updateCreateItemForm { current ->
            val newSku = if (current.autoGenerateSku) generateSkuFromName(newName) else current.sku
            current.copy(name = newName, sku = newSku)
        }
    }

    fun onAutoGenerateSkuToggle(enabled: Boolean) {
        updateCreateItemForm { current ->
            val newSku = if (enabled) generateSkuFromName(current.name) else current.sku
            current.copy(autoGenerateSku = enabled, sku = newSku)
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
                    length = form.length.toDoubleOrNull() ?: 0.0,
                    width = form.width.toDoubleOrNull() ?: 0.0,
                    height = form.height.toDoubleOrNull() ?: 0.0,
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
                "unit" to form.unit.toRequestBody(textMedia),
                "status" to form.status.toRequestBody(textMedia),
                "brand" to form.brand.toRequestBody(textMedia),
                "manufacturer" to form.manufacturer.toRequestBody(textMedia),
                "hsnCode" to form.hsnCode.toRequestBody(textMedia),
                "returnable" to form.returnable.toString().toRequestBody(textMedia),
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

            // Image part prepared with valid extension (.jpg, .png, etc.)
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

    fun deleteInventoryItem(itemId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isDeletingItem.value = true
            val result = inventoryRepository.deleteInventoryItem(itemId)
            _isDeletingItem.value = false

            result.onSuccess {
                // Remove item locally or trigger re-fetch
                fetchInventoryItems()
                onSuccess()
            }.onFailure { error ->
                _inventoryError.value = error.message
            }
        }
    }

    // =========================================================================
    // WAREHOUSE MANAGEMENT ACTIONS
    // =========================================================================

    fun loadWarehouses() {
        launchBusy {
            _warehouseUiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = inventoryRepository.getAllWarehouses()

            result.onSuccess { list ->
                _warehouseUiState.update {
                    it.copy(
                        isLoading = false,
                        warehouses = list,
                        filteredWarehouses = if (it.searchQuery.isBlank()) list else filterWarehouses(list, it.searchQuery),
                        errorMessage = null
                    )
                }
            }.onFailure { error ->
                _warehouseUiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = extractErrorMessage(error.message)
                    )
                }
            }
        }
    }

    fun loadWarehouseDropdown() {
        launchBusy {
            inventoryRepository.getWarehouseDropdown().onSuccess { dropdownItems ->
                _warehouseDropdown.value = dropdownItems
            }
        }
    }

    fun onWarehouseSearchQueryChanged(newQuery: String) {
        _warehouseUiState.update { state ->
            val filtered = filterWarehouses(state.warehouses, newQuery)
            state.copy(searchQuery = newQuery, filteredWarehouses = filtered)
        }

        warehouseSearchJob?.cancel()
        warehouseSearchJob = viewModelScope.launch {
            delay(350)
        }
    }

    private fun filterWarehouses(list: List<WarehouseItem>, query: String): List<WarehouseItem> {
        if (query.isBlank()) return list
        return list.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.code.contains(query, ignoreCase = true) ||
                    it.contactPerson.contains(query, ignoreCase = true) ||
                    it.address.city.contains(query, ignoreCase = true)
        }
    }

    fun fetchWarehouseById(id: String, onLoaded: (WarehouseItem) -> Unit = {}) {
        launchBusy {
            _isLoadingWarehouseDetail.value = true
            _warehouseDetailError.value = null

            val result = inventoryRepository.getWarehouseById(id)
            _isLoadingWarehouseDetail.value = false

            result.onSuccess { item ->
                _selectedWarehouse.value = item
                onLoaded(item)
            }.onFailure { error ->
                _warehouseDetailError.value = extractErrorMessage(error.message)
            }
        }
    }

    fun clearSelectedWarehouse() {
        _selectedWarehouse.value = null
        _warehouseDetailError.value = null
    }

    fun updateWarehouseForm(transform: (WarehouseFormState) -> WarehouseFormState) {
        _warehouseForm.update(transform)
    }

    fun resetWarehouseForm() {
        _warehouseForm.value = WarehouseFormState()
        _warehouseActionErrorMessage.value = null
        _warehouseActionSuccessMessage.value = null
    }

    fun populateWarehouseFormForEdit(warehouse: WarehouseItem) {
        _warehouseForm.value = WarehouseFormState(
            id = warehouse.id,
            name = warehouse.name,
            code = warehouse.code,
            type = warehouse.type,
            description = warehouse.description ?: "",
            contactPerson = warehouse.contactPerson,
            contactPhone = warehouse.contactPhone,
            isDefault = warehouse.isDefault,
            status = warehouse.status,
            branchId = warehouse.branchId,
            address = warehouse.address.address,
            city = warehouse.address.city,
            state = warehouse.address.state,
            country = warehouse.address.country,
            pincode = warehouse.address.pincode,
            totalFloorAreaSqft = warehouse.capacitySummary.totalFloorAreaSqft.toString(),
            defaultTemperatureZone = warehouse.capacitySummary.defaultTemperatureZone
        )
    }

    fun createWarehouse(onSuccess: () -> Unit = {}) {
        val form = _warehouseForm.value
        launchBusy {
            _isSubmittingWarehouse.value = true
            _warehouseActionErrorMessage.value = null
            _warehouseActionSuccessMessage.value = null

            val request = CreateWarehouseRequest(
                branchId = form.branchId?.takeIf { it.isNotBlank() },
                name = form.name,
                code = form.code,
                type = form.type,
                description = form.description.takeIf { it.isNotBlank() },
                contactPerson = form.contactPerson,
                contactPhone = form.contactPhone,
                isDefault = form.isDefault,
                address = WarehouseAddress(
                    address = form.address,
                    city = form.city,
                    state = form.state,
                    country = form.country,
                    pincode = form.pincode
                ),
                capacitySummary = CapacitySummary(
                    totalFloorAreaSqft = form.totalFloorAreaSqft.toDoubleOrNull() ?: 0.0,
                    defaultTemperatureZone = form.defaultTemperatureZone
                )
            )

            val result = inventoryRepository.createWarehouse(request)
            _isSubmittingWarehouse.value = false

            result.onSuccess {
                _warehouseActionSuccessMessage.value = "Warehouse created successfully"
                loadWarehouses()
                loadWarehouseDropdown()
                resetWarehouseForm()
                onSuccess()
            }.onFailure { error ->
                _warehouseActionErrorMessage.value = extractErrorMessage(error.message)
            }
        }
    }

    fun updateWarehouse(onSuccess: () -> Unit = {}) {
        val form = _warehouseForm.value
        val id = form.id ?: return

        launchBusy {
            _isSubmittingWarehouse.value = true
            _warehouseActionErrorMessage.value = null
            _warehouseActionSuccessMessage.value = null

            val request = UpdateWarehouseRequest(
                branchId = form.branchId?.takeIf { it.isNotBlank() },
                name = form.name,
                code = form.code,
                type = form.type,
                description = form.description.takeIf { it.isNotBlank() },
                contactPerson = form.contactPerson,
                contactPhone = form.contactPhone,
                isDefault = form.isDefault,
                status = form.status,
                address = WarehouseAddress(
                    address = form.address,
                    city = form.city,
                    state = form.state,
                    country = form.country,
                    pincode = form.pincode
                ),
                capacitySummary = CapacitySummary(
                    totalFloorAreaSqft = form.totalFloorAreaSqft.toDoubleOrNull() ?: 0.0,
                    defaultTemperatureZone = form.defaultTemperatureZone
                )
            )

            val result = inventoryRepository.updateWarehouse(id, request)
            _isSubmittingWarehouse.value = false

            result.onSuccess {
                _warehouseActionSuccessMessage.value = "Warehouse updated successfully"
                loadWarehouses()
                loadWarehouseDropdown()
                onSuccess()
            }.onFailure { error ->
                _warehouseActionErrorMessage.value = extractErrorMessage(error.message)
            }
        }
    }

    fun deleteWarehouse(id: String, onSuccess: () -> Unit = {}) {
        launchBusy {
            _isSubmittingWarehouse.value = true
            _warehouseActionErrorMessage.value = null

            val result = inventoryRepository.deleteWarehouse(id)
            _isSubmittingWarehouse.value = false

            result.onSuccess { message ->
                _warehouseActionSuccessMessage.value = message
                loadWarehouses()
                loadWarehouseDropdown()
                onSuccess()
            }.onFailure { error ->
                _warehouseActionErrorMessage.value = extractErrorMessage(error.message)
            }
        }
    }

    fun restoreWarehouse(id: String, onSuccess: () -> Unit = {}) {
        launchBusy {
            _isSubmittingWarehouse.value = true
            _warehouseActionErrorMessage.value = null

            val result = inventoryRepository.restoreWarehouse(id)
            _isSubmittingWarehouse.value = false

            result.onSuccess {
                _warehouseActionSuccessMessage.value = "Warehouse restored successfully"
                loadWarehouses()
                loadWarehouseDropdown()
                onSuccess()
            }.onFailure { error ->
                _warehouseActionErrorMessage.value = extractErrorMessage(error.message)
            }
        }
    }

    fun clearWarehouseActionAlerts() {
        _warehouseActionSuccessMessage.value = null
        _warehouseActionErrorMessage.value = null
    }

    // =========================================================================
    // SUPPLIERS ACTIONS
    // =========================================================================

    fun fetchSuppliers(page: Int = 1, limit: Int = 50, search: String? = null) {
        viewModelScope.launch {
            _isLoadingSuppliers.value = true
            _suppliersError.value = null
            inventoryRepository.getAllSuppliers(page, limit, search)
                .onSuccess { list -> _suppliers.value = list }
                .onFailure { error -> _suppliersError.value = extractErrorMessage(error.message) }
            _isLoadingSuppliers.value = false
        }
    }

    fun fetchSupplierDropdown() {
        viewModelScope.launch {
            inventoryRepository.getSupplierDropdown().onSuccess { items ->
                _supplierDropdown.value = items
            }
        }
    }

    fun fetchSupplierDetail(id: String) {
        viewModelScope.launch {
            _isLoadingDetail.value = true
            _errorMessage.value = null
            inventoryRepository.getSupplierById(id)
                .onSuccess { supplier -> _selectedSupplier.value = supplier }
                .onFailure { error -> _errorMessage.value = extractErrorMessage(error.message) }
            _isLoadingDetail.value = false
        }
    }

    fun fetchSupplierLedger(id: String) {
        viewModelScope.launch {
            _isLoadingLedger.value = true
            inventoryRepository.getSupplierLedger(id)
                .onSuccess { ledgerData -> _supplierLedger.value = ledgerData }
                .onFailure { error -> _errorMessage.value = extractErrorMessage(error.message) }
            _isLoadingLedger.value = false
        }
    }

    fun createSupplier(request: CreateSupplierRequest, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isSubmitting.value = true
            _errorMessage.value = null
            inventoryRepository.createSupplier(request)
                .onSuccess {
                    _successMessage.value = "Supplier created successfully"
                    fetchSuppliers()
                    fetchSupplierDropdown()
                    onSuccess()
                }
                .onFailure { error -> _errorMessage.value = extractErrorMessage(error.message) }
            _isSubmitting.value = false
        }
    }

    fun updateSupplier(id: String, request: CreateSupplierRequest, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isSubmitting.value = true
            _errorMessage.value = null
            inventoryRepository.updateSupplier(id, request)
                .onSuccess {
                    _successMessage.value = "Supplier updated successfully"
                    fetchSuppliers()
                    fetchSupplierDetail(id)
                    onSuccess()
                }
                .onFailure { error -> _errorMessage.value = extractErrorMessage(error.message) }
            _isSubmitting.value = false
        }
    }

    fun deleteSupplier(id: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isSubmitting.value = true
            _errorMessage.value = null
            inventoryRepository.deleteSupplier(id)
                .onSuccess { msg ->
                    _successMessage.value = msg
                    fetchSuppliers()
                    onSuccess()
                }
                .onFailure { error -> _errorMessage.value = extractErrorMessage(error.message) }
            _isSubmitting.value = false
        }
    }

    fun restoreSupplier(id: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isSubmitting.value = true
            _errorMessage.value = null
            inventoryRepository.restoreSupplier(id)
                .onSuccess {
                    _successMessage.value = "Supplier restored successfully"
                    fetchSuppliers()
                    onSuccess()
                }
                .onFailure { error -> _errorMessage.value = extractErrorMessage(error.message) }
            _isSubmitting.value = false
        }
    }

    fun clearAlerts() {
        _successMessage.value = null
        _errorMessage.value = null
        _suppliersError.value = null
    }

    fun clearSelectedSupplier() {
        _selectedSupplier.value = null
        _supplierLedger.value = null
    }

    // =========================================================================
    // PRIVATE UTILITY FUNCTIONS
    // =========================================================================

    fun generateSkuFromName(name: String): String {
        if (name.isBlank()) return ""
        // Clean string: splits by spaces, hyphens, slashes and commas
        val tokens = name.split(Regex("[\\s\\-/]+")).filter { it.isNotBlank() }

        return when {
            tokens.isEmpty() -> ""
            // Single word: Take up to first 3-4 chars in uppercase (e.g. "Rajasthani" -> "RAJ")
            tokens.size == 1 -> tokens[0].take(3).uppercase()
            // Multiple words: First word 3-char prefix + subsequent words/variants (e.g., "Rajasthani Silk Shirt - Gray / XL" -> "RAJ-GRAY-XL")
            else -> {
                val prefix = tokens[0].take(3).uppercase()
                val remaining = tokens.drop(1)
                    // Filter common connecting words if any
                    .filterNot { it.equals("and", ignoreCase = true) || it.equals("of", ignoreCase = true) }
                    .take(3)
                    .map { it.uppercase() }
                (listOf(prefix) + remaining).joinToString("-")
            }
        }
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

    // =========================================================================
    // BULK ITEMS
    // =========================================================================

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    fun fetchBulkItems() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            inventoryRepository.getBulkItems()
                .onSuccess { _bulkItems.value = it }
                .onFailure { _errorMessage.value = it.message ?: "Failed to load bulk items" }
            _isLoading.value = false
        }
    }

    fun fetchBulkItemDetail(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            inventoryRepository.getBulkItemById(id)
                .onSuccess { _selectedBulkItem.value = it }
                .onFailure { _errorMessage.value = it.message ?: "Failed to load details" }
            _isLoading.value = false
        }
    }

    fun createBulkItem(
        context: Context,
        name: String,
        sku: String,
        description: String?,
        categoryId: String?,
        brand: String?,
        unit: String,
        costPrice: Double,
        sellingPrice: Double,
        taxPercent: Double,
        salesAccountId: String?,
        purchaseAccountId: String?,
        trackInventory: Boolean,
        assemblyType: String,
        warehouseRestrictionId: String?,
        components: List<Pair<String, Int>>,
        imageUri: Uri?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            inventoryRepository.createBulkItem(
                context, name, sku, description, categoryId, brand, unit,
                costPrice, sellingPrice, taxPercent, salesAccountId, purchaseAccountId,
                trackInventory, assemblyType, warehouseRestrictionId, components, imageUri
            ).onSuccess {
                _successMessage.value = "Bulk item created successfully"
                fetchBulkItems()
                onSuccess()
            }.onFailure {
                _errorMessage.value = it.message ?: "Failed to create item"
            }
            _isLoading.value = false
        }
    }

    fun updateBulkItem(
        context: Context,
        id: String,
        name: String,
        sku: String,
        description: String?,
        categoryId: String?,
        brand: String?,
        unit: String,
        costPrice: Double,
        sellingPrice: Double,
        taxPercent: Double,
        salesAccountId: String?,
        purchaseAccountId: String?,
        trackInventory: Boolean,
        assemblyType: String,
        warehouseRestrictionId: String?,
        components: List<Pair<String, Int>>,
        imageUri: Uri?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            inventoryRepository.updateBulkItem(
                context, id, name, sku, description, categoryId, brand, unit,
                costPrice, sellingPrice, taxPercent, salesAccountId, purchaseAccountId,
                trackInventory, assemblyType, warehouseRestrictionId, components, imageUri
            ).onSuccess {
                _successMessage.value = "Bulk item updated successfully"
                fetchBulkItems()
                onSuccess()
            }.onFailure {
                _errorMessage.value = it.message ?: "Failed to update item"
            }
            _isLoading.value = false
        }
    }

    fun deleteBulkItem(id: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            inventoryRepository.deleteBulkItem(id)
                .onSuccess {
                    _successMessage.value = it
                    fetchBulkItems()
                    onSuccess()
                }
                .onFailure { _errorMessage.value = it.message ?: "Failed to delete" }
            _isLoading.value = false
        }
    }

    fun adjustStock(id: String, qty: Int, warehouseId: String?, remarks: String?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            inventoryRepository.adjustStock(id, qty, warehouseId, remarks)
                .onSuccess {
                    _selectedBulkItem.value = it
                    _successMessage.value = "Stock adjusted successfully"
                    fetchBulkItems()
                    onSuccess()
                }
                .onFailure { _errorMessage.value = it.message ?: "Failed to adjust stock" }
            _isLoading.value = false
        }
    }

    // =========================================================================
    // PURCHASE ORDER
    // =========================================================================

    // ── Actions ──

    fun fetchAllPurchaseOrders(search: String? = null, status: String? = null) {
        viewModelScope.launch {
            _isLoadingPurchaseOrders.value = true
            _purchaseOrdersError.value = null

            inventoryRepository.getAllPurchaseOrders(search = search, status = status)
                .onSuccess { list -> _purchaseOrdersList.value = list }
                .onFailure { error -> _purchaseOrdersError.value = extractErrorMessage(error.message) }

            _isLoadingPurchaseOrders.value = false
        }
    }

    fun createPurchaseOrderDirect(request: PurchaseOrder, onSuccess: (PurchaseOrder) -> Unit = {}) {
        viewModelScope.launch {
            _isSubmittingPO.value = true
            _purchaseOrdersError.value = null
            _poSuccessMessage.value = null

            inventoryRepository.createPurchaseOrderDirect(request)
                .onSuccess { data ->
                    _poSuccessMessage.value = "Purchase Order created (${data.poNumber})"
                    fetchAllPurchaseOrders()
                    onSuccess(data)
                }
                .onFailure { error -> _purchaseOrdersError.value = extractErrorMessage(error.message) }

            _isSubmittingPO.value = false
        }
    }

    fun updatePurchaseOrderDirect(id: String, request: PurchaseOrder, onSuccess: (PurchaseOrder) -> Unit = {}) {
        viewModelScope.launch {
            _isSubmittingPO.value = true
            _purchaseOrdersError.value = null
            _poSuccessMessage.value = null

            inventoryRepository.updatePurchaseOrder(id, request)
                .onSuccess { data ->
                    _poSuccessMessage.value = "Purchase Order updated successfully"
                    fetchAllPurchaseOrders()
                    onSuccess(data)
                }
                .onFailure { error -> _purchaseOrdersError.value = extractErrorMessage(error.message) }

            _isSubmittingPO.value = false
        }
    }

    fun receivePurchaseOrder(request: ReceivePurchaseOrderRequest, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isSubmittingPO.value = true
            _purchaseOrdersError.value = null
            _poSuccessMessage.value = null

            inventoryRepository.receivePurchaseOrder(request)
                .onSuccess { data ->
                    _poSuccessMessage.value = "Purchase received successfully (${data.receiveNumber})"
                    fetchAllPurchaseOrders()
                    onSuccess()
                }
                .onFailure { error -> _purchaseOrdersError.value = extractErrorMessage(error.message) }

            _isSubmittingPO.value = false
        }
    }

    fun fetchPOForBillConvert(poId: String) {
        viewModelScope.launch {
            _isLoadingBillConvert.value = true
            _purchaseOrdersError.value = null

            inventoryRepository.getPOForBillConvert(poId)
                .onSuccess { data -> _billConvertDetail.value = data }
                .onFailure { error -> _purchaseOrdersError.value = extractErrorMessage(error.message) }

            _isLoadingBillConvert.value = false
        }
    }

    fun clearPOAlerts() {
        _poSuccessMessage.value = null
        _purchaseOrdersError.value = null
    }

    fun clearBillConvertDetail() {
        _billConvertDetail.value = null
    }

    // =========================================================================
    // REQUISITION
    // =========================================================================
    fun fetchAllRequisitions(search: String? = null, status: String? = null) {
        viewModelScope.launch {
            _isLoadingRequisitions.value = true
            _requisitionErrorMessage.value = null

            inventoryRepository.getAllRequisitions(search = search, status = status)
                .onSuccess { list ->
                    _requisitionsList.value = list
                }
                .onFailure { error ->
                    _requisitionErrorMessage.value = extractErrorMessage(error.message)
                }

            _isLoadingRequisitions.value = false
        }
    }
    fun fetchRequisitionById(id: String, onLoaded: (PurchaseRequisition) -> Unit = {}) {
        viewModelScope.launch {
            _isLoadingRequisitions.value = true
            _requisitionErrorMessage.value = null

            inventoryRepository.getRequisitionById(id)
                .onSuccess { req ->
                    _selectedRequisition.value = req
                    onLoaded(req)
                }
                .onFailure { error ->
                    _requisitionErrorMessage.value = extractErrorMessage(error.message)
                }

            _isLoadingRequisitions.value = false
        }
    }

    // Add or replace this function in your InventoryViewModel.kt

//    fun addRequisitionComment(requisitionId: String, commentText: String) {
//        if (commentText.isBlank()) return // Do not send empty comments
//
//        viewModelScope.launch {
//            try {
//                // Call the repository function
//                val response = inventoryRepository.addRequisitionComment(requisitionId, commentText)
//
//                if (response.isSuccessful && response.body()?.success == true) {
//                    val updatedRequisition = response.body()?.data
//                    if (updatedRequisition != null) {
//                        // [OPTIMIZATION]
//                        // The API returns the full updated object.
//                        // So, we can directly update the state without making another fetch call.
//                        _selectedRequisition.value = updatedRequisition
//                        _requisitionSuccessMessage.value = "Comment added successfully!"
//                    } else {
//                        // If data is null, re-fetch as a fallback
//                        fetchRequisitionById(requisitionId)
//                    }
//                } else {
//                    _requisitionErrorMessage.value = "Failed to add comment. Please try again."
//                }
//            } catch (e: Exception) {
//                _requisitionErrorMessage.value = "An error occurred: ${e.message}"
//            }
//        }
//    }

    fun createRequisition(
        request: CreateRequisitionRequest,
        onSuccess: (PurchaseRequisition) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isSubmittingRequisition.value = true
            _requisitionErrorMessage.value = null
            _requisitionSuccessMessage.value = null

            inventoryRepository.createRequisition(request)
                .onSuccess { created ->
                    _requisitionSuccessMessage.value = "Requisition created successfully (${created.prNumber})"
                    fetchAllRequisitions()
                    onSuccess(created)
                }
                .onFailure { error ->
                    _requisitionErrorMessage.value = extractErrorMessage(error.message)
                }

            _isSubmittingRequisition.value = false
        }
    }

    fun actionRequisitionApproval(
        id: String,
        status: String,
        remarks: String? = null,
        onSuccess: (PurchaseRequisition) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isSubmittingRequisition.value = true
            _requisitionErrorMessage.value = null
            _requisitionSuccessMessage.value = null

            val req = RequisitionApprovalActionRequest(status = status, remarks = remarks)
            inventoryRepository.actionRequisitionApproval(id, req)
                .onSuccess { updated ->
                    _requisitionSuccessMessage.value = "Requisition marked as $status"
                    fetchAllRequisitions()
                    onSuccess(updated)
                }
                .onFailure { error ->
                    _requisitionErrorMessage.value = extractErrorMessage(error.message)
                }

            _isSubmittingRequisition.value = false
        }
    }

    fun clearRequisitionAlerts() {
        _requisitionSuccessMessage.value = null
        _requisitionErrorMessage.value = null
    }

    // =============================================================================
    // BARCODE ACTIONS
    // =========================================================================

    /**
     * Fetch all barcodes with optional search query and active/inactive status filter.
     */
    fun fetchAllBarcodes(search: String? = null, status: String? = null) {
        viewModelScope.launch {
            _isLoadingBarcodes.value = true
            _barcodeErrorMessage.value = null

            inventoryRepository.getAllBarcodes(search = search, status = status)
                .onSuccess { list ->
                    _barcodesList.value = list
                }
                .onFailure { error ->
                    _barcodeErrorMessage.value = extractErrorMessage(error.message)
                }

            _isLoadingBarcodes.value = false
        }
    }

    /**
     * Fetch single barcode details (View One) along with print logs.
     */
    fun fetchBarcodeViewOne(id: String, onLoaded: (BarcodeItemDoc) -> Unit = {}) {
        viewModelScope.launch {
            _isLoadingBarcodes.value = true
            _barcodeErrorMessage.value = null

            inventoryRepository.getBarcodeViewOne(id)
                .onSuccess { doc ->
                    _selectedBarcode.value = doc
                    onLoaded(doc)
                }
                .onFailure { error ->
                    _barcodeErrorMessage.value = extractErrorMessage(error.message)
                }

            _isLoadingBarcodes.value = false
        }
    }

    /**
     * Generate a new barcode.
     */
    fun generateBarcode(
        request: GenerateBarcodeRequest,
        onSuccess: (BarcodeItemDoc) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isSubmittingBarcode.value = true
            _barcodeErrorMessage.value = null
            _barcodeSuccessMessage.value = null

            inventoryRepository.generateBarcode(request)
                .onSuccess { createdDoc ->
                    _barcodeSuccessMessage.value = "Barcode generated successfully (${createdDoc.barcodeNumber})"
                    fetchAllBarcodes()
                    onSuccess(createdDoc)
                }
                .onFailure { error ->
                    _barcodeErrorMessage.value = extractErrorMessage(error.message)
                }

            _isSubmittingBarcode.value = false
        }
    }

    /**
     * Toggle Active / Inactive status of a barcode.
     */
    fun toggleBarcodeStatus(id: String, onSuccess: (BarcodeItemDoc) -> Unit = {}) {
        viewModelScope.launch {
            _isSubmittingBarcode.value = true
            _barcodeErrorMessage.value = null

            inventoryRepository.toggleBarcodeStatus(id)
                .onSuccess { updatedDoc ->
                    _barcodeSuccessMessage.value = "Barcode status updated to ${updatedDoc.status}"
                    fetchAllBarcodes()
                    onSuccess(updatedDoc)
                }
                .onFailure { error ->
                    _barcodeErrorMessage.value = extractErrorMessage(error.message)
                }

            _isSubmittingBarcode.value = false
        }
    }

    /**
     * Delete a barcode.
     */
    fun deleteBarcode(id: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isSubmittingBarcode.value = true
            _barcodeErrorMessage.value = null

            inventoryRepository.deleteBarcode(id)
                .onSuccess { message ->
                    _barcodeSuccessMessage.value = message
                    fetchAllBarcodes()
                    onSuccess()
                }
                .onFailure { error ->
                    _barcodeErrorMessage.value = extractErrorMessage(error.message)
                }

            _isSubmittingBarcode.value = false
        }
    }

    /**
     * Clear Barcode alerts and selection.
     */
    fun clearBarcodeAlerts() {
        _barcodeSuccessMessage.value = null
        _barcodeErrorMessage.value = null
    }

    fun clearSelectedBarcode() {
        _selectedBarcode.value = null
    }

    // =============================================================================
    //  PURCHASE RECEIVE
    // =============================================================================

    fun fetchAllReceives(search: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            inventoryRepository.getAllReceives(search = search)
                .onSuccess { _allReceives.value = it }
                .onFailure { _errorMessage.value = it.localizedMessage ?: "Error loading receives" }
            _isLoading.value = false
        }
    }

    fun fetchReceiveHistoryByPo(poId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            inventoryRepository.getReceiveHistoryByPo(poId)
                .onSuccess { _poHistory.value = it }
                .onFailure { _errorMessage.value = it.localizedMessage ?: "Error loading PO history" }
            _isLoading.value = false
        }
    }

    fun fetchSingleReceive(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            inventoryRepository.getSingleReceive(id)
                .onSuccess { _singleReceive.value = it }
                .onFailure { _errorMessage.value = it.localizedMessage ?: "Error loading receive" }
            _isLoading.value = false
        }
    }

    fun convertReceiveToBill(receiveId: String, onSuccess: (BillCreatedData) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            inventoryRepository.convertReceiveToBill(receiveId)
                .onSuccess { billData ->
                    _generatedBill.value = billData
                    onSuccess(billData)
                }
                .onFailure { _errorMessage.value = it.localizedMessage ?: "Error converting to bill" }
            _isLoading.value = false
        }
    }

    fun clearErrors() {
        _errorMessage.value = null
    }
}