# ----------- Build stage -----------
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# 1) Copy the POM first to leverage Docker layer caching for dependencies
COPY pom.xml ./

# 2) Pre-install local lib needed by the project (ensure .libs/eodsql.jar exists in repo)
COPY .libs/eodsql.jar .libs/
RUN mvn -B -q install:install-file \
    -Dfile=.libs/eodsql.jar \
    -DgroupId=net.lemnik \
    -DartifactId=eodsql \
    -Dversion=2.2 \
    -Dpackaging=jar

# 3) Pre-fetch dependencies (faster re-builds on CI)
RUN mvn -B -q -DskipTests dependency:go-offline

# 4) Now bring in the source and build
COPY src ./src
RUN mvn -B -DskipTests clean package

# ----------- Runtime stage -----------
FROM eclipse-temurin:17-jre-jammy AS runtime

WORKDIR /app

# Copy the built jar (use wildcard to avoid hardcoding version)
COPY --from=build /app/target/robot-world-*.jar /app/app.jar

# Default to the TCP server port you use in practice
EXPOSE 5000

HEALTHCHECK --interval=30s --timeout=5s --start-period=15s --retries=3 \
  CMD sh -c 'apt-get update >/dev/null 2>&1 && apt-get install -y curl >/dev/null 2>&1; curl -fs http://localhost:7000/health || exit 1'

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
CMD ["-p", "5000", "-s", "10", "-o", "1,1"]