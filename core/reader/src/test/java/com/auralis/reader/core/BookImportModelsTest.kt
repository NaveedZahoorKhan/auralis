package com.auralis.reader.core

import org.junit.Assert.assertEquals
import org.junit.Test

class BookImportModelsTest {
    @Test
    fun testImportedBook() {
        val chapter = ExtractedChapter("c1", "Chap 1", 0, "path", 100, 1, 2)
        val book = ImportedBook("b1", "Title", "Author", BookFormat.Pdf, "uri", "localPath", listOf(chapter), ImportStatus.Ready)
        assertEquals("b1", book.id)
        assertEquals(BookFormat.Pdf, book.format)
        assertEquals(ImportStatus.Ready, book.importStatus)
    }
}
