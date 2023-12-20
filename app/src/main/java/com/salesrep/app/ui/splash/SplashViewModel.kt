package com.salesrep.app.ui.splash

import android.content.Context
import androidx.lifecycle.ViewModel
import com.salesrep.app.data.apis.HostWebService
import com.salesrep.app.data.apis.SignupWebService
import com.salesrep.app.data.apis.WebService
import com.salesrep.app.data.appConfig.AppConfigData
import com.salesrep.app.data.appConfig.AppConfigResponse
import com.salesrep.app.data.models.requests.GeneralRequest
import com.salesrep.app.data.models.requests.GetLocalesRequest
import com.salesrep.app.data.network.responseUtil.ApiResponse
import com.salesrep.app.data.network.responseUtil.ApiUtils
import com.salesrep.app.data.network.responseUtil.Resource
import com.salesrep.app.data.repos.UserRepository
import com.salesrep.app.di.SingleLiveEvent
import com.salesrep.app.util.PrefsManager
import com.salesrep.app.util.PrefsManager.Companion.LOGGED_USER_DATA
import com.salesrep.app.util.PrefsManager.Companion.USER_DATA
import com.salesrep.app.util.isConnectedToInternet
import dagger.hilt.android.lifecycle.HiltViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val webService: WebService,
    private val signupWebService: SignupWebService,
    private val prefsManager: PrefsManager
) :
    ViewModel() {

    val loginRequestCommand = SingleLiveEvent<Boolean>()
    val appRequestCommand = SingleLiveEvent<Boolean>()
    val appConfigResponse by lazy { SingleLiveEvent<Resource<AppConfigData>>() }
    val appLocales by lazy { SingleLiveEvent<Resource<HashMap<String, String>>>() }
    val getHostUrlResponse by lazy { SingleLiveEvent<Resource<ArrayList<String>>>() }

    fun processNextTask() {

        if (userRepository.isUserLoggedIn()) {
//            if (userRepository.getUser()?.id == ProfileStatus.PROFILE_STATUS_PENDING) {
//                prefsManager.remove(USER_DATA)
//                loginRequestCommand.postValue(true)
//            } else {
                appRequestCommand.postValue(true)
//            }
        } else {
            prefsManager.remove(USER_DATA)
            prefsManager.remove(LOGGED_USER_DATA)
            loginRequestCommand.postValue(true)
        }
    }


    /* Apis Calling */
    fun appConfig(context: Context) {
        if (isConnectedToInternet(context, true)) {
            appConfigResponse.value = Resource.loading()

            val params= "{\"webappname\":\"tradicional\"}"

            webService.appConfig(WebService.METHOD_GET_APP_CONFIG,params)
                    .enqueue(object : Callback<AppConfigResponse> {
                        override fun onResponse(
                            call: Call<AppConfigResponse>,
                            response: Response<AppConfigResponse>
                        ) {
                            if (response.isSuccessful) {
                                appConfigResponse.value = Resource.success(response.body()?.data)
                            } else {
                                appConfigResponse.value = Resource.error(
                                        ApiUtils.getError(
                                                response.code(),
                                                response.errorBody()?.string()
                                        )
                                )
                            }
                        }

                        override fun onFailure(
                            call: Call<AppConfigResponse>,
                            throwable: Throwable
                        ) {
                            appConfigResponse.value = Resource.error(ApiUtils.failure(throwable))
                        }
                    })
        }
    }

    fun appLocales(context: Context,code: String) {
        if (isConnectedToInternet(context, true)) {
            appLocales.value = Resource.loading()

            val apiRequest = GeneralRequest(
                method = WebService.METHOD_GET_APP_LOCALES,
                service = WebService.CPORTAL_SERVICE,
                parameter = GetLocalesRequest(
                    lang = code,
                    platform = "android"
                )
            )
            signupWebService.appLocales(apiRequest)
                    .enqueue(object : Callback<ApiResponse<HashMap<String,String>>> {
                        override fun onResponse(
                                call: Call<ApiResponse<HashMap<String,String>>>,
                                response: Response<ApiResponse<HashMap<String,String>>>
                        ) {
                            if (response.isSuccessful) {
                                appLocales.value = Resource.success(response.body()?.returnData?.data)
                            } else {
                                appLocales.value = Resource.error(
                                        ApiUtils.getError(
                                                response.code(),
                                                response.errorBody()?.string()
                                        )
                                )
                            }
                        }

                        override fun onFailure(
                                call: Call<ApiResponse<HashMap<String,String>>>,
                                throwable: Throwable
                        ) {
                            appLocales.value = Resource.error(ApiUtils.failure(throwable))
                        }
                    })
        }
    }

}