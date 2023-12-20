package com.salesrep.app.data.models.response

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.salesrep.app.data.models.AddressTemplate
import com.salesrep.app.data.models.RouteTemplate
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "routes")
data class GetHomeRoutesResponse(
    val AllRoutes: ArrayList<Route>,
    val TodayRoutes: ArrayList<Route>,
    @PrimaryKey(autoGenerate = true)
    var id: Long?=null
):Parcelable

@Parcelize
data class Route(
//    val Accounts: List<Any>,
    val Address: AddressTemplate?=null,
    val EndAddress: AddressTemplate?=null,
    val StartAddress: AddressTemplate?=null,
    val Route: RouteTemplate?=null,
    var Accounts: ArrayList<GetRouteAccountResponse>?=null,
):Parcelable