package com.salesrep.app.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.salesrep.app.base.BaseViewModel
import com.salesrep.app.data.models.response.CustomerListModel
import com.salesrep.app.ui.auth.AuthViewModel
import com.salesrep.app.ui.customer.CustomerViewModel
import com.salesrep.app.ui.home.HomeViewModel
import com.salesrep.app.ui.host.HostViewModel
import com.salesrep.app.ui.splash.SplashViewModel

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import javax.inject.Singleton

/**
 * Created by Vipin.
 */

//@Module
//@InstallIn(SingletonComponent::class)
abstract class ViewModelsModule {

//    companion object {
//        @Provides
//        @Singleton
//        @JvmStatic
//        fun viewModelProviderFactory(factory: ViewModelProviderFactory): ViewModelProvider.Factory =
//            factory
//    }
//
//    @Binds
//    @IntoMap
//    @ViewModelKey(SplashViewModel::class)
//    abstract fun bindSplashViewModel(viewModel: SplashViewModel): ViewModel
//
//    @Binds
//    @IntoMap
//    @ViewModelKey(HostViewModel::class)
//    abstract fun bindHostViewModel(viewModel: HostViewModel): ViewModel

}