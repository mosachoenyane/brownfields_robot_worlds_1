# ----------- Build stage -----------
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Install make to run project Makefile
RUN apt-get update && apt-get install -y --no-install-recommends make && rm -rf /var/lib/apt/lists/*

# 1) Copy POM to leverage dependency layer caching
COPY pom.xml ./

# 2) Bring in all local libs (not just eodsql) so tests that reference them have access
COPY .libs/ ./.libs/

# 3) Pre-install eodsql into local Maven repo
RUN mvn -B -q install:install-file \
    -Dfile=.libs/eodsql.jar \
    -DgroupId=net.lemnik \
    -DartifactId=eodsql \
    -Dversion=2.2 \
    -Dpackaging=jar

# 4) Pre-fetch dependencies
RUN mvn -B -q -DskipTests dependency:go-offline

# 5) Copy source and Makefile, then run tests via Makefile
COPY src ./src
COPY Makefile ./Makefile

# Run the same tests as `make all-test`
RUN make all-test

# 6) Build the shaded jar (skip tests since they ran already)
RUN mvn -B -DskipTests clean package

# ----------- Runtime stage -----------
FROM eclipse-temurin:17-jre-jammy AS runtime

WORKDIR /app

# Copy the built jar (use wildcard to avoid hardcoding version)
COPY --from=build /app/target/robot-world-*.jar /app/app.jar

EXPOSE 5000

HEALTHCHECK --interval=30s --timeout=5s --start-period=15s --retries=3 \
  CMD sh -c 'apt-get update >/dev/null 2>&1 && apt-get install -y curl >/dev/null 2>&1; curl -fs http://localhost:7000/health || exit 1'

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
CMD ["-p", "5000", "-s", "10", "-o", "1,1"]