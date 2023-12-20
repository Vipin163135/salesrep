package com.salesrep.app.data.models.response

import android.os.Parcelable
import com.salesrep.app.data.models.ProductTemplate
import com.salesrep.app.data.models.inventory.ManufacturerTemplate
import kotlinx.android.parcel.Parcelize


@Parcelize
data class GetTeamProductsResponse (
    var Product : ProductTemplate?= null,
    var Manufacturer : ManufacturerTemplate?= null,
):Parcelable