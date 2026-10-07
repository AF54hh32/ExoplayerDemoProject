package com.organic.journey

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.IOException
import java.util.UUID

data class PdfItem(val file: File, val name: String)

object PdfStore {

    private fun dir(context: Context) =
        File(context.filesDir, "pdfs").apply { mkdirs() }

    /** Copies the picked PDF into app storage under a unique file name. */
    fun copyIn(context: Context, uri: Uri): PdfItem {
        var name = "document.pdf"
        context.contentResolver
            .query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { c -> if (c.moveToFirst()) name = c.getString(0) ?: name }
        name = name.replace('/', '_')

        val out = File(dir(context), "${UUID.randomUUID()}_$name")
        context.contentResolver.openInputStream(uri)?.use { input ->
            out.outputStream().use { input.copyTo(it) }
        } ?: throw IOException("Cannot open this file")
        return PdfItem(out, name)
    }

    /** All saved PDFs, newest first. The display name is stored after the first "_". */
    fun list(context: Context): List<PdfItem> =
        dir(context).listFiles()
            .orEmpty()
            .sortedByDescending { it.lastModified() }
            .map { PdfItem(it, it.name.substringAfter('_')) }
}