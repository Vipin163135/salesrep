package com.salesrep.app.ui.home

import android.content.ClipDescription
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.viewModels
import com.github.fragivity.navigator
import com.github.fragivity.popToRoot
import com.salesrep.app.R
import com.salesrep.app.base.BaseFragment
import com.salesrep.app.data.appConfig.Crmapp
import com.salesrep.app.data.repos.UserRepository
import com.salesrep.app.databinding.FragmentSupportBinding
import com.salesrep.app.ui.dialogs.ProgressDialog
import com.salesrep.app.ui.inventory.InventoryViewModel
import com.salesrep.app.util.PrefsManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class SupportFragment  : BaseFragment<FragmentSupportBinding>() {

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository

    private lateinit var binding: FragmentSupportBinding
    override val viewModel by viewModels<InventoryViewModel>()
    private lateinit var progressDialog: ProgressDialog



    override fun getLayoutResId(): Int {
        return R.layout.fragment_support
    }

    override fun onCreateView(
        instance: Bundle?,
        binding: FragmentSupportBinding
    ) {
        this.binding = binding

        progressDialog = ProgressDialog(requireActivity())
        initialize()

        listeners()

    }

    private fun listeners() {
        binding.tvBack.setOnClickListener {
            navigator.popToRoot()
        }
        binding.tvWhatsapp.setOnClickListener {
            val number= binding.tvWhatsapp.text
            val url = "https://api.whatsapp.com/send?phone=$number&text=Hello"
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }
        binding.tvCall.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:${binding.tvCall.text}")
            startActivity(intent)
        }
        binding.tvMail.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = ClipDescription.MIMETYPE_TEXT_PLAIN
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(binding.tvMail.text))
            intent.putExtra(android.content.Intent.EXTRA_SUBJECT,getString(R.string.salesrep_customer))
            intent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.add_email_description))
            startActivity(Intent.createChooser(intent,getString(R.string.send_email)))
        }
    }


    private fun initialize() {
        var crmApp: Crmapp =
            prefsManager.getObject(PrefsManager.APP_CONFIG, Crmapp::class.java)
                ?: Crmapp()
        binding.tvMail.text= crmApp.contact_email
        binding.tvWhatsapp.text= crmApp.whatsapp
        binding.tvCall.text= crmApp.contact_phone
    }


}