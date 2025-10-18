# NoMercy TV - Native Android App Development Plan

## 📋 Complete App Analysis for Android Development

### 🏗️ App Architecture Overview

**Core App Type:** Media streaming/management platform with multi-device support
- **Primary Function:** Video/music library management, streaming, and encoding
- **Target Devices:** Phone, Tablet, Android TV, Desktop
- **Authentication:** Keycloak-based with JWT tokens
- **Backend:** RESTful API with WebSocket real-time features

### 🗺️ Navigation Structure & Routes

Your app has **7 main route groups** with **50+ pages**:

#### 1. Main Media Routes (`/`)
- **Home dashboard** - Main landing page with content recommendations
- **Libraries** - Movies, TV shows, music organization
- **Search functionality** - Global search across all content types
- **Individual content pages** - `movie/:id`, `tv/:id` with detailed information
- **Watch/player pages** - Video/audio playback interfaces
- **Collections, genres, people pages** - Content categorization and discovery

#### 2. Music Section (`/music`)
- **Artists, albums, tracks** - Music library organization
- **Playlists and genres** - Music categorization
- **Music player interface** - Audio playback controls

#### 3. Admin Dashboard (`/dashboard`)
- **System management** - Users, libraries, general settings
- **Content management** - Metadata, ripper, specials
- **Device management** - DLNA, activity monitoring
- **Advanced features** - Logs, plugins, scheduled tasks

#### 4. User Preferences (`/preferences`)
- **Display settings** - UI customization
- **Profile management** - User account settings
- **Controls configuration** - Input and interaction preferences
- **Subtitles settings** - Text overlay preferences

#### 5. Developer Tools (`/dev`)
- **Cast functionality** - Chromecast/AirPlay features
- **Download management** - Offline content handling

#### 6. Setup Wizard (`/setup`)
- **Server selection** - Initial configuration
- **Post-install configuration** - Library setup and optimization

#### 7. Authentication (`/auth`, `/logout`)
- **Keycloak integration** - SSO authentication flow
- **Logout handling** - Session management

### 🎨 UI/UX Architecture

#### Multi-Layout System
Your app uses **3 distinct layouts** with device-specific optimizations:

**Mobile Layout (`src/Layout/Mobile/`):**
- Touch-optimized interface with bottom navigation
- Side flyouts for additional navigation
- Portrait/landscape orientation support
- Swipe gestures and pull-to-refresh
- Components: NavBar, BottomBar, SideFlyout, Modal

**TV Layout (`src/Layout/Tv/`):**
- D-pad navigation optimized for 10-foot UI
- Simplified interface with larger touch targets
- Focus management for remote control navigation
- Landscape-only orientation
- Components: NavBar, NavBarButton

**Desktop Layout (`src/Layout/Desktop/`):**
- Traditional desktop UI with sidebars
- Complex navigation with multiple panels
- Mouse and keyboard interaction patterns
- Components: Navbar, DashboardLayout

#### Key UI Components Identified
- **Responsive grid systems** for content cards
- **Video/music players** with custom controls
- **Modal systems** for detailed views
- **Toast notifications** for user feedback
- **Screensaver functionality** for TV platform
- **Image optimization** and lazy loading

### 🔧 Core Functionality

#### Media Management Features
- **Video streaming** with subtitle support and multiple quality options
- **Music playback** with playlists and queue management
- **Content encoding/ripping** for media processing
- **Metadata management** with external API integration
- **Library organization** with categorization and search

#### Real-time Features
- **WebSocket connections** for live updates and synchronization
- **Progress tracking** across devices and sessions
- **Notification system** for system events and user alerts
- **Device activity monitoring** for multi-user environments

#### Platform-Specific Features
**TV Platform:**
- Remote control navigation with D-pad support
- Screensaver activation during inactivity
- Immersive fullscreen media playback

**Mobile Platform:**
- Touch gestures for navigation and control
- Portrait/landscape mode adaptation
- Haptic feedback integration

**Cross-platform:**
- Responsive design adaptation
- Device detection and feature toggling
- Synchronized playback state

### 📊 State Management Architecture

Your app uses **20+ reactive stores** managing:

#### Core Application State
- **User authentication** (`src/store/user.ts`) - JWT tokens, user profile, permissions
- **Server connections** (`src/store/servers.ts`) - Available servers, connection status
- **Libraries management** (`src/store/Libraries.ts`) - Content organization, metadata

#### Media Playback State
- **Video player** (`src/store/videoPlayer.ts`) - Playback controls, progress, quality
- **Music playlists** (`src/store/musicPlaylists.ts`) - Queue management, shuffle, repeat
- **Video/music sockets** (`src/store/videoSocket.ts`, `src/store/musicSocket.ts`) - Real-time sync

