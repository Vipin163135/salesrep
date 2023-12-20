package com.salesrep.app.data.network


class Config {

    companion object {
        var BASE_URL = "https://mipedidobat-dev.obelit-sandbox.com/"
        var BASE_URL_DEV = "https://mipedidobat-dev.obelit-sandbox.com/"
        var SANDBOX_URL = "https://obelit.sonnarcrm.net/"
        val BASE_URL_GOOGLE_MAPS = "https://maps.googleapis.com/maps/api/"

//        var appMode = AppMode.SITE
//
//
//        val baseURL: String
//            get() {
//                init(appMode)
//                return BASE_URL
//            }
//
//        val siteURL: String
//            get() {
//                return SANDBOX_URL
//            }

//        private fun init(appMode: AppMode) {
//
//            when (appMode) {
//                AppMode.SITE -> {
//                    BASE_URL = SANDBOX_URL
//                }
//                AppMode.APP -> {
//                    BASE_URL = BASE_URL_DEV
//                }
//
//            }
//        }
//        enum class AppMode {
//            SITE, APP
//        }
    }
}