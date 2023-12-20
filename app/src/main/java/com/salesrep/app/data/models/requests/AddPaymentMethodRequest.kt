package com.salesrep.app.data.models.requests

data class AddPaymentProfileRequest(
    var profiles: ArrayList<AddPaymentMethodRequest>
)
data class AddPaymentMethodRequest(
    val account_id: Int,
    val data: String,
    val type: String
)