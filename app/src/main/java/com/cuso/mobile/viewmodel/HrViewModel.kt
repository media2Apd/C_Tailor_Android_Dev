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
import androidx.lifecycle.viewModelScope
import com.cuso.mobile.model.hr.CreateMemberRequest
import com.cuso.mobile.model.hr.CreatedMemberFullData
import com.cuso.mobile.model.hr.MemberDetail
import com.cuso.mobile.model.hr.MemberItem
import com.cuso.mobile.model.hr.RoleItem
import com.cuso.mobile.model.hr.ShiftItem
import com.cuso.mobile.model.hr.UpdateMemberRequest
import com.cuso.mobile.repository.HrRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * HrViewModel - Handles Roles, Shifts, Members (list + detail), and Create/Update
 * for the HR module.
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
        viewModelScope.launch {
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
        viewModelScope.launch {
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
    // ── Members (list) ──
    // ═══════════════════════════════════════════════
    private val _members = MutableStateFlow<List<MemberItem>>(emptyList())
    val members: StateFlow<List<MemberItem>> = _members.asStateFlow()

    private val _membersTotal = MutableStateFlow(0)
    val membersTotal: StateFlow<Int> = _membersTotal.asStateFlow()

    private val _isLoadingMembers = MutableStateFlow(false)
    val isLoadingMembers: StateFlow<Boolean> = _isLoadingMembers.asStateFlow()

    private val _membersError = MutableStateFlow<String?>(null)
    val membersError: StateFlow<String?> = _membersError.asStateFlow()

    fun fetchMembers(
        page: Int = 1,
        limit: Int = 10,
        search: String? = null,
        status: String? = null
    ) {
        viewModelScope.launch {
            _isLoadingMembers.value = true
            _membersError.value = null
            val result = hrRepository.getMembers(page, limit, search, status)
            result.fold(
                onSuccess = { response ->
                    _members.value = response.data
                    _membersTotal.value = response.total
                },
                onFailure = { e -> _membersError.value = e.message ?: "Failed to fetch employees" }
            )
            _isLoadingMembers.value = false
        }
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
    val uploadPictureState: StateFlow<UploadPictureState> = _uploadPictureState

    private val _deletePictureState = MutableStateFlow<DeletePictureState>(DeletePictureState.Idle)
    val deletePictureState: StateFlow<DeletePictureState> = _deletePictureState

    fun uploadProfilePicture(memberId: String, file: File) {
        viewModelScope.launch {
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
        viewModelScope.launch {
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
        viewModelScope.launch {
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



    private val _createMemberState = MutableStateFlow<CreateMemberState>(CreateMemberState.Idle)
    val createMemberState: StateFlow<CreateMemberState> = _createMemberState.asStateFlow()

    fun createMember(request: CreateMemberRequest) {
        viewModelScope.launch {
            _createMemberState.value = CreateMemberState.Loading
            val result = hrRepository.createMember(request)
            result.fold(
                onSuccess = { _createMemberState.value = CreateMemberState.Success(it) },
                onFailure = { e -> _createMemberState.value = CreateMemberState.Error(e.message ?: "Failed to create employee") }
            )
        }
    }

    fun updateMember(memberId: String, request: UpdateMemberRequest) {
        viewModelScope.launch {
            _createMemberState.value = CreateMemberState.Loading
            val result = hrRepository.updateMember(memberId, request)
            result.fold(
                onSuccess = { _createMemberState.value = CreateMemberState.Success(it) },
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

    // ═══════════════════════════════════════════════
    // ── Create / Update Member ──
    // ═══════════════════════════════════════════════
    sealed class CreateMemberState {
        object Idle : CreateMemberState()
        object Loading : CreateMemberState()
        data class Success(val member: CreatedMemberFullData) : CreateMemberState()
        data class Error(val message: String) : CreateMemberState()
    }
}
