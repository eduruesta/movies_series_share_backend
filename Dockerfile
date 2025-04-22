# Usa la imagen de Gradle para construir el proyecto
FROM gradle:7.6.0-jdk17 AS build
WORKDIR /app

# Copia los archivos de configuración de Gradle y el directorio src
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle ./gradle
COPY src ./src

# Construye el proyecto con Gradle
RUN gradle clean build -x test

# Usa la imagen JDK para ejecutar la aplicación
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copia el archivo JAR generado en la etapa de build
COPY --from=build /app/build/libs/com.bebi.ktor-api-all.jar ktor-api.jar

# Establece variables de entorno opcionales para MongoDB (Render las definirá automáticamente)
ENV MONGODB_URI="mmongodb+srv://bebiruesta90:OffrumqUiEMWkniY@cluster0.exkybc8.mongodb.net/"
ENV MONGO_DATABASE="moviesAndSeriesShare"

# Exponer el puerto que utiliza Ktor
EXPOSE 8080

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "ktor-api.jar"]
