FROM openjdk:11.0.16-slim
ARG JAR_FILE
ARG COMMIT=1
ARG BUILD_DATE=2022-08-10
ARG APP_VERSION=0.1
EXPOSE 8081

LABEL \
    org.opencontainers.image.title=productInventoryGateway \
    org.opencontainers.image.created=$BUILD_DATE \
    org.opencontainers.image.revision=$COMMIT \
    org.opencontainers.image.vendor=Commencis \
    org.opencontainers.image.version=$APP_VERSION
ADD --chown=122937 ${JAR_FILE} employeeDirector.jar
CMD java ${JAVA_OPTS} -jar employeeDirector.jar