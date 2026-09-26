@file:Suppress("unused")
package com.cuso.tailor.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.tailor.database.entities.GarmentMeasurement
import com.cuso.tailor.model.inventory.ProductCategoryItem
import com.cuso.tailor.model.sales.myOrganizationResponse
import com.cuso.tailor.model.settings.*
import com.cuso.tailor.repository.AuthRepository
import com.cuso.tailor.repository.SessionManager
import com.cuso.tailor.repository.SettingsRepository
import com.cuso.tailor.utils.launchBusy
import com.google.gson.JsonParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private var fetchJob: Job? = null

    // ===========================================================
    // 1. ORGANIZATION STATE
    // ===========================================================

    private val _organization = MutableStateFlow<myOrganizationResponse?>(null)
    val organization: StateFlow<myOrganizationResponse?> = _organization.asStateFlow()

    private val _isLoadingOrganization = MutableStateFlow(false)
    val isLoadingOrganization: StateFlow<Boolean> = _isLoadingOrganization.asStateFlow()

    private val _organizationError = MutableStateFlow<String?>(null)
    val organizationError: StateFlow<String?> = _organizationError.asStateFlow()

    // ===========================================================
    // 2. SEGMENTS STATE
    // ===========================================================

    private val _segments = MutableStateFlow<List<SegmentItem>>(emptyList())
    val segments: StateFlow<List<SegmentItem>> = _segments.asStateFlow()

    private val _isLoadingSegments = MutableStateFlow(false)
    val isLoadingSegments: StateFlow<Boolean> = _isLoadingSegments.asStateFlow()

    private val _segmentsError = MutableStateFlow<String?>(null)
    val segmentsError: StateFlow<String?> = _segmentsError.asStateFlow()

    private val _isCreatingSegment = MutableStateFlow(false)
    val isCreatingSegment: StateFlow<Boolean> = _isCreatingSegment.asStateFlow()

    private val _segmentError = MutableStateFlow<String?>(null)
    val segmentError: StateFlow<String?> = _segmentError.asStateFlow()

    private val _selectedSegmentDetail = MutableStateFlow<SegmentItem?>(null)
    val selectedSegmentDetail: StateFlow<SegmentItem?> = _selectedSegmentDetail.asStateFlow()

    private val _isLoadingSegmentDetail = MutableStateFlow(false)
    val isLoadingSegmentDetail: StateFlow<Boolean> = _isLoadingSegmentDetail.asStateFlow()

    private val _isChangingSegmentStatus = MutableStateFlow(false)
    val isChangingSegmentStatus: StateFlow<Boolean> = _isChangingSegmentStatus.asStateFlow()

    // ===========================================================
    // 3. GARMENTS STATE
    // ===========================================================

    private val _garments = MutableStateFlow<List<GarmentItem>>(emptyList())
    val garments: StateFlow<List<GarmentItem>> = _garments.asStateFlow()

    private val _isLoadingGarments = MutableStateFlow(false)
    val isLoadingGarments: StateFlow<Boolean> = _isLoadingGarments.asStateFlow()

    private val _garmentsError = MutableStateFlow<String?>(null)
    val garmentsError: StateFlow<String?> = _garmentsError.asStateFlow()

    private val _isCreatingGarment = MutableStateFlow(false)
    val isCreatingGarment: StateFlow<Boolean> = _isCreatingGarment.asStateFlow()

    private val _garmentError = MutableStateFlow<String?>(null)
    val garmentError: StateFlow<String?> = _garmentError.asStateFlow()

    private val _isChangingGarmentStatus = MutableStateFlow(false)
    val isChangingGarmentStatus: StateFlow<Boolean> = _isChangingGarmentStatus.asStateFlow()

    private val _selectedGarment = MutableStateFlow<GarmentDetail?>(null)
    val selectedGarment = _selectedGarment.asStateFlow()

    private val _isFetchingDetail = MutableStateFlow(false)
    val isFetchingDetail = _isFetchingDetail.asStateFlow()

    private val _isUpdatingPrice = MutableStateFlow(false)
    val isUpdatingPrice = _isUpdatingPrice.asStateFlow()

    // ===========================================================
    // 4. GARMENT STYLES / CATEGORIES STATE
    // ===========================================================

    private val _garmentStyles = MutableStateFlow<List<GarmentStyleItem>>(emptyList())
    val garmentStyles: StateFlow<List<GarmentStyleItem>> = _garmentStyles.asStateFlow()

    private val _isLoadingStyles = MutableStateFlow(false)
    val isLoadingStyles: StateFlow<Boolean> = _isLoadingStyles.asStateFlow()

    private val _isCreatingStyle = MutableStateFlow(false)
    val isCreatingStyle: StateFlow<Boolean> = _isCreatingStyle.asStateFlow()

    private val _selectedSegmentIdForStyle = MutableStateFlow<String?>(null)
    val selectedSegmentIdForStyle: StateFlow<String?> = _selectedSegmentIdForStyle.asStateFlow()

    private val _selectedGarmentIdForStyle = MutableStateFlow<String?>(null)
    val selectedGarmentIdForStyle: StateFlow<String?> = _selectedGarmentIdForStyle.asStateFlow()

    private val _selectedGarmentTitleForStyle = MutableStateFlow("Garment Categories")
    val selectedGarmentTitleForStyle: StateFlow<String> = _selectedGarmentTitleForStyle.asStateFlow()

    private val _selectedStyleDetail = MutableStateFlow<GarmentStyleItem?>(null)
    val selectedStyleDetail: StateFlow<GarmentStyleItem?> = _selectedStyleDetail.asStateFlow()

    private val _isLoadingStyleDetail = MutableStateFlow(false)
    val isLoadingStyleDetail: StateFlow<Boolean> = _isLoadingStyleDetail.asStateFlow()

    // ===========================================================
    // 5. MEASUREMENT FIELDS & LOCAL STATE
    // ===========================================================

    private val _measurementFields = MutableStateFlow<List<MeasurementFieldItem>>(emptyList())
    val measurementFields: StateFlow<List<MeasurementFieldItem>> = _measurementFields.asStateFlow()

    private val _isLoadingMeasurementFields = MutableStateFlow(false)
    val isLoadingMeasurementFields: StateFlow<Boolean> = _isLoadingMeasurementFields.asStateFlow()

    private val _isDeactivatingField = MutableStateFlow(false)
    val isDeactivatingField = _isDeactivatingField.asStateFlow()

    private val _localMeasurements = MutableStateFlow<List<GarmentMeasurement>>(emptyList())
    val localMeasurements: StateFlow<List<GarmentMeasurement>> = _localMeasurements.asStateFlow()

    // ===========================================================
    // 6. WORK PRICING STATE
    // ===========================================================

    private val _workPricingList = MutableStateFlow<List<WorkPricingItem>>(emptyList())
    val workPricingList = _workPricingList.asStateFlow()

    private val _isLoadingWorkPricing = MutableStateFlow(false)
    val isLoadingWorkPricing = _isLoadingWorkPricing.asStateFlow()

    private val _selectedWorkDetail = MutableStateFlow<WorkPricingDetail?>(null)
    val selectedWorkDetail = _selectedWorkDetail.asStateFlow()

    private val _isFetchingWorkDetail = MutableStateFlow(false)
    val isFetchingWorkDetail = _isFetchingWorkDetail.asStateFlow()

    // ===========================================================
    // 7. INVENTORY LOCATION STRUCTURE STATE
    // ===========================================================

    private val _productCategories = MutableStateFlow<List<ProductCategoryItem>>(emptyList())
    val productCategories: StateFlow<List<ProductCategoryItem>> = _productCategories.asStateFlow()

    private val _floors = MutableStateFlow<List<FloorItemSettings>>(emptyList())
    val floors: StateFlow<List<FloorItemSettings>> = _floors.asStateFlow()

    private val _sections = MutableStateFlow<List<SectionItem>>(emptyList())
    val sections: StateFlow<List<SectionItem>> = _sections.asStateFlow()

    private val _racks = MutableStateFlow<List<RackItem>>(emptyList())
    val racks: StateFlow<List<RackItem>> = _racks.asStateFlow()

    private val _bins = MutableStateFlow<List<BinItem>>(emptyList())
    val bins: StateFlow<List<BinItem>> = _bins.asStateFlow()

    private val _isLoadingLocationStructure = MutableStateFlow(false)
    val isLoadingLocationStructure: StateFlow<Boolean> = _isLoadingLocationStructure.asStateFlow()

    private val _floorErrorMessage = MutableStateFlow<String?>(null)
    val floorErrorMessage: StateFlow<String?> = _floorErrorMessage.asStateFlow()

    private var floorCurrentPage = 1
    private var isFloorEndReached = false
    private val _isPaginatingFloors = MutableStateFlow(false)
    val isPaginatingFloors: StateFlow<Boolean> = _isPaginatingFloors.asStateFlow()

    private var sectionCurrentPage = 1
    private var isSectionEndReached = false
    private val _isPaginatingSections = MutableStateFlow(false)
    val isPaginatingSections: StateFlow<Boolean> = _isPaginatingSections.asStateFlow()

    private var rackCurrentPage = 1
    private var isRackEndReached = false
    private val _isPaginatingRacks = MutableStateFlow(false)
    val isPaginatingRacks: StateFlow<Boolean> = _isPaginatingRacks.asStateFlow()

    private var binCurrentPage = 1
    private var isBinEndReached = false
    private val _isPaginatingBins = MutableStateFlow(false)
    val isPaginatingBins: StateFlow<Boolean> = _isPaginatingBins.asStateFlow()

    // Edit Selected States
    private val _selectedFloorForEdit = MutableStateFlow<FloorItemSettings?>(null)
    val selectedFloorForEdit: StateFlow<FloorItemSettings?> = _selectedFloorForEdit.asStateFlow()

    private val _selectedSectionForEdit = MutableStateFlow<SectionItem?>(null)
    val selectedSectionForEdit: StateFlow<SectionItem?> = _selectedSectionForEdit.asStateFlow()

    private val _selectedRackForEdit = MutableStateFlow<RackItem?>(null)
    val selectedRackForEdit: StateFlow<RackItem?> = _selectedRackForEdit.asStateFlow()

    private val _selectedBinForEdit = MutableStateFlow<BinItem?>(null)
    val selectedBinForEdit: StateFlow<BinItem?> = _selectedBinForEdit.asStateFlow()

    // ===========================================================
    // 8. DESIGNS STATE
    // ===========================================================

    private val _designs = MutableStateFlow<List<DesignItem>>(emptyList())
    val designs: StateFlow<List<DesignItem>> = _designs.asStateFlow()

    private val _selectedDesign = MutableStateFlow<DesignItem?>(null)
    val selectedDesign: StateFlow<DesignItem?> = _selectedDesign.asStateFlow()

    private val _isLoadingDesigns = MutableStateFlow(false)
    val isLoadingDesigns: StateFlow<Boolean> = _isLoadingDesigns.asStateFlow()

    private val _isSubmittingDesign = MutableStateFlow(false)
    val isSubmittingDesign: StateFlow<Boolean> = _isSubmittingDesign.asStateFlow()

    private val _designError = MutableStateFlow<String?>(null)
    val designError: StateFlow<String?> = _designError.asStateFlow()

    // Generic Action & Dynamic Message States
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _dynamicSuccessMessage = MutableStateFlow<String?>(null)
    val dynamicSuccessMessage: StateFlow<String?> = _dynamicSuccessMessage.asStateFlow()

    private val _dynamicErrorMessage = MutableStateFlow<String?>(null)
    val dynamicErrorMessage: StateFlow<String?> = _dynamicErrorMessage.asStateFlow()

    private val _isLoadingBins = MutableStateFlow(false)
    val isLoadingBins: StateFlow<Boolean> = _isLoadingBins.asStateFlow()

    // ===========================================================
    // 9. UTILITY & MESSAGE METHODS
    // ===========================================================

    fun showSuccess(msg: String) {
        _dynamicSuccessMessage.value = msg
    }

    fun showError(msg: String) {
        _dynamicErrorMessage.value = msg
    }

    fun clearSuccessMessage() {
        _dynamicSuccessMessage.value = null
    }

    fun clearDynamicErrorMessage() {
        _dynamicErrorMessage.value = null
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
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

    fun logout(onComplete: () -> Unit) {
        launchBusy {
            sessionManager.logout()
            clearOrganization()
            onComplete()
        }
    }

    // ===========================================================
    // 10. ORGANIZATION ACTIONS
    // ===========================================================

    fun fetchMyOrganization(token: String) {
        launchBusy {
            _isLoadingOrganization.value = true
            _organizationError.value = null
            try {
                val result = authRepository.getMyOrganization(token)
                if (result.isSuccess) {
                    _organization.value = result.getOrNull()
                } else {
                    _organizationError.value = result.exceptionOrNull()?.message ?: "Failed to fetch organization"
                }
            } catch (e: Exception) {
                _organizationError.value = e.message ?: "An error occurred"
            } finally {
                _isLoadingOrganization.value = false
            }
        }
    }

    fun clearOrganization() {
        _organization.value = null
        _organizationError.value = null
    }

    // ===========================================================
    // 11. SEGMENT ACTIONS
    // ===========================================================

    fun fetchSegments() {
        launchBusy {
            _isLoadingSegments.value = true
            _segmentsError.value = null
            val result = settingsRepository.getSegments()
            _isLoadingSegments.value = false
            if (result.isSuccess) {
                _segments.value = result.getOrDefault(emptyList()).sortedBy { it.displayOrder }
            } else {
                _segmentsError.value = result.exceptionOrNull()?.message ?: "Failed to load segments"
            }
        }
    }

    fun fetchSegmentById(id: String) {
        launchBusy {
            _isLoadingSegmentDetail.value = true
            val result = settingsRepository.getSegmentById(id)
            _isLoadingSegmentDetail.value = false
            if (result.isSuccess) {
                _selectedSegmentDetail.value = result.getOrNull()
            } else {
                _segmentError.value = result.exceptionOrNull()?.message ?: "Failed to fetch segment"
            }
        }
    }

    fun clearSelectedSegmentDetail() {
        _selectedSegmentDetail.value = null
    }

    fun createSegment(
        name: String,
        code: String,
        description: String?,
        displayOrder: Int,
        status: Boolean,
        onSuccess: (CreateSegmentResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        launchBusy {
            _isCreatingSegment.value = true
            _segmentError.value = null

            val request = CreateSegmentRequest(
                name = name.trim(),
                displayName = name.trim(),
                code = code.trim().uppercase(),
                description = description?.takeIf { it.isNotBlank() },
                displayOrder = displayOrder
            )

            val result = settingsRepository.createSegment(request)
            _isCreatingSegment.value = false

            if (result.isSuccess) {
                result.getOrNull()?.let { response -> onSuccess(response) }
            } else {
                val error = result.exceptionOrNull()?.message ?: "Failed to create segment"
                _segmentError.value = error
                onError(error)
            }
        }
    }

    fun updateSegment(
        id: String,
        name: String,
        code: String,
        description: String?,
        displayOrder: Int,
        status: String?,
        onSuccess: (CreateSegmentResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        launchBusy {
            _isCreatingSegment.value = true
            _segmentError.value = null

            val request = CreateSegmentRequest(
                name = name.trim(),
                displayName = name.trim(),
                code = code.trim().uppercase(),
                description = description?.takeIf { it.isNotBlank() },
                displayOrder = displayOrder,
                status = status ?: ""
            )

            val result = settingsRepository.updateSegment(id, request)
            _isCreatingSegment.value = false

            if (result.isSuccess) {
                result.getOrNull()?.let { response ->
                    fetchSegments()
                    onSuccess(response)
                }
            } else {
                val error = result.exceptionOrNull()?.message ?: "Failed to update segment"
                _segmentError.value = error
                onError(error)
            }
        }
    }

    fun deleteSegment(
        id: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        launchBusy {
            val result = settingsRepository.deleteSegment(id)
            if (result.isSuccess) {
                val message = result.getOrNull()?.message ?: "Segment deleted successfully"
                fetchSegments()
                onSuccess(message)
            } else {
                val error = result.exceptionOrNull()?.message ?: "Failed to delete segment"
                onError(error)
            }
        }
    }

    fun changeSegmentStatus(
        id: String,
        status: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        launchBusy {
            _isChangingSegmentStatus.value = true
            _segmentError.value = null

            val request = ChangeSegmentStatusRequest(status = status)
            val result = settingsRepository.changeSegmentStatus(id, request)
            _isChangingSegmentStatus.value = false

            if (result.isSuccess) {
                val response = result.getOrNull()
                fetchSegments()
                val msg = response?.message ?: "Segment status updated successfully"
                showSuccess(msg)
                onSuccess(msg)
            } else {
                val error = extractErrorMessage(result.exceptionOrNull()?.message)
                _segmentError.value = error
                showError(error)
                onError(error)
            }
        }
    }

    // ===========================================================
    // 12. GARMENT ACTIONS
    // ===========================================================

    fun fetchGarments() {
        viewModelScope.launch {
            _isLoadingGarments.value = true
            _garmentsError.value = null
            settingsRepository.getGarments(paginate = false, status = "Active")
                .onSuccess { items ->
                    _garments.value = items
                }
                .onFailure { error ->
                    _garmentsError.value = error.message
                }
            _isLoadingGarments.value = false
        }
    }

    fun fetchGarmentDetail(id: String) {
        viewModelScope.launch {
            _isFetchingDetail.value = true
            settingsRepository.getGarmentDetail(id).onSuccess { detail ->
                _selectedGarment.value = detail
            }.onFailure { exception ->
                val clean = extractErrorMessage(exception.message)
                showError(clean)
            }
            _isFetchingDetail.value = false
        }
    }

    fun clearSelectedGarment() {
        _selectedGarment.value = null
    }

    fun createGarment(
        context: Context,
        name: String,
        code: String,
        description: String?,
        applicableSegmentIds: List<String>,
        imageUri: Uri?,
        onSuccess: (CreateGarmentResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        launchBusy {
            _isCreatingGarment.value = true
            _garmentError.value = null

            val result = settingsRepository.createGarment(
                context = context,
                name = name.trim(),
                code = code.trim().uppercase(),
                description = description?.takeIf { it.isNotBlank() },
                applicableSegmentIds = applicableSegmentIds,
                imageUri = imageUri
            )
            _isCreatingGarment.value = false

            if (result.isSuccess) {
                result.getOrNull()?.let { response -> onSuccess(response) }
            } else {
                val error = result.exceptionOrNull()?.message ?: "Failed to create garment"
                _garmentError.value = error
                onError(error)
            }
        }
    }

    fun updateGarment(
        context: Context,
        id: String,
        name: String,
        description: String?,
        applicableSegmentIds: List<String>,
        imageUri: Uri?,
        onSuccess: (CreateGarmentResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        launchBusy {
            _isCreatingGarment.value = true
            _garmentError.value = null

            val result = settingsRepository.updateGarment(
                context = context,
                id = id,
                name = name.trim(),
                description = description?.takeIf { it.isNotBlank() },
                applicableSegmentIds = applicableSegmentIds,
                imageUri = imageUri
            )
            _isCreatingGarment.value = false

            if (result.isSuccess) {
                result.getOrNull()?.let { response ->
                    fetchGarments()
                    onSuccess(response)
                }
            } else {
                val error = result.exceptionOrNull()?.message ?: "Failed to update garment"
                _garmentError.value = error
                onError(error)
            }
        }
    }

    fun changeGarmentStatus(
        id: String,
        status: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        launchBusy {
            _isChangingGarmentStatus.value = true
            _garmentError.value = null

            val request = ChangeGarmentStatusRequest(status = status)
            val result = settingsRepository.changeGarmentStatus(id, request)
            _isChangingGarmentStatus.value = false

            if (result.isSuccess) {
                val response = result.getOrNull()
                fetchGarments()
                val msg = response?.message ?: "Garment status updated successfully"
                showSuccess(msg)
                onSuccess(msg)
            } else {
                val error = extractErrorMessage(result.exceptionOrNull()?.message)
                _garmentError.value = error
                showError(error)
                onError(error)
            }
        }
    }

    fun updateGarmentBasicPrice(
        id: String,
        price: Double,
        isActive: Boolean,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isUpdatingPrice.value = true
            val request = UpdateGarmentBasicPriceRequest(
                baseStitchingCharge = price,
                isActive = isActive
            )

            settingsRepository.updateGarmentBasicPrice(id, request)
                .onSuccess { response ->
                    fetchGarments()
                    val msg = response.message ?: "Pricing updated successfully"
                    showSuccess(msg)
                    onSuccess(msg)
                }
                .onFailure { error ->
                    val clean = extractErrorMessage(error.message)
                    showError(clean)
                    onError(clean)
                }
            _isUpdatingPrice.value = false
        }
    }

    fun deleteGarment(
        id: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        launchBusy {
            val result = settingsRepository.deleteGarment(id)
            if (result.isSuccess) {
                val message = result.getOrNull()?.message ?: "Garment deleted successfully"
                fetchGarments()
                showSuccess(message)
                onSuccess(message)
            } else {
                val error = extractErrorMessage(result.exceptionOrNull()?.message)
                showError(error)
                onError(error)
            }
        }
    }

    // ===========================================================
    // 13. GARMENT STYLES / CATEGORIES ACTIONS
    // ===========================================================

    fun setSelectedGarmentForDetail(segmentId: String?, garmentId: String?, title: String) {
        _selectedSegmentIdForStyle.value = segmentId
        _selectedGarmentIdForStyle.value = garmentId
        _selectedGarmentTitleForStyle.value = title
        fetchGarmentStyles(segmentId = segmentId, garmentId = garmentId)
    }

    fun fetchGarmentStyles(segmentId: String?, garmentId: String?) {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            _garmentStyles.value = emptyList()
            _isLoadingStyles.value = true
            _errorMessage.value = null

            val normSegmentId = segmentId?.trim()?.takeIf { it.isNotBlank() }
            val normGarmentId = garmentId?.trim()?.takeIf { it.isNotBlank() }

            settingsRepository.getGarmentStyles(
                segmentId = normSegmentId,
                garmentId = normGarmentId
            ).collect { result ->
                _isLoadingStyles.value = false
                result.onSuccess { list ->
                    _garmentStyles.value = list.filter { item ->
                        val itemSegmentId = item.segment?.id?.trim()
                        val itemGarmentId = item.garment?.id?.trim()
                        val matchSegment = normSegmentId == null || itemSegmentId == normSegmentId
                        val matchGarment = normGarmentId == null || itemGarmentId == normGarmentId
                        matchSegment && matchGarment
                    }
                }.onFailure { exception ->
                    _errorMessage.value = exception.localizedMessage ?: "Failed to fetch styles"
                }
            }
        }
    }

    fun fetchGarmentCategoryById(id: String) {
        launchBusy {
            _isLoadingStyleDetail.value = true
            _errorMessage.value = null

            val result = settingsRepository.getGarmentCategoryById(id)
            _isLoadingStyleDetail.value = false

            result.onSuccess { item ->
                _selectedStyleDetail.value = item
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Failed to fetch garment category"
            }
        }
    }

    fun clearSelectedStyleDetail() {
        _selectedStyleDetail.value = null
    }

    fun createGarmentStyle(
        name: String,
        displayName: String,
        sku: String? = null,
        description: String? = null,
        segmentId: String,
        garmentId: String,
        styleTags: List<String> = emptyList(),
        sleeveStyle: String? = null,
        stitchingCharge: Double = 0.0,
        isCustomStitchable: Boolean = true,
        onSuccess: (GarmentStyleItem) -> Unit,
        incomeAccount: String? = null,
        onError: (String) -> Unit
    ) {
        launchBusy {
            _isCreatingStyle.value = true
            _errorMessage.value = null

            val autoSku = sku?.takeIf { it.isNotBlank() }
                ?: (name.trim().uppercase().replace(" ", "-") + "-001")

            val request = CreateGarmentStyleRequest(
                name = name.trim(),
                displayName = displayName.trim(),
                sku = autoSku,
                description = description?.takeIf { it.isNotBlank() },
                segmentId = segmentId,
                garmentId = garmentId,
                styleTags = styleTags,
                sleeveStyle = sleeveStyle,
                stitchingCharge = stitchingCharge,
                isCustomStitchable = isCustomStitchable,
                incomeAccount = incomeAccount
            )

            val result = settingsRepository.createGarmentStyle(request)
            _isCreatingStyle.value = false

            result.onSuccess { item ->
                fetchGarmentStyles(segmentId, garmentId)
                onSuccess(item)
            }.onFailure { error ->
                _errorMessage.value = error.message
                onError(error.message ?: "Failed to create garment category")
            }
        }
    }

    fun updateGarmentStyle(
        id: String,
        name: String,
        displayName: String,
        sku: String? = null,
        description: String? = null,
        segmentId: String,
        garmentId: String,
        stitchingCharge: Double = 0.0,
        styleTags: List<String> = emptyList(),
        sleeveStyle: String? = null,
        onSuccess: (GarmentStyleItem) -> Unit,
        onError: (String) -> Unit
    ) {
        launchBusy {
            _isCreatingStyle.value = true
            _errorMessage.value = null

            val request = CreateGarmentStyleRequest(
                name = name.trim(),
                displayName = displayName.trim(),
                sku = sku,
                description = description?.takeIf { it.isNotBlank() },
                segmentId = segmentId,
                styleTags = styleTags,
                sleeveStyle = sleeveStyle,
                stitchingCharge = stitchingCharge,
                isCustomStitchable = true
            )

            val result = settingsRepository.updateGarmentStyle(id, request)
            _isCreatingStyle.value = false

            result.onSuccess { item ->
                fetchGarmentStyles(segmentId, garmentId)
                onSuccess(item)
            }.onFailure { error ->
                _errorMessage.value = error.message
                onError(error.message ?: "Failed to update garment category")
            }
        }
    }

    fun deleteGarmentStyle(
        id: String,
        segmentId: String?,
        garmentId: String?,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        launchBusy {
            val result = settingsRepository.deleteGarmentStyle(id)
            result.onSuccess { message ->
                fetchGarmentStyles(segmentId, garmentId)
                onSuccess(message)
            }.onFailure { error ->
                _errorMessage.value = error.message
                onError(error.message ?: "Failed to delete garment category")
            }
        }
    }

    fun changeGarmentCategoryStatus(
        categoryId: String,
        currentStatus: String,
        segmentId: String? = null,
        garmentId: String? = null,
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val nextStatus = when (currentStatus.lowercase()) {
            "active" -> "Inactive"
            "draft", "inactive" -> "Active"
            else -> "Active"
        }

        viewModelScope.launch {
            _isLoadingStyles.value = true
            val result = settingsRepository.changeGarmentCategoryStatus(categoryId, nextStatus)
            _isLoadingStyles.value = false

            result.fold(
                onSuccess = {
                    _garmentStyles.value = _garmentStyles.value.map {
                        if (it.id == categoryId) it.copy(status = nextStatus) else it
                    }
                    val msg = "Status successfully updated to $nextStatus"
                    showSuccess(msg)
                    onSuccess(msg)
                },
                onFailure = { error ->
                    val clean = extractErrorMessage(error.localizedMessage)
                    _errorMessage.value = clean
                    showError(clean)
                    onError(clean)
                }
            )
        }
    }

    fun activateGarmentStyleConfiguration(
        style: GarmentStyleItem,
        measurements: List<GarmentMeasurement>,
        onSuccess: (GarmentStyleItem) -> Unit,
        onError: (String) -> Unit
    ) {
        launchBusy {
            _isLoadingStyles.value = true
            _errorMessage.value = null

            val segmentIdStr = style.segment?.id?.takeIf { it.isNotBlank() }
                ?: _selectedSegmentIdForStyle.value?.takeIf { it.isNotBlank() }
            val garmentIdStr = style.garment?.id?.takeIf { it.isNotBlank() }
                ?: _selectedGarmentIdForStyle.value?.takeIf { it.isNotBlank() }

            val measurementEntries = measurements.mapIndexed { index, m ->
                StyleMeasurementFieldEntryRequest(
                    fieldId = m.id,
                    isRequired = m.isRequired,
                    displayOrder = index + 1
                )
            }

            val request = CreateGarmentStyleRequest(
                name = style.name,
                displayName = style.displayName ?: style.name,
                sku = style.sku,
                description = style.description,
                measurementFields = measurementEntries,
                styleTags = style.styleTags,
                sleeveStyle = style.sleeveStyle,
                stitchingCharge = style.stitchingCharge,
                isCustomStitchable = style.isCustomStitchable
            )

            val result = settingsRepository.updateGarmentStyle(style.id, request)
            _isLoadingStyles.value = false

            result.onSuccess { updatedStyle ->
                fetchGarmentStyles(segmentIdStr, garmentIdStr)
                onSuccess(updatedStyle)
            }.onFailure { error ->
                val cleanMsg = extractErrorMessage(error.message)
                _errorMessage.value = cleanMsg
                onError(cleanMsg)
            }
        }
    }

    fun saveGarmentProfileMeasurements(
        style: GarmentStyleItem,
        measurements: List<GarmentMeasurement>,
        onSuccess: (GarmentStyleItem) -> Unit,
        onError: (String) -> Unit
    ) {
        launchBusy {
            _isLoadingStyles.value = true
            _errorMessage.value = null

            val segmentIdStr = style.segment?.id?.takeIf { it.isNotBlank() }
                ?: _selectedSegmentIdForStyle.value?.takeIf { it.isNotBlank() }
            val garmentIdStr = style.garment?.id?.takeIf { it.isNotBlank() }
                ?: _selectedGarmentIdForStyle.value?.takeIf { it.isNotBlank() }

            val measurementEntries = measurements.mapIndexed { index, m ->
                StyleMeasurementFieldEntryRequest(
                    fieldId = m.id,
                    isRequired = m.isRequired,
                    displayOrder = index + 1
                )
            }

            val request = UpdateGarmentStyleRequest(
                name = style.name,
                displayName = style.displayName ?: style.name,
                sku = style.sku,
                description = style.description,
                measurementFields = measurementEntries,
                styleTags = style.styleTags,
                sleeveStyle = style.sleeveStyle,
                stitchingCharge = style.stitchingCharge,
                isCustomStitchable = style.isCustomStitchable
            )

            val result = settingsRepository.updateMeasurementField(style.id, request)
            _isLoadingStyles.value = false

            result.onSuccess { updatedStyle ->
                fetchGarmentStyles(segmentIdStr, garmentIdStr)
                onSuccess(updatedStyle)
            }.onFailure { error ->
                val cleanMsg = extractErrorMessage(error.message)
                _errorMessage.value = cleanMsg
                onError(cleanMsg)
            }
        }
    }

    // ===========================================================
    // 14. MEASUREMENT FIELD ACTIONS
    // ===========================================================

    fun fetchMeasurementFields() {
        launchBusy {
            _isLoadingMeasurementFields.value = true
            _errorMessage.value = null
            _dynamicErrorMessage.value = null
            val result = settingsRepository.getMeasurementFields()
            _isLoadingMeasurementFields.value = false

            result.onSuccess { list ->
                _measurementFields.value = list
            }.onFailure { error ->
                val clean = extractErrorMessage(error.message)
                _errorMessage.value = clean
                _dynamicErrorMessage.value = clean
            }
        }
    }

    fun createMeasurementField(
        name: String,
        displayName: String,
        code: String,
        grpName: String,
        description: String?,
        inputType: String,
        unit: String?,
        minValue: Double?,
        maxValue: Double?,
        options: List<String> = emptyList(),
        onSuccess: (MeasurementFieldItem) -> Unit,
        onError: (String) -> Unit
    ) {
        launchBusy {
            _isLoadingMeasurementFields.value = true
            _errorMessage.value = null

            val request = CreateMeasurementFieldRequest(
                name = name.trim(),
                displayName = displayName.trim(),
                code = code.trim().uppercase(),
                groupName = grpName,
                description = description?.takeIf { it.isNotBlank() },
                inputType = inputType,
                unit = unit,
                minValue = minValue,
                maxValue = maxValue,
                options = options
            )

            val result = settingsRepository.createMeasurementField(request)
            _isLoadingMeasurementFields.value = false

            result.onSuccess { item ->
                fetchMeasurementFields()
                onSuccess(item)
            }.onFailure { error ->
                _errorMessage.value = error.message
                onError(error.message ?: "Failed to create measurement field")
            }
        }
    }

    fun changeMeasurementFieldStatus(
        fieldId: String,
        nextStatus: String,
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoadingMeasurementFields.value = true
            val result = settingsRepository.changeMeasurementFieldStatus(fieldId, nextStatus)
            _isLoadingMeasurementFields.value = false

            result.fold(
                onSuccess = { message ->
                    _measurementFields.value = _measurementFields.value.map { item ->
                        if (item.id == fieldId) item.copy(status = nextStatus) else item
                    }
                    showSuccess(message)
                    onSuccess(message)
                },
                onFailure = { error ->
                    val cleanError = extractErrorMessage(error.message)
                    showError(cleanError)
                    onError(cleanError)
                }
            )
        }
    }

    fun deactivateMeasurementField(
        fieldId: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            settingsRepository.deactivateMeasurementField(fieldId)
                .onSuccess { response -> onSuccess(response.message) }
                .onFailure { exception -> onError(exception.localizedMessage ?: "Failed to deactivate field") }
        }
    }

    // ===========================================================
    // 15. LOCAL ROOM DB ACTIONS
    // ===========================================================

    fun loadLocalMeasurements(categoryId: String) {
        viewModelScope.launch {
            settingsRepository.getLocalMeasurements(categoryId).collect { garment ->
                _localMeasurements.value = garment?.measurements ?: emptyList()
            }
        }
    }

    fun saveSelectedFieldsToLocal(
        categoryId: String,
        categoryName: String,
        selectedFields: List<MeasurementFieldItem>,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val current = _localMeasurements.value.toMutableList()
            val existingIds = current.map { it.id }.toSet()

            selectedFields.forEach { item ->
                if (!existingIds.contains(item.id)) {
                    current.add(
                        GarmentMeasurement(
                            id = item.id,
                            label = item.displayName ?: item.name,
                            unit = item.unit ?: "inch",
                            inputType = item.inputType,
                            isRequired = false,
                            displayOrder = current.size + 1
                        )
                    )
                }
            }

            settingsRepository.saveSelectedFieldsToRoom(categoryId, categoryName, current)
            _localMeasurements.value = current
            onComplete()
        }
    }

    fun removeLocalMeasurementField(categoryId: String, fieldId: String) {
        viewModelScope.launch {
            val updated = _localMeasurements.value.filter { it.id != fieldId }
            settingsRepository.deleteLocalMeasurementField(categoryId, fieldId, _localMeasurements.value)
            _localMeasurements.value = updated
        }
    }

    // ===========================================================
    // 16. WORK PRICING ACTIONS
    // ===========================================================

    fun fetchWorkPricing(segmentId: String? = null, status: String? = "Active") {
        viewModelScope.launch {
            _isLoadingWorkPricing.value = true
            settingsRepository.fetchWorkPricing(segmentId, status)
                .onSuccess { list -> _workPricingList.value = list }
                .onFailure { exception ->
                    val clean = extractErrorMessage(exception.message)
                    showError(clean)
                }
            _isLoadingWorkPricing.value = false
        }
    }

    fun fetchWorkPricingDetail(id: String) {
        viewModelScope.launch {
            _isFetchingWorkDetail.value = true
            settingsRepository.getWorkPricingViewOne(id).onSuccess {
                _selectedWorkDetail.value = it
            }.onFailure {
                val clean = "Unable to load pricing details"
                _errorMessage.value = clean
                showError(clean)
            }
            _isFetchingWorkDetail.value = false
        }
    }

    fun clearWorkPricingDetail() {
        _selectedWorkDetail.value = null
        _errorMessage.value = null
    }

    fun createWorkPricing(
        request: WorkPricingRequest,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            _errorMessage.value = null

            val result = settingsRepository.createWorkPricing(request)

            result.onSuccess { response ->
                fetchWorkPricing(request.segmentId)
                val msg = response.message ?: "Work pricing created successfully"
                showSuccess(msg)
                onSuccess(msg)
            }.onFailure { exception ->
                val cleanError = extractErrorMessage(exception.message)
                _errorMessage.value = cleanError
                showError(cleanError)
                onError(cleanError)
            }
            _isSaving.value = false
        }
    }

    fun updateWorkPricing(
        id: String,
        request: WorkPricingRequest,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            settingsRepository.updateWorkPricing(id, request)
                .onSuccess { response ->
                    fetchWorkPricing(request.segmentId)
                    val msg = response.message ?: "Update successful"
                    showSuccess(msg)
                    onSuccess(msg)
                }
                .onFailure { error ->
                    val clean = extractErrorMessage(error.message)
                    showError(clean)
                    onError(clean)
                }
            _isSaving.value = false
        }
    }

    fun changeWorkPricingStatus(item: WorkPricingItem) {
        val newStatus = if (item.status.equals("Active", ignoreCase = true)) "Inactive" else "Active"

        viewModelScope.launch {
            settingsRepository.changeWorkPricingStatus(item.id, newStatus).onSuccess { updatedItem ->
                val updatedList = _workPricingList.value.map {
                    if (it.id == updatedItem.id) updatedItem else it
                }
                _workPricingList.value = updatedList
                showSuccess("Status changed to $newStatus")
            }.onFailure { error ->
                val clean = extractErrorMessage(error.message)
                showError(clean)
            }
        }
    }

    // ===========================================================
    // 17. INVENTORY PRODUCT CATEGORY ACTIONS
    // ===========================================================

    fun fetchProductCategories() {
        viewModelScope.launch {
            settingsRepository.getProductCategories()
                .onSuccess { list -> _productCategories.value = list }
                .onFailure { error -> showError(extractErrorMessage(error.message)) }
        }
    }

    // ===========================================================
    // 18. INVENTORY FLOOR ACTIONS
    // ===========================================================

    fun setSelectedFloorForEdit(floor: FloorItemSettings?) {
        _selectedFloorForEdit.value = floor
    }

    fun clearSelectedFloorForEdit() {
        _selectedFloorForEdit.value = null
    }

    fun fetchFloors(warehouseId: String? = null, isRefresh: Boolean = false) {
        if (isRefresh) {
            floorCurrentPage = 1
            isFloorEndReached = false
        }
        if (_isLoadingLocationStructure.value || _isPaginatingFloors.value || isFloorEndReached) return

        viewModelScope.launch {
            if (floorCurrentPage == 1) {
                _isLoadingLocationStructure.value = true
            } else {
                _isPaginatingFloors.value = true
            }
            _floorErrorMessage.value = null

            settingsRepository.getFloors(warehouseId = warehouseId, page = floorCurrentPage, limit = 20)
                .onSuccess { floorList ->
                    if (floorList.isEmpty()) {
                        isFloorEndReached = true
                    } else {
                        if (floorCurrentPage == 1) {
                            _floors.value = floorList
                        } else {
                            _floors.value = (_floors.value + floorList).distinctBy { it.id }
                        }
                        floorCurrentPage++
                    }
                }
                .onFailure { exception ->
                    val clean = extractErrorMessage(exception.message)
                    _floorErrorMessage.value = clean
                    showError(clean)
                }

            _isLoadingLocationStructure.value = false
            _isPaginatingFloors.value = false
        }
    }

    fun createFloor(
        warehouseId: String,
        name: String,
        code: String,
        sequenceOrder: Int,
        description: String?,
        temperatureZone: String,
        floorAreaSqft: Double,
        maxWeightCapacityKg: Double,
        status: String,
        onSuccess: (FloorItem) -> Unit,
        onError: (String) -> Unit
    ) {
        launchBusy {
            _isLoadingLocationStructure.value = true
            val req = CreateFloorRequest(
                warehouseId = warehouseId,
                name = name.trim(),
                code = code.trim().uppercase(),
                sequenceOrder = sequenceOrder,
                description = description?.takeIf { it.isNotBlank() },
                temperatureZone = temperatureZone,
                floorAreaSqft = floorAreaSqft,
                maxWeightCapacityKg = maxWeightCapacityKg,
                status = status
            )
            val res = settingsRepository.createFloor(req)
            _isLoadingLocationStructure.value = false
            res.onSuccess {
                fetchFloors(warehouseId, isRefresh = true)
                showSuccess("Floor created successfully")
                onSuccess(it)
            }.onFailure {
                val clean = extractErrorMessage(it.message)
                _errorMessage.value = clean
                showError(clean)
                onError(clean)
            }
        }
    }

    fun updateFloor(
        floorId: String,
        warehouseId: String,
        name: String,
        code: String,
        sequenceOrder: Int,
        description: String,
        temperatureZone: String,
        floorAreaSqft: Double,
        maxWeightCapacityKg: Double,
        status: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val request = CreateFloorRequest(
                warehouseId = warehouseId,
                name = name,
                code = code,
                sequenceOrder = sequenceOrder,
                description = description.ifBlank { null },
                temperatureZone = temperatureZone,
                floorAreaSqft = floorAreaSqft,
                maxWeightCapacityKg = maxWeightCapacityKg,
                status = status
            )

            settingsRepository.updateFloor(floorId, request)
                .onSuccess {
                    fetchFloors(warehouseId, isRefresh = true)
                    showSuccess("Floor updated successfully")
                    onSuccess()
                }
                .onFailure { exception ->
                    val clean = extractErrorMessage(exception.message)
                    showError(clean)
                    onError(clean)
                }
        }
    }

    fun deleteFloor(
        floorId: String,
        warehouseId: String? = null,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoadingLocationStructure.value = true
            _floorErrorMessage.value = null

            settingsRepository.deleteFloor(floorId)
                .onSuccess { message ->
                    _floors.value = _floors.value.filter { it.id != floorId }
                    showSuccess(message)
                    onSuccess()
                }
                .onFailure { exception ->
                    val clean = extractErrorMessage(exception.message)
                    showError(clean)
                }

            _isLoadingLocationStructure.value = false
        }
    }

    // ===========================================================
    // 19. INVENTORY SECTION ACTIONS
    // ===========================================================

    fun setSelectedSectionForEdit(section: SectionItem?) {
        _selectedSectionForEdit.value = section
    }

    fun clearSelectedSectionForEdit() {
        _selectedSectionForEdit.value = null
    }

    fun fetchSections(warehouseId: String? = null, floorId: String? = null, isRefresh: Boolean = false) {
        if (isRefresh) {
            sectionCurrentPage = 1
            isSectionEndReached = false
        }
        if (_isLoadingLocationStructure.value || _isPaginatingSections.value || isSectionEndReached) return

        viewModelScope.launch {
            if (sectionCurrentPage == 1) {
                _isLoadingLocationStructure.value = true
            } else {
                _isPaginatingSections.value = true
            }

            val result = settingsRepository.getSections(
                warehouseId = warehouseId,
                floorId = floorId,
                page = sectionCurrentPage,
                limit = 20
            )
            _isLoadingLocationStructure.value = false
            _isPaginatingSections.value = false

            result.onSuccess { list ->
                if (list.isEmpty()) {
                    isSectionEndReached = true
                } else {
                    if (sectionCurrentPage == 1) {
                        _sections.value = list
                    } else {
                        _sections.value = (_sections.value + list).distinctBy { it.id }
                    }
                    sectionCurrentPage++
                }
            }.onFailure {
                val clean = extractErrorMessage(it.message)
                _errorMessage.value = clean
                showError(clean)
            }
        }
    }

    fun createSection(
        warehouseId: String,
        floorId: String,
        name: String,
        code: String,
        sequenceOrder: Int,
        description: String?,
        allowedProductCategories: List<String>,
        storageType: String,
        climateControl: String,
        status: String,
        onSuccess: (SectionItem) -> Unit,
        onError: (String) -> Unit
    ) {
        launchBusy {
            _isLoadingLocationStructure.value = true
            val req = CreateSectionRequest(
                warehouseId = warehouseId,
                floorId = floorId,
                name = name.trim(),
                code = code.trim().uppercase(),
                sequenceOrder = sequenceOrder,
                description = description?.takeIf { it.isNotBlank() },
                allowedProductCategories = allowedProductCategories,
                storageType = storageType,
                climateControl = climateControl,
                status = status
            )
            val res = settingsRepository.createSection(req)
            _isLoadingLocationStructure.value = false
            res.onSuccess {
                fetchSections(warehouseId, floorId, isRefresh = true)
                showSuccess("Section created successfully")
                onSuccess(it)
            }.onFailure {
                val clean = extractErrorMessage(it.message)
                _errorMessage.value = clean
                showError(clean)
                onError(clean)
            }
        }
    }

    fun updateSection(
        sectionId: String,
        warehouseId: String,
        floorId: String,
        name: String,
        code: String,
        sequenceOrder: Int,
        description: String?,
        allowedProductCategories: List<String>,
        storageType: String,
        climateControl: String,
        status: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val request = CreateSectionRequest(
                warehouseId = warehouseId,
                floorId = floorId,
                name = name.trim(),
                code = code.trim().uppercase(),
                sequenceOrder = sequenceOrder,
                description = description?.takeIf { it.isNotBlank() },
                allowedProductCategories = allowedProductCategories,
                storageType = storageType,
                climateControl = climateControl,
                status = status
            )

            settingsRepository.updateSection(sectionId, request)
                .onSuccess {
                    fetchSections(warehouseId, floorId, isRefresh = true)
                    showSuccess("Section updated successfully")
                    onSuccess()
                }
                .onFailure { exception ->
                    val clean = extractErrorMessage(exception.message)
                    showError(clean)
                    onError(clean)
                }
        }
    }

    fun deleteSection(
        sectionId: String,
        warehouseId: String? = null,
        floorId: String? = null,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoadingLocationStructure.value = true
            settingsRepository.deleteSection(sectionId)
                .onSuccess { message ->
                    _sections.value = _sections.value.filter { it.id != sectionId }
                    showSuccess(message)
                    onSuccess()
                }
                .onFailure { exception ->
                    val clean = extractErrorMessage(exception.message)
                    showError(clean)
                }
            _isLoadingLocationStructure.value = false
        }
    }

    // ===========================================================
    // 20. INVENTORY RACK ACTIONS
    // ===========================================================

    fun setSelectedRackForEdit(rack: RackItem?) {
        _selectedRackForEdit.value = rack
    }

    fun clearSelectedRackForEdit() {
        _selectedRackForEdit.value = null
    }

    fun fetchRacks(
        warehouseId: String? = null,
        floorId: String? = null,
        sectionId: String? = null,
        isRefresh: Boolean = false
    ) {
        if (isRefresh) {
            rackCurrentPage = 1
            isRackEndReached = false
        }
        if (_isLoadingLocationStructure.value || _isPaginatingRacks.value || isRackEndReached) return

        viewModelScope.launch {
            if (rackCurrentPage == 1) {
                _isLoadingLocationStructure.value = true
            } else {
                _isPaginatingRacks.value = true
            }

            val result = settingsRepository.getRacks(
                warehouseId = warehouseId,
                floorId = floorId,
                sectionId = sectionId,
                page = rackCurrentPage,
                limit = 20
            )
            _isLoadingLocationStructure.value = false
            _isPaginatingRacks.value = false

            result.onSuccess { list ->
                if (list.isEmpty()) {
                    isRackEndReached = true
                } else {
                    if (rackCurrentPage == 1) {
                        _racks.value = list
                    } else {
                        _racks.value = (_racks.value + list).distinctBy { it.id }
                    }
                    rackCurrentPage++
                }
            }.onFailure {
                val clean = extractErrorMessage(it.message)
                _errorMessage.value = clean
                showError(clean)
            }
        }
    }

    fun createRack(
        warehouseId: String,
        floorId: String,
        sectionId: String,
        name: String,
        code: String,
        sequenceOrder: Int,
        description: String?,
        rackType: String,
        maxQuantityCapacity: Int,
        maxWeightCapacityKg: Double,
        status: String,
        onSuccess: (RackItem) -> Unit,
        onError: (String) -> Unit
    ) {
        launchBusy {
            _isLoadingLocationStructure.value = true
            val req = CreateRackRequest(
                warehouseId = warehouseId,
                floorId = floorId,
                sectionId = sectionId,
                name = name.trim(),
                code = code.trim().uppercase(),
                sequenceOrder = sequenceOrder,
                description = description?.takeIf { it.isNotBlank() },
                rackType = rackType,
                maxQuantityCapacity = maxQuantityCapacity,
                maxWeightCapacityKg = maxWeightCapacityKg,
                status = status
            )
            val res = settingsRepository.createRack(req)
            _isLoadingLocationStructure.value = false
            res.onSuccess {
                fetchRacks(warehouseId, floorId, sectionId, isRefresh = true)
                showSuccess("Rack created successfully")
                onSuccess(it)
            }.onFailure {
                val clean = extractErrorMessage(it.message)
                _errorMessage.value = clean
                showError(clean)
                onError(clean)
            }
        }
    }

    fun updateRack(
        rackId: String,
        warehouseId: String,
        floorId: String,
        sectionId: String,
        name: String,
        code: String,
        sequenceOrder: Int,
        description: String?,
        rackType: String,
        maxQuantityCapacity: Int,
        maxWeightCapacityKg: Double,
        status: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val request = CreateRackRequest(
                warehouseId = warehouseId,
                floorId = floorId,
                sectionId = sectionId,
                name = name.trim(),
                code = code.trim().uppercase(),
                sequenceOrder = sequenceOrder,
                description = description?.takeIf { it.isNotBlank() },
                rackType = rackType,
                maxQuantityCapacity = maxQuantityCapacity,
                maxWeightCapacityKg = maxWeightCapacityKg,
                status = status
            )

            settingsRepository.updateRack(rackId, request)
                .onSuccess {
                    fetchRacks(warehouseId, floorId, sectionId, isRefresh = true)
                    showSuccess("Rack updated successfully")
                    onSuccess()
                }
                .onFailure { exception ->
                    val clean = extractErrorMessage(exception.message)
                    showError(clean)
                    onError(clean)
                }
        }
    }

    fun deleteRack(
        rackId: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoadingLocationStructure.value = true
            settingsRepository.deleteRack(rackId)
                .onSuccess { message ->
                    _racks.value = _racks.value.filter { it.id != rackId }
                    showSuccess(message)
                    onSuccess()
                }
                .onFailure { exception ->
                    val clean = extractErrorMessage(exception.message)
                    showError(clean)
                }
            _isLoadingLocationStructure.value = false
        }
    }

    // ===========================================================
    // 21. INVENTORY BIN ACTIONS
    // ===========================================================

    fun setSelectedBinForEdit(bin: BinItem?) {
        _selectedBinForEdit.value = bin
    }

    fun clearSelectedBinForEdit() {
        _selectedBinForEdit.value = null
    }

    fun fetchBins(
        warehouseId: String? = null,
        rackId: String? = null,
        isRefresh: Boolean = false
    ) {
        if (isRefresh) {
            binCurrentPage = 1
            isBinEndReached = false
        }
        if (_isLoadingBins.value || _isPaginatingBins.value || isBinEndReached) return

        viewModelScope.launch {
            if (binCurrentPage == 1) {
                _isLoadingBins.value = true
                _isLoadingLocationStructure.value = true
            } else {
                _isPaginatingBins.value = true
            }

            val pageSize = 20
            val result = settingsRepository.getBins(
                page = binCurrentPage,
                limit = pageSize
            )
            _isLoadingBins.value = false
            _isLoadingLocationStructure.value = false
            _isPaginatingBins.value = false

            result.onSuccess { list ->
                if (list.isEmpty()) {
                    isBinEndReached = true
                    if (binCurrentPage == 1) {
                        _bins.value = emptyList()
                    }
                } else {
                    if (binCurrentPage == 1) {
                        _bins.value = list
                    } else {
                        _bins.value = (_bins.value + list).distinctBy { it.id }
                    }
                    if (list.size < pageSize) {
                        isBinEndReached = true
                    } else {
                        binCurrentPage++
                    }
                }
            }.onFailure {
                val clean = extractErrorMessage(it.message)
                _errorMessage.value = clean
                showError(clean)
            }
        }
    }

    fun createBin(
        warehouseId: String,
        floorId: String,
        sectionId: String,
        rackId: String,
        name: String,
        code: String,
        sequenceOrder: Int,
        binType: String,
        maxQuantity: Int,
        maxWeightKg: Double,
        defaultUOM: String,
        status: String,
        onSuccess: (BinItem) -> Unit,
        onError: (String) -> Unit
    ) {
        launchBusy {
            _isLoadingLocationStructure.value = true
            val req = CreateBinRequest(
                warehouseId = warehouseId,
                floorId = floorId,
                sectionId = sectionId,
                rackId = rackId,
                name = name.trim(),
                code = code.trim().uppercase(),
                sequenceOrder = sequenceOrder,
                binType = binType,
                maxQuantity = maxQuantity,
                maxWeightKg = maxWeightKg,
                defaultUOM = defaultUOM,
                status = status
            )
            val res = settingsRepository.createBin(req)
            _isLoadingLocationStructure.value = false
            res.onSuccess {
                fetchBins(warehouseId, rackId, isRefresh = true)
                showSuccess("Bin created successfully")
                onSuccess(it)
            }.onFailure {
                val clean = extractErrorMessage(it.message)
                _errorMessage.value = clean
                showError(clean)
                onError(clean)
            }
        }
    }

    fun updateBin(
        binId: String,
        warehouseId: String,
        floorId: String,
        sectionId: String,
        rackId: String,
        name: String,
        code: String,
        sequenceOrder: Int,
        binType: String,
        maxQuantity: Int,
        maxWeightKg: Double,
        defaultUOM: String,
        status: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val request = CreateBinRequest(
                warehouseId = warehouseId,
                floorId = floorId,
                sectionId = sectionId,
                rackId = rackId,
                name = name.trim(),
                code = code.trim().uppercase(),
                sequenceOrder = sequenceOrder,
                binType = binType,
                maxQuantity = maxQuantity,
                maxWeightKg = maxWeightKg,
                defaultUOM = defaultUOM,
                status = status
            )

            settingsRepository.updateBin(binId, request)
                .onSuccess {
                    fetchBins(warehouseId, rackId, isRefresh = true)
                    showSuccess("Bin updated successfully")
                    onSuccess()
                }
                .onFailure { exception ->
                    val clean = extractErrorMessage(exception.message)
                    showError(clean)
                    onError(clean)
                }
        }
    }

    fun deleteBin(
        binId: String,
        warehouseId: String? = null,
        rackId: String? = null,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoadingLocationStructure.value = true
            settingsRepository.deleteBin(binId)
                .onSuccess { message ->
                    _bins.value = _bins.value.filter { it.id != binId }
                    showSuccess(message)
                    onSuccess()
                }
                .onFailure { exception ->
                    val clean = extractErrorMessage(exception.message)
                    showError(clean)
                }
            _isLoadingLocationStructure.value = false
        }
    }

    // ===========================================================
    // 22. DESIGN ACTIONS
    // ===========================================================

    fun fetchDesigns(
        page: Int = 1,
        designType: String? = null,
        status: String? = null,
        search: String? = null
    ) {
        viewModelScope.launch {
            _isLoadingDesigns.value = true
            _designError.value = null

            val result = settingsRepository.getDesigns(page, 20, designType, status, search)
            _isLoadingDesigns.value = false

            result.onSuccess { list ->
                _designs.value = list
            }.onFailure { error ->
                val clean = extractErrorMessage(error.message)
                _designError.value = clean
                showError(clean)
            }
        }
    }

    fun fetchDesignById(id: String) {
        viewModelScope.launch {
            _isLoadingDesigns.value = true
            _designError.value = null

            val result = settingsRepository.getDesignById(id)
            _isLoadingDesigns.value = false

            result.onSuccess { design ->
                _selectedDesign.value = design
            }.onFailure { error ->
                val clean = extractErrorMessage(error.message)
                _designError.value = clean
                showError(clean)
            }
        }
    }

    fun clearSelectedDesign() {
        _selectedDesign.value = null
        _designError.value = null
    }

    fun createDesign(
        context: Context,
        name: String,
        designType: String,
        code: String,
        description: String?,
        status: String = "Active",
        segmentIds: List<String>,
        imageUri: Uri?,
        onSuccess: (DesignItem) -> Unit,
        onError: (String) -> Unit
    ) {
        launchBusy {
            _isSubmittingDesign.value = true
            _designError.value = null

            val result = settingsRepository.createDesign(
                context = context,
                name = name.trim(),
                designType = designType.trim(),
                code = code.trim().uppercase(),
                description = description?.takeIf { it.isNotBlank() },
                status = status,
                segmentIds = segmentIds,
                imageUri = imageUri
            )
            _isSubmittingDesign.value = false

            result.onSuccess { createdDesign ->
                fetchDesigns()
                showSuccess("Design created successfully")
                onSuccess(createdDesign)
            }.onFailure { error ->
                val clean = extractErrorMessage(error.message)
                _designError.value = clean
                showError(clean)
                onError(clean)
            }
        }
    }

    fun updateDesign(
        context: Context,
        id: String,
        name: String,
        designType: String,
        code: String,
        description: String?,
        status: String,
        segmentIds: List<String>,
        imageUri: Uri?,
        onSuccess: (DesignItem) -> Unit,
        onError: (String) -> Unit
    ) {
        launchBusy {
            _isSubmittingDesign.value = true
            _designError.value = null

            val result = settingsRepository.updateDesign(
                context = context,
                id = id,
                name = name.trim(),
                designType = designType.trim(),
                code = code.trim().uppercase(),
                description = description?.takeIf { it.isNotBlank() },
                status = status,
                segmentIds = segmentIds,
                imageUri = imageUri
            )
            _isSubmittingDesign.value = false

            result.onSuccess { updatedDesign ->
                fetchDesigns()
                showSuccess("Design updated successfully")
                onSuccess(updatedDesign)
            }.onFailure { error ->
                val clean = extractErrorMessage(error.message)
                _designError.value = clean
                showError(clean)
                onError(clean)
            }
        }
    }

    fun changeDesignStatus(
        id: String,
        currentStatus: String,
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val nextStatus = if (currentStatus.equals("Active", ignoreCase = true)) "Inactive" else "Active"

        viewModelScope.launch {
            _isLoadingDesigns.value = true
            _designError.value = null

            val result = settingsRepository.changeDesignStatus(id, nextStatus)
            _isLoadingDesigns.value = false

            result.onSuccess { message ->
                _designs.value = _designs.value.map {
                    if (it.id == id) it.copy(status = nextStatus) else it
                }
                showSuccess(message)
                onSuccess(message)
            }.onFailure { error ->
                val clean = extractErrorMessage(error.message)
                _designError.value = clean
                showError(clean)
                onError(clean)
            }
        }
    }

    fun deleteDesign(
        id: String,
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoadingDesigns.value = true
            _designError.value = null

            val result = settingsRepository.deleteDesign(id)
            _isLoadingDesigns.value = false

            result.onSuccess { message ->
                _designs.value = _designs.value.filter { it.id != id }
                showSuccess(message)
                onSuccess(message)
            }.onFailure { error ->
                val clean = extractErrorMessage(error.message)
                _designError.value = clean
                showError(clean)
                onError(clean)
            }
        }
    }
}