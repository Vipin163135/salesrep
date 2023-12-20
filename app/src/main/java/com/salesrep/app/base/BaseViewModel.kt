package com.salesrep.app.base

import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.*

open class BaseViewModel :ViewModel(

) {
  private val _snackBarMessageRes = MutableLiveData<Int?>()

  fun showMessage(@StringRes messageRes: Int) {
    _snackBarMessageRes.value = messageRes
  }

  fun clearMessage() {
    _snackBarMessageRes.value = null
  }
}