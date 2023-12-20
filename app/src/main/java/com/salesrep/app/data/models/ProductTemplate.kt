package com.salesrep.app.data.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ProductTemplate(

    val id: Int? = 0,
    val integration_num: String? = "0",
    val long_desc: String?=null,
    val product_name: String?=null,
    val lov_product_uom: String?=null,
    val lov_product_uom_major: String?=null,
    val lov_product_uom_minor: String?=null,
    val lov_product_location: String?=null,
    val name: String?=null,
    val part_number: String?=null,
    val picture: String?=null,
    val productline_id: Int? = 0,
    val short_desc: String?=null,
    val tags: String?=null,
    val title: String?=null,
    val created: String?=null,
    val extended_price: Double?=0.0,
    val payableinpoints: Boolean?= false,
    val isnew_flg: Boolean?= false,
    var max_qty: Double?=null,
    var min_qty: Double?=null,
    var order_qty: String?="0.0",
    var step_qty: Double?=null,
    var product_qty: Double?=null,
    val truncated_short_desc: String?=null,
    val unit_price: Double?= 0.0,
    val unit_price4: Double?= 0.0,
    val updated: String?=null,
    var lov_invbinproduct_status: String?=null,
    val lov_product_status: String?=null,
    var location: String?= null,
    var qty: String?= null,


//
//
//    "id": 448,
//    "name": "WO-1000448",
//"title": "PALL MALL RED & CLICK 20 s",
//"integration_num": 10126105,
//"lov_product_uom": "CAJ",
//"lov_product_status": "Active"

):Parcelable