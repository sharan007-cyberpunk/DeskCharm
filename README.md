# DeskCharm (Stages 1–4 of a staged build)

An original Windows desktop overlay: a decorative charm hangs from a physically
simulated rope near the top of the screen. You can hover it, grab it, drag it,
and throw it, and it swings and settles with Verlet-integrated rope physics.

This project is independently inspired by the public concept behind the
"Hangly" macOS app (a charm-on-a-rope desktop ornament). **No original Hangly
source code, artwork, icon, or branding is used anywhere in this project.**
All shapes, names, and code here are original.

> **Status: Stages 1–4 of a multi-stage build.** Every package in the
> original spec is now populated, every requested behavior is implemented,
> and the Windows packaging pipeline (jlink + jpackage + installer +
> GitHub Actions) is in place and ready to run on a `windows-latest` runner.
> What remains is validating the packaging pipeline and the native
> click-through code on real Windows hardware/CI, since this development
> environment cannot run Windows itself — see "Final testing" below for
> exactly what to check once you push this to GitHub.

## Features (Stages 1–4)

- Verlet-integrated rope: 20 segments / 21 nodes, fixed 1/240s timestep,
  gravity, damping, multi-iteration distance constraints, stretch limiting,
  sleep detection.
- Smooth rope rendering via quadratic curves (not a rough polyline), with
  shading, beads sampled along the rope curve, and a shadowed charm.
- `CharmRenderer` now draws every built-in `CharmType` (rounded diamond,
  star, circle pendant, crescent, leaf) using each charm's own color and
  size, and draws a user's imported custom image in place of a shape when
  a `CUSTOM_IMAGE` charm is selected.
- Transparent, undecorated, always-on-top JavaFX overlay window, now built
  entirely from an `AppEnvironment`: window height, rope thickness, gravity,
  damping, bead/animation visibility, monitor selection, and the on-screen
  offset all come from `AppSettings` rather than being hard-coded.
- Hover, grab, drag, and release-to-throw mouse interaction, driven by a
  dedicated `DragController`/`MouseTracker`/`InteractionState` state machine
  in the `interaction` package.
- An original 5-charm library (`Star`, `Moon`, `Planet`, `Diamond`, `Leaf`) —
  no Hangly artwork — plus `Charm`/`CharmType` data model.
- Custom image import (`CharmImageProcessor`): validates PNG/JPG/JPEG,
  resizes to fit while preserving aspect ratio and transparency, and copies
  the result into app storage; `CustomCharmStore` persists the
  imported-charm index as JSON, and `AppEnvironment` now loads it back in on
  startup, so imported charms are selectable again after a restart.
- JSON settings persistence (`AppSettings`/`SettingsStore`) under
  `%APPDATA%\DeskCharm\settings.json`, with validation/clamping, corrupt-file
  backup-and-recover, and safe defaults — no global mutable singleton; an
  explicit `AppEnvironment` holds the one active instance of each system and
  is handed to whatever needs it.
- `WindowManager` centralizing overlay show/hide/reset-position, screen
  enumeration, and applying settings changes (live-refreshing visibility
  toggles, or tearing down and relaunching the overlay when rope
  length/gravity/damping actually change the physics config).
- A JavaFX **settings window** (`SettingsWindow`/`SettingsController`/
  `MenuController`): charm dropdown, rope length/gravity/damping sliders,
  a **monitor picker** (multi-monitor support), beads/shadows/sound/animation
  checkboxes, an Import Custom Charm file chooser, Reset Settings, and Exit —
  plus a File/View menu bar.
- A Windows **system tray** icon (`SystemTrayManager`, `java.awt.SystemTray`)
  with show/hide, open settings, a charm submenu, an animation toggle, reset
  position, and exit — and a graceful no-op fallback (logged, not thrown) on
  a platform without tray support.
- **Native Windows click-through** (`WindowsClickThrough`, via JNA): the
  overlay toggles `WS_EX_TRANSPARENT` on/off in real time based on whether
  the cursor is over the charm's hit region, so the rest of the transparent
  window genuinely passes clicks through to the desktop.
