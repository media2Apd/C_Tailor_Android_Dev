package com.cuso.tailor.model.settings

import com.google.gson.annotations.SerializedName

data class TaxRegistrationResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: List<TaxRegistrationItemDto> = emptyList(),
    @SerializedName("pagination") val pagination: TaxRegistrationPagination? = null
)

data class TaxRegistrationItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("branchId") val branchId: String? = null,
    @SerializedName("countryCode") val countryCode: String? = null,
    @SerializedName("country") val country: String? = null,
    @SerializedName("subdivisionCode") val subdivisionCode: String? = null,
    @SerializedName("subdivisionName") val subdivisionName: String? = null,
    @SerializedName("registrationType") val registrationType: String? = null,
    @SerializedName("registrationNumber") val registrationNumber: String? = null,
    @SerializedName("registeredLegalName") val registeredLegalName: String? = null,
    @SerializedName("registeredAddress") val registeredAddress: String? = null,
    @SerializedName("taxAuthorityId") val taxAuthorityId: String? = null,
    @SerializedName("taxAuthorityName") val taxAuthorityName: String? = null,
    @SerializedName("taxRegime") val taxRegime: String? = null,
    @SerializedName("taxScheme") val taxScheme: String? = null,
    @SerializedName("filingMethod") val filingMethod: String? = null,
    @SerializedName("filingFrequency") val filingFrequency: String? = null,
    @SerializedName("validFrom") val validFrom: String? = null,
    @SerializedName("validTo") val validTo: String? = null,
    @SerializedName("taxRoundingRule") val taxRoundingRule: String? = null,
    @SerializedName("isGroupRegistration") val isGroupRegistration: Boolean = false,
    @SerializedName("groupRepresentativeName") val groupRepresentativeName: String? = null,
    @SerializedName("groupRepresentativeRegistrationNumber") val groupRepresentativeRegistrationNumber: String? = null,
    @SerializedName("returnPeriodStart") val returnPeriodStart: String? = null,
    @SerializedName("authorizedFilingUsers") val authorizedFilingUsers: List<String> = emptyList(),
    @SerializedName("taxAgentNumber") val taxAgentNumber: String? = null,
    @SerializedName("sendEmailReminder") val sendEmailReminder: Boolean = false,
    @SerializedName("notifyOnValidator") val notifyOnValidator: Boolean = false,
    @SerializedName("reminderDaysBeforeDeadline") val reminderDaysBeforeDeadline: Int = 0,
    @SerializedName("showTaxBreakdownOnInvoice") val showTaxBreakdownOnInvoice: Boolean = true,
    @SerializedName("showHsnSacOnInvoice") val showHsnSacOnInvoice: Boolean = true,
    @SerializedName("showPlaceOfSupplyOnInvoice") val showPlaceOfSupplyOnInvoice: Boolean = true,
    @SerializedName("defaultIntraStateTaxGroupId") val defaultIntraStateTaxGroupId: String? = null,
    @SerializedName("defaultInterStateTaxGroupId") val defaultInterStateTaxGroupId: String? = null,
    @SerializedName("approvalStatus") val approvalStatus: String? = null,
    @SerializedName("verificationStatus") val verificationStatus: String? = null,
    @SerializedName("verifiedAt") val verifiedAt: String? = null,
    @SerializedName("verificationReference") val verificationReference: String? = null,
    @SerializedName("isPrimary") val isPrimary: Boolean = false,
    @SerializedName("status") val status: String = "Active",
    @SerializedName("createdBy") val createdBy: TaxUserDto? = null,
    @SerializedName("updatedBy") val updatedBy: TaxUserDto? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

data class TaxUserDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("firstName") val firstName: String? = null,
    @SerializedName("lastName") val lastName: String? = null,
    @SerializedName("memberId") val memberId: String? = null
) {
    val fullName: String
        get() = listOfNotNull(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ")
}

data class TaxRegistrationPagination(
    @SerializedName("total") val total: Int = 0,
    @SerializedName("page") val page: Int = 1,
    @SerializedName("limit") val limit: Int = 10,
    @SerializedName("totalPages") val totalPages: Int = 1,
    @SerializedName("hasNextPage") val hasNextPage: Boolean = false,
    @SerializedName("hasPreviousPage") val hasPreviousPage: Boolean = false
)

data class TaxRegistrationViewOneResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: TaxRegistrationItemDto? = null,
    @SerializedName("message") val message: String? = null
)