package com.salesrep.app.data.models.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.salesrep.app.data.models.NotificationData

data class GetNotificationListResponse (
    @SerializedName("Notification")
    @Expose
    val Notification: NotificationData
)