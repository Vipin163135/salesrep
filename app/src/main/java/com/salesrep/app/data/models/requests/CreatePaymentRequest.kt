package com.salesrep.app.data.models.requests

import android.os.Parcelable
import com.salesrep.app.data.models.CreatePaymentTemplate
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CreatePaymentRequest(
    var payments: ArrayList<CreatePaymentTemplate>
):Parcelable