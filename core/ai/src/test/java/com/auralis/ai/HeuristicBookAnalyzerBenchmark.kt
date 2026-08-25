package com.auralis.ai

import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.system.measureTimeMillis

class HeuristicBookAnalyzerBenchmark {
    @Test
    fun benchmarkAnalysis() = runBlocking {
        val analyzer = HeuristicBookAnalyzer()
        val text = "The shadow fell across the room in fear. There was blood. The cold death was alone. " +
                "He ran suddenly and shouted in hurry to escape the danger. " +
                "A warm smile from a friend brought hope and laugh in the home. " +
                "John Smith is a detective. He investigated the murder clue in the case. "
        // repeat to make it 80,000 chars roughly
        val longText = text.repeat(350)

        // Warmup
        for(i in 1..20) {
            analyzer.analyze(
                BookAnalysisInput(
                    title = "Test",
                    chapterTitles = listOf("Chapter 1"),
                    textSample = longText
                )
            )
        }

        // Measure
        var totalTime = 0L
        val iterations = 100
        for (i in 1..iterations) {
            val time = measureTimeMillis {
                analyzer.analyze(
                    BookAnalysisInput(
                        title = "Test",
                        chapterTitles = listOf("Chapter 1"),
                        textSample = longText
                    )
                )
            }
            totalTime += time
        }

        println("BENCHMARK_RESULT_TOTAL: $totalTime ms")
        println("BENCHMARK_RESULT_AVG: ${totalTime / iterations.toDouble()} ms")
    }
}
