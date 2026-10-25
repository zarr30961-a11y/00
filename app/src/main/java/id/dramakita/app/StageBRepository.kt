package id.dramakita.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

internal data class WatchRecord(
    val seriesId: String,
    val seriesTitle: String,
    val episode: Int,
    val positionMs: Long,
    val durationMs: Long,
    val watchedAt: Long,
    val coverUrl: String
) {
    val progress: Float
        get() = if (durationMs > 0L) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
}

internal object StageBRepository {
    private const val PREFS = "dramakita"
    private const val HISTORY_KEY = "watch_history_v2"

    fun saveProgress(context: Context, drama: Drama, episode: Int, positionMs: Long, durationMs: Long) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val records = readHistory(context).filterNot { it.seriesId == drama.id && it.episode == episode }.toMutableList()
        records.add(WatchRecord(drama.id, drama.title, episode, positionMs.coerceAtLeast(0L), durationMs.coerceAtLeast(0L), System.currentTimeMillis(), drama.coverUrl.orEmpty()))
        val json = JSONArray()
        records.sortedByDescending { it.watchedAt }.take(100).forEach { record ->
            json.put(JSONObject().apply {
                put("seriesId", record.seriesId)
                put("seriesTitle", record.seriesTitle)
                put("episode", record.episode)
                put("positionMs", record.positionMs)
                put("durationMs", record.durationMs)
                put("watchedAt", record.watchedAt)
                put("coverUrl", record.coverUrl)
            })
        }
        prefs.edit().putString(HISTORY_KEY, json.toString()).apply()
        if (positionMs >= 10_000L) {
            val key = "watched_episode_ids"
            val watched = prefs.getStringSet(key, emptySet())?.toMutableSet() ?: mutableSetOf()
            watched.add("${drama.id}_$episode")
            prefs.edit().putStringSet(key, watched).apply()
        }
    }

    fun readHistory(context: Context): List<WatchRecord> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(HISTORY_KEY, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val item = array.optJSONObject(i) ?: continue
                    add(WatchRecord(
                        item.optString("seriesId"), item.optString("seriesTitle"),
                        item.optInt("episode", 1), item.optLong("positionMs"),
                        item.optLong("durationMs"), item.optLong("watchedAt"), item.optString("coverUrl")
                    ))
                }
            }.sortedByDescending { it.watchedAt }
        } catch (_: Exception) { emptyList() }
    }

    fun watchedEpisodeCount(context: Context): Int = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .getStringSet("watched_episode_ids", emptySet())?.size ?: 0

    fun badgeCount(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val streak = prefs.getInt("daily_streak", 0)
        var count = 0
        if (streak >= 3) count++
        if (streak >= 7) count++
        if (watchedEpisodeCount(context) >= 5) count++
        return count
    }

    fun checkInToday(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val today = localDayNumber()
        val lastDay = prefs.getLong("daily_checkin_day", -1L)
        var streak = prefs.getInt("daily_streak", 0)
        when {
            lastDay == today -> Unit
            lastDay == today - 1L -> streak++
            else -> streak = 1
        }
        prefs.edit().putLong("daily_checkin_day", today).putInt("daily_streak", streak).apply()
        return streak
    }

    fun hasCheckedInToday(context: Context): Boolean {
        val today = localDayNumber()
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getLong("daily_checkin_day", -1L) == today
    }

    private fun localDayNumber(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis / 86_400_000L
    }
}
