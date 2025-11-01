# NoMercy TV — Native Android App Development Plan

## App Analysis for Native Android

### Architecture Overview

**Core Type:** Media streaming and management platform  
**Target Devices:** ~~Phone, Tablet, Android TV, Desktop~~  
**Authentication:** ~~Keycloak-based with JWT tokens~~  
**Backend:** ~~RESTful API with WebSocket real-time features~~  
**Primary Function:** ~~Video/music library management, streaming, and encoding~~

---

## Navigation Structure

Your app has 7 route groups and 50+ screens.

### Route groups and key screens

- **/** Main Media  
  - Home dashboard  
  - Libraries  
  - Search  
  - Individual content pages (movie/:id, tv/:id)  
  - Watch/player pages  
  - Collections, genres, people pages

- **/music**  
  - Artists, albums, tracks  
  - Playlists and genres  
  - Music player

- **/dashboard**  
  - System management  
  - Content management  
  - Device management  
  - Advanced features (logs, plugins, scheduled tasks)

- **/preferences**  
  - Display settings  
  - Profile management  
  - Controls configuration  
  - Subtitles settings

- **/dev**  
  - Cast functionality  
  - Download management

- **/setup**  
  - Server selection  
  - Post-install configuration

- **/auth**, **/logout**  
  - Keycloak SSO  
  - Logout handling

---

## UI/UX Architecture

### Multi-Layout System

- **Mobile Layout (`src/Layout/Mobile/`)**  
  - ~~Touch-optimized interface with bottom navigation~~  
  - ~~Side flyouts for additional navigation~~  
  - ~~Portrait/landscape orientation support~~  
  - ~~Swipe gestures and pull-to-refresh~~  
  - ~~Components: NavBar, BottomBar, SideFlyout, Modal~~  
  - Note: Folder structure uses `views/.../mobile` rather than `platform/mobile`.

- **TV Layout (`src/Layout/Tv/`)**  
  - ~~D-pad navigation optimized for 10-foot UI~~  
  - ~~Simplified interface with larger touch targets~~  
  - ~~Focus management for remote control navigation~~  
  - ~~Landscape-only orientation~~  
  - ~~Components: NavBar, NavBarButton~~  
  - Note: Folder structure uses `views/.../tv` rather than `platform/tv`.

- **Desktop Layout (`src/Layout/Desktop/`)**  
  - Not implemented (Android only)

### Key UI Components Identified

- [ ] Responsive grid systems for content cards  
- [ ] Modal systems for detailed views  
- [x] Video/music players with custom controls (currently a placeholder)  
- [ ] Toast notifications for user feedback  
- [ ] Screensaver functionality for TV platform  
- [x] Image optimization and lazy loading (Coil used)

---

## Core Functionality

### Media Management Features

- [ ] Video streaming with subtitle support and multiple quality options (currently a placeholder)  
- [ ] Music playback with playlists and queue management (not production-ready)  
- [ ] Content encoding/ripping for media processing  
- [x] Metadata management with external API integration  
- [x] Library organization with categorization and search

### Real-time Features

- [x] WebSocket connections for live updates and synchronization (infrastructure present)  
- [ ] Progress tracking across devices and sessions  
- [ ] Notification system for system events and user alerts  
- [ ] Device activity monitoring for multi-user environments

### Platform-Specific Features

**TV Platform**  
- [x] Remote control navigation with D-pad support  
- [ ] Screensaver activation during inactivity  
- [ ] Immersive fullscreen media playback

**Mobile Platform**  
- [ ] Touch gestures for navigation and control (partial)  
- [ ] Portrait/landscape mode adaptation (partial)  
- [ ] Haptic feedback integration

**Cross-platform**  
- [ ] Responsive design adaptation (partial)  
- [ ] Device detection and feature toggling (partial)  
- [ ] Synchronized playback state

---

## State Management Architecture

Your app uses 20+ reactive stores.

### Core Application State

