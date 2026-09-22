package com.cuso.tailor.model.inventory

import com.google.gson.annotations.SerializedName

/**
 * Request payload for viewing multiple purchase receives.
 */
data class ViewMultipleReceivesRequest(
    @SerializedName("receiveIds") val receiveIds: List<String>
)

/**
 * Response payload containing the list of purchase receive details.
 */
data class ViewMultipleReceivesResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<PurchaseReceiveItem> = emptyList()
)