package com.salesrep.app.di

import com.salesrep.app.App
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

/**
 * Created by Vipin.
 */
//
//@Component(modules = [
//    AndroidSupportInjectionModule::class,
//    AppModule::class,
////    BindingsModule::class,
//    ServiceBuilderModule::class,
//    RetrofitClient::class,
//    DatabaseModule::class,
////    NetworkModule::class,
//    ViewModelsModule::class
//])
//
//@Singleton
//interface AppComponent : AndroidInjector<App> {
//
//    @Component.Builder
//    abstract class Builder : AndroidInjector.Builder<App>()
//}