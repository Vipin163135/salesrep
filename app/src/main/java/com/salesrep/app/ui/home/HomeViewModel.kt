package com.salesrep.app.ui.home

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.salesrep.app.base.BaseViewModel
import com.salesrep.app.dao.AppDatabase
import com.salesrep.app.data.apis.HostWebService
import com.salesrep.app.data.apis.SignupWebService
import com.salesrep.app.data.apis.WebService
import com.salesrep.app.data.models.RouteTemplate
import com.salesrep.app.data.models.inventory.*
import com.salesrep.app.data.models.requests.*
import com.salesrep.app.data.models.response.*
import com.salesrep.app.data.network.Config
import com.salesrep.app.data.network.responseUtil.ApiGeneralResponse
import com.salesrep.app.data.network.responseUtil.ApiResponse
import com.salesrep.app.data.network.responseUtil.ApiUtils
import com.salesrep.app.data.network.responseUtil.Resource
import com.salesrep.app.data.repos.UserRepository
import com.salesrep.app.di.SingleLiveEvent
import com.salesrep.app.util.PrefsManager
import com.salesrep.app.util.isConnectedToInternet
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

typealias todayRoutes= ArrayList<GetRouteAccountResponse>


@HiltViewModel
class HomeViewModel @Inject constructor(
    val webService: WebService,
    val userRepository: UserRepository,
    val prefsManager: PrefsManager,

) : BaseViewModel() {


    val getTeamRoutesResponse by lazy { SingleLiveEvent<Resource<GetHomeRoutesResponse>>() }
    val getTeamProductsResponse by lazy { SingleLiveEvent<Resource<ArrayList<GetTeamProductsResponse>>>() }
    val getTeamPricelistResponse by lazy { SingleLiveEvent<Resource<ArrayList<GetTeamPricelistResponse>>>() }
    val getRouteAccountResponse by lazy { SingleLiveEvent<Resource<GetPagedRouteAccountResponse>>() }
    val getCurrentRouteAccountResponse by lazy { SingleLiveEvent<Resource<ArrayList<GetRouteAccountResponse>>>() }
    val getPolyLineResponse by lazy { SingleLiveEvent<Resource<String>>() }
    val getPolyLineTrackResponse by lazy { SingleLiveEvent<Resource<String>>() }
    val getStartPolyLineResponse by lazy { SingleLiveEvent<Resource<String>>() }
    val formCatalogApiResponse  by lazy { SingleLiveEvent<Resource<GetFormCatalogResponse>>() }
    val updateRouteApiResponse  by lazy { SingleLiveEvent<Resource<GeneralResponse>>() }
    val createActivityApiResponse  by lazy { SingleLiveEvent<Resource<GeneralResponse>>() }
    val updateSurveyApiResponse  by lazy { SingleLiveEvent<Resource<GeneralResponse>>() }
    val createOrderApiResponse  by lazy { SingleLiveEvent<Resource<GeneralResponse>>() }
    val repriceOrderApiResponse  by lazy { SingleLiveEvent<Resource<GetRouteAccountResponse>>() }
    val orderDetailApiResponse  by lazy { SingleLiveEvent<Resource<GetRouteAccountResponse>>() }
    val updateRouteActivityApiResponse  by lazy { SingleLiveEvent<Resource<GeneralResponse>>() }
    val updateUserTrackApiResponse  by lazy { SingleLiveEvent<Resource<GeneralResponse>>() }
    val route= MutableLiveData<Route>()
    val currentRoute= MutableLiveData<ArrayList<GetRouteAccountResponse>>()
    var todayAllRoutes= MutableLiveData<ArrayList<todayRoutes>>()
    val currentRouteDistance= MutableLiveData<Pair<Double,Double>>()
    val isActiveRoute= MutableLiveData<Boolean>()

    val updateAccountsResponse by lazy { SingleLiveEvent<Resource<GeneralResponse>>() }
    val notificationList by lazy { SingleLiveEvent<Resource<ArrayList<GetNotificationListResponse>>>() }

    fun setSelectedRoute(selectedRouts: Route) {
        route.value= selectedRouts
    }

    fun isActiveRoute(isActive: Boolean) {
        isActiveRoute.value= isActive
    }


    fun getTeamRoutesApi(context: Context) {
        if (isConnectedToInternet(context, true)) {
            getTeamRoutesResponse.value = Resource.loading()
            val teamId= userRepository.getTeam()?.Team?.id
            val apiRequest = GeneralRequest(
                method = WebService.METHOD_GET_ROUTES,
                service = WebService.SALES_APP_SERVICE,
                parameter = GetTeamRouteRequest(
                    teamId!!
                )
            )

            webService.getTeamRoutes(apiRequest)
                .enqueue(object : Callback<ApiResponse<GetHomeRoutesResponse>> {
                    override fun onResponse(
                        call: Call<ApiResponse<GetHomeRoutesResponse>>,
                        response: Response<ApiResponse<GetHomeRoutesResponse>>
                    ) {
                        if (response.isSuccessful) {
                            if (response.body()?.appservice?.type == "success")
                                getTeamRoutesResponse.value =
                                    Resource.success(response.body()?.returnData?.data)
                            else
                                getTeamRoutesResponse.value = Resource.error(
                                    ApiUtils.getError(
                                        -1,
                                        Gson().toJson(response.body()?.appservice)
                                    )
                                )
                        } else {
                            getTeamRoutesResponse.value = Resource.error(
                                ApiUtils.getError(
                                    response.code(),
                                    Gson().toJson(response.errorBody())
                                )
                            )
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse<GetHomeRoutesResponse>>,
                        throwable: Throwable
                    ) {
                        getTeamRoutesResponse.value = Resource.error(ApiUtils.failure(throwable))
                    }
                })
        }
    }


    fun getTeamProductsApi(context: Context) {
        if (isConnectedToInternet(context, true)) {
            getTeamProductsResponse.value = Resource.loading()
            val teamId= userRepository.getTeam()?.Team?.id
            val apiRequest = GeneralRequest(
                method = WebService.METHOD_GET_TEAM_PRODUCTS,
                service = WebService.SALES_APP_SERVICE,
                parameter = GetTeamRouteRequest(
                    teamId!!
                )
            )

            webService.getTeamProducts(apiRequest)
                .enqueue(object : Callback<ApiResponse<ArrayList<GetTeamProductsResponse>>> {
                    override fun onResponse(
                        call: Call<ApiResponse<ArrayList<GetTeamProductsResponse>>>,
                        response: Response<ApiResponse<ArrayList<GetTeamProductsResponse>>>
                    ) {
                        if (response.isSuccessful) {
                            if (response.body()?.appservice?.type == "success")
                                getTeamProductsResponse.value =
                                    Resource.success(response.body()?.returnData?.data)
                            else
                                getTeamProductsResponse.value = Resource.error(
                                    ApiUtils.getError(
                                        -1,
                                        Gson().toJson(response.body()?.appservice)
                                    )
                                )
                        } else {
                            getTeamProductsResponse.value = Resource.error(
                                ApiUtils.getError(
                                    response.code(),
                                    Gson().toJson(response.errorBody())
                                )
                            )
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse<ArrayList<GetTeamProductsResponse>>>,
                        throwable: Throwable
                    ) {
                        getTeamProductsResponse.value = Resource.error(ApiUtils.failure(throwable))
                    }
                })
        }
    }

    fun getTeamPricelistApi(context: Context) {
        if (isConnectedToInternet(context, true)) {
            getTeamPricelistResponse.value = Resource.loading()
            val teamId= userRepository.getTeam()?.Team?.id
            val apiRequest = GeneralRequest(
                method = WebService.METHOD_GET_TEAM_PRICELIST,
                service = WebService.SALES_APP_SERVICE,
                parameter = GetTeamRouteRequest(
                    teamId!!
                )
            )

            webService.getTeamPriceList(apiRequest)
                .enqueue(object : Callback<ApiResponse<ArrayList<GetTeamPricelistResponse>>> {
                    override fun onResponse(
                        call: Call<ApiResponse<ArrayList<GetTeamPricelistResponse>>>,
                        response: Response<ApiResponse<ArrayList<GetTeamPricelistResponse>>>
                    ) {
                        if (response.isSuccessful) {
                            if (response.body()?.appservice?.type == "success")
                                getTeamPricelistResponse.value =
                                    Resource.success(response.body()?.returnData?.data)
                            else
                                getTeamPricelistResponse.value = Resource.error(
                                    ApiUtils.getError(
                                        -1,
                                        Gson().toJson(response.body()?.appservice)
                                    )
                                )
                        } else {
                            getTeamPricelistResponse.value = Resource.error(
                                ApiUtils.getError(
                                    response.code(),
                                    Gson().toJson(response.errorBody())
                                )
                            )
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse<ArrayList<GetTeamPricelistResponse>>>,
                        throwable: Throwable
                    ) {
                        getTeamPricelistResponse.value = Resource.error(ApiUtils.failure(throwable))
                    }
                })
        }
    }


    fun getRouteAccountApi(context: Context?, routeId:Int?,page:Int?=1) {
        if (isConnectedToInternet(context, true)) {
            getRouteAccountResponse.value = Resource.loading()

            val apiRequest = GeneralRequest(

                method = WebService.METHOD_GET_PAGED_ACCOUNTS,
                service = WebService.SALES_APP_SERVICE,
                parameter = GetRouteAccountsRequest(
                    routeId!!
                )
            )

            webService.getPagedRouteAccount(page!!,apiRequest)
                .enqueue(object : Callback<ApiResponse<GetPagedRouteAccountResponse>> {
                    override fun onResponse(
                        call: Call<ApiResponse<GetPagedRouteAccountResponse>>,
                        response: Response<ApiResponse<GetPagedRouteAccountResponse>>
                    ) {
                        if (response.isSuccessful) {
                            if (response.body()?.appservice?.type == "success")
                                getRouteAccountResponse.value =
                                    Resource.success(response.body()?.returnData?.data)
                            else
                                getRouteAccountResponse.value = Resource.error(
                                    ApiUtils.getError(
                                        -1,
                                        Gson().toJson(response.body()?.appservice)
                                    )
                                )
                        } else {
                            getRouteAccountResponse.value = Resource.error(
                                ApiUtils.getError(
                                    response.code(),
                                    Gson().toJson(response.errorBody())
                                )
                            )
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse<GetPagedRouteAccountResponse>>,
                        throwable: Throwable
                    ) {
                        getRouteAccountResponse.value = Resource.error(ApiUtils.failure(throwable))
                    }
                })
        }
    }

    fun getCurrentRouteAccountApi(context: Context?, routeId:Int?) {
        if (isConnectedToInternet(context, true)) {
            getCurrentRouteAccountResponse.value = Resource.loading()

            val apiRequest = GeneralRequest(

                method = WebService.METHOD_GET_ROUTE_ACCOUNTS,
                service = WebService.SALES_APP_SERVICE,
                parameter = GetRouteAccountsRequest(
                    routeId!!
                )
            )

            webService.getRouteAccounts(apiRequest)
                .enqueue(object : Callback<ApiResponse<ArrayList<GetRouteAccountResponse>>> {
                    override fun onResponse(
                        call: Call<ApiResponse<ArrayList<GetRouteAccountResponse>>>,
                        response: Response<ApiResponse<ArrayList<GetRouteAccountResponse>>>
                    ) {
                        if (response.isSuccessful) {
                            if (response.body()?.appservice?.type == "success")
                                getCurrentRouteAccountResponse.value =
                                    Resource.success(response.body()?.returnData?.data)
                            else
                                getCurrentRouteAccountResponse.value = Resource.error(
                                    ApiUtils.getError(
                                        -1,
                                        Gson().toJson(response.body()?.appservice)
                                    )
                                )
                        } else {
                            getCurrentRouteAccountResponse.value = Resource.error(
                                ApiUtils.getError(
                                    response.code(),
                                    Gson().toJson(response.errorBody())
                                )
                            )
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse<ArrayList<GetRouteAccountResponse>>>,
                        throwable: Throwable
                    ) {
                        getCurrentRouteAccountResponse.value = Resource.error(ApiUtils.failure(throwable))
                    }
                })
        }
    }

    fun drawPolyLineApi(sourceLat: Double?, sourceLong: Double?, destLat: Double?, destLong: Double?, key: String,wayOuts: String?)
    {
        getPolyLineResponse.value = Resource.loading()

        val retrofit =
            Retrofit.Builder().baseUrl(Config.BASE_URL_GOOGLE_MAPS)
                .addConverterFactory(GsonConverterFactory.create()).build()
        val api = retrofit.create(WebService::class.java)

        val callPolyline = api.getPolYLine(
            key=key,
            "$sourceLat,$sourceLong",
            "$destLat,$destLong",
            Locale.US.language,
            wayOuts?: ""
        )

        callPolyline.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                        val responsePolyline = response.body()?.string()
                        getPolyLineResponse.value = Resource.success(responsePolyline)
                } else {
                    getPolyLineResponse.value = Resource.error(
                        ApiUtils.getError(
                            response.code(),
                            Gson().toJson(response.errorBody())
                        )
                    )
                }
            }

            override fun onFailure(call: Call<ResponseBody>, throwable: Throwable) {
                getPolyLineResponse.value = Resource.error(ApiUtils.failure(throwable))
            }
        })
    }

    fun drawTrackPolyLineApi(sourceLat: Double?, sourceLong: Double?, destLat: Double?, destLong: Double?, key: String,wayOuts: String?) {
        getPolyLineTrackResponse.value = Resource.loading()

        val retrofit =
            Retrofit.Builder().baseUrl(Config.BASE_URL_GOOGLE_MAPS)
                .addConverterFactory(GsonConverterFactory.create()).build()
        val api = retrofit.create(WebService::class.java)

        val callPolyline = api.getPolYLine(
            key=key,
            "$sourceLat,$sourceLong",
            "$destLat,$destLong",
            Locale.US.language,
            wayOuts?: ""
        )

        callPolyline.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                        val responsePolyline = response.body()?.string()
                    getPolyLineTrackResponse.value = Resource.success(responsePolyline)
                } else {
                    getPolyLineTrackResponse.value = Resource.error(
                        ApiUtils.getError(
                            response.code(),
                            Gson().toJson(response.errorBody())
                        )
                    )
                }
            }

            override fun onFailure(call: Call<ResponseBody>, throwable: Throwable) {
                getPolyLineTrackResponse.value = Resource.error(ApiUtils.failure(throwable))
            }
        })
    }

    fun drawStartPolyLineApi(sourceLat: Double?, sourceLong: Double?, destLat: Double?, destLong: Double?, key: String,wayOuts: String?) {
        getStartPolyLineResponse.value = Resource.loading()

        val retrofit =
            Retrofit.Builder().baseUrl(Config.BASE_URL_GOOGLE_MAPS)
                .addConverterFactory(GsonConverterFactory.create()).build()
        val api = retrofit.create(WebService::class.java)

        val callPolyline = api.getPolYLine(
            key=key,
            "$sourceLat,$sourceLong",
            "$destLat,$destLong",
            Locale.US.language,
            wayOuts?: ""
        )

        callPolyline.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                        val responsePolyline = response.body()?.string()
                    getStartPolyLineResponse.value = Resource.success(responsePolyline)
                } else {
                    getStartPolyLineResponse.value = Resource.error(
                        ApiUtils.getError(
                            response.code(),
                            Gson().toJson(response.errorBody())
                        )
                    )
                }
            }
            override fun onFailure(call: Call<ResponseBody>, throwable: Throwable) {
                getStartPolyLineResponse.value = Resource.error(ApiUtils.failure(throwable))
            }
        })
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
                                prefsManager.save(PrefsManager.APP_FORM_CATALOG,response.body()?.returnData?.data)
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

    fun updateRouteApi(context: Context,routes: ArrayList<UpdateRoute>) {
        if (isConnectedToInternet(context, true)) {
            updateRouteApiResponse.value = Resource.loading()

            val apiRequest = GeneralRequest(
                method = WebService.METHOD_BULK_UPDATE_ROUTE,
                service = WebService.SALES_APP_SERVICE,
                parameter = UpdateRouteRequest(
                    routes
                )
            )

            webService.updateRouteApi(apiRequest)
                .enqueue(object : Callback<ApiGeneralResponse<GeneralResponse>> {
                    override fun onResponse(
                        call: Call<ApiGeneralResponse<GeneralResponse>>,
                        response: Response<ApiGeneralResponse<GeneralResponse>>
                    ) {
                        if (response.isSuccessful) {
                            if (response.body()?.appservice?.type== "success") {
                                updateRouteApiResponse.value = Resource.success(response.body()?.returnData)
//                                prefsManager.save(PrefsManager.APP_FORM_CATALOG,formCatalogApiResponse)
                            }
                            else
                                updateRouteApiResponse.value = Resource.error(
                                    ApiUtils.getError(
                                        -1,
                                        Gson().toJson(response.body()?.appservice)
                                    )
                                )
                        } else {
                            updateRouteApiResponse.value = Resource.error(
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
                        updateRouteApiResponse.value = Resource.error(ApiUtils.failure(throwable))
                    }
                })
        }
    }

    fun updateRouteActivityApi(context: Context,routes: ArrayList<UpdateRouteActivity>) {
        if (isConnectedToInternet(context, true)) {
            updateRouteApiResponse.value = Resource.loading()

            val apiRequest = GeneralRequest(
                method = WebService.METHOD_BULK_UPDATE_ACTIVITY,
                service = WebService.SALES_APP_SERVICE,
                parameter = UpdateActivityRequest(
                    routes
                )
            )

            webService.updateActivityApi(apiRequest)
                .enqueue(object : Callback<ApiGeneralResponse<GeneralResponse>> {
                    override fun onResponse(
                        call: Call<ApiGeneralResponse<GeneralResponse>>,
                        response: Response<ApiGeneralResponse<GeneralResponse>>
                    ) {
                        if (response.isSuccessful) {
                            if (response.body()?.appservice?.type== "success") {
                                updateRouteApiResponse.value = Resource.success(response.body()?.returnData)
//                                prefsManager.save(PrefsManager.APP_FORM_CATALOG,formCatalogApiResponse)
                            }
                            else
                                updateRouteApiResponse.value = Resource.error(
                                    ApiUtils.getError(
                                        -1,
                                        Gson().toJson(response.body()?.appservice)
                                    )
                                )
                        } else {
                            updateRouteApiResponse.value = Resource.error(
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
                        updateRouteApiResponse.value = Resource.error(ApiUtils.failure(throwable))
                    }
                })
        }
    }

    fun updateHomeRouteActivityApi(context: Context,routes: ArrayList<UpdateRouteActivity>) {
        if (isConnectedToInternet(context, true)) {
            updateRouteActivityApiResponse.value = Resource.loading()

            val apiRequest = GeneralRequest(
                method = WebService.METHOD_BULK_UPDATE_ACTIVITY,
                service = WebService.SALES_APP_SERVICE,
                parameter = UpdateActivityRequest(
                    routes
                )
            )

            webService.updateActivityApi(apiRequest)
                .enqueue(object : Callback<ApiGeneralResponse<GeneralResponse>> {
                    override fun onResponse(
                        call: Call<ApiGeneralResponse<GeneralResponse>>,
                        response: Response<ApiGeneralResponse<GeneralResponse>>
                    ) {
                        if (response.isSuccessful) {
                            if (response.body()?.appservice?.type== "success") {
                                updateRouteActivityApiResponse.value = Resource.success(response.body()?.returnData)
//                                prefsManager.save(PrefsManager.APP_FORM_CATALOG,formCatalogApiResponse)
                            }
                            else
                                updateRouteActivityApiResponse.value = Resource.error(
                                    ApiUtils.getError(
                                        -1,
                                        Gson().toJson(response.body()?.appservice)
                                    )
                                )
                        } else {
                            updateRouteActivityApiResponse.value = Resource.error(
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
                        updateRouteActivityApiResponse.value = Resource.error(ApiUtils.failure(throwable))
                    }
                })
        }
    }

    fun updateCustomerApi(context: Context, request: AddBulkCustomerRequest) {
        if (isConnectedToInternet(context, true)) {
            updateAccountsResponse.value = Resource.loading()

            val apiRequest = GeneralRequest(
                method = WebService.METHOD_BULK_UPDATE_ACCOUNTS,
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
                                updateAccountsResponse.value = Resource.success(response.body()?.returnData)
                            }
                            else
                                updateAccountsResponse.value = Resource.error(
                                    ApiUtils.getError(
                                        -1,
                                        Gson().toJson(response.body()?.appservice)
                                    )
                                )
                        } else {
                            updateAccountsResponse.value = Resource.error(
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
                        updateAccountsResponse.value = Resource.error(ApiUtils.failure(throwable))
                    }
                })
        }
    }

    fun updateSurveyApi(context: Context,routes: ArrayList<UpdateSurvey>) {
        if (isConnectedToInternet(context, true)) {
            updateSurveyApiResponse.value = Resource.loading()

            val apiRequest = GeneralRequest(
                method = WebService.METHOD_BULK_UPDATE_SURVEY,
                service = WebService.SALES_APP_SERVICE,
                parameter = UpdateSurveyRequest(
                    routes
                )
            )

            webService.updateSurveyApi(apiRequest)
                .enqueue(object : Callback<ApiGeneralResponse<GeneralResponse>> {
                    override fun onResponse(
                        call: Call<ApiGeneralResponse<GeneralResponse>>,
                        response: Response<ApiGeneralResponse<GeneralResponse>>
                    ) {
                        if (response.isSuccessful) {
                            if (response.body()?.appservice?.type== "success") {
                                updateSurveyApiResponse.value = Resource.success(response.body()?.returnData)
//                                prefsManager.save(PrefsManager.APP_FORM_CATALOG,formCatalogApiResponse)
                            }
                            else
                                updateSurveyApiResponse.value = Resource.error(
                                    ApiUtils.getError(
                                        -1,
                                        Gson().toJson(response.body()?.appservice)
                                    )
                                )
                        } else {
                            updateSurveyApiResponse.value = Resource.error(
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
                        updateSurveyApiResponse.value = Resource.error(ApiUtils.failure(throwable))
                    }
                })
        }
    }

    fun createOrderApi(context: Context,routes: ArrayList<CreateOrderTemplate>) {
        if (isConnectedToInternet(context, true)) {
            createOrderApiResponse.value = Resource.loading()

            val apiRequest = GeneralRequest(
                method = WebService.METHOD_BULK_CREATE_ORDERS,
                service = WebService.SALES_APP_SERVICE,
                parameter = CreateOrderRequest(
                    routes
                )
            )

            webService.createOrderApi(apiRequest)
                .enqueue(object : Callback<ApiGeneralResponse<GeneralResponse>> {
                    override fun onResponse(
                        call: Call<ApiGeneralResponse<GeneralResponse>>,
                        response: Response<ApiGeneralResponse<GeneralResponse>>
                    ) {
                        if (response.isSuccessful) {
                            if (response.body()?.appservice?.type== "success") {
                                createOrderApiResponse.value = Resource.success(response.body()?.returnData)
//                                prefsManager.save(PrefsManager.APP_FORM_CATALOG,formCatalogApiResponse)
                            }
                            else
                                createOrderApiResponse.value = Resource.error(
                                    ApiUtils.getError(
                                        -1,
                                        Gson().toJson(response.body()?.appservice)
                                    )
                                )
                        } else {
                            createOrderApiResponse.value = Resource.error(
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
                        createOrderApiResponse.value = Resource.error(ApiUtils.failure(throwable))
                    }
                })
        }
    }

    fun updateOrderApi(context: Context,routes: ArrayList<CreateOrderTemplate>) {
        if (isConnectedToInternet(context, true)) {
            createOrderApiResponse.value = Resource.loading()

            val apiRequest = GeneralRequest(
                method = WebService.METHOD_BULK_UPDATE_ORDERS,
                service = WebService.SALES_APP_SERVICE,
                parameter = CreateOrderRequest(
                    routes
                )
            )

            webService.createOrderApi(apiRequest)
                .enqueue(object : Callback<ApiGeneralResponse<GeneralResponse>> {
                    override fun onResponse(
                        call: Call<ApiGeneralResponse<GeneralResponse>>,
                        response: Response<ApiGeneralResponse<GeneralResponse>>
                    ) {
                        if (response.isSuccessful) {
                            if (response.body()?.appservice?.type== "success") {
                                createOrderApiResponse.value = Resource.success(response.body()?.returnData)
//                                prefsManager.save(PrefsManager.APP_FORM_CATALOG,formCatalogApiResponse)
                            }
                            else
                                createOrderApiResponse.value = Resource.error(
                                    ApiUtils.getError(
                                        -1,
                                        Gson().toJson(response.body()?.appservice)
                                    )
                                )
                        } else {
                            createOrderApiResponse.value = Resource.error(
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
                        createOrderApiResponse.value = Resource.error(ApiUtils.failure(throwable))
                    }
                })
        }
    }


    fun repriceOrderApi(context: Context, integrationId: String?) {
        if (isConnectedToInternet(context, true)) {
            repriceOrderApiResponse.value = Resource.loading()

            val apiRequest = GeneralRequest(
                method = WebService.METHOD_REPRICE_ORDER,
                service = WebService.SALES_APP_SERVICE,
                parameter = RepriceOrderRequest(
                    integrationId!!
                )
            )

            webService.repriceOrderApi(apiRequest)
                .enqueue(object : Callback<ApiResponse<GetRouteAccountResponse>> {
                    override fun onResponse(
                        call: Call<ApiResponse<GetRouteAccountResponse>>,
                        response: Response<ApiResponse<GetRouteAccountResponse>>
                    ) {
                        if (response.isSuccessful) {
                            if (response.body()?.appservice?.type== "success") {
                                repriceOrderApiResponse.value = Resource.success(response.body()?.returnData?.data)
//                                prefsManager.save(PrefsManager.APP_FORM_CATALOG,formCatalogApiResponse)
                            }
                            else
                                repriceOrderApiResponse.value = Resource.error(
                                    ApiUtils.getError(
                                        -1,
                                        Gson().toJson(response.body()?.appservice)
                                    )
                                )
                        } else {
                            repriceOrderApiResponse.value = Resource.error(
                                ApiUtils.getError(
                                    response.code(),
                                    Gson().toJson(response.errorBody())
                                )
                            )
                        }
                    }
                    override fun onFailure(
                        call: Call<ApiResponse<GetRouteAccountResponse>>,
                        throwable: Throwable
                    ) {
                        repriceOrderApiResponse.value = Resource.error(ApiUtils.failure(throwable))
                    }
                })
        }
    }

    fun orderDetailApi(context: Context, integrationId: String?) {
        if (isConnectedToInternet(context, true)) {
            orderDetailApiResponse.value = Resource.loading()

            val apiRequest = GeneralRequest(
                method = WebService.METHOD_REPRICE_ORDER,
                service = WebService.SALES_APP_SERVICE,
                parameter = RepriceOrderRequest(
                    integrationId!!
                )
            )

            webService.repriceOrderApi(apiRequest)
                .enqueue(object : Callback<ApiResponse<GetRouteAccountResponse>> {
                    override fun onResponse(
                        call: Call<ApiResponse<GetRouteAccountResponse>>,
                        response: Response<ApiResponse<GetRouteAccountResponse>>
                    ) {
                        if (response.isSuccessful) {
                            if (response.body()?.appservice?.type== "success") {
                                orderDetailApiResponse.value = Resource.success(response.body()?.returnData?.data)
//                                prefsManager.save(PrefsManager.APP_FORM_CATALOG,formCatalogApiResponse)
                            }
                            else
                                orderDetailApiResponse.value = Resource.error(
                                    ApiUtils.getError(
                                        -1,
                                        Gson().toJson(response.body()?.appservice)
                                    )
                                )
                        } else {
                            orderDetailApiResponse.value = Resource.error(
                                ApiUtils.getError(
                                    response.code(),
                                    Gson().toJson(response.errorBody())
                                )
                            )
                        }
                    }
                    override fun onFailure(
                        call: Call<ApiResponse<GetRouteAccountResponse>>,
                        throwable: Throwable
                    ) {
                        orderDetailApiResponse.value = Resource.error(ApiUtils.failure(throwable))
                    }
                })
        }
    }


    fun createActivityApi(context: Context,activityRequest: ArrayList<CreateActivityData>) {
        if (isConnectedToInternet(context, true)) {
            createActivityApiResponse.value = Resource.loading()

            val apiRequest = GeneralRequest(
                method = WebService.METHOD_BULK_CREATE_ACTIVITY,
                service = WebService.SALES_APP_SERVICE,
                parameter = CreateActivityRequest(
                    activityRequest
                )
            )

            webService.createActivityApi(apiRequest)
                .enqueue(object : Callback<ApiGeneralResponse<GeneralResponse>> {
                    override fun onResponse(
                        call: Call<ApiGeneralResponse<GeneralResponse>>,
                        response: Response<ApiGeneralResponse<GeneralResponse>>
                    ) {
                        if (response.isSuccessful) {
                            if (response.body()?.appservice?.type== "success") {
                                createActivityApiResponse.value = Resource.success(response.body()?.returnData)
//                                prefsManager.save(PrefsManager.APP_FORM_CATALOG,formCatalogApiResponse)
                            }
                            else
                                createActivityApiResponse.value = Resource.error(
                                    ApiUtils.getError(
                                        -1,
                                        Gson().toJson(response.body()?.appservice)
                                    )
                                )
                        } else {
                            createActivityApiResponse.value = Resource.error(
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
                        createActivityApiResponse.value = Resource.error(ApiUtils.failure(throwable))
                    }
                })
        }
    }


    fun getNotificationApi(context: Context) {
        if (isConnectedToInternet(context, true)) {
            notificationList.value = Resource.loading()

            val notificationRequest = NotificationRequest(
                method = WebService.METHOD_GET_NOTIFICATIONS,
                service = WebService.CPORTAL_SERVICE
            )

            webService.getNotifications(notificationRequest)
                .enqueue(object : Callback<ApiResponse<ArrayList<GetNotificationListResponse>>> {
                    override fun onResponse(
                        call: Call<ApiResponse<ArrayList<GetNotificationListResponse>>>,
                        response: Response<ApiResponse<ArrayList<GetNotificationListResponse>>>
                    ) {
                        if (response.isSuccessful) {
                            if (response.body()?.appservice?.type== "success") {
                                notificationList.value = Resource.success(response.body()?.returnData?.data)
//                                    prefsManager.save(PrefsManager.USER_CART_DATA, response.body()?.returnData?.data?.CartDetail)
                            }
                            else
                                notificationList.value = Resource.error(
                                    ApiUtils.getError(
                                        -1,
                                        Gson().toJson(response.body()?.appservice)
                                    )
                                )
                        } else {
                            notificationList.value = Resource.error(
                                ApiUtils.getError(
                                    response.code(),
                                    Gson().toJson(response.errorBody())
                                )
                            )
                        }
                    }
                    override fun onFailure(
                        call: Call<ApiResponse<ArrayList<GetNotificationListResponse>>>,
                        throwable: Throwable
                    ) {
                        notificationList.value = Resource.error(ApiUtils.failure(throwable))
                    }
                })
        }
    }
}