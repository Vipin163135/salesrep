package com.salesrep.app.data.apis

import com.salesrep.app.data.network.responseUtil.ApiResponse
import com.salesrep.app.data.models.GetValidHostRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface HostWebService {

    companion object {
        private const val APP_SERVICES = "api/appservices?method=executeAppService"
    }

    @POST(APP_SERVICES)
    fun getValidHost(@Body() request: GetValidHostRequest):  Call<ApiResponse<ArrayList<String>>>

}