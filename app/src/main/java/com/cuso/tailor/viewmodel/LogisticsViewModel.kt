@file:Suppress("unused", "AssignedValueIsNeverRead")

package com.cuso.tailor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.tailor.model.logistics.AssignCourierRequest
import com.cuso.tailor.model.logistics.AssignCourierResponse
import com.cuso.tailor.model.logistics.AssignDeliveryRequest
import com.cuso.tailor.model.logistics.ConfirmAllocationRequest
import com.cuso.tailor.model.logistics.ConfirmInStorePickupRequest
import com.cuso.tailor.model.logistics.ConfirmInStorePickupResponse
import com.cuso.tailor.model.logistics.CourierDeliveryOverviewData
import com.cuso.tailor.model.logistics.CourierDeliveryOverviewResponse
import com.cuso.tailor.model.logistics.CourierManagementItemDto
import com.cuso.tailor.model.logistics.CourierManagementListResponse
import com.cuso.tailor.model.logistics.CourierPartnerDetailData
import com.cuso.tailor.model.logistics.CourierPartnerDetailResponse
import com.cuso.tailor.model.logistics.CourierPartnerItemDto
import com.cuso.tailor.model.logistics.CourierPartnersResponse
import com.cuso.tailor.model.logistics.CreateCourierPartnerResponse
import com.cuso.tailor.model.logistics.CreateShipmentApiRequest
import com.cuso.tailor.model.logistics.CreateShipmentApiResponse
import com.cuso.tailor.model.logistics.CreateUpdateCourierPartnerRequest
import com.cuso.tailor.model.logistics.DeliveryDetailData
import com.cuso.tailor.model.logistics.DeliveryDetailResponse
import com.cuso.tailor.model.logistics.DeliveryItemDto
import com.cuso.tailor.model.logistics.DeliveryListResponse
import com.cuso.tailor.model.logistics.DeliveryShipmentAddressDto
import com.cuso.tailor.model.logistics.DimensionsDetailDto
import com.cuso.tailor.model.logistics.GenerateShippingLabelResponse
import com.cuso.tailor.model.logistics.LogisticsDashboardData
import com.cuso.tailor.model.logistics.LogisticsDashboardResponse
import com.cuso.tailor.model.logistics.LogisticsOrderDetailData
import com.cuso.tailor.model.logistics.LogisticsOrderDetailResponse
import com.cuso.tailor.model.logistics.LogisticsPackagesResponse
import com.cuso.tailor.model.logistics.LogisticsShipmentsResponse
import com.cuso.tailor.model.logistics.MarkDeliveryCompletedResponse
import com.cuso.tailor.model.logistics.MarkDispatchedRequest
import com.cuso.tailor.model.logistics.PackageDimensionDto
import com.cuso.tailor.model.logistics.PackageItemData
import com.cuso.tailor.model.logistics.ShipmentAllocationDto
import com.cuso.tailor.model.logistics.ShipmentDetailData
import com.cuso.tailor.model.logistics.ShipmentDetailResponse
import com.cuso.tailor.model.logistics.ShipmentItemDto
import com.cuso.tailor.model.logistics.ShipmentListItemDto
import com.cuso.tailor.model.logistics.ShipmentListResponse
import com.cuso.tailor.model.logistics.SimpleLogisticsActionResponse
import com.cuso.tailor.model.logistics.TimelineTimestampsDto
import com.cuso.tailor.model.logistics.ToggleCourierStatusRequest
import com.cuso.tailor.model.logistics.TrackingEventItemDto
import com.cuso.tailor.model.logistics.TrackingEventsResponse
import com.cuso.tailor.model.logistics.UpdateDeliveryStatusRequest
import com.cuso.tailor.model.logistics.UpdateDeliveryStatusResponse
import com.cuso.tailor.model.logistics.UpdateOrderStatusRequest
import com.cuso.tailor.model.logistics.UpdateOrderStatusResponse
import com.cuso.tailor.model.logistics.UpdateShipmentStatusRequest
import com.cuso.tailor.model.logistics.WarehouseAllocationData
import com.cuso.tailor.model.logistics.WarehouseAllocationResponse
import com.cuso.tailor.repository.ApiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

// --- Existing API Endpoints ---
private const val LOGISTICS_METRICS_URL = "/api/logistics/dashboard/metrics"
private const val LOGISTICS_ORDERS_URL = "/api/logistics/delivery/dashboard"
private const val LOGISTICS_ORDERS_DETAILS_URL = "/api/logistics/delivery/order-details"
private const val LOGISTICS_WAREHOUSES_URL = "/api/logistics/delivery/allocation-options"
private const val LOGISTICS_ALLOCATE_URL = "/api/logistics/delivery/allocate-warehouse"
private const val LOGISTICS_UPDATE_STATUS_URL = "/api/logistics/delivery/update-order-status"
private const val LOGISTICS_SHIPMENTS_URL = "/api/logistics/delivery/shipment-queue"
private const val LOGISTICS_MARK_DISPATCHED_URL = "/api/logistics/delivery/confirm-dispatch"

// --- Delivery API Endpoints ---
private const val LOGISTICS_ALL_DELIVERIES_URL = "/api/logistics/delivery/view-all"
private const val LOGISTICS_ASSIGN_DELIVERY_URL = "/api/logistics/delivery/assign-staff-modal"
private const val LOGISTICS_DELIVERY_VIEW_ONE_URL = "/api/logistics/delivery/view-one"
private const val LOGISTICS_DELIVERY_MARK_COMPLETED_URL = "/api/logistics/delivery/mark-completed"
private const val LOGISTICS_UPDATE_DELIVERY_STATUS_URL = "/api/logistics/delivery/update-status"
private const val LOGISTICS_CONFIRM_INSTORE_PICKUP_URL = "/api/logistics/in-store-pickup/confirm"

// --- SHIPMENT SPECIFIC API ENDPOINTS ---
private const val SHIPMENT_LIST_URL = "/api/logistics/shipment/list"

