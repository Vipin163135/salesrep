package com.salesrep.app.ui.customer.add_customer

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.github.fragivity.dialog.showDialog
import com.github.fragivity.navigator
import com.github.fragivity.pop
import com.github.fragivity.popToRoot
import com.github.fragivity.push
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.ktx.awaitMap
import com.salesrep.app.BuildConfig
import com.salesrep.app.R
import com.salesrep.app.base.BaseFragment
import com.salesrep.app.data.appConfig.Crmapp
import com.salesrep.app.data.models.requests.AddAddressData
import com.salesrep.app.data.models.requests.AddBulkCustomerRequest
import com.salesrep.app.data.models.requests.AddCustomerData
import com.salesrep.app.data.models.requests.AddCustomerRequest
import com.salesrep.app.data.models.response.StatusModel
import com.salesrep.app.data.network.responseUtil.ApisRespHandler
import com.salesrep.app.data.network.responseUtil.Status
import com.salesrep.app.data.repos.UserRepository
import com.salesrep.app.databinding.FragmentAddCustomerBinding
import com.salesrep.app.ui.customer.adapter.CustomerListAdapter
import com.salesrep.app.ui.customer.CustomerViewModel
import com.salesrep.app.ui.customer.adapter.VisitDaysAdapter
import com.salesrep.app.ui.dialogs.ProgressDialog
import com.salesrep.app.util.*
import com.salesrep.app.util.AppRequestCode.SELECT_TIME_REQUEST
import com.salesrep.app.util.PermissionUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import permissions.dispatcher.*
import java.io.IOException
import java.util.*
import javax.inject.Inject

@RuntimePermissions
@AndroidEntryPoint
class AddCustomerFragment : BaseFragment<FragmentAddCustomerBinding>() {

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository

    private lateinit var binding: FragmentAddCustomerBinding
    override val viewModel by viewModels<CustomerViewModel>()
    private lateinit var progressDialog: ProgressDialog
    private lateinit var adapter: CustomerListAdapter
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var billingMap: GoogleMap? = null
    private var deliveryMap: GoogleMap? = null

    private var latLng: LatLng? = null
    private var deliveryAddress: AddAddressData? = null
    private var billingAddress: AddAddressData? = null
    private var addressType = -1
    private var days: List<StatusModel> = arrayListOf()
    private var spinnerDays: VisitDaysAdapter? = null

    companion object {
        const val KEY_BILLING_ADDRESS = "KEY_BILLING_ADDRESS"
        const val REQUEST_ADDRESS = "REQUEST_ADDRESS"
    }
    
    override fun getLayoutResId(): Int {
        return R.layout.fragment_add_customer
    }

    override fun onCreateView(
        instance: Bundle?,
        binding: FragmentAddCustomerBinding
    ) {
        this.binding = binding

        progressDialog = ProgressDialog(requireActivity())
        initialize()
        bindObservers()
        listeners()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(REQUEST_ADDRESS) { key, bundle ->
            if (bundle.get(KEY_BILLING_ADDRESS) != null) {
                if (addressType == 2) {
                    billingAddress = bundle.getParcelable(KEY_BILLING_ADDRESS)
                    billingAddress?.let {
                        binding.tvBillingAddress.text =
                            "${it.street ?: ""},${it.suburb ?: ""},${it.city ?: ""},${it.state ?: ""},${it.zip ?: ""},${it.country ?: ""}"
                    }
                } else {
                    deliveryAddress = bundle.getParcelable(KEY_BILLING_ADDRESS)
                    deliveryAddress?.let {
                        binding.tvDeliveryAddress.text =
                            "${it.street ?: ""},${it.suburb ?: ""},${it.city ?: ""},${it.state ?: ""},${it.zip ?: ""},${it.country ?: ""}"
                    }
                }
            }
        }
        setFragmentResultListener(SELECT_TIME_REQUEST) { key, bundle ->
            if (bundle.get(DialogTimePickerFragment.ARGUMENT_TIME) != null) {
                binding.etPreferredTime.text= bundle.getString(DialogTimePickerFragment.ARGUMENT_TIME)
            }
        }
    }

