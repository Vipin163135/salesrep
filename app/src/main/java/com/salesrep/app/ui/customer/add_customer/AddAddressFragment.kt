package com.salesrep.app.ui.customer.add_customer

import android.location.Geocoder
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.github.fragivity.navigator
import com.github.fragivity.pop
import com.google.android.gms.maps.model.LatLng
import com.salesrep.app.R
import com.salesrep.app.base.BaseFragment
import com.salesrep.app.data.models.requests.AddAddressData
import com.salesrep.app.databinding.FragmentAddAddressBinding
import com.salesrep.app.ui.auth.AuthViewModel
import com.salesrep.app.ui.customer.CustomerViewModel
import com.salesrep.app.ui.customer.add_customer.AddCustomerFragment.Companion.KEY_BILLING_ADDRESS
import com.salesrep.app.ui.customer.add_customer.AddCustomerFragment.Companion.REQUEST_ADDRESS
import com.salesrep.app.ui.dialogs.ProgressDialog
import com.salesrep.app.util.PrefsManager
import com.salesrep.app.util.ValidationUtils
import com.salesrep.app.util.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class AddAddressFragment : BaseFragment<FragmentAddAddressBinding>() {

    @Inject
    lateinit var prefsManager: PrefsManager

    private lateinit var binding: FragmentAddAddressBinding
    override val viewModel by viewModels<CustomerViewModel>()
    private var lat: Double = 0.0
    private var lng: Double = 0.0
    private lateinit var progressDialog: ProgressDialog
    private val addressData by lazy { arguments?.getParcelable<AddAddressData>(KEY_BILLING_ADDRESS) }

    override fun getLayoutResId(): Int {
        return R.layout.fragment_add_address
    }

    override fun onCreateView(
        instance: Bundle?,
        binding: FragmentAddAddressBinding
    ) {
        this.binding = binding

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressDialog = ProgressDialog(requireActivity())
        intialize()
        listeners()
    }

    private fun listeners() {

        binding.btnAddAddress.setOnClickListener {
            when {
                ValidationUtils.isFieldNullOrEmpty(binding.etAddress.text.toString()) -> {
                    binding.root.showSnackBar(getString(R.string.error_address_empty))
                }
                ValidationUtils.isFieldNullOrEmpty(binding.etStreetNum.text.toString()) -> {
                    binding.root.showSnackBar(getString(R.string.error_street_num_empty))
                }
                ValidationUtils.isFieldNullOrEmpty(binding.etCity.text.toString()) -> {
                    binding.root.showSnackBar(getString(R.string.error_city_empty))
                }
                ValidationUtils.isFieldNullOrEmpty(binding.etState.text.toString()) -> {
                    binding.root.showSnackBar(getString(R.string.error_state_empty))
                }
                ValidationUtils.isFieldNullOrEmpty(binding.etZipCode.text.toString()) -> {
                    binding.root.showSnackBar(getString(R.string.error_zipcode_empty))
                }
                ValidationUtils.isFieldNullOrEmpty(binding.etCountry.text.toString()) -> {
                    binding.root.showSnackBar(getString(R.string.error_country_empty))
                }
                else -> {
                    val addressData = AddAddressData(
                        street = binding.etAddress.text.toString(),
                        street_no = (binding.etStreetNum.text.toString()),
                        suburb = binding.etSuburb.text.toString(),
                        state = binding.etState.text.toString(),
                        city = binding.etCity.text.toString(),
                        zip = binding.etZipCode.text.toString(),
                        country = binding.etCountry.text.toString(),
                        latitude = lat,
                        longitude = lng
                    )

                    setFragmentResult(
                        REQUEST_ADDRESS,
                        bundleOf(KEY_BILLING_ADDRESS to addressData)
                    )
                    navigator.pop()
                }
            }
        }

    }

    private fun intialize() {
        addressData?.let {
            lat= it.latitude?:0.0
            lng= it.longitude?:0.0
            binding.address= it
        }

    }


}