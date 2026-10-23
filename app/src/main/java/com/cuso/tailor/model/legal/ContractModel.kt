package com.cuso.tailor.model.legal

import com.google.gson.annotations.SerializedName

data class LegalContractsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: List<LegalContractDto>?,
    @SerializedName("pagination") val pagination: LegalPagination?
)

data class LegalContractDto(
    @SerializedName("_id") val id: String,
    @SerializedName("contractType") val contractType: String?,
    @SerializedName("contractName") val contractName: String?,
    @SerializedName("contractNumber") val contractNumber: String?,
    @SerializedName("contractDescription") val contractDescription: String?,
    @SerializedName("countryName") val countryName: String?,
    @SerializedName("region") val region: String?,
    @SerializedName("legalEntity") val legalEntity: String?,
    @SerializedName("partyType") val partyType: String?,
    @SerializedName("primaryParty") val primaryParty: String?,
    @SerializedName("secondaryParty") val secondaryParty: String?,
    @SerializedName("startDate") val startDate: String?,
    @SerializedName("endDate") val endDate: String?,
    @SerializedName("renewalType") val renewalType: String?,
    @SerializedName("renewalNoticePeriod") val renewalNoticePeriod: Int?,
    @SerializedName("contractValue") val contractValue: Double?,
    @SerializedName("currency") val currency: String?,
    @SerializedName("riskLevel") val riskLevel: String?,
    @SerializedName("priority") val priority: String?,
    @SerializedName("confidentiality") val confidentiality: String?,
    @SerializedName("contractCategory") val contractCategory: String?,
    @SerializedName("renewalStatus") val renewalStatus: String?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?
)

data class LegalPagination(
    @SerializedName("total") val total: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("totalPages") val totalPages: Int,
    @SerializedName("hasNextPage") val hasNextPage: Boolean,
    @SerializedName("hasPreviousPage") val hasPreviousPage: Boolean
)