package com.salesrep.app.data.models.requests

data class RevertPaymentRequest(
    val payments: List<RevertPaymentData>
)

data class RevertPaymentData(
    val integration_id: String?,
    val lov_payment_cancelreason: String?
)