- **Optional sound effects** (`SoundEffectPlayer`, Java Sound API only, no
  audio assets): short programmatically generated tones for grab/release/
  throw, gated behind `AppSettings.isSoundEnabled()` (off by default).
- **Windows packaging pipeline**: `scripts/build-windows.ps1` / `.bat` /
  `package-windows.ps1` (jlink + jpackage, producing a portable app folder
  and a `.exe` installer in `dist/`) and
  `.github/workflows/windows-build.yml` (builds the same pipeline on a
  `windows-latest` GitHub Actions runner and uploads the result as a
  downloadable `DeskCharm-Windows-x64` artifact).

## Technology stack

- Java 21
- JavaFX 21.0.4 (controls, graphics, fxml, swing)
- Maven (`javafx-maven-plugin` for `mvn javafx:run`, `maven-shade-plugin` for
  a runnable fat jar used by packaging in a later stage)
- Jackson (wired into the POM now; used once the settings system lands)
- JUnit 5

## Requirements

- **Java 21 JDK** — verify with `java -version` (expects `21.x`).
- **Maven 3.9+** — verify with `mvn -version`.
- Windows 10/11 64-bit to run the overlay itself (the physics/tests run on
  any OS; the transparent overlay is Windows-targeted per the project brief).

### Installing Java 21 (Windows)

Download an OpenJDK 21 build (e.g. Eclipse Temurin) and install it, then
confirm in PowerShell:

```powershell
java -version
```

### Installing Maven (Windows)

Download Maven, unzip it, add its `bin` folder to `PATH`, then confirm:

```powershell
mvn -version
```

## Running

```powershell
cd DeskCharm
mvn clean javafx:run
```

**Expected result:** a small transparent window appears near the top-center
of your primary screen with a rope, three beads (if enabled), and your
selected charm hanging from it. Moving the mouse over the charm shows a hand
cursor; click-and-drag moves the charm and rope; releasing while moving
throws it, and it swings and settles. If your platform supports it, a
DeskCharm tray icon also appears — right-click it for Show/Hide, Open
Settings, a charm submenu, Animation toggle, Reset Position, and Exit.
Opening the tray's "Open Settings..." shows the settings window described
below.

## Running the tests

```powershell
mvn test
```

This runs: `Vector2Test` (vector arithmetic), `RopeSimulationTest` (anchor
pinning, gravity settling, stretch limiting, sleep detection/wake-up, and
throw-velocity transfer), `SettingsStoreTest` (default fallback, save/load
round-trip, corrupt-JSON recovery, value clamping), and `CharmLibraryTest`
(built-in charm count, selection, custom charm registration, built-in charms
being protected from removal). Further tests (drag/throw interaction,
image-import validation) are added as the `interaction` and `charm` packages
grow in the next stage.

## VS Code setup

1. Install the "Extension Pack for Java" from the Extensions marketplace.
2. Open the `DeskCharm` folder in VS Code.
3. VS Code will detect the `pom.xml` and configure the project automatically.
4. Use the built-in Maven side panel, or a terminal inside VS Code, to run
   `mvn clean javafx:run`.

## Eclipse setup

1. `File -> Import -> Maven -> Existing Maven Projects`.
2. Select the `DeskCharm` folder and finish the import.
3. Eclipse will resolve dependencies from `pom.xml` automatically.
4. Run configurations: right-click the project -> `Run As -> Maven build...`,
   set Goals to `clean javafx:run`.

## Project structure (Stages 1–4 files)

