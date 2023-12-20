package com.salesrep.app.util

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter

/**
 * Created by Vipin.
 */
class CustomBindings {

    companion object {

        @BindingAdapter("android:visibility")
        @JvmStatic
        fun setVisibility(view: View, shouldShow: Boolean) {
            view.visibility = if (shouldShow) View.VISIBLE else View.INVISIBLE
        }

        @BindingAdapter("android:showHideView")
        @JvmStatic
        fun showHideView(view: View, shouldShow: Boolean) {

            view.visibility = if (shouldShow) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        @BindingAdapter(value = ["url", "thumbnail", "placeholder"], requireAll = false)
        @JvmStatic
        fun bindGlideWithImage(
            view: View,
            url: String?,
            thumbnailUrl: String?,
            placeholder: Drawable
        ) {
            loadImage(view.context, view as ImageView, thumbnailUrl, url, placeholder)
        }
    }
}
