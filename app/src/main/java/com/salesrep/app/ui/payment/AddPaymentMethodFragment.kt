package com.salesrep.app.ui.payment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.fragivity.navigator
import com.google.gson.Gson
import com.salesrep.app.R
import com.salesrep.app.base.BaseFragment
import com.salesrep.app.data.models.FieldItem
import com.salesrep.app.data.models.PaymentMethodsTemplate
import com.salesrep.app.data.network.responseUtil.ApisRespHandler
import com.salesrep.app.data.network.responseUtil.Status
import com.salesrep.app.databinding.FragmentAddPaymentMethodBinding
import com.salesrep.app.ui.dialogs.ProgressDialog
import com.salesrep.app.ui.payment.adapters.FieldsAdapter
import com.salesrep.app.ui.payment.adapters.PaymentMethodsAdapter
import com.salesrep.app.util.*
import dagger.hilt.android.AndroidEntryPoint
import tgio.rncryptor.RNCryptorNative
import timber.log.Timber
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@AndroidEntryPoint
class AddPaymentMethodFragment  : BaseFragment<FragmentAddPaymentMethodBinding>(),
    AdapterView.OnItemSelectedListener {

    companion object{
        const val REQUEST_ADD_PAYMENT= "REQUEST_ADD_PAYMENT"
        const val RESULT_ADD_PAYMENT_DATA= "RESULT_ADD_PAYMENT_DATA"

    }
    @Inject
    lateinit var prefsManager: PrefsManager

    private lateinit var binding: FragmentAddPaymentMethodBinding
    override val viewModel by viewModels<PaymentViewModel>()
    private lateinit var progressDialog: ProgressDialog
    private lateinit var fieldsAdapter: FieldsAdapter
    private var fieldsList = ArrayList<FieldItem>()
    private var spinnerSelection: PaymentMethodsAdapter? = null
    private var paymentMethodList = ArrayList<PaymentMethodsTemplate>()
    private lateinit var selectedPaymentMethod: PaymentMethodsTemplate

    override fun getLayoutResId(): Int {
        return R.layout.fragment_add_payment_method
    }

    override fun onCreateView(
        instance: Bundle?,
        binding: FragmentAddPaymentMethodBinding
    ) {
        this.binding = binding
        progressDialog = ProgressDialog(requireActivity())

        initialize()
        listeners()
        observers()
    }


    private fun initialize() {
        paymentMethodList.clear()
        fieldsList.clear()
        fieldsAdapter = FieldsAdapter(requireActivity(), false)
        binding.rvFields.adapter = fieldsAdapter
        binding.rvFields.layoutManager = LinearLayoutManager(requireContext())

        spinnerSelection = PaymentMethodsAdapter(
            requireActivity(),
            R.layout.item_field_selection_item,
            binding.tvText.id,
            paymentMethodList
        )

        binding.spinner.adapter = spinnerSelection

        binding.spinner.onItemSelectedListener = this

        viewModel.getPaymentMethodsApi(requireContext())
    }


    private fun listeners() {

        binding.tvBack.setOnClickListener {
           navigator.navigateUp()
        }
        binding.tvText.setOnClickListener {
            binding.spinner.performClick()
        }
        binding.btnAddPaymentMethod.setOnClickListener {
            Timber.d("Field values listeners: " + Gson().toJson(fieldsList))
            if (checkValidations()) {
                it.hideKeyboard()
                callAddPaymentApi()
            }
        }

    }

    private fun callAddPaymentApi() {
        val hashMap= HashMap<String,String>()
        val adapterList = fieldsAdapter?.getList()
        adapterList.forEach {

            when (it.type) {
                "select"->{
                    loop@  for(i in 0 until it.options.size){
                        if(it.options[i].isSelected == true)
                        {
                            hashMap[it.key] = it.options[i].value
                            break@loop
                        }
                    }
                }
                else -> { hashMap[it.key] = it.setValue!!}
            }

        }
        val jsonObj= Gson().toJson(hashMap)
        Timber.d(jsonObj)
//        var encryptedValue=""
        RNCryptorNative.encryptAsync(jsonObj, RNC_KEY) { encrypted, e ->
            run {
                Timber.d("encrypted async: $encrypted")
//                encryptedValue = encrypted
                viewModel.addPaymentMethodApi(requireContext(),encrypted,selectedPaymentMethod?.key)
            }
        }
//        RNCryptorNative.decryptAsync(encryptedValue, RNC_KEY) { encrypted, e -> Timber.d("encrypted async: $encrypted") }
    }

    @SuppressLint("FragmentLiveDataObserve")
    private fun observers() {
        viewModel.paymentMethodsList.observe(this, androidx.lifecycle.Observer {
            it ?: return@Observer
            when (it.status) {
                Status.LOADING -> progressDialog.setLoading(true)
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)
                    if (it.data != null) {
                        if (!it.data?.isNullOrEmpty()) {
                            setFields(it.data)
                        }
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
        viewModel.addPaymentMethodResponse.observe(this, androidx.lifecycle.Observer {
            it ?: return@Observer
            when (it.status) {
                Status.LOADING -> progressDialog.setLoading(true)
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)
                    if (!it.data.isNullOrEmpty())
                    setFragmentResult(REQUEST_ADD_PAYMENT, bundleOf(Pair(RESULT_ADD_PAYMENT_DATA,it.data)))
                    requireActivity().onBackPressed()

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

    private fun setFields(data: ArrayList<PaymentMethodsTemplate>) {
        paymentMethodList.clear()
        paymentMethodList.addAll(data ?: arrayListOf())
//        val pos = paymentMethodList.indexOfFirst { it.isSelected == true }
//        if (pos != -1)
//            binding.spinner?.setSelection(pos)
//        else
        spinnerSelection?.notifyDataSetChanged()
        binding.spinner.setSelection(0)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        //do nothing
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (position != -1) {
            binding.tvText.text = paymentMethodList[position].value
            if (!paymentMethodList[position].fields.isNullOrEmpty()) {
                fieldsList.clear()
                fieldsAdapter = FieldsAdapter(requireActivity(), false)
                binding.rvFields.adapter = fieldsAdapter
                fieldsList.addAll(paymentMethodList[position].fields)
                fieldsAdapter.addHomeList(fieldsList)
                selectedPaymentMethod= paymentMethodList[position]
            }
            if (paymentMethodList[position].icon != null) {
                binding.ivIcon.visible()
                loadImage(
                    requireContext(),
                    binding.ivIcon,
                    paymentMethodList[position].icon,
                    paymentMethodList[position].icon,
                    getDrawable(requireContext(), R.drawable.card_gradiant)
                )
//                itemView.tvText?.setTextColor(Color.parseColor(paymentMethodList[position]?.value))

            } else {
                binding.ivIcon.gone()
            }
        } else {
            binding.ivIcon.gone()
        }

//        (0 until paymentMethodList.size)
//            .forEach { i ->
//                paymentMethodList[i].isSelected = i == position
//            }

    }


    private fun checkValidations(): Boolean {

        val adapterList = fieldsAdapter?.getList()
        for (i in 0 until (adapterList?.size)) {
            when (adapterList[i].type) {
                "number", "cardnumber" -> {
                    if (ValidationUtils.isFieldNullOrEmpty(adapterList[i].setValue)) {
                        val message = getString(
                            R.string.empty_validation,
                            adapterList[i].value
                        )
                        view?.showSnackBar(message)
                        return false
                    } else if ((adapterList[i].setValue?.length ?: 0) < adapterList[i].min) {
                        val message = getString(
                            R.string.min_validation,
                            adapterList[i].value,
                            adapterList[i].min
                        )
                        view?.showSnackBar(message)
                        return false
                    } else if ((adapterList[i].setValue?.length ?: 0) > adapterList[i].max) {
                        val message = getString(
                            R.string.max_validation,
                            adapterList[i].value,
                            adapterList[i].max
                        )
                        view?.showSnackBar(message)
                        return false
                    }
                }

                "username" ->
                    if (ValidationUtils.isFieldNullOrEmpty(adapterList[i].setValue)) {
                        val message = getString(R.string.error_email_empty)
                        view?.showSnackBar(message)
                        return false
                    } else if (!ValidationUtils.isEmailValid(
                            adapterList[i].setValue ?: ""
                        )
                    ) {
                        val message = getString(R.string.error_invalid_email)
                        view?.showSnackBar(message)
                        return false
                    }
                "password" ->
                    if (ValidationUtils.isFieldNullOrEmpty(adapterList[i].setValue)) {
                        val message = getString(R.string.error_password_empty)
                        view?.showSnackBar(message)
                        return false
                    }
                "string" ->
                    if (ValidationUtils.isFieldNullOrEmpty(adapterList[i].setValue)) {
                        val message = getString(
                            R.string.empty_validation,
                            adapterList[i].value
                        )
                        view?.showSnackBar(message)
                        return false
                    } else if ((adapterList[i].setValue?.length ?: 0) < adapterList[i].min) {
                        val message = getString(
                            R.string.min_validation,
                            adapterList[i].value,
                            adapterList[i].min
                        )
                        view?.showSnackBar(message)
                        return false
                    } else if ((adapterList[i].setValue?.length ?: 0) > adapterList[i].max) {
                        val message = getString(
                            R.string.max_validation,
                            adapterList[i].value,
                            adapterList[i].min
                        )
                        view?.showSnackBar(message)
                        return false
                    } else if ((!adapterList[i].format.isNullOrEmpty()) && !checkFormatValidation(
                            adapterList[i]
                        )
                    ) {
                        val message = getString(
                            R.string.format_validation,
                            adapterList[i].value,
                            adapterList[i].format
                        )
                        view?.showSnackBar(message)
                        return false
                    }
            }
        }
        return true
    }

    private fun checkFormatValidation(fieldItem: FieldItem): Boolean {
        var date: Date? = null
        try {
            val dateFormat = SimpleDateFormat(fieldItem.format)
            dateFormat.isLenient = false
            date = dateFormat.parse(fieldItem.setValue)
        } catch (ex: ParseException) {
            ex.printStackTrace()
        }
        return date != null
    }
}