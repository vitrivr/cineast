FROM openjdk:14-buster AS build

RUN apt-get update && \
  apt-get install -y maven
COPY . /cineast-src
RUN cd /cineast-src && \
  ./gradlew getExternalFiles && \
  ./gradlew shadowJar

FROM openjdk:14-slim-buster

RUN mkdir -p /opt/cineast

COPY --from=build \
  /cineast-src/cineast.json \
  /opt/cineast/cineast.json

COPY --from=build \
  /cineast-src/cineast-runtime/build/libs/cineast-runtime-*-all.jar \
  /opt/cineast/cineast-cli.jar

COPY --from=build \
  /cineast-src/cineast-api/build/libs/cineast-api-*-all.jar \
  /opt/cineast/cineast-api.jar

COPY --from=build \
  /cineast-src/resources \
  /opt/cineast/resources

RUN printf '#!/bin/bash\n\
if [ "$1" != "api" ] && [ "$1" != "cli" ]; then\n\
    echo "Usage: $0 api|cli" >&2\n\
    exit 1\n\
fi\n\
cd /opt/cineast/ && java -jar cineast-$1.jar ${@:2}'\
> /opt/cineast/bootstrap.sh
RUN chmod +x /opt/cineast/bootstrap.sh

EXPOSE 4567
EXPOSE 4568

ENTRYPOINT ["/opt/cineast/bootstrap.sh"]
