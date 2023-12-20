package com.salesrep.app.data.models.response

data class LoggedUser(
    val email: String,
    val employee_id: Int,
    val group_id: Int,
    val id: Int,
    val lov_user_status: String,
    val lov_user_type: String,
    val needToSetPassword: Boolean,
    val picture: Any,
    val position_id: Int,
    val time_zone: String,
    val username: String,
    val webportal_flg: Any
)