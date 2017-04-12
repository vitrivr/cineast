[![Build Status](https://travis-ci.org/vitrivr/cineast.svg?branch=dev)](https://travis-ci.org/ppanopticon/vitrivr)

# Cineast
Cineast is a multi-feature sketch-based content video retrieval engine. It is capable of retrieving video sequences based on edge or color sketches and sketch-based motion queries.
Cineast is written in Java and uses [ADAM](https://github.com/dbisUnibas/ADAM) as a storage backend.

## Building Cineast
Cineast can be built using [Gradle](http://gradle.org/).

## Prerequisites
* git
* You should install [Docker](https://docs.docker.com/engine/installation/linux/ubuntulinux/) which you will need for ADAMpro.

### 3D rendering
For 3D rendering (required in order to support 3D models) you either need a video card or Mesa 3D. The JOGL library supports both. Rendering on Headless devices has been successfully tested with Xvfb. The following steps are required to enable
3D rendering support on a headless device without video card (Ubuntu 16.04.1 LTS)

1. Install Mesa 3D (should come pre-installed on Ubuntu). Check with `dpkg -l | grep mesa`
2. Install Xvfb:

 ```
 sudo apt-get install xvfb
 ```
 
3. Start a new screen:

 ```
 sudo Xvfb :1 -ac -screen 0 1024x768x24 &
 ```
 
4. Using the new screen, start Cineast:

 ```
 DISPLAY=:1 java -jar cineast.jar -3d
 ```
 
The -3d option will perform a 3D test. If it succeeds, cineast should generate a PNG image depicting two coloured
triangles on a black background.

## OSVC
* Then, you should get the OSVC data [Thumbnails](http://download-dbis.dmi.unibas.ch/thumbnails/) [OSVC](http://download-dbis.dmi.unibas.ch/OSVC/).
* Setup for the UI is on the vitrivr-ui repo

Use

 ./gradlew deploy

This will create the cineast.jar file which can be found in build/libs. Edit the cineast.properties file and point it to the location where you set up ADAM. The freshly created jar file can be started with:

java -jar cineast.jar 

## ADAMpro
If you have an adampro-container, run it with

sudo docker run --name=adampro -p 5890:5890 -p 9099:9099 -d adampro:withdata

You can then check up on the containers health with

sudo docker ps -a
respectively
sudo docker logs adampro

## Dependencies
Cineast has the following dependencies:

* [BoofCV](https://github.com/lessthanoptimal/BoofCV)
* [thumbnailator](https://github.com/coobird/thumbnailator)
* [JCodec](https://github.com/jcodec/jcodec)
* [JLibAV](https://github.com/operutka/jlibav)
* [Trove](https://bitbucket.org/trove4j/trove)
* [Guava](https://github.com/google/guava)
* [Log4j2](http://logging.apache.org/log4j/2.x/)
* [Commons Math](https://commons.apache.org/proper/commons-math/)
* [JOGL](http://jogamp.org/jogl/www/)

## Common Issues:

Could not parse default value '1.0' from Attr("distortion: float = 1.0") [locale dependent float parsing]
You have the issue reported [here](https://github.com/tensorflow/tensorflow/issues/2974)

the fix is to start cineast with LC_NUMERIC=C java -jar ...
