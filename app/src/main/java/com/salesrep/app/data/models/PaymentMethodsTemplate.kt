package com.salesrep.app.data.models


data class PaymentMethodsTemplate(
    val fields: ArrayList<FieldItem>,
    val key: String,
    val icon: String,
    val value: String
)

data class FieldItem(
    val format: String?="",
    val key: String,
    val max: Int,
    val min: Int,
    val options: ArrayList<FieldOption>,
    val type: String,
    val value: String,
    var setValue: String?= ""
)

data class FieldOption(
    val icon: String,
    val key: String,
    val value: String,
    var isSelected: Boolean?
)