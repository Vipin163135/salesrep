package com.salesrep.app.data.models.inventory

import android.os.Parcelable
import com.salesrep.app.data.models.ProductTemplate
import kotlinx.android.parcel.Parcelize

@Parcelize
class InvMovementProductData(
    val Product: ProductTemplate ?= null,
    val InvmovementProduct : InvBinProductsData?= null,
    val Manufacturer: ManufacturerTemplate?= null
):Parcelable
