# This Dockerfile is useful to quickly setup a test/dev environment.
# It should not be used in production.

# mvn package -Dmaven.test.skip=true
# docker build -f src/main/docker/Dockerfile -t tac/submission-code-server-ws-rest .

FROM registry.gitlab.inria.fr/stemcovid19/tac-server/docker-image/oracle-serverjre:1.8.0_241-b07
ADD ./target/submission-code-server-*.jar /app.jar
ADD ./src/main/resources/application.properties /work/config/application.properties
CMD ["java","-jar", "/app.jar", "--spring.config.location=file:/work/config/application.properties"]
