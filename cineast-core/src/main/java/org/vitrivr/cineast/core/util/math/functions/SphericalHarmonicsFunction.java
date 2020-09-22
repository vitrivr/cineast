package org.vitrivr.cineast.core.util.math.functions;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.apache.commons.math3.util.FastMath;

/**
 * @author rgasser
 * @version 1.0
 * @created 17.03.17
 */
public class SphericalHarmonicsFunction {

    /** Degree of the Spherical Harmonics function (l >= 0) */
    private final int l;

    /** Order of the Spherical Harmonics function (-l >= m >= l) */
    private final int m;

    /** Normalization factor for spherical harmonic function. */
    private final double Nlm;

    /** Associated Legendre Polynomial used to calculate the Spherical Harmonic function. */
    private final AssociatedLegendrePolynomial legendre;

    /**
     * Constructor for the SphericalHarmonicsFunction class.
     *
     * @param l Degree of the Spherical Harmonics function
     * @param m Order of the Spherical Harmonics function
     * @throws IllegalArgumentException if l or m do not satisfy the contraints.
     */
    public SphericalHarmonicsFunction(int l, int m) {

        if (l < 0) {
          throw new IllegalArgumentException("Spherical harmonics functions are not defined for l < 0.");
        }
        if (Math.abs(m) > l) {
          throw new IllegalArgumentException("Spherical harmonics functions are not defined for |m| > l.");
        }

        this.l = l;
        this.m = m;

        /* Calculate constants. */
        this.Nlm = SphericalHarmonicsFunction.getFactor(l,m);

        /* Instantiate associated legendre polynomial. */
        this.legendre = new AssociatedLegendrePolynomial(l,Math.abs(m));
    }


    /**
     * Compute the value of the function.
     *
     * @param theta Point at which the function value should be computed.
     * @param phi Point at which the function value should be computed.
     * @return the complex value of the function.
     */
   public Complex value(final double theta, final double phi) {
       double r = this.Nlm * this.legendre.value(FastMath.cos(theta));
       double arg = this.m * phi;
       return new Complex(r * FastMath.cos(arg), r * FastMath.sin(arg));
   }

    /**
     * Calculates and returns the normalisation factor for the Spherical Harmonics Function
     *
     * @param l Order
     * @param m
     * @return
     */
   public static double getFactor(int l, int m) {
       return FastMath.sqrt(((2*l + 1)/(4*Math.PI)) * ((double)CombinatoricsUtils.factorial(l-FastMath.abs(m)) / (double)CombinatoricsUtils.factorial(l+FastMath.abs(m))));
   }

    /**
     * Calculates and returns the number of coefficients that are expected for all spherical harmonics
     * up to max_l.
     *
     * @param max_l The maximum harmonic to consider.
     * @return
     */
   public static int numberOfCoefficients(int max_l, boolean onesided) {
        int number = 0;
        for (int l=0; l<=max_l; l++) {
            if (onesided) {
                for (int m=0; m<=l; m++) {
                    number += 1;
                }
            } else {
                for (int m=-l; m<=l; m++) {
                    number += 1;
                }
            }
        }
        return number;
   }
}
