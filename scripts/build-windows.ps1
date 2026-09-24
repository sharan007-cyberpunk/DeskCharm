<#
.SYNOPSIS
    Tests, builds and packages DeskCharm for Windows x64.

.DESCRIPTION
    Produces, inside dist\ :
      DeskCharm\                      portable app folder (run DeskCharm.exe, nothing to install)
      DeskCharm-<version>-portable.zip  the same folder, zipped
      DeskCharm-<version>.msi         Windows Installer package
      DeskCharm-<version>.exe         installer (same payload as the .msi)

    Requirements: JDK 21 (with jpackage), Maven 3.9+. The .msi/.exe installers also need
    the WiX Toolset 3.x (already present on GitHub's windows-latest runners). Use
    -SkipInstallers if WiX is not installed on your PC and you only want the portable app.

    Every native command is checked, so a failed step stops the build instead of
    silently producing a broken package.
#>
[CmdletBinding()]
param(
    [string]$Version = "",
    [switch]$SkipTests,
    [switch]$SkipInstallers
)

$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $ProjectRoot

$AppName   = "DeskCharm"
$Vendor    = "DeskCharm"
$MainClass = "com.sharan.deskcharm.Main"
$TargetDir = Join-Path $ProjectRoot "target"
$InputDir  = Join-Path $TargetDir "package-input"
$WorkDir   = Join-Path $ProjectRoot "build\app-image"
$DistDir   = Join-Path $ProjectRoot "dist"

function Invoke-Native {
    param(
        [Parameter(Mandatory = $true)][string]$Command,
        [string[]]$Arguments = @()
    )
    Write-Host ">> $Command $($Arguments -join ' ')" -ForegroundColor DarkGray
    & $Command @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "$Command failed with exit code $LASTEXITCODE"
    }
}

function Resolve-AppVersion {
    param([string]$Requested)
    if ($Requested) { return $Requested }
    # Tag build (v1.2.3) -> 1.2.3
    if ($env:GITHUB_REF -match '^refs/tags/v(\d+\.\d+\.\d+)$') { return $Matches[1] }
    # CI build -> 1.0.<run number>, so every build is a clean upgrade over the previous one
    if ($env:GITHUB_RUN_NUMBER -match '^\d+$') { return "1.0.$($env:GITHUB_RUN_NUMBER)" }
    return "1.0.0"
}

Write-Host "== DeskCharm Windows build ==" -ForegroundColor Cyan

foreach ($tool in @("java", "mvn", "jpackage")) {
    if (-not (Get-Command $tool -ErrorAction SilentlyContinue)) {
        throw "'$tool' was not found on PATH. Install JDK 21 (includes jpackage) and Maven 3.9+."
    }
}

$AppVersion = Resolve-AppVersion $Version
Write-Host "jpackage version : $(& jpackage --version)"
Write-Host "App version      : $AppVersion"

# ---------------------------------------------------------------------------
# 1. Clean, test, package
# ---------------------------------------------------------------------------
Write-Host "-- Cleaning --" -ForegroundColor Cyan
foreach ($dir in @($TargetDir, $DistDir, (Join-Path $ProjectRoot "build"))) {
    if (Test-Path $dir) { Remove-Item -Recurse -Force $dir }
}
New-Item -ItemType Directory -Force $DistDir | Out-Null

Write-Host "-- Maven: test + package --" -ForegroundColor Cyan
if ($SkipTests) {
    Invoke-Native "mvn" @("-B", "clean", "package", "-DskipTests")
} else {
    Invoke-Native "mvn" @("-B", "clean", "test", "package")
}

$mainJar = Get-ChildItem $TargetDir -Filter "deskcharm-*.jar" |
    Where-Object { $_.Name -notmatch "original|sources|javadoc" } |
    Select-Object -First 1
if (-not $mainJar) { throw "Application JAR was not produced in $TargetDir." }

# ---------------------------------------------------------------------------
# 2. Collect the jpackage input folder: app jar + every runtime dependency
# ---------------------------------------------------------------------------
Write-Host "-- Collecting runtime dependencies --" -ForegroundColor Cyan
New-Item -ItemType Directory -Force $InputDir | Out-Null
Copy-Item $mainJar.FullName $InputDir
Invoke-Native "mvn" @("-B", "dependency:copy-dependencies", "-DoutputDirectory=$InputDir", "-DincludeScope=runtime")

