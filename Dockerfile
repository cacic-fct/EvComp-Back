FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

RUN apk add --no-cache bash

# Instala a versão do Gradle compatível nativamente no Alpine
RUN apk add --no-cache gradle

# Copia as configurações do projeto para o container
COPY build.gradle settings.gradle ./

# Gera o wrapper correto internamente na versão 8.5
RUN gradle wrapper --gradle-version 8.5

RUN ./gradlew dependencies --no-daemon || true

COPY src src

RUN ./gradlew build -x test --no-daemon

# O Gradle gera dois JARs: um executável e um "-plain.jar". 
# Deletamos o plain para não bugar o comando COPY do Docker (que cria uma pasta se achar 2 jars)
RUN rm -f build/libs/*-plain.jar

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Duser.timezone=America/Sao_Paulo", "-jar", "app.jar"]
