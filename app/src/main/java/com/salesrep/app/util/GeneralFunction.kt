package com.salesrep.app.util

//import com.salesrep.app.DataTransferKeys.DATA

import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.Drawable
import android.location.Location
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.util.LruCache
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.snackbar.Snackbar
import com.salesrep.app.R
import com.salesrep.app.data.models.AddressTemplate
import com.salesrep.app.data.models.TrackTemplate
import com.salesrep.app.data.network.Config
import com.salesrep.app.ui.auth.AuthActivity
import com.salesrep.app.ui.home.MainActivity
import com.salesrep.app.util.AppLocales.brazilLocale
import com.salesrep.app.util.AppLocales.usLocale
import com.salesrep.app.util.DataTransferKeys.PAGE_TO_OPEN
import com.salesrep.app.util.PrefsManager.Companion.TEAM_DATA
import com.salesrep.app.util.PrefsManager.Companion.TEAM_ROUTES
import com.salesrep.app.util.PrefsManager.Companion.USER_DATA
import com.salesrep.app.util.PrefsManager.Companion.USER_RESPONSE_DATA
import dagger.android.support.DaggerDialogFragment
import timber.log.Timber
import java.io.File
import java.text.NumberFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.roundToLong


fun View.gone() {
    visibility = View.GONE
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun getFileSizeLess5MB(file: File?): Boolean {
    Timber.e(file?.length().toString())
    return (file?.length() ?: 0) < 5125000
}

fun View.showSnackBar(msg: String) {
    hideKeyboard()
    try {
        val snackBar = Snackbar.make(this, msg, Snackbar.LENGTH_LONG)
        val snackBarView = snackBar.view
        val textView =
            snackBarView.findViewById<View>(dev.b3nedikt.reword.R.id.snackbar_text) as TextView
        textView.maxLines = 3
        snackBar.setAction(R.string.ok) { snackBar.dismiss() }
        snackBarView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
        snackBar.setActionTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
        snackBar.show()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun logoutUser(activity: Activity?, prefsManager: PrefsManager) {

    Timber.d("clearData")

    val notificationManager =
        activity?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.cancelAll()

    prefsManager.remove(USER_DATA)
    prefsManager.remove(USER_RESPONSE_DATA)
    prefsManager.remove(TEAM_DATA)
    prefsManager.remove(TEAM_ROUTES)

    openAuthPage(activity, DataTransferKeys.LOGIN_PAGE)

    activity.setResult(Activity.RESULT_CANCELED)
    ActivityCompat.finishAffinity(activity)
    activity.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
}

fun shareApp(context: Context, content: String) {
    val sendIntent = Intent()
    sendIntent.action = Intent.ACTION_SEND
    sendIntent.putExtra(Intent.EXTRA_TEXT, content)
    sendIntent.type = "text/plain"

    context.startActivity(sendIntent)
}

fun rateApp(context: Context) {
    context.startActivity(
        Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE + context.packageName))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}

/*Digits should be 0,1,2,3*/
fun getCountFormat(digits: Int, count: Int?): String {
    when (digits) {
        1 -> return if (count ?: 0 <= 9)
            count.toString()
        else
            String.format(Locale.ENGLISH, "%d+", 9)
        2 -> return if (count ?: 0 <= 99)
            count.toString()
        else
            String.format(Locale.ENGLISH, "%d+", 99)
        3 -> return if (count ?: 0 <= 999)
            count.toString()
        else
            String.format(Locale.ENGLISH, "%d+", 999)
        else -> return if (count ?: 0 <= 9999)
            count.toString()
        else
            String.format(Locale.ENGLISH, "%d+", 9999)
    }
}

fun Context.longToast(text: CharSequence) {
    Toast.makeText(this, text, Toast.LENGTH_LONG).show()
}

fun addFragment(fragmentManager: FragmentManager?, fragment: Fragment, id: Int) {
    fragmentManager?.beginTransaction()?.setCustomAnimations(0, 0, 0, 0)
        ?.add(id, fragment)?.commitAllowingStateLoss()
}

fun replaceFragment(fragmentManager: FragmentManager?, fragment: Fragment, id: Int) {
    fragmentManager?.beginTransaction()?.replace(id, fragment, fragment.tag)?.addToBackStack(fragment.tag)
        ?.commitAllowingStateLoss()
}

fun loadImage(
    activity: Context, ivUser: ImageView, thumbnailImage: String?,
    originalImage: String?, placeholder: Drawable?
) {

    var originalImageF = originalImage
    var thumbnailImageF = thumbnailImage

    if (originalImageF?.contains("htt") == false && !File(originalImage).exists() &&
        originalImage?.startsWith("file:") == false && !originalImage.startsWith("/storage") &&
        !originalImage.startsWith("content")
    ) {
        originalImageF = Config.BASE_URL.removeSuffix("/") + originalImage
        thumbnailImageF = Config.BASE_URL.removeSuffix("/") + thumbnailImage

        Glide.with(activity).load(getUrlWithHeaders(originalImageF))
            .apply(RequestOptions().placeholder(placeholder))
            .thumbnail(Glide.with(activity).load(getUrlWithHeaders(thumbnailImageF))).into(ivUser)
    } else {
        Glide.with(activity).load(originalImageF)
            .apply(RequestOptions().placeholder(placeholder))
            .thumbnail(Glide.with(activity).load(thumbnailImageF)).into(ivUser)
    }
}

/*To load header for glide*/
fun getUrlWithHeaders(url: String?): GlideUrl {
    return GlideUrl(
        url ?: "a", LazyHeaders.Builder()
            .addHeader("token", "1")
            .build()
    )
}

fun removeFragment(fragmentManager: FragmentManager?, fragment: Fragment) {
    fragmentManager?.beginTransaction()?.remove(fragment)?.commitAllowingStateLoss()
}

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun View.showKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.toggleSoftInputFromWindow(
        applicationWindowToken,
        InputMethodManager.SHOW_FORCED, 0
    )
}

fun placePicker(fragment: Fragment?, activityMain: Activity) {
    val activity: Activity = if (fragment != null)
        fragment.activity as Activity
    else
        activityMain

    val fields =
        listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)
    val intent = Autocomplete.IntentBuilder(
        AutocompleteActivityMode.FULLSCREEN, fields
    )
        .build(activity)

    if (fragment == null)
        activity.startActivityForResult(intent, AppRequestCode.AUTOCOMPLETE_REQUEST_CODE)
    else
        fragment.startActivityForResult(intent, AppRequestCode.AUTOCOMPLETE_REQUEST_CODE)
}

