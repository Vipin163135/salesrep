package com.salesrep.app.ui.customer

import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.salesrep.app.base.BaseViewModel
import com.salesrep.app.data.apis.HostWebService
import com.salesrep.app.data.apis.SignupWebService
import com.salesrep.app.data.apis.WebService
import com.salesrep.app.data.models.requests.*
import com.salesrep.app.data.models.response.*
import com.salesrep.app.data.network.responseUtil.ApiGeneralResponse
import com.salesrep.app.data.network.responseUtil.ApiResponse
import com.salesrep.app.data.network.responseUtil.ApiUtils
import com.salesrep.app.data.network.responseUtil.Resource
import com.salesrep.app.data.repos.UserRepository
import com.salesrep.app.di.SingleLiveEvent
import com.salesrep.app.util.PrefsManager
import com.salesrep.app.util.isConnectedToInternet
import dagger.hilt.android.lifecycle.HiltViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class CustomerViewModel @Inject constructor(
    val webService: WebService,
    val userRepository: UserRepository,
    val prefsManager: PrefsManager
) : BaseViewModel() {

    val getTeamAccountsResponse by lazy { SingleLiveEvent<Resource<GetCustomerListResponse>>() }
    val createAccountsResponse by lazy { SingleLiveEvent<Resource<GeneralResponse>>() }
    val formCatalogApiResponse  by lazy { SingleLiveEvent<Resource<GetFormCatalogResponse>>() }


    fun getCustomerListApi(context: Context,  page: Int, count: Int,strSearch:String?= "") {
        if (isConnectedToInternet(context, true)) {
            getTeamAccountsResponse.value = Resource.loading()
            val teamId= userRepository.getTeam()?.Team?.id

            val apiRequest = GeneralRequest(
                method = WebService.METHOD_GET_TEAM_ACCOUNTS,
                service = WebService.SALES_APP_SERVICE,
                parameter = GetTeamRouteRequest(
                    teamId!!
                )
            )

            webService.getCustomerList(page,strSearch?: "",count,apiRequest)
                .enqueue(object : Callback<ApiResponse<GetCustomerListResponse>> {
                    override fun onResponse(
                        call: Call<ApiResponse<GetCustomerListResponse>>,
                        response: Response<ApiResponse<GetCustomerListResponse>>
                    ) {
                        if (response.isSuccessful) {
                            if (response.body()?.appservice?.type== "success") {
                                getTeamAccountsResponse.value = Resource.success(response.body()?.returnData?.data)
                            }
                            else
                                getTeamAccountsResponse.value = Resource.error(
                                    ApiUtils.getError(
                                        -1,
                                        Gson().toJson(response.body()?.appservice)
                                    )
                                )
                        } else {
                            getTeamAccountsResponse.value = Resource.error(
                                ApiUtils.getError(
                                    response.code(),
                                    Gson().toJson(response.errorBody())
                                )
                            )
                        }
                    }
                    override fun onFailure(
                        call: Call<ApiResponse<GetCustomerListResponse>>,
                        throwable: Throwable
                    ) {
                        getTeamAccountsResponse.value = Resource.error(ApiUtils.failure(throwable))
                    }
                })
        }
    }

    fun getFormCatalogApi(context: Context) {
        if (isConnectedToInternet(context, true)) {
            formCatalogApiResponse.value = Resource.loading()
            var language = prefsManager.getString(PrefsManager.USER_LANGUAGE,"en")?:"en"

            val apiRequest = GeneralRequest(
                method = WebService.METHOD_FORM_CATALOG,
                service = WebService.SALES_APP_SERVICE,
                parameter = FormCatalogRequest(
                    lang = language.removeSurrounding("\"")
                )
            )

            webService.formCatalogApi(apiRequest)
                .enqueue(object : Callback<ApiResponse<GetFormCatalogResponse>> {
                    override fun onResponse(
                        call: Call<ApiResponse<GetFormCatalogResponse>>,
                        response: Response<ApiResponse<GetFormCatalogResponse>>
                    ) {
                        if (response.isSuccessful) {
                            if (response.body()?.appservice?.type== "success") {
                                formCatalogApiResponse.value = Resource.success(response.body()?.returnData?.data)
                            }
                            else
                                formCatalogApiResponse.value = Resource.error(
                                    ApiUtils.getError(
                                        -1,
                                        Gson().toJson(response.body()?.appservice)
                                    )
                                )
                        } else {
                            formCatalogApiResponse.value = Resource.error(
                                ApiUtils.getError(
                                    response.code(),
                                    Gson().toJson(response.errorBody())
                                )
                            )
                        }
                    }
                    override fun onFailure(
                        call: Call<ApiResponse<GetFormCatalogResponse>>,
                        throwable: Throwable
                    ) {
                        formCatalogApiResponse.value = Resource.error(ApiUtils.failure(throwable))
                    }
                })
        }
    }



    fun createCustomerApi(context: Context, request: AddBulkCustomerRequest) {
        if (isConnectedToInternet(context, true)) {
            createAccountsResponse.value = Resource.loading()

            val apiRequest = GeneralRequest(
                method = WebService.METHOD_BULK_CREATE_ACCOUNTS,
                service = WebService.SALES_APP_SERVICE,
                parameter = request
            )

            webService.createBulkAccountApi(apiRequest)
                .enqueue(object : Callback<ApiGeneralResponse<GeneralResponse>> {
                    override fun onResponse(
                        call: Call<ApiGeneralResponse<GeneralResponse>>,
                        response: Response<ApiGeneralResponse<GeneralResponse>>
                    ) {
                        if (response.isSuccessful) {
                            if (response.body()?.appservice?.type== "success") {
                                createAccountsResponse.value = Resource.success(response.body()?.returnData)
                            }
                            else
                                createAccountsResponse.value = Resource.error(
                                    ApiUtils.getError(
                                        -1,
                                        Gson().toJson(response.body()?.appservice)
                                    )
                                )
                        } else {
                            createAccountsResponse.value = Resource.error(
                                ApiUtils.getError(
                                    response.code(),
                                    Gson().toJson(response.errorBody())
                                )
                            )
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiGeneralResponse<GeneralResponse>>,
                        throwable: Throwable
                    ) {
                        getTeamAccountsResponse.value = Resource.error(ApiUtils.failure(throwable))
                    }
                })
        }
    }



}