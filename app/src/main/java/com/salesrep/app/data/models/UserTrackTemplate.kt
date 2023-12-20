package com.salesrep.app.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.parcel.Parcelize


@Entity(tableName = "userTrack")
data class UserTrackTemplate(
    val trackLocations: List<TrackTemplate>,
    @PrimaryKey val id: Int?= null
)

data class TrackTemplate(
    val latLng: LatLng,
    val time: String
)
