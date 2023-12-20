package com.salesrep.app.ui.documents

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.github.fragivity.navigator
import com.github.fragivity.popToRoot
import com.github.fragivity.push
import com.salesrep.app.R
import com.salesrep.app.base.BaseFragment
import com.salesrep.app.data.models.response.GetDocumentListData
import com.salesrep.app.data.models.response.GetDocumentListResponse
import com.salesrep.app.data.network.responseUtil.ApisRespHandler
import com.salesrep.app.data.network.responseUtil.Status
import com.salesrep.app.data.repos.UserRepository
import com.salesrep.app.databinding.FragmentDocumentsBinding
import com.salesrep.app.ui.dialogs.ProgressDialog
import com.salesrep.app.ui.inventory.InventoryViewModel
import com.salesrep.app.util.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DocumentsFragment : BaseFragment<FragmentDocumentsBinding>() {

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository

    private lateinit var binding: FragmentDocumentsBinding
    override val viewModel by viewModels<InventoryViewModel>()
    private lateinit var progressDialog: ProgressDialog
    private lateinit var adapter: DocumentsAdapter

    private var page: Int = 1
    private var totalProducts: Int = 0

    var documentList = ArrayList<GetDocumentListData>()

    override fun getLayoutResId(): Int {
        return R.layout.fragment_documents
    }

    override fun onCreateView(
        instance: Bundle?,
        binding: FragmentDocumentsBinding
    ) {
        this.binding = binding

        progressDialog = ProgressDialog(requireActivity())
        initialize()
        bindObservers()
        listeners()

    }

    private fun listeners() {
        binding.tvBack.setOnClickListener {
            navigator.popToRoot()
        }

        binding.rvInventory.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

            if ((totalProducts > documentList.size)  && !recyclerView.canScrollVertically(1)) {
                    page++
                    hitProductsApi()
                }
            }
        })

    }


    private fun hitProductsApi() {
        viewModel.getDocumentListApi(requireContext(),  page, PER_PAGE)
    }

    private fun initialize() {
        adapter= DocumentsAdapter(requireContext(),::onClick)
        binding.rvInventory.adapter= adapter
        hitProductsApi()
    }

    private fun  onClick(document: GetDocumentListData){
        val bundle= Bundle()
        bundle.putParcelable(DataTransferKeys.KEY_DOCUMENT_DETAIL,document)

//        findNavController().navigate(R.id.webViewFragment,bundle)
        navigator.push(DocumentDetailFragment::class){
            this.arguments = bundle
        }
    }
    @SuppressLint("FragmentLiveDataObserve")
    private fun bindObservers() {
        viewModel.getPagedTeamDocuments.setObserver(viewLifecycleOwner, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.LOADING -> progressDialog.setLoading(true)
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)
                    if (it.data != null) {
                        setList(it.data)
//                        if (page==1){
//                            totalProducts= it.data.pagination.size
//                        }
                    }

                }
                Status.ERROR -> {
                    progressDialog.setLoading(false)
                    ApisRespHandler.handleError(
                        it.error,
                        requireActivity(),
                        prefsManager = prefsManager
                    )
                }
            }
        })


    }

    private fun setList(data: GetDocumentListResponse) {
        if (page==1) {
            documentList.clear()
            totalProducts= data.pagination.size
        }
        data.rows.forEach {
            documentList.add(it)
        }
        adapter.notifyData(documentList)
    }
}