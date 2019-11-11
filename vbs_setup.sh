#!/bin/bash
# use ./vbs_setup.sh /tank/import/ 3
./gradlew cineast-runtime:fatJar
./gradlew cineast-api:fatJar

restart_cottontail () {
  kill $(pgrep --full cottontail)
  cd ..
  cd cottontaildb
  tmux new-window -d -n cottontaildb "./gradlew run >> cottontail.log"
  cd ..
  cd cineast
}

# base-folder
base=$1
threads=$2
sleep=5
restart_cottontail
sleep $sleep
java -jar cineast-runtime/build/libs/cineast-runtime-2.5-full.jar vbs.json setup --clean
java -jar cineast-runtime/build/libs/cineast-runtime-2.5-full.jar vbs.json import --type PROTO --input $base/extracted_combined/ --threads $threads
java -jar cineast-runtime/build/libs/cineast-runtime-2.5-full.jar vbs.json import --type VBS2020 --input $base/text/ --threads $threads



restart_cottontail
sleep $sleep
java -jar cineast-runtime/build/libs/cineast-runtime-2.5-full.jar vbs.json optimize
restart_cottontail
sleep $sleep
tmux new-window -d -n cineast "java -jar cineast-runtime/build/libs/cineast-api-2.5-full.jar vbs.json"
