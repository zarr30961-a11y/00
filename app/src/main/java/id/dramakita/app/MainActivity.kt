package id.dramakita.app

import android.content.pm.ActivityInfo
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File
import java.io.FileOutputStream
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.Image
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.HeadsetMic
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import coil.compose.AsyncImage
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import android.content.SharedPreferences
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay

internal data class Episode(val number: Int, val videoUrl: String)
internal data class Drama(
    val id: String, val title: String, val category: String,
    val synopsis: String, val colors: List<Color>, val episodes: List<Episode>,
    val cover: Int? = null, val coverUrl: String? = null
)

private val sampleVideo = "https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
private val dramas = listOf(
    Drama("d4", "Tukang Mancing Terkuat S1", "Fantasi",
        "Menceritakan kisah Jiang Tayuan, seorang pria yang sekilas tampak hanya sebagai nelayan biasa di sebuah desa, namun sebenarnya memiliki kemampuan luar biasa untuk memancing makhluk-makhluk supranatural, termasuk raja iblis, menggunakan peralatan pancingnya.",
        listOf(Color(0xFF1B4F9C), Color(0xFF07142E)),
        listOf(Episode(1, "android.resource://id.dramakita.app/raw/tukang_mancing_s1_e1")),
        cover = R.drawable.cover_tukang_mancing),
    Drama("d1", "Janji di Musim Hujan", "Romansa",
        "Dua orang asing bertemu saat hujan dan menemukan bahwa masa lalu mereka ternyata saling terhubung.",
        listOf(Color(0xFF9B4766), Color(0xFF30213D)), (1..5).map { Episode(it, sampleVideo) }),
    Drama("d2", "Rumah yang Kita Rindukan", "Keluarga",
        "Keluarga yang lama terpisah belajar memaafkan dan membangun kembali rumah yang pernah mereka tinggalkan.",
        listOf(Color(0xFFB77B46), Color(0xFF3F3026)), (1..5).map { Episode(it, sampleVideo) }),
    Drama("d3", "Rahasia Kamar 09", "Misteri",
        "Sebuah surat tanpa nama membuka rangkaian petunjuk yang mengubah kehidupan penghuni sebuah apartemen.",
        listOf(Color(0xFF477B83), Color(0xFF172A38)), (1..5).map { Episode(it, sampleVideo) })
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(13, 8, 32)
        window.navigationBarColor = android.graphics.Color.rgb(14, 8, 37)
        setContent { DramaKitaApp() }
    }
}

@Composable
private fun DramaKitaApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("dramakita", 0) }
    var page by remember { mutableStateOf("home") }
    var catalog by remember { mutableStateOf(dramas) }
    var selectedDrama by remember { mutableStateOf(dramas.first()) }
    LaunchedEffect(Unit) { catalog = CatalogRepository.load(context, dramas) }
    var selectedEpisode by remember { mutableIntStateOf(1) }
    var favorites by remember {
        mutableStateOf(prefs.getStringSet("favorites", emptySet())?.toSet() ?: emptySet())
    }
    var lastWatched by remember {
        mutableStateOf(prefs.getString("last_drama", "") ?: "")
    }
    var lastEpisode by remember { mutableIntStateOf(prefs.getInt("last_episode", 1)) }
    var search by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Semua") }
    var backPage by remember { mutableStateOf("home") }
    var historyRefresh by remember { mutableIntStateOf(0) }

    BackHandler(enabled = page != "home") {
        page = when (page) { "player" -> "detail"; "detail" -> backPage; "history" -> backPage; else -> "home" }
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF8B3CF0),
            background = Color(0xFF0D0820),
            surface = Color(0xFF1A1236),
            onBackground = Color(0xFFF7F4F5),
            onSurface = Color(0xFFF7F4F5)
        )
    ) {
        when (page) {
            "player" -> PlayerScreen(
                drama = selectedDrama,
                episodeNumber = selectedEpisode,
                onBack = { page = "detail" },
                onEpisodeChange = { episode ->
                    selectedEpisode = episode.coerceIn(1, selectedDrama.episodes.size)
                    prefs.edit().putString("last_drama", selectedDrama.id)
                        .putInt("last_episode", selectedEpisode).apply()
                    lastWatched = selectedDrama.id
                    lastEpisode = selectedEpisode
                },
                onSaveProgress = { position, duration ->
                    prefs.edit().putString("last_drama", selectedDrama.id)
                        .putInt("last_episode", selectedEpisode)
                        .putLong("position_${selectedDrama.id}_$selectedEpisode", position).apply()
                    if (duration > 0L && position > 0L) {
                        StageBRepository.saveProgress(context, selectedDrama, selectedEpisode, position, duration)
                        historyRefresh++
                    }
                    lastWatched = selectedDrama.id
                    lastEpisode = selectedEpisode
                }
            )
            "detail" -> DetailScreen(
                drama = selectedDrama,
                favorite = favorites.contains(selectedDrama.id),
                resumeEpisode = if (lastWatched == selectedDrama.id) lastEpisode else 1,
                onBack = { page = backPage },
                onFavorite = {
                    favorites = if (favorites.contains(selectedDrama.id)) favorites - selectedDrama.id
                    else favorites + selectedDrama.id
                    prefs.edit().putStringSet("favorites", favorites).apply()
                },
                onPlay = { episode -> selectedEpisode = episode; page = "player" }
            )
            "profile" -> ProfileScreen(
                onNavigate = { destination -> if (destination == "history") backPage = "profile"; page = destination },
                watchedCount = StageBRepository.watchedEpisodeCount(context),
                favoriteCount = favorites.size,
                listCount = remember(historyRefresh, catalog, favorites) {
                    val ids = StageBRepository.readHistory(context).map { it.seriesId }.toSet() + favorites
                    catalog.count { it.id in ids }
                },
                badgeCount = StageBRepository.badgeCount(context)
            )
            "history" -> HistoryScreen(
                records = remember(historyRefresh, catalog) { StageBRepository.readHistory(context) },
                catalog = catalog,
                onBack = { page = backPage },
                onDramaEpisode = { drama, episode -> backPage = "history"; selectedDrama = drama; selectedEpisode = episode; page = "player" },
                onNavigate = { destination -> page = destination }
            )
            "gift" -> GiftScreen(onNavigate = { destination -> page = destination })
            "search" -> SearchScreen(
                dramas = catalog,
                search = search,
                onSearch = { search = it },
                category = category,
                onCategory = { category = it },
                onDrama = { selectedDrama = it; backPage = "search"; page = "detail" },
                onNavigate = { destination -> page = destination }
            )
            "mylist", "favorites" -> MyListScreen(
                dramas = catalog,
                records = remember(historyRefresh, catalog) { StageBRepository.readHistory(context) },
                favorites = favorites,
                initialFilter = if (page == "favorites") "Favorit" else "Semua",
                onDrama = { selectedDrama = it; backPage = page; page = "detail" },
                onPlay = { drama, episode ->
                    backPage = page
                    selectedDrama = drama
                    selectedEpisode = episode.coerceIn(1, drama.episodes.size)
                    page = "player"
                },
                onRemoveFavorite = { id ->
                    favorites = favorites - id
                    prefs.edit().putStringSet("favorites", favorites).apply()
                },
                onRemoveHistory = { id ->
                    StageBRepository.removeSeries(context, id)
                    if (lastWatched == id) {
                        lastWatched = ""
                        prefs.edit().remove("last_drama").apply()
                    }
                    historyRefresh++
                },
                onNavigate = { destination -> page = destination }
            )
            else -> HomeScreen(
                dramas = catalog,
                search = search,
                onSearch = { search = it },
                category = category,
                onCategory = { category = it },
                onDrama = { selectedDrama = it; backPage = "home"; page = "detail" },
                onNavigate = { destination -> page = destination }
            )
        }
    }
}

