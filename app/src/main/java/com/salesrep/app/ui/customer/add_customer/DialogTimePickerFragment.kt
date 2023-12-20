package com.salesrep.app.ui.customer.add_customer

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.salesrep.app.R
import com.salesrep.app.base.BaseBottomFragment
import com.salesrep.app.base.BaseDialogFragment
import com.salesrep.app.databinding.DialogRejectionBinding
import com.salesrep.app.databinding.DialogTimePickerBinding
import com.salesrep.app.ui.customer.CustomerViewModel
import com.salesrep.app.util.AppRequestCode
import com.salesrep.app.util.DataTransferKeys
import com.salesrep.app.util.DateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class DialogTimePickerFragment : BaseBottomFragment<DialogTimePickerBinding>() {

    private var binding: DialogTimePickerBinding? = null

    private val timeStr by lazy { requireArguments().getString(ARGUMENT_TIME) }

    override fun getLayoutResId(): Int {
        return R.layout.dialog_time_picker
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        instance: Bundle?,
        binding: DialogTimePickerBinding
    ) {
        this.binding = binding

        val localDate = if (timeStr.isNullOrBlank()) {
            LocalTime.parse("11:59")
        } else {
            LocalTime.parse(timeStr, DateTimeFormatter.ofPattern(DateFormat.TIME_FORMAT))
        }
        binding.timPicker.setOnTimeChangedListener(timeChangeLambda)
        binding.timPicker.hour = localDate.hour
        binding.timPicker.minute = localDate.minute
        binding.timPicker.setIs24HourView(true)
        binding.timPicker.isEnabled=true

        binding.tvOk.setOnClickListener {
            dismiss()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private val timeChangeLambda = TimePicker.OnTimeChangedListener { _, hour, mins ->
        val localTime = LocalTime.of(hour, mins)
        val formatter = DateTimeFormatter.ofPattern(DateFormat.TIME_FORMAT)
        setFragmentResult(
            AppRequestCode.SELECT_TIME_REQUEST,
            bundleOf(
                Pair(ARGUMENT_TIME, formatter.format(localTime))
            )
        )
//      dismiss()
    }

    companion object {
        const val ARGUMENT_TIME = "argument::time"
    }
}