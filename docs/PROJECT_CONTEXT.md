# ctOS Launcher — Project Context

**Project:** DedSec / Watch Dogs 2 themed Android Launcher  
**Target device:** Realme 8 Pro (Android 13, gesture-only navigation — no home button)  
**Stack:** Kotlin 2.0.0, Jetpack Compose, Material 3, AGP 8.5.2, Gradle 8.9  
**Repo:** https://github.com/arslanur/dedsec-ctos-launcher  

---

## What We Built

### Core App Structure

```
app/src/main/
├── AndroidManifest.xml
├── kotlin/com/dedsec/launcher/
│   ├── MainActivity.kt
│   ├── data/
│   │   ├── Models.kt               — AppInfo, BatteryState, MediaInfo, NetworkInfo, MediaAction
│   │   ├── LauncherViewModel.kt    — central ViewModel (apps, battery, media, network, clock, wallpaper, control centre)
│   │   └── WallpaperRepository.kt  — BUNDLED_WALLPAPERS list + SharedPrefs load/save
│   ├── service/
│   │   └── MediaNotificationService.kt — NotificationListenerService, exposes currentTrack flow
│   └── ui/
│       ├── theme/
│       │   ├── Color.kt            — NeonMagenta, NeonMagentaDim, PureBlack, CrispWhite, DimWhite, etc.
│       │   ├── Type.kt
│       │   └── Theme.kt            — DedSecTheme
│       ├── components/
│       │   ├── WallpaperBackground.kt    — renders bundled/custom wallpaper + halftone overlay
│       │   ├── WallpaperPickerSheet.kt   — bottom sheet to pick bundled or gallery wallpaper
│       │   ├── TelemetryHeader.kt        — HH:MM:SS clock, battery%, IP, day progress bar
│       │   ├── DedSecMediaWidget.kt      — now-playing card with skull art + playback controls
│       │   ├── HomeGrid.kt               — pinned-app grid with diamond-every-5th pattern
│       │   ├── HalftoneBackground.kt     — halftone dot pattern canvas
│       │   ├── CornerBracketBox.kt       — reusable magenta corner-bracket decoration
│       │   └── ControlCentrePanel.kt     — ctOS Control Centre overlay (new)
│       └── screens/
│           ├── BootSequenceScreen.kt     — ctOS boot animation on first launch
│           ├── HomeScreen.kt             — main launcher screen, gesture routing
│           └── AppDrawerSheet.kt         — full-screen app drawer with search
└── res/
    └── drawable/
        ├── wallpaper_dedsec_01.jpg   — neon pink skull halftone
        ├── wallpaper_dedsec_02.jpg   — DedSec skull with concentric dot circle
        ├── wallpaper_dedsec_03.jpg   — Dawn of DedSec purple/green glitch poster
        ├── wallpaper_dedsec_04.jpg   — Watch Dogs Legion red "W" logo on teal
        ├── wallpaper_dedsec_05.jpg   — Black/white/red comic "OMG IT'S DEDSEC" takeover
        └── wallpaper_dedsec_06.jpg   — Purple comic "FIREWALL OF LI..." DedSec art
```

---

## Feature Breakdown

### Boot Sequence
- Plays a ctOS-style terminal boot animation on first app launch
- Dismissed by `viewModel.dismissBoot()` after animation completes
- Uses `AnimatedContent` to cross-fade into HomeScreen

### Home Screen Gestures
| Gesture | Action |
|---------|--------|
| Swipe up (anywhere) | Open app drawer |
| Swipe down from **top-right** | Open ctOS Control Centre |
| Swipe down from **top-left** | Expand Android notification shade |
| Long-press | Open wallpaper picker |
| Back gesture (Realme) | Close whichever overlay is open |

### TelemetryHeader
- Live clock (HH:MM:SS, updates every second)
- Battery % with charging indicator
- Network type (WiFi / Cell) and local IP address
- Day progress bar (fills left-to-right from midnight to midnight)

### Media Widget
- Reads now-playing track via `NotificationListenerService`
- Shows title, artist, play/pause, skip prev/next, shuffle toggle
- Custom skull Canvas artwork as album art placeholder
- Magenta notched-corner border design

### Home Grid (Pinned Apps)
- 4-column LazyVerticalGrid, scroll disabled
- Every 5th icon (index % 5 == 2) rendered as a 45° rotated diamond
- Grayscale + brighten color filter on all icons
- Long-press any icon to unpin it

### App Drawer
- Full-screen overlay, slides up from bottom
- `root@ctOS:~# grep` search bar, auto-focuses keyboard on open
- 4-column grid of all installed apps
- Long-press any app → context menu: PIN TO HOME / APP INFO
- Dismiss: swipe down on drag handle, back gesture, or tap backdrop
- Search filters in real-time (case-insensitive)

### Wallpaper System
- 6 bundled wallpapers (`wallpaper_dedsec_01` through `_06`)
- Gallery picker for custom wallpapers (persists URI permission across reboots)
- State persisted in SharedPreferences (`launcher_prefs`)
- Halftone dot-matrix overlay drawn on top of any wallpaper

### ctOS Control Centre (new)
Half-screen panel that slides down from the top. Activated by swipe-down from top-right edge.

