# Silent Music

Premium personal Android local music player inspired by iOS / MagicOS glassmorphism.

## Stack

- Kotlin
- Jetpack Compose
- Media3 / ExoPlayer
- Room
- Hilt
- Jetpack Glance

## Core Features

- Local audio scan using MediaStore
- Deep glassmorphic UI with blurred album-art backdrop
- Favorites stored in Room
- Folder whitelist/exclude filtering stored in Room
- Background playback service foundation
- Home screen widget foundation
- GitHub Actions build and release workflow

## First release

The workflow supports manual release creation:

1. Open **Actions** in GitHub.
2. Run **Android Build & Release**.
3. Enter tag, e.g. `v1.0.0`.
4. The workflow builds the APK and creates a GitHub Release.

Or push a tag manually:

```bash
git tag v1.0.0
git push origin v1.0.0
```

## Important

The first workflow builds a debug-signed APK for personal use. Add proper signing secrets before public distribution.
