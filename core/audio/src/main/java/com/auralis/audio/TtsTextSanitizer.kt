package com.auralis.audio

object TtsTextSanitizer {
    private val ABBREVIATIONS = listOf(
        Regex("(?i)\\bMr\\.") to "Mister",
        Regex("(?i)\\bMrs\\.") to "Missus",
        Regex("(?i)\\bMs\\.") to "Miss",
        Regex("(?i)\\bDr\\.") to "Doctor",
        Regex("(?i)\\bProf\\.") to "Professor",
        Regex("(?i)\\bSt\\.(?=\\s+[A-Z])") to "Saint",
        Regex("(?i)\\bSt\\.(?=\\s+[a-z])") to "Street",
        Regex("(?i)\\bvs\\.") to "versus",
        Regex("(?i)\\betc\\.") to "et cetera",
        Regex("(?i)\\be\\.g\\.") to "for example",
        Regex("(?i)\\bi\\.e\\.") to "that is",
        Regex("(?i)\\bNo\\.(?=\\s*\\d)") to "Number",
        Regex("(?i)\\bVol\\.(?=\\s*\\d)") to "Volume",
        Regex("(?i)\\bFig\\.(?=\\s*\\d)") to "Figure",
        Regex("(?i)\\bSec\\.(?=\\s*\\d)") to "Section",
        Regex("(?i)\\bCh\\.(?=\\s*\\d)") to "Chapter",
        Regex("(?i)\\bVer\\.(?=\\s*\\d)") to "Version",
        Regex("(?i)\\bp\\.m\\.") to "pm",
        Regex("(?i)\\ba\\.m\\.") to "am",
        Regex("(?i)\\bp\\.(?=\\s*\\d)") to "page",
        Regex("(?i)\\bpp\\.(?=\\s*\\d)") to "pages",
        Regex("(?i)\\bCo\\.") to "Company",
        Regex("(?i)\\bInc\\.") to "Incorporated",
        Regex("(?i)\\bLtd\\.") to "Limited",
        Regex("(?i)\\bDept\\.") to "Department",
        Regex("(?i)\\bCapt\\.") to "Captain",
        Regex("(?i)\\bGen\\.") to "General",
        Regex("(?i)\\bCol\\.") to "Colonel",
        Regex("(?i)\\bLt\\.") to "Lieutenant",
        Regex("(?i)\\bSgt\\.") to "Sergeant",
        Regex("(?i)\\bRev\\.") to "Reverend",
        Regex("(?i)\\bHon\\.") to "Honorable",
        Regex("(?i)\\bJr\\.") to "Junior",
        Regex("(?i)\\bSr\\.") to "Senior",
        Regex("(?i)\\bapprox\\.") to "approximately",
        Regex("(?i)\\best\\.") to "estimated"
    )

    fun sanitize(text: String, enableSmartFiltering: Boolean = false): String {
        if (text.isBlank()) return text

        var result = if (enableSmartFiltering) SmartContentFilter.filter(text) else text

        // 1. Strip special bullet symbols or formatting characters that TTS reads aloud
        result = result.replace(Regex("[•*°▪▫~#@\\^]|--(?!-)"), " ")

        // 2. Expand common abbreviations so TTS does not read trailing dots as "dot"
        ABBREVIATIONS.forEach { (pattern, replacement) ->
            result = result.replace(pattern, replacement)
        }

        // 3. Remove dots after single capital letters (e.g. J. K. Rowling -> J K Rowling, A. Smith -> A Smith)
        result = result.replace(Regex("\\b([A-Z])\\.\\s*"), "$1 ")

        // 4. Convert numbered section/chapter headings (e.g. "1. Introduction" -> "1, Introduction")
        result = result.replace(Regex("(?m)^\\s*(\\d+)\\.\\s+"), "$1, ")

        // 5. Convert decimal numbers (e.g. 3.14 -> 3 point 14)
        result = result.replace(Regex("(\\d+)\\.(\\d+)"), "$1 point $2")

        // 6. Convert ellipses (... or .. or …) to a soft pause comma
        result = result.replace(Regex("\\.{2,}|…"), ", ")

        // 7. Remove spaces preceding periods (e.g. "word ." -> "word.")
        result = result.replace(Regex("\\s+\\."), ".")

        // 8. Remove orphan/isolated dots surrounded by spaces or start/end of string
        result = result.replace(Regex("(?<=^|\\s)\\.+(?=\\s|$)"), "")

        // 9. Normalize whitespace
        result = result.replace(Regex("\\s+"), " ").trim()

        return result
    }
}

