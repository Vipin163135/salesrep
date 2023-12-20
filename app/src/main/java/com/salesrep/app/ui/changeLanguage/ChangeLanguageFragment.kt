package com.salesrep.app.ui.changeLanguage

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.fragivity.navigator
import com.google.gson.Gson
import com.salesrep.app.R
import com.salesrep.app.base.BaseFragment
import com.salesrep.app.data.appConfig.AvailableLang
import com.salesrep.app.data.network.responseUtil.ApisRespHandler
import com.salesrep.app.data.network.responseUtil.Status
import com.salesrep.app.data.repos.UserRepository
import com.salesrep.app.databinding.FragmentLanguageBinding
import com.salesrep.app.ui.dialogs.ProgressDialog
import com.salesrep.app.ui.home.HomeViewModel
import com.salesrep.app.ui.splash.SplashViewModel
import com.salesrep.app.util.*
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.b3nedikt.restring.Restring
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@AndroidEntryPoint
class ChangeLanguageFragment : BaseFragment<FragmentLanguageBinding>(),
    ChooseLanguageAdapter.onLanguageSelect {

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var prefsManager: PrefsManager

    private val splashViewModel by viewModels<SplashViewModel>()
    private lateinit var binding: FragmentLanguageBinding

    private lateinit var progressDialog: ProgressDialog
    var adapter: ChooseLanguageAdapter? = null
    var selectedLang: AvailableLang? = null
    var arrLang: ArrayList<AvailableLang>? = arrayListOf()

    override fun getLayoutResId(): Int {
        return R.layout.fragment_language
    }


    override fun onCreateView(
        instance: Bundle?,
        binding: FragmentLanguageBinding
    ) {
        this.binding = binding
        progressDialog = ProgressDialog(requireActivity())
        initialize()
        listeners()
        observers()
    }

    private fun initialize() {
        arrLang = prefsManager.getArrayList(PrefsManager.AVAILABLE_LANGUAGES)

        val language = prefsManager.getString(PrefsManager.USER_LANGUAGE,"en")?:"en"

        arrLang?.mapIndexed { index, availableLang ->
            availableLang.isSelected = language.removeSurrounding("\"") == availableLang.code
        }
        adapter = ChooseLanguageAdapter(this)
        binding.rvlanguages.adapter = adapter
        binding.rvlanguages.layoutManager= LinearLayoutManager(requireContext())
        adapter?.notifyAdapter(arrLang ?: arrayListOf())
    }

    private fun listeners() {

        binding.tvBack.setOnClickListener {
           navigator.popBackStack()
        }

    }

    @SuppressLint("FragmentLiveDataObserve")
    private fun observers() {
        splashViewModel.appLocales.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.LOADING -> progressDialog.setLoading(true)
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)
                    if (it.data!=null) {
                        val jsonString: String  = Gson().toJson(it.data)
                        prefsManager.save(PrefsManager.APP_LOCALES, jsonString)
                        val locale = Locale(selectedLang?.code ?: DEFAULT_LANGUAGE)
                        Timber.d("Locale : $locale")
                        Restring.putStrings(locale, it.data!!)
                        AppLocaleProvider.currentLocale = locale
                        openDashBoardPage(requireContext(), true)
                    }
                }
                Status.ERROR -> {
                    progressDialog.setLoading(false)
                    ApisRespHandler.handleError(
                        it.error,
                        requireActivity(),
                        prefsManager = prefsManager
                    )
                }
            }
        })

    }

    override fun onLanguageSelect(position: Int) {
        if (adapter != null && arrLang != null) {
            val alertDialog = AlertDialogUtil.instance.createOkCancelDialog(
                requireContext(),
                R.string.change_language,
                R.string.change_language_text,
                R.string.yes, R.string.cancel,
                true,
                object : AlertDialogUtil.OnOkCancelDialogListener {
                    override fun onOkButtonClicked() {
                        arrLang?.mapIndexed { index, availableLang ->
                            availableLang.isSelected = index == position
                        }
                        selectedLang = arrLang?.get(position)

                        adapter?.notifyAdapter(arrLang ?: arrayListOf())

                        prefsManager.save(PrefsManager.USER_LANGUAGE, selectedLang?.code)
                        splashViewModel.appLocales(requireContext(),selectedLang?.code?: DEFAULT_LANGUAGE)
                    }


                    override fun onCancelButtonClicked() {
                    }

                })
            alertDialog.show()
        }

    }

}