package com.salesrep.app.ui.host

import android.content.Context
import android.util.Base64
import androidx.lifecycle.ViewModel
import com.salesrep.app.data.apis.HostWebService
import com.salesrep.app.data.apis.WebService
import com.salesrep.app.data.models.GetValidHostRequest
import com.salesrep.app.data.network.responseUtil.ApiResponse
import com.salesrep.app.data.network.responseUtil.ApiUtils
import com.salesrep.app.data.network.responseUtil.Resource
import com.salesrep.app.di.SingleLiveEvent
import com.salesrep.app.util.BASIC_AUTH_PASSWORD
import com.salesrep.app.util.BASIC_AUTH_USERNAME
import com.salesrep.app.util.isConnectedToInternet
import dagger.hilt.android.lifecycle.HiltViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HostViewModel @Inject constructor(
    private val hostWebService: HostWebService
    ) : ViewModel() {

    val getHostUrlResponse by lazy { SingleLiveEvent<Resource<ArrayList<String>>>() }

    /* Apis Calling */
    fun getHostUrlsApi(context: Context) {
        if (isConnectedToInternet(context, true)) {
            getHostUrlResponse.value = Resource.loading()

            val credentials: String = "$BASIC_AUTH_USERNAME:$BASIC_AUTH_PASSWORD"
            // create Base64 encodet string
            val basic = "Basic " + Base64.encodeToString(
                credentials.encodeToByteArray(),
                Base64.NO_WRAP
            )

            Timber.d("Authorization : $basic")

            val request= GetValidHostRequest(
                method = WebService.METHOD_GET_VALID_HOSTS,
                service = WebService.CPORTAL_SERVICE
            )

            hostWebService.getValidHost(request)
                .enqueue(object : Callback<ApiResponse<ArrayList<String>>> {
                    override fun onResponse(
                        call: Call<ApiResponse<ArrayList<String>>>,
                        response: Response<ApiResponse<ArrayList<String>>>
                    ) {
                        if (response.isSuccessful) {
                            getHostUrlResponse.value = Resource.success(response.body()?.returnData?.data)
                        } else {
                            getHostUrlResponse.value = Resource.error(
                                ApiUtils.getError(
                                    response.code(),
                                    response.errorBody()?.string()
                                )
                            )
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse<ArrayList<String>>>,
                        throwable: Throwable
                    ) {
                        getHostUrlResponse.value = Resource.error(ApiUtils.failure(throwable))
                    }
                })
        }
    }

}