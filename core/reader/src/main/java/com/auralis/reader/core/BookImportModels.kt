package com.auralis.reader.core

data class ImportedBook(
    val id: String,
    val title: String,
    val author: String?,
    val format: BookFormat,
    val sourceUri: String,
    val localPath: String,
    val chapters: List<ExtractedChapter>,
    val importStatus: ImportStatus
)

data class ExtractedChapter(
    val id: String,
    val title: String,
    val sortIndex: Int,
    val textPath: String,
    val characterCount: Int,
    val pageStart: Int? = null,
    val pageEnd: Int? = null
)

enum class BookFormat {
    Pdf,
    Epub,
    Unknown
}

enum class ImportStatus {
    Ready,
    NeedsOcr,
    Unsupported
}
