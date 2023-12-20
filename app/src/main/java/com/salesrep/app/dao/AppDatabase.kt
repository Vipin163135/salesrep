package com.salesrep.app.dao

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.salesrep.app.converters.ListConverters
import com.salesrep.app.converters.ObjectConverters
import com.salesrep.app.data.models.SaveOrderTemplate
import com.salesrep.app.data.models.UserTrackTemplate
import com.salesrep.app.data.models.inventory.GetTeamInventoryResponse
import com.salesrep.app.data.models.inventory.MovementCancelData
import com.salesrep.app.data.models.inventory.MovementListData
import com.salesrep.app.data.models.requests.CreateOrderTemplate
import com.salesrep.app.data.models.requests.UpdateRoute
import com.salesrep.app.data.models.requests.UpdateRouteActivity
import com.salesrep.app.data.models.requests.UpdateSurvey
import com.salesrep.app.data.models.response.GetHomeRoutesResponse
import com.salesrep.app.data.models.response.GetRouteAccountResponse


@Database(
    entities = [UserTrackTemplate::class,
        GetHomeRoutesResponse::class,
        CreateOrderTemplate::class,
        UpdateRoute::class,
        UpdateSurvey::class,
        UpdateRouteActivity::class,
        GetRouteAccountResponse::class,
        GetTeamInventoryResponse::class,
        MovementListData::class,
        MovementCancelData::class,
        SaveOrderTemplate::class],
    version =1,
    exportSchema = false
)

@TypeConverters(ObjectConverters::class, ListConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getRouteActivityDao(): RouteActivityDao

    abstract fun getRoutesDao(): RoutesDao

    abstract fun getUpdateRoutesDao(): UpdateRouteDao

    abstract fun getUpdateSurveyDao(): UpdateSurveyDao

    abstract fun getUpdateActivity(): UpdateActivityDao

    abstract fun userTrackDao(): UserTrackDao

    abstract fun createOrderDao(): CreateOrderDao

    abstract fun saveOrderDao(): SaveOrderDao

    abstract fun inventoryDao(): InventoryDao

    abstract fun createMovementDao(): CreateMovementDao

    abstract fun changeMovementStatusDao(): ChangeStatusMovementDao
}