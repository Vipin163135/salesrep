package com.salesrep.app.ui.reconcilation

import android.app.DatePickerDialog
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.DatePicker
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.print.PrintHelper
import com.github.fragivity.navigator
import com.salesrep.app.R
import com.salesrep.app.base.BaseFragment
import com.salesrep.app.data.models.response.GetTeamValueReconcilation
import com.salesrep.app.data.models.response.StatusModel
import com.salesrep.app.data.network.responseUtil.ApisRespHandler
import com.salesrep.app.data.network.responseUtil.Status
import com.salesrep.app.data.repos.UserRepository
import com.salesrep.app.databinding.FragmentValueReconcilationBinding
import com.salesrep.app.ui.dialogs.ProgressDialog
import com.salesrep.app.ui.inventory.InventoryViewModel
import com.salesrep.app.ui.reconcilation.adapter.ValueReconcilationAdapter
import com.salesrep.app.util.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.item_value_reconcilation_print.view.*
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


@AndroidEntryPoint
class ValueReconcilationFragment  : BaseFragment<FragmentValueReconcilationBinding>(){

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository


    override val viewModel by viewModels<InventoryViewModel>()
    private lateinit var binding: FragmentValueReconcilationBinding


    private lateinit var progressDialog: ProgressDialog

    private lateinit var adapter: ValueReconcilationAdapter
    private var cal = Calendar.getInstance()
    private var date:String?= null
    private var reconList: ArrayList<GetTeamValueReconcilation>?=null

    override fun getLayoutResId(): Int {
        return R.layout.fragment_value_reconcilation
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setFragmentResultListener(AppRequestCode.UPDATE_MOVEMENT_LIST_REQUEST) { key, bundle ->
            initialize()
        }
    }

    override fun onCreateView(instance: Bundle?, binding: FragmentValueReconcilationBinding) {
        this.binding = binding

        progressDialog = ProgressDialog(requireActivity())

        adapter= ValueReconcilationAdapter(requireContext())
        binding.rvInventory.adapter= adapter

        binding.tvDate.text= DateUtils.getCurrentDateWithFormat(DateFormat.YEAR_MON_DATE_FORMAT)
        binding.tvRepName.text= userRepository.getUser()?.name
        binding.tvTeamName.text= userRepository.getTeam()?.Team?.name

        initialize()
        listeners()
        bindObservers()
    }

    private fun listeners() {
        binding.tvBack.setOnClickListener {
            navigator.popBackStack()
        }
        binding.ivFilter.setOnClickListener {
            showDatePicker()
        }

        binding.tvCancelPrint.setOnClickListener {
            binding.viewFlipper.displayedChild=0
            binding.tvPrintRecon.gone()
            binding.tvCancelPrint.gone()
            binding.tvPrint.visible()
        }

        binding.tvPrint.setOnClickListener {
//            if (reconList.isNullOrEmpty()) {
//
//            } else {
                setPrintData()
                binding.viewFlipper.displayedChild=1
                binding.tvPrintRecon.visible()
                binding.tvCancelPrint.visible()
                binding.tvPrint.gone()
//            }

        }

        binding.tvPrintRecon.setOnClickListener {
            val printHelper = PrintHelper(requireActivity())
            printHelper.setColorMode(PrintHelper.COLOR_MODE_COLOR);
            printHelper.setScaleMode(PrintHelper.SCALE_MODE_FIT);
            val totalHeight = binding.scrollView.getChildAt(0).height
            val totalWidth = binding.scrollView.getChildAt(0).width

            val screenImg = getBitmapFromView(binding.scrollView, totalHeight, totalWidth)

            printHelper.printBitmap("Stock -" + SystemClock.uptimeMillis(), screenImg);

        }
    }

    private fun setPrintData() {
        binding.tvPrintTeamName.text= binding.tvTeamName.text.toString()
        binding.tvPrintVanName.text= binding.tvRepName.text.toString()
        binding.tvTotalProducts.text= binding.tvTotal.text.toString()
        binding.tvDateTime.text= binding.tvDate.text.toString()

        binding.llProducts.removeAllViews()
        reconList?.forEachIndexed {index,it ->
            val child: View = layoutInflater.inflate(R.layout.item_value_reconcilation_print, null)
            child.tvPaymentMethod.text= it.Record?.lov_payment_method
            child.tvTotalPayment.text= it.Record?.amount.toString()
            child.tvSerialNum.text= (index+1).toString()
            binding.llProducts.addView(child)
        }
    }


    private fun showDatePicker() {
        DatePickerDialog(requireContext(),
            dateSetListener,
            // set DatePickerDialog to point to today's date when it loads up
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)).show()
    }


    // create an OnDateSetListener
    val dateSetListener = object : DatePickerDialog.OnDateSetListener {
        override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int,
                               dayOfMonth: Int) {
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            date= DateUtils.dateFormatFromMillis(DateFormat.YEAR_MON_DATE_FORMAT, cal.timeInMillis)
            binding.tvDate.text=date
            initialize()
        }
    }

    private fun bindObservers() {
        viewModel.getTeamValueReconListResponse.setObserver(viewLifecycleOwner,  Observer {
            it ?: return@Observer
            when (it.status) {
                Status.LOADING -> {
                    progressDialog.setLoading(true)
                }
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)
                    setAdapterList(it.data)
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


    private fun initialize() {

        if (isConnectedToInternet(requireContext(),true)) {
            viewModel.getTeamValueReconcilationApi(date,requireContext())
        }
    }

    private fun setAdapterList(list: ArrayList<GetTeamValueReconcilation>?) {
//        if (!list.isNullOrEmpty()) {
            reconList = list
            adapter.notifyData(list)
            var totalAmount = 0.0
            list?.forEach {
                totalAmount += it.Record.amount ?: 0.0
            }
            binding.tvTotal.text = String.format("%.2f", totalAmount)
//            binding.tvPrint.visible()
//            binding.tvTotal.visible()
//            binding.tvTotalTxt.visible()
//        }else{
//            binding.tvPrint.invisible()
//            binding.tvTotal.gone()
//            binding.tvTotalTxt.gone()
//        }
    }

    fun getBitmapFromView(view: View, totalHeight: Int, totalWidth: Int): Bitmap {

        val height = Math.min(800, totalHeight);
        val percent = (height.toFloat() / totalHeight.toFloat())

        val canvasBitmap: Bitmap = Bitmap.createBitmap(
            (totalWidth * percent).toInt(),
            (totalHeight * percent).toInt(),
            Bitmap.Config.ARGB_8888
        );
        val canvas = Canvas(canvasBitmap);

        val bgDrawable = view.getBackground();
        if (bgDrawable != null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);

        canvas.save();
        canvas.scale(percent, percent);
        view.draw(canvas);
        canvas.restore();

        return canvasBitmap;

    }


}