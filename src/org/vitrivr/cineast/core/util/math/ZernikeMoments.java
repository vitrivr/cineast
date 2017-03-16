
//
//    This library is free software; you can redistribute it and/or
//    modify it under the terms of the GNU Lesser General Public
//    License as published by the Free Software Foundation; either
//    version 2.1 of the License, or (at your option) any later version.
//
//    This library is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//    Lesser General Public License for more details.
//
//    You should have received a copy of the GNU Lesser General Public
//    License along with this library; if not, write to the Free Software
//    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
//

package org.vitrivr.cineast.core.util.math;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.util.CombinatoricsUtils;

import java.util.ArrayList;

/**
 * This class can be used to compute Zernike Moments.
 *
 * The class was derived from the class with the same name found in the
 * Catalano-Framework (https://github.com/DiegoCatalano/Catalano-Framework).
 *
 * Minor modifications were made to make it compatible with the MathCommons
 * library used in Cineast.
 *
 * The code is published in the under the terms of the
 * GNU Lesser General Public (LGPL) 3.0 License.
 *
 * @author Diego Catalano, diego.catalano at live.com
 * @author Øivind Due Trier, oivind.due.trier at nr.no
 * @version 1.0
 * @created 15.03.17
 */
public final class ZernikeMoments {
    /**
     * Don't let anyone instantiate this class.
     */
    private ZernikeMoments() {}

    /**
     * Computes the Zernike radial polynomial, Rnm(p), in the definition of V(n,m,x,y).
     *
     * @param n Moment order.
     * @param m_in Moment order.
     * @param x X axis coordinate.
     * @param y Y axis coordinate.
     * @return Value of Rnm(p).
     */
    public static double radialPolynomial(int n, int m_in, double x, double y){

        long a; // (n-s)!
        int b; //   s!
        long c; // [(n+|m|)/2-s]!
        long d; // [(n-|m|)/2-s]!
        int sign;

        int m = Math.abs(m_in);

        if ((n - m) % 2 != 0){
            return 0;
        }

        double res = 0;
        if ((x * x + y * y) <= 1.0) {

            sign = 1;
            a = CombinatoricsUtils.factorial(n);
            b=1;
            c = CombinatoricsUtils.factorial(((n + m) / 2));
            d = CombinatoricsUtils.factorial(((n - m) / 2));

            // Before the loop is entered, all the integer variables
            // (sign, a, b, c, d) have their correct values for the
            // s=0 case.
            for(int s = 0; s <= (n - m) / 2; s++){
                res += sign*(a * 1.0 / (b * c * d)) * Math.pow((x * x + y * y),(n / 2.0) -s);
                // Now update the integer variables before the next
                // iteration of the loop.
                if (s < (n - m) / 2){
                    sign = -sign;
                    a /= (n - s);
                    b *= (s + 1);
                    c /= ((n + m) / 2 - s);
                    d /= ((n - m) / 2 - s);
                }
            }
        }
        return res;
    }

    /**
     * Computes the Zernike basis function V(n,m,x,y).
     *
     * @param n Moment order.
     * @param m Moment order.
     * @param x X axis coordinate.
     * @param y Y axis coordinate.
     * @return Complex number for V(n,m,x,y).
     */
    public static Complex zernikeBasisFunction(int n, int m, double x, double y) {
        if ((x * x + y * y) > 1.0) {
            return new Complex(0.0, 0.0);
        }
        else {
            double r = radialPolynomial(n,m,x,y);
            double arg = m * Math.atan2(y,x);
            double real = r * Math.cos(arg);
            double imag = r * Math.sin(arg);
            return new Complex(real,imag);
        }
    }

    /**
     * Compute zernike moments at n and m moments order.
     * Also compute the width, height, and centroid of the shape.
     * @param x X axis coordinates.
     * @param y Y axis coordinates.
     * @param nPoints Indicates the total number of points in the digitized shape.
     * @param n Moment order.
     * @param m Moment order.
     * @return Zernike moment.
     */
    public static Complex zernikeMoments(double[] x, double[] y, int nPoints, int n, int m){
        int diff = n-Math.abs(m);
        if ((n<0) || (Math.abs(m) > n) || (diff%2!=0)){
            throw new IllegalArgumentException("zer_mom: n="+n+", m="+m+", n-|m|="+diff);
        }
        double xmin = Double.MAX_VALUE;
        double ymin = Double.MAX_VALUE;
        double xmax = Double.MIN_VALUE;
        double ymax = Double.MIN_VALUE;

        for(int i = 0; i < nPoints; i++){
            xmin=Math.min(xmin,x[i]);
            xmax=Math.max(xmax,x[i]);
            ymin=Math.min(ymin,y[i]);
            ymax=Math.max(ymax,y[i]);
        }

        double w  = xmax-xmin;//width
        double h = ymax-ymin;//height
        double cx = xmin+w/2;
        double cy = ymin+h/2;
        return zernikeMoments(x, y, nPoints, w, h, cx, cy, n, m);
    }

