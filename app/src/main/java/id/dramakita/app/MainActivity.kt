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
        window.statusBarColor = android.graphics.Color.rgb(12, 12, 16)
        window.navigationBarColor = android.graphics.Color.rgb(12, 12, 16)
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
            primary = Color(0xFFFF5277),
            background = Color(0xFF0C0C10),
            surface = Color(0xFF19191F),
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
                listCount = catalog.count { it.id == lastWatched || favorites.contains(it.id) },
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
            "mylist" -> MyListScreen(
                dramas = catalog,
                lastWatched = lastWatched,
                lastEpisode = lastEpisode,
                favorites = favorites,
                onDrama = { selectedDrama = it; backPage = "mylist"; page = "detail" },
                onNavigate = { destination -> page = destination }
            )
            else -> HomeScreen(
                title = if (page == "favorites") "Drama Favorit Saya" else "DramaKita",
                dramas = if (page == "favorites") catalog.filter { favorites.contains(it.id) } else catalog,
                search = search,
                onSearch = { search = it },
                category = category,
                onCategory = { category = it },
                showSearch = true,
                lastDrama = if (page == "favorites") null else catalog.find { it.id == lastWatched },
                lastEpisode = lastEpisode,
                onDrama = { selectedDrama = it; backPage = page; page = "detail" },
                onResume = { backPage = page; selectedDrama = it; selectedEpisode = lastEpisode.coerceIn(1, it.episodes.size); page = "player" },
                selectedTab = if (page == "search") "search" else if (page == "favorites") "mylist" else "home",
                onNavigate = { destination -> page = destination }
            )
        }
    }
}

