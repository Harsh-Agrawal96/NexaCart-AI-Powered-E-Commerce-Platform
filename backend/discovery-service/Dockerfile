# syntax=docker/dockerfile:1.7

# ============================================================
# STAGE 1: BUILDER
# ============================================================
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build

# 1) Tüm pom dosyalarını kopyala
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


# 2) Dependency'leri offline cache'e indir
RUN mvn -pl discovery-service -am dependency:go-offline -B

# 3) Sadece bu servis için gereken kaynak kodları kopyala
COPY discovery-service/src  discovery-service/src

# 4) Build et
RUN mvn -pl discovery-service -am clean package -DskipTests -B

# ============================================================
# STAGE 2: RUNTIME
# ============================================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# wget healthcheck için lazım (alpine'da yok varsayılan olarak — aslında var ama emin olmak için)
RUN apk add --no-cache wget

RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=builder --chown=spring:spring /build/discovery-service/target/*.jar app.jar

USER spring:spring

EXPOSE 8761

ENTRYPOINT ["java", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+ExitOnOutOfMemoryError", \
    "-jar", "/app/app.jar"]