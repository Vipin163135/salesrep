package com.salesrep.app.data.models

import android.os.Parcelable
import com.salesrep.app.data.models.response.PaymentProfileTemplate
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Paymentprofiles(

    var type: Int=1,
    var title: String = "",
    var isSelected: Boolean = false,
    val Paymentprofile: PaymentProfileTemplate?= null
):Parcelable

//
//@Parcelize
//data class Paymentprofile(
//    val account_id: Int?,
//    val application_id: String?,
//    val cardCCV: Int?,
//    val cardExpirationMonth: String?,
//    val cardExpirationYear: String?,
//    val cardHolder: String,
//    val cardNumber: String,
//    val cardToken: Long,
//    val cc_type: String,
//    val cf_tsw_ContractID: String?,
//    val cf_tsw_PersonCreditCardID: String?,
//    val contact_id: String?,
//    val contract_id: String?,
//    val created: String,
//    val created_by: Int?,
//    val created_by_position: String?,
//    val currency: String,
//    val gateway_file: String?,
//    val gateway_status: String?,
//    val human_id: String?,
//    val icon: String,
//    val id: Int?,
//    val lov_card_type: String,
//    val lov_payment_bank_name: String?,
//    val lov_paymentprofile_gateway: String,
//    val lov_paymentprofile_status: String,
//    val name: Int?,
//    val organization_id: String?,
//    val updated: String?,
//    val updated_by: Int?
//):Parcelable