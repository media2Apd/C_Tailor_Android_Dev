package com.cuso.mobile.model

data class DashboardResponse(
    val success: Boolean,
    val message: String,
    val data: DashboardData
)

data class DashboardData(
    val stats: List<DashboardStatDto> = emptyList(),
    val collection: CollectionData? = null,
    val leadChart: List<LeadChartItem> = emptyList(),
    val invoiceChart: List<Any?> = emptyList(),
    val paymentChart: List<Any?> = emptyList(),
    val operations: List<OperationItem> = emptyList(),
    val orderStatus: OrderStatusData? = null,
    val activeOrders: List<ActiveOrderItem> = emptyList(),
    val inventory: List<Any?> = emptyList(),
    val transactions: List<Any?> = emptyList()
)

data class DashboardStatDto(
    val title: String,
    val value: Double,
    val change: Double,
    val type: String,    // "currency" | "number"
    val color: String    // "green" | "red"
)

data class CollectionData(
    val percentage: Double,
    val change: Double,
    val totalInvoiced: Double,
    val paymentsReceived: Double,
    val totalPending: Double,
    val target: Double
)

data class LeadChartItem(
    val name: String,
    val count: Int,
    val color: String
)

data class OperationItem(
    val customer: String,
    val type: String,
    val status: String
)

data class OrderStatusData(
    val total: Int,
    val inProgress: Int,
    val completed: Int,
    val scheduled: Int
)

data class ActiveOrderItem(
    val orderNumber: String,
    val customer: String,
    val amount: Double,
    val status: String
)