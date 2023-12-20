package com.salesrep.app.data.models.inventory

data class StockProductData(

    val id: Int? = 0,
    val integration_num: String? = "0",
    val name: String?,
    val title: String?,
    val lov_product_uom: String?,
    var total: String?=null,
    var qtyList: ArrayList<StockProductQtyData>?= null
    )

data class StockProductQtyData(
    var qty: String?= null,
    var title: String?= null
    )
