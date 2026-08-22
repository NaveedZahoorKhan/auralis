package com.auralis.reader

import android.content.Context
import android.net.Uri
import com.auralis.ai.BookAnalysisInput
import com.auralis.ai.HeuristicBookAnalyzer
import com.auralis.audio.VoiceModelRepository
import com.auralis.database.AudiobookJobEntity
import com.auralis.database.AuralisDatabase
import com.auralis.database.BookEntity
import com.auralis.database.BookMetadataEntity
import com.auralis.database.BookmarkEntity
import com.auralis.database.ChapterEntity
import com.auralis.database.CharacterProfileEntity
import com.auralis.database.HighlightEntity
import com.auralis.database.PronunciationHintEntity
import com.auralis.database.ReadingPositionEntity
import com.auralis.jobs.AudiobookJobScheduler
import com.auralis.reader.core.BookImporter
import java.io.File
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AuralisRepository(private val context: Context) {
    private val dao = AuralisDatabase.get(context).dao()
    private val importer = BookImporter(context)
    private val analyzer = HeuristicBookAnalyzer()
    private val voiceRepository = VoiceModelRepository(context, dao)
    private val audiobookJobScheduler = AudiobookJobScheduler(context)

    val books: Flow<List<BookEntity>> = dao.observeBooks()
    val voices = dao.observeVoiceModels()

    fun observeBook(bookId: String) = dao.observeBook(bookId)
    fun observeChapters(bookId: String) = dao.observeChapters(bookId)
    fun observeMetadata(bookId: String) = dao.observeMetadata(bookId)
    fun observeCharacters(bookId: String) = dao.observeCharacters(bookId)
    fun observeJob(bookId: String) = dao.observeLatestAudiobookJob(bookId)
    fun observeBookmarks(bookId: String) = dao.observeBookmarks(bookId)
    fun observeAudioBookmarks(bookId: String) = dao.observeAudioBookmarks(bookId)
    fun observeHighlights(bookId: String) = dao.observeHighlights(bookId)
    fun observeAudioSegments(bookId: String): Flow<List<com.auralis.database.AudioSegmentEntity>> = dao.observeAudioSegments(bookId)
    fun observeAudioPlaybackPosition(bookId: String): Flow<com.auralis.database.AudioPlaybackPositionEntity?> = dao.observeAudioPlaybackPosition(bookId)

    suspend fun getAudioPlaybackPosition(bookId: String): com.auralis.database.AudioPlaybackPositionEntity? = dao.getAudioPlaybackPosition(bookId)

    suspend fun saveAudioPlaybackPosition(
        bookId: String,
        segmentIndex: Int,
        positionMillis: Long,
        chapterId: String? = null
    ) = withContext(Dispatchers.IO) {
        dao.upsertAudioPlaybackPosition(
            com.auralis.database.AudioPlaybackPositionEntity(
                bookId = bookId,
                segmentIndex = segmentIndex,
                positionMillis = positionMillis,
                chapterId = chapterId,
                updatedAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun seedVoiceCatalog() = voiceRepository.seedCatalog()

    suspend fun importSampleBook(): String = withContext(Dispatchers.IO) {
        val sampleDir = File(context.cacheDir, "sample_books").also { it.mkdirs() }
        val sampleEpub = File(sampleDir, "The_Time_Machine.epub")
        writeSampleEpub(sampleEpub)
        importBook(Uri.fromFile(sampleEpub))
    }

    private fun writeSampleEpub(outputFile: File) {
        java.util.zip.ZipOutputStream(outputFile.outputStream().buffered()).use { zip ->
            // Mimetype must be first and uncompressed
            val mimeEntry = java.util.zip.ZipEntry("mimetype").apply { method = java.util.zip.ZipEntry.STORED }
            val mimeBytes = "application/epub+zip".toByteArray(Charsets.US_ASCII)
            mimeEntry.size = mimeBytes.size.toLong()
            mimeEntry.compressedSize = mimeBytes.size.toLong()
            val crc = java.util.zip.CRC32().apply { update(mimeBytes) }
            mimeEntry.crc = crc.value
            zip.putNextEntry(mimeEntry)
            zip.write(mimeBytes)
            zip.closeEntry()

            // Container
            zip.putNextEntry(java.util.zip.ZipEntry("META-INF/container.xml"))
            zip.write(
                """<?xml version="1.0"?>
                <container version="1.0" xmlns="urn:oasis:names:tc:opendocument:xmlns:container">
                  <rootfiles>
                    <rootfile full-path="OEBPS/content.opf" media-type="application/oebps-package+xml"/>
                  </rootfiles>
                </container>""".trimIndent().toByteArray(Charsets.UTF_8)
            )
            zip.closeEntry()

            // OPF
            zip.putNextEntry(java.util.zip.ZipEntry("OEBPS/content.opf"))
            zip.write(
                """<?xml version="1.0" encoding="utf-8"?>
                <package xmlns="http://www.idpf.org/2007/opf" unique-identifier="BookID" version="2.0">
                  <metadata xmlns:dc="http://purl.org/dc/elements/1.1/">
                    <dc:title>The Time Machine</dc:title>
                    <dc:creator>H. G. Wells</dc:creator>
                    <dc:language>en</dc:language>
                  </metadata>
                  <manifest>
                    <item id="chapter1" href="chapter_1.xhtml" media-type="application/xhtml+xml"/>
                    <item id="chapter2" href="chapter_2.xhtml" media-type="application/xhtml+xml"/>
                    <item id="chapter3" href="chapter_3.xhtml" media-type="application/xhtml+xml"/>
                  </manifest>
                  <spine>
                    <itemref idref="chapter1"/>
                    <itemref idref="chapter2"/>
                    <itemref idref="chapter3"/>
                  </spine>
                </package>""".trimIndent().toByteArray(Charsets.UTF_8)
            )
            zip.closeEntry()

            // Chapter 1
            zip.putNextEntry(java.util.zip.ZipEntry("OEBPS/chapter_1.xhtml"))
            zip.write(
                """<?xml version="1.0" encoding="utf-8"?>
                <html xmlns="http://www.w3.org/1999/xhtml">
                <head><title>Chapter 1: The Machine</title></head>
                <body>
                  <h1>Chapter 1: The Machine</h1>
                  <p>The Time Traveller was expounding a recondite matter to us. His grey eyes shone and twinkled, and his usually pale face was flushed and animated.</p>
                  <p>The fire burned brightly, and the soft radiance of the incandescent lights in the lilies of silver caught the bubbles that flashed and passed in our glasses.</p>
                  <p>Our chairs, being his patents, embraced and caressed us rather than submitted to be sat upon, and there was that luxurious after-dinner atmosphere when thought roams gracefully free of the trammels of precision.</p>
                </body>
                </html>""".trimIndent().toByteArray(Charsets.UTF_8)
            )
            zip.closeEntry()

            // Chapter 2
            zip.putNextEntry(java.util.zip.ZipEntry("OEBPS/chapter_2.xhtml"))
            zip.write(
                """<?xml version="1.0" encoding="utf-8"?>
                <html xmlns="http://www.w3.org/1999/xhtml">
                <head><title>Chapter 2: The Fourth Dimension</title></head>
                <body>
                  <h1>Chapter 2: The Fourth Dimension</h1>
                  <p>You must follow me carefully. I shall have to controvert one or two ideas that are almost universally accepted. The geometry, for instance, they taught you at school is founded on a misconception.</p>
                  <p>There are really four dimensions, three which we call the three planes of Space, and a fourth, Time. There is, however, a tendency to draw an unreal distinction between the former three dimensions and the latter.</p>
                  <p>It is simply this: that our consciousness moves along it.</p>
                </body>
                </html>""".trimIndent().toByteArray(Charsets.UTF_8)
            )
            zip.closeEntry()

            // Chapter 3
            zip.putNextEntry(java.util.zip.ZipEntry("OEBPS/chapter_3.xhtml"))
            zip.write(
                """<?xml version="1.0" encoding="utf-8"?>
                <html xmlns="http://www.w3.org/1999/xhtml">
                <head><title>Chapter 3: The Journey Begins</title></head>
                <body>
                  <h1>Chapter 3: The Journey Begins</h1>
                  <p>I took the starting lever in one hand and the stopping lever in the other, pressed the first, and almost immediately the second. I seemed to reel; I felt a nightmare sensation of falling.</p>
                  <p>Looking round the laboratory, I saw everything just as it was before. Had anything happened? For a moment I suspected that my intellect had tricked me.</p>
                  <p>Then I noted the clock. A moment before, as it seemed, it had stood at a minute or so past ten; now it was nearly half-past three!</p>
                </body>
                </html>""".trimIndent().toByteArray(Charsets.UTF_8)
            )
            zip.closeEntry()
        }
    }

    suspend fun importBook(uri: Uri): String = withContext(Dispatchers.IO) {
        val imported = importer.import(uri)
        val now = System.currentTimeMillis()
        val chapters = imported.chapters.map {
            ChapterEntity(
                id = it.id,
                bookId = imported.id,
                title = it.title,
                sortIndex = it.sortIndex,
                textPath = it.textPath,
                characterCount = it.characterCount,
                pageStart = it.pageStart,
                pageEnd = it.pageEnd
            )
        }
        val sample = imported.chapters
            .take(3)
            .joinToString("\n\n") { File(it.textPath).readText().take(12_000) }
        val analysis = analyzer.analyze(
            BookAnalysisInput(
                title = imported.title,
                chapterTitles = imported.chapters.map { it.title },
                textSample = sample
            )
        )

        dao.insertImportedBook(
            book = BookEntity(
                id = imported.id,
                title = imported.title,
                author = imported.author,
                format = imported.format.name,
                sourceUri = imported.sourceUri,
                localPath = imported.localPath,
                importStatus = imported.importStatus.name,
                createdAtMillis = now,
                updatedAtMillis = now
            ),
            chapters = chapters,
            metadata = BookMetadataEntity(
                bookId = imported.id,
                language = analysis.language,
                genre = analysis.genre,
                tone = analysis.tone,
                synopsis = analysis.synopsis,
                source = analysis.source,
                confidence = analysis.confidence,
                updatedAtMillis = now
            ),
            characters = analysis.characters.map {
                CharacterProfileEntity(
                    id = stableId(imported.id, it.name),
                    bookId = imported.id,
                    name = it.name,
                    aliases = it.aliases.joinToString("|"),
                    description = it.description,
                    pronunciation = it.pronunciation,
                    confidence = it.confidence
                )
            },
            hints = analysis.pronunciationHints.map {
                PronunciationHintEntity(
                    id = stableId(imported.id, it.phrase),
                    bookId = imported.id,
                    phrase = it.phrase,
                    hint = it.hint,
                    source = it.source
                )
            },
            job = AudiobookJobEntity(
                id = "job-${imported.id}",
                bookId = imported.id,
                voiceModelId = null,
                status = "not_started",
                currentChapterId = null,
                completedSegments = 0,
                totalSegments = chapters.size,
                lastError = null,
                updatedAtMillis = now
            )
        )
        imported.id
    }

    suspend fun saveReadingPosition(bookId: String, chapterId: String?) {
        dao.upsertReadingPosition(
            ReadingPositionEntity(
                bookId = bookId,
                chapterId = chapterId,
                textOffset = 0,
                pageIndex = null,
                updatedAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun addBookmark(bookId: String, chapterId: String, label: String, note: String? = null) {
        dao.insertBookmark(
            BookmarkEntity(
                bookId = bookId,
                chapterId = chapterId,
                textOffset = 0,
                label = label,
                note = note,
                type = "text",
                createdAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun addAudioBookmark(
        bookId: String,
        chapterId: String?,
        segmentIndex: Int,
        timestampMillis: Long,
        label: String,
        note: String? = null
    ) {
        dao.insertBookmark(
            BookmarkEntity(
                bookId = bookId,
                chapterId = chapterId,
                textOffset = 0,
                label = label,
                note = note,
                audioTimestampMillis = timestampMillis,
                segmentIndex = segmentIndex,
                type = "audio",
                createdAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteBookmark(bookmarkId: Long) {
        dao.deleteBookmark(bookmarkId)
    }

    suspend fun addHighlight(bookId: String, chapterId: String, note: String) {
        dao.insertHighlight(
            HighlightEntity(
                bookId = bookId,
                chapterId = chapterId,
                startOffset = 0,
                endOffset = 160,
                note = note,
                colorName = "sage",
                createdAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun installVoice(uri: Uri) = withContext(Dispatchers.IO) {
        voiceRepository.installOnnxVoice(uri)
    }

    suspend fun downloadDefaultVoice() = withContext(Dispatchers.IO) {
        voiceRepository.downloadDefaultKokoroVoice()
    }

    fun prepareAudiobook(bookId: String) {
        audiobookJobScheduler.enqueue(bookId)
    }

    fun readChapterText(chapter: ChapterEntity): String {
        return File(chapter.textPath).takeIf { it.exists() }?.readText().orEmpty()
    }

    private fun stableId(bookId: String, value: String): String {
        return UUID.nameUUIDFromBytes("$bookId:$value".toByteArray()).toString()
    }
}
