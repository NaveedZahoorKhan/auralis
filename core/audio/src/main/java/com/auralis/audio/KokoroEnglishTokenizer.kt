package com.auralis.audio

import java.util.Locale

internal class KokoroEnglishTokenizer {
    fun tokenize(text: String): LongArray {
        val cleanText = TtsTextSanitizer.sanitize(text)
        val phonemes = phonemize(cleanText)
        val ids = mutableListOf<Long>()
        ids += PAD
        phonemes.forEachCodePoint { token ->
            VOCAB[token]?.let { ids += it }
            if (ids.size >= MAX_CONTEXT - 1) return@forEachCodePoint
        }
        ids += PAD
        return ids.toLongArray()
    }

    fun splitForModel(text: String): List<String> {
        val cleanText = TtsTextSanitizer.sanitize(text)
        val sentences = cleanText
            .replace(Regex("\\s+"), " ")
            .trim()
            .split(Regex("(?<=[.!?])\\s+"))
            .flatMap(::splitLongSentence)
            .filter { it.isNotBlank() }

        val chunks = mutableListOf<String>()
        var current = StringBuilder()
        sentences.forEach { sentence ->
            val next = if (current.isEmpty()) sentence else "${current} $sentence"
            if (next.length > TARGET_CHARS && current.isNotEmpty()) {
                chunks += current.toString()
                current = StringBuilder(sentence)
            } else {
                current = StringBuilder(next)
            }
        }
        if (current.isNotEmpty()) chunks += current.toString()
        return chunks.ifEmpty { listOf(text.take(TARGET_CHARS)) }
    }

    private fun splitLongSentence(sentence: String): List<String> {
        if (sentence.length <= TARGET_CHARS) return listOf(sentence)
        val chunks = mutableListOf<String>()
        var current = StringBuilder()
        sentence.split(Regex("(?<=[,;:])\\s+|\\s+")).forEach { part ->
            if (current.length + part.length + 1 > TARGET_CHARS && current.isNotEmpty()) {
                chunks += current.toString()
                current = StringBuilder(part)
            } else {
                if (current.isNotEmpty()) current.append(' ')
                current.append(part)
            }
        }
        if (current.isNotEmpty()) chunks += current.toString()
        return chunks
    }

    private fun phonemize(text: String): String {
        val output = StringBuilder()
        TOKEN_REGEX.findAll(text).forEach { match ->
            val token = match.value
            when {
                token.all { it.isDigit() } -> {
                    numberWords(token).forEach { appendWord(output, it) }
                }
                token.first().isLetter() -> appendWord(output, token)
                token in PUNCTUATION -> {
                    if (output.isNotEmpty() && output.last() == ' ') output.setLength(output.length - 1)
                    output.append(token)
                    output.append(' ')
                }
            }
        }
        return output.toString().trim()
    }

    private fun appendWord(output: StringBuilder, word: String) {
        val phonemes = phonemizeWord(word)
        if (phonemes.isBlank()) return
        if (output.isNotEmpty() && output.last() != ' ') output.append(' ')
        output.append(phonemes)
        output.append(' ')
    }

    private fun phonemizeWord(rawWord: String): String {
        val word = rawWord
            .lowercase(Locale.US)
            .trim('\'')
            .replace(Regex("[^a-z']"), "")
        if (word.isBlank()) return ""
        DICTIONARY[word]?.let { return it }

        val normalized = word.replace("'", "")
        val output = StringBuilder()
        var index = 0
        while (index < normalized.length) {
            val rest = normalized.substring(index)
            when {
                rest.startsWith("tion") -> {
                    output.append("\\u0283\\u0259n".decodeEscapes())
                    index += 4
                }
                rest.startsWith("sion") -> {
                    output.append("\\u0292\\u0259n".decodeEscapes())
                    index += 4
                }
                rest.startsWith("ture") -> {
                    output.append("t\\u0283\\u025a".decodeEscapes())
                    index += 4
                }
                rest.startsWith("igh") -> {
                    output.append("a\\u026a".decodeEscapes())
                    index += 3
                }
                rest.startsWith("air") -> {
                    output.append("\\u025b\\u0279".decodeEscapes())
                    index += 3
                }
                rest.startsWith("ear") -> {
                    output.append("\\u026a\\u0279".decodeEscapes())
                    index += 3
                }
                rest.startsWith("er") || rest.startsWith("ir") || rest.startsWith("ur") -> {
                    output.append("\\u025a".decodeEscapes())
                    index += 2
                }
                rest.startsWith("ar") -> {
                    output.append("\\u0251\\u0279".decodeEscapes())
                    index += 2
                }
                rest.startsWith("or") -> {
                    output.append("\\u0254\\u0279".decodeEscapes())
                    index += 2
                }
                rest.startsWith("th") -> {
                    output.append((if (normalized in VOICED_TH_WORDS) "\\u00f0" else "\\u03b8").decodeEscapes())
                    index += 2
                }
                rest.startsWith("sh") -> {
                    output.append("\\u0283".decodeEscapes())
                    index += 2
                }
                rest.startsWith("ch") || rest.startsWith("tch") -> {
                    output.append("\\u02a7".decodeEscapes())
                    index += if (rest.startsWith("tch")) 3 else 2
                }
                rest.startsWith("ph") -> {
                    output.append('f')
                    index += 2
                }
                rest.startsWith("ng") -> {
                    output.append("\\u014b".decodeEscapes())
                    index += 2
                }
                rest.startsWith("ck") -> {
                    output.append('k')
                    index += 2
                }
                rest.startsWith("qu") -> {
                    output.append("kw")
                    index += 2
                }
                rest.startsWith("wh") -> {
                    output.append('w')
                    index += 2
                }
                rest.startsWith("oo") -> {
                    output.append('u')
                    index += 2
                }
                rest.startsWith("ee") || rest.startsWith("ea") -> {
                    output.append('i')
                    index += 2
                }
                rest.startsWith("ai") || rest.startsWith("ay") -> {
                    output.append("e\\u026a".decodeEscapes())
                    index += 2
                }
                rest.startsWith("oi") || rest.startsWith("oy") -> {
                    output.append("\\u0254\\u026a".decodeEscapes())
                    index += 2
                }
                rest.startsWith("ow") || rest.startsWith("ou") -> {
                    output.append("a\\u028a".decodeEscapes())
                    index += 2
                }
                else -> {
                    output.append(soundForLetter(normalized[index], normalized, index))
                    index += 1
                }
            }
        }
        return output.toString()
    }

