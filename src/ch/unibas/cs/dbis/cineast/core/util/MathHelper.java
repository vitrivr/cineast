package ch.unibas.cs.dbis.cineast.core.util;

public class MathHelper {

	private MathHelper(){}
	
	public static final double SQRT2 = Math.sqrt(2);
	public static final float SQRT2_f = (float)SQRT2;
	
	public static double getScore(double dist, double maxDist){
		if(Double.isNaN(dist) || Double.isNaN(maxDist)){
			return 0d;
		}
		double score = 1d - (dist / maxDist);
		if(score > 1d){
			return 1d;
		}
		if(score < 0d){
			return 0d;
		}
		return score;
	}
	
	public static double euclideanDist(float[] v1, float[] v2){
		float dist = 0;
		for(int i = 0; i < Math.min(v1.length, v2.length); ++i){
			dist += (v1[i] - v2[i]) * (v1[i] - v2[i]);
		}
		return Math.sqrt(dist);
	}
	
}
