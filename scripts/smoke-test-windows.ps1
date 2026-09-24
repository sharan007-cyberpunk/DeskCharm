<#
.SYNOPSIS
    Post-build check used by GitHub Actions (also runnable locally after build-windows.ps1).

.DESCRIPTION
    1. Starts the portable app through its bundled java.exe (so errors are captured) and
       checks it is still running after a few seconds.
    2. Installs the .msi silently, checks the app files landed on disk, launches the
       installed DeskCharm.exe and checks it is still running.

    Logs are written to the log directory (default: logs\) so they can be uploaded as a
    build artifact. Exit code is 1 if any check failed, 0 otherwise.
#>
[CmdletBinding()]
param(
    [string]$DistDir = "dist",
    [string]$LogDir = "logs",
    [int]$LaunchSeconds = 12
)

$ErrorActionPreference = "Continue"

New-Item -ItemType Directory -Force -Path $LogDir | Out-Null
$LogDir = (Resolve-Path $LogDir).Path
$DistDir = (Resolve-Path $DistDir).Path
$problems = New-Object System.Collections.Generic.List[string]

function Test-AppLaunch {
    param(
        [string]$Label,
        [string]$FilePath,
        [string[]]$Arguments,
        [string]$WorkingDirectory
    )
    Write-Host ""
    Write-Host "== Launch check: $Label ==" -ForegroundColor Cyan
    $out = Join-Path $LogDir "$Label-stdout.log"
    $err = Join-Path $LogDir "$Label-stderr.log"

    $startArgs = @{
        FilePath               = $FilePath
        WorkingDirectory       = $WorkingDirectory
        PassThru               = $true
        RedirectStandardOutput = $out
        RedirectStandardError  = $err
    }
    if ($Arguments -and $Arguments.Count -gt 0) { $startArgs["ArgumentList"] = $Arguments }

    try {
        $proc = Start-Process @startArgs
    } catch {
        $problems.Add("${Label}: could not start ($($_.Exception.Message))")
        return
    }
    $null = $proc.Handle   # keeps the exit code readable after the process ends
    Start-Sleep -Seconds $LaunchSeconds

    if ($proc.HasExited) {
        $problems.Add("${Label}: process exited within $LaunchSeconds s (exit code $($proc.ExitCode))")
    } else {
        Write-Host "$Label is still running after $LaunchSeconds s - OK"
        Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
    }

    foreach ($file in @($out, $err)) {
        if ((Test-Path $file) -and ((Get-Item $file).Length -gt 0)) {
            Write-Host "--- $(Split-Path -Leaf $file) (last 40 lines) ---"
            Get-Content $file -Tail 40
        }
    }
}

# ---------------------------------------------------------------------------
# 1. Portable app
# ---------------------------------------------------------------------------
$portable = Join-Path $DistDir "DeskCharm"
$java = Join-Path $portable "runtime\bin\java.exe"
if (-not (Test-Path $java)) {
    $problems.Add("portable: $java not found")
} else {
    Test-AppLaunch -Label "portable" -FilePath $java `
        -Arguments @("-cp", "app\*", "com.sharan.deskcharm.Main") `
        -WorkingDirectory $portable
}

# ---------------------------------------------------------------------------
# 2. MSI install + launch of the installed app
# ---------------------------------------------------------------------------
$msi = Get-ChildItem -Path $DistDir -Filter "*.msi" -ErrorAction SilentlyContinue | Select-Object -First 1
if (-not $msi) {
    $problems.Add("installer: no .msi found in $DistDir")
} else {
    Write-Host ""
    Write-Host "== Installing $($msi.Name) silently ==" -ForegroundColor Cyan
    $msiLog = Join-Path $LogDir "msi-install.log"
    $install = Start-Process -FilePath "msiexec.exe" -Wait -PassThru -ArgumentList @(
        "/i", "`"$($msi.FullName)`"", "/qn", "/norestart", "/l*v", "`"$msiLog`""
    )
    Write-Host "msiexec exit code: $($install.ExitCode)"
    if (@(0, 3010) -notcontains $install.ExitCode) {
        $problems.Add("installer: msiexec exit code $($install.ExitCode) (see msi-install.log)")
    } else {
        $candidates = @(
            (Join-Path $env:LOCALAPPDATA "DeskCharm\DeskCharm.exe"),
            (Join-Path $env:ProgramFiles "DeskCharm\DeskCharm.exe")
        )
        $installedExe = $candidates | Where-Object { Test-Path $_ } | Select-Object -First 1
        if (-not $installedExe) {
            $problems.Add("installer: DeskCharm.exe not found in: $($candidates -join ', ')")
        } else {
            Write-Host "Installed to: $(Split-Path -Parent $installedExe)"
            Test-AppLaunch -Label "installed" -FilePath $installedExe `
                -Arguments @() -WorkingDirectory (Split-Path -Parent $installedExe)
        }
    }
}

# ---------------------------------------------------------------------------
Write-Host ""
if ($problems.Count -gt 0) {
    Write-Host "== SMOKE TEST FAILED ==" -ForegroundColor Red
    $problems | ForEach-Object { Write-Host " - $_" -ForegroundColor Red }
    if ($env:GITHUB_STEP_SUMMARY) {
        Add-Content -Path $env:GITHUB_STEP_SUMMARY -Value "### DeskCharm smoke test: FAILED"
        $problems | ForEach-Object { Add-Content -Path $env:GITHUB_STEP_SUMMARY -Value "- $_" }
        Add-Content -Path $env:GITHUB_STEP_SUMMARY -Value "Download the DeskCharm-Logs artifact for details."
    }
    exit 1
}
Write-Host "== Smoke test passed ==" -ForegroundColor Green
if ($env:GITHUB_STEP_SUMMARY) {
    Add-Content -Path $env:GITHUB_STEP_SUMMARY -Value "### DeskCharm smoke test: passed (MSI installs, app launches)"
}
exit 0
