package com.auralis.ai

import ai.onnxruntime.OrtEnvironment
import java.io.File

class OnDeviceLlmRuntime {
    fun validateModel(modelFile: File): Boolean {
        if (!modelFile.exists() || modelFile.length() == 0L) return false
        return runCatching {
            OrtEnvironment.getEnvironment().use { environment ->
                environment.createSession(modelFile.absolutePath).use { session ->
                    session.inputNames.isNotEmpty() && session.outputNames.isNotEmpty()
                }
            }
        }.getOrDefault(false)
    }
}
