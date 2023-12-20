package com.salesrep.app.util

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.StringDef
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.salesrep.app.data.appConfig.AvailableLang
import java.lang.reflect.Type
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.ArrayList

@Singleton
class PrefsManager @Inject constructor(
    private val preferences: SharedPreferences,
    private val gson: Gson
) {
    private val sharedPrefName = "ArabLife"

    companion object {
        const val PREF_PROFILE = "PREF_PROFILE"
        const val PREF_API_TOKEN = "PREF_API_TOKEN"
        const val NOTIFICATION_SOUND = "notification_sound_enabled"
        const val KEY_CHANGE_FLAG = "is_notification_channel_needed"
        const val USER_LANGUAGE = "USER_LANGUAGE"
        const val USER_DATA = "USER_DATA"
        const val TEAM_DATA = "TEAM_DATA"
        const val TEAM_ROUTES = "TEAM_ROUTES"
        const val TEAM_PRODUCTS = "TEAM_PRODUCTS"
        const val TEAM_PRICELIST = "TEAM_PRICELIST"
        const val TEAM_INVENTORY = "TEAM_INVENTORY"

        const val USER_RESPONSE_DATA = "USER_RESPONSE_DATA"
        const val LOGGED_USER_DATA = "LOGGED_USER_DATA"
        const val SESSION_ID = "SESSION_ID"
        const val APP_CONFIG = "APP_CONFIGURATIONS"
        const val APP_LOCALES = "APP_LOCALES"
        const val APP_HOST_URL = "APP_HOST_URL"
        const val APP_FORM_CATALOG = "APP_FORM_CATALOG"
        const val AVAILABLE_LANGUAGES = "AVAILABLE_LANGUAGES"
        const val PASSWORD_POLICY = "PASSWORD_POLICY"
        const val SECRET_PASSWORD_KEY = "SECRET_PASSWORD_KEY"

        @StringDef(PREF_PROFILE, PREF_API_TOKEN)
        @Retention(AnnotationRetention.SOURCE)
        annotation class PrefKey

        private lateinit var instance: PrefsManager
        private val isInitialized =
            AtomicBoolean()     // To check if instance was previously initialized or not
        fun initialize(context: Context): PrefsManager {
            if (!isInitialized.getAndSet(true)) {
                instance =
                    PrefsManager(preferences = PreferenceManager.getDefaultSharedPreferences(context),gson = Gson())
            }
            return instance
        }

        fun get(): PrefsManager = instance
    }

    fun save(@PrefKey key: String, value: String) {
        preferences.edit().putString(key, value).apply()
    }

    fun save(@PrefKey key: String, value: Int) {
        preferences.edit().putInt(key, value).apply()
    }

    fun save(@PrefKey key: String, value: Boolean) {
        preferences.edit().putBoolean(key, value).apply()
    }

    fun save(@PrefKey key: String, `object`: Any?) {
        if (`object` == null) {
            throw IllegalArgumentException("Object is null")
        }

        // Convert the provided object to JSON string
        save(key, gson.toJson(`object`))
    }

    fun getString(@PrefKey key: String, defValue: String): String? =
        preferences.getString(key, defValue)

    fun getInt(@PrefKey key: String, defValue: Int): Int = preferences.getInt(key, defValue)

    fun getFloat(@PrefKey key: String, defValue: Float): Float = preferences.getFloat(key, defValue)

    fun getBoolean(@PrefKey key: String, defValue: Boolean): Boolean =
        preferences.getBoolean(key, defValue)

    fun <T> getObject(@PrefKey key: String, objectClass: Class<T>): T? {
        val jsonString = preferences.getString(key, null)
        return if (jsonString == null) {
            null
        } else {
            try {
                gson.fromJson(jsonString, objectClass)
            } catch (e: Exception) {
                throw IllegalArgumentException("Object stored with key $key is instance of other class")
            }
        }
    }

    fun saveArrayList(list: ArrayList<AvailableLang>?, key: String?) {
        val gson = Gson()
        val json: String = gson.toJson(list)
        preferences.edit().putString(key, json).apply()
    }

    fun getArrayList(key: String?): ArrayList<AvailableLang>? {
        val json: String? = preferences.getString(key, null)
        val type: Type = object : TypeToken<ArrayList<AvailableLang>?>() {}.getType()
        return gson.fromJson(json, type)
    }

//    fun getMap(key: String): HashMap<String,String> {
//
//    }

//    fun removeAll() {
//        preferences.edit().clear().apply()
//    }

    fun remove(key: String) {
        preferences.edit().remove(key).apply()
    }

    /**
     * Store boolean in preference.
     *
     * @param key   the key
     * @param value the value
     */

    fun storeBooleanInPreference(key: String, value: Boolean) {
        save(key, value)
    }

    /**
     * Gets the boolean from preference.
     *
     * @param key the key
     * @return boolean Value from preference
     */

    fun getBooleanFromPreference(key: String): Boolean {
        return getBoolean(key, false)
    }

    /**
     * Gets the int from preference.
     *
     * @param key the key
     * @return String Value from preference
     */

    fun getIntFromPreference(key: String): Int {
        return getInt(key, 0)
    }


    /**
     * Clear all preference.
     */
    fun clearAllPreference() {
        clearAllPreference()
    }

    /**
     * Store string in preference.
     *
     * @param key   the key
     * @param value the value
     */
    fun storeStringInPreference(key: String, value: String?) {
        save(key, value)
    }

    /**
     * Store int in preference.
     *
     * @param key   the key
     * @param value the value
     */
    fun storeIntInPreference(key: String, value: Int) {
       save(key, value)
    }

    fun getBooleanFromPreferenceDefaultTrueValue(key: String): Boolean {
        return getBoolean(key, true)
    }

}
