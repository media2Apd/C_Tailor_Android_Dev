@file:Suppress("unused", "AssignedValueIsNeverRead")

package com.cuso.tailor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.tailor.model.logistics.ConfirmAllocationRequest
import com.cuso.tailor.model.logistics.LogisticsDashboardData
import com.cuso.tailor.model.logistics.LogisticsDashboardResponse
import com.cuso.tailor.model.logistics.LogisticsOrderDetailData
import com.cuso.tailor.model.logistics.LogisticsOrderDetailResponse
import com.cuso.tailor.model.logistics.LogisticsPackagesResponse
import com.cuso.tailor.model.logistics.LogisticsShipmentsResponse
import com.cuso.tailor.model.logistics.MarkDispatchedRequest
import com.cuso.tailor.model.logistics.PackageItemData
import com.cuso.tailor.model.logistics.ShipmentItemDto
import com.cuso.tailor.model.logistics.SimpleLogisticsActionResponse
import com.cuso.tailor.model.logistics.TimelineTimestampsDto
import com.cuso.tailor.model.logistics.UpdateOrderStatusRequest
import com.cuso.tailor.model.logistics.UpdateOrderStatusResponse
import com.cuso.tailor.model.logistics.WarehouseAllocationData
import com.cuso.tailor.model.logistics.WarehouseAllocationResponse
import com.cuso.tailor.repository.ApiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val LOGISTICS_METRICS_URL = "/api/logistics/dashboard/metrics"
private const val LOGISTICS_ORDERS_URL = "/api/logistics/delivery/dashboard"
private const val LOGISTICS_ORDERS_DETAILS_URL = "/api/logistics/delivery/order-details"
private const val LOGISTICS_WAREHOUSES_URL = "/api/logistics/delivery/allocation-options"
private const val LOGISTICS_ALLOCATE_URL = "/api/logistics/delivery/allocate-warehouse"
private const val LOGISTICS_UPDATE_STATUS_URL = "/api/logistics/delivery/update-order-status"
private const val LOGISTICS_SHIPMENTS_URL = "/api/logistics/delivery/shipment-queue"
private const val LOGISTICS_MARK_DISPATCHED_URL = "/api/logistics/delivery/confirm-dispatch"

