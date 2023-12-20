package com.salesrep.app.data.appConfig

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
 data class AppConfig (

    var module_id:Int? = 0,
    var pricelist_id:Int? = 0,
    var currency: String? = null,
    var style: String? = null,
    var appLogo: String? = null,
    var contact_phone: String? = null,
    var contact_email: String? = null,
    var defaultlang: String? = null
): Parcelable