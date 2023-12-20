package com.salesrep.app.ui.inventory

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.salesrep.app.R
import com.salesrep.app.base.BaseDialogFragment
import com.salesrep.app.data.models.inventory.InvBinTemplate
import com.salesrep.app.data.models.response.AvailableManualTaskTemplate
import com.salesrep.app.data.repos.UserRepository
import com.salesrep.app.databinding.DialogSelectFromToBinding
import com.salesrep.app.ui.home.HomeViewModel
import com.salesrep.app.ui.inventory.adapters.BinAdapter
import com.salesrep.app.ui.inventory.adapters.FromToAdapter
import com.salesrep.app.util.AppRequestCode
import com.salesrep.app.util.DataTransferKeys
import javax.inject.Inject

class SelectBinDialog : BaseDialogFragment<DialogSelectFromToBinding>() {

    private lateinit var binding: DialogSelectFromToBinding
    private val invBinList by lazy { arguments?.getParcelableArrayList<InvBinTemplate>(
        DataTransferKeys.KEY_MOVEMENT_DATA) }
    var selectedActivity: AvailableManualTaskTemplate?= null

    @Inject
    lateinit var userRepository: UserRepository

    override fun getLayoutResId(): Int {
        return R.layout.dialog_select_from_to
    }

    override fun onCreateView(
        instance: Bundle?,
        binding: DialogSelectFromToBinding
    ) {
        this.binding = binding

        initialize()
        listeners()

    }

    private fun listeners() {
//        binding.btnSubmit.setOnClickListener {
//            if (selectedActivity!=null)
//                setFragmentResult(
//                    AppRequestCode.ADD_NEW_TASK_REQUEST,
//                    bundleOf(
//                        Pair(DataTransferKeys.ADD_NEW_TASK_RESULT,selectedActivity),
//                        Pair(DataTransferKeys.KEY_REASON,binding.etDescription.text.toString()?: "")
//                    )
//                )
//
//            dismiss()
//        }
//
//        binding.tvActivity.setOnClickListener {
//            binding.spinner.performClick()
//        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun getTheme(): Int {
        return  R.style.CustomDialog
    }

    private fun initialize() {
//        invBinList=

        if (!invBinList.isNullOrEmpty()){
            val adapter=  BinAdapter(requireContext(),::OnItemClick)
            binding.rvFromTo.adapter=adapter
            adapter.notifyData(invBinList)
        }
    }

    private fun OnItemClick(InvBinTemplate: InvBinTemplate){
        setFragmentResult(
            AppRequestCode.SELECT_MOVEMENT_BIN_REQUEST,
            bundleOf(
                Pair(DataTransferKeys.SELECT_MOVEMENT_BIN,InvBinTemplate)
            )
        )
        this.dismiss()
    }

}