- [x] User authentication (`src/store/user.ts`) — JWT tokens, user profile, permissions  
- [x] Server connections (`src/store/servers.ts`) — Available servers, connection status  
- [x] Libraries management (`src/store/Libraries.ts`) — Content organization, metadata  
- [x] Video player (`src/store/videoPlayer.ts`) — Playback controls, progress, quality  
- [ ] Music playlists (`src/store/musicPlaylists.ts`) — Queue management, shuffle, repeat  
- [ ] Video/music sockets (`src/store/videoSocket.ts`, `src/store/musicSocket.ts`) — Real-time sync (WebSocket support not implemented)

- [x] UI preferences (`src/store/ui.ts`) — Theme, layout preferences, accessibility  
- [x] Search functionality (`src/store/search.ts`) — Query history, filters, results  
- [ ] Notifications (`src/store/notifications.ts`)  
- [x] Screensaver (`src/store/screensaver.ts`) — Inactivity detection, activation

### System Management

- [x] Route state (`src/store/routeState.ts`) — Navigation history, deep linking  
- [x] Preferences (`src/store/preferences.ts`) — User settings, customization  
- [x] Ripper (`src/store/ripper.ts`) — Content processing status  
- [x] Indexer (`src/store/indexer.ts`) — Content scanning and organization

---

## External Integrations

### Authentication Services

- [x] Keycloak — Primary authentication service with SSO support  
- [x] JWT token management — Secure session handling

### Media Metadata

- [x] All metadata provided by NoMercyMediaServer; client consumes plain JSON

### Platform Integration

- [x] Capacitor plugins — Device detection, status bar, navigation controls  
- [x] WebSocket connections — Real-time server communication (infrastructure present)  
- [x] PWA features — Service workers, offline caching, installation

---

## Key User Flows

### 0. First-run Flow (authoritative order)  
Authentication (Keycloak Login) → Token Validation → Domain API: Fetch accessible servers → User selects a server → Fetch server config + user libraries → Initialize app (stores, sockets, image base, feature flags) → Load Home → Free navigation

### 1. Authentication Flow (detail)  
Keycloak Login → Token Validation/Refresh bootstrap → Proceed to Domain API: Servers

### 2. Server Selection & Initialization Flow  
Domain API: Get servers for user → User selects server → Fetch libraries + server capabilities/config → Warm caches (optional) → Navigate to Home

### 3. Content Discovery Flow  
Browse Libraries → Apply Filters → Search Content → View Details → Initiate Playback

### 4. Media Playback Flow  
Select Content → Quality Selection → Player Interface → Playback Controls → Progress Sync

### 5. Admin Management Flow  
Dashboard Access → System Configuration → Content Management → User Administration

---

## Native Android App Development Strategy

### Phase 1: Foundation Architecture

**Tech stack items with status**  
- [ ] Language: Kotlin with Coroutines  
- [ ] UI Framework: Jetpack Compose + Material Design 3  
- [ ] Architecture: MVVM + Repository + Clean Architecture  
- [x] Database: Room for local caching and offline support  
- [ ] Networking: Retrofit + OkHttp with WebSocket support  
- [x] Dependency Injection: Dagger Hilt  
- [ ] Image Loading: Coil with custom caching  
- [x] Media Playback: LibVLC (org.videolan.libvlc) with Compose UI controls

### Phase 2: Authentication System

- [ ] Keycloak Android SDK integration for SSO  
- [ ] JWT token management with automatic refresh  
- [ ] Post-auth server discovery and selection UI  
- [ ] Session persistence and security

### Phase 3: Multi-Device UI Implementation

**Phone UI**  
- [ ] Bottom Navigation: primary navigation with 5 tabs (Home, Libraries, Search, Music, Profile)  
- [ ] Swipe Gestures: pull-to-refresh, swipe-to-dismiss  
- [ ] Touch Optimization: large touch targets, gesture navigation  
- [ ] Adaptive Layout: portrait/landscape support  
- [ ] Material Design compliance

**TV UI**  
- [ ] Leanback Library integration  
- [ ] D-pad Navigation: focus management for remote control  
- [ ] 10-foot Interface: large text, simplified navigation  
- [ ] TV Launcher Integration: banner, categories, recommendations  
- [ ] Voice Search integration

