package com.salesrep.app.data.models.response

data class LoginApiResponse(
    val Employee: EmployeeTemplate?,
    val LoggedUser: LoggedUser?,
    val SessionId: String,
    val Teams: ArrayList<TeamTemplate>?,
    val passwordPolicy: PasswordPolicy,
    val passwordErrors: ArrayList<String>

    )