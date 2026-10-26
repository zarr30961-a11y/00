package id.dramakita.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** URL raw catalog.json milikmu. */
const val CATALOG_URL = "https://raw.githubusercontent.com/zarr30961-a11y/0/main/catalog.json"

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
    private val defaultColors = listOf(
        androidx.compose.ui.graphics.Color(0xFF58346D),
        androidx.compose.ui.graphics.Color(0xFF17121F)
    )

    /**
     * Mengambil katalog HANYA dari internet.
     * Mengembalikan null jika offline atau katalog gagal dimuat, sehingga tidak ada drama yang tampil.
     */
    suspend fun load(fallback: List<Drama>): List<Drama>? = withContext(Dispatchers.IO) {
        val remoteJson = try {
            val connection = (URL(CATALOG_URL).openConnection() as HttpURLConnection).apply {
                connectTimeout = 7000
                readTimeout = 7000
                requestMethod = "GET"
                useCaches = false
                setRequestProperty("Accept", "application/json")
            }
            try {
                if (connection.responseCode in 200..299) connection.inputStream.bufferedReader().use { it.readText() }
                else null
            } finally { connection.disconnect() }
        } catch (_: Exception) { null }

        if (remoteJson.isNullOrBlank()) return@withContext null
        val parsed = parse(remoteJson)
        if (parsed.isEmpty()) return@withContext null

        parsed.map { item ->
            val old = fallback.firstOrNull { it.id == item.id }
            Drama(
                id = item.id, title = item.title, category = item.category,
                synopsis = item.synopsis, colors = old?.colors ?: defaultColors,
                episodes = item.episodes.map { Episode(it.number, it.videoUrl) },
                cover = old?.cover, coverUrl = item.coverUrl
            )
        }
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
                            // Hanya link internet (http/https). Video lokal di dalam APK tidak dipakai lagi.
                            if (number > 0 && video.startsWith("http", true)) add(RemoteEpisode(number, video))
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
