@file:Suppress("unused","AssignedValueIsNeverRead", "unusedVariable")

package com.cuso.tailor.model.hr

import android.annotation.SuppressLint
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

// ── Static Reference Catalogs ──

data class ProductionSpecializationOption(
    val code: String,
    val name: String,
    val tagLabel: String,
    val description: String
)

data class ProductionShiftOption(
    val name: String,
    val startTime: String,
    val endTime: String,
    val displayLabel: String
)

val PRODUCTION_SPECIALIZATIONS = listOf(
    ProductionSpecializationOption(
        code = "PATTERN",
        name = "Pattern Making",
        tagLabel = "Drafting",
        description = "Draping, bespoke sizing blocks, CAD cut plans"
    ),
    ProductionSpecializationOption(
        code = "CUTTING",
        name = "Cutting & Sizing",
        tagLabel = "Cutter",
        description = "Fabric grain mapping, shear alignment, zero-waste trims"
    ),
    ProductionSpecializationOption(
        code = "STITCHING",
        name = "Stitching & Assembly",
        tagLabel = "Assembly",
        description = "Single needle lockstitch, canvas padding, lining join"
    ),
    ProductionSpecializationOption(
        code = "EMBROIDERY",
        name = "Hand & Machine Embroidery",
        tagLabel = "Embroidery",
        description = "Aari, zardozi needlework, sequin placement"
    ),
    ProductionSpecializationOption(
        code = "FINISHING",
        name = "Finishing & Pressing",
        tagLabel = "Finishing",
        description = "Steam pressing, blind hem, thread clipping"
    ),
    ProductionSpecializationOption(
        code = "QC",
        name = "Quality Check & Inspection",
        tagLabel = "QC",
        description = "Fabric flaw check, dimensional measurement audit"
    )
)

val PRODUCTION_SHIFTS = listOf(
    ProductionShiftOption(
        name = "Morning Shift",
        startTime = "08:30",
        endTime = "17:00",
        displayLabel = "Morning Shift (08:30 AM - 05:00 PM)"
    ),
    ProductionShiftOption(
        name = "Evening Shift",
        startTime = "13:00",
        endTime = "21:30",
        displayLabel = "Evening Shift (01:00 PM - 09:30 PM)"
    )
)

val GARMENT_PROFILE_SUGGESTIONS = listOf(
    "Bridal Wear (Lehenga / Sherwani)",
    "Traditional & Bespoke Kurta",
    "Bespoke 3-Piece Suiting",
    "Alterations & Fit Rework"
)

val TEAM_ROLE_OPTIONS = listOf(
    "Team_lead" to "Team Lead",
    "Member" to "Member",
    "Quality_audit" to "Quality Audit"
)

// ── API Response & DTO Models ──

data class ProductionTeamListResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<ProductionTeamDto> = emptyList(),
    val pagination: ProductionTeamPaginationDto? = null
)

data class ProductionTeamSingleResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: ProductionTeamDto? = null
)

data class ProductionTeamPaginationDto(
    val total: Int = 0,
    val page: Int = 1,
    val limit: Int = 10,
    val totalPages: Int = 1,
    val hasNextPage: Boolean = false,
    val hasPreviousPage: Boolean = false
)

