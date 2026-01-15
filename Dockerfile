FROM eclipse-temurin:21-jdk-alpine AS builder

RUN apk add --no-cache curl

WORKDIR /app

COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .
COPY elevator-domain/pom.xml elevator-domain/
COPY elevator-application/pom.xml elevator-application/
COPY elevator-infrastructure/pom.xml elevator-infrastructure/
COPY elevator-bootstrap/pom.xml elevator-bootstrap/

RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

COPY elevator-domain/src elevator-domain/src
COPY elevator-application/src elevator-application/src
COPY elevator-infrastructure/src elevator-infrastructure/src
COPY elevator-bootstrap/src elevator-bootstrap/src

RUN ./mvnw clean package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine

RUN addgroup -g 1000 app && adduser -u 1000 -G app -D app

WORKDIR /app

COPY --from=builder /app/elevator-bootstrap/target/*.jar app.jar

RUN chown -R app:app /app

USER app

HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
