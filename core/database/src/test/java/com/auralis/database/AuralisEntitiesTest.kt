package com.auralis.database

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class AuralisEntitiesTest {
    @Test
    fun testBookEntity() {
        val entity = BookEntity("id1", "Title", "Author", "Pdf", "uri", "path", "Ready", 1L, 2L)
        assertEquals("id1", entity.id)
        assertEquals("Title", entity.title)
    }
}