data class ProductionTeamDto(
    @SerializedName("_id")
    val id: String = "",
    val organizationId: String? = null,
    val teamCode: String = "",
    val teamSeq: Int = 0,
    val name: String = "",
    val scopeDescription: String = "",
    val location: String = "",
    // Backend may return department as an ObjectId string or a populated object
    val department: JsonElement? = null,
    val operationalStatus: String = "Active",
    val specializations: List<ProductionSpecializationDto> = emptyList(),
    val garmentProfiles: List<String> = emptyList(),
    val members: List<ProductionTeamMemberDto> = emptyList(),
    val teamLead: ProductionTeamLeadDto? = null,
    val defaultShift: ProductionShiftDto? = null,
    val workCenter: String = "",
    // Backend may return secondaryCoordinator as an ObjectId string or a populated object
    val secondaryCoordinator: JsonElement? = null,
    val lifecycleStatus: String = "Published",
    val revision: Int = 1,
    val workloadPercent: Double? = null,
    val isTemplate: Boolean = false,
    val sourceTemplateName: String = "",
    val duplicatedFromCode: String = "",
    val isDeleted: Boolean = false,
    val createdBy: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class ProductionSpecializationDto(
    val code: String = "",
    val name: String = "",
    val tagLabel: String = "",
    val description: String = ""
)

data class ProductionTeamMemberDto(
    val empId: String = "",
    val name: String = "",
    val designation: String = "",
    val role: String = "Member",
    val primarySkill: String = "",
    val subSkills: List<String> = emptyList(),
    val stationLabel: String = "",
    val experienceYears: Int = 0,
    val leftOn: String? = null,
    val joinedOn: String? = null
)

data class ProductionTeamLeadDto(
    val empId: String = "",
    val name: String = "",
    val designation: String = ""
)

data class ProductionShiftDto(
    val name: String = "",
    val startTime: String = "",
    val endTime: String = ""
)

// ── Assigned Work Models ──

data class ProductionTeamAssignedWorkResponse(
    val success: Boolean = false,
    val data: ProductionTeamAssignedWorkData? = null
)

data class ProductionTeamAssignedWorkData(
    val orders: List<ProductionAssignedOrderDto> = emptyList(),
    val completedThisMonth: Int = 0
)

data class ProductionAssignedOrderDto(
    @SerializedName("_id")
    val id: String = "",
    val orderNo: String = "",
    val garment: String = "",
    val customerName: String = "",
    val stage: String = "",
    val priority: String = "normal",
    val scheduledStart: String? = null,
    val scheduledEnd: String? = null,
    val status: String = "scheduled",
    val assignedStaff: ProductionAssignedStaffDto? = null
)

data class ProductionAssignedStaffDto(
    val name: String = "",
    val note: String = "",
    val scope: String = "individual"
)

// ── Request Payloads ──

data class CreateUpdateProductionTeamRequest(
    val name: String,
    val scopeDescription: String,
    val location: String,
    val department: String?,
    val operationalStatus: String,
    val specializations: List<ProductionSpecializationDto>,
    val garmentProfiles: List<String>,
    val members: List<ProductionTeamMemberDto>,
    val teamLead: ProductionTeamLeadDto?,
    val defaultShift: ProductionShiftDto,
    val workCenter: String,
    val secondaryCoordinator: String?,
    val lifecycleStatus: String = "Published",
    val sourceTemplateName: String = "",
    val duplicatedFromCode: String = ""
)

data class UpdateProductionTeamStatusRequest(
    val operationalStatus: String
)

// ── UI Summary & Dropdown Helper Models ──

data class ProductionTeamKpiSummary(
    val totalTeams: Int = 0,
    val activeTeams: Int = 0,
    val activePercentage: String = "0.0",
    val totalMembers: Int = 0,
    val availableTeams: Int = 0,
    val facilitiesCount: Int = 0
)

data class ProductionTeamDropdownOption(
    val id: String,
    val code: String = "",
    val name: String,
    val subtitle: String = ""
)

// ── Domain Mapping Extensions ──

fun ProductionTeamDto.activeMembers(): List<ProductionTeamMemberDto> =
    members.filter { it.leftOn.isNullOrBlank() }

fun ProductionTeamDto.workloadValue(): Int =
    (workloadPercent ?: 0.0).toInt().coerceIn(0, 100)

fun ProductionTeamDto.availabilityStatus(): String {
    if (!operationalStatus.equals("Active", ignoreCase = true) ||
        lifecycleStatus.equals("Archived", ignoreCase = true)
    ) {
        return "Unavailable"
    }
    val load = workloadValue()
    return when {
        load >= 100 -> "Fully Booked"
        load >= 80 -> "Near Capacity"
        else -> "Available"
    }
}

fun ProductionTeamDto.displayStatus(): String = when {
    lifecycleStatus.equals("Archived", ignoreCase = true) -> "Archived"
    lifecycleStatus.equals("Draft", ignoreCase = true) -> "Draft"
    operationalStatus.equals("Inactive", ignoreCase = true) -> "Inactive"
    else -> "Active"
}

fun JsonElement?.extractRefId(): String {
    if (this == null || isJsonNull) return ""
    if (isJsonPrimitive) return asString.orEmpty()
    if (isJsonObject) {
        return asJsonObject.get("_id")?.takeIf { !it.isJsonNull }?.asString.orEmpty()
    }
    return ""
}

@SuppressLint("CheckResult")
fun JsonElement?.extractRefName(fallbackLabel: String = "—"): String {
    if (this == null || isJsonNull) return fallbackLabel
    if (isJsonPrimitive) return fallbackLabel
    if (isJsonObject) {
        val obj = asJsonObject
        val fullName = obj.get("fullName")?.takeIf { !it.isJsonNull }?.asString
        val name = obj.get("name")?.takeIf { !it.isJsonNull }?.asString
        val deptName = obj.get("departmentName")?.takeIf { !it.isJsonNull }?.asString
        return fullName ?: name ?: deptName ?: fallbackLabel
    }
    return fallbackLabel
}