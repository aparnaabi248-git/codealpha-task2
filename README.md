# CodeAlpha DevOps Internship – Task 2
# Jenkins Remoting: Distributed Build Automation

![DevOps](https://img.shields.io/badge/DevOps-Jenkins%20Distributed%20CI%2FCD-02569B?style=for-the-badge&logo=jenkins&logoColor=white)
![Java](https://img.shields.io/badge/Java-21%20LTS-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-8.5-02303A?style=for-the-badge&logo=gradle&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Containers-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![JUnit](https://img.shields.io/badge/Testing-JUnit%205-25A162?style=for-the-badge&logo=junit5&logoColor=white)

---

## 📌 1. Project Objective

In modern enterprise DevOps environments, running resource-heavy builds and tests directly on the central **Jenkins Controller** creates severe performance bottlenecks, security vulnerabilities, and single points of failure.

The objective of this project is to implement **Distributed Build Automation using Jenkins Remoting**:
1. Develop a production-ready **Student Management Application** in **Java** using **Gradle** and **JUnit 5**.
2. Configure a **Jenkins Controller** responsible strictly for orchestrating pipelines and scheduling jobs.
3. Establish a separate, isolated **Jenkins Agent (Worker Node)** that executes compilation and test workloads remotely.
4. Connect the agent securely to the controller using the **Jenkins Inbound Remoting Protocol (JNLP / WebSocket)** with dynamic secret tokens.
5. Create a declarative `Jenkinsfile` pipeline that enforces execution exclusively on the remote agent node.
6. Verify and document proof of remote execution via Jenkins build logs, node metrics, and test reports.

---

## 🏛️ 2. Distributed Architecture Overview

```
 +-------------------------------------------------------------+
 |                   DEVELOPER WORKSTATION                     |
 |        git push origin main (CodeAlpha_JenkinsRemoting)     |
 +------------------------------+------------------------------+
                                |
                                v
             +-------------------------------------+
             |           GITHUB REPOSITORY         |
             |       (Source Code & Jenkinsfile)   |
             +------------------+------------------+
                                |
                                | Webhook / Polling
                                v
+---------------------------------------------------------------+
|                      JENKINS CONTROLLER                       |
|   - Web Dashboard (Port 8080)                                 |
|   - Job Scheduler & Pipeline Engine                           |
|   - Stores Artifacts, Test History, & Build Logs              |
|   - Remoting Listener (Port 50000 / WebSocket)                |
+-------------------------------+-------------------------------+
                                |
                                | Jenkins Remoting Protocol (Inbound TCP / JNLP)
                                | Authenticated via Agent Secret Token
                                v
+---------------------------------------------------------------+
|                 REMOTE JENKINS AGENT NODE                     |
|                   (Label: 'jenkins-agent')                    |
|                                                               |
|   [Stage 1] git checkout                                      |
|   [Stage 2] Verify Java 21 & Gradle Runtime                   |
|   [Stage 3] Compile Java Classes                              |
|   [Stage 4] Execute JUnit 5 Automated Unit Tests              |
|   [Stage 5] Package JAR & Ship Artifacts back to Controller   |
+---------------------------------------------------------------+
```

### 🔍 Controller vs. Agent Responsibilities

| Responsibility | Jenkins Controller | Jenkins Agent (Worker Node) |
| :--- | :--- | :--- |
| **Primary Role** | Orchestrator & Management | Dedicated Execution Engine |
| **Web UI & Dashboard** | Yes (`http://localhost:8080`) | No (Headless process or container) |
| **Code Compilation & Tests** | **No** (Zero build load) | **Yes** (All CPU/RAM intensive tasks) |
| **Credential Management** | Stores secrets & API keys | Receives scoped execution tasks |
| **Security Isolation** | Protected from malicious code | Isolated; build failures cannot compromise controller |

---

## 📂 3. Project Directory Structure

```text
CodeAlpha_JenkinsRemoting/
├── .gitignore                               # Excludes build artifacts & temporary files
├── build.gradle                             # Gradle build configuration & JUnit 5 dependencies
├── settings.gradle                          # Project name definition
├── gradlew                                  # Gradle wrapper executable for Linux/macOS
├── gradlew.bat                              # Gradle wrapper script for Windows
├── Jenkinsfile                              # Declarative CI/CD pipeline targeting remote agent
├── docker-compose.yml                       # Multi-container setup (Controller + Inbound Agent)
├── Dockerfile.agent                         # Custom Jenkins Inbound Agent image
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar               # Gradle wrapper executable archive
│       └── gradle-wrapper.properties        # Wrapper distribution URL (Gradle 8.5)
├── scripts/
│   ├── run-local-agent.ps1                  # PowerShell script to launch Windows native agent
│   └── test-local-app.ps1                   # Local smoke test runner
├── src/
│   ├── main/java/com/codealpha/student/
│   │   ├── Main.java                        # Interactive application console entrypoint
│   │   ├── Student.java                     # Student data model
│   │   ├── StudentService.java              # CRUD operations & business validation rules
│   │   └── StudentValidationException.java  # Custom domain exception for validation failures
│   └── test/java/com/codealpha/student/
│       └── StudentServiceTest.java          # JUnit 5 parameterized & unit test suite
└── README.md                                # Complete project documentation & guide
```

---

## ☕ 4. Java Application & Gradle Setup

The application is a **Student Management System** with rigorous validation rules:
- **Unique Student IDs**: Prevents duplicate enrollment.
- **Email Validation**: Strict regex verification (`name@domain.com`).
- **GPA Range Checks**: Ensures grades are within valid scale (`0.00` to `4.00`).
- **Statistical Computations**: Calculates class average GPA and student roster queries.

### Test and Build Locally First (Windows PowerShell)

Before involving Jenkins, verify that the application and tests pass locally:

```powershell
# 1. Navigate to project root
cd "C:\Users\user\Downloads\task 2"

# 2. Run automated tests
.\gradlew.bat test --info

# 3. Build executable JAR
.\gradlew.bat build

# 4. Run the application
java -jar build\libs\CodeAlpha_JenkinsRemoting-1.0.0.jar
```

Expected output:
```text
==================================================
  CodeAlpha - Student Management Application      
  DevOps Task 2: Jenkins Remoting Build System    
==================================================

[1] Adding sample students...
Successfully added 3 students.

[2] Current Student Roster:
 -> Student [ID=STU-101, Name=Alice Johnson, Email=alice@codealpha.org, Department=Computer Science, GPA=3.85]
 -> Student [ID=STU-102, Name=Bob Smith, Email=bob@codealpha.org, Department=Information Technology, GPA=3.60]
 -> Student [ID=STU-103, Name=Charlie Davis, Email=charlie@codealpha.org, Department=Cyber Security, GPA=3.92]

[3] Class Average GPA: 3.79

[4] Testing Validation (Invalid GPA):
 Caught expected validation error: GPA must be between 0.0 and 4.0. Given: 4.5

==================================================
 Application execution finished successfully!    
==================================================
```

---

## 🚀 5. Setting Up Jenkins Controller

You can run Jenkins using **Docker** (recommended) or as a standalone Java application.

### Method A: Running Controller via Docker (Recommended)

1. Make sure Docker Desktop is running.
2. In PowerShell, start only the Jenkins controller:
   ```powershell
   docker compose up -d jenkins-controller
   ```
3. Retrieve the initial administrator password:
   ```powershell
   docker exec jenkins-controller cat /var/jenkins_home/secrets/initialAdminPassword
   ```

### Method B: Running Controller Standalone (`jenkins.war`)

If you prefer not using Docker for the controller:
1. Download `jenkins.war` from [https://get.jenkins.io/war-stable/](https://get.jenkins.io/war-stable/).
2. Run in PowerShell:
   ```powershell
   java -jar jenkins.war --httpPort=8080
   ```
3. The initial password will be printed directly in the terminal output.

### Completing Initial Setup Wizard
1. Open your browser and navigate to: `http://localhost:8080`.
2. Paste the **initialAdminPassword**.
3. Click **Install suggested plugins** (Git, Pipeline, JUnit plugins will be installed automatically).
4. Create your Admin user (e.g., username: `admin`, password: `Password@123`).
5. Confirm Jenkins URL as `http://localhost:8080` (or `http://<your-machine-ip>:8080`).

---

## 🤝 6. Configuring the Jenkins Remote Agent

To configure Jenkins Remoting, we create a new Node in the Jenkins Controller.

### Step 1: Create the Node in Jenkins UI
1. In the Jenkins dashboard, click **Manage Jenkins** -> **Nodes** (or **Nodes and Clouds**).
2. Click **New Node** in the top left.
3. Fill in the details:
   - **Node name**: `jenkins-agent-node`
   - **Type**: Select **Permanent Agent**
   - Click **Create**.

### Step 2: Configure Node Properties
Configure the node with the following exact settings:
- **Description**: `Remote Worker Node for CodeAlpha Builds`
- **Number of executors**: `2`
- **Remote root directory**:
  - If using Docker agent: `/home/jenkins/agent`
  - If using Windows native agent: `C:\jenkins-agent-workdir`
- **Labels**: `jenkins-agent` *(Crucial: This matches the label in `Jenkinsfile`)*
- **Usage**: Select **Only build jobs with label expressions matching this node**
- **Launch method**: Select **Launch agent by connecting it to the controller** (Inbound Agent / Remoting)
- **Custom WorkDir path**: Leave default or specify directory.
- Click **Save**.

### Step 3: Retrieve the Agent Secret
1. On the Nodes page, click on **jenkins-agent-node**.
2. Because the agent is not yet connected, you will see a page saying:
   > **Run from agent command line:**
   > `java -jar agent.jar -url http://localhost:8080/ -secret <SECRET_HEX_STRING> -name "jenkins-agent-node" -workDir ...`
3. Copy the **secret** (a 64-character hexadecimal string).

> [!SECURITY NOTE]
> **Never commit the secret to GitHub!** Secrets are dynamically generated per agent and kept confidential.

---

## ⚡ 7. Connecting the Remote Agent via Jenkins Remoting

Choose either **Method A (Docker Agent)** or **Method B (Windows Native Agent)**.

### Method A: Connect Agent using Docker (Isolated Environment)

Run the official Jenkins inbound agent container, passing the secret securely via environment variable:

```powershell
# Set your copied secret
$AGENT_SECRET = "PASTE_YOUR_COPIED_SECRET_HERE"

# Run inbound-agent container
docker run -d `
  --name jenkins-agent `
  --network jenkins-net `
  --restart unless-stopped `
  -e JENKINS_URL="http://jenkins-controller:8080" `
  -e JENKINS_AGENT_NAME="jenkins-agent-node" `
  -e JENKINS_SECRET="$AGENT_SECRET" `
  -e JENKINS_AGENT_WORKDIR="/home/jenkins/agent" `
  jenkins/inbound-agent:latest-jdk21
```

Check the agent logs:
```powershell
docker logs -f jenkins-agent
```
You should see:
```text
INFO: Connected to http://jenkins-controller:8080
INFO: Agent is connected via Remoting
```

---

### Method B: Connect Agent natively on Windows via PowerShell

We have provided an automated script `scripts/run-local-agent.ps1` that downloads `agent.jar` and launches remoting:

```powershell
.\scripts\run-local-agent.ps1 -Secret "PASTE_YOUR_COPIED_SECRET_HERE"
```

Output:
```text
==========================================================
 CodeAlpha DevOps: Jenkins Remoting Agent Launcher
==========================================================
[+] Creating agent working directory at: C:\jenkins-agent-workdir
[+] Downloading Jenkins Remoting agent.jar from http://localhost:8080/jnlpJars/agent.jar ...
[✓] agent.jar downloaded successfully.
[+] Connecting agent 'jenkins-agent-node' to Controller (http://localhost:8080) via Jenkins Remoting...
INFO: Protocol JNLP4-connect sent
INFO: Connected
```

### ✅ Verification in Jenkins UI:
Refresh `Dashboard > Nodes`. The red "Offline" cross icon will turn into a **synchronized online agent** displaying free disk space, swap space, and executors!

---

## 🔄 8. Creating & Running the Jenkins Pipeline

### Step 1: Create Pipeline Job
1. Go to Jenkins Dashboard -> **New Item**.
2. Enter name: `CodeAlpha-JenkinsRemoting-Job`.
3. Select **Pipeline** and click **OK**.

### Step 2: Configure SCM or Pipeline Script
In the job configuration page:
- Scroll down to the **Pipeline** section.
- **Definition**: Choose **Pipeline script from SCM**.
  - **SCM**: Git
  - **Repository URL**: `https://github.com/<your-username>/CodeAlpha_JenkinsRemoting.git` (or your local git path)
  - **Branch Specifier**: `*/main`
  - **Script Path**: `Jenkinsfile`
*(Alternatively, you can choose "Pipeline script" and paste the contents of `Jenkinsfile` directly for testing!)*
- Click **Save**.

### Step 3: Trigger the Build
1. Click **Build Now** on the left menu.
2. Watch the **Stage View** execute each stage:
   - `Checkout Source Code`
   - `Verify Environment`
   - `Compile Java Application`
   - `Run JUnit Unit Tests`
   - `Generate Gradle Build Output`

---

## 🎯 9. Demonstrating Jenkins Remoting (The Proof)

The internship task requires proof that the job executed on the **remote agent** and **not on the controller**.

Open the build's **Console Output** (`Build #1 -> Console Output`). Look for these key markers:

```text
Started by user Admin
[Pipeline] Start of Pipeline
[Pipeline] node
Running on jenkins-agent-node in /home/jenkins/agent/workspace/CodeAlpha-JenkinsRemoting-Job
[Pipeline] {
[Pipeline] stage
[Pipeline] { (Checkout Source Code)
...
[Pipeline] stage
[Pipeline] { (Verify Environment)
[Pipeline] echo
========================================================
 [STAGE 2] Verifying Agent Runtime Environment          
========================================================
[Pipeline] echo
Jenkins Build ID:   1
[Pipeline] echo
Executing On Node:  jenkins-agent-node
[Pipeline] echo
Agent Node Labels:  jenkins-agent
[Pipeline] echo
Remote Workspace:   /home/jenkins/agent/workspace/CodeAlpha-JenkinsRemoting-Job
...
[Pipeline] stage
[Pipeline] { (Run JUnit Unit Tests)
> Task :test
Finished generating test XML results
[Pipeline] junit
Recording test results
[Pipeline] archiveArtifacts
Archiving artifacts
[Pipeline] echo
============================================================
 BUILD STATUS: SUCCESSFUL
 Executed by Remote Agent Node: jenkins-agent-node
============================================================
Finished: SUCCESS
```

### Key Differences Observed in Remoting:
1. **`Running on jenkins-agent-node`**: The pipeline engine allocated an executor on the worker node, not `Built-In Node` (the Controller).
2. **Remote Workspace**: Workspace is located under `/home/jenkins/agent/workspace/` or `C:\jenkins-agent-workdir\workspace\`.
3. **Artifact Transfer**: The compiled `.jar` and JUnit `.xml` test results were generated on the agent and securely streamed over Jenkins Remoting back to the Controller's master database.

---

## 📸 10. Screenshots to Capture for Internship Submission

Prepare these screenshots for your CodeAlpha Task 2 submission document:

1. **Local Build & Test Verification**: Terminal showing `.\gradlew.bat test` and `java -jar build/libs/...` running successfully.
2. **Jenkins Nodes Page (`Dashboard > Manage Jenkins > Nodes`)**: Showing both the Controller (`Built-In Node`) and the active, online Agent (`jenkins-agent-node`).
3. **Agent Configuration Page**: Demonstrating node label `jenkins-agent` and launch method "Launch agent by connecting it to the controller".
4. **Agent Connection Terminal**: Terminal showing `agent.jar` or Docker container running with `INFO: Connected`.
5. **Jenkins Pipeline Stage View**: Green boxes for all 5 stages from `Checkout Source Code` to `Generate Gradle Build Output`.
6. **Console Output Proving Remoting**: Highlight `Running on jenkins-agent-node` and `Executing On Node: jenkins-agent-node`.
7. **JUnit Test Results Trend / Page**: Showing all unit tests passing in Jenkins.
8. **Archived Artifact**: Showing `CodeAlpha_JenkinsRemoting-1.0.0.jar` under build artifacts.

---

## 🛠️ 11. Troubleshooting Common Errors

### 1. Agent Connection Timeout or `Failed to connect to port 50000`
- **Cause**: The JNLP remoting port on the controller is disabled or blocked by Windows Firewall.
- **Fix**: Go to **Manage Jenkins** -> **Security** -> Set **TCP port for inbound agents** to **Fixed: 50000** (or **Random**). Click Save. Ensure port 50000 is open in firewall.

### 2. `There are no nodes with the label 'jenkins-agent'`
- **Cause**: The label in `Jenkinsfile` (`agent { label 'jenkins-agent' }`) does not match the label configured on the agent node.
- **Fix**: Open node configuration in Jenkins, verify **Labels** field is set to `jenkins-agent`, and ensure the agent status is **Online**.

### 3. Gradle Permission Denied (`./gradlew: Permission denied`)
- **Cause**: Unix file permissions for `gradlew` were lost during git commit on Windows.
- **Fix**: The `Jenkinsfile` already includes `chmod +x gradlew` before executing. You can also run `git update-index --chmod=+x gradlew` in your repository.

### 4. Agent Status Displays "Offline: Connection Terminated"
- **Cause**: Invalid secret token or the agent process was stopped.
- **Fix**: Copy the updated secret from `Dashboard > Nodes > jenkins-agent-node` and restart the agent container or script.

---

## 👨‍💻 Author & Acknowledgments

- **Intern**: CodeAlpha DevOps Intern
- **Task**: Task 2 – Jenkins Remoting: Distributed Build Automation
- **Organization**: [CodeAlpha](https://codealpha.tech/)
