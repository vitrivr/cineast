#!/usr/bin/env bash
# run-cineast-plugin.sh
# Usage: ./run-cineast-plugin.sh (--api|--runtime) path/to/cineast.jar path/to/plugin.jar

# Constants
API_ENTRY_POINT="org.vitrivr.cineast.api.Main"
RUNTIME_ENTRY_POINT="org.vitrivr.cineast.standalone.Main"

# Variables
ENTRY_POINT=""
CINEAST_JAR=""
PLUGIN_JAR=""
ARGUMENTS=()

# Argument processing
for arg in "$@"; do
  case $arg in
  -a | --api)
    ENTRY_POINT="$API_ENTRY_POINT"
    shift
    ;;
  -r | --runtime)
    ENTRY_POINT="$RUNTIME_ENTRY_POINT"
    shift
    ;;
  -e | --entry-point)
    ENTRY_POINT="$2"
    shift
    shift
    ;;
  *)
    ARGUMENTS+=("$1")
    shift
    ;;
  esac
done

if [ -z "$ENTRY_POINT" ]; then
  echo "No entry point specified! Please specify if you are using the Cineast CLI (--api) or the Cineast Runtime (--runtime)."
  exit 1
fi

if [ ${#ARGUMENTS[@]} -lt 2 ]; then
  echo "Not enough arguments specified! Please specify path/to/cineast.jar and path/to/plugin.jar."
  exit 1
fi

#echo "Entry point: $ENTRY_POINT"
#echo "Additional args (${#ARGUMENTS[@]}): ${ARGUMENTS[*]}"

CINEAST_JAR="${ARGUMENTS[0]}"
PLUGIN_JAR="${ARGUMENTS[1]}"

echo "java -classpath $CINEAST_JAR:$PLUGIN_JAR $ENTRY_POINT ${ARGUMENTS[*]:2}"

java -classpath "$CINEAST_JAR:$PLUGIN_JAR" "$ENTRY_POINT" "${ARGUMENTS[@]:2}"
