package com.auralis.audio

object SmartContentFilter {

    private val HEADER_FOOTER_PATTERNS = listOf(
        Regex("(?i)^\\s*(?:page|pg\\.?)\\s*\\d+(?:\\s+of\\s+\\d+)?\\s*$", RegexOption.MULTILINE),
        Regex("(?i)^\\s*\\[?\\s*page\\s*\\d+\\s*\\]?\\s*$", RegexOption.MULTILINE),
        Regex("(?m)^\\s*\\d+\\s*\\|\\s*.*$", RegexOption.MULTILINE),
        Regex("(?m)^\\s*.*\\s*\\|\\s*\\d+\\s*$", RegexOption.MULTILINE)
    )

    private val LEGAL_COPYRIGHT_PATTERNS = listOf(
        Regex("(?i)all rights reserved(?:\\.|=|\\b)", RegexOption.MULTILINE),
        Regex("(?i)library of congress cataloging-in-publication data", RegexOption.MULTILINE),
        Regex("(?i)isbn(?:-13|-10)?:?\\s*[0-9\\-]{10,17}", RegexOption.MULTILINE),
        Regex("(?i)printed in (?:the )?(?:united states|uk|canada|china|germany)", RegexOption.MULTILINE),
        Regex("(?i)no part of this (?:book|publication|text) may be reproduced", RegexOption.MULTILINE)
    )

    private val CITATION_PATTERNS = listOf(
        Regex("\\[\\d+(?:\\s*,\\s*\\d+)*\\]"),
        Regex("\\((?:[A-Z][a-z]+\\s+et\\s+al\\.|[A-Z][a-z]+\\s+and\\s+[A-Z][a-z]+),\\s*\\d{4}\\)")
    )

    private val INDEX_FILLER_PATTERNS = listOf(
        Regex("(?m)^.*\\.{4,}\\s*\\d+\\s*$"),
        Regex("(?m)^\\s*(?:table of contents|index|bibliography|notes|further reading)\\s*$", RegexOption.MULTILINE)
    )

    fun filter(text: String, enableSkipping: Boolean = true): String {
        if (!enableSkipping || text.isBlank()) return text

        var result = text

        HEADER_FOOTER_PATTERNS.forEach { pattern ->
            result = result.replace(pattern, "")
        }

        LEGAL_COPYRIGHT_PATTERNS.forEach { pattern ->
            result = result.replace(pattern, "")
        }

        CITATION_PATTERNS.forEach { pattern ->
            result = result.replace(pattern, "")
        }

        INDEX_FILLER_PATTERNS.forEach { pattern ->
            result = result.replace(pattern, "")
        }

        return result.replace(Regex("\n{3,}"), "\n\n").trim()
    }
}
