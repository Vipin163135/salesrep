package com.salesrep.app.data.network.responseUtil

import android.app.Activity
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.salesrep.app.R
import com.salesrep.app.util.PrefsManager
import com.salesrep.app.util.logoutUser
import timber.log.Timber

object ApisRespHandler {

    private var alertDialog: AlertDialog.Builder? = null

    fun handleError(error: AppError?, activity: Activity, prefsManager: PrefsManager) {
        error ?: return

        Timber.w(error.toString())

        when (error) {
            is AppError.ApiError -> {
                errorMessage(activity, error.message)
            }

            is AppError.ApiUnauthorized -> {
                sessionExpired(activity, error.message, prefsManager)
            }

            is AppError.ApiInvalidCredentials -> {
                errorMessage(activity, error.message)
            }

            is AppError.ApiAccountBlock -> {
                accountDeleted(activity, error.message, prefsManager)
            }

            is AppError.ApiAccountRuleChanged -> {
                accountDeleted(activity, error.message, prefsManager)
            }

            is AppError.ApiFailure -> {
                errorMessage(activity, error.message)
            }
        }
    }

    private fun sessionExpired(activity: Activity, message: String?, prefsManager: PrefsManager) {
        try {
            if(!activity.isFinishing){
                alertDialog = AlertDialog.Builder(activity)
                alertDialog?.setCancelable(false)
                alertDialog?.setTitle(activity.getString(R.string.alert))
                alertDialog?.setMessage(message)
                alertDialog?.setPositiveButton(activity.getString(R.string.login)) { _, _ ->
                    logoutUser(activity, prefsManager)
                    alertDialog = null
                }
//                alertDialog?.show()

                val dialog = alertDialog?.create()
                dialog?.setOnShowListener { dialogInterface ->
                    dialog?.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ContextCompat
                            .getColor(activity, R.color.colorPrimary))
                    dialog?.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(ContextCompat
                            .getColor(activity, R.color.colorPrimary))
                }
                dialog?.show()
            }
        } catch (ignored: Exception) {}
    }

    private fun accountDeleted(activity: Activity, message: String?, prefsManager: PrefsManager) {
        try {
            if(!activity.isFinishing){
                alertDialog = AlertDialog.Builder(activity)
                alertDialog?.setCancelable(false)
                alertDialog?.setTitle(activity.getString(R.string.alert))
                alertDialog?.setMessage(message)
                alertDialog?.setPositiveButton(activity.getString(R.string.ok)) { _, _ ->
                    logoutUser(activity, prefsManager)
                    alertDialog = null
                }
//                alertDialog?.show()
                val dialog = alertDialog?.create()
                dialog?.setOnShowListener { dialogInterface ->
                    dialog?.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ContextCompat
                            .getColor(activity, R.color.colorPrimary))
                    dialog?.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(ContextCompat
                            .getColor(activity, R.color.colorPrimary))
                }
                dialog?.show()
            }
        } catch (ignored: Exception) {}
    }

    private fun errorMessage(activity: Activity, message: String?) {
        try {
            if (!activity.isFinishing) {
                alertDialog = AlertDialog.Builder(activity)
                alertDialog?.setCancelable(false)
                alertDialog?.setTitle(activity.getString(R.string.alert))
                alertDialog?.setMessage(message)
                alertDialog?.setPositiveButton(activity.getString(R.string.ok)) { _, _ ->
                    alertDialog = null
                }
//                alertDialog?.show()

                val dialog = alertDialog?.create()
                dialog?.setOnShowListener { dialogInterface ->
                    dialog?.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ContextCompat
                            .getColor(activity, R.color.colorPrimary))
                    dialog?.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(ContextCompat
                            .getColor(activity, R.color.colorPrimary))
                }
                dialog?.show()
            }
        } catch (ignored: Exception) {}
    }
}