    private fun listeners() {
        binding.ivBack.setOnClickListener {
            navigator.popToRoot()
        }
        binding.tvEditDelivery.setOnClickListener {
            addressType = 1
            navigator.push(AddAddressFragment::class) {
                arguments = bundleOf(KEY_BILLING_ADDRESS to deliveryAddress)
            }
        }
        binding.tvEditBilling.setOnClickListener {
            addressType = 2
            navigator.push(AddAddressFragment::class) {
                arguments = bundleOf(KEY_BILLING_ADDRESS to billingAddress)
            }
        }

        binding.etPreferredDay.setOnClickListener {
            binding.spinner.performClick()
        }


        binding.etPreferredTime.setOnClickListener {
            navigator.showDialog(
                DialogTimePickerFragment::class,
                bundleOf(Pair(DialogTimePickerFragment.ARGUMENT_TIME, binding.etPreferredTime.text.toString()))
            )
        }


        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                binding.etPreferredDay.text = days[position].value
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        binding.btnAdd.setOnClickListener {
            when {
                ValidationUtils.isFieldNullOrEmpty(binding.etAccountName.text.toString()) -> {
                    binding.root.showSnackBar(getString(R.string.error_account_name_empty))
                }
                ValidationUtils.isFieldNullOrEmpty(binding.etCallPhoneNum.text.toString()) -> {
                    binding.root.showSnackBar(getString(R.string.error_phone_empty))
                }
                ValidationUtils.isFieldNullOrEmpty(binding.etEmail.text.toString()) -> {
                    binding.root.showSnackBar(getString(R.string.error_email_empty))
                }
                ValidationUtils.isFieldNullOrEmpty(binding.etTaxNum.text.toString()) -> {
                    binding.root.showSnackBar(getString(R.string.error_tax_empty))
                }
                ValidationUtils.isFieldNullOrEmpty(binding.etPreferredDay.text.toString()) -> {
                    binding.root.showSnackBar(getString(R.string.error_day_empty))
                }
                ValidationUtils.isFieldNullOrEmpty(binding.etPreferredTime.text.toString()) -> {
                    binding.root.showSnackBar(getString(R.string.error_time_empty))
                }
                else -> {
                    var crmApp: Crmapp =
                        prefsManager.getObject(PrefsManager.APP_CONFIG, Crmapp::class.java)
                            ?: Crmapp()

                    val customerData = AddCustomerData(
                        accountname = binding.etAccountName.text.toString(),
                        phone = binding.etWorkPhoneNum.getText().toString(),
                        mobile_phone = binding.etCallPhoneNum.getText().toString(),
                        mobilephone = binding.etCallPhoneNum.getText().toString(),
                        email = binding.etEmail.text.toString(),
                        tax_number = binding.etTaxNum.text.toString(),
                        preferred_visit_day = binding.etPreferredDay.text.toString(),
                        preferred_visit_time = binding.etPreferredTime.text.toString(),
                        pricelist_id = crmApp.pricelist_id.toString(),
                        team_id = userRepository.getTeam()?.Team?.id,
                    )

                    val addCustomerRequest = arrayListOf<AddCustomerRequest>()
                    addCustomerRequest.add(
                        AddCustomerRequest(
                            Account = customerData,
                            deliveryAddress,
                            billingAddress
                        )
                    )
                    viewModel.createCustomerApi(
                        requireContext(),
                        AddBulkCustomerRequest(addCustomerRequest)
                    )

                }
            }
        }

    }


    private fun initialize() {

        lifecycleScope.launchWhenCreated {
            val deliveryMapFragment: WorkaroundMapFragment? =
                childFragmentManager.findFragmentById(R.id.deliveryMap) as? WorkaroundMapFragment
            val deliveryGoogleMap: GoogleMap? = deliveryMapFragment?.awaitMap()
            deliveryMap = deliveryGoogleMap

            deliveryMapFragment?.setListener(object : WorkaroundMapFragment.OnTouchListener {
                override fun onTouch() {
                    binding.scrollView.requestDisallowInterceptTouchEvent(true)
                }
            })

            if (this@AddCustomerFragment.latLng != null) {
                val cameraPosition = CameraPosition.Builder()
                    .target(latLng) // Sets the center of the map to location user
                    .zoom(7f) // Sets the zoom
                    .build() // Creates a CameraPosition from the builder
                deliveryMap?.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            }
            deliveryMap?.setOnCameraIdleListener {
                Log.e("==camera idle==", deliveryMap?.getCameraPosition()?.target.toString());
                setAddress(deliveryMap?.cameraPosition?.target!!, 1)
            }
            deliveryMap?.setOnMapClickListener { ltlng ->
                val cameraPosition = CameraPosition.Builder()
                    .target(ltlng) // Sets the center of the map to location user
                    .zoom(7f) // Sets the zoom
                    .build() // Creates a CameraPosition from the builder
                deliveryMap?.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            }

        }
        lifecycleScope.launchWhenCreated {
            val billingMapFragment: WorkaroundMapFragment? =
                childFragmentManager.findFragmentById(R.id.billingMap) as? WorkaroundMapFragment
            val billingGoogleMap: GoogleMap? = billingMapFragment?.awaitMap()

            billingMapFragment?.setListener(object : WorkaroundMapFragment.OnTouchListener {
                override fun onTouch() {
                    binding.scrollView.requestDisallowInterceptTouchEvent(true)
                }
            })
            billingMap = billingGoogleMap


            if (this@AddCustomerFragment.latLng != null) {
                val cameraPosition = CameraPosition.Builder()
                    .target(latLng) // Sets the center of the map to location user
                    .zoom(7f) // Sets the zoom
                    .build() // Creates a CameraPosition from the builder

                billingMap?.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

            }


            billingMap?.setOnCameraIdleListener {
                Log.e("==camera idle==", billingMap?.getCameraPosition()?.target.toString());
                setAddress(billingMap?.cameraPosition?.target!!, 2)
            }


            billingMap?.setOnMapClickListener { ltlng ->
                val cameraPosition = CameraPosition.Builder()
                    .target(ltlng) // Sets the center of the map to location user
                    .zoom(7f) // Sets the zoom
                    .build() // Creates a CameraPosition from the builder

                billingMap?.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            }
        }


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        viewModel.getFormCatalogApi(requireContext())
        checkPermissions()

    }


