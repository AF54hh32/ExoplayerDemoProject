package com.organic.journey

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.organic.journey.databinding.ItemPdfBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class PdfListAdapter(
    private val scope: CoroutineScope,
    private val onClick: (PdfItem) -> Unit
) : ListAdapter<PdfItem, PdfListAdapter.VH>(DIFF) {

    class VH(val binding: ItemPdfBinding) : RecyclerView.ViewHolder(binding.root) {
        var job: Job? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemPdfBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.binding.tvName.text = item.name
        holder.binding.ivThumb.setImageDrawable(null)
        holder.binding.root.setOnClickListener { onClick(item) }

        holder.job?.cancel()
        holder.job = scope.launch {
            val bitmap = runCatching { PdfThumbnails.get(item.file) }.getOrNull()
            if (bitmap != null) holder.binding.ivThumb.setImageBitmap(bitmap)
        }
    }

    override fun onViewRecycled(holder: VH) {
        holder.job?.cancel()
        holder.job = null
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<PdfItem>() {
            override fun areItemsTheSame(a: PdfItem, b: PdfItem) = a.file.path == b.file.path
            override fun areContentsTheSame(a: PdfItem, b: PdfItem) = a == b
        }
    }
}