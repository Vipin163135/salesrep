package com.salesrep.app.data.apis

import com.google.gson.JsonObject
import com.salesrep.app.data.appConfig.AppConfigResponse
import com.salesrep.app.data.models.PaymentMethodsTemplate
import com.salesrep.app.data.models.inventory.*
import com.salesrep.app.data.models.requests.*
import com.salesrep.app.data.models.response.*
import com.salesrep.app.data.network.responseUtil.ApiGeneralResponse
import com.salesrep.app.data.network.responseUtil.ApiResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*


/**
 * Created by VIPIN.
 */

interface WebService {
    companion object {
        private const val GET_APP_CONFIG = "api/crmapps/"
        private const val GET_LOCALES = "api/locales"
        private const val APP_SERVICES = "api/appservices?method=executeAppService"
        const val METHOD_GET_VALID_HOSTS = "getValidHosts"
        const val METHOD_GET_APP_CONFIG= "getConfig"
        const val CPORTAL_SERVICE= "cportalServices"
        const val SALES_APP_SERVICE= "salesrepAppServices"
        const val METHOD_GET_APP_LOCALES= "getLocales"

        const val METHOD_LOGIN= "login"
        const val METHOD_CHANGE_PASSWORD= "changeUserPassword"
        const val METHOD_RESET_PASSWORD= "requestPasswordReset"
        const val METHOD_SET_PASSWORD= "setUserPassword"
        const val METHOD_GET_ROUTES= "getTeamRoutes"
        const val METHOD_GET_ROUTE_ACCOUNTS= "getRouteAccounts"
        const val METHOD_GET_TEAM_ACCOUNTS= "getTeamAccounts"
        const val METHOD_GET_TEAM_PRODUCTS= "getTeamProducts"
        const val METHOD_GET_TEAM_PRICELIST= "getTeamPricelists"
        const val METHOD_FORM_CATALOG= "getFormCatalogs"
        const val METHOD_BULK_CREATE_ACCOUNTS= "bulkCreateAccounts"
        const val METHOD_BULK_UPDATE_ACCOUNTS= "bulkUpdateAccounts"
        const val METHOD_BULK_UPDATE_ROUTE= "bulkUpdateRoutes"
        const val METHOD_BULK_UPDATE_SURVEY= "bulkUpdateSurveys"
        const val METHOD_BULK_UPDATE_ACTIVITY= "bulkUpdateActivities"
        const val METHOD_BULK_CREATE_ORDERS= "bulkCreateOrders"
        const val METHOD_BULK_UPDATE_ORDERS= "bulkUpdateOrders"
        const val METHOD_REPRICE_ORDER= "repriceOrder"
        const val METHOD_ORDER_DETAIL= "getOrderData"

        const val METHOD_GET_PAYMENT_METHODS= "getPaymentOptions"
        const val METHOD_ADD_PAYMENT_METHOD= "bulkCreatePaymentProfiles"
        const val METHOD_GET_PAYMENT_PROFILES= "getPaymentProfiles"
        const val METHOD_PAY_ORDER= "payOrder"
        const val METHOD_REMOVE_PAYMENT= "removePaymentProfile"
        const val METHOD_CREATE_PAYMENTS= "bulkCreatePayments"
        const val METHOD_CREATE_CREDIT_NOTE= "bulkCreateCreditNotes"
        const val METHOD_CANCEL_PAYMENTS= "bulkCancelPayments"
        const val METHOD_GET_PAGED_ACCOUNTS= "getPagedRouteAccounts"
        const val METHOD_BULK_CREATE_ACTIVITY= "bulkCreateActivities"
        const val METHOD_GET_TEAM_INVENTORY= "getTeamInventory"
        const val METHOD_BULK_CREATE_MOVEMENT= "bulkCreateInvMovement"
        const val METHOD_BULK_UPDATE_MOVEMENT= "bulkUpdateInvMovement"
        const val METHOD_BULK_COMMIT_MOVEMENT= "bulkCommitInvMovement"
        const val METHOD_BULK_CANCEL_MOVEMENT= "bulkCancelInvMovement"
        const val METHOD_BULK_PREPARE_MOVEMENT= "prepareInvMovement"
        const val METHOD_GET_TEAM_VALUE_RECONCILATION= "getTeamValueReconcilation"
        const val METHOD_GET_NOTIFICATIONS= "getNotificationMessages"
        const val METHOD_GET_DOCUMENTS= "getPagedTeamDocuments"
    }


    /*POST APIS*/
    @GET(GET_APP_CONFIG)
    fun appConfig(@Query("method") method: String,
                  @Query("params") params: String  ): Call<AppConfigResponse>

    @POST(APP_SERVICES)
    fun login(@Header("Authorization") token: String, @Body() request: LoginRequest): Call<ApiGeneralResponse<LoginApiResponse>>

    @POST(APP_SERVICES)
    fun changePasswordApi(@Body() request: GeneralRequest<ChangePasswordRequest>): Call<ApiGeneralResponse<GeneralResponse>>

    @POST(APP_SERVICES)
    fun getTeamRoutes(@Body() request: GeneralRequest<GetTeamRouteRequest>): Call<ApiResponse<GetHomeRoutesResponse>>

    @POST(APP_SERVICES)
    fun getTeamProducts(@Body() request: GeneralRequest<GetTeamRouteRequest>): Call<ApiResponse<ArrayList<GetTeamProductsResponse>>>

    @POST(APP_SERVICES)
    fun getTeamPriceList(@Body() request: GeneralRequest<GetTeamRouteRequest>): Call<ApiResponse<ArrayList<GetTeamPricelistResponse>>>