@Composable
private fun HomeScreen(
    title: String, dramas: List<Drama>, search: String, onSearch: (String) -> Unit,
    category: String, onCategory: (String) -> Unit, showSearch: Boolean,
    lastDrama: Drama?, lastEpisode: Int, onDrama: (Drama) -> Unit,
    onResume: (Drama) -> Unit, selectedTab: String, onNavigate: (String) -> Unit
) {
    val filtered = dramas.filter {
        it.title.contains(search, true) && (category == "Semua" || it.category == category)
    }
    Scaffold(
        containerColor = AppBg,
        bottomBar = { DrakooBottomBar(selectedTab, onNavigate) }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            item {
                Spacer(Modifier.height(18.dp))
                Text(if (selectedTab == "search") "Search" else title, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                Text("Cerita singkat, rasa yang panjang.", color = Color.LightGray, fontSize = 14.sp)
                Spacer(Modifier.height(16.dp))
            }
            if (showSearch) {
                item {
                    OutlinedTextField(
                        value = search, onValueChange = onSearch, modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Cari judul serial...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) }, singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF5277),
                            unfocusedBorderColor = Color(0xFF3A3A43)
                        )
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Semua", "Romansa", "Keluarga", "Misteri", "Fantasi").forEach { c ->
                            FilterChip(selected = category == c, onClick = { onCategory(c) },
                                label = { Text(c) })
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    if (lastDrama != null) {
                        Text("Lanjutkan menonton", fontSize = 19.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Row(
                            Modifier.fillMaxWidth().background(Color(0xFF202027), RoundedCornerShape(14.dp))
                                .clickable { onResume(lastDrama) }.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Poster(lastDrama, Modifier.size(width = 70.dp, height = 92.dp))
                            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                                Text(lastDrama.title, fontWeight = FontWeight.Bold)
                                Text("Episode $lastEpisode", color = Color.LightGray, fontSize = 13.sp)
                                Text("Lanjutkan  →", color = Color(0xFFFF5277), fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                    }
                }
            }
            item {
                Text(if (title == "Drama Favorit Saya") "Serial favorit" else "Pilihan untukmu",
                    fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
            }
            if (filtered.isEmpty()) {
                item { Text("Belum ada serial di sini.", color = Color.LightGray, modifier = Modifier.padding(vertical = 28.dp)) }
            } else {
                item {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxWidth().height(((filtered.size + 1) / 2 * 280).dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        userScrollEnabled = false
                    ) {
                        items(filtered) { drama ->
                            Column(Modifier.clickable { onDrama(drama) }) {
                                Poster(drama, Modifier.fillMaxWidth().height(210.dp))
                                Spacer(Modifier.height(8.dp))
                                Text(drama.title, fontWeight = FontWeight.Bold, maxLines = 2,
                                    overflow = TextOverflow.Ellipsis, color = Color.White)
                                Text("${drama.episodes.size} episode • ${drama.category}",
                                    color = Color.LightGray, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(20.dp)) }
        }
    }
}


private val AppBg = Color(0xFF1A0F1E)
private val PanelBg = Color(0xFF2A1B30)
private val Coral = Color(0xFFFF5C60)
private val Amber = Color(0xFFFFB53F)
private val Muted = Color(0xFFB4A6BA)
private val LineColor = Color(0xFF3F2C47)

private val ThinChevron: ImageVector by lazy {
    ImageVector.Builder(defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(9f, 5f); lineTo(16f, 12f); lineTo(9f, 19f)
        }
    }.build()
}

private val CrownIcon: ImageVector by lazy {
    ImageVector.Builder(defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(4f, 17f); lineTo(3f, 7f); lineTo(8f, 11f); lineTo(12f, 4f)
            lineTo(16f, 11f); lineTo(21f, 7f); lineTo(20f, 17f); close()
            moveTo(5f, 21f); lineTo(19f, 21f)
        }
    }.build()
}

private data class DrakooTab(val id: String, val label: String, val icon: ImageVector, val iconOn: ImageVector)

@Composable
private fun DrakooBottomBar(selected: String, onNavigate: (String) -> Unit) {
    val tabs = listOf(
        DrakooTab("home", "Beranda", Icons.Outlined.Home, Icons.Filled.Home),
        DrakooTab("search", "Search", Icons.Outlined.Search, Icons.Filled.Search),
        DrakooTab("gift", "Hadiah", Icons.Outlined.CardGiftcard, Icons.Filled.CardGiftcard),
        DrakooTab("mylist", "Daftar Saya", Icons.Outlined.BookmarkBorder, Icons.Filled.Bookmark),
        DrakooTab("profile", "Profil", Icons.Outlined.Person, Icons.Filled.Person)
    )
    Column(Modifier.fillMaxWidth().background(AppBg)) {
        HorizontalDivider(color = LineColor)
        Row(Modifier.fillMaxWidth().navigationBarsPadding().height(58.dp)) {
            tabs.forEach { tab ->
                val on = tab.id == selected
                val tint = if (on) Amber else Muted
                Column(
                    Modifier.weight(1f).fillMaxHeight().clickable { onNavigate(tab.id) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(if (on) tab.iconOn else tab.icon, contentDescription = tab.label, tint = tint, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.height(3.dp))
                    Text(tab.label, color = tint, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
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
    var displayName by remember { mutableStateOf(prefs.getString("profile_name", "Penonton Drakoo") ?: "Penonton Drakoo") }
    var bio by remember { mutableStateOf(prefs.getString("profile_bio", "Pecinta drama pendek ❤️") ?: "") }
    var editName by remember { mutableStateOf("") }
    var editBio by remember { mutableStateOf("") }
    var showEdit by remember { mutableStateOf(false) }
    var showHelp by remember { mutableStateOf(false) }
    val openEdit = { editName = displayName; editBio = bio; showEdit = true }

    Scaffold(
        containerColor = AppBg,
        bottomBar = { DrakooBottomBar("profile", onNavigate) }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)) {
            item {
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(30.dp).background(Amber, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.PlayArrow, null, tint = Color(0xFF1A0F1E), modifier = Modifier.size(22.dp))
                    }
                    Text("Drakoo", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White,
                        modifier = Modifier.weight(1f).padding(start = 10.dp))
                    IconButton(onClick = { onNavigate("search") }) {
                        Icon(Icons.Outlined.Search, "Cari", tint = Color.White, modifier = Modifier.size(26.dp))
                    }
                    IconButton(onClick = { showHelp = true }) {
                        Icon(Icons.Filled.Settings, "Pengaturan", tint = Color.White, modifier = Modifier.size(26.dp))
                    }
                }
                Spacer(Modifier.height(20.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(90.dp).clickable { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                        Box(
                            Modifier.fillMaxSize().clip(CircleShape)
                                .background(Brush.verticalGradient(listOf(Color(0xFFEA7A63), Color(0xFF8B285E)))),
                            contentAlignment = Alignment.Center
                        ) {
                            if (avatarPath.isNotBlank() && File(avatarPath).exists()) {
                                AsyncImage(model = File(avatarPath), contentDescription = "Foto profil", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(CircleShape))
                            } else {
                                Icon(Icons.Filled.Person, null, tint = Color(0xFFFFE9E4), modifier = Modifier.size(52.dp))
                            }
                        }
                        Box(
                            Modifier.align(Alignment.BottomEnd).offset(x = 2.dp, y = 2.dp).size(30.dp)
                                .clip(CircleShape).background(PanelBg).border(2.dp, AppBg, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.PhotoCamera, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                    Column(Modifier.weight(1f).padding(start = 22.dp).clickable { openEdit() }) {
                        Text(displayName, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Spacer(Modifier.height(2.dp))
                        Text("@penonton", color = Muted, fontSize = 14.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(bio, color = Muted, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                    Icon(ThinChevron, null, tint = Muted, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.height(22.dp))
                Row(Modifier.fillMaxWidth().height(46.dp), verticalAlignment = Alignment.CenterVertically) {
                    val stats = listOf(
                        watchedCount.toString() to "Ditonton",
                        listCount.toString() to "Daftar Tonton",
                        favoriteCount.toString() to "Favorit",
                        badgeCount.toString() to "Lencana"
                    )
                    stats.forEachIndexed { index, stat ->
                        if (index > 0) VerticalDivider(color = LineColor, modifier = Modifier.height(44.dp))
                        ProfileStat(stat.first, stat.second, Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(14.dp))
            }
            item {
                Column(Modifier.fillMaxWidth().background(PanelBg, RoundedCornerShape(26.dp)).padding(horizontal = 14.dp)) {
                    ProfileRow(Icons.Filled.Person, "Profil Saya", "Ubah nama, bio, dan foto profil") { openEdit() }
                    HorizontalDivider(color = LineColor, modifier = Modifier.padding(start = 44.dp))
                    ProfileRow(Icons.Filled.Bookmark, "Daftar Tonton Saya", "Drama yang sedang kamu tonton") { onNavigate("mylist") }
                    HorizontalDivider(color = LineColor, modifier = Modifier.padding(start = 44.dp))
                    ProfileRow(Icons.Filled.Favorite, "Drama Favorit Saya", "Koleksi drama favorit kamu") { onNavigate("favorites") }
                    HorizontalDivider(color = LineColor, modifier = Modifier.padding(start = 44.dp))
                    ProfileRow(Icons.Outlined.Schedule, "Riwayat Tontonan", "Drama yang pernah kamu tonton") { onNavigate("history") }
                }
                Spacer(Modifier.height(18.dp))
                Column(Modifier.fillMaxWidth().background(PanelBg, RoundedCornerShape(26.dp)).padding(horizontal = 14.dp)) {
                    ProfileRow(CrownIcon, "Langganan Premium", "Nikmati fitur eksklusif tanpa iklan") { showHelp = true }
                    HorizontalDivider(color = LineColor, modifier = Modifier.padding(start = 44.dp))
                    ProfileRow(Icons.Outlined.HeadsetMic, "Pusat Bantuan", "Panduan dan pertanyaan umum") { showHelp = true }
                }
                Spacer(Modifier.height(24.dp))
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
                }) { Text("Simpan", color = Coral) }
            },
            dismissButton = { TextButton(onClick = { showEdit = false }) { Text("Batal", color = Muted) } })
    }
    if (showHelp) {
        AlertDialog(onDismissRequest = { showHelp = false }, containerColor = PanelBg,
            title = { Text("Segera hadir", color = Color.White) },
            text = { Text("Fitur ini akan ditambahkan pada versi berikutnya.", color = Muted) },
            confirmButton = { TextButton(onClick = { showHelp = false }) { Text("Mengerti", color = Coral) } })
    }
}

@Composable
private fun ProfileStat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(3.dp))
        Text(label, color = Muted, fontSize = 12.sp, maxLines = 1)
    }
}

@Composable
private fun ProfileRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(44.dp)) {
            Icon(icon, null, tint = Coral, modifier = Modifier.size(28.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
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

@Composable
private fun MyListScreen(
    dramas: List<Drama>, lastWatched: String, lastEpisode: Int, favorites: Set<String>,
    onDrama: (Drama) -> Unit, onNavigate: (String) -> Unit
) {
    val myDramas = dramas.filter { it.id == lastWatched || favorites.contains(it.id) }
    Scaffold(containerColor = AppBg, bottomBar = { DrakooBottomBar("mylist", onNavigate) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            item { Text("Daftar Saya", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold); Spacer(Modifier.height(6.dp)); Text("Drama yang sedang kamu ikuti dan favoritmu", color = Color(0xFFB9A9BD)); Spacer(Modifier.height(20.dp)) }
            if (myDramas.isEmpty()) item { Text("Belum ada drama di daftar kamu. Pilih serial dari Beranda untuk mulai menonton.", color = Color(0xFFB9A9BD)) }
            items(myDramas.size) { index ->
                val drama = myDramas[index]
                Row(Modifier.fillMaxWidth().clickable { onDrama(drama) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Poster(drama, Modifier.size(84.dp, 112.dp))
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(drama.title, color = Color.White, fontWeight = FontWeight.Bold)
                        Text(if (drama.id == lastWatched) "Terakhir: Episode $lastEpisode" else "Favorit kamu", color = Color(0xFFB9A9BD), fontSize = 13.sp)
                    }
                    Icon(Icons.Default.ChevronRight, null, tint = Color(0xFFB9A9BD))
                }
            }
        }
    }
}

@Composable
private fun Poster(drama: Drama, modifier: Modifier = Modifier) {
    if (!drama.coverUrl.isNullOrBlank()) {
        AsyncImage(
            model = drama.coverUrl,
            contentDescription = drama.title,
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(RoundedCornerShape(14.dp))
        )
        return
    }
    if (drama.cover != null) {
        Image(
            painter = painterResource(drama.cover),
            contentDescription = drama.title,
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(RoundedCornerShape(14.dp))
        )
        return
    }
    Box(
        modifier.background(Brush.verticalGradient(drama.colors), RoundedCornerShape(14.dp))
            .padding(12.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Column {
            Text("DRAMAKITA", color = Color.White.copy(alpha = .75f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(drama.title, color = Color.White, fontSize = 20.sp, lineHeight = 22.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(4.dp))
            Text(drama.category.uppercase(), color = Color.White.copy(alpha = .8f), fontSize = 10.sp)
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
