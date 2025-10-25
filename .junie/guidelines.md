NoMercy Android App – Development Guidelines

Audience: Senior Android/Kotlin developers working on this repository.

1. Build and configuration
- Toolchain
  - JDK: 17 (Gradle and Android toolchain configured for Java 17).
  - Kotlin: Android plugin with kotlinx-serialization; Compose Compiler 1.5.4.
  - Android SDK: compileSdk 36, targetSdk 34, minSdk 31.
  - Gradle: use the provided wrapper (gradlew/gradlew.bat). Do not change the Gradle version unless coordinated.
- Modules
  - Single Android application module at app with namespace tv.nomercy.app.
- Key plugins
  - com.android.application, org.jetbrains.kotlin.android, org.jetbrains.kotlin.plugin.serialization, org.jetbrains.compose.compiler.
- Dependencies of note
  - Jetpack Compose (Material 3, Navigation, Foundation), Coroutines, Retrofit + Gson + Kotlinx Serialization converter, OkHttp (with logging), AppAuth (OIDC/OAuth2), AndroidX Security Crypto, DataStore (Preferences), Paging Compose, Room KTX (present, no Room schema dir committed), Media3 (androidx.media), Coil v3 (including Ktor/OkHttp network and SVG support), custom composemeshgradient.
- Build features/options
  - Compose enabled; buildConfig enabled.
  - Packaging excludes META-INF AL2.0, LGPL2.1 to prevent duplicate license merge issues.
  - Resolution strategy pins Dagger to 2.48 to avoid transitive version drift.
- Signing/Keystore
  - Not configured in this repo (debug builds only). To create a release, wire a signingConfig and secrets via local.properties or environment/CI variables.
- Build commands (Windows PowerShell)
  - Clean: .\gradlew.bat clean
  - Assemble debug: .\gradlew.bat :app:assembleDebug
  - Run unit tests: .\gradlew.bat :app:testDebugUnitTest
  - Lint (Compose + Android Lint): .\gradlew.bat :app:lint
  - Static checks spot-bugs/detekt are not configured at present.
- IDE setup
  - Android Studio Giraffe/Koala+ recommended with JDK 17.
  - Enable Kotlin code style: Kotlin official; set JVM target 17 for inspections.

2. Testing
- Frameworks/deps
  - Unit tests: JUnit 4 (testImplementation junit), run on the JVM.
  - Instrumented tests: AndroidX JUnit, Espresso, Compose UI Test (androidTestImplementation). Requires an emulator/device with API 31+.
  - Compose testing: ui-test-junit4 for androidTest; ui-test-manifest used in debug for tooling.
- Locations
  - Unit tests: app/src/test/java
  - Instrumented tests: app/src/androidTest/java
- How to run
  - All unit tests: .\gradlew.bat :app:testDebugUnitTest
  - Single unit test class: .\gradlew.bat :app:testDebugUnitTest --tests "tv.nomercy.app.ExampleUnitTest"
  - Single unit test method: .\gradlew.bat :app:testDebugUnitTest --tests "tv.nomercy.app.ExampleUnitTest.addition_isCorrect"
  - All instrumented tests (device/emulator required): .\gradlew.bat :app:connectedDebugAndroidTest
- Adding new unit tests
  - Create a file under app/src/test/java with package tv.nomercy.app (or the package under test). Keep business logic free of Android framework types when possible to keep tests JVM-only.
  - Prefer small, deterministic tests. For coroutines, use kotlinx-coroutines-test; not added yet—add as needed and align versions.
- Adding new Compose UI tests
  - Place in app/src/androidTest/java. Use createAndroidComposeRule and @RunWith(AndroidJUnit4::class). Remember to use testTags in composables under test.
- Demonstrated test run (verified locally during guideline creation)
  - Existing unit test executed successfully: tv.nomercy.app.ExampleUnitTest::addition_isCorrect passed.

3. Development notes and conventions
- Kotlin/Compose style
  - Use Kotlin official code style; keep composables small and previewable. Prefer state hoisting and immutable data where possible.
  - Use Material3 theme and tokens from MaterialTheme; avoid hardcoding colors/typography.
  - For performance, prefer remember/derivedStateOf; avoid heavy work in composable bodies.
- Navigation and ViewModels
  - Compose Navigation is present; ViewModels via androidx.lifecycle.viewmodel.compose. Share state via stores where appropriate; avoid passing NavController deep into leaf composables.
- Networking and serialization
  - Retrofit configured with Gson and a Kotlinx Serialization converter alongside—avoid mixing in the same service unless intentional. Choose one per API surface to prevent ambiguous converter selection ordering.
  - OkHttp logging interceptor is included; disable or lower level for release.
- Auth
  - AppAuth present for OAuth2/OIDC; token storage via AndroidX Security Crypto + DataStore. Be cautious with context lifecycles when initiating flows.
- Media/Images
  - Coil v3 is used (compose + network modules, SVG). Prefer AsyncImage (coil3) for non-TMDB assets; TMDBImage is a custom component for TMDB paths.
- Testing focus areas
  - Unit-test pure business logic in shared stores and utilities (e.g., aspectFromType, AppConfigStore transformations).
  - UI testing for TV: validate D-pad focus and navigation using Compose testing and tv-specific testTags.
- CI
  - There is a GitHub Actions workflow for releases. Add a CI job to run :app:testDebugUnitTest on PRs if stability is needed.

4. Common pitfalls and troubleshooting
- JDK mismatch: Ensure JAVA_HOME points to JDK 17; Gradle will fail during Kotlin/Javac tasks otherwise.
- SDK mismatch: compileSdk 36 requires recent Android SDK/Build Tools in your local environment.
- Compose compiler: Keep Compose BOM and compiler compatible; currently compiler 1.5.4 is aligned with Kotlin 1.9.x toolchain.
- Duplicate converter factories: If both Gson and Kotlinx Serialization converters are on the same Retrofit instance, order matters; register only one unless you need both.
- Media3 permissions: If adding playback features, validate foreground service/media permissions on API 34+.

5. Quickstart snippets
- Build debug
  - .\gradlew.bat :app:assembleDebug
- Run unit tests
  - .\gradlew.bat :app:testDebugUnitTest
- Create a new unit test
  - File: app/src/test/java/tv/nomercy/app/MyFeatureTest.kt
    package tv.nomercy.app
    import org.junit.Test
    import org.junit.Assert.assertEquals
    class MyFeatureTest {
        @Test fun math_works() { assertEquals(9, 4 + 5) }
    }
  - Run: .\gradlew.bat :app:testDebugUnitTest --tests "tv.nomercy.app.MyFeatureTest.math_works"

Notes
- These guidelines intentionally omit basic Android/Gradle usage; they focus on repo-specific setup and conventions.
