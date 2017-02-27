package org.vitrivr.cineast.core.util;

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

	/**
	 * Normalizes a float array with respect to the L2 (euclidian) norm. The
	 * method will return a new array and leave the original array unchanged.
	 *
	 * @param v Array that should be normalized.
	 * @return Normalized array.
	 */
	public static float[] normalizeL2(float[] v) {
		double norm = normL2(v);
		if (norm > 0.0f) {
			float[] vn = new float[v.length];
			for (int i = 0; i < v.length; i++) {
				vn[i] = (float) (v[i] / norm);
			}
			return vn;
		} else {
			return v;
		}
	}

	/**
	 * Normalizes a double array with respect to the L2 (euclidian) norm. The
	 * method will return a new array and leave the original array unchanged.
	 *
	 * @param v Array that should be normalized.
	 * @return Normalized array.
	 */
	public static double[] normalizeL2(double[] v) {
		double norm = normL2(v);
		if (norm > 0.0f) {
			double[] vn = new double[v.length];
			for (int i = 0; i < v.length; i++) {
				vn[i] = (float) (v[i] / norm);
			}
			return vn;
		} else {
			return v;
		}
	}


	/**
	 * Calculates and returns the L2 (euclidian) norm of a float array.
	 *
	 * @param v Float array for which to calculate the L2 norm.
	 * @return L2 norm
	 */
	public static double normL2(float[] v) {
		float dist = 0;
		for(int i = 0; i < v.length; i++){
			dist += Math.pow(v[i], 2);
		}
		return Math.sqrt(dist);
	}

	/**
	 * Calculates and returns the L2 (euclidian) norm of a double array.
	 *
	 * @param v Double array for which to calculate the L2 norm.
	 * @return L2 norm
	 */
	public static double normL2(double[] v) {
		float dist = 0;
		for(int i = 0; i < v.length; i++){
			dist += Math.pow(v[i], 2);
		}
		return Math.sqrt(dist);
	}


	/**
	 * Checks whether the provided array is a zero array or not.
	 *
	 * @param array Array to check
	 * @return true if array is not zero, false otherwise.
	 */
	public static boolean checkNotZero(double[] array) {
		for (double v : array) {
			if (v > 0.0 || v < 0.0) return true;
		}
		return false;
	}

    /**
     * Checks whether the provided array is a zero array or not.
     *
     * @param array Array to check
     * @return true if array is not zero, false otherwise.
     */
	public static boolean checkNotZero(float[] array) {
		for (float v : array) {
			if (v > 0.0 || v < 0.0) return true;
		}
		return false;
	}

	public static float limit(float val, float min, float max){
		val = val > max ? max : val;
		val = val < min ? min : val;
		return val;
	}
	
}
