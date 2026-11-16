# syntax=docker/dockerfile:1.7

FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build

# Tüm pom'lar (parent şart koşuyor)
COPY pom.xml .
COPY common-lib/pom.xml         common-lib/pom.xml
COPY discovery-service/pom.xml  discovery-service/pom.xml
COPY config-service/pom.xml     config-service/pom.xml
COPY api-gateway/pom.xml        api-gateway/pom.xml
COPY user-service/pom.xml       user-service/pom.xml
COPY product-service/pom.xml    product-service/pom.xml
COPY order-service/pom.xml      order-service/pom.xml
COPY cart-service/pom.xml       cart-service/pom.xml
COPY payment-service/pom.xml    payment-service/pom.xml
COPY log-service/pom.xml        log-service/pom.xml
COPY comment-service/pom.xml    comment-service/pom.xml
COPY ai-service/pom.xml         ai-service/pom.xml

RUN mvn -pl order-service -am dependency:go-offline -B

COPY common-lib/src      common-lib/src
COPY order-service/src order-service/src

RUN mvn -pl order-service -am clean package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN apk add --no-cache wget
RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=builder --chown=spring:spring /build/order-service/target/*.jar app.jar

USER spring:spring
EXPOSE 8083

ENTRYPOINT ["java", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+ExitOnOutOfMemoryError", \
    "-jar", "/app/app.jar"]