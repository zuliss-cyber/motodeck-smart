# Distributing MotoDeck//smart on GitHub

This guide covers building a signed release APK and publishing it through GitHub
Releases so anyone can install it without using Google Play.

---

## 1. What is already set up

| Item | Location | Committed to Git? |
| --- | --- | --- |
| Release keystore | `release.jks` | **No** (ignored by `.gitignore`) |
| Keystore passwords | `keystore.properties` | **No** (ignored by `.gitignore`) |
| Signing config | `app/build.gradle.kts` | Yes |
| Signed APK output | `app/build/outputs/apk/release/app-release.apk` | **No** (build output) |

> **Back up `release.jks` and `keystore.properties` now.** If you lose the keystore you
> can never publish an update that installs over the existing app — users would have to
> uninstall first. Keep a copy on an external drive or in a password manager. Never upload
> either file to GitHub.

---

## 2. Build a signed release APK

Open PowerShell in the project folder:

```powershell
$env:JAVA_HOME = "C:\Users\User\AppData\Local\AndroidToolchain\jdk-17.0.21.0.2+1"
$env:ANDROID_HOME = "C:\Users\User\AppData\Local\AndroidToolchain\sdk"
cd "C:\Users\User\Documents\Default Project\MotorcycleSmartLauncher"
.\gradlew.bat :app:assembleRelease --no-daemon
```

The signed APK is written to:

```
app/build/outputs/apk/release/app-release.apk
```

Verify the signature:

```powershell
& "$env:ANDROID_HOME\build-tools\35.0.0\apksigner.bat" verify --print-certs `
  "app\build\outputs\apk\release\app-release.apk"
```

Check that the APK declares the launcher role and the expected version:

```powershell
& "$env:ANDROID_HOME\build-tools\35.0.0\aapt2.exe" dump badging `
  "app\build\outputs\apk\release\app-release.apk" | Select-String "package:|launchable-activity|category.LAUNCHER"
```

Generate a SHA-256 checksum to publish next to the download:

```powershell
Get-FileHash "app\build\outputs\apk\release\app-release.apk" -Algorithm SHA256
```

---

## 3. Publish the source code to GitHub

The release page of a **private** repository is not downloadable by the public, so the
repository must be **public** if you want strangers to install the APK.

### With GitHub Desktop (no command line)

1. Install GitHub Desktop: <https://desktop.github.com/>
2. Sign in with your GitHub account (create one at <https://github.com/signup> if needed).
3. **File → Add local repository** → choose
   `C:\Users\User\Documents\Default Project\MotorcycleSmartLauncher`.
4. GitHub Desktop says the folder is not a repository — click **create a repository here**.
5. Name: `motodeck-smart`. Keep the local path as-is. Click **Create repository**.
6. Check the **Changes** list: it should show source files only — **no** `release.jks`,
   `keystore.properties`, `local.properties`, `build/` or `.gradle/`.
7. Commit message: `MotoDeck//smart 1.1.0`. Click **Commit to main**.
8. Click **Publish repository**, untick **Keep this code private**, then **Publish**.

### With Git on the command line

```powershell
cd "C:\Users\User\Documents\Default Project\MotorcycleSmartLauncher"
git init -b main
git add .
git status              # confirm release.jks / keystore.properties are NOT staged
git commit -m "MotoDeck//smart 1.1.0"
git remote add origin https://github.com/<YOUR-USERNAME>/motodeck-smart.git
git push -u origin main
```

---

## 4. Create the release

1. Open `https://github.com/<YOUR-USERNAME>/motodeck-smart/releases/new`.
2. **Choose a tag** → type `v1.1.0` → **Create new tag on publish**.
3. Release title: `MotoDeck//smart 1.1.0`.
4. Paste the release notes (features, changes, known limitations — see the template below).
5. Rename the built file to **`MotoDeck-smart.apk`** and drag it into the **Attach binaries**
   box. Keep this exact name for every release — see the permanent link below.
6. Click **Publish release**.

The human-friendly page is:

```
https://github.com/<YOUR-USERNAME>/motodeck-smart/releases/latest
```

Because the asset name never changes, this direct link always downloads the newest build:

```
https://github.com/<YOUR-USERNAME>/motodeck-smart/releases/latest/download/MotoDeck-smart.apk
```

Share that second link on WhatsApp, forums or a QR code — it never needs updating.

---

## 5. Release notes template

```markdown
## MotoDeck//smart 1.1.0

Icy HUD cockpit theme, in-app cockpit wallpaper, GPS dashboard for motorcycle riders.
A real Android home-screen replacement — not a widget or a mockup.

### Install
1. Download `MotoDeck-smart.apk` below.
2. Open the file and allow "Install unknown apps" for your browser or file manager.
3. Open MotoDeck//smart and accept the home-screen prompt.

Permanent link (always the newest build):
`https://github.com/<YOUR-USERNAME>/motodeck-smart/releases/latest/download/MotoDeck-smart.apk`

**SHA-256:** `<paste hash here>`

### What's new
- Icy blue HUD theme with aurora glow and rounded glass panels
- Cockpit wallpaper picker (does not change the system wallpaper)

### Notes
- Android 8.0 (API 26) or newer.
- GPS speed is an estimate from the phone's location provider — it is not a certified
  speedometer. Always trust your motorcycle's instruments.
- Weather uses Open-Meteo by default; no API key needed.
- No analytics, no accounts, no background location. See README for the full privacy notes.
```

---

## 6. Publishing an update

1. Change `versionCode` and `versionName` in `app/build.gradle.kts`.
   `versionCode` **must** increase every release.
2. Build `:app:assembleRelease` again with the **same** `release.jks`.
3. Commit and push the source change.
4. Create a new release with a new tag (`v1.1.1`, `v1.2.0`, …) and attach the new APK,
   still named `MotoDeck-smart.apk`.

Copy the APK to `MotoDeck-smart.apk` before uploading, then publish from the command line:

```powershell
Copy-Item "app\build\outputs\apk\release\app-release.apk" "MotoDeck-smart.apk"
gh release create v1.1.1 "MotoDeck-smart.apk" --title "MotoDeck//smart 1.1.1" --notes-file notes.md --latest
```

Users who already installed the app can install the new APK over the old one, because both
are signed with the same key and share the application id `com.motosmart.launcher`.

---

## 7. Troubleshooting

| Symptom | Cause / fix |
| --- | --- |
| `app-release-unsigned.apk` instead of `app-release.apk` | `keystore.properties` missing or has a wrong path/password. |
| "App not installed" when updating | The installed copy was signed with a different key (for example the debug build `com.motosmart.launcher.debug`, or an APK built on another machine). Uninstall the old copy once. |
| Play Protect warning on install | Expected for apps distributed outside Google Play. The user taps **Install anyway**. |
| Download link 404 | The repository is private, or the release is still a draft. |
| Build takes a long time | R8 minification is enabled for release. First build is the slowest. |
