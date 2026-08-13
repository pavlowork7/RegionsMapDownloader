package com.pavlo.regionsmapdownloader.data.queue

import android.content.Context
import androidx.core.content.edit
import com.pavlo.regionsmapdownloader.domain.data_source.DownloadQueueStorage
import com.pavlo.regionsmapdownloader.domain.model.DownloadQueueItem
import org.json.JSONArray
import org.json.JSONObject

class SharedPrefsDownloadQueueStorage(context: Context) : DownloadQueueStorage {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun load(): List<DownloadQueueItem> {
        val raw = prefs.getString(KEY_ITEMS, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).mapNotNull { index ->
                val item = array.optJSONObject(index) ?: return@mapNotNull null
                val downloadName = item.optString(FIELD_DOWNLOAD_NAME)
                val displayName = item.optString(FIELD_DISPLAY_NAME)
                if (downloadName.isEmpty()) null else DownloadQueueItem(downloadName, displayName)
            }
        }.getOrDefault(emptyList())
    }

    override fun save(items: List<DownloadQueueItem>) {
        val array = JSONArray()
        items.forEach { item ->
            array.put(
                JSONObject()
                    .put(FIELD_DOWNLOAD_NAME, item.downloadName)
                    .put(FIELD_DISPLAY_NAME, item.displayName)
            )
        }
        prefs.edit { putString(KEY_ITEMS, array.toString()) }
    }

    private companion object {
        const val PREFS_NAME = "download_queue"
        const val KEY_ITEMS = "items"
        const val FIELD_DOWNLOAD_NAME = "download_name"
        const val FIELD_DISPLAY_NAME = "display_name"
    }
}
