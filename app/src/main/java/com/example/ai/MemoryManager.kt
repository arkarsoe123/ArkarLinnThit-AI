package com.example.ai

import android.content.Context
import com.example.model.NoteItem
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Small, privacy-friendly local memory store. Data stays in app private
 * SharedPreferences and is never uploaded by this class.
 */
class MemoryManager(context: Context) {
    private val prefs = context.getSharedPreferences("arkar_ai_preferences", Context.MODE_PRIVATE)
    private val key = "memory_notes"

    fun load(): List<NoteItem> = runCatching {
        val raw = prefs.getString(key, null) ?: return emptyList()
        val array = JSONArray(raw)
        buildList {
            for (i in 0 until array.length()) {
                val o = array.optJSONObject(i) ?: continue
                add(NoteItem(
                    id = o.optString("id").ifBlank { UUID.randomUUID().toString() },
                    title = o.optString("title", "မှတ်စု"),
                    content = o.optString("content"),
                    colorHex = o.optLong("colorHex", 0xFF0284C7),
                    dateText = o.optString("dateText"),
                    tag = o.optString("tag", "အထွေထွေ")
                ))
            }
        }
    }.getOrDefault(emptyList())

    fun save(notes: List<NoteItem>) {
        runCatching {
            val array = JSONArray()
            notes.take(200).forEach { n ->
                array.put(JSONObject().apply {
                    put("id", n.id); put("title", n.title); put("content", n.content)
                    put("colorHex", n.colorHex); put("dateText", n.dateText); put("tag", n.tag)
                })
            }
            prefs.edit().putString(key, array.toString()).apply()
        }
    }

    fun search(query: String, notes: List<NoteItem> = load()): List<NoteItem> {
        val q = query.trim().lowercase()
        if (q.isBlank()) return notes
        return notes.filter { "${it.title} ${it.content} ${it.tag}".lowercase().contains(q) }
    }
}