#### UI and User Experience
- **UI preferences** (`src/store/ui.ts`) - Theme, layout preferences, accessibility
- **Search functionality** (`src/store/search.ts`) - Query history, filters, results
- **Notifications** (`src/store/notifications.ts`) - System alerts, user messages
- **Screensaver** (`src/store/screensaver.ts`) - Inactivity detection, activation

#### System Management
- **Route state** (`src/store/routeState.ts`) - Navigation history, deep linking
- **Preferences** (`src/store/preferences.ts`) - User settings, customization
- **Ripper** (`src/store/ripper.ts`) - Content processing status
- **Indexer** (`src/store/indexer.ts`) - Content scanning and organization

### 🔌 External Integrations

#### Authentication Services
- **Keycloak** - Primary authentication service with SSO support
- **JWT token management** - Secure session handling

#### Media Metadata
All metadata is provided by the user's NoMercyMediaServer. The Android app does not integrate directly with third‑party metadata providers; any enrichment happens server‑side and is consumed as plain JSON by the app.

#### Platform Integration
- **Capacitor Plugins** - Device detection, status bar, navigation controls
- **WebSocket connections** - Real-time server communication
- **PWA features** - Service workers, offline caching, installation

### 🎯 Key User Flows

#### 0. First‑run Flow (authoritative order)
```
Authentication (Keycloak Login) → Token Validation → Domain API: Fetch accessible servers → User selects a server → Fetch server config + user libraries → Initialize app (stores, sockets, image base, feature flags) → Load Home → Free navigation
```

#### 1. Authentication Flow (detail)
```
Keycloak Login → Token Validation/Refresh bootstrap → Proceed to Domain API: Servers
```

#### 2. Server Selection & Initialization Flow
```
Domain API: Get servers for user → User selects server → Fetch libraries + server capabilities/config → Warm caches (optional) → Navigate to Home
```

#### 3. Content Discovery Flow
```
Browse Libraries → Apply Filters → Search Content → View Details → Initiate Playback
```

#### 4. Media Playback Flow
```
Select Content → Quality Selection → Player Interface → Playback Controls → Progress Sync
```

#### 5. Admin Management Flow
```
Dashboard Access → System Configuration → Content Management → User Administration
```

## 📱 Native Android App Development Strategy

### **Phase 1: Foundation Architecture**
**Tech Stack Selection:**
- **Language:** Kotlin with Coroutines
- **UI Framework:** Jetpack Compose + Material Design 3
- **Architecture:** MVVM + Repository pattern with Clean Architecture layers
- **Database:** Room for local caching and offline support
- **Networking:** Retrofit + OkHttp with WebSocket support (OkHttp WebSocket)
- **Dependency Injection:** Dagger Hilt
- **Image Loading:** Coil with custom caching
- **Media Playback:** LibVLC (org.videolan.libvlc) with Compose UI controls

### **Phase 2: Authentication System**
**Implementation Requirements:**
- Keycloak Android SDK integration for SSO
- JWT token management with automatic refresh
- Post-auth server discovery and selection UI (after successful login)
- Biometric authentication support (fingerprint/face)
- Session persistence and security

### **Phase 3: Multi-Device UI Implementation**

#### Phone UI Requirements
- **Bottom Navigation:** Primary navigation with 5 tabs (Home, Libraries, Search, Music, Profile)
- **Swipe Gestures:** Pull-to-refresh, swipe-to-dismiss
- **Touch Optimization:** Large touch targets, gesture navigation
- **Adaptive Layout:** Portrait/landscape orientation support
- **Material Design:** Following Android design guidelines

#### TV UI Requirements
- **Leanback Library:** Android TV framework integration
- **D-pad Navigation:** Focus management for remote control
- **10-foot Interface:** Large text, simplified navigation
- **TV Launcher Integration:** Proper banner, categories, recommendations
- **Voice Search:** Integration with Android TV voice commands

#### Shared Components
- **Media Cards:** Poster/banner display with metadata
- **Video/Audio Players:** Custom controls with LibVLC
- **Modal Dialogs:** Information panels and settings screens
- **Search Interface:** Global search with filters and suggestions

### **Phase 4: Media Engine Implementation**

#### Video Playback System
- **LibVLC Integration:** Primary playback engine with wide codec support
- **Subtitle Support:** Multiple formats (SRT, VTT, ASS)
- **Quality Selection:** Adaptive bitrate streaming
- **Background Service:** Continue playback when app minimized
- **Picture-in-Picture:** Android PiP support for phones
- **App-to-App Casting:** Mobile controls TV via our custom protocol (no Google Cast)

