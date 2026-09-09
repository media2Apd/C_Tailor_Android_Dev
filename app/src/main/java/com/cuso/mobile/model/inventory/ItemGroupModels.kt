package com.cuso.mobile.model.inventory

import com.google.gson.annotations.SerializedName

// =============================================================================
// 1. REQUEST MODELS
// =============================================================================

data class CreateItemGroupRequest(
    @SerializedName("name") val name: String,
    @SerializedName("categoryId") val categoryId: String? = null,
    @SerializedName("subCategoryId") val subCategoryId: String? = null,
    @SerializedName("unit") val unit: String? = "pcs",
    @SerializedName("productType") val productType: String? = "physical",
    @SerializedName("type") val type: String? = "goods",
    @SerializedName("brand") val brand: String? = null,
    @SerializedName("manufacturer") val manufacturer: String? = null,
    @SerializedName("shortDescription") val shortDescription: String? = null,
    @SerializedName("longDescription") val longDescription: String? = null,
    @SerializedName("variantAttributes") val variantAttributes: List<VariantAttributeDto> = emptyList(),
    @SerializedName("pricing") val pricing: PricingDto? = null,
    @SerializedName("status") val status: String? = "active"
)

// =============================================================================
// 2. RESPONSE MODELS
// =============================================================================

data class CreateItemGroupResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: ItemGroupDto
)

data class ItemGroupListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("groups") val groups: List<ItemGroupDto> = emptyList(),
    @SerializedName("total") val total: Int = 0,
    @SerializedName("page") val page: Int = 1,
    @SerializedName("pageSize") val pageSize: Int = 20,
    @SerializedName("totalPages") val totalPages: Int = 1
)

// =============================================================================
// 3. CORE ITEM GROUP DTO
// =============================================================================

data class ItemGroupDto(
    @SerializedName("_id") val id: String,
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("name") val name: String,
    @SerializedName("groupCode") val groupCode: String? = null,
    @SerializedName("unit") val unit: String? = null,
    @SerializedName("productType") val productType: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("fabric") val fabric: Boolean? = false,
    @SerializedName("categoryId") val categoryId: String? = null,
    @SerializedName("subCategoryId") val subCategoryId: String? = null,
    @SerializedName("brand") val brand: String? = null,
    @SerializedName("manufacturer") val manufacturer: String? = null,
    @SerializedName("shortDescription") val shortDescription: String? = null,
    @SerializedName("longDescription") val longDescription: String? = null,
    @SerializedName("featureList") val featureList: List<String> = emptyList(),
    @SerializedName("productAttributes") val productAttributes: List<Any> = emptyList(),
    @SerializedName("variantAttributes") val variantAttributes: List<VariantAttributeDto> = emptyList(),
    @SerializedName("variantCount") val variantCount: Int = 0,
    @SerializedName("taxPreference") val taxPreference: String? = null,
    @SerializedName("pricing") val pricing: PricingDto? = null,
    @SerializedName("pricingTax") val pricingTax: PricingTaxDto? = null,
    @SerializedName("media") val media: MediaDto? = null,
    @SerializedName("publishing") val publishing: PublishingDto? = null,
    @SerializedName("seo") val seo: SeoDto? = null,
    @SerializedName("channels") val channels: List<ChannelDto> = emptyList(),
    @SerializedName("status") val status: String? = null,
    @SerializedName("isDeleted") val isDeleted: Boolean = false,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

// =============================================================================
// 4. NESTED & SUPPORTING DTOs
// =============================================================================

data class VariantAttributeDto(
    @SerializedName("name") val name: String,
    @SerializedName("values") val values: List<String> = emptyList(),
    @SerializedName("required") val required: Boolean = false
)

data class PricingDto(
    @SerializedName("currency") val currency: String? = "INR",
    @SerializedName("costPrice") val costPrice: Double? = 0.0,
    @SerializedName("sellingPrice") val sellingPrice: Double? = 0.0,
    @SerializedName("discountPrice") val discountPrice: Double? = null,
    @SerializedName("tiers") val tiers: List<Any> = emptyList()
)

data class PricingTaxDto(
    @SerializedName("costPrice") val costPrice: Double? = 0.0,
    @SerializedName("sellingPrice") val sellingPrice: Double? = 0.0,
    @SerializedName("taxPreference") val taxPreference: String? = null,
    @SerializedName("taxRate") val taxRate: String? = null,
    @SerializedName("hsnCode") val hsnCode: String? = null
)

data class MediaDto(
    @SerializedName("images") val images: List<ImageDto> = emptyList()
)

data class ImageDto(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("fileUrl") val fileUrl: String,
    @SerializedName("publicId") val publicId: String? = null,
    @SerializedName("variantValue") val variantValue: String? = null,
    @SerializedName("sortOrder") val sortOrder: Int? = 0
)

data class PublishingDto(
    @SerializedName("publishOption") val publishOption: String? = null,
    @SerializedName("visibility") val visibility: String? = null
)

data class SeoDto(
    @SerializedName("title") val title: String? = null,
    @SerializedName("metaDescription") val metaDescription: String? = null,
    @SerializedName("urlSlug") val urlSlug: String? = null,
    @SerializedName("searchTags") val searchTags: List<String> = emptyList()
)

data class ChannelDto(
    @SerializedName("channelType") val channelType: String? = null,
    @SerializedName("enabled") val enabled: Boolean = false,
    @SerializedName("visibility") val visibility: String? = null
)

data class DeleteItemGroupResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null
)