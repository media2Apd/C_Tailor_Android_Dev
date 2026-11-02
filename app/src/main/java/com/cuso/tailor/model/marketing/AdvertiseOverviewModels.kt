@file:Suppress("unused")
package com.cuso.tailor.model.marketing

import com.google.gson.annotations.SerializedName

// =========================================================================
// 1. OAUTH & AD PLATFORM INTEGRATIONS
// =========================================================================

data class OAuthStatusResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<OAuthPlatformStatusDto> = emptyList(),
    @SerializedName("message") val message: String? = null
)

data class OAuthPlatformStatusDto(
    @SerializedName("platform") val platform: String = "",
    @SerializedName("status") val status: String = "NOT_CONNECTED",
    @SerializedName("lastSyncStatus") val lastSyncStatus: String? = null,
    @SerializedName("adAccountId") val adAccountId: String? = null,
    @SerializedName("lastSyncedAt") val lastSyncedAt: String? = null
)

data class AvailableAdAccountsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<AdAccountItemDto> = emptyList(),
    @SerializedName("message") val message: String? = null
)

data class AdAccountItemDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("currency") val currency: String? = null
)

data class SyncPlatformResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null
)

// =========================================================================
// 2. ADVERTISING OVERVIEW & KPIS
// =========================================================================

data class AdvertisingOverviewResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: AdvertisingOverviewData? = null,
    @SerializedName("message") val message: String? = null
)

data class AdvertisingOverviewData(
    @SerializedName("topRowKpis") val topRowKpis: TopRowKpisDto? = null,
    @SerializedName("secondRowKpis") val secondRowKpis: SecondRowKpisDto? = null,
    @SerializedName("platformCards") val platformCards: List<AdvertisingPlatformCardDto> = emptyList()
)

data class KpiMetricValueDto(
    @SerializedName("value") val value: Double = 0.0,
    @SerializedName("raw") val raw: Double = 0.0,
    @SerializedName("changePercentage") val changePercentage: Double? = null,
    @SerializedName("isPositive") val isPositive: Boolean? = true
)

data class TopRowKpisDto(
    @SerializedName("totalAdSpend") val totalAdSpend: KpiMetricValueDto = KpiMetricValueDto(),
    @SerializedName("impressions") val impressions: KpiMetricValueDto = KpiMetricValueDto(),
    @SerializedName("reach") val reach: KpiMetricValueDto = KpiMetricValueDto(),
    @SerializedName("clicks") val clicks: KpiMetricValueDto = KpiMetricValueDto(),
    @SerializedName("ctr") val ctr: KpiMetricValueDto = KpiMetricValueDto(),
    @SerializedName("cpc") val cpc: KpiMetricValueDto = KpiMetricValueDto(),
    @SerializedName("cpm") val cpm: KpiMetricValueDto = KpiMetricValueDto()
)

data class SecondRowKpisDto(
    @SerializedName("promotions") val promotions: KpiMetricValueDto = KpiMetricValueDto(),
    @SerializedName("conversions") val conversions: KpiMetricValueDto = KpiMetricValueDto(),
    @SerializedName("cpa") val cpa: KpiMetricValueDto = KpiMetricValueDto(),
    @SerializedName("revenue") val revenue: KpiMetricValueDto = KpiMetricValueDto(),
    @SerializedName("roas") val roas: KpiMetricValueDto = KpiMetricValueDto(),
    @SerializedName("roi") val roi: KpiMetricValueDto = KpiMetricValueDto()
)

data class AdvertisingPlatformCardDto(
    @SerializedName("platform") val platform: String = "",
    @SerializedName("spend") val spend: Double = 0.0,
    @SerializedName("impressions") val impressions: Long = 0,
    @SerializedName("reach") val reach: Long = 0,
    @SerializedName("clicks") val clicks: Long = 0,
    @SerializedName("promotions") val promotions: Long = 0,
    @SerializedName("conversions") val conversions: Long = 0,
    @SerializedName("revenue") val revenue: Double = 0.0,
    @SerializedName("roas") val roas: Double = 0.0,
    @SerializedName("ctr") val ctr: Double = 0.0,
    @SerializedName("cpc") val cpc: Double = 0.0,
    @SerializedName("cpa") val cpa: Double = 0.0,
    @SerializedName("status") val status: String = "Connected"
)

// =========================================================================
// 3. ADVERTISING CREATIVES
// =========================================================================

data class AdvertisingCreativesResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<AdvertisingCreativeDto> = emptyList(),
    @SerializedName("message") val message: String? = null
)

data class AdvertisingCreativeDto(
    @SerializedName("creativeCode") val creativeCode: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("formatType") val formatType: String = "Ad",
    @SerializedName("status") val status: String = "ACTIVE",
    @SerializedName("impressions") val impressions: Long = 0,
    @SerializedName("clicks") val clicks: Long = 0,
    @SerializedName("spend") val spend: Double = 0.0,
    @SerializedName("conversions") val conversions: Long = 0,
    @SerializedName("revenue") val revenue: Double = 0.0,
    @SerializedName("ctr") val ctr: Double = 0.0,
    @SerializedName("roas") val roas: Double = 0.0,
    @SerializedName("campaignName") val campaignName: String = "",
    @SerializedName("platform") val platform: String = "Meta"
)

data class AdIntegrationAuthorizeResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("redirectUrl") val redirectUrl: String? = null,
    @SerializedName("message") val message: String? = null
)