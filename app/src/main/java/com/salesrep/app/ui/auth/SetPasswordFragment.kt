package com.salesrep.app.ui.auth

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.ContentFrameLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.salesrep.app.R
import com.salesrep.app.base.BaseFragment
import com.salesrep.app.data.network.responseUtil.ApisRespHandler
import com.salesrep.app.data.network.responseUtil.Status
import com.salesrep.app.databinding.FragmentResetPasswordBinding
import com.salesrep.app.ui.dialogs.ProgressDialog
import com.salesrep.app.util.DataTransferKeys
import com.salesrep.app.util.PrefsManager
import com.salesrep.app.util.openDashBoardPage
import com.salesrep.app.util.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import dev.b3nedikt.reword.Reword
import javax.inject.Inject

@AndroidEntryPoint
class SetPasswordFragment : BaseFragment<FragmentResetPasswordBinding>() {

    @Inject
    lateinit var prefsManager: PrefsManager

    private lateinit var binding: FragmentResetPasswordBinding
    override val viewModel by viewModels<AuthViewModel>()
    private lateinit var progressDialog: ProgressDialog
    private val email : String? by lazy { arguments?.getString(DataTransferKeys.KEY_EMAIL) }
    private var showPassword=false
    private var showConfirmPassword=false

    override fun getLayoutResId(): Int {
        return R.layout.fragment_reset_password
    }

    override fun onCreateView(
        instance: Bundle?,
        binding: FragmentResetPasswordBinding
    ) {
        this.binding = binding
        this.binding.viewModel = viewModel

        progressDialog = ProgressDialog(requireActivity())
        binding.tvEmail.setText(getString(R.string.code_over_email,email))
        viewModel.emailSignup.set(email)
        bindObservers()
        setListeners()

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
        viewModel.setPasswordResponse.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.LOADING -> progressDialog.setLoading(true)
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)
//                    if(it.data?.success== true) {
                    if (it.data?.passwordErrors?.size ?:0  >0)
                        showAlertMessage(it.data?.passwordErrors)
                    else{
                        prefsManager.save(PrefsManager.USER_DATA, it.data?.Employee)
                        prefsManager.save(PrefsManager.LOGGED_USER_DATA, it.data?.LoggedUser)
                        prefsManager.save(PrefsManager.SESSION_ID, it.data?.SessionId)
                        prefsManager.save(PrefsManager.USER_RESPONSE_DATA, it.data)

                        val bundle = Bundle()
                        bundle.putParcelableArrayList(DataTransferKeys.KEY_TEAMS,it.data?.Teams)
                        (requireActivity() as AuthActivity).updateFragment(DataTransferKeys.SELECT_TEAM_PAGE,bundle)


//                          showSuccessMessage(it.data?.message!!)

                    }
//                    }
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
    }

    private fun showAlertMessage(message: ArrayList<String>?) {
        var msg  = ""
        message?.forEach {it -> msg += it }
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.password_policy)
            .setMessage(msg)
            .setPositiveButton(R.string.ok){ dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
//            .instance.createOkCancelDialog(requireContext(),
//        R.string.signup_successfull,)
    }
    private fun showSuccessMessage(message: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.signup_successfull)
            .setMessage(message)
            .setPositiveButton(R.string.ok){ dialog, _ ->
                dialog.dismiss()
//                openAuthPage(requireContext(),LOGIN_PAGE)
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
