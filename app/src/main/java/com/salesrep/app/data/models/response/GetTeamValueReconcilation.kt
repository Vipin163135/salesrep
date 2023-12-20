package com.salesrep.app.data.models.response

data class GetTeamValueReconcilation(
    val Record: PaymentRecord
)

data class PaymentRecord(
    val amount: Double?,
    val currency: String,
    val lov_payment_method: String
)