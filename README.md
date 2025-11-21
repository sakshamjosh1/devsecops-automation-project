# DevSecOps Automation Project

This project demonstrates a complete CI/CD pipeline using Jenkins, GitHub, Docker, and Docker Hub.  
It automates building, testing, containerizing, and deploying a simple Java application.

---

## Overview

The pipeline automatically performs the following steps:

1. Pulls source code from GitHub  
2. Builds and tests the Java application using Maven  
3. Builds a Docker image using a multi-stage Dockerfile  
4. (Optional) Runs SonarQube code analysis  
5. Pushes the image to Docker Hub  
6. Runs the application inside a Docker container


---

## Technologies Used

- Java  
- Maven  
- JUnit  
- GitHub  
- Jenkins (CI/CD)  
- Docker  
- Docker Hub  
- SonarQube (optional)

---

## Features

- Complete CI/CD automation using Jenkins  
- Parameterized Jenkins pipeline  
- Maven build + unit testing  
- Multi-stage Docker image build  
- Optional SonarQube scanning  
- Automatic image push to Docker Hub  
- Lightweight Java HTTP server running on port 8081  

---

## Project Structure

```
devsecops-automation-project/
│
├── src/
│   ├── main/java/Main.java
│   └── test/java/GreetingTest.java
│
├── Dockerfile
├── Jenkinsfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

---

## How to Run Locally

### 1. Clone the repository
```bash
git clone https://github.com/sakshamjosh1/devsecops-automation-project.git
cd devsecops-automation-project
```

### 2. Build with Maven
```bash
mvn clean test package
```

### 3. Build Docker image
```bash
docker build -t devsecops-simple:local .
```

### 4. Run Docker container
```bash
docker run -p 8081:8081 devsecops-simple:local
```

Open the app in browser:  
```
http://localhost:8081
```

---

## Jenkins Pipeline Overview

Pipeline stages used:

- Checkout  
- Build & Test  
- Docker Build  
- (Optional) SonarQube Analysis  
- Push to Docker Hub  
- Post Actions (archive, JUnit reports)

Pipeline parameters:

- `SKIP_SONAR`  
- `PUSH_TO_REGISTRY`  
- `FAIL_ON_MISSING_SONAR_CREDENTIAL`

---

## Docker Commands

### Build
```bash
docker build -t sakshamjoshii/devsecops-simple .
```

### Push
```bash
docker push sakshamjoshii/devsecops-simple
```

### Run
```bash
docker run -p 8081:8081 sakshamjoshii/devsecops-simple
```

---


## Future Improvements

- Add Trivy image security scanning  
- Deploy to Kubernetes  
- Add OWASP ZAP security testing  
- Add Slack or email notifications  
- Terraform IaC automation  

---