private const val SHIPMENT_DETAILS_URL = "/api/logistics/shipment/details"
private const val SHIPMENT_UPDATE_STATUS_URL = "/api/logistics/shipment/update-status"
private const val SHIPMENT_GENERATE_LABEL_URL = "/api/logistics/shipment/generate-label"
private const val SHIPMENT_CANCEL_URL = "/api/logistics/shipment/cancel"

private const val COURIER_MANAGEMENT_LIST_URL = "/api/logistics/courier-management/list"
private const val COURIER_PARTNERS_LIST_URL = "/api/logistics/courier-partner/list"
private const val CREATE_SHIPMENT_URL = "/api/logistics/shipment/create"
private const val LOGISTICS_ASSIGN_COURIER_URL = "/api/logistics/courier-management/assign"
private const val COURIER_DELIVERY_OVERVIEW_URL = "/api/logistics/courier-management/overview"
private const val MARK_COURIER_COMPLETED_URL = "/api/logistics/courier-management/mark-completed"
private const val COURIER_PARTNER_DETAIL_URL = "/api/logistics/courier-partner/overview"
private const val COURIER_PARTNER_TOGGLE_STATUS_URL = "/api/logistics/courier-partner/toggle-status"

private const val COURIER_PARTNER_CREATE_URL = "/api/logistics/courier-partner/create"
private const val COURIER_PARTNER_UPDATE_URL = "/api/logistics/courier-partner/update"
private const val LOGISTICS_TRACKING_EVENTS_URL = "/api/logistics/tracking-events/list"


