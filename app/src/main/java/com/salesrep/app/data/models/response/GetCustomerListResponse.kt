package com.salesrep.app.data.models.response

import android.os.Parcelable
import com.google.gson.JsonObject
import com.salesrep.app.data.models.*
import kotlinx.android.parcel.Parcelize

data class GetCustomerListResponse(
    val pagination: PaginationTemplate,
    val rows: List<CustomerListModel>
)

@Parcelize
data class CustomerListModel(
    val Account: AccountTemplate? = null,
    val DeliveryAddress: AddressTemplate? = null,
    val Orders: ArrayList<OrderListObject>? = null,
    val ProductAssortment: ArrayList<ProductAssortment>?= null,
//    val Products: ArrayList<JsonObject>? = null,
//    val Assets: ArrayList<JsonObject>? = null,
//    val Promotions: ArrayList<JsonObject>? = null,
//    val Surveys: ArrayList<JsonObject>? = null,
    val Routes: ArrayList<Route>? = null,
    val Assets: ArrayList<AssetTemplate>? = null,
    ):Parcelable
