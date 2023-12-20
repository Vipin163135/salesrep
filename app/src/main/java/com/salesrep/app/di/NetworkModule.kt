package com.salesrep.app.di

/**
 * Created by Vipin.
 */
//@Module
object NetworkModule {

//    @Provides
//    @Singleton
//    @JvmStatic
//    fun okHttpClient(prefsManager: PrefsManager): OkHttpClient {
//        return OkHttpClient.Builder()
//                .connectTimeout(100, TimeUnit.SECONDS)
//                .readTimeout(100, TimeUnit.SECONDS)
//                .writeTimeout(100, TimeUnit.SECONDS)
//                .addInterceptor(getHttpLoggingInterceptor())
//                .addInterceptor(getNetworkInterceptor(prefsManager))
//                .build()
//    }

//    @Provides
//    @Singleton
//    @JvmStatic
//    fun retrofit(client: OkHttpClient, gson: Gson): Retrofit {
//        return Retrofit.Builder()
//                .baseUrl(Config.baseURL)
//                .client(client)
//                .addConverterFactory(GsonConverterFactory.create(gson))
//                .addConverterFactory(ScalarsConverterFactory.create())
//                .addCallAdapterFactory(CoroutineCallAdapterFactory())
//                .build()
//    }

//    private fun getNetworkInterceptor(prefsManager: PrefsManager): Interceptor {
//        return Interceptor { chain ->
//            var request = chain.request()
//
//            val app_config = prefsManager.getObject(APP_CONFIG, Crmapp::class.java)
//            var language = prefsManager.getString(USER_LANGUAGE,"en")?:"en"
//            var session_id: String = prefsManager.getString(SESSION_ID, "") ?: ""
//            session_id= session_id.removeSurrounding("\"")
//            language= language.removeSurrounding("\"")
//
//            Timber.d("Session id : $session_id")
//            Timber.d("User Lang : $language")
//
//            request = if (session_id.isNotEmpty()) {
//                        request.newBuilder().addHeader("Content-Type", "application/json")
//                                .addHeader("dynApp", "SonnarCRM")
//                            .addHeader("lang",language)
////                                .addHeader("SONNARCRMSESSIONID", "5b5b762d2b8992ff8e47d7723e081953").build()
//                                .addHeader("SONNARCRMSESSIONID", session_id).build()
//
//                    } else {
//                        request.newBuilder().addHeader("Content-Type", "application/json")
//                                .addHeader("dynApp", "SonnarCRM").build()
//                    }
//
//            chain.proceed(request)
//        }
//    }
//
//    private fun getHttpLoggingInterceptor(): HttpLoggingInterceptor {
//        val httpLoggingInterceptor = HttpLoggingInterceptor()
//        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
//        return httpLoggingInterceptor
//    }
//
//    @Provides
//    @Singleton
//    @JvmStatic
//    fun webService(retrofit: Retrofit): WebService = retrofit.create(WebService::class.java)


}