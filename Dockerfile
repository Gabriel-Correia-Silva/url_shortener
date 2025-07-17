# Estágio 1: Build com Maven
# Usamos uma imagem que já tem o Maven e o JDK (Java Development Kit) 21.
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

# Define o diretório de trabalho dentro do contêiner
WORKDIR /app

# Copia primeiro o pom.xml para aproveitar o cache do Docker
# Se as dependências não mudarem, o Docker não vai baixá-las de novo.
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia o resto do código-fonte
COPY src ./src

# Executa o build do Maven, que irá gerar o arquivo .jar
# O -DskipTests pula a execução dos testes para agilizar o build no pipeline.
RUN mvn package -DskipTests


# Estágio 2: Imagem Final
# Usamos uma imagem base leve, apenas com o ambiente de execução Java (JRE).
FROM amazoncorretto:21-alpine-jre

WORKDIR /app

# Copia o .jar gerado no estágio de build para a imagem final
COPY --from=builder /app/target/*.jar app.jar

# Expõe a porta que a aplicação usa
EXPOSE 8080

# Comando para iniciar a aplicação quando o contêiner for executado
ENTRYPOINT ["java", "-jar", "app.jar"]