    @SuppressLint("FragmentLiveDataObserve")
    private fun bindObservers() {
        viewModel.formCatalogApiResponse.observe(this, androidx.lifecycle.Observer {
            it ?: return@Observer
            when (it.status) {
                Status.LOADING -> {
                    progressDialog.setLoading(true)
                }
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)
                    if (it.data != null) {
                        days = it.data.VisitDays

                        spinnerDays = VisitDaysAdapter(
                            requireContext(),
                            R.layout.item_field_selection_item,
                            binding.etPreferredDay.id,
                            days
                        )
                        binding.spinner.adapter = spinnerDays

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

        viewModel.createAccountsResponse.setObserver(viewLifecycleOwner, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.LOADING -> progressDialog.setLoading(true)
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)
                    if (it.data != null && it.data.success == true) {
                        navigator.pop()
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


    @SuppressLint("MissingPermission")
    private fun checkPermissions() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            if (isLocationEnabled()) {
                mFusedLocationClient.lastLocation.addOnCompleteListener(requireActivity()) { task ->
                    val mLastLocation: Location? = task.result
                    if (mLastLocation != null) {
                        val latLng = LatLng(mLastLocation.latitude, mLastLocation.longitude)
                        this.latLng = latLng
                        setAddress(latLng, 0)

                        val cameraPosition = CameraPosition.Builder()
                            .target(latLng) // Sets the center of the map to location user
                            .zoom(7f) // Sets the zoom
                            .build() // Creates a CameraPosition from the builder

                        billingMap?.moveCamera(
                            CameraUpdateFactory.newCameraPosition(
                                cameraPosition
                            )
                        )
                        deliveryMap?.moveCamera(
                            CameraUpdateFactory.newCameraPosition(
                                cameraPosition
                            )
                        )

                    }
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    R.string.we_will_need_your_location,
                    Toast.LENGTH_LONG
                ).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)

                if (requireActivity().packageName.equals(BuildConfig.APPLICATION_ID))
                    startActivity(intent)
            }

        } else {
            getLocationWithPermissionCheck()
        }
    }

    private fun setAddress(latLng: LatLng, type: Int) {
        lifecycleScope.launch {
            val geocoder = Geocoder(context, Locale.US)
            try {
                val results = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                if (results.size > 0) {
                    val address = results[0]
                    // so something
                    when (type) {
                        0 -> {
                            setDeliveryAddress(address)
                            setBillingAddress(address)
                        }
                        1 -> {
                            setDeliveryAddress(address)
                        }
                        2 -> {
                            setBillingAddress(address)
                        }
                    }

                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun setBillingAddress(address: Address) {
        val streetAddress = address.subAdminArea
        val streetnum = address.subThoroughfare
        val postalCode = address.postalCode
        val state = address.adminArea
        val country = address.countryName
        val city = address.locality
        val suburb = address.subLocality

        billingAddress = AddAddressData(
            street = streetAddress,
            street_no = streetnum,
            suburb = suburb,
            state = state,
            city = city,
            zip = postalCode,
            country = country,
            latitude = address.latitude,
            longitude = address.longitude
        )
        billingAddress?.let {
            binding.tvBillingAddress.text =
                "${it.street ?: ""},${it.suburb ?: ""},${it.city ?: ""},${it.state ?: ""},${it.zip ?: ""},${it.country ?: ""}"
        }
    }

    private fun setDeliveryAddress(address: Address) {
        val streetAddress = address.subAdminArea
        val streetnum = address.subThoroughfare
        val postalCode = address.postalCode
        val state = address.adminArea
        val country = address.countryName
        val city = address.locality
        val suburb = address.subLocality

        deliveryAddress = AddAddressData(
            street = streetAddress,
            street_no = streetnum,
            suburb = suburb,
            state = state,
            city = city,
            zip = postalCode,
            country = country,
            latitude = address.latitude,
            longitude = address.longitude
        )
        deliveryAddress?.let {
            binding.tvDeliveryAddress.text =
                "${it.street ?: ""},${it.suburb ?: ""},${it.city ?: ""},${it.state ?: ""},${it.zip ?: ""},${it.country ?: ""}"
        }

    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    @NeedsPermission(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    fun getLocation() {
        checkPermissions()
    }

    @OnShowRationale(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    fun showLocationRationale(request: PermissionRequest) {
        PermissionUtils.showRationalDialog(
            requireContext(),
            R.string.we_will_need_your_location,
            request
        )
    }

    @OnNeverAskAgain(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    fun onNeverAskAgainRationale() {
        PermissionUtils.showAppSettingsDialog(
            requireContext(), R.string.we_will_need_your_location
        )
    }

    @OnPermissionDenied(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    fun showDeniedForStorage() {
        PermissionUtils.showAppSettingsDialog(
            requireContext(), R.string.we_will_need_your_location
        )
    }
}