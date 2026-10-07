package com.organic.journey

import android.app.Application
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader

class OurApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PDFBoxResourceLoader.init(applicationContext)
    }
}