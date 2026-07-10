package com.cuso.mobile.view.home.sales.sales_order


import com.cuso.mobile.database.entities.SelectedGarment
import com.cuso.mobile.model.OrderOverviewData

fun OrderOverviewData.toOrderReviewData(): OrderReviewData {
    val order = this.order
    val customer = order.customerId

    return OrderReviewData(
        orderId = order._id,
        customerId = customer._id,
        branchId = order.branch._id,
        fullName = customer.name ?: "",
        countryCode = "+91",
        phone = customer.mobile.takeLast(10),
        gender = customer.gender ?: "",  // ✅ Already has null safety
        dressFor = order.wearerType ?: "",
        address = customer.address?.addressLine ?: "",
        garments = this.items.map { item ->
            SelectedGarment(
                category = item._id,
                categoryName = item.categoryName ?: "",
                categoryId = item._id,
                quantity = item.quantity ?: 1,
                price = item.stitchingCharge ?: 0.0,
                priority = item.priority ?: "Low",
                trialRequired = item.trialRequired ?: false,
                fabricSource = item.fabricDetails?.fabricSource ?: "In-House",
                fabricType = item.fabricDetails?.fabricType ?: "",
                colorTone = item.fabricDetails?.color ?: "",
                pattern = item.fabricDetails?.pattern ?: "Solid",
                models = emptyList(),
                measurements = emptyList()
            )
        },
        orderDate = flipToDdMmYyyy(order.orderDate.take(10)),
        source = order.source ?: "Walk-in",
        trialDate = order.trialDate?.take(10)?.let { flipToDdMmYyyy(it) } ?: "",
        deliveryDate = order.deliveryDate?.take(10)?.let { flipToDdMmYyyy(it) } ?: "",
        discount = order.discount ?: 0.0,
        paidSoFar = order.totalPaid ?: 0.0,
        designImages = emptyList(),
        existingImageUrls = emptyList()
    )
}
private fun flipToDdMmYyyy(isoDate: String): String {
    val parts = isoDate.split("-")
    return if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}" else isoDate
}