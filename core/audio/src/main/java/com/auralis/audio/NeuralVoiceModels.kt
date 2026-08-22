package com.auralis.audio

import com.auralis.database.VoiceModelEntity

data class NarrationSegmentRequest(
    val id: String,
    val chapterId: String,
    val sortIndex: Int,
    val text: String,
    val textStartOffset: Int,
    val textEndOffset: Int
)

data class RenderedAudioSegment(
    val filePath: String,
    val durationMillis: Long,
    val checksum: String
)

sealed class VoiceRuntimeFailure(message: String) : RuntimeException(message) {
    class MissingVoiceModel : VoiceRuntimeFailure("A natural neural voice model must be installed before audiobook generation.")
    class UnsupportedVoicePack : VoiceRuntimeFailure("The selected ONNX voice pack could not be opened by the local runtime.")
    class SynthesisFailed(message: String) : VoiceRuntimeFailure(message)
}

interface LocalNeuralTtsEngine {
    suspend fun render(
        request: NarrationSegmentRequest,
        voiceModel: VoiceModelEntity,
        outputDirectory: String
    ): RenderedAudioSegment
}
