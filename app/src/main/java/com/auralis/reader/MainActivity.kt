package com.auralis.reader

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import android.media.MediaPlayer
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.auralis.database.AudioPlaybackPositionEntity
import com.auralis.database.AudioSegmentEntity
import com.auralis.database.AudiobookJobEntity
import com.auralis.database.BookEntity
import com.auralis.database.BookMetadataEntity
import com.auralis.database.BookmarkEntity
import com.auralis.database.ChapterEntity
import com.auralis.database.CharacterProfileEntity
import com.auralis.database.HighlightEntity
import com.auralis.database.VoiceModelEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = AuralisRepository(applicationContext)
        setContent {
            AuralisTheme {
                AuralisApp(repository)
            }
        }
    }
}

@Composable
private fun AuralisTheme(content: @Composable () -> Unit) {
    val colors = lightColorScheme(
        primary = Color(0xFF2F6F68),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFD6E7E1),
        onPrimaryContainer = Color(0xFF10201D),
        secondary = Color(0xFF756144),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFF0DFBE),
        tertiary = Color(0xFF9A4F3E),
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFFFDAD1),
        surface = Color(0xFFFAFBF8),
        surfaceVariant = Color(0xFFE1E7DF),
        background = Color(0xFFFAFBF8),
        error = Color(0xFFB3261E)
    )

    MaterialTheme(colorScheme = colors) {
        Surface(Modifier.fillMaxSize(), color = colors.background) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuralisApp(repository: AuralisRepository) {
    val scope = rememberCoroutineScope()
    val books by repository.books.collectAsState(initial = emptyList())
    val voices by repository.voices.collectAsState(initial = emptyList())
    var selectedBookId by rememberSaveable { mutableStateOf<String?>(null) }
    var transientStatus by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        repository.seedVoiceCatalog()
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                transientStatus = "Importing book"
                selectedBookId = runCatching { repository.importBook(uri) }
                    .onFailure { transientStatus = it.message ?: "Import failed" }
                    .getOrNull()
                if (selectedBookId != null) transientStatus = null
            }
        }
    }

    fun downloadDefaultVoice() {
        scope.launch {
            transientStatus = null
            runCatching { repository.downloadDefaultVoice() }
                .onSuccess { transientStatus = "Natural voice installed" }
                .onFailure { transientStatus = it.message ?: "Voice download failed" }
        }
    }

    fun loadSampleBook() {
        scope.launch {
            transientStatus = "Loading sample book..."
            selectedBookId = runCatching { repository.importSampleBook() }
                .onFailure { transientStatus = it.message ?: "Failed to load sample book" }
                .getOrNull()
            if (selectedBookId != null) transientStatus = null
        }
    }

    if (selectedBookId == null) {
        LibraryScreen(
            books = books,
            voices = voices,
            status = transientStatus,
            onImportBook = { importLauncher.launch(arrayOf("application/pdf", "application/epub+zip")) },
            onLoadSampleBook = ::loadSampleBook,
            onInstallVoice = ::downloadDefaultVoice,
            onOpenBook = { selectedBookId = it }
        )
    } else {
        BookScreen(
            repository = repository,
            bookId = selectedBookId.orEmpty(),
            onBack = { selectedBookId = null },
            onInstallVoice = ::downloadDefaultVoice
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibraryScreen(
    books: List<BookEntity>,
    voices: List<VoiceModelEntity>,
    status: String?,
    onImportBook: () -> Unit,
    onLoadSampleBook: () -> Unit,
    onInstallVoice: () -> Unit,
    onOpenBook: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Auralis", fontWeight = FontWeight.SemiBold)
                        Text("Library", style = MaterialTheme.typography.labelMedium)
                    }
                },
                actions = {
                    IconButton(onClick = onInstallVoice) {
                        Icon(Icons.Rounded.Mic, contentDescription = "Install voice")
                    }
                    FilledIconButton(onClick = onImportBook) {
                        Icon(Icons.Rounded.Add, contentDescription = "Import book")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatusStrip(status = status, voices = voices, onInstallVoice = onInstallVoice)
            if (books.isEmpty()) {
                EmptyLibrary(onImportBook = onImportBook, onLoadSampleBook = onLoadSampleBook)
            } else {
                books.forEach { book ->
                    BookRow(book = book, onClick = { onOpenBook(book.id) })
                }
            }
        }
    }
}

