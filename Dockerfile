FROM openjdk:17

# Define el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia el archivo JAR generado en el directorio target al contenedor
COPY target/*.jar app.jar

# Configura las variables de entorno para la base de datos
ENV DATABASE_URL=${DATABASE_URL}
ENV DATABASE_USERNAME=${DATABASE_USERNAME}
ENV DATABASE_PASSWORD=${DATABASE_PASSWORD}

# Expone el puerto en el que la aplicación escuchará dentro del contenedor
EXPOSE 8085

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]

# Líneas comentadas que pueden ser útiles para referencia futura:
#COPY target/*.jar app.jar
#EXPOSE 8080
#ENTRYPOINT [ "java","-jar","app.jar" ]
