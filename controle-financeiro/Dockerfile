# --- Etapa 1: build ---------------------------------------------------------
# Compila o projeto com Maven dentro de um container, sem precisar ter Maven
# instalado na máquina que vai rodar o build (Render, por exemplo).
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copia primeiro só o pom.xml e baixa as dependências: isso cria uma camada de cache
# que só é invalidada quando o pom.xml muda, deixando builds seguintes mais rápidos.
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# --- Etapa 2: imagem final ---------------------------------------------------
# Usa só o JRE (não o JDK completo) para a imagem final ficar bem menor.
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# A hospedagem (Render, Railway etc.) informa a porta via a variável de ambiente PORT.
# Se ela não existir (ex.: rodando localmente), usa 8080 como padrão.
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT}"]
