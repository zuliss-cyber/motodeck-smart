# MotoDeck//smart

A real Android home-screen replacement built for motorcycle riders. The launcher is a
native Kotlin/Jetpack Compose app with a genuine `HOME` intent filter, a racing-inspired
cockpit dashboard, GPS instrumentation, media controls, navigation shortcuts, an in-app
cockpit wallpaper and a customisable app dock.

> GPS speed is an estimate from the phone's location provider. It is not an ECU, wheel
> sensor or certified speedometer reading and must never replace the motorcycle's
> instruments or the rider's judgement.

## Download

The signed APK is published on the [latest GitHub release](https://github.com/zuliss-cyber/motodeck-smart/releases/latest).
Download `MotoDeck-smart-<version>.apk`, open it on the phone and allow installation from
your browser or file manager. Android 8.0 (API 26) or newer is required.

## Features

- **Icy HUD cockpit dashboard** — deep teal-black surfaces, icy blue accent palette,
  rounded glass-like panels, subtle technical grid and aurora glow, a glowing segmented
  gauge, digital italic typography and large touch targets. Dark, light and system themes.
  The default accent is **Ice Blue**, with orange, red, amber, cyan and lime alternatives.
- **Cockpit wallpaper** — choose a photo with the system picker and show it behind the
  cockpit panels inside MotoDeck//smart. The image is copied into app-private storage; the
  phone's home and lock screen wallpaper are never changed. No storage permission is required.
- **Clock and date** — live seconds, 12/24-hour formats and a compact clock that scales
  down further in landscape and riding layouts.
- **GPS speed** — current, maximum and average speed plus trip distance, accuracy and
  fix status, shown on a segmented racing gauge. Clearly labelled as GPS-estimated.
- **Compass** — rotation-vector heading with an accelerometer/magnetometer fallback and a
  reported state when no sensor exists.
- **Weather** — Open-Meteo (no key required) or OpenWeatherMap (local API key), or fully
  disabled. Shows temperature, condition, humidity, wind and feels-like.
- **Battery** — level, charging state and battery temperature. The top-bar battery pill
  is hidden automatically on narrow portrait widths.
- **GPS status** — permission, provider, accuracy, last fix, coordinates and a reverse
  geocoded local label.
- **Music** — active media session title, artist, album artwork and play/pause/next/
  previous controls, plus a shortcut to the music app.
- **Navigation** — destination search, saved destinations with optional coordinates and
  one-touch Google Maps launch through Android intents. No Maps API key is embedded.
- **App drawer** — searchable, alphabetically grouped application grid with favourites,
  long-press actions and app info.
- **Custom dock** — up to six apps, reorderable and removable from Settings.
- **Riding mode** — simplified high-contrast layout with a compact two-column landscape
  cockpit, explicit safety warning and optional screen-awake and forced dark cockpit.
- **Exit action** — available from the dashboard overflow menu and Settings. When
  MotoDeck//smart is the default home app, Exit opens Android's Home app settings so
  another launcher can be selected; otherwise the launcher closes normally.
- **Settings** — theme, accent, units, clock format, panel visibility, cockpit wallpaper, weather
  provider, permissions, dock editor, about, reset and exit. Persisted with Jetpack
  DataStore.

## Requirements

- Android Studio (Ladybug or newer recommended) **or** command-line Gradle
- JDK 17
- Android SDK platform 35 and build-tools 35.0.0
- minSdk 26 (Android 8.0), targetSdk/compileSdk 35

The project uses Gradle 8.9, Android Gradle Plugin 8.7.3, Kotlin 2.0.21 and Compose BOM
2024.10.01. The Gradle wrapper is included, so no separate Gradle installation is needed.
Current version: **1.1.0** (versionCode 2).

## Build the debug APK

From the project root:

```powershell
# Windows PowerShell
$env:JAVA_HOME = "C:\path\to\jdk-17"
$env:ANDROID_HOME = "C:\path\to\android-sdk"
.\gradlew.bat :app:assembleDebug
```

```bash
# macOS / Linux
export JAVA_HOME=/path/to/jdk-17
export ANDROID_HOME=/path/to/android-sdk
./gradlew :app:assembleDebug
```

The debug APK is written to:

```
app/build/outputs/apk/debug/app-debug.apk
```

The debug build uses the application id `com.motosmart.launcher.debug` so it can be
installed alongside a release build.

### Install and enable as home screen

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell cmd package set-home-activity com.motosmart.launcher.debug/com.motosmart.launcher.ui.LauncherActivity
```

Alternatively open Android **Settings → Apps → Default apps → Home app** and choose
**MotoDeck//smart**. The launcher also shows a "Make this your home screen" banner when it
is not the default, and Settings → Home screen provides a role request.

## Release APK

1. Create a keystore:

   ```bash
   keytool -genkeypair -v -keystore release.jks -alias moto \
     -keyalg RSA -keysize 2048 -validity 10000
   ```

2. Create `keystore.properties` in the project root (never commit it):

   ```properties
   storeFile=C:/absolute/path/to/release.jks
   storePassword=YOUR_STORE_PASSWORD
   keyAlias=moto
   keyPassword=YOUR_KEY_PASSWORD
   ```

   An example file is provided at `keystore.properties.example`.

3. Build:

   ```bash
   ./gradlew :app:assembleRelease
   ```

   The signed release APK is written to:

   ```
   app/build/outputs/apk/release/app-release.apk
   ```

If `keystore.properties` is absent, `assembleRelease` still builds but produces an
unsigned APK (or fails at signing depending on the Gradle/AGP version used).

## Distribute through GitHub

`DISTRIBUTION.md` walks through building the signed APK, publishing the source with GitHub
Desktop, attaching the APK to a GitHub Release, and publishing updates. The release
keystore (`release.jks`) and `keystore.properties` are git-ignored — back them up privately
and never commit them.

## Permissions and privacy

| Permission | Why it is used |
| --- | --- |
| `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` | GPS speed, compass position label, local weather and navigation shortcuts. Requested at runtime only when needed. |
| `INTERNET` | Weather requests and Android web/map fallbacks. |
| `ACCESS_NETWORK_STATE` | Report a useful weather error when offline. |
| Notification listener access | Read the active `MediaSession` for title, artist and transport controls. Granted explicitly through Android settings. |

- Location updates run **only while the dashboard is visible** and stop when the app is
  paused. There is no background location service and no wake lock.
- The launcher does not upload location. Coordinates are sent only to the selected
  weather provider when weather is refreshed.
- Notification content is never read, stored or transmitted.
- Preferences are stored locally with DataStore. There are no accounts, analytics or
  third-party tracking SDKs.
- A chosen cockpit wallpaper is copied into app-private storage (filesDir) and never leaves
  the device. It is shown only inside this launcher.
- Apps are discovered with `LauncherApps` and package visibility queries; the project
  does not request `QUERY_ALL_PACKAGES`.

## Weather configuration

- **Open-Meteo** is the default and needs no API key.
- **OpenWeatherMap** requires a free key from <https://openweathermap.org/>. Paste it in
  Settings → Weather. It is stored in local DataStore storage.
- **Off** hides live weather. The rest of the dashboard continues to work offline.

## Project structure

```
app/src/main/java/com/motosmart/launcher/
├── MotoApplication.kt              Application + dependency container
├── data/
│   ├── applications/               LauncherApps discovery, dock defaults, launching
│   ├── battery/                    Sticky battery broadcasts
│   ├── location/                   GPS-only session telemetry and reverse geocoding
│   ├── music/                      MediaSession + NotificationListenerService
│   ├── navigation/                 geo:/google.navigation: intents and fallbacks
│   ├── preferences/                DataStore settings model/repository
│   ├── sensors/                    Rotation-vector compass
│   └── weather/                    Open-Meteo/OpenWeatherMap client
├── ui/
│   ├── appdrawer/                  Searchable app grid, favourites, long-press actions
│   ├── components/                 Shared cockpit surfaces, fields and headers
│   ├── dashboard/                  Dashboard screen, cards and view model
│   ├── navigation/                 Destination search and saved places
│   ├── settings/                   Settings and dock editor
│   ├── theme/                      Palettes, typography and Material 3 theme
│   └── LauncherActivity.kt         Compose root, HOME handling, permissions
└── util/                           Formatting, headings, home-role and cockpit wallpaper helpers

app/src/test/java/com/motosmart/launcher/
├── util/                           Unit formatter and compass direction tests
└── data/weather/                   Weather code mapping tests
```

## Verification

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug
./gradlew :app:assembleDebug
```

The current source state was built with all three commands:

- `:app:assembleDebug` — **passed**, APK produced at `app/build/outputs/apk/debug/app-debug.apk`
- `:app:testDebugUnitTest` — **passed**, 13 unit tests, 0 failures
- `:app:lintDebug` — **passed**, 0 errors

The unit tests cover speed/distance/clock formatting, compass headings and weather-code
mapping. Runtime behaviour (GPS reception, media sessions, weather replies and maps
availability) depends on the device, installed apps, network and location permissions and
must be checked on the target phone.