**8 Toggles:**
| Toggle | Behaviour |
|--------|-----------|
| WIFI | Opens WiFi Settings |
| DATA | Opens Wireless Settings |
| BT (Bluetooth) | Opens Bluetooth Settings (Android 13 can't toggle programmatically) |
| TORCH | Direct toggle via `CameraManager.setTorchMode()` — no CAMERA permission needed |
| ROTATE (Auto-rotate) | Direct toggle via `Settings.System.ACCELEROMETER_ROTATION` — needs WRITE_SETTINGS |
| DND | Direct toggle via `NotificationManager.setInterruptionFilter()` — needs ACCESS_NOTIFICATION_POLICY |
| FLIGHT (Airplane Mode) | Opens Airplane Mode Settings |
| PSAVE (Power Save) | Opens Battery Saver Settings |

**2 Sliders:**
- **BRITE** — Screen brightness (0–255 mapped to 0–1 float); switches to manual brightness mode; needs WRITE_SETTINGS
- **VOL** — Media volume via `AudioManager.STREAM_MUSIC`; needs MODIFY_AUDIO_SETTINGS

**Permission handling:** WRITE_SETTINGS and ACCESS_NOTIFICATION_POLICY are special permissions that can't be granted at install time. On first use, the app opens the correct system settings screen. After granting, toggles work directly.

---

## Permissions

```xml
QUERY_ALL_PACKAGES           — enumerate installed apps
ACCESS_NETWORK_STATE         — read WiFi/Cell state
ACCESS_WIFI_STATE            — read WiFi SSID/state
RECEIVE_BOOT_COMPLETED       — (reserved for future boot receiver)
READ_PHONE_STATE             — read network type
EXPAND_STATUS_BAR            — expand notification shade via StatusBarManager reflection
CHANGE_WIFI_STATE            — (declared, WiFi toggling goes via Settings on Android 10+)
BLUETOOTH_CONNECT            — read BT state on Android 12+
BLUETOOTH_ADMIN              — (legacy, BT toggling goes via Settings on Android 13)
WRITE_SETTINGS               — brightness + auto-rotate (special permission, user grants in Settings)
ACCESS_NOTIFICATION_POLICY   — DND toggle (special permission, user grants in Settings)
MODIFY_AUDIO_SETTINGS        — volume slider
```

---

## CI/CD Pipeline

**File:** `.github/workflows/build-apk.yml`

The CI bypasses the Gradle wrapper to avoid Maven Central dependency resolution failures caused by the `gradle/actions/setup-gradle@v3` Develocity interceptors. Instead it:
1. Downloads the Gradle 8.9 binary distribution directly from `services.gradle.org`
2. Runs `bin/gradle assembleDebug` directly (no wrapper JAR)
3. Uploads the resulting APK as a GitHub Actions artifact

**Key insight:** Running `gradle wrapper` or using `setup-gradle@v3` triggers project evaluation during the wrapper phase, which downloads `kotlin-reflect`, `slf4j-api`, etc. from Maven Central — this was intermittently failing. The fix is to skip the wrapper entirely.

**How to install the APK on device:**
1. Go to Actions tab → latest passing run → download artifact
2. Extract the zip to get the `.apk`
3. Enable "Install unknown apps" for your browser/Files app in Settings
4. Open the APK → install → dismiss Play Protect warning (tap "Install anyway")
5. Go to Settings → Default apps → Home app → select "ctOS Launcher"

---

## Known Limitations / Deferred Features

- **Lock screen**: Planned but skipped. Fingerprint-on-screen unlock with ctOS theme was discussed but decided against implementing.
- **WiFi / Bluetooth direct toggle**: Android 10+ (WiFi) and Android 13+ (BT) block programmatic toggling. Both open the respective Settings screen instead.
- **Airplane Mode direct toggle**: Restricted to system apps. Opens settings.
- **Power Save direct toggle**: Restricted to system apps. Opens settings.
- **WRITE_SETTINGS / ACCESS_NOTIFICATION_POLICY**: These are "special" permissions — first use of brightness/auto-rotate/DND sliders/toggles will redirect to the system grant screen once, then work directly.

---

## Design Language

| Element | Value |
|---------|-------|
| Primary accent | `NeonMagenta` — `#E6007E` |
| Dim accent | `NeonMagentaDim` — `#60E6007E` (60% alpha) |
| Background | `PureBlack` — `#000000` |
| Surface | `SurfaceDark` — `#0A0A0A` |
| Text primary | `CrispWhite` — `#FFFFFF` |
| Text secondary | `DimWhite` — `#B3FFFFFF` (70% alpha) |
| Font | `FontFamily.Monospace` throughout |
| Icon style | Grayscale + 1.4× brightness boost |
| Borders | 1–1.5dp magenta, `RoundedCornerShape(2–4dp)` |
| Corner decorations | Notch-bracket Canvas drawn at corners |

---

## Development Notes

### ColorMatrix Gotcha
Compose's `androidx.compose.ui.graphics.ColorMatrix` does NOT have `setSaturation()` or `postConcat()`. Use `android.graphics.ColorMatrix` for operations, then convert:
```kotlin
val am = android.graphics.ColorMatrix()
am.setSaturation(0f)
ColorFilter.colorMatrix(ColorMatrix(am.array))
```

### Compose Stroke Parameter
`androidx.compose.ui.graphics.drawscope.Stroke` uses `width`, not `strokeWidth`:
```kotlin
drawRect(color, style = Stroke(width = 1.5f))  // correct
```

### PaddingValues Overloads
No overload mixes `horizontal`/`vertical` with individual sides. Use all four named args:
```kotlin
PaddingValues(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 32.dp)
```

### Realme Gesture Navigation
The Realme 8 Pro has no physical/virtual home button. All "go home" and "go back" actions are edge swipes. To close overlays, `BackHandler` must live **inside** the overlay composable — a global `BackHandler(enabled = true) {}` in MainActivity blocks everything.
