package com.salesrep.app.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels


abstract class BaseDialogFragment<binding : ViewDataBinding> :
       DialogFragment() {

//    @Inject
//    lateinit var viewModelFactory: ViewModelProviderFactory

    private var rootView: View? = null
    open val viewModel by viewModels<BaseViewModel>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return if (rootView == null) {

            val binding: binding = DataBindingUtil.inflate(inflater, getLayoutResId(), container, false)
//            val viewModel = ViewModelProviders.of(this, viewModelFactory).get(getViewModel())
//            val viewModel = viewModelFactory.create(getViewModel())
            onCreateView(savedInstanceState,  binding)
            rootView = binding.root

            rootView

        } else {
            rootView
        }

    }

    abstract fun getLayoutResId(): Int
    abstract fun onCreateView(instance: Bundle?,  binding: binding)
}