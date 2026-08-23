package com.auralis.reader

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import com.auralis.audio.TtsTextSanitizer
import com.auralis.audio.HumanSpeechPacer
import com.auralis.audio.SentenceCadence
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.sp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.LastPage
import androidx.compose.material.icons.automirrored.rounded.VolumeDown
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.FirstPage
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.LastPage
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlayCircleOutline
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.VolumeDown
import androidx.compose.material.icons.rounded.VolumeMute
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.sin
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
        val themePreferencesManager = ThemePreferencesManager.get(applicationContext)
        setContent {
            AuralisThemeProvider(themePreferencesManager) {
                AuralisApp(repository)
            }
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
    var bookToDelete by remember { mutableStateOf<BookEntity?>(null) }

    var showVoiceSettingsDialog by remember { mutableStateOf(false) }
    var activeVoiceId by rememberSaveable { mutableStateOf(com.auralis.audio.VoiceModelRepository.DEFAULT_KOKORO_VOICE_ID) }
    var enableSmartSkipping by rememberSaveable { mutableStateOf(true) }

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

    if (showVoiceSettingsDialog) {
        VoiceModelsDialog(
            voices = voices,
            selectedVoiceId = activeVoiceId,
            onSelectVoice = { activeVoiceId = it },
            onDownloadVoice = { downloadDefaultVoice() },
            enableSmartSkipping = enableSmartSkipping,
            onToggleSmartSkipping = { enableSmartSkipping = it },
            onDismissRequest = { showVoiceSettingsDialog = false }
        )
    }

    bookToDelete?.let { book ->
        AlertDialog(
            onDismissRequest = { bookToDelete = null },
            title = { Text("Remove Book") },
            text = { Text("Are you sure you want to remove \"${book.title}\" from your library? This will delete all chapters, bookmarks, and audio for this book.") },
            confirmButton = {
                Button(
                    onClick = {
                        val id = book.id
                        bookToDelete = null
                        if (selectedBookId == id) {
                            selectedBookId = null
                        }
                        scope.launch {
                            repository.deleteBook(id)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { bookToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (selectedBookId == null) {
        LibraryScreen(
            books = books,
            voices = voices,
            status = transientStatus,
            onImportBook = { importLauncher.launch(arrayOf("application/pdf", "application/epub+zip")) },
            onLoadSampleBook = ::loadSampleBook,
            onInstallVoice = ::downloadDefaultVoice,
            onOpenVoiceSettings = { showVoiceSettingsDialog = true },
            onOpenBook = { selectedBookId = it },
            onDeleteBook = { bookToDelete = it }
        )
    } else {
        BookScreen(
            repository = repository,
            bookId = selectedBookId.orEmpty(),
            onBack = { selectedBookId = null },
            onInstallVoice = ::downloadDefaultVoice,
            onOpenVoiceSettings = { showVoiceSettingsDialog = true },
            onDeleteBook = { bookToDelete = it }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun LibraryScreen(
    books: List<BookEntity>,
    voices: List<VoiceModelEntity>,
    status: String?,
    onImportBook: () -> Unit,
    onLoadSampleBook: () -> Unit,
    onInstallVoice: () -> Unit,
    onOpenVoiceSettings: () -> Unit,
    onOpenBook: (String) -> Unit,
    onDeleteBook: (BookEntity) -> Unit
) {
    var isGridView by rememberSaveable { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Auralis", fontWeight = FontWeight.SemiBold)
                        Text("Library (${books.size})", style = MaterialTheme.typography.labelMedium)
                    }
                },
                actions = {
                    IconButton(onClick = { isGridView = !isGridView }) {
                        Icon(
                            if (isGridView) Icons.Rounded.GraphicEq else Icons.Rounded.AutoStories,
                            contentDescription = "Toggle Grid/List View"
                        )
                    }
                    ThemeActionIconButton()
                    IconButton(onClick = onOpenVoiceSettings) {
                        Icon(Icons.Rounded.Mic, contentDescription = "Voice Models & Settings")
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
            StatusStrip(status = status, voices = voices, onOpenVoiceSettings = onOpenVoiceSettings)
            if (books.isEmpty()) {
                EmptyLibrary(onImportBook = onImportBook, onLoadSampleBook = onLoadSampleBook)
            } else if (isGridView) {
                androidx.compose.foundation.layout.FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    books.forEach { book ->
                        BookGridCard(
                            book = book,
                            onClick = { onOpenBook(book.id) },
                            onDelete = { onDeleteBook(book) }
                        )
                    }
                }
            } else {
                books.forEach { book ->
                    BookRow(
                        book = book,
                        onClick = { onOpenBook(book.id) },
                        onDelete = { onDeleteBook(book) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusStrip(
    status: String?,
    voices: List<VoiceModelEntity>,
    onOpenVoiceSettings: () -> Unit
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
            onClick = onOpenVoiceSettings,
            label = {
                Text(
                    when {
                        installedVoice != null -> "Voice: ${installedVoice.displayName}"
                        isDownloading -> "Downloading voice model..."
                        else -> "Voice Models & Engine Settings"
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
private fun BookCoverThumbnail(
    title: String,
    format: String,
    modifier: Modifier = Modifier
) {
    val colorPair = remember(title) {
        val hash = title.hashCode()
        val gradients = listOf(
            Pair(Color(0xFF1E293B), Color(0xFF0F172A)), // Midnight Slate
            Pair(Color(0xFF1E3C72), Color(0xFF2A5298)), // Royal Blue
            Pair(Color(0xFF0F766E), Color(0xFF134E4A)), // Deep Teal
            Pair(Color(0xFF581C87), Color(0xFF3B0764)), // Rich Purple
            Pair(Color(0xFF831843), Color(0xFF500724)), // Wine Crimson
            Pair(Color(0xFF1F2937), Color(0xFF111827)), // Dark Obsidian
            Pair(Color(0xFF1C1917), Color(0xFF0C0A09)), // Charcoal Gold
            Pair(Color(0xFF312E81), Color(0xFF1E1B4B))  // Indigo Dusk
        )
        gradients[kotlin.math.abs(hash) % gradients.size]
    }

    val initials = remember(title) {
        title.split(Regex("\\s+"))
            .filter { it.isNotBlank() && it.first().isLetterOrDigit() }
            .take(2)
            .map { it.first().uppercaseChar() }
            .joinToString("")
            .ifBlank { title.take(2).uppercase() }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(brush = Brush.verticalGradient(colors = listOf(colorPair.first, colorPair.second)))
            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        // Book Spine Shadow on Left Edge
        Box(
            Modifier
                .align(Alignment.CenterStart)
                .width(6.dp)
                .fillMaxHeight()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent)
                    )
                )
        )

        // Monogram & Format Badge
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(4.dp)
        ) {
            Text(
                text = initials,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.height(4.dp))
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color.Black.copy(alpha = 0.45f)
            ) {
                Text(
                    text = format.uppercase(),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
private fun BookGridCard(
    book: BookEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(155.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BookCoverThumbnail(
                title = book.title,
                format = book.format,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            )
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    book.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        book.author ?: "Unknown",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Rounded.DeleteOutline,
                            contentDescription = "Remove book",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BookRow(
    book: BookEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            BookCoverThumbnail(
                title = book.title,
                format = book.format,
                modifier = Modifier.size(width = 52.dp, height = 70.dp)
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    book.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    book.author ?: "Unknown Author",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ) {
                        Text(
                            book.format.uppercase(),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Text(
                        book.importStatus.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Rounded.DeleteOutline,
                    contentDescription = "Remove book",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookScreen(
    repository: AuralisRepository,
    bookId: String,
    onBack: () -> Unit,
    onInstallVoice: () -> Unit,
    onOpenVoiceSettings: () -> Unit,
    onDeleteBook: (BookEntity) -> Unit
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
    var audioJumpTarget by remember { mutableStateOf<Pair<Int, Long>?>(null) }
    var isImmersive by rememberSaveable { mutableStateOf(false) }
    var showTopBarOverflow by remember { mutableStateOf(false) }

    var selectedVoiceId by rememberSaveable { mutableStateOf(com.auralis.audio.VoiceModelRepository.DEFAULT_KOKORO_VOICE_ID) }
    var enableSmartSkipping by rememberSaveable { mutableStateOf(true) }

    var selectedSlmId by rememberSaveable { mutableStateOf("smollm2-1.7b") }
    var enableWholeBookScan by rememberSaveable { mutableStateOf(true) }
    var lastGeneratedSlmId by rememberSaveable { mutableStateOf("smollm2-1.7b") }
    var lastGeneratedWholeBookScan by rememberSaveable { mutableStateOf(true) }
    var showSlmSettingsDialog by remember { mutableStateOf(false) }
    var deepstashRegenCount by remember { mutableIntStateOf(0) }

    val context = LocalContext.current
    val llmRuntime = remember { com.auralis.ai.OnDeviceLlmRuntime() }

    var deepstashSummaryState by remember { mutableStateOf<com.auralis.ai.DeepstashSummaryResult?>(null) }
    var isDeepstashLoading by remember { mutableStateOf(false) }

    LaunchedEffect(book?.title, chapters, selectedSlmId, enableWholeBookScan, deepstashRegenCount) {
        if (chapters.isNotEmpty()) {
            isDeepstashLoading = true
            deepstashSummaryState = null // Instantly clear out previous content
            kotlinx.coroutines.delay(100)

            val slmSpec = com.auralis.ai.OnDeviceLlmRuntime.AVAILABLE_SLM_MODELS.find { it.id == selectedSlmId }
                ?: com.auralis.ai.OnDeviceLlmRuntime.AVAILABLE_SLM_MODELS.first()
            val slmFile = llmRuntime.getModelFile(context.filesDir, selectedSlmId)
            val status = llmRuntime.checkModelStatus(context.filesDir, selectedSlmId)

            val activeChapters = if (enableWholeBookScan) {
                chapters
            } else {
                chapters.take(6)
            }

            val bookTitle = book?.title ?: "Book"
            val author = book?.author ?: "Unknown Author"

            val metadata = GoogleBooksClient.getMetadata(bookTitle, author)

            val result = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                com.auralis.ai.DeepstashSummarizer(llmRuntime).generateSummary(
                    bookTitle = bookTitle,
                    author = author,
                    chapters = activeChapters.map { chap -> Pair(chap.title, repository.readChapterText(chap)) },
                    slmModelFile = if (status == com.auralis.ai.SlmModelStatus.INSTALLED) slmFile else null,
                    slmModelName = if (status == com.auralis.ai.SlmModelStatus.INSTALLED) "${slmSpec.name} (ONNX SLM)" else "Built-in SLM Engine (Fallback for ${slmSpec.name})",
                    bookDescription = metadata?.description
                )
            }

            deepstashSummaryState = result
            isDeepstashLoading = false
        }
    }

    val isSlmChanged = (selectedSlmId != lastGeneratedSlmId || enableWholeBookScan != lastGeneratedWholeBookScan)

    val selectedChapter = chapters.getOrNull(chapterIndex.coerceIn(0, (chapters.size - 1).coerceAtLeast(0)))
    LaunchedEffect(selectedChapter?.id) {
        repository.saveReadingPosition(bookId, selectedChapter?.id)
    }

    val showTopControls = !isImmersive || mode != "read"

    Scaffold(
        topBar = {
            if (showTopControls) {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    },
                    title = {
                        Column {
                            Text(book?.title ?: "Book", maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${chapters.size} chapters", style = MaterialTheme.typography.labelMedium)
                        }
                    },
                    actions = {
                        ThemeActionIconButton()
                        IconButton(onClick = { mode = "search" }) {
                            Icon(Icons.Rounded.Search, contentDescription = "Search")
                        }
                        Box {
                            IconButton(onClick = { showTopBarOverflow = true }) {
                                Icon(Icons.Rounded.MoreVert, contentDescription = "More options")
                            }
                            DropdownMenu(
                                expanded = showTopBarOverflow,
                                onDismissRequest = { showTopBarOverflow = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Book Intelligence") },
                                    leadingIcon = { Icon(Icons.Rounded.Psychology, contentDescription = null) },
                                    onClick = {
                                        showTopBarOverflow = false
                                        showSlmSettingsDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Voice Models") },
                                    leadingIcon = { Icon(Icons.Rounded.Mic, contentDescription = null) },
                                    onClick = {
                                        showTopBarOverflow = false
                                        onOpenVoiceSettings()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Remove book", color = MaterialTheme.colorScheme.error) },
                                    leadingIcon = { Icon(Icons.Rounded.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        showTopBarOverflow = false
                                        book?.let(onDeleteBook)
                                    }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = isSlmChanged && mode == "deepstash",
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { it / 2 }
            ) {
                ExtendedFloatingActionButton(
                    onClick = {
                        lastGeneratedSlmId = selectedSlmId
                        lastGeneratedWholeBookScan = enableWholeBookScan
                        deepstashRegenCount++
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Regenerate Deepstash Summary"
                        )
                    },
                    text = {
                        Text(
                            text = "New SLM Selected • Tap to Regenerate",
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    elevation = FloatingActionButtonDefaults.elevation(8.dp)
                )
            }
        },
        bottomBar = {
            if (showTopControls) {
                androidx.compose.material3.NavigationBar {
                    NavigationBarItem(
                        selected = mode == "read",
                        onClick = { mode = "read" },
                        icon = { Icon(Icons.Rounded.Book, contentDescription = "Read") },
                        label = { Text("Read", maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    )
                    NavigationBarItem(
                        selected = mode == "audio",
                        onClick = { mode = "audio" },
                        icon = { Icon(Icons.Rounded.Headphones, contentDescription = "Audio") },
                        label = { Text("Audio", maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    )
                    NavigationBarItem(
                        selected = mode == "deepstash",
                        onClick = { mode = "deepstash" },
                        icon = { Icon(Icons.Rounded.AutoStories, contentDescription = "Deepstash") },
                        label = { Text("Deepstash", maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    )
                    NavigationBarItem(
                        selected = mode == "notes",
                        onClick = { mode = "notes" },
                        icon = { Icon(Icons.Rounded.Star, contentDescription = "Notes") },
                        label = { Text("Notes", maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    )
                    NavigationBarItem(
                        selected = mode == "details",
                        onClick = { mode = "details" },
                        icon = { Icon(Icons.Rounded.GraphicEq, contentDescription = "Details") },
                        label = { Text("Details", maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    )
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = if (isImmersive && mode == "read") 10.dp else 16.dp),
            verticalArrangement = Arrangement.spacedBy(if (isImmersive && mode == "read") 4.dp else 10.dp)
        ) {
            when (mode) {
                "audio" -> AudioPane(
                    bookId = bookId,
                    job = job,
                    segments = segments,
                    chapters = chapters,
                    voices = voices,
                    savedPosition = audioPlaybackPosition,
                    audioBookmarks = bookmarks.filter { it.type == "audio" },
                    jumpTarget = audioJumpTarget,
                    onJumpHandled = { audioJumpTarget = null },
                    onSavePosition = { segIdx, posMillis, chapId ->
                        scope.launch {
                            repository.saveAudioPlaybackPosition(bookId, segIdx, posMillis, chapId)
                        }
                    },
                    onAddAudioBookmark = { segIdx, posMillis, chapId, label, note ->
                        scope.launch {
                            repository.addAudioBookmark(bookId, chapId, segIdx, posMillis, label, note)
                        }
                    },
                    onDeleteBookmark = { bookmarkId ->
                        scope.launch {
                            repository.deleteBookmark(bookmarkId)
                        }
                    },
                    onInstallVoice = onInstallVoice,
                    onOpenVoiceSettings = onOpenVoiceSettings,
                    onPrepare = { repository.prepareAudiobook(bookId) }
                )
                "voices" -> VoiceModelsSettingsPane(
                    voices = voices,
                    selectedVoiceId = selectedVoiceId,
                    onSelectVoice = { selectedVoiceId = it },
                    onDownloadVoice = { onInstallVoice() },
                    enableSmartSkipping = enableSmartSkipping,
                    onToggleSmartSkipping = { enableSmartSkipping = it }
                )
                "deepstash" -> DeepstashPane(
                    summary = deepstashSummaryState,
                    isLoading = isDeepstashLoading,
                    onOpenSlmSettings = { showSlmSettingsDialog = true },
                    onRegenerate = {
                        lastGeneratedSlmId = selectedSlmId
                        lastGeneratedWholeBookScan = enableWholeBookScan
                        deepstashRegenCount++
                    }
                )
                "details" -> DetailsPane(metadata, characters)
                "notes" -> NotesPane(
                    bookmarks = bookmarks,
                    highlights = highlights,
                    chapters = chapters,
                    onSelectAudioBookmark = { bookmark ->
                        audioJumpTarget = Pair(bookmark.segmentIndex ?: 0, bookmark.audioTimestampMillis ?: 0L)
                        mode = "audio"
                    },
                    onSelectTextBookmark = { bookmark ->
                        val targetChapterIdx = chapters.indexOfFirst { it.id == bookmark.chapterId }
                        if (targetChapterIdx >= 0) {
                            chapterIndex = targetChapterIdx
                        }
                        mode = "read"
                    },
                    onDeleteBookmark = { bookmarkId ->
                        scope.launch {
                            repository.deleteBookmark(bookmarkId)
                        }
                    }
                )
                "search" -> SearchPane(chapters, repository)
                else -> ReaderPane(
                    chapter = selectedChapter,
                    chapters = chapters,
                    chapterCount = chapters.size,
                    chapterIndex = chapterIndex,
                    text = selectedChapter?.let(repository::readChapterText).orEmpty(),
                    isImmersive = isImmersive,
                    onToggleImmersive = { isImmersive = !isImmersive },
                    onPrevious = { chapterIndex = (chapterIndex - 1).coerceAtLeast(0) },
                    onNext = { chapterIndex = (chapterIndex + 1).coerceAtMost((chapters.size - 1).coerceAtLeast(0)) },
                    onSelectChapter = { index -> chapterIndex = index },
                    onBookmark = { label, note ->
                        selectedChapter?.let { chapter ->
                            scope.launch { repository.addBookmark(bookId, chapter.id, label, note) }
                        }
                    },
                    onHighlight = {
                        selectedChapter?.let { chapter ->
                            scope.launch { repository.addHighlight(bookId, chapter.id, "Opening passage") }
                        }
                    },
                    onTextClicked = { chapId, textOffset ->
                        val chapterSegments = segments.filter { it.chapterId == chapId }
                        if (chapterSegments.isNotEmpty()) {
                            val targetSeg = chapterSegments.firstOrNull { textOffset in it.textStartOffset..it.textEndOffset }
                                ?: chapterSegments.minByOrNull { kotlin.math.abs(it.textStartOffset - textOffset) }
                            targetSeg?.let { seg ->
                                val segIdx = segments.indexOf(seg)
                                if (segIdx >= 0) {
                                    val segLen = (seg.textEndOffset - seg.textStartOffset).coerceAtLeast(1)
                                    val offsetInSeg = (textOffset - seg.textStartOffset).coerceIn(0, segLen)
                                    val targetMillis = ((offsetInSeg.toFloat() / segLen.toFloat()) * seg.durationMillis).toLong()
                                    audioJumpTarget = Pair(segIdx, targetMillis)
                                    mode = "audio"
                                }
                            }
                        }
                    }
                )
            }
        }

        if (showSlmSettingsDialog) {
            SlmModelSettingsDialog(
                selectedSlmId = selectedSlmId,
                onSelectSlm = { selectedSlmId = it },
                enableWholeBookScan = enableWholeBookScan,
                onToggleWholeBookScan = { enableWholeBookScan = it },
                enableSmartSkipping = enableSmartSkipping,
                onToggleSmartSkipping = { enableSmartSkipping = it },
                onDismissRequest = { showSlmSettingsDialog = false }
            )
        }
    }
}



private data class ParagraphItem(val startOffset: Int, val endOffset: Int, val text: String)

@Composable
private fun ReaderPane(
    chapter: ChapterEntity?,
    chapters: List<ChapterEntity>,
    chapterCount: Int,
    chapterIndex: Int,
    text: String,
    isImmersive: Boolean,
    onToggleImmersive: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSelectChapter: (Int) -> Unit,
    onBookmark: (String, String?) -> Unit,
    onHighlight: () -> Unit,
    onTextClicked: (String, Int) -> Unit
) {
    val context = LocalContext.current
    var showBookmarkDialog by remember { mutableStateOf(false) }
    var showChapterDropdown by remember { mutableStateOf(false) }
    var bookmarkLabel by remember { mutableStateOf("") }
    var bookmarkNote by remember { mutableStateOf("") }

    // Live Read Aloud State (Android Text-To-Speech)
    var isLiveSpeaking by remember { mutableStateOf(false) }
    var isTtsReady by remember { mutableStateOf(false) }
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }

    val paragraphs = remember(text) {
        if (text.isBlank()) emptyList()
        else {
            val list = mutableListOf<ParagraphItem>()
            var searchPos = 0
            text.split("\n\n").forEach { rawPara ->
                val idx = text.indexOf(rawPara, searchPos)
                val start = if (idx != -1) idx else searchPos
                searchPos = start + rawPara.length
                if (rawPara.isNotBlank()) {
                    list.add(ParagraphItem(start, start + rawPara.length, rawPara.trim()))
                }
            }
            if (list.isEmpty()) {
                list.add(ParagraphItem(0, text.length, text.trim()))
            }
            list
        }
    }

    DisposableEffect(Unit) {
        var instance: TextToSpeech? = null
        instance = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                instance?.language = Locale.US
                ttsEngine = instance
                isTtsReady = true
                instance?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        isLiveSpeaking = true
                    }
                    override fun onDone(utteranceId: String?) {
                        isLiveSpeaking = false
                    }
                    override fun onError(utteranceId: String?) {
                        isLiveSpeaking = false
                    }
                })
            }
        }
        onDispose {
            instance?.stop()
            instance?.shutdown()
            ttsEngine = null
            isLiveSpeaking = false
            isTtsReady = false
        }
    }

    // Stop speaking when switching chapters
    LaunchedEffect(chapterIndex) {
        if (isLiveSpeaking) {
            ttsEngine?.stop()
            isLiveSpeaking = false
        }
    }

    fun speakCadences(cadences: List<SentenceCadence>) {
        val engine = ttsEngine ?: return
        if (cadences.isEmpty()) return
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        val currentVol = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 0
        if (currentVol == 0 && audioManager != null) {
            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (maxVol * 0.75f).toInt().coerceAtLeast(1), 0)
        }

        engine.stop()
        cadences.forEachIndexed { index, cadence ->
            val queueMode = if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            val params = Bundle().apply {
                putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
            }
            engine.setPitch(cadence.pitch)
            engine.setSpeechRate(cadence.speechRate)
            engine.speak(cadence.text, queueMode, params, "live_cadence_$index")
            if (cadence.pauseAfterMillis > 0) {
                engine.playSilentUtterance(cadence.pauseAfterMillis, TextToSpeech.QUEUE_ADD, "live_pause_$index")
            }
        }
        isLiveSpeaking = true
    }

    fun toggleLiveReadAloud() {
        val engine = ttsEngine ?: return
        if (isLiveSpeaking) {
            engine.stop()
            isLiveSpeaking = false
        } else {
            val cadences = HumanSpeechPacer.analyze(text)
            speakCadences(cadences)
        }
    }

    if (showBookmarkDialog && chapter != null) {
        AlertDialog(
            onDismissRequest = { showBookmarkDialog = false },
            title = { Text("Bookmark Chapter") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = bookmarkLabel,
                        onValueChange = { bookmarkLabel = it },
                        label = { Text("Label") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = bookmarkNote,
                        onValueChange = { bookmarkNote = it },
                        label = { Text("Note (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalLabel = bookmarkLabel.ifBlank { chapter.title }
                        onBookmark(finalLabel, bookmarkNote.ifBlank { null })
                        showBookmarkDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBookmarkDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Sleek E-Reader Header Control Bar
            AnimatedVisibility(
                visible = !isImmersive,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.95f),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 4.dp)
                ) {
                    Row(
                        Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = onPrevious,
                            enabled = chapterIndex > 0,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = "Previous Chapter")
                        }

                        Box(Modifier.weight(1f)) {
                            Surface(
                                onClick = { if (chapters.isNotEmpty()) showChapterDropdown = true },
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = chapter?.title ?: "Document Reader",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "(${chapterIndex + 1}/${chapterCount.coerceAtLeast(1)})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Icon(
                                        Icons.Rounded.ArrowDropDown,
                                        contentDescription = "Select Chapter",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            DropdownMenu(
                                expanded = showChapterDropdown,
                                onDismissRequest = { showChapterDropdown = false }
                            ) {
                                chapters.forEachIndexed { idx, ch ->
                                    DropdownMenuItem(
                                        text = { Text("${idx + 1}. ${ch.title}", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                        onClick = {
                                            onSelectChapter(idx)
                                            showChapterDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = onNext,
                            enabled = chapterIndex < chapterCount - 1,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Next Chapter")
                        }

                        IconButton(
                            onClick = {
                                bookmarkLabel = chapter?.title ?: "Chapter ${chapterIndex + 1}"
                                bookmarkNote = ""
                                showBookmarkDialog = true
                            },
                            enabled = chapter != null,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Rounded.BookmarkAdd, contentDescription = "Bookmark", modifier = Modifier.size(18.dp))
                        }

                        IconButton(
                            onClick = ::toggleLiveReadAloud,
                            enabled = isTtsReady && text.isNotBlank(),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                if (isLiveSpeaking) Icons.Rounded.Stop else Icons.Rounded.RecordVoiceOver,
                                contentDescription = if (isLiveSpeaking) "Stop Narration" else "Read Aloud",
                                tint = if (isLiveSpeaking) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onToggleImmersive,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                if (isImmersive) Icons.Rounded.FullscreenExit else Icons.Rounded.Fullscreen,
                                contentDescription = if (isImmersive) "Exit Fullscreen" else "Focus Mode",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            if (isLiveSpeaking) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MiniEqualizerBars(isPlaying = true, color = MaterialTheme.colorScheme.primary)
                        Text(
                            "Reading aloud with HumanSpeechPacer...",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { ttsEngine?.stop(); isLiveSpeaking = false }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Rounded.Stop, contentDescription = "Stop", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // Clean Prose Book Reader Canvas
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 12.dp, horizontal = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (paragraphs.isEmpty()) {
                    Box(
                        Modifier.fillMaxSize().padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "This document section does not contain extractable text.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    paragraphs.forEach { para ->
                        Text(
                            text = para.text,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 17.sp,
                                lineHeight = 27.sp,
                                fontFamily = FontFamily.Serif,
                                color = MaterialTheme.colorScheme.onBackground
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isLiveSpeaking) {
                                        val remainingText = text.substring(para.startOffset)
                                        val cadences = HumanSpeechPacer.analyze(remainingText)
                                        speakCadences(cadences)
                                    }
                                    chapter?.id?.let { chapId ->
                                        onTextClicked(chapId, para.startOffset)
                                    }
                                }
                                .padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // Floating pill button to toggle Focus Mode in Immersive state
        if (isImmersive) {
            Surface(
                onClick = onToggleImmersive,
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.9f),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 12.dp, end = 12.dp)
            ) {
                Row(
                    Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Rounded.FullscreenExit, contentDescription = "Exit Fullscreen", modifier = Modifier.size(16.dp))
                    Text("Exit Fullscreen", style = MaterialTheme.typography.labelSmall)
                }
            }
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
    audioBookmarks: List<BookmarkEntity>,
    jumpTarget: Pair<Int, Long>?,
    onJumpHandled: () -> Unit,
    onSavePosition: (Int, Long, String?) -> Unit,
    onAddAudioBookmark: (Int, Long, String?, String, String?) -> Unit,
    onDeleteBookmark: (Long) -> Unit,
    onInstallVoice: () -> Unit,
    onOpenVoiceSettings: () -> Unit,
    onPrepare: () -> Unit
) {
    val context = LocalContext.current
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager }
    val maxDeviceVolume = remember { audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15 }
    var deviceVolume by remember {
        mutableFloatStateOf(
            ((audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 10).toFloat() / maxDeviceVolume.toFloat()).coerceIn(0f, 1f)
        )
    }

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
    var playbackErrorMessage by remember { mutableStateOf<String?>(null) }
    var showAddBookmarkDialog by remember { mutableStateOf(false) }
    var bookmarkLabel by remember { mutableStateOf("") }
    var bookmarkNote by remember { mutableStateOf("") }

    val player = remember {
        MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setVolume(1.0f, 1.0f)
        }
    }
    val latestOnSavePosition by rememberUpdatedState(onSavePosition)

    val activeSegment = segments.getOrNull(activeSegmentIndex.coerceIn(0, (segments.size - 1).coerceAtLeast(0)))
    val activeSegmentDuration = activeSegment?.durationMillis?.coerceAtLeast(1000L) ?: 1000L

    fun ensureAudibleVolume() {
        audioManager?.let { am ->
            val cur = am.getStreamVolume(AudioManager.STREAM_MUSIC)
            if (cur <= 1) {
                val target = (maxDeviceVolume * 0.75f).toInt().coerceAtLeast(2)
                am.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)
                deviceVolume = target.toFloat() / maxDeviceVolume.toFloat()
            }
        }
    }

    fun playSegment(index: Int, startPositionMillis: Long = 0L) {
        if (index !in segments.indices) return
        val segment = segments[index]
        activeSegmentIndex = index
        playbackErrorMessage = null
        runCatching {
            ensureAudibleVolume()
            player.reset()
            player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            player.setVolume(1.0f, 1.0f)
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
            player.setOnErrorListener { _, what, extra ->
                isPlaying = false
                playbackErrorMessage = "Playback error ($what, $extra)"
                true
            }
        }.onFailure { ex ->
            isPlaying = false
            playbackErrorMessage = "Unable to play audio: ${ex.message}"
        }
    }

    // Handle jump target from bookmark selection
    LaunchedEffect(jumpTarget) {
        jumpTarget?.let { (targetSegment, targetMillis) ->
            if (targetSegment in segments.indices) {
                playSegment(targetSegment, targetMillis)
                onJumpHandled()
            }
        }
    }

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

    fun togglePlayPause() {
        if (segments.isEmpty()) return
        if (isPlaying) {
            val currentPos = runCatching { player.currentPosition.toLong() }.getOrDefault(currentPositionMillis)
            player.pause()
            isPlaying = false
            currentPositionMillis = currentPos
            latestOnSavePosition(activeSegmentIndex, currentPos, activeSegment?.chapterId)
        } else {
            ensureAudibleVolume()
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

    fun skipToPreviousChapter() {
        if (segments.isEmpty()) return
        val currentChapterId = activeSegment?.chapterId ?: return
        val currentChapIdx = chapters.indexOfFirst { it.id == currentChapterId }
        if (currentChapIdx > 0) {
            val targetChapId = chapters[currentChapIdx - 1].id
            val targetSegIdx = segments.indexOfFirst { it.chapterId == targetChapId }
            if (targetSegIdx >= 0) {
                playSegment(targetSegIdx, 0L)
            }
        } else {
            playSegment(0, 0L)
        }
    }

    fun skipToNextChapter() {
        if (segments.isEmpty()) return
        val currentChapterId = activeSegment?.chapterId ?: return
        val currentChapIdx = chapters.indexOfFirst { it.id == currentChapterId }
        if (currentChapIdx >= 0 && currentChapIdx + 1 < chapters.size) {
            val targetChapId = chapters[currentChapIdx + 1].id
            val targetSegIdx = segments.indexOfFirst { it.chapterId == targetChapId }
            if (targetSegIdx >= 0) {
                playSegment(targetSegIdx, 0L)
            }
        }
    }

    fun skipToPreviousLine() {
        if (segments.isEmpty()) return
        val targetIdx = (activeSegmentIndex - 1).coerceAtLeast(0)
        playSegment(targetIdx, 0L)
    }

    fun skipToNextLine() {
        if (segments.isEmpty()) return
        val targetIdx = (activeSegmentIndex + 1).coerceAtMost(segments.size - 1)
        playSegment(targetIdx, 0L)
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

    if (showAddBookmarkDialog) {
        val currentChapter = chapters.firstOrNull { it.id == activeSegment?.chapterId }
        AlertDialog(
            onDismissRequest = { showAddBookmarkDialog = false },
            title = { Text("Add Audio Bookmark") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Track ${activeSegmentIndex + 1} • ${formatAudioTime(currentPositionMillis)}" + (currentChapter?.let { " (${it.title})" } ?: ""),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    OutlinedTextField(
                        value = bookmarkLabel,
                        onValueChange = { bookmarkLabel = it },
                        label = { Text("Bookmark Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = bookmarkNote,
                        onValueChange = { bookmarkNote = it },
                        label = { Text("Note / Thought (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val defaultLabel = currentChapter?.let { "${it.title} - ${formatAudioTime(currentPositionMillis)}" }
                            ?: "Track ${activeSegmentIndex + 1} (${formatAudioTime(currentPositionMillis)})"
                        val finalLabel = bookmarkLabel.ifBlank { defaultLabel }
                        onAddAudioBookmark(
                            activeSegmentIndex,
                            currentPositionMillis,
                            activeSegment?.chapterId,
                            finalLabel,
                            bookmarkNote.ifBlank { null }
                        )
                        showAddBookmarkDialog = false
                    }
                ) {
                    Text("Save Bookmark")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBookmarkDialog = false }) {
                    Text("Cancel")
                }
            }
        )
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
        if (!playbackErrorMessage.isNullOrBlank()) {
            Text(playbackErrorMessage!!, color = MaterialTheme.colorScheme.error)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilledTonalButton(onClick = onOpenVoiceSettings) {
                Icon(Icons.Rounded.Mic, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Voice Models & Settings")
            }
            Button(onClick = onPrepare, enabled = installedVoice != null && synthesisReady) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    when {
                        installedVoice == null -> "Install voice first"
                        job?.status == "running" -> "Restarting..."
                        job?.status == "complete" -> "Regenerate Audio"
                        else -> "Generate Audio"
                    }
                )
            }
        }

        // Volume Level & Audio Output Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Row(
                Modifier
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = when {
                        deviceVolume <= 0.01f -> Icons.AutoMirrored.Rounded.VolumeOff
                        deviceVolume < 0.5f -> Icons.AutoMirrored.Rounded.VolumeDown
                        else -> Icons.AutoMirrored.Rounded.VolumeUp
                    },
                    contentDescription = "Volume",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    "Volume: ${(deviceVolume * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = deviceVolume,
                    onValueChange = { newVol ->
                        deviceVolume = newVol
                        audioManager?.let { am ->
                            val streamVol = (newVol * maxDeviceVolume).toInt().coerceIn(0, maxDeviceVolume)
                            am.setStreamVolume(AudioManager.STREAM_MUSIC, streamVol, 0)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                if (deviceVolume <= 0.01f) {
                    TextButton(
                        onClick = {
                            val target = (maxDeviceVolume * 0.8f).toInt().coerceAtLeast(2)
                            audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)
                            deviceVolume = target.toFloat() / maxDeviceVolume.toFloat()
                        }
                    ) {
                        Text("Unmute", fontWeight = FontWeight.Bold)
                    }
                }
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

        if (chapters.isNotEmpty() && segments.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "Chapters",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    chapters.forEachIndexed { idx, chap ->
                        val isCurrent = chap.id == activeSegment?.chapterId
                        FilterChip(
                            selected = isCurrent,
                            onClick = {
                                val segIdx = segments.indexOfFirst { it.chapterId == chap.id }
                                if (segIdx >= 0) {
                                    playSegment(segIdx, 0L)
                                }
                            },
                            label = { Text("Ch ${idx + 1}: ${chap.title}", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            leadingIcon = if (isCurrent && isPlaying) {
                                { MiniEqualizerBars(isPlaying = true, color = MaterialTheme.colorScheme.primary) }
                            } else null
                        )
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
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val currentChapter = chapters.firstOrNull { it.id == activeSegment?.chapterId }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = currentChapter?.title ?: "Narration Track",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Track ${activeSegmentIndex + 1} of ${segments.size}  •  ${formatAudioTime(currentPositionMillis)} / ${formatAudioTime(activeSegmentDuration)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Full Audio Playback Controls Bar
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton(
                            onClick = ::skipToPreviousChapter,
                            enabled = segments.isNotEmpty()
                        ) {
                            Icon(
                                Icons.Rounded.FirstPage,
                                contentDescription = "Previous Chapter",
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        IconButton(
                            onClick = ::skipToPreviousLine,
                            enabled = segments.isNotEmpty()
                        ) {
                            Icon(
                                Icons.Rounded.SkipPrevious,
                                contentDescription = "Previous Line",
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        IconButton(
                            onClick = { seekToMillis(currentPositionMillis - 10_000L) },
                            enabled = segments.isNotEmpty()
                        ) {
                            Icon(
                                Icons.Rounded.FastRewind,
                                contentDescription = "Rewind 10s",
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        FilledIconButton(
                            onClick = ::togglePlayPause,
                            modifier = Modifier.size(52.dp)
                        ) {
                            Icon(
                                if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        IconButton(
                            onClick = { seekToMillis(currentPositionMillis + 10_000L) },
                            enabled = segments.isNotEmpty()
                        ) {
                            Icon(
                                Icons.Rounded.FastForward,
                                contentDescription = "Forward 10s",
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        IconButton(
                            onClick = ::skipToNextLine,
                            enabled = segments.isNotEmpty()
                        ) {
                            Icon(
                                Icons.Rounded.SkipNext,
                                contentDescription = "Next Line",
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        IconButton(
                            onClick = ::skipToNextChapter,
                            enabled = segments.isNotEmpty()
                        ) {
                            Icon(
                                Icons.AutoMirrored.Rounded.LastPage,
                                contentDescription = "Next Chapter",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Waveform Visualizer
                    WaveformVisualizer(
                        segmentId = activeSegment?.id ?: "default",
                        currentPositionMillis = currentPositionMillis,
                        totalDurationMillis = activeSegmentDuration,
                        isPlaying = isPlaying,
                        onSeek = { seekToMillis(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(84.dp)
                            .padding(vertical = 4.dp)
                    )

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (isPlaying) {
                                MiniEqualizerBars(isPlaying = true, color = MaterialTheme.colorScheme.primary)
                            }
                            Text(
                                if (isPlaying) "Narrating • ${formatAudioTime(currentPositionMillis)}" else formatAudioTime(currentPositionMillis),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "-${formatAudioTime((activeSegmentDuration - currentPositionMillis).coerceAtLeast(0L))}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(
                                onClick = {
                                    val defLabel = currentChapter?.let { "${it.title} - ${formatAudioTime(currentPositionMillis)}" }
                                        ?: "Track ${activeSegmentIndex + 1} (${formatAudioTime(currentPositionMillis)})"
                                    bookmarkLabel = defLabel
                                    bookmarkNote = ""
                                    showAddBookmarkDialog = true
                                }
                            ) {
                                Icon(Icons.Rounded.BookmarkAdd, contentDescription = "Add bookmark", modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Bookmark", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Audio Bookmarks section
            if (audioBookmarks.isNotEmpty()) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Audio Bookmarks (${audioBookmarks.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                audioBookmarks.forEach { bookmark ->
                    val bookmarkChapter = chapters.firstOrNull { it.id == bookmark.chapterId }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                    ) {
                        Row(
                            Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                Modifier
                                    .size(36.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.Bookmark,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = bookmark.label,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Track ${(bookmark.segmentIndex ?: 0) + 1} • ${formatAudioTime(bookmark.audioTimestampMillis ?: 0L)}" +
                                            (bookmarkChapter?.let { " • ${it.title}" } ?: ""),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                val bookmarkNoteText = bookmark.note
                                if (!bookmarkNoteText.isNullOrBlank()) {
                                    Text(
                                        text = bookmarkNoteText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    playSegment(bookmark.segmentIndex ?: 0, bookmark.audioTimestampMillis ?: 0L)
                                }
                            ) {
                                Icon(
                                    Icons.Rounded.PlayCircleOutline,
                                    contentDescription = "Jump to bookmark",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(
                                onClick = { onDeleteBookmark(bookmark.id) }
                            ) {
                                Icon(
                                    Icons.Rounded.DeleteOutline,
                                    contentDescription = "Delete bookmark",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
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
                        containerColor = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (isCurrent && isPlaying) {
                            MiniEqualizerBars(isPlaying = true, color = MaterialTheme.colorScheme.primary)
                        } else {
                            Icon(
                                imageVector = if (isCurrent) Icons.AutoMirrored.Rounded.VolumeUp else Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                tint = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
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

@Composable
private fun WaveformVisualizer(
    segmentId: String,
    currentPositionMillis: Long,
    totalDurationMillis: Long,
    isPlaying: Boolean,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_pulse")
    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val playedFraction = (currentPositionMillis.toFloat() / totalDurationMillis.coerceAtLeast(1000L).toFloat()).coerceIn(0f, 1f)

    // Deterministic base amplitude pattern from segmentId
    val sampleCount = 48
    val baseAmplitudes = remember(segmentId) {
        val hash = abs(segmentId.hashCode())
        val random = java.util.Random(hash.toLong())
        FloatArray(sampleCount) { i ->
            val factor = 0.2f + 0.8f * random.nextFloat()
            val posRatio = i.toFloat() / sampleCount.toFloat()
            val envelope = sin(posRatio * Math.PI.toFloat()).coerceAtLeast(0.35f)
            (factor * envelope).coerceIn(0.18f, 1.0f)
        }
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.tertiary
    val inactiveColor = MaterialTheme.colorScheme.surfaceTint.copy(alpha = 0.25f)
    val backgroundTrackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)

    Box(
        modifier = modifier
            .background(backgroundTrackColor, RoundedCornerShape(12.dp))
            .pointerInput(totalDurationMillis, onSeek) {
                detectTapGestures { offset ->
                    val frac = (offset.x / size.width).coerceIn(0f, 1f)
                    onSeek((frac * totalDurationMillis).toLong())
                }
            }
            .pointerInput(totalDurationMillis, onSeek) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val frac = (change.position.x / size.width).coerceIn(0f, 1f)
                    onSeek((frac * totalDurationMillis).toLong())
                }
            }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val barSpacing = 3.dp.toPx()
            val totalSpacing = barSpacing * (sampleCount - 1)
            val barWidth = ((canvasWidth - totalSpacing) / sampleCount).coerceAtLeast(2.dp.toPx())
            val minBarHeight = 6.dp.toPx()
            val maxBarHeight = canvasHeight * 0.88f
            val centerY = canvasHeight / 2f
            val playheadX = canvasWidth * playedFraction

            for (i in 0 until sampleCount) {
                val x = i * (barWidth + barSpacing)
                val baseAmp = baseAmplitudes[i]

                // Live dynamic talking/narration animation
                val dynamicAmp = if (isPlaying) {
                    val distToPlayhead = abs(x - playheadX) / canvasWidth
                    val waveInfluence = (1f - distToPlayhead * 2f).coerceIn(0f, 1f)
                    val modulation = 0.15f * sin(pulsePhase + i * 0.45f) + 0.10f * sin(pulsePhase * 2f + i * 0.9f)
                    (baseAmp + modulation * (0.4f + 0.6f * waveInfluence)).coerceIn(0.12f, 1.0f)
                } else {
                    baseAmp
                }

                val currentBarHeight = (minBarHeight + (maxBarHeight - minBarHeight) * dynamicAmp)
                val top = centerY - currentBarHeight / 2f

                val isPlayed = (x + barWidth / 2f) <= playheadX
                val barColor = if (isPlayed) {
                    if (isPlaying && (x >= playheadX - barWidth * 3 && x <= playheadX)) secondaryColor else primaryColor
                } else {
                    inactiveColor
                }

                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(x, top),
                    size = Size(barWidth, currentBarHeight),
                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                )
            }

            // Draw Playhead line & glowing cursor dot
            drawLine(
                color = primaryColor,
                start = Offset(playheadX, 0f),
                end = Offset(playheadX, canvasHeight),
                strokeWidth = 2.5.dp.toPx()
            )

            drawCircle(
                color = if (isPlaying) secondaryColor else primaryColor,
                radius = if (isPlaying) 5.dp.toPx() else 4.dp.toPx(),
                center = Offset(playheadX, centerY)
            )
        }
    }
}

@Composable
private fun MiniEqualizerBars(
    isPlaying: Boolean,
    color: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mini_eq")
    val bar1 by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(420, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "b1"
    )
    val bar2 by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(560, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "b2"
    )
    val bar3 by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 0.95f,
        animationSpec = infiniteRepeatable(tween(380, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "b3"
    )

    Row(
        modifier = modifier.size(width = 16.dp, height = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        val bars = if (isPlaying) listOf(bar1, bar2, bar3) else listOf(0.4f, 0.4f, 0.4f)
        bars.forEach { fraction ->
            Box(
                Modifier
                    .weight(1f)
                    .height((14 * fraction).dp.coerceAtLeast(3.dp))
                    .background(color, RoundedCornerShape(1.dp))
            )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotesPane(
    bookmarks: List<BookmarkEntity>,
    highlights: List<HighlightEntity>,
    chapters: List<ChapterEntity>,
    onSelectAudioBookmark: (BookmarkEntity) -> Unit,
    onSelectTextBookmark: (BookmarkEntity) -> Unit,
    onDeleteBookmark: (Long) -> Unit
) {
    var selectedFilter by rememberSaveable { mutableStateOf("all") }
    val audioBookmarks = remember(bookmarks) { bookmarks.filter { it.type == "audio" } }
    val textBookmarks = remember(bookmarks) { bookmarks.filter { it.type != "audio" } }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Filter Chips
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedFilter == "all",
                onClick = { selectedFilter = "all" },
                label = { Text("All (${bookmarks.size + highlights.size})") }
            )
            FilterChip(
                selected = selectedFilter == "audio",
                onClick = { selectedFilter = "audio" },
                label = { Text("Audio (${audioBookmarks.size})") },
                leadingIcon = { Icon(Icons.Rounded.Headphones, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            FilterChip(
                selected = selectedFilter == "text",
                onClick = { selectedFilter = "text" },
                label = { Text("Reader (${textBookmarks.size})") },
                leadingIcon = { Icon(Icons.Rounded.Book, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            FilterChip(
                selected = selectedFilter == "highlights",
                onClick = { selectedFilter = "highlights" },
                label = { Text("Highlights (${highlights.size})") }
            )
        }

        // Audio Bookmarks section
        if (selectedFilter == "all" || selectedFilter == "audio") {
            if (audioBookmarks.isNotEmpty() || selectedFilter == "audio") {
                Text(
                    "Audio Bookmarks (${audioBookmarks.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (audioBookmarks.isEmpty()) {
                    Text(
                        "No audio bookmarks yet. Tap the bookmark icon while playing audio to save a timestamp.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    audioBookmarks.forEach { bookmark ->
                        val chapter = chapters.firstOrNull { it.id == bookmark.chapterId }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                        ) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        Modifier
                                            .size(36.dp)
                                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Rounded.Headphones,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            text = bookmark.label,
                                            fontWeight = FontWeight.SemiBold,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Text(
                                            text = "Track ${(bookmark.segmentIndex ?: 0) + 1} • ${formatAudioTime(bookmark.audioTimestampMillis ?: 0L)}" +
                                                    (chapter?.let { " • ${it.title}" } ?: ""),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    IconButton(onClick = { onDeleteBookmark(bookmark.id) }) {
                                        Icon(
                                            Icons.Rounded.DeleteOutline,
                                            contentDescription = "Delete bookmark",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }

                                val audioNoteText = bookmark.note
                                if (!audioNoteText.isNullOrBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = audioNoteText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }

                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    FilledTonalButton(
                                        onClick = { onSelectAudioBookmark(bookmark) },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Listen from ${formatAudioTime(bookmark.audioTimestampMillis ?: 0L)}")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Reader Bookmarks section
        if (selectedFilter == "all" || selectedFilter == "text") {
            if (textBookmarks.isNotEmpty() || selectedFilter == "text") {
                Text(
                    "Reader Bookmarks (${textBookmarks.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (textBookmarks.isEmpty()) {
                    Text(
                        "No reader bookmarks yet. Tap the bookmark button in reading mode.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    textBookmarks.forEach { bookmark ->
                        val chapter = chapters.firstOrNull { it.id == bookmark.chapterId }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                        ) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        Modifier
                                            .size(36.dp)
                                            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Rounded.Book,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            text = bookmark.label,
                                            fontWeight = FontWeight.SemiBold,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Text(
                                            text = chapter?.title ?: "Chapter Bookmark",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                    IconButton(onClick = { onDeleteBookmark(bookmark.id) }) {
                                        Icon(
                                            Icons.Rounded.DeleteOutline,
                                            contentDescription = "Delete bookmark",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }

                                val textNoteText = bookmark.note
                                if (!textNoteText.isNullOrBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = textNoteText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }

                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    FilledTonalButton(
                                        onClick = { onSelectTextBookmark(bookmark) },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Rounded.AutoStories, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Open Chapter")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Highlights section
        if (selectedFilter == "all" || selectedFilter == "highlights") {
            if (highlights.isNotEmpty() || selectedFilter == "highlights") {
                Text(
                    "Highlights (${highlights.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (highlights.isEmpty()) {
                    Text(
                        "No highlights yet. Tap highlight while reading to capture key passages.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    highlights.forEach { highlight ->
                        val chapter = chapters.firstOrNull { it.id == highlight.chapterId }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        ) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = highlight.note ?: "Highlight in ${chapter?.title ?: "Chapter"}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = chapter?.title ?: "Passage",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        if (bookmarks.isEmpty() && highlights.isEmpty()) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Rounded.BookmarkAdd,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        "No bookmarks or notes yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Create audio or reader bookmarks to easily jump back to favorite moments.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
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
        OutlinedTextField(
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

@Composable
fun ThemeActionIconButton() {
    val themeController = LocalThemeController.current
    var showDialog by remember { mutableStateOf(false) }

    IconButton(onClick = { showDialog = true }) {
        val icon = when (themeController.themeMode) {
            AppThemeMode.SYSTEM -> Icons.Rounded.BrightnessAuto
            AppThemeMode.LIGHT -> Icons.Rounded.LightMode
            AppThemeMode.DARK -> Icons.Rounded.DarkMode
        }
        val desc = "Theme: ${themeController.themeMode.title}"
        Icon(icon, contentDescription = desc, tint = MaterialTheme.colorScheme.onSurface)
    }

    if (showDialog) {
        ThemeSelectorDialog(
            currentMode = themeController.themeMode,
            onSelectMode = {
                themeController.setThemeMode(it)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
fun ThemeSelectorDialog(
    currentMode: AppThemeMode,
    onSelectMode: (AppThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    Icons.Rounded.Palette,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text("Appearance & Theme", fontWeight = FontWeight.SemiBold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Select your preferred interface theme. Setting applies immediately across the entire application.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(2.dp))
                AppThemeMode.entries.forEach { mode ->
                    val isSelected = mode == currentMode
                    Surface(
                        onClick = { onSelectMode(mode) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val icon = when (mode) {
                                AppThemeMode.SYSTEM -> Icons.Rounded.BrightnessAuto
                                AppThemeMode.LIGHT -> Icons.Rounded.LightMode
                                AppThemeMode.DARK -> Icons.Rounded.DarkMode
                            }
                            Box(
                                Modifier
                                    .size(36.dp)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                        RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    icon,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(Modifier.weight(1f)) {
                                Text(
                                    mode.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    mode.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    Icons.Rounded.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

