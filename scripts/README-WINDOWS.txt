DeskCharm - Windows build output
================================

Produced by scripts\build-windows.ps1 (and by the GitHub Actions workflow):

  DeskCharm\                         Portable app folder. Double-click DeskCharm.exe
                                     inside it. Nothing to install.
  DeskCharm-<version>-portable.zip   The same folder, zipped (local builds / releases).
  DeskCharm-<version>.msi            Windows Installer package (double-click to install).
  DeskCharm-<version>.exe            Installer with the same contents as the .msi.

All of them bundle their own Java and JavaFX runtime - you do NOT need to install Java,
JavaFX, Maven, VS Code or Eclipse to run DeskCharm.

The installers install per-user (no administrator rights needed) and add a Start Menu
shortcut and a Desktop shortcut. Uninstall from Settings -> Apps -> DeskCharm.

Settings and imported custom charm images are stored per-user in:
    %APPDATA%\DeskCharm\

If the app does not appear
--------------------------
DeskCharm has no main window: it shows a charm hanging from the top of the screen plus a
tray icon (bottom-right, possibly hidden under the ^ arrow).
Run DeskCharm\Run-DeskCharm-with-console.bat - it starts the same app but keeps a console
open so any error message stays visible.

If Windows blocks the download
------------------------------
The files are not code-signed, so SmartScreen may show "Windows protected your PC".
Click "More info" -> "Run anyway", or right-click the downloaded .zip -> Properties ->
tick "Unblock" BEFORE extracting it. In PowerShell:  Get-ChildItem -Recurse . | Unblock-File
