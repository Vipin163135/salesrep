package com.salesrep.app.ui.home

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.app.Notification
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.github.fragivity.loadRoot
import com.github.fragivity.push
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.salesrep.app.R
import com.salesrep.app.base.BaseActivity
import com.salesrep.app.dao.ChangeStatusMovementDao
import com.salesrep.app.dao.CreateMovementDao
import com.salesrep.app.dao.InventoryDao
import com.salesrep.app.data.models.inventory.GetTeamInventoryResponse
import com.salesrep.app.data.models.inventory.MovementCancelData
import com.salesrep.app.data.models.inventory.MovementListData
import com.salesrep.app.data.models.response.GetRouteAccountResponse
import com.salesrep.app.data.models.response.Route
import com.salesrep.app.data.network.responseUtil.ApisRespHandler
import com.salesrep.app.data.network.responseUtil.Status
import com.salesrep.app.data.repos.UserRepository
import com.salesrep.app.databinding.ActivityHomeBinding
import com.salesrep.app.ui.changeLanguage.ChangeLanguageFragment
import com.salesrep.app.ui.changePassword.ChangePasswordFragment
import com.salesrep.app.ui.customer.CustomerListFragment
import com.salesrep.app.ui.dialogs.ProgressDialog
import com.salesrep.app.ui.documents.DocumentsAdapter
import com.salesrep.app.ui.documents.DocumentsFragment
import com.salesrep.app.ui.inventory.InventoryFragment
import com.salesrep.app.ui.inventory.InventoryViewModel
import com.salesrep.app.ui.notifications.NotificationsFragment
import com.salesrep.app.ui.productStock.StockCountFragment
import com.salesrep.app.ui.reconcilation.ValueReconcilationFragment
import com.salesrep.app.ui.route.RouteListFragment
import com.salesrep.app.util.*
import com.salesrep.app.util.PermissionUtils
import dagger.hilt.android.AndroidEntryPoint
import de.hdodenhof.circleimageview.CircleImageView
import permissions.dispatcher.*
import java.io.*
import javax.inject.Inject

@AndroidEntryPoint

class MainActivity : BaseActivity<ActivityHomeBinding>() {
    private var distinationId: Int = 0
    lateinit var binding: ActivityHomeBinding
    override val viewModel by viewModels<InventoryViewModel>()
    private lateinit var filterDialog: BottomSheetDialog
    private lateinit var progressDialog: ProgressDialog

    private var navController: NavController? = null

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var inventoryDao: InventoryDao

    @Inject
    lateinit var createMovementDao: CreateMovementDao

    @Inject
    lateinit var changeStatusMovementDao: ChangeStatusMovementDao


    override fun getLayoutResId(): Int {
        return R.layout.activity_home
    }

    override fun onCreate(
        instance: Bundle?,
        binding: ActivityHomeBinding
    ) {
//        doLocalise()
        this.binding = binding

        progressDialog = ProgressDialog(this)
        intialize()
        listeners()
        observers()
    }

