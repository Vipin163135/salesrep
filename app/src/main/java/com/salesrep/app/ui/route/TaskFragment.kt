package com.salesrep.app.ui.route

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.github.fragivity.dialog.showDialog
import com.github.fragivity.navigator
import com.github.fragivity.pop
import com.github.fragivity.push
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.salesrep.app.R
import com.salesrep.app.base.BaseFragment
import com.salesrep.app.dao.RouteActivityDao
import com.salesrep.app.dao.UpdateActivityDao
import com.salesrep.app.data.models.ActivityTemplate
import com.salesrep.app.data.models.ProductTemplate
import com.salesrep.app.data.models.requests.*
import com.salesrep.app.data.models.requests.Attachment
import com.salesrep.app.data.models.response.*
import com.salesrep.app.data.network.responseUtil.ApisRespHandler
import com.salesrep.app.data.network.responseUtil.Status
import com.salesrep.app.data.repos.UserRepository
import com.salesrep.app.databinding.FragmentTasksBinding
import com.salesrep.app.ui.customer.add_customer.DialogTimePickerFragment
import com.salesrep.app.ui.customer.adapter.VisitDaysAdapter
import com.salesrep.app.ui.dialogs.ProgressDialog
import com.salesrep.app.ui.facingCheck.FacingCheckFragment
import com.salesrep.app.ui.home.HomeViewModel
import com.salesrep.app.ui.payment.PaymentCollectionFragment
import com.salesrep.app.ui.returnOrder.ReturnOrderListFragment
import com.salesrep.app.ui.route.adapters.TasksAdapter
import com.salesrep.app.ui.stock.RouteStockFragment
import com.salesrep.app.ui.survey.RouteSurveyFragment
import com.salesrep.app.ui.takeOrder.TakeOrderListFragment
import com.salesrep.app.util.*
import com.salesrep.app.util.DataTransferKeys.KEY_ATTACHMENTS
import com.salesrep.app.util.DataTransferKeys.KEY_NAME
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.lang.reflect.Type
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@AndroidEntryPoint
class TaskFragment : BaseFragment<FragmentTasksBinding>() {

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var updateActivityDao: UpdateActivityDao

    @Inject
    lateinit var routeActivityDao: RouteActivityDao

    private lateinit var binding: FragmentTasksBinding
    override val viewModel by activityViewModels<HomeViewModel>()
    private lateinit var progressDialog: ProgressDialog
    private lateinit var taskAdapter: TasksAdapter
    private val task by lazy {
        arguments?.getParcelable<GetRouteAccountResponse>(
            DataTransferKeys.KEY_TASKS
        )
    }
    private val taskStatus by lazy { arguments?.getString(DataTransferKeys.KEY_STATUS) }
    private val jumpFrom by lazy { arguments?.getString(DataTransferKeys.KEY_FROM) }
    private val routeStatus by lazy { arguments?.getString(DataTransferKeys.KEY_ROUTE_STATUS) }
    private var taskList: ArrayList<TaskData>? = null
    private var days: List<StatusModel> = arrayListOf()
    private var spinnerDays: VisitDaysAdapter? = null
    private var selectedItem: TaskData? = null
    private var selectedPos: Int = -1

    private var isRouteEditable: Int = -1

