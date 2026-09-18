<#
.SYNOPSIS
    Creates a custom jlink runtime image and runs jpackage to produce a
    self-contained DeskCharm Windows application + installer from an
    already-built shaded jar (target/deskcharm-shaded.jar).

.DESCRIPTION
    This script assumes `mvn clean package` has already produced
    target/deskcharm-shaded.jar (see build-windows.ps1, which calls this
    script as its final step). It is kept separate so packaging alone can be
    re-run without recompiling, e.g. while iterating on jpackage options.

    Modules included in the jlink runtime cover what JavaFX controls/graphics/
    swing typically need reflectively at runtime (AWT interop for the system
    tray, clipboard/drag-and-drop plumbing, XML, preferences, and
    sun.misc.Unsafe via jdk.unsupported for JNA). If you add a feature that
    throws NoClassDefFoundError/NoSuchMethodError only in the packaged app
    (never in `mvn javafx:run`), it is almost always a missing module here —
    add it to $JlinkModules below.
#>

$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent $PSScriptRoot
$TargetDir = Join-Path $ProjectRoot "target"
$RuntimeDir = Join-Path $ProjectRoot "runtime"
$DistDir = Join-Path $ProjectRoot "dist"
$ShadedJar = Join-Path $TargetDir "deskcharm-shaded.jar"

$AppName = "DeskCharm"
$AppVersion = "1.0.0"
$AppVendor = "Sharan"
$MainClass = "com.sharan.deskcharm.Main"

$JlinkModules = @(
    "java.base",
    "java.desktop",
    "java.logging",
    "java.xml",
    "java.naming",
    "java.prefs",
    "java.datatransfer",
    "jdk.unsupported",
    "jdk.crypto.ec"
) -join ","

Write-Host "== DeskCharm Windows Packaging ==" -ForegroundColor Cyan

if (-not (Test-Path $ShadedJar)) {
    throw "Could not find $ShadedJar. Run 'mvn clean package' (or build-windows.ps1) first."
}

Write-Host "-- Removing any previous runtime image --" -ForegroundColor Cyan
if (Test-Path $RuntimeDir) {
    Remove-Item -Recurse -Force $RuntimeDir
}

Write-Host "-- Running jlink --" -ForegroundColor Cyan
& jlink `
    --add-modules $JlinkModules `
    --output $RuntimeDir `
    --strip-debug `
    --no-header-files `
    --no-man-pages `
    --compress=2
if ($LASTEXITCODE -ne 0) { throw "jlink failed with exit code $LASTEXITCODE" }

Write-Host "-- Preparing dist directory --" -ForegroundColor Cyan
if (-not (Test-Path $DistDir)) {
    New-Item -ItemType Directory -Path $DistDir | Out-Null
}

Write-Host "-- Running jpackage (app image) --" -ForegroundColor Cyan
$AppImageDir = Join-Path $DistDir "app-image"
if (Test-Path $AppImageDir) {
    Remove-Item -Recurse -Force $AppImageDir
}
& jpackage `
    --type app-image `
    --input $TargetDir `
    --main-jar (Split-Path -Leaf $ShadedJar) `
    --main-class $MainClass `
    --runtime-image $RuntimeDir `
    --name $AppName `
    --app-version $AppVersion `
    --vendor $AppVendor `
    --dest $AppImageDir
if ($LASTEXITCODE -ne 0) { throw "jpackage (app-image) failed with exit code $LASTEXITCODE" }

Write-Host "-- Running jpackage (installer, .exe) --" -ForegroundColor Cyan
& jpackage `
    --type exe `
    --app-image (Join-Path $AppImageDir $AppName) `
    --name $AppName `
    --app-version $AppVersion `
    --vendor $AppVendor `
    --win-dir-chooser `
    --win-menu `
    --win-shortcut `
    --dest $DistDir
if ($LASTEXITCODE -ne 0) { throw "jpackage (installer) failed with exit code $LASTEXITCODE" }

Write-Host "-- Copying portable app image and README into dist/ --" -ForegroundColor Cyan
Copy-Item -Recurse -Force (Join-Path $AppImageDir $AppName) (Join-Path $DistDir $AppName)
Copy-Item -Force (Join-Path $ProjectRoot "scripts\README-WINDOWS.txt") $DistDir -ErrorAction SilentlyContinue

Write-Host ""
Write-Host "== Packaging complete ==" -ForegroundColor Green
Write-Host "Portable app folder: dist\$AppName\$AppName.exe"
Write-Host "Installer:           dist\$AppName-$AppVersion.exe"
Get-ChildItem $DistDir
