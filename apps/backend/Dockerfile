FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21.0.8_9-jre

RUN groupadd --system newsplatform && useradd --system --gid newsplatform --home-dir /app --create-home newsplatform \
    && apt-get update \
    && apt-get install --yes --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
ENV JAVA_OPTS=""
COPY --from=build --chown=newsplatform:newsplatform /workspace/target/news-platform-api-*.jar /app/app.jar
USER newsplatform
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
