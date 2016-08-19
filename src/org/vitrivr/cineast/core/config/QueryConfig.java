package org.vitrivr.cineast.core.config;

import java.util.Optional;

public class QueryConfig {

	public static enum Distance{
		chisquared, correlation, cosine, hamming, jaccard, kullbackleibler, chebyshev, euclidean, squaredeuclidean, manhattan, minkowski, spannorm
	}
	
	private Distance distance = null;
	private float[] distanceWeights = null;
	private float norm = Float.NaN;
	
	public QueryConfig(){}
	
	public QueryConfig(QueryConfig qc){
		if(qc == null){
			return;
		}
		this.distance = qc.distance;
		this.distanceWeights = qc.distanceWeights;
		this.norm = qc.norm;
	}
	
	public Optional<Distance> getDistance(){
		return Optional.ofNullable(this.distance);
	}
	
	public Optional<float[]> getDistanceWeights(){
		return Optional.ofNullable(this.distanceWeights);
	}
	
	public Optional<Float> getNorm(){
		return Optional.ofNullable(Float.isNaN(norm) ? null : norm);
	}
	
	public QueryConfig setDistance(Distance distance){
		this.distance = distance;
		if(distance == Distance.euclidean){
			this.norm = 2f;
		}
		else if(distance == Distance.manhattan){
			this.norm = 1f;
		}
		else if(distance == Distance.chebyshev){
			this.norm = Float.POSITIVE_INFINITY;
		}
		return this;
	}
	
	public QueryConfig setDistanceWeights(float[] weights){
		this.distanceWeights = weights;
		return this;
	}
	
	public QueryConfig setNorm(float norm){
		this.norm = norm;
		if(Math.abs(norm - 2f) < 1e6f){
			this.distance = Distance.euclidean;
		}else if(Math.abs(norm - 1f) < 1e6f){
			this.distance = Distance.manhattan;
		}else if(Float.isInfinite(norm) && norm > 0){
			this.distance = Distance.chebyshev;
		}
		return this;
	}
	
	public QueryConfig clone(){
		return new QueryConfig(this);
	}
}
