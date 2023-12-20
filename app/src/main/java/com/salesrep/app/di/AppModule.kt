package com.salesrep.app.di

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
//import com.salesrep.app.pushNotifications.MessagingService
import com.salesrep.app.App
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Created by Vipin.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

//    @Provides
//    @Singleton
//    @JvmStatic
//    fun provideContext(app: App): Context = app.applicationContext


//    @Provides
//    @Singleton
//    @JvmStatic
//    fun provideMessagingService(messagingService: MessagingService): MessagingService = MessagingService()

    @Provides
    @JvmStatic
    fun sharedPreferences(@ApplicationContext context: Context): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    @Provides
    @JvmStatic
    fun provideGson(): Gson {
        return GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
                .setPrettyPrinting()
                .setLenient()
                .create()
    }
}
