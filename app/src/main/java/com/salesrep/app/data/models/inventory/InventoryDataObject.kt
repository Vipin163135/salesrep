package com.salesrep.app.data.models.inventory

import android.os.Parcelable
import com.salesrep.app.data.models.ProductTemplate
import kotlinx.android.parcel.Parcelize

@Parcelize
class InventoryDataObject(
    val Product: ProductTemplate,
    val InvbinsProduct : InvBinProductsData,
    val Manufacturer : ManufacturerTemplate?=null
):Parcelable