**Shared Components**  
- [ ] Media Cards: poster/banner display with metadata  
- [x] Video/Audio Players: custom controls with LibVLC  
- [ ] Modal Dialogs: information panels and settings screens  
- [ ] Search Interface: global search with filters and suggestions

### Phase 4: Media Engine Implementation

**Video Playback System**  
- [x] LibVLC Integration: primary playback engine planned  
- [ ] Subtitle Support: SRT, VTT, ASS  
- [ ] Quality Selection: adaptive bitrate streaming  
- [ ] Background Service: continue playback when app minimized  
- [ ] Picture-in-Picture: Android PiP support for phones  
- [ ] App-to-App Casting: mobile controls TV via custom protocol

**Audio Playback System**  
- [x] LibVLC Audio planned  
- [ ] Queue Management: playlist handling, shuffle/repeat  
- [ ] Media Session: system media controls integration  
- [ ] Notification Controls: lock screen and notification panel controls  
- [ ] Bluetooth Integration: car stereo and headphone controls

### Phase 5: Real-time Features

**WebSocket Implementation**  
- [x] OkHttp WebSocket infra present  
- [ ] Event Handling: progress sync, notifications, live updates  
- [ ] Connection Management: automatic reconnection and error handling  
- [ ] Background Sync: maintain connection during backgrounding

**Notification System**  
- [ ] Firebase Cloud Messaging: push notifications for system events  
- [ ] Local Notifications: progress updates, download completion  
- [ ] Channel Management: categorized notification types  
- [ ] Action Buttons: quick actions from notifications

### Phase 6: Data Management

**Local Database**  
- [ ] Room — not implemented

**Caching Strategy**  
- [x] Image Caching: Coil with custom cache policies  
- [ ] API Response Caching: Room database with TTL  
- [ ] Offline Support: downloaded content for offline viewing  
- [ ] Sync Strategy: background sync when network available

### Phase 7: Platform-Specific Features

**Android TV Exclusive Features**  
- [ ] Android TV Launcher: proper integration with TV home screen  
- [ ] Recommendations: content suggestions on TV launcher  
- [ ] Voice Search phrases  
- [ ] TV Input Framework integration  
- [ ] HDMI-CEC support

**Phone Exclusive Features**  
- [ ] Adaptive Brightness during playback  
- [ ] Battery Optimization for background processing  
- [ ] Share Integration with Android share sheet  
- [ ] Widget Support for home screen  
- [ ] Shortcuts for recent content  
- [ ] Cast Support: Chromecast and Android TV casting  
- [ ] Deep Linking: handle nomercy:// URLs  
- [ ] Auto-Backup: preferences and watch history  
- [ ] Multi-User: profile switching and parental controls

### Phase 8: Development Workflow

**Project configuration**  
- [ ] Folder structure matches plan (platform folder not present in repo)  
- [ ] Product Flavors: separate phone and TV builds (no tablet yet)  
- [ ] Build Types: Debug, Release, Beta  
- [ ] Signing Configuration: production keys  
- [ ] Proguard/R8: obfuscation and optimization

**CI / Releases**  
- [ ] GitHub Actions: automated build/test (partial)  
- [ ] Fastlane: release automation (partial)  
- [ ] Firebase Distribution: beta distribution  
- [ ] Crash Reporting: Firebase Crashlytics

### Phase 9: Testing Strategy

**Unit Testing**  
- [ ] Repository layer tests  
- [ ] UseCase tests  
- [ ] ViewModel tests

**Integration Testing**  
- [ ] API integration tests  
- [ ] Database (Room) tests  
- [ ] Media playback tests (LibVLC)

**UI Testing**  
- [ ] Compose UI tests  
- [ ] TV D-pad navigation tests  
- [ ] Phone gesture tests

### Phase 10: Deployment Strategy

**Google Play**  
- [ ] App Bundle with dynamic features  
- [ ] Device Targeting: separate phone/TV listings  
- [ ] Beta track for QA  
- [ ] Staged rollout to production

