FROM openjdk:21-jdk-slim

WORKDIR /app

# Copiar arquivos do Maven
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Baixar dependências
RUN ./mvnw dependency:go-offline -B

# Copiar código fonte
COPY src ./src

# Construir aplicação
RUN ./mvnw clean package -DskipTests

# Expor porta
EXPOSE 8080

# Comando para executar a aplicação
CMD ["java", "-jar", "target/shortener_url_java_spring-0.0.1-SNAPSHOT.jar"]