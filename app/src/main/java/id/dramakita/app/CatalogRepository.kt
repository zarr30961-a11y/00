package id.dramakita.app

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** Ubah URL ini menjadi URL raw catalog.json milikmu. */
const val CATALOG_URL = "https://raw.githubusercontent.com/USERNAME/REPOSITORY/main/catalog.json"

internal data class RemoteEpisode(val number: Int, val videoUrl: String)
internal data class RemoteSeries(
    val id: String,
    val title: String,
    val category: String,
    val synopsis: String,
    val coverUrl: String,
    val episodes: List<RemoteEpisode>
)

internal object CatalogRepository {
    private const val PREFS = "dramakita"
    private const val CACHE_KEY = "catalog_json_cache"

    suspend fun load(context: Context, fallback: List<Drama>): List<Drama> = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val remoteJson = try {
            val connection = (URL(CATALOG_URL).openConnection() as HttpURLConnection).apply {
                connectTimeout = 7000
                readTimeout = 7000
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
            }
            try {
                if (connection.responseCode in 200..299) connection.inputStream.bufferedReader().use { it.readText() }
                else null
            } finally { connection.disconnect() }
        } catch (_: Exception) { null }

        if (!remoteJson.isNullOrBlank()) {
            val parsed = parse(remoteJson)
            if (parsed.isNotEmpty()) {
                prefs.edit().putString(CACHE_KEY, remoteJson).apply()
                return@withContext parsed.map { item ->
                    val old = fallback.firstOrNull { it.id == item.id }
                    Drama(
                        id = item.id, title = item.title, category = item.category,
                        synopsis = item.synopsis, colors = old?.colors ?: listOf(androidx.compose.ui.graphics.Color(0xFF58346D), androidx.compose.ui.graphics.Color(0xFF17121F)),
                        episodes = item.episodes.map { Episode(it.number, it.videoUrl) }, cover = old?.cover, coverUrl = item.coverUrl
                    )
                }
            }
        }

        val cached = prefs.getString(CACHE_KEY, null)
        if (!cached.isNullOrBlank()) {
            val parsed = parse(cached)
            if (parsed.isNotEmpty()) return@withContext parsed.map { item ->
                val old = fallback.firstOrNull { it.id == item.id }
                Drama(item.id, item.title, item.category, item.synopsis,
                    old?.colors ?: listOf(androidx.compose.ui.graphics.Color(0xFF58346D), androidx.compose.ui.graphics.Color(0xFF17121F)),
                    item.episodes.map { Episode(it.number, it.videoUrl) }, old?.cover, item.coverUrl)
            }
        }
        fallback
    }

    private fun parse(raw: String): List<RemoteSeries> {
        return try {
        val root = JSONObject(raw)
        val array = root.optJSONArray("series") ?: root.optJSONArray("catalog") ?: return emptyList()
        buildList {
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                val id = obj.optString("id").trim()
                val title = obj.optString("judul", obj.optString("title")).trim()
                if (id.isBlank() || title.isBlank()) continue
                val eps = obj.optJSONArray("episodes") ?: obj.optJSONArray("daftarEpisode") ?: continue
                val episodeList = buildList {
                    for (j in 0 until eps.length()) {
                        val ep = eps.optJSONObject(j) ?: continue
                        val number = ep.optInt("nomor", ep.optInt("number", j + 1))
                        val video = ep.optString("videoUrl", ep.optString("video_url", ep.optString("url"))).trim()
                        if (number > 0 && (video.startsWith("http", true) || video.startsWith("android.resource://"))) {
                            add(RemoteEpisode(number, video))
                        }
                    }
                }.sortedBy { it.number }
                if (episodeList.isEmpty()) continue
                add(RemoteSeries(
                    id = id,
                    title = title,
                    category = obj.optString("kategori", obj.optString("category", "Drama")),
                    synopsis = obj.optString("sinopsis", obj.optString("synopsis", "")),
                    coverUrl = obj.optString("coverUrl", obj.optString("cover_url", "")),
                    episodes = episodeList
                ))
            }
        }
        } catch (_: Exception) { emptyList() }
    }
}
