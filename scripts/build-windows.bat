@echo off
REM Double-clickable wrapper: runs the real build pipeline in build-windows.ps1.
REM Equivalent manual command:
REM   powershell -ExecutionPolicy Bypass -File "%~dp0build-windows.ps1"

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0build-windows.ps1"
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Build failed. See the output above for details.
    pause
    exit /b %ERRORLEVEL%
)
echo.
echo Build succeeded. See the dist folder for DeskCharm.exe and the installer.
pause
