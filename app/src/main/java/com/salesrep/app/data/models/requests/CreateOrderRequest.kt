package com.salesrep.app.data.models.requests

import androidx.room.Entity
import androidx.room.PrimaryKey

data class CreateOrderRequest(
    var orders: ArrayList<CreateOrderTemplate>
)

@Entity(tableName = "createOrder")
data class CreateOrderTemplate(
    @PrimaryKey(autoGenerate = true)
    var id: Int?=null,
    var Order: CreateOrderData,
    var products: ArrayList<CreateOrderProduct>?= null
)

data class CreateOrderData(
    var id: Int?=null,
    var account_id: Int?,
    var integration_id: String?,
    var integration_total: Double?,
    var lov_order_status: String?,
    var lov_order_type: String?= null,
    var tcp_activity_integration_id: String?=null,
    var lov_return_reason: String?= null
)

data class CreateOrderProduct(
    var integration_total: Double?,
    var lov_invbinproduct_status: String?=null,
    var product_id: Int?,
    var product_qty: Double?
)