#### Audio Playback System
- **LibVLC Audio:** Background audio playback service
- **Queue Management:** Playlist handling and shuffle/repeat
- **Media Session:** System media controls integration
- **Notification Controls:** Lock screen and notification panel controls
- **Bluetooth Integration:** Car stereo and headphone controls

### **Phase 5: Real-time Features**

#### WebSocket Implementation
- **OkHttp WebSocket:** Real-time server communication
- **Event Handling:** Progress sync, notifications, live updates
- **Connection Management:** Automatic reconnection and error handling
- **Background Sync:** Maintain connection during app backgrounding

#### Notification System
- **Firebase Cloud Messaging:** Push notifications for system events
- **Local Notifications:** Progress updates, download completion
- **Channel Management:** Categorized notification types
- **Action Buttons:** Quick actions from notifications

### **Phase 6: Data Management**

#### Local Database (Room)
```text
// Core entities based on your store structure
// Example pseudo-Kotlin entity names; illustrative only.
@Entity data class User(...)
@Entity data class Library(...)
@Entity data class MediaItem(...)
@Entity data class Playlist(...)
@Entity data class DownloadItem(...)
```

#### Caching Strategy
- **Image Caching:** Coil with custom cache policies matching your PWA strategy
- **API Response Caching:** Room database with TTL
- **Offline Support:** Downloaded content for offline viewing
- **Sync Strategy:** Background sync when network available

### **Phase 7: Platform-Specific Features**

#### Android TV Exclusive Features
- **Android TV Launcher:** Proper integration with TV home screen
- **Recommendations:** Content suggestions on TV launcher
- **Voice Search:** "OK Google, search for movies on NoMercy"
- **TV Input Framework:** Integration as a TV input source
- **HDMI-CEC:** TV remote control integration

#### Phone Exclusive Features
- **Adaptive Brightness:** Automatic brightness during video playback
- **Battery Optimization:** Efficient background processing
- **Share Integration:** Android's share sheet for content sharing
- **Widget Support:** Home screen widgets for quick access
- **Shortcuts:** Dynamic shortcuts for recent content

#### Shared Features
- **Cast Support:** Chromecast and Android TV casting
- **Deep Linking:** Handle nomercy:// URLs
- **Auto-Backup:** User preferences and watch history
- **Multi-User:** Profile switching and parental controls

### **Phase 8: Development Workflow**

#### Project Structure
```
app/
├── src/main/java/tv/nomercy/app/
│   ├── views/
│   │   ├── base/
│   │   │   └── library/
│   │   │       ├── mobile/      # Phone composables for Library page
│   │   │       ├── tv/          # TV composables for Library page
│   │   │       ├── tablet/      # Tablet composables (later)
│   │   │       └── shared/      # ViewModel, contracts, page models
│   │   ├── music/
│   │   │   └── cards/
│   │   │       ├── mobile/
│   │   │       ├── tv/
│   │   │       ├── tablet/
│   │   │       └── shared/
│   │   └── dashboard/
│   │       └── home/
│   │           ├── mobile/
│   │           ├── tv/
│   │           ├── tablet/
│   │           └── shared/
│   ├── platform/
│   │   ├── mobile/      # Activities, NavHost, scaffolds for phone
│   │   └── tv/          # Activities, NavHost, scaffolds for TV
│   ├── core/
│   │   ├── data/        # DTOs, Retrofit services, repositories
│   │   ├── domain/      # Use-cases and pure Kotlin logic
│   │   ├── ui/          # Reusable UI atoms/molecules (no ViewModels)
│   │   └── util/        # Helpers and extensions
│   └── shared/          # Truly generic components used across pages
├── src/tv/                  # TV-specific resources
└── src/phone/               # Phone-specific resources
```

Folder nesting guidance to keep mental overhead low:
- Default: Place shared page logic under views/<category>/<page>/shared (co-located with the page).
- If a category has many tiny pages, you may move shared logic up one level to views/<category>/shared and keep per-page UI under mobile/tv/tablet.
- Rule of three: avoid going deeper than category/page/shared; prefer flatter models and contracts.
- Shared UI atoms that are not page-specific live in core/ui or shared/components and must not import ViewModels; accept state + callbacks.

#### Build Configuration
- **Product Flavors:** Separate phone and TV builds
- **Build Types:** Debug, Release, Beta
- **Signing Configuration:** Production signing keys
- **Proguard/R8:** Code obfuscation and optimization

### **Phase 9: Testing Strategy**