@Composable
private fun StatusStrip(
    status: String?,
    voices: List<VoiceModelEntity>,
    onInstallVoice: () -> Unit
) {
    val voice = voices.firstOrNull { it.id == "kokoro-natural-en" }
    val installedVoice = voice?.takeIf { it.status == "installed" }
    val isDownloading = voice?.status == "downloading"
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AssistChip(
            onClick = onInstallVoice,
            label = {
                Text(
                    when {
                        installedVoice != null -> installedVoice.displayName
                        isDownloading -> "Downloading natural voice"
                        else -> "Download natural voice"
                    }
                )
            },
            leadingIcon = {
                Icon(
                    when {
                        installedVoice != null -> Icons.Rounded.CheckCircle
                        isDownloading -> Icons.Rounded.GraphicEq
                        else -> Icons.Rounded.Download
                    },
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        )
        if (status != null) {
            AssistChip(
                onClick = {},
                label = { Text(status) },
                leadingIcon = {
                    Icon(Icons.Rounded.GraphicEq, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            )
        }
    }
}

@Composable
private fun EmptyLibrary(onImportBook: () -> Unit, onLoadSampleBook: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(420.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Image(
                painter = painterResource(R.drawable.asset_empty_library),
                contentDescription = null,
                modifier = Modifier.size(132.dp)
            )
            Text("No books yet", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            FilledTonalButton(onClick = onImportBook) {
                Icon(Icons.Rounded.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Import PDF or EPUB")
            }
            OutlinedButton(onClick = onLoadSampleBook) {
                Icon(Icons.Rounded.AutoStories, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Load Sample Book (The Time Machine)")
            }
        }
    }
}

@Composable
private fun BookRow(book: BookEntity, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.asset_reader_cover),
                contentDescription = null,
                modifier = Modifier.size(width = 48.dp, height = 64.dp)
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(book.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${book.format.uppercase()}  ${book.importStatus}", style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Rounded.Headphones, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookScreen(
    repository: AuralisRepository,
    bookId: String,
    onBack: () -> Unit,
    onInstallVoice: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val book by repository.observeBook(bookId).collectAsState(initial = null)
    val chapters by repository.observeChapters(bookId).collectAsState(initial = emptyList())
    val metadata by repository.observeMetadata(bookId).collectAsState(initial = null)
    val characters by repository.observeCharacters(bookId).collectAsState(initial = emptyList())
    val job by repository.observeJob(bookId).collectAsState(initial = null)
    val segments by repository.observeAudioSegments(bookId).collectAsState(initial = emptyList())
    val bookmarks by repository.observeBookmarks(bookId).collectAsState(initial = emptyList())
    val highlights by repository.observeHighlights(bookId).collectAsState(initial = emptyList())
    val voices by repository.voices.collectAsState(initial = emptyList())
    val audioPlaybackPosition by repository.observeAudioPlaybackPosition(bookId).collectAsState(initial = null)
    var mode by rememberSaveable { mutableStateOf("read") }
    var chapterIndex by rememberSaveable(bookId) { mutableIntStateOf(0) }

    val selectedChapter = chapters.getOrNull(chapterIndex.coerceIn(0, (chapters.size - 1).coerceAtLeast(0)))
    LaunchedEffect(selectedChapter?.id) {
        repository.saveReadingPosition(bookId, selectedChapter?.id)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Column {
                        Text(book?.title ?: "Book", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${chapters.size} chapters", style = MaterialTheme.typography.labelMedium)
                    }
                },
                actions = {
                    IconButton(onClick = { mode = "search" }) {
                        Icon(Icons.Rounded.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = onInstallVoice) {
                        Icon(Icons.Rounded.Download, contentDescription = "Install voice")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ModeTabs(selected = mode, onSelected = { mode = it })
            when (mode) {
                "audio" -> AudioPane(
                    bookId = bookId,
                    job = job,
                    segments = segments,
                    chapters = chapters,
                    voices = voices,
                    savedPosition = audioPlaybackPosition,
                    onSavePosition = { segIdx, posMillis, chapId ->
                        scope.launch {
                            repository.saveAudioPlaybackPosition(bookId, segIdx, posMillis, chapId)
                        }
                    },
                    onInstallVoice = onInstallVoice,
                    onPrepare = { repository.prepareAudiobook(bookId) }
                )
                "details" -> DetailsPane(metadata, characters)
                "notes" -> NotesPane(bookmarks, highlights)
                "search" -> SearchPane(chapters, repository)
                else -> ReaderPane(
                    chapter = selectedChapter,
                    chapterCount = chapters.size,
                    chapterIndex = chapterIndex,
                    text = selectedChapter?.let(repository::readChapterText).orEmpty(),
                    onPrevious = { chapterIndex = (chapterIndex - 1).coerceAtLeast(0) },
                    onNext = { chapterIndex = (chapterIndex + 1).coerceAtMost((chapters.size - 1).coerceAtLeast(0)) },
                    onBookmark = {
                        selectedChapter?.let { chapter ->
                            scope.launch { repository.addBookmark(bookId, chapter.id, chapter.title) }
                        }
                    },
                    onHighlight = {
                        selectedChapter?.let { chapter ->
                            scope.launch { repository.addHighlight(bookId, chapter.id, "Opening passage") }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ModeTabs(selected: String, onSelected: (String) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ModeButton("read", "Read", Icons.Rounded.Book, selected, onSelected)
        ModeButton("audio", "Audio", Icons.Rounded.Headphones, selected, onSelected)
        ModeButton("details", "Details", Icons.Rounded.GraphicEq, selected, onSelected)
        ModeButton("notes", "Notes", Icons.Rounded.Star, selected, onSelected)
    }
}

@Composable
private fun ModeButton(
    id: String,
    label: String,
    icon: ImageVector,
    selected: String,
    onSelected: (String) -> Unit
) {
    val selectedMode = selected == id
    if (selectedMode) {
        Button(onClick = { onSelected(id) }, modifier = Modifier.height(40.dp)) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(label)
        }
    } else {
        OutlinedButton(onClick = { onSelected(id) }, modifier = Modifier.height(40.dp)) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun ReaderPane(
    chapter: ChapterEntity?,
    chapterCount: Int,
    chapterIndex: Int,
    text: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onBookmark: () -> Unit,
    onHighlight: () -> Unit
) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onPrevious, enabled = chapterIndex > 0) { Text("Previous") }
            Column(Modifier.weight(1f)) {
                Text(chapter?.title ?: "No readable text", fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${chapterIndex + 1} of ${chapterCount.coerceAtLeast(1)}", style = MaterialTheme.typography.labelMedium)
            }
            OutlinedButton(onClick = onNext, enabled = chapterIndex < chapterCount - 1) { Text("Next") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilledTonalButton(onClick = onBookmark, enabled = chapter != null) { Text("Bookmark") }
            FilledTonalButton(onClick = onHighlight, enabled = chapter != null) { Text("Highlight") }
        }
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = text.ifBlank { "This file did not expose selectable text. OCR support is reserved for the next implementation pass." },
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = FontFamily.Serif,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.35
            )
        }
    }
}

@Composable
private fun AudioPane(
    bookId: String,
    job: AudiobookJobEntity?,
    segments: List<AudioSegmentEntity>,
    chapters: List<ChapterEntity>,
    voices: List<VoiceModelEntity>,
    savedPosition: AudioPlaybackPositionEntity?,
    onSavePosition: (Int, Long, String?) -> Unit,
    onInstallVoice: () -> Unit,
    onPrepare: () -> Unit
) {
    val installedVoice = voices.firstOrNull { it.status == "installed" }
    val isDownloading = voices.any { it.status == "downloading" }
    val synthesisReady = installedVoice != null
    val jobError = audioJobError(job?.lastError)
    val progress = if ((job?.totalSegments ?: 0) > 0) {
        (job?.completedSegments ?: 0).toFloat() / job!!.totalSegments.toFloat()
    } else {
        0f
    }

    var activeSegmentIndex by rememberSaveable(bookId) { mutableIntStateOf(0) }
    var currentPositionMillis by rememberSaveable(bookId) { mutableLongStateOf(0L) }
    var isPlaying by remember { mutableStateOf(false) }
    var isSeeking by remember { mutableStateOf(false) }
    var sliderValue by remember { mutableFloatStateOf(0f) }
    val player = remember { MediaPlayer() }
    val latestOnSavePosition by rememberUpdatedState(onSavePosition)

    // Automatically restore saved playback position on first load
    var hasRestoredSavedPosition by rememberSaveable(bookId) { mutableStateOf(false) }
    LaunchedEffect(savedPosition, segments) {
        if (!hasRestoredSavedPosition && savedPosition != null && segments.isNotEmpty()) {
            if (savedPosition.segmentIndex in segments.indices) {
                activeSegmentIndex = savedPosition.segmentIndex
                currentPositionMillis = savedPosition.positionMillis
            }
            hasRestoredSavedPosition = true
        }
    }

    val activeSegment = segments.getOrNull(activeSegmentIndex.coerceIn(0, (segments.size - 1).coerceAtLeast(0)))
    val activeSegmentDuration = activeSegment?.durationMillis?.coerceAtLeast(1000L) ?: 1000L

    // Progress updater loop while playing - saves to Room periodically
    LaunchedEffect(isPlaying, activeSegmentIndex) {
        while (isPlaying) {
            runCatching {
                if (player.isPlaying) {
                    val pos = player.currentPosition.toLong()
                    if (!isSeeking) {
                        currentPositionMillis = pos
                    }
                    latestOnSavePosition(activeSegmentIndex, pos, activeSegment?.chapterId)
                }
            }
            delay(1000)
        }
    }

    fun playSegment(index: Int, startPositionMillis: Long = 0L) {
        if (index !in segments.indices) return
        val segment = segments[index]
        activeSegmentIndex = index
        runCatching {
            player.reset()
            player.setDataSource(segment.filePath)
            player.prepare()
            if (startPositionMillis > 0 && startPositionMillis < (segment.durationMillis - 250)) {
                player.seekTo(startPositionMillis.toInt())
                currentPositionMillis = startPositionMillis
            } else {
                currentPositionMillis = 0L
            }
            player.start()
            isPlaying = true
            latestOnSavePosition(activeSegmentIndex, currentPositionMillis, segment.chapterId)
            player.setOnCompletionListener {
                if (activeSegmentIndex + 1 < segments.size) {
                    playSegment(activeSegmentIndex + 1, 0L)
                } else {
                    isPlaying = false
                    currentPositionMillis = segment.durationMillis
                    latestOnSavePosition(activeSegmentIndex, currentPositionMillis, segment.chapterId)
                }
            }
        }.onFailure {
            isPlaying = false
        }
    }

    fun togglePlayPause() {
        if (segments.isEmpty()) return
        if (isPlaying) {
            val currentPos = runCatching { player.currentPosition.toLong() }.getOrDefault(currentPositionMillis)
            player.pause()
            isPlaying = false
            currentPositionMillis = currentPos
            latestOnSavePosition(activeSegmentIndex, currentPos, activeSegment?.chapterId)
        } else {
            if (player.currentPosition > 0 && activeSegmentIndex in segments.indices) {
                player.start()
                isPlaying = true
            } else {
                playSegment(activeSegmentIndex.coerceIn(0, (segments.size - 1).coerceAtLeast(0)), currentPositionMillis)
            }
        }
    }

    fun seekToMillis(targetMillis: Long) {
        val clamped = targetMillis.coerceIn(0L, activeSegmentDuration)
        currentPositionMillis = clamped
        runCatching {
            player.seekTo(clamped.toInt())
        }
        latestOnSavePosition(activeSegmentIndex, clamped, activeSegment?.chapterId)
    }

    DisposableEffect(bookId) {
        onDispose {
            runCatching {
                if (player.isPlaying) {
                    val pos = player.currentPosition.toLong()
                    latestOnSavePosition(activeSegmentIndex, pos, activeSegment?.chapterId)
                }
                player.release()
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                Modifier
                    .size(56.dp)
                    .background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(
                        if (installedVoice == null) {
                            R.drawable.asset_audio_generation
                        } else {
                            R.drawable.asset_voice_ready
                        }
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(52.dp)
                )
            }
            Column(Modifier.weight(1f)) {
                Text(audioJobStatus(job?.status), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    when {
                        installedVoice != null -> installedVoice.displayName
                        isDownloading -> "Downloading natural voice"
                        else -> "Download natural voice"
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
        if (!jobError.isNullOrBlank()) {
            Text(jobError, color = MaterialTheme.colorScheme.error)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilledTonalButton(onClick = onInstallVoice) {
                Icon(Icons.Rounded.Mic, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (installedVoice == null) "Download voice" else "Voice ready")
            }
            Button(onClick = onPrepare, enabled = installedVoice != null && synthesisReady) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    when {
                        installedVoice == null -> "Install voice first"
                        job?.status == "running" -> "Restarting..."
                        job?.status == "complete" -> "Regenerate"
                        else -> "Generate Audio"
                    }
                )
            }
        }

        if (savedPosition != null && segments.isNotEmpty() && !isPlaying) {
            val savedChapter = chapters.firstOrNull { it.id == savedPosition.chapterId }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Rounded.Headphones,
                        contentDescription = "Saved Position",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Saved position: Track ${savedPosition.segmentIndex + 1} (${formatAudioTime(savedPosition.positionMillis)})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = {
                            playSegment(savedPosition.segmentIndex, savedPosition.positionMillis)
                        }
                    ) {
                        Text("Resume", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (segments.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val currentChapter = chapters.firstOrNull { it.id == activeSegment?.chapterId }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FilledIconButton(onClick = ::togglePlayPause) {
                            Icon(
                                if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play"
                            )
                        }
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = currentChapter?.title ?: "Narration Track",
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Track ${activeSegmentIndex + 1} of ${segments.size}  •  ${formatAudioTime(currentPositionMillis)} / ${formatAudioTime(activeSegmentDuration)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        TextButton(
                            onClick = { seekToMillis(currentPositionMillis - 10_000L) }
                        ) {
                            Text("-10s", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                        TextButton(
                            onClick = { seekToMillis(currentPositionMillis + 10_000L) }
                        ) {
                            Text("+10s", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Scrubber Slider
                    val effectiveFraction = if (isSeeking) {
                        sliderValue
                    } else {
                        (currentPositionMillis.toFloat() / activeSegmentDuration.toFloat()).coerceIn(0f, 1f)
                    }

                    Slider(
                        value = effectiveFraction,
                        onValueChange = { frac ->
                            isSeeking = true
                            sliderValue = frac
                        },
                        onValueChangeFinished = {
                            val targetMillis = (sliderValue * activeSegmentDuration).toLong()
                            isSeeking = false
                            seekToMillis(targetMillis)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(formatAudioTime(currentPositionMillis), style = MaterialTheme.typography.labelSmall)
                        Text(formatAudioTime(activeSegmentDuration), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Text("Audio Tracks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            segments.forEachIndexed { idx, segment ->
                val chapter = chapters.firstOrNull { it.id == segment.chapterId }
                val isCurrent = idx == activeSegmentIndex
                Card(
                    onClick = {
                        val resumeOffset = if (isCurrent) currentPositionMillis else 0L
                        playSegment(idx, resumeOffset)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else Color.White
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = if (isCurrent && isPlaying) Icons.Rounded.VolumeUp else Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            tint = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = chapter?.title ?: "Segment ${idx + 1}",
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${segment.durationMillis / 1000}s duration" + if (isCurrent && currentPositionMillis > 0) " • at ${formatAudioTime(currentPositionMillis)}" else "",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatAudioTime(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)
}

private fun audioJobStatus(status: String?): String {
    return when (status) {
        null -> "not started"
        "waiting_for_voice_adapter", "synthesis_adapter_pending" -> "ready to prepare"
        "unsupported_voice_pack" -> "unsupported voice pack"
        else -> status.replace('_', ' ')
    }
}

private fun audioJobError(error: String?): String? {
    val value = error?.takeIf { it.isNotBlank() } ?: return null
    val oldAdapterMessage = value.contains("not compatible", ignoreCase = true) ||
        value.contains("synthesis adapter", ignoreCase = true)
    return if (oldAdapterMessage) null else value
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailsPane(
    metadata: BookMetadataEntity?,
    characters: List<CharacterProfileEntity>
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(metadata?.synopsis ?: "Analysis pending", style = MaterialTheme.typography.bodyLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(onClick = {}, label = { Text(metadata?.genre ?: "genre") })
            AssistChip(onClick = {}, label = { Text(metadata?.tone ?: "tone") })
            AssistChip(onClick = {}, label = { Text(metadata?.source ?: "source") })
        }
        Text("Characters", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        characters.forEach { character ->
            CharacterRow(character)
        }
    }
}

@Composable
private fun CharacterRow(character: CharacterProfileEntity) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            Modifier
                .size(38.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(character.name.take(1), fontWeight = FontWeight.Bold)
        }
        Column(Modifier.weight(1f)) {
            Text(character.name, fontWeight = FontWeight.SemiBold)
            Text(character.description, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun NotesPane(
    bookmarks: List<BookmarkEntity>,
    highlights: List<HighlightEntity>
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Bookmarks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        if (bookmarks.isEmpty()) Text("None")
        bookmarks.forEach { Text(it.label, style = MaterialTheme.typography.bodyLarge) }
        Text("Highlights", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        if (highlights.isEmpty()) Text("None")
        highlights.forEach { Text(it.note ?: it.colorName, style = MaterialTheme.typography.bodyLarge) }
    }
}

@Composable
private fun SearchPane(chapters: List<ChapterEntity>, repository: AuralisRepository) {
    var query by rememberSaveable { mutableStateOf("") }
    val matches = remember(query, chapters) {
        if (query.length < 3) {
            emptyList()
        } else {
            chapters.mapNotNull { chapter ->
                val text = repository.readChapterText(chapter)
                val index = text.indexOf(query, ignoreCase = true)
                if (index >= 0) chapter.title to text.substring(index, (index + 180).coerceAtMost(text.length)) else null
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        androidx.compose.material3.OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            label = { Text("Search") }
        )
        matches.forEach { (title, excerpt) ->
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(excerpt, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
