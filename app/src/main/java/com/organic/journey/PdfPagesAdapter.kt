package com.organic.journey

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.organic.journey.databinding.ItemPdfPageBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class PdfPagesAdapter(
    private val scope: CoroutineScope,
    private val pageWidth: Int
) : RecyclerView.Adapter<PdfPagesAdapter.VH>() {

    private val items = mutableListOf<Pair<PdfDoc, Int>>()   // (document, page index)

    class VH(val binding: ItemPdfPageBinding) : RecyclerView.ViewHolder(binding.root) {
        var job: Job? = null
    }

    fun addDoc(doc: PdfDoc) {
        val start = items.size
        repeat(doc.pageCount) { items.add(doc to it) }
        notifyItemRangeInserted(start, doc.pageCount)
    }

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemPdfPageBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val (doc, pageIndex) = items[position]
        holder.job?.cancel()
        holder.binding.pageImage.setImageDrawable(null)
        holder.binding.pageImage.minimumHeight = (pageWidth * 1.4f).toInt() // avoids list jumping

        holder.job = scope.launch {
            val bitmap = doc.render(pageIndex, pageWidth)
            holder.binding.pageImage.minimumHeight = 0
            holder.binding.pageImage.setImageBitmap(bitmap)
        }
    }

    override fun onViewRecycled(holder: VH) {
        holder.job?.cancel()
        holder.job = null
    }
}