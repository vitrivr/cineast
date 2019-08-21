package org.vitrivr.cineast.api.doc;

/**
 * Custom {@link com.beerboy.ss.ApiEndpoint} due to the way
 * <a href="https://github.com/manusant/spark-swagger">SparSwagger</a> set-up its API.
 * <br>
 * Since {@link com.beerboy.ss.SparkSwagger}'s access is very limited,
 * we have to re-implement {@link com.beerboy.ss.SparkSwagger} (maps to {@link CineastSparkSwagger})
 *
 *
 * @author loris.sauter
 */
public class CineastSparkSwaggerApiEndpoint {

}
