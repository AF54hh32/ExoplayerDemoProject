package com.organic.journey

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.Closeable
import java.io.File

class PdfDoc(val file: File, val name: String) : Closeable {

    private val descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    private val renderer = PdfRenderer(descriptor)
    private val mutex = Mutex()

    val pageCount: Int get() = renderer.pageCount

    suspend fun render(index: Int, targetWidth: Int): Bitmap = withContext(Dispatchers.Default) {
        mutex.withLock {
            val page = renderer.openPage(index)
            try {
                val height = (targetWidth.toFloat() / page.width * page.height).toInt()
                val bitmap = Bitmap.createBitmap(targetWidth, height, Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bitmap
            } finally {
                page.close()
            }
        }
    }

    override fun close() {
        renderer.close()
        descriptor.close()
    }
}