package com.organic.journey

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

object ClaudeClient {
    private const val URL = "https://api.anthropic.com/v1/messages"
    private const val MODEL = "claude-sonnet-5-5"

    private val http = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build()

    suspend fun complete(system: String, prompt: String): String = withContext(Dispatchers.IO) {
        val key = BuildConfig.CLAUDE_API_KEY
        check(key.isNotBlank()) { "Add CLAUDE_API_KEY=your_key to local.properties and rebuild." }

        val body = JSONObject()
            .put("model", MODEL)
            .put("max_tokens", 4096)
            .put("system", system)
            .put("messages", JSONArray().put(
                JSONObject().put("role", "user").put("content", prompt)))

        val request = Request.Builder()
            .url(URL)
            .header("x-api-key", key)
            .header("anthropic-version", "2023-06-01")
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .build()

        http.newCall(request).execute().use { resp ->
            val raw = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) {
                val msg = runCatching {
                    JSONObject(raw).getJSONObject("error").getString("message")
                }.getOrDefault("Request failed (HTTP ${resp.code})")
                throw IOException(msg)
            }
            val content = JSONObject(raw).getJSONArray("content")
            buildString {
                for (i in 0 until content.length()) {
                    val block = content.getJSONObject(i)
                    if (block.optString("type") == "text") append(block.getString("text"))
                }
            }.trim()
        }
    }
}