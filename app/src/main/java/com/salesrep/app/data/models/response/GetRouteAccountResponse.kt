package com.salesrep.app.data.models.response

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.salesrep.app.data.models.*
import com.salesrep.app.data.models.inventory.InvBinProductsData
import com.salesrep.app.data.models.inventory.ManufacturerTemplate
import com.salesrep.app.data.models.requests.Attachment
import com.salesrep.app.data.models.requests.CreatePaymentRequest
import kotlinx.android.parcel.Parcelize

@Parcelize
class GetPagedRouteAccountResponse(
val pagination: PaginationTemplate?=null,
val rows: ArrayList<GetRouteAccountResponse>?= null
):Parcelable

@Parcelize
@Entity(tableName = "routeActivity")
class GetRouteAccountResponse(
    @PrimaryKey(autoGenerate = true)
    var id: Long?=null,
    var routeId: Int?=null,
    var DeliveryAddress: AddressTemplate?= null,
    var Account : AccountTemplate?= null,
    var Visit : VisitData?= null,
    var Pricelist : PricelistTemplate?= null,
    var ProductAssortment : List<ProductAssortment>?= null,

    var Surveys : List<SurveyData>?= null,
    var Promotions : List<PromotionData>?= null,
    var Paymentprofiles : ArrayList<Paymentprofiles>?= null,
    var PendingPayments : ArrayList<PendingPaymentTemplate>?= null,

    var CartProducts: List<ProductAssortment>?=null,
    var Order: OrderTemplate?=null,
    var Orders: List<OrderListObject>?=null,

):Parcelable

@Parcelize
class OrderListObject (

    var id: Long?=null,
    var routeId: Int?=null,
    var Account : AccountTemplate?= null,
    var ProductAssortment : List<ProductAssortment>?= null,
    var Payments : ArrayList<OrderPaymentTemplate>?= null,

    var lov_order_type: String?=null,
    var tcp_activity_integration_id: String?=null,
    var lov_return_reason: String?=null,

    var Promotions : List<PromotionData>?= null,
    var Paymentprofiles : ArrayList<Paymentprofiles>?= null,
    var PendingPayments : ArrayList<PendingPaymentTemplate>?= null,

    var CartProducts: List<ProductAssortment>?=null,
    var Order: OrderTemplate?=null,
    var Assets: ArrayList<AssetTemplate>?=null
):Parcelable



@Parcelize
class PendingPaymentTemplate(
    var Payment: CreatePaymentTemplate
):Parcelable


@Parcelize
class VisitData(
    var Activity : ActivityTemplate?= null,
    var Tasks : ArrayList<TaskData>?= null
):Parcelable


@Parcelize
class ProductAssortment(
    var AccountProduct : AccountProductTemplate?= null,
    var Product : ProductTemplate?= null,
    var PricelistProduct : AccountProductTemplate?= null,
    var PromoRules: List<PromoRule>?=null,
    var Tmplpromo: Tmplpromo?=null,
    var SourceProducts: List<ProductAssortment>?=null,
    var TargetProducts: List<ProductAssortment>?=null,
    val OrdersProduct: OrdersProduct?=null,
    val Promotions: List<PromotionListObject>?=null,
//    val RedeemablePointproduct: RedeemablePointProduct?=null,
    val Discounts: List<RedeemDiscount>?=null,
    val Manufacturer: ManufacturerTemplate?= null,
    var InvbinsProduct: InvBinProductsData?= null,

):Parcelable


@Parcelize
class TaskData(
    var Activity : ActivityTemplate?= null,
    var Attachments : ArrayList<TaskAttatchment>?= null,
    var ActivityProducts : ArrayList<ActivityProducts>?= null
):Parcelable

@Parcelize
class TaskAttatchment(
    var Attachment : Attachment?= null
):Parcelable

@Parcelize
class ActivityProducts(
    var ActivityProduct : ProductTemplate?= null,
    var Product : ProductTemplate?= null
):Parcelable

