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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.auralis.database.AudiobookJobEntity
import com.auralis.database.BookEntity
import com.auralis.database.BookMetadataEntity
import com.auralis.database.BookmarkEntity
import com.auralis.database.ChapterEntity
import com.auralis.database.CharacterProfileEntity
import com.auralis.database.HighlightEntity
import com.auralis.database.VoiceModelEntity
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

    if (selectedBookId == null) {
        LibraryScreen(
            books = books,
            voices = voices,
            status = transientStatus,
            onImportBook = { importLauncher.launch(arrayOf("application/pdf", "application/epub+zip")) },
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
                EmptyLibrary(onImportBook)
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
private fun EmptyLibrary(onImportBook: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(360.dp),
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
    val bookmarks by repository.observeBookmarks(bookId).collectAsState(initial = emptyList())
    val highlights by repository.observeHighlights(bookId).collectAsState(initial = emptyList())
    val voices by repository.voices.collectAsState(initial = emptyList())
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
                    job = job,
                    voices = voices,
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
    job: AudiobookJobEntity?,
    voices: List<VoiceModelEntity>,
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

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
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
                Text(if (installedVoice == null) "Download voice" else "Voice installed")
            }
            Button(onClick = onPrepare, enabled = installedVoice != null && synthesisReady) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    when {
                        installedVoice == null -> "Install voice first"
                        job?.status == "running" -> "Restart"
                        else -> "Prepare"
                    }
                )
            }
        }
    }
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
