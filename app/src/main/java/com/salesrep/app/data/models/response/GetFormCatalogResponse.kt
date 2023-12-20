package com.salesrep.app.data.models.response

data class GetFormCatalogResponse(
    val ActivityReasons: List<RouteStatusReasonsModel>,
    val ActivityStatuses: List<StatusModel>,
    val Countries: List<Country>,
    val RouteStatusReasons: List<RouteStatusReasonsModel>,
    val RouteStatuses: List<StatusModel>,
    val VisitDays: List<StatusModel>,
    val ProductLocations: List<StatusModel>,
    val PaymentCancellationReasons: List<RouteStatusReasonsModel>,
    val OrderStatuses: List<StatusModel>,
    val ProductStatuses: List<StatusModel>,
    val OrderReturnReasons: List<RouteStatusReasonsModel>
)

data class StatusModel(
    val code: String,
    val value: String
)
data class RouteStatusReasonsModel(
    val code: String,
    val value: String,
    var isSelected: Boolean?= false
)


data class Country(
    val States: List<State>,
    val code: String,
    val phonecode: Int,
    val value: String
)

data class State(
    val code: String,
    val value: String
)