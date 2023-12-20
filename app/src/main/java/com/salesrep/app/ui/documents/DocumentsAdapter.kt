package com.salesrep.app.ui.documents

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.salesrep.app.R
import com.salesrep.app.data.models.response.GetDocumentListData
import com.salesrep.app.databinding.ItemDocumentsBinding
import com.salesrep.app.databinding.ItemValueReconcilationBinding
import com.salesrep.app.ui.reconcilation.adapter.ValueReconcilationAdapter

class DocumentsAdapter(
    val context: Context,
    val onClick: (GetDocumentListData) -> Unit,
) :
    RecyclerView.Adapter<DocumentsAdapter.ViewHolder>() {

    private var documentList = listOf<GetDocumentListData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemDocumentsBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_documents, parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(documentList[position])
    }

    override fun getItemCount(): Int {
        return documentList.size
    }

    fun notifyData(list: List<GetDocumentListData>?) {
        this.documentList= listOf()
        list?.let {
            this.documentList= it
            notifyDataSetChanged()
        }
    }


    inner class ViewHolder(val binding: ItemDocumentsBinding) : RecyclerView.ViewHolder(binding.root) {
        val context = binding.root.context
        fun bind(valueTemplate: GetDocumentListData) {
            binding.tvCode.text = valueTemplate.Attachment.id.toString()
            binding.tvName.text= valueTemplate.Attachment.file_name
            binding.tvType.text= valueTemplate.Attachment.file_ext
            binding.tvDate.text= (valueTemplate.Attachment.created?:"").split(" ")[0]

            binding.root.setOnClickListener {
                onClick(valueTemplate)
            }
        }
    }
}