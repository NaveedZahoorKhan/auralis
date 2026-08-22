package com.auralis.audio

import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.max
import kotlin.math.min

object PcmWavWriter {
    fun writeMono16(file: File, samples: FloatArray, sampleRate: Int) {
        file.parentFile?.mkdirs()
        val dataSize = samples.size * BYTES_PER_SAMPLE
        val byteRate = sampleRate * BYTES_PER_SAMPLE
        val blockAlign = BYTES_PER_SAMPLE

        BufferedOutputStream(FileOutputStream(file), 64 * 1024).use { output ->
            val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
            header.put("RIFF".toByteArray(Charsets.US_ASCII))
            header.putInt(36 + dataSize)
            header.put("WAVE".toByteArray(Charsets.US_ASCII))
            header.put("fmt ".toByteArray(Charsets.US_ASCII))
            header.putInt(16) // Subchunk1Size
            header.putShort(1.toShort()) // AudioFormat = PCM
            header.putShort(1.toShort()) // NumChannels = 1 (Mono)
            header.putInt(sampleRate)
            header.putInt(byteRate)
            header.putShort(blockAlign.toShort())
            header.putShort(16.toShort()) // BitsPerSample
            header.put("data".toByteArray(Charsets.US_ASCII))
            header.putInt(dataSize)
            output.write(header.array())

            val chunkSize = 4096
            val byteBuffer = ByteBuffer.allocate(chunkSize * BYTES_PER_SAMPLE).order(ByteOrder.LITTLE_ENDIAN)
            var index = 0
            while (index < samples.size) {
                val end = min(index + chunkSize, samples.size)
                byteBuffer.clear()
                for (i in index until end) {
                    val clamped = max(-1f, min(1f, samples[i]))
                    byteBuffer.putShort((clamped * 32767f).toInt().toShort())
                }
                output.write(byteBuffer.array(), 0, (end - index) * BYTES_PER_SAMPLE)
                index = end
            }
            output.flush()
        }
    }

    private const val BYTES_PER_SAMPLE = 2
}