```
DeskCharm/
├── pom.xml
├── README.md
├── .gitignore
├── .github/workflows/
│   └── windows-build.yml
├── scripts/
│   ├── build-windows.ps1
│   ├── build-windows.bat
│   ├── package-windows.ps1
│   └── README-WINDOWS.txt
├── src/main/java/com/sharan/deskcharm/
│   ├── Main.java
│   ├── DeskCharmApplication.java
│   ├── app/
│   │   └── AppEnvironment.java
│   ├── audio/
│   │   └── SoundEffectPlayer.java
│   ├── physics/
│   │   ├── Vector2.java
│   │   ├── RopeNode.java
│   │   ├── RopeConfiguration.java
│   │   ├── RopeSimulation.java
│   │   ├── SimulationClock.java
│   │   └── Bead.java
│   ├── rendering/
│   │   ├── RopeRenderer.java
│   │   ├── BeadRenderer.java
│   │   ├── CharmRenderer.java
│   │   └── ShadowRenderer.java
│   ├── interaction/
│   │   ├── InteractionState.java
│   │   ├── MouseTracker.java
│   │   └── DragController.java
│   ├── charm/
│   │   ├── Charm.java
│   │   ├── CharmType.java
│   │   ├── CharmLibrary.java
│   │   ├── CustomCharmStore.java
│   │   └── CharmImageProcessor.java
│   ├── settings/
│   │   ├── AppSettings.java
│   │   └── SettingsStore.java
│   ├── windows/
│   │   ├── OverlayWindow.java
│   │   ├── WindowManager.java
│   │   ├── SystemTrayManager.java
│   │   └── WindowsClickThrough.java
│   └── ui/
│       ├── SettingsWindow.java
│       ├── SettingsController.java
│       └── MenuController.java
├── src/main/resources/
│   ├── application.properties
│   ├── styles/
│   │   └── settings.css
│   └── charms/        (populated at runtime by custom-charm imports)
└── src/test/java/com/sharan/deskcharm/
    ├── Vector2Test.java
    ├── RopeSimulationTest.java
    ├── SettingsStoreTest.java
    ├── CharmLibraryTest.java
    └── SoundEffectPlayerTest.java
```

Every package folder named in the original spec is populated, plus one
addition beyond the original tree — `audio/SoundEffectPlayer.java` — since
the spec's sound requirement (section 16) didn't specify which package it
belonged in.

## Physics explanation

The rope is 21 point masses (`RopeNode`) connected by 20 distance
constraints, integrated with **Verlet integration**: each unpinned node's new
position is `position + damped(position - previousPosition) + gravity*dt²`,
so velocity is implicit rather than stored directly — this is what makes
Verlet trivially stable under repeated constraint correction. `RopeSimulation`
runs on a **fixed 1/240s timestep** via `SimulationClock`'s accumulator
pattern, so physics behavior is identical regardless of the display's actual
frame rate. Each step: (1) pin the anchor and, if dragging, the bottom node;
(2) integrate; (3) run 8 Gauss-Seidel-style constraint relaxation passes to
pull segments back toward rest length; (4) a hard stretch-limiting pass caps
any segment at 112% of rest length so fast drags/throws can't stretch the
rope unrealistically; (5) update a sleep counter so a rope at rest stops
consuming CPU until woken by a hover or drag.

## Rendering explanation

`RopeRenderer`, `BeadRenderer`, and `CharmRenderer` only **read** the
simulation's node list — they never mutate physics state, keeping the two
concerns cleanly separated as required. The rope is drawn as a sequence of
quadratic Bézier segments through node midpoints (a common "smooth polyline"
technique) rather than a rough straight-line polyline. Beads sample their
render position by linear interpolation between the two nearest rope nodes
at a fixed rest fraction along the rope, so they can never visually detach
from it. The charm is an original rounded-diamond pendant shape with a radial
gradient fill, a soft elliptical drop shadow, and a small stroked connector
line to the rope — deliberately not resembling Hangly's artwork.

## Charm system explanation

`Charm` is an immutable data holder (id, display name, `CharmType`, size,
colors, optional image/sound path, relative mass); `CharmLibrary` holds the
five built-in charms plus any registered custom ones and tracks which is
selected. Custom charms are never truly deleted from disk on `unregister` —
only built-ins are protected outright from removal — and `CustomCharmStore`
is the JSON-backed index (`custom-charms.json` under app storage) that lets
imported charms survive a restart; it silently skips any entry whose image
file has since gone missing, rather than crashing. `CharmImageProcessor`
does the actual import: it checks the extension, decodes the file as a real
image (not just a magic-byte sniff), resizes it to at most 256px on its
longest side while preserving aspect ratio and alpha, and writes a fresh PNG
copy into app storage — it does **not** attempt AI background removal; a
JPG or an opaque PNG will show its full rectangular bounds as the charm.

