package com.salesrep.app.data.repos

import com.salesrep.app.util.PrefsManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StringRepository @Inject constructor(
        private val prefsManager: PrefsManager
) {
    val appLocales = HashMap<String,String>()

    fun getStringLocale(key: String): String? {
        return appLocales[key]
    }

    fun saveAppLocales(){}

}

