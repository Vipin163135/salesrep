package com.salesrep.app.ui.facingCheck

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Base64
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.github.fragivity.navigator
import com.github.fragivity.pop
import com.salesrep.app.R
import com.salesrep.app.base.BaseFragment
import com.salesrep.app.data.models.requests.Attachment
import com.salesrep.app.data.models.response.GetRouteAccountResponse
import com.salesrep.app.data.models.response.TaskData
import com.salesrep.app.data.repos.UserRepository
import com.salesrep.app.databinding.FragmentFacingCheckBinding
import com.salesrep.app.ui.dialogs.ProgressDialog
import com.salesrep.app.ui.facingCheck.adapter.ImagesAdapter
import com.salesrep.app.ui.home.HomeViewModel
import com.salesrep.app.util.*
import com.salesrep.app.util.DataTransferKeys.KEY_ATTACHMENTS
import com.salesrep.app.util.DataTransferKeys.KEY_NAME
import com.salesrep.app.util.PermissionUtils

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_facing_check.*
import permissions.dispatcher.*
import java.io.*
import javax.inject.Inject

@AndroidEntryPoint
@RuntimePermissions
class FacingCheckFragment : BaseFragment<FragmentFacingCheckBinding>() {

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository

    private lateinit var binding: FragmentFacingCheckBinding
    override val viewModel by viewModels<HomeViewModel>()
    private lateinit var progressDialog: ProgressDialog
    private var imagesList = arrayListOf<String>()
    private var attachmentList = arrayListOf<Attachment>()

    private lateinit var picturesAdapter: ImagesAdapter
    private var fileToUpload: File? = null

    private val products by lazy {
        arguments?.getParcelable<GetRouteAccountResponse>(
            DataTransferKeys.KEY_PRODUCTS
        )
    }

    private val task by lazy {
        arguments?.getParcelable<TaskData>(
            DataTransferKeys.KEY_TASK_DATA
        )
    }


    override fun getLayoutResId(): Int {
        return R.layout.fragment_facing_check
    }

    override fun onCreateView(instance: Bundle?, binding: FragmentFacingCheckBinding) {
        this.binding = binding

        progressDialog = ProgressDialog(requireActivity())

        initialize()
        listeners()
    }


    private fun listeners() {
        binding.tvBack.setOnClickListener {
            navigator.pop()
        }

        binding.tvTakePicture.setOnClickListener {
            getStorageWithPermissionCheck()
        }
        binding.tvEnd.setOnClickListener {
            if (TextUtils.isEmpty(etDescription.text)) {
                Toast.makeText(
                    requireContext(), getString(R.string.please_add_description),
                    Toast.LENGTH_SHORT
                ).show()

//            }else  if (attachmentList.isNullOrEmpty()){
//               Toast.makeText(requireContext(),getString(R.string.please_add_images),
//                   Toast.LENGTH_SHORT).show()

            } else {
                setFragmentResult(
                    AppRequestCode.CURRENT_TASK_FACECHECKING_STATUS_CHANGED,
                    bundleOf(
                        Pair(KEY_ATTACHMENTS, attachmentList),
                        Pair(KEY_NAME, etDescription.text.toString())
                    )
                )
                navigator.pop()
            }
        }
    }

    private fun initialize() {
        val isCompleted = arguments?.getBoolean(DataTransferKeys.KEY_IS_COMPLETED, false)
        binding.tvName.text = products?.Account?.name
        binding.tvCustomerNum.text = products?.Account?.accountname
        imagesList = arrayListOf()
        attachmentList = arrayListOf()
        picturesAdapter = ImagesAdapter(requireContext(), ::OnImageClick, isCompleted)
        binding.rvImages.adapter = picturesAdapter


        if (!task?.Attachments.isNullOrEmpty()) {
            task?.Attachments?.forEach { taskData ->
                imagesList.add(taskData.Attachment?.file ?: "")
            }
            picturesAdapter.notifyData(imagesList)
            binding.etDescription.setText(task?.Activity?.description)
        }

        if (isCompleted == true) {
            binding.tvTakePicture.gone()
            binding.tvEnd.gone()
            binding.etDescription.isFocusable=false
            binding.etDescription.isClickable=false
        }

    }

    private fun OnImageClick(pos: Int) {
        imagesList.removeAt(pos)
        attachmentList.removeAt(pos)
        picturesAdapter.notifyData(imagesList)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    @NeedsPermission(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun getStorage() {
        ImageUtils.displayImagePicker(this)
    }

    @OnShowRationale(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showStorageRationale(request: PermissionRequest) {
        PermissionUtils.showRationalDialog(requireContext(), R.string.media_permission, request)
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun onNeverAskAgainRationale() {
        PermissionUtils.showAppSettingsDialog(requireContext(), R.string.media_permission)
    }

    @OnPermissionDenied(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showDeniedForStorage() {
        PermissionUtils.showAppSettingsDialog(requireContext(), R.string.media_permission)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode == Activity.RESULT_OK) {

            when (requestCode) {
                ImageUtils.REQ_CODE_CAMERA_PICTURE -> {
                    fileToUpload = ImageUtils.getFile(requireContext())

                    fileToUpload?.let {
                        val extension = it.extension
                        lifecycleScope.launchWhenResumed {
                            val picString = "data:image/$extension;base64,${getBase64Image(it.path)}"
                            attachmentList.add(Attachment(picString, it.name, it.path))
                        }
                        imagesList.add(it.path)
                        picturesAdapter.notifyData(imagesList)
//                        val feedbackActivityModel = FeedbackActivityModel(
//                            imageList,
//                            "",
//                            ""
//                        )
//                        viewModel.createSrvFeedbackRequestApi(
//                            requireContext(),
//                            feedbackActivityModel,
//                            serviceId!!
//                        )

                    }

                }
                ImageUtils.REQ_CODE_GALLERY_PICTURE -> {

                    fileToUpload = ImageUtils.getImagePathFromGallery(
                        requireContext(), data?.data
                            ?: Uri.EMPTY
                    )


                    fileToUpload?.let {
                        val extension = it.extension

                        lifecycleScope.launchWhenResumed {

                            val picString = "data:image/$extension;base64,${getBase64Image(it.path)}"
                            attachmentList.add(Attachment(picString, it.name, it.path))

                        }
                        imagesList.add(it.path)
                        picturesAdapter.notifyData(imagesList)


                    }

//                    filesToUpload.add(fileToUpload!!)
//                    addImageAdapter.addList(filesToUpload)

                }
            }
        }
    }

    private suspend fun getBase64Image(path: String): String {
        val inputStream: InputStream =
            FileInputStream(path) // You can get an inputStream using any I/O API

        val bytes: ByteArray
        val buffer = ByteArray(8192)
        var bytesRead: Int
        val output = ByteArrayOutputStream()

        try {
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                output.write(buffer, 0, bytesRead)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        bytes = output.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

}