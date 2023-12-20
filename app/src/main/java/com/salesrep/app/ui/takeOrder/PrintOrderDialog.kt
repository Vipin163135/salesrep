package com.salesrep.app.ui.takeOrder

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.github.fragivity.navigator
import com.github.fragivity.push
import com.salesrep.app.R
import com.salesrep.app.base.BaseDialogFragment
import com.salesrep.app.data.models.CreatePaymentTemplate
import com.salesrep.app.data.models.response.GetRouteAccountResponse
import com.salesrep.app.databinding.DialogPrintOptionBinding
import com.salesrep.app.ui.home.HomeViewModel
import com.salesrep.app.ui.payment.PrintPaymentCollectionFragment
import com.salesrep.app.util.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PrintOrderDialog : BaseDialogFragment<DialogPrintOptionBinding>(){

    override val viewModel by viewModels<HomeViewModel>()
    private lateinit var binding: DialogPrintOptionBinding

    @Inject
    lateinit var prefsManager: PrefsManager

    private val products by lazy {
        arguments?.getParcelable<GetRouteAccountResponse>(
            DataTransferKeys.KEY_ACCOUNT_DETAIL
        )!!
    }

    private val payments by lazy {
        arguments?.getParcelableArrayList<CreatePaymentTemplate>(
            DataTransferKeys.KEY_PAYMENT_LIST
        )!!
    }


    override fun getLayoutResId(): Int {
        return R.layout.dialog_print_option
    }

    override fun onCreateView(
        instance: Bundle?,
        binding: DialogPrintOptionBinding
    ) {
        this.binding = binding

        listeners()

    }

    private fun listeners() {

        binding.tvOrderDetail.setOnClickListener {
            navigator.push(PrintOrderFragment::class) {
                arguments = bundleOf(
                    Pair(DataTransferKeys.KEY_ACCOUNT_DETAIL, products)
                )
            }
            dismiss()
        }
        binding.tvPaymentDetail.setOnClickListener {
            navigator.push(PrintOrderPaymentFragment::class) {
                arguments = bundleOf(
                    Pair(DataTransferKeys.KEY_ACCOUNT_DETAIL, products),
                    Pair(DataTransferKeys.KEY_PAYMENT_LIST, payments),
                )
            }
            dismiss()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun getTheme(): Int {
        return R.style.CustomDialog
    }

    }