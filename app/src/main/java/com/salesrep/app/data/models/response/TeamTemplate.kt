package com.salesrep.app.data.models.response

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TeamTemplate(
    val Team: Team,
    val AvailableManualTasks: ArrayList<AvailableManualTaskTemplate>,
): Parcelable {
    override fun toString(): String {
        return this.Team.title // What to display in the Spinner list.
    }
}


@Parcelize
data class AvailableManualTaskTemplate(
    val ActivityplanTemplate: ActivityplanTemplate
): Parcelable {
    override fun toString(): String {
        return this.ActivityplanTemplate.title // What to display in the Spinner list.
    }
}

@Parcelize
data class ActivityplanTemplate(
    val title: String,
    val lov_activityplantemp_type: String,
): Parcelable {
    override fun toString(): String {
        return this.title // What to display in the Spinner list.
    }
}