private val AppBg = Color(0xFF0D0820)
private val BarBg = Color(0xFF0E0825)
private val PanelBg = Color(0xFF140D2E)
private val CardBg = Color(0xFF120B29)
private val Coral = Color(0xFFA06BFF)
private val Purple = Color(0xFF8B3CF0)
private val PurpleLight = Color(0xFFA56BFF)
private val TitlePurple = Color(0xFFA64DF5)
private val Amber = Color(0xFFFFA726)
private val Muted = Color(0xFFA99BCB)
private val LineColor = Color(0xFF2B2057)
private val ChipBorder = Color(0xFF3A2D6B)

private val ThinChevron: ImageVector by lazy {
    ImageVector.Builder(defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(9f, 5f); lineTo(16f, 12f); lineTo(9f, 19f)
        }
    }.build()
}

private val CrownFilled: ImageVector by lazy {
    ImageVector.Builder(defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
        path(fill = SolidColor(Color.Black), pathFillType = PathFillType.EvenOdd) {
            moveTo(2.5f, 8f); lineTo(7.5f, 12f); lineTo(12f, 4.5f); lineTo(16.5f, 12f); lineTo(21.5f, 8f)
            lineTo(19.5f, 19f); lineTo(4.5f, 19f); close()
            moveTo(12f, 11.5f); lineTo(14f, 14f); lineTo(12f, 16.5f); lineTo(10f, 14f); close()
        }
        path(fill = SolidColor(Color.Black)) {
            moveTo(5f, 20.5f); lineTo(19f, 20.5f); lineTo(19f, 22f); lineTo(5f, 22f); close()
        }
    }.build()
}

private fun readSearchHistory(prefs: SharedPreferences): List<String> =
    prefs.getString("search_history", "")?.split("\n")?.filter { it.isNotBlank() } ?: emptyList()

private fun writeSearchHistory(prefs: SharedPreferences, list: List<String>) {
    prefs.edit().putString("search_history", list.joinToString("\n")).apply()
}

private fun badgeIcon(badge: String): ImageVector? = when (badge) {
    "Populer" -> Icons.Filled.LocalFireDepartment
    "Terbaru" -> Icons.Filled.AutoAwesome
    "Paling Baru" -> Icons.Filled.NewReleases
    "Favorit" -> Icons.Filled.Favorite
    else -> null
}

private fun secondTag(category: String): String? = when (category) {
    "Fantasi" -> "Aksi"
    "Romansa", "Keluarga" -> "Drama"
    "Misteri" -> "Thriller"
    else -> null
}

@Composable
private fun GradientTitle(first: String, second: String, size: Int = 30) {
    Row {
        Text(first, color = Color.White, fontSize = size.sp, fontWeight = FontWeight.ExtraBold)
        Text(second, color = TitlePurple, fontSize = size.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun SearchField(value: String, onChange: (String) -> Unit, glow: Boolean = false, onDone: () -> Unit = {}) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Cari judul serial...", color = Muted) },
        leadingIcon = { Icon(Icons.Outlined.Search, null, tint = Muted) },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onDone() }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PurpleLight,
            unfocusedBorderColor = if (glow) Color(0xFF5B44C7) else ChipBorder,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = PurpleLight
        )
    )
}