fun getAddress(place: Place): String {
    val finalAddress: String
    val name = place.name.toString()
    val placeAddress = place.address.toString()

    if (place.address?.contains(name) == true) {
        finalAddress = placeAddress
    } else {
        finalAddress = "$name, $placeAddress"
    }
    return finalAddress
}

fun getAddress(item: AddressTemplate?): String {
    val finalAddress = "${item?.street_no?: ""}, ${item?.street?: ""}," +
            "${item?.suburb?: ""},${item?.city?: ""},${item?.state?: ""},${item?.zip?: ""},${item?.country?: ""}"
    return finalAddress
}

fun disableButton(btn: View?) {
    btn?.isEnabled = false

    Handler().postDelayed({
        btn?.isEnabled = true
    }, 1500)// set time as per your requirement
}

@SuppressLint("DefaultLocale")
fun getFormattedDurationTime(context: Context, timeInMilliseconds: Long): String {
    return if (timeInMilliseconds < 1000) {
        ""
    } else {
        /* if call duration greater than one hour change duration format */
        if (timeInMilliseconds >= 3600 * 1000) {
            String.format(
                "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(timeInMilliseconds),
                TimeUnit.MILLISECONDS.toSeconds(timeInMilliseconds) - TimeUnit.MINUTES.toSeconds(
                    TimeUnit.MILLISECONDS.toMinutes(timeInMilliseconds)
                )
            )
        } else {
            String.format(
                "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(timeInMilliseconds) - TimeUnit.HOURS.toMinutes(
                    TimeUnit.MILLISECONDS.toHours(timeInMilliseconds)
                ),
                TimeUnit.MILLISECONDS.toSeconds(timeInMilliseconds) - TimeUnit.MINUTES.toSeconds(
                    TimeUnit.MILLISECONDS.toMinutes(timeInMilliseconds)
                )
            ) + " " + context.getString(R.string.mins)
        }
    }
}


fun openAuthPage(context: Context, pageToOpen: String) {
    context.startActivity(with(Intent(context, AuthActivity::class.java)) {
        putExtra(PAGE_TO_OPEN, pageToOpen)
    })
}

fun openDashBoardPage(context: Context, refreshApp: Boolean) {
    if (refreshApp) {
        (context as Activity).finishAffinity()
    }
    context.startActivity(Intent(context, MainActivity::class.java))
}

fun openFragment(context: Context, bundle: Bundle, requestCode: Int) {
//    (context as Activity).startActivityForResult(
//        with(
//            Intent(
//                context,
//                FragmentContainerActivity::class.java
//            )
//        ) {
//            putExtra(DATA, bundle)
//        }, requestCode
//    )
}

fun openOrder(context: Context, bundle: Bundle, requestCode: Int) {
//    (context as Activity).startActivityForResult(
//        with(
//            Intent(
//                context,
//                ActiveOrderActivity::class.java
//            )
//        ) {
//            putExtra(DATA, bundle)
//        }, requestCode
//    )
}

