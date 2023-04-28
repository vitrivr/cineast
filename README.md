[![vitrivr - cineast](https://img.shields.io/static/v1?label=vitrivr&message=cineast&color=blue&logo=github)](https://github.com/vitrivr/cineast)
[![GitHub release](https://img.shields.io/github/release/vitrivr/cineast?include_prereleases=&sort=semver&color=2ea44f)](https://github.com/vitrivr/cineast/releases/)
[![License](https://img.shields.io/badge/License-MIT-blueviolet)](LICENSE)
[![swagger-editor](https://img.shields.io/badge/open--API-in--editor-green.svg?style=flat&label=Open-Api%20(Release))](https://editor.swagger.io/?url=https://raw.githubusercontent.com/vitrivr/cineast/master/docs/openapi.json)
[![swagger-editor](https://img.shields.io/badge/open--API-in--editor-green.svg?style=flat&label=Open-Api%20(Dev))](https://editor.swagger.io/?url=https://raw.githubusercontent.com/vitrivr/cineast/dev/docs/openapi.json)
[![Java CI with Gradle](https://github.com/vitrivr/cineast/workflows/Java%20CI%20with%20Gradle/badge.svg)](https://github.com/vitrivr/cineast/actions?query=workflow:"Java+CI+with+Gradle")

# Cineast
Cineast is a multi-feature content-based multimedia retrieval engine. It is capable of retrieving images, audio- and video sequences as well as 3d models based on edge or color sketches, sketch-based motion queries and example objects.
Cineast is written in Java and uses [CottontailDB](https://github.com/vitrivr/cottontaildb) as a storage backend.

## Building Cineast
Cineast can be built using [Gradle](https://gradle.org/). It needs Java 17+. Building and running it is as easy as
```
git clone https://github.com/vitrivr/cineast.git
cd cineast
./gradlew getExternalFiles cineast-runtime:shadowJar
java -jar cineast-runtime/build/libs/cineast-runtime-x.x-all.jar cineast.json
 ```

For more setup information, consult our [Wiki](https://github.com/vitrivr/cineast/wiki)

## Docker image

There is a Docker image available [on Docker
Hub](https://hub.docker.com/r/vitrivr/cineast).

You can run the CLI with:
```
docker run vitrivr/cineast cli cineast.json help
```

To change the configuration you can use a bind mount, e.g. to run the API
server with custom configuration file cineast.json in the current directory:
```
docker run -v "$PWD"/cineast.json:/opt/cineast/cineast.json:ro,Z vitrivr/cineast api cineast.json
```

## Generate OpenApi Specification

If you need to rebuild the OpenApi Specification (OAS), there is a gradle task for this purpose:

```
./gradlew -PcineastConfig=<path/to/your/config> generateOpenApiSpecs
```

You can omit `-PcineastConfig`, then the default config (`cineast.json`) is used.
The generated OAS is stored at `docs/openapi.json`


## Prerequisites
### System dependencies
* git
* JDK 17 or higher

### 3D rendering
For 3D rendering (required in order to support 3D models) you either need a video card or Mesa 3D. The JOGL library supports both. Rendering on Headless devices has been successfully tested with Xvfb. The following steps are required to enable
3D rendering support on a headless device without video card (Ubuntu 16.04.1 LTS)

1. Install Mesa 3D (should come pre-installed on Ubuntu). Check with `dpkg -l | grep mesa`
2. Install Xvfb:

 ```
 $> sudo apt-get install xvfb
 ```
 
3. Start a new screen:

 ```
 $> sudo Xvfb :1 -ac -screen 0 1024x768x24 &
 ```
 
4. Using the new screen, start Cineast:

 ```
 $> DISPLAY=:1 java -jar cineast.jar -3d
 ```
 
The -3d option will perform a 3D test. If it succeeds, cineast should generate a PNG image depicting two coloured
triangles on a black background.

### Versioning

Cineast uses [semantic versioning](https://semver.org). See [the releases page](https://github.com/vitrivr/cineast/releases).

### Code Style

Cineast primarily uses the [Google Java Styleguide](https://google.github.io/styleguide/javaguide.html).
Please use the file supplied in the `docs/` folder

To automatically apply the styleguide in [IntelliJ IDEA](https://www.jetbrains.com/idea/) go to_File_ -> _Settings_ -> _Editor_ -> _Code Style_ -> _Java_ and import the supplied file via the gear icon.

You can also use [Eclipse](https://www.eclipse.org/) for development and use Google's [styleguide for eclipse](https://github.com/google/styleguide/blob/gh-pages/eclipse-java-google-style.xml).
