package com.salesrep.app.ui.payment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.fragivity.navigator
import com.github.fragivity.push
import com.salesrep.app.R
import com.salesrep.app.base.BaseFragment
import com.salesrep.app.data.models.Paymentprofiles
import com.salesrep.app.data.models.response.PaymentProfileTemplate
import com.salesrep.app.data.network.responseUtil.ApisRespHandler
import com.salesrep.app.data.network.responseUtil.Status
import com.salesrep.app.databinding.FragmentPaymentBinding
import com.salesrep.app.ui.dialogs.ProgressDialog
import com.salesrep.app.ui.payment.adapters.CardAdapter
import com.salesrep.app.util.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PaymentFragment : BaseFragment<FragmentPaymentBinding>() {

    private var paymentMethodList = ArrayList<Paymentprofiles>()
    private var selectedPaymentMethod: Paymentprofiles ?=null

    @Inject
    lateinit var prefsManager: PrefsManager

    private lateinit var binding: FragmentPaymentBinding
    override val viewModel by viewModels<PaymentViewModel>()
    private lateinit var progressDialog: ProgressDialog
    private lateinit var cardAdapter: CardAdapter
    private var account: ArrayList<Paymentprofiles>?= null

    private var removepos:Int= -1
    override fun getLayoutResId(): Int {
        return R.layout.fragment_payment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(AddPaymentMethodFragment.REQUEST_ADD_PAYMENT) { key, bundle ->
            if (bundle.get(AddPaymentMethodFragment.RESULT_ADD_PAYMENT_DATA) != null) {
                val list= bundle.getParcelableArrayList<PaymentProfileTemplate>(AddPaymentMethodFragment.RESULT_ADD_PAYMENT_DATA)

                if (!list.isNullOrEmpty()){
                    list.forEach { item->
                        if (item.lov_paymentprofile_gateway== "Card"){
                            val paymentProfile=Paymentprofiles(
                                type = 1,
                                isSelected = true,
                                Paymentprofile = item
                            )
                            paymentMethodList.add(paymentProfile)
                        }else{
                            val paymentProfile=Paymentprofiles(
                                type = 2,
                                isSelected = true,
                                Paymentprofile = item
                            )
                            paymentMethodList.add(paymentProfile)

                        }
                    }
                    cardAdapter.addList(paymentMethodList,paymentMethodList.size-1)
                    binding.chkCash.isChecked=false

                }
            }
        }
    }

    override fun onCreateView(
        instance: Bundle?,
        binding: FragmentPaymentBinding
    ) {
        this.binding = binding

        progressDialog = ProgressDialog(requireActivity())
        intialize()
        listeners()
        observers()
    }

    @SuppressLint("FragmentLiveDataObserve")
    private fun observers() {
//        viewModel.paymentProfileList.observe(this, Observer {
//            it ?: return@Observer
//            when (it.status) {
//                Status.LOADING ->{
//                    progressDialog.setLoading(true)
//                }
//                Status.SUCCESS -> {
//                    progressDialog.setLoading(false)
//                    if (!it.data.isNullOrEmpty()) {
//
//                    }
//                }
//                Status.ERROR -> {
//                    progressDialog.setLoading(false)
//
//                    ApisRespHandler.handleError(
//                        it.error,
//                        requireActivity(),
//                        prefsManager = prefsManager
//                    )
//                }
//            }
//        })
//

        viewModel.removePaymentMethodResponse.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.LOADING -> progressDialog.setLoading(true)
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)
                    if (it.data?.success==true) {
                        paymentMethodList.removeAt(removepos)
                        cardAdapter.addList(paymentMethodList,-1)
//                        cardAdapter.notifyItemRemove(removepos)
                    }
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

    private fun listeners() {
        binding.ivAddPayment.setOnClickListener {
            navigator.push(AddPaymentMethodFragment::class)
        }

        binding.btnMakePayment.setOnClickListener {
            if (selectedPaymentMethod==null){
                setFragmentResult(REQUEST_SELECT_PAYMENT, bundleOf())
            }else {
                setFragmentResult(REQUEST_SELECT_PAYMENT, bundleOf(Pair(
                    RESULT_SELECT_PAYMENT_DATA,
                    selectedPaymentMethod)))
            }
            requireActivity().onBackPressed()
        }

        binding.llCash.setOnClickListener {
            binding.chkCash.isChecked=true
            selectedPaymentMethod= null
            cardAdapter.addList(paymentMethodList,-1)
        }
    }

    private fun intialize() {
        paymentMethodList.clear()
        if (account==null){
            account= arguments?.getParcelableArrayList<Paymentprofiles>(
                DataTransferKeys.KEY_ACCOUNT
            )
        }

//            cardAdapter = CardAdapter(requireContext(), this, false)
//            binding.btnMakePayment.gone()
//        }else{
            cardAdapter = CardAdapter(requireContext(), this, true)
            binding.btnMakePayment.visible()
//        }
        binding.rvPaymentMethods.layoutManager= LinearLayoutManager(requireContext())
        binding.rvPaymentMethods.adapter= cardAdapter

//        viewModel.getPaymentProfileApi(requireContext())

        account?.let{
            paymentMethodList.clear()
            it.forEach { item->
                if (item.Paymentprofile?.lov_paymentprofile_gateway== "Card"){
                    item.type= 1
                    paymentMethodList.add(item)
                }
            }
            if (paymentMethodList.size < it.size){
                paymentMethodList.add(Paymentprofiles(3,getString(R.string.other_payment)))
                it.forEach { item->
                    if (item.Paymentprofile?.lov_paymentprofile_gateway!= "Card"){
                        item.type= 2
                        paymentMethodList.add(item)
                    }
                }
            }
            cardAdapter.addList(paymentMethodList,-1)

        }

    }


    fun onClickCard(pos:Int) {
        paymentMethodList.mapIndexed { index, paymentProfileResponse ->
            if (index== pos) {
                paymentProfileResponse.isSelected = true
                selectedPaymentMethod= paymentProfileResponse
//                selectedPaymentId= paymentProfileResponse.Paymentprofile?.id
            }else{
                paymentProfileResponse.isSelected = false
            }
        }
        cardAdapter.addList(paymentMethodList,pos)
        binding.chkCash.isChecked=false
    }

    fun onDeleteCardClicked(pos:Int) {
        showAlertMessage(pos)
    }

    private fun showAlertMessage(pos:Int) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_payment_method)
            .setMessage(R.string.delete_payment_method_text)
            .setPositiveButton(R.string.yes){ dialog, _ ->
                dialog.dismiss()
                removepos= pos
                paymentMethodList[pos].Paymentprofile?.id?.let {
                    viewModel.removePaymentApi(requireContext(),
                        it
                    )
                }
            }
            .setNegativeButton(R.string.no){ dialog, _ ->
                dialog.cancel()
            }
            .setCancelable(true)
            .show()
    }

    companion object{
        const val REQUEST_SELECT_PAYMENT= "REQUEST_SELECT_PAYMENT"
        const val RESULT_SELECT_PAYMENT_DATA= "RESULT_SELECT_PAYMENT_DATA"

    }
}