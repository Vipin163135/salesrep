package com.salesrep.app.ui.notifications

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.github.fragivity.navigator
import com.github.fragivity.push
import com.salesrep.app.R
import com.salesrep.app.base.BaseFragment
import com.salesrep.app.data.models.NotificationData
import com.salesrep.app.data.models.response.GetNotificationListResponse
import com.salesrep.app.data.network.responseUtil.ApisRespHandler
import com.salesrep.app.data.network.responseUtil.Status
import com.salesrep.app.databinding.FragmentNotificationsBinding
import com.salesrep.app.ui.dialogs.ProgressDialog
import com.salesrep.app.ui.home.HomeViewModel
import com.salesrep.app.ui.inventory.InventoryFragment
import com.salesrep.app.util.DataTransferKeys
import com.salesrep.app.util.PrefsManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NotificationsFragment : BaseFragment<FragmentNotificationsBinding>(),
    NotificationAdapter.NotificationClick {

    private lateinit var notificationAdapter: NotificationAdapter
    @Inject
    lateinit var prefsManager: PrefsManager

    private lateinit var binding: FragmentNotificationsBinding
    override val viewModel by viewModels<HomeViewModel>()
    private lateinit var progressDialog: ProgressDialog
    private lateinit var notificationlist: List<GetNotificationListResponse>

    override fun getLayoutResId(): Int {
        return R.layout.fragment_notifications
    }

    override fun onCreateView(
        instance: Bundle?,
        binding: FragmentNotificationsBinding
    ) {
        this.binding = binding
        progressDialog = ProgressDialog(requireActivity())
        initialize()
        listeners()
        observers()
    }

    private fun listeners() {
//        binding.etSearch.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//            }
//
//            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                if (p0.isNullOrEmpty()) {
//                    binding.ivCancelSearch.gone()
//                } else {
//                    binding.ivCancelSearch.visible()
//                }
//            }
//
//            override fun afterTextChanged(p0: Editable?) {
//            }
//        })
//
//        binding.ivCancelSearch.setOnClickListener {
//            binding.etSearch.setText("")
//            viewModel.getNotificationApi(requireContext())
//            it?.hideKeyboard()
//        }
        binding.tvBack.setOnClickListener {
            navigator.popBackStack()
        }

//        binding.etSearch.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
//            if (actionId == EditorInfo.IME_ACTION_SEARCH && !TextUtils.isEmpty(binding.etSearch.text)) {
//                viewModel.searchNotificationApi(requireContext(),binding.etSearch.text.toString())
//                v.hideKeyboard()
//                return@OnEditorActionListener true
//            }
//            false
//        })

    }

    @SuppressLint("FragmentLiveDataObserve")
    private fun observers() {

//        viewModel.notificationCount.observe(this, Observer {
//            it ?: return@Observer
//            (requireActivity() as HomeActivity).showNotificationMark(it)
//        })
        viewModel.notificationList.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.LOADING ->
                    progressDialog.setLoading(true)
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    if (it.data != null) {
                        notificationlist= it.data
                        notificationAdapter.addList(notificationlist)
                    }else{
                        notificationlist= arrayListOf()
                        notificationAdapter.addList(notificationlist)
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

    private fun initialize() {
        notificationAdapter= NotificationAdapter(requireContext(),this)
        binding.rvNotifications.adapter= notificationAdapter
    }

    override fun onResume() {
        super.onResume()
        viewModel.getNotificationApi(requireContext())
    }

    override fun onNotificationClick(pos:Int,notificationData: NotificationData) {
//        viewModel.readNotificationApi(requireContext(),notificationData.id!!)
        notificationlist[pos].Notification.read_dt="read"
        notificationAdapter.addList(notificationlist)

        val bundle= Bundle()
        bundle.putString(DataTransferKeys.KEY_NOTIFICATION_DATA,notificationData.message)

//        findNavController().navigate(R.id.webViewFragment,bundle)
        navigator.push(WebViewFragment::class){
            this.arguments = bundle
        }
    }

}