@Composable
private fun CategoryChips(selected: String, onSelect: (String) -> Unit) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("Semua", "Romansa", "Keluarga", "Misteri", "Fantasi").forEach { c ->
            val on = c == selected
            Box(
                Modifier.clip(RoundedCornerShape(50))
                    .then(
                        if (on) Modifier.background(Brush.horizontalGradient(listOf(Color(0xFF7B3FE4), Color(0xFF9B4DFF))))
                        else Modifier.border(1.dp, ChipBorder, RoundedCornerShape(50))
                    )
                    .clickable { onSelect(c) }
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(c, color = if (on) Color.White else Color(0xFFCFC6EA), fontSize = 14.sp,
                    fontWeight = if (on) FontWeight.Bold else FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String, icon: ImageVector? = null, iconTint: Color = PurpleLight,
    action: String? = null, onAction: () -> Unit = {}
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        if (action != null) {
            Row(Modifier.clickable { onAction() }, verticalAlignment = Alignment.CenterVertically) {
                Text(action, color = PurpleLight, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Icon(ThinChevron, null, tint = PurpleLight, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun BadgePill(text: String, icon: ImageVector?, modifier: Modifier = Modifier) {
    Row(
        modifier.background(Color(0xFF7C3AED), RoundedCornerShape(50)).padding(horizontal = 9.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(13.dp))
            Spacer(Modifier.width(4.dp))
        }
        Text(text, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SeriesCard(drama: Drama, badge: String?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(16.dp)
    Column(modifier.clip(shape).background(CardBg).border(1.dp, LineColor, shape).clickable { onClick() }) {
        Box {
            Poster(
                drama, Modifier.fillMaxWidth().aspectRatio(1.42f),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                alignment = BiasAlignment(0f, 0.55f)
            )
            if (badge != null) BadgePill(badge, badgeIcon(badge), Modifier.align(Alignment.TopStart).padding(8.dp))
        }
        Column(Modifier.padding(horizontal = 10.dp, vertical = 10.dp)) {
            Text(drama.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(5.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.PlayCircle, null, tint = PurpleLight, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("${drama.episodes.size} episode • ${drama.category}", color = Muted, fontSize = 12.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun HomeScreen(
    dramas: List<Drama>, search: String, onSearch: (String) -> Unit,
    category: String, onCategory: (String) -> Unit,
    onDrama: (Drama) -> Unit, onNavigate: (String) -> Unit
) {
    var showNotif by remember { mutableStateOf(false) }
    val filtered = dramas.filter {
        it.title.contains(search, true) && (category == "Semua" || it.category == category)
    }
    val rows = filtered.chunked(2)
    val badges = listOf("Populer", "Terbaru", "Paling Baru")
    Scaffold(containerColor = AppBg, bottomBar = { DrakooBottomBar("home", onNavigate) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            item {
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        GradientTitle("Drama", "Kita", 30)
                        Text("Cerita singkat, rasa yang panjang.", color = Muted, fontSize = 14.sp)
                    }
                    Box(Modifier.clickable { showNotif = true }.padding(8.dp)) {
                        Icon(Icons.Outlined.Notifications, "Notifikasi", tint = Color(0xFFD9CCFF), modifier = Modifier.size(28.dp))
                        Box(Modifier.align(Alignment.TopEnd).size(9.dp).background(PurpleLight, CircleShape))
                    }
                }
                Spacer(Modifier.height(16.dp))
                SearchField(search, onSearch)
                Spacer(Modifier.height(14.dp))
                CategoryChips(category, onCategory)
                Spacer(Modifier.height(24.dp))
                SectionHeader("Pilihan untukmu", action = "Lihat Semua") { onCategory("Semua"); onSearch("") }
                Spacer(Modifier.height(12.dp))
            }
            if (filtered.isEmpty()) {
                item { Text("Belum ada serial di sini.", color = Muted, modifier = Modifier.padding(vertical = 28.dp)) }
            }
            items(rows.size) { r ->
                Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rows[r].forEachIndexed { k, d ->
                        SeriesCard(d, badges.getOrNull(r * 2 + k), { onDrama(d) }, Modifier.weight(1f))
                    }
                    if (rows[r].size == 1) Spacer(Modifier.weight(1f))
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
    if (showNotif) {
        AlertDialog(
            onDismissRequest = { showNotif = false }, containerColor = PanelBg,
            title = { Text("Notifikasi", color = Color.White) },
            text = { Text("Belum ada notifikasi baru.", color = Muted) },
            confirmButton = { TextButton(onClick = { showNotif = false }) { Text("Tutup", color = PurpleLight) } }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchScreen(
    dramas: List<Drama>, search: String, onSearch: (String) -> Unit,
    category: String, onCategory: (String) -> Unit,
    onDrama: (Drama) -> Unit, onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("dramakita", 0) }
    var history by remember { mutableStateOf(readSearchHistory(prefs)) }
    fun saveTerm(term: String) {
        val t = term.trim()
        if (t.isBlank()) return
        history = (listOf(t) + history.filter { !it.equals(t, true) }).take(8)
        writeSearchHistory(prefs, history)
    }
    val results = dramas.filter {
        it.title.contains(search, true) && (category == "Semua" || it.category == category)
    }
    val rankColors = listOf(Color(0xFF8B3CF0), Color(0xFFF0467A), Color(0xFFF5A524), Color(0xFF3B9BF0), Color(0xFF8B5CF6))

    Scaffold(containerColor = AppBg, bottomBar = { DrakooBottomBar("search", onNavigate) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            item {
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        GradientTitle("Sear", "ch", 30)
                        Text("Cari judul, author, atau genre favoritmu...", color = Muted, fontSize = 14.sp)
                    }
                    Icon(Icons.Outlined.Search, null, tint = PurpleLight, modifier = Modifier.padding(top = 6.dp, end = 4.dp).size(28.dp))
                }
                Spacer(Modifier.height(16.dp))
                SearchField(search, onSearch, glow = true, onDone = { saveTerm(search) })
                Spacer(Modifier.height(14.dp))
                CategoryChips(category, onCategory)
                Spacer(Modifier.height(22.dp))
            }
            if (history.isNotEmpty()) {
                item {
                    SectionHeader("Riwayat Pencarian", icon = Icons.Outlined.Schedule, action = "Hapus Semua") {
                        history = emptyList(); writeSearchHistory(prefs, history)
                    }
                    Spacer(Modifier.height(12.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        history.forEach { term ->
                            Row(
                                Modifier.clip(RoundedCornerShape(50)).background(CardBg)
                                    .border(1.dp, ChipBorder, RoundedCornerShape(50))
                                    .clickable { onSearch(term) }
                                    .padding(start = 12.dp, end = 10.dp, top = 9.dp, bottom = 9.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.Search, null, tint = Muted, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(term, color = Color(0xFFE6DFF7), fontSize = 14.sp, maxLines = 1,
                                    overflow = TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = 190.dp))
                                Spacer(Modifier.width(8.dp))
                                Icon(Icons.Filled.Close, "Hapus", tint = Muted,
                                    modifier = Modifier.size(16.dp).clickable {
                                        history = history.filter { it != term }; writeSearchHistory(prefs, history)
                                    })
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
            item {
                SectionHeader("Pencarian Populer", icon = Icons.Filled.LocalFireDepartment,
                    iconTint = Color(0xFFB45CFF), action = "Lihat Semua") { onSearch(""); onCategory("Semua") }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    dramas.take(5).forEachIndexed { index, d ->
                        Column(Modifier.width(100.dp).clickable { onDrama(d) }) {
                            Box {
                                Poster(d, Modifier.fillMaxWidth().height(80.dp).border(1.dp, LineColor, RoundedCornerShape(12.dp)),
                                    shape = RoundedCornerShape(12.dp), alignment = BiasAlignment(0f, 0.4f))
                                Box(
                                    Modifier.align(Alignment.TopStart).padding(6.dp).size(22.dp)
                                        .background(rankColors[index % rankColors.size], RoundedCornerShape(6.dp)),
                                    contentAlignment = Alignment.Center
                                ) { Text("${index + 1}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(d.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                                maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 17.sp)
                        }
                    }
                }
                Spacer(Modifier.height(22.dp))
                HorizontalDivider(color = LineColor)
                Spacer(Modifier.height(20.dp))
                SectionHeader(if (search.isBlank()) "Hasil Pencarian Terbaru" else "Hasil Pencarian",
                    icon = Icons.Outlined.Schedule, action = "Lihat Semua") { onSearch(""); onCategory("Semua") }
                Spacer(Modifier.height(12.dp))
                if (results.isEmpty()) Text("Tidak ada hasil.", color = Muted, modifier = Modifier.padding(vertical = 12.dp))
            }
            items(results.size) { index ->
                val d = results[index]
                val shape = RoundedCornerShape(14.dp)
                Row(
                    Modifier.fillMaxWidth().padding(bottom = 10.dp).height(60.dp).clip(shape).background(CardBg)
                        .border(1.dp, LineColor, shape)
                        .clickable { saveTerm(search); onDrama(d) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Poster(d, Modifier.width(72.dp).fillMaxHeight(),
                        shape = RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp))
                    Column(Modifier.weight(1f).padding(start = 14.dp)) {
                        Text(d.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Spacer(Modifier.height(3.dp))
                        Text("${d.category}  ·  ${d.episodes.size} episode", color = Muted, fontSize = 13.sp)
                    }
                    Icon(ThinChevron, null, tint = Color(0xFFCFC6EA), modifier = Modifier.padding(end = 14.dp).size(22.dp))
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

private data class DrakooTab(val id: String, val label: String, val icon: ImageVector, val iconOn: ImageVector, val activeColor: Color)

@Composable
private fun DrakooBottomBar(selected: String, onNavigate: (String) -> Unit) {
    val tabs = listOf(
        DrakooTab("home", "Beranda", Icons.Outlined.Home, Icons.Filled.Home, Color(0xFF9B6BFF)),
        DrakooTab("search", "Search", Icons.Outlined.Search, Icons.Filled.Search, Color(0xFFA56BFF)),
        DrakooTab("gift", "Hadiah", Icons.Outlined.CardGiftcard, Icons.Filled.CardGiftcard, Color(0xFFA56BFF)),
        DrakooTab("mylist", "Daftar Saya", Icons.Outlined.BookmarkBorder, Icons.Filled.Bookmark, Amber),
        DrakooTab("profile", "Profil", Icons.Outlined.Person, Icons.Filled.Person, Color(0xFFB44DF0))
    )
    val barShape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
    Column(Modifier.fillMaxWidth().clip(barShape).background(BarBg).border(1.dp, LineColor, barShape)) {
        Row(Modifier.fillMaxWidth().navigationBarsPadding().height(62.dp)) {
            tabs.forEach { tab ->
                val on = tab.id == selected
                val tint = if (on) tab.activeColor else Color(0xFF9A8EC2)
                Box(
                    Modifier.weight(1f).fillMaxHeight().clickable { onNavigate(tab.id) },
                    contentAlignment = Alignment.Center
                ) {
                    if (on && tab.id == "profile") {
                        Box(Modifier.align(Alignment.TopCenter).width(46.dp).height(3.dp)
                            .background(tint, RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp)))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(if (on) tab.iconOn else tab.icon, contentDescription = tab.label, tint = tint, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.height(3.dp))
                        Text(tab.label, color = tint, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileScreen(
    onNavigate: (String) -> Unit,
    watchedCount: Int,
    favoriteCount: Int,
    listCount: Int,
    badgeCount: Int
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("dramakita", 0) }
    var avatarPath by remember { mutableStateOf(prefs.getString("profile_avatar_path", "") ?: "") }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) {
            try {
                val target = File(context.filesDir, "profile_avatar.jpg")
                context.contentResolver.openInputStream(uri)?.use { input -> FileOutputStream(target).use { output -> input.copyTo(output) } }
                if (target.exists() && target.length() > 0L) {
                    avatarPath = target.absolutePath
                    prefs.edit().putString("profile_avatar_path", avatarPath).apply()
                }
            } catch (_: Exception) { }
        }
    }
    var displayName by remember { mutableStateOf(prefs.getString("profile_name", "Pengguna Drakoo") ?: "Pengguna Drakoo") }
    var bio by remember { mutableStateOf(prefs.getString("profile_bio", "Nikmati setiap cerita 💜") ?: "") }
    var editName by remember { mutableStateOf("") }
    var editBio by remember { mutableStateOf("") }
    var showEdit by remember { mutableStateOf(false) }
    var showHelp by remember { mutableStateOf(false) }
    val openEdit = { editName = displayName; editBio = bio; showEdit = true }
    val handle = "@" + displayName.trim().lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_').ifBlank { "pengguna" }
    val hasAvatar = avatarPath.isNotBlank() && File(avatarPath).exists()
    val panelShape = RoundedCornerShape(24.dp)

    Scaffold(containerColor = AppBg, bottomBar = { DrakooBottomBar("profile", onNavigate) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (hasAvatar) {
                AsyncImage(model = File(avatarPath), contentDescription = null, contentScale = ContentScale.Crop,
                    alpha = 0.22f, modifier = Modifier.fillMaxWidth().height(300.dp))
                Box(Modifier.fillMaxWidth().height(300.dp).background(Brush.verticalGradient(listOf(Color.Transparent, AppBg))))
            } else {
                Box(Modifier.fillMaxWidth().height(260.dp).background(Brush.verticalGradient(listOf(Color(0xFF2A1459), AppBg))))
            }
            LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
                item {
                    Spacer(Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(34.dp).clip(RoundedCornerShape(9.dp))
                                .background(Brush.linearGradient(listOf(Color(0xFF7B3FE4), Color(0xFFB44DF0)))),
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Filled.PlayArrow, null, tint = Color.White, modifier = Modifier.size(22.dp)) }
                        Text("Drakoo", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White,
                            modifier = Modifier.weight(1f).padding(start = 10.dp))
                        IconButton(onClick = { onNavigate("search") }) {
                            Icon(Icons.Outlined.Search, "Cari", tint = Color.White, modifier = Modifier.size(26.dp))
                        }
                        IconButton(onClick = { showHelp = true }) {
                            Icon(Icons.Filled.Settings, "Pengaturan", tint = Color.White, modifier = Modifier.size(26.dp))
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(96.dp).clickable { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                            Box(
                                Modifier.fillMaxSize().clip(CircleShape)
                                    .background(Brush.sweepGradient(listOf(Color(0xFF8B3CF0), Color(0xFFFF4DA6), Color(0xFF8B3CF0))))
                                    .padding(3.dp)
                            ) {
                                Box(Modifier.fillMaxSize().clip(CircleShape).background(PanelBg), contentAlignment = Alignment.Center) {
                                    if (hasAvatar) {
                                        AsyncImage(model = File(avatarPath), contentDescription = "Foto profil",
                                            contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                    } else {
                                        Icon(Icons.Filled.Person, null, tint = Color(0xFFE6DFF7), modifier = Modifier.size(52.dp))
                                    }
                                }
                            }
                            Box(
                                Modifier.align(Alignment.BottomEnd).offset(x = 2.dp, y = 2.dp).size(32.dp)
                                    .clip(CircleShape).background(PanelBg).border(2.dp, AppBg, CircleShape),
                                contentAlignment = Alignment.Center
                            ) { Icon(Icons.Filled.PhotoCamera, null, tint = Color.White, modifier = Modifier.size(17.dp)) }
                        }
                        Column(Modifier.weight(1f).padding(start = 18.dp).clickable { openEdit() }) {
                            Text(displayName, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Spacer(Modifier.height(2.dp))
                            Text(handle, color = Muted, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Spacer(Modifier.height(8.dp))
                            Text(bio, color = Color(0xFFC9B8F5), fontSize = 15.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                        Icon(ThinChevron, null, tint = Muted, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.height(22.dp))
                    Row(Modifier.fillMaxWidth().height(54.dp), verticalAlignment = Alignment.CenterVertically) {
                        val stats = listOf(
                            Triple(Icons.Outlined.Visibility, watchedCount.toString(), "Ditonton"),
                            Triple(Icons.Outlined.BookmarkBorder, listCount.toString(), "Daftar Tonton"),
                            Triple(Icons.Outlined.FavoriteBorder, favoriteCount.toString(), "Favorit"),
                            Triple(Icons.Outlined.VerifiedUser, badgeCount.toString(), "Lencana")
                        )
                        stats.forEachIndexed { index, stat ->
                            if (index > 0) VerticalDivider(color = LineColor, modifier = Modifier.height(40.dp))
                            ProfileStat(stat.first, stat.second, stat.third, Modifier.weight(1f))
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                }
                item {
                    Column(Modifier.fillMaxWidth().clip(panelShape).background(PanelBg).border(1.dp, LineColor, panelShape).padding(horizontal = 14.dp)) {
                        ProfileRow(Icons.Filled.Person, Color(0xFFFF5C9D), "Profil Saya", "Ubah nama, bio, dan foto profil") { openEdit() }
                        HorizontalDivider(color = LineColor, modifier = Modifier.padding(start = 44.dp))
                        ProfileRow(Icons.Filled.Bookmark, Color(0xFFB05CFF), "Daftar Tonton Saya", "Drama yang sedang kamu tonton") { onNavigate("mylist") }
                        HorizontalDivider(color = LineColor, modifier = Modifier.padding(start = 44.dp))
                        ProfileRow(Icons.Filled.Favorite, Color(0xFFFF4D8D), "Drama Favorit Saya", "Koleksi drama favorit kamu") { onNavigate("favorites") }
                        HorizontalDivider(color = LineColor, modifier = Modifier.padding(start = 44.dp))
                        ProfileRow(Icons.Outlined.History, Color(0xFF9B6BFF), "Riwayat Tontonan", "Drama yang pernah kamu tonton") { onNavigate("history") }
                    }
                    Spacer(Modifier.height(18.dp))
                    Column(Modifier.fillMaxWidth().clip(panelShape).background(PanelBg).border(1.dp, LineColor, panelShape).padding(horizontal = 14.dp)) {
                        ProfileRow(CrownFilled, Color(0xFFFFB020), "Langganan Premium", "Nikmati fitur eksklusif tanpa iklan") { showHelp = true }
                        HorizontalDivider(color = LineColor, modifier = Modifier.padding(start = 44.dp))
                        ProfileRow(Icons.Filled.HeadsetMic, Color(0xFF4C8DFF), "Pusat Bantuan", "Panduan dan pertanyaan umum") { showHelp = true }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
    if (showEdit) {
        AlertDialog(onDismissRequest = { showEdit = false }, containerColor = PanelBg,
            title = { Text("Profil Saya", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Nama") })
                    OutlinedTextField(value = editBio, onValueChange = { editBio = it }, label = { Text("Bio") })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    displayName = editName.trim().ifBlank { displayName }
                    bio = editBio.trim()
                    prefs.edit().putString("profile_name", displayName).putString("profile_bio", bio).apply()
                    showEdit = false
                }) { Text("Simpan", color = PurpleLight) }
            },
            dismissButton = { TextButton(onClick = { showEdit = false }) { Text("Batal", color = Muted) } })
    }
    if (showHelp) {
        AlertDialog(onDismissRequest = { showHelp = false }, containerColor = PanelBg,
            title = { Text("Segera hadir", color = Color.White) },
            text = { Text("Fitur ini akan ditambahkan pada versi berikutnya.", color = Muted) },
            confirmButton = { TextButton(onClick = { showHelp = false }) { Text("Mengerti", color = PurpleLight) } })
    }
}

@Composable
private fun ProfileStat(icon: ImageVector, value: String, label: String, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = PurpleLight, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(8.dp))
            Text(value, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        }
        Spacer(Modifier.height(2.dp))
        Text(label, color = Muted, fontSize = 12.sp, maxLines = 1)
    }
}

@Composable
private fun ProfileRow(icon: ImageVector, tint: Color, title: String, subtitle: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(44.dp)) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(28.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, color = Muted, fontSize = 13.sp)
        }
        Icon(ThinChevron, null, tint = Muted, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun GiftScreen(onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("dramakita", 0) }
    var streak by remember { mutableIntStateOf(prefs.getInt("daily_streak", 0)) }
    var checkedIn by remember { mutableStateOf(StageBRepository.hasCheckedInToday(context)) }
    var badgeCount by remember { mutableIntStateOf(StageBRepository.badgeCount(context)) }
    Scaffold(containerColor = AppBg, bottomBar = { DrakooBottomBar("gift", onNavigate) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(20.dp)) {
            item {
                Text("Hadiah Harian", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(6.dp))
                Text("Absen setiap hari untuk menjaga streak dan membuka lencana.", color = Muted)
                Spacer(Modifier.height(22.dp))
                Column(Modifier.fillMaxWidth().background(PanelBg, RoundedCornerShape(24.dp)).padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.LocalFireDepartment, null, tint = Coral, modifier = Modifier.size(58.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("$streak hari beruntun", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                    Text(if (checkedIn) "Absen hari ini sudah selesai" else "Jangan lupa absen hari ini", color = Muted)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = {
                        streak = StageBRepository.checkInToday(context)
                        checkedIn = true
                        badgeCount = StageBRepository.badgeCount(context)
                    }, enabled = !checkedIn, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Filled.CheckCircle, null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (checkedIn) "Sudah Absen" else "Absen Sekarang")
                    }
                }
                Spacer(Modifier.height(20.dp))
                Text("Pencapaian", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                AchievementRow("🔥", "Absen 3 hari", streak >= 3)
                AchievementRow("🏆", "Absen 7 hari", streak >= 7)
                AchievementRow("🎬", "Tonton 5 episode", StageBRepository.watchedEpisodeCount(context) >= 5)
                Spacer(Modifier.height(12.dp))
                Text("Lencana terbuka: $badgeCount", color = Coral, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun AchievementRow(emoji: String, title: String, unlocked: Boolean) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp).background(PanelBg, RoundedCornerShape(14.dp)).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(emoji, fontSize = 25.sp)
        Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f).padding(start = 12.dp))
        Text(if (unlocked) "Terbuka" else "Terkunci", color = if (unlocked) Coral else Muted, fontSize = 12.sp)
    }
}

@Composable
private fun HistoryScreen(
    records: List<WatchRecord>, catalog: List<Drama>, onBack: () -> Unit,
    onDramaEpisode: (Drama, Int) -> Unit, onNavigate: (String) -> Unit
) {
    Scaffold(containerColor = AppBg, bottomBar = { DrakooBottomBar("profile", onNavigate) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Kembali", tint = Color.White) }
                    Text("Riwayat Tontonan", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
                }
                Text("Episode dan posisi terakhir tersimpan di perangkat ini.", color = Muted, modifier = Modifier.padding(bottom = 14.dp))
            }
            if (records.isEmpty()) item {
                Text("Belum ada riwayat. Tonton episode terlebih dahulu untuk melihat progresnya di sini.", color = Muted, modifier = Modifier.fillMaxWidth().padding(top = 30.dp), textAlign = TextAlign.Center)
            }
            items(records.size) { index ->
                val record = records[index]
                val drama = catalog.firstOrNull { it.id == record.seriesId }
                Column(Modifier.fillMaxWidth().padding(vertical = 7.dp).background(PanelBg, RoundedCornerShape(16.dp)).clickable {
                    if (drama != null) onDramaEpisode(drama, record.episode)
                }.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (drama != null) Poster(drama, Modifier.size(76.dp, 94.dp))
                        else Box(Modifier.size(76.dp, 94.dp).background(Color(0xFF30243A), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) { Text("▶", color = Color.White) }
                        Column(Modifier.weight(1f).padding(start = 12.dp)) {
                            Text(record.seriesTitle, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text("Episode ${record.episode}", color = Muted, fontSize = 13.sp)
                            Text(if (record.durationMs > 0L) "${formatTime(record.positionMs)} / ${formatTime(record.durationMs)}" else "Posisi tersimpan", color = Muted, fontSize = 12.sp)
                            Text(java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.SHORT, java.text.DateFormat.SHORT).format(java.util.Date(record.watchedAt)), color = Muted, fontSize = 11.sp)
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(progress = record.progress, modifier = Modifier.fillMaxWidth(), color = Coral, trackColor = LineColor)
                        }
                    }
                }
            }
        }
    }
}

private fun formatTime(milliseconds: Long): String {
    val totalSeconds = (milliseconds.coerceAtLeast(0L) / 1000L).toInt()
    return "%02d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}

@Composable
private fun SimpleTabScreen(title: String, subtitle: String, icon: String, onNavigate: (String) -> Unit) {
    Scaffold(containerColor = AppBg, bottomBar = { DrakooBottomBar("gift", onNavigate) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(icon, fontSize = 58.sp)
            Spacer(Modifier.height(14.dp))
            Text(title, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(subtitle, color = Color(0xFFB9A9BD))
        }
    }
}

private data class ListEntry(val drama: Drama, val record: WatchRecord?, val favorite: Boolean) {
    val finished: Boolean
        get() = record != null && record.episode >= drama.episodes.size && record.progress >= 0.95f
    val inProgress: Boolean
        get() = record != null && !finished
}

@Composable
private fun ListFilterChip(label: String, icon: ImageVector, count: Int, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.clip(RoundedCornerShape(50))
            .then(
                if (selected) Modifier.background(Brush.horizontalGradient(listOf(Color(0xFF7B3FE4), Color(0xFF9B4DFF))))
                else Modifier.border(1.dp, ChipBorder, RoundedCornerShape(50))
            )
            .clickable { onClick() }
            .padding(start = 14.dp, end = 10.dp, top = 9.dp, bottom = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = if (selected) Color.White else Color(0xFFCFC6EA), modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, color = if (selected) Color.White else Color(0xFFCFC6EA), fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, maxLines = 1)
        Spacer(Modifier.width(8.dp))
        Box(
            Modifier.size(22.dp).background(if (selected) Color(0x33FFFFFF) else Color(0xFF2A1F52), CircleShape),
            contentAlignment = Alignment.Center
        ) { Text("$count", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun ListEntryCard(
    entry: ListEntry, firstInProgress: Boolean,
    onOpen: () -> Unit, onPlay: () -> Unit, onRemoveFavorite: () -> Unit, onRemoveHistory: () -> Unit
) {
    var menu by remember { mutableStateOf(false) }
    val drama = entry.drama
    val record = entry.record
    val progress = if (entry.finished) 1f else (record?.progress ?: 0f)
    val badge = when {
        entry.favorite -> "Favorit"
        entry.inProgress -> if (firstInProgress) "Sedang Tonton" else "Lanjutkan"
        else -> null
    }
    val shape = RoundedCornerShape(18.dp)
    Row(
        Modifier.fillMaxWidth().padding(bottom = 12.dp).clip(shape).background(CardBg)
            .border(1.dp, LineColor, shape).clickable { onOpen() }.padding(10.dp)
    ) {
        Box(Modifier.size(96.dp, 112.dp)) {
            Poster(drama, Modifier.fillMaxSize(), shape = RoundedCornerShape(12.dp), alignment = BiasAlignment(0f, 0.4f))
            if (badge != null) BadgePill(badge, badgeIcon(badge), Modifier.align(Alignment.TopStart).padding(5.dp))
        }
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(drama.title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Box {
                    Icon(Icons.Filled.MoreVert, "Menu", tint = Muted,
                        modifier = Modifier.size(24.dp).clickable { menu = true })
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        if (entry.favorite) {
                            DropdownMenuItem(text = { Text("Hapus dari favorit") }, onClick = { menu = false; onRemoveFavorite() })
                        }
                        if (record != null) {
                            DropdownMenuItem(text = { Text("Hapus riwayat") }, onClick = { menu = false; onRemoveHistory() })
                        }
                        DropdownMenuItem(text = { Text("Lihat detail") }, onClick = { menu = false; onOpen() })
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOfNotNull(drama.category, secondTag(drama.category)).forEach { tag ->
                    Text(tag, color = Color(0xFFCFC6EA), fontSize = 12.sp, fontWeight = FontWeight.Medium,
                        modifier = Modifier.background(Color(0xFF251A4A), RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 4.dp))
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                val statusIcon = if (entry.finished) Icons.Outlined.CheckCircle else Icons.Outlined.Schedule
                val statusText = when {
                    entry.finished -> "Selesai  •  ${drama.episodes.size} episode"
                    record == null -> "Belum ditonton"
                    record.durationMs <= 0L -> "Episode ${record.episode}"
                    else -> {
                        val remain = ((record.durationMs - record.positionMs).coerceAtLeast(0L) / 60_000L).toInt()
                        "Episode ${record.episode}  •  " + (if (remain < 1) "kurang dari 1 menit lagi" else "$remain menit lagi")
                    }
                }
                Icon(statusIcon, null, tint = if (entry.finished) Color(0xFF7C8CFF) else Muted, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(statusText, color = Muted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f))
                Box(
                    Modifier.size(40.dp).clip(CircleShape)
                        .then(if (entry.finished) Modifier.border(1.5.dp, PurpleLight, CircleShape) else Modifier.background(Purple))
                        .clickable { onPlay() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.PlayArrow, "Putar", tint = if (entry.finished) PurpleLight else Color.White, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFF2A1F52))) {
                    Box(
                        Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).fillMaxHeight()
                            .background(Brush.horizontalGradient(listOf(Color(0xFF7B3FE4), PurpleLight)))
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text("${(progress * 100).toInt()}%", color = Color(0xFFCFC6EA), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun MyListScreen(
    dramas: List<Drama>, records: List<WatchRecord>, favorites: Set<String>, initialFilter: String,
    onDrama: (Drama) -> Unit, onPlay: (Drama, Int) -> Unit,
    onRemoveFavorite: (String) -> Unit, onRemoveHistory: (String) -> Unit, onNavigate: (String) -> Unit
) {
    var filter by remember(initialFilter) { mutableStateOf(initialFilter) }
    val latest = records.groupBy { it.seriesId }.mapValues { it.value.first() }
    val entries = dramas.mapNotNull { d ->
        val r = latest[d.id]
        val f = favorites.contains(d.id)
        if (r != null || f) ListEntry(d, r, f) else null
    }.sortedByDescending { it.record?.watchedAt ?: 0L }
    val visible = when (filter) {
        "Sedang Tonton" -> entries.filter { it.inProgress }
        "Favorit" -> entries.filter { it.favorite }
        "Selesai" -> entries.filter { it.finished }
        else -> entries
    }
    val firstInProgress = entries.firstOrNull { it.inProgress }?.drama?.id
    val filters = listOf(
        Triple("Semua", Icons.Outlined.Schedule, entries.size),
        Triple("Sedang Tonton", Icons.Outlined.PlayArrow, entries.count { it.inProgress }),
        Triple("Favorit", Icons.Outlined.FavoriteBorder, entries.count { it.favorite }),
        Triple("Selesai", Icons.Outlined.CheckCircle, entries.count { it.finished })
    )
    Scaffold(containerColor = AppBg, bottomBar = { DrakooBottomBar("mylist", onNavigate) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            item {
                Spacer(Modifier.height(14.dp))
                GradientTitle("Daftar ", "Saya", 30)
                Text("Drama yang sedang kamu ikuti dan favoritmu", color = Muted, fontSize = 14.sp)
                Spacer(Modifier.height(16.dp))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    filters.forEach { f -> ListFilterChip(f.first, f.second, f.third, filter == f.first) { filter = f.first } }
                }
                Spacer(Modifier.height(18.dp))
                if (visible.isEmpty()) {
                    Text(
                        if (entries.isEmpty()) "Belum ada drama di daftar kamu. Tonton serial dari Beranda atau tandai favorit untuk mulai."
                        else "Belum ada drama di kategori ini.",
                        color = Muted, modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }
            items(visible.size) { index ->
                val entry = visible[index]
                ListEntryCard(
                    entry = entry,
                    firstInProgress = entry.drama.id == firstInProgress,
                    onOpen = { onDrama(entry.drama) },
                    onPlay = {
                        val ep = if (entry.record == null || entry.finished) 1 else entry.record.episode
                        onPlay(entry.drama, ep)
                    },
                    onRemoveFavorite = { onRemoveFavorite(entry.drama.id) },
                    onRemoveHistory = { onRemoveHistory(entry.drama.id) }
                )
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun Poster(
    drama: Drama, modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(14.dp), alignment: Alignment = Alignment.Center
) {
    if (!drama.coverUrl.isNullOrBlank()) {
        AsyncImage(
            model = drama.coverUrl,
            contentDescription = drama.title,
            contentScale = ContentScale.Crop,
            alignment = alignment,
            modifier = modifier.clip(shape)
        )
        return
    }
    if (drama.cover != null) {
        Image(
            painter = painterResource(drama.cover),
            contentDescription = drama.title,
            contentScale = ContentScale.Crop,
            alignment = alignment,
            modifier = modifier.clip(shape)
        )
        return
    }
    BoxWithConstraints(
        modifier.clip(shape).background(Brush.verticalGradient(drama.colors)).padding(8.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        if (maxWidth > 120.dp && maxHeight > 100.dp) {
            Column(Modifier.padding(4.dp)) {
                Text("DRAMAKITA", color = Color.White.copy(alpha = .75f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(drama.title, color = Color.White, fontSize = 20.sp, lineHeight = 22.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(4.dp))
                Text(drama.category.uppercase(), color = Color.White.copy(alpha = .8f), fontSize = 10.sp)
            }
        } else {
            Text(drama.title, color = Color.White, fontSize = 11.sp, lineHeight = 13.sp,
                fontWeight = FontWeight.ExtraBold, maxLines = 3, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun DetailScreen(
    drama: Drama, favorite: Boolean, resumeEpisode: Int,
    onBack: () -> Unit, onFavorite: () -> Unit, onPlay: (Int) -> Unit
) {
    Scaffold(containerColor = Color(0xFF0C0C10)) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding)) {
            item {
                Box {
                    Poster(drama, Modifier.fillMaxWidth().height(310.dp))
                    IconButton(onClick = onBack, modifier = Modifier.align(Alignment.TopStart).padding(8.dp)) {
                        Icon(Icons.Default.ArrowBack, "Kembali", tint = Color.White)
                    }
                    IconButton(onClick = onFavorite, modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)) {
                        Icon(if (favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            "Favorit", tint = if (favorite) Color(0xFFFF5277) else Color.White)
                    }
                }
                Column(Modifier.padding(16.dp)) {
                    Text(drama.title, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
                    Text("${drama.category} • ${drama.episodes.size} episode", color = Color.LightGray)
                    Spacer(Modifier.height(12.dp))
                    Text(drama.synopsis, color = Color(0xFFD2D0D4), lineHeight = 22.sp)
                    Spacer(Modifier.height(18.dp))
                    Button(onClick = { onPlay(resumeEpisode.coerceIn(1, drama.episodes.size)) },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Icon(Icons.Default.PlayArrow, null)
                        Text(if (resumeEpisode > 1) "Lanjutkan episode $resumeEpisode" else "Mulai menonton")
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Daftar episode", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                }
            }
            items(drama.episodes.size) { index ->
                val episode = index + 1
                Row(
                    Modifier.fillMaxWidth().clickable { onPlay(episode) }.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(54.dp, 42.dp).background(Color(0xFF292931), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.PlayArrow, null, tint = Color(0xFFFF5277))
                    }
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text("Episode $episode", fontWeight = FontWeight.SemiBold)
                        Text(if (episode == resumeEpisode) "Terakhir ditonton" else "Ketuk untuk menonton",
                            color = Color.LightGray, fontSize = 12.sp)
                    }
                }
                HorizontalDivider(color = Color(0xFF25252C))
            }
        }
    }
}

@Composable
private fun PlayerScreen(
    drama: Drama, episodeNumber: Int, onBack: () -> Unit,
    onEpisodeChange: (Int) -> Unit, onSaveProgress: (Long, Long) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("dramakita", 0) }
    var playerError by remember { mutableStateOf(false) }
    var ended by remember { mutableStateOf(false) }
    var speedIndex by remember { mutableIntStateOf(1) }
    val speeds = listOf(0.75f, 1f, 1.25f, 1.5f)
    val pagerState = rememberPagerState(initialPage = (episodeNumber - 1).coerceIn(0, drama.episodes.lastIndex), pageCount = { drama.episodes.size })
    val player = remember(drama.id) {
        ExoPlayer.Builder(context).build().apply { repeatMode = Player.REPEAT_MODE_OFF }
    }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) { playerError = true }
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) ended = true
            }
        }
        player.addListener(listener)
        onDispose { player.removeListener(listener); player.release() }
    }

    LaunchedEffect(drama.id, pagerState.currentPage) {
        val index = pagerState.currentPage.coerceIn(0, drama.episodes.lastIndex)
        val episode = drama.episodes[index]
        playerError = false
        ended = false
        onEpisodeChange(index + 1)
        val saved = prefs.getLong("position_${drama.id}_${episode.number}", 0L)
        val items = drama.episodes.map { MediaItem.fromUri(it.videoUrl) }
        player.setMediaItems(items, index, saved)
        player.prepare()
        player.playWhenReady = true
        player.setPlaybackSpeed(speeds[speedIndex])
    }

    LaunchedEffect(player, pagerState.currentPage) {
        while (true) {
            delay(1500)
            val index = pagerState.currentPage.coerceIn(0, drama.episodes.lastIndex)
            if ((player.isPlaying || player.currentPosition > 0) && player.playbackState != Player.STATE_ENDED) {
                val episode = drama.episodes[index]
                prefs.edit().putLong("position_${drama.id}_${episode.number}", player.currentPosition).apply()
                onSaveProgress(player.currentPosition, player.duration.coerceAtLeast(0L))
            }
        }
    }
    LaunchedEffect(ended) {
        if (ended && pagerState.currentPage < drama.episodes.lastIndex) {
            delay(450)
            onSaveProgress(0L, 0L)
            pagerState.animateScrollToPage(pagerState.currentPage + 1)
        }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        VerticalPager(state = pagerState, modifier = Modifier.fillMaxSize(), beyondViewportPageCount = 1) { pageIndex ->
            if (pageIndex == pagerState.currentPage) {
                AndroidView(
                    factory = { ctx -> PlayerView(ctx).apply {
                        this.player = player
                        useController = true
                        setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
                        resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
                    } },
                    update = { it.player = player },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
                    Text("Episode ${drama.episodes[pageIndex].number}", color = Color.White.copy(alpha = 0.65f))
                }
            }
        }
        Row(
            Modifier.align(Alignment.TopStart).statusBarsPadding().padding(10.dp)
                .background(Color.Black.copy(alpha = .55f), RoundedCornerShape(24.dp))
                .clickable { onBack() }.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.ArrowBack, "Kembali", tint = Color.White)
            Text("  ${drama.title} • Ep. ${pagerState.currentPage + 1}", color = Color.White, maxLines = 1)
        }
        TextButton(
            onClick = { speedIndex = (speedIndex + 1) % speeds.size; player.setPlaybackSpeed(speeds[speedIndex]) },
            modifier = Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(8.dp)
                .background(Color.Black.copy(alpha = .55f), RoundedCornerShape(20.dp))
        ) { Text("${speeds[speedIndex]}x", color = Color.White, fontWeight = FontWeight.Bold) }

        if (playerError) {
            Column(
                Modifier.align(Alignment.Center).padding(24.dp)
                    .background(Color(0xEE19191F), RoundedCornerShape(16.dp)).padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Video gagal dimuat.", fontWeight = FontWeight.Bold, color = Color.White)
                Text("Periksa internet lalu coba lagi.", color = Color.LightGray)
                Spacer(Modifier.height(10.dp))
                Button(onClick = {
                    playerError = false
                    val index = pagerState.currentPage.coerceIn(0, drama.episodes.lastIndex)
                    val saved = prefs.getLong("position_${drama.id}_${drama.episodes[index].number}", 0L)
                    player.setMediaItems(drama.episodes.map { MediaItem.fromUri(it.videoUrl) }, index, saved)
                    player.prepare()
                    player.playWhenReady = true
                }) { Text("Coba lagi") }
            }
        }
        Text(
            "Geser ke atas/bawah untuk pindah episode",
            Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 28.dp)
                .background(Color.Black.copy(alpha = .45f), RoundedCornerShape(20.dp)).padding(horizontal = 12.dp, vertical = 7.dp),
            color = Color.White.copy(alpha = .85f), fontSize = 12.sp
        )
    }
}
