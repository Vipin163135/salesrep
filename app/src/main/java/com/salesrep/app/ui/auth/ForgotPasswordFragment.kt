package com.salesrep.app.ui.auth

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.appcompat.widget.ContentFrameLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.firebase.messaging.FirebaseMessaging
import com.salesrep.app.R
import com.salesrep.app.base.BaseFragment
import com.salesrep.app.data.appConfig.Crmapp
import com.salesrep.app.data.network.responseUtil.ApisRespHandler
import com.salesrep.app.data.network.responseUtil.Status
import com.salesrep.app.databinding.FragmentForgotPasswordBinding
import com.salesrep.app.ui.dialogs.ProgressDialog
import com.salesrep.app.util.DataTransferKeys
import com.salesrep.app.util.PrefsManager
import com.salesrep.app.util.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import dev.b3nedikt.reword.Reword
import javax.inject.Inject

@AndroidEntryPoint
class ForgotPasswordFragment : BaseFragment<FragmentForgotPasswordBinding>() {

    @Inject
    lateinit var prefsManager: PrefsManager

    private lateinit var binding: FragmentForgotPasswordBinding

    override val viewModel by viewModels<AuthViewModel>()

    private lateinit var progressDialog: ProgressDialog
    private val email : String? by lazy { arguments?.getString(DataTransferKeys.KEY_EMAIL) }

    override fun getLayoutResId(): Int {
        return R.layout.fragment_forgot_password
    }


    override fun onCreateView(
        instance: Bundle?,
        binding: FragmentForgotPasswordBinding
    ) {
        this.binding = binding
        this.binding.viewModel = viewModel

        progressDialog = ProgressDialog(requireActivity())
        binding.etEmail.setText(email)
        viewModel.emailSignup.set(email)
        bindObservers()
        setListeners()

//        val crmApp: Crmapp =
//            prefsManager.getObject(PrefsManager.APP_CONFIG, Crmapp::class.java) ?: Crmapp()
//        binding.layoutHeader.tvSubTitle.text= crmApp.header_text ?: getString(R.string.header_text)
    }

    override fun onResume() {
        super.onResume()
        val rootView =
            requireActivity()
                .window
                .decorView
                .findViewById<ContentFrameLayout>(android.R.id.content)

        Reword.reword(rootView)
    }

    @SuppressLint("FragmentLiveDataObserve")
    private fun bindObservers() {
        viewModel.resetPasswordResponse.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.LOADING -> progressDialog.setLoading(true)
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)
                    if(it.data?.success== true) {
                        showSuccessMessage(it.data.message)
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

        viewModel.successErrorResponse.observe(this, Observer {
            it ?: return@Observer
            binding.root.showSnackBar(getString(it))
        })
    }

    private fun showSuccessMessage(message: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.recover_password)
            .setMessage(message)
            .setPositiveButton(R.string.ok){ dialog, _ ->
                val bundle= Bundle()
                bundle.putString(DataTransferKeys.KEY_EMAIL,binding.etEmail.text.toString())
                (requireActivity() as AuthActivity).updateFragment(DataTransferKeys.SET_PASS_PAGE,bundle)
            }
            .setCancelable(false)
            .show()
    }

    fun setListeners() {
        binding.ivBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }
}