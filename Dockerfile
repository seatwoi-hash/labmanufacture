FROM tomcat:10.1-jdk17-temurin

# Устанавливаем unzip (которого не хватало)
RUN apt-get update && apt-get install -y unzip && rm -rf /var/lib/apt/lists/*

# Очищаем webapps
RUN rm -rf /usr/local/tomcat/webapps/*

# Копируем WAR файл
COPY target/labManufacture-*.war /usr/local/tomcat/webapps/ROOT.war

# Распаковываем WAR (опционально, Tomcat сделает это сам при запуске)
# RUN unzip /usr/local/tomcat/webapps/ROOT.war -d /usr/local/tomcat/webapps/ROOT/ && \
#     rm /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080

CMD ["catalina.sh", "run"]
