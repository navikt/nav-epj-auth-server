FROM gcr.io/distroless/java21-debian12@sha256:e208483887fa86308e33c1b5d5907d7f3e7ca775723cb20c61e988b43926c97f

WORKDIR /app

COPY build/libs/*.jar app.jar

ENV TZ="Europe/Oslo"
ENV JAVA_OPTS="-Dlogback.configurationFile=logback.xml"

EXPOSE 8080

USER nonroot

CMD ["app.jar"]