#### Unit Testing
- **Repository Layer:** Data access and caching logic
- **Use Cases:** Business logic validation
- **ViewModels:** UI state management

#### Integration Testing
- **API Integration:** Network requests and responses
- **Database Operations:** Room database interactions
- **Media Playback:** LibVLC integration

#### UI Testing
- **Compose Testing:** UI component validation
- **TV Navigation:** D-pad navigation testing
- **Phone Gestures:** Touch interaction testing

### **Phase 10: Deployment Strategy**

#### Google Play Store
- **App Bundle:** Optimized delivery with dynamic features
- **Device Targeting:** Separate phone and TV listings
- **Beta Testing:** Internal testing track for QA
- **Staged Rollout:** Gradual release to production

#### CI/CD Pipeline
- **GitHub Actions:** Automated building and testing
- **Fastlane:** Release automation
- **Firebase Distribution:** Beta release distribution
- **Crash Reporting:** Firebase Crashlytics integration

## 🎯 Migration Mapping

### Component Translation Guide

| Vue Component | Android Equivalent | Implementation |
|---------------|-------------------|----------------|
| `src/Layout/Mobile/` | Phone Activity/Fragments | Jetpack Compose screens |
| `src/Layout/Tv/` | Leanback Fragments | TV-specific Compose UI |
| `src/views/Base/Watch/` | Video Player Activity | LibVLC + Compose UI |
| `src/store/` modules | Repository + ViewModel | Room + Retrofit + StateFlow |
| Router navigation | Navigation Component | Jetpack Navigation Compose |
| WebSocket clients | OkHttp WebSocket | Kotlin Coroutines + Flow |

### API Integration Mapping

| Current Service | Android Implementation |
|-----------------|----------------------|
| `src/lib/clients/apiClient.ts` | Retrofit interface with Hilt injection |
| `src/lib/clients/serverClient.ts` | Repository pattern with Room caching |
| `src/lib/auth/` | Keycloak Android SDK + secure storage |
| WebSocket services | OkHttp WebSocket + StateFlow |

This comprehensive plan provides the complete roadmap for recreating your NoMercy TV Ionic Vue app as a native Android application while maintaining all functionality and improving performance through native implementation.

# NoMercy TV - Native Android App Development Plan

## 📋 Complete App Analysis for Android Development

### 🏗️ App Architecture Overview

**Core App Type:** Media streaming/management platform with multi-device support
- **Primary Function:** Video/music library management, streaming, and encoding
- **Target Devices:** Phone, Tablet, Android TV, Desktop
- **Authentication:** Keycloak-based with JWT tokens
- **Backend:** RESTful API with WebSocket real-time features

### 🗺️ Navigation Structure & Routes

Your app has **7 main route groups** with **50+ pages**:

#### 1. Main Media Routes (`/`)
- **Home dashboard** - Main landing page with content recommendations
- **Libraries** - Movies, TV shows, music organization
- **Search functionality** - Global search across all content types
- **Individual content pages** - `movie/:id`, `tv/:id` with detailed information
- **Watch/player pages** - Video/audio playback interfaces
- **Collections, genres, people pages** - Content categorization and discovery

#### 2. Music Section (`/music`)
- **Artists, albums, tracks** - Music library organization
- **Playlists and genres** - Music categorization
- **Music player interface** - Audio playback controls

#### 3. Admin Dashboard (`/dashboard`)
- **System management** - Users, libraries, general settings
- **Content management** - Metadata, ripper, specials
- **Device management** - DLNA, activity monitoring
- **Advanced features** - Logs, plugins, scheduled tasks

#### 4. User Preferences (`/preferences`)
- **Display settings** - UI customization
- **Profile management** - User account settings
- **Controls configuration** - Input and interaction preferences
- **Subtitles settings** - Text overlay preferences

#### 5. Developer Tools (`/dev`)
- **Cast functionality** - Chromecast/AirPlay features
- **Download management** - Offline content handling

#### 6. Setup Wizard (`/setup`)
- **Server selection** - Initial configuration
- **Post-install configuration** - Library setup and optimization

#### 7. Authentication (`/auth`, `/logout`)
- **Keycloak integration** - SSO authentication flow
- **Logout handling** - Session management

### 🎨 UI/UX Architecture

#### Multi-Layout System
Your app uses **3 distinct layouts** with device-specific optimizations:

**Mobile Layout (`src/Layout/Mobile/`):**
- Touch-optimized interface with bottom navigation
- Side flyouts for additional navigation
- Portrait/landscape orientation support
- Swipe gestures and pull-to-refresh
- Components: NavBar, BottomBar, SideFlyout, Modal

