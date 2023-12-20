package com.salesrep.app.ui.takeOrder

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import androidx.print.PrintHelper
import com.github.fragivity.navigator
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.salesrep.app.R
import com.salesrep.app.base.BaseFragment
import com.salesrep.app.data.models.CreatePaymentTemplate
import com.salesrep.app.data.models.response.GetRouteAccountResponse
import com.salesrep.app.data.models.response.GetTeamPricelistResponse
import com.salesrep.app.data.models.response.OrderListObject
import com.salesrep.app.data.repos.UserRepository
import com.salesrep.app.databinding.FragmentOrderPaymentPrintBinding
import com.salesrep.app.util.DataTransferKeys
import com.salesrep.app.util.DateFormat
import com.salesrep.app.util.DateUtils
import com.salesrep.app.util.PrefsManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.item_order_payment_print.view.*
import java.lang.reflect.Type
import javax.inject.Inject

@AndroidEntryPoint
class PrintOrderPaymentFragment : BaseFragment<FragmentOrderPaymentPrintBinding>(){

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var prefsManager: PrefsManager


    private lateinit var binding: FragmentOrderPaymentPrintBinding


    private val products by lazy {
        arguments?.getParcelable<GetRouteAccountResponse>(
            DataTransferKeys.KEY_ACCOUNT_DETAIL
        )!!
    }

    private val payments by lazy {
        arguments?.getParcelableArrayList<CreatePaymentTemplate>(
            DataTransferKeys.KEY_PAYMENT_LIST
        )
    }


    override fun getLayoutResId(): Int {
        return R.layout.fragment_order_payment_print
    }


    override fun onCreateView(instance: Bundle?, binding: FragmentOrderPaymentPrintBinding) {
        this.binding = binding
        binding.tvDateTime.text= DateUtils.getCurrentDateWithFormat(DateFormat.DATE_TIME_FORMAT)
        binding.tvRepName.text= userRepository.getUser()?.name

        setPrintData()
        listeners()
    }

    private fun listeners() {
        binding.tvBack.setOnClickListener {
            navigator.popBackStack()
        }

        binding.tvPrintOrder.setOnClickListener {
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

        val gson = prefsManager.getString(PrefsManager.TEAM_PRICELIST, "")
        var currentPriceList: GetTeamPricelistResponse?=null

        if (!gson.isNullOrEmpty()) {

            val listType: Type = object : TypeToken<ArrayList<GetTeamPricelistResponse?>?>() {}.type
            val productPriceList =
                Gson().fromJson<ArrayList<GetTeamPricelistResponse>>(gson, listType)
            currentPriceList =
                productPriceList.find { products?.Pricelist?.id == it.Pricelist?.id }

        }

        binding.llProducts.removeAllViews()

        binding.tvStatus.text = products?.Order?.lov_order_status ?: "01 - Pending"
        binding.tvOrderNum.text = products?.Order?.name
        if (products?.Order?.delivery_date.isNullOrEmpty()){
            binding.tvDeliveryDate.text = "N/A"
        }else {
            binding.tvDeliveryDate.text = products?.Order?.delivery_date.toString().split(" ")[0]
        }

        if (products?.Order?.confirmation_date.isNullOrEmpty()){
            binding.tvConfDate.text = "N/A"
        }else {
            binding.tvConfDate.text = products?.Order?.confirmation_date.toString().split(" ")[0]
        }
        binding.tvCusName.text = products?.Account?.accountname.toString()

        binding.tvType.text= products?.Order?.lov_order_type



        payments?.forEachIndexed {index,it ->
            val child: View = layoutInflater.inflate(R.layout.item_order_payment_print, null)
            child.tvProductName.text= it?.integration_id
            child.tvTotalAmnt.text = it.payment_type
            child.tvDueAmount.text = it.lov_payment_status
            child.tvPaidAmnt.text =
                getString(
                    R.string.cart_price_text,
                    currentPriceList?.Pricelist?.currency_symbol?:"$", (it.amount)
                )

            binding.llProducts.addView(child)
        }

//        binding.tvTotal.text =
//            String.format(getString(R.string.cart_price_text), currentPriceList?.Pricelist?.currency_symbol?:"$", (orders?.Order?.total?:0.0))

    }


    private fun getBitmapFromView(view: View, totalHeight: Int, totalWidth: Int): Bitmap {

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