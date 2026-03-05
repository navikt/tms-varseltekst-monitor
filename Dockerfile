FROM gcr.io/distroless/java21-debian12

ENV JAVA_OPTS='-XX:MaxRAMPercentage=75'

COPY api/build/libs/*.jar ./
COPY web-app/dist /app/public

ENV TZ="Europe/Oslo"
EXPOSE 8080
CMD ["app.jar"]