**TV Layout (`src/Layout/Tv/`):**
- D-pad navigation optimized for 10-foot UI
- Simplified interface with larger touch targets
- Focus management for remote control navigation
- Landscape-only orientation
- Components: NavBar, NavBarButton

**Desktop Layout (`src/Layout/Desktop/`):**
- Traditional desktop UI with sidebars
- Complex navigation with multiple panels
- Mouse and keyboard interaction patterns
- Components: Navbar, DashboardLayout

#### Key UI Components Identified
- **Responsive grid systems** for content cards
- **Video/music players** with custom controls
- **Modal systems** for detailed views
- **Toast notifications** for user feedback
- **Screensaver functionality** for TV platform
- **Image optimization** and lazy loading

### 🔧 Core Functionality

#### Media Management Features
- **Video streaming** with subtitle support and multiple quality options
- **Music playback** with playlists and queue management
- **Content encoding/ripping** for media processing
- **Metadata management** with external API integration
- **Library organization** with categorization and search

#### Real-time Features
- **WebSocket connections** for live updates and synchronization
- **Progress tracking** across devices and sessions
- **Notification system** for system events and user alerts
- **Device activity monitoring** for multi-user environments

#### Platform-Specific Features
**TV Platform:**
- Remote control navigation with D-pad support
- Screensaver activation during inactivity
- Immersive fullscreen media playback

**Mobile Platform:**
- Touch gestures for navigation and control
- Portrait/landscape mode adaptation
- Haptic feedback integration

---

## 📁 Platform-aware views/ folder structure (Vue-like, Kotlin-optimized)

Goal: Match your Vue convention while keeping Kotlin/Compose ergonomics high and mental overhead low. Colocate each page by category and platform, with a shared spot for page state/contracts.

Top-level under app/src/main/java/tv/nomercy/app:
- core/
  - data/ … repositories, DTOs, Retrofit services (device-agnostic)
  - domain/ … use-cases, interactors, pure Kotlin logic
  - ui/ … shared UI atoms/molecules (compose) with ZERO ViewModel deps
  - util/ … common helpers, extensions
- views/
  - base/
    - library/
      - mobile/ … composables optimized for phone
      - tv/ … composables optimized for TV (D‑pad, focus)
      - tablet/ … composables for tablet (add later)
      - shared/ … LibrariesViewModel, state holders, navigation contracts
  - music/
    - cards/
      - mobile/
      - tv/
      - tablet/
      - shared/ … CardsViewModel, contracts
  - dashboard/
    - home/
      - mobile/
      - tv/
      - tablet/
      - shared/
- platform/
  - mobile/ … entrypoints, Activities, app scaffolds and NavHost wiring
  - tv/ … entrypoints, Activities, app scaffolds and NavHost wiring
- shared/ … cross-page generic components (only if truly generic)

Notes:
- ViewModels live under views/<category>/<page>/shared. They expose platform-agnostic StateFlows and events.
- Each platform UI consumes the same ViewModel contract and composes device-specific layout.
- Reusable small UI atoms that are not page-specific go into core/ui or shared/components, and must not import ViewModels; accept data + callbacks.

### Example: Libraries page
- views/base/library/shared/LibrariesViewModel.kt
- views/base/library/shared/model/
- views/base/library/mobile/LibraryScreen.kt
- views/base/library/tv/LibraryScreen.kt
- shared/components/Indexer.kt (accepts flows + callback only)

This mirrors your Vue layout: ./views/(base|music|dashboard)/(page)/(mobile|tablet|tv), adapted to Kotlin packages.

### Package naming example
- tv.nomercy.app.views.base.library.mobile.LibraryScreen
- tv.nomercy.app.views.music.cards.tv.CardsScreen

### Navigation
- Keep page-level route constants in views/<category>/<page>/shared/navigation.
- Platform-specific NavHost setups live under platform/mobile and platform/tv.