    private fun listeners() {

//        binding.btnBack.setOnClickListener {
//            it?.hideKeyboard()
//            onBackPressed()
//        }

        navController?.addOnDestinationChangedListener { controller, destination, arguments ->
            distinationId = destination.id
            window.decorView.rootView?.hideKeyboard()

            when (distinationId) {

            }
        }
        binding.llLogout.setOnClickListener {
            showlogoutDialog()
        }
        binding.llCustomerList.setOnClickListener {
            navController?.push(CustomerListFragment::class)
            binding.drawerLayout.closeDrawer(binding.drawer)
        }
        binding.llRoutes.setOnClickListener {
            navController?.push(RouteListFragment::class)
            binding.drawerLayout.closeDrawer(binding.drawer)
        }

        binding.llProductMovement.setOnClickListener {
            navController?.push(InventoryFragment::class)
            binding.drawerLayout.closeDrawer(binding.drawer)
        }

        binding.llStockCount.setOnClickListener {
            navController?.push(StockCountFragment::class)
            binding.drawerLayout.closeDrawer(binding.drawer)
        }
        binding.llValueRecon.setOnClickListener {
            navController?.push(ValueReconcilationFragment::class)
            binding.drawerLayout.closeDrawer(binding.drawer)
        }

        binding.llNotification.setOnClickListener {
            navController?.push(NotificationsFragment::class)
            binding.drawerLayout.closeDrawer(binding.drawer)
        }
        binding.llChangePassword.setOnClickListener {
            navController?.push(ChangePasswordFragment::class)
            binding.drawerLayout.closeDrawer(binding.drawer)
        }
        binding.llChangeLang.setOnClickListener {
            navController?.push(ChangeLanguageFragment::class)
            binding.drawerLayout.closeDrawer(binding.drawer)
        }
        binding.llDocuments.setOnClickListener {
            navController?.push(DocumentsFragment::class)
            binding.drawerLayout.closeDrawer(binding.drawer)
        }
        binding.llSupport.setOnClickListener {
            navController?.push(SupportFragment::class)
            binding.drawerLayout.closeDrawer(binding.drawer)
        }
//        binding.tvLanguage.setOnClickListener {
//            navController?.navigate( R.id.languageFragment)
//            binding.drawerLayout.closeDrawer(binding.drawer)
//        }
//        binding.tvChangePassword.setOnClickListener {
//            navController?.navigate( R.id.changePasswordFragment)
//            binding.drawerLayout.closeDrawer(binding.drawer)
//        }

    }

    fun showMenu() {
        binding.drawerLayout.openDrawer(binding.drawer)
    }

    private fun showlogoutDialog() {
        val bottomSheet = layoutInflater.inflate(R.layout.dialog_logout, null)
        val dialog = Dialog(this, R.style.CustomDialog)
        dialog.setContentView(bottomSheet)
        dialog.setCancelable(true)
        dialog.findViewById<AppCompatButton>(R.id.btnClear).setOnClickListener {
            dialog.cancel()
        }
        dialog.findViewById<AppCompatButton>(R.id.btnYes).setOnClickListener {
            dialog.cancel()
            logoutUser(this, prefsManager)
        }
        dialog.findViewById<AppCompatTextView>(R.id.tvName)!!.text =
            userRepository.getUser()?.firstname

        dialog.show()
    }

    private fun intialize() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container_view) as NavHostFragment
        navController = navHostFragment.navController
        binding.drawerLayout.setScrimColor(Color.TRANSPARENT)

        navHostFragment.loadRoot(HomeFragment::class)

        val actionBarDrawerToggle: ActionBarDrawerToggle =
            object :
                ActionBarDrawerToggle(this, binding.drawerLayout, R.string.open, R.string.close) {
                private val scaleFactor = 1f
                override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                    super.onDrawerSlide(drawerView, slideOffset)
                }
            }
