DeskCharm - Windows Build Output
=================================

DeskCharm\DeskCharm.exe   - Portable app folder; double-click DeskCharm.exe
                            inside the DeskCharm\ folder to run without
                            installing anything.
DeskCharm-1.0.0.exe       - Windows installer. Run it to install DeskCharm
                            with a Start Menu shortcut (and optional Desktop
                            shortcut), then launch it from the Start Menu.

Both bundle their own Java and JavaFX runtime - you do NOT need to install
Java, JavaFX, Maven, VS Code, or Eclipse to run either one.

Settings and any imported custom charm images are stored per-user in:
    %APPDATA%\DeskCharm\

To uninstall (if you used the installer): Settings -> Apps -> DeskCharm ->
Uninstall, or use the shortcut created in the Start Menu folder.
