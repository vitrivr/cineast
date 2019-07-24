package org.vitrivr.cineast.core.util.math;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.complex.Complex;
import org.vitrivr.cineast.core.util.math.functions.ZernikeBasisFunction;


/**
 * This class can be used to compute Zernike Moments.
 *
 * @author Ralph Gasser
 * @version 1.0
 * @created 15.03.17
 */
public final class ZernikeMoments {

    /** List of calculated Zernike Moments. */
    private final List<Complex> moments = new ArrayList<>();

    /** 2D data for which Zernike moment should be calculated. */
    private final double[][] f;

    /** Width of the original data (x-dimension). */
    private final int width;

    /** Height of the original data (y-dimension). */
    private final int height;

    /**
     * Default constructor for Zernike Moment.
     *
     * @param f 2D array containing the data for which the moments should be calculated.
     */
    public ZernikeMoments(double[][] f) {
        this.f = f;
        this.width = f.length;
        this.height = f[0].length;
    }

    /**
     * Computes the Zernike Moments up to order n.
     *
     * @param n Maximum order n.
     */
    public void compute(int n) {
        this.moments.clear();
        this.moments.addAll(ZernikeMoments.calculateZernikeMoments(n, this.f, this.width, this.height));
    }

    /**
     * Getter for the moments array. Must be computed prior
     * to calling this method. Otherwise the list will be empty.
     *
     * @return List of Zernike Moments.
     */
    public List<Complex> getMoments() {
        return this.moments;
    }

    /**
     * Compute Zernike moments at specified order.
     *
     * @param w Width of the bounding box of the shape.
     * @param h Height of the bounding box of the shape.
     * @param n 1st order of the moment.
     * @param m 2nd order of the moment.
     *
     * @return Zernike moment of data in f[][].
     */
    public static Complex calculateZernikeMoment(double[][] f, int w, int h, int n, int m){
        int diff = n-Math.abs(m);
        if ((n<0) || (Math.abs(m) > n) || (diff%2!=0)){
            throw new IllegalArgumentException("zer_mom: n="+n+", m="+m+", n-|m|="+diff);
        }

        final double c = -1;
        final double d = 1;


        ZernikeBasisFunction zernike = new ZernikeBasisFunction(n,m);
        Complex res = new Complex(0.0, 0.0);
        for (int i=0;i<w;i++){
            for (int j=0;j<h;j++) {
                Complex v = new Complex(c+(i*(d-c))/(w-1), d-(j*(d-c))/(h-1));
                res = res.add(zernike.value(v).conjugate().multiply(f[i][j]));
            }
        }

        return res.multiply((n+1)/Math.PI);
    }

    /**
     * Compute the set of Zernike's moments up to the specified order.
     *
     * @param order Maximum order n (1st order).
     * @param w Width of the bounding box of the shape.
     * @param h Height of the bounding box of the shape.
     *
     * @return List of Zernike moments.
     */
    public static List<Complex> calculateZernikeMoments(int order, double[][] f, int w, int h){
        ArrayList<Complex> list = new ArrayList<>(order);
        for(int n=0; n<=order; n++){
            for(int m=0; m<=n; m++){
                if((n-Math.abs(m))%2 == 0){
                    Complex v = calculateZernikeMoment(f,w,h,n,m);
                    list.add(v);
                }
            }
        }
        return list;
    }

    /**
     * Getter for width of the original image.
     *
     * @return Width of the original image.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Getter for height of the original image.
     *
     * @return Height of the original image.
     */
    public int getHeight() {
        return height;
    }

}
