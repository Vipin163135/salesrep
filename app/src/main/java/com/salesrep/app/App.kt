package com.salesrep.app


import android.app.Application
import com.salesrep.app.util.PrefsManager
import com.google.firebase.FirebaseApp
import com.salesrep.app.util.AppLocaleProvider
import dagger.hilt.android.HiltAndroidApp
import dev.b3nedikt.restring.Restring
import dev.b3nedikt.reword.RewordInterceptor
import dev.b3nedikt.viewpump.ViewPump
import timber.log.Timber

@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()

//        if (Config.baseURL != Config.BASE_URL_DEV && Config.baseURL != Config.BASE_URL_LOCAL)
//            Fabric.with(this, Crashlytics())

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        PrefsManager.initialize(applicationContext)
        /* Initialize FirebaseApp */
        FirebaseApp.initializeApp(applicationContext)

        /*Initialize Restring*/
        ViewPump.init(RewordInterceptor)
        Restring.init(applicationContext)
        Restring.localeProvider = AppLocaleProvider
    }


//    @Inject
//    lateinit var workerFactory: HiltWorkerFactory
//
//    override fun getWorkManagerConfiguration(): androidx.work.Configuration {
//        return androidx.work.Configuration.Builder()
//            .setWorkerFactory(workerFactory)
//            .build()
//    }

//    override fun applicationInjector(): AndroidInjector<out DaggerApplication> =
//        DaggerAppComponent.builder().create(this)
}