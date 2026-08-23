package com.auralis.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.FormatQuote
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.auralis.ai.DeepstashInsightCard
import com.auralis.ai.DeepstashSummaryResult
import com.auralis.ai.InsightType
import com.auralis.database.VoiceModelEntity

data class VoiceModelSpec(
    val id: String,
    val name: String,
    val engine: String,
    val ramRequirement: String,
    val cpuCompatibility: String,
    val description: String,
    val sizeText: String
)

val VOICE_MODEL_SPECS = listOf(
    VoiceModelSpec(
        id = "kokoro-natural-en",
        name = "Kokoro Natural Neural (82M)",
        engine = "ONNX Neural SLM",
        ramRequirement = "1.5 GB RAM",
        cpuCompatibility = "High Perf (Snapdragon 8 / Tensor / Dimensity 9000+)",
        description = "Studio-quality human prosody, emotional pitch contours, and sentence cadence.",
        sizeText = "~86 MB"
    ),
    VoiceModelSpec(
        id = "piper-fast-en",
        name = "Piper Neural English (Fast)",
        engine = "ONNX Light SLM",
        ramRequirement = "512 MB RAM",
        cpuCompatibility = "Universal (All ARM64 & Budget Processors)",
        description = "Ultra-fast responsive narration with low battery draw. Ideal for legacy devices.",
        sizeText = "~28 MB"
    ),
    VoiceModelSpec(
        id = "system-native-tts",
        name = "Android System Native TTS",
        engine = "OS Native Hardware",
        ramRequirement = "0 MB Overhead",
        cpuCompatibility = "100% Device Compatible",
        description = "Built-in hardware synthesized speech. Uses zero additional disk storage.",
        sizeText = "Built-in"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceModelsDialog(
    voices: List<VoiceModelEntity>,
    selectedVoiceId: String,
    onSelectVoice: (String) -> Unit,
    onDownloadVoice: (String) -> Unit,
    enableSmartSkipping: Boolean,
    onToggleSmartSkipping: (Boolean) -> Unit,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Button(onClick = onDismissRequest) {
                Text("Done")
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.Mic,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text("Voice Models & Settings", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Box(Modifier.height(440.dp)) {
                VoiceModelsSettingsPane(
                    voices = voices,
                    selectedVoiceId = selectedVoiceId,
                    onSelectVoice = onSelectVoice,
                    onDownloadVoice = onDownloadVoice,
                    enableSmartSkipping = enableSmartSkipping,
                    onToggleSmartSkipping = onToggleSmartSkipping
                )
            }
        }
    )
}

@Composable
fun VoiceModelsSettingsPane(
    voices: List<VoiceModelEntity>,
    selectedVoiceId: String,
    onSelectVoice: (String) -> Unit,
    onDownloadVoice: (String) -> Unit,
    enableSmartSkipping: Boolean,
    onToggleSmartSkipping: (Boolean) -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Smart Content Filter SLM Toggle Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            )
        ) {
            Row(
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.FilterList,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "Skip Unnecessary Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "Smart SLM filter automatically skips page numbers, legal boilerplate, citations, and index filler during narration.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.width(8.dp))
                Switch(
                    checked = enableSmartSkipping,
                    onCheckedChange = onToggleSmartSkipping
                )
            }
        }

        // Section Title: Available Voice Models
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Rounded.Mic,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Voice Models & Compatibility Guidelines",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Models Catalog List
        VOICE_MODEL_SPECS.forEach { spec ->
            val voiceEntity = voices.find { it.id == spec.id }
            val isSelected = selectedVoiceId == spec.id
            val isInstalled = voiceEntity?.status == "installed" || spec.id == "system-native-tts"

            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectVoice(spec.id) },
                shape = RoundedCornerShape(16.dp),
                border = if (isSelected) {
                    androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                } else {
                    androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                },
                colors = CardDefaults.outlinedCardColors(
                    containerColor = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                )
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isSelected) Icons.Rounded.RadioButtonChecked else Icons.Rounded.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    spec.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    spec.engine,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        AssistChip(
                            onClick = { },
                            label = { Text(spec.sizeText, style = MaterialTheme.typography.labelSmall) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        )
                    }

                    Text(
                        spec.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Hardware & Compatibility Guidelines Badges
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CompatibilityBadge(
                            icon = Icons.Rounded.Memory,
                            label = spec.ramRequirement,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        CompatibilityBadge(
                            icon = Icons.Rounded.Speed,
                            label = spec.cpuCompatibility,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }

                    // Action Button (Download / Active)
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isSelected) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "Active Voice Engine",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else if (!isInstalled) {
                            Button(
                                onClick = { onDownloadVoice(spec.id) },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Download Model")
                            }
                        } else {
                            Button(
                                onClick = { onSelectVoice(spec.id) },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                            ) {
                                Text("Use This Voice", color = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompatibilityBadge(icon: ImageVector, label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Row(
            Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = color)
        }
    }
}

@Composable
fun DeepstashPane(
    summary: DeepstashSummaryResult?,
    isLoading: Boolean = false,
    onOpenSlmSettings: () -> Unit = {},
    onRegenerate: () -> Unit = {}
) {
    if (isLoading || summary == null) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        "Rescanning & Distilling Book...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Clearing previous insights and running selected SLM engine across book chapters.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        return
    }

    var selectedFilter by remember { mutableStateOf<InsightType?>(null) }

    val filteredCards = if (selectedFilter == null) {
        summary.cards
    } else {
        summary.cards.filter { it.type == selectedFilter }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Deepstash Executive Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
            )
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.AutoStories,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                "Deepstash Visual Summary",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Text(
                                "${summary.keyTakeawaysCount} Takeaways • Scanned ${summary.scannedChaptersCount} Chapters",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (summary.isOnnxActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { onOpenSlmSettings() }
                        ) {
                            Row(
                                Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (summary.isOnnxActive) Icons.Rounded.AutoAwesome else Icons.Rounded.Memory,
                                    contentDescription = null,
                                    tint = if (summary.isOnnxActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    if (summary.isOnnxActive) "ONNX Active" else "Engine: ${summary.slmModelUsed}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (summary.isOnnxActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = onRegenerate,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Refresh,
                                contentDescription = "Regenerate Deepstash Summary",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                Text(
                    summary.executiveSummary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }

        // Category Chips
        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedFilter == null,
                onClick = { selectedFilter = null },
                label = { Text("All (${summary.cards.size})") }
            )
            InsightType.values().forEach { type ->
                val count = summary.cards.count { it.type == type }
                if (count > 0) {
                    FilterChip(
                        selected = selectedFilter == type,
                        onClick = { selectedFilter = if (selectedFilter == type) null else type },
                        label = { Text("${type.label} ($count)") }
                    )
                }
            }
        }

        // Insight Cards List
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            filteredCards.forEach { card ->
                DeepstashInsightCardItem(card)
            }

            Spacer(Modifier.height(4.dp))

            // Bottom Full-Width Regenerate Action Button
            Button(
                onClick = onRegenerate,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Icon(
                    Icons.Rounded.Refresh,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Rescan & Regenerate Deepstash Summary",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}

@Composable
private fun DeepstashInsightCardItem(card: DeepstashInsightCard) {
    val (typeColor, icon) = when (card.type) {
        InsightType.KEY_IDEA -> Pair(MaterialTheme.colorScheme.primary, Icons.Rounded.Lightbulb)
        InsightType.ACTIONABLE_TAKEAWAY -> Pair(MaterialTheme.colorScheme.secondary, Icons.Rounded.Psychology)
        InsightType.CORE_CONCEPT -> Pair(MaterialTheme.colorScheme.tertiary, Icons.Rounded.Star)
        InsightType.QUOTE -> Pair(MaterialTheme.colorScheme.error, Icons.Rounded.FormatQuote)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(typeColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = typeColor, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        card.type.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = typeColor
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Text(
                        "${card.readTimeSeconds}s read",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                card.content,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 24.sp,
                fontWeight = if (card.type == InsightType.QUOTE) FontWeight.Normal else FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            card.chapterTitle?.let { chapterName ->
                Text(
                    "From $chapterName",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlmModelSettingsDialog(
    selectedSlmId: String,
    onSelectSlm: (String) -> Unit,
    enableWholeBookScan: Boolean,
    onToggleWholeBookScan: (Boolean) -> Unit,
    enableSmartSkipping: Boolean,
    onToggleSmartSkipping: (Boolean) -> Unit,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Button(onClick = onDismissRequest) {
                Text("Done")
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.Psychology,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text("Book Intelligence & SLM Settings", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Box(Modifier.height(480.dp)) {
                SlmModelsSettingsPane(
                    selectedSlmId = selectedSlmId,
                    onSelectSlm = onSelectSlm,
                    enableWholeBookScan = enableWholeBookScan,
                    onToggleWholeBookScan = onToggleWholeBookScan,
                    enableSmartSkipping = enableSmartSkipping,
                    onToggleSmartSkipping = onToggleSmartSkipping
                )
            }
        }
    )
}

@Composable
fun SlmModelsSettingsPane(
    selectedSlmId: String,
    onSelectSlm: (String) -> Unit,
    enableWholeBookScan: Boolean,
    onToggleWholeBookScan: (Boolean) -> Unit,
    enableSmartSkipping: Boolean,
    onToggleSmartSkipping: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val llmRuntime = remember { com.auralis.ai.OnDeviceLlmRuntime() }
    val downloader = remember { com.auralis.ai.SlmModelDownloader() }
    val scope = rememberCoroutineScope()

    var downloadingState by remember { mutableStateOf<Map<String, com.auralis.ai.DownloadProgress>>(emptyMap()) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Whole Book Scanning Toggle Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
            )
        ) {
            Row(
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.AutoStories,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "Full Book Intelligence Scan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "Scans all chapters across the entire book to generate comprehensive Deepstash takeaways and key concept summaries.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.width(8.dp))
                Switch(
                    checked = enableWholeBookScan,
                    onCheckedChange = onToggleWholeBookScan
                )
            }
        }

        // Active Model Runtime Notice Card
        val selectedSpec = com.auralis.ai.OnDeviceLlmRuntime.AVAILABLE_SLM_MODELS.find { it.id == selectedSlmId }
            ?: com.auralis.ai.OnDeviceLlmRuntime.AVAILABLE_SLM_MODELS.first()
        var selectedStatus by remember(selectedSlmId) { mutableStateOf(llmRuntime.checkModelStatus(context.filesDir, selectedSlmId)) }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (selectedStatus == com.auralis.ai.SlmModelStatus.INSTALLED) 
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) 
                else 
                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
            )
        ) {
            Row(
                Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (selectedStatus == com.auralis.ai.SlmModelStatus.INSTALLED) Icons.Rounded.CheckCircle else Icons.Rounded.ErrorOutline,
                    contentDescription = null,
                    tint = if (selectedStatus == com.auralis.ai.SlmModelStatus.INSTALLED) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        if (selectedStatus == com.auralis.ai.SlmModelStatus.INSTALLED)
                            "ONNX Session Active: ${selectedSpec.name}"
                        else
                            "Built-in SLM Engine Fallback Active",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (selectedStatus == com.auralis.ai.SlmModelStatus.INSTALLED)
                            "Full-book scanning is powered by local ONNX neural execution."
                        else
                            "${selectedSpec.name} (${selectedSpec.sizeText}) model file is not installed on disk. Deepstash is using the lightweight built-in distillation engine.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Section Title: Available SLM Models
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Rounded.Memory,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "On-Device SLM Model Catalog",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        com.auralis.ai.OnDeviceLlmRuntime.AVAILABLE_SLM_MODELS.forEach { spec ->
            val isSelected = selectedSlmId == spec.id
            val status = llmRuntime.checkModelStatus(context.filesDir, spec.id)
            val statusLabel = when (status) {
                com.auralis.ai.SlmModelStatus.INSTALLED -> "ONNX Installed & Active"
                com.auralis.ai.SlmModelStatus.DOWNLOAD_AVAILABLE -> "Downloaded ONNX File"
                com.auralis.ai.SlmModelStatus.BUILT_IN_FALLBACK -> "Built-in Engine Fallback Active"
            }

            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectSlm(spec.id) },
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                ),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                )
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onSelectSlm(spec.id) }
                            )
                            Spacer(Modifier.width(6.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        spec.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (spec.isRecommended) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.tertiaryContainer
                                        ) {
                                            Text(
                                                "Top Pick",
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onTertiaryContainer
                                            )
                                        }
                                    }
                                }
                                Text(
                                    "${spec.parameterCount} Params • ${spec.sizeText} • ${spec.recommendedRam}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Text(
                        spec.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val dlProgress = downloadingState[spec.id]

                    val errText = dlProgress?.error
                    if (errText != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Download Failed",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                                Text(
                                    errText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(onClick = { downloadingState = downloadingState - spec.id }) {
                                        Text("Dismiss", color = MaterialTheme.colorScheme.onErrorContainer)
                                    }
                                    Spacer(Modifier.width(4.dp))
                                    Button(
                                        onClick = {
                                            downloadingState = downloadingState - spec.id
                                            scope.launch {
                                                downloader.downloadModel(context.filesDir, spec).collect { progress ->
                                                    downloadingState = downloadingState + (spec.id to progress)
                                                    if (progress.isCompleted) {
                                                        onSelectSlm(spec.id)
                                                        selectedStatus = llmRuntime.checkModelStatus(context.filesDir, spec.id)
                                                    }
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Retry", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    if (dlProgress?.isDownloading == true) {
                        Column(
                            Modifier.fillMaxWidth().padding(top = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Downloading ${spec.name}...",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "${"%.1f".format(dlProgress.downloadedMB)} MB / ${"%.1f".format(dlProgress.totalMB)} MB (${(dlProgress.progress * 100).toInt()}%)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            LinearProgressIndicator(
                                progress = { dlProgress.progress },
                                modifier = Modifier.fillMaxWidth().height(6.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        }
                    }

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when (status) {
                                com.auralis.ai.SlmModelStatus.INSTALLED -> MaterialTheme.colorScheme.primaryContainer
                                com.auralis.ai.SlmModelStatus.DOWNLOAD_AVAILABLE -> MaterialTheme.colorScheme.secondaryContainer
                                com.auralis.ai.SlmModelStatus.BUILT_IN_FALLBACK -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (status == com.auralis.ai.SlmModelStatus.INSTALLED) Icons.Rounded.CheckCircle else Icons.Rounded.Memory,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    statusLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (status == com.auralis.ai.SlmModelStatus.INSTALLED) {
                                IconButton(
                                    onClick = {
                                        llmRuntime.deleteModelFile(context.filesDir, spec.id)
                                        selectedStatus = llmRuntime.checkModelStatus(context.filesDir, spec.id)
                                    },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.Delete,
                                        contentDescription = "Uninstall Model File",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            if (status != com.auralis.ai.SlmModelStatus.INSTALLED && dlProgress?.isDownloading != true) {
                                Button(
                                    onClick = {
                                        downloadingState = downloadingState - spec.id
                                        scope.launch {
                                            downloader.downloadModel(context.filesDir, spec).collect { progress ->
                                                downloadingState = downloadingState + (spec.id to progress)
                                                if (progress.isCompleted) {
                                                    onSelectSlm(spec.id)
                                                    selectedStatus = llmRuntime.checkModelStatus(context.filesDir, spec.id)
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier.height(30.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Install (${spec.sizeText})", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