//        Log.e("NOTIFY",prefsManager.getObject(USER_DATA, User::class.java)!!.client_preference.alert_dismiss_time!!.toString())
        binding.drawerLayout.setScrimColor(Color.TRANSPARENT)
        binding.drawerLayout.drawerElevation = 0f
        binding.drawerLayout.addDrawerListener(actionBarDrawerToggle)

        if (userRepository.isUserLoggedIn()) {

            viewModel.getTeamInventoryApi(this)
            lifecycleScope.launchWhenCreated {
                val createMovements = createMovementDao.getCreateMovements(false)
                if (!createMovements.isNullOrEmpty()) {
                    viewModel.createMovementApi(
                        baseContext,
                        createMovements as ArrayList<MovementListData>
                    )
                }
                val updateMovements = createMovementDao.getUpdateMovements(true)
                if (!updateMovements.isNullOrEmpty()) {
                    viewModel.updateMovementApi(
                        baseContext,
                        updateMovements as ArrayList<MovementListData>
                    )
                }
                val commitMovements = changeStatusMovementDao.getCommitMovements("Commited")
                if (!commitMovements.isNullOrEmpty()) {
                    viewModel.commitMovementApi(
                        baseContext,
                        commitMovements as ArrayList<MovementCancelData>
                    )
                }
                val cancelMovements = changeStatusMovementDao.getCancelMovements("Cancelled")
                if (!cancelMovements.isNullOrEmpty()) {
                    viewModel.cancelMovementApi(
                        baseContext,
                        cancelMovements as ArrayList<MovementCancelData>
                    )
                }
            }


            when (userRepository.getTeam()?.Team?.lov_team_type) {
                "Presales",
                "Trade" -> {
                    binding.llProductMovement.gone()
                    binding.llStockCount.gone()
//                    binding.llRoutes.gone()
                }
                else -> {
                    binding.llProductMovement.visible()
                    binding.llStockCount.visible()
//                    binding.llRoutes.visible()
                }
            }

            binding.tvName.text=userRepository.getUser()?.email
        }
    }

    private fun observers() {

        viewModel.getTeamInventoryResponse.setObserver(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.LOADING -> {
                }
                Status.SUCCESS -> {

                    prefsManager.save(PrefsManager.TEAM_INVENTORY, it.data)

                    savetoDb(it.data)
                }
                Status.ERROR -> {
                    ApisRespHandler.handleError(
                        it.error,
                        this,
                        prefsManager = prefsManager
                    )
                }
            }
        })

        viewModel.createMovementResponse.setObserver(
            this,
            androidx.lifecycle.Observer {
                it ?: return@Observer
                when (it.status) {
                    Status.LOADING -> {}
                    Status.SUCCESS -> {
                        lifecycleScope.launchWhenCreated {
                            createMovementDao.deleteCreateMovement(false)
                        }
                    }
                    Status.ERROR -> {
                        ApisRespHandler.handleError(
                            it.error,
                            this,
                            prefsManager = prefsManager
                        )
                    }
                }
            })

        viewModel.updateMovementResponse.setObserver(
            this,
            androidx.lifecycle.Observer {
                it ?: return@Observer
                when (it.status) {
                    Status.LOADING -> {}
                    Status.SUCCESS -> {
                        lifecycleScope.launchWhenCreated {
                            createMovementDao.deleteCreateMovement(true)
                        }
                    }
                    Status.ERROR -> {
                        ApisRespHandler.handleError(
                            it.error,
                            this,
                            prefsManager = prefsManager
                        )
                    }
                }
            })

        viewModel.cancelMovementResponse.setObserver(
            this,
            androidx.lifecycle.Observer {
                it ?: return@Observer
                when (it.status) {
                    Status.LOADING -> {}
                    Status.SUCCESS -> {
                        lifecycleScope.launchWhenCreated {
                            changeStatusMovementDao.deleteCancelMovement("Cancelled")
                        }
                    }
                    Status.ERROR -> {
                        ApisRespHandler.handleError(
                            it.error,
                            this,
                            prefsManager = prefsManager
                        )
                    }
                }
            })
        viewModel.commitMovementResponse.setObserver(
            this,
            androidx.lifecycle.Observer {
                it ?: return@Observer
                when (it.status) {
                    Status.LOADING -> {}
                    Status.SUCCESS -> {
                        lifecycleScope.launchWhenCreated {
                            changeStatusMovementDao.deleteCancelMovement("Commited")
                        }
                    }
                    Status.ERROR -> {
                        ApisRespHandler.handleError(
                            it.error,
                            this,
                            prefsManager = prefsManager
                        )
                    }
                }
            })
    }

    private fun savetoDb(data: GetTeamInventoryResponse?) {
        lifecycleScope.launchWhenCreated {
            inventoryDao.clearAll()
            inventoryDao.insert(data)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.e("Main Activity", "onResume: called")
    }

}