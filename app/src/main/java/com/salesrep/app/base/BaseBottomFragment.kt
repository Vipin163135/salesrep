package com.salesrep.app.base

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.salesrep.app.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class BaseBottomFragment<binding : ViewDataBinding> :
    BottomSheetDialogFragment() {

    private var rootView: View? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return if (rootView == null) {
//                val viewModel = viewModelFactory.create(getViewModel())
            val binding: binding =
                DataBindingUtil.inflate(inflater, getLayoutResId(), container, false)
//                val viewModel = ViewModelProviders.of(this, viewModelFactory).get(getViewModel())
            onCreateView(savedInstanceState, binding)
            rootView = binding.root

            rootView

        } else {
            rootView
        }

    }

    abstract fun getLayoutResId(): Int
    abstract fun onCreateView(instance: Bundle?, binding: binding)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        setStyle(STYLE_NORMAL, R.style.MyTimePickerDialogTheme)
        return super.onCreateDialog(savedInstanceState)
    }
}