    /**
     * Compute zernike moments at specified order.
     * @param x X axis coordinates.
     * @param y Y axis coordinates.
     * @param nPoints Indicates the total number of points in the digitized shape.
     * @param w Width of the bounding box of the shape.
     * @param h Height of the bounding box of the shape.
     * @param cx X axis of centroid point of the shape.
     * @param cy Y axis of centroid point of the shape.
     * @param n Moment order.
     * @param m Moment order.
     * @return Zernike moment.
     */
    public static Complex zernikeMoments(double[] x, double[] y, int nPoints, double w, double h, double cx, double cy, int n, int m){
        double i_0, j_0;
        double i_scale, j_scale;
        double X,Y;
        Complex v;
        //double isize, jsize;

        int diff = n-Math.abs(m);
        if ((n<0) || (Math.abs(m) > n) || (diff%2!=0)){
            throw new IllegalArgumentException("zer_mom: n="+n+", m="+m+", n-|m|="+diff);
        }
        //isize = ww;
        //jsize = hh;
        i_0 = cx;
        j_0 = cy;
        double radius = w/2;
        i_scale=Math.sqrt(2)*radius;
        radius=h/2;
        j_scale=Math.sqrt(2)*radius; //note we want to construct a circle to contain the rectangle
        Complex res = new Complex(0.0, 0.0);
        for(int i=0; i<nPoints; i++){
            X = (x[i]-i_0)/i_scale;
            Y = (y[i]-j_0)/j_scale;
            if (((X*X + Y*Y) <= 1.0)){// we ignore (x,y) not in the unit circle
                v = zernikeBasisFunction(n,m,X,Y);
                res = res.add(v);
            }
        }
        return res.multiply((n+1)/Math.PI);
    }

    /**
     * Compute the set of Zernike's moments up to the specified order.
     * Also compute the width, height, and centroid of the shape.
     * @param order Order.
     * @param x X axis coordinates.
     * @param y Y axis coordinates.
     * @param npoints Indicates the total number of points in the digitized shape.
     * @return Zernike`s moments.
     */
    public static Complex[] zernikeMoments(int order, double[] x, double[] y, int npoints){
        double xmin = Double.MAX_VALUE;
        double ymin = Double.MAX_VALUE;
        double xmax = Double.MIN_VALUE;
        double ymax = Double.MIN_VALUE;
        for(int i=0; i<npoints; i++){
            xmin=Math.min(xmin,x[i]);
            xmax=Math.max(xmax,x[i]);
            ymin=Math.min(ymin,y[i]);
            ymax=Math.max(ymax,y[i]);
        }
        double ww  = xmax-xmin;//width
        double hh = ymax-ymin;//height
        double cx = xmin+ww/2;
        double cy = ymin+hh/2;
        return zernikeMoments(order, x, y, npoints, ww, hh, cx, cy);
    }

    /**
     * Compute the set of Zernike's moments up to the specified order.
     * @param order Order.
     * @param x X axis coordinate.
     * @param y Y axis coordinate.
     * @param npoints Indicates the total number of points in the digitized shape.
     * @param w Width of the bounding box of the shape.
     * @param h Height of the bounding box of the shape.
     * @param cx X axis of centroid point of the shape.
     * @param cy Y axis of centroid point of the shape.
     * @return Set of Zernike's moments.
     */
    public static Complex[] zernikeMoments(int order, double[] x, double[] y, int npoints, double w, double h, double cx, double cy){
        ArrayList<Complex> list = new ArrayList<>(order);
        int ct=0;
        for(int n=0; n<=order; n++){
            for(int m=0; m<=n; m++){
                if((n-Math.abs(m))%2 == 0){
                    Complex v = zernikeMoments(x,y,npoints,w,h,cx,cy,n,m);
                    list.add(ct, v);
                    list.add(v);
                    ct++;
                }
            }
        }
        Complex[] mmts = new Complex[ct];
        for(int i=0; i<ct; i++){
            mmts[i]=list.get(i);
        }
        return mmts;
    }
}
