FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-21
COPY api/build/libs/*.jar ./
COPY web-app/dist /app/public

ENV TZ="Europe/Oslo"
EXPOSE 8080

ENV JAVA_OPTS='-XX:MaxRAMPercentage=75'
CMD ["-jar", "app.jar"]