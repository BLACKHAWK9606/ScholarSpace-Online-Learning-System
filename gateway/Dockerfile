# =============================================================================
# GATEWAY Dockerfile (./gateway/Dockerfile)
# =============================================================================
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app
ARG MAVEN_OPTS
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN ./mvnw dependency:go-offline -B -Dhttp.maxConnections=10 -Dmaven.wagon.http.pool=false -Dmaven.wagon.httpconnectionManager.ttlSeconds=120 || true
COPY src src
RUN ./mvnw clean package -DskipTests -B -Dhttp.maxConnections=10 -Dmaven.wagon.http.pool=false -Dmaven.wagon.httpconnectionManager.ttlSeconds=120 -Dmaven.wagon.rto=600000

FROM eclipse-temurin:21-jre-jammy
RUN addgroup --system spring && adduser --system spring --ingroup spring
WORKDIR /app
COPY --from=build /app/target/gateway-0.0.1-SNAPSHOT.jar app.jar
RUN chown spring:spring app.jar
USER spring:spring
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s --start-period=120s --retries=3 CMD curl -f http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]