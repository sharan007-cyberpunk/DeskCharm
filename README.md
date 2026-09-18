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

On Windows with JDK 21:

```powershell
.\scripts\build-windows.ps1
```

The script runs tests, builds the JAR and invokes jpackage when available.

GitHub Actions also builds a Windows installer automatically using `.github/workflows/windows-build.yml`.

## Data

User settings and imported charms are stored under `%APPDATA%\DeskCharm`.

## Notes

JavaFX transparent windows cannot provide perfect OS-level click-through semantics using only JavaFX. This implementation keeps the overlay bounded to a transparent region around the ornament so most of the desktop remains naturally interactive.

For a production release, code-signing and a Windows installer certificate should be added.
