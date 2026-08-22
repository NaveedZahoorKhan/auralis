package com.auralis.reader.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class PdfTextExtractionServiceTest {

    @Test(expected = IllegalArgumentException::class)
    fun extractRawText_nonExistentFileThrows() {
        val service = PdfBoxTextExtractionService()
        val nonExistentFile = File("/tmp/non_existent_${System.currentTimeMillis()}.pdf")
        service.extractRawText(nonExistentFile)
    }

    @Test(expected = IllegalArgumentException::class)
    fun extractPageText_nonExistentFileThrows() {
        val service = PdfBoxTextExtractionService()
        val nonExistentFile = File("/tmp/non_existent_${System.currentTimeMillis()}.pdf")
        service.extractPageText(nonExistentFile, 1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun extractMetadata_nonExistentFileThrows() {
        val service = PdfBoxTextExtractionService()
        val nonExistentFile = File("/tmp/non_existent_${System.currentTimeMillis()}.pdf")
        service.extractMetadata(nonExistentFile)
    }

    @Test
    fun models_dataValidation() {
        val metadata = PdfMetadata(
            title = "Pride and Prejudice",
            author = "Jane Austen",
            subject = "Classic Literature",
            keywords = "novel, romance",
            creator = "Auralis Publisher",
            producer = "PDFBox Engine",
            pageCount = 350
        )
        assertEquals("Pride and Prejudice", metadata.title)
        assertEquals("Jane Austen", metadata.author)
        assertEquals("Classic Literature", metadata.subject)
        assertEquals("novel, romance", metadata.keywords)
        assertEquals("Auralis Publisher", metadata.creator)
        assertEquals("PDFBox Engine", metadata.producer)
        assertEquals(350, metadata.pageCount)

        val page1 = PdfPageContent(
            pageNumber = 1,
            text = "It is a truth universally acknowledged..."
        )
        assertEquals(1, page1.pageNumber)
        assertEquals("It is a truth universally acknowledged...".length, page1.characterCount)

        val page2 = PdfPageContent(
            pageNumber = 2,
            text = "My dear Mr. Bennet, said his lady to him one day..."
        )
        assertEquals(2, page2.pageNumber)

        val result = PdfRawTextResult(
            fullText = "${page1.text}\n\n${page2.text}",
            pageCount = 2,
            pages = listOf(page1, page2),
            metadata = metadata
        )
        assertFalse(result.isScannedOrEmpty)
        assertEquals(2, result.pages.size)
        assertEquals(2, result.pageCount)
        assertTrue(result.fullText.contains("truth universally acknowledged"))
    }

    @Test
    fun models_scannedOrEmptyDetection() {
        val emptyResult = PdfRawTextResult(
            fullText = "    \n   ",
            pageCount = 5,
            pages = emptyList(),
            metadata = PdfMetadata(pageCount = 5)
        )
        assertTrue(emptyResult.isScannedOrEmpty)

        val shortResult = PdfRawTextResult(
            fullText = "Too short",
            pageCount = 1,
            pages = listOf(PdfPageContent(1, "Too short")),
            metadata = PdfMetadata(pageCount = 1)
        )
        assertTrue(shortResult.isScannedOrEmpty)
    }
}

