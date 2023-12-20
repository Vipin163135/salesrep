package com.salesrep.app.data.apis

import com.salesrep.app.data.models.requests.GeneralRequest
import com.salesrep.app.data.models.requests.GetLocalesRequest
import com.salesrep.app.data.models.requests.ResetPasswordRequest
import com.salesrep.app.data.models.requests.SetPasswordRequest
import com.salesrep.app.data.models.response.LoginApiResponse
import com.salesrep.app.data.models.response.SignupResponseData
import com.salesrep.app.data.network.responseUtil.ApiGeneralResponse
import com.salesrep.app.data.network.responseUtil.ApiResponse
import retrofit2.Call
import retrofit2.http.*

interface SignupWebService {

    companion object {
        private const val APP_SERVICES = "api/appservices?method=executeAppService"
    }

//    @POST(APP_SERVICES)
//    fun signup(@Body() request: SignupRequest): Call<SignupResponse>


    @POST(APP_SERVICES)
    fun setPassword(@Body() request: SetPasswordRequest): Call<ApiGeneralResponse<LoginApiResponse>>

    @POST(APP_SERVICES)
    fun resetPassword(@Body() request: ResetPasswordRequest): Call<ApiGeneralResponse<SignupResponseData>>

    @POST(APP_SERVICES)
    fun appLocales(@Body() request: GeneralRequest<GetLocalesRequest>): Call<ApiResponse<HashMap<String, String>>>

}