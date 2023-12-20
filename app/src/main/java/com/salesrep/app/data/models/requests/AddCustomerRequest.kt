package com.salesrep.app.data.models.requests

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize


data class AddBulkCustomerRequest(
    @SerializedName("accounts")
    val accounts: ArrayList<AddCustomerRequest>?= null
)


data class AddCustomerRequest(
    @SerializedName("Account")
    val Account: AddCustomerData?=null,
    @SerializedName("DeliveryAddress")
    val DeliveryAddress: AddAddressData?=null,
    @SerializedName("BillingAddress")
    val BillingAddress: AddAddressData?=null
)

data class AddCustomerData(
    @SerializedName("accountname")
    val accountname: String?=null,
    @SerializedName("id")
    val id: Int?=null,
    @SerializedName("email")
    val email: String?=null,
    @SerializedName("mobile_phone")
    val mobile_phone: String?=null,
    @SerializedName("phone")
    val phone: String?=null,
    @SerializedName("mobilephone")
    val mobilephone: String?=null,
    @SerializedName("tax_number")
    val tax_number: String?=null,
    @SerializedName("pricelist_id")
    val pricelist_id: String?=null,
    @SerializedName("preferred_visit_day")
    val preferred_visit_day: String?=null,
    @SerializedName("preferred_visit_time")
    val preferred_visit_time: String?=null,
    @SerializedName("team_id")
    val team_id: Int?=null

)

@Parcelize
data class AddAddressData(
    @SerializedName("country")
    val country: String?,
    @SerializedName("street")
    val street: String?,
    @SerializedName("suburb")
    val suburb: String?,
    @SerializedName("city")
    val city: String?,
    @SerializedName("state")
    val state: String?,
    @SerializedName("latitude")
    val latitude: Double?,
    @SerializedName("longitude")
    val longitude: Double?,
    @SerializedName("zip")
    val zip: String?,
    @SerializedName("street_no")
    val street_no: String?,
):Parcelable


