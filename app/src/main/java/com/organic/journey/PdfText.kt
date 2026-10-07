package com.organic.journey

import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object PdfText {
    suspend fun pages(file: File): List<String> = withContext(Dispatchers.IO) {
        PDDocument.load(file).use { doc ->
            val stripper = PDFTextStripper()
            (1..doc.numberOfPages).map { p ->
                stripper.startPage = p
                stripper.endPage = p
                stripper.getText(doc).trim()
            }
        }
    }

}