# Fail early if the Windows JavaFX natives or other libraries did not come along:
# the installer would build fine but the app would never start.
$required = @(
    "javafx-base-*-win*.jar",
    "javafx-graphics-*-win*.jar",
    "javafx-controls-*-win*.jar",
    "gson-*.jar",
    "jna-5*.jar",
    "jna-platform-*.jar"
)
foreach ($pattern in $required) {
    if (-not (Get-ChildItem $InputDir -Filter $pattern -ErrorAction SilentlyContinue)) {
        throw "Missing '$pattern' in $InputDir. The packaged app would not start."
    }
}
Write-Host "Packaging input:" -ForegroundColor Cyan
Get-ChildItem $InputDir | ForEach-Object { Write-Host ("  {0}  ({1:N0} KB)" -f $_.Name, ($_.Length / 1KB)) }

$commonArgs = @(
    "--name", $AppName,
    "--app-version", $AppVersion,
    "--vendor", $Vendor,
    "--description", "Desktop hanging charm with rope physics",
    "--input", $InputDir,
    "--main-jar", $mainJar.Name,
    "--main-class", $MainClass
)

# ---------------------------------------------------------------------------
# 3. Installers (.msi and .exe) -> dist\
# ---------------------------------------------------------------------------
if ($SkipInstallers) {
    Write-Warning "Skipping installers (-SkipInstallers). Only the portable app will be created."
} else {
    $installerArgs = @(
        "--win-menu",
        "--win-shortcut",
        "--win-dir-chooser",
        "--win-per-user-install",
        "--dest", $DistDir
    )
    foreach ($type in @("msi", "exe")) {
        Write-Host "-- jpackage: $type installer --" -ForegroundColor Cyan
        Invoke-Native "jpackage" ($commonArgs + @("--type", $type) + $installerArgs)
    }
}

# ---------------------------------------------------------------------------
# 4. Portable app image -> dist\DeskCharm
# ---------------------------------------------------------------------------
Write-Host "-- jpackage: portable app image --" -ForegroundColor Cyan
New-Item -ItemType Directory -Force $WorkDir | Out-Null
Invoke-Native "jpackage" ($commonArgs + @("--type", "app-image", "--dest", $WorkDir))

$Portable = Join-Path $DistDir $AppName
Copy-Item -Recurse -Force (Join-Path $WorkDir $AppName) $Portable

if (-not (Test-Path (Join-Path $Portable "$AppName.exe"))) {
    throw "Portable app image is missing $AppName.exe"
}

$consoleBat = @"
@echo off
rem Runs DeskCharm with a console window so any startup error is printed here.
cd /d "%~dp0"
"runtime\bin\java.exe" -cp "app\*" com.sharan.deskcharm.Main
echo.
echo DeskCharm exited with code %ERRORLEVEL%.
pause
"@
Set-Content -Path (Join-Path $Portable "Run-DeskCharm-with-console.bat") -Value $consoleBat -Encoding ASCII

$portableReadme = @"
DeskCharm (portable)
====================
Double-click DeskCharm.exe to start. Nothing needs to be installed, and Java is bundled.
DeskCharm has no main window: look for the charm hanging from the top of your screen and
the tray icon (bottom-right, maybe under the ^ arrow) for the menu.

If nothing appears, double-click Run-DeskCharm-with-console.bat instead. It starts the
same app but keeps a console open so any error message stays visible.

Settings and imported charms live in %APPDATA%\DeskCharm
"@
Set-Content -Path (Join-Path $Portable "README.txt") -Value $portableReadme -Encoding ASCII

$zipPath = Join-Path $DistDir "$AppName-$AppVersion-portable.zip"
Write-Host "-- Zipping portable app --" -ForegroundColor Cyan
Compress-Archive -Path $Portable -DestinationPath $zipPath -CompressionLevel Optimal

# ---------------------------------------------------------------------------
Write-Host ""
Write-Host "== Build complete ($AppVersion) ==" -ForegroundColor Green
Get-ChildItem $DistDir | ForEach-Object {
    if ($_.PSIsContainer) { Write-Host ("  {0}\  (portable folder)" -f $_.Name) }
    else { Write-Host ("  {0}  ({1:N1} MB)" -f $_.Name, ($_.Length / 1MB)) }
}
