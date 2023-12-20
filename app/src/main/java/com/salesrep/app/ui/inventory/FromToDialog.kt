package com.salesrep.app.ui.inventory

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.salesrep.app.R
import com.salesrep.app.base.BaseDialogFragment
import com.salesrep.app.data.models.inventory.InventoryTemplate
import com.salesrep.app.data.models.response.AvailableManualTaskTemplate
import com.salesrep.app.data.repos.UserRepository
import com.salesrep.app.databinding.DialogSelectFromToBinding
import com.salesrep.app.ui.home.HomeViewModel
import com.salesrep.app.ui.inventory.adapters.FromToAdapter
import com.salesrep.app.util.AppRequestCode
import com.salesrep.app.util.BitmapFromVector
import com.salesrep.app.util.DataTransferKeys
import javax.inject.Inject

class FromToDialog(): BaseDialogFragment<DialogSelectFromToBinding>() {
    override val viewModel by viewModels<HomeViewModel>()
    private lateinit var binding: DialogSelectFromToBinding
    private val invLocList by lazy { arguments?.getParcelableArrayList<InventoryTemplate>(DataTransferKeys.KEY_MOVEMENT_DATA) }
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
//        invLocList=

        if (!invLocList.isNullOrEmpty()){
            val adapter=  FromToAdapter(requireContext(),::OnItemClick)
            binding.rvFromTo.adapter=adapter
            adapter.notifyData(invLocList)
        }
    }

    private fun OnItemClick(inventoryTemplate: InventoryTemplate){
        setFragmentResult(AppRequestCode.SELECT_MOVEMENT_TYPE_REQUEST,
            bundleOf(
                Pair(DataTransferKeys.SELECT_MOVEMENT_TYPE,inventoryTemplate)
            ))
        this.dismiss()
    }

}