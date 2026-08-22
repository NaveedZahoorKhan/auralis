# Project Context
## Project Structure
.
├── .gitignore
├── README.md
├── app
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src
│       └── main
│           ├── AndroidManifest.xml
│           ├── java
│           │   └── com
│           │       └── auralis
│           │           └── reader
│           │               ├── AuralisRepository.kt
│           │               └── MainActivity.kt
│           └── res
│               ├── drawable
│               │   ├── asset_audio_generation.xml
│               │   ├── asset_empty_library.xml
│               │   ├── asset_reader_cover.xml
│               │   ├── asset_voice_ready.xml
│               │   ├── ic_launcher_foreground.xml
│               │   ├── ic_launcher_monochrome.xml
│               │   └── ic_notification.xml
│               ├── mipmap-anydpi-v26
│               │   ├── ic_launcher.xml
│               │   └── ic_launcher_round.xml
│               └── values
│                   ├── colors.xml
│                   ├── strings.xml
│                   └── styles.xml
├── build.gradle.kts
├── build_context.sh
├── core
│   ├── ai
│   │   ├── build.gradle.kts
│   │   └── src
│   │       └── main
│   │           └── java
│   │               └── com
│   │                   └── auralis
│   │                       └── ai
│   │                           ├── BookAnalysis.kt
│   │                           ├── HeuristicBookAnalyzer.kt
│   │                           └── OnDeviceLlmRuntime.kt
│   ├── audio
│   │   ├── build.gradle.kts
│   │   └── src
│   │       ├── main
│   │       │   └── java
│   │       │       └── com
│   │       │           └── auralis
│   │       │               └── audio
│   │       │                   ├── KokoroEnglishTokenizer.kt
│   │       │                   ├── NarrationPlanner.kt
│   │       │                   ├── NeuralVoiceModels.kt
│   │       │                   ├── OnnxNaturalTtsEngine.kt
│   │       │                   ├── PcmWavWriter.kt
│   │       │                   └── VoiceModelRepository.kt
│   │       └── test
│   │           └── java
│   │               └── com
│   │                   └── auralis
│   │                       └── audio
│   │                           ├── KokoroEnglishTokenizerTest.kt
│   │                           ├── NarrationPlannerTest.kt
│   │                           └── PcmWavWriterTest.kt
│   ├── database
│   │   ├── build.gradle.kts
│   │   └── src
│   │       └── main
│   │           └── java
│   │               └── com
│   │                   └── auralis
│   │                       └── database
│   │                           ├── AuralisDao.kt
│   │                           ├── AuralisDatabase.kt
│   │                           └── AuralisEntities.kt
│   ├── jobs
│   │   ├── build.gradle.kts
│   │   └── src
│   │       └── main
│   │           └── java
│   │               └── com
│   │                   └── auralis
│   │                       └── jobs
│   │                           ├── AudiobookGenerationWorker.kt
│   │                           └── AudiobookJobScheduler.kt
│   └── reader
│       ├── build.gradle.kts
│       └── src
│           └── main
│               └── java
│                   └── com
│                       └── auralis
│                           └── reader
│                               └── core
│                                   ├── BookImportModels.kt
│                                   └── BookImporter.kt
├── generate_context.sh
├── gradle
│   ├── libs.versions.toml
│   └── wrapper
│       └── gradle-wrapper.properties
├── gradle.properties
├── gradlew
├── scripts
│   ├── android-tools.sh
│   ├── install-debug.sh
│   ├── java-env.sh
│   ├── logcat.sh
│   └── run-avd.sh
└── settings.gradle.kts

57 directories, 56 files

## Files

### ./.gitignore

```
.gradle/
.idea/
*.iml
build/
local.properties
captures/
externalNativeBuild/
.cxx/
app/build/
core/*/build/
```

### ./build.gradle.kts

```
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
```

### ./app/build.gradle.kts

```
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

val debugAbiFilters = (findProperty("auralis.abiFilters") as String?)
    ?.split(",")
    ?.map { it.trim() }
    ?.filter { it.isNotEmpty() }
    ?: listOf("x86_64")

android {
    namespace = "com.auralis.reader"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.auralis.reader"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
            abiFilters += debugAbiFilters
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core:database"))
    implementation(project(":core:reader"))
    implementation(project(":core:ai"))
    implementation(project(":core:audio"))
    implementation(project(":core:jobs"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    testImplementation(libs.junit)
}
```

### ./app/proguard-rules.pro

```
# Keep ONNX Runtime symbols for downloaded voice packs.
-keep class ai.onnxruntime.** { *; }
```

### ./app/src/main/AndroidManifest.xml

```
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:theme="@style/Theme.Auralis">
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

### ./app/src/main/res/values/strings.xml

```
<resources>
    <string name="app_name">Auralis</string>
</resources>
```

### ./app/src/main/res/values/colors.xml

```
<resources>
    <color name="auralis_seed">#2F6F68</color>
    <color name="auralis_ink">#10201D</color>
    <color name="auralis_teal">#2F6F68</color>
    <color name="auralis_mint">#D6E7E1</color>
    <color name="auralis_parchment">#F0DFBE</color>
    <color name="auralis_rust">#9A4F3E</color>
    <color name="launcher_background">#D6E7E1</color>
</resources>
```

### ./app/src/main/res/values/styles.xml

```
<resources>
    <style name="Theme.Auralis" parent="android:style/Theme.Material.Light.NoActionBar">
        <item name="android:fontFamily">sans</item>
        <item name="android:windowLightStatusBar">true</item>
        <item name="android:navigationBarColor">#FAFBF8</item>
        <item name="android:statusBarColor">#FAFBF8</item>
    </style>
</resources>
```

### ./app/src/main/res/drawable/ic_launcher_monochrome.xml

```
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="#000000"
        android:pathData="M29,30C29,26.7 31.7,24 35,24H52C55.3,24 58,26.7 58,30V82C58,85.3 55.3,88 52,88H35C31.7,88 29,85.3 29,82V30ZM50,30C50,26.7 52.7,24 56,24H73C76.3,24 79,26.7 79,30V82C79,85.3 76.3,88 73,88H56C52.7,88 50,85.3 50,82V30Z" />
    <path
        android:strokeColor="#000000"
        android:strokeWidth="4"
        android:strokeLineCap="round"
        android:strokeLineJoin="round"
        android:fillColor="@android:color/transparent"
        android:pathData="M40,59C43,49 47,49 50,59C53,69 57,69 60,59C63,49 67,49 70,59" />
    <path
        android:fillColor="#000000"
        android:pathData="M61,47L61,70L76,58.5L61,47Z" />
</vector>
```

### ./app/src/main/res/drawable/asset_voice_ready.xml

```
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="96dp"
    android:height="96dp"
    android:viewportWidth="96"
    android:viewportHeight="96">
    <path
        android:fillColor="#D6E7E1"
        android:pathData="M48,88C70.1,88 88,70.1 88,48C88,25.9 70.1,8 48,8C25.9,8 8,25.9 8,48C8,70.1 25.9,88 48,88Z" />
    <path
        android:fillColor="#2F6F68"
        android:pathData="M38,23C38,18.6 41.6,15 46,15H50C54.4,15 58,18.6 58,23V49C58,53.4 54.4,57 50,57H46C41.6,57 38,53.4 38,49V23Z" />
    <path
        android:strokeColor="#10201D"
        android:strokeWidth="5"
        android:strokeLineCap="round"
        android:fillColor="@android:color/transparent"
        android:pathData="M27,44C27,57 36,67 48,67C60,67 69,57 69,44" />
    <path
        android:strokeColor="#10201D"
        android:strokeWidth="5"
        android:strokeLineCap="round"
        android:fillColor="@android:color/transparent"
        android:pathData="M48,68V79" />
    <path
        android:strokeColor="#9A4F3E"
        android:strokeWidth="4"
        android:strokeLineCap="round"
        android:fillColor="@android:color/transparent"
        android:pathData="M70,27L75,32L84,20" />
</vector>
```

### ./app/src/main/res/drawable/ic_notification.xml

```
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#FFFFFFFF"
        android:pathData="M5,5.5C5,4.1 6.1,3 7.5,3H12C13.1,3 14,3.9 14,5V19C14,20.1 13.1,21 12,21H7.5C6.1,21 5,19.9 5,18.5V5.5ZM12,5.5V18.5H8V5.5H12Z" />
    <path
        android:fillColor="#FFFFFFFF"
        android:pathData="M12,5C12,3.9 12.9,3 14,3H16.5C17.9,3 19,4.1 19,5.5V18.5C19,19.9 17.9,21 16.5,21H14C12.9,21 12,20.1 12,19V5Z" />
    <path
        android:strokeColor="#FFFFFFFF"
        android:strokeWidth="1.7"
        android:strokeLineCap="round"
        android:strokeLineJoin="round"
        android:fillColor="@android:color/transparent"
        android:pathData="M8.5,12C9.3,9.5 10.2,9.5 11,12C11.8,14.5 12.7,14.5 13.5,12C14.3,9.5 15.2,9.5 16,12" />
</vector>
```

### ./app/src/main/res/drawable/ic_launcher_foreground.xml

```
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="#10201D"
        android:fillAlpha="0.10"
        android:pathData="M54,92C73.9,92 90,75.9 90,56C90,36.1 73.9,20 54,20C34.1,20 18,36.1 18,56C18,75.9 34.1,92 54,92Z" />
    <path
        android:fillColor="#2F6F68"
        android:pathData="M29,30C29,26.7 31.7,24 35,24H52C55.3,24 58,26.7 58,30V82C58,85.3 55.3,88 52,88H35C31.7,88 29,85.3 29,82V30Z" />
    <path
        android:fillColor="#2F6F68"
        android:pathData="M50,30C50,26.7 52.7,24 56,24H73C76.3,24 79,26.7 79,30V82C79,85.3 76.3,88 73,88H56C52.7,88 50,85.3 50,82V30Z" />
    <path
        android:fillColor="#F0DFBE"
        android:pathData="M35,31C35,29.9 35.9,29 37,29H50C51.7,29 53,30.3 53,32V78C53,79.1 52.1,80 51,80H37C35.9,80 35,79.1 35,78V31Z" />
    <path
        android:fillColor="#FAFBF8"
        android:pathData="M55,32C55,30.3 56.3,29 58,29H71C72.1,29 73,29.9 73,31V78C73,79.1 72.1,80 71,80H57C55.9,80 55,79.1 55,78V32Z" />
    <path
        android:strokeColor="#10201D"
        android:strokeAlpha="0.18"
        android:strokeWidth="2"
        android:strokeLineCap="round"
        android:fillColor="@android:color/transparent"
        android:pathData="M45,35V74" />
    <path
        android:strokeColor="#10201D"
        android:strokeWidth="3"
        android:strokeLineCap="round"
        android:strokeLineJoin="round"
        android:fillColor="@android:color/transparent"
        android:pathData="M40,59C43,49 47,49 50,59C53,69 57,69 60,59C63,49 67,49 70,59" />
    <path
        android:fillColor="#9A4F3E"
        android:pathData="M61,47L61,70L76,58.5L61,47Z" />
