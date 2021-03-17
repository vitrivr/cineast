[![swagger-editor](https://img.shields.io/badge/open--API-in--editor-green.svg?style=flat&label=open-api-v2)](https://editor.swagger.io/?url=https://raw.githubusercontent.com/vitrivr/cineast/multi-module-openapi-support/docs/swagger.json)
[![Build Status](https://travis-ci.org/vitrivr/cineast.svg?branch=master)](https://travis-ci.org/vitrivr/cineast)

# Cineast
Cineast is a multi-feature content-based multimedia retrieval engine. It is capable of retrieving images, audio- and video sequences as well as 3d models based on edge or color sketches, sketch-based motion queries and example objects.
Cineast is written in Java and uses [CottontailDB](https://github.com/vitrivr/cottontaildb) as a storage backend.

## Building Cineast
For setup information, consult our [Wiki](https://github.com/vitrivr/cineast/wiki/Environment-Setup)

## Docker image

There is a Docker image available [on Docker
Hub](https://hub.docker.com/r/vitrivr/cineast).

You can run the CLI with:
```
$> docker run vitrivr/cineast cli cineast.json help
```

To change the configuration you can use a bind mount, e.g. to run the API
server with custom configuration file cineast.json in the current directory:
```
$> docker run \
  --mount type=bind,source="$(pwd)"/cineast.json,target=/opt/cineast/cineast.json \
  cineast.json vitrivr/cineast api cineast.json
```

## Generate OpenApi Specification

If you need to rebuild the OpenApi Specification (OAS), there is a gradle task for this purpose:

```
$> ./gradlew -PcineastConfig=<path/to/your/config> generateOpenApiSpecs
```

You can omit `-PcineastConfig`, then the default config (`cineast.json`) is used.
As a result, the OAS is stored at `docs/swagger.json`


## Prerequisites
### System dependencies
* git
* JDK 11 or higher

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

## Contribution

> Contributions are always welcome.

### Versioning

Cineast uses [semantic versioning](https://semver.org). See [the releases page](https://github.com/vitrivr/cineast/releases).

### Code Style

Cineast primarily uses the [Google Java Styleguide](https://google.github.io/styleguide/javaguide.html).
Please use the file supplied in the `docs/` folder

To automatically apply the styleguide in [IntelliJ IDEA](https://www.jetbrains.com/idea/) go to_File_ -> _Settings_ -> _Editor_ -> _Code Style_ -> _Java_ and import the supplied file via the gear icon.

You can also use [Eclipse](https://www.eclipse.org/) for development and use Google's [styleguide for eclipse](https://github.com/google/styleguide/blob/gh-pages/eclipse-java-google-style.xml).