    private fun soundForLetter(letter: Char, word: String, index: Int): String {
        val last = index == word.lastIndex
        return when (letter) {
            'a' -> if (last) "\\u0259" else "\\u00e6"
            'b' -> "b"
            'c' -> if (word.getOrNull(index + 1) in SOFT_VOWELS) "s" else "k"
            'd' -> "d"
            'e' -> if (last && word.length > 2) "" else "\\u025b"
            'f' -> "f"
            'g' -> if (word.getOrNull(index + 1) in SOFT_VOWELS) "\\u02a4" else "\\u0261"
            'h' -> "h"
            'i' -> "\\u026a"
            'j' -> "\\u02a4"
            'k' -> "k"
            'l' -> "l"
            'm' -> "m"
            'n' -> "n"
            'o' -> "\\u0254"
            'p' -> "p"
            'q' -> "k"
            'r' -> "\\u0279"
            's' -> "s"
            't' -> "t"
            'u' -> "\\u028c"
            'v' -> "v"
            'w' -> "w"
            'x' -> "ks"
            'y' -> if (last) "i" else "j"
            'z' -> "z"
            else -> ""
        }.decodeEscapes()
    }

    private fun numberWords(raw: String): List<String> {
        val value = raw.toIntOrNull() ?: return raw.map { DIGITS[it].orEmpty() }.filter { it.isNotBlank() }
        if (value == 0) return listOf("zero")
        if (value > 9999) return raw.map { DIGITS[it].orEmpty() }.filter { it.isNotBlank() }
        val words = mutableListOf<String>()
        var remaining = value
        if (remaining >= 1000) {
            SMALL_NUMBERS[remaining / 1000]?.let { words += it }
            words += "thousand"
            remaining %= 1000
        }
        if (remaining >= 100) {
            SMALL_NUMBERS[remaining / 100]?.let { words += it }
            words += "hundred"
            remaining %= 100
        }
        if (remaining >= 20) {
            words += TENS[(remaining / 10) * 10].orEmpty()
            remaining %= 10
        }
        if (remaining > 0) {
            SMALL_NUMBERS[remaining]?.let { words += it }
        }
        return words.filter { it.isNotBlank() }
    }

    private fun String.forEachCodePoint(block: (String) -> Unit) {
        var index = 0
        while (index < length) {
            val codePoint = codePointAt(index)
            block(String(Character.toChars(codePoint)))
            index += Character.charCount(codePoint)
        }
    }

    private fun String.decodeEscapes(): String {
        return this
            .replace("\\u00e6", "\u00e6")
            .replace("\\u00f0", "\u00f0")
            .replace("\\u014b", "\u014b")
            .replace("\\u0251", "\u0251")
            .replace("\\u0254", "\u0254")
            .replace("\\u0259", "\u0259")
            .replace("\\u025a", "\u025a")
            .replace("\\u025b", "\u025b")
            .replace("\\u0261", "\u0261")
            .replace("\\u026a", "\u026a")
            .replace("\\u0279", "\u0279")
            .replace("\\u0283", "\u0283")
            .replace("\\u028a", "\u028a")
            .replace("\\u028c", "\u028c")
            .replace("\\u0292", "\u0292")
            .replace("\\u02a4", "\u02a4")
            .replace("\\u02a7", "\u02a7")
            .replace("\\u03b8", "\u03b8")
    }