</vector>
```

### ./app/src/main/res/drawable/asset_reader_cover.xml

```
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="96dp"
    android:height="128dp"
    android:viewportWidth="96"
    android:viewportHeight="128">
    <path
        android:fillColor="#2F6F68"
        android:pathData="M18,8H76C82.6,8 88,13.4 88,20V108C88,114.6 82.6,120 76,120H18C12.5,120 8,115.5 8,110V18C8,12.5 12.5,8 18,8Z" />
    <path
        android:fillColor="#F0DFBE"
        android:pathData="M20,20H73C75.2,20 77,21.8 77,24V96C77,98.2 75.2,100 73,100H20V20Z" />
    <path
        android:strokeColor="#10201D"
        android:strokeWidth="3"
        android:strokeLineCap="round"
        android:strokeLineJoin="round"
        android:fillColor="@android:color/transparent"
        android:pathData="M28,64C33,50 39,50 44,64C49,78 55,78 60,64C65,50 71,50 76,64" />
    <path
        android:fillColor="#9A4F3E"
        android:pathData="M54,42L54,86L82,64L54,42Z" />
    <path
        android:fillColor="#10201D"
        android:fillAlpha="0.18"
        android:pathData="M8,18C8,12.5 12.5,8 18,8H25V120H18C12.5,120 8,115.5 8,110V18Z" />
</vector>
```

### ./app/src/main/res/drawable/asset_audio_generation.xml

```
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="132dp"
    android:height="96dp"
    android:viewportWidth="132"
    android:viewportHeight="96">
    <path
        android:fillColor="#F0DFBE"
        android:pathData="M10,20C10,12.3 16.3,6 24,6H108C115.7,6 122,12.3 122,20V76C122,83.7 115.7,90 108,90H24C16.3,90 10,83.7 10,76V20Z" />
    <path
        android:strokeColor="#2F6F68"
        android:strokeWidth="6"
        android:strokeLineCap="round"
        android:strokeLineJoin="round"
        android:fillColor="@android:color/transparent"
        android:pathData="M24,50H34L41,31L51,72L62,22L73,72L83,39L91,50H108" />
    <path
        android:fillColor="#9A4F3E"
        android:pathData="M56,32L56,64L80,48L56,32Z" />
    <path
        android:strokeColor="#10201D"
        android:strokeAlpha="0.25"
        android:strokeWidth="3"
        android:strokeLineCap="round"
        android:fillColor="@android:color/transparent"
        android:pathData="M28,18H104" />
</vector>
```

### ./app/src/main/res/drawable/asset_empty_library.xml

```
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="180dp"
    android:height="180dp"
    android:viewportWidth="180"
    android:viewportHeight="180">
    <path
        android:fillColor="#D6E7E1"
        android:pathData="M90,160C128.7,160 160,128.7 160,90C160,51.3 128.7,20 90,20C51.3,20 20,51.3 20,90C20,128.7 51.3,160 90,160Z" />
    <path
        android:fillColor="#2F6F68"
        android:pathData="M50,45C50,39.5 54.5,35 60,35H87C92.5,35 97,39.5 97,45V137C97,142.5 92.5,147 87,147H60C54.5,147 50,142.5 50,137V45Z" />
    <path
        android:fillColor="#2F6F68"
        android:pathData="M83,45C83,39.5 87.5,35 93,35H120C125.5,35 130,39.5 130,45V137C130,142.5 125.5,147 120,147H93C87.5,147 83,142.5 83,137V45Z" />
    <path
        android:fillColor="#F0DFBE"
        android:pathData="M60,46C60,44.3 61.3,43 63,43H84C87.3,43 90,45.7 90,49V128C90,129.7 88.7,131 87,131H63C61.3,131 60,129.7 60,128V46Z" />
    <path
        android:fillColor="#FAFBF8"
        android:pathData="M90,49C90,45.7 92.7,43 96,43H117C118.7,43 120,44.3 120,46V128C120,129.7 118.7,131 117,131H93C91.3,131 90,129.7 90,128V49Z" />
    <path
        android:strokeColor="#10201D"
        android:strokeWidth="5"
        android:strokeLineCap="round"
        android:strokeLineJoin="round"
        android:fillColor="@android:color/transparent"
        android:pathData="M68,95C73,78 80,78 85,95C90,112 97,112 102,95C107,78 114,78 119,95" />
    <path
        android:fillColor="#9A4F3E"
        android:pathData="M101,74L101,113L127,93.5L101,74Z" />
</vector>
```

### ./app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml

```
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
    <monochrome android:drawable="@drawable/ic_launcher_monochrome" />
</adaptive-icon>
```

### ./app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml

```
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
    <monochrome android:drawable="@drawable/ic_launcher_monochrome" />
</adaptive-icon>
```

### ./app/src/main/java/com/auralis/reader/AuralisRepository.kt

```
package com.auralis.reader

