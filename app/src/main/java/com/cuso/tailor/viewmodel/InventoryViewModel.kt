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

package com.cuso.tailor.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.tailor.model.inventory.AssignStockLocationRequest
import com.cuso.tailor.model.inventory.BarcodeItemDoc
import com.cuso.tailor.model.inventory.BillCreatedData
import com.cuso.tailor.model.inventory.BillResponseData
import com.cuso.tailor.model.inventory.BulkItemDoc
import com.cuso.tailor.model.inventory.CapacitySummary
import com.cuso.tailor.model.inventory.CreateBillRequest
import com.cuso.tailor.model.inventory.CreateInventoryItemResponse
import com.cuso.tailor.model.inventory.CreateItemGroupRequest
import com.cuso.tailor.model.inventory.CreatePoItemRequest
import com.cuso.tailor.model.inventory.CreatePurchaseOrderRequest
import com.cuso.tailor.model.inventory.CreateRequisitionRequest
import com.cuso.tailor.model.inventory.CreateSupplierRequest
import com.cuso.tailor.model.inventory.CreateWarehouseRequest
import com.cuso.tailor.model.inventory.DecreaseStockRequest
import com.cuso.tailor.model.inventory.GenerateBarcodeRequest
import com.cuso.tailor.model.inventory.HierarchyDropdownItem
import com.cuso.tailor.model.inventory.IncreaseStockRequest
import com.cuso.tailor.model.inventory.InventoryItem
import com.cuso.tailor.model.inventory.InventoryItemviewone
import com.cuso.tailor.model.inventory.InventoryPagination
import com.cuso.tailor.model.inventory.ItemGroupDto
import com.cuso.tailor.model.inventory.ItemGroupViewOneData
import com.cuso.tailor.model.inventory.LowStockItemDto
import com.cuso.tailor.model.inventory.POBillConvertData
import com.cuso.tailor.model.inventory.PaymentTermDto
import com.cuso.tailor.model.inventory.PhysicalAttributes
import com.cuso.tailor.model.inventory.PurchaseOrder
import com.cuso.tailor.model.inventory.PurchaseOrderData
import com.cuso.tailor.model.inventory.PurchaseOrderDetailData
import com.cuso.tailor.model.inventory.PurchaseOrderSummaryDto
import com.cuso.tailor.model.inventory.PurchaseReceiveItem
import com.cuso.tailor.model.inventory.PurchaseRequisition
import com.cuso.tailor.model.inventory.ReceiveHistoryByPoResponse
import com.cuso.tailor.model.inventory.ReceivePurchaseOrderRequest
import com.cuso.tailor.model.inventory.RecordPaymentRequest
import com.cuso.tailor.model.inventory.SafetyStockItemDto
import com.cuso.tailor.model.inventory.StockAdjustmentData
import com.cuso.tailor.model.inventory.StockLocationAssignmentData
import com.cuso.tailor.model.inventory.StockLocationItemDto
import com.cuso.tailor.model.inventory.StockLocationViewOneData
import com.cuso.tailor.model.inventory.StockSummaryItemDto
import com.cuso.tailor.model.inventory.SupplierDropdownItem
import com.cuso.tailor.model.inventory.SupplierDto
import com.cuso.tailor.model.inventory.SupplierLedgerContainer
import com.cuso.tailor.model.inventory.TaxGroupDto
import com.cuso.tailor.model.inventory.TransferStockRequest
import com.cuso.tailor.model.inventory.UpdateWarehouseRequest
import com.cuso.tailor.model.inventory.VariantSelection
import com.cuso.tailor.model.inventory.WarehouseAddress
import com.cuso.tailor.model.inventory.WarehouseDropdownItem
import com.cuso.tailor.model.inventory.WarehouseItem
import com.cuso.tailor.repository.InventoryRepository
import com.cuso.tailor.utils.launchBusy
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
    val isLoadingMore: Boolean = false,
    val canLoadMore: Boolean = true,
    val currentPage: Int = 1,
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

    companion object {
        private const val DEFAULT_LIMIT = 10
        private const val ITEM_GROUP_PAGE_SIZE = 20
        private const val SUPPLIER_PAGE_SIZE = 20
        private const val PO_PAGE_SIZE = 15
        private const val REQ_PAGE_SIZE = 15
        private const val RECEIVE_PAGE_SIZE = 20
    }

    // =========================================================================
    // 1. ITEM GROUPS (VIEW-ALL: PAGINATED)
    // =========================================================================
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

    // =========================================================================
    // 2. INVENTORY ITEMS (VIEW-ALL: PAGINATED)
    // =========================================================================
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

    // ── Inventory Item: View One / Detail / Recent ──
    private val _viewOneItem = MutableStateFlow<InventoryItemviewone?>(null)
    val viewOneItem: StateFlow<InventoryItemviewone?> = _viewOneItem.asStateFlow()

    private val _isLoadingViewOne = MutableStateFlow(false)
    val isLoadingViewOne: StateFlow<Boolean> = _isLoadingViewOne.asStateFlow()

    private val _viewOneError = MutableStateFlow<String?>(null)
    val viewOneError: StateFlow<String?> = _viewOneError.asStateFlow()

    private val _showViewOneSheet = MutableStateFlow(false)
    val showViewOneSheet: StateFlow<Boolean> = _showViewOneSheet.asStateFlow()

    private val _selectedItem = MutableStateFlow<InventoryItem?>(null)
    val selectedItem: StateFlow<InventoryItem?> = _selectedItem.asStateFlow()

    private val _isLoadingItemDetail = MutableStateFlow(false)
    val isLoadingItemDetail: StateFlow<Boolean> = _isLoadingItemDetail.asStateFlow()

    private val _itemDetailError = MutableStateFlow<String?>(null)
    val itemDetailError: StateFlow<String?> = _itemDetailError.asStateFlow()

    private val _showItemDetailSheet = MutableStateFlow(false)
    val showItemDetailSheet: StateFlow<Boolean> = _showItemDetailSheet.asStateFlow()

    private val _recentItems = MutableStateFlow<List<InventoryItem>>(emptyList())
    val recentItems: StateFlow<List<InventoryItem>> = _recentItems.asStateFlow()

    private val _isLoadingRecentItems = MutableStateFlow(false)
    val isLoadingRecentItems: StateFlow<Boolean> = _isLoadingRecentItems.asStateFlow()

    private val _recentItemsError = MutableStateFlow<String?>(null)
    val recentItemsError: StateFlow<String?> = _recentItemsError.asStateFlow()

    // =========================================================================
    // 3. STOCK ADJUSTMENT (VIEW-ALL: PAGINATED)
    // =========================================================================
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

    private val _validAdjustmentReasons = MutableStateFlow<List<String>>(emptyList())
    val validAdjustmentReasons: StateFlow<List<String>> = _validAdjustmentReasons.asStateFlow()

    private val _stockAdjustmentsList = MutableStateFlow<List<StockAdjustmentData>>(emptyList())
    val stockAdjustmentsList: StateFlow<List<StockAdjustmentData>> = _stockAdjustmentsList.asStateFlow()

    private val _selectedAdjustmentDetail = MutableStateFlow<StockAdjustmentData?>(null)
    val selectedAdjustmentDetail: StateFlow<StockAdjustmentData?> = _selectedAdjustmentDetail.asStateFlow()

    private val _isLoadingAdjustments = MutableStateFlow(false)
    val isLoadingAdjustments: StateFlow<Boolean> = _isLoadingAdjustments.asStateFlow()

    private val _isLoadingMoreAdjustments = MutableStateFlow(false)
    val isLoadingMoreAdjustments: StateFlow<Boolean> = _isLoadingMoreAdjustments.asStateFlow()

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

    private val _selectedAdjustmentType = MutableStateFlow<String?>("transfer")
    val selectedAdjustmentType: StateFlow<String?> = _selectedAdjustmentType.asStateFlow()

    private var activeAdjustmentSearch: String? = null

    // ── Stock Summary Pagination States ──
    private val _isLoadingMoreStockSummary = MutableStateFlow(false)
    val isLoadingMoreStockSummary: StateFlow<Boolean> = _isLoadingMoreStockSummary.asStateFlow()

    private val _canLoadMoreStockSummary = MutableStateFlow(true)
    val canLoadMoreStockSummary: StateFlow<Boolean> = _canLoadMoreStockSummary.asStateFlow()

    private val _currentStockSummaryPage = MutableStateFlow(1)
    val currentStockSummaryPage: StateFlow<Int> = _currentStockSummaryPage.asStateFlow()

    // ── Low Stock Alerts ──
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

    // ── Low Stock Pagination States ──
    private val _isLoadingMoreLowStock = MutableStateFlow(false)
    val isLoadingMoreLowStock: StateFlow<Boolean> = _isLoadingMoreLowStock.asStateFlow()

    private val _canLoadMoreLowStock = MutableStateFlow(true)
    val canLoadMoreLowStock: StateFlow<Boolean> = _canLoadMoreLowStock.asStateFlow()

    private val _currentLowStockPage = MutableStateFlow(1)
    val currentLowStockPage: StateFlow<Int> = _currentLowStockPage.asStateFlow()

    private var activeLowStockWarehouseId: String? = null

    // ── Create Item Form State ──
    private val _expandedSection = MutableStateFlow(ItemSection.ITEM_IDENTITY)
    val expandedSection: StateFlow<ItemSection> = _expandedSection.asStateFlow()

    private val _createItemForm = MutableStateFlow(CreateItemFormState())
    val createItemForm: StateFlow<CreateItemFormState> = _createItemForm.asStateFlow()

    private val _createItemUiState = MutableStateFlow<CreateItemUiState>(CreateItemUiState.Idle)
    val createItemUiState: StateFlow<CreateItemUiState> = _createItemUiState.asStateFlow()

    // =========================================================================
    // 4. WAREHOUSES
    // =========================================================================
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

    // =========================================================================
    // 5. SUPPLIERS (VIEW-ALL: PAGINATED)
    // =========================================================================
    private val _suppliers = MutableStateFlow<List<SupplierDto>>(emptyList())
    val suppliers: StateFlow<List<SupplierDto>> = _suppliers.asStateFlow()

    private val _isLoadingSuppliers = MutableStateFlow(false)
    val isLoadingSuppliers: StateFlow<Boolean> = _isLoadingSuppliers.asStateFlow()

    private val _isLoadingMoreSuppliers = MutableStateFlow(false)
    val isLoadingMoreSuppliers: StateFlow<Boolean> = _isLoadingMoreSuppliers.asStateFlow()

    private val _canLoadMoreSuppliers = MutableStateFlow(true)
    val canLoadMoreSuppliers: StateFlow<Boolean> = _canLoadMoreSuppliers.asStateFlow()

    private val _currentSupplierPage = MutableStateFlow(1)
    val currentSupplierPage: StateFlow<Int> = _currentSupplierPage.asStateFlow()

    private val _suppliersError = MutableStateFlow<String?>(null)
    val suppliersError: StateFlow<String?> = _suppliersError.asStateFlow()

    private var activeSupplierSearch: String? = null
    private var activeSupplierStatus: String? = null

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

    // ── Bulk Items ──
    private val _bulkItems = MutableStateFlow<List<BulkItemDoc>>(emptyList())
    val bulkItems: StateFlow<List<BulkItemDoc>> = _bulkItems.asStateFlow()

    private val _selectedBulkItem = MutableStateFlow<BulkItemDoc?>(null)
    val selectedBulkItem: StateFlow<BulkItemDoc?> = _selectedBulkItem.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLoadingBulkDetail = MutableStateFlow(false)
    val isLoadingBulkDetail: StateFlow<Boolean> = _isLoadingBulkDetail.asStateFlow()

    private val _bulkDetailError = MutableStateFlow<String?>(null)
    val bulkDetailError: StateFlow<String?> = _bulkDetailError.asStateFlow()

    private val _bulkError = MutableStateFlow<String?>(null)
    val bulkError: StateFlow<String?> = _bulkError.asStateFlow()

    private val _bulkSuccessMessage = MutableStateFlow<String?>(null)
    val bulkSuccessMessage: StateFlow<String?> = _bulkSuccessMessage.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private var activeBulkSearch: String? = null

    private val _isLoadingMoreBulk = MutableStateFlow(false)
    val isLoadingMoreBulk: StateFlow<Boolean> = _isLoadingMoreBulk.asStateFlow()

    private val _canLoadMoreBulk = MutableStateFlow(true)
    val canLoadMoreBulk: StateFlow<Boolean> = _canLoadMoreBulk.asStateFlow()

    private val _currentBulkPage = MutableStateFlow(1)
    val currentBulkPage: StateFlow<Int> = _currentBulkPage.asStateFlow()

    // =========================================================================
    // 6. PURCHASE ORDERS (VIEW-ALL: PAGINATED)
    // =========================================================================
    private val _purchaseOrdersList = MutableStateFlow<List<PurchaseOrder>>(emptyList())
    val purchaseOrdersList: StateFlow<List<PurchaseOrder>> = _purchaseOrdersList.asStateFlow()

    private val _isLoadingPurchaseOrders = MutableStateFlow(false)
    val isLoadingPurchaseOrders: StateFlow<Boolean> = _isLoadingPurchaseOrders.asStateFlow()

    private val _isLoadingMorePurchaseOrders = MutableStateFlow(false)
    val isLoadingMorePurchaseOrders: StateFlow<Boolean> = _isLoadingMorePurchaseOrders.asStateFlow()

    private val _canLoadMorePurchaseOrders = MutableStateFlow(true)
    val canLoadMorePurchaseOrders: StateFlow<Boolean> = _canLoadMorePurchaseOrders.asStateFlow()

    private val _currentPOPage = MutableStateFlow(1)
    val currentPOPage: StateFlow<Int> = _currentPOPage.asStateFlow()

    private val _purchaseOrdersError = MutableStateFlow<String?>(null)
    val purchaseOrdersError: StateFlow<String?> = _purchaseOrdersError.asStateFlow()

    private var activePOSearch: String? = null
    private var activePOStatus: String? = null

    private val _isSubmittingPO = MutableStateFlow(false)
    val isSubmittingPO: StateFlow<Boolean> = _isSubmittingPO.asStateFlow()

    private val _poSuccessMessage = MutableStateFlow<String?>(null)
    val poSuccessMessage: StateFlow<String?> = _poSuccessMessage.asStateFlow()

    private val _billConvertDetail = MutableStateFlow<POBillConvertData?>(null)
    val billConvertDetail: StateFlow<POBillConvertData?> = _billConvertDetail.asStateFlow()

    private val _isLoadingBillConvert = MutableStateFlow(false)
    val isLoadingBillConvert: StateFlow<Boolean> = _isLoadingBillConvert.asStateFlow()

    // =========================================================================
    // 7. PURCHASE REQUISITIONS (VIEW-ALL: PAGINATED)
    // =========================================================================
    private val _requisitionsList = MutableStateFlow<List<PurchaseRequisition>>(emptyList())
    val requisitionsList: StateFlow<List<PurchaseRequisition>> = _requisitionsList.asStateFlow()

    private val _isLoadingRequisitions = MutableStateFlow(false)
    val isLoadingRequisitions: StateFlow<Boolean> = _isLoadingRequisitions.asStateFlow()

    private val _isLoadingMoreRequisitions = MutableStateFlow(false)
    val isLoadingMoreRequisitions: StateFlow<Boolean> = _isLoadingMoreRequisitions.asStateFlow()

    private val _canLoadMoreRequisitions = MutableStateFlow(true)
    val canLoadMoreRequisitions: StateFlow<Boolean> = _canLoadMoreRequisitions.asStateFlow()

    private val _currentRequisitionPage = MutableStateFlow(1)
    val currentRequisitionPage: StateFlow<Int> = _currentRequisitionPage.asStateFlow()

    private val _selectedRequisition = MutableStateFlow<PurchaseRequisition?>(null)
    val selectedRequisition: StateFlow<PurchaseRequisition?> = _selectedRequisition.asStateFlow()

    private val _isSubmittingRequisition = MutableStateFlow(false)
    val isSubmittingRequisition: StateFlow<Boolean> = _isSubmittingRequisition.asStateFlow()

    private val _requisitionSuccessMessage = MutableStateFlow<String?>(null)
    val requisitionSuccessMessage: StateFlow<String?> = _requisitionSuccessMessage.asStateFlow()

    private val _requisitionErrorMessage = MutableStateFlow<String?>(null)
    val requisitionErrorMessage: StateFlow<String?> = _requisitionErrorMessage.asStateFlow()

    private var activeReqSearch: String? = null
    private var activeReqStatus: String? = null

    // ── Barcode States ──
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

    private val _isLoadingMoreBarcodes = MutableStateFlow(false)
    val isLoadingMoreBarcodes: StateFlow<Boolean> = _isLoadingMoreBarcodes.asStateFlow()

    private val _canLoadMoreBarcodes = MutableStateFlow(true)
    val canLoadMoreBarcodes: StateFlow<Boolean> = _canLoadMoreBarcodes.asStateFlow()

    private val _currentBarcodePage = MutableStateFlow(1)
    val currentBarcodePage: StateFlow<Int> = _currentBarcodePage.asStateFlow()

    private var activeBarcodeSearch: String? = null
    private var activeBarcodeStatus: String? = null

    // =========================================================================
    // 8. PURCHASE RECEIVE & RECEIVE HISTORY
    // =========================================================================
    private val _allReceives = MutableStateFlow<List<PurchaseReceiveItem>>(emptyList())
    val allReceives: StateFlow<List<PurchaseReceiveItem>> = _allReceives.asStateFlow()

    private val _isLoadingReceives = MutableStateFlow(false)
    val isLoadingReceives: StateFlow<Boolean> = _isLoadingReceives.asStateFlow()

    private val _isLoadingMoreReceives = MutableStateFlow(false)
    val isLoadingMoreReceives: StateFlow<Boolean> = _isLoadingMoreReceives.asStateFlow()

    private val _canLoadMoreReceives = MutableStateFlow(true)
    val canLoadMoreReceives: StateFlow<Boolean> = _canLoadMoreReceives.asStateFlow()

    private val _currentReceivesPage = MutableStateFlow(1)
    val currentReceivesPage: StateFlow<Int> = _currentReceivesPage.asStateFlow()

    private var activeReceivesSearch: String? = null

    private val _poHistory = MutableStateFlow<ReceiveHistoryByPoResponse?>(null)
    val poHistory: StateFlow<ReceiveHistoryByPoResponse?> = _poHistory.asStateFlow()

    private val _singleReceive = MutableStateFlow<PurchaseReceiveItem?>(null)
    val singleReceive: StateFlow<PurchaseReceiveItem?> = _singleReceive.asStateFlow()

    private val _generatedBill = MutableStateFlow<BillCreatedData?>(null)
    val generatedBill: StateFlow<BillCreatedData?> = _generatedBill.asStateFlow()

    // ── Purchase Order Receive Summary List States ──
    private val _poSummaryList = MutableStateFlow<List<PurchaseOrderSummaryDto>>(emptyList())
    val poSummaryList: StateFlow<List<PurchaseOrderSummaryDto>> = _poSummaryList.asStateFlow()

    private val _isLoadingPoSummary = MutableStateFlow(false)
    val isLoadingPoSummary: StateFlow<Boolean> = _isLoadingPoSummary.asStateFlow()

    private val _isLoadingMorePoSummary = MutableStateFlow(false)
    val isLoadingMorePoSummary: StateFlow<Boolean> = _isLoadingMorePoSummary.asStateFlow()

    private val _canLoadMorePoSummary = MutableStateFlow(true)
    val canLoadMorePoSummary: StateFlow<Boolean> = _canLoadMorePoSummary.asStateFlow()

    private val _currentPoSummaryPage = MutableStateFlow(1)
    val currentPoSummaryPage: StateFlow<Int> = _currentPoSummaryPage.asStateFlow()

    private val _poSummaryError = MutableStateFlow<String?>(null)
    val poSummaryError: StateFlow<String?> = _poSummaryError.asStateFlow()

    private var activePoSummarySearch: String? = null

    // =========================================================================
    // 9. LOCATION MANAGEMENT (PAGINATED)
    // =========================================================================
    private val _stockLocationItems = MutableStateFlow<List<StockLocationItemDto>>(emptyList())
    val stockLocationItems: StateFlow<List<StockLocationItemDto>> = _stockLocationItems.asStateFlow()

    private val _isLoadingStockLocations = MutableStateFlow(false)
    val isLoadingStockLocations: StateFlow<Boolean> = _isLoadingStockLocations.asStateFlow()

    private val _isLoadingMoreStockLocations = MutableStateFlow(false)
    val isLoadingMoreStockLocations: StateFlow<Boolean> = _isLoadingMoreStockLocations.asStateFlow()

    private val _canLoadMoreStockLocations = MutableStateFlow(true)
    val canLoadMoreStockLocations: StateFlow<Boolean> = _canLoadMoreStockLocations.asStateFlow()

    private val _currentStockLocationPage = MutableStateFlow(1)
    val currentStockLocationPage: StateFlow<Int> = _currentStockLocationPage.asStateFlow()

    private val _stockLocationTotal = MutableStateFlow(0)
    val stockLocationTotal: StateFlow<Int> = _stockLocationTotal.asStateFlow()

    private val _stockLocationError = MutableStateFlow<String?>(null)
    val stockLocationError: StateFlow<String?> = _stockLocationError.asStateFlow()

    private var activeStockLocationSearch: String? = null
    private var activeStockLocationStatus: String? = null
    private var stockLocationSearchJob: Job? = null

    private val _selectedStockLocationDetail = MutableStateFlow<StockLocationViewOneData?>(null)
    val selectedStockLocationDetail: StateFlow<StockLocationViewOneData?> = _selectedStockLocationDetail.asStateFlow()

    private val _isLoadingStockLocationDetail = MutableStateFlow(false)
    val isLoadingStockLocationDetail: StateFlow<Boolean> = _isLoadingStockLocationDetail.asStateFlow()

    private val _stockLocationDetailError = MutableStateFlow<String?>(null)
    val stockLocationDetailError: StateFlow<String?> = _stockLocationDetailError.asStateFlow()

    private val _floorDropdown = MutableStateFlow<List<HierarchyDropdownItem>>(emptyList())
    val floorDropdown: StateFlow<List<HierarchyDropdownItem>> = _floorDropdown.asStateFlow()

    private val _sectionDropdown = MutableStateFlow<List<HierarchyDropdownItem>>(emptyList())
    val sectionDropdown: StateFlow<List<HierarchyDropdownItem>> = _sectionDropdown.asStateFlow()

    private val _rackDropdown = MutableStateFlow<List<HierarchyDropdownItem>>(emptyList())
    val rackDropdown: StateFlow<List<HierarchyDropdownItem>> = _rackDropdown.asStateFlow()

    private val _binDropdown = MutableStateFlow<List<HierarchyDropdownItem>>(emptyList())
    val binDropdown: StateFlow<List<HierarchyDropdownItem>> = _binDropdown.asStateFlow()

    private val _isSubmittingStockLocation = MutableStateFlow(false)
    val isSubmittingStockLocation: StateFlow<Boolean> = _isSubmittingStockLocation.asStateFlow()

    private val _stockLocationActionSuccess = MutableStateFlow<String?>(null)
    val stockLocationActionSuccess: StateFlow<String?> = _stockLocationActionSuccess.asStateFlow()

    private val _stockLocationActionError = MutableStateFlow<String?>(null)
    val stockLocationActionError: StateFlow<String?> = _stockLocationActionError.asStateFlow()

    // =========================================================================
    // PURCHASE ORDER VIEW-ONE / DETAIL
    // =========================================================================
    private val _purchaseOrderDetail = MutableStateFlow<PurchaseOrderDetailData?>(null)
    val purchaseOrderDetail: StateFlow<PurchaseOrderDetailData?> = _purchaseOrderDetail.asStateFlow()

    private val _isLoadingPODetail = MutableStateFlow<Boolean>(false)
    val isLoadingPODetail: StateFlow<Boolean> = _isLoadingPODetail.asStateFlow()

    private val _poDetailError = MutableStateFlow<String?>(null)
    val poDetailError: StateFlow<String?> = _poDetailError.asStateFlow()


    // ── Create Bill States ──
    private val _isCreatingBill = MutableStateFlow(false)
    val isCreatingBill: StateFlow<Boolean> = _isCreatingBill.asStateFlow()

    private val _createBillSuccessMessage = MutableStateFlow<String?>(null)
    val createBillSuccessMessage: StateFlow<String?> = _createBillSuccessMessage.asStateFlow()

    private val _createBillErrorMessage = MutableStateFlow<String?>(null)
    val createBillErrorMessage: StateFlow<String?> = _createBillErrorMessage.asStateFlow()

    // ─────────────────────────────────────────────────────────────