    @POST(APP_SERVICES)
    fun getRouteAccounts(@Body() request: GeneralRequest<GetRouteAccountsRequest>): Call<ApiResponse<ArrayList<GetRouteAccountResponse>>>

    @GET("directions/json?sensor=false&mode=driving&alternatives=false&units=metric")
    fun getPolYLine(
        @Query("key") key: String,
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("language") language: String,
        @Query("waypoints") waypoints: String
    ): Call<ResponseBody>

    @POST(APP_SERVICES)
    fun getCustomerList(@Query("page") page: Int,@Query("sSearch") search: String, @Query("count") count: Int, @Body() request: GeneralRequest<GetTeamRouteRequest>): Call<ApiResponse<GetCustomerListResponse>>

    @POST(APP_SERVICES)
    fun formCatalogApi(@Body() request: GeneralRequest<FormCatalogRequest>): Call<ApiResponse<GetFormCatalogResponse>>

    @POST(APP_SERVICES)
    fun createBulkAccountApi(@Body() request: GeneralRequest<AddBulkCustomerRequest>): Call<ApiGeneralResponse<GeneralResponse>>

    @POST(APP_SERVICES)
    fun updateBulkAccountApi(@Body() request: GeneralRequest<AddBulkCustomerRequest>): Call<ApiGeneralResponse<GeneralResponse>>

    @POST(APP_SERVICES)
    fun updateRouteApi(@Body() request: GeneralRequest<UpdateRouteRequest>): Call<ApiGeneralResponse<GeneralResponse>>

    @POST(APP_SERVICES)
    fun updateSurveyApi(@Body() request: GeneralRequest<UpdateSurveyRequest>): Call<ApiGeneralResponse<GeneralResponse>>

    @POST(APP_SERVICES)
    fun updateActivityApi(@Body() request: GeneralRequest<UpdateActivityRequest>): Call<ApiGeneralResponse<GeneralResponse>>

    @POST(APP_SERVICES)
    fun createOrderApi(@Body() request: GeneralRequest<CreateOrderRequest>): Call<ApiGeneralResponse<GeneralResponse>>

    @POST(APP_SERVICES)
    fun repriceOrderApi(@Body() request: GeneralRequest<RepriceOrderRequest>): Call<ApiResponse<GetRouteAccountResponse>>

    @POST(APP_SERVICES)
    fun getPaymentMethods(@Body() request: GeneralRequest<JsonObject>): Call<ApiResponse<ArrayList<PaymentMethodsTemplate>>>

    @POST(APP_SERVICES)
    fun addPaymentMethod(@Body() request: GeneralRequest<AddPaymentProfileRequest>): Call<ApiResponse<ArrayList<PaymentProfileTemplate>>>

    @POST(APP_SERVICES)
    fun getPaymentProfile(@Body() request: GeneralRequest<GetPaymentProfilesRequest>): Call<ApiResponse<ArrayList<PaymentProfileResponse>>>

//    @POST(APP_SERVICES)
//    fun makePaymentApi(@Body() request: GeneralRequest<MakePaymentRequest>): Call<ApiResponse<GetCartResponse>>

    @POST(APP_SERVICES)
    fun removePaymentApi(@Body() request: GeneralRequest<RemovePaymentMethodRequest>): Call<ApiGeneralResponse<GeneralResponse>>

    @POST(APP_SERVICES)
    fun createPaymentsApi(@Body() request: GeneralRequest<CreatePaymentRequest>): Call<ApiGeneralResponse<GeneralResponse>>

    @POST(APP_SERVICES)
    fun revertPaymentsApi(@Body() request: GeneralRequest<RevertPaymentRequest>): Call<ApiGeneralResponse<GeneralResponse>>

    @POST(APP_SERVICES)
    fun getPagedRouteAccount(@Query("page") page: Int, @Body() request: GeneralRequest<GetRouteAccountsRequest>): Call<ApiResponse<GetPagedRouteAccountResponse>>

    @POST(APP_SERVICES)
    fun createActivityApi(@Body() request: GeneralRequest<CreateActivityRequest>): Call<ApiGeneralResponse<GeneralResponse>>

    @POST(APP_SERVICES)
    fun createCreditNoteApi(@Body() request: GeneralRequest<CreatePaymentRequest>): Call<ApiGeneralResponse<GeneralResponse>>

    @POST(APP_SERVICES)
    fun getTeamInventoryApi(@Body() request: GeneralRequest<GetTeamRouteRequest>): Call<ApiResponse<GetTeamInventoryResponse>>

    @POST(APP_SERVICES)
    fun createBulkMovementApi(@Body() request: GeneralRequest<CreateMovementRequest>): Call<ApiGeneralResponse<GeneralResponse>>

    @POST(APP_SERVICES)
    fun cancelBulkMovementApi(@Body() request: GeneralRequest<CancelMovementRequest>): Call<ApiGeneralResponse<GeneralResponse>>

    @POST(APP_SERVICES)
    fun prepareMovementApi(@Body() request: GeneralRequest<MovementCancelData>): Call<ApiResponse<InvMovementTemplate>>

    @POST(APP_SERVICES)
    fun getTeamValueRecon(@Body() request: GeneralRequest<GetTeamRouteRequest>): Call<ApiResponse<ArrayList<GetTeamValueReconcilation>>>

    @POST(APP_SERVICES)
    fun getNotifications(@Body() request: NotificationRequest): Call<ApiResponse<ArrayList<GetNotificationListResponse>>>

    @POST(APP_SERVICES)
    fun getDocumentList(@Query("page") page: Int, @Query("count") count: Int, @Body() request: GeneralRequest<GetTeamRouteRequest>): Call<ApiResponse<GetDocumentListResponse>>

}