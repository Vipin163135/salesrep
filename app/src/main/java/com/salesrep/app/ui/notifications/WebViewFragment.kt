package com.salesrep.app.ui.notifications

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.salesrep.app.R
import com.salesrep.app.base.BaseFragment
import com.salesrep.app.databinding.FragmentWebViewBinding
import com.salesrep.app.ui.dialogs.ProgressDialog
import com.salesrep.app.ui.home.HomeViewModel
import com.salesrep.app.util.DataTransferKeys
import com.salesrep.app.util.PrefsManager
import javax.inject.Inject

class WebViewFragment  : BaseFragment<FragmentWebViewBinding>() {

    @Inject
    lateinit var prefsManager: PrefsManager

    private lateinit var binding: FragmentWebViewBinding
    override val viewModel by viewModels<HomeViewModel>()
    private val data by lazy { arguments?.getString(DataTransferKeys.KEY_NOTIFICATION_DATA, "") ?: "" }
    private lateinit var progressDialog: ProgressDialog

    override fun getLayoutResId(): Int {
        return R.layout.fragment_web_view
    }

    override fun onCreateView(
        instance: Bundle?,
        binding: FragmentWebViewBinding
    ) {
        this.binding = binding
        progressDialog = ProgressDialog(requireActivity())
        initialize()

    }

    private fun initialize() {
        binding.webView.loadData(data,"text/html", "UTF-8")
    }

}