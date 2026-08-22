package com.auralis.audio

import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.max
import kotlin.math.min

internal object PcmWavWriter {
    fun writeMono16(file: File, samples: FloatArray, sampleRate: Int) {
        file.parentFile?.mkdirs()
        val dataSize = samples.size * BYTES_PER_SAMPLE
        val byteRate = sampleRate * BYTES_PER_SAMPLE
        val blockAlign = BYTES_PER_SAMPLE
        file.outputStream().use { output ->
            output.write("RIFF".toByteArray(Charsets.US_ASCII))
            output.writeIntLe(36 + dataSize)
            output.write("WAVE".toByteArray(Charsets.US_ASCII))
            output.write("fmt ".toByteArray(Charsets.US_ASCII))
            output.writeIntLe(16)
            output.writeShortLe(1)
            output.writeShortLe(1)
            output.writeIntLe(sampleRate)
            output.writeIntLe(byteRate)
            output.writeShortLe(blockAlign)
            output.writeShortLe(16)
            output.write("data".toByteArray(Charsets.US_ASCII))
            output.writeIntLe(dataSize)
            samples.forEach { sample ->
                val clamped = max(-1f, min(1f, sample))
                output.writeShortLe((clamped * Short.MAX_VALUE).toInt())
            }
        }
    }

    private fun java.io.OutputStream.writeIntLe(value: Int) {
        write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array())
    }

    private fun java.io.OutputStream.writeShortLe(value: Int) {
        write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(value.toShort()).array())
    }

    private const val BYTES_PER_SAMPLE = 2
}
