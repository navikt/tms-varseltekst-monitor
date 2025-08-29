FROM ghcr.io/navikt/baseimages/temurin:21

ENV JAVA_OPTS='-XX:MaxRAMPercentage=75'

COPY api/build/libs/*.jar ./
COPY /web-app/dist /app/public
