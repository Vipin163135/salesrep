package com.salesrep.app.ui.route

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.salesrep.app.R
import com.salesrep.app.base.BaseDialogFragment
import com.salesrep.app.data.models.response.AvailableManualTaskTemplate
import com.salesrep.app.data.models.response.GetFormCatalogResponse
import com.salesrep.app.data.models.response.RouteStatusReasonsModel
import com.salesrep.app.data.models.response.TeamTemplate
import com.salesrep.app.data.repos.UserRepository
import com.salesrep.app.databinding.DialogCreateActivityBinding
import com.salesrep.app.databinding.DialogRejectionBinding
import com.salesrep.app.ui.home.HomeViewModel
import com.salesrep.app.ui.route.adapters.CancelReasonAdapter
import com.salesrep.app.util.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_select_team.*
import javax.inject.Inject

@AndroidEntryPoint
class AddActivityDialog(): BaseDialogFragment<DialogCreateActivityBinding>(){

    override val viewModel by viewModels<HomeViewModel>()
    private lateinit var binding: DialogCreateActivityBinding
    private var activityList : ArrayList<AvailableManualTaskTemplate>?= null
    private var selectedPos= -1
    var selectedActivity: AvailableManualTaskTemplate?= null

    @Inject
    lateinit var userRepository: UserRepository

    override fun getLayoutResId(): Int {
        return R.layout.dialog_create_activity
    }

    override fun onCreateView(
        instance: Bundle?,
        binding: DialogCreateActivityBinding
    ) {
        this.binding = binding

        initialize()
        listeners()

    }

    private fun listeners() {
        binding.btnSubmit.setOnClickListener {
            if (selectedActivity!=null)
            setFragmentResult(
                AppRequestCode.ADD_NEW_TASK_REQUEST,
                bundleOf(
                    Pair(DataTransferKeys.ADD_NEW_TASK_RESULT,selectedActivity),
                    Pair(DataTransferKeys.KEY_REASON,binding.etDescription.text.toString()?: "")
                )
            )

            dismiss()

        }

        binding.tvActivity.setOnClickListener {
            binding.spinner.performClick()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun getTheme(): Int {
        return  R.style.CustomDialog
    }

    private fun initialize() {
        activityList= userRepository.getTeam()?.AvailableManualTasks

        if (!activityList.isNullOrEmpty()){

            val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, activityList!!.toList())

            binding.spinner.adapter = spinnerAdapter
            binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedPos= position
                    binding.tvActivity.text= activityList?.get(position)?.ActivityplanTemplate?.title
                    selectedActivity= activityList?.get(position)
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }

    }

}