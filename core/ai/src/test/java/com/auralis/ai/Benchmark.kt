package com.auralis.ai

import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

fun main() = runBlocking {
    val analyzer = HeuristicBookAnalyzer()

    // Generate a long text
    val words = listOf("kingdom", "sword", "detective", "shadow", "warm", "suddenly", "planet", "empire", "heart", "John Doe", "The")
    val textSample = (1..5000).joinToString(" ") { words.random() }
    val input = BookAnalysisInput(
        title = "Test Book",
        chapterTitles = emptyList(),
        textSample = textSample
    )

    // Warmup
    for (i in 1..5) {
        analyzer.analyze(input)
    }

    // Benchmark
    val times = mutableListOf<Long>()
    for (i in 1..20) {
        times.add(measureTimeMillis {
            analyzer.analyze(input)
        })
    }
    println("Average time: ${times.average()} ms")
}
