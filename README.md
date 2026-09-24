# DeskCharm

A Windows JavaFX desktop ornament inspired by the documented interaction model of the public Hangly project.

This is an independent implementation. It does not copy Hangly source code, artwork, branding, or protected assets.

## Features

- Transparent borderless overlay
- 20-segment / 21-node Verlet rope
- Fixed 240 Hz physics
- Mouse grab, drag and throw
- Decorative beads
- Original geometric charms
- Custom PNG/JPG/JPEG image import
- Persistent JSON settings
- Windows system tray
- Multi-screen placement
- Optional launch-at-startup
- Maven project for VS Code and Eclipse
- GitHub Actions Windows packaging

## Requirements for development

- Windows 10/11 x64
- JDK 21
- Maven 3.9+
- VS Code or Eclipse

Run:

```powershell
mvn clean test
mvn javafx:run
```

## Eclipse

File -> Import -> Maven -> Existing Maven Projects -> select the project directory.

Run `Main.java` as a Java Application, or create a Maven run configuration for `javafx:run`.

## Windows packaging

On Windows with JDK 21 (and the WiX Toolset 3.x for the installers):

```powershell
.\scripts\build-windows.ps1
```

The script runs the tests, builds the jar, gathers the JavaFX/Gson/JNA dependencies and
runs `jpackage`. Output in `dist\`:

| File | What it is |
| --- | --- |
| `DeskCharm\` (and `DeskCharm-<version>-portable.zip`) | Portable app, run `DeskCharm.exe`, nothing to install |
| `DeskCharm-<version>.msi` | Windows Installer package |
| `DeskCharm-<version>.exe` | Installer (same payload as the `.msi`) |

Use `-SkipInstallers` if WiX is not installed and you only want the portable app.

### GitHub Actions

`.github/workflows/windows-build.yml` builds on `windows-latest` for every push to `main`,
every `v*` tag and on manual runs, then installs the `.msi` silently on the runner and
launches the app as a smoke test. Artifacts:

- `DeskCharm-Windows-x64` - the `.exe` and `.msi` installers
- `DeskCharm-Portable` - the portable folder (extract, run `DeskCharm.exe`)
- `DeskCharm-Logs` - smoke-test and MSI install logs

Pushing a tag such as `v1.0.1` also publishes the installers and the portable zip as a
GitHub Release, which is the easiest link to share. Push builds are versioned
`1.0.<run number>`, so each installer upgrades the previous one cleanly.

### Troubleshooting

- **Installer does nothing when double-clicked**: use the `DeskCharm-Portable` artifact or
  the `.msi` instead. The files are not code-signed, so SmartScreen can block them:
  right-click the downloaded `.zip` -> Properties -> **Unblock** before extracting.
- **App starts but nothing is visible**: DeskCharm has no main window. Look for the tray icon,
  or run `Run-DeskCharm-with-console.bat` from the portable folder to see any error text.

## Data

User settings and imported charms are stored under `%APPDATA%\DeskCharm`.

## Notes

JavaFX transparent windows cannot provide perfect OS-level click-through semantics using only JavaFX. This implementation keeps the overlay bounded to a transparent region around the ornament so most of the desktop remains naturally interactive.

For a production release, code-signing and a Windows installer certificate should be added.
