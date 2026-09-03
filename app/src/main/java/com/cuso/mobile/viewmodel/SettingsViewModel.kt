@file:Suppress("unused")
package com.cuso.mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.mobile.database.entities.GarmentMeasurement
import com.cuso.mobile.model.inventory.*
import com.cuso.mobile.model.sales.myOrganizationResponse
import com.cuso.mobile.model.settings.*
import com.cuso.mobile.repository.AuthRepository
import com.cuso.mobile.repository.SessionManager
import com.cuso.mobile.repository.SettingsRepository
import com.cuso.mobile.utils.launchBusy
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

    // ── Organization State ──
    private val _organization = MutableStateFlow<myOrganizationResponse?>(null)
    val organization: StateFlow<myOrganizationResponse?> = _organization.asStateFlow()

    private val _isLoadingOrganization = MutableStateFlow(false)
    val isLoadingOrganization: StateFlow<Boolean> = _isLoadingOrganization.asStateFlow()

    private val _organizationError = MutableStateFlow<String?>(null)
    val organizationError: StateFlow<String?> = _organizationError.asStateFlow()

    // ── Segments List State ──
    private val _segments = MutableStateFlow<List<SegmentItem>>(emptyList())
    val segments: StateFlow<List<SegmentItem>> = _segments.asStateFlow()

    private val _isLoadingSegments = MutableStateFlow(false)
    val isLoadingSegments: StateFlow<Boolean> = _isLoadingSegments.asStateFlow()

    private val _segmentsError = MutableStateFlow<String?>(null)
    val segmentsError: StateFlow<String?> = _segmentsError.asStateFlow()

    // ── Create Segment State ──
    private val _isCreatingSegment = MutableStateFlow(false)
    val isCreatingSegment: StateFlow<Boolean> = _isCreatingSegment.asStateFlow()

    private val _segmentError = MutableStateFlow<String?>(null)
    val segmentError: StateFlow<String?> = _segmentError.asStateFlow()

    // ── View One Segment State ──
    private val _selectedSegmentDetail = MutableStateFlow<SegmentItem?>(null)
    val selectedSegmentDetail: StateFlow<SegmentItem?> = _selectedSegmentDetail.asStateFlow()

    private val _isLoadingSegmentDetail = MutableStateFlow(false)
    val isLoadingSegmentDetail: StateFlow<Boolean> = _isLoadingSegmentDetail.asStateFlow()

    // ── Garments List State ──
    private val _garments = MutableStateFlow<List<GarmentItem>>(emptyList())
    val garments: StateFlow<List<GarmentItem>> = _garments.asStateFlow()

    private val _isLoadingGarments = MutableStateFlow(false)
    val isLoadingGarments: StateFlow<Boolean> = _isLoadingGarments.asStateFlow()

    private val _garmentsError = MutableStateFlow<String?>(null)
    val garmentsError: StateFlow<String?> = _garmentsError.asStateFlow()

    // ── Garment Mutation State ──
    private val _isCreatingGarment = MutableStateFlow(false)
    val isCreatingGarment: StateFlow<Boolean> = _isCreatingGarment.asStateFlow()

    private val _garmentError = MutableStateFlow<String?>(null)
    val garmentError: StateFlow<String?> = _garmentError.asStateFlow()

    // ── Garment category (Styles) State ──
    private val _garmentStyles = MutableStateFlow<List<GarmentStyleItem>>(emptyList())
    val garmentStyles: StateFlow<List<GarmentStyleItem>> = _garmentStyles.asStateFlow()

    private val _isLoadingStyles = MutableStateFlow(false)
    val isLoadingStyles: StateFlow<Boolean> = _isLoadingStyles.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // ── Selected Garment & Segment For Detail Screen ──
    private val _selectedSegmentIdForStyle = MutableStateFlow<String?>(null)
    val selectedSegmentIdForStyle: StateFlow<String?> = _selectedSegmentIdForStyle.asStateFlow()

    private val _selectedGarmentIdForStyle = MutableStateFlow<String?>(null)
    val selectedGarmentIdForStyle: StateFlow<String?> = _selectedGarmentIdForStyle.asStateFlow()

    private val _selectedGarmentTitleForStyle = MutableStateFlow("Garment Categories")
    val selectedGarmentTitleForStyle: StateFlow<String> = _selectedGarmentTitleForStyle.asStateFlow()

    // ── Room DB Local Measurements State ──
    private val _localMeasurements = MutableStateFlow<List<GarmentMeasurement>>(emptyList())
    val localMeasurements: StateFlow<List<GarmentMeasurement>> = _localMeasurements.asStateFlow()

    // ==========================================
    // ── Inventory Location Structure States ──
    // ==========================================
    private val _floors = MutableStateFlow<List<FloorItem>>(emptyList())
    val floors: StateFlow<List<FloorItem>> = _floors.asStateFlow()

    private val _sections = MutableStateFlow<List<SectionItem>>(emptyList())
    val sections: StateFlow<List<SectionItem>> = _sections.asStateFlow()

    private val _racks = MutableStateFlow<List<RackItem>>(emptyList())
    val racks: StateFlow<List<RackItem>> = _racks.asStateFlow()

    private val _bins = MutableStateFlow<List<BinItem>>(emptyList())
    val bins: StateFlow<List<BinItem>> = _bins.asStateFlow()

    private val _isLoadingLocationStructure = MutableStateFlow(false)
    val isLoadingLocationStructure: StateFlow<Boolean> = _isLoadingLocationStructure.asStateFlow()

    // Helper to extract clean message string from JSON error response
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

    fun setSelectedGarmentForDetail(segmentId: String?, garmentId: String?, title: String) {
        _selectedSegmentIdForStyle.value = segmentId
        _selectedGarmentIdForStyle.value = garmentId
        _selectedGarmentTitleForStyle.value = title
        fetchGarmentStyles(segmentId = segmentId, garmentId = garmentId)
    }

    fun loadLocalMeasurements(categoryId: String) {
        viewModelScope.launch {
            settingsRepository.getLocalMeasurements(categoryId).collect { garment ->
                _localMeasurements.value = garment?.measurements ?: emptyList()
            }
        }
    }

    private val _isDeactivatingField = MutableStateFlow(false)
    val isDeactivatingField = _isDeactivatingField.asStateFlow()

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

    fun clearOrganization() {
        _organization.value = null
        _organizationError.value = null
    }

    fun createSegment(
        name: String,
        code: String,
        description: String?,
        displayOrder: Int,
        isActive: Boolean,
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
                result.getOrNull()?.let { response ->
                    onSuccess(response)
                }
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
        isActive: Boolean,
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
                isActive = isActive
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

    fun fetchGarments() {
        launchBusy {
            _isLoadingGarments.value = true
            _garmentsError.value = null

            val result = settingsRepository.getGarments()
            _isLoadingGarments.value = false

            if (result.isSuccess) {
                _garments.value = result.getOrDefault(emptyList())
            } else {
                _garmentsError.value = result.exceptionOrNull()?.message ?: "Failed to load garments"
            }
        }
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

    fun createGarment(
        name: String,
        code: String,
        description: String?,
        applicableSegmentIds: List<String>,
        baseStitchingCharge: Double,
        onSuccess: (CreateGarmentResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        launchBusy {
            _isCreatingGarment.value = true
            _garmentError.value = null

            val request = CreateGarmentRequest(
                name = name.trim(),
                displayName = name.trim(),
                code = code.trim().uppercase(),
                description = description?.takeIf { it.isNotBlank() },
                applicableSegments = applicableSegmentIds,
                baseStitchingCharge = baseStitchingCharge,
                isCustomStitchable = true
            )

            val result = settingsRepository.createGarment(request)
            _isCreatingGarment.value = false

            if (result.isSuccess) {
                result.getOrNull()?.let { response ->
                    onSuccess(response)
                }
            } else {
                val error = result.exceptionOrNull()?.message ?: "Failed to create garment"
                _garmentError.value = error
                onError(error)
            }
        }
    }

    private val _isCreatingStyle = MutableStateFlow(false)
    val isCreatingStyle: StateFlow<Boolean> = _isCreatingStyle.asStateFlow()

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
//                isActive = true
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
//                garmentId = garmentId,
                styleTags = styleTags,
                sleeveStyle = sleeveStyle,
                stitchingCharge = stitchingCharge,
                isCustomStitchable = true,
//                isActive = true
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

    private val _measurementFields = MutableStateFlow<List<MeasurementFieldItem>>(emptyList())
    val measurementFields: StateFlow<List<MeasurementFieldItem>> = _measurementFields.asStateFlow()

    private val _isLoadingMeasurementFields = MutableStateFlow(false)
    val isLoadingMeasurementFields: StateFlow<Boolean> = _isLoadingMeasurementFields.asStateFlow()

    fun fetchMeasurementFields() {
        launchBusy {
            _isLoadingMeasurementFields.value = true
            _errorMessage.value = null

            val result = settingsRepository.getMeasurementFields()
            _isLoadingMeasurementFields.value = false

            result.onSuccess { list ->
                _measurementFields.value = list
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Failed to load measurement fields"
            }
        }
    }

    fun createMeasurementField(
        name: String,
        displayName: String,
        code: String,
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
                description = description?.takeIf { it.isNotBlank() },
                inputType = inputType,
                unit = unit,
                minValue = minValue,
                maxValue = maxValue,
                options = options,
//                isActive = true
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
//                segmentId = segmentIdStr,
//                garmentId = garmentIdStr,
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
//                segmentId = segmentIdStr,
//                garmentId = garmentIdStr,
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

    private val _selectedStyleDetail = MutableStateFlow<GarmentStyleItem?>(null)
    val selectedStyleDetail: StateFlow<GarmentStyleItem?> = _selectedStyleDetail.asStateFlow()

    private val _isLoadingStyleDetail = MutableStateFlow(false)
    val isLoadingStyleDetail: StateFlow<Boolean> = _isLoadingStyleDetail.asStateFlow()

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

    fun deactivateMeasurementField(
        fieldId: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            settingsRepository.deactivateMeasurementField(fieldId)
                .onSuccess { response ->
                    onSuccess(response.message)
                }
                .onFailure { exception ->
                    onError(exception.localizedMessage ?: "Failed to deactivate field")
                }
        }
    }

    // ========================================================
    // ── Location Structure ViewModel Implementation Calls ──
    // ========================================================

    fun fetchFloors(warehouseId: String? = null) {
        launchBusy {
            _isLoadingLocationStructure.value = true
            val result = settingsRepository.getFloors(warehouseId)
            _isLoadingLocationStructure.value = false
            result.onSuccess { list -> _floors.value = list }
                .onFailure { _errorMessage.value = extractErrorMessage(it.message) }
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
                fetchFloors(warehouseId)
                onSuccess(it)
            }.onFailure {
                val clean = extractErrorMessage(it.message)
                _errorMessage.value = clean
                onError(clean)
            }
        }
    }

    fun fetchSections(warehouseId: String? = null, floorId: String? = null) {
        launchBusy {
            _isLoadingLocationStructure.value = true
            val result = settingsRepository.getSections(warehouseId, floorId)
            _isLoadingLocationStructure.value = false
            result.onSuccess { list -> _sections.value = list }
                .onFailure { _errorMessage.value = extractErrorMessage(it.message) }
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
                fetchSections(warehouseId, floorId)
                onSuccess(it)
            }.onFailure {
                val clean = extractErrorMessage(it.message)
                _errorMessage.value = clean
                onError(clean)
            }
        }
    }

    fun fetchRacks(warehouseId: String? = null, floorId: String? = null, sectionId: String? = null) {
        launchBusy {
            _isLoadingLocationStructure.value = true
            val result = settingsRepository.getRacks(warehouseId, floorId, sectionId)
            _isLoadingLocationStructure.value = false
            result.onSuccess { list -> _racks.value = list }
                .onFailure { _errorMessage.value = extractErrorMessage(it.message) }
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
                fetchRacks(warehouseId, floorId, sectionId)
                onSuccess(it)
            }.onFailure {
                val clean = extractErrorMessage(it.message)
                _errorMessage.value = clean
                onError(clean)
            }
        }
    }

    fun fetchBins(warehouseId: String? = null, rackId: String? = null) {
        launchBusy {
            _isLoadingLocationStructure.value = true
            val result = settingsRepository.getBins(warehouseId, rackId)
            _isLoadingLocationStructure.value = false
            result.onSuccess { list -> _bins.value = list }
                .onFailure { _errorMessage.value = extractErrorMessage(it.message) }
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
                fetchBins(warehouseId, rackId)
                onSuccess(it)
            }.onFailure {
                val clean = extractErrorMessage(it.message)
                _errorMessage.value = clean
                onError(clean)
            }
        }
    }

    fun clearSelectedStyleDetail() {
        _selectedStyleDetail.value = null
    }

    fun logout(onComplete: () -> Unit) {
        launchBusy {
            sessionManager.logout()
            clearOrganization()
            onComplete()
        }
    }

    fun clearSelectedSegmentDetail() {
        _selectedSegmentDetail.value = null
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}