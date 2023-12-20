package com.salesrep.app.ui.facingCheck.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.salesrep.app.R
import com.salesrep.app.databinding.ItemPicturesBinding
import com.salesrep.app.util.loadImage
import kotlinx.android.synthetic.main.item_pictures.view.*

class ImagesAdapter(
    val context: Context,
    val onImageClick: (Int) -> Unit,
    val isCompleted: Boolean?
) :
    RecyclerView.Adapter<ImagesAdapter.ViewHolder>() {

    private var imagesList = arrayListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemPicturesBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_pictures, parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(imagesList[position])
        holder.itemView.ivDelete.setOnClickListener {
                onImageClick(position)
        }

    }

    override fun getItemCount(): Int {
        return imagesList.size
    }

    fun notifyData(list: ArrayList<String>?) {
        this.imagesList = arrayListOf()
        list?.let {
            this.imagesList = it
            notifyDataSetChanged()
        }
    }

   inner class ViewHolder(val binding: ItemPicturesBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(path: String) {
            loadImage(binding.root.context,
                binding.ivImage,
                path,
                path,
                null
            )

           binding.ivDelete.isVisible= isCompleted==false

        }
    }
}
