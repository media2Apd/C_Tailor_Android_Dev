@file:Suppress("unused", "AssignedValueIsNeverRead")

package com.cuso.tailor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.tailor.model.legal.*
import com.cuso.tailor.repository.ApiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

private const val LEGAL_DASHBOARD_URL = "/api/legal/legal-dashboard/summary"
private const val LEGAL_CONTRACTS_URL = "/api/legal/legal-contract/view-all"

@HiltViewModel
class LegalViewModel @Inject constructor(
    private val repo: ApiRepository
) : ViewModel() {

    val currentCalendarYear: Int = Calendar.getInstance().get(Calendar.YEAR)

    private val _selectedYear = MutableStateFlow(currentCalendarYear)
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _dashboardData = MutableStateFlow<LegalDashboardData?>(null)
    val dashboardData: StateFlow<LegalDashboardData?> = _dashboardData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // ── Legal Contracts State ──
    private val _contracts = MutableStateFlow<List<LegalContractDto>>(emptyList())
    val contracts: StateFlow<List<LegalContractDto>> = _contracts.asStateFlow()

    private val _isLoadingContracts = MutableStateFlow(false)
    val isLoadingContracts: StateFlow<Boolean> = _isLoadingContracts.asStateFlow()

    private val _contractsError = MutableStateFlow<String?>(null)
    val contractsError: StateFlow<String?> = _contractsError.asStateFlow()

    init {
        fetchDashboardSummary(currentCalendarYear)
        fetchContracts()
    }

    fun fetchDashboardSummary(year: Int = _selectedYear.value) {
        _selectedYear.value = year
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = repo.request<LegalDashboardResponse> {
                get(LEGAL_DASHBOARD_URL, repo.query("year" to year))
            }

            result.fold(
                onSuccess = { res -> _dashboardData.value = res.data },
                onFailure = { error -> _errorMessage.value = error.message }
            )
            _isLoading.value = false
        }
    }

    /**
     * Fetches legal contracts list with optional search query and pagination
     */
    fun fetchContracts(search: String? = null, page: Int = 1, limit: Int = 20) {
        viewModelScope.launch {
            _isLoadingContracts.value = true
            _contractsError.value = null

            val result = repo.request<LegalContractsResponse> {
                get(
                    LEGAL_CONTRACTS_URL,
                    repo.query(
                        "search" to search,
                        "page" to page,
                        "limit" to limit
                    )
                )
            }

            result.fold(
                onSuccess = { res ->
                    _contracts.value = res.data.orEmpty()
                },
                onFailure = { error ->
                    _contractsError.value = error.message ?: "Failed to fetch legal contracts"
                }
            )
            _isLoadingContracts.value = false
        }
    }
}