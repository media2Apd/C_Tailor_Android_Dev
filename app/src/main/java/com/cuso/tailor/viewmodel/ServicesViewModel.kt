@file:Suppress("unused")
package com.cuso.tailor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.tailor.model.settings.CreateStageRequest
import com.cuso.tailor.model.settings.CreateTemplateRequest
import com.cuso.tailor.model.settings.ProductionStageDto
import com.cuso.tailor.model.settings.ProductionTemplateDto
import com.cuso.tailor.repository.ServicesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductionTemplateUiState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val canLoadMore: Boolean = true,
    val currentPage: Int = 1,
    val templates: List<ProductionTemplateDto> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val searchQuery: String = ""
)

@HiltViewModel
class ServicesViewModel @Inject constructor(
    private val repository: ServicesRepository
) : ViewModel() {

    // Template States
    private val _uiState = MutableStateFlow(ProductionTemplateUiState())
    val uiState: StateFlow<ProductionTemplateUiState> = _uiState.asStateFlow()

    private val _selectedTemplate = MutableStateFlow<ProductionTemplateDto?>(null)
    val selectedTemplate: StateFlow<ProductionTemplateDto?> = _selectedTemplate.asStateFlow()

    private val _isLoadingDetail = MutableStateFlow(false)
    val isLoadingDetail: StateFlow<Boolean> = _isLoadingDetail.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    // Production Stages States
    private val _stages = MutableStateFlow<List<ProductionStageDto>>(emptyList())
    val stages: StateFlow<List<ProductionStageDto>> = _stages.asStateFlow()

    private val _isLoadingStages = MutableStateFlow(false)
    val isLoadingStages: StateFlow<Boolean> = _isLoadingStages.asStateFlow()

    private val _stagesError = MutableStateFlow<String?>(null)
    val stagesError: StateFlow<String?> = _stagesError.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadTemplates()
        loadStages()
    }

    // ─────────────────────────────────────────────────────────────
    // Template Actions
    // ─────────────────────────────────────────────────────────────

    fun loadTemplates(search: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, currentPage = 1) }
            repository.getTemplates(page = 1, limit = 10, search = search)
                .onSuccess { response ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            templates = response.data,
                            canLoadMore = response.pagination?.hasNextPage ?: false,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
        }
    }

    fun loadMoreTemplates() {
        val state = _uiState.value
        if (state.isLoadingMore || state.isLoading || !state.canLoadMore) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            val nextPage = state.currentPage + 1

            repository.getTemplates(
                page = nextPage,
                limit = 10,
                search = state.searchQuery.takeIf { it.isNotBlank() }
            ).onSuccess { response ->
                _uiState.update { s ->
                    s.copy(
                        isLoadingMore = false,
                        currentPage = nextPage,
                        templates = (s.templates + response.data).distinctBy { it.id },
                        canLoadMore = response.pagination?.hasNextPage ?: false
                    )
                }
            }.onFailure {
                _uiState.update { it.copy(isLoadingMore = false) }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)
            loadTemplates(search = query.takeIf { it.isNotBlank() })
        }
    }

    fun fetchTemplateDetail(id: String) {
        viewModelScope.launch {
            _isLoadingDetail.value = true
            repository.getTemplateById(id)
                .onSuccess { template -> _selectedTemplate.value = template }
                .onFailure { error -> _uiState.update { it.copy(errorMessage = error.message) } }
            _isLoadingDetail.value = false
        }
    }

    fun setSelectedTemplateDirect(template: ProductionTemplateDto?) {
        _selectedTemplate.value = template
    }

    fun createTemplate(request: CreateTemplateRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isSubmitting.value = true
            repository.createTemplate(request)
                .onSuccess {
                    _uiState.update { s -> s.copy(successMessage = "Template created successfully!") }
                    loadTemplates()
                    onSuccess()
                }
                .onFailure { error -> _uiState.update { s -> s.copy(errorMessage = error.message) } }
            _isSubmitting.value = false
        }
    }

    fun updateTemplate(id: String, request: CreateTemplateRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isSubmitting.value = true
            repository.updateTemplate(id, request)
                .onSuccess {
                    _uiState.update { s -> s.copy(successMessage = "Template updated successfully!") }
                    loadTemplates()
                    onSuccess()
                }
                .onFailure { error -> _uiState.update { s -> s.copy(errorMessage = error.message) } }
            _isSubmitting.value = false
        }
    }

    fun deleteTemplate(id: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isSubmitting.value = true
            repository.deleteTemplate(id)
                .onSuccess {
                    _uiState.update { s -> s.copy(successMessage = "Template deleted successfully") }
                    loadTemplates()
                    onSuccess()
                }
                .onFailure { error -> _uiState.update { s -> s.copy(errorMessage = error.message) } }
            _isSubmitting.value = false
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Production Stage Actions
    // ─────────────────────────────────────────────────────────────

    fun loadStages() {
        viewModelScope.launch {
            _isLoadingStages.value = true
            _stagesError.value = null
            repository.getProductionStages(page = 1, limit = 50)
                .onSuccess { response ->
                    _stages.value = response.data
                    _isLoadingStages.value = false
                }
                .onFailure { error ->
                    _stagesError.value = error.message
                    _isLoadingStages.value = false
                }
        }
    }

    fun createStage(request: CreateStageRequest, onSuccess: (ProductionStageDto) -> Unit) {
        viewModelScope.launch {
            _isSubmitting.value = true
            repository.createProductionStage(request)
                .onSuccess { createdStage ->
                    loadStages()
                    onSuccess(createdStage)
                }
                .onFailure { error ->
                    _uiState.update { s -> s.copy(errorMessage = error.message) }
                }
            _isSubmitting.value = false
        }
    }

    fun deleteStage(id: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isSubmitting.value = true
            repository.deleteProductionStage(id)
                .onSuccess {
                    loadStages()
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.update { s -> s.copy(errorMessage = error.message) }
                }
            _isSubmitting.value = false
        }
    }

    fun clearAlerts() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}