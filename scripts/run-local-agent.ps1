<#
.SYNOPSIS
    Connects a local Windows Jenkins Inbound Agent to the Jenkins Controller.
.DESCRIPTION
    Downloads the official Jenkins agent.jar (remoting client) from the Controller
    and connects securely using the Inbound Agent secret without hardcoding secrets.
.PARAMETER JenkinsUrl
    URL where Jenkins Controller is reachable (default: http://localhost:8080)
.PARAMETER NodeName
    Agent node name registered in Jenkins (default: jenkins-agent-node)
.PARAMETER Secret
    Agent secret generated in Jenkins UI (Manage Jenkins -> Nodes -> jenkins-agent-node)
.EXAMPLE
    .\run-local-agent.ps1 -Secret "your-secret-here"
#>

param(
    [string]$JenkinsUrl = "http://localhost:8080",
    [string]$NodeName = "jenkins-agent-node",
    [Parameter(Mandatory=$true)]
    [string]$Secret,
    [string]$WorkDir = "C:\jenkins-agent-workdir"
)

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " CodeAlpha DevOps: Jenkins Remoting Agent Launcher" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# 1. Create work directory
if (-not (Test-Path $WorkDir)) {
    Write-Host "[+] Creating agent working directory at: $WorkDir" -ForegroundColor Green
    New-Item -ItemType Directory -Path $WorkDir -Force | Out-Null
}

# 2. Download agent.jar if missing
$AgentJarPath = Join-Path $WorkDir "agent.jar"
if (-not (Test-Path $AgentJarPath)) {
    $DownloadUrl = "$JenkinsUrl/jnlpJars/agent.jar"
    Write-Host "[+] Downloading Jenkins Remoting agent.jar from $DownloadUrl ..." -ForegroundColor Yellow
    try {
        Invoke-WebRequest -Uri $DownloadUrl -OutFile $AgentJarPath
        Write-Host "[✓] agent.jar downloaded successfully." -ForegroundColor Green
    } catch {
        Write-Host "[!] Could not download agent.jar from Jenkins Controller. Ensure Jenkins is running at $JenkinsUrl" -ForegroundColor Red
        Exit 1
    }
}

# 3. Connect Agent to Controller
Write-Host "[+] Connecting agent '$NodeName' to Controller ($JenkinsUrl) via Jenkins Remoting..." -ForegroundColor Cyan
Write-Host "[i] Press Ctrl+C at any time to disconnect the agent." -ForegroundColor Gray

java -jar $AgentJarPath -url $JenkinsUrl -secret $Secret -name $NodeName -workDir $WorkDir
