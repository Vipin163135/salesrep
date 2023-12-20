package com.salesrep.app.ui.inventory

import android.content.Context
import com.google.gson.Gson
import com.salesrep.app.base.BaseViewModel
import com.salesrep.app.data.apis.WebService
import com.salesrep.app.data.models.inventory.*
import com.salesrep.app.data.models.requests.GeneralRequest
import com.salesrep.app.data.models.requests.GetTeamRouteRequest
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
class InventoryViewModel  @Inject constructor(
    val webService: WebService,
    val userRepository: UserRepository,
    val prefsManager: PrefsManager,

    ) : BaseViewModel() {

    val getTeamInventoryResponse  by lazy { SingleLiveEvent<Resource<GetTeamInventoryResponse>>() }
    val createMovementResponse  by lazy { SingleLiveEvent<Resource<GeneralResponse>>() }
    val updateMovementResponse  by lazy { SingleLiveEvent<Resource<GeneralResponse>>() }
    val commitMovementResponse  by lazy { SingleLiveEvent<Resource<GeneralResponse>>() }
    val cancelMovementResponse  by lazy { SingleLiveEvent<Resource<GeneralResponse>>() }
    val prepareMovementResponse  by lazy { SingleLiveEvent<Resource<InvMovementTemplate>>() }
    val getTeamValueReconListResponse by lazy { SingleLiveEvent<Resource<ArrayList<GetTeamValueReconcilation>>>() }
    val getPagedTeamDocuments by lazy { SingleLiveEvent<Resource<GetDocumentListResponse>>() }


    fun getTeamInventoryApi(context: Context) {
        if (isConnectedToInternet(context, true)) {
            getTeamInventoryResponse.value = Resource.loading()
            val teamId= userRepository.getTeam()?.Team?.id
            val apiRequest = GeneralRequest(
                method = WebService.METHOD_GET_TEAM_INVENTORY,
                service = WebService.SALES_APP_SERVICE,
                parameter = GetTeamRouteRequest(
                    teamId!!
                )
            )

            webService.getTeamInventoryApi(apiRequest)
                .enqueue(object : Callback<ApiResponse<GetTeamInventoryResponse>> {
                    override fun onResponse(
                        call: Call<ApiResponse<GetTeamInventoryResponse>>,
                        response: Response<ApiResponse<GetTeamInventoryResponse>>
                    ) {
                        if (response.isSuccessful) {
                            if (response.body()?.appservice?.type == "success")
                                getTeamInventoryResponse.value =
                                    Resource.success(response.body()?.returnData?.data)
                            else
                                getTeamInventoryResponse.value = Resource.error(
                                    ApiUtils.getError(
                                        -1,
                                        Gson().toJson(response.body()?.appservice)
                                    )
                                )
                        } else {
                            getTeamInventoryResponse.value = Resource.error(
                                ApiUtils.getError(
                                    response.code(),
                                    Gson().toJson(response.errorBody())
                                )
                            )
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse<GetTeamInventoryResponse>>,
                        throwable: Throwable
                    ) {
                        getTeamInventoryResponse.value = Resource.error(ApiUtils.failure(throwable))
                    }
                })
        }
    }


    fun createMovementApi(context: Context, movementList: ArrayList<MovementListData>){
        if (isConnectedToInternet(context, true)) {
            createMovementResponse.value = Resource.loading()
            val apiRequest = GeneralRequest(
                method = WebService.METHOD_BULK_CREATE_MOVEMENT,
                service = WebService.SALES_APP_SERVICE,
                parameter = CreateMovementRequest(
                    movementList
                )
            )

            webService.createBulkMovementApi(apiRequest)
                .enqueue(object : Callback<ApiGeneralResponse<GeneralResponse>> {
                    override fun onResponse(
                        call: Call<ApiGeneralResponse<GeneralResponse>>,
                        response: Response<ApiGeneralResponse<GeneralResponse>>
                    ) {
                        if (response.isSuccessful) {
                            if (response.body()?.appservice?.type == "success")
                                createMovementResponse.value =
                                    Resource.success(response.body()?.returnData)
                            else
                                createMovementResponse.value = Resource.error(
                                    ApiUtils.getError(
                                        -1,
                                        Gson().toJson(response.body()?.appservice)
                                    )
                                )
                        } else {
                            createMovementResponse.value = Resource.error(
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
                        createMovementResponse.value = Resource.error(ApiUtils.failure(throwable))
                    }
                })
        }
    }

    fun updateMovementApi(context: Context, movementList: ArrayList<MovementListData>){
        if (isConnectedToInternet(context, true)) {
            updateMovementResponse.value = Resource.loading()
            val apiRequest = GeneralRequest(
                method = WebService.METHOD_BULK_UPDATE_MOVEMENT,
                service = WebService.SALES_APP_SERVICE,
                parameter = CreateMovementRequest(
                    movementList
                )
            )

            webService.createBulkMovementApi(apiRequest)
                .enqueue(object : Callback<ApiGeneralResponse<GeneralResponse>> {
                    override fun onResponse(
                        call: Call<ApiGeneralResponse<GeneralResponse>>,
                        response: Response<ApiGeneralResponse<GeneralResponse>>
                    ) {
                        if (response.isSuccessful) {
                            if (response.body()?.appservice?.type == "success")
                                updateMovementResponse.value =
                                    Resource.success(response.body()?.returnData)
                            else
                                updateMovementResponse.value = Resource.error(
                                    ApiUtils.getError(
                                        -1,
                                        Gson().toJson(response.body()?.appservice)
                                    )
                                )
                        } else {
                            updateMovementResponse.value = Resource.error(
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
                        updateMovementResponse.value = Resource.error(ApiUtils.failure(throwable))
                    }
                })
        }
    }

    fun cancelMovementApi(context: Context, movementList: ArrayList<MovementCancelData>){
        if (isConnectedToInternet(context, true)) {
            cancelMovementResponse.value = Resource.loading()
            val apiRequest = GeneralRequest(
                method = WebService.METHOD_BULK_CANCEL_MOVEMENT,
                service = WebService.SALES_APP_SERVICE,
                parameter = CancelMovementRequest(
                    movementList
                )
            )

            webService.cancelBulkMovementApi(apiRequest)
                .enqueue(object : Callback<ApiGeneralResponse<GeneralResponse>> {
                    override fun onResponse(
                        call: Call<ApiGeneralResponse<GeneralResponse>>,
                        response: Response<ApiGeneralResponse<GeneralResponse>>
                    ) {
                        if (response.isSuccessful) {
                            if (response.body()?.appservice?.type == "success")
                                cancelMovementResponse.value =
                                    Resource.success(response.body()?.returnData)
                            else
                                cancelMovementResponse.value = Resource.error(
                                    ApiUtils.getError(
                                        -1,
                                        Gson().toJson(response.body()?.appservice)
                                    )
                                )
                        } else {
                            cancelMovementResponse.value = Resource.error(
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
                        cancelMovementResponse.value = Resource.error(ApiUtils.failure(throwable))
                    }
                })
        }
    }
    fun commitMovementApi(context: Context, movementList: ArrayList<MovementCancelData>){
        if (isConnectedToInternet(context, true)) {
            commitMovementResponse.value = Resource.loading()
            val apiRequest = GeneralRequest(
                method = WebService.METHOD_BULK_COMMIT_MOVEMENT,
                service = WebService.SALES_APP_SERVICE,
                parameter = CancelMovementRequest(
                    movementList
                )
            )

            webService.cancelBulkMovementApi(apiRequest)
                .enqueue(object : Callback<ApiGeneralResponse<GeneralResponse>> {
                    override fun onResponse(
                        call: Call<ApiGeneralResponse<GeneralResponse>>,
                        response: Response<ApiGeneralResponse<GeneralResponse>>
                    ) {
                        if (response.isSuccessful) {
                            if (response.body()?.appservice?.type == "success")
                                commitMovementResponse.value =
                                    Resource.success(response.body()?.returnData)
                            else
                                commitMovementResponse.value = Resource.error(
                                    ApiUtils.getError(
                                        -1,
                                        Gson().toJson(response.body()?.appservice)
                                    )
                                )
                        } else {
                            commitMovementResponse.value = Resource.error(
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
                        commitMovementResponse.value = Resource.error(ApiUtils.failure(throwable))
                    }
                })
        }
    }

    fun prepareMovementApi(context: Context, movementList: MovementCancelData){
        if (isConnectedToInternet(context, true)) {
            prepareMovementResponse.value = Resource.loading()
            val apiRequest = GeneralRequest(
                method = WebService.METHOD_BULK_PREPARE_MOVEMENT,
                service = WebService.SALES_APP_SERVICE,
                parameter = movementList

            )

            webService.prepareMovementApi(apiRequest)
                .enqueue(object : Callback<ApiResponse<InvMovementTemplate>> {
                    override fun onResponse(
                        call: Call<ApiResponse<InvMovementTemplate>>,
                        response: Response<ApiResponse<InvMovementTemplate>>
                    ) {
                        if (response.isSuccessful) {
                            if (response.body()?.appservice?.type == "success")
                                prepareMovementResponse.value =
                                    Resource.success(response.body()?.returnData?.data)
                            else
                                prepareMovementResponse.value = Resource.error(
                                    ApiUtils.getError(
                                        -1,
                                        Gson().toJson(response.body()?.appservice)
                                    )
                                )
                        } else {
                            prepareMovementResponse.value = Resource.error(
                                ApiUtils.getError(
                                    response.code(),
                                    Gson().toJson(response.errorBody())
                                )
                            )
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse<InvMovementTemplate>>,
                        throwable: Throwable
                    ) {
                        prepareMovementResponse.value = Resource.error(ApiUtils.failure(throwable))
                    }
                })
        }
    }

    fun getTeamValueReconcilationApi(date:String?,context: Context) {
        if (isConnectedToInternet(context, true)) {
            getTeamValueReconListResponse.value = Resource.loading()
            val teamId= userRepository.getTeam()?.Team?.id
            val apiRequest = GeneralRequest(
                method = WebService.METHOD_GET_TEAM_VALUE_RECONCILATION,
                service = WebService.SALES_APP_SERVICE,
                parameter = GetTeamRouteRequest(
                    teamId!!,
                    date
                )
            )

            webService.getTeamValueRecon(apiRequest)
                .enqueue(object : Callback<ApiResponse<ArrayList<GetTeamValueReconcilation>>> {
                    override fun onResponse(
                        call: Call<ApiResponse<ArrayList<GetTeamValueReconcilation>>>,
                        response: Response<ApiResponse<ArrayList<GetTeamValueReconcilation>>>
                    ) {
                        if (response.isSuccessful) {
                            if (response.body()?.appservice?.type == "success")
                                getTeamValueReconListResponse.value =
                                    Resource.success(response.body()?.returnData?.data)
                            else
                                getTeamValueReconListResponse.value = Resource.error(
                                    ApiUtils.getError(
                                        -1,
                                        Gson().toJson(response.body()?.appservice)
                                    )
                                )
                        } else {
                            getTeamValueReconListResponse.value = Resource.error(
                                ApiUtils.getError(
                                    response.code(),
                                    Gson().toJson(response.errorBody())
                                )
                            )
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse<ArrayList<GetTeamValueReconcilation>>>,
                        throwable: Throwable
                    ) {
                        getTeamValueReconListResponse.value = Resource.error(ApiUtils.failure(throwable))
                    }
                })
        }
    }


    fun getDocumentListApi(context: Context,  page: Int, count: Int) {
        if (isConnectedToInternet(context, true)) {
            getPagedTeamDocuments.value = Resource.loading()
            val teamId= userRepository.getTeam()?.Team?.id

            val apiRequest = GeneralRequest(
                method = WebService.METHOD_GET_DOCUMENTS,
                service = WebService.SALES_APP_SERVICE,
                parameter = GetTeamRouteRequest(
                    teamId!!
                )
            )

            webService.getDocumentList(page,count,apiRequest)
                .enqueue(object : Callback<ApiResponse<GetDocumentListResponse>> {
                    override fun onResponse(
                        call: Call<ApiResponse<GetDocumentListResponse>>,
                        response: Response<ApiResponse<GetDocumentListResponse>>
                    ) {
                        if (response.isSuccessful) {
                            if (response.body()?.appservice?.type== "success") {
                                getPagedTeamDocuments.value = Resource.success(response.body()?.returnData?.data)
                            }
                            else
                                getPagedTeamDocuments.value = Resource.error(
                                    ApiUtils.getError(
                                        -1,
                                        Gson().toJson(response.body()?.appservice)
                                    )
                                )
                        } else {
                            getPagedTeamDocuments.value = Resource.error(
                                ApiUtils.getError(
                                    response.code(),
                                    Gson().toJson(response.errorBody())
                                )
                            )
                        }
                    }
                    override fun onFailure(
                        call: Call<ApiResponse<GetDocumentListResponse>>,
                        throwable: Throwable
                    ) {
                        getPagedTeamDocuments.value = Resource.error(ApiUtils.failure(throwable))
                    }
                })
        }
    }


}