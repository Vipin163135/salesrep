package com.salesrep.app.ui.returnOrder

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.salesrep.app.R
import com.salesrep.app.base.BaseDialogFragment
import com.salesrep.app.data.models.response.GetFormCatalogResponse
import com.salesrep.app.data.models.response.RouteStatusReasonsModel
import com.salesrep.app.databinding.DialogRejectionBinding
import com.salesrep.app.ui.home.HomeViewModel
import com.salesrep.app.ui.route.adapters.CancelReasonAdapter
import com.salesrep.app.util.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class ReturnReasonDialog : BaseDialogFragment<DialogRejectionBinding>(),
    CancelReasonAdapter.onReasonSelectListener {

    override val viewModel by viewModels<HomeViewModel>()
    private lateinit var binding: DialogRejectionBinding
    var routeStatusReasons: List<RouteStatusReasonsModel>? = null
    lateinit var adapter: CancelReasonAdapter
    var selectedReason: RouteStatusReasonsModel? = null

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
                        AppRequestCode.ORDER_RETURN_REASON,
                        bundleOf(
                            Pair(DataTransferKeys.KEY_REASON, binding.etReason.text.toString())
                        )
                    )

                    dismiss()
                }

            } else {
                setFragmentResult(
                    AppRequestCode.ORDER_RETURN_REASON,
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
        routeStatusReasons = reasons?.OrderReturnReasons

        binding.rvOrders.layoutManager = LinearLayoutManager(requireContext())
        adapter = CancelReasonAdapter(this)
        binding.rvOrders.adapter = adapter
        adapter.notifyAdapter(routeStatusReasons)

    }

    override fun onReasonSelect(position: Int) {
        routeStatusReasons?.forEachIndexed { index, reason ->
            reason.isSelected = index == position
        }
        selectedReason = routeStatusReasons?.get(position)

        if (routeStatusReasons?.get(position)?.code == "Other") {
            binding.etReason.visible()
        } else {
            binding.etReason.gone()
        }
        adapter.notifyAdapter(routeStatusReasons)
    }
}