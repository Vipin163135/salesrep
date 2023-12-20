package com.salesrep.app.ui.payment

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.salesrep.app.base.BaseViewModel
import com.salesrep.app.data.apis.WebService
import com.salesrep.app.data.models.CreatePaymentTemplate
import com.salesrep.app.data.models.PaymentMethodsTemplate
import com.salesrep.app.data.models.requests.*
import com.salesrep.app.data.models.response.GeneralResponse
import com.salesrep.app.data.models.response.PaymentProfileResponse
import com.salesrep.app.data.models.response.PaymentProfileTemplate
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
class PaymentViewModel @Inject constructor(
    val webService: WebService,
    val userRepository: UserRepository,
    val prefsManager: PrefsManager,

    ) : BaseViewModel() {

    val paymentMethodsList by lazy { SingleLiveEvent<Resource<ArrayList<PaymentMethodsTemplate>>>() }
    val paymentProfileList by lazy { SingleLiveEvent<Resource<ArrayList<PaymentProfileResponse>>>() }
    val addPaymentMethodResponse by lazy { SingleLiveEvent<Resource<ArrayList<PaymentProfileTemplate>>>() }
    val removePaymentMethodResponse by lazy { SingleLiveEvent<Resource<GeneralResponse>>() }
    val createPaymentResponse by lazy { SingleLiveEvent<Resource<GeneralResponse>>() }
    val revertPaymentResponse by lazy { SingleLiveEvent<Resource<GeneralResponse>>() }

    fun getPaymentMethodsApi(context: Context) {
        if (isConnectedToInternet(context, true)) {
            paymentMethodsList.value = Resource.loading()

            val paymentMethodApiRequest = GeneralRequest(
                method = WebService.METHOD_GET_PAYMENT_METHODS,
                service = WebService.SALES_APP_SERVICE,
                parameter = JsonObject()
            )

            webService.getPaymentMethods(paymentMethodApiRequest)
                .enqueue(object : Callback<ApiResponse<ArrayList<PaymentMethodsTemplate>>> {
                    override fun onResponse(
                        call: Call<ApiResponse<ArrayList<PaymentMethodsTemplate>>>,
                        response: Response<ApiResponse<ArrayList<PaymentMethodsTemplate>>>
                    ) {
                        if (response.isSuccessful) {
                            if (response.body()?.appservice?.type == "success") {
                                paymentMethodsList.value =
                                    Resource.success(response.body()?.returnData?.data)
                            } else
                                paymentMethodsList.value = Resource.error(
                                    ApiUtils.getError(
                                        -1,
                                        Gson().toJson(response.body()?.appservice)
                                    )
                                )
                        } else {
                            paymentMethodsList.value = Resource.error(
                                ApiUtils.getError(
                                    response.code(),
                                    Gson().toJson(response.errorBody())
                                )
                            )
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse<ArrayList<PaymentMethodsTemplate>>>,
                        throwable: Throwable
                    ) {
                        paymentMethodsList.value = Resource.error(ApiUtils.failure(throwable))
                    }
                })
        }
    }

    fun addPaymentMethodApi(context: Context, data: String, type: String) {
        if (isConnectedToInternet(context, true)) {
            addPaymentMethodResponse.value = Resource.loading()

            val paymentMethodApiRequest = GeneralRequest(
                method = WebService.METHOD_ADD_PAYMENT_METHOD,
                service = WebService.SALES_APP_SERVICE,
                parameter = AddPaymentProfileRequest(
                    profiles = arrayListOf(
                        AddPaymentMethodRequest(
                            account_id = userRepository.getUser()!!.id,
                            data = data,
                            type = type
                        )
                    )
                )
            )

            webService.addPaymentMethod(paymentMethodApiRequest)
                .enqueue(object : Callback<ApiResponse<ArrayList<PaymentProfileTemplate>>> {
                    override fun onResponse(
                        call: Call<ApiResponse<ArrayList<PaymentProfileTemplate>>>,
                        response: Response<ApiResponse<ArrayList<PaymentProfileTemplate>>>
                    ) {
                        if (response.isSuccessful) {
                            if (response.body()?.appservice?.type == "success") {
                                addPaymentMethodResponse.value =
                                    Resource.success(response.body()?.returnData?.data)
                            } else
                                addPaymentMethodResponse.value = Resource.error(
                                    ApiUtils.getError(
                                        -1,
                                        Gson().toJson(response.body()?.appservice)
                                    )
                                )
                        } else {
                            addPaymentMethodResponse.value = Resource.error(
                                ApiUtils.getError(
                                    response.code(),
                                    Gson().toJson(response.errorBody())
                                )
                            )
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse<ArrayList<PaymentProfileTemplate>>>,
                        throwable: Throwable
                    ) {
                        addPaymentMethodResponse.value = Resource.error(ApiUtils.failure(throwable))
                    }
                })
        }
    }

    fun createPaymentApi(context: Context, list: ArrayList<CreatePaymentTemplate>) {
        if (isConnectedToInternet(context, true)) {
            createPaymentResponse.value = Resource.loading()

            val paymentMethodApiRequest = GeneralRequest(
                method = WebService.METHOD_CREATE_PAYMENTS,
                service = WebService.SALES_APP_SERVICE,
                parameter = CreatePaymentRequest(
                    payments = list
                    )
            )

            webService.createPaymentsApi(paymentMethodApiRequest)
                .enqueue(object : Callback<ApiGeneralResponse<GeneralResponse>> {
                    override fun onResponse(
                        call: Call<ApiGeneralResponse<GeneralResponse>>,
                        response: Response<ApiGeneralResponse<GeneralResponse>>
                    ) {
                        if (response.isSuccessful) {
                            if (response.body()?.appservice?.type == "success") {
                                createPaymentResponse.value =
                                    Resource.success()
                            } else
                                createPaymentResponse.value = Resource.error(
                                    ApiUtils.getError(
                                        -1,
                                        Gson().toJson(response.body()?.appservice)
                                    )
                                )
                        } else {
                            createPaymentResponse.value = Resource.error(
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
                        createPaymentResponse.value = Resource.error(ApiUtils.failure(throwable))
                    }
                })
        }
    }

    fun getPaymentProfileApi(context: Context) {
        if (isConnectedToInternet(context, true)) {
            paymentProfileList.value = Resource.loading()

            val paymentProfilesApiRequest = GeneralRequest(
                method = WebService.METHOD_GET_PAYMENT_PROFILES,
                service = WebService.CPORTAL_SERVICE,
                parameter = GetPaymentProfilesRequest(
                    account_id = userRepository.getUser()!!.id
                )
            )

            webService.getPaymentProfile(paymentProfilesApiRequest)
                .enqueue(object : Callback<ApiResponse<ArrayList<PaymentProfileResponse>>> {
                    override fun onResponse(
                        call: Call<ApiResponse<ArrayList<PaymentProfileResponse>>>,
                        response: Response<ApiResponse<ArrayList<PaymentProfileResponse>>>
                    ) {
                        if (response.isSuccessful) {
                            if (response.body()?.appservice?.type == "success") {
                                paymentProfileList.value =
                                    Resource.success(response.body()?.returnData?.data)
                            } else
                                paymentProfileList.value = Resource.error(
                                    ApiUtils.getError(
                                        -1,
                                        Gson().toJson(response.body()?.appservice)
                                    )
                                )
                        } else {
                            paymentProfileList.value = Resource.error(
                                ApiUtils.getError(
                                    response.code(),
                                    Gson().toJson(response.errorBody())
                                )
                            )
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse<ArrayList<PaymentProfileResponse>>>,
                        throwable: Throwable
                    ) {
                        paymentProfileList.value = Resource.error(ApiUtils.failure(throwable))
                    }
                })
        }
    }

    fun removePaymentApi(context: Context, list: List<RevertPaymentData>) {
        if (isConnectedToInternet(context, true)) {
            revertPaymentResponse.value = Resource.loading()

            val paymentMethodApiRequest = GeneralRequest(
                method = WebService.METHOD_CANCEL_PAYMENTS,
                service = WebService.SALES_APP_SERVICE,
                parameter = RevertPaymentRequest(
                    payments = list
                )
            )

            webService.revertPaymentsApi(paymentMethodApiRequest)
                .enqueue(object : Callback<ApiGeneralResponse<GeneralResponse>> {
                    override fun onResponse(
                        call: Call<ApiGeneralResponse<GeneralResponse>>,
                        response: Response<ApiGeneralResponse<GeneralResponse>>
                    ) {
                        if (response.isSuccessful) {
                            if (response.body()?.appservice?.type == "success") {
                                revertPaymentResponse.value =
                                    Resource.success()
                            } else
                                revertPaymentResponse.value = Resource.error(
                                    ApiUtils.getError(
                                        -1,
                                        Gson().toJson(response.body()?.appservice)
                                    )
                                )
                        } else {
                            revertPaymentResponse.value = Resource.error(
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
                        revertPaymentResponse.value = Resource.error(ApiUtils.failure(throwable))
                    }
                })
        }
    }

//    fun makePaymentApi(context: Context, orderId:Int, paymentMethodId:Int) {
//        if (isConnectedToInternet(context, true)) {
//            checkoutCartApiResponse.value = Resource.loading()
//
//            val cartApiRequest = GeneralRequest(
//                method = WebService.METHOD_PAY_ORDER,
//                service = WebService.CPORTAL_SERVICE,
//                parameter =MakePaymentRequest(
//                    orderId,
//                    paymentMethodId
//                )
//            )
//
//            webService.makePaymentApi(cartApiRequest)
//                .enqueue(object : Callback<ApiResponse<GetCartResponse>> {
//                    override fun onResponse(
//                        call: Call<ApiResponse<GetCartResponse>>,
//                        response: Response<ApiResponse<GetCartResponse>>
//                    ) {
//                        if (response.isSuccessful) {
//                            if (response.body()?.appservice?.type== "success") {
//                                checkoutCartApiResponse.value = Resource.success(response.body()?.returnData?.data)
//                            }
//                            else
//                                checkoutCartApiResponse.value = Resource.error(
//                                    ApiUtils.getError(
//                                        -1,
//                                        Gson().toJson(response.body()?.appservice)
//                                    )
//                                )
//                        } else {
//                            checkoutCartApiResponse.value = Resource.error(
//                                ApiUtils.getError(
//                                    response.code(),
//                                    Gson().toJson(response.errorBody())
//                                )
//                            )
//                        }
//                    }
//                    override fun onFailure(
//                        call: Call<ApiResponse<GetCartResponse>>,
//                        throwable: Throwable
//                    ) {
//                        checkoutCartApiResponse.value = Resource.error(ApiUtils.failure(throwable))
//                    }
//                })
//        }
//    }


    fun removePaymentApi(context: Context, paymentMethodId: Int) {
        if (isConnectedToInternet(context, true)) {
            removePaymentMethodResponse.value = Resource.loading()

            val removePaymentApiRequest = GeneralRequest(
                method = WebService.METHOD_REMOVE_PAYMENT,
                service = WebService.SALES_APP_SERVICE,
                parameter = RemovePaymentMethodRequest(
                    paymentMethodId
                )
            )

            webService.removePaymentApi(removePaymentApiRequest)
                .enqueue(object : Callback<ApiGeneralResponse<GeneralResponse>> {
                    override fun onResponse(
                        call: Call<ApiGeneralResponse<GeneralResponse>>,
                        response: Response<ApiGeneralResponse<GeneralResponse>>
                    ) {
                        if (response.isSuccessful) {
                            if (response.body()?.appservice?.type == "success") {
                                removePaymentMethodResponse.value =
                                    Resource.success(response.body()?.returnData)
                            } else
                                removePaymentMethodResponse.value = Resource.error(
                                    ApiUtils.getError(
                                        -1,
                                        Gson().toJson(response.body()?.appservice)
                                    )
                                )
                        } else {
                            removePaymentMethodResponse.value = Resource.error(
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
                        removePaymentMethodResponse.value =
                            Resource.error(ApiUtils.failure(throwable))
                    }
                })
        }
    }


    fun createCreditNoteApi(context: Context, list: ArrayList<CreatePaymentTemplate>) {
        if (isConnectedToInternet(context, true)) {
            createPaymentResponse.value = Resource.loading()

            val paymentMethodApiRequest = GeneralRequest(
                method = WebService.METHOD_CREATE_CREDIT_NOTE,
                service = WebService.SALES_APP_SERVICE,
                parameter = CreatePaymentRequest(
                    payments = list
                )
            )

            webService.createPaymentsApi(paymentMethodApiRequest)
                .enqueue(object : Callback<ApiGeneralResponse<GeneralResponse>> {
                    override fun onResponse(
                        call: Call<ApiGeneralResponse<GeneralResponse>>,
                        response: Response<ApiGeneralResponse<GeneralResponse>>
                    ) {
                        if (response.isSuccessful) {
                            if (response.body()?.appservice?.type == "success") {
                                createPaymentResponse.value =
                                    Resource.success()
                            } else
                                createPaymentResponse.value = Resource.error(
                                    ApiUtils.getError(
                                        -1,
                                        Gson().toJson(response.body()?.appservice)
                                    )
                                )
                        } else {
                            createPaymentResponse.value = Resource.error(
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
                        createPaymentResponse.value = Resource.error(ApiUtils.failure(throwable))
                    }
                })
        }
    }



}