
package com.salesrep.app.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.location.Address
import android.location.Geocoder
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.util.DisplayMetrics
import android.util.Patterns
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.EditText
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.DimenRes
import androidx.core.content.res.use
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import java.io.File
import java.io.IOException
import java.util.*
import java.util.regex.Pattern

fun isValidEmail(target: CharSequence?): Boolean {
    return target != null && Patterns.EMAIL_ADDRESS.matcher(target)
        .matches()
}


fun View.onSnackbar(msg: String) {
    //Snackbar(view)
    val snackbar = Snackbar.make(
        this, msg,
        Snackbar.LENGTH_LONG
    )

    snackbar.setAction("ok") { snackbar.dismiss() }
    val snackbarView = snackbar.view
    snackbarView.setBackgroundColor(Color.parseColor("#000000"))
    snackbar.setActionTextColor(Color.parseColor("#000000"))
    snackbar.show()
}

fun String?.toTextRequestBody(): RequestBody? = this?.toRequestBody("text/plain".toMediaType())
fun String.toMediaRequestBody(): RequestBody =
    this.toRequestBody("multipart/form-data".toMediaType())

fun toMultipart(fileName: String, path: String,keyName :String): MultipartBody.Part {
    val file = File(path)

    val requestFile = file.asRequestBody("image/*".toMediaType())

    val requestBody = MultipartBody.Part.createFormData(keyName, file.name, requestFile)

    return requestBody
}

 fun isValidEmailId(email: String): Boolean {
    val EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"

    return Pattern.compile(EMAIL_PATTERN).matcher(email).matches()
}

fun FragmentManager.getCurrentNavigationFragment(): Fragment? =
    primaryNavigationFragment?.childFragmentManager?.fragments?.first()

fun pxFromDp(dp: Int, context: Context?): Int {
    return (dp * (context?.resources?.displayMetrics?.density ?: 0f)).toInt()
}



fun Context.isNetworkConnected(): Boolean {
    val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = cm.activeNetworkInfo
    return activeNetwork != null && activeNetwork.isConnectedOrConnecting
}

fun String?.ifEmpty(default: String): String{
    return if (this.isNullOrEmpty()){
        default
    }else
        this!!
}

fun Context.getWidth(): Int {

    val displayMetrics = DisplayMetrics()
    val windowmanager: WindowManager =
        this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    windowmanager.defaultDisplay.getMetrics(displayMetrics)
    return displayMetrics.widthPixels
}

fun Context.getHeight(): Int {

    val displayMetrics = DisplayMetrics()
    val windowmanager: WindowManager =
        this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    windowmanager.defaultDisplay.getMetrics(displayMetrics)
    return displayMetrics.heightPixels
}


fun toVisibility(constraint: Boolean): Int = if (constraint) {
    View.VISIBLE
} else {
    View.GONE
}

@ColorInt
@SuppressLint("Recycle")
fun Context.themeColor(
    @AttrRes themeAttrId: Int
): Int {
    return obtainStyledAttributes(
        intArrayOf(themeAttrId)
    ).use {
        it.getColor(0, Color.MAGENTA)
    }
}

//fun Activity.toStart() {
//    Intent(this, AuthenticationActivity::class.java).also {
//        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        it.putExtra("openFragment","login_sign_up")
//        startActivity(it)
//    }
//}


fun setBaseUrl(baseUrl: String, retrofit: Retrofit) {
    val field = Retrofit::class.java.getDeclaredField("baseUrl")
    field.isAccessible = true
    val newHttpUrl = baseUrl.toHttpUrlOrNull()
    field.set(retrofit, newHttpUrl)
}

fun getAddress(mContext: Context, latitude: Double?, longitude: Double?): String {

    val addresses: List<Address>
    var address: Address
    var addressData = ""
    val geocoder = Geocoder(mContext, Locale.getDefault())

    try {
        addresses = geocoder.getFromLocation(
            latitude ?: 0.0,
            longitude ?: 0.0,
            1
        ) // Here 1 represent max location result to returned, by documents it recommended 1 to 5

        for (i in addresses.indices) {
            address = addresses[i]
            if (address.getAddressLine(0) != null) {
                addressData = address.getAddressLine(0)
                break
            }
        }

        return addressData
    } catch (e: IOException) {
        e.printStackTrace()
    }

    return addressData
}

//fun Activity.forceLayoutToRTL() {
//    this.window.decorView.layoutDirection = View.LAYOUT_DIRECTION_RTL
//}
//
//fun Activity.forceLayoutToLTR() {
//    this.window.decorView.layoutDirection = View.LAYOUT_DIRECTION_LTR
//}

inline fun <reified T : Any> Activity.launchActivity(
    requestCode: Int = -1,
    options: Bundle? = null,
    noinline init: Intent.() -> Unit = {}
) {

    val intent = newIntent<T>(this)
    intent.init()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        startActivityForResult(intent, requestCode, options)
    } else {
        startActivityForResult(intent, requestCode)
    }
}

fun EditText.onChange(cb: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            cb(s.toString())
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    })
}

inline fun <reified T : Any> Context.launchActivity(
    options: Bundle? = null,
    noinline init: Intent.() -> Unit = {}
) {

    val intent = newIntent<T>(this)
    intent.init()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        startActivity(intent, options)
    } else {
        startActivity(intent)
    }
}


fun EditText.limitLength(maxLength: Int) {
    filters = arrayOf(InputFilter.LengthFilter(maxLength))
}

fun EditText.onlyNumbers() {
    inputType = InputType.TYPE_CLASS_NUMBER  or InputType.TYPE_NUMBER_FLAG_SIGNED
    keyListener = DigitsKeyListener.getInstance("0123456789")
}

inline fun <reified T : Any> newIntent(context: Context): Intent =
    Intent(context, T::class.java)

/**
 * Adds margin to the left and right sides of the RecyclerView item.
 * Adapted from https://stackoverflow.com/a/27664023/4034572
 * @param horizontalMarginInDp the margin resource, in dp.
 */
class HorizontalMarginItemDecoration(context: Context, @DimenRes horizontalMarginInDp: Int) :
        RecyclerView.ItemDecoration() {

    private val horizontalMarginInPx: Int =
            context.resources.getDimension(horizontalMarginInDp).toInt()

    override fun getItemOffsets(
            outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
        outRect.right = horizontalMarginInPx
        outRect.left = horizontalMarginInPx
    }

}

fun RecyclerView?.getCurrentPosition() : Int {
    return (this?.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
}

fun ViewTreeObserver.addDisposableOnGlobalLayoutListener(listener: ViewTreeObserver.OnGlobalLayoutListener) {
    addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            removeOnGlobalLayoutListener(this)
            listener.onGlobalLayout()
        }
    })
}