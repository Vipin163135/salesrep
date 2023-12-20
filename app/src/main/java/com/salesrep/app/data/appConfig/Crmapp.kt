package com.salesrep.app.data.appConfig

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Crmapp (
    var id:Int?  = 0,
    var created: String? = null,
    var updated: String? = null,
    var created_by:Int? = 0,
    var updated_by:Int? = 0,
    var name: String? = null,
    var title: String? = null,
    var url: String? = null,
    var description: String? = null,
    var properties: String? = null,
    var active_flg:Boolean? = false,
    var login_terms: String? = null,
    var login_legal_text: String? = null,
    var site_terms: String? = null,
    var privacy_terms: String? = null,
    var faqs: String? = null,
    var header_text: String? = null,
    var footer_text: String? = null,
    var loyaltyprogram_id:Int? = 0,
    var group_id:Int? = 0,
    var pricelist_id:Int? = 0,
    var currency: String? = null,
    var module_id:Int? =  0,
    var style: String? = null,
    var contact_phone: String? = null,
    var contact_email: String? = null,
    var whatsapp: String? = null,
    var logo: String? = null,
    var credit_limit_flg:Boolean? = false,
    var gtagid: String? = null,
    var zendeskid: String? = null,
    var show_distfee_flg:Boolean? = false,
    var show_retention_flg:Boolean? = false,
    var gmapsid:String?= null,
    var signup_flg:Boolean?= true

): Parcelable