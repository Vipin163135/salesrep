package com.salesrep.app.data.models.response

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PaymentProfileResponse(
    var type: Int?=1,
    var title: String? = "",
    var isSelected: Boolean? = false,
    var Paymentprofile: PaymentProfileTemplate? = null
):Parcelable


@Parcelize
data class PaymentProfileTemplate(
    val account_id: Int?,
    val application_id: Int?,
    val cardCCV: Int?,
    val cardExpirationMonth: String?,
    val cardExpirationYear: String?,
    val cardHolder: String?,
    val cardNumber: String?,
    val cardToken: String?,
    val cc_type: String?,
//    val cf_tsw_ContractID: Any,
//    val cf_tsw_PersonCreditCardID: Any,
//    val contact_id: Any,
//    val contract_id: Any,
    val created: String?,
    val created_by: Int?,
    val created_by_position: Int?,
    val currency: String?,
//    val gateway_file: Any,
//    val gateway_status: Any,
//    val human_id: Any,
    val icon: String?,
    val id: Int?,
    val lov_card_type: String?,
    val lov_payment_bank_name: String?,
    val lov_paymentprofile_gateway: String?,
    val lov_paymentprofile_status: String?,
    val name: String?,
    val organization_id: Int?,
    val updated: String?,
    val updated_by: Int?
):Parcelable