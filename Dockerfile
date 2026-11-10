FROM eclipse-temurin:17-jre-noble
RUN groupadd --system app && useradd --system --gid app app
WORKDIR /app
COPY --chown=app:app build/libs/realrunner.jar app.jar
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
USER app
ENTRYPOINT ["java", "-jar", "app.jar"]
