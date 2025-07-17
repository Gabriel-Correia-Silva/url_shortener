# Estágio 1: Build com Maven
# CORREÇÃO: Usando a imagem oficial do Maven com a base Amazon Corretto 21
FROM maven:3-amazoncorretto-21 AS builder

# Define o diretório de trabalho dentro do contêiner
WORKDIR /app

# Copia primeiro o pom.xml para aproveitar o cache do Docker
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia o resto do código-fonte
COPY src ./src

# Executa o build do Maven, que irá gerar o arquivo .jar
RUN mvn package -DskipTests


# Estágio 2: Imagem Final
# CORREÇÃO: Usando a imagem oficial e leve do Amazon Corretto 21
FROM amazoncorretto:21-alpine

WORKDIR /app

# Copia o .jar gerado no estágio de build para a imagem final
COPY --from=builder /app/target/*.jar app.jar

# Expõe a porta que a aplicação usa
EXPOSE 8080

# Comando para iniciar a aplicação quando o contêiner for executado
ENTRYPOINT ["java", "-jar", "app.jar"]