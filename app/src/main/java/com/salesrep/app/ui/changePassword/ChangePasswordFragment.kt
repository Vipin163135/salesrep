package com.salesrep.app.ui.changePassword

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.github.fragivity.navigator
import com.salesrep.app.R
import com.salesrep.app.base.BaseFragment
import com.salesrep.app.data.network.responseUtil.ApisRespHandler
import com.salesrep.app.data.network.responseUtil.Status
import com.salesrep.app.databinding.FragmentChangePasswordBinding
import com.salesrep.app.ui.auth.AuthViewModel
import com.salesrep.app.ui.dialogs.ProgressDialog
import com.salesrep.app.util.PrefsManager
import com.salesrep.app.util.logoutUser
import com.salesrep.app.util.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChangePasswordFragment : BaseFragment<FragmentChangePasswordBinding>() {

    @Inject
    lateinit var prefsManager: PrefsManager

    private lateinit var binding: FragmentChangePasswordBinding
    override val viewModel by viewModels<AuthViewModel>()
    private lateinit var progressDialog: ProgressDialog
    private var showPassword=false
    private var showConfirmPassword=false

    override fun getLayoutResId(): Int {
        return R.layout.fragment_change_password
    }

    override fun onCreateView(
        instance: Bundle?,
        binding: FragmentChangePasswordBinding
    ) {
        this.binding = binding
        this.binding.viewModel = viewModel
        progressDialog = ProgressDialog(requireActivity())
        intialize()
        listeners()
        bindObservers()
    }

    private fun listeners() {

        binding.tvBack.setOnClickListener {
            navigator.popBackStack()
        }

        binding.ivSetPassword.setOnClickListener {

            if (showPassword) {
                val start = binding.etPassword.getSelectionStart()
                val end = binding.etPassword.getSelectionEnd()
                binding.etPassword.setTransformationMethod(PasswordTransformationMethod())
                showPassword=false
                binding.etPassword.setSelection(start, end)
                binding.ivSetPassword.setImageResource(R.drawable.ic_eye1)
            } else {
                val start = binding.etPassword.getSelectionStart()
                val end = binding.etPassword.getSelectionEnd()
                binding.etPassword.setTransformationMethod(null)
                showPassword=true
                binding.etPassword.setSelection(start, end)
                binding.ivSetPassword.setImageResource(R.drawable.ic_eye2)
            }
        }


        binding.ivNewPassword.setOnClickListener {

            if (showConfirmPassword) {
                val start = binding.etNewPassword.getSelectionStart()
                val end = binding.etNewPassword.getSelectionEnd()
                binding.etNewPassword.setTransformationMethod(PasswordTransformationMethod())
                showConfirmPassword=false
                binding.etNewPassword.setSelection(start, end)
                binding.ivNewPassword.setImageResource(R.drawable.ic_eye1)
            } else {
                val start = binding.etNewPassword.getSelectionStart()
                val end = binding.etNewPassword.getSelectionEnd()
                binding.etNewPassword.setTransformationMethod(null)
                showConfirmPassword=true
                binding.etNewPassword.setSelection(start, end)
                binding.ivNewPassword.setImageResource(R.drawable.ic_eye2)
            }
        }

    }

    private fun intialize() {
//        viewModel.getAccountApi(requireContext())
        binding.passwordPolicy.text= getString(R.string.password_policy_terms,
            viewModel.passwordPolicy?.MinLength,
            viewModel.passwordPolicy?.MaxLength,
            viewModel.passwordPolicy?.MinUpperCaseLetters,
            viewModel.passwordPolicy?.MinLowerCaseLetters,
            viewModel.passwordPolicy?.MinNumbers,
            viewModel.passwordPolicy?.MinSymbols,
        )
    }

    @SuppressLint("FragmentLiveDataObserve")
    private fun bindObservers() {

        viewModel.successErrorResponse.observe(this, Observer {
            it ?: return@Observer
            binding.root.showSnackBar(getString(it))
        })

        viewModel.changePasswordApiResponse.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.LOADING -> progressDialog.setLoading(true)
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)
                    if (it.data?.success== true) {
                        logoutUser(requireActivity(), prefsManager)
                    }else{
                        Toast.makeText(requireContext(),it.data?.message, Toast.LENGTH_LONG).show()
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

}