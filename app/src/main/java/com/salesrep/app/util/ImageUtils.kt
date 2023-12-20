package com.salesrep.app.util

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.salesrep.app.BuildConfig
import com.salesrep.app.R
//import com.salesrep.app.ui.auth.signup.OnImageRemoveListener
//import com.salesrep.app.ui.auth.signup.SignUpFirstStepFragment
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.jvm.Throws

object ImageUtils {
    val REQ_CODE_CAMERA_PICTURE = 1
    val REQ_CODE_GALLERY_PICTURE = 2

    val REQ_CODE_CAMERA_VIDEO = 3
    val REQ_CODE_GALLERY_VIDEO = 4
    const val REQ_CODE_GALLERY_DOC = 5

    private val TAG = ImageUtils::class.java.simpleName
    private var imageFile: File? = null
    private var videoFile: File? = null

    fun displayImagePicker(parentContext: Any) {
        var context: Context? = null
        if (parentContext is Fragment) {
            context = parentContext.context
        } else if (parentContext is Activity)
            context = parentContext

        if (context != null) {
            val pickerItems = arrayOf(
                context.getString(R.string.dialog_camera),
                context.getString(R.string.dialog_gallery),
                context.getString(android.R.string.cancel)
            )

            val builder = AlertDialog.Builder(context)
            builder.setTitle(context.getString(R.string.dialog_select_your_choice))
            builder.setItems(pickerItems) { dialog, which ->
                when (which) {
                    0 -> openCamera(parentContext)

                    1 -> openGallery(parentContext)
                }
                dialog.dismiss()
            }
            val alertDialog = builder.create()
            alertDialog.show()
        }
    }

    fun displayImagePickerWithRemoveOption(
        parentContext: Any,
//        listener: OnImageRemoveListener,
        type: String
    ) {
        var context: Context? = null
        if (parentContext is Fragment) {
            context = parentContext.context
        } else if (parentContext is Activity)
            context = parentContext

        if (context != null) {
            val pickerItems = arrayOf(
                context.getString(R.string.dialog_camera),
                context.getString(R.string.dialog_gallery),
                context.getString(R.string.dialog_remove_image),
                context.getString(android.R.string.cancel)
            )

            val builder = AlertDialog.Builder(context)
            builder.setTitle(context.getString(R.string.dialog_select_your_choice))
            builder.setItems(pickerItems) { dialog, which ->
                when (which) {
                    0 -> openCamera(parentContext)

                    1 -> openGallery(parentContext)

//                    2 -> listener.onImageRemove(type)
                }
                dialog.dismiss()
            }
            val alertDialog = builder.create()
            alertDialog.show()
        }
    }

    fun displayVideoPicker(parentContext: Any) {
        var context: Context? = null
        if (parentContext is Fragment) {
            context = parentContext.context
        } else if (parentContext is Activity)
            context = parentContext

        if (context != null) {
            val pickerItems = arrayOf(
                context.getString(R.string.dialog_camera),
                context.getString(R.string.dialog_gallery),
                context.getString(android.R.string.cancel)
            )

            val builder = AlertDialog.Builder(context)
            builder.setTitle(context.getString(R.string.dialog_select_your_choice))
            builder.setItems(pickerItems) { dialog, which ->
                when (which) {
                    0 -> openVideo(parentContext)

                    1 -> openVideoGallery(parentContext)
                }
                dialog.dismiss()
            }
            val alertDialog = builder.create()
            alertDialog.show()
        }
    }

    fun displayImageDocPicker(parentContext: Any) {
        var context: Context? = null
        if (parentContext is Fragment) {
            context = parentContext.context
        } else if (parentContext is Activity)
            context = parentContext

        openGalleryDoc(parentContext)
    }

    /*   fun displayPhotoVideoCapturePicker(parentContext: Any) {
           var context: Context? = null
           if (parentContext is Fragment) {
               context = parentContext.context
           } else if (parentContext is Activity)
               context = parentContext

           if (context != null) {
               val pickerItems = arrayOf(
                   context.getString(R.string.dialog_take_a_photo),
                   context.getString(R.string.dialog_take_a_video)
               )

               val builder = AlertDialog.Builder(context)
               builder.setTitle(context.getString(R.string.dialog_select_your_choice))
               builder.setItems(pickerItems) { dialog, which ->
                   when (which) {
                       0 -> openCamera(parentContext)

                       1 -> openVideo(parentContext)
                   }
                   dialog.dismiss()
               }
               val alertDialog = builder.create()
               alertDialog.show()
           }
       }*/