    override fun getLayoutResId(): Int {
        return R.layout.fragment_tasks
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(AppRequestCode.SELECT_TIME_REQUEST) { key, bundle ->
            if (bundle.get(DialogTimePickerFragment.ARGUMENT_TIME) != null) {
                binding.etPreferredTime.text =
                    bundle.getString(DialogTimePickerFragment.ARGUMENT_TIME)
            }
        }
        setFragmentResultListener(AppRequestCode.SELECT_PRODUCT_LIST_REQUEST) { key, bundle ->
            if (bundle.get(DataTransferKeys.KEY_PRODUCTS) != null) {
                val gson = bundle.getString(DataTransferKeys.KEY_PRODUCTS)
                if (!gson.isNullOrEmpty()) {
                    val listType: Type =
                        object : TypeToken<ArrayList<UpdateProductData?>?>() {}.type
                    val products =
                        Gson().fromJson<ArrayList<UpdateProductData>>(gson, listType)
                    selectedItem?.Activity?.lov_activity_status =
                        RouteStatus.STATUS_COMPLETED
                    selectedItem?.Activity?.actual_enddate =
                        DateUtils.getCurrentDateWithFormat(DateFormat.DATE_FORMAT_RENEW)
                    setCurrentTaskStatus(selectedPos, selectedItem, productList = products)

                }
            }
        }

        setFragmentResultListener(AppRequestCode.CURRENT_ACTIVITY_STATUS_CHANGED) { key, bundle ->
            if (bundle.get(DataTransferKeys.KEY_CANCEL_STATUS) != null) {
                val status = bundle.getString(DataTransferKeys.KEY_CANCEL_STATUS, "")
                val reason = bundle.getString(DataTransferKeys.KEY_REASON)
                task?.Visit?.Activity?.lov_activity_status = RouteStatus.STATUS_SKIPPED
                onActivityStatusClick(task?.Visit?.Activity, reason)

            }
        }

        setFragmentResultListener(AppRequestCode.ADD_NEW_TASK_REQUEST) { key, bundle ->
            if (bundle.get(DataTransferKeys.ADD_NEW_TASK_RESULT) != null) {
                val newTask =
                    bundle.getParcelable<AvailableManualTaskTemplate>(DataTransferKeys.ADD_NEW_TASK_RESULT)
                val description = bundle.getString(DataTransferKeys.KEY_REASON)
                var isPresent = false

                taskList?.forEach {
                    if (it.Activity?.title == newTask?.ActivityplanTemplate?.title) {
                        isPresent = true
                    }
                }
                if (!isPresent) {
                    val activityTemplate = ActivityTemplate(
                        account_id = task?.Account?.id,
                        actual_duration = null,
                        actual_enddate = null,
                        actual_startdate = null,
                        description = description,
                        id = null,
                        integration_id = Calendar.getInstance().timeInMillis.toString(),
                        lov_activity_type = newTask?.ActivityplanTemplate?.lov_activityplantemp_type,
                        lov_activity_reason = null,
                        longitude = task?.Visit?.Activity?.longitude,
                        latitude = task?.Visit?.Activity?.latitude,
                        required_flg = true,
                        lov_activity_status = RouteStatus.STATUS_PENDING,
                        name = newTask?.ActivityplanTemplate?.title ?: "",
                        route_id = task?.Visit?.Activity?.route_id,
                        team_id = task?.Account?.team_id,
                        title = newTask?.ActivityplanTemplate?.title ?: ""
                    )
                    addNewTask(activityTemplate)
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.task_already_present),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        setFragmentResultListener(AppRequestCode.CURRENT_TASK_STATUS_CHANGED) { key, bundle ->
            if (bundle.get(DataTransferKeys.KEY_CANCEL_STATUS) != null) {
                val status = bundle.getString(DataTransferKeys.KEY_CANCEL_STATUS, "")
                val reason = bundle.getString(DataTransferKeys.KEY_REASON)
                if (selectedItem != null && selectedPos > -1) {
                    selectedItem?.Activity?.lov_activity_status = RouteStatus.STATUS_SKIPPED
                    setCurrentTaskStatus(selectedPos, selectedItem, reason)
                }
            }
        }

        setFragmentResultListener(AppRequestCode.CURRENT_TASK_SURVEY_STATUS_COMPLETED) { key, bundle ->
            if (selectedItem != null && selectedPos > -1) {
                selectedItem?.Activity?.lov_activity_status =
                    RouteStatus.STATUS_COMPLETED
                selectedItem?.Activity?.actual_enddate =
                    DateUtils.getCurrentDateWithFormat(DateFormat.DATE_FORMAT_RENEW)
                setCurrentTaskStatus(selectedPos, selectedItem)
            }
        }

        setFragmentResultListener(AppRequestCode.CURRENT_TASK_SURVEY_STATUS_CHANGED) { key, bundle ->
            if (selectedItem != null && selectedPos > -1) {
//                    selectedItem?.lov_activity_status =
//                        RouteStatus.STATUS_IN_PROGRESS
//                    selectedItem?.actual_enddate =
//                        DateUtils.getCurrentDateWithFormat(DateFormat.DATE_FORMAT_RENEW)
                setCurrentTaskStatus(selectedPos, selectedItem, isChangeInStatus = false)
            }
        }

        setFragmentResultListener(AppRequestCode.CURRENT_TASK_ORDER_STATUS_CHANGED) { key, bundle ->
            if (selectedItem != null && selectedPos > -1) {
                selectedItem?.Activity?.lov_activity_status =
                    RouteStatus.STATUS_COMPLETED
                selectedItem?.Activity?.actual_enddate =
                    DateUtils.getCurrentDateWithFormat(DateFormat.DATE_FORMAT_RENEW)
                setCurrentTaskStatus(selectedPos, selectedItem)
            }
        }

        setFragmentResultListener(AppRequestCode.CURRENT_TASK_FACECHECKING_STATUS_CHANGED) { key, bundle ->
            if (selectedItem != null && selectedPos > -1) {
                val attachment = bundle.getParcelableArrayList<Attachment>(KEY_ATTACHMENTS)
                val description = bundle.getString(KEY_NAME, "")
                selectedItem?.Activity?.lov_activity_status =
                    RouteStatus.STATUS_COMPLETED
                selectedItem?.Activity?.actual_enddate =
                    DateUtils.getCurrentDateWithFormat(DateFormat.DATE_FORMAT_RENEW)
                setCurrentTaskStatus(
                    selectedPos,
                    selectedItem,
                    attachment = attachment,
                    description = description
                )
            }
        }
    }

    override fun onCreateView(
        instance: Bundle?,
        binding: FragmentTasksBinding
    ) {
        this.binding = binding

        progressDialog = ProgressDialog(requireActivity())
        initialize()
        bindObservers()
        listeners()
    }

    @SuppressLint("LogNotTimber")
    private fun listeners() {
        binding.ivBack.setOnClickListener {
            navigator.pop()
        }
        binding.etPreferredDay.setOnClickListener {
            binding.spinner.performClick()
        }
        binding.tvAddNew.setOnClickListener {
            if (task?.Visit?.Activity?.lov_activity_status == RouteStatus.STATUS_IN_PROGRESS) {
                navigator.showDialog(
                    AddActivityDialog::class,
                    bundleOf()
                )
            }
        }

        binding.etPreferredTime.setOnClickListener {
            navigator.showDialog(
                DialogTimePickerFragment::class,
                bundleOf(
                    Pair(
                        DialogTimePickerFragment.ARGUMENT_TIME,
                        binding.etPreferredTime.text.toString()
                    )
                )
            )
        }


        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                binding.etPreferredDay.text = days[position].value
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        binding.ivNav.setOnClickListener {
//            setFragmentResult(
//                AppRequestCode.SELECT_CUSTOMER_NAV_REQUEST,
//                bundleOf(
//                    Pair(DataTransferKeys.KEY_CUSTOMER_NAV,task))
//            )
//            navigator.pop()
            if (task?.DeliveryAddress != null) {
                val lat = task?.DeliveryAddress?.latitude
                val lng = task?.DeliveryAddress?.longitude
                val title = task?.Account?.accountname
                openGoogleMaps(requireContext(), lat!!, lng!!, title!!)
            }
        }

        binding.tvStart.setOnClickListener {
            Log.e("CurrentStatus: ", "${task?.Visit?.Activity?.lov_activity_status}")
            if (isRouteEditable == 1) {
                showAlertDialog(
                    requireContext(),
                    R.string.message_alert,
                    R.string.start_route_first
                )
            } else if (isRouteEditable == 0) {

            } else {
                when (task?.Visit?.Activity?.lov_activity_status) {
                    RouteStatus.STATUS_PENDING, RouteStatus.STATUS_SKIPPED -> {
                        task?.Visit?.Activity?.lov_activity_status =
                            RouteStatus.STATUS_IN_PROGRESS
                        task?.Visit?.Activity?.actual_startdate =
                            DateUtils.getCurrentDateWithFormat(DateFormat.DATE_FORMAT_RENEW)
                        onActivityStatusClick(task?.Visit?.Activity)
                    }
                    else -> {
                    }
                }
            }

//            else {
//                showAlertDialog(
//                    requireContext(),
//                    R.string.message_alert,
//                    R.string.start_route_first
//                )
//            }

        }
        binding.tvSkip.setOnClickListener {
            Log.e("CurrentStatus: ", "${task?.Visit?.Activity?.lov_activity_status}")
            if (isRouteEditable == 1) {
                showAlertDialog(
                    requireContext(),
                    R.string.message_alert,
                    R.string.start_route_first
                )
            } else if (isRouteEditable == 0) {

            } else {

                when (task?.Visit?.Activity?.lov_activity_status) {
                    RouteStatus.STATUS_PENDING -> {
                        navigator.showDialog(
                            CancelRouteDialog::class,
                            bundleOf(
                                Pair(DataTransferKeys.KEY_STATUS, RouteStatus.STATUS_SKIPPED),
                                Pair(DataTransferKeys.KEY_TYPE, "Activity")
                            )
                        )
                    }
                    RouteStatus.STATUS_SKIPPED -> {
                        task?.Visit?.Activity?.lov_activity_status =
                            RouteStatus.STATUS_NOT_STARTED
                        onActivityStatusClick(task?.Visit?.Activity)
                    }
                    else -> {

                    }
                }
            }

//            else {
//                showAlertDialog(
//                    requireContext(),
//                    R.string.message_alert,
//                    R.string.start_route_first
//                )
//            }
        }
        binding.tvEnd.setOnClickListener {
            if (isRouteEditable == 1) {
                showAlertDialog(
                    requireContext(),
                    R.string.message_alert,
                    R.string.start_route_first
                )
            } else if (isRouteEditable == 0) {

            } else {
                when (task?.Visit?.Activity?.lov_activity_status) {
                    RouteStatus.STATUS_IN_PROGRESS -> {
                        var isTaskRequired = false
                        task?.Visit?.Tasks?.forEach {
                            if (it.Activity?.required_flg == true && ((it.Activity?.lov_activity_status == RouteStatus.STATUS_PENDING) || (it.Activity?.lov_activity_status == RouteStatus.STATUS_IN_PROGRESS))) {
                                isTaskRequired = true
                            }
                        }
                        if (!isTaskRequired) {
                            task?.Visit?.Activity?.lov_activity_status =
                                RouteStatus.STATUS_COMPLETED
                            task?.Visit?.Activity?.actual_enddate =
                                DateUtils.getCurrentDateWithFormat(DateFormat.DATE_FORMAT_RENEW)
                            onActivityStatusClick(task?.Visit?.Activity)
                        } else {
                            showAlertDialog(
                                requireContext(),
                                R.string.message_alert,
                                R.string.complete_task_first
                            )
                        }
                    }
//                    RouteStatus.STATUS_COMPLETED,RouteStatus.STATUS_SKIPPED,RouteStatus.STATUS_NOT_STARTED->{
//                        task?.Visit?.Activity?.lov_activity_status =
//                            RouteStatus.STATUS_PENDING
//                        task?.Visit?.Activity?.actual_startdate =
//                            DateUtils.getCurrentDateWithFormat(DateFormat.DATE_FORMAT_RENEW)
//                        onActivityStatusClick(task?.Visit?.Activity)
//                    }
                    else -> {
                    }
                }
            }
//            else {
//                showAlertDialog(
//                    requireContext(),
//                    R.string.message_alert,
//                    R.string.complete_task_first
//                )
//            }
        }
        binding.tvUpdate.setOnClickListener {
            when {
                ValidationUtils.isFieldNullOrEmpty(binding.etPreferredDay.text.toString()) -> {
                    binding.root.showSnackBar(getString(R.string.error_day_empty))
                }
                ValidationUtils.isFieldNullOrEmpty(binding.etPreferredTime.text.toString()) -> {
                    binding.root.showSnackBar(getString(R.string.error_time_empty))
                }
                else -> {

                    val customerData = AddCustomerData(
                        id = task?.Account?.id,
                        preferred_visit_day = binding.etPreferredDay.text.toString(),
                        preferred_visit_time = binding.etPreferredTime.text.toString()
                    )

                    val addCustomerRequest = arrayListOf<AddCustomerRequest>()
                    addCustomerRequest.add(
                        AddCustomerRequest(
                            Account = customerData,
                        )
                    )
                    viewModel.updateCustomerApi(
                        requireContext(),
                        AddBulkCustomerRequest(addCustomerRequest)
                    )
                }
            }
        }
    }

    private fun initialize() {
        binding.tvTitle.text = task?.Account?.accountname
        val isEditable = taskStatus == RouteStatus.STATUS_IN_PROGRESS
        isRouteEditable = when (routeStatus) {
            RouteStatus.STATUS_COMPLETED, RouteStatus.STATUS_SKIPPED, RouteStatus.STATUS_CANCELLED -> 0
            RouteStatus.STATUS_PENDING, RouteStatus.STATUS_NOT_STARTED -> 1
            else -> -1
        }
        taskAdapter = TasksAdapter(
            requireContext(),
            isEditable,
            ::onTaskStatusClick,
            ::onTaskClick,
            isRouteEditable
        )
        binding.rvTasks.adapter = taskAdapter
        taskList = task?.Visit?.Tasks
        taskAdapter.notifyData(taskList)
        setStatus()

        val formCatalog = prefsManager.getObject(
            PrefsManager.APP_FORM_CATALOG,
            GetFormCatalogResponse::class.java
        )
        days = formCatalog!!.VisitDays

        spinnerDays = VisitDaysAdapter(
            requireContext(),
            R.layout.item_field_selection_item,
            binding.etPreferredDay.id,
            days
        )
        binding.spinner.adapter = spinnerDays

        binding.etPreferredDay.text = task?.Account?.delivery_day_name

        if (jumpFrom == "home" || jumpFrom == "track") {
            binding.ivNav.visible()
        } else
            binding.ivNav.gone()

    }

    private fun setStatus() {
        when (task?.Visit?.Activity?.lov_activity_status) {
            RouteStatus.STATUS_IN_PROGRESS -> {
                binding.tvEnd.visible()
                binding.tvSkip.gone()
                binding.tvStart.gone()
                binding.tvAddNew.visible()
            }
            RouteStatus.STATUS_COMPLETED, RouteStatus.STATUS_NOT_STARTED, RouteStatus.STATUS_CANCELLED -> {
                binding.tvEnd.visible()
                binding.tvEnd.text = getString(R.string.ended)
                binding.tvSkip.gone()
                binding.tvStart.gone()
                binding.tvAddNew.gone()
            }
            RouteStatus.STATUS_SKIPPED -> {
                binding.tvSkip.visible()
                binding.tvStart.visible()
                binding.tvEnd.gone()
                binding.tvStart.text = getString(R.string.start)
                binding.tvSkip.text = getString(R.string.not_started)
                binding.tvAddNew.gone()
            }
            else -> {
                binding.tvStart.text = getString(R.string.start)
                binding.tvSkip.text = getString(R.string.skip)
                binding.tvEnd.gone()
                binding.tvSkip.visible()
                binding.tvStart.visible()
                binding.tvAddNew.gone()
            }
        }
    }

    @SuppressLint("FragmentLiveDataObserve")
    private fun bindObservers() {

        viewModel.updateRouteApiResponse.setObserver(viewLifecycleOwner, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.LOADING -> {
                    progressDialog.setLoading(true)
                }
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)
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

        viewModel.updateAccountsResponse.setObserver(viewLifecycleOwner, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.LOADING -> progressDialog.setLoading(true)
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.update_successfully),
                        Toast.LENGTH_LONG
                    ).show()
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

    private fun onActivityStatusClick(account: ActivityTemplate?, reason: String? = null) {
        task?.Visit?.Activity?.lov_activity_status = account?.lov_activity_status ?: ""

        viewModel.currentRoute.value?.find {
            it.Account?.id.toString() == task?.Account?.id.toString()
        }.let {
            it?.Visit?.Activity?.lov_activity_status = account?.lov_activity_status ?: ""
            lifecycleScope.launchWhenCreated {
                routeActivityDao.insert(it)
            }
        }

        val isEditable =
            task?.Visit?.Activity?.lov_activity_status == RouteStatus.STATUS_IN_PROGRESS
        taskAdapter.notifyEditable(isEditable)

        if (jumpFrom == "home") {
            setFragmentResult(
                AppRequestCode.CURRENT_ROUTE_STATUS_CHANGED,
                bundleOf(
                    Pair(DataTransferKeys.KEY_ROUTE_STATUS, account?.lov_activity_status)
                )
            )
        } else if (jumpFrom == "track") {
            setFragmentResult(
                AppRequestCode.CURRENT_ROUTE_TRACK_STATUS_CHANGED,
                bundleOf(
                    Pair(DataTransferKeys.KEY_ROUTE_STATUS, account?.lov_activity_status)
                )
            )
        } else {
            setFragmentResult(
                AppRequestCode.CURRENT_ROUTE_ACTIVITY_STATUS_CHANGED,
                bundleOf(
                    Pair(DataTransferKeys.KEY_ROUTE_STATUS, account?.lov_activity_status)
                )
            )
        }
        updateActivityStatus(account?.lov_activity_status ?: "", task?.Visit?.Activity, reason)
        setStatus()
    }

    private fun onTaskStatusClick(pos: Int, account: TaskData?) {
        selectedItem = account
        selectedPos = pos
        if (account?.Activity?.lov_activity_status == RouteStatus.STATUS_SKIPPED) {
            navigator.showDialog(
                CancelRouteDialog::class,
                bundleOf(
                    Pair(DataTransferKeys.KEY_STATUS, RouteStatus.STATUS_SKIPPED),
                    Pair(DataTransferKeys.KEY_TYPE, "Task")
                )
            )
        } else {
            setCurrentTaskStatus(pos, account)
        }
    }

    private fun onTaskClick(pos: Int, taskData: TaskData?) {
        if (taskData?.Activity?.lov_activity_status == RouteStatus.STATUS_IN_PROGRESS
            || taskData?.Activity?.lov_activity_status == RouteStatus.STATUS_COMPLETED
        ) {
            if (taskData.Activity!!.title.lowercase().contains("stock")) {
                selectedItem = taskData
                selectedPos = pos
                val bundle = Bundle()
                bundle.putParcelable(DataTransferKeys.KEY_PRODUCTS, task)
                bundle.putParcelable(DataTransferKeys.KEY_TASK_DATA, taskData)
                if (taskData.Activity!!.lov_activity_status == RouteStatus.STATUS_COMPLETED) {
                    bundle.putBoolean(DataTransferKeys.KEY_IS_COMPLETED, true)
                }
                navigator.push(RouteStockFragment::class) {
                    this.arguments = bundle
                }
            } else if (taskData.Activity!!.title.lowercase().contains("survey")) {
                selectedItem = taskData
                selectedPos = pos
                val bundle = Bundle()
                bundle.putParcelable(DataTransferKeys.KEY_PRODUCTS, task)
                bundle.putParcelable(DataTransferKeys.KEY_TASK_DATA, taskData)
                if (taskData.Activity!!.lov_activity_status == RouteStatus.STATUS_COMPLETED) {
                    bundle.putBoolean(DataTransferKeys.KEY_IS_COMPLETED, true)
                }
                navigator.push(RouteSurveyFragment::class) {
                    this.arguments = bundle
                }
            } else if (taskData.Activity!!.title.lowercase().contains("take")) {
                selectedItem = taskData
                selectedPos = pos
                val bundle = Bundle()
                bundle.putParcelable(DataTransferKeys.KEY_PRODUCTS, task)
                if (taskData.Activity!!.lov_activity_status == RouteStatus.STATUS_COMPLETED) {
                    bundle.putBoolean(DataTransferKeys.KEY_IS_COMPLETED, true)
                }
                navigator.push(TakeOrderListFragment::class) {
                    this.arguments = bundle
                }
            } else if (taskData.Activity!!.title.lowercase().contains("return")) {
                selectedItem = taskData
                selectedPos = pos
                val bundle = Bundle()
                bundle.putParcelable(DataTransferKeys.KEY_PRODUCTS, task)
                if (taskData.Activity!!.lov_activity_status == RouteStatus.STATUS_COMPLETED) {
                    bundle.putBoolean(DataTransferKeys.KEY_IS_COMPLETED, true)
                }
                navigator.push(ReturnOrderListFragment::class) {
                    this.arguments = bundle
                }
            } else if (taskData.Activity!!.title.lowercase().contains("delivery")) {
                selectedItem = taskData
                selectedPos = pos
                val bundle = Bundle()
                bundle.putParcelable(DataTransferKeys.KEY_PRODUCTS, task)
                if (taskData.Activity!!.lov_activity_status == RouteStatus.STATUS_COMPLETED) {
                    bundle.putBoolean(DataTransferKeys.KEY_IS_COMPLETED, true)
                }
                navigator.push(TakeOrderListFragment::class) {
                    this.arguments = bundle
                }
            } else if (taskData.Activity!!.title.lowercase().contains("facing")) {
                selectedItem = taskData
                selectedPos = pos

                val bundle = Bundle()
                bundle.putParcelable(DataTransferKeys.KEY_PRODUCTS, task)
                bundle.putParcelable(DataTransferKeys.KEY_TASK_DATA, taskData)
                if (taskData.Activity!!.lov_activity_status == RouteStatus.STATUS_COMPLETED) {
                    bundle.putBoolean(DataTransferKeys.KEY_IS_COMPLETED, true)
                }
                navigator.push(FacingCheckFragment::class) {
                    this.arguments = bundle
                }
            } else if (taskData.Activity!!.title.lowercase().contains("payment")) {

                when (userRepository.getTeam()?.Team?.lov_team_type) {
                    "Presales",
                    "Trade" -> {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.presales_or_trade), Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> {
                        selectedItem = taskData
                        selectedPos = pos

                        val bundle = Bundle()
                        bundle.putParcelable(DataTransferKeys.KEY_PRODUCTS, task)
                        if (taskData.Activity?.lov_activity_status == RouteStatus.STATUS_COMPLETED) {
                            bundle.putBoolean(DataTransferKeys.KEY_IS_COMPLETED, true)
                        }
                        navigator.push(PaymentCollectionFragment::class) {
                            this.arguments = bundle
                        }
                    }
                }
            }
        }
    }

    private fun setCurrentTaskStatus(
        pos: Int,
        account: TaskData?,
        reason: String? = null,
        attachment: ArrayList<Attachment>? = null,
        description: String? = null,
        productList: ArrayList<UpdateProductData>? = null,
        isChangeInStatus: Boolean? = true
    ) {

        taskList?.get(pos)?.Activity?.lov_activity_status =
            account?.Activity?.lov_activity_status ?: ""
        if (!attachment.isNullOrEmpty()) {
            val attatchments = arrayListOf<TaskAttatchment>()
            attachment.forEach {
                attatchments.add(
                    TaskAttatchment(
                        Attachment(
                            file = it.file,
                            file_name = it.file_name
                        )
                    )
                )
            }
            taskList?.get(pos)?.Attachments = attatchments
            taskList?.get(pos)?.Activity?.description = description
        }

        if (!productList.isNullOrEmpty()) {
            val activityProducts = arrayListOf<ActivityProducts>()
            productList.forEach { productData ->
                val productTemplate = ProductTemplate(
                    integration_num = productData.product_integration_num,
                    title = productData.product_title,
                    id = productData.product_id
                )
                val activityProductTemplate = ProductTemplate(
                    lov_product_status = productData.lov_product_status,
                    lov_product_uom = productData.lov_product_uom,
                    location = productData.lov_product_location,
                    product_qty = (productData.product_qty ?: 0.0),
                )
                activityProducts.add(
                    ActivityProducts(
                        activityProductTemplate,
                        productTemplate
                    )
                )
            }
            taskList?.get(pos)?.ActivityProducts = activityProducts

        }


        viewModel.currentRoute.value?.find {
            it.Account?.id.toString() == task?.Account?.id.toString()
        }?.let {
            it.Visit?.Tasks = taskList
            lifecycleScope.launchWhenCreated {
                routeActivityDao.insert(it)
            }
        }
        taskAdapter.notifyDataItem(taskList?.get(pos), pos)

        if (isChangeInStatus == true) {
            updateActivityStatus(
                account?.Activity?.lov_activity_status ?: "",
                taskList?.get(pos)?.Activity,
                reason, productList, attachment, description
            )
        }
    }

    private fun addNewTask(account: ActivityTemplate?) {

        taskList?.add(
            TaskData(account)
        )

        viewModel.currentRoute.value?.find {
            it.Account?.id.toString() == task?.Account?.id.toString()
        }?.let {
            it.Visit?.Tasks = taskList
            lifecycleScope.launchWhenCreated {
                routeActivityDao.insert(it)
            }
        }
        taskAdapter.notifyData(taskList)

        addNewActivityApi(
            RouteStatus.STATUS_PENDING,
            account
        )
    }

    private fun updateActivityStatus(
        status: String,
        account: ActivityTemplate?,
        reason: String? = null,
        productList: ArrayList<UpdateProductData>? = null,
        attachment: ArrayList<Attachment>? = null,
        description: String? = null
    ) {

        val activityData = RouteActivityData(
            id = account?.id ?: null,
            integration_id = account?.integration_id ?: null,
            lov_activity_status = status,
            lov_route_exec_status_reason = reason,
            actual_enddate = account?.actual_enddate,
            actual_startdate = account?.actual_startdate,
            latitude = account?.latitude,
            longitude = account?.longitude,
            ActivityProducts = productList,
            description = description,
            Attachments = attachment
        )
        val taskList = arrayListOf<UpdateRouteActivity>()
        taskList.add(UpdateRouteActivity(activityData))
        if (isConnectedToInternet(requireContext(), false)) {
            viewModel.updateRouteActivityApi(requireContext(), taskList)
        } else {
            lifecycleScope.launchWhenStarted {
                val result = updateActivityDao.insert(taskList)
                Timber.e(result.toString())
            }
        }
    }

    private fun addNewActivityApi(
        status: String,
        account: ActivityTemplate?
    ) {

        val activityData = CreateActivityTemplate(
            lov_activity_status = status,
            latitude = account?.latitude,
            longitude = account?.longitude,
            visit_id = task?.Visit?.Activity?.id,
            description = account?.description,
            lov_activity_type = account?.lov_activity_type,
            integration_id = account?.integration_id
        )
        val taskList = arrayListOf<CreateActivityData>()
        taskList.add(CreateActivityData(activityData))
        if (isConnectedToInternet(requireContext(), false)) {
            viewModel.createActivityApi(requireContext(), taskList)
        } else {
//            lifecycleScope.launchWhenStarted {
//                val result=updateActivityDao.insert(taskList)
//                Timber.e(result.toString())
//            }
        }
    }
}
