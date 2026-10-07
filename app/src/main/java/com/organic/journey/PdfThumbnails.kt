package com.organic.journey

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object PdfThumbnails {

    private const val THUMB_WIDTH = 200
    private val cache = LruCache<String, Bitmap>(40)

    suspend fun get(file: File): Bitmap = withContext(Dispatchers.Default) {
        cache.get(file.path)?.let { return@withContext it }

        val bitmap = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
            val renderer = PdfRenderer(pfd)
            try {
                val page = renderer.openPage(0)
                try {
                    val h = (THUMB_WIDTH.toFloat() / page.width * page.height).toInt()
                    val bmp = Bitmap.createBitmap(THUMB_WIDTH, h, Bitmap.Config.ARGB_8888)
                    bmp.eraseColor(Color.WHITE)
                    page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bmp
                } finally {
                    page.close()
                }
            } finally {
                renderer.close()
            }
        }
        cache.put(file.path, bitmap)
        bitmap
    }
}