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
import com.cuso.mobile.model.hr.CreateMemberRequest
import com.cuso.mobile.model.hr.CreatedMemberFullData
import com.cuso.mobile.model.hr.MemberDetail
import com.cuso.mobile.model.hr.MemberItem
import com.cuso.mobile.model.hr.RoleItem
import com.cuso.mobile.model.hr.ShiftItem
import com.cuso.mobile.model.hr.UpdateMemberRequest
import com.cuso.mobile.repository.HrRepository
import com.cuso.mobile.utils.launchBusy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject

/**
 * HrViewModel - Handles Roles, Shifts, Members (with infinite scroll pagination),
 * and Create/Update operations for the HR module.
 */
@HiltViewModel
class HrViewModel @Inject constructor(
    private val hrRepository: HrRepository
) : ViewModel() {

    // ═══════════════════════════════════════════════
    // ── Roles ──
    // ═══════════════════════════════════════════════
    private val _roles = MutableStateFlow<List<RoleItem>>(emptyList())
    val roles: StateFlow<List<RoleItem>> = _roles.asStateFlow()

    private val _isLoadingRoles = MutableStateFlow(false)
    val isLoadingRoles: StateFlow<Boolean> = _isLoadingRoles.asStateFlow()

    private val _rolesError = MutableStateFlow<String?>(null)
    val rolesError: StateFlow<String?> = _rolesError.asStateFlow()

    fun fetchRoles() {
        launchBusy {
            _isLoadingRoles.value = true
            _rolesError.value = null
            val result = hrRepository.getRoles()
            result.fold(
                onSuccess = { _roles.value = it },
                onFailure = { e -> _rolesError.value = e.message ?: "Failed to fetch roles" }
            )
            _isLoadingRoles.value = false
        }
    }

    // ═══════════════════════════════════════════════
    // ── Shifts ──
    // ═══════════════════════════════════════════════
    private val _shifts = MutableStateFlow<List<ShiftItem>>(emptyList())
    val shifts: StateFlow<List<ShiftItem>> = _shifts.asStateFlow()

    private val _isLoadingShifts = MutableStateFlow(false)
    val isLoadingShifts: StateFlow<Boolean> = _isLoadingShifts.asStateFlow()

    private val _shiftsError = MutableStateFlow<String?>(null)
    val shiftsError: StateFlow<String?> = _shiftsError.asStateFlow()

    fun fetchShifts() {
        launchBusy {
            _isLoadingShifts.value = true
            _shiftsError.value = null
            val result = hrRepository.getShifts()
            result.fold(
                onSuccess = { _shifts.value = it },
                onFailure = { e -> _shiftsError.value = e.message ?: "Failed to fetch shifts" }
            )
            _isLoadingShifts.value = false
        }
    }

    // ═══════════════════════════════════════════════
    // ── Members (List & Infinite Scroll) ──
    // ═══════════════════════════════════════════════
    private val _members = MutableStateFlow<List<MemberItem>>(emptyList())
    val members: StateFlow<List<MemberItem>> = _members.asStateFlow()

    private val _membersTotal = MutableStateFlow(0)
    val membersTotal: StateFlow<Int> = _membersTotal.asStateFlow()

    private val _isLoadingMembers = MutableStateFlow(false)
    val isLoadingMembers: StateFlow<Boolean> = _isLoadingMembers.asStateFlow()

    private val _isLoadingMoreMembers = MutableStateFlow(false)
    val isLoadingMoreMembers: StateFlow<Boolean> = _isLoadingMoreMembers.asStateFlow()

    private val _canLoadMoreMembers = MutableStateFlow(true)
    val canLoadMoreMembers: StateFlow<Boolean> = _canLoadMoreMembers.asStateFlow()

    private val _currentMemberPage = MutableStateFlow(1)
    val currentMemberPage: StateFlow<Int> = _currentMemberPage.asStateFlow()

    private val _membersError = MutableStateFlow<String?>(null)
    val membersError: StateFlow<String?> = _membersError.asStateFlow()

    private var activeMemberSearch: String? = null
    private var activeMemberStatus: String? = null
    private var fetchMembersJob: Job? = null

    fun fetchMembers(
        page: Int = 1,
        limit: Int = 10,
        search: String? = null,
        status: String? = null
    ) {
        fetchMembersJob?.cancel()
        fetchMembersJob = launchBusy {
            _isLoadingMembers.value = true
            _membersError.value = null
            _currentMemberPage.value = page
            activeMemberSearch = search
            activeMemberStatus = status

            val result = hrRepository.getMembers(page, limit, search, status)
            result.fold(
                onSuccess = { response ->
                    val newMembers = response.data
                    _members.value = newMembers
                    _membersTotal.value = response.total

                    // Check if more items exist based on total count
                    _canLoadMoreMembers.value = newMembers.size < response.total && newMembers.isNotEmpty()
                },
                onFailure = { e ->
                    if (e !is CancellationException) {
                        _membersError.value = e.message ?: "Failed to fetch employees"
                    }
                }
            )
            _isLoadingMembers.value = false
        }
    }

    fun loadMoreMembers(limit: Int = 10) {
        if (_isLoadingMoreMembers.value || _isLoadingMembers.value || !_canLoadMoreMembers.value) {
            return
        }

        launchBusy {
            _isLoadingMoreMembers.value = true
            val nextPage = _currentMemberPage.value + 1

            val result = hrRepository.getMembers(
                page = nextPage,
                limit = limit,
                search = activeMemberSearch,
                status = activeMemberStatus
            )

            result.fold(
                onSuccess = { response ->
                    val newMembers = response.data
                    if (newMembers.isNotEmpty()) {
                        val updatedList = _members.value + newMembers
                        _members.value = updatedList
                        _currentMemberPage.value = nextPage
                        _membersTotal.value = response.total

                        _canLoadMoreMembers.value = updatedList.size < response.total
                    } else {
                        _canLoadMoreMembers.value = false
                    }
                },
                onFailure = {
                    // Do not permanently lock pagination so user can retry on scroll
                }
            )
            _isLoadingMoreMembers.value = false
        }
    }

    fun refreshMembers() {
        fetchMembers(page = 1, search = activeMemberSearch, status = activeMemberStatus)
    }

    fun clearMembersError() {
        _membersError.value = null
    }

    // ═══════════════════════════════════════════════
    // ── Member Detail (VIEW / EDIT prefill) ──
    // ═══════════════════════════════════════════════
    private val _memberDetail = MutableStateFlow<MemberDetail?>(null)
    val memberDetail: StateFlow<MemberDetail?> = _memberDetail.asStateFlow()

    private val _isLoadingMemberDetail = MutableStateFlow(false)
    val isLoadingMemberDetail: StateFlow<Boolean> = _isLoadingMemberDetail.asStateFlow()

    private val _memberDetailError = MutableStateFlow<String?>(null)
    val memberDetailError: StateFlow<String?> = _memberDetailError.asStateFlow()

    private val _uploadPictureState = MutableStateFlow<UploadPictureState>(UploadPictureState.Idle)
    val uploadPictureState: StateFlow<UploadPictureState> = _uploadPictureState.asStateFlow()

    private val _deletePictureState = MutableStateFlow<DeletePictureState>(DeletePictureState.Idle)
    val deletePictureState: StateFlow<DeletePictureState> = _deletePictureState.asStateFlow()

    fun uploadProfilePicture(memberId: String, file: File) {
        launchBusy {
            _uploadPictureState.value = UploadPictureState.Loading
            val result = hrRepository.uploadProfilePicture(memberId, file)
            result.onSuccess { response ->
                val url = response.member.profilePicture.orEmpty()
                _uploadPictureState.value = UploadPictureState.Success(url)
            }.onFailure { e ->
                _uploadPictureState.value = UploadPictureState.Error(e.message ?: "Upload failed")
            }
        }
    }

    fun resetUploadPictureState() {
        _uploadPictureState.value = UploadPictureState.Idle
    }

    fun deleteProfilePicture(memberId: String) {
        launchBusy {
            _deletePictureState.value = DeletePictureState.Loading
            val result = hrRepository.deleteProfilePicture(memberId)
            result.onSuccess {
                _deletePictureState.value = DeletePictureState.Success
            }.onFailure { e ->
                _deletePictureState.value = DeletePictureState.Error(e.message ?: "Delete failed")
            }
        }
    }

    fun resetDeletePictureState() {
        _deletePictureState.value = DeletePictureState.Idle
    }

    fun fetchMemberDetail(memberId: String) {
        launchBusy {
            _isLoadingMemberDetail.value = true
            _memberDetailError.value = null
            val result = hrRepository.getMemberDetail(memberId)
            result.fold(
                onSuccess = { _memberDetail.value = it },
                onFailure = { e -> _memberDetailError.value = e.message ?: "Failed to fetch employee detail" }
            )
            _isLoadingMemberDetail.value = false
        }
    }

    fun clearMemberDetail() {
        _memberDetail.value = null
    }

    // ═══════════════════════════════════════════════
    // ── Create / Update Member ──
    // ═══════════════════════════════════════════════
    private val _createMemberState = MutableStateFlow<CreateMemberState>(CreateMemberState.Idle)
    val createMemberState: StateFlow<CreateMemberState> = _createMemberState.asStateFlow()

    fun createMember(request: CreateMemberRequest) {
        launchBusy {
            _createMemberState.value = CreateMemberState.Loading
            val result = hrRepository.createMember(request)
            result.fold(
                onSuccess = {
                    _createMemberState.value = CreateMemberState.Success(it)
                    refreshMembers()
                },
                onFailure = { e -> _createMemberState.value = CreateMemberState.Error(e.message ?: "Failed to create employee") }
            )
        }
    }

    fun updateMember(memberId: String, request: UpdateMemberRequest) {
        launchBusy {
            _createMemberState.value = CreateMemberState.Loading
            val result = hrRepository.updateMember(memberId, request)
            result.fold(
                onSuccess = {
                    _createMemberState.value = CreateMemberState.Success(it)
                    refreshMembers()
                },
                onFailure = { e -> _createMemberState.value = CreateMemberState.Error(e.message ?: "Failed to update employee") }
            )
        }
    }

    fun resetCreateMemberState() {
        _createMemberState.value = CreateMemberState.Idle
    }

    sealed class UploadPictureState {
        object Idle : UploadPictureState()
        object Loading : UploadPictureState()
        data class Success(val pictureUrl: String) : UploadPictureState()
        data class Error(val message: String) : UploadPictureState()
    }

    sealed class DeletePictureState {
        object Idle : DeletePictureState()
        object Loading : DeletePictureState()
        object Success : DeletePictureState()
        data class Error(val message: String) : DeletePictureState()
    }

    sealed class CreateMemberState {
        object Idle : CreateMemberState()
        object Loading : CreateMemberState()
        data class Success(val member: CreatedMemberFullData) : CreateMemberState()
        data class Error(val message: String) : CreateMemberState()
    }
}