# Базовый образ
FROM eclipse-temurin:17-jre

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем JAR файл
COPY target/labManufacture-*.jar app.jar


# Открываем порт
EXPOSE 80

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "app.jar"]