// ── PAYMENT TERMS & TAX GROUPS STATE ──
// ─────────────────────────────────────────────────────────────
    private val _paymentTerms = MutableStateFlow<List<PaymentTermDto>>(emptyList())
    val paymentTerms: StateFlow<List<PaymentTermDto>> = _paymentTerms.asStateFlow()

    private val _isLoadingPaymentTerms = MutableStateFlow(false)
    val isLoadingPaymentTerms: StateFlow<Boolean> = _isLoadingPaymentTerms.asStateFlow()

    private val _taxGroups = MutableStateFlow<List<TaxGroupDto>>(emptyList())
    val taxGroups: StateFlow<List<TaxGroupDto>> = _taxGroups.asStateFlow()

    private val _isLoadingTaxGroups = MutableStateFlow(false)
    val isLoadingTaxGroups: StateFlow<Boolean> = _isLoadingTaxGroups.asStateFlow()

    private val _isSubmittingBill = MutableStateFlow(false)
    val isSubmittingBill: StateFlow<Boolean> = _isSubmittingBill.asStateFlow()


    //SAFETY STOCK

    // Inside InventoryViewModel:
    private val _safetyStockList = MutableStateFlow<List<SafetyStockItemDto>>(emptyList())
    val safetyStockList = _safetyStockList.asStateFlow()

    private val _isLoadingSafetyStock = MutableStateFlow(false)
    val isLoadingSafetyStock = _isLoadingSafetyStock.asStateFlow()

    private val _isLoadingMoreSafetyStock = MutableStateFlow(false)
    val isLoadingMoreSafetyStock = _isLoadingMoreSafetyStock.asStateFlow()

    private val _canLoadMoreSafetyStock = MutableStateFlow(true)
    val canLoadMoreSafetyStock = _canLoadMoreSafetyStock.asStateFlow()

    private val _safetyStockError = MutableStateFlow<String?>(null)
    val safetyStockError = _safetyStockError.asStateFlow()

    private var currentSafetyStockPage = 1
    private var totalSafetyStockPages = 1

    // =============================================================================
    // Bill list
    // =============================================================================
    private val _billsList = MutableStateFlow<List<com.cuso.tailor.model.inventory.ProcurementBillItem>>(emptyList())
    val billsList: StateFlow<List<com.cuso.tailor.model.inventory.ProcurementBillItem>> = _billsList.asStateFlow()

    private val _isLoadingBills = MutableStateFlow(false)
    val isLoadingBills: StateFlow<Boolean> = _isLoadingBills.asStateFlow()

    private val _isLoadingMoreBills = MutableStateFlow(false)
    val isLoadingMoreBills: StateFlow<Boolean> = _isLoadingMoreBills.asStateFlow()

    private val _canLoadMoreBills = MutableStateFlow(true)
    val canLoadMoreBills: StateFlow<Boolean> = _canLoadMoreBills.asStateFlow()

    private val _currentBillsPage = MutableStateFlow(1)
    val currentBillsPage: StateFlow<Int> = _currentBillsPage.asStateFlow()

    private val _billsError = MutableStateFlow<String?>(null)
    val billsError: StateFlow<String?> = _billsError.asStateFlow()

    private var activeBillsSearch: String? = null
    private var activeBillsStatus: String? = null
    private var billsSearchJob: Job? = null

    //sent and void

    private val _isBillActionInProgress = MutableStateFlow(false)
    val isBillActionInProgress: StateFlow<Boolean> = _isBillActionInProgress.asStateFlow()

    private val _billActionSuccessMessage = MutableStateFlow<String?>(null)
    val billActionSuccessMessage: StateFlow<String?> = _billActionSuccessMessage.asStateFlow()

    private val _billActionErrorMessage = MutableStateFlow<String?>(null)
    val billActionErrorMessage: StateFlow<String?> = _billActionErrorMessage.asStateFlow()


    // =========================================================================
    // PROCUREMENT BILL DETAIL STATE & ACTIONS
    // =========================================================================
    private val _selectedBillDetail = MutableStateFlow<com.cuso.tailor.model.inventory.ProcurementBillDetailData?>(null)
    val selectedBillDetail: StateFlow<com.cuso.tailor.model.inventory.ProcurementBillDetailData?> = _selectedBillDetail.asStateFlow()

    private val _isLoadingBillDetail = MutableStateFlow(false)
    val isLoadingBillDetail: StateFlow<Boolean> = _isLoadingBillDetail.asStateFlow()

    private val _billDetailError = MutableStateFlow<String?>(null)
    val billDetailError: StateFlow<String?> = _billDetailError.asStateFlow()

    //record payment

    private val _isRecordingPayment = MutableStateFlow(false)
    val isRecordingPayment: StateFlow<Boolean> = _isRecordingPayment.asStateFlow()

    private val _recordPaymentErrorMessage = MutableStateFlow<String?>(null)
    val recordPaymentErrorMessage: StateFlow<String?> = _recordPaymentErrorMessage.asStateFlow()



    fun fetchPurchaseOrderDetail(poId: String) {
        if (poId.isBlank()) return
        viewModelScope.launch {
            _isLoadingPODetail.value = true
            _poDetailError.value = null
            inventoryRepository.getPurchaseOrderById(poId)
                .onSuccess { detail ->
                    _purchaseOrderDetail.value = detail
                }
                .onFailure { error ->
                    _poDetailError.value = extractErrorMessage(error.message)
                }
            _isLoadingPODetail.value = false
        }
    }

    fun clearPurchaseOrderDetail() {
        _purchaseOrderDetail.value = null
        _poDetailError.value = null
    }

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
    // 1. ITEM GROUP ACTIONS (INFINITE SCROLL ENABLED)
    // =========================================================================

    fun loadItemGroups(query: String? = null) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    currentPage = 1,
                    canLoadMore = true,
                    searchQuery = query ?: ""
                )
            }
            val result = inventoryRepository.getInventoryItemGroup(page = 1, pageSize = ITEM_GROUP_PAGE_SIZE, search = query)
            result.onSuccess { response ->
                val safeList = response.groups
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        itemGroups = safeList,
                        filteredList = safeList,
                        canLoadMore = safeList.size >= ITEM_GROUP_PAGE_SIZE,
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

    fun loadMoreItemGroups() {
        val currentState = _uiState.value
        if (currentState.isLoadingMore || currentState.isLoading || !currentState.canLoadMore) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            val nextPage = currentState.currentPage + 1

            val result = inventoryRepository.getInventoryItemGroup(
                page = nextPage,
                pageSize = ITEM_GROUP_PAGE_SIZE,
                search = currentState.searchQuery.takeIf { it.isNotBlank() }
            )

            result.onSuccess { response ->
                val newGroups = response.groups
                _uiState.update { state ->
                    val combinedList = (state.itemGroups + newGroups).distinct()
                    state.copy(
                        isLoadingMore = false,
                        currentPage = nextPage,
                        itemGroups = combinedList,
                        filteredList = combinedList,
                        canLoadMore = newGroups.size >= ITEM_GROUP_PAGE_SIZE
                    )
                }
            }.onFailure {
                _uiState.update { it.copy(isLoadingMore = false) }
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
            loadItemGroups(query = newQuery.takeIf { it.isNotBlank() })
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

            request.variantAttributes.forEachIndexed { index, attr ->
                params["variantAttributes[$index][name]"] = attr.name.toRequestBody(textMedia)
                attr.values.forEachIndexed { valIndex, value ->
                    params["variantAttributes[$index][values][$valIndex]"] = value.toRequestBody(textMedia)
                }
            }

            request.categoryId?.takeIf { it.isNotBlank() }?.let { params["categoryId"] = it.toRequestBody(textMedia) }
            request.brand?.takeIf { it.isNotBlank() }?.let { params["brand"] = it.toRequestBody(textMedia) }
            request.shortDescription?.takeIf { it.isNotBlank() }?.let { params["shortDescription"] = it.toRequestBody(textMedia) }
            request.longDescription?.takeIf { it.isNotBlank() }?.let { params["longDescription"] = it.toRequestBody(textMedia) }

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

            request.variantAttributes.forEachIndexed { index, attr ->
                params["variantAttributes[$index][name]"] = attr.name.toRequestBody(textMedia)
                attr.values.forEachIndexed { valIndex, value ->
                    params["variantAttributes[$index][values][$valIndex]"] = value.toRequestBody(textMedia)
                }
            }

            request.categoryId?.takeIf { it.isNotBlank() }?.let { params["categoryId"] = it.toRequestBody(textMedia) }
            request.brand?.takeIf { it.isNotBlank() }?.let { params["brand"] = it.toRequestBody(textMedia) }
            request.shortDescription?.takeIf { it.isNotBlank() }?.let { params["shortDescription"] = it.toRequestBody(textMedia) }
            request.longDescription?.takeIf { it.isNotBlank() }?.let { params["longDescription"] = it.toRequestBody(textMedia) }

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

    fun clearSelectedItemGroupDetail() { _selectedItemGroupDetail.value = null }
    fun clearDeleteSuccessMessage() { _deleteItemGroupSuccess.value = null }
    fun clearItemGroupAlerts() {
        _createItemGroupError.value = null
        _createItemGroupSuccess.value = null
    }
    fun refreshItemGroups() {
        loadItemGroups(query = _uiState.value.searchQuery.takeIf { it.isNotBlank() })
    }

    // =========================================================================
    // 2. INVENTORY ITEMS (INFINITE SCROLL ENABLED)
    // =========================================================================

    fun fetchInventoryItems(
        page: Int = 1,
        limit: Int = DEFAULT_LIMIT,
        search: String? = null,
        status: String? = null
    ) {
        fetchInventoryJob?.cancel()
        fetchInventoryJob = launchBusy {
            _isLoadingInventoryItems.value = true
            _inventoryError.value = null
            _currentInventoryPage.value = page
            _canLoadMoreInventoryItems.value = true
            activeInventorySearch = search
            activeInventoryStatus = status

            val result = inventoryRepository.getInventoryItems(page, limit, search, status)
            result.fold(
                onSuccess = { response ->
                    val newItems = response.data
                    val pagination = response.pagination
                    val totalPages = pagination?.totalPages ?: 1

                    _inventoryItems.value = newItems
                    _inventoryPagination.value = pagination
                    _canLoadMoreInventoryItems.value = page < totalPages && newItems.isNotEmpty()
                },
                onFailure = { e ->
                    if (e !is CancellationException) {
                        _inventoryError.value = extractErrorMessage(e.message)
                    }
                }
            )
            _isLoadingInventoryItems.value = false
        }
    }

    fun loadMoreInventoryItems(limit: Int = DEFAULT_LIMIT) {
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
                        _inventoryItems.update { (it + newItems).distinct() }
                        _currentInventoryPage.value = nextPage
                        _inventoryPagination.value = pagination

                        val totalPages = pagination?.totalPages ?: nextPage
                        _canLoadMoreInventoryItems.value = nextPage < totalPages
                    } else {
                        _canLoadMoreInventoryItems.value = false
                    }
                },
                onFailure = { e ->
                    _inventoryError.value = extractErrorMessage(e.message)
                }
            )
            _isLoadingMoreInventoryItems.value = false
        }
    }

    fun refreshInventoryItems() {
        fetchInventoryItems(page = 1, search = activeInventorySearch, status = activeInventoryStatus)
    }

    fun clearInventoryError() { _inventoryError.value = null }

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
                onFailure = { e -> _viewOneError.value = extractErrorMessage(e.message) }
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
                onFailure = { e -> _itemDetailError.value = extractErrorMessage(e.message) }
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
                onFailure = { e -> _recentItemsError.value = extractErrorMessage(e.message) }
            )
            _isLoadingRecentItems.value = false
        }
    }

    // =========================================================================
    // 3. STOCK ADJUSTMENT ACTIONS (INFINITE SCROLL ENABLED)
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
        adjustmentType: String? = _selectedAdjustmentType.value,
        search: String? = null
    ) {
        viewModelScope.launch {
            _isLoadingAdjustments.value = true
            _adjustmentErrorMessage.value = null
            _selectedAdjustmentType.value = adjustmentType
            activeAdjustmentSearch = search

            val targetPage = 1
            _currentAdjustmentPage.value = targetPage
            _canLoadMoreAdjustments.value = true

            inventoryRepository.getStockAdjustments(
                page = targetPage,
                limit = DEFAULT_LIMIT,
                adjustmentType = adjustmentType,
                search = search
            ).fold(
                onSuccess = { response ->
                    _stockAdjustmentsList.value = response.data
                    _totalAdjustments.value = response.totalCount
                    _canLoadMoreAdjustments.value = targetPage < response.totalPagesCount
                },
                onFailure = { error ->
                    _adjustmentErrorMessage.value = extractErrorMessage(error.message)
                }
            )
            _isLoadingAdjustments.value = false
        }
    }

    fun loadMoreStockAdjustments() {
        if (_isLoadingMoreAdjustments.value || _isLoadingAdjustments.value || !_canLoadMoreAdjustments.value) return

        viewModelScope.launch {
            _isLoadingMoreAdjustments.value = true
            val nextPage = _currentAdjustmentPage.value + 1

            inventoryRepository.getStockAdjustments(
                page = nextPage,
                limit = DEFAULT_LIMIT,
                adjustmentType = _selectedAdjustmentType.value,
                search = activeAdjustmentSearch
            ).fold(
                onSuccess = { response ->
                    val newItems = response.data
                    if (newItems.isNotEmpty()) {
                        _stockAdjustmentsList.update { (it + newItems).distinct() }
                        _currentAdjustmentPage.value = nextPage
                        _totalAdjustments.value = response.totalCount
                        _canLoadMoreAdjustments.value = nextPage < response.totalPagesCount
                    } else {
                        _canLoadMoreAdjustments.value = false
                    }
                },
                onFailure = { error ->
                    _adjustmentErrorMessage.value = extractErrorMessage(error.message)
                }
            )
            _isLoadingMoreAdjustments.value = false
        }
    }

    fun submitIncreaseStock(request: IncreaseStockRequest, onSuccess: (StockAdjustmentData) -> Unit = {}) {
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

    fun submitDecreaseStock(request: DecreaseStockRequest, onSuccess: (StockAdjustmentData) -> Unit = {}) {
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

    fun submitStockTransfer(request: TransferStockRequest, onSuccess: (StockAdjustmentData) -> Unit = {}) {
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

    fun loadMoreStockSummaryList(limit: Int = 20, search: String? = null) {
        if (_isLoadingMoreStockSummary.value || _isLoadingStockSummary.value || !_canLoadMoreStockSummary.value) return

        viewModelScope.launch {
            _isLoadingMoreStockSummary.value = true
            val nextPage = _currentStockSummaryPage.value + 1

            val result = inventoryRepository.getStockSummaryList(page = nextPage, limit = limit, search = search)
            result.onSuccess { response ->
                val newItems = response.data
                if (newItems.isNotEmpty()) {
                    _stockSummaryList.update { (it + newItems).distinct() }
                    _currentStockSummaryPage.value = nextPage
                    _canLoadMoreStockSummary.value = newItems.size >= limit
                } else {
                    _canLoadMoreStockSummary.value = false
                }
            }.onFailure {
                _isLoadingMoreStockSummary.value = false
            }
            _isLoadingMoreStockSummary.value = false
        }
    }

    fun clearStockSummaryAlerts() { _stockSummaryError.value = null }

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
    // 4. LOW STOCK ALERTS & PURCHASE ORDER ACTIONS
    // =========================================================================

    fun fetchLowStockAlerts(warehouseId: String? = null, limit: Int = 10) {
        launchBusy {
            _isLoadingLowStock.value = true
            _lowStockError.value = null
            _currentLowStockPage.value = 1
            _canLoadMoreLowStock.value = true
            activeLowStockWarehouseId = warehouseId

            val result = inventoryRepository.getLowStockAlerts(warehouseId)
            _isLoadingLowStock.value = false

            result.onSuccess { list ->
                _lowStockItems.value = list
                _canLoadMoreLowStock.value = list.size >= limit
            }.onFailure { error ->
                _lowStockError.value = extractErrorMessage(error.message)
            }
        }
    }

    fun loadMoreLowStockAlerts(limit: Int = 10) {
        if (_isLoadingMoreLowStock.value || _isLoadingLowStock.value || !_canLoadMoreLowStock.value) return

        viewModelScope.launch {
            _isLoadingMoreLowStock.value = true
            val nextPage = _currentLowStockPage.value + 1

            val result = inventoryRepository.getLowStockAlerts(activeLowStockWarehouseId)
            result.onSuccess { newItems ->
                val currentList = _lowStockItems.value
                val existingIds = currentList.map { it.itemId }.toSet()
                val distinctNewItems = newItems.filter { it.itemId !in existingIds }

                if (distinctNewItems.isNotEmpty()) {
                    _lowStockItems.value = currentList + distinctNewItems
                    _currentLowStockPage.value = nextPage
                    _canLoadMoreLowStock.value = newItems.size >= limit
                } else {
                    _canLoadMoreLowStock.value = false
                }
            }.onFailure {
                _canLoadMoreLowStock.value = false
            }

            _isLoadingMoreLowStock.value = false
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

            val poItem = CreatePoItemRequest(itemId = itemId, qty = qty, rate = rate, taxPercent = 18.0)
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
                _reorderDetailError.value = extractErrorMessage(error.message)
            }
        }
    }

    fun setReorderItemDirectly(item: LowStockItemDto?) { _reorderItemDetail.value = item }
    fun clearReorderItemDetail() {
        _reorderItemDetail.value = null
        _reorderDetailError.value = null
    }

    // =========================================================================
    // 5. CREATE / EDIT ITEM FORM ACTIONS
    // =========================================================================

    fun toggleSection(section: ItemSection) { _expandedSection.value = section }
    fun updateCreateItemForm(transform: (CreateItemFormState) -> CreateItemFormState) { _createItemForm.update(transform) }
    fun onImageSelected(uri: Uri) { _createItemForm.update { it.copy(imageUri = uri) } }

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

            val params = mutableMapOf(
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

            form.parentGroupId?.takeIf { it.isNotBlank() }?.let { params["parentGroupId"] = it.toRequestBody(textMedia) }
            form.category.takeIf { it.isNotBlank() }?.let { params["categoryId"] = it.toRequestBody(textMedia) }

            val imagePart = inventoryRepository.prepareImagePart(context, form.imageUri)
            val result = inventoryRepository.createItem(params, imagePart)

            result.fold(
                onSuccess = { response ->
                    _createItemUiState.value = CreateItemUiState.Success(response)
                },
                onFailure = { error ->
                    _createItemUiState.value = CreateItemUiState.Error(extractErrorMessage(error.message))
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

            val params = mutableMapOf(
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

            form.category.takeIf { it.isNotBlank() }?.let { params["categoryId"] = it.toRequestBody(textMedia) }

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
                    _createItemUiState.value = CreateItemUiState.Error(extractErrorMessage(error.message))
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
                fetchInventoryItems()
                onSuccess()
            }.onFailure { error ->
                _inventoryError.value = extractErrorMessage(error.message)
            }
        }
    }

    // =========================================================================
    // 6. WAREHOUSE MANAGEMENT ACTIONS
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
    // 7. SUPPLIERS ACTIONS (INFINITE SCROLL ENABLED)
    // =========================================================================

    fun fetchSuppliers(
        page: Int = 1,
        limit: Int = SUPPLIER_PAGE_SIZE,
        search: String? = null,
        status: String? = null
    ) {
        viewModelScope.launch {
            _isLoadingSuppliers.value = true
            _suppliersError.value = null
            _currentSupplierPage.value = page
            _canLoadMoreSuppliers.value = true
            activeSupplierSearch = search
            activeSupplierStatus = status

            inventoryRepository.getAllSuppliers(page, limit, search, status)
                .onSuccess { list ->
                    _suppliers.value = list
                    _canLoadMoreSuppliers.value = list.size >= limit
                }
                .onFailure { error ->
                    _suppliersError.value = extractErrorMessage(error.message)
                }
            _isLoadingSuppliers.value = false
        }
    }

    fun loadMoreSuppliers(limit: Int = SUPPLIER_PAGE_SIZE) {
        if (_isLoadingMoreSuppliers.value || _isLoadingSuppliers.value || !_canLoadMoreSuppliers.value) return

        viewModelScope.launch {
            _isLoadingMoreSuppliers.value = true
            val nextPage = _currentSupplierPage.value + 1

            inventoryRepository.getAllSuppliers(nextPage, limit, activeSupplierSearch, activeSupplierStatus)
                .onSuccess { newSuppliers ->
                    if (newSuppliers.isNotEmpty()) {
                        _suppliers.update { (it + newSuppliers).distinct() }
                        _currentSupplierPage.value = nextPage
                        _canLoadMoreSuppliers.value = newSuppliers.size >= limit
                    } else {
                        _canLoadMoreSuppliers.value = false
                    }
                }
                .onFailure { error ->
                    _suppliersError.value = extractErrorMessage(error.message)
                }
            _isLoadingMoreSuppliers.value = false
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
    // 8. BULK ITEMS
    // =========================================================================

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
        _bulkError.value = null
        _bulkSuccessMessage.value = null
    }

    fun clearBulkError() { _bulkError.value = null }
    fun clearBulkSuccessMessage() { _bulkSuccessMessage.value = null }

    fun fetchBulkItems(page: Int = 1, limit: Int = 10, search: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _bulkError.value = null
            _currentBulkPage.value = page
            _canLoadMoreBulk.value = true
            activeBulkSearch = search

            inventoryRepository.getBulkItems(page = page, limit = limit, search = search)
                .onSuccess { list ->
                    _bulkItems.value = list
                    _canLoadMoreBulk.value = list.size >= limit
                }
                .onFailure {
                    val msg = extractErrorMessage(it.message)
                    _errorMessage.value = msg
                    _bulkError.value = msg
                }
            _isLoading.value = false
        }
    }

    fun loadMoreBulkItems(limit: Int = 10) {
        if (_isLoadingMoreBulk.value || _isLoading.value || !_canLoadMoreBulk.value) return

        viewModelScope.launch {
            _isLoadingMoreBulk.value = true
            val nextPage = _currentBulkPage.value + 1

            inventoryRepository.getBulkItems(page = nextPage, limit = limit, search = activeBulkSearch)
                .onSuccess { newItems ->
                    val currentList = _bulkItems.value
                    val existingIds = currentList.map { it.id }.toSet()
                    val distinctNewItems = newItems.filter { it.id !in existingIds }

                    if (distinctNewItems.isNotEmpty()) {
                        _bulkItems.value = currentList + distinctNewItems
                        _currentBulkPage.value = nextPage
                        _canLoadMoreBulk.value = newItems.size >= limit
                    } else {
                        _canLoadMoreBulk.value = false
                    }
                }
                .onFailure {
                    _canLoadMoreBulk.value = false
                }
            _isLoadingMoreBulk.value = false
        }
    }

    fun fetchBulkItemDetail(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            inventoryRepository.getBulkItemById(id)
                .onSuccess { _selectedBulkItem.value = it }
                .onFailure { _errorMessage.value = extractErrorMessage(it.message) }
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
                _errorMessage.value = extractErrorMessage(it.message)
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
                _errorMessage.value = extractErrorMessage(it.message)
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
                .onFailure { _errorMessage.value = extractErrorMessage(it.message) }
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
                .onFailure { _errorMessage.value = extractErrorMessage(it.message) }
            _isLoading.value = false
        }
    }

    // =========================================================================
    // 9. PURCHASE ORDERS (INFINITE SCROLL ENABLED)
    // =========================================================================

    fun fetchAllPurchaseOrders(
        search: String? = null,
        status: String? = null
    ) {
        viewModelScope.launch {
            _isLoadingPurchaseOrders.value = true
            _purchaseOrdersError.value = null
            _currentPOPage.value = 1
            _canLoadMorePurchaseOrders.value = true
            activePOSearch = search
            activePOStatus = status

            inventoryRepository.getAllPurchaseOrders(page = 1, limit = PO_PAGE_SIZE, search = search, status = status)
                .onSuccess { list ->
                    _purchaseOrdersList.value = list
                    _canLoadMorePurchaseOrders.value = list.size >= PO_PAGE_SIZE
                }
                .onFailure { error ->
                    _purchaseOrdersError.value = extractErrorMessage(error.message)
                }

            _isLoadingPurchaseOrders.value = false
        }
    }

    fun loadMorePurchaseOrders() {
        if (_isLoadingMorePurchaseOrders.value || _isLoadingPurchaseOrders.value || !_canLoadMorePurchaseOrders.value) return

        viewModelScope.launch {
            _isLoadingMorePurchaseOrders.value = true
            val nextPage = _currentPOPage.value + 1

            inventoryRepository.getAllPurchaseOrders(page = nextPage, limit = PO_PAGE_SIZE, search = activePOSearch, status = activePOStatus)
                .onSuccess { newOrders ->
                    if (newOrders.isNotEmpty()) {
                        _purchaseOrdersList.update { (it + newOrders).distinct() }
                        _currentPOPage.value = nextPage
                        _canLoadMorePurchaseOrders.value = newOrders.size >= PO_PAGE_SIZE
                    } else {
                        _canLoadMorePurchaseOrders.value = false
                    }
                }
                .onFailure { error ->
                    _purchaseOrdersError.value = extractErrorMessage(error.message)
                }

            _isLoadingMorePurchaseOrders.value = false
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

    fun clearBillConvertDetail() { _billConvertDetail.value = null }

    // =========================================================================
    // 10. REQUISITION ACTIONS (INFINITE SCROLL ENABLED)
    // =========================================================================

    fun fetchAllRequisitions(
        search: String? = null,
        status: String? = null
    ) {
        viewModelScope.launch {
            _isLoadingRequisitions.value = true
            _requisitionErrorMessage.value = null
            _currentRequisitionPage.value = 1
            _canLoadMoreRequisitions.value = true
            activeReqSearch = search
            activeReqStatus = status

            inventoryRepository.getAllRequisitions(page = 1, limit = REQ_PAGE_SIZE, search = search, status = status)
                .onSuccess { list ->
                    _requisitionsList.value = list
                    _canLoadMoreRequisitions.value = list.size >= REQ_PAGE_SIZE
                }
                .onFailure { error ->
                    _requisitionErrorMessage.value = extractErrorMessage(error.message)
                }

            _isLoadingRequisitions.value = false
        }
    }

    fun loadMoreRequisitions() {
        if (_isLoadingMoreRequisitions.value || _isLoadingRequisitions.value || !_canLoadMoreRequisitions.value) return

        viewModelScope.launch {
            _isLoadingMoreRequisitions.value = true
            val nextPage = _currentRequisitionPage.value + 1

            inventoryRepository.getAllRequisitions(page = nextPage, limit = REQ_PAGE_SIZE, search = activeReqSearch, status = activeReqStatus)
                .onSuccess { newRequisitions ->
                    if (newRequisitions.isNotEmpty()) {
                        _requisitionsList.update { (it + newRequisitions).distinct() }
                        _currentRequisitionPage.value = nextPage
                        _canLoadMoreRequisitions.value = newRequisitions.size >= REQ_PAGE_SIZE
                    } else {
                        _canLoadMoreRequisitions.value = false
                    }
                }
                .onFailure { error ->
                    _requisitionErrorMessage.value = extractErrorMessage(error.message)
                }

            _isLoadingMoreRequisitions.value = false
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

    /**
     * Submits an existing requisition directly for approval workflow.
     */
    fun submitForApproval(
        id: String,
        remarks: String? = null,
        onSuccess: (PurchaseRequisition) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isSubmittingRequisition.value = true
            _requisitionErrorMessage.value = null
            _requisitionSuccessMessage.value = null

            inventoryRepository.submitForApproval(id, remarks)
                .onSuccess { updated ->
                    _selectedRequisition.value = updated
                    _requisitionSuccessMessage.value = "Requisition submitted for approval"
                    fetchAllRequisitions()
                    onSuccess(updated)
                }
                .onFailure { error ->
                    _requisitionErrorMessage.value = extractErrorMessage(error.message)
                }

            _isSubmittingRequisition.value = false
        }
    }

    /**
     * Creates a new requisition and immediately chains a submission for approval.
     */
    fun createAndSubmitRequisitionForApproval(
        request: CreateRequisitionRequest,
        onSuccess: (PurchaseRequisition) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isSubmittingRequisition.value = true
            _requisitionErrorMessage.value = null
            _requisitionSuccessMessage.value = null

            val createResult = inventoryRepository.createRequisition(request)
            createResult.fold(
                onSuccess = { created ->
                    val reqId = created.id
                    if (!reqId.isNullOrBlank()) {
                        val submitResult = inventoryRepository.submitForApproval(reqId, created.justification)
                        submitResult.fold(
                            onSuccess = { approvedOrSubmitted ->
                                _requisitionSuccessMessage.value = "Requisition submitted for approval (${approvedOrSubmitted.prNumber ?: created.prNumber})"
                                fetchAllRequisitions()
                                onSuccess(approvedOrSubmitted)
                            },
                            onFailure = { submitError ->
                                _requisitionSuccessMessage.value = "Requisition created (${created.prNumber}), but submission failed"
                                _requisitionErrorMessage.value = extractErrorMessage(submitError.message)
                                fetchAllRequisitions()
                                onSuccess(created)
                            }
                        )
                    } else {
                        _requisitionSuccessMessage.value = "Requisition created successfully"
                        fetchAllRequisitions()
                        onSuccess(created)
                    }
                },
                onFailure = { error ->
                    _requisitionErrorMessage.value = extractErrorMessage(error.message)
                }
            )

            _isSubmittingRequisition.value = false
        }
    }

    fun updateRequisition(
        id: String,
        request: CreateRequisitionRequest,
        onSuccess: (PurchaseRequisition) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isSubmittingRequisition.value = true
            _requisitionErrorMessage.value = null
            _requisitionSuccessMessage.value = null

            inventoryRepository.updateRequisition(id, request)
                .onSuccess { updated ->
                    _requisitionSuccessMessage.value = "Requisition updated successfully"
                    fetchAllRequisitions()
                    onSuccess(updated)
                }
                .onFailure { error ->
                    _requisitionErrorMessage.value = extractErrorMessage(error.message)
                }

            _isSubmittingRequisition.value = false
        }
    }

    fun updateAndSubmitRequisitionForApproval(
        id: String,
        request: CreateRequisitionRequest,
        onSuccess: (PurchaseRequisition) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isSubmittingRequisition.value = true
            _requisitionErrorMessage.value = null
            _requisitionSuccessMessage.value = null

            val updateResult = inventoryRepository.updateRequisition(id, request)
            updateResult.fold(
                onSuccess = { updated ->
                    val submitResult = inventoryRepository.submitForApproval(id, updated.justification)
                    submitResult.fold(
                        onSuccess = { approved ->
                            _requisitionSuccessMessage.value = "Requisition updated and submitted for approval"
                            fetchAllRequisitions()
                            onSuccess(approved)
                        },
                        onFailure = { submitError ->
                            _requisitionSuccessMessage.value = "Requisition updated, but submission failed"
                            _requisitionErrorMessage.value = extractErrorMessage(submitError.message)
                            fetchAllRequisitions()
                            onSuccess(updated)
                        }
                    )
                },
                onFailure = { error ->
                    _requisitionErrorMessage.value = extractErrorMessage(error.message)
                }
            )

            _isSubmittingRequisition.value = false
        }
    }

    /**
     * Actions requisition approval (Approve / Reject).
     */
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

            val remarkPayload = remarks ?: "Requisition marked as $status"
            inventoryRepository.submitForApproval(id, remarkPayload)
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

    /**
     * Soft-deletes a requisition by its ID.
     */
    fun deleteRequisition(id: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isSubmittingRequisition.value = true
            _requisitionErrorMessage.value = null
            _requisitionSuccessMessage.value = null

            inventoryRepository.deleteRequisition(id)
                .onSuccess { message ->
                    _requisitionSuccessMessage.value = message
                    fetchAllRequisitions()
                    onSuccess()
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

    private val _isSubmittingComment = MutableStateFlow(false)
    val isSubmittingComment: StateFlow<Boolean> = _isSubmittingComment.asStateFlow()

    fun addRequisitionComment(
        requisitionId: String,
        commentText: String,
        onSuccess: () -> Unit = {}
    ) {
        if (commentText.isBlank()) return

        viewModelScope.launch {
            _isSubmittingComment.value = true
            _requisitionErrorMessage.value = null
            _requisitionSuccessMessage.value = null

            val result = inventoryRepository.addRequisitionComment(
                requisitionId = requisitionId,
                commentText = commentText.trim()
            )

            result.onSuccess { updatedRequisition ->
                _selectedRequisition.value = updatedRequisition
                _requisitionSuccessMessage.value = "Comment added successfully"
                onSuccess()
            }.onFailure { error ->
                _requisitionErrorMessage.value = extractErrorMessage(error.message)
            }

            _isSubmittingComment.value = false
        }
    }

    // =========================================================================
    // 11. BARCODE ACTIONS
    // =========================================================================

    fun fetchAllBarcodes(
        page: Int = 1,
        limit: Int = 10,
        search: String? = null,
        status: String? = null
    ) {
        viewModelScope.launch {
            _isLoadingBarcodes.value = true
            _barcodeErrorMessage.value = null
            _currentBarcodePage.value = page
            _canLoadMoreBarcodes.value = true
            activeBarcodeSearch = search
            activeBarcodeStatus = status

            inventoryRepository.getAllBarcodes(search = search, status = status)
                .onSuccess { list ->
                    _barcodesList.value = list
                    _canLoadMoreBarcodes.value = list.size >= limit
                }
                .onFailure { error ->
                    _barcodeErrorMessage.value = extractErrorMessage(error.message)
                }

            _isLoadingBarcodes.value = false
        }
    }

    fun loadMoreBarcodes(limit: Int = 10) {
        if (_isLoadingMoreBarcodes.value || _isLoadingBarcodes.value || !_canLoadMoreBarcodes.value) return

        viewModelScope.launch {
            _isLoadingMoreBarcodes.value = true
            val nextPage = _currentBarcodePage.value + 1

            inventoryRepository.getAllBarcodes(search = activeBarcodeSearch, status = activeBarcodeStatus)
                .onSuccess { newItems ->
                    val currentList = _barcodesList.value
                    val existingIds = currentList.map { it.id }.toSet()
                    val distinctNewItems = newItems.filter { it.id !in existingIds }

                    if (distinctNewItems.isNotEmpty()) {
                        _barcodesList.value = currentList + distinctNewItems
                        _currentBarcodePage.value = nextPage
                        _canLoadMoreBarcodes.value = newItems.size >= limit
                    } else {
                        _canLoadMoreBarcodes.value = false
                    }
                }
                .onFailure {
                    _canLoadMoreBarcodes.value = false
                }

            _isLoadingMoreBarcodes.value = false
        }
    }

    fun fetchBarcodeViewOne(id: String, onLoaded: (BarcodeItemDoc) -> Unit = {}) {
        viewModelScope.launch {
            _isLoadingBarcodes.value = true
            _barcodeErrorMessage.value = null

            inventoryRepository.getBarcodeViewOne(id)
                .onSuccess { doc ->
                    _selectedBarcode.value = doc
                    onLoaded(doc)
                }
                .onFailure { error -> _barcodeErrorMessage.value = extractErrorMessage(error.message) }

            _isLoadingBarcodes.value = false
        }
    }

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
                .onFailure { error -> _barcodeErrorMessage.value = extractErrorMessage(error.message) }

            _isSubmittingBarcode.value = false
        }
    }

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
                .onFailure { error -> _barcodeErrorMessage.value = extractErrorMessage(error.message) }

            _isSubmittingBarcode.value = false
        }
    }

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
                .onFailure { error -> _barcodeErrorMessage.value = extractErrorMessage(error.message) }

            _isSubmittingBarcode.value = false
        }
    }

    fun clearBarcodeAlerts() {
        _barcodeSuccessMessage.value = null
        _barcodeErrorMessage.value = null
    }

    fun clearSelectedBarcode() { _selectedBarcode.value = null }

    // =========================================================================
    // 12. PURCHASE RECEIVE ACTIONS (SINGLE DEFINITIONS)
    // =========================================================================

    fun fetchAllReceives(search: String? = null) {
        viewModelScope.launch {
            _isLoadingReceives.value = true
            _errorMessage.value = null
            _currentReceivesPage.value = 1
            _canLoadMoreReceives.value = true
            activeReceivesSearch = search

            inventoryRepository.getAllReceives(page = 1, limit = RECEIVE_PAGE_SIZE, search = search)
                .onSuccess { list ->
                    _allReceives.value = list
                    _canLoadMoreReceives.value = list.size >= RECEIVE_PAGE_SIZE
                }
                .onFailure { _errorMessage.value = extractErrorMessage(it.message) }

            _isLoadingReceives.value = false
        }
    }

    fun loadMoreReceives() {
        if (_isLoadingMoreReceives.value || _isLoadingReceives.value || !_canLoadMoreReceives.value) return

        viewModelScope.launch {
            _isLoadingMoreReceives.value = true
            val nextPage = _currentReceivesPage.value + 1

            inventoryRepository.getAllReceives(page = nextPage, limit = RECEIVE_PAGE_SIZE, search = activeReceivesSearch)
                .onSuccess { newReceives ->
                    if (newReceives.isNotEmpty()) {
                        _allReceives.update { (it + newReceives).distinct() }
                        _currentReceivesPage.value = nextPage
                        _canLoadMoreReceives.value = newReceives.size >= RECEIVE_PAGE_SIZE
                    } else {
                        _canLoadMoreReceives.value = false
                    }
                }
                .onFailure { _errorMessage.value = extractErrorMessage(it.message) }

            _isLoadingMoreReceives.value = false
        }
    }

    fun fetchReceiveHistoryByPo(poId: String) {
        if (poId.isBlank()) return
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            inventoryRepository.getReceiveHistoryByPo(poId)
                .onSuccess { historyResponse ->
                    _poHistory.value = historyResponse
                }
                .onFailure { error ->
                    _errorMessage.value = extractErrorMessage(error.message)
                }

            _isLoading.value = false
        }
    }

    fun fetchPaymentTerms() {
        launchBusy {
            _isLoadingPaymentTerms.value = true
            val result = inventoryRepository.getPaymentTerms()
            result.fold(
                onSuccess = { _paymentTerms.value = it },
                onFailure = { /* Fail silently or handle error */ }
            )
            _isLoadingPaymentTerms.value = false
        }
    }

    fun fetchTaxGroups() {
        launchBusy {
            _isLoadingTaxGroups.value = true
            val result = inventoryRepository.getTaxGroups()
            result.fold(
                onSuccess = { _taxGroups.value = it },
                onFailure = { /* Fail silently or handle error */ }
            )
            _isLoadingTaxGroups.value = false
        }
    }
    /**
     * Creates and submits a new bill for the purchase receive.
     */
    fun submitCreateBill(
        request: CreateBillRequest,
        onSuccess: (BillResponseData) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isCreatingBill.value = true
            _createBillErrorMessage.value = null
            _createBillSuccessMessage.value = null

            val result = inventoryRepository.createBill(request)
            _isCreatingBill.value = false

            result.onSuccess { data ->
                _createBillSuccessMessage.value = "Bill created successfully (${data.billNumber})"
                onSuccess(data)
            }.onFailure { error ->
                _createBillErrorMessage.value = extractErrorMessage(error.message)
            }
        }
    }

    fun clearBillAlerts() {
        _createBillSuccessMessage.value = null
        _createBillErrorMessage.value = null
    }

    fun fetchSingleReceive(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            inventoryRepository.getSingleReceive(id)
                .onSuccess { _singleReceive.value = it }
                .onFailure { _errorMessage.value = extractErrorMessage(it.message) }
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
                .onFailure { _errorMessage.value = extractErrorMessage(it.message) }
            _isLoading.value = false
        }
    }

    // =========================================================================
    // VIEW MULTIPLE RECEIVES STATE & ACTIONS
    // =========================================================================
    private val _multipleReceivesData = MutableStateFlow<List<PurchaseReceiveItem>>(emptyList())
    val multipleReceivesData: StateFlow<List<PurchaseReceiveItem>> = _multipleReceivesData.asStateFlow()

    private val _isLoadingMultipleReceives = MutableStateFlow(false)
    val isLoadingMultipleReceives: StateFlow<Boolean> = _isLoadingMultipleReceives.asStateFlow()

    fun fetchMultipleReceives(
        receiveIds: List<String>,
        onSuccess: (List<PurchaseReceiveItem>) -> Unit = {}
    ) {
        if (receiveIds.isEmpty()) return
        viewModelScope.launch {
            _isLoadingMultipleReceives.value = true
            _errorMessage.value = null

            inventoryRepository.viewMultipleReceives(receiveIds)
                .onSuccess { dataList ->
                    _multipleReceivesData.value = dataList
                    onSuccess(dataList)
                }
                .onFailure { error ->
                    _errorMessage.value = extractErrorMessage(error.message)
                }

            _isLoadingMultipleReceives.value = false
        }
    }

    fun clearMultipleReceivesData() {
        _multipleReceivesData.value = emptyList()
    }

    fun fetchPurchaseOrderSummary(page: Int = 1, limit: Int = 10, search: String? = null) {
        viewModelScope.launch {
            _isLoadingPoSummary.value = true
            _poSummaryError.value = null
            _currentPoSummaryPage.value = page
            _canLoadMorePoSummary.value = true
            activePoSummarySearch = search

            inventoryRepository.getPurchaseOrderSummary(page, limit, search)
                .onSuccess { response ->
                    _poSummaryList.value = response.data
                    val totalPages = response.pagination?.totalPages ?: 1
                    _canLoadMorePoSummary.value = page < totalPages && response.data.isNotEmpty()
                }
                .onFailure { error ->
                    _poSummaryError.value = extractErrorMessage(error.message)
                }

            _isLoadingPoSummary.value = false
        }
    }

    fun loadMorePurchaseOrderSummary(limit: Int = 10) {
        if (_isLoadingMorePoSummary.value || _isLoadingPoSummary.value || !_canLoadMorePoSummary.value) return

        viewModelScope.launch {
            _isLoadingMorePoSummary.value = true
            val nextPage = _currentPoSummaryPage.value + 1

            inventoryRepository.getPurchaseOrderSummary(nextPage, limit, activePoSummarySearch)
                .onSuccess { response ->
                    val newItems = response.data
                    if (newItems.isNotEmpty()) {
                        _poSummaryList.update { (it + newItems).distinctBy { item -> item.poId } }
                        _currentPoSummaryPage.value = nextPage
                        val totalPages = response.pagination?.totalPages ?: nextPage
                        _canLoadMorePoSummary.value = nextPage < totalPages
                    } else {
                        _canLoadMorePoSummary.value = false
                    }
                }
                .onFailure {
                    _canLoadMorePoSummary.value = false
                }

            _isLoadingMorePoSummary.value = false
        }
    }

    fun clearErrors() { _errorMessage.value = null }

    // =========================================================================
    // PRIVATE UTILITY FUNCTIONS
    // =========================================================================

    fun generateSkuFromName(name: String): String {
        if (name.isBlank()) return ""
        val tokens = name.split(Regex("[\\s\\-/]+")).filter { it.isNotBlank() }

        return when {
            tokens.isEmpty() -> ""
            tokens.size == 1 -> tokens[0].take(3).uppercase()
            else -> {
                val prefix = tokens[0].take(3).uppercase()
                val remaining = tokens.drop(1)
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

    // =============================================================================
    // LOCATION MANAGEMENT
    // =============================================================================

    private val _selectedStockLocationItem = MutableStateFlow<StockLocationItemDto?>(null)
    val selectedStockLocationItem: StateFlow<StockLocationItemDto?> = _selectedStockLocationItem.asStateFlow()

    fun setSelectedStockLocationItem(item: StockLocationItemDto?) {
        _selectedStockLocationItem.value = item
    }

    fun fetchStockLocationItems(
        page: Int = 1,
        limit: Int = 10,
        search: String? = null,
        status: String? = null
    ) {
        viewModelScope.launch {
            _isLoadingStockLocations.value = true
            _stockLocationError.value = null
            _currentStockLocationPage.value = page
            _canLoadMoreStockLocations.value = true
            activeStockLocationSearch = search
            activeStockLocationStatus = status

            val result = inventoryRepository.getStockLocationItems(
                page = page,
                limit = limit,
                search = search,
                status = status
            )

            result.onSuccess { response ->
                _stockLocationItems.value = response.data
                _stockLocationTotal.value = response.total
                _canLoadMoreStockLocations.value = page < response.totalPages
            }.onFailure { error ->
                _stockLocationError.value = extractErrorMessage(error.message)
            }

            _isLoadingStockLocations.value = false
        }
    }

    fun loadMoreStockLocationItems(limit: Int = 10) {
        if (_isLoadingMoreStockLocations.value || _isLoadingStockLocations.value || !_canLoadMoreStockLocations.value) return

        viewModelScope.launch {
            _isLoadingMoreStockLocations.value = true
            val nextPage = _currentStockLocationPage.value + 1

            val result = inventoryRepository.getStockLocationItems(
                page = nextPage,
                limit = limit,
                search = activeStockLocationSearch,
                status = activeStockLocationStatus
            )

            result.onSuccess { response ->
                val newItems = response.data
                if (newItems.isNotEmpty()) {
                    _stockLocationItems.update { (it + newItems).distinctBy { item -> item.id } }
                    _currentStockLocationPage.value = nextPage
                    _canLoadMoreStockLocations.value = nextPage < response.totalPages
                } else {
                    _canLoadMoreStockLocations.value = false
                }
            }.onFailure {
                _canLoadMoreStockLocations.value = false
            }

            _isLoadingMoreStockLocations.value = false
        }
    }

    fun onStockLocationSearchQueryChanged(newQuery: String) {
        stockLocationSearchJob?.cancel()
        stockLocationSearchJob = viewModelScope.launch {
            delay(400)
            fetchStockLocationItems(
                page = 1,
                search = newQuery.takeIf { it.isNotBlank() },
                status = activeStockLocationStatus
            )
        }
    }

    fun fetchStockLocationViewOne(id: String) {
        viewModelScope.launch {
            _isLoadingStockLocationDetail.value = true
            _stockLocationDetailError.value = null

            val result = inventoryRepository.getStockLocationViewOne(id)
            result.onSuccess { data ->
                _selectedStockLocationDetail.value = data
            }.onFailure { error ->
                _stockLocationDetailError.value = extractErrorMessage(error.message)
            }
            _isLoadingStockLocationDetail.value = false
        }
    }

    fun clearSelectedStockLocationDetail() {
        _selectedStockLocationDetail.value = null
        _stockLocationDetailError.value = null
    }

    fun loadFloorDropdown(warehouseId: String) {
        if (warehouseId.isBlank()) return
        viewModelScope.launch {
            _floorDropdown.value = emptyList()
            _sectionDropdown.value = emptyList()
            _rackDropdown.value = emptyList()
            _binDropdown.value = emptyList()

            inventoryRepository.getFloorDropdown(warehouseId).onSuccess { list ->
                _floorDropdown.value = list
            }
        }
    }

    fun loadSectionDropdown(floorId: String) {
        if (floorId.isBlank()) return
        viewModelScope.launch {
            _sectionDropdown.value = emptyList()
            _rackDropdown.value = emptyList()
            _binDropdown.value = emptyList()

            inventoryRepository.getSectionDropdown(floorId).onSuccess { list ->
                _sectionDropdown.value = list
            }
        }
    }

    fun loadRackDropdown(sectionId: String) {
        if (sectionId.isBlank()) return
        viewModelScope.launch {
            _rackDropdown.value = emptyList()
            _binDropdown.value = emptyList()

            inventoryRepository.getRackDropdown(sectionId).onSuccess { list ->
                _rackDropdown.value = list
            }
        }
    }

    fun loadBinDropdown(rackId: String) {
        if (rackId.isBlank()) return
        viewModelScope.launch {
            _binDropdown.value = emptyList()

            inventoryRepository.getBinDropdown(rackId).onSuccess { list ->
                _binDropdown.value = list
            }
        }
    }

    fun clearHierarchyDropdowns() {
        _floorDropdown.value = emptyList()
        _sectionDropdown.value = emptyList()
        _rackDropdown.value = emptyList()
        _binDropdown.value = emptyList()
    }

    fun assignStockLocation(
        request: AssignStockLocationRequest,
        onSuccess: (StockLocationAssignmentData) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isSubmittingStockLocation.value = true
            _stockLocationActionError.value = null
            _stockLocationActionSuccess.value = null

            val result = inventoryRepository.assignStockLocation(request)
            _isSubmittingStockLocation.value = false

            result.onSuccess { data ->
                _stockLocationActionSuccess.value = "Stock assigned to location successfully"
                fetchStockLocationItems(page = 1)
                onSuccess(data)
            }.onFailure { error ->
                _stockLocationActionError.value = extractErrorMessage(error.message)
            }
        }
    }

    fun clearStockLocationActionAlerts() {
        _stockLocationActionSuccess.value = null
        _stockLocationActionError.value = null
    }

    //SAFETY STOCK



    fun fetchSafetyStock(search: String? = null, warehouseId: String? = null) {
        viewModelScope.launch {
            _isLoadingSafetyStock.value = true
            _safetyStockError.value = null
            currentSafetyStockPage = 1

            val result = inventoryRepository.getSafetyStock(
                page = currentSafetyStockPage,
                limit = 10,
                search = search?.ifBlank { null },
                warehouseId = warehouseId
            )

            result.onSuccess { response ->
                _safetyStockList.value = response.data
                totalSafetyStockPages = response.pagination?.totalPages ?: 1
                _canLoadMoreSafetyStock.value = currentSafetyStockPage < totalSafetyStockPages
            }.onFailure { err ->
                _safetyStockError.value = err.localizedMessage ?: "Failed to load safety stock"
            }

            _isLoadingSafetyStock.value = false
        }
    }

    fun loadMoreSafetyStock(search: String? = null, warehouseId: String? = null) {
        if (_isLoadingMoreSafetyStock.value || !_canLoadMoreSafetyStock.value) return

        viewModelScope.launch {
            _isLoadingMoreSafetyStock.value = true
            val nextPage = currentSafetyStockPage + 1

            val result = inventoryRepository.getSafetyStock(
                page = nextPage,
                limit = 10,
                search = search?.ifBlank { null },
                warehouseId = warehouseId
            )

            result.onSuccess { response ->
                currentSafetyStockPage = nextPage
                _safetyStockList.value = _safetyStockList.value + response.data
                totalSafetyStockPages = response.pagination?.totalPages ?: 1
                _canLoadMoreSafetyStock.value = currentSafetyStockPage < totalSafetyStockPages
            }.onFailure { err ->
                _safetyStockError.value = err.localizedMessage
            }

            _isLoadingMoreSafetyStock.value = false
        }
    }

    fun clearSafetyStockAlerts() {
        _safetyStockError.value = null
    }

    // =========================================================================
    // PROCUREMENT BILLS (VIEW-ALL: PAGINATED)
    // =========================================================================


    fun fetchAllBills(
        page: Int = 1,
        limit: Int = 10,
        search: String? = null,
        status: String? = null
    ) {
        viewModelScope.launch {
            _isLoadingBills.value = true
            _billsError.value = null
            _currentBillsPage.value = page
            _canLoadMoreBills.value = true
            activeBillsSearch = search
            activeBillsStatus = status

            inventoryRepository.getAllBills(page = page, limit = limit, search = search, status = status)
                .onSuccess { response ->
                    _billsList.value = response.data
                    val totalPages = response.pagination?.totalPages ?: 1
                    _canLoadMoreBills.value = page < totalPages && response.data.isNotEmpty()
                }
                .onFailure { error ->
                    _billsError.value = extractErrorMessage(error.message)
                }

            _isLoadingBills.value = false
        }
    }

    fun loadMoreBills(limit: Int = 10) {
        if (_isLoadingMoreBills.value || _isLoadingBills.value || !_canLoadMoreBills.value) return

        viewModelScope.launch {
            _isLoadingMoreBills.value = true
            val nextPage = _currentBillsPage.value + 1

            inventoryRepository.getAllBills(
                page = nextPage,
                limit = limit,
                search = activeBillsSearch,
                status = activeBillsStatus
            ).onSuccess { response ->
                val newBills = response.data
                if (newBills.isNotEmpty()) {
                    _billsList.update { (it + newBills).distinctBy { item -> item.id } }
                    _currentBillsPage.value = nextPage
                    val totalPages = response.pagination?.totalPages ?: nextPage
                    _canLoadMoreBills.value = nextPage < totalPages
                } else {
                    _canLoadMoreBills.value = false
                }
            }.onFailure {
                _canLoadMoreBills.value = false
            }

            _isLoadingMoreBills.value = false
        }
    }

    fun onBillsSearchQueryChanged(newQuery: String) {
        billsSearchJob?.cancel()
        billsSearchJob = viewModelScope.launch {
            delay(400)
            fetchAllBills(page = 1, search = newQuery.takeIf { it.isNotBlank() }, status = activeBillsStatus)
        }
    }

    fun clearBillsError() {
        _billsError.value = null
    }

    //bill list view one
    fun fetchBillDetail(id: String) {
        if (id.isBlank()) return
        viewModelScope.launch {
            _isLoadingBillDetail.value = true
            _billDetailError.value = null
            inventoryRepository.getBillById(id)
                .onSuccess { detail ->
                    _selectedBillDetail.value = detail
                }
                .onFailure { error ->
                    _billDetailError.value = extractErrorMessage(error.message)
                }
            _isLoadingBillDetail.value = false
        }
    }

    fun clearBillDetail() {
        _selectedBillDetail.value = null
        _billDetailError.value = null
    }


    fun sendBill(id: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isBillActionInProgress.value = true
            _billActionErrorMessage.value = null
            _billActionSuccessMessage.value = null

            inventoryRepository.sendBill(id)
                .onSuccess { updatedBill ->
                    _selectedBillDetail.value = updatedBill
                    _billActionSuccessMessage.value = "Bill sent successfully"
                    fetchAllBills(page = 1)
                    onSuccess()
                }
                .onFailure { error ->
                    _billActionErrorMessage.value = extractErrorMessage(error.message)
                }

            _isBillActionInProgress.value = false
        }
    }

    fun voidBill(id: String, reason: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isBillActionInProgress.value = true
            _billActionErrorMessage.value = null
            _billActionSuccessMessage.value = null

            inventoryRepository.voidBill(id, reason)
                .onSuccess { updatedBill ->
                    _selectedBillDetail.value = updatedBill
                    _billActionSuccessMessage.value = "Bill voided."
                    fetchAllBills(page = 1)
                    onSuccess()
                }
                .onFailure { error ->
                    _billActionErrorMessage.value = extractErrorMessage(error.message)
                }

            _isBillActionInProgress.value = false
        }
    }

    fun clearBillActionAlerts() {
        _billActionSuccessMessage.value = null
        _billActionErrorMessage.value = null
    }


    fun recordPayment(
        billId: String,
        request: RecordPaymentRequest,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isRecordingPayment.value = true
            _recordPaymentErrorMessage.value = null

            inventoryRepository.recordBillPayment( request)
                .onSuccess {
                    // Refresh both the individual bill and the list
                    fetchBillDetail(billId)
                    fetchAllBills(page = 1)
                    _isRecordingPayment.value = false
                    onSuccess()
                }
                .onFailure { error ->
                    _recordPaymentErrorMessage.value = extractErrorMessage(error.message)
                    _isRecordingPayment.value = false
                }
        }
    }

    fun clearRecordPaymentError() {
        _recordPaymentErrorMessage.value = null
    }
}
