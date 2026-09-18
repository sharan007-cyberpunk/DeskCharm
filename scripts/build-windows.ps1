<#
.SYNOPSIS
    Full DeskCharm Windows build pipeline: verify tools -> clean -> test ->
    compile -> package -> jlink runtime -> jpackage app + installer -> dist/.

.DESCRIPTION
    Run this from PowerShell in the project root, or double-click
    build-windows.bat, which just calls this script.

    Exact command:
        powershell -ExecutionPolicy Bypass -File scripts\build-windows.ps1
#>

$ErrorActionPreference = "Stop"
$ProjectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $ProjectRoot

function Assert-Command($Name, $VersionArgs, $Pattern) {
    Write-Host "-- Verifying $Name --" -ForegroundColor Cyan
    try {
        $output = & $Name $VersionArgs 2>&1 | Out-String
    } catch {
        throw "$Name was not found on PATH. Please install it and try again."
    }
    if ($Pattern -and ($output -notmatch $Pattern)) {
        Write-Warning "$Name is installed but the version could not be confirmed as expected ($Pattern). Continuing anyway."
    }
    Write-Host $output
}

Write-Host "======================================" -ForegroundColor Green
Write-Host " DeskCharm Windows Build" -ForegroundColor Green
Write-Host "======================================" -ForegroundColor Green

# 1. Verify Java 21
Assert-Command "java" "-version" "21\."

# 2. Verify Maven
Assert-Command "mvn" "-version" "Apache Maven"

# 3. Clean the project
Write-Host "-- mvn clean --" -ForegroundColor Cyan
& mvn clean
if ($LASTEXITCODE -ne 0) { throw "mvn clean failed" }

# 4. Run unit tests
Write-Host "-- mvn test --" -ForegroundColor Cyan
& mvn test
if ($LASTEXITCODE -ne 0) { throw "Unit tests failed; aborting build. Fix failing tests before packaging." }

# 5 & 6. Compile and package the application (also produces the shaded jar)
Write-Host "-- mvn package (-DskipTests, already tested above) --" -ForegroundColor Cyan
& mvn package "-DskipTests"
if ($LASTEXITCODE -ne 0) { throw "mvn package failed" }

# 7-10. Create the runtime image, run jpackage, create the installer
Write-Host "-- Packaging (jlink + jpackage) --" -ForegroundColor Cyan
& (Join-Path $PSScriptRoot "package-windows.ps1")

# 11. Final files are already placed into dist/ by package-windows.ps1
Write-Host ""
Write-Host "======================================" -ForegroundColor Green
Write-Host " Build complete. See the dist\ folder:" -ForegroundColor Green
Write-Host "======================================" -ForegroundColor Green
Get-ChildItem (Join-Path $ProjectRoot "dist")
