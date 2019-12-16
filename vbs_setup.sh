#!/bin/bash
# use ./vbs_setup.sh /tank/import/ 3
./gradlew cineast-runtime:fatJar
./gradlew cineast-api:fatJar

timestamp(){
  date +"%m-%d_%H-%M-%S"
}

restart_cottontail () {
  echo "restarting cottontail"
  kill $(pgrep --full cottontail)
  cd ..
  cd cottontaildb
  tmux new-window -d -n cottontaildb "./gradlew run >> cottontail_$(timestamp).log"
  cd ..
  cd cineast
}

# base-folder
base=$1
threads=$2
sleep=5
rm *.log
rm ../cottontaildb/*.log
restart_cottontail
sleep $sleep
echo "setting up"
java -jar cineast-runtime/build/libs/cineast-runtime-2.5-full.jar vbs.json setup --clean >> vbs_setup_$(timestamp).log
echo "importing proto files"
java -jar cineast-runtime/build/libs/cineast-runtime-2.5-full.jar vbs.json import --type PROTO --input $base/extracted_combined/ --batchsize 400 --threads $threads >> proto_import_$(timestamp).log
echo "importing text & metadata"
java -jar cineast-runtime/build/libs/cineast-runtime-2.5-full.jar vbs.json import --type AUDIOTRANSCRIPTION --input $base/text/audiomerge.json --threads $threads --batchsize 15000 >> text_import_$(timestamp).log
java -jar cineast-runtime/build/libs/cineast-runtime-2.5-full.jar vbs.json import --type CAPTIONING --input $base/text/captions.json --threads $threads >> text_import_$(timestamp).log
java -jar cineast-runtime/build/libs/cineast-runtime-2.5-full.jar vbs.json import --type GOOGLEVISION --input $base/text/gvision.json --threads $threads >> text_import_$(timestamp).log
java -jar cineast-runtime/build/libs/cineast-runtime-2.5-full.jar vbs.json import --type METADATA --input $base/text/metamerge.json --threads $threads --batchsize 25000 >> text_import_$(timestamp).log
java -jar cineast-runtime/build/libs/cineast-runtime-2.5-full.jar vbs.json import --type TAGS --input $base/text/tags.json --threads $threads --batchsize 35000 >> text_import_$(timestamp).log
java -jar cineast-runtime/build/libs/cineast-runtime-2.5-full.jar vbs.json import --type V3C1CLASSIFICATIONS --input $base/text/V3C1Analysis --threads $threads --batchsize 25000 >> text_import_$(timestamp).log
java -jar cineast-runtime/build/libs/cineast-runtime-2.5-full.jar vbs.json import --type V3C1FACES --input $base/text/V3C1Analysis/faces --threads $threads --batchsize 25000 >> text_import_$(timestamp).log
java -jar cineast-runtime/build/libs/cineast-runtime-2.5-full.jar vbs.json import --type OBJECTINSTANCE --input $base/obj_det_feat.json --threads $threads --batchsize 400 >> mlt_import_$(timestamp).log

sleep $sleep
echo "optimizing"
java -jar cineast-runtime/build/libs/cineast-runtime-2.5-full.jar vbs.json optimize >> vbs_optimize_$(timestamp).log
sleep $sleep
rm -r cineast_cache_*
tmux new-window -d -n cineast "java -jar cineast-api/build/libs/cineast-api-2.5-full.jar vbs.json"
