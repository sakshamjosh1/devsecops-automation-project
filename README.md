# 🚀 DevSecOps Automation Pipeline

> A production-inspired DevSecOps CI/CD pipeline for a Spring Boot REST API integrating automated build, testing, static code analysis, dependency vulnerability scanning, container security scanning, and Docker image publishing.

---

## 📖 Overview

This project demonstrates how security can be integrated into every stage of a CI/CD pipeline by adopting DevSecOps practices.

Starting from a GitHub repository, the pipeline automatically builds a Spring Boot application, executes unit tests, performs static code analysis, scans third-party dependencies for known vulnerabilities, builds a Docker image, scans the container image for vulnerabilities, and optionally pushes the image to Docker Hub.

The objective is to shift security left by detecting issues early in the software development lifecycle while maintaining a fully automated delivery pipeline.

---

# 🏗 Architecture

> **Add architecture diagram here**

```
                GitHub Repository
                       │
                       ▼
              GitHub Actions (CI)
                       │
                       ▼
                 Jenkins Pipeline
                       │
        ┌──────────────┼──────────────┐
        ▼              ▼              ▼
   Maven Build     Unit Tests     JaCoCo
        │
        ▼
 SonarQube (SAST)
        │
        ▼
OWASP Dependency Check (SCA)
        │
        ▼
 Docker Image Build
        │
        ▼
 Trivy Image Scan
        │
        ▼
 Docker Hub (Optional)
```

---

# ✨ Features

- Automated CI/CD pipeline using Jenkins
- Spring Boot REST API
- Maven build automation
- Unit testing using JUnit 5
- Code coverage with JaCoCo
- Static Application Security Testing (SAST) using SonarQube
- Software Composition Analysis (SCA) using OWASP Dependency-Check
- Container vulnerability scanning using Trivy
- Docker image creation
- Optional Docker Hub image publishing
- GitHub Actions for Continuous Integration

---

# 🛠 Tech Stack

| Category | Technology |
|----------|------------|
| Language | Java 17 |
| Framework | Spring Boot |
| Build Tool | Maven |
| Testing | JUnit 5 |
| Code Coverage | JaCoCo |
| CI | GitHub Actions |
| CI/CD | Jenkins |
| Static Code Analysis | SonarQube |
| Dependency Scanning | OWASP Dependency-Check |
| Container Security | Trivy |
| Containerization | Docker |
| Registry | Docker Hub |
| Version Control | Git & GitHub |

---

# 📂 Project Structure

```
devsecops-automation-project
│
├── .github/
│   └── workflows/
│       └── ci.yml
│
├── src/
│   ├── main/
│   └── test/
│
├── Dockerfile
├── Jenkinsfile
├── pom.xml
├── README.md
└── .gitignore
```

---

# ⚙ CI/CD Pipeline

The Jenkins pipeline executes the following stages:

### 1. Checkout

- Pulls the latest source code from GitHub.

---

### 2. Build & Test

- Compiles the application using Maven.
- Executes unit tests.
- Generates JaCoCo coverage reports.

---

### 3. SonarQube Analysis

Performs Static Application Security Testing (SAST).

Checks for:

- Bugs
- Code Smells
- Security Hotspots
- Maintainability Issues
- Code Coverage

---

### 4. OWASP Dependency-Check

Performs Software Composition Analysis (SCA).

Scans third-party dependencies for:

- Known CVEs
- Vulnerable libraries
- Dependency risks
- Security advisories

---

### 5. Docker Build

Builds a production-ready Docker image using a multi-stage Dockerfile.

---

### 6. Trivy Scan

Scans the generated Docker image for:

- Operating System vulnerabilities
- Java dependency vulnerabilities
- High & Critical CVEs

---

### 7. Docker Hub Push (Optional)

If enabled, publishes the Docker image to Docker Hub.

---

# 🔐 DevSecOps Security Pipeline

| Security Tool | Purpose |
|---------------|---------|
| SonarQube | Static Application Security Testing (SAST) |
| OWASP Dependency-Check | Software Composition Analysis (SCA) |
| Trivy | Container Image Vulnerability Scanning |

---

# 📸 Screenshots

## Jenkins Pipeline

> `assets/jenkins-pipeline.png`

---

## SonarQube Dashboard

> `assets/sonarqube-dashboard.png`

---

## OWASP Dependency Check Report

> `assets/dependency-check-report.png`

---

## Trivy Scan Results

> `assets/trivy-scan.png`

---

## GitHub Actions

> `assets/github-actions.png`

---

## Running Spring Boot API

> `assets/api-demo.png`

---

# 🚀 Running the Project

## Clone Repository

```bash
git clone https://github.com/yourusername/devsecops-automation-project.git

cd devsecops-automation-project
```

---

## Build

```bash
mvn clean package
```

---

## Run

```bash
java -jar target/*.jar
```

---

## Docker

Build Image

```bash
docker build -t devsecops-task-api .
```

Run Container

```bash
docker run -p 8080:8080 devsecops-task-api
```

---

# 📊 Pipeline Workflow

```
Developer

    │

Git Push

    │

GitHub

    │

GitHub Actions

    │

Jenkins

    │

Maven Build

    │

JUnit Tests

    │

JaCoCo

    │

SonarQube

    │

OWASP Dependency Check

    │

Docker Build

    │

Trivy Scan

    │

Docker Hub
```

---

# 📈 Project Outcome

- Automated software build and testing
- Early detection of code quality issues
- Detection of vulnerable third-party dependencies
- Container vulnerability assessment
- Automated Docker image generation
- Production-style DevSecOps CI/CD workflow

---

# 🔮 Future Improvements

- Kubernetes Deployment
- Terraform Infrastructure Provisioning
- Slack Notifications
- Email Notifications
- SBOM Generation
- Image Signing with Cosign
- Kubernetes Admission Policies
- AI-assisted Vulnerability Triage

---

# 👨‍💻 Author

**Saksham Joshi**

DevOps | Cloud | DevSecOps | Linux

GitHub: https://github.com/sakshamjosh1