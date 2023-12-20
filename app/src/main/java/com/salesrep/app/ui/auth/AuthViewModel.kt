package com.salesrep.app.ui.auth

import android.content.Context
import android.util.Base64
import android.view.View
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.salesrep.app.R
import com.salesrep.app.base.BaseViewModel
import com.salesrep.app.data.apis.HostWebService
import com.salesrep.app.data.apis.SignupWebService
import com.salesrep.app.data.apis.WebService
import com.salesrep.app.data.models.requests.*
import com.salesrep.app.data.models.response.GeneralResponse
import com.salesrep.app.data.models.response.LoginApiResponse
import com.salesrep.app.data.models.response.PasswordPolicy
import com.salesrep.app.data.models.response.SignupResponseData
import com.salesrep.app.data.network.responseUtil.ApiGeneralResponse
import com.salesrep.app.data.network.responseUtil.ApiResponse
import com.salesrep.app.data.network.responseUtil.ApiUtils
import com.salesrep.app.data.network.responseUtil.Resource
import com.salesrep.app.data.repos.UserRepository
import com.salesrep.app.di.SingleLiveEvent
import com.salesrep.app.util.PrefsManager
import com.salesrep.app.util.ValidationUtils
import com.salesrep.app.util.ValidationUtils.isEmailValid
import com.salesrep.app.util.ValidationUtils.isFieldNullOrEmpty
import com.salesrep.app.util.ValidationUtils.isNameValid
import com.salesrep.app.util.isConnectedToInternet
import dagger.hilt.android.lifecycle.HiltViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    val webService: WebService,
    val signupWebService: SignupWebService,
    val prefsManager: PrefsManager
): BaseViewModel() {


    val successErrorResponse by lazy { SingleLiveEvent<Int>() }
    val setPasswordResponse by lazy { SingleLiveEvent<Resource<LoginApiResponse>>() }
    val resetPasswordResponse by lazy { SingleLiveEvent<Resource<SignupResponseData>>() }
    val loginResponse by lazy { SingleLiveEvent<Resource<LoginApiResponse>>() }
    val changePasswordApiResponse  by lazy { SingleLiveEvent<Resource<GeneralResponse>>() }

    val name = ObservableField<String>("")
    val country = ObservableField<String>("")
    val code = ObservableField<String>("")
    val city = ObservableField<String>("")
    val email = ObservableField<String>("salesrep003")
    val emailSignup = ObservableField<String>("")
    val password = ObservableField<String>("salesrep003")
    val passwordSignup = ObservableField<String>("")
    val confirmPassword = ObservableField<String>("")
    val profilePic = ObservableField<String>("")
    val ISO = ObservableField<String>("IN")
    val deviceToken = ObservableField<String>("")
    val deviceType = ObservableField<String>("android")
    val isTermsAccepted = ObservableBoolean(false)

    val oldPassword = ObservableField<String>("")
    val newPassword = ObservableField<String>("")

    val passwordPolicy: PasswordPolicy? = prefsManager.getObject(PrefsManager.PASSWORD_POLICY, PasswordPolicy::class.java)


    fun onClickLogin(view: View) {
        when {
            isFieldNullOrEmpty(email.get()) -> {
                successErrorResponse.postValue(R.string.error_email_empty)
            }
            isFieldNullOrEmpty(password.get()) -> {
                successErrorResponse.postValue(R.string.error_password_empty)
            }
//            (!isTermsAccepted.get()) -> {
//                successErrorResponse.postValue(R.string.login_terms_check)
//            }
            else -> {
                login(view.context)
            }
        }
    }

    fun onClickUpdatePassword(view: View) {
        when {
            ValidationUtils.isFieldNullOrEmpty(oldPassword.get()) -> {
                successErrorResponse.postValue(R.string.error_password_empty)
            }
            ValidationUtils.isFieldNullOrEmpty(newPassword.get()) -> {
                successErrorResponse.postValue(R.string.error_password_empty)
            }
            else -> {
                updatePasswordAccount(view.context)
            }
        }
    }



    private fun updatePasswordAccount(context: Context) {
        if (isConnectedToInternet(context, true)) {
            changePasswordApiResponse.value = Resource.loading()

            val oldPass = Base64.encodeToString(
                oldPassword.get().toString().encodeToByteArray(),
                Base64.NO_WRAP
            )
            val newPass = Base64.encodeToString(
                newPassword.get().toString().encodeToByteArray(),
                Base64.NO_WRAP
            )

            val apiRequest = GeneralRequest(
                method = WebService.METHOD_CHANGE_PASSWORD,
                service = WebService.CPORTAL_SERVICE,
                parameter =ChangePasswordRequest(
                    requestData = RequestData(
                        oldPassword = oldPass,
                        newPassword = newPass
                    )
                )
            )

            webService.changePasswordApi(apiRequest)
                .enqueue(object : Callback<ApiGeneralResponse<GeneralResponse>> {
                    override fun onResponse(
                        call: Call<ApiGeneralResponse<GeneralResponse>>,
                        response: Response<ApiGeneralResponse<GeneralResponse>>
                    ) {
                        if (response.isSuccessful) {
                            if (response.body()?.appservice?.type== "success") {
                                changePasswordApiResponse.value = Resource.success(response.body()?.returnData)
                            }
                            else
                                changePasswordApiResponse.value = Resource.error(
                                    ApiUtils.getError(
                                        -1,
                                        Gson().toJson(response.body()?.appservice)
                                    )
                                )
                        } else {
                            changePasswordApiResponse.value = Resource.error(
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
                        changePasswordApiResponse.value = Resource.error(ApiUtils.failure(throwable))
                    }
                })
        }
    }


    //    fun onClickForgotPassword(view: View) {
////        (view.context as AuthActivity).updateFragment(FORGOT_PASSWORD_PAGE)
//    }
//
    fun onClickDoneResetPassword(view: View) {
        when {
            isFieldNullOrEmpty(emailSignup.get()) -> {
                successErrorResponse.postValue(R.string.error_email_empty)
            }
            !isEmailValid(emailSignup.get() ?: "") -> {
                successErrorResponse.postValue(R.string.error_invalid_email)
            }
            else -> {
                sendResetPasswordLink(view.context)
            }
        }
    }

//    fun onClickProceedToNextStep(view: View) {
//
//        when {
//            isFieldNullOrEmpty(name.get()?.trim()) -> {
//                successErrorResponse.postValue(R.string.error_please_enter_full_name)
//            }
//            !isNameValid(name.get() ?: "") -> {
//                successErrorResponse.postValue(R.string.error_name_invalid)
//            }
//            isFieldNullOrEmpty(emailSignup.get()?.trim()) -> {
//                successErrorResponse.postValue(R.string.error_email_empty)
//            }
//            !isEmailValid(email = emailSignup.get() ?: "") -> {
//                successErrorResponse.postValue(R.string.error_please_enter_valid_email)
//            }
//            isFieldNullOrEmpty(phoneNumber.get()?.trim()) -> {
//                successErrorResponse.postValue(R.string.error_phone_number_empty)
//            }
//            !isPhoneNumberValid(phoneNumber = phoneNumber.get() ?: "") -> {
//                successErrorResponse.postValue(R.string.error_please_enter_valid_phone_number)
//            }
//            (!isTermsAccepted.get()) -> {
//                successErrorResponse.postValue(R.string.login_terms_check)
//            }
//            else -> {
//                signUp(view.context)
//            }
//        }
//    }

    fun onClickContinue(view: View) {

        when {

            isFieldNullOrEmpty(code.get()?.trim()) -> {
                successErrorResponse.postValue(R.string.error_code_empty)
            }
            !isNameValid(code.get() ?: "") -> {
                successErrorResponse.postValue(R.string.error_please_enter_valid_code)
            }
            isFieldNullOrEmpty(passwordSignup.get()?.trim()) -> {
                successErrorResponse.postValue(R.string.error_password_empty)
            }
            isFieldNullOrEmpty(confirmPassword.get()?.trim()) -> {
                successErrorResponse.postValue(R.string.error_confirm_password_empty)
            }
            (passwordSignup.get()?.trim() != confirmPassword.get()?.trim()) -> {
                successErrorResponse.postValue(R.string.error_password_did_not_match)
            }
            else -> {
                setPassword(view.context)
            }
        }
    }

    /* Apis Calling */
    private fun login(context: Context) {
        if (isConnectedToInternet(context, true)) {
            loginResponse.value = Resource.loading()

            val loginRequest = LoginRequest(
                method = WebService.METHOD_LOGIN,
                service = WebService.SALES_APP_SERVICE,
                parameters = LoginParameters(
                    device_token = deviceToken.get().toString(),
                    device_type = deviceType.get().toString()
                )
            )
            if (email.get() != null && password.get() != null) {
                // concatenate username and password with colon for authentication
                val credentials: String = email.get() + ":" + password.get()
                // create Base64 encodet string
                val basic = "Basic " + Base64.encodeToString(
                    credentials.encodeToByteArray(),
                    Base64.NO_WRAP
                );
                Timber.d("Authorization : $basic")
                webService.login(basic, loginRequest)
                    .enqueue(object : Callback<ApiGeneralResponse<LoginApiResponse>> {
                        override fun onResponse(
                            call: Call<ApiGeneralResponse<LoginApiResponse>>,
                            response: Response<ApiGeneralResponse<LoginApiResponse>>
                        ) {
                            if (response.isSuccessful) {
                                if (response.body()?.appservice?.type == "success")
                                    loginResponse.value = Resource.success(response.body()?.returnData)
                                else
                                    loginResponse.value = Resource.error(
                                        ApiUtils.getError(
                                            -1,
                                            Gson().toJson(response.body()?.appservice)
                                        )
                                    )
                            } else {
                                loginResponse.value = Resource.error(
                                    ApiUtils.getError(
                                        response.code(),
                                        Gson().toJson(response.errorBody())
                                    )
                                )
                            }
                        }

                        override fun onFailure(
                            call: Call<ApiGeneralResponse<LoginApiResponse>>,
                            throwable: Throwable
                        ) {
                            loginResponse.value = Resource.error(ApiUtils.failure(throwable))
                        }
                    })
            }
        }
    }

    private fun sendResetPasswordLink(context: Context) {

        if (isConnectedToInternet(context, true)) {
            resetPasswordResponse.value = Resource.loading()

            val setPassRequest = ResetPasswordRequest(
                method = WebService.METHOD_RESET_PASSWORD,
                service = WebService.CPORTAL_SERVICE,
                parameters = ResetPassParameters(
                    resetPassData = ResetPassData(
                        email = emailSignup.get().toString()
                    )
                )
            )

            signupWebService.resetPassword(setPassRequest)
                .enqueue(object : Callback<ApiGeneralResponse<SignupResponseData>> {
                    override fun onResponse(
                        call: Call<ApiGeneralResponse<SignupResponseData>>,
                        response: Response<ApiGeneralResponse<SignupResponseData>>
                    ) {
                        if (response.isSuccessful) {
                            if (response.body()?.appservice?.type == "success")
                                resetPasswordResponse.value = Resource.success(response.body()?.returnData)
                            else
                                resetPasswordResponse.value = Resource.error(
                                    ApiUtils.getError(
                                        -1,
                                        Gson().toJson(response.body()?.appservice)
                                    )
                                )
                        } else {
                            resetPasswordResponse.value = Resource.error(
                                ApiUtils.getError(
                                    response.code(),
                                    Gson().toJson(response.errorBody())
                                )
                            )
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiGeneralResponse<SignupResponseData>>,
                        throwable: Throwable
                    ) {
                        resetPasswordResponse.value = Resource.error(ApiUtils.failure(throwable))
                    }
                })
        }
    }

//    fun signUp(context: Context) {
//        if (isConnectedToInternet(context, true)) {
//            ApiGeneralResponse<SignupResponseData>.value = Resource.loading()
//
//            val signupRequest = SignupRequest(
//                method = WebService.METHOD_SIGNUP,
//                service = WebService.CPORTAL_SERVICE,
//                parameters = Parameters(
//                    signupData = SignupData(
//                        email = emailSignup.get().toString(),
//                        phone = phoneNumber.get().toString(),
//                        accountname = name.get().toString(),
//                        device_token = deviceToken.get().toString(),
//                        device_type = deviceType.get().toString()
//                    )
//                )
//            )
//
//            signupWebService.signup(signupRequest)
//                .enqueue(object : Callback<ApiGeneralResponse<SignupResponseData>> {
//                    override fun onResponse(
//                        call: Call<ApiGeneralResponse<SignupResponseData>>,
//                        response: Response<ApiGeneralResponse<SignupResponseData>>
//                    ) {
//                        if (response.isSuccessful) {
//                            if (response.body()?.appservice?.type == "success")
//                                ApiGeneralResponse<SignupResponseData>.value = Resource.success(response.body()?._return)
//                            else
//                                ApiGeneralResponse<SignupResponseData>.value = Resource.error(
//                                    ApiUtils.getError(
//                                        -1,
//                                        Gson().toJson(response.body()?.appservice)
//                                    )
//                                )
//                        } else {
//                            ApiGeneralResponse<SignupResponseData>.value = Resource.error(
//                                ApiUtils.getError(
//                                    response.code(),
//                                    Gson().toJson(response.errorBody())
//                                )
//                            )
//                        }
//                    }
//
//                    override fun onFailure(
//                        call: Call<ApiGeneralResponse<SignupResponseData>>,
//                        throwable: Throwable
//                    ) {
//                        ApiGeneralResponse<SignupResponseData>.value = Resource.error(ApiUtils.failure(throwable))
//                    }
//                })
//        }
//
//    }
//
    fun setPassword(context: Context) {
        if (isConnectedToInternet(context, true)) {
            setPasswordResponse.value = Resource.loading()
            val credentials: String = passwordSignup.get().toString()
            // create Base64 encodet string
            val basic = Base64.encodeToString(
                credentials.encodeToByteArray(),
                Base64.NO_WRAP
            )
            val setPassRequest = SetPasswordRequest(
                method = WebService.METHOD_SET_PASSWORD,
                service = WebService.CPORTAL_SERVICE,
                parameters = PassParameters(
                    setPassData = SetPassData(
                        email = emailSignup.get().toString(),
                        password = basic,
                        code = code.get().toString()
                    )
                )
            )

            signupWebService.setPassword(setPassRequest)
                .enqueue(object : Callback<ApiGeneralResponse<LoginApiResponse>> {
                    override fun onResponse(
                        call: Call<ApiGeneralResponse<LoginApiResponse>>,
                        response: Response<ApiGeneralResponse<LoginApiResponse>>
                    ) {
                        if (response.isSuccessful) {
                            if (response.body()?.appservice?.type == "success")
                                setPasswordResponse.value = Resource.success(response.body()?.returnData)
                            else
                                setPasswordResponse.value = Resource.error(
                                    ApiUtils.getError(
                                        -1,
                                        Gson().toJson(response.body()?.appservice)
                                    )
                                )

                        } else {
                            setPasswordResponse.value = Resource.error(
                                ApiUtils.getError(
                                    response.code(),
                                    Gson().toJson(response.errorBody())
                                )
                            )
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiGeneralResponse<LoginApiResponse>>,
                        throwable: Throwable
                    ) {
                        setPasswordResponse.value = Resource.error(ApiUtils.failure(throwable))
                    }
                })
        }

    }
//
//
//
//    private fun addSignUpProfile() {
//        addSignUpProfileResponse.value = Resource.loading()
//
////        val vehicleDetailsRequest = VehicleDetailsRequest(
////            vehicleType = vehicleType.get() ?: "",
////            vehicleNumber = vehicleNumber.get() ?: "",
////            vehicleRegistrationNumber = vehicleRegNumber.get() ?: "",
////            vehicleColor = vehicleColor.get() ?: ""
////        )
////
////        vehicleDetails.set(vehicleDetailsRequest)
////
////        signUpProfileRequest.set(
////            SignUpProfileRequest(
////                step = SIGN_UP_PROFILE_STEP,
////                referralUsed = "",
////                socialSecurityNumber = socialSecurityNumber.get() ?: "",
////                licenseImages = licenseImages.get(),
////                vehicleDetails = vehicleDetails.get(),
////                documents = documents.get(),
////                vehicleImages = vehicleImages.get()
////            )
////        )
////
////        webService.addSignUpProfile(signUpProfileRequest.get())
////            .enqueue(object : Callback<ApiResponse<UserData>> {
////                override fun onResponse(
////                    call: Call<ApiResponse<UserData>>,
////                    response: Response<ApiResponse<UserData>>
////                ) {
////                    if (response.isSuccessful) {
////                        addSignUpProfileResponse.value = Resource.success(response.body()?.data)
////                    } else {
////                        addSignUpProfileResponse.value = Resource.error(
////                            ApiUtils.getError(
////                                response.code(),
////                                response.errorBody()?.string()
////                            )
////                        )
////                    }
////                }
////
////                override fun onFailure(call: Call<ApiResponse<UserData>>, throwable: Throwable) {
////                    addSignUpProfileResponse.value = Resource.error(ApiUtils.failure(throwable))
////                }
////            })
//    }
//
//    private fun editProfile() {
//        addSignUpProfileResponse.value = Resource.loading()
//
////        val vehicleDetailsRequest = VehicleDetailsRequest(
////            vehicleType = vehicleType.get() ?: "",
////            vehicleNumber = vehicleNumber.get() ?: "",
////            vehicleRegistrationNumber = vehicleRegNumber.get() ?: "",
////            vehicleColor = vehicleColor.get() ?: ""
////        )
////
////        vehicleDetails.set(vehicleDetailsRequest)
////
////        addEditProfileRequest.set(
////            AddOrEditProfileRequest(
////                name = name.get() ?: "",
////                countryCode = countryCodeWithPlus.get() ?: "",
////                phoneNumber = phoneNumber.get() ?: "",
////                profilePic = profilePicRequest.get(),
////                ISO = ISO.get() ?: "",
////                socialSecurityNumber = socialSecurityNumber.get() ?: "",
////                licenseImages = licenseImages.get(),
////                vehicleDetails = vehicleDetails.get(),
////                vehicleImages = vehicleImages.get()
////            )
////        )
////
////        webService.addOrEditProfileRequest(addEditProfileRequest.get())
////            .enqueue(object : Callback<ApiResponse<UserData>> {
////                override fun onResponse(
////                    call: Call<ApiResponse<UserData>>,
////                    response: Response<ApiResponse<UserData>>
////                ) {
////                    if (response.isSuccessful) {
////                        addSignUpProfileResponse.value = Resource.success(response.body()?.data)
////                    } else {
////                        addSignUpProfileResponse.value = Resource.error(
////                            ApiUtils.getError(
////                                response.code(),
////                                response.errorBody()?.string()
////                            )
////                        )
////                    }
////                }
////
////                override fun onFailure(call: Call<ApiResponse<UserData>>, throwable: Throwable) {
////                    addSignUpProfileResponse.value = Resource.error(ApiUtils.failure(throwable))
////                }
////            })
//    }
//
//    fun getProfile() {
//        addSignUpProfileResponse.value = Resource.loading()
//
////        webService.getProfile()
////            .enqueue(object : Callback<ApiResponse<UserData>> {
////                override fun onResponse(
////                    call: Call<ApiResponse<UserData>>,
////                    response: Response<ApiResponse<UserData>>
////                ) {
////                    if (response.isSuccessful) {
////                        addSignUpProfileResponse.value = Resource.success(response.body()?.data)
////                    } else {
////                        addSignUpProfileResponse.value = Resource.error(
////                            ApiUtils.getError(
////                                response.code(),
////                                response.errorBody()?.string()
////                            )
////                        )
////                    }
////                }
////
////                override fun onFailure(call: Call<ApiResponse<UserData>>, throwable: Throwable) {
////                    addSignUpProfileResponse.value = Resource.error(ApiUtils.failure(throwable))
////                }
////            })
//    }
//
//    private fun useReferralCode() {
//        addSignUpProfileResponse.value = Resource.loading()
//
////        webService.useReferralCode(
////            referralCode.get() ?: "",
////            SIGN_UP_REFERRAL_STEP,
////            termsAccepted = isTermsAccepted.get(),
////            logisticCity = logisticCityId.get() ?: "",
////            logisticCompany = logisticCompanyId.get() ?: ""
////        )
////            .enqueue(object : Callback<ApiResponse<UserData>> {
////                override fun onResponse(
////                    call: Call<ApiResponse<UserData>>,
////                    response: Response<ApiResponse<UserData>>
////                ) {
////                    if (response.isSuccessful) {
////                        addSignUpProfileResponse.value = Resource.success(response.body()?.data)
////                    } else {
////                        addSignUpProfileResponse.value = Resource.error(
////                            ApiUtils.getError(
////                                response.code(),
////                                response.errorBody()?.string()
////                            )
////                        )
////                    }
////                }
////                override fun onFailure(call: Call<ApiResponse<UserData>>, throwable: Throwable) {
////                    addSignUpProfileResponse.value = Resource.error(ApiUtils.failure(throwable))
////                }
////            })
//    }
//
//    fun getTerms(): String {
//        return userRepository.getAppConfig()?.login_terms ?: ""
//    }
//
//    fun getCommonServices() {
////        commonServicesResponse.value = Resource.loading()
//
////        webService.getCommonServices(
////            type = commonServiceType.get() ?: "",
////            country = country.get() ?: "",
////            city = logisticCityId.get() ?: ""
////        )
////            .enqueue(object : Callback<ApiResponse<CommonServiceResponse>> {
////                override fun onResponse(
////                    call: Call<ApiResponse<CommonServiceResponse>>,
////                    response: Response<ApiResponse<CommonServiceResponse>>
////                ) {
////                    if (response.isSuccessful) {
////                        commonServicesResponse.value = Resource.success(response.body()?.data)
////                    } else {
////                        commonServicesResponse.value = Resource.error(
////                            ApiUtils.getError(
////                                response.code(),
////                                response.errorBody()?.string()
////                            )
////                        )
////                    }
////                }
////
////                override fun onFailure(
////                    call: Call<ApiResponse<CommonServiceResponse>>,
////                    throwable: Throwable
////                ) {
////                    commonServicesResponse.value = Resource.error(ApiUtils.failure(throwable))
////                }
////            })
//    }
}