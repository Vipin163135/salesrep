package com.salesrep.app.ui.documents

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import androidx.core.content.ContextCompat
import com.salesrep.app.R
import androidx.fragment.app.viewModels
import com.salesrep.app.base.BaseFragment
import com.salesrep.app.data.models.response.GetDocumentListData
import com.salesrep.app.databinding.FragmentDocumentViewDetailBinding
import com.salesrep.app.ui.dialogs.ProgressDialog
import com.salesrep.app.ui.home.HomeViewModel
import com.salesrep.app.util.DataTransferKeys
import com.salesrep.app.util.PermissionUtils
import com.salesrep.app.util.PrefsManager
import dagger.hilt.android.AndroidEntryPoint
import permissions.dispatcher.*
import java.io.File
import javax.inject.Inject


@RuntimePermissions
@AndroidEntryPoint
class DocumentDetailFragment : BaseFragment<FragmentDocumentViewDetailBinding>() {

    @Inject
    lateinit var prefsManager: PrefsManager

    private lateinit var binding: FragmentDocumentViewDetailBinding
    override val viewModel by viewModels<HomeViewModel>()
    private val data by lazy { arguments?.getParcelable<GetDocumentListData>(DataTransferKeys.KEY_DOCUMENT_DETAIL) }
    private lateinit var progressDialog: ProgressDialog

    override fun getLayoutResId(): Int {
        return R.layout.fragment_document_view_detail
    }

    override fun onCreateView(
        instance: Bundle?,
        binding: FragmentDocumentViewDetailBinding
    ) {
        this.binding = binding
        progressDialog = ProgressDialog(requireActivity())
        initialize()
    }

    private fun initialize() {

        val urlString = data?.Attachment?.file
        val file = File(
            "file:///storage/emulated/0/Android/data/com.salesrep.app/files/Download/",
            data?.Attachment?.file_name ?: ""
        )
        if (file.isFile) {
            openFolder()
        } else {

//            lifecycleScope.launch(Dispatchers.IO) {
//                val bitmap = Html2Bitmap.Builder().setContext(requireContext())
//                    .setContent(WebViewContent.html(urlString))
//                    .setBitmapWidth(1080)
//                    .build().bitmap
//
//                launch(Dispatchers.Main) {
//                    binding.webView.setImageBitmap(bitmap)
//                }
//            }

//            val webView = findViewById(R.id.mwebview) as WebView
//            val mStringUrl = "http://api.androidhive.info/images/sample.jpg"

            binding.webView.settings.loadWithOverviewMode = true;
            binding.webView.settings.useWideViewPort = true;
            binding.webView.settings.javaScriptEnabled = true
            binding.webView.loadUrl(urlString?:"")

//            binding.webView.loadData(urlString ?: "", "text/html", "UTF-8")
        }

        binding.imgDownload.setOnClickListener {
//            val printManager = ContextCompat.getSystemService(
//                requireContext(),
//                PrintManager::class.java
//            ) as PrintManager
//
//            val printAdapter: PrintDocumentAdapter = binding.webView.createPrintDocumentAdapter("Print test")
//
//            val jobName = getString(com.salesrep.app.R.string.app_name) + " Print Test"
//
//            printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
            getStorageWithPermissionCheck()
        }

    }


    fun downloadFile() {
        val DownloadUrl: String = data?.Attachment?.file ?: ""
        if (!DownloadUrl.isNullOrEmpty()) {
            val request1 = DownloadManager.Request(Uri.parse(DownloadUrl))
            request1.setDescription(data?.Attachment?.file_name) //appears the same in Notification bar while downloading
            request1.setTitle(data?.Attachment?.file_name ?: "File1.pdf")
            request1.setVisibleInDownloadsUi(false)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                request1.allowScanningByMediaScanner()
                request1.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)}
            request1.setDestinationInExternalFilesDir(
                requireActivity().applicationContext,
                Environment.DIRECTORY_DOWNLOADS,
                data?.Attachment?.file_name
            )
            val manager1 =
                requireActivity().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?
            val ref = manager1?.enqueue(request1)


        }

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    @NeedsPermission(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    fun getStorage() {
        val file = File(
            "file:///storage/emulated/0/Android/data/com.salesrep.app/files/Download/",
            data?.Attachment?.file_name ?: ""
        )
        if (file.isFile) {
            openFolder()
        } else {
            downloadFile()
        }
    }

    private fun openFolder() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)

        intent.setDataAndType(
            Uri.parse(
                Environment.getExternalStorageDirectory().path
                        + File.separator + Environment.DIRECTORY_DOWNLOADS + File.separator
            ), "file/*"
        )
        startActivityForResult(intent, 1234)
    }

    @OnShowRationale(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    fun showStorageRationale(request: PermissionRequest) {
        PermissionUtils.showRationalDialog(requireContext(), R.string.media_permission, request)
    }

    @OnNeverAskAgain(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    fun onNeverAskAgainRationale() {
        PermissionUtils.showAppSettingsDialog(requireContext(), R.string.media_permission)
    }

    @OnPermissionDenied(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    fun showDeniedForStorage() {
        PermissionUtils.showAppSettingsDialog(requireContext(), R.string.media_permission)
    }

}