## Settings system explanation

`AppSettings` is a plain mutable bean with documented defaults for every
field and a `validateAndRepair()` pass that clamps anything out of a sane
range (e.g. damping outside `(0.80, 1.0]`) back to a safe value — this runs
both after loading from disk and immediately before saving. `SettingsStore`
resolves `%APPDATA%\DeskCharm\settings.json` (falling back to a dotfile
under the user's home directory on non-Windows systems, so tests and any
future macOS/Linux port aren't blocked), and on a corrupt or unparsable file
it renames the bad file aside as `settings.json.corrupt` and returns fresh
defaults rather than throwing — the corrupt file is preserved for
inspection, not silently deleted. There is deliberately no static/singleton
settings instance anywhere: `AppEnvironment` owns the one `SettingsStore`
and the one loaded `AppSettings` for the running application and is passed
explicitly to whatever needs them.

## Settings window & system tray explanation

`SettingsWindow` is a plain (non-transparent) JavaFX `Stage`, separate from
the overlay, containing a charm `ComboBox`, three `Slider`s (rope length,
gravity, damping), four `CheckBox`es (beads/shadows/sound/animation), and
Import/Reset/Exit buttons, laid out in a `GridPane`; a `MenuController`-built
`MenuBar` duplicates the Import/Reset/Exit actions as menu items plus a
"Close Settings Window" item, since some users expect a menu bar and others
expect buttons. All of the actual behavior lives in `SettingsController`,
which never touches the UI controls directly — each control's change
listener calls one `SettingsController` method, which mutates `AppSettings`
through `AppEnvironment` and then asks `WindowManager.applySettingsChanges(...)`
to either live-refresh the overlay (visibility/animation toggles, charm
selection) or fully rebuild the rope simulation (rope length, gravity,
damping — since those change `RopeConfiguration` itself).

`SystemTrayManager` wraps `java.awt.SystemTray`/`TrayIcon`/`PopupMenu`
directly rather than any JavaFX API, since JavaFX still has no native tray
support; it draws its own small circular icon in code (see
`renderTrayIconImage()`) so no bitmap asset — and no risk of resembling
Hangly's icon — is needed. `installIfSupported()` checks
`SystemTray.isSupported()` first and simply logs a warning and returns
`false` if the platform doesn't support it, per the "fail gracefully"
requirement; the settings window's own Exit button remains available either
way.

## Windows limitations (Stage 1–4)

- `WindowsClickThrough` resolves the overlay's native HWND via JavaFX's
  internal glass window peer through reflection (there is no public,
  stable API for this in JavaFX 21). This is inherently a little fragile
  across JavaFX point releases: it is wrapped defensively so any failure
  (wrong method name for your exact JavaFX version, running on a non-Windows
  OS, etc.) simply disables click-through and logs a warning rather than
  crashing the app — the overlay stays fully usable, just without true
  click-through, in that case. **This has not been run against real Windows
  hardware from this environment**; if the reflective lookup needs
  adjustment for your exact JavaFX build, `resolveHwnd()` in
  `WindowsClickThrough.java` is the only place that needs to change.
- `SystemTrayManager` and the settings window's monitor picker have been
  written against the documented `java.awt.SystemTray`/`javafx.stage.Screen`
  APIs and unit-testable pieces are covered by tests, but the visual/tray
  behavior itself is likewise best verified on real Windows — see "Final
  testing" below.

## Building the Windows package (jlink + jpackage)

Locally, once you're on a Windows machine with JDK 21 (which bundles
`jlink`/`jpackage`) and Maven installed:

```powershell
cd DeskCharm
powershell -ExecutionPolicy Bypass -File scripts\build-windows.ps1
```