fun openDialogFragment(
    fragment: DaggerDialogFragment,
    supportFragmentManager: FragmentManager
) {
    fragment.show(supportFragmentManager, fragment.tag)
}

fun getDrawable(context: Context, id: Int): Drawable? {
    return ContextCompat.getDrawable(context, id)
}

fun openSignUpActivity(activity: Activity, bundle: Bundle) {
//    val intent = Intent(activity, SignUpActivity::class.java)
//    intent.putExtra(DATA, bundle)
//    activity.startActivity(intent)
}

fun openWebView(context: Context, webUrl: String) {
//    val intent = Intent(context, WebViewActivity::class.java)
//    intent.putExtra(DataTransferKeys.WEB_URL, webUrl)
//    context.startActivity(intent)
}

fun calculateDistance(latA: Double, lngA: Double, latB: Double, lngB: Double): Float {
    val locationA: Location = Location("point A")

    locationA.latitude = latA
    locationA.longitude = lngA

    val locationB = Location("point B")

    locationB.latitude = latB
    locationB.longitude = lngB

    Log.e( "point A", locationA.latitude.toString() )
    Log.e( "point B", locationB.toString() )
    Log.e( "distance : ", locationA.distanceTo(locationB).toString() )

    return locationA.distanceTo(locationB)
}
fun calculateTotalDistance(userTrackLatLng: List<TrackTemplate>): Float {

    var dist= 0.0f
    var previousLoc:Location? = null
    userTrackLatLng.forEachIndexed { index,track ->

        if (previousLoc!=null){
            val newLoc= Location(index.toString())
            newLoc.latitude= track.latLng.latitude
            newLoc.longitude= track.latLng.longitude

            dist+= newLoc.distanceTo(previousLoc)

            previousLoc = Location(index.toString())
            previousLoc?.latitude= track.latLng.latitude
            previousLoc?.longitude= track.latLng.longitude
        }else{
            previousLoc = Location(index.toString())
            previousLoc?.latitude= track.latLng.latitude
            previousLoc?.longitude= track.latLng.longitude
        }

    }

    return dist
}

fun makeCall(context: Context, callNumber: String) {
    val u = Uri.parse("tel:$callNumber")
    val i = Intent(Intent.ACTION_DIAL, u)

    try {
        // Launch the Phone app's dialer with a phone
        // number to dial a call.
        context.startActivity(i)
    } catch (s: SecurityException) {
        // show() method display the toast with
        // exception message.
        Toast.makeText(context, s.message, Toast.LENGTH_LONG)
            .show()
    }
}

fun sendSms(context: Context, callNumber: String) {
    val sendIntent = Intent(Intent.ACTION_VIEW)
    sendIntent.putExtra("sms_body", "default content")
    sendIntent.type = "vnd.android-dir/mms-sms"
    context.startActivity(sendIntent)
}

fun Context.openUrl(url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    } catch (e: Exception) {
        Log.e("exp", e.toString())
    }
}

fun Context.openWazeApp(destinationAddress: String, destination: String) {
    try {
        // Launch Waze to look for Hawaii:
        val url = "https://waze.com/ul?q=$destinationAddress&&ll=$destination&navigate=yes"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    } catch (ex: ActivityNotFoundException) {
        // If Waze is not installed, open it in Google Play:
        val intent =
            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.waze"))
        startActivity(intent)
    }
}

fun playMp3RawFile(context: Context, fileNameId: Int): MediaPlayer {
    val mediaPlayer: MediaPlayer = MediaPlayer.create(context, fileNameId)
    mediaPlayer.start()
    mediaPlayer.isLooping = true
    return mediaPlayer
}

fun convertCurrencyToBrazil(number: Float) : String {
    val nfBR = NumberFormat.getCurrencyInstance(brazilLocale)
    val nfUS = NumberFormat.getCurrencyInstance(usLocale)

    println("[BR] " + nfBR.format(number))
    println("[US] " + nfUS.format(number))
    println("-----")

    return nfBR.format(number)
}

fun View.expand( ) {
    measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    val targtetHeight = measuredHeight
    layoutParams.height = 0
    visibility = View.VISIBLE
    val a: Animation = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
            layoutParams.height =
                if (interpolatedTime == 1f) ViewGroup.LayoutParams.WRAP_CONTENT else (targtetHeight * interpolatedTime).toInt()
            requestLayout()
        }

        override fun willChangeBounds(): Boolean {
            return true
        }
    }
    a.setDuration((targtetHeight / context.resources.displayMetrics.density).roundToLong())
    startAnimation(a)
}

