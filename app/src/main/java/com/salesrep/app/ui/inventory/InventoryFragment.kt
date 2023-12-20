package com.salesrep.app.ui.inventory

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.github.fragivity.navigator
import com.github.fragivity.push
import com.salesrep.app.R
import com.salesrep.app.base.BaseFragment
import com.salesrep.app.dao.InventoryDao
import com.salesrep.app.data.models.ProductTemplate
import com.salesrep.app.data.models.inventory.GetTeamInventoryResponse
import com.salesrep.app.data.models.inventory.InvBinProductsData
import com.salesrep.app.data.models.inventory.InvMovementProductData
import com.salesrep.app.data.models.inventory.InvMovementTemplate
import com.salesrep.app.data.network.responseUtil.ApisRespHandler
import com.salesrep.app.data.network.responseUtil.Status
import com.salesrep.app.data.repos.UserRepository
import com.salesrep.app.databinding.FragmentInventoryBinding
import com.salesrep.app.ui.dialogs.ProgressDialog
import com.salesrep.app.ui.inventory.adapters.MovementsAdapter
import com.salesrep.app.util.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class InventoryFragment : BaseFragment<FragmentInventoryBinding>(){

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var inventoryDao: InventoryDao


    override val viewModel by viewModels<InventoryViewModel>()
    private lateinit var binding: FragmentInventoryBinding

    private var movementList: ArrayList<InvMovementTemplate>? =null

    private lateinit var progressDialog: ProgressDialog

    private lateinit var adapter: MovementsAdapter

    override fun getLayoutResId(): Int {
        return R.layout.fragment_inventory
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setFragmentResultListener(AppRequestCode.UPDATE_MOVEMENT_LIST_REQUEST) { key, bundle ->
            initialize()
        }
    }

    override fun onCreateView(instance: Bundle?, binding: FragmentInventoryBinding) {
        this.binding = binding

        progressDialog = ProgressDialog(requireActivity())

        adapter= MovementsAdapter(requireContext(),::OnClickItem)
        binding.rvInventory.adapter= adapter

        movementList= arrayListOf()

        initialize()
        listeners()
        bindObservers()

        when (userRepository.getTeam()?.Team?.lov_team_type){
            "Delivery",
            "Trade" ->{
            }
            else ->{
            }
        }

    }

    private fun listeners() {
        binding.tvBack.setOnClickListener {
            navigator.popBackStack()
        }

        binding.tvAddInventory.setOnClickListener {
            navigator.push(ProductMovementFragment::class) {
                this.arguments = bundleOf(
                    Pair(DataTransferKeys.KEY_IS_NEW,true)
                )
            }
        }
    }


    private fun bindObservers() {
        viewModel.getTeamInventoryResponse.setObserver(viewLifecycleOwner,  Observer {
            it ?: return@Observer
            when (it.status) {
                Status.LOADING -> {
                    progressDialog.setLoading(true)
                }
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    prefsManager.save(PrefsManager.TEAM_INVENTORY, it.data)

                    savetoDb(it.data)
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

    private fun savetoDb(inventory: GetTeamInventoryResponse?) {
        lifecycleScope.launchWhenCreated {
            inventoryDao.clearAll()
            inventory?.id= inventoryDao.insert(inventory)

            if (inventory != null) {
                adapter.notifyData(inventory.Movements)
            }
        }
    }

    private fun initialize() {

        if (!isConnectedToInternet(requireContext(),false)) {
            lifecycleScope.launchWhenCreated {
                val inventory = inventoryDao.getAllInventory().lastOrNull()
                if (inventory != null) {
                    adapter.notifyData(inventory.Movements)
                }
            }
        }else {
            viewModel.getTeamInventoryApi(requireContext())
        }
    }

    private fun OnClickItem(movementData: InvMovementTemplate) {
        navigator.push(ProductMovementFragment::class) {
            this.arguments = bundleOf(
                Pair(DataTransferKeys.KEY_IS_NEW,false),
                Pair(DataTransferKeys.KEY_MOVEMENT_DATA, movementData)
            )
        }
    }


}