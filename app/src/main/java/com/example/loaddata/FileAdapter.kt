package com.example.loaddata

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.loaddata.databinding.LayoutItemBinding


class FileAdapter(private val files: MutableList<Data>) :
    RecyclerView.Adapter<FileAdapter.ViewHolder>()  {

        class ViewHolder(val binding: LayoutItemBinding) : RecyclerView.ViewHolder(binding.root) {
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LayoutItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return files.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = files[position]

        when (file.type) {
            "jpg", "jpeg", "png", "gif" -> {Glide.with(holder.itemView.context)
                .load(file.link)
                .into(holder.binding.ivAvatar)
             }
            else -> {
                holder.binding.ivAvatar.setImageResource(R.drawable.ic_picture)
            }
        }

        with(holder.binding){
            tvName.text = file.name
            tvLink.text = file.link
            tvSize.text = "%.2f".format(file.size.toDouble()) + " MB"
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: Data) {
        files.add(0, newData)
        notifyItemInserted(0)
    }
}