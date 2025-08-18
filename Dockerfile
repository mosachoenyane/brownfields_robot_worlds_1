# ----------- Build stage -----------
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends make && rm -rf /var/lib/apt/lists/*

# Copy POM and local libs first for better layer caching
COPY pom.xml ./
COPY .libs/ ./.libs/

# Install local dependency
RUN mvn -B -q install:install-file \
    -Dfile=.libs/eodsql.jar \
    -DgroupId=net.lemnik \
    -DartifactId=eodsql \
    -Dversion=2.2 \
    -Dpackaging=jar

# Pre-fetch dependencies
RUN mvn -B -q -DskipTests dependency:go-offline

# Copy source and build
COPY src ./src
COPY Makefile ./Makefile

# Build the shaded jar (do NOT run tests here; CI runs them in the test stage)
RUN mvn -B -DskipTests clean package

# ----------- Runtime stage -----------
FROM eclipse-temurin:17-jre-jammy AS runtime

WORKDIR /app
COPY --from=build /app/target/robot-world-*.jar /app/app.jar

EXPOSE 5000

HEALTHCHECK --interval=30s --timeout=5s --start-period=15s --retries=3 \
  CMD sh -c 'apt-get update >/dev/null 2>&1 && apt-get install -y curl >/dev/null 2>&1; curl -fs http://localhost:7000/health || exit 1'

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
CMD ["-p", "5000", "-s", "10", "-o", "1,1"]