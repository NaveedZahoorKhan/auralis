package com.auralis.reader.core

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Metadata extracted from a PDF document.
 */
data class PdfMetadata(
    val title: String? = null,
    val author: String? = null,
    val subject: String? = null,
    val keywords: String? = null,
    val creator: String? = null,
    val producer: String? = null,
    val pageCount: Int = 0
)

/**
 * Text content extracted from an individual PDF page.
 * @param pageNumber 1-indexed page number
 * @param text The raw extracted text on this page
 * @param characterCount Length of the text string
 */
data class PdfPageContent(
    val pageNumber: Int,
    val text: String,
    val characterCount: Int = text.length
)

/**
 * Complete raw text extraction result from a PDF document.
 */
data class PdfRawTextResult(
    val fullText: String,
    val pageCount: Int,
    val pages: List<PdfPageContent> = emptyList(),
    val metadata: PdfMetadata = PdfMetadata(pageCount = pageCount),
    val isScannedOrEmpty: Boolean = fullText.trim().length < 50
)

/**
 * Service contract for extracting raw text and metadata from PDF files using PDFBox.
 */
interface PdfTextExtractionService {
    fun extractRawText(file: File, includePerPages: Boolean = true): PdfRawTextResult
    fun extractRawText(inputStream: InputStream, includePerPages: Boolean = true): PdfRawTextResult
    fun extractRawText(bytes: ByteArray, includePerPages: Boolean = true): PdfRawTextResult
    fun extractRawText(context: Context, uri: Uri, includePerPages: Boolean = true): PdfRawTextResult
    fun extractPageText(file: File, pageNumber: Int): String
    fun extractPageRangeText(file: File, startPage: Int, endPage: Int): String
    fun extractMetadata(file: File): PdfMetadata
}

/**
 * Default implementation of [PdfTextExtractionService] backed by PDFBox Android.
 */
class PdfBoxTextExtractionService(
    private val defaultContext: Context? = null
) : PdfTextExtractionService {

    init {
        defaultContext?.let { initPdfBox(it) }
    }

    override fun extractRawText(file: File, includePerPages: Boolean): PdfRawTextResult {
        require(file.exists() && file.isFile) { "PDF file does not exist: ${file.absolutePath}" }
        defaultContext?.let { initPdfBox(it) }
        return PDDocument.load(file).use { document ->
            processDocument(document, includePerPages)
        }
    }

    override fun extractRawText(inputStream: InputStream, includePerPages: Boolean): PdfRawTextResult {
        defaultContext?.let { initPdfBox(it) }
        return PDDocument.load(inputStream).use { document ->
            processDocument(document, includePerPages)
        }
    }

    override fun extractRawText(bytes: ByteArray, includePerPages: Boolean): PdfRawTextResult {
        defaultContext?.let { initPdfBox(it) }
        return ByteArrayInputStream(bytes).use { stream ->
            PDDocument.load(stream).use { document ->
                processDocument(document, includePerPages)
            }
        }
    }

    override fun extractRawText(context: Context, uri: Uri, includePerPages: Boolean): PdfRawTextResult {
        initPdfBox(context)
        val stream = context.contentResolver.openInputStream(uri)
            ?: run {
                val path = uri.path
                if (path != null && File(path).exists()) File(path).inputStream() else null
            }
        requireNotNull(stream) { "Unable to open input stream for URI: $uri" }
        return stream.use { input ->
            PDDocument.load(input).use { document ->
                processDocument(document, includePerPages)
            }
        }
    }

    override fun extractPageText(file: File, pageNumber: Int): String {
        return extractPageRangeText(file, pageNumber, pageNumber)
    }

    override fun extractPageRangeText(file: File, startPage: Int, endPage: Int): String {
        require(file.exists() && file.isFile) { "PDF file does not exist: ${file.absolutePath}" }
        defaultContext?.let { initPdfBox(it) }
        return PDDocument.load(file).use { document ->
            val totalPages = document.numberOfPages
            val start = startPage.coerceIn(1, totalPages)
            val end = endPage.coerceIn(start, totalPages)

            val stripper = PDFTextStripper().apply {
                this.startPage = start
                this.endPage = end
                sortByPosition = true
            }
            stripper.getText(document)
        }
    }

    override fun extractMetadata(file: File): PdfMetadata {
        require(file.exists() && file.isFile) { "PDF file does not exist: ${file.absolutePath}" }
        defaultContext?.let { initPdfBox(it) }
        return PDDocument.load(file).use { document ->
            readMetadata(document)
        }
    }

    private fun processDocument(document: PDDocument, includePerPages: Boolean): PdfRawTextResult {
        val totalPages = document.numberOfPages
        val metadata = readMetadata(document)

        val stripper = PDFTextStripper().apply {
            sortByPosition = true
        }
        val fullText = stripper.getText(document) ?: ""

        val pages = if (includePerPages && totalPages > 0) {
            (1..totalPages).map { pageNum ->
                val pageStripper = PDFTextStripper().apply {
                    startPage = pageNum
                    endPage = pageNum
                    sortByPosition = true
                }
                val pageText = runCatching { pageStripper.getText(document) }.getOrDefault("")
                PdfPageContent(
                    pageNumber = pageNum,
                    text = pageText
                )
            }
        } else {
            emptyList()
        }

        return PdfRawTextResult(
            fullText = fullText,
            pageCount = totalPages,
            pages = pages,
            metadata = metadata
        )
    }

    private fun readMetadata(document: PDDocument): PdfMetadata {
        val info = document.documentInformation
        return PdfMetadata(
            title = info?.title?.trim()?.ifBlank { null },
            author = info?.author?.trim()?.ifBlank { null },
            subject = info?.subject?.trim()?.ifBlank { null },
            keywords = info?.keywords?.trim()?.ifBlank { null },
            creator = info?.creator?.trim()?.ifBlank { null },
            producer = info?.producer?.trim()?.ifBlank { null },
            pageCount = document.numberOfPages
        )
    }

    companion object {
        private val isInitialized = AtomicBoolean(false)

        fun initPdfBox(context: Context) {
            if (!isInitialized.get()) {
                synchronized(isInitialized) {
                    if (!isInitialized.get()) {
                        runCatching {
                            if (!PDFBoxResourceLoader.isReady()) {
                                PDFBoxResourceLoader.init(context.applicationContext)
                            }
                        }
                        isInitialized.set(true)
                    }
                }
            }
        }
    }
}
