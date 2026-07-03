# Hablas

Android APK generated from the uploaded premium glassmorphic music player package.

## Build

```bash
npm ci
npm run build
mkdir -p app/src/main/assets
cp dist/index.html app/src/main/assets/index.html
gradle :app:assembleDebug
```

The GitHub workflow builds and publishes `Hablas-v*.apk` on tag push.