This runs the full pipeline from the spec: verify Java/Maven -> `mvn clean`
-> `mvn test` -> `mvn package` (produces `target\deskcharm-shaded.jar`) ->
`jlink` (custom runtime image in `runtime\`) -> `jpackage` app-image ->
`jpackage` installer -> everything placed in `dist\`:

```
dist/
├── DeskCharm/              (portable app folder — DeskCharm\DeskCharm.exe)
├── DeskCharm-1.0.0.exe     (Windows installer)
└── README-WINDOWS.txt
```

Double-clicking `scripts\build-windows.bat` runs the same thing without
opening PowerShell yourself.

**If you don't have a Windows machine:** push this project to GitHub. The
included `.github/workflows/windows-build.yml` runs the same test-and-package
pipeline on a `windows-latest` GitHub Actions runner and uploads the result
as a downloadable artifact named `DeskCharm-Windows-x64` — from the repo's
"Actions" tab, open the workflow run, and the artifact is listed under
"Artifacts" at the bottom of the run summary page as a downloadable zip
containing the same `dist/` contents described above. This is how you get
an actual `.exe`/installer without owning a Windows build machine yourself,
since this development environment can't compile or sign a Windows binary
directly.

## Troubleshooting

- **`mvn: command not found`** — Maven isn't on your `PATH`; re-check the
  Maven install step above.
- **JavaFX runtime components missing / module errors** — make sure you're
  running via `mvn javafx:run` (which wires up the JavaFX module path for
  you) rather than trying to `java -jar` the plain jar directly; direct jar
  execution needs an explicit `--module-path`, which is handled for you once
  packaging (Stage: Windows Packaging) is in place.
- **Nothing appears on screen** — check that the window didn't spawn off
  your primary display's visible bounds if you have an unusual multi-monitor
  layout; multi-monitor selection is a planned stage.

## Final testing checklist (do this on real Windows/CI before calling it done)

Per the project's own acceptance criterion, verify each of these once you
have Windows access (locally or via the GitHub Actions artifact):

1. `mvn clean test` passes (all four test classes).
2. `mvn clean javafx:run` starts without errors and shows the overlay.
3. The transparent overlay renders correctly (no black box around it).
4. Rope physics looks right: swings under gravity, settles, doesn't jitter.
5. Dragging the charm feels responsive; the rope follows it.
6. A fast drag-and-release throws the charm with visible momentum.
7. Hovering/dragging away from the charm lets clicks reach the desktop
   underneath (validates `WindowsClickThrough`).
8. The settings window opens from the tray, and every control (charm,
   sliders, monitor picker, checkboxes) visibly changes the overlay.
9. Importing a custom PNG/JPG shows it as the charm; restarting the app
   keeps it selectable (validates `CustomCharmStore` round-tripping).
10. The tray icon appears and every menu item works; killing/reopening
    doesn't duplicate tray icons.
11. Exiting via the settings window or tray closes the app cleanly (no
    orphaned process).
12. `scripts\build-windows.ps1` completes and produces `dist\DeskCharm\
    DeskCharm.exe` and `dist\DeskCharm-1.0.0.exe`.
13. The portable `DeskCharm.exe` launches without a separately installed
    JDK/JavaFX on a clean Windows machine/VM.
14. The installer installs, creates Start Menu/Desktop shortcuts as chosen,
    and uninstalls cleanly from "Apps & Features".
15. The GitHub Actions workflow run succeeds and its
    `DeskCharm-Windows-x64` artifact contains the same three items.

## Future improvements (beyond this build)

1. Additional unit tests around `DragController` interaction sequencing,
   `CharmImageProcessor` validation/error paths, and `CustomCharmStore`
   round-tripping under concurrent access.
2. Code-signing the installer (currently unsigned, so Windows SmartScreen
   will show an "unknown publisher" warning on first run — expected for an
   unsigned indie app, but worth a signing certificate before wide
   distribution).
3. An actual `.ico` app icon (currently jpackage uses its own default icon;
   pass `--icon assets\deskcharm.ico` in `package-windows.ps1` once one
   exists — must be original artwork, not adapted from Hangly's icon).
4. MSI packaging alongside the EXE installer (`--type msi` is a one-line
   change in `package-windows.ps1` if your organization prefers MSI for
   managed deployment).

## License

Original code and assets in this repository are provided under the MIT
License (add a `LICENSE` file with your preferred terms before publishing).
This project contains no code or assets copied from the referenced Hangly
repository.
