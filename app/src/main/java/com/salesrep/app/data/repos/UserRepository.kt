package com.salesrep.app.data.repos

import androidx.lifecycle.MutableLiveData
import com.salesrep.app.App
import com.salesrep.app.data.apis.WebService
import com.salesrep.app.data.appConfig.Crmapp
import com.salesrep.app.data.models.response.EmployeeTemplate
import com.salesrep.app.data.models.response.LoggedUser
import com.salesrep.app.data.models.response.TeamTemplate

//import com.customnative.data.models.response.AppSettingsModel
import com.salesrep.app.util.PrefsManager
import com.salesrep.app.util.PrefsManager.Companion.APP_CONFIG
import com.salesrep.app.util.PrefsManager.Companion.LOGGED_USER_DATA
import com.salesrep.app.util.PrefsManager.Companion.SECRET_PASSWORD_KEY
import com.salesrep.app.util.PrefsManager.Companion.TEAM_DATA
import com.salesrep.app.util.PrefsManager.Companion.USER_DATA
import com.salesrep.app.util.PrefsManager.Companion.USER_RESPONSE_DATA
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by VIPIN.
 */
@Singleton
class UserRepository @Inject constructor(
    private val prefsManager: PrefsManager) {
    val pushData = MutableLiveData<String>()
    val userData = MutableLiveData<String>()

    fun isUserLoggedIn(): Boolean {
        return getTeam()?.Team?.id != null
    }

    fun getUser(): EmployeeTemplate? {
        return prefsManager.getObject(USER_DATA, EmployeeTemplate::class.java)
    }


    fun getTeam(): TeamTemplate? {
        return prefsManager.getObject(TEAM_DATA, TeamTemplate::class.java)
    }


//    fun getUserResponseData(): UserResponseData? {
//        return prefsManager.getObject(USER_RESPONSE_DATA, UserResponseData::class.java)
//    }

    fun getLoggedUser(): LoggedUser? {
        return prefsManager.getObject(LOGGED_USER_DATA, LoggedUser::class.java)
    }
//
//    fun getUserCart(): String? {
//        return prefsManager.getObject(USER_CART_DATA, String::class.java)
//    }

    fun getAppConfig(): Crmapp? {
        return prefsManager.getObject(APP_CONFIG, Crmapp::class.java)
    }

    fun logoutUser() {
        prefsManager.remove(USER_DATA)
        prefsManager.remove(USER_RESPONSE_DATA)
        prefsManager.remove(LOGGED_USER_DATA)
    }

    fun getSavedPassword(): String {
        return prefsManager.getString(SECRET_PASSWORD_KEY, "") ?: ""
    }

    fun pushTokenUpdate(token: String) {
        val hashMap = HashMap<String, String>()
        hashMap["deviceToken"] = token

//        webService.updatePushToken(hashMap)
//            .enqueue(object : Callback<ApiResponse<Any>> {
//
//                override fun onResponse(
//                    call: Call<ApiResponse<Any>>,
//                    response: Response<ApiResponse<Any>>
//                ) {
//                    if (response.isSuccessful) {
//                        Timber.e("fcmToken" + "Success")
//                    } else {
//                        Timber.e("fcmToken" + "Faliure")
//                    }
//                }
//
//                override fun onFailure(call: Call<ApiResponse<Any>>, throwable: Throwable) {
//                    Timber.e("fcmToken" + "faliue 500")
//                }
//            })
    }
}