    companion object {
        private const val PAD = 0L
        private const val MAX_CONTEXT = 512
        private const val TARGET_CHARS = 360
        private val TOKEN_REGEX = Regex("[A-Za-z']+|[0-9]+|[.,!?;:()\\\"\\-]")
        private val PUNCTUATION = setOf(".", ",", "!", "?", ";", ":", "(", ")", "\"", "-")
        private val SOFT_VOWELS = setOf('e', 'i', 'y')
        private val VOICED_TH_WORDS = setOf(
            "the", "this", "that", "these", "those", "there", "their", "them", "then", "than", "though", "thus"
        )
        private val DIGITS = mapOf(
            '0' to "zero",
            '1' to "one",
            '2' to "two",
            '3' to "three",
            '4' to "four",
            '5' to "five",
            '6' to "six",
            '7' to "seven",
            '8' to "eight",
            '9' to "nine"
        )
        private val SMALL_NUMBERS = mapOf(
            1 to "one",
            2 to "two",
            3 to "three",
            4 to "four",
            5 to "five",
            6 to "six",
            7 to "seven",
            8 to "eight",
            9 to "nine",
            10 to "ten",
            11 to "eleven",
            12 to "twelve",
            13 to "thirteen",
            14 to "fourteen",
            15 to "fifteen",
            16 to "sixteen",
            17 to "seventeen",
            18 to "eighteen",
            19 to "nineteen"
        )
        private val TENS = mapOf(
            20 to "twenty",
            30 to "thirty",
            40 to "forty",
            50 to "fifty",
            60 to "sixty",
            70 to "seventy",
            80 to "eighty",
            90 to "ninety"
        )
        private val DICTIONARY = mapOf(
            "a" to "\u0259",
            "an" to "\u00e6n",
            "and" to "\u00e6nd",
            "are" to "\u0251\u0279",
            "as" to "\u00e6z",
            "at" to "\u00e6t",
            "be" to "bi",
            "been" to "b\u026an",
            "but" to "b\u028ct",
            "by" to "ba\u026a",
            "can" to "k\u00e6n",
            "could" to "k\u028ad",
            "do" to "du",
            "for" to "f\u0254\u0279",
            "from" to "f\u0279\u028cm",
            "had" to "h\u00e6d",
            "has" to "h\u00e6z",
            "have" to "h\u00e6v",
            "he" to "hi",
            "her" to "h\u025a",
            "his" to "h\u026az",
            "i" to "a\u026a",
            "in" to "\u026an",
            "is" to "\u026az",
            "it" to "\u026at",
            "its" to "\u026ats",
            "me" to "mi",
            "my" to "ma\u026a",
            "not" to "n\u0254t",
            "of" to "\u028cv",
            "on" to "\u0254n",
            "or" to "\u0254\u0279",
            "our" to "a\u028a\u0279",
            "said" to "s\u025bd",
            "she" to "\u0283i",
            "should" to "\u0283\u028ad",
            "so" to "so",
            "that" to "\u00f0\u00e6t",
            "the" to "\u00f0\u0259",
            "their" to "\u00f0\u025b\u0279",
            "them" to "\u00f0\u025bm",
            "there" to "\u00f0\u025b\u0279",
            "they" to "\u00f0e\u026a",
            "this" to "\u00f0\u026as",
            "to" to "tu",
            "was" to "w\u028cz",
            "we" to "wi",
            "were" to "w\u025a",
            "what" to "w\u028ct",
            "when" to "w\u025bn",
            "who" to "hu",
            "will" to "w\u026al",
            "with" to "w\u026a\u00f0",
            "would" to "w\u028ad",
            "you" to "ju",
            "your" to "j\u0254\u0279"
        )
        private val VOCAB = mapOf(
            ";" to 1L,
            ":" to 2L,
            "," to 3L,
            "." to 4L,
            "!" to 5L,
            "?" to 6L,
            "\"" to 11L,
            "(" to 12L,
            ")" to 13L,
            " " to 16L,
            "A" to 24L,
            "I" to 25L,
            "O" to 31L,
            "S" to 35L,
            "T" to 36L,
            "W" to 39L,
            "Y" to 41L,
            "a" to 43L,
            "b" to 44L,
            "c" to 45L,
            "d" to 46L,
            "e" to 47L,
            "f" to 48L,
            "h" to 50L,
            "i" to 51L,
            "j" to 52L,
            "k" to 53L,
            "l" to 54L,
            "m" to 55L,
            "n" to 56L,
            "o" to 57L,
            "p" to 58L,
            "q" to 59L,
            "r" to 60L,
            "s" to 61L,
            "t" to 62L,
            "u" to 63L,
            "v" to 64L,
            "w" to 65L,
            "x" to 66L,
            "y" to 67L,
            "z" to 68L,
            "\u0251" to 69L,
            "\u00e6" to 72L,
            "\u0254" to 76L,
            "\u00f0" to 81L,
            "\u0259" to 83L,
            "\u025a" to 85L,
            "\u025b" to 86L,
            "\u0261" to 92L,
            "\u026a" to 102L,
            "\u014b" to 112L,
            "\u03b8" to 119L,
            "\u0279" to 123L,
            "\u0283" to 131L,
            "\u02a7" to 133L,
            "\u028a" to 135L,
            "\u028c" to 138L,
            "\u0292" to 147L,
            "\u02a4" to 82L
        )
    }
}
