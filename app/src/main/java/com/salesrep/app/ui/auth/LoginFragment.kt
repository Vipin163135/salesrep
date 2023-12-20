package com.salesrep.app.ui.auth

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.appcompat.widget.ContentFrameLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.room.Insert
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.salesrep.app.R
import com.salesrep.app.base.BaseFragment
import com.salesrep.app.dao.RouteActivityDao
import com.salesrep.app.dao.RoutesDao
import com.salesrep.app.dao.SaveOrderDao
import com.salesrep.app.dao.UserTrackDao
import com.salesrep.app.data.network.responseUtil.ApisRespHandler
import com.salesrep.app.data.network.responseUtil.Status
import com.salesrep.app.databinding.FragmentLoginBinding
import com.salesrep.app.ui.dialogs.ProgressDialog
import com.salesrep.app.util.DataTransferKeys
import com.salesrep.app.util.PrefsManager
import com.salesrep.app.util.PrefsManager.Companion.LOGGED_USER_DATA
import com.salesrep.app.util.PrefsManager.Companion.SESSION_ID
import com.salesrep.app.util.PrefsManager.Companion.USER_DATA
import com.salesrep.app.util.PrefsManager.Companion.USER_RESPONSE_DATA
import com.salesrep.app.util.openDashBoardPage
import com.salesrep.app.util.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import dev.b3nedikt.reword.Reword
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : BaseFragment<FragmentLoginBinding>() {

    @Inject
    lateinit var prefsManager: PrefsManager

    private lateinit var binding: FragmentLoginBinding
    override val viewModel by viewModels<AuthViewModel>()
    private lateinit var progressDialog: ProgressDialog
    private var showPassword = false

    @Inject
    lateinit var routeActivityDao: RouteActivityDao
    @Inject
    lateinit var routesDao: RoutesDao
    @Inject
    lateinit var ordersDao: SaveOrderDao
    @Inject
    lateinit var trackDao: UserTrackDao


    override fun getLayoutResId(): Int {
        return R.layout.fragment_login
    }

    override fun onCreateView(
        instance: Bundle?,
        binding: FragmentLoginBinding
    ) {
        this.binding = binding
        this.binding.viewModel = viewModel

        progressDialog = ProgressDialog(requireActivity())
        initialize()
        bindObservers()
        setListeners()
    }

    private fun initialize() {
        val rootView =
            requireActivity().window.decorView.findViewById<ContentFrameLayout>(android.R.id.content)
        Reword.reword(rootView)

        lifecycleScope.launchWhenCreated {
            routesDao.clearAll()
            routeActivityDao.clearAll()
            ordersDao.clearAll()
            trackDao.clearAll()
        }
    }

    @SuppressLint("FragmentLiveDataObserve")
    private fun bindObservers() {
        viewModel.loginResponse.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.LOADING -> progressDialog.setLoading(true)
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)
//                    if (it.data?.LoggedUser?.needToSetPassword == true) {
//                        showSuccessMessage()
//                    } else {
                        prefsManager.save(USER_DATA, it.data?.Employee)
                        prefsManager.save(LOGGED_USER_DATA, it.data?.LoggedUser)
                        prefsManager.save(SESSION_ID, it.data?.SessionId)
                        prefsManager.save(USER_RESPONSE_DATA, it.data)
                    val bundle = Bundle()
                    bundle.putParcelableArrayList(DataTransferKeys.KEY_TEAMS,it.data?.Teams)
                    (requireActivity() as AuthActivity).updateFragment(DataTransferKeys.SELECT_TEAM_PAGE,bundle)
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
    }

    fun setListeners() {

        binding.tvForgotPassword.setOnClickListener {
            (requireActivity() as AuthActivity).updateFragment(DataTransferKeys.RESET_PASS_PAGE)
        }


        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            // Get new FCM registration token
            val token = task.result
            // Log and toast
            viewModel.deviceToken.set(token ?: "")
            //val msg = getString(R.string.msg_token_fmt, token)
//            Log.d("Device Token", deviceToken)
//            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
        })
    }


}