**Tools**  
- [ ] GitHub Actions (partial)  
- [ ] Fastlane (partial)  
- [ ] Firebase Distribution  
- [ ] Crashlytics

---

## Migration Mapping

### Component Translation Guide

- Vue `src/Layout/Mobile/` → Android Phone Activity/Fragments → Jetpack Compose screens  
- Vue `src/Layout/Tv/` → Leanback Fragments / TV-specific Compose UI (Leanback not implemented)  
- `src/views/Base/Watch/` → Video Player Activity → Compose UI (LibVLC not implemented)  
- `src/store/` modules → Repository + ViewModel with StateFlow (Room not implemented)  
- Router navigation → Navigation Component / Navigation Compose  
- WebSocket clients → OkHttp WebSocket with Kotlin Coroutines + Flow

### API Integration Mapping

- `src/lib/clients/apiClient.ts` → Retrofit interface (no Hilt)  
- `src/lib/clients/serverClient.ts` → Repository (no Room caching)  
- `src/lib/auth/` → Keycloak Android SDK + secure storage  
- WebSocket services → OkHttp WebSocket + StateFlow

---

## Current Folder Structure (as of Oct 2025)

Top-level under `app/src/main/java/tv/nomercy/app`:

- `components/` — Shared UI components (Indexer, music controls, images, nMComponents)  
- `core/` — Core features (currently `cast/`)  
- `layout/` — Device layout scaffolds (mobile, tv, shared)  
- `shared/` — Cross-cutting logic: `api/`, `auth/`, `models/`, `repositories/`, `routes/`, `stores/`, `ui/`, `utils/`  
- `views/` — Feature screens by domain: `base/`, `dashboard/`, `music/`, `preferences/`, `profile/`, `setup/`  
  - Each feature has `mobile/`, `tv/`, and `shared/` (ViewModels, contracts)

**Not present**  
- [ ] `platform/` folder (entrypoints like Activities/NavHost are in root or under `views/`)  
- [ ] explicit `data/` or `domain/` layers (DTOs and logic live in `shared/`)

### Examples

- Libraries page  
  - `views/base/library/mobile/LibraryScreen.kt`  
  - `views/base/library/tv/LibraryScreen.kt`  
  - `views/base/library/shared/LibrariesViewModel.kt`  
  - `components/Indexer.kt` (accepts flows + callback only)

- Layout scaffolds  
  - `layout/mobile/MobileMainScaffold.kt`  
  - `layout/tv/TvMainScaffold.kt`  
  - `layout/shared/SharedMainScreen.kt`

- Shared logic  
  - `shared/stores/` — AppConfigStore, MusicPlayerStore, etc.  
  - `shared/ui/` — Theming, typography, system UI

---

## Suggestions for Improvement (prioritized)

- [ ] Add a `platform/` folder to separate entrypoints (Activities, NavHost) for mobile and TV.  
- [ ] Introduce `data/` and `domain/` layers to follow Clean Architecture as business logic grows.  
- [ ] Add `CONTRIBUTING.md` documenting folder conventions and onboarding steps.  
- [ ] Ensure shared components in `components/` do not import ViewModels; accept state and callbacks only.  
- [ ] Move ViewModels into `views/<category>/<page>/shared` for reusability.  
- [ ] Add `tablet/` subfolders per feature when tablet support is added.  
- [ ] Migrate business logic out of `mobile/` and `tv/` into `shared/` so platform folders are UI-only.

---

## Minimal Refactor Done

- [x] Decoupled `shared/components/Indexer.kt` from mobile `LibrariesViewModel` dependency; it now accepts StateFlows and an optional callback.

Example usage:
```kotlin
Indexer(
  modifier = Modifier,
  showIndexerState = viewModel.showIndexer,
  selectedIndexState = viewModel.selectedIndex,
  activeLettersState = viewModel.activeIndexerLetters,
  onIndexSelectedCallback = { c -> viewModel.onIndexSelected(c) }
)
```

---

