# syntax=docker/dockerfile:1

################################################################################
# Stage 1: baixar dependências
FROM eclipse-temurin:21-jdk-jammy as deps

WORKDIR /build

# Copia arquivos necessários do Maven
COPY --chmod=0755 mvnw mvnw
COPY .mvn/ .mvn/
COPY pom.xml .

# Baixa dependências (cacheável)
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw dependency:go-offline -DskipTests

################################################################################
# Stage 2: build da aplicação
FROM deps as package

WORKDIR /build

# Copia código fonte
COPY src/ src/
COPY pom.xml .

RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw package -DskipTests && \
    mv target/*.jar target/app.jar

################################################################################
# Stage 3: extrair camadas do Spring Boot
FROM package as extract

WORKDIR /build

RUN java -Djarmode=layertools -jar target/app.jar extract --destination target/extracted

################################################################################
# Stage 4: runtime leve
FROM eclipse-temurin:21-jre-jammy AS final

ARG UID=10001
RUN adduser \
    --disabled-password \
    --gecos "" \
    --home "/nonexistent" \
    --shell "/sbin/nologin" \
    --no-create-home \
    --uid "${UID}" \
    appuser

USER appuser
WORKDIR /app

# Corrigido: caminho correto (faltava / no seu)
COPY --from=extract /build/target/extracted/dependencies/ ./
COPY --from=extract /build/target/extracted/spring-boot-loader/ ./
COPY --from=extract /build/target/extracted/snapshot-dependencies/ ./
COPY --from=extract /build/target/extracted/application/ ./

EXPOSE 8081

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]