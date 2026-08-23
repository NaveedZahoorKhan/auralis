package com.auralis.reader

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object GoogleBooksClient {
    private const val TAG = "GoogleBooksClient"

    suspend fun getMetadata(title: String, author: String): BookMetadata? = withContext(Dispatchers.IO) {
        try {
            val queryParts = mutableListOf<String>()
            if (title.isNotBlank()) queryParts.add("intitle:\"${title}\"")
            if (author.isNotBlank() && author.lowercase() != "unknown author") queryParts.add("inauthor:\"${author}\"")

            if (queryParts.isEmpty()) return@withContext null

            val query = URLEncoder.encode(queryParts.joinToString(" "), "UTF-8")

            val urlString = "https://www.googleapis.com/books/v1/volumes?q=$query&maxResults=1"
            Log.d(TAG, "Requesting: $urlString")
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "Auralis/1.0")

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                val items = json.optJSONArray("items")
                if (items != null && items.length() > 0) {
                    val volumeInfo = items.getJSONObject(0).optJSONObject("volumeInfo")
                    if (volumeInfo != null) {
                        return@withContext BookMetadata(
                            title = volumeInfo.optString("title", title),
                            author = volumeInfo.optJSONArray("authors")?.optString(0) ?: author,
                            description = volumeInfo.optString("description", ""),
                            firstPublishYear = volumeInfo.optString("publishedDate", "").take(4).toIntOrNull()
                        )
                    }
                }
            } else {
                Log.e(TAG, "HTTP error: ${connection.responseCode}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch metadata from Google Books", e)
        }
        null
    }
}

data class BookMetadata(
    val title: String,
    val author: String,
    val description: String,
    val firstPublishYear: Int?
)
