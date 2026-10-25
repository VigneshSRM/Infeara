# syntax=docker/dockerfile:1
# Build and run the Infeara website with Java 27.
#
#   docker build -t infeara-website .
#   docker run --rm -p 8080:8080 \
#     -e SITE_BASE_URL=http://localhost:8080 \
#     -e SITE_INDEXABLE=true \
#     infeara-website
#
# Then open http://localhost:8080 and run scripts/seo-smoke-test.sh against it.
# Set SUPABASE_URL and SUPABASE_SERVICE_ROLE_KEY to enable enquiry submissions and the
# Supabase-backed article archive; without them the site still renders, using the local
# editorial fallback, and the form reports that enquiries are unavailable.

FROM maven:3-eclipse-temurin-27 AS build
WORKDIR /build
# Resolve dependencies in their own layer so source-only changes rebuild quickly.
COPY pom.xml ./
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q -DskipTests package

FROM eclipse-temurin:27-jre
WORKDIR /app
RUN useradd --system --uid 10001 --user-group infeara
COPY --from=build /build/target/*.jar /app/app.jar
USER infeara
ENV PORT=8080 \
    SITE_BASE_URL=http://localhost:8080 \
    SITE_INDEXABLE=false
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
