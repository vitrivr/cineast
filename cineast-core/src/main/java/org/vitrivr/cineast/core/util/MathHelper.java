package org.vitrivr.cineast.core.util;

import java.util.Arrays;

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
	 * Normalizes a float array with respect to the L2 (Euclidian) norm. The
	 * method will perform the normalisation in place.
	 *
	 * @param v Array that should be normalized.
	 * @return Normalized array.
	 */
	public static float[] normalizeL2InPlace(float[] v) {
		double norm = normL2(v);
		if (norm > 0.0f) {
			for (int i = 0; i < v.length; i++) {
				v[i] = (float) (v[i] / norm);
			}
			return v;
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
			if (v > 0.0 || v < 0.0) {
        return true;
      }
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
			if (v > 0.0 || v < 0.0) {
        return true;
      }
		}
		return false;
	}

	/**
	 * Checks whether the provided array is a zero array or not.
	 *
	 * @param array Array to check
	 * @return true if array is not zero, false otherwise.
	 */
	public static boolean checkNotNaN(double[] array) {
		for (double v : array) {
			if (Double.isNaN(v)) {
        return false;
      }
		}
		return true;
	}

	/**
	 * Checks whether the provided array is a zero array or not.
	 *
	 * @param array Array to check
	 * @return true if array is not zero, false otherwise.
	 */
	public static boolean checkNotNaN(float[] array) {
		for (float v : array) {
			if (Float.isNaN(v)) {
        return false;
      }
		}
		return true;
	}

	public static float limit(float val, float min, float max){
		return val > max ? max : (val < min ? min : val);
	}

	public static double limit(double val, double min, double max){
		return val > max ? max : (val < min ? min : val);
	}

	/**
	 * Kronecker-Delta function. Returns 1 if i=j and 0 otherwise.
	 *
	 * @param i Value of i
	 * @param j Value of j
	 * @return Result of Kronecker-Delta.
	 */
	public static int kronecker(int i, int j) {
		if (j == i) {
			return 1;
		} else {
			return 0;
		}
	}

	public static double[] softmax(double[] input){

		double[] params = Arrays.copyOf(input, input.length);

		double sum = 0;

		for (int i=0; i<params.length; i++) {
			params[i] = Math.exp(params[i]);
			sum += params[i];
		}

		if (Double.isNaN(sum) || sum < 0) {
			Arrays.fill(params, 1.0 / params.length);
		} else {
			for (int i=0; i<params.length; i++) {
				params[i] = params[i] / sum;
			}
		}

		return params;

	}

	public static ArgMaxResult argMax(double[] params) {
		int maxIndex = 0;
		for (int i=0; i<params.length; i++) {
			if (params[maxIndex] < params[i]) {
				maxIndex = i;
			}
		}
		return new ArgMaxResult(maxIndex, params[maxIndex]);
	}


	public static class ArgMaxResult {
		private int index;
		private double maxValue;

		public ArgMaxResult(int index, double maxValue) {
			this.index = index;
			this.maxValue = maxValue;
		}

		public int getIndex() {
			return index;
		}

		public double getMaxValue() {
			return maxValue;
		}
	}

	//based on https://jonisalonen.com/2012/converting-decimal-numbers-to-ratios/
	public static int[] toFraction(double d){
		if (d < 0){
			int[] fraction = toFraction(-d);
			fraction[0] *= -1;
			return fraction;
		}

		final double tolerance = 1.0E-6;
		double h1 = 1d, h2 = 0d;
		double k1 = 0d, k2 = 1d;
		double b = d;
		do {
			double a = Math.floor(b);
			double aux = h1;
			h1 = a * h1 + h2;
			h2 = aux;
			aux = k1;
			k1 = a * k1 + k2;
			k2 = aux;
			b = 1d / (b - a);
		} while (Math.abs(d - h1 / k1) > d * tolerance);

		return new int[]{(int) h1, (int) k1};
	}
}