fun View.collapse() {
    val initialHeight = measuredHeight
    val a: Animation = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
            if (interpolatedTime == 1f) {
                visibility = View.GONE
            } else {
                layoutParams.height = initialHeight - (initialHeight * interpolatedTime).toInt()
                requestLayout()
            }
        }

        override fun willChangeBounds(): Boolean {
            return true
        }
    }
    a.setDuration(((initialHeight / context.resources.displayMetrics.density).roundToLong()))
    startAnimation(a)
}

fun BitmapFromVector( context: Context, vectorResId: Int): BitmapDescriptor {
    // below line is use to generate a drawable.
     val vectorDrawable: Drawable? = ContextCompat.getDrawable(context, vectorResId);

    // below line is use to set bounds to our vector drawable.
    vectorDrawable?.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());

    // below line is use to create a bitmap for our
    // drawable which we have added.
    val bitmap: Bitmap = Bitmap.createBitmap(vectorDrawable!!.getIntrinsicWidth(), vectorDrawable!!.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

    // below line is use to add bitmap in our canvas.
    val canvas: Canvas =  Canvas(bitmap);

    // below line is use to draw our
    // vector drawable in canvas.
    vectorDrawable.draw(canvas);

    // after generating our bitmap we are returning our bitmap.
    return BitmapDescriptorFactory.fromBitmap(bitmap);
}

fun BitmapMarker(context: Context,drawableId: Int,sequence: String): BitmapDescriptor{
    val conf = Bitmap.Config.ARGB_8888
    val bmp = Bitmap.createBitmap(80, 80, conf)
    val canvas1 = Canvas(bmp)


    val color = Paint()
    color.setTextSize(25f)
    color.setColor(Color.WHITE)

    canvas1.drawBitmap(
        BitmapFactory.decodeResource(
            context.getResources(),
            drawableId
        ), 0.0f, 0.0f, color
    )
    canvas1.drawText(sequence, 20.0f, 30.0f, color)
    return BitmapDescriptorFactory.fromBitmap(bmp);
}

fun decodePoly(encoded: String): java.util.ArrayList<LatLng> {
    val poly = java.util.ArrayList<LatLng>()
    var index = 0
    val len = encoded.length
    var lat = 0
    var lng = 0
    while (index < len) {
        var b: Int
        var shift = 0
        var result = 0
        do {
            b = encoded[index++].toInt() - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lat += dlat
        shift = 0
        result = 0
        do {
            b = encoded[index++].toInt() - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lng += dlng
        val p = LatLng(
            lat.toDouble() / 1E5,
            lng.toDouble() / 1E5
        )
        poly.add(p)
    }
    return poly
}


fun openGoogleMaps(context: Context,lat: Double,lon:Double,label:String){
    val mapUri = Uri.parse("geo:0,0?q=$lat,$lon($label)")
    val mapIntent = Intent(Intent.ACTION_VIEW, mapUri)
    mapIntent.setPackage("com.google.android.apps.maps")
    context.startActivity(mapIntent)
}

fun showAlertDialog(context: Context, titleId: Int, msgId: Int) {
    AlertDialogUtil.instance.createOkCancelDialog(
        context,
        titleId,
        msgId,
        R.string.ok,
        0,
        true,
        null
    ).show()
}


fun getScreenshotFromRecyclerView(view: RecyclerView): Bitmap? {
    val adapter = view.adapter
    var bigBitmap: Bitmap? = null
    if (adapter != null) {
        val size = adapter.itemCount
        var height = 0
        val paint = Paint()
        var iHeight = 0
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()

        // Use 1/8th of the available memory for this memory cache.
        val cacheSize = maxMemory / 8
        val bitmaCache: LruCache<String, Bitmap> = LruCache(cacheSize)
        for (i in 0 until size) {
            val holder = adapter.createViewHolder(view, adapter.getItemViewType(i))
            adapter.onBindViewHolder(holder, i)
            holder.itemView.measure(
                View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            holder.itemView.layout(
                0,
                0,
                holder.itemView.measuredWidth,
                holder.itemView.measuredHeight
            )
            holder.itemView.isDrawingCacheEnabled = true
            holder.itemView.buildDrawingCache()
            val drawingCache = holder.itemView.drawingCache
            if (drawingCache != null) {
                bitmaCache.put(i.toString(), drawingCache)
            }
            //                holder.itemView.setDrawingCacheEnabled(false);
//                holder.itemView.destroyDrawingCache();
            height += holder.itemView.measuredHeight
        }
        bigBitmap = Bitmap.createBitmap(view.measuredWidth, height, Bitmap.Config.ARGB_8888)
        val bigCanvas = Canvas(bigBitmap)
        bigCanvas.drawColor(Color.WHITE)
        for (i in 0 until size) {
            val bitmap: Bitmap = bitmaCache.get(i.toString())
            bigCanvas.drawBitmap(bitmap, 0f, iHeight.toFloat(), paint)
            iHeight += bitmap.height
            bitmap.recycle()
        }
    }

    return bigBitmap
}




