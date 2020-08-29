FROM openjdk:14-buster AS build

RUN apt-get update && \
  apt-get install -y maven wget unzip
COPY . /cineast-src
RUN cd /cineast-src && \
  ./gradlew :cineast-core:generateProto && \
  ./gradlew getExternalFiles && \
  ./gradlew fatJar

RUN wget https://download.pytorch.org/libtorch/cpu/libtorch-shared-with-deps-1.6.0%2Bcpu.zip && \
  unzip libtorch-shared-with-deps-1.6.0+cpu.zip && \
  rm libtorch-shared-with-deps-1.6.0+cpu.zip

FROM openjdk:14-slim-buster

RUN mkdir -p /opt/cineast

RUN apt-get update && apt-get install -y libgtk2.0-0

COPY --from=build \
  /cineast-src/cineast.json \
  /opt/cineast/cineast.json

COPY --from=build \
  /cineast-src/cineast-runtime/build/libs/cineast-runtime-*-full.jar \
  /opt/cineast/cineast-cli.jar

COPY --from=build \
  /cineast-src/cineast-api/build/libs/cineast-api-*-full.jar \
  /opt/cineast/cineast-api.jar

COPY --from=build \
  /cineast-src/resources \
  /opt/cineast/resources

COPY --from=build \
  /cineast-src/libtorch/ \
  /opt/libtorch/

RUN printf '#!/bin/bash\n\
if [ "$1" != "api" ] && [ "$1" != "cli" ]; then\n\
    echo "Usage: $0 api|cli" >&2\n\
    exit 1\n\
fi\n\
cd /opt/cineast/ && java -Djava.library.path=/opt/libtorch/lib -jar cineast-$1.jar ${@:2}'\
> /opt/cineast/bootstrap.sh
RUN chmod +x /opt/cineast/bootstrap.sh

EXPOSE 4567
EXPOSE 4568

ENTRYPOINT ["/opt/cineast/bootstrap.sh"]
