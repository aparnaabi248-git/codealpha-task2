<#
.SYNOPSIS
    Compiles, tests, and runs the Student Management Application locally.
#>

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " Testing Student Management Java Application Locally" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# 1. Clean and run tests
Write-Host "`n[Step 1] Running Gradle Automated Tests..." -ForegroundColor Yellow
.\gradlew.bat test --info

if ($LASTEXITCODE -ne 0) {
    Write-Host "[!] Unit tests failed!" -ForegroundColor Red
    Exit $LASTEXITCODE
}
Write-Host "[✓] All Unit Tests Passed!" -ForegroundColor Green

# 2. Build executable JAR
Write-Host "`n[Step 2] Building Application JAR..." -ForegroundColor Yellow
.\gradlew.bat build

if ($LASTEXITCODE -ne 0) {
    Write-Host "[!] Build failed!" -ForegroundColor Red
    Exit $LASTEXITCODE
}
Write-Host "[✓] JAR successfully generated under build\libs!" -ForegroundColor Green

# 3. Execute JAR
Write-Host "`n[Step 3] Executing Application..." -ForegroundColor Yellow
java -jar build\libs\CodeAlpha_JenkinsRemoting-1.0.0.jar

Write-Host "`n[✓] Local verification completed successfully!" -ForegroundColor Green
