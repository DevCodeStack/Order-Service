FROM openjdk:8

COPY ./target/Order-Service.jar orderservice.jar

EXPOSE 8083

CMD ["java","-jar","-Dspring.profile.active=local","orderservice.jar"]