package com.kdr.nuven

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

sealed class LoadResult {
    object Loading : LoadResult()
    data class Success(val data: String, val lastUpdated: String) : LoadResult()
    data class Error(val message: String) : LoadResult()
}

object DataManager {

    private const val TAG = "DataManager"

    private val _loadState = MutableLiveData<LoadResult>()
    val loadState: LiveData<LoadResult> = _loadState

    private val _pagesLiveData = MutableLiveData<String?>()
    val pagesLiveData: LiveData<String?> = _pagesLiveData

    private var dataHash: String? = null
    var pages: String? = null
        private set

    var lastUpdated: String? = null
        private set

    fun loadInitialData() {
        _loadState.postValue(LoadResult.Loading)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val (content, hash, lastUpdated) = downloadAndHash()
                pages = content
                dataHash = hash
                DataManager.lastUpdated = lastUpdated
                _pagesLiveData.postValue(content)
                _loadState.postValue(LoadResult.Success(content, lastUpdated))
            } catch (e: Exception) {
                Log.e(TAG, "Error loading initial data", e)
                _loadState.postValue(LoadResult.Error("Error loading data: ${e.message}"))
            }
        }
    }

    suspend fun refreshData(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val (newContent, newHash, newLastUpdated) = downloadAndHash()
                if (newHash != dataHash) {
                    pages = newContent
                    dataHash = newHash
                    lastUpdated = newLastUpdated
                    _pagesLiveData.postValue(newContent)
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing data", e)
                false
            }
        }
    }

    fun notifyDataChanged() {
        // This function is called by RefreshWorker when data is updated.
        // It triggers observers of pagesLiveData.
        _pagesLiveData.postValue(pages) // Post current pages to trigger observers
    }

    private fun downloadAndHash(): Triple<String, String, String> {
        val url = URL(AppConfig.dataUrl)
        val connection = url.openConnection() as HttpURLConnection
        val content = StringBuilder()
        try {
            connection.connect()
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            reader.forEachLine { line ->
                content.append(line)
            }
        } finally {
            connection.disconnect()
        }
        val contentString = content.toString()
        val hash = contentString.toSha256()

        // Fetch commit date
        val apiConnection = URL(AppConfig.githubCommitsApiUrl).openConnection() as HttpURLConnection
        var lastUpdated = ""
        try {
            apiConnection.connect()
            val apiReader = BufferedReader(InputStreamReader(apiConnection.inputStream))
            val apiResponse = apiReader.readText()
            val jsonArray = org.json.JSONArray(apiResponse)
            if (jsonArray.length() > 0) {
                val commit = jsonArray.getJSONObject(0).getJSONObject("commit")
                val author = commit.getJSONObject("author")
                lastUpdated = author.getString("date")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching commit date", e)
        } finally {
            apiConnection.disconnect()
        }

        return Triple(contentString, hash, lastUpdated)
    }

    private fun String.toSha256(): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(this.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
