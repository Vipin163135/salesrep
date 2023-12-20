package com.salesrep.app.util

import android.R
import android.content.Context
import com.google.android.gms.maps.SupportMapFragment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import com.salesrep.app.util.WorkaroundMapFragment.TouchableWrapper
import android.widget.FrameLayout
import android.view.MotionEvent
import android.view.View

class WorkaroundMapFragment : SupportMapFragment() {
    private var mListener: OnTouchListener? = null
    override fun onCreateView(
        layoutInflater: LayoutInflater,
        viewGroup: ViewGroup?,
        savedInstance: Bundle?
    ): View? {
        val layout = super.onCreateView(layoutInflater, viewGroup, savedInstance)
        val frameLayout = TouchableWrapper(activity)
        frameLayout.setBackgroundColor(resources.getColor(R.color.transparent))
        (layout as ViewGroup?)!!.addView(
            frameLayout,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        return layout
    }

    fun setListener(listener: OnTouchListener?) {
        mListener = listener
    }

    interface OnTouchListener {
        fun onTouch()
    }

    inner class TouchableWrapper(context: Context?) : FrameLayout(
        context!!
    ) {
        override fun dispatchTouchEvent(event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> mListener!!.onTouch()
                MotionEvent.ACTION_UP -> mListener!!.onTouch()
            }
            return super.dispatchTouchEvent(event)
        }
    }
}