## Migration Checklist (incremental, low-risk)

- [x] Remove platform imports from shared components; accept data and callbacks instead (Indexer done)  
- [ ] Move ViewModels to `views/<category>/<page>/shared` and keep APIs platform-agnostic (StateFlow, immutable state)  
- [ ] Keep composables only in `views/<category>/<page>/{mobile,tv}` (no business logic)  
- [ ] Place platform entrypoints and scaffolds in `platform/mobile` and `platform/tv`  
- [ ] Add `views/<category>/<page>/tablet` when tablet support is added  
- [ ] Add device-conditional selection at NavHost level or via DI for choosing platform screen implementations

---

## Conventions for Shared UI Components

- [ ] Do not import ViewModels; receive state via parameters (State, StateFlow) and callbacks only  
- [ ] Use MaterialTheme tokens for styling; avoid hard-coded colors  
- [ ] Favor small, pure composables with Previews and `testTag` for testing

---

## Next Safe Steps

- [ ] Gradually move mobile ViewModels into `views/<category>/<page>/shared` without changing public APIs  
- [ ] Duplicate TV and Mobile screens into `views/<category>/<page>/{tv,mobile}` with updated package declarations only  
- [ ] Add a `DeviceProfile` enum to guide platform selection (documented, not implemented)

---

## NM Component System: Server‑Driven Layout (Authoritative Spec v0.1)

Audience: Android app maintainers, NoMercyMediaServer, and Vue web app. Goal: a stable, evolvable server-driven UI contract shared across platforms.

### Purpose and principles

- Server-driven layout: server returns a component tree the client renders.  
- Contracts over code: JSON is source of truth; clients ignore unknown keys.  
- Independent component refresh: components declare refresh behavior to update in place.  
- Cross-app compatibility: changes must remain backward-compatible; use additive changes and version gates.

### High-level data flow

1. Client requests GET /api/layout/home or GET /api/libraries/{id}  
2. Server returns JSON component tree  
3. Android deserializes with kotlinx.serialization into DTOs  
4. Registry maps component type → Composable renderer  
5. Components refresh independently using declared refresh specs

### Canonical JSON envelope (example)

Each component may include:
```json
{
  "component": "NMGrid",
  "id": "grid:continue-watching",
  "props": { ... },
  "children": [ ... ],
  "refresh": {
    "method": "GET|POST",
    "url": "/api/components/continue-watching",
    "query": { "limit": 20 },
    "body": { ... },
    "pollMs": 0
  },
  "meta": { "testTag": "continueWatching", "analytics": { "section": "home" } }
}
```

Notes:
- `component` is the discriminator.  
- `id` is required for targeted refresh and events; server should provide stable ids.

### Known component types (examples)

- NMGrid: items[], columns, aspect, gap  
- NMCard: id, title, titleSort, subtitle, image, link, badges[]  
- NMRow: title, items[]  
- NMHeader: title, subtitle  
- NMCarousel: items[], autoPlayMs

### Android deserialization strategy

- Configure kotlinx.serialization JSON:
  - ignoreUnknownKeys = true  
  - explicitNulls = false  
  - isLenient = true  
  - classDiscriminator = "component"
- Use polymorphic sealed interface DTOs with @SerialName subtypes.  
- Provide default values in props classes to remain compatible with older clients.

### Rendering pipeline in Compose

- Registry pattern: map component type → Renderer composable.  
- Render NMGrid with LazyVerticalGrid using props.columns.  
- Keep renderers pure and side-effect free; event/refresh infrastructure handles dynamic updates.

### Component self-refresh

Client-side infra:
- NMComponentEventBus: Flow of events `{ Refresh(id), Replace(id, node), Remove(id) }`  
- NMComponentHost: holds component tree StateFlow; listens for events; calls refresh URLs and replaces subtree immutably  
- Renderer reads Host StateFlow and delegates to NMRenderRegistry

Server-side:
- Refresh endpoints should return the same component type or explicit Replace payloads.  
- Support ETag/If-None-Match to reduce bandwidth.

### Versioning and compatibility

