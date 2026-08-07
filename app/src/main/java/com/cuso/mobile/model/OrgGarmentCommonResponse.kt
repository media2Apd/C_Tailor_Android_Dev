// In your model package - Update OrgGarmentModels.kt
package com.cuso.mobile.model

// GET Org Garments Response
data class OrgGarmentResponse(
    val success: Boolean,
    val data: List<OrgGarmentCategory>
)

// ADD Org Garment Response
data class AddOrgGarmentResponse(
    val success: Boolean,
    val data: AddOrgGarmentData,
    val message: String
)
data class AddGarmentRequest(
    val categoryId: String
)
data class AddOrgGarmentData(
    val added: Int,
    val categories: List<OrgGarmentCategory>
)

// REMOVE Org Garment Response
data class RemoveOrgGarmentResponse(
    val success: Boolean,
    val data: RemoveOrgGarmentData
)

data class RemoveOrgGarmentData(
    val message: String
)

// Org Garment Category
data class OrgGarmentCategory(
    val _id: String,
    val categoryName: String,
    val organizationId: String,
    val categoryId: OrgCategoryDetail?,
    val isActive: Boolean=false,
    val __v: Int,
    val createdAt: String,
    val updatedAt: String
)

data class OrgCategoryDetail(
    val _id: String,
    val categoryName: String,
    val measurements: List<OrgMeasurement>,
    val models: List<OrgModel>
)

data class OrgMeasurement(
    val fieldName: String,
    val unit: String,
    val inputType: String,
    val inputCount: Int,
    val options: List<String>,
    val isCommonField: Boolean,
    val commonFieldId: String?,
    val _id: String
)

data class OrgModel(
    val modelName: String,
    val pieceRate: Int,
    val modelIcon: String,
    val _id: String
)

// GET /api/org-garments/view-all response
data class ActiveOrgGarmentResponse(
    val success: Boolean,
    val data: ActiveOrgGarmentData
)

data class ActiveOrgGarmentData(
    val totalAssigned: Int,
    val active: Int,
    val inactive: Int,
    val availableSlots: Int?,
    val categories: List<OrgGarmentCategory>  // ← இதுதான் நமக்கு வேணும்
)