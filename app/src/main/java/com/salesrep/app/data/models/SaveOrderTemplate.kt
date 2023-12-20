package com.salesrep.app.data.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.salesrep.app.data.models.response.*
import kotlinx.android.parcel.Parcelize


@Entity(tableName = "savedOrder")
@Parcelize
class SaveOrderTemplate (
    @PrimaryKey(autoGenerate = true)
    var id: Long?=null,
    var routeId: Int?=null,
    var taskId: Int?=null,
    var accountId: Int?=null,
    var orderId: Int?=null,
    var lov_order_type: String?=null,
    var tcp_activity_integration_id: String?=null,
    var lov_return_reason: String?=null,
    var Account : AccountTemplate?= null,
    var ProductAssortment : List<ProductAssortment>?= null,
    var Promotions : List<PromotionData>?= null,
    var Paymentprofiles : ArrayList<Paymentprofiles>?= null,
    var PendingPayments : ArrayList<PendingPaymentTemplate>?= null,
    var CartProducts: List<ProductAssortment>?=null,
    var Order: OrderTemplate?=null,
    var Payments : ArrayList<OrderPaymentTemplate>?= null
): Parcelable

