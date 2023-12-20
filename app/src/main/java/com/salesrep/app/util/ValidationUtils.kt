package com.salesrep.app.util

import android.util.Patterns
import java.util.regex.Pattern

object ValidationUtils {

    private const val SOCIAL_SECURITY_REG_EX = "[0-9]{3}[.][0-9]{3}[.][0-9]{3}[-]\\d{2}"
    private const val VEHICLE_REG_REG_EX = "[A-Z]{3}[-]\\d{4}"

    fun isFieldNullOrEmpty(text: String?): Boolean {
        return text.isNullOrEmpty()
    }

    fun isNameValid(text: String): Boolean {
        return text.trim().length >= 3
    }

    fun isCityValid(text: String): Boolean {
        return text.trim().length >= 3
    }

    fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isPasswordValid(password: String): Boolean {
        return password.length >= 6
    }

    fun isPhoneNumberValid(phoneNumber: String): Boolean {
        if (!Pattern.matches("[a-zA-Z]+", phoneNumber)) {
            return phoneNumber.length in 7..15
        }
        return false
    }

    fun isSocialSecurityNumberValid(text: String): Boolean {
        return text.matches(SOCIAL_SECURITY_REG_EX.toRegex())
    }

    fun isVehicleRegNumberValid(text: String): Boolean {
        return text.length >= 7
    }
}