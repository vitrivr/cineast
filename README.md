# Cineast
Cineast is a multi-feature sketch-based content video retrieval engine. It is capable of retrieving video sequences based on edge or color sketches and sketch-based motion queries.
Cineast is written in Java and uses [ADAM](https://github.com/dbisUnibas/ADAM) as a storage backend.

## building Cineast
Cineast can be built using [Gradle](http://gradle.org/).

Use

 ./gradlew deploy

This will create the cineast.jar file which can be found in build/libs. Edit the cineast.properties file and point it to the location where you set up ADAM. The freshly created jar file can be started with:

java -jar cineast.jar 

If you have an adampro-container, run it with

sudo docker run --name=adampro -p 5890:5890 -p 9099:9099 -d adampro:withdata

You can then check up on the containers health with

sudo docker ps -a
respectively
sudo docker logs adampro

Cineast has the following dependencies:

* [BoofCV](https://github.com/lessthanoptimal/BoofCV)
* [thumbnailator](https://github.com/coobird/thumbnailator)
* [JCodec](https://github.com/jcodec/jcodec)
* [JLibAV](https://github.com/operutka/jlibav)
* [Trove](https://bitbucket.org/trove4j/trove)
* [Guava](https://github.com/google/guava)
* [Log4j2](http://logging.apache.org/log4j/2.x/)

Common Issues:

Could not parse default value '1.0' from Attr("distortion: float = 1.0") [locale dependent float parsing]
You have the issue reported [here](https://github.com/tensorflow/tensorflow/issues/2974)

the fix is to start cineast with LC_NUMERIC=C java -jar ...