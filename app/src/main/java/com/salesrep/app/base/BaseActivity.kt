package com.salesrep.app.base

import android.content.Context
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.ViewPumpAppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.salesrep.app.di.ViewModelProviderFactory
import com.salesrep.app.util.AppLocaleProvider
import com.salesrep.app.util.DEFAULT_LANGUAGE
import com.salesrep.app.util.PrefsManager
import com.salesrep.app.util.PrefsManager.Companion.APP_LOCALES
import dagger.android.support.DaggerAppCompatActivity
import dev.b3nedikt.app_locale.AppLocale
import dev.b3nedikt.restring.Restring
import dev.b3nedikt.restring.Restring.wrapContext
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap


abstract class BaseActivity<B : ViewDataBinding> :
    AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: B = DataBindingUtil.setContentView(this, getLayoutResId())
        onCreate(savedInstanceState, binding)
    }

    protected abstract fun getLayoutResId(): Int
    protected abstract fun onCreate(instance: Bundle?,binding: B)
    open val viewModel by viewModels<BaseViewModel>()

    private val appCompatDelegate: AppCompatDelegate by lazy {
        ViewPumpAppCompatDelegate(
            baseDelegate = super.getDelegate(),
            baseContext = this,
            wrapContext = { baseContext -> wrapContext(baseContext) }
        )
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(wrapContext(newBase))
    }

    override fun getDelegate(): AppCompatDelegate {
        return appCompatDelegate
    }

    override fun onBackPressed() {

        val count = supportFragmentManager.backStackEntryCount
        if (count == 0) {
            super.onBackPressed()
        } else {
            val fragment =
                supportFragmentManager.fragments.toList()

            if (fragment.isNotEmpty()) {
                finish()
            } else {
                supportFragmentManager.popBackStack()
            }
        }

    }
}