[![Build Status](https://travis-ci.org/vitrivr/cineast.svg?branch=master)](https://travis-ci.org/vitrivr/cineast)

# Cineast
Cineast is a multi-feature content-based mulitmedia retrieval engine. It is capable of retrieving images, audio- and video sequences as well as 3d models based on edge or color sketches, sketch-based motion queries and example objects.
Cineast is written in Java and uses [ADAMpro](https://github.com/vitrivr/ADAMpro) as a storage backend.

## Building Cineast
Cineast can be built using [Gradle](http://gradle.org/). Building and running it is as easy as
```
 git clone --recursive https://github.com/vitrivr/cineast.git cineast
 cd cineast
 ./gradlew deploy
 cd build/libs
 java -jar cineast.jar
 ```

## Prerequisites
### System dependencies
* git
* JDK 8 or higher
* Optionally: You should install [Docker](https://docs.docker.com/engine/installation/linux/ubuntulinux/) which you can use for ADAMpro.

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