    /**
     * Open the device camera using the ACTION_IMAGE_CAPTURE intent
     *
     * @param uiReference Reference of the current ui.
     * Use either android.support.v7.app.AppCompatActivity or android.support
     * .v4.app.Fragment
     * //     * @param imageFile   Destination image file
     */
    fun openCamera(uiReference: Any) {
        var context: Context? = null
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            if (uiReference is Fragment)
                context = uiReference.context
            else if (uiReference is AppCompatActivity)
                context = uiReference
            if (context != null) {

                if (context.externalCacheDir != null)
                    imageFile = createImageFile(context.externalCacheDir?.path ?: "")
                // Put the uri of the image file as intent extra
                val imageUri = FileProvider.getUriForFile(
                    context,
                    BuildConfig.APPLICATION_ID + ".provider",
                    imageFile!!
                )

                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                //        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)

                // Get a list of all the camera apps
                val resolvedIntentActivities = context.packageManager
                    .queryIntentActivities(
                        cameraIntent, PackageManager
                            .MATCH_DEFAULT_ONLY
                    )

                // Grant uri read/write permissions to the camera apps
                for (resolvedIntentInfo in resolvedIntentActivities) {
                    val packageName = resolvedIntentInfo.activityInfo.packageName

                    context.grantUriPermission(
                        packageName, imageUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }

                if (uiReference is Fragment)
                    uiReference.startActivityForResult(
                        cameraIntent,
                        REQ_CODE_CAMERA_PICTURE
                    )
                else
                    (uiReference as AppCompatActivity).startActivityForResult(
                        cameraIntent,
                        REQ_CODE_CAMERA_PICTURE
                    )
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun getPathFromUri(context: Context, uri: Uri): String? {

        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            when {
                isExternalStorageDocument(uri) -> {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split =
                        docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val type = split[0]

                    return if ("primary".equals(type, ignoreCase = true)) {
                        Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                    } else {
                        val appsDir = ContextCompat.getExternalFilesDirs(context, null)
                        val extRootPaths = ArrayList<File>()
                        for (file in appsDir) {
                            extRootPaths.add(file.parentFile.parentFile.parentFile.parentFile)
                        }
                        extRootPaths[1].path + "/" + split[1]
                    }
                }
                isDownloadsDocument(uri) -> {

                    val id = DocumentsContract.getDocumentId(uri)
                    val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        java.lang.Long.valueOf(id)
                    )

                    return getDataColumn(context, contentUri, null, null)
                }
                isMediaDocument(uri) -> {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split =
                        docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val type = split[0]

                    var contentUri: Uri? = null
                    when (type) {
                        "image" -> contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        "video" -> contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        "audio" -> contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }

                    val selection = "_id=?"
                    val selectionArgs = arrayOf(split[1])

                    return getDataColumn(context, contentUri, selection, selectionArgs)
                }// MediaProvider
                // DownloadsProvider
            }// MediaProvider
            // DownloadsProvider
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {

            // Return the remote location
            return if (isGooglePhotosUri(uri)) uri.lastPathSegment else
                getDataColumn(context, uri, null, null)

        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        // File
        // MediaStore (and general)

        return null
    }

    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }
    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }
    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    private fun openVideo(uiReference: Any) {
        var context: Context? = null
        val takeVideoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0)
        takeVideoIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 20000000)
        takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30)
        try {
            if (uiReference is Fragment)
                context = uiReference.context
            else if (uiReference is AppCompatActivity)
                context = uiReference
            if (context != null) {

                if (context.externalCacheDir != null)
                    videoFile = createVideoFile(context.externalCacheDir?.path ?: "")
                // Put the uri of the image file as intent extra
                val imageUri = FileProvider.getUriForFile(
                    context,
                    BuildConfig.APPLICATION_ID + ".provider",
                    videoFile!!
                )

                takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)


                // Get a list of all the camera apps
                val resolvedIntentActivities = context.packageManager
                    .queryIntentActivities(
                        takeVideoIntent, PackageManager
                            .MATCH_DEFAULT_ONLY
                    )

                // Grant uri read/write permissions to the camera apps
                for (resolvedIntentInfo in resolvedIntentActivities) {
                    val packageName = resolvedIntentInfo.activityInfo.packageName

                    context.grantUriPermission(
                        packageName, imageUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }

                if (uiReference is Fragment)
                    uiReference.startActivityForResult(
                        takeVideoIntent,
                        REQ_CODE_CAMERA_VIDEO
                    )
                else
                    (uiReference as AppCompatActivity).startActivityForResult(
                        takeVideoIntent,
                        REQ_CODE_CAMERA_VIDEO
                    )
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }


    private fun openGalleryDoc(uiReference: Any) {
        val galleryIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        galleryIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        galleryIntent.type = "application/pdf"
        galleryIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        if (uiReference is AppCompatActivity) {
            uiReference.startActivityForResult(galleryIntent, REQ_CODE_GALLERY_DOC)
        } else if (uiReference is Fragment) {
            uiReference.startActivityForResult(galleryIntent, REQ_CODE_GALLERY_DOC)
        }
    }

    fun openGallery(uiReference: Any) {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )


        if (uiReference is AppCompatActivity)
            uiReference.startActivityForResult(
                galleryIntent,
                REQ_CODE_GALLERY_PICTURE
            )
        else if (uiReference is Fragment)
            uiReference.startActivityForResult(
                galleryIntent,
                REQ_CODE_GALLERY_PICTURE
            )
    }

    private fun openVideoGallery(uiReference: Any) {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        )
        galleryIntent.type = "video/*"

        galleryIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30)

        if (uiReference is AppCompatActivity)
            uiReference.startActivityForResult(
                galleryIntent,
                REQ_CODE_GALLERY_VIDEO
            )
        else if (uiReference is Fragment)
            uiReference.startActivityForResult(
                galleryIntent,
                REQ_CODE_GALLERY_VIDEO
            )
    }

    fun getImagePathFromGallery(context: Context, imageUri: Uri): File? {
        var imagePath: String? = null
        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = context.contentResolver.query(
            imageUri,
            filePathColumn, null, null, null
        )
        if (cursor != null) {
            cursor.moveToFirst()
            val columnIndex = cursor.getColumnIndex(filePathColumn[0])
            imagePath = cursor.getString(columnIndex)
            cursor.close()
        }

        return File(imagePath)
    }

    private fun saveImageToExternalStorage(context: Context, finalBitmap: Bitmap?): File? {
        val file2: File
        val mediaStorageDir = File(context.externalCacheDir?.path)
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null
            }
        }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        file2 = File(
            mediaStorageDir.path + File.separator
                    + "IMG_" + timeStamp + ".jpg"
        )

        try {
            val out = FileOutputStream(file2)
            finalBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.flush()
            out.close()
            return file2
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return file2
    }


    fun getVideoPathFromGallery(context: Context, videoUri: Uri): File? {
        var videoPath: String? = null
        val filePathColumn = arrayOf(MediaStore.Video.Media.DATA)
        val cursor = context.contentResolver.query(
            videoUri,
            filePathColumn, null, null, null
        )
        if (cursor != null) {
            cursor.moveToFirst()
            val columnIndex = cursor.getColumnIndex(filePathColumn[0])
            videoPath = cursor.getString(columnIndex)
            cursor.close()
        }

//        return saveVideoToExternalStorage(context, getFile(videoPath))
        return File(videoPath)
    }

    private fun saveVideoToExternalStorage(context: Context, finalBitmap: Bitmap?): File? {
        val file2: File
        val mediaStorageDir = File(context.externalCacheDir?.path)
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null
            }
        }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        file2 = File(
            mediaStorageDir.path + File.separator
                    + "VID_" + timeStamp + ".mp4"
        )

        try {
            val out = FileOutputStream(file2)
            finalBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.flush()
            out.close()
            return file2
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return file2
    }

    private fun getFile(imgPath: String?): Bitmap? {
        var bMapRotate: Bitmap? = null
        try {

            if (imgPath != null) {
                val exif = ExifInterface(imgPath)

                val mOrientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 1
                )

                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                BitmapFactory.decodeFile(imgPath, options)
                options.inSampleSize = calculateInSampleSize(options, 400, 400)
                options.inJustDecodeBounds = false

                bMapRotate = BitmapFactory.decodeFile(imgPath, options)
                when (mOrientation) {
                    6 -> {
                        val matrix = Matrix()
                        matrix.postRotate(90f)
                        bMapRotate = Bitmap.createBitmap(
                            bMapRotate!!, 0, 0,
                            bMapRotate.width, bMapRotate.height,
                            matrix, true
                        )
                    }
                    8 -> {
                        val matrix = Matrix()
                        matrix.postRotate(270f)
                        bMapRotate = Bitmap.createBitmap(
                            bMapRotate!!, 0, 0,
                            bMapRotate.width, bMapRotate.height,
                            matrix, true
                        )
                    }
                    3 -> {
                        val matrix = Matrix()
                        matrix.postRotate(180f)
                        bMapRotate = Bitmap.createBitmap(
                            bMapRotate!!, 0, 0,
                            bMapRotate.width, bMapRotate.height,
                            matrix, true
                        )
                    }
                }
            }

        } catch (e: OutOfMemoryError) {
            bMapRotate = null
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: Exception) {
            bMapRotate = null
            e.printStackTrace()
        }

        return bMapRotate
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int, reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize > reqHeight && halfWidth / inSampleSize > reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    /*  @NonNull
    public static String getFacebookProfileImage(@NonNull String profileId)
    {
        return "https://graph.facebook.com/" + profileId + "/picture?type=large";
    }*/

    /*private void cropAndRotateImage(Fragment fragment, File sourceFile, File destinationFile)
    {
        Context context = fragment.getContext();
        if (sourceFile != null && destinationFile != null)
        {
            Uri sourceFileUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID +
            ".provider",
                    sourceFile);

            Uri destinationFileUri = FileProvider.getUriForFile(context, BuildConfig
            .APPLICATION_ID + ".provider",
                    destinationFile);

            CropImage.activity(sourceFileUri)
                    .setOutputCompressFormat(Bitmap.CompressFormat.JPEG)
                    .setAspectRatio(1, 1)
                    .setMinCropResultSize(700, 700)
                    .setRequestedSize(2000, 2000)
                    .setOutputCompressQuality(90)
                    .setOutputUri(destinationFileUri)
                    .start(context, fragment);
        } else
            Log.e(TAG, "Source or destination file is null");
    }*/

    @Throws(IOException::class)
    fun createImageFile(directory: String): File? {
        var imageFile: File? = null
        if (Environment.MEDIA_MOUNTED == Environment
                .getExternalStorageState()
        ) {
            val storageDir = File(directory)
            if (!storageDir.mkdirs()) {
                if (!storageDir.exists()) {
                    Log.d(TAG, "Failed to create directory")
                    return null
                }
            }
            val imageFileName = "JPEG_" + System.currentTimeMillis() + "_"

            imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)
        }
        return imageFile
    }

    @Throws(IOException::class)
    private fun createVideoFile(directory: String): File? {
        var videoFile: File? = null
        if (Environment.MEDIA_MOUNTED == Environment
                .getExternalStorageState()
        ) {
            val storageDir = File(directory)
            if (!storageDir.mkdirs()) {
                if (!storageDir.exists()) {
                    Log.d(TAG, "Failed to create directory")
                    return null
                }
            }
            val videoFileName = "VID_" + System.currentTimeMillis() + "_"

            videoFile = File.createTempFile(videoFileName, ".mp4", storageDir)
        }
        return videoFile
    }

    /**
     * Revoke access permission for the content URI to the specified package otherwise the
     * permission won't be
     * revoked until the device restarts.
     */
    /* public static void revokeUriPermission(Context context, File file)
    {
        Log.d(TAG, "Uri permission revoked");
        context.revokeUriPermission(FileProvider.getUriForFile(context,
                BuildConfig.APPLICATION_ID + ".provider", file),
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
    }*/
    fun getFile(context: Context): File? {
        return if (imageFile != null)
            saveImageToExternalStorage(context, getFile(imageFile!!.path))
        else
            null
    }

    fun getVideoFile(context: Context): File? {
        return if (videoFile != null)
            File(videoFile!!.path)
        else
            null
    }

    private fun getDataColumn(context: Context, uri: Uri?, selection: String?,
                              selectionArgs: Array<String>?): String? {

        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)

        try {
            cursor = uri?.let { context.contentResolver.query(it, projection, selection, selectionArgs, null) }
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    private fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }

    fun getBitmapFromBase64String(encodedImage: String): Bitmap? {
        val decodedString = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
    }
}
