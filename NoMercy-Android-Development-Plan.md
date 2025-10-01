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

#### Media Metadata APIs
- **TMDB (The Movie Database)** - Movie and TV show metadata
- **IMDB** - Additional movie information and ratings
- **TheTVDB** - TV series and episode details
- **Last.fm** - Music metadata and scrobbling
- **Spotify** - Music streaming integration
- **Archive.org** - Public domain content
- **Fanart.tv** - High-quality artwork and imagery

#### Platform Integration
- **Capacitor Plugins** - Device detection, status bar, navigation controls
- **WebSocket connections** - Real-time server communication
- **PWA features** - Service workers, offline caching, installation

### 🎯 Key User Flows

#### 1. Authentication Flow
```
Server Discovery → Server Selection → Keycloak Login → Token Validation → Dashboard Access
```

#### 2. Content Discovery Flow
```
Browse Libraries → Apply Filters → Search Content → View Details → Initiate Playback
```

#### 3. Media Playback Flow
```
Select Content → Quality Selection → Player Interface → Playback Controls → Progress Sync
```

#### 4. Admin Management Flow
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
- **Media Playback:** ExoPlayer with Media3 library

### **Phase 2: Authentication System**
**Implementation Requirements:**
- Keycloak Android SDK integration for SSO
- JWT token management with automatic refresh
- Server discovery and selection UI
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
- **Video/Audio Players:** Custom controls with ExoPlayer
- **Modal Dialogs:** Information panels and settings screens
- **Search Interface:** Global search with filters and suggestions

### **Phase 4: Media Engine Implementation**

#### Video Playback System
- **ExoPlayer Integration:** Primary video playback engine
- **Subtitle Support:** Multiple formats (SRT, VTT, ASS)
- **Quality Selection:** Adaptive bitrate streaming
- **Background Service:** Continue playback when app minimized
- **Picture-in-Picture:** Android PiP support for phones
- **Chromecast Support:** Google Cast SDK integration

#### Audio Playback System
- **Media3 Audio:** Background audio playback service
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
```kotlin
// Core entities based on your store structure
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
│   ├── ui/
│   │   ├── phone/          # Phone-specific UI
│   │   ├── tv/             # TV-specific UI  
│   │   └── shared/         # Shared components
│   ├── data/
│   │   ├── local/          # Room database
│   │   ├── remote/         # API clients
│   │   └── repository/     # Data layer
│   ├── domain/             # Business logic
│   ├── di/                 # Dependency injection
│   └── service/            # Background services
├── src/tv/                 # TV-specific resources
└── src/phone/              # Phone-specific resources
```

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
- **Media Playback:** ExoPlayer integration

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
| `src/views/Base/Watch/` | Video Player Activity | ExoPlayer + Compose UI |
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
