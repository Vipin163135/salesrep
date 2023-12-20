package com.salesrep.app.ui.auth

import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import com.salesrep.app.R
import com.salesrep.app.base.BaseActivity
import com.salesrep.app.databinding.ActivityContainerBinding
import com.salesrep.app.ui.customer.CustomerViewModel
import com.salesrep.app.util.DataTransferKeys
import com.salesrep.app.util.DataTransferKeys.LOGIN_PAGE
import com.salesrep.app.util.DataTransferKeys.PAGE_TO_OPEN
import com.salesrep.app.util.DataTransferKeys.RESET_PASS_PAGE
import com.salesrep.app.util.DataTransferKeys.SELECT_TEAM_PAGE
import com.salesrep.app.util.DataTransferKeys.SET_PASS_PAGE
import com.salesrep.app.util.DataTransferKeys.SIGNUP_PAGE
import com.salesrep.app.util.addFragment
import com.salesrep.app.util.replaceFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity : BaseActivity< ActivityContainerBinding>() {

    private lateinit var binding: ActivityContainerBinding
    override val viewModel by viewModels<AuthViewModel>()

    private val pageToOpen: String by lazy {
        intent.getStringExtra(PAGE_TO_OPEN).toString()
    }

    override fun getLayoutResId(): Int {
        return R.layout.activity_container
    }

    override fun onCreate(
        instance: Bundle?,
        binding: ActivityContainerBinding
    ) {
        this.binding = binding

        bindViews()
    }

    private fun bindViews() {
        updateFragment(pageToOpen)
    }

    fun updateFragment(pageToOpen: String, bundle: Bundle? = null) {
        when (pageToOpen) {
            LOGIN_PAGE -> addFragment(
                supportFragmentManager,
                LoginFragment(),
                R.id.container
            )
            SELECT_TEAM_PAGE -> {


                val fragment = TeamSelectFragment()
                fragment.arguments = bundle
                replaceFragment(
                    supportFragmentManager,
                    fragment,
                    R.id.container
                )
            }
            SET_PASS_PAGE -> {
                val fragment = SetPasswordFragment()
                fragment.arguments = bundle
                replaceFragment(
                    supportFragmentManager,
                    fragment,
                    R.id.container
                )
            }
            RESET_PASS_PAGE -> {
                val fragment = ForgotPasswordFragment()
                fragment.arguments = bundle
                replaceFragment(
                    supportFragmentManager,
                    fragment,
                    R.id.container
                )
            }
//            FORGOT_PASSWORD_PAGE -> replaceFragment(
//                supportFragmentManager,
//                ForgotPasswordFragment(),
//                R.id.container
//            )

        }
    }
}