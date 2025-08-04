FROM amazoncorretto:17-alpine
WORKDIR /moyeorak-BE
COPY build/libs/moyeorak-0.0.1-SNAPSHOT.jar moyeorak.jar
ENTRYPOINT ["java", "-jar", "moyeorak.jar"]

