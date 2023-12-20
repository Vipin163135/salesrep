package com.salesrep.app.ui.splash

import android.nfc.NfcAdapter
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.salesrep.app.R
import com.salesrep.app.data.appConfig.AvailableLang
import com.salesrep.app.data.network.responseUtil.ApisRespHandler
import com.salesrep.app.data.network.responseUtil.Status
import com.salesrep.app.data.repos.UserRepository
import com.salesrep.app.databinding.ActivitySplashBinding
import com.salesrep.app.ui.dialogs.ProgressDialog
import com.salesrep.app.util.*
//import com.salesrep.app.ui.auth.AccountApprovalActivity
import com.salesrep.app.util.DataTransferKeys.LOGIN_PAGE
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.salesrep.app.data.appConfig.Crmapp
import com.salesrep.app.ui.home.HomeViewModel
//import com.salesrep.app.DataTransferKeys.SIGN_UP_REFERRAL_STEP_PAGE
import dagger.android.support.DaggerAppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import dev.b3nedikt.restring.Restring
import kotlinx.android.synthetic.main.dialog_languages.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@AndroidEntryPoint
class SplashActivity : AppCompatActivity(), View.OnClickListener,
    LanguageAdapter.onLanguageSelect {

//    @Inject
//    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var prefsManager: PrefsManager

    private lateinit var binding: ActivitySplashBinding
    private val viewModel by viewModels<SplashViewModel>()

    private lateinit var progressDialog: ProgressDialog

    var adapter: LanguageAdapter? = null
    var selectedLang: AvailableLang? = null
    var arrLang: ArrayList<AvailableLang>? = arrayListOf()

    fun getLayoutResId(): Int {
        return R.layout.activity_splash
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressDialog = ProgressDialog(this)
        binding = DataBindingUtil.setContentView(this, getLayoutResId())
//        viewModel = ViewModelProviders.of(this, viewModelFactory).get(SplashViewModel::class.java)
        doLocalise()
        bindObservers()
        listeners()
        if (prefsManager.getObject(PrefsManager.APP_CONFIG, Crmapp::class.java) != null) {
            val  crmApp= (prefsManager.getObject(PrefsManager.APP_CONFIG, Crmapp::class.java))
            arrLang= (prefsManager.getArrayList(PrefsManager.AVAILABLE_LANGUAGES ))
//            prefsManager.save(PrefsManager.APP_CONFIG, it.data?.crmapp)
           // prefsManager.saveArrayList(crmApp?.availableLangs, PrefsManager.AVAILABLE_LANGUAGES)

//            arrLang = it.data?.availableLangs ?: arrayListOf()
            Handler().postDelayed({ viewModel.processNextTask() }, SPLASH_TIME_MILLIS)

        } else{
//                   Config.appMode = Config.AppMode.APP
//                    Config.BASE_URL_DEV= it.data?.crmapp?.url
            viewModel.appConfig(this)
    }
    }

    private fun listeners() {
    }

    private fun doLocalise() {
        val language = prefsManager.getString(PrefsManager.USER_LANGUAGE,DEFAULT_LANGUAGE)?:DEFAULT_LANGUAGE
        val locale = Locale(language)
        Timber.d("Locale : $locale")
        val localeString= prefsManager.getString(PrefsManager.APP_LOCALES,"")
        if (!localeString.isNullOrEmpty()) {
            val listType = object : TypeToken<HashMap<String, String>>() {}.type
            val locales = Gson().fromJson<HashMap<String, String>>(localeString, listType)
            if (locales!=null) {
                Restring.putStrings(locale, locales)
                AppLocaleProvider.currentLocale = locale
            }
        }

////        val locale = Locale.getDefault().language
////        LocaleHelper.onCreate(this, locale, PrefsManager.initialize(this))
//        LocaleHelper.onCreate(this, DEFAULT_LANGUAGE, PrefsManager.initialize(this))

    }

    private fun bindObservers() {

        viewModel.appConfigResponse.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    prefsManager.save(PrefsManager.APP_CONFIG, it.data?.crmapp)
                    prefsManager.saveArrayList(it.data?.availableLangs,PrefsManager.AVAILABLE_LANGUAGES)

                    arrLang = it.data?.availableLangs ?: arrayListOf()

//                   Config.appMode = Config.AppMode.APP
//                    Config.BASE_URL_DEV= it.data?.crmapp?.url

                    Handler().postDelayed({ viewModel.processNextTask() }, SPLASH_TIME_MILLIS)
                }

                Status.ERROR -> {
                    ApisRespHandler.handleError(
                        it.error,
                        this,
                        prefsManager = prefsManager
                    )
                }
            }
        })


        viewModel.loginRequestCommand.observe(this, Observer {
//            doLocalise()
            selectLangDialog()
        })


        viewModel.appRequestCommand.observe(this, Observer {
//            if (userRepository.getUser()?.isAdminVerified == true)
                openDashBoardPage(this, refreshApp = true)
            finish()
        })

        viewModel.appLocales.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.LOADING -> progressDialog.setLoading(true)
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)
                    val jsonString: String  = Gson().toJson(it.data)
                    prefsManager.save(PrefsManager.APP_LOCALES, jsonString)
                    val locale= Locale(selectedLang?.code?: DEFAULT_LANGUAGE)
//                    Timber.d("Locale : $locale")
                    Log.e( "Locale: ", "$locale")
                    if (it.data!=null) {
                        Restring.putStrings(locale, it.data!!)
                        AppLocaleProvider.currentLocale = locale
                    }
                    openAuthPage(this, LOGIN_PAGE)
                    finish()
                }
                Status.ERROR -> {
                    progressDialog.setLoading(false)
                    ApisRespHandler.handleError(
                        it.error,
                        this,
                        prefsManager = prefsManager
                    )
                }
            }
        })
    }

    private fun selectLangDialog() {
        val bottomSheet = layoutInflater.inflate(R.layout.dialog_languages, null)
        val dialog = BottomSheetDialog(this, R.style.SheetDialog)
        dialog.setContentView(bottomSheet)
        dialog.setCanceledOnTouchOutside(false)
        val  btnGetStarted=  dialog.findViewById<AppCompatButton>(R.id.btnGetStarted)
        btnGetStarted?.setOnClickListener{
            if (selectedLang!=null){
                dialog.dismiss()
                prefsManager.save(PrefsManager.USER_LANGUAGE, selectedLang?.code)
                viewModel.appLocales(this,selectedLang?.code?: DEFAULT_LANGUAGE)
            }else{
                Toast.makeText(this,getString(R.string.please_select_language),Toast.LENGTH_SHORT).show()
            }
        }
        val rvLang = dialog.findViewById<RecyclerView>(R.id.rvlanguages)
        rvLang?.layoutManager = GridLayoutManager(this, 2)
        adapter = LanguageAdapter(this)
        rvLang?.adapter = adapter
        adapter?.notifyAdapter(arrLang ?: arrayListOf())
        dialog.show()
    }

    override fun onLanguageSelect(position: Int) {
        if (adapter != null && arrLang != null) {
            arrLang?.mapIndexed { index, availableLang ->
                availableLang.isSelected = index == position
            }
            selectedLang = arrLang?.get(position)

            adapter?.notifyAdapter(arrLang ?: arrayListOf())
        }
    }

    override fun onClick(v: View?) {
    //        when (v?.id) {
//            R.id.btnGetStarted ->
//        }
    }

}