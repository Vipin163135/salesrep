package com.salesrep.app.data.models.response

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

data class SignupResponseData(

    val message: String,
    val passwordPolicy: PasswordPolicy,
    val passwordErrors: ArrayList<String>,
    val success: Boolean,
    val type: String

)


@Parcelize
data class PasswordPolicy(
    val DistinctPassword: Boolean,
    val MaxLength: String,
    val MinLength: String,
    val MinLowerCaseLetters: String,
    val MinNumbers: String,
    val MinSymbols: String,
    val MinUpperCaseLetters: String
): Parcelable