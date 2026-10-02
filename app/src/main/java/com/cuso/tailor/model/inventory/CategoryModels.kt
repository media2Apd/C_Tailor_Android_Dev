package com.cuso.tailor.model.inventory

import com.google.gson.annotations.SerializedName

data class CategoryListResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("categories")
    val categories: List<CategoryItemDto> = emptyList(),
    @SerializedName("total")
    val total: Int = 0,
    @SerializedName("page")
    val page: Int = 1,
    @SerializedName("pageSize")
    val pageSize: Int = 20,
    @SerializedName("totalPages")
    val totalPages: Int = 1
)

data class CategoryItemDto(
    @SerializedName("_id")
    val id: String,
    @SerializedName("organizationId")
    val organizationId: String?,
    @SerializedName("name")
    val name: String,
    @SerializedName("code")
    val code: String?,
    @SerializedName("parentCategoryId")
    val parentCategoryId: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("status")
    val status: String?,
    @SerializedName("productCount")
    val productCount: Int = 0,
    @SerializedName("createdBy")
    val createdBy: String?,
    @SerializedName("isDeleted")
    val isDeleted: Boolean = false,
    @SerializedName("createdAt")
    val createdAt: String?,
    @SerializedName("updatedAt")
    val updatedAt: String?,
    @SerializedName("__v")
    val v: Int? = null
)

/**
 * API response for viewing a single category detail.
 */
data class CategoryViewOneResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("data")
    val data: CategoryItemDto?
)

/**
 * Payload used to create or update an inventory category.
 */
data class CreateCategoryRequest(
    @SerializedName("name")
    val name: String,
    @SerializedName("code")
    val code: String?,
    @SerializedName("parentCategoryId")
    val parentCategoryId: String? = null,
    @SerializedName("description")
    val description: String? = null
)
/**
 * Response model received after deleting a category.
 */
data class DeleteCategoryResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String?
)