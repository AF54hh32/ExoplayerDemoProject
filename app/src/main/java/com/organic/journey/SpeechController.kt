package com.organic.journey

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

class SpeechController(
    context: Context,
    private val onSpeakingChanged: (Boolean) -> Unit,
    private val onMessage: (String) -> Unit
) {
    private var tts: TextToSpeech? = null
    private var ready = false
    private var pending: Pair<String, Locale>? = null
    private val main = Handler(Looper.getMainLooper())

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {
                        if (utteranceId == LAST_ID) main.post { onSpeakingChanged(false) }
                    }
                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        main.post { onSpeakingChanged(false) }
                    }
                })
                ready = true
                pending?.let { (text, locale) -> pending = null; speak(text, locale) }
            } else {
                onMessage("Text-to-speech is not available on this device.")
            }
        }
    }

    fun speak(text: String, locale: Locale) {
        val engine = tts
        if (!ready || engine == null) {
            pending = text to locale
            return
        }
        val result = engine.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            onMessage("No ${locale.displayLanguage} voice is installed. Add it in your device's text-to-speech settings.")
            onSpeakingChanged(false)
            return
        }
        engine.stop()
        val chunks = chunk(text, TextToSpeech.getMaxSpeechInputLength() - 100)
        chunks.forEachIndexed { i, c ->
            val id = if (i == chunks.lastIndex) LAST_ID else "chunk_$i"
            engine.speak(c, if (i == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD, null, id)
        }
        onSpeakingChanged(true)
    }

    fun stop() {
        tts?.stop()
        onSpeakingChanged(false)
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }

    /** Android limits how much text one speak() call accepts, so split at sentence boundaries. */
    private fun chunk(text: String, max: Int): List<String> {
        val out = mutableListOf<String>()
        val sb = StringBuilder()
        for (s in text.split(Regex("(?<=[.!?।。])\\s*|\\n+"))) {
            val sentence = s.trim()
            if (sentence.isEmpty()) continue
            if (sentence.length > max) {
                if (sb.isNotEmpty()) { out += sb.toString(); sb.clear() }
                sentence.chunked(max).forEach { out += it }
                continue
            }
            if (sb.length + sentence.length + 1 > max) { out += sb.toString(); sb.clear() }
            if (sb.isNotEmpty()) sb.append(' ')
            sb.append(sentence)
        }
        if (sb.isNotEmpty()) out += sb.toString()
        return out
    }

    private companion object {
        const val LAST_ID = "last_chunk"
    }
}