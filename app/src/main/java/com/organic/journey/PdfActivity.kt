package com.organic.journey

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.organic.journey.databinding.ActivityMapBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PdfActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMapBinding
    private lateinit var listAdapter: PdfListAdapter

    private val pickPdfs = registerForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isEmpty()) return@registerForActivityResult
        lifecycleScope.launch {
            for (uri in uris) {
                try {
                    val item = withContext(Dispatchers.IO) { PdfStore.copyIn(this@PdfActivity, uri) }
                    try {
                        PdfThumbnails.get(item.file)   // also checks the PDF can be opened
                    } catch (e: Exception) {
                        item.file.delete()
                        throw e
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        this@PdfActivity,
                        e.message ?: "Could not open this PDF",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            refreshList()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        listAdapter = PdfListAdapter(lifecycleScope) { item ->
            PdfViewerActivity.start(this, item)
        }
        binding.rvPdfs.layoutManager = LinearLayoutManager(this)
        binding.rvPdfs.adapter = listAdapter

        binding.btAddPdf.setOnClickListener {
            pickPdfs.launch(arrayOf("application/pdf"))
        }

        refreshList()
    }

    private fun refreshList() {
        val items = PdfStore.list(this)
        listAdapter.submitList(items)
        binding.tvEmpty.isVisible = items.isEmpty()
    }
}