package com.salesrep.app.ui.auth

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.widget.ContentFrameLayout
import androidx.fragment.app.viewModels
import com.salesrep.app.R
import com.salesrep.app.base.BaseFragment
import com.salesrep.app.data.models.response.TeamTemplate
import com.salesrep.app.databinding.FragmentSelectTeamBinding
import com.salesrep.app.ui.dialogs.ProgressDialog
import com.salesrep.app.util.DataTransferKeys
import com.salesrep.app.util.PrefsManager
import com.salesrep.app.util.openDashBoardPage
import dagger.hilt.android.AndroidEntryPoint
import dev.b3nedikt.reword.Reword
import kotlinx.android.synthetic.main.fragment_select_team.*
import javax.inject.Inject


@AndroidEntryPoint
class TeamSelectFragment : BaseFragment<FragmentSelectTeamBinding>() {

    @Inject
    lateinit var prefsManager: PrefsManager


    private lateinit var binding: FragmentSelectTeamBinding
    override val viewModel by viewModels<AuthViewModel>()
    private lateinit var progressDialog: ProgressDialog
    private val teamList by lazy { arguments?.getParcelableArrayList<TeamTemplate>(DataTransferKeys.KEY_TEAMS) }
    private var selectedPos= -1
    override fun getLayoutResId(): Int {
        return R.layout.fragment_select_team
    }

    override fun onCreateView(
        instance: Bundle?,
        binding: FragmentSelectTeamBinding
    ) {
        this.binding = binding
        progressDialog = ProgressDialog(requireActivity())
        initialize()
        bindObservers()
        setListeners()
    }

    private fun initialize() {
        val rootView =
            requireActivity().window.decorView.findViewById<ContentFrameLayout>(android.R.id.content)
        Reword.reword(rootView)

        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, teamList!!.toList())

        binding.spinner.adapter = spinnerAdapter
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedPos= position
                binding.tvTeamName.text= teamList?.get(position)?.Team?.title
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

    }

    @SuppressLint("FragmentLiveDataObserve")
    private fun bindObservers() {


//        viewModel.successErrorResponse.observe(this, Observer {
//            it ?: return@Observer
//            binding.root.showSnackBar(getString(it))
//        })
    }

    fun setListeners() {

        binding.tvTeamName.setOnClickListener {
            spinner.performClick()
        }

        binding.btnContinue.setOnClickListener {
            prefsManager.save(PrefsManager.TEAM_DATA, teamList?.get(selectedPos))

            openDashBoardPage(requireActivity(),true)
        }

    }



}


