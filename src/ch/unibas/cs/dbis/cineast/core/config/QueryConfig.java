package ch.unibas.cs.dbis.cineast.core.config;

import java.util.Optional;

public class QueryConfig {

	public enum Distance{
		chisquared, correlation, cosine, hamming, jaccard, kullbackleibler, chebyshev, euclidean, squaredeuclidean, manhattan, minkowski, spannorm
	}
	

	private Distance distance;
	private float[] distanceWeights;
	private float norm = 0f / 0f;
	
	public Optional<Distance> getDistance(){
		return Optional.ofNullable(this.distance);
	}
	
	public Optional<float[]> getDistanceWeights(){
		return Optional.ofNullable(this.distanceWeights);
	}
	
	public Optional<Float> getNorm(){
		return Optional.ofNullable(Float.isNaN(norm) ? null : norm);
	}
}
