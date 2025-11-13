# DevSecOps Simple Project

Commands:

# Build & test locally (requires Maven & Java 17)
mvn -B clean test package

# Run via Maven
mvn -Dexec.mainClass=Main exec:java

# Build with Docker (docker must be installed)
docker build -t devsecops-simple:local .
docker run --rm -it devsecops-simple:local
