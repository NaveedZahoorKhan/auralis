package com.auralis.reader.core

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.util.Locale
import java.util.UUID
import java.util.zip.ZipInputStream

class BookImporter(
    private val context: Context,
    private val pdfExtractionService: PdfTextExtractionService = PdfBoxTextExtractionService(context)
) {
    suspend fun import(uri: Uri): ImportedBook {
        val id = UUID.randomUUID().toString()
        val displayName = queryDisplayName(uri) ?: "Imported book"
        val format = inferFormat(uri, displayName)
        val bookDir = File(context.filesDir, "books/$id").also { it.mkdirs() }
        val sourceFile = File(bookDir, "source.${format.extensionFor(displayName)}")

        val sourceStream = runCatching {
            context.contentResolver.openInputStream(uri)
        }.getOrNull() ?: run {
            val path = uri.path
            if (path != null && File(path).exists()) File(path).inputStream() else null
        }
        requireNotNull(sourceStream) { "Unable to open selected book." }
        sourceStream.use { input ->
            sourceFile.outputStream().use { output -> input.copyTo(output) }
        }

        val extraction = when (format) {
            BookFormat.Epub -> extractEpub(sourceFile, bookDir, id)
            BookFormat.Pdf -> extractPdf(sourceFile, bookDir, id)
            BookFormat.Unknown -> ExtractionResult(emptyList(), ImportStatus.Unsupported)
        }

        return ImportedBook(
            id = id,
            title = displayName.toTitle(),
            author = null,
            format = format,
            sourceUri = uri.toString(),
            localPath = sourceFile.absolutePath,
            chapters = extraction.chapters,
            importStatus = extraction.status
        )
    }

    private fun queryDisplayName(uri: Uri): String? {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
        } ?: uri.lastPathSegment
    }

    private fun inferFormat(uri: Uri, displayName: String): BookFormat {
        val mime = context.contentResolver.getType(uri)?.lowercase(Locale.US).orEmpty()
        val name = displayName.lowercase(Locale.US)
        return when {
            mime == "application/epub+zip" || name.endsWith(".epub") -> BookFormat.Epub
            mime == "application/pdf" || name.endsWith(".pdf") -> BookFormat.Pdf
            else -> BookFormat.Unknown
        }
    }

    private fun extractPdf(sourceFile: File, bookDir: File, bookId: String): ExtractionResult {
        val result = runCatching {
            pdfExtractionService.extractRawText(sourceFile, includePerPages = false)
        }.getOrElse {
            return ExtractionResult(emptyList(), ImportStatus.Unsupported)
        }

        val normalized = result.fullText.normalizeWhitespace()
        if (result.isScannedOrEmpty || normalized.length < 120) {
            return ExtractionResult(emptyList(), ImportStatus.NeedsOcr)
        }

        val chapters = writeChapters(
            bookDir = bookDir,
            bookId = bookId,
            rawSections = splitIntoBookSections(normalized),
            pageCount = result.pageCount
        )
        return ExtractionResult(chapters, ImportStatus.Ready)
    }

    private fun extractEpub(sourceFile: File, bookDir: File, bookId: String): ExtractionResult {
        val sections = mutableListOf<NamedText>()
        ZipInputStream(sourceFile.inputStream()).use { zip ->
            generateSequence { zip.nextEntry }.forEach { entry ->
                val lowerName = entry.name.lowercase(Locale.US)
                if (!entry.isDirectory && (lowerName.endsWith(".xhtml") || lowerName.endsWith(".html") || lowerName.endsWith(".htm"))) {
                    val html = zip.readBytes().toString(Charsets.UTF_8)
                    val text = htmlToText(html).normalizeWhitespace()
                    if (text.length > 50) {
                        val parsedHeading = extractHtmlHeading(html)
                        val title = when {
                            lowerName.contains("toc") || lowerName.contains("contents") -> "Table of Contents"
                            !parsedHeading.isNullOrBlank() -> parsedHeading
                            else -> titleFromPath(entry.name)
                        }
                        sections += NamedText(title, text)
                    }
                }
            }
        }

        if (sections.isEmpty()) {
            return ExtractionResult(emptyList(), ImportStatus.Unsupported)
        }

        return ExtractionResult(
            chapters = writeChapters(bookDir, bookId, sections),
            status = ImportStatus.Ready
        )
    }

    private fun extractHtmlHeading(html: String): String? {
        val h1 = Regex("(?is)<h1[^>]*>(.*?)</h1>").find(html)?.groupValues?.get(1)
        val h2 = Regex("(?is)<h2[^>]*>(.*?)</h2>").find(html)?.groupValues?.get(1)
        val title = Regex("(?is)<title[^>]*>(.*?)</title>").find(html)?.groupValues?.get(1)
        val raw = (h1 ?: h2 ?: title)?.let { htmlToText(it).trim() } ?: return null
        val clean = raw.replace(Regex("\\s+"), " ").trim()
        return if (clean.length in 3..90 && !clean.lowercase().contains("untitled")) clean else null
    }

    private fun writeChapters(
        bookDir: File,
        bookId: String,
        rawSections: List<NamedText>,
        pageCount: Int? = null
    ): List<ExtractedChapter> {
        val chapterDir = File(bookDir, "chapters").also { it.mkdirs() }
        val sections = if (rawSections.isEmpty()) listOf(NamedText("Chapter 1", "")) else rawSections
        return sections.mapIndexed { index, section ->
            val chapterId = "$bookId-${index + 1}"
            val textFile = File(chapterDir, "${index.toString().padStart(4, '0')}.txt")
            textFile.writeText(section.text)
            ExtractedChapter(
                id = chapterId,
                title = section.title.ifBlank { "Chapter ${index + 1}" },
                sortIndex = index,
                textPath = textFile.absolutePath,
                characterCount = section.text.length,
                pageStart = pageCount?.let { ((index.toFloat() / sections.size) * it).toInt().coerceAtLeast(0) },
                pageEnd = pageCount?.let { ((((index + 1).toFloat() / sections.size) * it).toInt() - 1).coerceAtLeast(0) }
            )
        }
    }

    private fun splitIntoBookSections(text: String): List<NamedText> {
        // Advanced multi-pattern chapter and TOC header matcher
        val marker = Regex("(?im)(^\\s*(table of contents|contents|index of chapters|chapter|book|part|section|unit|module|introduction|preface|foreword|epilogue|conclusion|appendix)\\b[\\w\\-.' :]{0,80}$)")
        val numberedMarker = Regex("(?im)(^\\s*([0-9]{1,2}|[IVXLCDM]{1,6})[.\\s–-]+([A-Z][\\w\\-.' :]{2,80})$)")

        var matches = marker.findAll(text).toList()
        if (matches.size < 2) {
            matches = numberedMarker.findAll(text).toList()
        }

        if (matches.size < 2) {
            return chunkText(text, 18_000).mapIndexed { index, chunk ->
                val isToc = index == 0 && (chunk.take(500).lowercase().contains("contents") || chunk.take(500).lowercase().contains("table of contents"))
                NamedText(if (isToc) "Table of Contents" else "Section ${index + 1}", chunk)
            }
        }

        return matches.mapIndexed { index, match ->
            val start = match.range.first
            val end = matches.getOrNull(index + 1)?.range?.first ?: text.length
            var title = match.value.trim().take(80)
            val lowerTitle = title.lowercase()
            if (lowerTitle.contains("contents") || lowerTitle.contains("table of contents")) {
                title = "Table of Contents"
            }
            NamedText(title, text.substring(start, end).trim())
        }
    }

    private fun chunkText(text: String, targetSize: Int): List<String> {
        if (text.length <= targetSize) return listOf(text)
        val chunks = mutableListOf<String>()
        var cursor = 0
        while (cursor < text.length) {
            val end = (cursor + targetSize).coerceAtMost(text.length)
            val sentenceEnd = text.lastIndexOf('.', end).takeIf { it > cursor + targetSize / 2 } ?: end
            chunks += text.substring(cursor, sentenceEnd).trim()
            cursor = sentenceEnd.coerceAtLeast(cursor + 1)
        }
        return chunks.filter { it.isNotBlank() }
    }

    private fun htmlToText(html: String): String {
        return html
            .replace(Regex("(?is)<script.*?</script>"), " ")
            .replace(Regex("(?is)<style.*?</style>"), " ")
            .replace(Regex("(?is)<[^>]+>"), " ")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
    }

    private fun titleFromPath(path: String): String {
        return path.substringAfterLast('/')
            .substringBeforeLast('.')
            .replace('_', ' ')
            .replace('-', ' ')
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .joinToString(" ") { word -> word.replaceFirstChar { it.titlecase(Locale.US) } }
            .ifBlank { "Chapter" }
    }

    private fun String.normalizeWhitespace(): String {
        return replace("\u0000", " ")
            .replace(Regex("[ \\t\\x0B\\f\\r]+"), " ")
            .replace(Regex("\\n{3,}"), "\n\n")
            .trim()
    }

    private fun String.toTitle(): String {
        return substringBeforeLast('.')
            .replace('_', ' ')
            .replace('-', ' ')
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .joinToString(" ") { word -> word.replaceFirstChar { it.titlecase(Locale.US) } }
            .ifBlank { this }
    }

    private fun BookFormat.extensionFor(displayName: String): String {
        val extension = displayName.substringAfterLast('.', missingDelimiterValue = "")
        if (extension.length in 2..6) return extension.lowercase(Locale.US)
        return when (this) {
            BookFormat.Pdf -> "pdf"
            BookFormat.Epub -> "epub"
            BookFormat.Unknown -> "book"
        }
    }

    private data class NamedText(val title: String, val text: String)
    private data class ExtractionResult(val chapters: List<ExtractedChapter>, val status: ImportStatus)
}