@HiltViewModel
class LogisticsViewModel @Inject constructor(
    private val repo: ApiRepository
) : ViewModel() {

    // --- Dashboard Metrics State ---
    private val _dashboardData = MutableStateFlow<LogisticsDashboardData?>(null)
    val dashboardData: StateFlow<LogisticsDashboardData?> = _dashboardData.asStateFlow()

    // --- Packages / Orders List State ---
    private val _packagesList = MutableStateFlow<List<PackageItemData>>(emptyList())
    val packagesList: StateFlow<List<PackageItemData>> = _packagesList.asStateFlow()

    // --- Search Query State ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // --- Loading & Error States ---
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _trendRange = MutableStateFlow("monthly")
    val trendRange: StateFlow<String> = _trendRange.asStateFlow()

    private val _startDate = MutableStateFlow<String?>(null)
    val startDate: StateFlow<String?> = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow<String?>(null)
    val endDate: StateFlow<String?> = _endDate.asStateFlow()

    // --- Order Details State ---
    private val _orderDetail = MutableStateFlow<LogisticsOrderDetailData?>(null)
    val orderDetail: StateFlow<LogisticsOrderDetailData?> = _orderDetail.asStateFlow()

    private val _isLoadingDetail = MutableStateFlow(false)
    val isLoadingDetail: StateFlow<Boolean> = _isLoadingDetail.asStateFlow()

    private val _detailError = MutableStateFlow<String?>(null)
    val detailError: StateFlow<String?> = _detailError.asStateFlow()

    // --- Warehouse Allocation BottomSheet State ---
    private val _warehouseAllocationData = MutableStateFlow<WarehouseAllocationData?>(null)
    val warehouseAllocationData: StateFlow<WarehouseAllocationData?> = _warehouseAllocationData.asStateFlow()

    private val _isLoadingWarehouses = MutableStateFlow(false)
    val isLoadingWarehouses: StateFlow<Boolean> = _isLoadingWarehouses.asStateFlow()

    private val _warehouseError = MutableStateFlow<String?>(null)
    val warehouseError: StateFlow<String?> = _warehouseError.asStateFlow()

    // --- Action Execution State (Allocate / Status Update) ---
    private val _isSubmittingAction = MutableStateFlow(false)
    val isSubmittingAction: StateFlow<Boolean> = _isSubmittingAction.asStateFlow()

    private val _statusUpdateError = MutableStateFlow<String?>(null)
    val statusUpdateError: StateFlow<String?> = _statusUpdateError.asStateFlow()

    init {
        fetchDashboardMetrics()
        fetchOrders()
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun clearStatusUpdateError() {
        _statusUpdateError.value = null
    }

    /**
     * Fetches packages/shipment orders list
     */
    fun fetchOrders() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = repo.request<LogisticsPackagesResponse> {
                get(LOGISTICS_ORDERS_URL)
            }

            result.fold(
                onSuccess = { response ->
                    if (response.success) {
                        _packagesList.value = response.data
                    } else {
                        _errorMessage.value = response.message ?: "Failed to fetch logistics packages"
                    }
                },
                onFailure = { error ->
                    _errorMessage.value = error.message ?: "Failed to connect to logistics packages API"
                }
            )
            _isLoading.value = false
        }
    }

    /**
     * Fetches order details by package ID or order ID
     */
    fun fetchOrderDetail(id: String) {
        viewModelScope.launch {
            _isLoadingDetail.value = true
            _detailError.value = null

            val result = repo.request<LogisticsOrderDetailResponse> {
                get("$LOGISTICS_ORDERS_DETAILS_URL/$id")
            }

            result.fold(
                onSuccess = { response ->
                    if (response.success && response.data != null) {
                        _orderDetail.value = response.data
                    } else {
                        _detailError.value = response.message ?: "Failed to fetch order details"
                    }
                },
                onFailure = { error ->
                    _detailError.value = error.message ?: "Network error while fetching order details"
                }
            )
            _isLoadingDetail.value = false
        }
    }

    /**
     * Fetches available warehouses for order allocation
     */
    fun fetchAvailableWarehouses(orderId: String) {
        viewModelScope.launch {
            _isLoadingWarehouses.value = true
            _warehouseError.value = null

            val result = repo.request<WarehouseAllocationResponse> {
                get("$LOGISTICS_WAREHOUSES_URL/$orderId")
            }

            result.fold(
                onSuccess = { response ->
                    if (response.success && response.data != null) {
                        _warehouseAllocationData.value = response.data
                    } else {
                        _warehouseError.value = response.message ?: "Failed to load warehouses"
                    }
                },
                onFailure = { error ->
                    _warehouseError.value = error.message ?: "Failed to load warehouse data"
                }
            )
            _isLoadingWarehouses.value = false
        }
    }

    /**
     * Confirms warehouse allocation for an order
     */
    fun confirmWarehouseAllocation(
        orderId: String,
        warehouseId: String,
        isAutoAllocate: Boolean,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isSubmittingAction.value = true
            val body = ConfirmAllocationRequest(
                warehouseId = warehouseId,
                isAutoAllocate = isAutoAllocate
            )

            val result = repo.request<SimpleLogisticsActionResponse> {
                post("$LOGISTICS_ALLOCATE_URL/$orderId", body)
            }

            result.fold(
                onSuccess = { response ->
                    if (response.success) {
                        fetchOrderDetail(orderId)
                        onSuccess()
                    }
                },
                onFailure = { /* Handle error */ }
            )
            _isSubmittingAction.value = false
        }
    }

    /**
     * Updates order fulfillment status matching SummaryApi.updateLogisticsOrderStatus
     * Payload: { status: String, note: String? }
     */
    fun updateOrderStatus(
        orderId: String,
        newStatus: String,
        note: String?,
        onSuccess: (message: String) -> Unit
    ) {
        viewModelScope.launch {
            _isSubmittingAction.value = true
            _statusUpdateError.value = null

            val trimmedNote = note?.trim()?.ifBlank { null }
            val requestBody = UpdateOrderStatusRequest(
                status = newStatus,
                notes = trimmedNote
            )

            val result = repo.request<UpdateOrderStatusResponse> {
                put("$LOGISTICS_UPDATE_STATUS_URL/$orderId", requestBody)
            }

            result.fold(
                onSuccess = { response ->
                    if (response.success && response.data != null) {
                        val updatedData = response.data

                        // Clean null-safe update with backtick `package`
                        val currentDetail = _orderDetail.value
                        if (currentDetail != null) {
                            val currentLogistics = currentDetail.logistics
                            val newTimeline = updatedData.timeline?.let {
                                TimelineTimestampsDto(
                                    createdAt = it.createdAt ?: currentLogistics?.timeline?.createdAt,
                                    pickedAt = it.pickedAt ?: currentLogistics?.timeline?.pickedAt,
                                    packedAt = it.packedAt ?: currentLogistics?.timeline?.packedAt,
                                    inTransitAt = it.shippedAt ?: currentLogistics?.timeline?.inTransitAt,
                                    deliveredAt = it.deliveredAt ?: currentLogistics?.timeline?.deliveredAt
                                )
                            } ?: currentLogistics?.timeline

                            val updatedLogistics = currentLogistics?.copy(
                                status = updatedData.status,
                                timeline = newTimeline,
                                activityLog = updatedData.activityLog
                            )

                            val updatedPackage = currentDetail.`package`?.copy(
                                status = updatedData.status
                            )

                            val updatedShipment = currentDetail.shipment?.copy(
                                status = updatedData.status
                            )

                            _orderDetail.value = currentDetail.copy(
                                logistics = updatedLogistics,
                                `package` = updatedPackage,
                                shipment = updatedShipment
                            )
                        }

                        // Re-sync complete data in background
                        fetchOrderDetail(orderId)
                        onSuccess(response.message ?: "Order status updated successfully")
                    } else {
                        _statusUpdateError.value = response.message ?: "Failed to update status"
                    }
                },
                onFailure = { error ->
                    _statusUpdateError.value = error.message ?: "Failed to update status"
                }
            )
            _isSubmittingAction.value = false
        }
    }

    /**
     * Calls GET /api/logistics/dashboard/metrics
     */
    fun fetchDashboardMetrics(
        startDate: String? = _startDate.value,
        endDate: String? = _endDate.value,
        trendRange: String = _trendRange.value
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _startDate.value = startDate
            _endDate.value = endDate
            _trendRange.value = trendRange

            val queryParams = mutableListOf<Pair<String, Any?>>()
            if (!startDate.isNullOrBlank()) queryParams.add("startDate" to startDate)
            if (!endDate.isNullOrBlank()) queryParams.add("endDate" to endDate)
            queryParams.add("trendRange" to trendRange)

            val result = repo.request<LogisticsDashboardResponse> {
                get(
                    LOGISTICS_METRICS_URL,
                    repo.query(*queryParams.toTypedArray())
                )
            }

            result.fold(
                onSuccess = { response ->
                    if (response.success && response.data != null) {
                        _dashboardData.value = response.data
                    } else {
                        _errorMessage.value = response.message ?: "Failed to fetch logistics metrics"
                    }
                },
                onFailure = { error ->
                    _errorMessage.value = error.message ?: "Failed to connect to logistics dashboard"
                }
            )
            _isLoading.value = false
        }
    }

    // --- Shipments State ---
    private val _shipmentsList = MutableStateFlow<List<ShipmentItemDto>>(emptyList())
    val shipmentsList: StateFlow<List<ShipmentItemDto>> = _shipmentsList.asStateFlow()

    private val _shipmentSearchQuery = MutableStateFlow("")
    val shipmentSearchQuery: StateFlow<String> = _shipmentSearchQuery.asStateFlow()

    private val _isLoadingShipments = MutableStateFlow(false)
    val isLoadingShipments: StateFlow<Boolean> = _isLoadingShipments.asStateFlow()

    private val _shipmentError = MutableStateFlow<String?>(null)
    val shipmentError: StateFlow<String?> = _shipmentError.asStateFlow()

    fun onShipmentSearchQueryChange(query: String) {
        _shipmentSearchQuery.value = query
    }

    /**
     * Fetches shipments list
     */
    fun fetchShipments() {
        viewModelScope.launch {
            _isLoadingShipments.value = true
            _shipmentError.value = null

            val result = repo.request<LogisticsShipmentsResponse> {
                get(LOGISTICS_SHIPMENTS_URL)
            }

            result.fold(
                onSuccess = { response ->
                    if (response.success) {
                        _shipmentsList.value = response.data
                    } else {
                        _shipmentError.value = response.message ?: "Failed to fetch shipments"
                    }
                },
                onFailure = { error ->
                    _shipmentError.value = error.message ?: "Network error while loading shipments"
                }
            )
            _isLoadingShipments.value = false
        }
    }

    /**
     * Triggers shipment dispatch action (e.g. "Mark Dispatched")
     */
    fun markShipmentDispatched(shipmentId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isSubmittingAction.value = true
            val body = MarkDispatchedRequest(shipmentId = shipmentId)

            val result = repo.request<SimpleLogisticsActionResponse> {
                post("$LOGISTICS_MARK_DISPATCHED_URL/$shipmentId", body)
            }

            result.fold(
                onSuccess = { response ->
                    if (response.success) {
                        fetchShipments()
                        onSuccess()
                    }
                },
                onFailure = { /* Handle error */ }
            )
            _isSubmittingAction.value = false
        }
    }
}