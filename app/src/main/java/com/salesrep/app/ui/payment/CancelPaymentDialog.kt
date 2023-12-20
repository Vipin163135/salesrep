package com.salesrep.app.ui.payment

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import com.salesrep.app.R
import com.salesrep.app.base.BaseDialogFragment
import com.salesrep.app.data.models.response.GetFormCatalogResponse
import com.salesrep.app.data.models.response.RouteStatusReasonsModel
import com.salesrep.app.databinding.DialogRejectionBinding
import com.salesrep.app.ui.route.adapters.CancelReasonAdapter
import com.salesrep.app.util.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CancelPaymentDialog : BaseDialogFragment<DialogRejectionBinding>(),
    CancelReasonAdapter.onReasonSelectListener {

    //    override val viewModel by viewModels<HomeViewModel>()
    private lateinit var binding: DialogRejectionBinding
    var removePaymentReasons: List<RouteStatusReasonsModel>? = null
    lateinit var adapter: CancelReasonAdapter
    var selectedReason: RouteStatusReasonsModel? = null

//    private val routeStatus by lazy { arguments?.getString(DataTransferKeys.KEY_STATUS) }
//    private val returnType by lazy { arguments?.getString(DataTransferKeys.KEY_TYPE) }

    @Inject
    lateinit var prefsManager: PrefsManager

    override fun getLayoutResId(): Int {
        return R.layout.dialog_rejection
    }


    override fun onCreateView(
        instance: Bundle?,
        binding: DialogRejectionBinding
    ) {
        this.binding = binding

        initialize()
        listeners()

    }

    private fun listeners() {
        binding.btnApply.setOnClickListener {
            if (selectedReason?.code == "Other") {
                if (ValidationUtils.isFieldNullOrEmpty(binding.etReason.text.toString())) {

                } else {
                    setFragmentResult(
                        AppRequestCode.REMOVE_PAYMENT_OPTION_REQUEST,
                        bundleOf(
                            Pair(DataTransferKeys.KEY_REASON, binding.etReason.text.toString())
                        )
                    )
                    dismiss()
                }

            } else {
                setFragmentResult(
                    AppRequestCode.REMOVE_PAYMENT_OPTION_REQUEST,
                    bundleOf(
                        Pair(DataTransferKeys.KEY_REASON, selectedReason?.code)
                    )
                )

                dismiss()
            }
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun getTheme(): Int {
        return R.style.CustomDialog
    }

    private fun initialize() {
        val reasons = prefsManager.getObject(
            PrefsManager.APP_FORM_CATALOG,
            GetFormCatalogResponse::class.java
        )
//        val reasons= Gson().fromJson(gson,GetFormCatalogResponse::class.java)
        removePaymentReasons = reasons?.PaymentCancellationReasons

        binding.rvOrders.layoutManager = LinearLayoutManager(requireContext())
        adapter = CancelReasonAdapter(this)
        binding.rvOrders.adapter = adapter
        adapter.notifyAdapter(removePaymentReasons)

    }

    override fun onReasonSelect(position: Int) {
        removePaymentReasons?.forEachIndexed { index, reason ->
            reason.isSelected = index == position
        }
        selectedReason = removePaymentReasons?.get(position)

        if (removePaymentReasons?.get(position)?.code == "Other") {
            binding.etReason.visible()
        } else {
            binding.etReason.gone()
        }
        adapter.notifyAdapter(removePaymentReasons)
    }
}