@echo off
:: Cineast supports plugins in form of external JARs
:: To use these plugins conveniently, we provide this script
:: Arguments:
:: /A - Flag to start the API, if not present, the runtime will be started
:: CINEAST-JAR - The path to the cineast JAR (either API or Runtime)
:: PLUGINS - The path tot he plugin JAR (or a folder containing multiple)
:: ARGS - The arguments that should be passed to cineast, space separated

:: Help Message
set HELPLINEONE=Call this progamm with [/A] CINEASTJAR PLUGINS ARGS
set HELPLINETWO= /A - Flag to specify to run the API. Defaults to runtime.
set HELPLINETHREE= CINEASTJAR - The path to the cineast jar.
set HELPLINEFOUR= PLUGINS - The path to the plugin jar or folder with plugins.
set HELPLINEFIVE= ARGS - The arguments, space separated, to pass to cineast.

:: Constant Main class entries
set APIMAIN="org.vitrivr.cineast.api.Main"
set RTMAIN="org.vitrivr.cineast.standalone.Main"

:: Count arguments
set /A argsC=0
for %%x in (%*) do set /A argsC+=1

set /A count=%argsC%

echo %*

if %count%<3 (
  :: too few arguments

  echo "Too few arguments!"
  echo ""
  echo %HELPLINEONE%
  echo %HELPLINETWO%
  echo %HELPLINETHREE%
  echo %HELPLINEFIVE%
  exit -1
)

exit 0


if not "%1"==""(
  if "%1"=="/A" (
    set MAIN=%APIMAIN%
    shift
  )
  if "%1"=="/R"(
    set MAIN=%RTMAIN%
    shift
  )
  if "%1"=="/?"(
    :: Print Help
      echo %HELPLINEONE%
      echo %HELPLINETWO%
      echo %HELPLINETHREE%
      echo %HELPLINEFIVE%
      exit 0
  )
)


:: Defaults to runtime
if not defined MAIN (
  set MAIN=%RTMAIN%
)


:: Read positional arguments
set CINEASTJAR=%1
shift
set PLUGINS=%1
shift

if not defined CINEASTJAR (
  echo "No cineast jar found!"
    echo

    echo %HELPLINEONE%
    echo %HELPLINETWO%
    echo %HELPLINETHREE%
    echo %HELPLINEFIVE%
  exit -2
)

if not defined PLUGINS (
  echo "No plugin directory / jar given!"
  echo "Too few arguments!"
    echo

    echo %HELPLINEONE%
    echo %HELPLINETWO%
    echo %HELPLINETHREE%
    echo %HELPLINEFIVE%
    exit -3
)


set CLASSPATH=%PLUGINS%:%CINEASTJAR%

:: At this point, %* refers to the remaining arguments to be passed to cineast

echo "java -classpath %CLASSPATH %MAIN %*"