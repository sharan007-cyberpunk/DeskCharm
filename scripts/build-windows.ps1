$ErrorActionPreference = "Stop"

Write-Host "== DeskCharm Windows Build =="

if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
    throw "Java 21 is required."
}
if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
    throw "Maven is required."
}

$javaVersion = java -version 2>&1 | Select-String 'version "21'
if (-not $javaVersion) {
    Write-Warning "JDK 21 was not detected. Continuing, but Java 21 is recommended."
}

Remove-Item -Recurse -Force target, dist -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force dist | Out-Null

mvn clean test package

$jar = Get-ChildItem target -Filter "deskcharm-*.jar" |
    Where-Object { $_.Name -notmatch "original" } |
    Select-Object -First 1

if (-not $jar) {
    throw "Application JAR was not produced."
}

Copy-Item $jar.FullName "dist\DeskCharm.jar"

if (-not (Get-Command jpackage -ErrorAction SilentlyContinue)) {
    Write-Warning "jpackage was not found. Source/JAR build completed; installer was not created."
    exit 0
}

$inputDir = "target\package-input"
Remove-Item -Recurse -Force $inputDir -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force $inputDir | Out-Null
Copy-Item $jar.FullName $inputDir

# jpackage receives the Maven dependencies through the classpath.
# For a production release, dependency jars should be copied to this directory as well.
$deps = "target\dependency"
mvn dependency:copy-dependencies "-DoutputDirectory=$deps" "-DincludeScope=runtime"
Get-ChildItem $deps -Filter "*.jar" | Copy-Item -Destination $inputDir

jpackage `
  --type exe `
  --name DeskCharm `
  --app-version 1.0.0 `
  --input $inputDir `
  --main-jar $jar.Name `
  --main-class com.sharan.deskcharm.Main `
  --dest dist `
  --win-menu `
  --win-shortcut `
  --win-dir-chooser `
  --win-per-user-install `
  --description "Desktop hanging charm" `
  --vendor "DeskCharm"

Write-Host "Build complete. Check the dist directory."
