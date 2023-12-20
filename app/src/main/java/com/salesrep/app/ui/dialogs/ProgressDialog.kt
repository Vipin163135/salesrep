package com.salesrep.app.ui.dialogs

import android.app.Activity
import android.app.Dialog
import android.view.View
import com.salesrep.app.R

class ProgressDialog() {

    private lateinit var dialog: Dialog

    private var activity: Activity? = null

    constructor(context: Activity) : this() {
        activity = context
        val dialogView = View.inflate(context, R.layout.dialog_progress, null)
        dialog = Dialog(context, R.style.CustomDialog)
        dialog.setContentView(dialogView)
        dialog.setCancelable(false)
    }

    private fun show() {
        if (!dialog.isShowing) {
            if (activity?.isFinishing == false)
                dialog.show()
        }
    }

    private fun dismiss() {
        if (dialog.isShowing) {
            if (activity?.isFinishing == false)
                dialog.dismiss()
        }
    }

    fun setLoading(isLoading: Boolean) {
        if (isLoading)
            show()
        else
            dismiss()
    }
}