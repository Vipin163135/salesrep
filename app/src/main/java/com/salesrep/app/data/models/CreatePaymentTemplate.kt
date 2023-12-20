package com.salesrep.app.data.models

import android.os.Parcelable
import androidx.databinding.adapters.AdapterViewBindingAdapter
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
data class OrderPaymentTemplate(
    val Payment: CreatePaymentTemplate?=null
) : Parcelable

@Entity(tableName = "createPayment")
@Parcelize
data class CreatePaymentTemplate(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    var account_id: Int? = null,
    var amount: Double? = null,
    var order_id: Int? = null,
    var order_integration_id: String? = null,
    var integration_id: String? = null,
    var payment_data: String? = null,
    var payment_id: Int? = null,
    var payment_type: String? = null,
    var paymentprofile_id: Int? = null,
    var lov_payment_status: String? = null,
    var lov_payment_method: String? = null,
    var lov_payment_type: String? = null,
    var isSelected: Boolean? = false,
    var allocations: OrderAllocations? = null
) : Parcelable

@Parcelize
class OrderAllocations(
    val amount: Double,
    val order_id: Int
) : Parcelable