@HiltViewModel
class LogisticsViewModel @Inject constructor(
    private val repo: ApiRepository
) : ViewModel() {

    // =========================================================================
    // 1. DASHBOARD METRICS STATE
    // =========================================================================
    private val _dashboardData = MutableStateFlow<LogisticsDashboardData?>(null)
    val dashboardData: StateFlow<LogisticsDashboardData?> = _dashboardData.asStateFlow()

    private val _trendRange = MutableStateFlow("monthly")
    val trendRange: StateFlow<String> = _trendRange.asStateFlow()

    private val _startDate = MutableStateFlow<String?>(null)
    val startDate: StateFlow<String?> = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow<String?>(null)
    val endDate: StateFlow<String?> = _endDate.asStateFlow()

    // =========================================================================
    // 2. PACKAGES / ORDERS LIST STATE
    // =========================================================================
    private val _packagesList = MutableStateFlow<List<PackageItemData>>(emptyList())
    val packagesList: StateFlow<List<PackageItemData>> = _packagesList.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Loading & Error States
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // =========================================================================
    // 3. ORDER DETAILS STATE
    // =========================================================================
    private val _orderDetail = MutableStateFlow<LogisticsOrderDetailData?>(null)
    val orderDetail: StateFlow<LogisticsOrderDetailData?> = _orderDetail.asStateFlow()

    private val _isLoadingDetail = MutableStateFlow(false)
    val isLoadingDetail: StateFlow<Boolean> = _isLoadingDetail.asStateFlow()

    private val _detailError = MutableStateFlow<String?>(null)
    val detailError: StateFlow<String?> = _detailError.asStateFlow()

    // =========================================================================
    // 4. WAREHOUSE ALLOCATION STATE
    // =========================================================================
    private val _warehouseAllocationData = MutableStateFlow<WarehouseAllocationData?>(null)
    val warehouseAllocationData: StateFlow<WarehouseAllocationData?> = _warehouseAllocationData.asStateFlow()

    private val _isLoadingWarehouses = MutableStateFlow(false)
    val isLoadingWarehouses: StateFlow<Boolean> = _isLoadingWarehouses.asStateFlow()

    private val _warehouseError = MutableStateFlow<String?>(null)
    val warehouseError: StateFlow<String?> = _warehouseError.asStateFlow()

    // =========================================================================
    // 5. SHIPMENTS STATE
    // =========================================================================
    private val _shipmentsList = MutableStateFlow<List<ShipmentItemDto>>(emptyList())
    val shipmentsList: StateFlow<List<ShipmentItemDto>> = _shipmentsList.asStateFlow()

    private val _shipmentSearchQuery = MutableStateFlow("")
    val shipmentSearchQuery: StateFlow<String> = _shipmentSearchQuery.asStateFlow()

    private val _isLoadingShipments = MutableStateFlow(false)
    val isLoadingShipments: StateFlow<Boolean> = _isLoadingShipments.asStateFlow()

    private val _shipmentError = MutableStateFlow<String?>(null)
    val shipmentError: StateFlow<String?> = _shipmentError.asStateFlow()

    // =========================================================================
    // 6. ACTION EXECUTION STATE
    // =========================================================================
    private val _isSubmittingAction = MutableStateFlow(false)
    val isSubmittingAction: StateFlow<Boolean> = _isSubmittingAction.asStateFlow()

    private val _statusUpdateError = MutableStateFlow<String?>(null)
    val statusUpdateError: StateFlow<String?> = _statusUpdateError.asStateFlow()

    // =========================================================================
    // 7. ALL DELIVERY SECTION (NEW)
    // =========================================================================
    private val _deliveriesList = MutableStateFlow<List<DeliveryItemDto>>(emptyList())
    val deliveriesList: StateFlow<List<DeliveryItemDto>> = _deliveriesList.asStateFlow()

    private val _deliverySearchQuery = MutableStateFlow("")
    val deliverySearchQuery: StateFlow<String> = _deliverySearchQuery.asStateFlow()

    private val _selectedDeliveryStatus = MutableStateFlow<String?>(null)
    val selectedDeliveryStatus: StateFlow<String?> = _selectedDeliveryStatus.asStateFlow()

    private val _selectedDeliveryType = MutableStateFlow<String?>(null)
    val selectedDeliveryType: StateFlow<String?> = _selectedDeliveryType.asStateFlow()

    private val _isLoadingDeliveries = MutableStateFlow(false)
    val isLoadingDeliveries: StateFlow<Boolean> = _isLoadingDeliveries.asStateFlow()

    private val _deliveryError = MutableStateFlow<String?>(null)
    val deliveryError: StateFlow<String?> = _deliveryError.asStateFlow()

    /**
     * Combined reactive list for deliveries filtered by search query, status, and delivery type.
     */
    val filteredDeliveries: StateFlow<List<DeliveryItemDto>> = combine(
        _deliveriesList,
        _deliverySearchQuery,
        _selectedDeliveryStatus,
        _selectedDeliveryType
    ) { deliveries, query, status, type ->
        val cleanQuery = query.trim().lowercase()
        deliveries.filter { item ->
            val matchesQuery = cleanQuery.isEmpty() ||
                    item.customer.lowercase().contains(cleanQuery) ||
                    item.orderId.lowercase().contains(cleanQuery) ||
                    item.deliveryId.lowercase().contains(cleanQuery) ||
                    (item.courierPartner?.contains(cleanQuery) == true)

            val matchesStatus = status.isNullOrBlank() || item.status.equals(status, ignoreCase = true)
            val matchesType = type.isNullOrBlank() || item.deliveryType.equals(type, ignoreCase = true)

            matchesQuery && matchesStatus && matchesType
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        fetchDashboardMetrics()
        fetchOrders()
        fetchDeliveries()
    }

    // =========================================================================
    // EXISTING FUNCTIONS
    // =========================================================================
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun clearStatusUpdateError() {
        _statusUpdateError.value = null
    }

    fun onShipmentSearchQueryChange(query: String) {
        _shipmentSearchQuery.value = query
    }

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

    private val _deliveryDetail = MutableStateFlow<DeliveryDetailData?>(null)
    val deliveryDetail: StateFlow<DeliveryDetailData?> = _deliveryDetail.asStateFlow()

    private val _isLoadingDeliveryDetail = MutableStateFlow(false)
    val isLoadingDeliveryDetail: StateFlow<Boolean> = _isLoadingDeliveryDetail.asStateFlow()

    private val _deliveryDetailError = MutableStateFlow<String?>(null)
    val deliveryDetailError: StateFlow<String?> = _deliveryDetailError.asStateFlow()

// 3. Add these functions inside LogisticsViewModel class

    /**
     * Fetches single delivery record by delivery ID.
     * Endpoint: GET /api/logistics/delivery/view-one/:id
     */
    fun fetchDeliveryDetail(deliveryId: String) {
        viewModelScope.launch {
            _isLoadingDeliveryDetail.value = true
            _deliveryDetailError.value = null

            val result = repo.request<DeliveryDetailResponse> {
                get("$LOGISTICS_DELIVERY_VIEW_ONE_URL/$deliveryId")
            }

            result.fold(
                onSuccess = { response ->
                    if (response.success && response.data != null) {
                        _deliveryDetail.value = response.data
                    } else {
                        _deliveryDetailError.value = response.message ?: "Failed to fetch delivery details"
                    }
                },
                onFailure = { error ->
                    _deliveryDetailError.value = error.message ?: "Network error while loading delivery detail"
                }
            )
            _isLoadingDeliveryDetail.value = false
        }
    }

    private val _isMarkingCompleted = MutableStateFlow(false)
    val isMarkingCompleted: StateFlow<Boolean> = _isMarkingCompleted.asStateFlow()

    private val _markCompletedError = MutableStateFlow<String?>(null)
    val markCompletedError: StateFlow<String?> = _markCompletedError.asStateFlow()

    /**
     * Calls PUT /api/logistics/delivery/mark-completed/:id
     * Refreshes delivery details and delivery list on success.
     */
    fun markDeliveryCompleted(
        deliveryId: String,
        onSuccess: (message: String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isMarkingCompleted.value = true
            _markCompletedError.value = null

            val result = repo.request<MarkDeliveryCompletedResponse> {
                put("$LOGISTICS_DELIVERY_MARK_COMPLETED_URL/$deliveryId")
            }

            result.fold(
                onSuccess = { response ->
                    if (response.success) {
                        fetchDeliveryDetail(deliveryId)
                        fetchDeliveries()
                        onSuccess(response.message ?: "Delivery completed successfully")
                    } else {
                        _markCompletedError.value = response.message ?: "Failed to complete delivery"
                    }
                },
                onFailure = { error ->
                    _markCompletedError.value = error.message ?: "Network error while completing delivery"
                }
            )
            _isMarkingCompleted.value = false
        }
    }

    /**
     * Confirms in-store pickup with the provided payload.
     */
    fun confirmInStorePickup(
        orderId: String,
        handedOverByStaffName: String,
        collectedBy: String,
        verificationMethod: String,
        packageCondition: String,
        notes: String,
        onSuccess: (message: String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isSubmittingAction.value = true
            _statusUpdateError.value = null

            val requestBody = ConfirmInStorePickupRequest(
                orderId = orderId,
                handedOverByStaffName = handedOverByStaffName,
                collectedBy = collectedBy,
                verificationMethod = verificationMethod,
                packageCondition = packageCondition,
                notes = notes
            )

            val result = repo.request<ConfirmInStorePickupResponse> {
                post(LOGISTICS_CONFIRM_INSTORE_PICKUP_URL, requestBody)
            }

            result.fold(
                onSuccess = { response ->
                    if (response.success) {
                        onSuccess(response.message ?: "In-store pickup confirmed successfully")
                    } else {
                        _statusUpdateError.value = response.message ?: "Failed to confirm pickup"
                    }
                },
                onFailure = { error ->
                    _statusUpdateError.value = error.message ?: "Network error while confirming pickup"
                }
            )
            _isSubmittingAction.value = false
        }
    }

    private val _isUpdatingDeliveryStatus = MutableStateFlow(false)
    val isUpdatingDeliveryStatus: StateFlow<Boolean> = _isUpdatingDeliveryStatus.asStateFlow()

    private val _updateDeliveryStatusError = MutableStateFlow<String?>(null)
    val updateDeliveryStatusError: StateFlow<String?> = _updateDeliveryStatusError.asStateFlow()

    /**
     * Updates delivery status with comment/note.
     * Endpoint: PUT /api/logistics/delivery/update-status/:deliveryId
     */
    fun updateDeliveryStatus(
        deliveryId: String,
        newStatus: String,
        message: String?,
        onSuccess: (message: String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isUpdatingDeliveryStatus.value = true
            _updateDeliveryStatusError.value = null

            val requestBody = UpdateDeliveryStatusRequest(
                status = newStatus,
                message = message?.trim()?.ifBlank { null }
            )

            val result = repo.request<UpdateDeliveryStatusResponse> {
                put("$LOGISTICS_UPDATE_DELIVERY_STATUS_URL/$deliveryId", requestBody)
            }

            result.fold(
                onSuccess = { response ->
                    if (response.success) {
                        fetchDeliveryDetail(deliveryId)
                        fetchDeliveries()
                        onSuccess(response.message ?: "Status changed to $newStatus successfully")
                    } else {
                        _updateDeliveryStatusError.value = response.message ?: "Failed to update delivery status"
                    }
                },
                onFailure = { error ->
                    _updateDeliveryStatusError.value = error.message ?: "Network error while updating delivery status"
                }
            )
            _isUpdatingDeliveryStatus.value = false
        }
    }

    /**
     * Clears current delivery detail state on exit.
     */
    fun clearDeliveryDetail() {
        _deliveryDetail.value = null
        _deliveryDetailError.value = null
    }

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

    // =========================================================================
    // 8. DELIVERY FUNCTIONS (NEW)
    // =========================================================================

    /**
     * Updates delivery search query.
     */
    fun onDeliverySearchQueryChange(query: String) {
        _deliverySearchQuery.value = query
    }

    /**
     * Filters deliveries by status (e.g., "Ready", "Delivered", "In Transit").
     */
    fun onDeliveryStatusFilterChange(status: String?) {
        _selectedDeliveryStatus.value = status
    }

    /**
     * Filters deliveries by delivery type (e.g., "Chennai", "Outstation").
     */
    fun onDeliveryTypeFilterChange(type: String?) {
        _selectedDeliveryType.value = type
    }

    /**
     * Clears all delivery search and filter options.
     */
    fun clearDeliveryFilters() {
        _deliverySearchQuery.value = ""
        _selectedDeliveryStatus.value = null
        _selectedDeliveryType.value = null
    }

    /**
     * Fetches delivery list from the API endpoint.
     */
    fun fetchDeliveries() {
        viewModelScope.launch {
            _isLoadingDeliveries.value = true
            _deliveryError.value = null

            val result = repo.request<DeliveryListResponse> {
                get(LOGISTICS_ALL_DELIVERIES_URL)
            }

            result.fold(
                onSuccess = { response ->
                    if (response.success) {
                        _deliveriesList.value = response.data
                    } else {
                        _deliveryError.value = response.message ?: "Failed to fetch deliveries"
                    }
                },
                onFailure = { error ->
                    _deliveryError.value = error.message ?: "Network error while loading delivery list"
                }
            )
            _isLoadingDeliveries.value = false
        }
    }

    /**
     * Executes the "Assign" action for a delivery item.
     */
    fun assignDelivery(
        deliveryId: String,
        staffId: String,
        staffInstructions: String,
        targetCompletionDate: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isSubmittingAction.value = true

            val requestBody = AssignDeliveryRequest(
                staffId = staffId,
                staffInstructions = staffInstructions,
                targetCompletionDate = targetCompletionDate
            )

            val result = repo.request<SimpleLogisticsActionResponse> {
                post(
                    "$LOGISTICS_ASSIGN_DELIVERY_URL/$deliveryId",
                    requestBody
                )
            }

            result.fold(
                onSuccess = { response ->
                    if (response.success) {
                        fetchDeliveries()
                        onSuccess()
                    }
                },
                onFailure = { error ->
                    _deliveryError.value =
                        error.message ?: "Failed to assign delivery"
                }
            )

            _isSubmittingAction.value = false
        }
    }
    // =============================================================================
// SHIPMENT LIST STATE & METHODS
// =============================================================================
    private val _shipmentItems = MutableStateFlow<List<ShipmentListItemDto>>(emptyList())
    val shipmentItems: StateFlow<List<ShipmentListItemDto>> = _shipmentItems.asStateFlow()

    private val _isLoadingShipmentList = MutableStateFlow(false)
    val isLoadingShipmentList: StateFlow<Boolean> = _isLoadingShipmentList.asStateFlow()

    private val _shipmentListError = MutableStateFlow<String?>(null)
    val shipmentListError: StateFlow<String?> = _shipmentListError.asStateFlow()

    val filteredShipments: StateFlow<List<ShipmentListItemDto>> = combine(
        _shipmentItems,
        _shipmentSearchQuery
    ) { list, query ->
        val cleanQuery = query.trim().lowercase()
        if (cleanQuery.isEmpty()) {
            list
        } else {
            list.filter { item ->
                item.shipmentId.lowercase().contains(cleanQuery) ||
                        (item.orderCode?.lowercase()?.contains(cleanQuery) == true) ||
                        (item.trackingId?.lowercase()?.contains(cleanQuery) == true) ||
                        (item.customerName?.lowercase()?.contains(cleanQuery) == true) ||
                        (item.courierPartner?.lowercase()?.contains(cleanQuery) == true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun fetchShipmentList(page: Int = 1, limit: Int = 20) {
        viewModelScope.launch {
            _isLoadingShipmentList.value = true
            _shipmentListError.value = null

            val result = repo.request<ShipmentListResponse> {
                get(SHIPMENT_LIST_URL, repo.query("page" to page, "limit" to limit))
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        _shipmentItems.value = res.data
                    } else {
                        _shipmentListError.value = res.message ?: "Failed to load shipments"
                    }
                },
                onFailure = { err ->
                    _shipmentListError.value = err.message ?: "Network error fetching shipment list"
                }
            )
            _isLoadingShipmentList.value = false
        }
    }

    // =============================================================================
// SHIPMENT DETAIL STATE & METHODS
// =============================================================================
    private val _currentShipmentDetail = MutableStateFlow<ShipmentDetailData?>(null)
    val currentShipmentDetail: StateFlow<ShipmentDetailData?> = _currentShipmentDetail.asStateFlow()

    private val _isLoadingShipmentDetail = MutableStateFlow(false)
    val isLoadingShipmentDetail: StateFlow<Boolean> = _isLoadingShipmentDetail.asStateFlow()

    private val _shipmentDetailError = MutableStateFlow<String?>(null)
    val shipmentDetailError: StateFlow<String?> = _shipmentDetailError.asStateFlow()

    fun fetchShipmentDetails(shipmentId: String) {
        viewModelScope.launch {
            _isLoadingShipmentDetail.value = true
            _shipmentDetailError.value = null

            val result = repo.request<ShipmentDetailResponse> {
                get("$SHIPMENT_DETAILS_URL/$shipmentId")
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success && res.data != null) {
                        _currentShipmentDetail.value = res.data
                    } else {
                        _shipmentDetailError.value = res.message ?: "Failed to fetch shipment details"
                    }
                },
                onFailure = { err ->
                    _shipmentDetailError.value = err.message ?: "Network error fetching details"
                }
            )
            _isLoadingShipmentDetail.value = false
        }
    }

    // =============================================================================
// UPDATE STATUS, GENERATE LABEL & CANCEL ACTIONS
// =============================================================================
    fun updateShipmentLiveStatus(
        shipmentId: String,
        status: String,
        currentLocation: String?,
        notes: String?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isSubmittingAction.value = true
            val body = UpdateShipmentStatusRequest(
                status = status,
                currentLocation = currentLocation?.trim()?.ifBlank { null },
                notes = notes?.trim()?.ifBlank { null }
            )

            val result = repo.request<SimpleLogisticsActionResponse> {
                put("$SHIPMENT_UPDATE_STATUS_URL/$shipmentId", body)
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        fetchShipmentDetails(shipmentId)
                        fetchShipmentList()
                        onSuccess()
                    } else {
                        _statusUpdateError.value = res.message ?: "Failed to update shipment status"
                    }
                },
                onFailure = { err ->
                    _statusUpdateError.value = err.message ?: "Failed to connect to server"
                }
            )
            _isSubmittingAction.value = false
        }
    }

    fun generateShipmentLabel(shipmentId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isSubmittingAction.value = true
            val result = repo.request<GenerateShippingLabelResponse> {
                post("$SHIPMENT_GENERATE_LABEL_URL/$shipmentId", emptyMap<String, String>())
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        fetchShipmentDetails(shipmentId)
                        onSuccess()
                    }
                },
                onFailure = { /* Handled silently or mapped to error state */ }
            )
            _isSubmittingAction.value = false
        }
    }

    fun cancelShipment(shipmentId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isSubmittingAction.value = true
            val result = repo.request<SimpleLogisticsActionResponse> {
                put("$SHIPMENT_CANCEL_URL/$shipmentId")
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        fetchShipmentDetails(shipmentId)
                        fetchShipmentList()
                        onSuccess()
                    }
                },
                onFailure = { /* Handled */ }
            )
            _isSubmittingAction.value = false
        }
    }

    private val _courierPartnersList = MutableStateFlow<List<CourierPartnerItemDto>>(emptyList())
    val courierPartnersList: StateFlow<List<CourierPartnerItemDto>> = _courierPartnersList.asStateFlow()

    private val _isLoadingCouriers = MutableStateFlow(false)
    val isLoadingCouriers: StateFlow<Boolean> = _isLoadingCouriers.asStateFlow()

    private val _courierError = MutableStateFlow<String?>(null)
    val courierError: StateFlow<String?> = _courierError.asStateFlow()

    private val _isCreatingShipment = MutableStateFlow(false)
    val isCreatingShipment: StateFlow<Boolean> = _isCreatingShipment.asStateFlow()

    private val _createShipmentError = MutableStateFlow<String?>(null)
    val createShipmentError: StateFlow<String?> = _createShipmentError.asStateFlow()

    /**
     * Fetches available courier partners from the backend.
     * Endpoint: GET /api/logistics/courier/list
     */
    fun fetchCourierPartners() {
        viewModelScope.launch {
            _isLoadingCouriers.value = true
            _courierError.value = null

            val result = repo.request<CourierPartnersResponse> {
                get(COURIER_PARTNERS_LIST_URL, repo.query("limit" to 100))
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        _courierPartnersList.value = res.data.filter { it.status.equals("Active", ignoreCase = true) }
                    } else {
                        _courierError.value = res.message ?: "Failed to fetch couriers"
                    }
                },
                onFailure = { err ->
                    _courierError.value = err.message ?: "Network error loading couriers"
                }
            )
            _isLoadingCouriers.value = false
        }
    }

    /**
     * Submits the create shipment form matching the exact backend payload.
     * Endpoint: POST /api/logistics/shipment/create
     */
    fun submitCreateShipment(
        orderId: String,
        recipientName: String,
        phoneNumber: String,
        fullAddress: String,
        city: String = "",
        pincode: String = "",
        state: String = "",
        courierPartner: String,
        serviceType: String,
        weightKg: Double,
        lengthCm: Double,
        widthCm: Double,
        heightCm: Double,
        warehouseId: String? = null,
        warehouseName: String? = null,
        onSuccess: (message: String) -> Unit
    ) {
        viewModelScope.launch {
            _isCreatingShipment.value = true
            _createShipmentError.value = null

            val allocationPayload = if (!warehouseId.isNullOrBlank()) {
                ShipmentAllocationDto(
                    warehouseId = warehouseId,
                    warehouseName = warehouseName
                )
            } else null

            val addressPayload = DeliveryShipmentAddressDto(
                recipientName = recipientName.trim(),
                phoneNumber = phoneNumber.trim(),
                fullAddress = fullAddress.trim(),
                city = city.trim(),
                pincode = pincode.trim(),
                state = state.trim(),
                countryCode = "IN"
            )

            val packagePayload = PackageDimensionDto(
                weightKg = weightKg,
                dimensions = DimensionsDetailDto(
                    lengthCm = lengthCm,
                    widthCm = widthCm,
                    heightCm = heightCm
                )
            )

            val requestBody = CreateShipmentApiRequest(
                orderId = orderId,
                courierPartner = courierPartner,
                serviceType = serviceType,
                deliveryAddress = addressPayload,
                packageDetails = packagePayload,
                allocation = allocationPayload
            )

            val result = repo.request<CreateShipmentApiResponse> {
                post(CREATE_SHIPMENT_URL, requestBody)
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        fetchShipmentList() // Refresh the shipment list
                        onSuccess(res.message ?: "Shipment created & dispatched")
                    } else {
                        _createShipmentError.value = res.message ?: "Failed to create shipment"
                    }
                },
                onFailure = { err ->
                    _createShipmentError.value = err.message ?: "Failed to connect to server"
                }
            )
            _isCreatingShipment.value = false
        }
    }

    private val _isAssigningCourier = MutableStateFlow(false)
    val isAssigningCourier: StateFlow<Boolean> = _isAssigningCourier.asStateFlow()

    private val _assignCourierError = MutableStateFlow<String?>(null)
    val assignCourierError: StateFlow<String?> = _assignCourierError.asStateFlow()

    /**
     * Assigns a courier partner to a delivery order.
     * Endpoint: POST /api/logistics/delivery/assign-courier/:deliveryId
     */
    fun assignCourier(
        deliveryId: String,
        courierPartner: String,
        deliveryCharge: Double,
        instructions: String?,
        onSuccess: (message: String) -> Unit
    ) {
        viewModelScope.launch {
            _isAssigningCourier.value = true
            _assignCourierError.value = null

            val requestBody = AssignCourierRequest(
                courierPartner = courierPartner,
                deliveryCharge = deliveryCharge,
                instructions = instructions?.trim()?.ifBlank { null }
            )

            val result = repo.request<AssignCourierResponse> {
                post("$LOGISTICS_ASSIGN_COURIER_URL/$deliveryId", requestBody)
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        fetchDeliveries() // Refresh delivery list
                        onSuccess(res.message ?: "Courier assigned successfully")
                    } else {
                        _assignCourierError.value = res.message ?: "Failed to assign courier"
                    }
                },
                onFailure = { err ->
                    _assignCourierError.value = err.message ?: "Failed to connect to server"
                }
            )
            _isAssigningCourier.value = false
        }
    }

    private val _courierOverview = MutableStateFlow<CourierDeliveryOverviewData?>(null)
    val courierOverview: StateFlow<CourierDeliveryOverviewData?> = _courierOverview.asStateFlow()

    private val _isLoadingCourierOverview = MutableStateFlow(false)
    val isLoadingCourierOverview: StateFlow<Boolean> = _isLoadingCourierOverview.asStateFlow()

    private val _courierOverviewError = MutableStateFlow<String?>(null)
    val courierOverviewError: StateFlow<String?> = _courierOverviewError.asStateFlow()

    private val _isMarkingCourierCompleted = MutableStateFlow(false)
    val isMarkingCourierCompleted: StateFlow<Boolean> = _isMarkingCourierCompleted.asStateFlow()

    private val _courierDeliveriesList = MutableStateFlow<List<CourierManagementItemDto>>(emptyList())
    val courierDeliveriesList: StateFlow<List<CourierManagementItemDto>> = _courierDeliveriesList.asStateFlow()

    private val _courierSearchQuery = MutableStateFlow("")
    val courierSearchQuery: StateFlow<String> = _courierSearchQuery.asStateFlow()

    private val _isLoadingCourierDeliveries = MutableStateFlow(false)
    val isLoadingCourierDeliveries: StateFlow<Boolean> = _isLoadingCourierDeliveries.asStateFlow()

    private val _courierDeliveriesError = MutableStateFlow<String?>(null)
    val courierDeliveriesError: StateFlow<String?> = _courierDeliveriesError.asStateFlow()

    /**
     * Filtered reactive list for courier management items based on search query.
     */
    val filteredCourierDeliveries: StateFlow<List<CourierManagementItemDto>> = combine(
        _courierDeliveriesList,
        _courierSearchQuery
    ) { list, query ->
        val cleanQuery = query.trim().lowercase()
        if (cleanQuery.isEmpty()) {
            list
        } else {
            list.filter { item ->
                item.customer.lowercase().contains(cleanQuery) ||
                        item.orderId.lowercase().contains(cleanQuery) ||
                        item.deliveryId.lowercase().contains(cleanQuery) ||
                        (item.courierPartner?.lowercase()?.contains(cleanQuery) == true) ||
                        item.deliveryType.lowercase().contains(cleanQuery)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onCourierSearchQueryChange(query: String) {
        _courierSearchQuery.value = query
    }

    /**
     * Fetches courier deliveries list.
     * Endpoint: GET /api/logistics/courier-management/list
     */
    fun fetchCourierManagementList(page: Int = 1, limit: Int = 50) {
        viewModelScope.launch {
            _isLoadingCourierDeliveries.value = true
            _courierDeliveriesError.value = null

            val result = repo.request<CourierManagementListResponse> {
                get(COURIER_MANAGEMENT_LIST_URL, repo.query("page" to page, "limit" to limit))
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        _courierDeliveriesList.value = res.data
                    } else {
                        _courierDeliveriesError.value = res.message ?: "Failed to fetch courier deliveries"
                    }
                },
                onFailure = { err ->
                    _courierDeliveriesError.value = err.message ?: "Network error fetching courier deliveries"
                }
            )
            _isLoadingCourierDeliveries.value = false
        }
    }

    /**
     * Fetches the courier overview details.
     * Endpoint: GET /api/logistics/delivery/courier-overview/:id
     */
    fun fetchCourierOverview(deliveryId: String) {
        if (deliveryId.isBlank()) return
        viewModelScope.launch {
            _isLoadingCourierOverview.value = true
            _courierOverviewError.value = null

            val result = repo.request<CourierDeliveryOverviewResponse> {
                get("$COURIER_DELIVERY_OVERVIEW_URL/$deliveryId")
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success && res.data != null) {
                        _courierOverview.value = res.data
                    } else {
                        _courierOverviewError.value = res.message ?: "Failed to load courier delivery details"
                    }
                },
                onFailure = { err ->
                    _courierOverviewError.value = err.message ?: "Network error loading details"
                }
            )
            _isLoadingCourierOverview.value = false
        }
    }

    /**
     * Marks the courier delivery as completed.
     * Endpoint: PUT /api/logistics/delivery/mark-completed/:id
     */
    fun markCourierDeliveryCompleted(
        deliveryId: String,
        onSuccess: (message: String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isMarkingCourierCompleted.value = true
            _statusUpdateError.value = null

            val result = repo.request<SimpleLogisticsActionResponse> {
                put("$MARK_COURIER_COMPLETED_URL/$deliveryId")
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        fetchCourierOverview(deliveryId)
                        fetchDeliveries()
                        onSuccess(res.message ?: "Delivery marked as completed")
                    } else {
                        _statusUpdateError.value = res.message ?: "Failed to mark as completed"
                    }
                },
                onFailure = { err ->
                    _statusUpdateError.value = err.message ?: "Failed to connect to server"
                }
            )
            _isMarkingCourierCompleted.value = false
        }
    }

    //COURIER API

    private val _selectedCourierPartnerDetail = MutableStateFlow<CourierPartnerDetailData?>(null)
    val selectedCourierPartnerDetail: StateFlow<CourierPartnerDetailData?> = _selectedCourierPartnerDetail.asStateFlow()

    private val _isLoadingCourierPartnerDetail = MutableStateFlow(false)
    val isLoadingCourierPartnerDetail: StateFlow<Boolean> = _isLoadingCourierPartnerDetail.asStateFlow()

    private val _courierPartnerDetailError = MutableStateFlow<String?>(null)
    val courierPartnerDetailError: StateFlow<String?> = _courierPartnerDetailError.asStateFlow()

    private val _isTogglingCourierStatus = MutableStateFlow(false)
    val isTogglingCourierStatus: StateFlow<Boolean> = _isTogglingCourierStatus.asStateFlow()

    /**
     * Fetches single courier partner detail by ID.
     * Endpoint: GET /api/logistics/courier-partner/details/:id
     */
    fun fetchCourierPartnerDetail(courierId: String) {
        if (courierId.isBlank()) return
        viewModelScope.launch {
            _isLoadingCourierPartnerDetail.value = true
            _courierPartnerDetailError.value = null

            val result = repo.request<CourierPartnerDetailResponse> {
                get("$COURIER_PARTNER_DETAIL_URL/$courierId")
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success && res.data != null) {
                        _selectedCourierPartnerDetail.value = res.data
                    } else {
                        _courierPartnerDetailError.value = res.message ?: "Failed to load courier partner detail"
                    }
                },
                onFailure = { err ->
                    _courierPartnerDetailError.value = err.message ?: "Network error loading partner detail"
                }
            )
            _isLoadingCourierPartnerDetail.value = false
        }
    }

    /**
     * Enables or disables the courier partner status.
     * Endpoint: PUT /api/logistics/courier-partner/toggle-status/:id
     */
    /**
     * Enables or disables the courier partner status.
     * Endpoint: PUT /api/logistics/courier-partner/toggle-status/:id
     * Payload: { status: "Active" | "Inactive" }
     */
    fun toggleCourierPartnerStatus(
        courierId: String,
        nextStatus: String,
        onSuccess: (message: String) -> Unit = {}
    ) {
        if (courierId.isBlank()) return
        viewModelScope.launch {
            _isTogglingCourierStatus.value = true
            val requestBody = ToggleCourierStatusRequest(status = nextStatus)

            val result = repo.request<SimpleLogisticsActionResponse> {
                put("$COURIER_PARTNER_TOGGLE_STATUS_URL/$courierId", requestBody)
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        fetchCourierPartnerDetail(courierId)
                        fetchCourierPartners()
                        onSuccess(
                            res.message ?: "Courier ${if (nextStatus == "Active") "enabled" else "disabled"}"
                        )
                    }
                },
                onFailure = { /* Handle error if needed */ }
            )
            _isTogglingCourierStatus.value = false
        }
    }

    private val _isSubmittingCourier = MutableStateFlow(false)
    val isSubmittingCourier: StateFlow<Boolean> = _isSubmittingCourier.asStateFlow()

    private val _courierSubmitError = MutableStateFlow<String?>(null)
    val courierSubmitError: StateFlow<String?> = _courierSubmitError.asStateFlow()

    /**
     * Creates a new courier partner.
     * Endpoint: POST /api/logistics/courier-partner/create
     */
    fun createCourierPartner(
        request: CreateUpdateCourierPartnerRequest,
        onSuccess: (message: String) -> Unit
    ) {
        viewModelScope.launch {
            _isSubmittingCourier.value = true
            _courierSubmitError.value = null

            val result = repo.request<CreateCourierPartnerResponse> {
                post(COURIER_PARTNER_CREATE_URL, request)
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        fetchCourierPartners()
                        onSuccess(res.message ?: "Courier partner added successfully")
                    } else {
                        _courierSubmitError.value = res.message ?: "Failed to add courier partner"
                    }
                },
                onFailure = { err ->
                    _courierSubmitError.value = err.message ?: "Network error creating courier partner"
                }
            )
            _isSubmittingCourier.value = false
        }
    }

    /**
     * Updates an existing courier partner.
     * Endpoint: PUT /api/logistics/courier-partner/update/:id
     */
    fun updateCourierPartner(
        courierId: String,
        request: CreateUpdateCourierPartnerRequest,
        onSuccess: (message: String) -> Unit
    ) {
        viewModelScope.launch {
            _isSubmittingCourier.value = true
            _courierSubmitError.value = null

            val result = repo.request<CreateCourierPartnerResponse> {
                put("$COURIER_PARTNER_UPDATE_URL/$courierId", request)
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        fetchCourierPartnerDetail(courierId)
                        fetchCourierPartners()
                        onSuccess(res.message ?: "Courier partner updated successfully")
                    } else {
                        _courierSubmitError.value = res.message ?: "Failed to update courier partner"
                    }
                },
                onFailure = { err ->
                    _courierSubmitError.value = err.message ?: "Network error updating courier partner"
                }
            )
            _isSubmittingCourier.value = false
        }
    }

    // 2. Add inside LogisticsViewModel class:
    private val _trackingEventsList = MutableStateFlow<List<TrackingEventItemDto>>(emptyList())
    val trackingEventsList: StateFlow<List<TrackingEventItemDto>> = _trackingEventsList.asStateFlow()

    private val _trackingSearchQuery = MutableStateFlow("")
    val trackingSearchQuery: StateFlow<String> = _trackingSearchQuery.asStateFlow()

    private val _selectedTrackingStatus = MutableStateFlow<String?>(null)
    val selectedTrackingStatus: StateFlow<String?> = _selectedTrackingStatus.asStateFlow()

    private val _isLoadingTrackingEvents = MutableStateFlow(false)
    val isLoadingTrackingEvents: StateFlow<Boolean> = _isLoadingTrackingEvents.asStateFlow()

    private val _trackingEventsError = MutableStateFlow<String?>(null)
    val trackingEventsError: StateFlow<String?> = _trackingEventsError.asStateFlow()

    /**
     * Reactive filtered tracking events based on search query and status filter.
     */
    val filteredTrackingEvents: StateFlow<List<TrackingEventItemDto>> = combine(
        _trackingEventsList,
        _trackingSearchQuery,
        _selectedTrackingStatus
    ) { list, query, status ->
        val cleanQuery = query.trim().lowercase()
        list.filter { item ->
            val matchesQuery = cleanQuery.isEmpty() ||
                    item.shipmentId.lowercase().contains(cleanQuery) ||
                    item.location.lowercase().contains(cleanQuery) ||
                    item.updatedBy.lowercase().contains(cleanQuery) ||
                    (item.trackingId?.lowercase()?.contains(cleanQuery) == true)

            val matchesStatus = status.isNullOrBlank() || item.status.equals(status, ignoreCase = true)

            matchesQuery && matchesStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onTrackingSearchQueryChange(query: String) {
        _trackingSearchQuery.value = query
    }

    fun onTrackingStatusFilterChange(status: String?) {
        _selectedTrackingStatus.value = status
    }

    fun clearTrackingFilters() {
        _trackingSearchQuery.value = ""
        _selectedTrackingStatus.value = null
    }

    /**
     * Fetches tracking events list from the backend.
     * Endpoint: GET /api/logistics/tracking-events/list
     */
    fun fetchTrackingEvents(page: Int = 1, limit: Int = 20) {
        viewModelScope.launch {
            _isLoadingTrackingEvents.value = true
            _trackingEventsError.value = null

            val result = repo.request<TrackingEventsResponse> {
                get(LOGISTICS_TRACKING_EVENTS_URL, repo.query("page" to page, "limit" to limit))
            }

            result.fold(
                onSuccess = { res ->
                    if (res.success) {
                        _trackingEventsList.value = res.data
                    } else {
                        _trackingEventsError.value = res.message ?: "Failed to load tracking events"
                    }
                },
                onFailure = { err ->
                    _trackingEventsError.value = err.message ?: "Network error fetching tracking events"
                }
            )
            _isLoadingTrackingEvents.value = false
        }
    }

    /**
     * Formats ISO date to "Oct 29 2026" matching desktop screenshot.
     */
    fun formatTrackingDate(isoDate: String?): String {
        if (isoDate.isNullOrBlank()) return "—"
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = parser.parse(isoDate)
            val formatter = SimpleDateFormat("MMM dd yyyy", Locale.getDefault())
            date?.let { formatter.format(it) } ?: isoDate
        } catch (e: Exception) {
            isoDate
        }
    }

    fun clearCourierPartnerDetail() {
        _selectedCourierPartnerDetail.value = null
        _courierPartnerDetailError.value = null
    }

    fun clearCourierOverview() {
        _courierOverview.value = null
        _courierOverviewError.value = null
    }

    fun formatDateTimeToHuman(isoDate: String?): String {
        if (isoDate.isNullOrBlank()) return "--"
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = parser.parse(isoDate)
            val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
            date?.let { formatter.format(it) } ?: isoDate
        } catch (e: Exception) {
            isoDate
        }
    }

    fun formatDateOnly(isoDate: String?): String {
        if (isoDate.isNullOrBlank()) return "--"
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = parser.parse(isoDate)
            val formatter = SimpleDateFormat("MMM dd", Locale.getDefault())
            date?.let { formatter.format(it) } ?: isoDate
        } catch (e: Exception) {
            isoDate
        }
    }

    /**
     * Safe helper to format ISO dates (e.g. 2026-10-27T07:44:42.016Z) into "25 Feb 2026".
     */
    fun formatDeliveryDate(rawDate: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = parser.parse(rawDate)
            val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            date?.let { formatter.format(it) } ?: rawDate
        } catch (e: Exception) {
            rawDate
        }
    }
}