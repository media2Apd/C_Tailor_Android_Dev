package com.cuso.tailor.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuso.tailor.database.entities.UserEntity
import com.cuso.tailor.database.entities.OrganizationEntity
import com.cuso.tailor.database.entities.TokensEntity
import com.cuso.tailor.repository.LoginRepository
import com.cuso.tailor.utils.launchBusy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.cuso.tailor.model.home.*
import com.cuso.tailor.repository.ApiRepository
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
@Suppress("UNUSED_PARAMETER")
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    private val repo: ApiRepository
) : ViewModel() {

    private val _user = MutableStateFlow<UserEntity?>(null)
    val user: StateFlow<UserEntity?> = _user

    private val _org = MutableStateFlow<OrganizationEntity?>(null)
    val org: StateFlow<OrganizationEntity?> = _org

    private val _tokens = MutableStateFlow<TokensEntity?>(null)
    val tokens: StateFlow<TokensEntity?> = _tokens

    private val _isLoggedOut = MutableStateFlow(false)
    val isLoggedOut: StateFlow<Boolean> = _isLoggedOut

    init {
        loadData()
    }

    private fun loadData() {
        launchBusy {
            _user.value = loginRepository.getUser()
            _org.value = loginRepository.getOrganization()
            _tokens.value = loginRepository.getTokens()
        }
    }
    fun logout() {
        launchBusy {
            loginRepository.clearAll()
            _isLoggedOut.value = true
        }
    }

    private val _kpis = MutableStateFlow<HomeKpisGroup?>(null)
    val kpis: StateFlow<HomeKpisGroup?> = _kpis.asStateFlow()

    private val _revenueTrend = MutableStateFlow<RevenueTrendData?>(null)
    val revenueTrend: StateFlow<RevenueTrendData?> = _revenueTrend.asStateFlow()

    private val _marketing = MutableStateFlow<HomeMarketingData?>(null)
    val marketing: StateFlow<HomeMarketingData?> = _marketing.asStateFlow()

    private val _orderStatus = MutableStateFlow<DonutSectionData?>(null)
    val orderStatus: StateFlow<DonutSectionData?> = _orderStatus.asStateFlow()

    private val _stockStatus = MutableStateFlow<DonutSectionData?>(null)
    val stockStatus: StateFlow<DonutSectionData?> = _stockStatus.asStateFlow()

    private val _attendance = MutableStateFlow<DonutSectionData?>(null)
    val attendance: StateFlow<DonutSectionData?> = _attendance.asStateFlow()

    private val _serviceStatus = MutableStateFlow<DonutSectionData?>(null)
    val serviceStatus: StateFlow<DonutSectionData?> = _serviceStatus.asStateFlow()

    private val _topProducts = MutableStateFlow<List<LeaderboardProductItem>>(emptyList())
    val topProducts: StateFlow<List<LeaderboardProductItem>> = _topProducts.asStateFlow()

    private val _actionTasks = MutableStateFlow<List<HomeActionTaskItem>>(emptyList())
    val actionTasks: StateFlow<List<HomeActionTaskItem>> = _actionTasks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchAllHomeDashboardData()
    }

    fun fetchAllHomeDashboardData() {
        viewModelScope.launch {
            _isLoading.value = true

            // Parallel launches for all dashboard cards
            launch {
                repo.request<HomeKpiResponse> {
                    get("/api/home/dashboard/kpi-summary")
                }.onSuccess { _kpis.value = it.data?.kpis }
            }

            launch {
                repo.request<RevenueTrendResponse> {
                    get("/api/home/dashboard/revenue-trend")
                }.onSuccess { _revenueTrend.value = it.data }
            }

            launch {
                repo.request<HomeMarketingResponse> {
                    get("/api/home/dashboard/marketing-performance")
                }.onSuccess { _marketing.value = it.data }
            }

            launch {
                repo.request<DonutSectionResponse> {
                    get("/api/home/dashboard/order-status")
                }.onSuccess { _orderStatus.value = it.data }
            }

            launch {
                repo.request<DonutSectionResponse> {
                    get("/api/home/dashboard/stock-status")
                }.onSuccess { _stockStatus.value = it.data }
            }

            launch {
                repo.request<DonutSectionResponse> {
                    get("/api/home/dashboard/today-attendance")
                }.onSuccess { _attendance.value = it.data }
            }

            launch {
                repo.request<DonutSectionResponse> {
                    get("/api/home/dashboard/service-status")
                }.onSuccess { _serviceStatus.value = it.data }
            }

            launch {
                repo.request<TopProductsResponse> {
                    get("/api/home/dashboard/top-products")
                }.onSuccess { _topProducts.value = it.data?.products.orEmpty() }
            }

            launch {
                repo.request<HomeActionRequiredResponse> {
                    get("/api/home/dashboard/actions-required")
                }.onSuccess { _actionTasks.value = it.data?.actions.orEmpty() }
            }

            _isLoading.value = false
        }
    }
}