# Lumen — Native Android Reference Architecture

This folder is **reference/blueprint code** for the real native Android app described in the
brief ("Silent Luxury" glassmorphic local music player). It is not compiled by this web
sandbox — the interactive product living in `src/` is a faithful **web prototype** of the same
design system, playback flows, folder-whitelist logic and widget concept, running on your local
audio files in the browser.

Use these files as the starting skeleton when you create the actual Android Studio project.

## Module Map

```
app/
 ├─ di/                     Hilt modules (Singleton scope — fine for a personal-use app)
 │   └─ AppModule.kt
 ├─ data/
 │   └─ Database.kt         Room entities, DAOs, AppDatabase (Favorites, FolderFilters, Playlists)
 ├─ theme/
 │   └─ GlassTheme.kt        Compose design system: colors, glass modifiers, Palette extraction
 ├─ playback/
 │   └─ PlaybackService.kt   Media3 MediaLibraryService + ExoPlayer + MediaSession
 ├─ widget/
 │   └─ MusicGlanceWidget.kt Jetpack Glance app widget + GlanceAppWidgetReceiver + actions
 └─ ui/
     └─ NowPlayingTransition.kt   SharedTransitionLayout example (list → full player)
```

## Gradle dependencies (version catalog excerpt)

```toml
[versions]
media3 = "1.4.1"
room = "2.6.1"
glance = "1.1.1"
hilt = "2.52"
compose-bom = "2024.09.03"
palette = "1.0.0"

[libraries]
media3-exoplayer     = { module = "androidx.media3:media3-exoplayer", version.ref = "media3" }
media3-session       = { module = "androidx.media3:media3-session", version.ref = "media3" }
media3-common        = { module = "androidx.media3:media3-common", version.ref = "media3" }
room-runtime         = { module = "androidx.room:room-runtime", version.ref = "room" }
room-ktx             = { module = "androidx.room:room-ktx", version.ref = "room" }
room-compiler        = { module = "androidx.room:room-compiler", version.ref = "room" }
glance-appwidget     = { module = "androidx.glance:glance-appwidget", version.ref = "glance" }
glance-material3     = { module = "androidx.glance:glance-material3", version.ref = "glance" }
hilt-android         = { module = "com.google.dagger:hilt-android", version.ref = "hilt" }
hilt-compiler        = { module = "com.google.dagger:hilt-android-compiler", version.ref = "hilt" }
palette-ktx          = { module = "androidx.palette:palette-ktx", version.ref = "palette" }
```

## Key architectural decisions

1. **Single source of truth**: `MusicRepository` (Singleton, Hilt-provided) wraps MediaStore
   scanning + Room folder-filter rules and exposes a `Flow<List<Track>>` that the whole app
   (UI, PlaybackService, Glance widget) collects. No duplicated scanning logic.
2. **Folder whitelist lives in Room**, not SharedPreferences, because it needs relational
   integrity with future playlist/queue features and because Flow-based DAOs make the filtered
   library reactive — toggle a folder off and every screen (and the widget) updates instantly.
3. **Playback is fully decoupled from UI** via `MediaLibraryService` — the widget and the app
   both talk to the same `MediaController`, so the widget's Play/Skip buttons never need to open
   an Activity.
4. **Color theming**: `Palette.from(bitmap)` extracts swatches, but instead of using raw
   `Palette.getVibrantColor()`, we down-sample the HSL saturation/lightness in
   `GlassTheme.kt::desaturateForLuxury()` — this is the single most important line for the
   "premium, not chaotic" look requested.
5. **Blur strategy**: `Modifier.blur(60.dp)` (Android 12+, `RenderEffect`) with a graceful
   fallback to a pre-blurred `RenderScript`/`android.graphics.RenderEffect` bitmap cache on
   older APIs, computed once per track and cached in-memory (LRU) keyed by album art URI.
