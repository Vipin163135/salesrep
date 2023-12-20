package com.salesrep.app.ui.host

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.salesrep.app.R
import com.salesrep.app.data.network.responseUtil.ApisRespHandler
import com.salesrep.app.data.network.responseUtil.Status
import com.salesrep.app.databinding.ActivityHostBinding
import com.salesrep.app.ui.dialogs.ProgressDialog
import com.salesrep.app.ui.home.HomeViewModel
import com.salesrep.app.ui.splash.SplashActivity

import com.salesrep.app.util.*
import com.salesrep.app.util.PrefsManager.Companion.APP_HOST_URL
import dagger.android.support.DaggerAppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_host.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@AndroidEntryPoint
class HostActivity: AppCompatActivity(), View.OnClickListener{


//    @Inject
//    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var prefsManager: PrefsManager

    private lateinit var binding: ActivityHostBinding
    val viewModel by viewModels<HostViewModel>()
    private lateinit var progressDialog: ProgressDialog

    var arrHosts: ArrayList<String>? = arrayListOf()

    fun getLayoutResId(): Int {
        return R.layout.activity_host
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressDialog = ProgressDialog(this)
        binding = DataBindingUtil.setContentView(this, getLayoutResId())
//        viewModel = ViewModelProviders.of(this, viewModelFactory).get(HostViewModel::class.java)
        bindObservers()
        listeners()
        if (prefsManager.getString(APP_HOST_URL,"").isNullOrEmpty()) {
            viewModel.getHostUrlsApi(this)
        }else{
            startActivity(Intent(this, SplashActivity::class.java))
            finish()
        }


    }

    private fun listeners() {

        binding.btnContinue.setOnClickListener(this)

        binding.etHostUrl.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(charSequence:  CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (!charSequence?.trim().isNullOrEmpty()) {
                    if (!arrHosts.isNullOrEmpty() && arrHosts!!.contains(charSequence?.trim())){
//                        binding.btnContinue.isClickable= true
//                        binding.ivCheck.visible()
                    }else{
//                        binding.btnContinue.isClickable= false
//                        binding.ivCheck.gone()
                    }
                }else{
//                    binding.btnContinue.isClickable= false
//                    binding.ivCheck.gone()
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })
    }

    private fun bindObservers() {
        viewModel.getHostUrlResponse.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.LOADING -> progressDialog.setLoading(true)
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)
                    arrHosts= it.data
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



    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnContinue ->{

                if (TextUtils.isEmpty(binding.etHostUrl.text.toString().trim())){
                    Toast.makeText(this,getString(R.string.please_enter_host),Toast.LENGTH_LONG).show()
                }else if (arrHosts.isNullOrEmpty() || !arrHosts!!.contains(binding.etHostUrl.text.toString().trim())){
                    Timber.d(binding.etHostUrl.text.toString().trim())
                    Toast.makeText(this,getString(R.string.invalid_host),Toast.LENGTH_SHORT).show()
                }else{
                    binding.btnContinue.isClickable= false
//                    binding.ivCheck.gone()
                    prefsManager.save(APP_HOST_URL, "https://${etHostUrl.text.toString()}/")
//                    finishAffinity()
                    startActivity(Intent(this, SplashActivity::class.java))
                    finish()
                }
//                mipedidobat-dev.obelit-sandbox.com
            }
        }
    }

}