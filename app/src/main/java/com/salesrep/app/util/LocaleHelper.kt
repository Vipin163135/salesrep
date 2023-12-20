package com.salesrep.app.util

import android.content.Context
import dev.b3nedikt.restring.Restring
import java.util.*


/**
 * Created by Vipin.
 */
object LocaleHelper {

    fun onCreate(context: Context, defaultLanguage: String, prefsManager: PrefsManager) {
        val lang = getPersistedData(defaultLanguage, prefsManager)
        setLocale(context, lang, prefsManager)
    }

    private fun setLocale(context: Context, language: String, prefsManager: PrefsManager) {
        persist(language, prefsManager)
        updateResources(context, language)
    }

    fun getPersistedData(
        defaultLanguage: String,
        prefsManager: PrefsManager
    ): String {
        return prefsManager.getString(PrefsManager.USER_LANGUAGE, defaultLanguage) ?: defaultLanguage
    }

    private fun persist(language: String, prefsManager: PrefsManager) {
        prefsManager.save(language, PrefsManager.USER_LANGUAGE)
    }

    private fun updateResources(context: Context, language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        Restring.locale= locale
        val resources = context.resources
        val configuration = resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }
}