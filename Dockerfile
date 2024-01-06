FROM amd64/amazoncorretto:17

WORKDIR /app

COPY ./build/libs/moddy-server-0.0.1-SNAPSHOT.jar /app/moddy-server.jar

CMD ["java", "-Duser.timezone=Asia/Seoul", "-jar", "-Dspring.profiles.active=test", "moddy-server.jar"]