import android.content.Context
import android.net.Uri
import com.auralis.ai.BookAnalysisInput
import com.auralis.ai.HeuristicBookAnalyzer
import com.auralis.audio.VoiceModelRepository
import com.auralis.database.AudiobookJobEntity
import com.auralis.database.AuralisDatabase
import com.auralis.database.BookEntity
import com.auralis.database.BookMetadataEntity
import com.auralis.database.BookmarkEntity
import com.auralis.database.ChapterEntity
import com.auralis.database.CharacterProfileEntity
import com.auralis.database.HighlightEntity
import com.auralis.database.PronunciationHintEntity
import com.auralis.database.ReadingPositionEntity
import com.auralis.jobs.AudiobookJobScheduler
import com.auralis.reader.core.BookImporter
import java.io.File
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AuralisRepository(private val context: Context) {
    private val dao = AuralisDatabase.get(context).dao()
    private val importer = BookImporter(context)
    private val analyzer = HeuristicBookAnalyzer()
    private val voiceRepository = VoiceModelRepository(context, dao)
    private val audiobookJobScheduler = AudiobookJobScheduler(context)

    val books: Flow<List<BookEntity>> = dao.observeBooks()
    val voices = dao.observeVoiceModels()

    fun observeBook(bookId: String) = dao.observeBook(bookId)
    fun observeChapters(bookId: String) = dao.observeChapters(bookId)
    fun observeMetadata(bookId: String) = dao.observeMetadata(bookId)
    fun observeCharacters(bookId: String) = dao.observeCharacters(bookId)
    fun observeJob(bookId: String) = dao.observeLatestAudiobookJob(bookId)
    fun observeBookmarks(bookId: String) = dao.observeBookmarks(bookId)
    fun observeHighlights(bookId: String) = dao.observeHighlights(bookId)

    suspend fun seedVoiceCatalog() = voiceRepository.seedCatalog()

    suspend fun importBook(uri: Uri): String = withContext(Dispatchers.IO) {
        val imported = importer.import(uri)
        val now = System.currentTimeMillis()
        val chapters = imported.chapters.map {
            ChapterEntity(
                id = it.id,
                bookId = imported.id,
                title = it.title,
                sortIndex = it.sortIndex,
                textPath = it.textPath,
                characterCount = it.characterCount,
                pageStart = it.pageStart,
                pageEnd = it.pageEnd
            )
        }
        val sample = imported.chapters
            .take(3)
            .joinToString("\n\n") { File(it.textPath).readText().take(12_000) }
        val analysis = analyzer.analyze(
            BookAnalysisInput(
                title = imported.title,
                chapterTitles = imported.chapters.map { it.title },
                textSample = sample
            )
        )

        dao.insertImportedBook(
            book = BookEntity(
                id = imported.id,
                title = imported.title,
                author = imported.author,
                format = imported.format.name,
                sourceUri = imported.sourceUri,
                localPath = imported.localPath,
                importStatus = imported.importStatus.name,
                createdAtMillis = now,
                updatedAtMillis = now
            ),
            chapters = chapters,
            metadata = BookMetadataEntity(
                bookId = imported.id,
                language = analysis.language,
                genre = analysis.genre,
                tone = analysis.tone,
                synopsis = analysis.synopsis,
                source = analysis.source,
                confidence = analysis.confidence,
                updatedAtMillis = now
            ),
            characters = analysis.characters.map {
                CharacterProfileEntity(
                    id = stableId(imported.id, it.name),
                    bookId = imported.id,
                    name = it.name,
                    aliases = it.aliases.joinToString("|"),
                    description = it.description,
                    pronunciation = it.pronunciation,
                    confidence = it.confidence
                )
            },
            hints = analysis.pronunciationHints.map {
                PronunciationHintEntity(
                    id = stableId(imported.id, it.phrase),
                    bookId = imported.id,
                    phrase = it.phrase,
                    hint = it.hint,
                    source = it.source
                )
            },
            job = AudiobookJobEntity(
                id = "job-${imported.id}",
                bookId = imported.id,
                voiceModelId = null,
                status = "not_started",
                currentChapterId = null,
                completedSegments = 0,
                totalSegments = chapters.size,
                lastError = null,
                updatedAtMillis = now
            )
        )
        imported.id
    }

    suspend fun saveReadingPosition(bookId: String, chapterId: String?) {
        dao.upsertReadingPosition(
            ReadingPositionEntity(
                bookId = bookId,
                chapterId = chapterId,
                textOffset = 0,
                pageIndex = null,
                updatedAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun addBookmark(bookId: String, chapterId: String, label: String) {
        dao.insertBookmark(
            BookmarkEntity(
                bookId = bookId,
                chapterId = chapterId,
                textOffset = 0,
                label = label,
                createdAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun addHighlight(bookId: String, chapterId: String, note: String) {
        dao.insertHighlight(
            HighlightEntity(
                bookId = bookId,
                chapterId = chapterId,
                startOffset = 0,
                endOffset = 160,
                note = note,
                colorName = "sage",
                createdAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun installVoice(uri: Uri) = withContext(Dispatchers.IO) {
        voiceRepository.installOnnxVoice(uri)
    }

    suspend fun downloadDefaultVoice() = withContext(Dispatchers.IO) {
        voiceRepository.downloadDefaultKokoroVoice()
    }

    fun prepareAudiobook(bookId: String) {
        audiobookJobScheduler.enqueue(bookId)
    }

    fun readChapterText(chapter: ChapterEntity): String {
        return File(chapter.textPath).takeIf { it.exists() }?.readText().orEmpty()
    }

    private fun stableId(bookId: String, value: String): String {
        return UUID.nameUUIDFromBytes("$bookId:$value".toByteArray()).toString()
    }
}
```

### ./app/src/main/java/com/auralis/reader/MainActivity.kt

```
package com.auralis.reader

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.auralis.database.AudiobookJobEntity
import com.auralis.database.BookEntity
import com.auralis.database.BookMetadataEntity
import com.auralis.database.BookmarkEntity
import com.auralis.database.ChapterEntity
import com.auralis.database.CharacterProfileEntity
import com.auralis.database.HighlightEntity
import com.auralis.database.VoiceModelEntity
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = AuralisRepository(applicationContext)
        setContent {
            AuralisTheme {
                AuralisApp(repository)
            }
        }
    }
}

@Composable
private fun AuralisTheme(content: @Composable () -> Unit) {
    val colors = lightColorScheme(
        primary = Color(0xFF2F6F68),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFD6E7E1),
        onPrimaryContainer = Color(0xFF10201D),
        secondary = Color(0xFF756144),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFF0DFBE),
        tertiary = Color(0xFF9A4F3E),
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFFFDAD1),
        surface = Color(0xFFFAFBF8),
        surfaceVariant = Color(0xFFE1E7DF),
        background = Color(0xFFFAFBF8),
        error = Color(0xFFB3261E)
    )

    MaterialTheme(colorScheme = colors) {
        Surface(Modifier.fillMaxSize(), color = colors.background) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuralisApp(repository: AuralisRepository) {
    val scope = rememberCoroutineScope()
    val books by repository.books.collectAsState(initial = emptyList())
    val voices by repository.voices.collectAsState(initial = emptyList())
    var selectedBookId by rememberSaveable { mutableStateOf<String?>(null) }
    var transientStatus by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        repository.seedVoiceCatalog()
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                transientStatus = "Importing book"
                selectedBookId = runCatching { repository.importBook(uri) }
                    .onFailure { transientStatus = it.message ?: "Import failed" }
                    .getOrNull()
                if (selectedBookId != null) transientStatus = null
            }
        }
    }

    fun downloadDefaultVoice() {
        scope.launch {
            transientStatus = null
            runCatching { repository.downloadDefaultVoice() }
                .onSuccess { transientStatus = "Natural voice installed" }
                .onFailure { transientStatus = it.message ?: "Voice download failed" }
        }
    }

    if (selectedBookId == null) {
        LibraryScreen(
            books = books,
            voices = voices,
            status = transientStatus,
            onImportBook = { importLauncher.launch(arrayOf("application/pdf", "application/epub+zip")) },
            onInstallVoice = ::downloadDefaultVoice,
            onOpenBook = { selectedBookId = it }
        )
    } else {
        BookScreen(
            repository = repository,
            bookId = selectedBookId.orEmpty(),
            onBack = { selectedBookId = null },
            onInstallVoice = ::downloadDefaultVoice
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibraryScreen(
    books: List<BookEntity>,
    voices: List<VoiceModelEntity>,
    status: String?,
    onImportBook: () -> Unit,
    onInstallVoice: () -> Unit,
    onOpenBook: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Auralis", fontWeight = FontWeight.SemiBold)
                        Text("Library", style = MaterialTheme.typography.labelMedium)
                    }
                },
                actions = {
                    IconButton(onClick = onInstallVoice) {
                        Icon(Icons.Rounded.Mic, contentDescription = "Install voice")
                    }
                    FilledIconButton(onClick = onImportBook) {
                        Icon(Icons.Rounded.Add, contentDescription = "Import book")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatusStrip(status = status, voices = voices, onInstallVoice = onInstallVoice)
            if (books.isEmpty()) {
                EmptyLibrary(onImportBook)
            } else {
                books.forEach { book ->
                    BookRow(book = book, onClick = { onOpenBook(book.id) })
                }
            }
        }
    }
}

@Composable
private fun StatusStrip(
    status: String?,
    voices: List<VoiceModelEntity>,
    onInstallVoice: () -> Unit
) {
    val voice = voices.firstOrNull { it.id == "kokoro-natural-en" }
    val installedVoice = voice?.takeIf { it.status == "installed" }
    val isDownloading = voice?.status == "downloading"
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AssistChip(
            onClick = onInstallVoice,
            label = {
                Text(
                    when {
                        installedVoice != null -> installedVoice.displayName
                        isDownloading -> "Downloading natural voice"
                        else -> "Download natural voice"
                    }
                )
            },
            leadingIcon = {
                Icon(
                    when {
                        installedVoice != null -> Icons.Rounded.CheckCircle
                        isDownloading -> Icons.Rounded.GraphicEq
                        else -> Icons.Rounded.Download
                    },
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        )
        if (status != null) {
            AssistChip(
                onClick = {},
                label = { Text(status) },
                leadingIcon = {
                    Icon(Icons.Rounded.GraphicEq, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            )
        }
    }
}

@Composable
private fun EmptyLibrary(onImportBook: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(360.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Image(
                painter = painterResource(R.drawable.asset_empty_library),
                contentDescription = null,
                modifier = Modifier.size(132.dp)
            )
            Text("No books yet", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            FilledTonalButton(onClick = onImportBook) {
                Icon(Icons.Rounded.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Import PDF or EPUB")
            }
        }
    }
}

@Composable
private fun BookRow(book: BookEntity, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.asset_reader_cover),
                contentDescription = null,
                modifier = Modifier.size(width = 48.dp, height = 64.dp)
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(book.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${book.format.uppercase()}  ${book.importStatus}", style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Rounded.Headphones, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookScreen(
    repository: AuralisRepository,
    bookId: String,
    onBack: () -> Unit,
    onInstallVoice: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val book by repository.observeBook(bookId).collectAsState(initial = null)
    val chapters by repository.observeChapters(bookId).collectAsState(initial = emptyList())
    val metadata by repository.observeMetadata(bookId).collectAsState(initial = null)
    val characters by repository.observeCharacters(bookId).collectAsState(initial = emptyList())
    val job by repository.observeJob(bookId).collectAsState(initial = null)
    val bookmarks by repository.observeBookmarks(bookId).collectAsState(initial = emptyList())
    val highlights by repository.observeHighlights(bookId).collectAsState(initial = emptyList())
    val voices by repository.voices.collectAsState(initial = emptyList())
    var mode by rememberSaveable { mutableStateOf("read") }
    var chapterIndex by rememberSaveable(bookId) { mutableIntStateOf(0) }

    val selectedChapter = chapters.getOrNull(chapterIndex.coerceIn(0, (chapters.size - 1).coerceAtLeast(0)))
    LaunchedEffect(selectedChapter?.id) {
        repository.saveReadingPosition(bookId, selectedChapter?.id)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Column {
                        Text(book?.title ?: "Book", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${chapters.size} chapters", style = MaterialTheme.typography.labelMedium)
                    }
                },
                actions = {
                    IconButton(onClick = { mode = "search" }) {
                        Icon(Icons.Rounded.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = onInstallVoice) {
                        Icon(Icons.Rounded.Download, contentDescription = "Install voice")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ModeTabs(selected = mode, onSelected = { mode = it })
            when (mode) {
                "audio" -> AudioPane(
                    job = job,
                    voices = voices,
                    onInstallVoice = onInstallVoice,
                    onPrepare = { repository.prepareAudiobook(bookId) }
                )
                "details" -> DetailsPane(metadata, characters)
                "notes" -> NotesPane(bookmarks, highlights)
                "search" -> SearchPane(chapters, repository)
                else -> ReaderPane(
                    chapter = selectedChapter,
                    chapterCount = chapters.size,
                    chapterIndex = chapterIndex,
                    text = selectedChapter?.let(repository::readChapterText).orEmpty(),
                    onPrevious = { chapterIndex = (chapterIndex - 1).coerceAtLeast(0) },
                    onNext = { chapterIndex = (chapterIndex + 1).coerceAtMost((chapters.size - 1).coerceAtLeast(0)) },
                    onBookmark = {
                        selectedChapter?.let { chapter ->
                            scope.launch { repository.addBookmark(bookId, chapter.id, chapter.title) }
                        }
                    },
                    onHighlight = {
                        selectedChapter?.let { chapter ->
                            scope.launch { repository.addHighlight(bookId, chapter.id, "Opening passage") }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ModeTabs(selected: String, onSelected: (String) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ModeButton("read", "Read", Icons.Rounded.Book, selected, onSelected)
        ModeButton("audio", "Audio", Icons.Rounded.Headphones, selected, onSelected)
        ModeButton("details", "Details", Icons.Rounded.GraphicEq, selected, onSelected)
        ModeButton("notes", "Notes", Icons.Rounded.Star, selected, onSelected)
    }
}

@Composable
private fun ModeButton(
    id: String,
    label: String,
    icon: ImageVector,
    selected: String,
    onSelected: (String) -> Unit
) {
    val selectedMode = selected == id
    if (selectedMode) {
        Button(onClick = { onSelected(id) }, modifier = Modifier.height(40.dp)) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(label)
        }
    } else {
        OutlinedButton(onClick = { onSelected(id) }, modifier = Modifier.height(40.dp)) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun ReaderPane(
    chapter: ChapterEntity?,
    chapterCount: Int,
    chapterIndex: Int,
    text: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onBookmark: () -> Unit,
    onHighlight: () -> Unit
) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onPrevious, enabled = chapterIndex > 0) { Text("Previous") }
            Column(Modifier.weight(1f)) {
                Text(chapter?.title ?: "No readable text", fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${chapterIndex + 1} of ${chapterCount.coerceAtLeast(1)}", style = MaterialTheme.typography.labelMedium)
            }
            OutlinedButton(onClick = onNext, enabled = chapterIndex < chapterCount - 1) { Text("Next") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilledTonalButton(onClick = onBookmark, enabled = chapter != null) { Text("Bookmark") }
            FilledTonalButton(onClick = onHighlight, enabled = chapter != null) { Text("Highlight") }
        }
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = text.ifBlank { "This file did not expose selectable text. OCR support is reserved for the next implementation pass." },
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = FontFamily.Serif,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.35
            )
        }
    }
}

@Composable
private fun AudioPane(
    job: AudiobookJobEntity?,
    voices: List<VoiceModelEntity>,
    onInstallVoice: () -> Unit,
    onPrepare: () -> Unit
) {
    val installedVoice = voices.firstOrNull { it.status == "installed" }
    val isDownloading = voices.any { it.status == "downloading" }
    val synthesisReady = installedVoice != null
    val jobError = audioJobError(job?.lastError)
    val progress = if ((job?.totalSegments ?: 0) > 0) {
        (job?.completedSegments ?: 0).toFloat() / job!!.totalSegments.toFloat()
    } else {
        0f
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                Modifier
                    .size(56.dp)
                    .background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(
                        if (installedVoice == null) {
                            R.drawable.asset_audio_generation
                        } else {
                            R.drawable.asset_voice_ready
                        }
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(52.dp)
                )
            }
            Column(Modifier.weight(1f)) {
                Text(audioJobStatus(job?.status), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    when {
                        installedVoice != null -> installedVoice.displayName
                        isDownloading -> "Downloading natural voice"
                        else -> "Download natural voice"
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
        if (!jobError.isNullOrBlank()) {
            Text(jobError, color = MaterialTheme.colorScheme.error)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilledTonalButton(onClick = onInstallVoice) {
                Icon(Icons.Rounded.Mic, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (installedVoice == null) "Download voice" else "Voice installed")
            }
            Button(onClick = onPrepare, enabled = installedVoice != null && synthesisReady) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    when {
                        installedVoice == null -> "Install voice first"
                        job?.status == "running" -> "Restart"
                        else -> "Prepare"
                    }
                )
            }
        }
    }
}

private fun audioJobStatus(status: String?): String {
    return when (status) {
        null -> "not started"
        "waiting_for_voice_adapter", "synthesis_adapter_pending" -> "ready to prepare"
        "unsupported_voice_pack" -> "unsupported voice pack"
        else -> status.replace('_', ' ')
    }
}

private fun audioJobError(error: String?): String? {
    val value = error?.takeIf { it.isNotBlank() } ?: return null
    val oldAdapterMessage = value.contains("not compatible", ignoreCase = true) ||
        value.contains("synthesis adapter", ignoreCase = true)
    return if (oldAdapterMessage) null else value
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailsPane(
    metadata: BookMetadataEntity?,
    characters: List<CharacterProfileEntity>
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(metadata?.synopsis ?: "Analysis pending", style = MaterialTheme.typography.bodyLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(onClick = {}, label = { Text(metadata?.genre ?: "genre") })
            AssistChip(onClick = {}, label = { Text(metadata?.tone ?: "tone") })
            AssistChip(onClick = {}, label = { Text(metadata?.source ?: "source") })
        }
        Text("Characters", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        characters.forEach { character ->
            CharacterRow(character)
        }
    }
}

@Composable
private fun CharacterRow(character: CharacterProfileEntity) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            Modifier
                .size(38.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(character.name.take(1), fontWeight = FontWeight.Bold)
        }
        Column(Modifier.weight(1f)) {
            Text(character.name, fontWeight = FontWeight.SemiBold)
            Text(character.description, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun NotesPane(
    bookmarks: List<BookmarkEntity>,
    highlights: List<HighlightEntity>
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Bookmarks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        if (bookmarks.isEmpty()) Text("None")
        bookmarks.forEach { Text(it.label, style = MaterialTheme.typography.bodyLarge) }
        Text("Highlights", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        if (highlights.isEmpty()) Text("None")
        highlights.forEach { Text(it.note ?: it.colorName, style = MaterialTheme.typography.bodyLarge) }
    }
}

@Composable
private fun SearchPane(chapters: List<ChapterEntity>, repository: AuralisRepository) {
    var query by rememberSaveable { mutableStateOf("") }
    val matches = remember(query, chapters) {
        if (query.length < 3) {
            emptyList()
        } else {
            chapters.mapNotNull { chapter ->
                val text = repository.readChapterText(chapter)
                val index = text.indexOf(query, ignoreCase = true)
                if (index >= 0) chapter.title to text.substring(index, (index + 180).coerceAtMost(text.length)) else null
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        androidx.compose.material3.OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            label = { Text("Search") }
        )
        matches.forEach { (title, excerpt) ->
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(excerpt, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
```

### ./build_context.sh

```
#!/bin/bash
mkdir -p .context
CONTEXT_FILE=".context/project_context.md"

echo "# Project Context" > "$CONTEXT_FILE"
echo "## Project Structure" >> "$CONTEXT_FILE"
tree -a -I "build|.gradle|.git|.context|*.jar" >> "$CONTEXT_FILE"

echo -e "\n## Files" >> "$CONTEXT_FILE"
find . -type f -not -path "*/build/*" -not -path "*/\.gradle/*" -not -path "*/\.git/*" -not -path "*/\.context/*" -not -path "*/gradle/wrapper/*" -not -name "gradlew" -not -name "*.jar" -not -name "*.png" -not -name "*.webp" | while read -r file; do
    echo -e "\n### $file\n" >> "$CONTEXT_FILE"
    echo '```' >> "$CONTEXT_FILE"
    cat "$file" >> "$CONTEXT_FILE"
    echo '```' >> "$CONTEXT_FILE"
done
```

### ./gradle.properties

```
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
android.defaults.buildfeatures.resvalues=true
android.uniquePackageNames=false
android.dependency.useConstraints=true
android.builtInKotlin=false
android.newDsl=false
```

### ./core/database/build.gradle.kts

```
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
}

android {
    namespace = "com.auralis.database"
    compileSdk = 36

    defaultConfig {
        minSdk = 29
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    api(libs.androidx.room.runtime)
    api(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)
    testImplementation(libs.junit)
}
```

### ./core/database/src/main/java/com/auralis/database/AuralisDao.kt

```
package com.auralis.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AuralisDao {
    @Query("SELECT * FROM books ORDER BY updatedAtMillis DESC")
    fun observeBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE id = :bookId")
    fun observeBook(bookId: String): Flow<BookEntity?>

    @Query("SELECT * FROM books WHERE id = :bookId")
    suspend fun getBook(bookId: String): BookEntity?

    @Upsert
    suspend fun upsertBook(book: BookEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterEntity>)

    @Query("SELECT * FROM chapters WHERE bookId = :bookId ORDER BY sortIndex")
    fun observeChapters(bookId: String): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE bookId = :bookId ORDER BY sortIndex")
    suspend fun getChapters(bookId: String): List<ChapterEntity>

    @Query("SELECT * FROM chapters WHERE id = :chapterId")
    suspend fun getChapter(chapterId: String): ChapterEntity?

    @Upsert
    suspend fun upsertReadingPosition(position: ReadingPositionEntity)

    @Query("SELECT * FROM reading_positions WHERE bookId = :bookId")
    fun observeReadingPosition(bookId: String): Flow<ReadingPositionEntity?>

    @Insert
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("SELECT * FROM bookmarks WHERE bookId = :bookId ORDER BY createdAtMillis DESC")
    fun observeBookmarks(bookId: String): Flow<List<BookmarkEntity>>

    @Insert
    suspend fun insertHighlight(highlight: HighlightEntity)

    @Query("SELECT * FROM highlights WHERE bookId = :bookId ORDER BY createdAtMillis DESC")
    fun observeHighlights(bookId: String): Flow<List<HighlightEntity>>

    @Upsert
    suspend fun upsertMetadata(metadata: BookMetadataEntity)

    @Query("SELECT * FROM book_metadata WHERE bookId = :bookId")
    fun observeMetadata(bookId: String): Flow<BookMetadataEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacters(characters: List<CharacterProfileEntity>)

    @Query("SELECT * FROM characters WHERE bookId = :bookId ORDER BY confidence DESC, name")
    fun observeCharacters(bookId: String): Flow<List<CharacterProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPronunciationHints(hints: List<PronunciationHintEntity>)

    @Query("SELECT * FROM pronunciation_hints WHERE bookId = :bookId ORDER BY phrase")
    fun observePronunciationHints(bookId: String): Flow<List<PronunciationHintEntity>>

    @Upsert
    suspend fun upsertVoiceModel(voiceModel: VoiceModelEntity)

    @Query("SELECT * FROM voice_models ORDER BY displayName")
    fun observeVoiceModels(): Flow<List<VoiceModelEntity>>

    @Query("SELECT * FROM voice_models WHERE status = 'installed' ORDER BY updatedAtMillis DESC LIMIT 1")
    suspend fun getDefaultInstalledVoice(): VoiceModelEntity?

    @Query("SELECT * FROM voice_models WHERE id = :voiceModelId")
    suspend fun getVoiceModel(voiceModelId: String): VoiceModelEntity?

    @Upsert
    suspend fun upsertAudiobookJob(job: AudiobookJobEntity)

    @Query("SELECT * FROM audiobook_jobs WHERE bookId = :bookId ORDER BY updatedAtMillis DESC LIMIT 1")
    fun observeLatestAudiobookJob(bookId: String): Flow<AudiobookJobEntity?>

    @Query("SELECT * FROM audiobook_jobs WHERE bookId = :bookId ORDER BY updatedAtMillis DESC LIMIT 1")
    suspend fun getLatestAudiobookJob(bookId: String): AudiobookJobEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudioSegment(segment: AudioSegmentEntity)

    @Query("SELECT * FROM audio_segments WHERE bookId = :bookId ORDER BY sortIndex")
    fun observeAudioSegments(bookId: String): Flow<List<AudioSegmentEntity>>

    @Transaction
    suspend fun insertImportedBook(
        book: BookEntity,
        chapters: List<ChapterEntity>,
        metadata: BookMetadataEntity,
        characters: List<CharacterProfileEntity>,
        hints: List<PronunciationHintEntity>,
        job: AudiobookJobEntity
    ) {
        upsertBook(book)
        insertChapters(chapters)
        upsertMetadata(metadata)
        insertCharacters(characters)
        insertPronunciationHints(hints)
        upsertAudiobookJob(job)
    }
}
```

### ./core/database/src/main/java/com/auralis/database/AuralisEntities.kt

```
package com.auralis.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey val id: String,
    val title: String,
    val author: String?,
    val format: String,
    val sourceUri: String,
    val localPath: String,
    val importStatus: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)

@Entity(
    tableName = "chapters",
    indices = [Index("bookId"), Index(value = ["bookId", "sortIndex"], unique = true)],
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ChapterEntity(
    @PrimaryKey val id: String,
    val bookId: String,
    val title: String,
    val sortIndex: Int,
    val textPath: String,
    val characterCount: Int,
    val pageStart: Int?,
    val pageEnd: Int?
)

@Entity(tableName = "reading_positions")
data class ReadingPositionEntity(
    @PrimaryKey val bookId: String,
    val chapterId: String?,
    val textOffset: Int,
    val pageIndex: Int?,
    val updatedAtMillis: Long
)

@Entity(
    tableName = "bookmarks",
    indices = [Index("bookId"), Index("chapterId")]
)
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: String,
    val chapterId: String,
    val textOffset: Int,
    val label: String,
    val createdAtMillis: Long
)

@Entity(
    tableName = "highlights",
    indices = [Index("bookId"), Index("chapterId")]
)
data class HighlightEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: String,
    val chapterId: String,
    val startOffset: Int,
    val endOffset: Int,
    val note: String?,
    val colorName: String,
    val createdAtMillis: Long
)

@Entity(tableName = "book_metadata")
data class BookMetadataEntity(
    @PrimaryKey val bookId: String,
    val language: String,
    val genre: String,
    val tone: String,
    val synopsis: String,
    val source: String,
    val confidence: Float,
    val updatedAtMillis: Long
)

@Entity(
    tableName = "characters",
    indices = [Index("bookId")]
)
data class CharacterProfileEntity(
    @PrimaryKey val id: String,
    val bookId: String,
    val name: String,
    val aliases: String,
    val description: String,
    val pronunciation: String?,
    val confidence: Float
)

@Entity(
    tableName = "pronunciation_hints",
    indices = [Index("bookId")]
)
data class PronunciationHintEntity(
    @PrimaryKey val id: String,
    val bookId: String,
    val phrase: String,
    val hint: String,
    val source: String
)

@Entity(tableName = "voice_models")
data class VoiceModelEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val language: String,
    val runtime: String,
    val status: String,
    val modelPath: String?,
    val configPath: String?,
    val sizeBytes: Long?,
    val updatedAtMillis: Long
)

@Entity(
    tableName = "audiobook_jobs",
    indices = [Index("bookId")]
)
data class AudiobookJobEntity(
    @PrimaryKey val id: String,
    val bookId: String,
    val voiceModelId: String?,
    val status: String,
    val currentChapterId: String?,
    val completedSegments: Int,
    val totalSegments: Int,
    val lastError: String?,
    val updatedAtMillis: Long
)

@Entity(
    tableName = "audio_segments",
    indices = [Index("bookId"), Index("chapterId"), Index("jobId")]
)
data class AudioSegmentEntity(
    @PrimaryKey val id: String,
    val jobId: String,
    val bookId: String,
    val chapterId: String,
    val sortIndex: Int,
    val textStartOffset: Int,
    val textEndOffset: Int,
    val filePath: String,
    val durationMillis: Long,
    val checksum: String,
    val createdAtMillis: Long
)
```

### ./core/database/src/main/java/com/auralis/database/AuralisDatabase.kt

```
package com.auralis.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        BookEntity::class,
        ChapterEntity::class,
        ReadingPositionEntity::class,
        BookmarkEntity::class,
        HighlightEntity::class,
        BookMetadataEntity::class,
        CharacterProfileEntity::class,
        PronunciationHintEntity::class,
        VoiceModelEntity::class,
        AudiobookJobEntity::class,
        AudioSegmentEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AuralisDatabase : RoomDatabase() {
    abstract fun dao(): AuralisDao

    companion object {
        @Volatile
        private var instance: AuralisDatabase? = null

        fun get(context: Context): AuralisDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AuralisDatabase::class.java,
                    "auralis.db"
                )
                    .fallbackToDestructiveMigration(false)
                    .build()
                    .also { instance = it }
            }
        }
    }
}
```

### ./core/jobs/build.gradle.kts

```
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.auralis.jobs"
    compileSdk = 36

    defaultConfig {
        minSdk = 29
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":core:database"))
    implementation(project(":core:audio"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.work.runtime.ktx)
    testImplementation(libs.junit)
}
```

### ./core/jobs/src/main/java/com/auralis/jobs/AudiobookJobScheduler.kt

```
package com.auralis.jobs

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf

class AudiobookJobScheduler(private val context: Context) {
    fun enqueue(bookId: String) {
        val request = OneTimeWorkRequestBuilder<AudiobookGenerationWorker>()
            .setInputData(workDataOf(AudiobookGenerationWorker.KEY_BOOK_ID to bookId))
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "audiobook-$bookId",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}
```

### ./core/jobs/src/main/java/com/auralis/jobs/AudiobookGenerationWorker.kt

```
package com.auralis.jobs

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.auralis.audio.NarrationPlanner
import com.auralis.audio.OnnxNaturalTtsEngine
import com.auralis.audio.VoiceRuntimeFailure
import com.auralis.database.AudioSegmentEntity
import com.auralis.database.AudiobookJobEntity
import com.auralis.database.AuralisDatabase
import java.io.File
import java.security.MessageDigest
import kotlinx.coroutines.CancellationException

class AudiobookGenerationWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val bookId = inputData.getString(KEY_BOOK_ID) ?: return Result.failure()
        val database = AuralisDatabase.get(applicationContext)
        val dao = database.dao()
        val chapters = dao.getChapters(bookId)
        if (chapters.isEmpty()) {
            upsertJob(bookId, status = "failed", error = "No extracted chapters are available.")
            return Result.failure()
        }

        val voice = dao.getDefaultInstalledVoice()
        if (voice == null) {
            upsertJob(
                bookId = bookId,
                status = "waiting_for_voice",
                totalSegments = chapters.size,
                error = "Install a natural ONNX voice model before generation."
            )
            return Result.success()
        }

        val planner = NarrationPlanner()
        val engine = OnnxNaturalTtsEngine()
        var completed = 0
        val total = chapters.sumOf { chapter ->
            planner.planChapter(chapter.id, File(chapter.textPath).readText()).size.coerceAtLeast(1)
        }

        upsertJob(bookId, voice.id, "running", totalSegments = total)

        return try {
            chapters.forEach { chapter ->
                val text = File(chapter.textPath).readText()
                val outputDir = File(applicationContext.filesDir, "audio/$bookId/${chapter.id}").also { it.mkdirs() }
                planner.planChapter(chapter.id, text).forEach { request ->
                    val rendered = engine.render(request, voice, outputDir.absolutePath)
                    dao.insertAudioSegment(
                        AudioSegmentEntity(
                            id = request.id,
                            jobId = "job-$bookId",
                            bookId = bookId,
                            chapterId = chapter.id,
                            sortIndex = request.sortIndex,
                            textStartOffset = request.textStartOffset,
                            textEndOffset = request.textEndOffset,
                            filePath = rendered.filePath,
                            durationMillis = rendered.durationMillis,
                            checksum = rendered.checksum.ifBlank { checksum(File(rendered.filePath)) },
                            createdAtMillis = System.currentTimeMillis()
                        )
                    )
                    completed += 1
                    upsertJob(bookId, voice.id, "running", chapter.id, completed, total)
                }
            }
            upsertJob(bookId, voice.id, "complete", completedSegments = completed, totalSegments = total)
            Result.success()
        } catch (failure: VoiceRuntimeFailure) {
            val status = when (failure) {
                is VoiceRuntimeFailure.MissingVoiceModel -> "waiting_for_voice"
                is VoiceRuntimeFailure.UnsupportedVoicePack -> "unsupported_voice_pack"
                is VoiceRuntimeFailure.SynthesisFailed -> "failed"
            }
            upsertJob(bookId, voice.id, status, completedSegments = completed, totalSegments = total, error = failure.message)
            Result.success()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            upsertJob(bookId, voice.id, "failed", completedSegments = completed, totalSegments = total, error = throwable.message)
            Result.retry()
        }
    }

    private suspend fun upsertJob(
        bookId: String,
        voiceModelId: String? = null,
        status: String,
        currentChapterId: String? = null,
        completedSegments: Int = 0,
        totalSegments: Int = 0,
        error: String? = null
    ) {
        AuralisDatabase.get(applicationContext).dao().upsertAudiobookJob(
            AudiobookJobEntity(
                id = "job-$bookId",
                bookId = bookId,
                voiceModelId = voiceModelId,
                status = status,
                currentChapterId = currentChapterId,
                completedSegments = completedSegments,
                totalSegments = totalSegments,
                lastError = error,
                updatedAtMillis = System.currentTimeMillis()
            )
        )
    }

    private fun checksum(file: File): String {
        if (!file.exists()) return ""
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val read = input.read(buffer)
                if (read <= 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    companion object {
        const val KEY_BOOK_ID = "book_id"
    }
}
```

### ./core/ai/build.gradle.kts

```
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.auralis.ai"
    compileSdk = 36

    defaultConfig {
        minSdk = 29
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.onnxruntime.android)
    testImplementation(libs.junit)
}
```

### ./core/ai/src/main/java/com/auralis/ai/BookAnalysis.kt

```
package com.auralis.ai

data class BookAnalysisInput(
    val title: String,
    val chapterTitles: List<String>,
    val textSample: String
)

data class BookAnalysisResult(
    val language: String,
    val genre: String,
    val tone: String,
    val synopsis: String,
    val source: String,
    val confidence: Float,
    val characters: List<CharacterCandidate>,
    val pronunciationHints: List<PronunciationCandidate>
)

data class CharacterCandidate(
    val name: String,
    val aliases: List<String>,
    val description: String,
    val pronunciation: String?,
    val confidence: Float
)

data class PronunciationCandidate(
    val phrase: String,
    val hint: String,
    val source: String
)

interface LocalBookAnalyzer {
    suspend fun analyze(input: BookAnalysisInput): BookAnalysisResult
}
```

### ./core/ai/src/main/java/com/auralis/ai/OnDeviceLlmRuntime.kt

```
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
```

### ./core/ai/src/main/java/com/auralis/ai/HeuristicBookAnalyzer.kt

```
package com.auralis.ai

import java.util.Locale

class HeuristicBookAnalyzer : LocalBookAnalyzer {
    override suspend fun analyze(input: BookAnalysisInput): BookAnalysisResult {
        val sample = input.textSample.take(80_000)
        val lower = sample.lowercase(Locale.US)
        val genre = detectGenre(lower)
        val tone = detectTone(lower)
        val characters = detectCharacterNames(sample)
        val synopsis = buildSynopsis(input.title, input.chapterTitles, genre, tone)

        return BookAnalysisResult(
            language = "en",
            genre = genre,
            tone = tone,
            synopsis = synopsis,
            source = "local-heuristic",
            confidence = 0.42f,
            characters = characters,
            pronunciationHints = characters.take(8).map {
                PronunciationCandidate(
                    phrase = it.name,
                    hint = "Preserve the name as written unless the voice model supplies a stronger pronunciation.",
                    source = "local-heuristic"
                )
            }
        )
    }

    private fun detectGenre(lower: String): String {
        val scores = mapOf(
            "mystery" to listOf("detective", "murder", "clue", "investigation", "case"),
            "fantasy" to listOf("kingdom", "sword", "magic", "dragon", "wizard"),
            "science fiction" to listOf("planet", "spaceship", "android", "colony", "galaxy"),
            "romance" to listOf("heart", "kiss", "beloved", "marriage", "desire"),
            "history" to listOf("empire", "war", "century", "king", "revolution")
        ).mapValues { (_, terms) -> terms.sumOf { term -> Regex("\\b$term\\b").findAll(lower).count() } }
        return scores.maxByOrNull { it.value }?.takeIf { it.value > 1 }?.key ?: "literary"
    }

    private fun detectTone(lower: String): String {
        val dark = listOf("shadow", "fear", "death", "cold", "blood", "alone").sumOf { Regex("\\b$it\\b").findAll(lower).count() }
        val warm = listOf("warm", "smile", "home", "friend", "laugh", "hope").sumOf { Regex("\\b$it\\b").findAll(lower).count() }
        val urgent = listOf("ran", "suddenly", "shouted", "hurry", "escape", "danger").sumOf { Regex("\\b$it\\b").findAll(lower).count() }
        return when {
            urgent >= dark && urgent >= warm && urgent > 2 -> "urgent"
            dark > warm && dark > 2 -> "tense"
            warm > dark && warm > 2 -> "warm"
            else -> "measured"
        }
    }

    private fun detectCharacterNames(sample: String): List<CharacterCandidate> {
        val commonWords = setOf(
            "The", "A", "An", "Chapter", "Book", "Part", "When", "Then", "There", "This",
            "That", "He", "She", "They", "It", "I", "We", "You", "But", "And", "For"
        )
        val counts = Regex("\\b[A-Z][a-z]{2,}(?:\\s+[A-Z][a-z]{2,})?\\b")
            .findAll(sample)
            .map { it.value.trim() }
            .filterNot { it in commonWords }
            .groupingBy { it }
            .eachCount()

        return counts.entries
            .filter { it.value >= 2 }
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .take(12)
            .map { (name, count) ->
                CharacterCandidate(
                    name = name,
                    aliases = emptyList(),
                    description = "Mentioned $count times in the extracted sample.",
                    pronunciation = null,
                    confidence = (0.35f + count.coerceAtMost(12) / 24f).coerceAtMost(0.85f)
                )
            }
    }

    private fun buildSynopsis(
        title: String,
        chapterTitles: List<String>,
        genre: String,
        tone: String
    ): String {
        val visibleChapters = chapterTitles.take(4).joinToString(", ")
        return if (visibleChapters.isBlank()) {
            "$title is indexed as a $tone $genre work. Detailed summaries will improve after the local LLM model is installed."
        } else {
            "$title is indexed as a $tone $genre work. Early sections include $visibleChapters."
        }
    }
}
```

### ./core/reader/build.gradle.kts

```
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.auralis.reader.core"
    compileSdk = 36

    defaultConfig {
        minSdk = 29
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.pdfbox.android)
    testImplementation(libs.junit)
}
```

### ./core/reader/src/main/java/com/auralis/reader/core/BookImportModels.kt

```
package com.auralis.reader.core

data class ImportedBook(
    val id: String,
    val title: String,
    val author: String?,
    val format: BookFormat,
    val sourceUri: String,
    val localPath: String,
    val chapters: List<ExtractedChapter>,
    val importStatus: ImportStatus
)

data class ExtractedChapter(
    val id: String,
    val title: String,
    val sortIndex: Int,
    val textPath: String,
    val characterCount: Int,
    val pageStart: Int? = null,
    val pageEnd: Int? = null
)

enum class BookFormat {
    Pdf,
    Epub,
    Unknown
}

enum class ImportStatus {
    Ready,
    NeedsOcr,
    Unsupported
}
```

### ./core/reader/src/main/java/com/auralis/reader/core/BookImporter.kt

```
package com.auralis.reader.core

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.File
import java.util.Locale
import java.util.UUID
import java.util.zip.ZipInputStream

class BookImporter(private val context: Context) {
    suspend fun import(uri: Uri): ImportedBook {
        val id = UUID.randomUUID().toString()
        val displayName = queryDisplayName(uri) ?: "Imported book"
        val format = inferFormat(uri, displayName)
        val bookDir = File(context.filesDir, "books/$id").also { it.mkdirs() }
        val sourceFile = File(bookDir, "source.${format.extensionFor(displayName)}")

        context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "Unable to open selected book." }
            sourceFile.outputStream().use { output -> input.copyTo(output) }
        }

        val extraction = when (format) {
            BookFormat.Epub -> extractEpub(sourceFile, bookDir, id)
            BookFormat.Pdf -> extractPdf(sourceFile, bookDir, id)
            BookFormat.Unknown -> ExtractionResult(emptyList(), ImportStatus.Unsupported)
        }

        return ImportedBook(
            id = id,
            title = displayName.toTitle(),
            author = null,
            format = format,
            sourceUri = uri.toString(),
            localPath = sourceFile.absolutePath,
            chapters = extraction.chapters,
            importStatus = extraction.status
        )
    }

    private fun queryDisplayName(uri: Uri): String? {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
        } ?: uri.lastPathSegment
    }

    private fun inferFormat(uri: Uri, displayName: String): BookFormat {
        val mime = context.contentResolver.getType(uri)?.lowercase(Locale.US).orEmpty()
        val name = displayName.lowercase(Locale.US)
        return when {
            mime == "application/epub+zip" || name.endsWith(".epub") -> BookFormat.Epub
            mime == "application/pdf" || name.endsWith(".pdf") -> BookFormat.Pdf
            else -> BookFormat.Unknown
        }
    }

    private fun extractPdf(sourceFile: File, bookDir: File, bookId: String): ExtractionResult {
        PDFBoxResourceLoader.init(context)
        val text = runCatching {
            PDDocument.load(sourceFile).use { document ->
                val stripper = PDFTextStripper()
                stripper.getText(document) to document.numberOfPages
            }
        }.getOrElse {
            return ExtractionResult(emptyList(), ImportStatus.Unsupported)
        }

        val normalized = text.first.normalizeWhitespace()
        if (normalized.length < 120) {
            return ExtractionResult(emptyList(), ImportStatus.NeedsOcr)
        }

        val chapters = writeChapters(
            bookDir = bookDir,
            bookId = bookId,
            rawSections = splitIntoBookSections(normalized),
            pageCount = text.second
        )
        return ExtractionResult(chapters, ImportStatus.Ready)
    }

    private fun extractEpub(sourceFile: File, bookDir: File, bookId: String): ExtractionResult {
        val sections = mutableListOf<NamedText>()
        ZipInputStream(sourceFile.inputStream()).use { zip ->
            generateSequence { zip.nextEntry }.forEach { entry ->
                val lowerName = entry.name.lowercase(Locale.US)
                if (!entry.isDirectory && (lowerName.endsWith(".xhtml") || lowerName.endsWith(".html") || lowerName.endsWith(".htm"))) {
                    val html = zip.readBytes().toString(Charsets.UTF_8)
                    val text = htmlToText(html).normalizeWhitespace()
                    if (text.length > 80) {
                        sections += NamedText(titleFromPath(entry.name), text)
                    }
                }
            }
        }

        if (sections.isEmpty()) {
            return ExtractionResult(emptyList(), ImportStatus.Unsupported)
        }

        return ExtractionResult(
            chapters = writeChapters(bookDir, bookId, sections),
            status = ImportStatus.Ready
        )
    }

    private fun writeChapters(
        bookDir: File,
        bookId: String,
        rawSections: List<NamedText>,
        pageCount: Int? = null
    ): List<ExtractedChapter> {
        val chapterDir = File(bookDir, "chapters").also { it.mkdirs() }
        val sections = if (rawSections.isEmpty()) listOf(NamedText("Chapter 1", "")) else rawSections
        return sections.mapIndexed { index, section ->
            val chapterId = "$bookId-${index + 1}"
            val textFile = File(chapterDir, "${index.toString().padStart(4, '0')}.txt")
            textFile.writeText(section.text)
            ExtractedChapter(
                id = chapterId,
                title = section.title.ifBlank { "Chapter ${index + 1}" },
                sortIndex = index,
                textPath = textFile.absolutePath,
                characterCount = section.text.length,
                pageStart = pageCount?.let { ((index.toFloat() / sections.size) * it).toInt().coerceAtLeast(0) },
                pageEnd = pageCount?.let { ((((index + 1).toFloat() / sections.size) * it).toInt() - 1).coerceAtLeast(0) }
            )
        }
    }

    private fun splitIntoBookSections(text: String): List<NamedText> {
        val marker = Regex("(?im)(^\\s*(chapter|book|part)\\s+([\\w\\-.' ]{1,60})$)")
        val matches = marker.findAll(text).toList()
        if (matches.size < 2) {
            return chunkText(text, 18_000).mapIndexed { index, chunk ->
                NamedText("Section ${index + 1}", chunk)
            }
        }

        return matches.mapIndexed { index, match ->
            val start = match.range.first
            val end = matches.getOrNull(index + 1)?.range?.first ?: text.length
            val title = match.value.trim().take(80)
            NamedText(title, text.substring(start, end).trim())
        }
    }

    private fun chunkText(text: String, targetSize: Int): List<String> {
        if (text.length <= targetSize) return listOf(text)
        val chunks = mutableListOf<String>()
        var cursor = 0
        while (cursor < text.length) {
            val end = (cursor + targetSize).coerceAtMost(text.length)
            val sentenceEnd = text.lastIndexOf('.', end).takeIf { it > cursor + targetSize / 2 } ?: end
            chunks += text.substring(cursor, sentenceEnd).trim()
            cursor = sentenceEnd.coerceAtLeast(cursor + 1)
        }
        return chunks.filter { it.isNotBlank() }
    }

    private fun htmlToText(html: String): String {
        return html
            .replace(Regex("(?is)<script.*?</script>"), " ")
            .replace(Regex("(?is)<style.*?</style>"), " ")
            .replace(Regex("(?is)<[^>]+>"), " ")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
    }

    private fun titleFromPath(path: String): String {
        return path.substringAfterLast('/')
            .substringBeforeLast('.')
            .replace('_', ' ')
            .replace('-', ' ')
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .joinToString(" ") { word -> word.replaceFirstChar { it.titlecase(Locale.US) } }
            .ifBlank { "Chapter" }
    }

    private fun String.normalizeWhitespace(): String {
        return replace("\u0000", " ")
            .replace(Regex("[ \\t\\x0B\\f\\r]+"), " ")
            .replace(Regex("\\n{3,}"), "\n\n")
            .trim()
    }

    private fun String.toTitle(): String {
        return substringBeforeLast('.')
            .replace('_', ' ')
            .replace('-', ' ')
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .joinToString(" ") { word -> word.replaceFirstChar { it.titlecase(Locale.US) } }
            .ifBlank { this }
    }

    private fun BookFormat.extensionFor(displayName: String): String {
        val extension = displayName.substringAfterLast('.', missingDelimiterValue = "")
        if (extension.length in 2..6) return extension.lowercase(Locale.US)
        return when (this) {
            BookFormat.Pdf -> "pdf"
            BookFormat.Epub -> "epub"
            BookFormat.Unknown -> "book"
        }
    }

    private data class NamedText(val title: String, val text: String)
    private data class ExtractionResult(val chapters: List<ExtractedChapter>, val status: ImportStatus)
}
```

### ./core/audio/build.gradle.kts

```
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.auralis.audio"
    compileSdk = 36

    defaultConfig {
        minSdk = 29
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":core:database"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.onnxruntime.android)
    testImplementation(libs.junit)
}
```

### ./core/audio/src/test/java/com/auralis/audio/PcmWavWriterTest.kt

```
package com.auralis.audio

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PcmWavWriterTest {
    @Test
    fun writeMono16CreatesPcmWaveFile() {
        val file = File.createTempFile("auralis-test", ".wav").also { it.deleteOnExit() }

        PcmWavWriter.writeMono16(file, floatArrayOf(-1f, 0f, 1f), 24_000)

        val bytes = file.readBytes()
        assertTrue(bytes.size > 44)
        assertEquals("RIFF", bytes.copyOfRange(0, 4).toString(Charsets.US_ASCII))
        assertEquals("WAVE", bytes.copyOfRange(8, 12).toString(Charsets.US_ASCII))
        assertEquals("fmt ", bytes.copyOfRange(12, 16).toString(Charsets.US_ASCII))
        assertEquals("data", bytes.copyOfRange(36, 40).toString(Charsets.US_ASCII))
        assertEquals(44 + 6, bytes.size)
    }
}
```

### ./core/audio/src/test/java/com/auralis/audio/NarrationPlannerTest.kt

```
package com.auralis.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NarrationPlannerTest {
    @Test
    fun planChapterSplitsLongParagraphsAndKeepsOffsets() {
        val text = buildString {
            append("This is the first paragraph for a narrated chapter. It has enough text to be useful.")
            append("\n\n")
            repeat(80) {
                append("Sentence $it keeps the narration segment long enough for chunking. ")
            }
        }

        val segments = NarrationPlanner().planChapter("chapter-1", text)

        assertTrue(segments.size > 2)
        assertEquals("chapter-1", segments.first().chapterId)
        assertTrue(segments.zipWithNext().all { (left, right) -> left.textEndOffset <= right.textEndOffset })
        assertTrue(segments.all { it.textStartOffset in 0 until text.length })
    }
}
```

### ./core/audio/src/test/java/com/auralis/audio/KokoroEnglishTokenizerTest.kt

```
package com.auralis.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KokoroEnglishTokenizerTest {
    @Test
    fun tokenizeAddsPaddingAndUsesKokoroVocabulary() {
        val tokens = KokoroEnglishTokenizer().tokenize("The quick thing.")

        assertEquals(0L, tokens.first())
        assertEquals(0L, tokens.last())
        assertTrue(tokens.size in 4..512)
        assertTrue(tokens.contains(81L))
        assertTrue(tokens.contains(83L))
        assertTrue(tokens.contains(4L))
    }

    @Test
    fun splitForModelKeepsChunksUnderContextTarget() {
        val text = List(80) { "This sentence should be split before it grows past the local model input window." }
            .joinToString(" ")

        val chunks = KokoroEnglishTokenizer().splitForModel(text)

        assertTrue(chunks.size > 1)
        assertTrue(chunks.all { it.length <= 430 })
    }
}
```

### ./core/audio/src/main/java/com/auralis/audio/NarrationPlanner.kt

```
package com.auralis.audio

class NarrationPlanner {
    fun planChapter(chapterId: String, chapterText: String): List<NarrationSegmentRequest> {
        val paragraphs = chapterText
            .split(Regex("\\n\\s*\\n"))
            .map { it.trim() }
            .filter { it.length > 20 }

        var cursor = 0
        return paragraphs.flatMapIndexed { paragraphIndex, paragraph ->
            chunkParagraph(paragraph).mapIndexed { chunkIndex, chunk ->
                val start = chapterText.indexOf(chunk.take(40), cursor).takeIf { it >= 0 } ?: cursor
                val end = (start + chunk.length).coerceAtMost(chapterText.length)
                cursor = end
                NarrationSegmentRequest(
                    id = "$chapterId-$paragraphIndex-$chunkIndex",
                    chapterId = chapterId,
                    sortIndex = paragraphIndex * 1000 + chunkIndex,
                    text = chunk,
                    textStartOffset = start,
                    textEndOffset = end
                )
            }
        }
    }

    private fun chunkParagraph(paragraph: String): List<String> {
        if (paragraph.length <= 900) return listOf(paragraph)
        val chunks = mutableListOf<String>()
        var cursor = 0
        while (cursor < paragraph.length) {
            val end = (cursor + 900).coerceAtMost(paragraph.length)
            val sentenceEnd = paragraph.lastIndexOf('.', end).takeIf { it > cursor + 350 } ?: end
            chunks += paragraph.substring(cursor, sentenceEnd).trim()
            cursor = sentenceEnd.coerceAtLeast(cursor + 1)
        }
        return chunks.filter { it.isNotBlank() }
    }
}
```

### ./core/audio/src/main/java/com/auralis/audio/NeuralVoiceModels.kt

```
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
```

### ./core/audio/src/main/java/com/auralis/audio/VoiceModelRepository.kt

```
package com.auralis.audio

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.auralis.database.AuralisDao
import com.auralis.database.VoiceModelEntity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class VoiceModelRepository(
    private val context: Context,
    private val dao: AuralisDao
) {
    suspend fun seedCatalog() {
        val existing = dao.getVoiceModel(DEFAULT_KOKORO_VOICE_ID)
        installedVoiceFromFiles()?.let {
            if (existing?.status != "installed" || existing.modelPath != it.modelPath) {
                dao.upsertVoiceModel(it)
            }
            return
        }

        val now = System.currentTimeMillis()
        dao.upsertVoiceModel(
            VoiceModelEntity(
                id = DEFAULT_KOKORO_VOICE_ID,
                displayName = "Kokoro Natural English",
                language = "en",
                runtime = "kokoro-onnx",
                status = "available",
                modelPath = null,
                configPath = null,
                sizeBytes = null,
                updatedAtMillis = now
            )
        )
    }

    suspend fun installOnnxVoice(uri: Uri, displayName: String? = null): VoiceModelEntity {
        val voiceDir = File(context.filesDir, "voices/$DEFAULT_KOKORO_VOICE_ID").also { it.mkdirs() }
        val fileName = displayName ?: queryDisplayName(uri) ?: "kokoro.onnx"
        val modelFile = File(voiceDir, fileName.ensureOnnxExtension())
        context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "Unable to open selected voice model." }
            modelFile.outputStream().use { output -> input.copyTo(output) }
        }

        val entity = VoiceModelEntity(
            id = DEFAULT_KOKORO_VOICE_ID,
            displayName = "Kokoro Natural English",
            language = "en",
            runtime = "kokoro-onnx",
            status = "installed",
            modelPath = modelFile.absolutePath,
            configPath = null,
            sizeBytes = modelFile.length(),
            updatedAtMillis = System.currentTimeMillis()
        )
        dao.upsertVoiceModel(entity)
        return entity
    }

    suspend fun downloadDefaultKokoroVoice(): VoiceModelEntity {
        val voiceDir = File(context.filesDir, "voices/$DEFAULT_KOKORO_VOICE_ID").also { it.mkdirs() }
        val now = System.currentTimeMillis()
        val downloadingEntity = VoiceModelEntity(
            id = DEFAULT_KOKORO_VOICE_ID,
            displayName = "Kokoro Natural English",
            language = "en",
            runtime = "kokoro-onnx",
            status = "downloading",
            modelPath = null,
            configPath = voiceDir.absolutePath,
            sizeBytes = null,
            updatedAtMillis = now
        )
        dao.upsertVoiceModel(downloadingEntity)

        return try {
            val modelFile = downloadFile(
                url = KOKORO_MODEL_URL,
                outputFile = File(voiceDir, KOKORO_MODEL_FILE),
                expectedBytes = KOKORO_MODEL_BYTES
            )
            downloadFile(
                url = KOKORO_CONFIG_URL,
                outputFile = File(voiceDir, "config.json"),
                expectedBytes = KOKORO_CONFIG_BYTES
            )
            downloadFile(
                url = KOKORO_VOICE_URL,
                outputFile = File(voiceDir, "af.bin"),
                expectedBytes = KOKORO_VOICE_BYTES
            )

            val entity = installedVoiceFromFiles(modelFile.parentFile ?: voiceDir)
                ?: error("Downloaded voice files are incomplete.")
            dao.upsertVoiceModel(entity)
            entity
        } catch (throwable: Throwable) {
            dao.upsertVoiceModel(downloadingEntity.copy(status = "available", updatedAtMillis = System.currentTimeMillis()))
            throw throwable
        }
    }

    private fun queryDisplayName(uri: Uri): String? {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
        }
    }

    private fun String.ensureOnnxExtension(): String {
        return if (endsWith(".onnx", ignoreCase = true)) this else "$this.onnx"
    }

    private fun installedVoiceFromFiles(voiceDir: File = File(context.filesDir, "voices/$DEFAULT_KOKORO_VOICE_ID")): VoiceModelEntity? {
        val modelFile = File(voiceDir, "model_q8f16.onnx")
        val quantizedModelFile = File(voiceDir, KOKORO_MODEL_FILE)
        val configFile = File(voiceDir, "config.json")
        val voiceFile = File(voiceDir, "af.bin")
        val selectedModelFile = when {
            quantizedModelFile.length() >= KOKORO_MODEL_BYTES -> quantizedModelFile
            modelFile.length() >= LEGACY_KOKORO_Q8F16_MODEL_BYTES -> null
            else -> null
        } ?: return null
        if (configFile.length() < KOKORO_CONFIG_BYTES) return null
        if (voiceFile.length() < KOKORO_VOICE_BYTES) return null
        return VoiceModelEntity(
            id = DEFAULT_KOKORO_VOICE_ID,
            displayName = "Kokoro Natural English",
            language = "en",
            runtime = "kokoro-onnx",
            status = "installed",
            modelPath = selectedModelFile.absolutePath,
            configPath = voiceDir.absolutePath,
            sizeBytes = voiceDir.walkTopDown().filter { it.isFile }.sumOf { it.length() },
            updatedAtMillis = System.currentTimeMillis()
        )
    }

    private fun downloadFile(url: String, outputFile: File, expectedBytes: Long): File {
        if (outputFile.exists() && outputFile.length() >= expectedBytes) {
            return outputFile
        }

        val tempFile = File(outputFile.parentFile, "${outputFile.name}.download")

        repeat(DOWNLOAD_ATTEMPTS) { attempt ->
            var connection: HttpURLConnection? = null
            try {
                val existingBytes = tempFile.length().coerceAtMost(expectedBytes)
                connection = (URL(url).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15_000
                    readTimeout = 20_000
                    instanceFollowRedirects = true
                    if (existingBytes > 0L) {
                        setRequestProperty("Range", "bytes=$existingBytes-")
                    }
                }
                val responseCode = connection.responseCode
                val append = existingBytes > 0L && responseCode == HttpURLConnection.HTTP_PARTIAL
                if (!append && tempFile.exists()) tempFile.delete()

                connection.inputStream.use { input ->
                    FileOutputStream(tempFile, append).use { output ->
                        input.copyTo(output)
                    }
                }

                if (tempFile.length() >= expectedBytes) {
                    if (outputFile.exists()) outputFile.delete()
                    check(tempFile.renameTo(outputFile)) { "Unable to save ${outputFile.name}" }
                    check(outputFile.length() >= expectedBytes) { "Downloaded ${outputFile.name} is incomplete." }
                    return outputFile
                }
            } catch (exception: IOException) {
                if (attempt == DOWNLOAD_ATTEMPTS - 1) throw exception
            } finally {
                connection?.disconnect()
            }
        }

        error("Downloaded ${outputFile.name} is incomplete.")
    }

    companion object {
        const val DEFAULT_KOKORO_VOICE_ID = "kokoro-natural-en"
        private const val DOWNLOAD_ATTEMPTS = 5
        private const val KOKORO_MODEL_FILE = "model_quantized.onnx"
        private const val KOKORO_MODEL_BYTES = 92_361_116L
        private const val LEGACY_KOKORO_Q8F16_MODEL_BYTES = 86_033_585L
        private const val KOKORO_CONFIG_BYTES = 44L
        private const val KOKORO_VOICE_BYTES = 524_288L
        private const val KOKORO_BASE_URL = "https://huggingface.co/onnx-community/Kokoro-82M-v1.0-ONNX/resolve/main"
        private const val KOKORO_MODEL_URL = "$KOKORO_BASE_URL/onnx/$KOKORO_MODEL_FILE"
        private const val KOKORO_CONFIG_URL = "$KOKORO_BASE_URL/config.json"
        private const val KOKORO_VOICE_URL = "$KOKORO_BASE_URL/voices/af.bin"
    }
}
```

### ./core/audio/src/main/java/com/auralis/audio/KokoroEnglishTokenizer.kt

```
package com.auralis.audio

import java.util.Locale

internal class KokoroEnglishTokenizer {
    fun tokenize(text: String): LongArray {
        val phonemes = phonemize(text)
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
        val sentences = text
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
```

### ./core/audio/src/main/java/com/auralis/audio/OnnxNaturalTtsEngine.kt

```
package com.auralis.audio

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import com.auralis.database.VoiceModelEntity
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.LongBuffer
import java.security.MessageDigest

class OnnxNaturalTtsEngine : LocalNeuralTtsEngine {
    private val tokenizer = KokoroEnglishTokenizer()
    private val environment: OrtEnvironment by lazy { OrtEnvironment.getEnvironment() }
    private var sessionPath: String? = null
    private var session: OrtSession? = null
    private var stylePack: StylePack? = null

    override suspend fun render(
        request: NarrationSegmentRequest,
        voiceModel: VoiceModelEntity,
        outputDirectory: String
    ): RenderedAudioSegment {
        val modelPath = voiceModel.modelPath ?: throw VoiceRuntimeFailure.MissingVoiceModel()
        val modelFile = File(modelPath)
        if (!modelFile.exists() || voiceModel.status != "installed") {
            throw VoiceRuntimeFailure.MissingVoiceModel()
        }

        val chunks = tokenizer.splitForModel(request.text)
        val samples = mutableListOf<FloatArray>()
        chunks.forEachIndexed { index, textChunk ->
            samples += synthesizeChunk(textChunk, modelFile, voiceModel)
            if (index != chunks.lastIndex) {
                samples += FloatArray((SAMPLE_RATE * SILENCE_BETWEEN_CHUNKS_SECONDS).toInt())
            }
        }

        val mergedSamples = samples.concat()
        if (mergedSamples.isEmpty()) {
            throw VoiceRuntimeFailure.SynthesisFailed("Kokoro synthesis produced no audio.")
        }

        val outputFile = File(outputDirectory, "${request.id}.wav")
        PcmWavWriter.writeMono16(outputFile, mergedSamples, SAMPLE_RATE)
        return RenderedAudioSegment(
            filePath = outputFile.absolutePath,
            durationMillis = (mergedSamples.size * 1000L) / SAMPLE_RATE,
            checksum = checksum(outputFile)
        )
    }

    private fun synthesizeChunk(
        text: String,
        modelFile: File,
        voiceModel: VoiceModelEntity
    ): FloatArray {
        val tokenIds = tokenizer.tokenize(text)
        val tokenCount = (tokenIds.size - 2).coerceAtLeast(0)
        val style = loadStyleVector(voiceModel, tokenCount)
        val currentSession = getSession(modelFile)
        val inputTensor = OnnxTensor.createTensor(
            environment,
            LongBuffer.wrap(tokenIds),
            longArrayOf(1L, tokenIds.size.toLong())
        )
        val styleTensor = OnnxTensor.createTensor(
            environment,
            FloatBuffer.wrap(style),
            longArrayOf(1L, KOKORO_STYLE_WIDTH.toLong())
        )
        val speedTensor = OnnxTensor.createTensor(
            environment,
            FloatBuffer.wrap(floatArrayOf(1f)),
            longArrayOf(1L)
        )
        return try {
            currentSession.run(
                mapOf(
                    INPUT_IDS to inputTensor,
                    STYLE to styleTensor,
                    SPEED to speedTensor
                )
            ).use { result ->
                val audioTensor = result.get(0) as? OnnxTensor
                    ?: throw VoiceRuntimeFailure.SynthesisFailed("Kokoro synthesis returned an unsupported output tensor.")
                val buffer = audioTensor.floatBuffer
                buffer.rewind()
                FloatArray(buffer.remaining()).also { buffer.get(it) }
            }
        } catch (failure: VoiceRuntimeFailure) {
            throw failure
        } catch (throwable: Throwable) {
            throw VoiceRuntimeFailure.SynthesisFailed("Kokoro synthesis failed: ${throwable.message ?: throwable.javaClass.simpleName}")
        } finally {
            inputTensor.close()
            styleTensor.close()
            speedTensor.close()
        }
    }

    private fun getSession(modelFile: File): OrtSession {
        val path = modelFile.absolutePath
        val existing = session
        if (existing != null && sessionPath == path) return existing

        session?.close()
        return try {
            environment.createSession(path).also {
                if (it.inputNames.isEmpty() || it.outputNames.isEmpty()) {
                    it.close()
                    throw VoiceRuntimeFailure.UnsupportedVoicePack()
                }
                session = it
                sessionPath = path
            }
        } catch (failure: VoiceRuntimeFailure) {
            throw failure
        } catch (_: Throwable) {
            throw VoiceRuntimeFailure.UnsupportedVoicePack()
        }
    }

    private fun loadStyleVector(voiceModel: VoiceModelEntity, tokenCount: Int): FloatArray {
        val voiceDirectory = voiceModel.configPath?.let(::File)
            ?: voiceModel.modelPath?.let { File(it).parentFile }
            ?: throw VoiceRuntimeFailure.MissingVoiceModel()
        val styleFile = File(voiceDirectory, "af.bin")
        if (!styleFile.exists()) throw VoiceRuntimeFailure.MissingVoiceModel()

        val pack = stylePack?.takeIf { it.path == styleFile.absolutePath } ?: readStylePack(styleFile).also {
            stylePack = it
        }
        val index = tokenCount.coerceIn(0, pack.vectorCount - 1)
        return pack.values.copyOfRange(index * KOKORO_STYLE_WIDTH, (index + 1) * KOKORO_STYLE_WIDTH)
    }

    private fun readStylePack(styleFile: File): StylePack {
        val bytes = styleFile.readBytes()
        val floats = FloatArray(bytes.size / Float.SIZE_BYTES)
        ByteBuffer.wrap(bytes)
            .order(ByteOrder.LITTLE_ENDIAN)
            .asFloatBuffer()
            .get(floats)
        val vectorCount = floats.size / KOKORO_STYLE_WIDTH
        if (vectorCount <= 0) throw VoiceRuntimeFailure.MissingVoiceModel()
        return StylePack(styleFile.absolutePath, floats, vectorCount)
    }

    private fun List<FloatArray>.concat(): FloatArray {
        val totalSize = sumOf { it.size }
        val output = FloatArray(totalSize)
        var cursor = 0
        forEach { chunk ->
            chunk.copyInto(output, cursor)
            cursor += chunk.size
        }
        return output
    }

    private fun checksum(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val read = input.read(buffer)
                if (read <= 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private data class StylePack(
        val path: String,
        val values: FloatArray,
        val vectorCount: Int
    )

    companion object {
        private const val INPUT_IDS = "input_ids"
        private const val STYLE = "style"
        private const val SPEED = "speed"
        private const val SAMPLE_RATE = 24_000
        private const val KOKORO_STYLE_WIDTH = 256
        private const val SILENCE_BETWEEN_CHUNKS_SECONDS = 0.18f
    }
}
```

### ./core/audio/src/main/java/com/auralis/audio/PcmWavWriter.kt

```
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
```

### ./scripts/install-debug.sh

```
#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
# shellcheck source=android-tools.sh
source "${SCRIPT_DIR}/android-tools.sh"
# shellcheck source=java-env.sh
source "${SCRIPT_DIR}/java-env.sh"

ADB="$(android_tool adb)"
"${ROOT_DIR}/gradlew" -p "${ROOT_DIR}" :app:assembleDebug
"${ADB}" install -r "${ROOT_DIR}/app/build/outputs/apk/debug/app-debug.apk"
"${ADB}" shell monkey -p com.auralis.reader 1
```

### ./scripts/java-env.sh

```
#!/usr/bin/env bash
set -euo pipefail

if [[ -n "${JAVA_HOME:-}" && -x "${JAVA_HOME}/bin/java" ]]; then
  export JAVA_HOME
  export PATH="${JAVA_HOME}/bin:${PATH}"
  return
fi

for candidate in "/usr/opt/android-studio/jbr" "/opt/android-studio/jbr" "$HOME/.jdks"/*; do
  if [[ -x "${candidate}/bin/java" ]]; then
    export JAVA_HOME="${candidate}"
    export PATH="${JAVA_HOME}/bin:${PATH}"
    return
  fi
done

echo "JDK not found. Install a JDK or set JAVA_HOME." >&2
exit 1
```

### ./scripts/logcat.sh

```
#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=android-tools.sh
source "${SCRIPT_DIR}/android-tools.sh"

"$(android_tool adb)" logcat -v time | grep --line-buffered -E "Auralis|AndroidRuntime|WorkManager|ONNX|PdfBox"
```

### ./scripts/android-tools.sh

```
#!/usr/bin/env bash
set -euo pipefail

find_android_sdk() {
  if [[ -n "${ANDROID_HOME:-}" && -d "${ANDROID_HOME}" ]]; then
    printf '%s\n' "${ANDROID_HOME}"
    return
  fi
  if [[ -n "${ANDROID_SDK_ROOT:-}" && -d "${ANDROID_SDK_ROOT}" ]]; then
    printf '%s\n' "${ANDROID_SDK_ROOT}"
    return
  fi
  for candidate in "$HOME/Android/Sdk" "$HOME/Library/Android/sdk" "/opt/android-sdk"; do
    if [[ -d "${candidate}" ]]; then
      printf '%s\n' "${candidate}"
      return
    fi
  done
  echo "Android SDK not found. Set ANDROID_HOME or ANDROID_SDK_ROOT." >&2
  exit 1
}

android_tool() {
  local sdk tool
  sdk="$(find_android_sdk)"
  tool="$1"
  case "${tool}" in
    adb) printf '%s\n' "${sdk}/platform-tools/adb" ;;
    emulator) printf '%s\n' "${sdk}/emulator/emulator" ;;
    avdmanager) printf '%s\n' "${sdk}/cmdline-tools/latest/bin/avdmanager" ;;
    *) echo "Unknown Android tool: ${tool}" >&2; exit 1 ;;
  esac
}
```

### ./scripts/run-avd.sh

```
#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=android-tools.sh
source "${SCRIPT_DIR}/android-tools.sh"

AVD_NAME="${1:-Medium_Phone}"
EMULATOR="$(android_tool emulator)"
ADB="$(android_tool adb)"

if "${ADB}" devices | grep -q "emulator-.*device"; then
  echo "An emulator is already running."
  exit 0
fi

"${EMULATOR}" -avd "${AVD_NAME}" -netdelay none -netspeed full >/tmp/auralis-emulator.log 2>&1 &
echo "Starting AVD ${AVD_NAME}. Logs: /tmp/auralis-emulator.log"
"${ADB}" wait-for-device
"${ADB}" shell getprop sys.boot_completed | grep -m 1 "1" >/dev/null || {
  until [[ "$("${ADB}" shell getprop sys.boot_completed | tr -d '\r')" == "1" ]]; do
    sleep 2
  done
}
echo "AVD ${AVD_NAME} is ready."
```

### ./settings.gradle.kts

```
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Auralis"
include(":app")
include(":core:database")
include(":core:reader")
include(":core:ai")
include(":core:audio")
include(":core:jobs")
```

### ./README.md

```
# Auralis

Auralis is a greenfield Android reader prototype for text PDFs and EPUBs with a local-first audiobook pipeline. It uses native Kotlin, Jetpack Compose, Room, WorkManager, PDFBox Android, Media3, and ONNX Runtime Mobile.

## Current MVP

- Imports EPUB and selectable-text PDF files through Android's document picker.
- Copies books into app-private storage and stores extracted chapters as text files.
- Persists books, chapters, metadata, reading state, notes, voice models, audiobook jobs, and audio segments in Room.
- Runs local heuristic book analysis now, with an on-device LLM runtime boundary in `core:ai`.
- Refuses to use Android system TTS for audiobook generation.
- Supports ONNX voice-pack installation records and a neural-only audio engine boundary for Kokoro-style voices.
- Schedules audiobook generation through WorkManager and records `waiting_for_voice` when no natural voice is installed.

## Build

This shell does not currently expose Java on `PATH`, so use Android Studio's bundled JDK:

```bash
JAVA_HOME=/usr/opt/android-studio/jbr PATH=/usr/opt/android-studio/jbr/bin:$PATH ./gradlew :app:assembleDebug
```

The debug build filters native libraries to `x86_64` by default so it installs cleanly on the local `Medium_Phone` AVD. Build for an ARM64 phone with:

```bash
JAVA_HOME=/usr/opt/android-studio/jbr PATH=/usr/opt/android-studio/jbr/bin:$PATH ./gradlew :app:assembleDebug -Pauralis.abiFilters=arm64-v8a
```

## AVD

```bash
./scripts/run-avd.sh Medium_Phone
./scripts/install-debug.sh
./scripts/logcat.sh
```

Verified locally on `Medium_Phone` (`emulator-5554`) with `app-debug.apk` installed and launched.

## Next Engineering Step

The natural TTS runtime is intentionally not a fake speech generator. The remaining production work is the Kokoro/Sherpa ONNX adapter: tokenizer/phonemizer inputs, speaker embedding selection, waveform output, and voice manifest download URLs.
# auralis
```

### ./gradle/libs.versions.toml

```
[versions]
agp = "9.2.1"
kotlin = "2.2.10"
coreKtx = "1.18.0"
activityCompose = "1.10.1"
composeBom = "2026.03.00"
material3 = "1.4.0"
lifecycle = "2.10.0"
room = "2.8.4"
work = "2.11.2"
media3 = "1.10.1"
onnxRuntime = "1.26.0"
pdfboxAndroid = "2.0.27.0"
junit = "4.13.2"
androidxJunit = "1.3.0"
espresso = "3.7.0"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycle" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }

androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-compose-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-compose-ui-test-junit4 = { group = "androidx.compose.ui", name = "ui-test-junit4" }
androidx-compose-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest" }
androidx-compose-material3 = { group = "androidx.compose.material3", name = "material3", version.ref = "material3" }
androidx-compose-material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended" }

androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
androidx-work-runtime-ktx = { group = "androidx.work", name = "work-runtime-ktx", version.ref = "work" }

androidx-media3-exoplayer = { group = "androidx.media3", name = "media3-exoplayer", version.ref = "media3" }
androidx-media3-ui = { group = "androidx.media3", name = "media3-ui", version.ref = "media3" }
onnxruntime-android = { group = "com.microsoft.onnxruntime", name = "onnxruntime-android", version.ref = "onnxRuntime" }
pdfbox-android = { group = "com.tom-roush", name = "pdfbox-android", version.ref = "pdfboxAndroid" }

junit = { group = "junit", name = "junit", version.ref = "junit" }
androidx-junit = { group = "androidx.test.ext", name = "junit", version.ref = "androidxJunit" }
androidx-espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espresso" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
android-library = { id = "com.android.library", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-kapt = { id = "org.jetbrains.kotlin.kapt", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
```
