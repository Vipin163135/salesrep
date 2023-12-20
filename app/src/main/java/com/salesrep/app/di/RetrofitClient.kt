package com.salesrep.app.di


import com.salesrep.app.data.apis.WebService
import com.salesrep.app.data.appConfig.Crmapp

import com.google.gson.GsonBuilder
import com.salesrep.app.data.apis.HostWebService
import com.salesrep.app.data.apis.SignupWebService
import com.salesrep.app.data.network.Config
import com.salesrep.app.util.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
object RetrofitClient {

    @Provides
    @JvmStatic
    fun webService(): WebService = RETROFIT.create(WebService::class.java)

    @Provides
    @JvmStatic
    fun webServiceSignup(): SignupWebService = SIGNUP_RETROFIT.create(SignupWebService::class.java)

    @Provides
    @JvmStatic
    fun webServiceHost(): HostWebService = HOST_RETROFIT.create(HostWebService::class.java)

    private val LOGGING_INTERCEPTOR by lazy {
        HttpLoggingInterceptor().setLevel(
                HttpLoggingInterceptor.Level.BODY
        )
    }

    private val NETWORK_INTERCEPTOR by lazy {
//        Log.e("accessToken", UserManager.getAccessToken())
        Interceptor  { chain ->
            var request = chain.request()
            val prefsManager= PrefsManager.get()
            val app_config = prefsManager.getObject(PrefsManager.APP_CONFIG, Crmapp::class.java)
            var language = prefsManager.getString(PrefsManager.USER_LANGUAGE,"en")?:"en"
            var session_id: String = prefsManager.getString(PrefsManager.SESSION_ID, "") ?: ""
            session_id= session_id.removeSurrounding("\"")
            language= language.removeSurrounding("\"")

            Timber.d("Session id : $session_id")
            Timber.d("User Lang : $language")

            request = if (session_id.isNotEmpty()) {
                request.newBuilder().addHeader("Content-Type", "application/json")
                    .addHeader("dynApp", "SonnarCRM")
                    .addHeader("lang",language)
//                                .addHeader("SONNARCRMSESSIONID", "5b5b762d2b8992ff8e47d7723e081953").build()
                    .addHeader("SONNARCRMSESSIONID", session_id).build()
            } else {
                request.newBuilder().addHeader("Content-Type", "application/json")
                    .addHeader("dynApp", "SonnarCRM").build()
            }
            chain.proceed(request)
        }
    }

    private val OK_HTTP_CLIENT by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(LOGGING_INTERCEPTOR)
            .addNetworkInterceptor(NETWORK_INTERCEPTOR)
            .build()
    }

    private var gson = GsonBuilder()
        .setLenient()
        .create()

    private val RETROFIT by lazy {
//        println("TOKEN -- " + UserManager.getAccessToken())
        val prefsManager= PrefsManager.get()
        Config.BASE_URL = prefsManager.getString(PrefsManager.APP_HOST_URL,Config.BASE_URL)?:Config.BASE_URL
        Retrofit.Builder()
            .baseUrl(Config.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(OK_HTTP_CLIENT)
            .build()
    }

    private val OK_HTTP_CLIENT_SIGNUP by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(LOGGING_INTERCEPTOR)
            .addNetworkInterceptor(NETWORK_INTERCEPTOR)
            .addInterceptor(BasicAuthInterceptor(BASIC_AUTH_USERNAME, BASIC_AUTH_PASSWORD))
            .build()
    }

    private val OK_HTTP_CLIENT_HOST by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(LOGGING_INTERCEPTOR)
            .addNetworkInterceptor(NETWORK_INTERCEPTOR)
            .addInterceptor(BasicAuthInterceptor(BASIC_AUTH_HOST_USERNAME, BASIC_AUTH_HOST_PASSWORD))
            .build()
    }

    private val SIGNUP_RETROFIT by lazy {
        val prefsManager= PrefsManager.get()
        Config.BASE_URL = prefsManager.getString(PrefsManager.APP_HOST_URL,Config.BASE_URL)?:Config.BASE_URL
        Retrofit.Builder()
            .baseUrl(Config.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(OK_HTTP_CLIENT_SIGNUP)
            .build()
    }

    private val HOST_RETROFIT by lazy {
        Retrofit.Builder()
            .baseUrl(Config.SANDBOX_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(OK_HTTP_CLIENT_HOST)
            .build()
    }

}