- Include `x-nm-schema-version` HTTP header (e.g., 1) and optional per-component `"schema": 1`.  
- Clients accept additive schema changes; otherwise show graceful placeholders and log telemetry.  
- Server rules: adding fields is allowed; renaming/removing required fields requires coordinated dual releases.  
- Feature flags may gate new component types.

### Error handling and fallbacks

- Unknown components render as UnknownComponent and log warnings.  
- Malformed props use defaults and render partial UI.  
- Network failures on refresh keep previous subtree and surface a retry affordance.

### Developer workflow for new components

1. Server: define type and example payloads; add refresh endpoint if needed.  
2. Android: add @Serializable props, DTO subtype, renderer, register in NMRenderRegistry.  
3. Vue: implement renderer with same type and props.  
4. Tests: add JSON fixtures and parsing tests.

### Android stability notes

- Enable ignoreUnknownKeys globally on Retrofit converter.  
- Prefer immutable component tree state; Compose will diff efficiently.  
- Use `remember` and `derivedStateOf` to avoid heavy recompositions.  
- On TV, preserve D-pad focus by keeping stable keys (e.g., key = card.props.id).

### Shared JSON fixtures and tests

- Create canonical fixtures: minimal NMGrid+NMCard, unknown component, unknown keys, refresh with polling.  
- Add Android unit tests to parse fixtures into DTOs and assert expected types.

### Migration plan (phased)

- Phase A: Docs-only adoption.  
- Phase B: Emit `x-nm-schema-version` and `id` on selected components.  
- Phase C: Implement NMComponentHost + EventBus on Android.  
- Phase D: Roll-out ids and refresh across components and add UnknownComponent fallbacks.

### Checklist for changes

- [ ] Add only new fields/types where possible.  
- [ ] Keep `component` discriminator unchanged unless coordinating dual releases.  
- [ ] Set defaults for new props.  
- [ ] Ensure `id` is stable for refresh.  
- [ ] Set `x-nm-schema-version` header.

### Action items for next PRs

- [ ] Server: Add `x-nm-schema-version: 1`; return stable `id` for components; add refresh for Continue Watching.  
- [ ] Android: Add NMComponentHost + EventBus scaffolding; add UnknownComponent placeholder; add parsing tests.  
- [ ] Vue: Ensure NMComponent.vue ignores unknown `id`/`refresh` fields gracefully.

---

## App-to-App Casting (Mobile controls TV) — Architecture v0.1

### Goal and principles

Goal: Let the Android mobile app control playback/navigation on the Android TV app (our receiver) without Google Cast.

Principles:
- Transport-agnostic, additive protocol (Kotlin data classes).  
- Start with server-relayed signaling via WebSocket; evolve to LAN discovery.  
- Secure pairing: PIN/account trust, session scoping, opt-in receiver exposure.

### Packages introduced

- `core/cast` — protocol and interfaces:
  - CastRole, CastMessage (Discover, Advertise, PairRequest, PairResult), Control (Play, Pause, SeekTo, SetVolume, Navigate), State (AppState)  
  - CastSession, CastSignalingClient, CastReceiver  
  - CastNoop — default no-op signaling placeholder
- `core/cast` — CastControllerFacade and CastReceiverFacade

### Rollout plan

- v0.1 — Protocol DTOs, interfaces, facades; no runtime wiring  
- v0.2 — Server WebSocket signaling client; pairing UI (PIN)  
- v0.3 — Control hooks on TV player (map controls to LibVLC + MediaSession); AppState updates  
- v0.4 — Mobile discovery UX; LAN discovery optimization

### Security basics

- Pairing required per controller-TV pair; store device token.  
- Messages scoped to sessionId; server validates claims.  
- TV exposes availability only when user is active and opted-in.

### Testing

- Unit-test DTO serialization with kotlinx.serialization; fuzz unknown fields.  
- Integration tests for control round-trips after signaling exists.

### Migration note (structure)

- Feature-first wrappers added to migrate screens under `features/<category>/<platform>` while delegating to existing screens.