### Dependency rule of thumb
- platform/* -> views/*/{mobile|tv|tablet} -> views/*/*/shared -> core/*
- Never the opposite direction.

---

## ✅ Minimal Refactor Done Now
- Decoupled shared/components/Indexer.kt from mobile LibrariesViewModel dependency. It now only takes StateFlows and an optional callback, so it’s reusable across TV, Mobile, and Tablet without importing platform packages.

Usage:
Indexer(
  modifier = Modifier,
  showIndexerState = viewModel.showIndexer,
  selectedIndexState = viewModel.selectedIndex,
  activeLettersState = viewModel.activeIndexerLetters,
  onIndexSelectedCallback = { c -> viewModel.onIndexSelected(c) }
)

No call sites needed changes; one screen that passed only modifier still compiles and renders nothing when flows are absent.

---

## 🧭 Migration Checklist (incremental, low-risk)
1. For any file in shared/components that imports tv.*.mobile or tv.*.tv, remove the dependency. Accept data and callbacks instead. ✓ Indexer
2. Move ViewModels into views/<category>/<page>/shared; keep their APIs platform-agnostic (StateFlow, immutable state objects).
3. Under views/<category>/<page>/{mobile,tv}, keep only composables; no business logic.
4. Keep platform entrypoints and scaffolds (Activities, Navigation) in platform/mobile and platform/tv.
5. When adding Tablet later, create views/<category>/<page>/tablet with tablet-optimized composables that reuse the shared ViewModel.
6. Add device-conditional choosing at NavHost level (or pass through DI) to select the platform screen implementation.

---

## 📌 Conventions for Shared UI Components
- Must not import any ViewModel. Receive state via parameters (State, StateFlow) + callbacks.
- Keep styling via MaterialTheme tokens only; no hard-coded colors.
- Prefer small, pure composables with previews and testTags.

---

## 🔄 Next Steps You Can Take Safely
- Gradually move existing mobile ViewModels into views/<category>/<page>/shared without changing their public API.
- Duplicate TV and Mobile screens into views/<category>/<page>/{tv,mobile}, respectively. Update package declarations only; imports remain the same.
- Add a simple DeviceProfile enum later to guide platform selection. Documented here but not implemented to keep this PR minimal.




---

# NM Component System: Server‑Driven Layout for NoMercy (Authoritative Spec v0.1)

Audience: Maintainers of the Android app, C# NoMercyMediaServer, and Vue web app. Goal is a stable, evolvable, server‑driven UI contract shared across platforms.

1. Purpose and principles
- Server‑driven layout: the server returns a component tree that the client renders. This lets us change UI structure, layout density, and data sources without publishing a new Android build.
- Contracts over code: the JSON format is the source of truth. The client must be liberal in what it accepts (ignore unknown keys) and strict in what it emits (well‑formed requests).
- Independent component refresh: each component may declare how to refresh itself and be updated in place without reloading the whole screen.
- Cross‑app compatibility: any change in the contract must remain backward‑compatible with the existing Vue app unless both are updated together. Use additive changes and version gates.

2. High‑level data flow
- Request: Client hits an endpoint like GET /api/layout/home or GET /api/libraries/{id} returning a JSON component tree.
- Deserialize: Android uses kotlinx.serialization to parse the tree into component DTOs and props.
- Render: A registry maps component -> Composable renderer; props drive UI.
- Update: Components may refresh individually (e.g., new Continue Watching) via a declared refresh spec.

3. Canonical JSON envelope
- Top‑level can be either a single component or a list of components. Android already consumes lists in some flows.
- Each component object uses:
  {
    "component": "NMGrid",            // type discriminator
    "id": "grid:continue-watching",   // stable id for targeted refresh/events
    "props": { ... },                   // type-specific props
    "children": [ { ... } ],            // optional nested components
    "refresh": {                        // optional, declares self-refresh behavior
      "method": "GET|POST",
      "url": "/api/components/continue-watching",
      "query": { "limit": 20 },       // optional
      "body": { ... },                  // optional
      "pollMs": 0                       // optional polling interval; 0 = disabled
    },
    "meta": {                           // optional tracking/analytics/test tags
      "testTag": "continueWatching",
      "analytics": { "section": "home" }
    }
  }
- Naming notes:
  - component is the discriminator currently used in Android (matches existing checks like component == "NMGrid"). Keep this for compatibility with the Vue app.
  - id is required for targeted refresh and event routing. If the server cannot provide stable ids yet, derive deterministic ones server-side as "<type>:<slug>".

4. Known component types and example props
- NMGrid
  props:
  {
    "items": [ { "component": "NMCard", "props": { ... } }, ... ],
    "columns": 3,
    "aspect": "2:3",          // optional, client may map to width/height
    "gap": 8                   // dp on Android; px on web; treat as density-agnostic token
  }
- NMCard
  props:
  {
    "id": "movie:123",
    "title": "The Matrix",
    "titleSort": "Matrix, The",
    "subtitle": "1999",
    "image": "https://.../poster.jpg",
    "link": "/movie/123",
    "badges": [ { "text": "4K" }, { "text": "Dolby Vision" } ]
  }
- NMRow
  props:
  {
    "title": "Continue Watching",
    "items": [ { "component": "NMCard", "props": { ... } } ]
  }
- NMHeader
  props:
  {
    "title": "Movies",
    "subtitle": "Popular this week"
  }
- NMCarousel
  props:
  {
    "items": [ ... ],
    "autoPlayMs": 8000
  }
- Extend with new components as needed following the same contract. Add only new fields; never repurpose existing fields.

5. Android deserialization strategy (kotlinx.serialization)
- Configuration:
  val json = Json {
    ignoreUnknownKeys = true           // forward-compatible with server additions
    explicitNulls = false
    isLenient = true
    classDiscriminator = "component"   // aligns with server discriminator
  }
- Polymorphic model: prefer a sealed interface with @SerialName on each subtype.
  @Serializable
  sealed interface NMComponentDto {
    val id: String?
    val meta: Map<String, JsonElement>?
  }
  @Serializable @SerialName("NMGrid")
  data class NMGridDto(
    override val id: String? = null,
    val props: NMGridProps,
    val children: List<NMComponentDto>? = null,
    val refresh: NMRefreshSpec? = null,
    override val meta: Map<String, JsonElement>? = null
  ) : NMComponentDto
  // Repeat for NMRow, NMCard, etc.
- If not all subtypes are known, use JsonContentPolymorphicSerializer to route by element["component"].
- Props classes (e.g., NMGridProps, NMCardProps) should be @Serializable and keep default values for new fields to avoid breaking older clients.

6. Rendering pipeline in Compose
- Registry pattern:
  typealias Renderer = @Composable (component: NMComponentDto) -> Unit
  object NMRenderRegistry {
    private val map = mutableMapOf<String, Renderer>()
    fun register(type: String, renderer: Renderer) { map[type] = renderer }
    fun render(node: NMComponentDto) { map[node::class.serialName]?.invoke(node) ?: UnknownComponent(node) }
  }
- Rendering NMGrid maps to a LazyVerticalGrid on mobile/TV with props.columns and items.
- Keep renderers pure and side-effect free; use events/refresh for dynamic updates.

7. Component self-refresh (critical requirement)
- Motivation: e.g., server tells client to refresh Continue Watching and replace the carousel with fresh data. Vue does this in NMComponent.vue; Android must mirror it.
- Contract additions in JSON:
  - Each component may include refresh with url/method and optional query/body.
  - Each component should have an id (stable within a screen) for event targeting.
- Client-side infrastructure (Android):
  - NMComponentEventBus: singleton exposing Flow<NMComponentEvent> with events { Refresh(id), Replace(id, node), Remove(id) }.
  - NMComponentHost: presenter that holds the component tree state (StateFlow<List<NMComponentDto>>). It listens to the EventBus. On Refresh(id), it finds the node, calls the declared refresh.url (Retrofit), deserializes the returned component(s), and immutably replaces the subtree by id, then emits new state for Compose to recompose.
  - Renderer (NMComponent composable) consumes the Host’s StateFlow and delegates to NMRenderRegistry.
- Server hooks for refresh:
  - Endpoint must return the same component type that is being refreshed (or an explicit Replace event payload with new type) to avoid UI mismatch.
  - Optional ETag/If-None-Match to reduce bandwidth.

8. Versioning and compatibility
- Introduce schema versioning immediately:
  - HTTP response header: x-nm-schema-version: 1
  - Optional per-component: "schema": 1 in component/meta.
- Client behavior:
  - Accept higher schema versions only if changes are additive and unknown fields can be ignored; otherwise show a graceful error/placeholder.
  - Log version mismatches to telemetry for detection.
- Change rules (server):
  - Allowed: add new components, add new props (with defaults), add refresh/meta.
  - Not allowed without web+android dual update: rename fields, change discriminator names, remove required props, change semantics of existing fields.
- Feature flags:
  - Server may gate new component types behind a feature flag exposed via /config. Clients only render flagged components they explicitly support; otherwise fallback to UnknownComponent.

9. Error handling and fallbacks
- Unknown component: render an UnknownComponent frame with the component name, log a warning, continue rendering siblings.
- Malformed props: use default values and show partial UI; never crash the whole screen.
- Network failure on refresh: keep previous subtree and surface a retry affordance.

10. End-to-end developer workflow
- Adding a new component type
  1) Server: define type name and props (additive). Provide example payloads and a /components/{id} refresh endpoint if needed.
  2) Android: add @Serializable Props, DTO subtype with @SerialName, and a Composable renderer. Register in NMRenderRegistry.
  3) Vue: implement the Vue renderer with the same type name and props.
  4) Tests: add JSON fixtures and parsing tests for both Android and Vue.
- Evolving props
  - Only add optional fields with safe defaults. Do not repurpose existing fields. Coordinate removal via version bump and dual releases.
- Component refresh testing
  - Provide a mock endpoint returning varied payloads. Verify Replace(id) works and the subtree updates without losing scroll position or focus (important for TV).

11. Android specifics for stability
- kotlinx.serialization config with ignoreUnknownKeys = true must be enabled globally on Retrofit’s converter.
- Prefer immutable state objects for the component tree; Compose will diff and recompose efficiently.
- Use remember and derivedStateOf to avoid heavy recompositions.
- On TV, preserve D-pad focus across subtree replacement by keeping item keys stable (e.g., key = card.props.id).

12. Shared JSON fixtures and tests (cross‑repo)
- Create a small set of canonical JSON fixtures in a shared folder in the NoMercyMediaServer repo (or a new repo) referenced by both Android and Vue tests.
- Include cases: minimal NMGrid + NMCard, unknown component, props with unknown keys, refresh with polling.
- Add Android unit tests (future task): parse fixtures into DTOs and assert expected types.

13. Migration plan to adopt v0.1 in phases
- Phase A (docs-only): Adopt this spec on paper; server keeps emitting current shape (component + props). Add id and refresh gradually.
- Phase B (compat): Start emitting x-nm-schema-version: 1 and id on selected components (Continue Watching, Home rows). Android will ignore id until implemented; Vue ignores unknown fields.
- Phase C (client infra): Implement NMComponentHost + EventBus in Android and wire targeted refresh for at least one component.
- Phase D (roll-out): Add ids and refresh to more components. Introduce UnknownComponent fallback visuals.

14. Quick checklist for every change
- Does it only add new fields/types? Yes → safe. No → coordinate dual update.
- Is the discriminator (component) unchanged? If changed, dual release required.
- Are defaults set for new props? If not, add them server-side or in DTOs.
- Is id stable for refresh? If not, make it deterministic.
- Is schema version header set? Ensure x-nm-schema-version present.

15. Action items for next PRs
- Server: Add x-nm-schema-version header = 1; start returning id on existing components (stable slug), add refresh spec for Continue Watching.
- Android: Introduce NMComponentHost + EventBus scaffolding, keep renderers unchanged; add UnknownComponent placeholder; add minimal parsing tests.
- Vue: Ensure NMComponent.vue gracefully ignores id/refresh and is ready to consume targeted refresh events if present.

This section establishes a resilient, evolvable foundation for the NM server‑driven UI across Android and the existing Vue app while minimizing breakage risk.

---

## App-to-App Casting (Mobile controls TV) — Architecture v0.1

Goal: Let the Android mobile app control playback and navigation on the Android TV app (our own receiver), similar to YouTube/Netflix second-screen — without relying on Google Cast.

Key principles
- Additive, transport-agnostic protocol (messages are simple Kotlin data classes in core/cast).
- Start with server-relayed signaling (WebSocket via NoMercyMediaServer) for simplicity; evolve to LAN discovery later.
- Secure pairing: explicit pairing step (PIN or account trust), session scoping, and opt-in receiver exposure.

Packages added in this PR
- core/cast — protocol and interfaces
  - CastRole, CastMessage (Discover, Advertise, PairRequest, PairResult), Control (Play, Pause, SeekTo, SetVolume, Navigate), State (AppState)
  - CastSession, CastSignalingClient, CastReceiver
  - CastNoop — default no-op signaling implementation used as placeholder
- platform/mobile — CastControllerFacade (controller entrypoint)
- platform/tv — CastReceiverFacade (receiver entrypoint)

Rollout plan
1) v0.1 (this PR): Protocol DTOs, interfaces, facades, no runtime wiring. Safe, no behavior change.
2) v0.2: Implement server WebSocket signaling client (controller + receiver), scoped by user/device; add simple pairing UI (enter PIN on mobile shown on TV).
3) v0.3: Control hooks on TV player (map Play/Pause/SeekTo/SetVolume to LibVLC-backed player + MediaSession); periodic AppState updates back to controller.
4) v0.4: Discovery UX on mobile (list online TVs), quick-connect to last TV; LAN discovery for same-network optimization.

Security basics
- Pairing required per controller-TV pair; store a device token post-pair.
- Messages scoped to a sessionId; server validates user/device claims and routes only to paired receiver.
- TV exposes availability only when user is active and has opted-in.

Testing
- Unit-test DTO serialization (kotlinx.serialization) when wired; fuzz unknown fields to ensure forward-compatibility.
- Integration tests for control round-trips once signaling exists.

Migration note (structure)
- New feature-first wrappers have been added to gradually move screens under features/<category>/<platform> while delegating to existing screens. Begin importing from features.* packages going forward.
