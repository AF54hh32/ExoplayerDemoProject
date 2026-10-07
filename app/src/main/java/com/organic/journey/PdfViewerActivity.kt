package com.organic.journey

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.organic.journey.databinding.ActivityPdfViewerBinding
import java.io.File

class PdfViewerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfViewerBinding
    private val vm: AssistantViewModel by viewModels()   // the sheet shares this same instance
    private var doc: PdfDoc? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPdfViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val path = intent.getStringExtra(EXTRA_PATH)
        val name = intent.getStringExtra(EXTRA_NAME).orEmpty()
        if (path == null) {
            finish()
            return
        }
        binding.tvTitle.text = name

        try {
            val pdf = PdfDoc(File(path), name)
            doc = pdf

            val layoutManager = LinearLayoutManager(this)
            val adapter = PdfPagesAdapter(lifecycleScope, resources.displayMetrics.widthPixels)
            binding.rvPages.layoutManager = layoutManager
            binding.rvPages.adapter = adapter
            adapter.addDoc(pdf)
            updatePageInfo(1, pdf.pageCount)

            binding.rvPages.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                    val first = layoutManager.findFirstVisibleItemPosition()
                    if (first >= 0) {
                        vm.currentPage = first + 1
                        updatePageInfo(first + 1, pdf.pageCount)
                    }
                }
            })
        } catch (e: Exception) {
            Toast.makeText(this, e.message ?: "Could not open this PDF", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        vm.loadText(File(path))
        vm.textStatus.observe(this) { status ->
            val ready = status == TextStatus.READY
            binding.btSummarize.isEnabled = ready
            binding.btTranslate.isEnabled = ready
            binding.btListen.isEnabled = ready
            when (status) {
                TextStatus.NO_TEXT -> Toast.makeText(
                    this, "No selectable text (maybe a scan), so AI features are unavailable.",
                    Toast.LENGTH_LONG
                ).show()
                TextStatus.FAILED -> Toast.makeText(
                    this, "Could not read text from this PDF.", Toast.LENGTH_LONG
                ).show()
                else -> {}
            }
        }

        binding.btSummarize.setOnClickListener {
            AssistantSheet.show(supportFragmentManager, AssistantSheet.Mode.SUMMARY)
        }
        binding.btTranslate.setOnClickListener {
            AssistantSheet.show(supportFragmentManager, AssistantSheet.Mode.TRANSLATE)
        }
        binding.btListen.setOnClickListener {
            AssistantSheet.show(supportFragmentManager, AssistantSheet.Mode.LISTEN)
        }
    }

    private fun updatePageInfo(page: Int, total: Int) {
        binding.tvPageInfo.text = "Page $page / $total"
    }

    override fun onDestroy() {
        doc?.close()
        super.onDestroy()
    }

    companion object {
        private const val EXTRA_PATH = "path"
        private const val EXTRA_NAME = "name"

        fun start(context: Context, item: PdfItem) {
            context.startActivity(
                Intent(context, PdfViewerActivity::class.java)
                    .putExtra(EXTRA_PATH, item.file.absolutePath)
                    .putExtra(EXTRA_NAME, item.name)
            )
        }
    }
}